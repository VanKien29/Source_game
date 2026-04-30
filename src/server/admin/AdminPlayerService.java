package server.admin;

import item.Item;
import java.util.ArrayList;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import player.Inventory;
import player.Player;
import server.Client;
import services.InventoryService;
import services.ItemService;
import services.Service;
import services.func.TransactionService;

public class AdminPlayerService {

    public static String syncInventory(JSONObject body) {
        long playerId = longValue(body.get("player_id"), 0);
        if (playerId <= 0) {
            return result(false, "PLAYER_ID_REQUIRED", "Thiếu player_id", false);
        }

        Player player = Client.gI().getPlayer(playerId);
        if (player == null) {
            return result(true, "PLAYER_OFFLINE", "Nhân vật đang offline, dữ liệu DB sẽ áp dụng khi đăng nhập lại", false);
        }

        try {
            List<Item> bodyItems = parseItems(stringValue(body.get("items_body")), true);
            List<Item> bagItems = parseItems(stringValue(body.get("items_bag")), false);
            List<Item> boxItems = parseItems(stringValue(body.get("items_box")), false);

            TransactionService.gI().cancelTrade(player);
            player.inventory.itemsBody.clear();
            player.inventory.itemsBody.addAll(bodyItems);
            player.inventory.itemsBag.clear();
            player.inventory.itemsBag.addAll(bagItems);
            player.inventory.itemsBox.clear();
            player.inventory.itemsBox.addAll(boxItems);

            InventoryService.gI().sortItems(player.inventory.itemsBag);
            InventoryService.gI().sendItemBody(player);
            InventoryService.gI().sendItemBag(player);
            InventoryService.gI().sendItemBox(player);
            Service.gI().point(player);
            Service.gI().Send_Caitrang(player);
            Service.gI().sendFlagBag(player);
            Service.gI().sendThongBao(player, "Admin vừa cập nhật hành trang của bạn");

            return result(true, "PLAYER_INVENTORY_SYNCED", "Đã đồng bộ hành trang cho nhân vật online", true);
        } catch (Exception e) {
            e.printStackTrace();
            return result(false, "PLAYER_INVENTORY_SYNC_ERROR", "Lỗi đồng bộ hành trang: " + e.getMessage(), true);
        }
    }

    private static List<Item> parseItems(String raw, boolean bodySlots) {
        List<Item> items = new ArrayList<>();
        Object parsed = JSONValue.parse(raw == null || raw.isBlank() ? "[]" : raw);
        JSONArray dataArray = parsed instanceof JSONArray ? (JSONArray) parsed : new JSONArray();
        int max = bodySlots ? Inventory.BODY_SLOT_COUNT : dataArray.size();

        for (int i = 0; i < max; i++) {
            Item item = ItemService.gI().createItemNull();
            if (i < dataArray.size()) {
                item = parseItem(dataArray.get(i));
            }
            items.add(item);
        }
        return items;
    }

    private static Item parseItem(Object rawSlot) {
        Object slotValue = rawSlot;
        if (slotValue instanceof String) {
            slotValue = JSONValue.parse(String.valueOf(slotValue));
        }
        if (!(slotValue instanceof JSONArray)) {
            return ItemService.gI().createItemNull();
        }

        JSONArray dataItem = (JSONArray) slotValue;
        if (dataItem.isEmpty()) {
            return ItemService.gI().createItemNull();
        }

        short tempId = shortValue(dataItem.get(0), (short) -1);
        if (tempId == -1) {
            Item empty = ItemService.gI().createItemNull();
            empty.createTime = longValue(dataItem.size() > 3 ? dataItem.get(3) : 0, 0);
            return empty;
        }

        Item item = ItemService.gI().createNewItem(tempId, intValue(dataItem.size() > 1 ? dataItem.get(1) : 1, 1));
        JSONArray options = parseOptions(dataItem.size() > 2 ? dataItem.get(2) : null);
        for (Object optValue : options) {
            Object parsed = optValue instanceof String ? JSONValue.parse(String.valueOf(optValue)) : optValue;
            if (!(parsed instanceof JSONArray)) {
                continue;
            }
            JSONArray opt = (JSONArray) parsed;
            if (opt.size() >= 2) {
                item.itemOptions.add(new Item.ItemOption(intValue(opt.get(0), 0), intValue(opt.get(1), 0)));
            }
        }
        item.createTime = longValue(dataItem.size() > 3 ? dataItem.get(3) : 0, System.currentTimeMillis());
        if (ItemService.gI().isOutOfDateTime(item)) {
            return ItemService.gI().createItemNull();
        }
        return item;
    }

    private static JSONArray parseOptions(Object rawOptions) {
        if (rawOptions instanceof JSONArray) {
            return (JSONArray) rawOptions;
        }
        Object parsed = JSONValue.parse(stringValue(rawOptions).replace("\"[", "[").replace("]\"", "]"));
        return parsed instanceof JSONArray ? (JSONArray) parsed : new JSONArray();
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

    private static long longValue(Object value, long fallback) {
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (Exception e) {
            return fallback;
        }
    }

    private static short shortValue(Object value, short fallback) {
        try {
            return Short.parseShort(String.valueOf(value));
        } catch (Exception e) {
            return fallback;
        }
    }

    private static String result(boolean ok, String code, String message, boolean online) {
        return "{"
                + "\"ok\":" + ok + ","
                + "\"code\":\"" + escape(code) + "\","
                + "\"message\":\"" + escape(message) + "\","
                + "\"online\":" + online
                + "}";
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\r", "\\r").replace("\n", "\\n");
    }
}
