package models.Combine.manifest;

import consts.ConstNpc;
import item.Item;
import item.Item.ItemOption;
import models.Combine.CombineService;
import player.Player;
import services.InventoryService;
import services.ItemService;
import services.Service;
import utils.Util;

/**
 *
 * @author Administrator
 */
public class NangCapChanMenh {

    private static boolean isXuKrai(Item item) {
        return (item != null && item.isNotNullItem() && item.template.id == 1705);
    }

    private static boolean isChanMenh(Item item) {
        return item != null && item.isNotNullItem() && (item.template.id >= 1733 && item.template.id <= 1741);
    }

    private static boolean isDaNguSac(Item item) {
        return item != null && item.isNotNullItem() && item.template.id == 674;
    }

    private static float getRatioCombine(Item item) {
        switch (item.template.id) {
            case 1733:
                return 40;
            case 1734:
                return 35;
            case 1735:
                return 30;
            case 1736:
                return 25;
            case 1737:
                return 20;
            case 1738:
                return 15;
            case 1739:
                return 10;
            case 1740:
                return 5;
            default:
                return 0;

        }
    }

    public static void showInfoCombine(Player player) {
        if (player.combine.itemsCombine.size() != 3) {
            Service.gI().sendThongBao(player, "Thiếu nguyên liệu");
            return;
        }

        Item chanMenh = null;
        Item daNguSac = null;
        Item xukraiVip = null;
        for (Item item : player.combine.itemsCombine) {
            if (isChanMenh(item)) {
                chanMenh = item;
            } else if (isDaNguSac(item)) {
                daNguSac = item;
            } else if (isXuKrai(item)) {
                xukraiVip = item;
            }
        }
        if (chanMenh == null || daNguSac == null || xukraiVip == null) {
            Service.gI().sendThongBao(player, "Thiếu nguyên liệu");
            return;
        }

        if (chanMenh.template.id == 1741) {
            Service.gI().sendThongBao(player, "Chân mệnh của bạn đã đạt cấp tối đa!");
            return;
        }

        
        String npcSay = "";
        if (daNguSac.quantity >= 99 && xukraiVip.quantity >= 5) {
            npcSay = "Bạn có muốn nâng cấp " + chanMenh.template.name + " không ?\n"
                    + "|0|Tỉ lệ thành công là : "+getRatioCombine(chanMenh)+"%\n"
                    + "|7|Cần 99 " + daNguSac.template.name + " và 5 " + xukraiVip.template.name;
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay, "Nâng cấp", "Từ chối");
        } else {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, npcSay, "Đóng", "Từ chối");

        }

    }

    public static void startCombine(Player player) {
        if (player.combine.itemsCombine.size() != 3) {
            Service.gI().sendThongBao(player, "Thiếu nguyên liệu");
            return;
        }

        Item chanMenh = null;
        Item daNguSac = null;
        Item xukraiVip = null;
        for (Item item : player.combine.itemsCombine) {
            if (isChanMenh(item)) {
                chanMenh = item;
            } else if (isDaNguSac(item)) {
                daNguSac = item;
            } else if (isXuKrai(item)) {
                xukraiVip = item;
            }
        }

        if (chanMenh == null || daNguSac == null || xukraiVip == null) { // Đổi tên biến
            Service.gI().sendThongBao(player, "Thiếu nguyên liệu");
            return;
        }

        if (daNguSac.quantity < 99 || xukraiVip.quantity < 5) { // Đổi tên biến
            Service.gI().sendThongBao(player, "Không đủ nguyên liệu nâng cấp! 1");
            return;
        }

        InventoryService.gI().subQuantityItemsBag(player, daNguSac, 99);
        InventoryService.gI().subQuantityItemsBag(player, xukraiVip, 5);

        if (Util.isTrue(getRatioCombine(chanMenh), 100)) {

            Item chanMenhMoi = ItemService.gI().createNewItem((short) (chanMenh.template.id + 1));
            int capChanMenh = chanMenhMoi.template.id - 1733;

            // Thêm các tùy chọn (options) cho Chân Mệnh mới
//            if (Util.isTrue(1, 100)) { // Tỉ lệ 1% để thêm option đặc biệt
//                chanMenhMoi.itemOptions.add(new ItemOption(50, 5 + capChanMenh * 2));
//            }
            chanMenhMoi.itemOptions.add(new ItemOption(77, 5 + capChanMenh * 2));
            chanMenhMoi.itemOptions.add(new ItemOption(103, 5 + capChanMenh * 2));
            chanMenhMoi.itemOptions.add(new ItemOption(5, 5 + capChanMenh));
            chanMenhMoi.itemOptions.add(new ItemOption(14, 5 + capChanMenh));

            // Thêm Chân Mệnh mới vào hành trang
            InventoryService.gI().addItemBag(player, chanMenhMoi);
            Service.gI().sendThongBao(player, "|0|Bạn nhận được " + chanMenhMoi.template.name);

            // Xóa Chân Mệnh cũ khỏi hành trang
            InventoryService.gI().subQuantityItemsBag(player, chanMenh, 1);

            // Gửi hiệu ứng thành công
            CombineService.gI().sendEffectSuccessCombine(player);
        } else { // Thất bại
            Service.gI().sendThongBao(player, "|0|Nâng cấp thất bại!");
            CombineService.gI().sendEffectFailCombine(player);
        }
        // Cập nhật hành trang và giao diện
        InventoryService.gI().sendItemBag(player);
        CombineService.gI().reOpenItemCombine(player);
    }

}
