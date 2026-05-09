package npc.npc_manifest;

/**
 * @author CongHoan
 */
import consts.ConstNpc;
import consts.ConstTask;

import item.Item;

import java.util.ArrayList;
import java.util.List;

import jdbc.daos.PlayerDAO;
import npc.Npc;
import player.Player;
import services.InventoryService;
import services.ItemService;
import services.NpcService;
import services.Service;
import services.TaskService;
import services.func.Input;
import shop.ShopService;

import utils.Util;
import services.PetService;
import task.TaskDanhHieu;
import task.TaskPlayer;

public class OngGohan extends Npc {

    public OngGohan(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    int costNapVang = 1;

    // { tiền, Thỏi vàng(TV) (457) }
    int[][] napVang = {
            { 10000, 30 },
            { 20000, 70 },
            { 50000, 180 },
            { 100000, 350 },
            { 200000, 800 },
            { 500000, 2000 },
            { 1000000, 5000 }
    };

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            if (!TaskService.gI().checkDoneTaskTalkNpc(player, this)) {
                boolean isActived = player.getSession().actived;
                if (isActived) {
                    this.createOtherMenu(player, ConstNpc.BASE_MENU,
                            "Cần Gì nhóc ???"
                                    + "\n|5| Biết điều thì donate! ít thì 5 lít, nhiều thì 5 củ!!!"
                                    + "\b|7| Giftcode: open opentv openspl(mtv)"
                                    + "\b|7| Nhập giftcode xong vui lòng vào hòm thư để nhận!",
                            "Gift-Code",
                            "Quy Đổi",
                            "Nhận 1 triệu\nNgọc xanh",
                            "Hỗ trợ\nNhiệm vụ",
                            "Hòm thư",
                            "Mở\nThành Viên",
                            "Từ chối");
                } else {
                    this.createOtherMenu(player, ConstNpc.BASE_MENU,
                            "Con cần ta giúp gì ?"
                                    + "\n|5| Biết điều thì donate! ít thì 5 lít, nhiều thì 5 củ!!!"
                                    + "\b|7| Giftcode: open opentv openspl(mtv)"
                                    + "\b|7| Nhập giftcode xong vui lòng vào hòm thư để nhận!",
                            "Gift-Code",
                            "Quy Đổi",
                            "Nhận 1 triệu\nNgọc xanh\n[Miễn phí]",
                            "Hỗ trợ\nNhiệm vụ",
                            "Hòm thư",
                            "Mở\nThành Viên",
                            "Từ chối");
                }
            }
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            if (player.iDMark.isBaseMenu()) {
                boolean isActived = player.getSession().actived;
                switch (select) {
                    case 0 ->
                        Input.gI().createFormGiftCode(player);
                    case 1 -> {
                        String npcSay = "Số dư của con là: " + Util.mumberToLouis(player.getSession().cash)
                                + " VND\n"
                                + "Ta đang giữ giúp con:\n"
                                + "- " + Util.mumberToLouis(player.getSession().goldBar + player.getSession().goldBarUnLock) + " TV"
                                + "\b|7|Thuật ngữ viết tắt:\n"
                                + "TV: Thỏi vàng(TV)";
                        createOtherMenu(player, ConstNpc.NAP_TIEN, npcSay,
                                "Quy Đổi\nXu Elite",
                                "Quy Đổi\nThỏi Vàng",
                                "Nhận\nThỏi vàng",
                                "Đóng");
                    }
                    case 2 -> {
                        if (player.inventory.gem >= 10_000_000) {
                            Service.gI().sendThongBao(player, "Nhận lắm ăn lồn à!");
                            return;
                        }
                        int soLuongNgocXanh = 1_000_000;
                        player.inventory.gem += soLuongNgocXanh;
                        Service.gI().sendMoney(player);
                        Service.gI().sendThongBao(player,
                                "Bạn vừa nhận " + Util.mumberToLouis(soLuongNgocXanh) + " ngọc xanh");
                    }
                    case 3 -> {
                        if (TaskService.gI().getIdTask(player) >= ConstTask.TASK_9_0
                                && TaskService.gI().getIdTask(player) < ConstTask.TASK_11_0) {
                            player.playerTask.taskMain.id = 10;
                            player.playerTask.taskMain.index = 0;
                            TaskService.gI().sendNextTaskMain(player);
                            Service.gI().sendThongBao(player, "Bạn đã được hỗ trợ nhiệm vụ thành công");
                        } else if (TaskService.gI().getIdTask(player) >= ConstTask.TASK_16_0
                                && TaskService.gI().getIdTask(player) < ConstTask.TASK_17_0) {
                            player.playerTask.taskMain.id = 17;
                            player.playerTask.taskMain.index = 0;
                            TaskService.gI().sendNextTaskMain(player);
                            Service.gI().sendThongBao(player, "Bạn đã được hỗ trợ nhiệm vụ thành công");
                        } else if (TaskService.gI().getIdTask(player) >= ConstTask.TASK_18_0
                                && TaskService.gI().getIdTask(player) < ConstTask.TASK_20_0) {
                            player.playerTask.taskMain.id = 19;
                            player.playerTask.taskMain.index = 0;
                            TaskService.gI().sendNextTaskMain(player);
                            Service.gI().sendThongBao(player, "Bạn đã được hỗ trợ nhiệm vụ thành công");
                        } else {
                            Service.gI().sendThongBao(player,
                                    "Chỉ hỗ trợ nhiệm vụ tàu pảy pảy và nhiệm vụ DHVT, Trung úy trắng");
                        }
                    }
                    case 4 ->
                        this.createOtherMenu(player, ConstNpc.MAIL_BOX,
                                "|0|Website: nrohorizon.online\n"
                                        + "|7|Lưu ý: Nếu không mở được Hòm Thư <-> Hãy XÓA DỮ LIỆU!\n"
                                        + "|7|Lưu ý: Nếu không mở được Hòm Thư <-> Hãy XÓA DỮ LIỆU!\n"
                                        + "|7|Lưu ý: Nếu không mở được Hòm Thư <-> Hãy XÓA DỮ LIỆU!\n",
                                "Hòm Thư\n(" + (player.inventory.itemsMailBox.size()
                                        - InventoryService.gI().getCountEmptyListItem(player.inventory.itemsMailBox))
                                        + " món)",
                                "Xóa Hết\nHòm Thư", "Đóng");
                    case 5 -> {
                        if (!isActived) {
                            this.createOtherMenu(player, 6789,
                                    "|7| MỞ THÀNH VIÊN\n"
                                            + "|0| Có 2 cách mở:\n"
                                            + "- FREE: 80 tỉ sức mạnh + xong nhiệm vụ 23\n"
                                            + "- Hoặc nạp 10K mở ngay\n\n"
                                            + "Sức mạnh hiện tại: " + Util.mumberToLouis(player.nPoint.power) + "\n"
                                            + "Task hiện tại: " + TaskService.gI().getIdTask(player) + "\n"
                                            + "Số dư: " + player.getSession().cash + " VND",
                                    "Mở FREE",
                                    "Mở 10K",
                                    "Đóng");
                        } else {
                            Service.gI().sendThongBao(player, "Bạn đã mở thành viên rồi");
                        }
                    }
                    case 6 ->
                        Service.gI().sendThongBao(player, "Bạn đã từ chối");
                }
            } else if (player.iDMark.getIndexMenu() == 6789) {
                switch (select) {

                    // =========================
                    // ⭐ NÚT 0 → MỞ FREE
                    // =========================
                    case 0 -> {

                        long sucManhCan = 80_000_000_000L;
                        long sucManh = player.nPoint.power;

                        int task = TaskService.gI().getIdTask(player);

                        boolean duSucManh = sucManh >= sucManhCan;
                        boolean xongTask = task >= ConstTask.TASK_23_0;

                        if (duSucManh && xongTask) {
                            PlayerDAO.updateActive(player, 1);
                            Service.gI().sendThongBao(player, "Bạn đã mở thành viên FREE!");
                            Service.gI().sendLogout(player);
                        } else {

                            StringBuilder msg = new StringBuilder();
                            msg.append("Bạn chưa đủ điều kiện mở FREE\n");

                            if (!duSucManh) {
                                msg.append("- Cần 80 tỉ sức mạnh\n");
                            }

                            if (!xongTask) {
                                msg.append("- Chưa hoàn thành nhiệm vụ fide\n");
                            }

                            Service.gI().sendThongBao(player, msg.toString());
                        }
                    }

                    // =========================
                    // ⭐ NÚT 1 → MỞ 10K
                    // =========================
                    case 1 -> {

                        int price = 10000;

                        if (player.getSession().cash >= price) {
                            PlayerDAO.subcash(player, price);
                            PlayerDAO.updateActive(player, 1);
                            Service.gI().sendThongBao(player, "Bạn đã mở thành viên thành công");
                            Service.gI().sendLogout(player);
                        } else {
                            Service.gI().sendThongBao(player, "Bạn không đủ 10K");
                        }
                    }

                    case 2 -> {
                        // đóng menu
                    }
                }
            } else if (player.iDMark.getIndexMenu() == ConstNpc.MAIL_BOX) { // Menu hộp thư
                switch (select) {
                    case 0 ->
                        ShopService.gI().opendShop(player, "ITEMS_MAIL_BOX", true);
                    case 1 ->
                        NpcService.gI().createMenuConMeo(player,
                                ConstNpc.CONFIRM_REMOVE_ALL_ITEM_MAIL_BOX, this.avartar,
                                "|3|Bạn chắc muốn xóa hết vật phẩm trong hòm thư?\n"
                                        + "|7|Sau khi xóa sẽ không thể khôi phục!",
                                "Đồng ý", "Hủy bỏ");
                    case 2 -> {
                        // Đóng menu
                    }
                }
            } else if (player.iDMark.getIndexMenu() == ConstNpc.NAP_TIEN) {
                switch (select) {
                    case 0 -> {
                        this.createOtherMenu(player, ConstNpc.DOIXUOCEAN,
                                "Ta sẽ tạm giữ giúp con\n"
                                        + "Nếu con cần dùng tới hãy quay lại đây gặp ta!",
                                "10.000\n100 Xu\nElite",
                                "20.000\n220 Xu\nElite",
                                "30.000\n330 Xu\nElite",
                                "50.000\n560 Xu\nElite",
                                "100.000\n1.150 Xu\nElite",
                                "200.000\n2.340 Xu\nElite",
                                "500.000\n6.000 Xu\nElite",
                                "1.000.000\n12.500 Xu\nElite");
                        return;
                    }
                    case 1 -> {
                        List<String> menu = new ArrayList<>();
                        for (int i = 0; i < napVang.length; i++) {
                            String text = Util.mumberToLouis(napVang[i][0]) + "\n"
                                    + napVang[i][1] + " TV";
                            menu.add(text);
                        }
                        createOtherMenu(player, ConstNpc.NAP_VANG,
                                "Ta sẽ giữ giúp con\nNếu cần hãy quay lại gặp ta!",
                                menu.toArray(String[]::new));
                        return;
                    }
                    case 2 -> {
                        List<Item> listItem = new ArrayList<>();

                        int totalGoldBar = player.getSession().goldBar + player.getSession().goldBarUnLock;

                        if (totalGoldBar > 0) {
                            listItem.add(ItemService.gI().createNewItem(
                                    (short) 457, totalGoldBar));
                        }

                        if (InventoryService.gI().getCountEmptyBag(player) < listItem.size()) {
                            Service.gI().sendThongBao(player,
                                    "Cần ít nhất " + listItem.size() + " ô trống trong hành trang");
                            return;
                        }

                        for (Item it : listItem) {
                            InventoryService.gI().addItemBag(player, it);
                        }
                        InventoryService.gI().sendItemBag(player);

                        PlayerDAO.subGoldBar(player, player.getSession().goldBar);
                        PlayerDAO.subGoldBarUnLock(player, player.getSession().goldBarUnLock);

                        Service.gI().sendThongBao(player, "Bạn đã nhận toàn bộ thỏi vàng");
                    }
                }
            } else if (player.iDMark.getIndexMenu() == ConstNpc.NAP_VANG) {
                if (player.getSession().cash >= napVang[select][0]) {
                    int vndExchange = napVang[select][0];
                    if (PlayerDAO.subcash(player, vndExchange)) {
                        PlayerDAO.subGoldBar(player, -napVang[select][1]);
                        addDanhHieuNapProgress(player, vndExchange);
                        Service.gI().sendThongBao(player,
                                "Bạn nhận được:\n"
                                        + napVang[select][1] + " TV");
                    } else {
                        Service.gI().sendThongBao(player, "Có lỗi khi quy đổi, vui lòng thử lại");
                    }
                } else {
                    Service.gI().sendThongBao(player, "Không đủ số dư");
                }
            } else if (player.iDMark.getIndexMenu() == ConstNpc.DOIXUOCEAN) {
                switch (select) {
                    case 0 ->
                        exchangeXukrai(player, 10_000, 100); // 10.000 -> 100 Xu
                    case 1 ->
                        exchangeXukrai(player, 20_000, 220); // 20.000 -> 220 Xu
                    case 2 ->
                        exchangeXukrai(player, 30_000, 330); // 30.000 -> 330 Xu
                    case 3 ->
                        exchangeXukrai(player, 50_000, 560); // 50.000 -> 560 Xu
                    case 4 ->
                        exchangeXukrai(player, 100_000, 1_150); // 100.000 -> 1.150 Xu
                    case 5 ->
                        exchangeXukrai(player, 200_000, 2_340); // 200.000 -> 2.340 Xu
                    case 6 ->
                        exchangeXukrai(player, 500_000, 6_000); // 500.000 -> 6.000 Xu
                    case 7 ->
                        exchangeXukrai(player, 1_000_000, 12_500);// 1.000.000 -> 12.500 Xu
                }
            }
        }
    }

    private void exchangeXukrai(Player player, int i, int i0) {
        if (player.getSession().cash >= i) {
            if (PlayerDAO.subcash(player, i)) {
                addDanhHieuNapProgress(player, i);
                Item xu = ItemService.gI().createNewItem((short) 1705, i0);
                InventoryService.gI().addItemBag(player, xu);
                Service.gI().sendMoney(player);
                InventoryService.gI().sendItemBag(player);
                Service.gI().sendThongBao(player, "Bạn có vừa có thêm " + i0 + "xu");
            } else {
                Service.gI().sendThongBao(player, "Có lỗi khi quy đổi, vui lòng thử lại");
            }
        } else {
            Service.gI().sendThongBao(player, "Bạn không đủ số dư");
        }
    }

    private void addDanhHieuNapProgress(Player player, int exchangedVnd) {
        if (exchangedVnd <= 0) {
            return;
        }
        if (player.playerTask == null) {
            player.playerTask = new TaskPlayer();
        }
        if (player.playerTask.taskdh == null) {
            player.playerTask.taskdh = new TaskDanhHieu();
        }
        int newProgress = player.playerTask.taskdh.Nap + exchangedVnd;
        player.playerTask.taskdh.Nap = Math.min(newProgress, 1_000_000);
        player.effect.setPointDaiGiaMoiNhu(player.playerTask.taskdh.Nap);
    }
}