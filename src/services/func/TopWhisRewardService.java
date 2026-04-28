package services.func;

import item.Item;
import jdbc.DBConnecter;
import jdbc.daos.NDVSqlFetcher;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import player.Player;
import server.Client;
import services.ItemService;
import services.Service;
import utils.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TopWhisRewardService {

    private static final String REWARD_TABLE = "moc_whis_top";
    private static final String SQL_LOAD_REWARDS = "SELECT id, detail FROM " + REWARD_TABLE + " ORDER BY id ASC";

    private static TopWhisRewardService instance;

    private boolean tableInitialized;

    public static TopWhisRewardService gI() {
        if (instance == null) {
            instance = new TopWhisRewardService();
        }
        return instance;
    }

    public void checkAndRewardWeekly() {
        Calendar now = Calendar.getInstance();
        if (now.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY
                || now.get(Calendar.HOUR_OF_DAY) != 23
                || now.get(Calendar.MINUTE) != 59) {
            return;
        }
        rewardWeeklyTopWhis(false, null);
    }

    public boolean rewardWeeklyTopWhisByAdmin(Player admin) {
        return rewardWeeklyTopWhis(true, admin);
    }

    private synchronized boolean rewardWeeklyTopWhis(boolean manual, Player actor) {
        ensureRewardTable();
        try (Connection con = DBConnecter.getConnectionServer()) {
            List<RewardRank> rewardRanks = loadRewardRanks(con);
            if (rewardRanks.isEmpty()) {
                if (manual && actor != null) {
                    Service.gI().sendThongBao(actor, "Top Whis chưa có cấu hình quà trong bảng " + REWARD_TABLE);
                }
                return false;
            }

            int maxRank = rewardRanks.get(rewardRanks.size() - 1).rank;
            List<WhisTopEntry> standings = loadCurrentStandings(con, maxRank);
            if (standings.isEmpty()) {
                if (manual && actor != null) {
                    Service.gI().sendThongBao(actor, "Top Whis hiện đang trống");
                }
                return false;
            }

            Map<Integer, WhisTopEntry> standingByRank = new HashMap<>();
            for (WhisTopEntry entry : standings) {
                standingByRank.put(entry.rank, entry);
            }

            int rewarded = 0;
            Map<Long, Integer> topWhisEffectRanks = new HashMap<>();
            for (RewardRank rewardRank : rewardRanks) {
                WhisTopEntry entry = standingByRank.get(rewardRank.rank);
                if (entry == null) {
                    continue;
                }
                if (giveReward(entry, rewardRank.detail)) {
                    rewarded++;
                    if (entry.rank <= 3) {
                        topWhisEffectRanks.put(entry.playerId, entry.rank);
                    }
                }
            }

            if (rewarded <= 0) {
                if (manual && actor != null) {
                    Service.gI().sendThongBao(actor, "Không có người chơi nào nhận được quà Top Whis");
                }
                return false;
            }

            long rewardTime = System.currentTimeMillis();
            resetWhisTop(con, rewardTime, topWhisEffectRanks);
            syncOnlinePlayersAfterReset(rewardTime, topWhisEffectRanks);
            TopService.refreshWhisTop();
            Service.gI().sendThongBaoAllPlayer("Top Whis tuần đã được trao quà và reset");
            Logger.success("Top Whis weekly reward completed. Rewarded players: " + rewarded + "\n");

            if (manual && actor != null) {
                Service.gI().sendThongBao(actor, "Đã trao quà Top Whis cho " + rewarded + " người và reset BXH");
            }
            return true;
        } catch (Exception e) {
            Logger.logException(TopWhisRewardService.class, e, "Error rewarding weekly Whis top");
            if (manual && actor != null) {
                Service.gI().sendThongBao(actor, "Trao quà Top Whis thất bại, xem log server");
            }
            return false;
        }
    }

    private void ensureRewardTable() {
        if (tableInitialized) {
            return;
        }
        synchronized (this) {
            if (tableInitialized) {
                return;
            }
            try (Connection con = DBConnecter.getConnectionServer();
                    PreparedStatement ps = con.prepareStatement(
                            "CREATE TABLE IF NOT EXISTS " + REWARD_TABLE
                            + " (id INT NOT NULL PRIMARY KEY, detail LONGTEXT NOT NULL)")) {
                ps.executeUpdate();
                tableInitialized = true;
            } catch (Exception e) {
                Logger.logException(TopWhisRewardService.class, e, "Error creating " + REWARD_TABLE);
            }
        }
    }

    private List<RewardRank> loadRewardRanks(Connection con) throws Exception {
        List<RewardRank> rewards = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(SQL_LOAD_REWARDS);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                RewardRank rewardRank = new RewardRank();
                rewardRank.rank = rs.getInt("id");
                rewardRank.detail = rs.getString("detail");
                rewards.add(rewardRank);
            }
        }
        return rewards;
    }

    private List<WhisTopEntry> loadCurrentStandings(Connection con, int limit) throws Exception {
        List<WhisTopEntry> standings = new ArrayList<>();
        String query = "SELECT player.id, player.name,"
                + " CAST(JSON_EXTRACT(data_luyentap, '$[5]') AS UNSIGNED) AS top,"
                + " CAST(JSON_EXTRACT(data_luyentap, '$[6]') AS UNSIGNED) AS time"
                + " FROM player INNER JOIN account ON account.id = player.account_id"
                + " WHERE account.ban = 0"
                + " AND CAST(JSON_EXTRACT(data_luyentap, '$[5]') AS UNSIGNED) > 10"
                + " ORDER BY CAST(JSON_EXTRACT(data_luyentap, '$[5]') AS UNSIGNED) DESC,"
                + " CAST(JSON_EXTRACT(data_luyentap, '$[6]') AS UNSIGNED) ASC"
                + " LIMIT " + limit;
        try (PreparedStatement ps = con.prepareStatement(query);
                ResultSet rs = ps.executeQuery()) {
            int rank = 1;
            while (rs.next()) {
                WhisTopEntry entry = new WhisTopEntry();
                entry.rank = rank++;
                entry.playerId = rs.getLong("id");
                entry.playerName = rs.getString("name");
                entry.topLevel = rs.getInt("top");
                entry.time = rs.getInt("time");
                standings.add(entry);
            }
        }
        return standings;
    }

    private boolean giveReward(WhisTopEntry entry, String rewardDetail) {
        JSONArray dataArray = parseRewardArray(rewardDetail);
        if (dataArray == null || dataArray.isEmpty()) {
            return false;
        }
        Player player = NDVSqlFetcher.loadPlayerByID(entry.playerId);
        if (player == null) {
            return false;
        }
        try {
            for (Object obj : dataArray) {
                JSONObject dataObject = (JSONObject) JSONValue.parse(String.valueOf(obj));
                if (dataObject == null) {
                    continue;
                }
                int tempId = Integer.parseInt(String.valueOf(dataObject.get("temp_id")));
                int quantity = Integer.parseInt(String.valueOf(dataObject.get("quantity")));
                Item item = ItemService.gI().createNewItem((short) tempId);
                item.quantity = quantity;
                JSONArray optionsArray = (JSONArray) dataObject.get("options");
                if (optionsArray != null) {
                    for (Object option : optionsArray) {
                        JSONObject optionObject = (JSONObject) option;
                        if (optionObject == null) {
                            continue;
                        }
                        int optionId = Integer.parseInt(String.valueOf(optionObject.get("id")));
                        int param = Integer.parseInt(String.valueOf(optionObject.get("param")));
                        item.itemOptions.add(new Item.ItemOption(optionId, param));
                    }
                }
                player.inventory.itemsMailBox.add(item);
            }
            if (NDVSqlFetcher.updateMailBox(player)) {
                if (Client.gI().getPlayer(player.id) != null) {
                    Service.gI().sendThongBao(player, "Bạn vừa nhận quà Top Whis về mail thành công");
                }
                Logger.success("Rewarded Top Whis rank " + entry.rank + " for " + entry.playerName
                        + " (LV " + entry.topLevel + ", " + entry.time + " ms)\n");
                return true;
            }
        } catch (Exception e) {
            Logger.logException(TopWhisRewardService.class, e, "Error rewarding player " + entry.playerName);
        }
        return false;
    }

    private JSONArray parseRewardArray(String rewardDetail) {
        if (rewardDetail == null || rewardDetail.isBlank()) {
            return null;
        }
        Object parsed = JSONValue.parse(rewardDetail);
        if (parsed instanceof JSONArray array) {
            return array;
        }
        return null;
    }

    private void resetWhisTop(Connection con, long rewardTime, Map<Long, Integer> topWhisEffectRanks) throws Exception {
        clearTopWhisEffects(con);
        String query = "UPDATE player SET data_luyentap = JSON_SET(data_luyentap,"
                + " '$[8]', CAST(JSON_EXTRACT(data_luyentap, '$[5]') AS UNSIGNED),"
                + " '$[9]', ?,"
                + " '$[5]', 0,"
                + " '$[6]', 0,"
                + " '$[7]', 0)"
                + " WHERE CAST(JSON_EXTRACT(data_luyentap, '$[5]') AS UNSIGNED) > 0";
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setLong(1, rewardTime);
            ps.executeUpdate();
        }
        applyTopWhisEffects(con, topWhisEffectRanks);
    }

    private void clearTopWhisEffects(Connection con) throws Exception {
        String query = "UPDATE player SET data_luyentap = JSON_SET(data_luyentap, '$[10]', 0)"
                + " WHERE COALESCE(CAST(JSON_EXTRACT(data_luyentap, '$[10]') AS UNSIGNED), 0) > 0";
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.executeUpdate();
        }
    }

    private void applyTopWhisEffects(Connection con, Map<Long, Integer> topWhisEffectRanks) throws Exception {
        if (topWhisEffectRanks.isEmpty()) {
            return;
        }
        String query = "UPDATE player SET data_luyentap = JSON_SET(data_luyentap, '$[10]', ?) WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(query)) {
            for (Map.Entry<Long, Integer> entry : topWhisEffectRanks.entrySet()) {
                ps.setInt(1, entry.getValue());
                ps.setLong(2, entry.getKey());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void syncOnlinePlayersAfterReset(long rewardTime, Map<Long, Integer> topWhisEffectRanks) {
        for (Player player : Client.gI().getPlayers()) {
            if (player == null || player.traning == null) {
                continue;
            }
            if (player.traning.getTopWhis() > 0) {
                Service.gI().removeTopWhisEffect(player);
            }
            if (player.traning.getTop() > 0) {
                player.traning.setLastTop(player.traning.getTop());
                player.traning.setLastRewardTime(rewardTime);
                player.traning.setTop(0);
                player.traning.setTime(0);
                player.traning.setLastTime(0);
            }
            int topWhisRank = topWhisEffectRanks.getOrDefault(player.id, 0);
            player.traning.setTopWhis(topWhisRank);
            if (topWhisRank > 0) {
                Service.gI().sendTopWhisEffect(player);
            }
        }
    }

    private static class RewardRank {

        private int rank;
        private String detail;
    }

    private static class WhisTopEntry {

        private int rank;
        private long playerId;
        private String playerName;
        private int topLevel;
        private int time;
    }
}
