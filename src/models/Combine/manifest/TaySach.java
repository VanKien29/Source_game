package models.Combine.manifest;

import consts.ConstFont;
import consts.ConstNpc;
import item.Item;
import models.Combine.CombineService;
import player.Player;
import services.InventoryService;
import services.Service;
import utils.Util;

public class TaySach {

    public static void showInfoCombine(Player player) {
        if (player.combine.itemsCombine.size() != 1) {
            Service.gI().sendDialogMessage(player, "Cần Sách Tuyệt Kỹ để tẩy.");
            return;
        }
        Item sachTuyetKy = player.combine.itemsCombine.get(0);
        if (sachTuyetKy == null || (!sachTuyetKy.isSachTuyetKy() && !sachTuyetKy.isSachTuyetKy2())) {
            Service.gI().sendDialogMessage(player, "Cần Sách Tuyệt Kỹ để tẩy.");
            return;
        }
        StringBuilder text = new StringBuilder();
        text.append(ConstFont.BOLD_BLUE).append("Tẩy Sách Tuyệt Kỹ ?\n");
        text.append(ConstFont.BOLD_RED).append("Sẽ xóa toàn bộ các option hiện tại!\n");
        text.append(ConstFont.BOLD_BLUE).append("Số lần tẩy còn lại: ").append(sachTuyetKy.getOptionParam(219));
        CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, text.toString(), "Đồng ý", "Từ chối");
    }

    public static void taySach(Player player) {
        if (player.combine.itemsCombine.size() != 1) {
            return;
        }
        Item sachTuyetKy = player.combine.itemsCombine.get(0);
        if (sachTuyetKy == null || (!sachTuyetKy.isSachTuyetKy() && !sachTuyetKy.isSachTuyetKy2())) {
            return;
        }
        if (sachTuyetKy.getOptionParam(219) <= 0 || sachTuyetKy.isHaveOption(217)) {
            Service.gI().sendServerMessage(player, "Đã hết số lượt tẩy!");
            return;
        }

        // Xóa tất cả option trừ id == 21 và id == 219
        sachTuyetKy.itemOptions.removeIf(io -> io.optionTemplate.id != 21 && io.optionTemplate.id != 219);

        // Thêm 1 option mới với id == 217 và param == 0
        sachTuyetKy.itemOptions.add(new Item.ItemOption(217, 0));

        // Giảm số lần tẩy (option id == 219)
        sachTuyetKy.subOptionParam(219, 1);

        CombineService.gI().sendEffectSuccessCombine(player);
        Service.gI().sendServerMessage(player, "Tẩy sách thành công!");
        InventoryService.gI().sendItemBag(player);
        CombineService.gI().reOpenItemCombine(player);
    }
}