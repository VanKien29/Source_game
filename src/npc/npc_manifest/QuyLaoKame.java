package npc.npc_manifest;

/**
 *
 * @author CongHoan
 */
import clan.Clan;
import consts.ConstNpc;
import item.Item;
import java.util.ArrayList;
import jdbc.daos.PlayerDAO;
import models.TreasureUnderSea.TreasureUnderSea;
import models.TreasureUnderSea.TreasureUnderSeaService;
import npc.Npc;
import static npc.NpcFactory.PLAYERID_OBJECT;
import player.Archivement;
import player.Player;
import services.InventoryService;
import services.ItemService;
import services.NpcService;
import services.RewardService;
import services.Service;
import services.TaskService;
import services.func.ChangeMapService;
import services.func.Input;
import services.func.TopService;
import shop.ShopService;
import utils.Util;

public class QuyLaoKame extends Npc {

    public QuyLaoKame(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
        String[] cauNoi = {
            "La là lá la... Ta là Vua Lọ"
        };

        Npc.autoChat(this, cauNoi, 1, 3); // 10s bắt đầu, 60s lặp lại
    }

    @Override
    public void openBaseMenu(Player player) {
        this.npcChat(player, "La là lá la... ta đang luyện tập võ công tuyệt thế!");
        Item ruacon = InventoryService.gI().findItemBag(player, 874);
        if (canOpenNpc(player)) {
            ArrayList<String> menu = new ArrayList<>();
            if (!player.canReward) {
                menu.add("Nói\nchuyện");
                //  menu.add("Map úp\nMHBT");
                // menu.add("Quà\nMốc Nạp");
                menu.add("Quà mốc\n nạp");
                // menu.add("Điểm\nTích nạp");
                menu.add("Đổi điểm\nTích nạp\n[" + player.inventory.coupon + "]");
                int diemReceive = (player.getSession().danap / 1_000) - player.getSession().diemReceive;
                menu.add("Nạp nhận\nTích nạp\n[" + (diemReceive <= 0 ? 0 : diemReceive) + "]");
                menu.add("Hòm Thư");
                menu.add("Sự kiện\nbé ngoan");
//                menu.add("Nhận quà\nKOL");

                // menu.add("Bảng\n Xếp hạng\nNhiệm vụ");
                if (ruacon != null && ruacon.quantity >= 1) {
                    menu.add("Giao\n Bé na");
                }
            } else {
                menu.add("Giao\n Bé na");
            }
            String[] menus = menu.toArray(String[]::new);
            if (!TaskService.gI().checkDoneTaskTalkNpc(player, this)) {
                this.createOtherMenu(player, ConstNpc.BASE_MENU, "Con muốn hỏi gì nào?", menus);
            }
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            if (player.canReward) {
                RewardService.gI().rewardLancon(player);
                return;
            }
            switch (player.iDMark.getIndexMenu()) {
                case ConstNpc.BASE_MENU -> {
                    switch (select) {
                        case 0 -> {
                            ArrayList<String> menu = new ArrayList<>();
                            menu.add("Nhiệm vụ");
                            menu.add("Học\nKỹ năng");
                            Clan clan = player.clan;
                            if (clan != null) {
                                menu.add("Về khu\nvực bang");
                                if (clan.isLeader(player)) {
                                    menu.add("Giải tán\nBang hội");
                                }
                            }
                            menu.add("Kho báu\ndưới biển");
                            String[] menus = menu.toArray(String[]::new);

                            this.createOtherMenu(player, 0,
                                    "Chào con, ta rất vui khi gặp con\nCon muốn làm gì nào ?", menus);
                        }
//                        case 1 -> {
//                            ChangeMapService.gI().changeMapNonSpaceship(player, 156, -1, -1);
//                        }
                        case 1 -> {
                            if (player.getSession().actived) {
                                Archivement.gI().getAchievement(player);
                            } else {
                                Service.gI().sendThongBao(player, "Mở thành viên đi rồi qua đây nhận nhe baby!");
                            }
                        }
                        case 2 -> {
                            ShopService.gI().opendShop(player, "SHOP_QUY_LAO", false);
                        }
                        case 3 -> {
                            int pointReceive = (player.getSession().danap / 1_000) - player.getSession().diemReceive;
                            if (pointReceive > 0) {
                                if (!PlayerDAO.addPointEvent(player, pointReceive)) {
                                    Service.gI().sendThongBao(player, "Đã có lỗi xảy ra, vui lòng thử lại");
                                    return;
                                }
                                player.inventory.coupon += pointReceive;
                                Service.gI().sendThongBao(player, "Bạn đã nhận được " + pointReceive + " điểm sự kiện");
                            } else {
                                Service.gI().sendThongBao(player, "Có nạp thêm đồng nào đâu mà đòi nhận, đi nạp thêm đi con");
                            }
                        }
                        case 4 -> {
                            this.createOtherMenu(player, ConstNpc.MAIL_BOX,
                                    "|0|Website: nrohorizon.online\n"
                                    + "|7|Lưu ý: Nếu không mở được Hòm Thư <-> Hãy XÓA DỮ LIỆU!",
                                    "Hòm Thư\n(" + (player.inventory.itemsMailBox.size()
                                    - InventoryService.gI()
                                            .getCountEmptyListItem(player.inventory.itemsMailBox))
                                    + " món)",
                                    "Xóa Hết\nHòm Thư", "Đóng");
                            break;
                        }
                        case 5 -> {
                            Item phieubengoan = InventoryService.gI().findItemBag(player, 1194);
                            int soLuong = (phieubengoan != null) ? phieubengoan.quantity : 0;
                            this.createOtherMenu(player, ConstNpc.ĐỔI_PHIẾU_BÉ_NGOAN,
                                    "Con đang có " + soLuong + " phiếu bé ngoan\n"
                                    + "Con muốn đổi quà gì nào ?\n"
                                    + "2 phiếu: Xí muội hoa đào +20%sd trong 10 phút\n"
                                    + "2 phiếu: Xí muội hoa mai +20%hp ki trong 10 phút\n"
                                    + "30 phiếu: Mèo mun đột biến(đeo lưng) hsd 5 ngày 10% sd hp ki\n"
                                    + "50 phiếu: Lý Tiểu Nương Rực Rỡ hsd 15 hoặc 30 ngày 25% sd hp ki"
                                    + "\b|5| Hãy tìm bọn Bojack để đoạt lấy phiếu bé ngoan",
                                    "2", "2", "30", "50", "Đóng");
                            break;
                        }
//                        case 4 -> {
//                            this.createOtherMenu(player, ConstNpc.MENU_TET_2025,
//                                    "Con muốn gì nào ?\n",
//                                    "Top\nPháo bông\nVIP", "Top\n Lì xì VIP", "Cửa hàng", "Đóng");
//                            break;
//                        }
//                        case 6 -> {
//                            if (player.playerTask.kolTask.template != null) {
//                                String npcSay = "Nhiệm vụ " + (player.playerTask.kolTask.template.id + 1) + ":"
//                                        + "\n" + player.playerTask.kolTask.getTaskInfo()
//                                        + "\n" + player.playerTask.kolTask.getRewardsInfo()
//                                        + "\nHoàn thành: " + player.playerTask.kolTask.count + "/" + player.playerTask.kolTask.template.max_count + " (" + player.playerTask.kolTask.getPercentProcess() + "%)";
//                                this.createOtherMenu(player, ConstNpc.RECEIVE_KOL_TASK, npcSay, player.playerTask.kolTask.isDone() ? "Trả\nnhiệm vụ" : "Đóng");
//                            }
//                        }
                        // case 4 -> {
                        // Item ruacon = InventoryService.gI().findItemBag(player, 874);
                        // if (ruacon != null && ruacon.quantity >= 1) {
                        // this.createOtherMenu(player, 1,
                        // "Cảm ơn cậu đã cứu con rùa của ta\nĐể cảm ơn ta sẽ tặng cậu món quà.",
                        // "Nhận quà", "Đóng");
                        // break;
                        // }
                        // }
                    }
                }
                case ConstNpc.MENU_TET_2025 -> {
                    switch (select) {
                        case 0: {
                            TopService.showListTop(player, 6);
                            break;
                        }
                        case 1: {
                            TopService.showListTop(player, 7);
                            break;
                        }
                        case 2: {
                            ShopService.gI().opendShop(player, "SHOP_QUY_LAO", false);
                            break;
                        }
                    }

                }
                case ConstNpc.ĐỔI_PHIẾU_BÉ_NGOAN -> {
                    switch (select) {
                        case 0: {
                            if (InventoryService.gI().getCountEmptyBag(player) < 2) {
                                Service.gI().sendThongBao(player, "Cần 2 ô hành trang mới có thể đổi!!!");
                                return;
                            }
                            int soluong = 2;
                            Item phieubengoan = InventoryService.gI().findItemBag(player, 1194);
                            if (phieubengoan == null || phieubengoan.quantity < soluong) {
                                Service.gI().sendThongBao(player, "Cần ít nhất " + soluong + " Phiếu Bé Ngoan để đổi!!!");
                                return;
                            }
                            InventoryService.gI().subQuantityItemsBag(player, phieubengoan, soluong);
                            Item ximuoidoadao = ItemService.gI().createNewItem((short) 1195);
                            InventoryService.gI().addItemBag(player, ximuoidoadao);
                            InventoryService.gI().sendItemBag(player);
                            Service.gI().sendThongBao(player, "Đổi thành công! Bạn đã nhận được vật phẩm.");
                            break;
                        }
                        case 1: {
                            if (InventoryService.gI().getCountEmptyBag(player) < 2) {
                                Service.gI().sendThongBao(player, "Cần 2 ô hành trang mới có thể đổi!!!");
                                return;
                            }
                            int soluong = 2;
                            Item phieubengoan = InventoryService.gI().findItemBag(player, 1194);
                            if (phieubengoan == null || phieubengoan.quantity < soluong) {
                                Service.gI().sendThongBao(player, "Cần ít nhất " + soluong + " Phiếu Bé Ngoan để đổi!!!");
                                return;
                            }
                            InventoryService.gI().subQuantityItemsBag(player, phieubengoan, soluong);
                            Item ximuoidoamai = ItemService.gI().createNewItem((short) 1196);
                            InventoryService.gI().addItemBag(player, ximuoidoamai);
                            InventoryService.gI().sendItemBag(player);
                            Service.gI().sendThongBao(player, "Đổi thành công! Bạn đã nhận được vật phẩm.");
                            break;
                        }
                        case 2: {
                            if (InventoryService.gI().getCountEmptyBag(player) < 2) {
                                Service.gI().sendThongBao(player, "Cần 2 ô hành trang mới có thể đổi!!!");
                                return;
                            }
                            int soluong = 30;
                            Item phieubengoan = InventoryService.gI().findItemBag(player, 1194);
                            if (phieubengoan == null || phieubengoan.quantity < soluong) {
                                Service.gI().sendThongBao(player, "Cần ít nhất " + soluong + " Phiếu Bé Ngoan để đổi!!!");
                                return;
                            }
                            InventoryService.gI().subQuantityItemsBag(player, phieubengoan, soluong);
                            Item meomun = ItemService.gI().createNewItem((short) 1140);
                            meomun.itemOptions.add(new Item.ItemOption(50, 10));
                            meomun.itemOptions.add(new Item.ItemOption(77, 10));
                            meomun.itemOptions.add(new Item.ItemOption(103, 10));
                            meomun.itemOptions.add(new Item.ItemOption(93, 5));
                            InventoryService.gI().addItemBag(player, meomun);
                            InventoryService.gI().sendItemBag(player);
                            Service.gI().sendThongBao(player, "Đổi thành công! Bạn đã nhận được vật phẩm.");
                            break;
                        }
                        case 3: {
                            if (InventoryService.gI().getCountEmptyBag(player) < 2) {
                                Service.gI().sendThongBao(player, "Cần 2 ô hành trang mới có thể đổi!!!");
                                return;
                            }
                            int soluong = 50;
                            Item phieubengoan = InventoryService.gI().findItemBag(player, 1194);
                            if (phieubengoan == null || phieubengoan.quantity < soluong) {
                                Service.gI().sendThongBao(player, "Cần ít nhất " + soluong + " Phiếu Bé Ngoan để đổi!!!");
                                return;
                            }
                            InventoryService.gI().subQuantityItemsBag(player, phieubengoan, soluong);
                            Item bunma = ItemService.gI().createNewItem((short) 1756);
                            bunma.itemOptions.add(new Item.ItemOption(50, 25));
                            bunma.itemOptions.add(new Item.ItemOption(77, 25));
                            bunma.itemOptions.add(new Item.ItemOption(103, 25));
                            bunma.itemOptions.add(new Item.ItemOption(93, Util.nextInt(15, 30)));
                            InventoryService.gI().addItemBag(player, bunma);
                            InventoryService.gI().sendItemBag(player);
                            Service.gI().sendThongBao(player, "Đổi thành công! Bạn đã nhận được vật phẩm.");
                            break;
                        }
                    }

                }
                case ConstNpc.MAIL_BOX -> {
                    switch (select) {
                        case 0:
                            // player.inventory.itemsMailBox.clear();
                            // player.inventory.itemsMailBox.addAll(GodGK.getMailBox(player));
                            ShopService.gI().opendShop(player, "ITEMS_MAIL_BOX", true);
                            break;
                        case 1:
                            NpcService.gI().createMenuConMeo(player,
                                    ConstNpc.CONFIRM_REMOVE_ALL_ITEM_MAIL_BOX, this.avartar,
                                    "|3|Bạn chắc muốn xóa hết vật phẩm trong hòm thư?\n"
                                    + "|7|Sau khi xóa sẽ không thể khôi phục!",
                                    "Đồng ý", "Hủy bỏ");
                            break;
                        case 2:
                            break;
                    }

                }

                case 1115 -> {
                    if (select == 0) {
                        if (player.getSession().actived) {
                            Archivement.gI().getAchievement(player);
                        } else {
                            Service.gI().sendThongBao(player, "Mở thành viên đi rồi qua đây nhận nhe baby!");
                        }
                    }
                }
                case 0 -> {
                    switch (select) {
                        case 0 ->
                            NpcService.gI().createTutorial(player, tempId, avartar,
                                    player.playerTask.taskMain.subTasks.get(player.playerTask.taskMain.index).name);
                        case 1 ->
                            Service.gI().sendThongBao(player, "Bạn đã học hết các kỹ năng");
                        case 2 -> {
                            Clan clan = player.clan;
                            if (clan != null && select == 2) {
                                if (player.nPoint.power > 60_000_000_000L) { // kiểm tra power > 80 tỷ
                                    ChangeMapService.gI().changeMapNonSpaceship(player, 156, Util.nextInt(392, 400), 192);
                                } else {
                                    Service.gI().sendThongBao(player, "Sức mạnh của con chưa đủ 60 tỷ để vào map này!");
                                }
                            } else {
                                if (player.clan != null && player.clan.BanDoKhoBau != null) {
                                    this.createOtherMenu(player, ConstNpc.MENU_OPENED_DBKB,
                                            "Bang hội con đang ở hang kho báu cấp "
                                            + player.clan.BanDoKhoBau.level + "\ncon có muốn đi cùng họ không?",
                                            "Top\nBang hội", "Thành tích\nBang", "Đồng ý", "Từ chối");
                                } else {
                                    this.createOtherMenu(player, ConstNpc.MENU_OPEN_DBKB,
                                            "Đây là bản đồ kho báu hải tặc tí hon\nCác con cứ yên tâm lên đường\nỞ đây có ta lo\nNhớ chọn cấp độ vừa sức mình nhé",
                                            "Top\nBang hội", "Thành tích\nBang", "Chọn\ncấp độ", "Từ chối");
                                }
                            }
                        }
                        case 3 -> {
                            boolean clanCheck = true;
                            Clan clan = player.clan;
                            if (clan != null) {
                                clanCheck = false;
                                if (clan.isLeader(player)) {
                                    createOtherMenu(player, 3, "Con có chắc muốn giải tán bang hội không?", "Đồng ý",
                                            "Từ chối");
                                } else {
                                    clanCheck = true;
                                }
                            }
                            if (clanCheck) {
                                if (player.clan != null && player.clan.BanDoKhoBau != null) {
                                    this.createOtherMenu(player, ConstNpc.MENU_OPENED_DBKB,
                                            "Bang hội con đang ở hang kho báu cấp "
                                            + player.clan.BanDoKhoBau.level + "\ncon có muốn đi cùng họ không?",
                                            "Top\nBang hội", "Thành tích\nBang", "Đồng ý", "Từ chối");
                                } else {
                                    this.createOtherMenu(player, ConstNpc.MENU_OPEN_DBKB,
                                            "Đây là bản đồ kho báu hải tặc tí hon\nCác con cứ yên tâm lên đường\nỞ đây có ta lo\nNhớ chọn cấp độ vừa sức mình nhé",
                                            "Top\nBang hội", "Thành tích\nBang", "Chọn\ncấp độ", "Từ chối");
                                }
                            }
                        }
                        case 4 -> {
                            if (player.clan != null && player.clan.BanDoKhoBau != null) {
                                this.createOtherMenu(player, ConstNpc.MENU_OPENED_DBKB,
                                        "Bang hội con đang ở hang kho báu cấp "
                                        + player.clan.BanDoKhoBau.level + "\ncon có muốn đi cùng họ không?",
                                        "Top\nBang hội", "Thành tích\nBang", "Đồng ý", "Từ chối");
                            } else {
                                this.createOtherMenu(player, ConstNpc.MENU_OPEN_DBKB,
                                        "Đây là bản đồ kho báu hải tặc tí hon\nCác con cứ yên tâm lên đường\nỞ đây có ta lo\nNhớ chọn cấp độ vừa sức mình nhé",
                                        "Top\nBang hội", "Thành tích\nBang", "Chọn\ncấp độ", "Từ chối");
                            }
                        }
                    }
                }
                case 3 -> {
                    Clan clan = player.clan;
                    if (clan != null) {
                        if (clan.isLeader(player)) {
                            if (select == 0) {
                                Input.gI().createFormGiaiTanBangHoi(player);
                            }
                        }
                    }
                }
//                case ConstNpc.RECEIVE_KOL_TASK -> {
//                    switch (select) {
//                        case 0 -> {
//                            if (player.playerTask.kolTask.isDone()) {
//                                player.playerTask.kolTask.receive(player);
//                            }
//                        }
//                    }
//                }
                case ConstNpc.MENU_OPENED_DBKB -> {
                    switch (select) {
                        case 2 -> {
                            if (player.clan == null) {
                                Service.gI().sendThongBao(player, "Hãy vào bang hội trước");
                                return;
                            }
                            if (player.isAdmin() || player.nPoint.power >= TreasureUnderSea.POWER_CAN_GO_TO_DBKB) {
                                ChangeMapService.gI().goToDBKB(player);
                            } else {
                                this.npcChat(player, "Yêu cầu sức mạnh lớn hơn "
                                        + Util.numberToMoney(TreasureUnderSea.POWER_CAN_GO_TO_DBKB));
                            }
                        }

                    }
                }
//                case ConstNpc.MENU_OPEN_DBKB -> {
//                    switch (select) {
//                        case 2 -> {
//                            if (player.clan == null) {
//                                Service.gI().sendThongBao(player, "Hãy vào bang hội trước");
//                                return;
//                            }
//                            if (player.isAdmin()) {
//                                Input.gI().createFormChooseLevelBDKB(player);
//                            } else {
//                                this.npcChat(player, "Chức năng tạm đóng để đua top "
//                                        + Util.numberToMoney(TreasureUnderSea.POWER_CAN_GO_TO_DBKB));
//                            }
//                        }
//
//                    }
//                }
                case ConstNpc.MENU_OPEN_DBKB -> {
                    switch (select) {
                        case 2 -> {
                            if (player.clan == null) {
                                Service.gI().sendThongBao(player, "Hãy vào bang hội trước");
                                return;
                            }
                            if (player.isAdmin() || player.nPoint.power >= TreasureUnderSea.POWER_CAN_GO_TO_DBKB) {
                                Input.gI().createFormChooseLevelBDKB(player);
                            } else {
                                this.npcChat(player, "Yêu cầu sức mạnh lớn hơn "
                                        + Util.numberToMoney(TreasureUnderSea.POWER_CAN_GO_TO_DBKB));
                            }
                        }

                    }
                }
                case ConstNpc.MENU_ACCEPT_GO_TO_BDKB -> {
                    switch (select) {
                        case 0 ->
                            TreasureUnderSeaService.gI().openBanDoKhoBau(player,
                                    Byte.parseByte(String.valueOf(PLAYERID_OBJECT.get(player.id))));
                    }
                }
            }
        }
    }

}
