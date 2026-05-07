package models.Combine.manifest;

import consts.ConstFont;
import consts.ConstNpc;
import item.Item;
import models.Combine.CombineService;
import player.Player;
import services.InventoryService;
import services.ItemService;
import services.Service;
import utils.Util;

public class LamPhepNhapDa {

    private static final int ID_MANH_DA_VUN = 225;
    private static final int ID_BINH_NUOC_PHEP = 226;
    private static final int MIN_ID_DA_NANG_CAP = 220;
    private static final int MAX_ID_DA_NANG_CAP = 224;
    private static final int COST_MANH_DA_VUN = 10;
    private static final int COST_BINH_NUOC_PHEP = 1;

    public static void showInfoCombine(Player player) {
        if (InventoryService.gI().getCountEmptyBag(player) == 0) {
            Service.gI().sendDialogMessage(player, "Hành trang đã đầy, cần một ô trống trong hành trang");
            return;
        }
        if (player.combine.itemsCombine.size() != 2) {
            Service.gI().sendDialogMessage(player, "Cần 10 Mảnh đá vụn và 1 Bình nước phép");
            return;
        }

        Item manhDaVun = null;
        Item binhNuocPhep = null;
        for (Item item : player.combine.itemsCombine) {
            if (item == null || !item.isNotNullItem()) {
                continue;
            }
            if (item.template.id == ID_MANH_DA_VUN) {
                manhDaVun = item;
            } else if (item.template.id == ID_BINH_NUOC_PHEP) {
                binhNuocPhep = item;
            }
        }

        if (manhDaVun == null || binhNuocPhep == null) {
            Service.gI().sendDialogMessage(player, "Cần 10 Mảnh đá vụn và 1 Bình nước phép");
            return;
        }

        StringBuilder text = new StringBuilder();
        text.append(ConstFont.BOLD_BLUE).append("Con có muốn làm phép nhập đá không?\n");
        text.append(manhDaVun.quantity >= COST_MANH_DA_VUN ? ConstFont.BOLD_GREEN : ConstFont.BOLD_RED)
                .append("Cần 10 Mảnh đá vụn\n");
        text.append(binhNuocPhep.quantity >= COST_BINH_NUOC_PHEP ? ConstFont.BOLD_GREEN : ConstFont.BOLD_RED)
                .append("Cần 1 Bình nước phép\n");
        text.append(ConstFont.BOLD_BLUE).append("Nhận ngẫu nhiên 1 đá nâng cấp");

        if (manhDaVun.quantity < COST_MANH_DA_VUN) {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, text.toString(),
                    "Còn thiếu\n" + (COST_MANH_DA_VUN - manhDaVun.quantity) + " Mảnh đá vụn");
            return;
        }
        if (binhNuocPhep.quantity < COST_BINH_NUOC_PHEP) {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, text.toString(),
                    "Còn thiếu\nBình nước phép");
            return;
        }
        CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, text.toString(),
                "Làm phép", "Từ chối");
    }

    public static void lamPhepNhapDa(Player player) {
        if (InventoryService.gI().getCountEmptyBag(player) == 0 || player.combine.itemsCombine.size() != 2) {
            return;
        }

        Item manhDaVun = null;
        Item binhNuocPhep = null;
        for (Item item : player.combine.itemsCombine) {
            if (item == null || !item.isNotNullItem()) {
                continue;
            }
            if (item.template.id == ID_MANH_DA_VUN) {
                manhDaVun = item;
            } else if (item.template.id == ID_BINH_NUOC_PHEP) {
                binhNuocPhep = item;
            }
        }

        if (manhDaVun == null || binhNuocPhep == null
                || manhDaVun.quantity < COST_MANH_DA_VUN
                || binhNuocPhep.quantity < COST_BINH_NUOC_PHEP) {
            return;
        }

        short idDaNangCap = (short) Util.nextInt(MIN_ID_DA_NANG_CAP, MAX_ID_DA_NANG_CAP);
        Item daNangCap = ItemService.gI().createNewItem(idDaNangCap);
        CombineService.gI().sendEffectCombineItem(player, (byte) 7, (short) daNangCap.template.iconID, (short) -1);
        InventoryService.gI().subQuantityItemsBag(player, manhDaVun, COST_MANH_DA_VUN);
        InventoryService.gI().subQuantityItemsBag(player, binhNuocPhep, COST_BINH_NUOC_PHEP);
        InventoryService.gI().addItemBag(player, daNangCap);
        InventoryService.gI().sendItemBag(player);
        Service.gI().sendServerMessage(player, "Bạn nhận được " + daNangCap.template.name);
        CombineService.gI().reOpenItemCombine(player);
    }
}
