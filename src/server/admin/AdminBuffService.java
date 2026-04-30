package server.admin;

import item.Item;
import jdbc.DBConnecter;
import jdbc.daos.NDVSqlFetcher;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import player.Player;
import server.Client;
import server.io.MySession;
import services.ItemService;
import services.Service;

public class AdminBuffService {

    private static final int MAX_MAIL_ITEM_QUANTITY = 99999;

    public static String buffMail(JSONObject body) {
        String target = stringValue(body.get("target")).trim();
        JSONArray items = asArray(body.get("items"));
        boolean notify = boolValue(body.get("notify"), true);

        if (target.isEmpty()) {
            return result(false, "TARGET_REQUIRED", "Thiếu tên nhân vật", 0);
        }
        if (items.isEmpty()) {
            return result(false, "ITEMS_REQUIRED", "Chưa có vật phẩm để buff", 0);
        }

        int sent = 0;
        if ("all".equalsIgnoreCase(target)) {
            for (Player player : NDVSqlFetcher.getAllPlayer()) {
                sent += buffMailForPlayer(player, items, notify);
            }
        } else {
            Player player = NDVSqlFetcher.loadPlayerByName(target);
            if (player == null) {
                return result(false, "PLAYER_NOT_FOUND", "Không tìm thấy nhân vật: " + target, 0);
            }
            sent = buffMailForPlayer(player, items, notify);
        }

        return sent > 0
                ? result(true, "MAIL_BUFFED", "Đã buff " + sent + " vật phẩm vào thư", sent)
                : result(false, "MAIL_BUFF_FAILED", "Không buff đươcj vật phẩm nào", 0);
    }

    public static String buffAccount(JSONObject body) {
        String targetType = stringValue(body.get("target_type")).trim();
        String target = stringValue(body.get("target")).trim();
        int cash = Math.max(0, intValue(body.get("cash"), 0));
        int danap = Math.max(0, intValue(body.get("danap"), 0));
        boolean setActive = boolValue(body.get("active"), false);
        boolean notify = boolValue(body.get("notify"), true);

        if (targetType.isEmpty()) {
            targetType = "player_name";
        }
        if (target.isEmpty()) {
            return result(false, "TARGET_REQUIRED", "Thiếu tài khoản nhân vật", 0);
        }
        if (cash == 0 && danap == 0 && !setActive) {
            return result(false, "NO_ACCOUNT_CHANGE", "Chưa có giá trị cash, danap hoặc mtv để cập nhật", 0);
        }

        AccountTarget account = findAccount(targetType, target);
        if (account == null) {
            return result(false, "ACCOUNT_NOT_FOUND", "Khong tim thay account phu hop", 0);
        }

        String sql = "update account set cash = cash + ?, danap = danap + ?"
                + (setActive ? ", active = 1" : "")
                + " where id = ?";
        try (Connection con = DBConnecter.getConnectionServer(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, cash);
            ps.setInt(2, danap);
            ps.setInt(3, account.id);
            int rows = ps.executeUpdate();
            if (rows <= 0) {
                return result(false, "ACCOUNT_BUFF_FAILED", "Không cập nhật được account", 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return result(false, "ACCOUNT_BUFF_ERROR", "Lỗi cập nhật account: " + e.getMessage(), 0);
        }

        syncOnlineAccount(account.id, cash, danap, setActive, notify);
        return result(true, "ACCOUNT_BUFFED", "Đã buff account #" + account.id, 1);
    }

    private static int buffMailForPlayer(Player player, JSONArray items, boolean notify) {
        if (player == null) {
            return 0;
        }

        int sent = 0;
        for (Object value : items) {
            if (!(value instanceof JSONObject)) {
                continue;
            }
            JSONObject row = (JSONObject) value;
            int tempId = intValue(row.get("temp_id"), -1);
            int quantity = Math.max(1, Math.min(MAX_MAIL_ITEM_QUANTITY, intValue(row.get("quantity"), 1)));
            if (tempId < 0) {
                continue;
            }

            try {
                Item item = ItemService.gI().createNewItem((short) tempId, quantity);
                JSONArray options = asArray(row.get("options"));
                for (Object optionValue : options) {
                    if (!(optionValue instanceof JSONObject)) {
                        continue;
                    }
                    JSONObject option = (JSONObject) optionValue;
                    int optionId = intValue(option.get("id"), -1);
                    int param = intValue(option.get("param"), 0);
                    if (optionId >= 0) {
                        item.itemOptions.add(new Item.ItemOption(optionId, param));
                    }
                }
                player.inventory.itemsMailBox.add(item);
                sent++;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (sent <= 0) {
            return 0;
        }
        if (!NDVSqlFetcher.updateMailBox(player)) {
            return 0;
        }
        Player online = Client.gI().getPlayerByName(player.name);
        if (notify && online != null) {
            Service.gI().sendThongBao(online, "Admin vừa gửi " + sent + " vào hòm thư");
        }
        return sent;
    }

    private static AccountTarget findAccount(String targetType, String target) {
        String sql;
        boolean numeric = "account_id".equalsIgnoreCase(targetType);
        if (numeric) {
            sql = "select id, username from account where id = ? limit 1";
        } else if ("username".equalsIgnoreCase(targetType)) {
            sql = "select id, username from account where username = ? limit 1";
        } else {
            sql = "select a.id, a.username from account a join player p on p.account_id = a.id where p.name = ? limit 1";
        }

        try (Connection con = DBConnecter.getConnectionServer(); PreparedStatement ps = con.prepareStatement(sql)) {
            if (numeric) {
                ps.setInt(1, intValue(target, 0));
            } else {
                ps.setString(1, target);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new AccountTarget(rs.getInt("id"), rs.getString("username"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void syncOnlineAccount(int accountId, int cash, int danap, boolean setActive, boolean notify) {
        Player player = Client.gI().getPlayerByUser(accountId);
        if (player == null || player.getSession() == null) {
            return;
        }
        MySession session = (MySession) player.getSession();
        session.cash += cash;
        session.danap += danap;
        if (setActive) {
            session.actived = true;
        }
        if (notify) {
            StringBuilder message = new StringBuilder("Admin vua buff account");
            if (cash != 0) {
                message.append(" cash +").append(cash);
            }
            if (danap != 0) {
                message.append(" danap +").append(danap);
            }
            if (setActive) {
                message.append(" và mở thành viên");
            }
            Service.gI().sendThongBao(player, message.toString());
        }
    }

    private static JSONArray asArray(Object value) {
        return value instanceof JSONArray ? (JSONArray) value : new JSONArray();
    }

    private static String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private static int intValue(Object value, int fallback) {
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (Exception e) {
            return fallback;
        }
    }

    private static boolean boolValue(Object value, boolean fallback) {
        if (value == null) {
            return fallback;
        }
        String raw = String.valueOf(value).trim();
        return "true".equalsIgnoreCase(raw) || "1".equals(raw) || "yes".equalsIgnoreCase(raw);
    }

    private static String result(boolean ok, String code, String message, int affected) {
        return "{"
                + "\"ok\":" + ok + ","
                + "\"code\":\"" + escape(code) + "\","
                + "\"message\":\"" + escape(message) + "\","
                + "\"affected\":" + affected
                + "}";
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\r", "\\r").replace("\n", "\\n");
    }

    private static class AccountTarget {
        final int id;
        final String username;

        AccountTarget(int id, String username) {
            this.id = id;
            this.username = username;
        }
    }
}
