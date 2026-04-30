package models.Combine.manifest;

import consts.ConstNpc;
import item.Item;
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

    public static boolean isDoHuyDiet(Item item) {
        if (item.template.id >= 650 && item.template.id <= 662) {
            return true;
        }
        return false;
    }

    public static void showInfoCombine(Player player) {
        if (player.combine != null && player.combine.itemsCombine != null && player.combine.itemsCombine.size() == 2) {
            Item trangbiHuyDiet = null;
            Item daKichHoat = null;
            for (Item item : player.combine.itemsCombine) {
                if (isDoHuyDiet(item)) {
                    trangbiHuyDiet = item;
                } else if (item.template.id == 1742) {
                    daKichHoat = item;
                }
            }
            player.combine.goldCombine = 500_000_000;
            int goldCombie = player.combine.goldCombine;
            if (trangbiHuyDiet != null && daKichHoat != null) {
                String npcSay = "Sau khi cường hoá, sẽ được nâng cấp trang bị Huỷ Diệt thành trang bị Kích hoạt";
                CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay,
                        "Cường hoá\n" + Util.numberToMoney(goldCombie) + " vàng", "Từ chối");
            } else {
                Service.gI().sendThongBaoOK(player, "Cần 1 trang bị huỷ diệt và 1 viên đá kích hoạt");
            }
        } else {
            Service.gI().sendThongBaoOK(player, "Cần 1 trang bị huỷ diệt và 1 viên đá kích hoạt");
        }
    }

    public static void startCombine(Player player) {
        if (player.combine.itemsCombine.size() == 2) {
            int gold = player.combine.goldCombine;
            if (player.inventory.gold < gold) {
                Service.gI().sendThongBao(player, "Bạn không đủ vàng, còn thiếu " + Util.numberToMoney(gold - player.inventory.gold) + " vàng nữa");
                Service.gI().sendMoney(player);
                return;
            }

            Item trangbiHuyDiet = null;
            Item daKichHoat = null;
            for (Item item : player.combine.itemsCombine) {
                if (isDoHuyDiet(item)) {
                    trangbiHuyDiet = item;
                } else if (item.template.id == 1742) {
                    daKichHoat = item;
                }
            }
            if (trangbiHuyDiet == null || daKichHoat == null) {
                Service.gI().sendThongBaoOK(player, "Cần 1 trang bị huỷ diệt và 1 viên đá kích hoạt");
                return;
            }

            int gender = trangbiHuyDiet.template.gender;
            int playerGender = player.gender;

            // Set thường vs set hiếm
            final int W_COMMON = 10;
            final int W_RARE = 3;
            // TD (Trái đất)
            int[][] oldPairsTD = {
                {129, 141}, // Sôngôku (HIẾM)
                {127, 139}, // Thên Xin Hăng (thường)
                {128, 140} // Kirin (thường)
            };
            int[] oldWeightTD = {
                W_RARE, // Sôngôku thấp
                W_COMMON,
                W_COMMON
            };
            int[] newFullTD = {245, 246, 247, 248}; // Thần Vũ Trụ Kaio (HIẾM)
            int newWeightTD = W_RARE;

            // NM (Namếc)
            int[][] oldPairsNM = {
                {132, 144}, // Pikkoro Daimao (thường)
                {131, 143}, // Ốc tiêu (thường)
                {130, 142}, // Picolo (thường)
                {251, 254} // Liên Hoàn (HIẾM)
            };
            int[] oldWeightNM = {
                W_COMMON,
                W_COMMON,
                W_COMMON,
                W_RARE // Liên Hoàn thấp
            };
            int[] newFullNM = {237, 238, 239, 240}; // Nail chiến binh Namếc (HIẾM)
            int newWeightNM = W_RARE;

            // XD (Xayda)
            int[][] oldPairsXD = {
                {135, 138}, // Nappa (HIẾM)
                {133, 136}, // Kakarot (thường)
                {134, 137} // Ca Đíc (thường)
            };
            int[] oldWeightXD = {
                W_RARE, // Nappa thấp
                W_COMMON,
                W_COMMON
            };
            int[] newFullXD = {241, 242, 243, 244}; // Cađic M (HIẾM)
            int newWeightXD = W_RARE;

            boolean isTD = ((gender == 0 || gender == 3) && playerGender == 0);
            boolean isNM = ((gender == 1 || gender == 3) && playerGender == 1);
            
            int[][] oldPairs;
            int[] oldWeights;
            int[] newFull;
            int newWeight;

            if (isTD) {
                oldPairs = oldPairsTD;
                oldWeights = oldWeightTD;
                newFull = newFullTD; 
                newWeight = newWeightTD;
            } else if (isNM) {
                oldPairs = oldPairsNM;
                oldWeights = oldWeightNM;
                newFull = newFullNM;
                newWeight = newWeightNM;
            } else {
                oldPairs = oldPairsXD;
                oldWeights = oldWeightXD;
                newFull = newFullXD;
                newWeight = newWeightXD;
            }
            Item newItem;
            if (trangbiHuyDiet.template.type == 4) {
                newItem = ItemService.gI().createNewItem((short) 12);
            } else {
                newItem = ItemService.gI().createNewItem(Manager.trangBiKichHoat[gender][trangbiHuyDiet.template.type]);
            }
            RewardService.gI().initBaseOptionClothes(newItem.template.id, newItem.template.type, newItem.itemOptions);
            int total = newWeight;
            for (int w : oldWeights) {
                total += w;
            }

            int roll = Util.nextInt(1, total);
            int acc = 0;
            boolean applied = false;
            for (int i = 0; i < oldPairs.length; i++) {
                acc += oldWeights[i];
                if (roll <= acc) {
                    newItem.itemOptions.add(new Item.ItemOption(oldPairs[i][0], 0));
                    newItem.itemOptions.add(new Item.ItemOption(oldPairs[i][1], 0));
                    applied = true;
                    break;
                }
            }
            if (!applied) {
                for (int optionId : newFull) {
                    newItem.itemOptions.add(new Item.ItemOption(optionId, 0));
                }
            }
            NangCapLevelKichHoat.ensureLevelZero(newItem);
            InventoryService.gI().addItemBag(player, newItem);
            InventoryService.gI().subQuantityItemsBag(player, trangbiHuyDiet, 1);
            InventoryService.gI().subQuantityItemsBag(player, daKichHoat, 1);

            CombineService.gI().sendEffectSuccessCombine(player);
            InventoryService.gI().sendItemBag(player);
            Service.gI().sendMoney(player);
            CombineService.gI().reOpenItemCombine(player);
        }
    }
}
