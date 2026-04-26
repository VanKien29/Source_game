package npc.npc_manifest;

import consts.ConstNpc;
import consts.cn;
import item.Item;
import player.Player;
import services.InventoryService;
import services.ItemService;
import services.Service;
import services.TaskService;
import services.func.Input;
import utils.Util;
import npc.Npc;
import services.InventoryService;

public class RenVang extends Npc {

    public RenVang(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (!canOpenNpc(player)) {
            return;
        }

        Item carot = InventoryService.gI().findItemBag(player, cn.cr);
        if (!TaskService.gI().checkDoneTaskTalkNpc(player, this)) {
            if (carot != null && carot.quantity >= cn.check_sl_cr) {
                this.createOtherMenu(player, 12,
                        "\b|2|Tốt lắm! Ngươi đã thu thập đủ " + ItemService.gI().getTemplate(cn.cr).name + ".\n"
                        + "|7|Giờ hãy chọn phần thưởng mà ngươi muốn nhận!",
                        "Nhận Quà", "Đóng");
            } else {
                this.createOtherMenu(player, ConstNpc.BASE_MENU,
                        "\b|7|♦♦♦ CHỨC NĂNG RÈN THỎI VÀNG ♦♦♦\n"
                        + "|5|Cơ Chế Hoạt Động:"
                        + "\b|0|- Dùng Thỏi vàng khóa để rèn thành Thỏi vàng thường.\n"
                        + "- Mỗi lần rèn sẽ tiêu hao số thỏi vàng khóa mà ngươi nhập.\n"
                        + "- Hệ thống sẽ quay ngẫu nhiên hai số từ 1 đến 9.\n"
                        + "- Tổng càng cao → Tỉ lệ thành công càng lớn.\n"
                        + "|5|Kết Quả Sau Rèn:"
                        + "\b|0|- Tổng ≤ 10 và 2 số giống nhau → Thất bại, mất toàn bộ thỏi vàng.\n"
                        + "- Tổng > 10 → Rèn thành công, được hoàn lại một phần thỏi vàng.\n"
                        + "  → Tổng càng lớn, hoàn càng nhiều, tối đa 80% khi tổng = 18.\n"
                        + "|7|Thỏi vàng rèn thành công sẽ được gửi vào Hòm Thư.",
                        "Bắt đầu Rèn", "Đóng");
            }
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (!canOpenNpc(player)) {
            return;
        }

        // Khi chọn "Nhận quà"
        if (player.iDMark.getIndexMenu() == 12) {
            switch (select) {
                case 0 -> {
                    this.createOtherMenu(player, 5,
                            "Cảm ơn ngươi đã mang cà rốt cho ta!\nTa sẽ tặng ngươi phần thưởng.",
                            "Đổi " + cn.slnx + " Ngọc Xanh",
                            "Đổi " + cn.slnh + " Hồng Ngọc",
                            "Đổi " + cn.slbd + " Bản Đồ Kho Báu",
                            "Đổi " + cn.slthoiVang_ + " Thỏi Vàng");
                }
                case 1 -> {
                    /* Đóng */ }
            }
        } // Khi chọn loại phần thưởng
        else if (player.iDMark.getIndexMenu() == 5) {
            Item carot = InventoryService.gI().findItemBag(player, cn.cr);
            if (carot == null || carot.quantity < cn.get_sl_cr) {
                Service.gI().sendThongBao(player, "Bạn chưa có đủ " + ItemService.gI().getTemplate(cn.cr).name);
                return;
            }

            switch (select) {
                case 0 -> { // Ngọc xanh
                    player.inventory.gem += cn.slnx;
                    InventoryService.gI().subQuantityItemsBag(player, carot, cn.get_sl_cr);
                    Service.gI().sendMoney(player);
                    Service.gI().sendThongBao(player, "Bạn vừa nhận được " + cn.slnx + " Ngọc Xanh");
                }
                case 1 -> { // Hồng ngọc
                    player.inventory.ruby += cn.slnh;
                    InventoryService.gI().subQuantityItemsBag(player, carot, cn.get_sl_cr);
                    Service.gI().sendMoney(player);
                    Service.gI().sendThongBao(player, "Bạn vừa nhận được " + cn.slnh + " Hồng Ngọc");
                }
                case 2 -> { // Bản đồ kho báu
                    Item bdkb = ItemService.gI().createNewItem((short) 611, (short) cn.slbd);
                    InventoryService.gI().subQuantityItemsBag(player, carot, cn.get_sl_cr);
                    InventoryService.gI().addItemBag(player, bdkb);
                    InventoryService.gI().sendItemBag(player);
                    Service.gI().sendThongBao(player, "Bạn nhận được " + cn.slbd + " Bản Đồ Kho Báu");
                }
                case 3 -> { // Thỏi vàng
                    Item tv = ItemService.gI().createNewItem((short) 457, (short) cn.slthoiVang_);
                    tv.itemOptions.add(new Item.ItemOption(93, 10));
//                    tv.itemOptions.add(new Item.ItemOption(30, 1));
                    InventoryService.gI().subQuantityItemsBag(player, carot, cn.get_sl_cr);
                    InventoryService.gI().addItemBag(player, tv);
                    InventoryService.gI().sendItemBag(player);
                    Service.gI().sendThongBao(player, "Bạn nhận được " + cn.slthoiVang_ + " Thỏi Vàng");
                }
            }
        } // Menu rèn vàng khóa
        else if (player.iDMark.getIndexMenu() == ConstNpc.BASE_MENU) {
            switch (select) {
                case 0 -> {
                    if (!player.getSession().actived) {
                        Service.gI().sendThongBao(player, "Vui lòng kích hoạt tài khoản để sử dụng chức năng này");
                    } else {
                        Input.gI().DOITHOI(player);
                    }
                }
                case 1 -> {
                }
            }
        }
    }
}
