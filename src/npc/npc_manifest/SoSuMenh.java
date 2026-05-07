package npc.npc_manifest;

import consts.ConstNpc;
import consts.cn;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jdbc.DBConnecter;
import jdbc.daos.PlayerDAO;

import npc.Npc;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import player.Archivement;
import player.Player;
import services.InventoryService;
import services.ItemService;
import services.NpcService;
import services.PetService;
import services.Service;
import services.func.TopService;
import shop.ShopService;
import sosumenh.SoSuMenhManager;
import sosumenh.SoSuMenhService;
import sosumenh.SoSuMenhTaskMain;
import sosumenh.SoSuMenhTaskTemplate;

public class SoSuMenh extends Npc {

    public SoSuMenh(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            List<String> menu = new ArrayList<>(Arrays.asList(
                    "Xem phần thưởng", // 0
                    "Nhận Thưởng", // 1
                    "Mở Sổ Vip", // 2
                    "Mua lever\n [5k/level]", // 3
                    "Xem xếp hạng", // 4
                    "Xem Thông\n Tin Sổ", // 5
                    "Hòm Thư" // 6
            ));

            String[] menus = menu.toArray(new String[0]);
            createOtherMenu(player, ConstNpc.BASE_MENU,
                    "SỔ VŨ TRỤ NGỌC RỒNG HORIZON MÙA 1\nChào mừng đến với bình nguyên vô vọng", menus);
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (!canOpenNpc(player)) {
            return;
        }

        if (player.iDMark.isBaseMenu()) {
            switch (select) {
                case 0 -> {
                    // Gộp thành menu con
                    createOtherMenu(player, ConstNpc.MENU_REWARD,
                            "Chọn loại phần thưởng muốn xem:",
                            "Sổ thường", "Sổ VIP", "Thoát");
                    break;
                }
                case 1 -> {
                    this.createOtherMenu(player, 9999,
                            "Nhận thưởng sổ ở đây",
                            "Sổ thường", "Sổ Vip", "Từ chối");
                    break;
                }
                case 2 -> {
                    if (player.sosumenhplayer.isVip()) {
                        Service.gI().sendThongBao(player, "Bạn đã mở vip sổ rồi không thể mở tiếp!");
                        return;
                    }
                    if (player.getSession() != null && player.getSession().cash < 100_000) {
                        Service.gI().sendThongBao(player, "Bạn không đủ 100k VND");
                        return;
                    }
                    createOtherMenu(player, ConstNpc.CONFIRM_OPEN_VIP,
                            "Bạn có chắc chắn muốn mở VIP sổ với giá 100.000 VND không?",
                            "Có", "Không");
                    break;
                }
                case 3 -> {
                    // Thêm confirm trước khi mua level
                    if (player.sosumenhplayer.getLevel() >= 20) {
                        Service.gI().sendThongBao(player, "Đã đạt cấp độ tối đa không thể nâng cấp");
                        return;
                    }
                    if (player.getSession() != null && player.getSession().cash < 5_000) {
                        Service.gI().sendThongBao(player, "Bạn không đủ 5k VND");
                        return;
                    }
                    createOtherMenu(player, ConstNpc.CONFIRM_BUY_LEVEL,
                            "Bạn có chắc muốn mua 1 cấp độ sổ với giá 5.000 VND không?",
                            "Đồng ý", "Hủy bỏ");
                    break;
                }
                case 4 -> {
                    TopService.showListTop(player, 9);
                    break;
                }
                case 5 -> {
                    try {
                        List<SoSuMenhTaskMain> doneTasks = new ArrayList<>();
                        List<SoSuMenhTaskMain> notDoneTasks = new ArrayList<>();

                        for (SoSuMenhTaskMain task : player.sosumenhplayer.ssmTaskMain) {
                            if (task.finish) {
                                doneTasks.add(task);
                            } else {
                                notDoneTasks.add(task);
                            }
                        }

                        StringBuilder name = new StringBuilder();
                        StringBuilder name1 = new StringBuilder();

                        for (SoSuMenhTaskMain t : doneTasks) {
                            SoSuMenhTaskTemplate task = SoSuMenhManager.getInstance().findById(t.idTask);
                            if (task != null) {
                                name.append("- ").append(task.getTask()).append(" (").append(t.countTask).append("/").append(t.maxCount).append(")\n");
                            }
                        }

                        for (SoSuMenhTaskMain t : notDoneTasks) {
                            SoSuMenhTaskTemplate task = SoSuMenhManager.getInstance().findById(t.idTask);
                            if (task != null) {
                                name1.append("- ").append(task.getTask()).append(" (").append(t.countTask).append("/").append(t.maxCount).append(")\n");
                            }
                        }

                        int level = player.sosumenhplayer.getLevel();
                        int point = player.sosumenhplayer.getPoint();
                        int maxPoint = (level + 1) * 100;
                        int remaining = Math.max(0, maxPoint - point);
                        int percent = (int) ((point * 100.0) / maxPoint);

                        String levelProgress = "\b|2|Cấp độ hiện tại: " + level
                                + "\nTổng điểm: " + point + " / " + maxPoint + " (" + percent + "%)"
                                + "\n=> Còn " + remaining + " điểm nữa để lên cấp " + (level + 1);

                        String vip = player.sosumenhplayer.isVip()
                                ? "\b|5|(Đã mở sổ Vip)"
                                : "\b|3|(Chưa mở sổ Vip)";

                        String message = "\b|5|Cấp độ Sổ Horizon hiện tại: " + level + " " + vip
                                + "\n" + levelProgress
                                + "\n\n\b|2|--- Nhiệm vụ đã hoàn thành ---\n" + name
                                + "\n\b|3|--- Nhiệm vụ chưa hoàn thành ---\n" + name1;

                        Service.gI().sendThongBaoFromAdmin(player, message);

                    } catch (Exception e) {
                        e.printStackTrace();
                        Service.gI().sendThongBaoFromAdmin(player, "Đã xảy ra lỗi khi hiển thị thông tin Sổ Sứ Mệnh!");
                    }
                    break;
                }

                case 6 -> {
                    this.createOtherMenu(player, ConstNpc.MAIL_BOX,
                            "|5|HÒM THƯ\n"
                            + "|0|Số vật phẩm trong thư: "
                            + (player.inventory.itemsMailBox.size()
                            - InventoryService.gI().getCountEmptyListItem(player.inventory.itemsMailBox))
                            + " món",
                            "Mở Hòm Thư", "Xóa Hết", "Đóng");
                    break;
                }

            }
        } else if (player.iDMark.getIndexMenu() == ConstNpc.MENU_REWARD) {
            // Menu xem phần thưởng
            switch (select) {
                case 0 ->
                    showSoThuongReward(player);
                case 1 ->
                    showSoVipReward(player);
                case 2 -> {
                }
            }

        } else if (player.iDMark.getIndexMenu() == ConstNpc.CONFIRM_OPEN_VIP) {
            switch (select) {
                case 0 -> {
                    if (PlayerDAO.subcash(player, 100_000)) {
                        player.sosumenhplayer.setVip(true);
                        Service.gI().sendThongBao(player, "Chúc mừng bạn đã mở vip sổ thành công!!");
                    } else {
                        Service.gI().sendThongBao(player, "Đã có lỗi xảy ra !!");
                    }
                }
                case 1 -> {
                }
            }

        } else if (player.iDMark.getIndexMenu() == ConstNpc.CONFIRM_BUY_LEVEL) {
            switch (select) {
                case 0 -> {
                    if (PlayerDAO.subcash(player, 5_000)) {
                        player.sosumenhplayer.addPoint(100);
                        Service.gI().sendThongBao(player,
                                "Mua thành công! Cấp hiện tại: " + player.sosumenhplayer.getLevel());
                    } else {
                        Service.gI().sendThongBao(player, "Đã có lỗi xảy ra !!");
                    }
                }
                case 1 -> {
                }
            }

        } else if (player.iDMark.getIndexMenu() == ConstNpc.MAIL_BOX) {
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
                }
            }

        } else {
            switch (select) {
                case 0 ->
                    SoSuMenhService.getInstance().loadAchievements(player, false);
                case 1 ->
                    SoSuMenhService.getInstance().loadAchievements(player, true);
            }
        }
    }

    // ================== HÀM HIỂN THỊ PHẦN THƯỞNG ===================
    private void showSoThuongReward(Player player) {
        try (Connection con2 = DBConnecter.getConnectionServer()) {
            PreparedStatement ps = con2.prepareStatement("SELECT * FROM so_su_menh_reward");
            ResultSet rs = ps.executeQuery();
            StringBuilder sb = new StringBuilder();
            sb.append("|0|꧁__Sổ Thường - Phần Thưởng Theo Level_꧂\n");
            while (rs.next()) {
                sb.append("◥_____________________◤\n|7|");
                sb.append("✎▶Level ").append(rs.getInt("level")).append("◀\n|0|");
                sb.append("◢¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯◣\n|0|");
                JSONArray dataArray = (JSONArray) JSONValue.parse(rs.getString("items"));
                for (Object obj : dataArray) {
                    JSONObject dataObject = (JSONObject) JSONValue.parse(String.valueOf(obj));
                    int tempid = Integer.parseInt(String.valueOf(dataObject.get("temp_id")));
                    int quantity = Integer.parseInt(String.valueOf(dataObject.get("quantity")));
                    sb.append("▷ x").append(quantity).append(" ")
                            .append(ItemService.gI().getTemplate(tempid).name).append("\n|4|");
                    JSONArray optionsArray = (JSONArray) dataObject.get("options");
                    if (optionsArray != null) {
                        for (Object o : optionsArray) {
                            JSONObject optionObject = (JSONObject) o;
                            int optionId = Integer.parseInt(String.valueOf(optionObject.get("id")));
                            int param = Integer.parseInt(String.valueOf(optionObject.get("param")));
                            String optName = ItemService.gI().getItemOptionTemplate(optionId).name;
                            sb.append(optName.replace("#", String.valueOf(param))).append("\n");
                        }
                    }
                    sb.append("\n");
                }
            }
            Service.gI().sendThongBaoFromAdmin(player, sb.toString());
        } catch (SQLException e) {
            Logger.getLogger(SoSuMenh.class.getName()).log(Level.SEVERE, null, e);
            Service.gI().sendThongBao(player, "Lỗi khi tải phần thưởng sổ thường!");
        }
    }

    private void showSoVipReward(Player player) {
        try (Connection con2 = DBConnecter.getConnectionServer()) {
            PreparedStatement ps = con2.prepareStatement("SELECT * FROM so_su_menh_reward");
            ResultSet rs = ps.executeQuery();
            StringBuilder sb = new StringBuilder();
            sb.append("|0|꧁__Sổ VIP - Phần Thưởng Theo Level_꧂\n");
            while (rs.next()) {
                sb.append("◥_____________________◤\n|7|");
                sb.append("✎▶Level ").append(rs.getInt("level")).append("◀\n|0|");
                sb.append("◢¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯◣\n|0|");
                JSONArray dataArray = (JSONArray) JSONValue.parse(rs.getString("items2"));
                for (Object obj : dataArray) {
                    JSONObject dataObject = (JSONObject) JSONValue.parse(String.valueOf(obj));
                    int tempid = Integer.parseInt(String.valueOf(dataObject.get("temp_id")));
                    int quantity = Integer.parseInt(String.valueOf(dataObject.get("quantity")));
                    sb.append("▷ x").append(quantity).append(" ")
                            .append(ItemService.gI().getTemplate(tempid).name).append("\n|4|");
                    JSONArray optionsArray = (JSONArray) dataObject.get("options");
                    if (optionsArray != null) {
                        for (Object o : optionsArray) {
                            JSONObject optionObject = (JSONObject) o;
                            int optionId = Integer.parseInt(String.valueOf(optionObject.get("id")));
                            int param = Integer.parseInt(String.valueOf(optionObject.get("param")));
                            String optName = ItemService.gI().getItemOptionTemplate(optionId).name;
                            sb.append(optName.replace("#", String.valueOf(param))).append("\n");
                        }
                    }
                    sb.append("\n");
                }
            }
            Service.gI().sendThongBaoFromAdmin(player, sb.toString());
        } catch (SQLException e) {
            Logger.getLogger(SoSuMenh.class.getName()).log(Level.SEVERE, null, e);
            Service.gI().sendThongBao(player, "Lỗi khi tải phần thưởng sổ VIP!");
        }
    }
}
