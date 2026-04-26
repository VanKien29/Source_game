package models.Combine.manifest;

import player.Player;

import consts.ConstNpc;
import item.Item;
import item.Item.ItemOption;
import models.Combine.CombineService;
import player.Player;
import services.InventoryService;
import services.Service;
import utils.Util;

/**
 *
 * @author Administrator
 */
public class GiaHanVatPham {

    public static void showInfoCombine(Player player) {
        if (player.combine.itemsCombine.size() == 2) {
            Item thegh = null;
            Item itemGiahan = null;

            for (Item item_ : player.combine.itemsCombine) {
                if (item_ == null || !item_.isNotNullItem()) {
                    continue;
                }
                if (item_.template.id == 1723) {
                    thegh = item_;
                } else if (item_.isTrangBiHSD()) {
                    itemGiahan = item_;
                }
            }

            if (thegh == null || itemGiahan == null) {
                Service.gI().sendThongBaoOK(player, "Cần 1 trang bị có hạn sử dụng và 1 Đá Gia Hạn");
                return;
            }

            if (!itemGiahan.isTrangBiHSD()) {
                Service.gI().sendThongBaoOK(player, "Trang bị này không phải trang bị có Hạn Sử Dụng");
                return;
            }

            String npcSay = "Trang bị được gia hạn \"" + itemGiahan.template.name + "\"\n|2|";
            for (Item.ItemOption io : itemGiahan.itemOptions) {
                npcSay += io.getOptionString() + "\n";
            }
            npcSay += "\n|0|Sau khi gia hạn + ~ 3 - 7 ngày\n";
            npcSay += "|0|Tỉ lệ thành công: 100%\n";

            if (player.inventory.gold >= 500_000_000_000L) {
                npcSay += "|2|Cần 500 tỷ vàng";
                CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay,
                        "Gia hạn", "Từ chối");
            } else {
                long thieu = 500_000_000_000L - player.inventory.gold;
                CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                        "Bạn còn thiếu " + thieu + " Vàng");
            }
        } else {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                    "Hành trang cần ít nhất 1 chỗ trống");
        }
    }

    public static void startCombine(Player player) {
        if (player.combine.itemsCombine.size() != 2) {
            Service.gI().sendThongBao(player, "Thiếu nguyên liệu");
            return;
        }

        if (player.combine.itemsCombine.stream()
                .filter(item -> item.isNotNullItem() && item.isTrangBiHSD())
                .count() != 1) {
            Service.gI().sendThongBao(player, "Thiếu trang bị HSD");
            return;
        }

        if (player.combine.itemsCombine.stream()
                .filter(item -> item.isNotNullItem() && item.template.id == 1723)
                .count() != 1) {
            Service.gI().sendThongBao(player, "Thiếu Đá Gia Hạn");
            return;
        }

        if (InventoryService.gI().getCountEmptyBag(player) <= 0) {
            Service.gI().sendThongBao(player, "Bạn phải có ít nhất 1 ô trống hành trang");
            return;
        }

        Item thegh = player.combine.itemsCombine.stream()
                .filter(item -> item.isNotNullItem() && item.template.id == 1723)
                .findFirst().orElse(null);
        Item tbiHSD = player.combine.itemsCombine.stream()
                .filter(item -> item.isNotNullItem() && item.isTrangBiHSD())
                .findFirst().orElse(null);

        if (thegh == null) {
            Service.gI().sendThongBao(player, "Thiếu Đá Gia Hạn");
            return;
        }
        if (tbiHSD == null) {
            Service.gI().sendThongBao(player, "Thiếu trang bị HSD");
            return;
        }

        boolean hasValidHsd = false;
        for (ItemOption itopt : tbiHSD.itemOptions) {
            if (itopt != null && itopt.optionTemplate.id == 93 && itopt.param >= 0) {
                hasValidHsd = true;
                break;
            }
        }
        if (!hasValidHsd) {
            Service.gI().sendThongBao(player, "Không Phải Trang Bị Có HSD");
            return;
        }
        long cost = 500_000_000_000L;
        if (player.inventory.gold < cost) {
            long thieu = cost - player.inventory.gold;
            Service.gI().sendThongBao(player, "Bạn còn thiếu " + Util.numberToMoney(thieu) + " vàng");
            Service.gI().sendMoney(player);
            return;
        }
        player.inventory.gold -= cost;
        Service.gI().sendMoney(player);

        if (Util.isTrue(5, 100)) {
            for (ItemOption itopt : tbiHSD.itemOptions) {
                if (itopt != null && itopt.optionTemplate.id == 93) {
                    itopt.param += Util.nextInt(3, 7);
                    break;
                }
            }
        } else {
            for (ItemOption itopt : tbiHSD.itemOptions) {
                if (itopt != null && itopt.optionTemplate.id == 93) {
                    itopt.param += 1;
                    break;
                }
            }
        }

        CombineService.gI().sendEffectSuccessCombine(player);
        InventoryService.gI().subQuantityItemsBag(player, thegh, 1);
        InventoryService.gI().sendItemBag(player);
        CombineService.gI().reOpenItemCombine(player);
    }
}
