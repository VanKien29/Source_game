package models.Combine.manifest;

import consts.ConstFont;
import consts.ConstNpc;
import item.Item;
import models.Combine.CombineService;
import static models.Combine.CombineService.MAX_STAR_ITEM;
import models.Combine.CombineUtil;
import player.Player;
import server.ServerNotify;
import services.InventoryService;
import services.Service;
import utils.Util;

public class PhaLeHoaTrangBi {

    public static void showInfoCombine(Player player) {
        if (player.combine.itemsCombine.size() == 1) {
            Item item = player.combine.itemsCombine.get(0);
            if (CombineUtil.isTrangBiPhaLeHoa(item)) {
                int star = 0;
                for (Item.ItemOption io : item.itemOptions) {
                    if (io.optionTemplate.id == 107) {
                        star = io.param;
                        break;
                    }
                }
                if (star < CombineService.MAX_STAR_ITEM) {
                    player.combine.goldCombine = CombineUtil.getGoldPhaLeHoa(star);
                    player.combine.gemCombine = CombineUtil.getGemPhaLeHoa(star);
                    player.combine.ratioCombine = CombineUtil.getRatioPhaLeHoa(star);

                    String npcSay = item.template.name + "\n|2|";
                    for (Item.ItemOption io : item.itemOptions) {
                        if (io.optionTemplate.id != 102) {
                            npcSay += io.getOptionString() + "\n";
                        }
                    }
                    npcSay += "|7|Tỉ lệ thành công: " + player.combine.ratioCombine + "%" + "\n";
                    if (player.combine.goldCombine <= player.inventory.gold) {
                        npcSay += "|1|Cần " + Util.numberToMoney(player.combine.goldCombine) + " vàng";
                        CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay,
                                "Nâng cấp 10000 lần", "Nâng cấp 1000 lần", "Nâng cấp 100 lần", "Nâng cấp 10 lần",
                                "Nâng cấp\ncần " + player.combine.gemCombine + " ngọc");
                    } else {
                        npcSay += "Còn thiếu " + Util.numberToMoney(player.combine.goldCombine - player.inventory.gold) + " vàng";
                        CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, npcSay, "Đóng");
                    }

                } else {
                    CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Vật phẩm đã đạt tối đa sao pha lê", "Đóng");
                }
            } else {
                CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Vật phẩm này không thể đục lỗ", "Đóng");
            }
        } else {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Hãy chọn 1 vật phẩm để pha lê hóa", "Đóng");
        }
    }

    public static void phaLeHoa(Player player, int... numm) {
        if (player.iDMark != null && !Util.canDoWithTime(player.iDMark.getLastTimeCombine(), 500)) {
            return;
        }
        player.iDMark.setLastTimeCombine(System.currentTimeMillis());
        int n = 1;
        if (numm.length > 0) {
            n = numm[0];
        }
        if (!player.combine.itemsCombine.isEmpty()) {
            int gold = player.combine.goldCombine;
            int gem = player.combine.gemCombine;
            if (player.inventory.gold < gold) {
                Service.gI().sendThongBao(player, "Không đủ vàng để thực hiện");
                return;
            } else if (player.inventory.gem < gem) {
                Service.gI().sendThongBao(player, "Không đủ ngọc để thực hiện");
                return;
            }
            int barGoldSell = 0;
            int num = 0;
            int star = 0;
            boolean success = false;
            int fail = 0;
            Item item = null;
            Item.ItemOption optionStar = null;
            for (int i = 0; i < n; i++) {
                num = i;
                gold = player.combine.goldCombine;
                gem = player.combine.gemCombine;
                if (player.inventory.gem < gem) {
                    if (n > 1) {
                        Service.gI().sendThongBao(player, "Xịt liên tục " + i + " lần và đã hết ngọc!");
                        Service.gI().sendMoney(player);
                    }
                    break;
                }
                if (n > 1 && player.inventory.gold < gold && InventoryService.gI().findItemBag(player, 457) != null && InventoryService.gI().findItemBag(player, 457).quantity >= 1) {
                    InventoryService.gI().subQuantityItemsBag(player, InventoryService.gI().findItemBag(player, 457), 1);
                    player.inventory.gold += 37000000;
                    barGoldSell++;
                }
                if (player.inventory.gold < gold) {
                    break;
                }
                item = player.combine.itemsCombine.get(0);
                if (CombineUtil.isTrangBiPhaLeHoa(item)) {
                    star = 0;
                    optionStar = null;
                    for (Item.ItemOption io : item.itemOptions) {
                        if (io.optionTemplate.id == 107) {
                            star = io.param;
                            optionStar = io;
                            break;
                        }
                    }
                    if (star < MAX_STAR_ITEM) {
                        player.combine.goldCombine = CombineUtil.getGoldPhaLeHoa(star);
                        player.combine.gemCombine = CombineUtil.getGemPhaLeHoa(star);
                        player.combine.ratioCombine = CombineUtil.getRatioPhaLeHoa(star);
                        player.inventory.gold -= gold;
                        player.inventory.gem -= gem;
                        int ratio = 1;
                        boolean succ = true;
                        if (optionStar != null) {
                            switch (optionStar.param) {
                                case 4 ->
                                    ratio *= 2;
                                case 5 ->
                                    ratio *= 2.2;
                                case 6 ->
                                    ratio *= 16;
                                case 7 ->
                                    ratio *= 30;
                                case 8 ->
                                    ratio *= 60;
                                case 9 ->
                                    ratio *= 1000;
                                case 10 ->
                                    ratio *= 800;
                                case 11 ->
                                    ratio *= 160;
                                case 12 ->
                                    ratio *= 320;
                            }
                            if (optionStar.param > 10) {
                                succ = false;
                                if (Util.isTrue(1, 100000000)) {
                                    succ = true;
                                }
                            }
                        }
                        // tỉ lệ đập đồ open note dòng 153 bật dòng 154
                        //  if (Util.isTrue(100, 100)) {
                        if (Util.isTrue(player.combine.ratioCombine, 100 * ratio) && succ) {
                            success = true;
                            break;
                        } else {
                            fail++;
                        }
                    }
                } else {
                    break;
                }
            }
            num++;
            if (item != null && fail > 0) {
                Item.ItemOption optionXit = null;
                for (Item.ItemOption io : item.itemOptions) {
                    if (io.optionTemplate.id == 66) {
                        fail += io.param;
                        optionXit = io;
                        break;
                    }
                }
                if (optionXit == null) {
                    item.itemOptions.add(new Item.ItemOption(66, fail));
                } else {
                    optionXit.param = fail;
                }
            }
            if (success) {
                star++;
                if (item != null) {
                    if (optionStar == null) {
                        item.itemOptions.add(new Item.ItemOption(107, star));
                    } else {
                        optionStar.param = star;
                    }
                    if (star > 4) {
                        ServerNotify.gI().notify("Chúc mừng " + player.name + " vừa pha lê hóa "
                                + "thành công " + item.template.name + " lên " + star + " sao pha lê");
                    }
                }
                if (n > 1) {
                    Service.gI().sendThongBao(player, "Pha lê hóa trang bị lên " + star + " sao thành công, sau " + num + " lần nâng cấp!");
                }
                CombineService.gI().sendEffectSuccessCombine(player);
            } else {
                if (n >= 1) {
                    Service.gI().sendThongBao(player, "Xịt liên tục " + num + " lần" + (barGoldSell > 0 ? ", đã bán " + barGoldSell + " thỏi vàng" : "") + " và đã hết vàng!");
                }
                CombineService.gI().sendEffectFailCombine(player);
            }
            InventoryService.gI().sendItemBag(player);
            Service.gI().sendMoney(player);
            CombineService.gI().reOpenItemCombine(player);
        }
    }
}
