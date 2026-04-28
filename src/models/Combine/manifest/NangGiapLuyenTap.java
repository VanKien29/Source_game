/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package models.Combine.manifest;

import java.util.Arrays;
import java.util.List;

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
public class NangGiapLuyenTap {

    private static float getTileNangCapDo(int level) {
        switch (level) {
            case 0:
                return 50f;
            case 1:
                return 25f;
            case 2:
                return 15.5f;
            case 3:
                return 10.5f;
            case 4:
                return 8f;
            case 5:
                return 3f;
            case 6:
                return 1f;
            case 7: // +8
                return 0.3f;
        }
        return 0;
    }

    private static int getCountDaQuy(int level) {
        switch (level) {
            case 0:
                return 10;
            case 1:
                return 15;
            case 2:
                return 20;
            case 3:
                return 25;
            case 4:
                return 35;
            case 5:
                return 40;
            case 6:
                return 50;
            case 7:
                return 60;
        }
        return 0;
    }

    private static int getCountDaBaoVe(int level) {
        return level + 1;
    }

    private static int getGoldNangCapDo(int level) {
        switch (level) {
            case 0:
                return 100_000_000;
            case 1:
                return 300_000_000;
            case 2:
                return 700_000_000;
            case 3:
                return 1_500_000_000;
            case 4:
                return 1_600_000_000;
            case 5:
                return 1_700_000_000;
            case 6:
                return 1_800_000_000;
            case 7:
                return 2_000_000_000;
        }
        return 0;
    }

    private static boolean isCoupleItemNangCap(Item trangBi, Item daNangCap) {
        if (trangBi != null && daNangCap != null) {
            return (trangBi.template.type == 32) && daNangCap.template.id == 1710;
        }
        return false;
    }

