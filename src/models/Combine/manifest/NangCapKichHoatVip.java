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
public class NangCapKichHoatVip {

    public static boolean isDoThienSu(Item item) {
        if (item.template.id >= 1048 && item.template.id <= 1062) {
            return true;
        }
        return false;
    }

    public static void showInfoCombine(Player player) {
        if (player.combine != null && player.combine.itemsCombine != null && player.combine.itemsCombine.size() == 2) {
            Item trangBiThienSu = null;
            Item daKichHoatVip = null;
            for (Item item : player.combine.itemsCombine) {
                if (isDoThienSu(item)) {
                    trangBiThienSu = item;
                } else if (item.template.id == 1743) {
                    daKichHoatVip = item;
                }
            }
            player.combine.goldCombine = 500_000_000;
            int goldCombie = player.combine.goldCombine;
            if (trangBiThienSu != null && daKichHoatVip != null) {
                String npcSay = "Sau khi cường hoá, sẽ được nâng cấp trang bị Thiên Sứ thành trang bị Kích hoạt Vip";
                CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay,
                        "Cường hoá\n" + Util.numberToMoney(goldCombie) + " vàng", "Từ chối");
            } else {
                Service.gI().sendThongBaoOK(player, "Cần 1 trang bị Thiên Sứ và 1 viên đá kích hoạt vip");
            }
        } else {
            Service.gI().sendThongBaoOK(player, "Cần 1 trang bị Thiên Sứ và 1 viên đá kích hoạt vip");
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

            Item trangBiThienSu = null;
            Item daKichHoatVip = null;
            for (Item item : player.combine.itemsCombine) {
                if (isDoThienSu(item)) {
                    trangBiThienSu = item;
                } else if (item.template.id == 1743) {
                    daKichHoatVip = item;
                }
            }

            if (trangBiThienSu == null || daKichHoatVip == null) {
                Service.gI().sendThongBaoOK(player, "Cần 1 trang bị Thiên Sứ và 1 viên đá kích hoạt vip");
                return;
            }

            int gender = trangBiThienSu.template.gender;
            int playerGender = player.gender;

            // Set thường vs set hiếm
            final int W_COMMON = 10;
            final int W_RARE = 3; // muốn hiếm hơn nữa thì giảm xuống 1-2

            // TD (Trái đất)
            int[][] oldPairsTD = {
                {129, 141}, // Sôngôku (HIẾM)
                {127, 139}, // Thên Xin Hăng (thường)
                {128, 140} // Kirin (thường)
            };
            int[] oldWeightTD = {
                W_RARE,
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
                W_RARE
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
                W_RARE,
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

            int slot = trangBiThienSu.template.type;

// type==4 bạn xử lý riêng (rada/cải trang...) -> giữ nguyên
            if (slot == 4) {
                short[] ids = {57, 58, 59, 184, 185, 186, 187, 278, 279, 280, 281, 561};
                short id = ids[Util.nextInt(0, ids.length - 1)];
                newItem = ItemService.gI().createNewItem(id);
            } else {
                // Chỉ áp dụng cho 4 món: áo/quần/găng/giày (slot 0..3)
                // Xác định "planet" theo logic giống phần SKH (đừng dùng gender thẳng vì gender có thể =3)
                int planet;
                if (isTD) {
                    planet = 0; // TD
                } else if (isNM) {
                    planet = 1; // NM
                } else {
                    planet = 2; // XD
                }

                // Nếu slot không nằm trong 0..3 thì fallback an toàn
                if (slot >= 0 && slot <= 3) {
                    boolean isDoThan = Util.isTrue(50, 100); // 5% đồ thần

                    int id;
                    if (isDoThan) {
                        id = Manager.DO_THAN_4MON[planet][slot];
                    } else {
                        int[] pool = Manager.LIST_DO_KHAC_4MON[planet][slot];
                        id = pool[Util.nextInt(0, pool.length - 1)];
                    }

                    newItem = ItemService.gI().createNewItem((short) id);
                } else {
                    // fallback (nếu server bạn có type khác ngoài 0..4)
                    newItem = ItemService.gI().createNewItem(Manager.trangBiKichHoatVip[gender][slot]);
                }
            }

            RewardService.gI().initBaseOptionClothes(newItem.template.id, newItem.template.type, newItem.itemOptions);

            int total = newWeight;
            for (int w : oldWeights) {
                total += w;
            }

            int roll = Util.nextInt(1, total); // 1..total
            int acc = 0;

            boolean applied = false;

            // old (2 dòng)
            for (int i = 0; i < oldPairs.length; i++) {
                acc += oldWeights[i];
                if (roll <= acc) {
                    newItem.itemOptions.add(new Item.ItemOption(oldPairs[i][0], 0));
                    newItem.itemOptions.add(new Item.ItemOption(oldPairs[i][1], 0));
                    applied = true;
                    break;
                }
            }

            // new (4 dòng)
            if (!applied) {
                for (int optionId : newFull) {
                    newItem.itemOptions.add(new Item.ItemOption(optionId, 0));
                }
            }
            NangCapLevelKichHoat.ensureLevelZero(newItem);
            InventoryService.gI().addItemBag(player, newItem);
            InventoryService.gI().subQuantityItemsBag(player, trangBiThienSu, 1);
            InventoryService.gI().subQuantityItemsBag(player, daKichHoatVip, 1);

            CombineService.gI().sendEffectSuccessCombine(player);
            InventoryService.gI().sendItemBag(player);
            Service.gI().sendMoney(player);
            CombineService.gI().reOpenItemCombine(player);
        }
    }
}
