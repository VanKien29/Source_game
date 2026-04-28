package models.Combine.manifest;

import consts.ConstFont;
import consts.ConstNpc;
import item.Item;
import models.Combine.CombineService;
import player.Player;
import services.InventoryService;
import services.Service;
import utils.Util;

public class GiamDinhSach {

    public static void showInfoCombine(Player player) {
        if (player.combine.itemsCombine.size() != 2) {
            Service.gI().sendDialogMessage(player, "Cần Sách Tuyệt Kỹ và bùa giám định.");
            return;
        }
        Item sachTuyetKy = null;
        Item buaGiamDinh = null;
        for (Item item : player.combine.itemsCombine) {
            if (item.isSachTuyetKy() || item.isSachTuyetKy2()) {
                sachTuyetKy = item;
            } else if (item.template.id == 1284) {
                buaGiamDinh = item;
            }
        }
        if (sachTuyetKy == null || buaGiamDinh == null) {
            Service.gI().sendDialogMessage(player, "Cần Sách Tuyệt Kỹ và bùa giám định.");
            return;
        }
        StringBuilder text = new StringBuilder();
        text.append(ConstFont.BOLD_GREEN).append("Giám định ").append(sachTuyetKy.template.name).append(" ?\n");
        text.append(ConstFont.BOLD_BLUE).append("Bùa giám định ").append(buaGiamDinh.quantity).append("/1\n");
        text.append(ConstFont.BOLD_RED).append("Tỉ lệ thành công: 50%\n");
        text.append(ConstFont.BOLD_BLUE).append(sachTuyetKy.isSachTuyetKy() ? "Sẽ thêm 1 option (chỉ số 1-10%)" : "Sẽ thêm 2 option (chỉ số 1-10%)");
        CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, text.toString(), "Giám định", "Từ chối");
    }

    public static void giamDinhSach(Player player) {
        if (player.combine.itemsCombine.size() != 2) {
            return;
        }
        Item sachTuyetKy = null;
        Item buaGiamDinh = null;
        for (Item item : player.combine.itemsCombine) {
            if (item.isSachTuyetKy() || item.isSachTuyetKy2()) {
                sachTuyetKy = item;
            } else if (item.template.id == 1284) {
                buaGiamDinh = item;
            }
        }
        if (sachTuyetKy == null || buaGiamDinh == null) {
            return;
        }
        if (!sachTuyetKy.isHaveOption(217)) {
            Service.gI().sendServerMessage(player, "Còn cái nịt mà giám");
            return;
        }

        // Trừ bùa giám định trước
        InventoryService.gI().subQuantityItemsBag(player, buaGiamDinh, 1);

        // Kiểm tra tỉ lệ thành công (50%)
        if (Util.isTrue(50, 100)) {
            // Thành công
            int[] options = {77, 103, 50, 108, 94, 14, 80, 81, 175, 5, 214, 216};

            // Xóa option có id == 217
            sachTuyetKy.itemOptions.removeIf(io -> io.optionTemplate.id == 217);

            // Thêm option mới dựa trên loại sách
            int numOptionsToAdd = sachTuyetKy.isSachTuyetKy() ? 1 : 2; // 1 option cho cấp 1, 2 option cho cấp 2
            for (int i = 0; i < numOptionsToAdd; i++) {
                int randomOptionId = options[Util.nextInt(options.length)];
                int randomParam = Util.nextInt(1, 10); // Chỉ số từ 1 đến 2
                sachTuyetKy.itemOptions.add(new Item.ItemOption(randomOptionId, randomParam));
            }

            CombineService.gI().sendEffectSuccessCombine(player);
//            Service.gI().sendServerMessage(player, "Giám định thành công! Đã thêm " + numOptionsToAdd + " option!");
        } else {
            // Thất bại
            CombineService.gI().sendEffectFailCombine(player);
//            Service.gI().sendServerMessage(player, "Giám định thất bại, sách không thay đổi!");
        }

        InventoryService.gI().sendItemBag(player);
        CombineService.gI().reOpenItemCombine(player);
    }
}