    public static void showInfoCombine(Player player) {
        if (player.combine.itemsCombine.size() == 3) {

            Item giapLT = null;
            Item daNangCap = null;
            Item daBaoVe = null;

            for (Item item : player.combine.itemsCombine) {
                if (!item.isNotNullItem()) {
                    continue;
                }

                if (item.template.type == 32) {
                    giapLT = item;
                } else if (item.template.id == 1710) {
                    daNangCap = item;
                } else if (item.template.id == 987) {
                    daBaoVe = item;
                }
            }

            if (!isCoupleItemNangCap(giapLT, daNangCap) || daBaoVe == null) {
                CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                        "Cần:\n- Giáp luyện tập\n- Đá Hổ Phách\n- Đá bảo vệ", "Đóng");
                return;
            }

            int level = 0;
            for (ItemOption io : giapLT.itemOptions) {
                if (io.optionTemplate.id == 72) {
                    level = io.param;
                    break;
                }
            }
            player.combine.goldCombine = getGoldNangCapDo(level);
            player.combine.ratioCombine = getTileNangCapDo(level);
            player.combine.countDaQuy = getCountDaQuy(level);
            player.combine.countDaBaoVe = (short) getCountDaBaoVe(level);

            String npcSay = "|2|Hiện tại " + giapLT.template.name + " (+" + level + ")\n|0|";
            for (ItemOption io : giapLT.itemOptions) {
                if (io.optionTemplate.id != 72) {
                    npcSay += io.getOptionString() + "\n";
                }
            }

            npcSay += "|2|Sau khi nâng sẽ tăng ngẫu nhiên 1 chỉ số pháp sư (HP, KI, Sức đánh, Giáp)\n"
                    + "|7|Tỉ lệ thành công: " + player.combine.ratioCombine + "%\n"
                    + (player.combine.countDaQuy > daNangCap.quantity ? "|7|" : "|1|")
                    + "Cần " + player.combine.countDaQuy + " " + daNangCap.template.name + "\n"
                    + (player.combine.countDaBaoVe > daBaoVe.quantity ? "|7|" : "|1|")
                    + "Cần " + player.combine.countDaBaoVe + " " + daBaoVe.template.name + "\n"
                    + (player.combine.goldCombine > player.inventory.gold ? "|7|" : "|1|")
                    + "Cần " + Util.numberToMoney(player.combine.goldCombine) + " vàng";
            if (player.combine.countDaQuy > daNangCap.quantity) {
                CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                        npcSay, "Còn thiếu\n" + (player.combine.countDaQuy - daNangCap.quantity) + " " + daNangCap.template.name);
            } else if (player.combine.countDaBaoVe > daBaoVe.quantity) {
                CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                        npcSay, "Còn thiếu\n" + (player.combine.countDaBaoVe - daBaoVe.quantity) + " " + daBaoVe.template.name);
            } else if (player.combine.goldCombine > player.inventory.gold) {
                CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                        npcSay, "Còn thiếu\n" + Util.numberToMoney(player.combine.goldCombine - player.inventory.gold) + " vàng");
            } else {
                CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE,
                        npcSay, "Nâng cấp\n" + Util.numberToMoney(player.combine.goldCombine) + " vàng", "Từ chối");
            }

        } else {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                    "Cần chọn đúng 3 vật phẩm:\n- Giáp luyện tập \n- Đá Hổ Phách\n- Đá bảo vệ", "Đóng");
        }
    }

    public static void startCombine(Player player) {
        int countDaNangCap = player.combine.countDaQuy;
        long gold = player.combine.goldCombine;
        short countDaBaoVe = player.combine.countDaBaoVe;

        if (player.combine.itemsCombine.size() != 3) {
            Service.gI().sendThongBao(player, "Cần đủ 3 vật phẩm: Giáp LT + Đá nâng cấp + Đá bảo vệ");
            return;
        }

        Item giapLT = null;
        Item daNangCap = null;
        Item daBaoVe = null;

        for (Item item : player.combine.itemsCombine) {
            if (!item.isNotNullItem()) {
                continue;
            }

            if (item.template.type == 32) {
                giapLT = item;
            } else if (item.template.id == 1710) {
                daNangCap = item;
            } else if (item.template.id == 987) {
                daBaoVe = item;
            }
        }

        if (!isCoupleItemNangCap(giapLT, daNangCap) || daBaoVe == null) {
            Service.gI().sendThongBao(player, "Sai vật phẩm! Cần Giáp LT + Đá nâng cấp + Đá bảo vệ");
            return;
        }
        if (daNangCap.quantity < countDaNangCap) {
            Service.gI().sendThongBao(player, "Thiếu " + (countDaNangCap - daNangCap.quantity) + " đá nâng cấp");
            return;
        }
        if (daBaoVe.quantity < countDaBaoVe) {
            Service.gI().sendThongBao(player, "Thiếu " + (countDaBaoVe - daBaoVe.quantity) + " đá bảo vệ");
            return;
        }

        if (player.inventory.gold < gold) {
            Service.gI().sendThongBao(player, "Không đủ vàng! Cần " + Util.numberToMoney(gold));
            return;
        }

        int level = 0;
        ItemOption optionLevel = null;
        for (ItemOption io : giapLT.itemOptions) {
            if (io.optionTemplate.id == 72) {
                level = io.param;
                optionLevel = io;
                break;
            }
        }

        List<Integer> psOptions = Arrays.asList(0, 6, 7);
        int randomPsId = psOptions.get(Util.nextInt(0, psOptions.size() - 1));

        ItemOption psOption = null;
        for (ItemOption io : giapLT.itemOptions) {
            if (io.optionTemplate.id == randomPsId) {
                psOption = io;
                break;
            }
        }

        player.inventory.gold -= gold;

        if (Util.isTrue(player.combine.ratioCombine, 100)) {
            int addValue;
            if (randomPsId == 0) {
                addValue = Util.nextInt(500, 2000);
            } else {
                addValue = Util.nextInt(5000, 20000);
            }
            if (psOption != null) {
                psOption.param += addValue;
            } else {
                giapLT.itemOptions.add(new ItemOption(randomPsId, addValue));
            }
            if (optionLevel != null) {
                optionLevel.param++;
            } else {
                giapLT.itemOptions.add(new Item.ItemOption(72, 1));
            }

            CombineService.gI().sendEffectSuccessCombine(player);
            Service.gI().sendThongBao(player, "Nâng cấp thành công! Giáp luyện tập mạnh hơn!");
        } else {
            CombineService.gI().sendEffectFailCombine(player);
            Service.gI().sendThongBao(player, "Nâng cấp thất bại! May mà có đá bảo vệ nên không rớt cấp.");
        }

        InventoryService.gI().subQuantityItemsBag(player, daNangCap, countDaNangCap);
        InventoryService.gI().subQuantityItemsBag(player, daBaoVe, countDaBaoVe);

        InventoryService.gI().sendItemBag(player);
        Service.gI().sendMoney(player);
        CombineService.gI().reOpenItemCombine(player);
    }
}
