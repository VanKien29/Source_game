package models.Combine.manifest;

import consts.ConstNpc;
import item.Item;
import item.Item.ItemOption;
import models.Combine.CombineService;
import player.Player;
import services.InventoryService;
import services.ItemService;
import services.Service;
import utils.Util;

/**
 *
 * @author CongHoan
 */
public class DoiVeHDCongHoan {

    // Hiển thị thông tin ghép đồ
    public static void showInfoCombine(Player player) {
        if (player.combine.itemsCombine.size() != 1) {
            Service.gI().sendThongBao(player, "Thiếu nguyên liệu");
            return;
        }
        Item DTL = null;
        for (Item item : player.combine.itemsCombine) {
            if (item.isDTL()) {
                DTL = item;
            }
        }
        if (DTL == null) {
            Service.gI().sendThongBao(player, "Không đủ vật phẩm nâng cấp!");
            return;
        }
        if (player.inventory.gold < 500_000_000) {
            Service.gI().sendThongBao(player, "Không đủ vàng!");
            CombineService.gI().reOpenItemCombine(player);
            return;
        }
        String npcSay = """
                        |2|Con có muốn nâng các món nguyên liệu ?
                        |7| 1 món đồ thần bất kì
                         + 500 triệu vàng""";

        CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay, "Nâng cấp", "Từ chối");
    }

    // Bắt đầu thực hiện ghép
    public static void startCombine(Player player) {
        if (player.combine.itemsCombine.size() != 1) {
            Service.gI().sendThongBao(player, "Thiếu nguyên liệu");
            return;
        }
        Item DTL = null;
        for (Item item : player.combine.itemsCombine) {
            if (item.isDTL()) {
                DTL = item;
            }
        }
        if (DTL == null) {
            Service.gI().sendThongBao(player, "Không đủ vật phẩm nâng cấp!");
            return;
        }
        if (player.inventory.gold < 500_000_000) {
            Service.gI().sendThongBao(player, "Không đủ vàng!");
            CombineService.gI().reOpenItemCombine(player);
            return;
        }

        int ManhHD = -1;
        switch (DTL.template.id) {
            case 555, 557, 559 -> ManhHD = 1792; // Áo
            case 556, 558, 560 -> ManhHD = 1795; // Quần
            case 561 -> ManhHD = 1804; // Nhẫn
            case 562, 564, 566 -> ManhHD = 1798; // Găng
            case 563, 565, 567 -> ManhHD = 1801; // Giày
        }

        if (ManhHD != -1) {
            Item newItem = ItemService.gI().createNewItem((short) ManhHD);
            InventoryService.gI().addItemBag(player, newItem);
            Service.gI().sendThongBao(player, "|7|Bạn nhận được " + newItem.template.name);
            InventoryService.gI().subQuantityItemsBag(player, DTL, 1);
            player.inventory.gold -= 500_000_000;
            CombineService.gI().sendEffectSuccessCombine(player);
            InventoryService.gI().sendItemBag(player);
            Service.gI().sendMoney(player);
            CombineService.gI().reOpenItemCombine(player);
        } else {
            Service.gI().sendThongBao(player, "Không đủ vật phẩm nâng cấp!");
        }
    }
}
