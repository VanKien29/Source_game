package npc.npc_manifest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import consts.ConstDataEventNAP;
import consts.ConstDataEventSM;
import consts.ConstDataEventNV;
import consts.ConstMenu;
import npc.Npc;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import player.Player;
import services.ItemService;
import services.Service;
import services.func.TopService;
import jdbc.DBConnecter;

public class DaiThienSu extends Npc {

    public DaiThienSu(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        this.createOtherMenu(player, ConstMenu.MENU_SHOW,
                "|0|Đang diễn ra sự kiện đua top mùa 1\n|3|"
                + "Thể lệ đua top mùa 1:\n|4|"
                + "- Thời gian tạo acc tại thời điểm sự kiện diễn ra\n",
                "Đua Top Sức Mạnh", "Đua Top Nạp", "Top Nhiệm Vụ");
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (!canOpenNpc(player)) {
            return;
        }

        switch (player.iDMark.getIndexMenu()) {
            case ConstMenu.MENU_SHOW:
                switch (select) {
                    case 0: // Đua Top Sức Mạnh
                        if (!ConstDataEventSM.isRunningSK) {
                            Service.gI().sendThongBao(player, "Sự kiện top Sức mạnh đã kết thúc");
                            return;
                        }
                        this.createOtherMenu(player, 1115, "|0|Đua Top Sức Mạnh",
                                "Xem Top", "Xem quà top", "Đóng");
                        break;

                    case 1: // Đua Top Nạp
                        if (!ConstDataEventNAP.isRunningSK) {
                            Service.gI().sendThongBao(player, "Sự kiện top Nạp đã kết thúc");
                            return;
                        }
                        this.createOtherMenu(player, 1116, "|0|Đua Top Nạp",
                                "Xem Top", "Xem quà top", "Đóng");
                        break;

                    case 2: // Top Nhiệm Vụ
                        if (!ConstDataEventNV.isRunningSK) {
                            Service.gI().sendThongBao(player, "Sự kiện top Nhiệm Vụ đã kết thúc");
                            return;
                        }
                        this.createOtherMenu(player, 1117, "|0|Top Nhiệm Vụ",
                                "Xem Top", "Xem quà top", "Đóng");
                        break;
                }
                break;

            case 1115: // Menu Top Sức Mạnh
                switch (select) {
                    case 0:
                        TopService.showListTop(player, 2);
                        break;
                    case 1:
                        showTopSMGift(player);
                        break;
                    case 2:
                        // Đóng menu, không làm gì
                        break;
                }
                break;

            case 1116: // Menu Top Nạp
                switch (select) {
                    case 0:
                        TopService.showListTop(player, 4);
                        break;
                    case 1:
                        showTopNapGift(player);
                        break;
                    case 2:
                        // Đóng menu
                        break;
                }
                break;

            case 1117: // Menu Top Nhiệm Vụ
                switch (select) {
                    case 0:
                        TopService.showListTop(player, 0);
                        break;
                    case 1:
                        showTopNhiemVuGift(player);
                        break;
                    case 2:
                        // Đóng menu
                        break;
                }
                break;
        }
    }

    // Hàm hiển thị quà Top Sức Mạnh
    private void showTopSMGift(Player player) {
        StringBuilder sb = new StringBuilder();
        sb.append("|0|꧁TOP SỨC MẠNH:\n"
                + "|5|Quà Top Sức Mạnh\n"
//                + ConstDataEventSM.HOUR_OPEN + "H" + ConstDataEventSM.MIN_OPEN + " ngày "
//                + ConstDataEventSM.DATE_OPEN + "/" + ConstDataEventSM.MONTH_OPEN + "/2024\n"
//                + "Nhận quà vào ngày " + ConstDataEventSM.HOUR_END + "H" + ConstDataEventSM.MIN_END
//                + " ngày " + ConstDataEventSM.DATE_END + "/" + ConstDataEventSM.MONTH_END
//                + "/2024, quà sẽ về hộp thư người chơi꧂\n"
        );

        try (Connection con = DBConnecter.getConnectionServer(); PreparedStatement ps = con.prepareStatement("SELECT * FROM moc_suc_manh_top"); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                JSONArray dataArray = (JSONArray) JSONValue.parse(rs.getString("detail"));
                sb.append("◥_____________________◤\n|7|");
                sb.append("✎▶TOP ").append(rs.getInt("id")).append("◀\n|0|");
                sb.append("◢¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯◣\n|0|");

                for (Object obj : dataArray) {
                    JSONObject dataObject = (JSONObject) JSONValue.parse(obj.toString());
                    int tempid = Integer.parseInt(String.valueOf(dataObject.get("temp_id")));
                    int quantity = Integer.parseInt(String.valueOf(dataObject.get("quantity")));
                    JSONArray optionsArray = (JSONArray) dataObject.get("options");

                    sb.append("▷ x").append(quantity).append(" ")
                            .append(ItemService.gI().getTemplate(tempid).name).append("\n|4|");

                    if (optionsArray != null) {
                        for (Object opt : optionsArray) {
                            JSONObject optionObject = (JSONObject) opt;
                            int optionId = Integer.parseInt(String.valueOf(optionObject.get("id")));
                            int param = Integer.parseInt(String.valueOf(optionObject.get("param")));

                            String optionName = ItemService.gI()
                                    .getItemOptionTemplate(optionId).name.replace("#", String.valueOf(param));
                            sb.append(optionName).append("\n");
                        }
                    }
                    sb.append("\n|0|");
                }
            }

        } catch (SQLException ex) {
            Logger.getLogger(DaiThienSu.class.getName()).log(Level.SEVERE, null, ex);
        }

