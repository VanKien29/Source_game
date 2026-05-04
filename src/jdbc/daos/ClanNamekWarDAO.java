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

    private ClanNamekWarDAO() {
    }

    public static void saveRegistration(int seasonId, Clan clan, Player leader) {
        try {
            DBConnecter.executeUpdate(
                    "INSERT INTO clan_namek_war_registration(season_id, clan_id, clan_name, leader_id, leader_name, register_time) "
                    + "VALUES (?, ?, ?, ?, ?, ?)",
                    seasonId, clan.id, clan.name, leader.id, leader.name, System.currentTimeMillis());
        } catch (Exception e) {
            Logger.logException(ClanNamekWarDAO.class, e, "Loi saveRegistration ClanNamekWar");
        }
    }

    public static void saveMatchResult(int seasonId, ClanNamekWarMatch match) {
        try {
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
}
