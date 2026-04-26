package models.Combine.manifest;

import consts.ConstNpc;
import item.Item;
import item.Item.ItemOption;
import java.util.Arrays;
import java.util.List;
import models.Combine.CombineService;
import player.Player;
import services.InventoryService;
import services.Service;
import utils.Util;

/**
 * @author BCHoan
 */
public class MoKhoaItem {

    public static void showInfoCombine(Player player) {
        if (InventoryService.gI().getCountEmptyBag(player) > 0) {
            if (player.combine.itemsCombine.size() == 2) {

                Item daHoangKim = null;
                Item itemKhoaGD = null;

                for (Item item_ : player.combine.itemsCombine) {
                    System.out.println("Item type: " + item_.template.type);

                    if (item_.template.id == 1723) {
                        daHoangKim = item_;
                    } else if (InventoryService.gI().haveOption(item_, 30)) {
                        itemKhoaGD = item_;
                    }
                }

                if (daHoangKim == null) {
                    CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                            "Cần có Đá Gia Hạn", "Đóng");
                    return;
                }

                if (itemKhoaGD == null) {
                    CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                            "Cần có trang bị bị khóa giao dịch", "Đóng");
                    return;
                }

                String npcSay = "|2|Hiện tại: " + itemKhoaGD.template.name + "\n|0|";
                for (ItemOption io : itemKhoaGD.itemOptions) {
                    npcSay += io.getOptionString() + "\n";
                }

                npcSay += "|2|Sau khi mở khóa, vật phẩm của bạn sẽ trở thành vật phẩm giao dịch được.\n"
                        + "|7|Tỉ lệ thành công: 30%\n"
                        + "Cần " + Util.numberToMoney(500_000_000_000L) + " vàng";

                CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE,
                        npcSay, "Mở khóa\n" + Util.numberToMoney(500_000_000_000L) + " vàng", "Từ chối");

            } else {
                CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                        "Cần có 2 vật phẩm:\n- Trang bị bị khóa giao dịch\n- Đá Gia Hạn", "Đóng");
            }
        } else {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                    "Hành trang cần ít nhất 1 ô trống", "Đóng");
        }
    }

    public static void startCombine(Player player) {
        if (player.combine.itemsCombine.size() != 2) {
            Service.gI().sendThongBao(player, "Thiếu nguyên liệu để mở khóa");
            return;
        }
        if (player.combine.itemsCombine.stream()
                .filter(item -> item.isNotNullItem() && InventoryService.gI().haveOption(item, 30))
                .count() != 1) {
            Service.gI().sendThongBao(player, "Thiếu trang bị bị khóa giao dịch");
            return;
        }
        if (player.combine.itemsCombine.stream()
                .filter(item -> item.isNotNullItem() && item.template.id == 1723)
                .count() != 1) {
            Service.gI().sendThongBao(player, "Cần có Đá Gia Hạn (mua tại Bà Hạt Mít)");
            return;
        }

        if (player.inventory.gold < 500_000_000_000L) {
            Service.gI().sendThongBao(player, "Cần 500 tỷ vàng để mở khóa");
            return;
        }
        Item daHoangKim = player.combine.itemsCombine.stream()
                .filter(item -> item.template.id == 1723)
                .findFirst().orElse(null);
        Item trangBiKhoaGD = player.combine.itemsCombine.stream()
                .filter(item -> InventoryService.gI().haveOption(item, 30))
                .findFirst().orElse(null);

        if (daHoangKim == null || trangBiKhoaGD == null) {
            Service.gI().sendThongBao(player, "Thiếu vật phẩm cần thiết");
            return;
        }
        player.inventory.gold -= 500_000_000_000L;
        if (Util.isTrue(30, 100)) {
            CombineService.gI().sendEffectSuccessCombine(player);
            ItemOption option_30 = null;
            for (ItemOption itopt : trangBiKhoaGD.itemOptions) {
                if (itopt.optionTemplate.id == 30) {
                    option_30 = itopt;
                    break;
                }
            }

            if (option_30 != null) {
                trangBiKhoaGD.itemOptions.remove(option_30);
                trangBiKhoaGD.itemOptions.add(new Item.ItemOption(73, 0));
            }

            Service.gI().sendThongBao(player, "Mở khóa thành công! Vật phẩm giờ có thể giao dịch.");
        } else {
            CombineService.gI().sendEffectFailCombine(player);
            Service.gI().sendThongBao(player, "Mở khóa thất bại! Đá Gia Hạn đã bị vỡ.");
        }
        InventoryService.gI().subQuantityItemsBag(player, daHoangKim, 1);
        InventoryService.gI().sendItemBag(player);
        Service.gI().sendMoney(player);
        player.combine.itemsCombine.clear();
        CombineService.gI().reOpenItemCombine(player);
    }
}
