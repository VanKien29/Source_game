package models.Combine.manifest;

import consts.ConstNpc;
import item.Item;
import java.util.ArrayList;
import java.util.List;
import models.Combine.CombineService;
import player.Player;
import server.Manager;
import services.InventoryService;
import services.ItemService;
import services.RewardService;
import services.Service;
import utils.Util;

/**
 *
 * @author CongHoan
 */
public class NangCapKichHoat {

    private static final int ITEM_THOI_VANG = 457;
    private static final int ITEM_XU_HORIZON = 1705;
    private static final int ITEM_DA_SKH_THUONG = 1742;
    private static final int ITEM_DA_SKH_VIP = 1743;
    private static final int REQUIRED_HUY_DIET = 3;
    private static final int REQUIRED_THAN_LINH = 5;
    private static final int REQUIRED_DA_THUONG = 10;
    private static final int REQUIRED_DA_VIP = 2;
    private static final int REQUIRED_THOI_VANG = 500;
    private static final int REQUIRED_XU_HORIZON = 500;
    private static final short[] RADAR_IDS = {57, 58, 59, 184, 185, 186, 187, 278, 279, 280, 281, 561};

    public static boolean isDoHuyDiet(Item item) {
        return item != null && item.isNotNullItem() && item.template.id >= 650 && item.template.id <= 662;
    }

    private static boolean isDoThienSu(Item item) {
        return item != null && item.isNotNullItem() && item.template.id >= 1048 && item.template.id <= 1062;
    }

    private static boolean isDoThanLinh(Item item) {
        return item != null && item.isNotNullItem() && item.template.id >= 555 && item.template.id <= 567;
    }

    private static boolean isDaSkhVip(Item item) {
        return item != null && item.isNotNullItem() && item.template.id == ITEM_DA_SKH_VIP;
    }

