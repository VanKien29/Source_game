package models.Combine.manifest;

import player.Player;
import consts.ConstNpc;
import item.Item;
import item.Item.ItemOption;
import models.Combine.CombineService;
import services.InventoryService;
import services.Service;
import utils.Util;

/**
 *
 * @author BCHoan
 */
public class TayGiapLuyenTap {

    public static void showInfoCombine(Player player) {
        if (InventoryService.gI().getCountEmptyBag(player) > 0) {
            if (player.combine.itemsCombine.size() == 2) {
                Item daHacHoa = null;
                Item itemHacHoa = null;
                for (Item item_ : player.combine.itemsCombine) {
                    if (item_.template.id == 1708) { 
                        daHacHoa = item_;
                    } else if (item_.isTrangBiHacHoa()) {
                        itemHacHoa = item_;
                    }
                }

                if (daHacHoa == null) {
                    CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                            "Bạn còn thiếu Ngọc Tẩy", "Đóng");
                    return;
                }
                if (itemHacHoa == null) {
                    CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                            "Cần Giáp luyện tập có chỉ số đặc biệt\n",
                            "Đóng");
                    return;
                }

                String npcSay = "|2|Hiện tại " + itemHacHoa.template.name + "\n|0|";
                for (ItemOption io : itemHacHoa.itemOptions) {
                    if (io.optionTemplate.id != 72) {
                        npcSay += io.getOptionString() + "\n";
                    }
                }

                npcSay += "|2|Sau khi tẩy sẽ xóa các chỉ số pháp sư:\n"
                        + "Tấn công, HP, KI, Giáp\n"
                        + "|7|Chỉ áp dụng cho trang bị: Giáp luyện tập\n"
                        + "|7|Tỉ lệ thành công: 35%\n"
                        + "Cần " + Util.numberToMoney(50_000_000_000L) + " vàng";

                CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE,
                        npcSay, "Tẩy pháp sư\n" + Util.numberToMoney(50_000_000_000L) + " vàng", "Từ chối");
            } else {
                CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                        "Cần có trang bị và Ngọc Tẩy", "Đóng");
            }
        } else {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                    "Hành trang cần ít nhất 1 chỗ trống", "Đóng");
        }
    }

    public static void startCombine(Player player) {
        if (player.combine.itemsCombine.size() != 2) {
            return;
        }
        if (player.combine.itemsCombine.stream().filter(item -> item.isNotNullItem() && item.isTrangBiHacHoa()).count() != 1) {
            return;
        }
        if (player.combine.itemsCombine.stream().filter(item -> item.isNotNullItem() && item.template.id == 1708).count() != 1) {
            return;
        }

        if (InventoryService.gI().getCountEmptyBag(player) > 0) {
            if (player.inventory.gold < 50_000_000_000L) {
                Service.gI().sendThongBao(player, "Con cần 50 tỷ vàng để tẩy...");
                return;
            }

            Item daHacHoa = player.combine.itemsCombine.stream()
                    .filter(item -> item.template.id == 1708)
                    .findFirst().orElse(null);
            Item trangBi = player.combine.itemsCombine.stream()
                    .filter(Item::isTrangBiHacHoa)
                    .findFirst().orElse(null);

            if (daHacHoa == null || trangBi == null) {
                Service.gI().sendThongBao(player, "Thiếu nguyên liệu cần thiết");
                return;
            }
            if (trangBi.template.type == 32) {
                if (Util.isTrue(35, 100)) {
                    CombineService.gI().sendEffectSuccessCombine(player);
                    int[] optionRemoveIds = {0, 6, 7, 72};

                    trangBi.itemOptions.removeIf(opt -> {
                        for (int id : optionRemoveIds) {
                            if (opt.optionTemplate.id == id) {
                                return true;
                            }
                        }
                        return false;
                    });
                    Service.gI().sendThongBao(player, "Đã tẩy thành công các chỉ số pháp sư!");
                    InventoryService.gI().sendItemBag(player);
                } else {
                    CombineService.gI().sendEffectFailCombine(player);
                    Service.gI().sendThongBao(player, "Tẩy thất bại, trang bị không bị mất chỉ số!");
                }
            } else {
                Service.gI().sendThongBao(player, "Chỉ có thể tẩy Giáp luyện tập!");
            }
            player.inventory.gold -= 50_000_000_000L;
            InventoryService.gI().subQuantityItemsBag(player, daHacHoa, 1);
            InventoryService.gI().sendItemBag(player);
            Service.gI().sendMoney(player);
            player.combine.itemsCombine.clear();
            CombineService.gI().reOpenItemCombine(player);
        }
    }
}
