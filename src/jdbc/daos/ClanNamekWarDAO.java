package jdbc.daos;

import clan.Clan;
import jdbc.DBConnecter;
import jdbc.NDVResultSet;
import item.Item;
import models.ClanNamekWar.ClanNamekWarMatch;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import player.Player;
import services.ItemService;
import utils.Logger;

public class ClanNamekWarDAO {

    private static boolean tablesEnsured;

    private ClanNamekWarDAO() {
    }

    public static void saveRegistration(int seasonId, Clan clan, Player leader) {
        try {
            ensureTables();
            DBConnecter.executeUpdate(
                    "INSERT INTO clan_namek_war_registration(season_id, clan_id, clan_name, leader_id, leader_name, register_time) "
                    + "VALUES (?, ?, ?, ?, ?, ?) "
                    + "ON DUPLICATE KEY UPDATE clan_name = VALUES(clan_name), leader_id = VALUES(leader_id), "
                    + "leader_name = VALUES(leader_name), register_time = VALUES(register_time)",
                    seasonId, clan.id, clan.name, leader.id, leader.name, System.currentTimeMillis());
        } catch (Exception e) {
            Logger.logException(ClanNamekWarDAO.class, e, "Loi saveRegistration ClanNamekWar");
        }
    }

    public static void saveMatchResult(int seasonId, ClanNamekWarMatch match) {
        try {
            ensureTables();
            Clan winner = match.getWinner();
            DBConnecter.executeUpdate(
                    "INSERT INTO clan_namek_war_match(season_id, round, clan_a_id, clan_b_id, damage_a, damage_b, winner_clan_id, finish_time) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                    seasonId, match.round, match.clanA.id, match.clanB == null ? 0 : match.clanB.id,
                    match.damageClanA, match.damageClanB,
                    winner == null ? 0 : winner.id, System.currentTimeMillis());
        } catch (Exception e) {
            Logger.logException(ClanNamekWarDAO.class, e, "Loi saveMatchResult ClanNamekWar");
        }
    }

    public static boolean giveRewardToPlayer(Player player, int rewardType) {
        if (player == null || player.isBot || player.inventory == null || player.inventory.itemsMailBox == null) {
            return false;
        }
        boolean hasReward = false;
        NDVResultSet rs = null;
        try {
            ensureTables();
            rs = DBConnecter.executeQuery(
                    "SELECT item_id, quantity, options FROM clan_namek_war_reward_config "
                    + "WHERE reward_type = ? AND active = 1 ORDER BY id ASC",
                    rewardType);
            while (rs.next()) {
                short itemId = rs.getShort("item_id");
                int quantity = Math.max(1, rs.getInt("quantity"));
                Item item = ItemService.gI().createNewItem(itemId);
                item.quantity = quantity;
                addRewardOptions(item, rs.getString("options"));
                player.inventory.itemsMailBox.add(item);
                hasReward = true;
            }
            return hasReward && NDVSqlFetcher.updateMailBox(player);
        } catch (Exception e) {
            Logger.logException(ClanNamekWarDAO.class, e, "Loi giveRewardToPlayer ClanNamekWar");
            return false;
        } finally {
            if (rs != null) {
                rs.dispose();
            }
        }
    }

    private static void addRewardOptions(Item item, String optionsJson) {
        JSONArray options = (JSONArray) JSONValue.parse(optionsJson == null || optionsJson.isEmpty() ? "[]" : optionsJson);
        if (options == null) {
            return;
        }
        for (Object option : options) {
            int optionId;
            int param;
            if (option instanceof JSONObject) {
                JSONObject obj = (JSONObject) option;
                optionId = Integer.parseInt(String.valueOf(obj.get("id")));
                param = Integer.parseInt(String.valueOf(obj.get("param")));
            } else {
                JSONArray arr = (JSONArray) JSONValue.parse(String.valueOf(option));
                if (arr == null || arr.size() < 2) {
                    continue;
                }
                optionId = Integer.parseInt(String.valueOf(arr.get(0)));
                param = Integer.parseInt(String.valueOf(arr.get(1)));
            }
            item.itemOptions.add(new Item.ItemOption(optionId, param));
        }
    }

    private static synchronized void ensureTables() throws Exception {
        if (tablesEnsured) {
            return;
        }
        DBConnecter.executeUpdate(
                "CREATE TABLE IF NOT EXISTS clan_namek_war_registration ("
                + "id INT NOT NULL AUTO_INCREMENT,"
                + "season_id INT NOT NULL,"
                + "clan_id INT NOT NULL,"
                + "clan_name VARCHAR(255) NOT NULL,"
                + "leader_id BIGINT NOT NULL,"
                + "leader_name VARCHAR(255) NOT NULL,"
                + "register_time BIGINT NOT NULL,"
                + "PRIMARY KEY (id),"
                + "UNIQUE KEY uniq_season_clan (season_id, clan_id)"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
        DBConnecter.executeUpdate(
                "CREATE TABLE IF NOT EXISTS clan_namek_war_match ("
                + "id INT NOT NULL AUTO_INCREMENT,"
                + "season_id INT NOT NULL,"
                + "round INT NOT NULL,"
                + "clan_a_id INT NOT NULL,"
                + "clan_b_id INT NOT NULL,"
                + "damage_a BIGINT NOT NULL DEFAULT 0,"
                + "damage_b BIGINT NOT NULL DEFAULT 0,"
                + "winner_clan_id INT NOT NULL DEFAULT 0,"
                + "finish_time BIGINT NOT NULL,"
                + "reward_state TINYINT NOT NULL DEFAULT 0,"
                + "PRIMARY KEY (id),"
                + "KEY idx_season_round (season_id, round),"
                + "KEY idx_winner (winner_clan_id)"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
        DBConnecter.executeUpdate(
                "CREATE TABLE IF NOT EXISTS clan_namek_war_reward_config ("
                + "id INT NOT NULL AUTO_INCREMENT,"
                + "reward_type TINYINT NOT NULL COMMENT '1: thang, 2: thua, 3: hoa',"
                + "item_id SMALLINT NOT NULL,"
                + "quantity INT NOT NULL DEFAULT 1,"
                + "options TEXT NULL COMMENT 'JSON options, vi du: [{\"id\":30,\"param\":0}]',"
                + "active TINYINT NOT NULL DEFAULT 1,"
                + "PRIMARY KEY (id),"
                + "KEY idx_reward_type (reward_type, active)"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
        tablesEnsured = true;
    }
}