    public static void showInfoCombine(Player player) {
        CombineData data = getCombineData(player);
        if (!isReady(data)) {
            Service.gI().sendThongBaoOK(player, getMissingMessage(data));
            return;
        }

        String stoneText = data.useVipStone()
                ? REQUIRED_DA_VIP + " đá SKH VIP"
                : REQUIRED_DA_THUONG + " đá SKH thường";
        String npcSay = "Nâng cấp SKH VIP NEW sẽ đổi đồ Hủy Diệt đầu tiên thành SKH NEW cùng món.\n"
                + "Cần " + REQUIRED_HUY_DIET + " đồ Hủy Diệt bất kỳ, "
                + REQUIRED_THAN_LINH + " đồ Thần Linh bất kỳ, "
                + stoneText + ", " + REQUIRED_THOI_VANG + " thỏi vàng và "
                + REQUIRED_XU_HORIZON + " xu Horizon.";
        CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay,
                "Nâng cấp", "Từ chối");
    }

    public static void startCombine(Player player) {
        CombineData data = getCombineData(player);
        if (!isReady(data)) {
            Service.gI().sendThongBaoOK(player, getMissingMessage(data));
            return;
        }

        Item newItem = createNewSkhItem(player, data.huyDietItems.get(0));
        consumeItems(player, data);
        InventoryService.gI().addItemBag(player, newItem);

        CombineService.gI().sendEffectSuccessCombine(player);
        InventoryService.gI().sendItemBag(player);
        Service.gI().sendMoney(player);
        CombineService.gI().reOpenItemCombine(player);
    }

    private static Item createNewSkhItem(Player player, Item baseItem) {
        int planet = getPlanet(player, baseItem);
        int slot = baseItem.template.type;
        short itemId = getRandomTemplateId(planet, slot);
        Item newItem = ItemService.gI().createNewItem(itemId);
        RewardService.gI().initBaseOptionClothes(newItem.template.id, newItem.template.type, newItem.itemOptions);

        int[] opsrand = ItemService.gI().randOptionItemKichHoatNew(planet);
        for (int optionId : opsrand) {
            newItem.itemOptions.add(new Item.ItemOption(optionId, 0));
        }
        newItem.itemOptions.add(new Item.ItemOption(30, 0));
        return newItem;
    }

    private static short getRandomTemplateId(int planet, int slot) {
        if (slot >= 0 && slot <= 3) {
            int[] pool = Manager.LIST_DO_KHAC_4MON[planet][slot];
            return (short) pool[Util.nextInt(0, pool.length - 1)];
        }
        return RADAR_IDS[Util.nextInt(0, RADAR_IDS.length - 1)];
    }

    private static int getPlanet(Player player, Item item) {
        int gender = item.template.gender;
        if (gender < 0 || gender > 2) {
            gender = player.gender;
        }
        return gender;
    }

    private static CombineData getCombineData(Player player) {
        CombineData data = new CombineData();
        if (player.combine != null && player.combine.itemsCombine != null) {
            for (Item item : player.combine.itemsCombine) {
                if (isDoHuyDiet(item)) {
                    data.huyDietItems.add(item);
                } else if (isDoThanLinh(item)) {
                    data.thanLinhItems.add(item);
                } else if (data.daVipSelected == null && isDaSkhVip(item)) {
                    data.daVipSelected = item;
                }
            }
        }
        data.daThuong = InventoryService.gI().findItemBag(player, ITEM_DA_SKH_THUONG);
        data.thoiVang = InventoryService.gI().findItemBag(player, ITEM_THOI_VANG);
        data.xuHorizon = InventoryService.gI().findItemBag(player, ITEM_XU_HORIZON);
        return data;
    }

    private static boolean isReady(CombineData data) {
        return data.huyDietItems.size() >= REQUIRED_HUY_DIET
                && data.thanLinhItems.size() >= REQUIRED_THAN_LINH
                && hasEnoughStone(data)
                && hasEnough(data.thoiVang, REQUIRED_THOI_VANG)
                && hasEnough(data.xuHorizon, REQUIRED_XU_HORIZON);
    }

    private static boolean hasEnoughStone(CombineData data) {
        if (data.useVipStone()) {
            return hasEnough(data.daVipSelected, REQUIRED_DA_VIP);
        }
        return hasEnough(data.daThuong, REQUIRED_DA_THUONG);
    }

    private static boolean hasEnough(Item item, int quantity) {
        return item != null && item.isNotNullItem() && item.quantity >= quantity;
    }

    private static String getMissingMessage(CombineData data) {
        StringBuilder sb = new StringBuilder("Cần ")
                .append(REQUIRED_HUY_DIET).append(" đồ Hủy Diệt bất kỳ, ")
                .append(REQUIRED_THAN_LINH).append(" đồ Thần Linh bất kỳ, ")
                .append(REQUIRED_DA_THUONG).append(" đá SKH thường hoặc ")
                .append(REQUIRED_DA_VIP).append(" đá SKH VIP, ")
                .append(REQUIRED_THOI_VANG).append(" thỏi vàng và ")
                .append(REQUIRED_XU_HORIZON).append(" xu Horizon.");
        if (data.huyDietItems.size() < REQUIRED_HUY_DIET) {
            sb.append("\nThiếu ").append(REQUIRED_HUY_DIET - data.huyDietItems.size()).append(" đồ Hủy Diệt.");
        }
        if (data.thanLinhItems.size() < REQUIRED_THAN_LINH) {
            sb.append("\nThiếu ").append(REQUIRED_THAN_LINH - data.thanLinhItems.size()).append(" đồ Thần Linh.");
        }
        if (!hasEnoughStone(data)) {
            sb.append("\nThiếu đá SKH.");
        }
        if (!hasEnough(data.thoiVang, REQUIRED_THOI_VANG)) {
            int have = data.thoiVang == null ? 0 : data.thoiVang.quantity;
            sb.append("\nThiếu ").append(REQUIRED_THOI_VANG - have).append(" thỏi vàng.");
        }
        if (!hasEnough(data.xuHorizon, REQUIRED_XU_HORIZON)) {
            int have = data.xuHorizon == null ? 0 : data.xuHorizon.quantity;
            sb.append("\nThiếu ").append(REQUIRED_XU_HORIZON - have).append(" xu Horizon.");
        }
        return sb.toString();
    }

    private static void consumeItems(Player player, CombineData data) {
        consumeItems(player, data.huyDietItems, REQUIRED_HUY_DIET);
        consumeItems(player, data.thanLinhItems, REQUIRED_THAN_LINH);
        if (data.useVipStone()) {
            InventoryService.gI().subQuantityItemsBag(player, data.daVipSelected, REQUIRED_DA_VIP);
        } else {
            InventoryService.gI().subQuantityItemsBag(player, data.daThuong, REQUIRED_DA_THUONG);
        }
        InventoryService.gI().subQuantityItemsBag(player, data.thoiVang, REQUIRED_THOI_VANG);
        InventoryService.gI().subQuantityItemsBag(player, data.xuHorizon, REQUIRED_XU_HORIZON);
    }

    private static void consumeItems(Player player, List<Item> items, int quantity) {
        for (int i = 0; i < quantity; i++) {
            InventoryService.gI().subQuantityItemsBag(player, items.get(i), 1);
        }
    }

    private static class CombineData {

        private Item daVipSelected;
        private Item daThuong;
        private Item thoiVang;
        private Item xuHorizon;
        private final List<Item> huyDietItems = new ArrayList<>();
        private final List<Item> thanLinhItems = new ArrayList<>();

        private boolean useVipStone() {
            return daVipSelected != null;
        }
    }
}
