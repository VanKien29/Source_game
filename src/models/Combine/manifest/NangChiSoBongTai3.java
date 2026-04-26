package models.Combine.manifest;

import consts.ConstFont;
import consts.ConstNpc;
import item.Item;
import models.Combine.CombineService;
import player.Player;
import services.InventoryService;
import services.Service;
import utils.Util;

public class NangChiSoBongTai3 {

    public static void showInfoCombine(Player player) {
        if (player.combine.itemsCombine.size() != 3) {
            Service.gI().sendDialogMessage(player, "Cần 1 bông tai cấp 3, 99 mảnh hồn porata cấp 3 và 1 đá xanh lam.");
            return;
        }
        Item bongTai = null;
        Item manhHonBongTai = null;
        Item daXanhLam = null;
        for (Item item : player.combine.itemsCombine) {
            if (item.isNotNullItem()) {
                switch (item.template.id) {
                    case 1774 ->
                        bongTai = item;
                    case 1773 ->
                        manhHonBongTai = item;
                    case 935 ->
                        daXanhLam = item;
                }
            }
        }

        if (bongTai == null || manhHonBongTai == null || daXanhLam == null) {
            Service.gI().sendDialogMessage(player, "Cần 1 bông tai cấp 3, 99 mảnh hồn porata cấp 3 và 1 đá xanh lam.");
            return;
        }

        StringBuilder text = new StringBuilder();
        text.append(ConstFont.BOLD_BLUE).append("Bông tai Porata [+3]\n\n");
        text.append(ConstFont.BOLD_BLUE).append("Tỉ lệ thành công: 35%\n");
        text.append(manhHonBongTai.quantity >= 99 ? ConstFont.BOLD_BLUE : ConstFont.BOLD_RED).append("Cần 99 Mảnh hồn bông tai cấp 3\n");
        text.append(daXanhLam.quantity >= 1 ? ConstFont.BOLD_BLUE : ConstFont.BOLD_RED).append("Cần 1 Đá xanh lam\n");
        text.append(player.inventory.getGemAndRuby() >= 250 ? ConstFont.BOLD_BLUE : ConstFont.BOLD_RED).append("Cần 250 ngọc\n");
        text.append(ConstFont.BOLD_GREEN).append("+1 Chỉ số ngẫu nhiên\n");
        if (player.inventory.getGemAndRuby() < 250) {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, text.toString(), "Còn thiếu\n" + Util.numberToMoney(250 - player.inventory.getGemAndRuby()) + " ngọc");
            return;
        }
        if (daXanhLam.quantity < 1) {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, text.toString(), "Còn thiếu\nĐá xanh lam");
            return;
        }
        if (manhHonBongTai.quantity < 99) {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, text.toString(), "Còn thiếu\n" + (99 - manhHonBongTai.quantity) + " Mảnh hồn bông tai");
            return;
        }
        CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, text.toString(), "Nâng cấp\n250 ngọc", "Từ chối");
    }

    public static void nangChiSoBongTai3(Player player) {
        if (player.combine.itemsCombine.size() != 3) {
            return;
        }
        Item bongTai = null;
        Item manhHonBongTai = null;
        Item daXanhLam = null;
        for (Item item : player.combine.itemsCombine) {
            if (item.isNotNullItem()) {
                switch (item.template.id) {
                    case 1774 ->
                        bongTai = item;
                    case 1773 ->
                        manhHonBongTai = item;
                    case 935 ->
                        daXanhLam = item;
                }
            }
        }

        if (bongTai == null || manhHonBongTai == null || daXanhLam == null || player.inventory.getGemAndRuby() < 250 || daXanhLam.quantity < 1 || manhHonBongTai.quantity < 99) {
            return;
        }
        if (Util.isTrue(25, 100)) {
            int[] options = {77, 103, 50, 108, 94, 14, 80, 81, 175, 5};

            // ---- Chỉ số dòng 1 ----
            int option1 = options[Util.nextInt(options.length)];
            int param1 = (option1 == 94 || option1 == 14) ? Util.nextInt(3, 10) : Util.nextInt(5, 15);

            // ---- Chỉ số dòng 2 ----
            int option2 = options[Util.nextInt(options.length)];
            int param2 = (option2 == 94 || option2 == 14) ? Util.nextInt(1, 3) : Util.nextInt(1, 7);

            bongTai.itemOptions.clear();
            bongTai.itemOptions.add(new Item.ItemOption(option1, param1));
            bongTai.itemOptions.add(new Item.ItemOption(option2, param2));

            // Giữ nguyên option mặc định 38, 0
            bongTai.itemOptions.add(new Item.ItemOption(38, 0));
            bongTai.itemOptions.add(new Item.ItemOption(30, 0));

            CombineService.gI().sendEffectSuccessCombine(player);
        } else {
            CombineService.gI().sendEffectFailCombine(player);
        }

        InventoryService.gI().subQuantityItemsBag(player, manhHonBongTai, 99);
        InventoryService.gI().subQuantityItemsBag(player, daXanhLam, 1);
        InventoryService.gI().sendItemBag(player);
        CombineService.gI().reOpenItemCombine(player);
    }

}