        Service.gI().sendThongBaoFromAdmin(player, sb.toString());
    }

    // Hàm hiển thị quà Top Nạp
    private void showTopNapGift(Player player) {
        StringBuilder sb = new StringBuilder();
        sb.append("|0|꧁TOP NẠP:\n"
                + "Tài khoản cần nạp 50K trở lên để có thể vào top\n"
                + "|5|Quà top Nạp\n"
//                + "Nhận quà vào ngày " + ConstDataEventNAP.HOUR_END + "H" + ConstDataEventNAP.MIN_END
//                + " ngày " + ConstDataEventNAP.DATE_END + "/" + ConstDataEventNAP.MONTH_END
//                + "/2024, quà sẽ về hộp thư người chơi꧂\n"
        );

        try (Connection con = DBConnecter.getConnectionServer(); PreparedStatement ps = con.prepareStatement("SELECT * FROM moc_nap_top"); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                JSONArray dataArray = (JSONArray) JSONValue.parse(rs.getString("detail"));
                sb.append("◥_____________________◤\n|7|");
                sb.append("✎▶TOP ").append(rs.getInt("id")).append("◀\n|0|");
                sb.append("◢¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯◣\n|0|");

                for (Object obj : dataArray) {
                    JSONObject dataObject = (JSONObject) JSONValue.parse(obj.toString());
                    int tempid = Integer.parseInt(String.valueOf(dataObject.get("temp_id")));
                    int quantity = Integer.parseInt(String.valueOf(dataObject.get("quantity")));
                    JSONArray optionsArray = (JSONArray) dataObject.get("options");

                    sb.append("▷ x").append(quantity).append(" ")
                            .append(ItemService.gI().getTemplate(tempid).name).append("\n|4|");

                    if (optionsArray != null) {
                        for (Object opt : optionsArray) {
                            JSONObject optionObject = (JSONObject) opt;
                            int optionId = Integer.parseInt(String.valueOf(optionObject.get("id")));
                            int param = Integer.parseInt(String.valueOf(optionObject.get("param")));

                            String optionName = ItemService.gI()
                                    .getItemOptionTemplate(optionId).name.replace("#", String.valueOf(param));
                            sb.append(optionName).append("\n");
                        }
                    }
                    sb.append("\n|0|");
                }
            }

        } catch (SQLException ex) {
            Logger.getLogger(DaiThienSu.class.getName()).log(Level.SEVERE, null, ex);
        }

        Service.gI().sendThongBaoFromAdmin(player, sb.toString());
    }

    // Hàm hiển thị quà Top Nhiệm Vụ
    private void showTopNhiemVuGift(Player player) {
        StringBuilder sb = new StringBuilder();
        sb.append("|0|꧁TOP NHIỆM VỤ:\n"
                + "|5|Quà top Nhiệm Vụ\n"
//                + "Chỉ dành cho tài khoản tạo từ "
//                + ConstDataEventNV.HOUR_OPEN + "H" + ConstDataEventNV.MIN_OPEN + " ngày "
//                + ConstDataEventNV.DATE_OPEN + "/" + ConstDataEventNV.MONTH_OPEN + "/2024\n"
//                + "Nhận quà vào ngày " + ConstDataEventNV.HOUR_END + "H" + ConstDataEventNV.MIN_END
//                + " ngày " + ConstDataEventNV.DATE_END + "/" + ConstDataEventNV.MONTH_END
//                + "/2024, quà sẽ về hộp thư người chơi꧂\n"
        );

        try (Connection con = DBConnecter.getConnectionServer(); PreparedStatement ps = con.prepareStatement("SELECT * FROM moc_nhiem_vu_top"); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                JSONArray dataArray = (JSONArray) JSONValue.parse(rs.getString("detail"));
                sb.append("◥_____________________◤\n|7|");
                sb.append("✎▶TOP ").append(rs.getInt("id")).append("◀\n|0|");
                sb.append("◢¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯◣\n|0|");

                for (Object obj : dataArray) {
                    JSONObject dataObject = (JSONObject) JSONValue.parse(obj.toString());
                    int tempid = Integer.parseInt(String.valueOf(dataObject.get("temp_id")));
                    int quantity = Integer.parseInt(String.valueOf(dataObject.get("quantity")));
                    JSONArray optionsArray = (JSONArray) dataObject.get("options");

                    sb.append("▷ x").append(quantity).append(" ")
                            .append(ItemService.gI().getTemplate(tempid).name).append("\n|4|");

                    if (optionsArray != null) {
                        for (Object opt : optionsArray) {
                            JSONObject optionObject = (JSONObject) opt;
                            int optionId = Integer.parseInt(String.valueOf(optionObject.get("id")));
                            int param = Integer.parseInt(String.valueOf(optionObject.get("param")));

                            String optionName = ItemService.gI()
                                    .getItemOptionTemplate(optionId).name.replace("#", String.valueOf(param));
                            sb.append(optionName).append("\n");
                        }
                    }
                    sb.append("\n|0|");
                }
            }

        } catch (SQLException ex) {
            Logger.getLogger(DaiThienSu.class.getName()).log(Level.SEVERE, null, ex);
        }

        Service.gI().sendThongBaoFromAdmin(player, sb.toString());
    }
}