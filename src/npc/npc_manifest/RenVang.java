package npc.npc_manifest;

import consts.ConstNpc;
import consts.cn;
import item.Item;
import jdbc.daos.NDVSqlFetcher;
import player.Player;
import services.InventoryService;
import services.ItemService;
import services.NpcService;
import services.Service;
import services.TaskService;
import services.func.ChangeMapService;
import utils.Util;
import npc.Npc;

public class RenVang extends Npc {

    private static final int MENU_REN_VANG = 12125;
    private static final int ID_THOI_VANG_KHOA = 457;
    private static final int ID_THOI_VANG_THUONG = 1810;
    private static final int ID_BINH_PHEP = 1259;
    private static final int COST_TVK_PER_TURN = 10;
    private static final int COST_BINH_PHEP_PER_TURN = 1;
    private static final int[] REWARD_TV = {1, 2, 3, 4, 5, 6, 7, 8};
    private static final int[] REWARD_WEIGHTS = {22, 22, 20, 16, 10, 6, 3, 1};

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
                        "Nhận Quà", "Vào map 168", "Đóng");
            } else {
                this.createOtherMenu(player, ConstNpc.BASE_MENU,
                        "\b|7|♦♦♦ CHỨC NĂNG RÈN THỎI VÀNG ♦♦♦\n"
                        + "|5|Cơ Chế Hoạt Động:"
                        + "\b|0|- Mỗi lượt tốn 10 Thỏi vàng khóa + 1 Bình phép.\n"
                        + "- Mỗi lượt nhận ngẫu nhiên 1-8 Thỏi vàng thường.\n"
                        + "|5|Kết Quả Sau Rèn:"
                        + "\b|0|- Có thể rèn nhanh x1, x10 hoặc x100.",
                        "Bắt đầu Rèn", "Map\nĐáy xã hội", "Đóng");
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
                    ChangeMapService.gI().changeMapBySpaceShip(player, 168, -1, -1);
                }
                case 2 -> {
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
                        this.createOtherMenu(player, MENU_REN_VANG,
                                "\b|7|RÈN THỎI VÀNG\n"
                                + "|0|Mỗi lượt tốn 10 Thỏi vàng khóa + 1 Bình phép.\n"
                                + "|0|Mỗi lượt nhận 1-8 Thỏi vàng thường.",
                                "Đổi x1", "Đổi x10", "Đổi x100", "Đóng");
                    }
                }
                case 1 -> {
                    ChangeMapService.gI().changeMapBySpaceShip(player, 168, -1, -1);
                }
                case 2 -> {
                }
            }
        } else if (player.iDMark.getIndexMenu() == MENU_REN_VANG) {
            switch (select) {
                case 0 -> forgeGold(player, 1);
                case 1 -> forgeGold(player, 10);
                case 2 -> forgeGold(player, 100);
                default -> {
                }
            }
        }
    }

    private void forgeGold(Player player, int turns) {
        int needLockedGold = turns * COST_TVK_PER_TURN;
        int needMagicBottle = turns * COST_BINH_PHEP_PER_TURN;
        int lockedGold = countLockedGold(player);
        int magicBottle = countItem(player, ID_BINH_PHEP);
        if (lockedGold < needLockedGold) {
            Service.gI().sendThongBao(player, "Không đủ Thỏi vàng khóa. Cần " + needLockedGold + " thỏi.");
            return;
        }
        if (magicBottle < needMagicBottle) {
            Service.gI().sendThongBao(player, "Không đủ Bình phép. Cần " + needMagicBottle + " bình.");
            return;
        }

        consumeLockedGold(player, needLockedGold);
        consumeItem(player, ID_BINH_PHEP, needMagicBottle);
        InventoryService.gI().sendItemBag(player);

        int reward = 0;
        int[] detail = new int[REWARD_TV.length];
        for (int i = 0; i < turns; i++) {
            int value = rollRewardGold();
            reward += value;
            detail[value - 1]++;
        }

        Item thoiVang = ItemService.gI().createNewItem((short) ID_THOI_VANG_THUONG, reward);
        boolean added = InventoryService.gI().addItemBag(player, thoiVang);
        if (added) {
            InventoryService.gI().sendItemBag(player);
        } else {
            player.inventory.itemsMailBox.add(thoiVang);
            NDVSqlFetcher.updateMailBox(player);
        }

        NpcService.gI().createMenuConMeo(player, ConstNpc.BASE_MENU, -1,
                "\b|5|KẾT QUẢ RÈN\n"
                + "|0|Số lượt: " + turns + "\n"
                + "|0|Tiêu hao: " + needLockedGold + " Thỏi vàng khóa, " + needMagicBottle + " Bình phép\n"
                + "|0|Kết quả: " + reward + " Thỏi vàng thường\n"
                + "|0|Chi tiết: " + rewardDetail(detail) + "\n"
                + (added ? "|7|Thỏi vàng đã vào hành trang." : "|7|Hành trang đầy, thỏi vàng đã gửi vào Hòm Thư."),
                new String[]{"Đóng"});
    }

    private int rollRewardGold() {
        int total = 0;
        for (int weight : REWARD_WEIGHTS) {
            total += weight;
        }
        int rand = Util.nextInt(1, total);
        int current = 0;
        for (int i = 0; i < REWARD_TV.length; i++) {
            current += REWARD_WEIGHTS[i];
            if (rand <= current) {
                return REWARD_TV[i];
            }
        }
        return 1;
    }

    private String rewardDetail(int[] detail) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < detail.length; i++) {
            if (detail[i] > 0) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append(i + 1).append("tv x").append(detail[i]);
            }
        }
        return sb.length() == 0 ? "Không có" : sb.toString();
    }

    private int countLockedGold(Player player) {
        int count = 0;
        for (Item item : player.inventory.itemsBag) {
            if (isLockedGold(item)) {
                count += item.quantity;
            }
        }
        return count;
    }

    private int countItem(Player player, int itemId) {
        int count = 0;
        for (Item item : player.inventory.itemsBag) {
            if (item.isNotNullItem() && item.template.id == itemId) {
                count += item.quantity;
            }
        }
        return count;
    }

    private void consumeLockedGold(Player player, int quantity) {
        int remain = quantity;
        for (Item item : player.inventory.itemsBag) {
            if (remain <= 0) {
                break;
            }
            if (!isLockedGold(item)) {
                continue;
            }
            int sub = Math.min(remain, item.quantity);
            InventoryService.gI().subQuantityItemsBag(player, item, sub);
            remain -= sub;
        }
    }

    private void consumeItem(Player player, int itemId, int quantity) {
        int remain = quantity;
        for (Item item : player.inventory.itemsBag) {
            if (remain <= 0) {
                break;
            }
            if (!item.isNotNullItem() || item.template.id != itemId) {
                continue;
            }
            int sub = Math.min(remain, item.quantity);
            InventoryService.gI().subQuantityItemsBag(player, item, sub);
            remain -= sub;
        }
    }

    private boolean isLockedGold(Item item) {
        return item.isNotNullItem() && item.template.id == ID_THOI_VANG_KHOA;
    }
}
