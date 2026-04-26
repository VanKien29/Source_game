package npc.npc_manifest;

/**
 *
 * @author CongHoan
 */
import consts.ConstNpc;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import jdbc.DBConnecter;
import jdbc.daos.PlayerDAO;
import npc.Npc;
import player.Player;
import services.Service;
import services.TaskService;
import shop.ShopService;

public class Bardock extends Npc {

    public Bardock(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            if (!TaskService.gI().checkDoneTaskTalkNpc(player, this)) {
                switch (mapId) {
                    case 0, 7, 14 -> {
                        String[] menuOptions;
                        if (player.getSession().actived) {
                            // Nếu đã mở thành viên, ẩn ô "Mở Thành viên"
                            menuOptions = new String[]{
                                "Của hàng",
                                "Điểm Danh",
                                "Nhận Quà\n Điểm Danh"
                            };
                        } else {
                            // Nếu chưa mở thành viên, hiển thị ô "Mở Thành viên"
                            menuOptions = new String[]{
                                "Của hàng",
                                "Điểm Danh",
                                "Nhận Quà\n Điểm Danh",
                                "Mở\nThành viên"
                            };
                        }
                        this.createOtherMenu(player, ConstNpc.BASE_MENU,
                                "|0| Ta bán đồ cho người giàu, người có tiền không đó ???\n|4|"
                                + "Số tiền ngươi đang có: " + player.getSession().cash + " VND",
                                menuOptions
                        );
                    }
                    default ->
                        super.openBaseMenu(player);
                }
            }
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            if (player.iDMark.isBaseMenu()) {
                switch (select) {
                    case 0: {
                        if(player.isAdmin() == true){
                        ShopService.gI().opendShop(player, "BARDOCK_SHOP", false);
                        break;}
                    }
                    case 1: {
                        this.createOtherMenu(player, 8386,
                                "|7| [Điểm danh hàng ngày để nhận vè những phần quà vô cùng hấp dẫn]\n|0| "
                                + "Bạn đã điểm danh được: " + player.getSession().diemdanh + " ngày",
                                "Đồng ý", "Không");
                        break;
                    }
                    case 2: {
                        ShopService.gI().opendShop(player, "DIEM_DANH", false);
                        break;
                    }
                    case 3: {
                        this.createOtherMenu(player, 6789,
                                "|7| [Chỉ với 20K, mở thành viên ngay để nhận vô vàn ưu đãi hấp dẫn!]\n|0| "
                                + "Số tiền đang có: " + player.getSession().cash + " VND",
                                "Đồng ý", "Không");
                        break;
                    }
                }
            } else if (player.iDMark.getIndexMenu() == 6789) {
                switch (select) {
                    case 0 -> {
                        int price = 20000;
                        if (player.getSession().cash >= price) {
                            PlayerDAO.subcash(player, price);
                            PlayerDAO.updateActive(player, 1);
                            Service.gI().sendThongBao(player, "Bạn đã mở thành viên thành công");
                            Service.gI().sendLogout(player);
                        } else {
                            Service.gI().sendThongBao(player, "Bạn không đủ số dư để mở thành viên");
                        }
                        break;
                    }
                }
            } else if (player.iDMark.getIndexMenu() == 8386) {
                switch (select) {
                    case 0 -> {
                        try ( Connection con = DBConnecter.getConnectionServer()) {

                            java.sql.Date today = new java.sql.Date(System.currentTimeMillis());
                            PreparedStatement ps = con.prepareStatement(
                                    "UPDATE account SET DiemDanh = DiemDanh + 1, lastDiemDanh = ? "
                                    + "WHERE id = ? AND (lastDiemDanh IS NULL OR lastDiemDanh != ?)");
                            ps.setDate(1, today);
                            ps.setInt(2, player.getSession().userId);
                            ps.setDate(3, today);
                            int rowsAffected = ps.executeUpdate();
                            if (rowsAffected > 0) {
                                Service.gI().sendThongBao(player, "Điểm danh thành công");
                            } else {
                                Service.gI().sendThongBao(player, "Bạn đã điểm danh hôm nay rồi");
                            }
                        } catch (SQLException e) {
                        }
                    }
                }
            }
        }
    }
}
