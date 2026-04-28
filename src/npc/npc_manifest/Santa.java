package npc.npc_manifest;

/**
 * @author CongHoan
 */
import consts.ConstNpc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jdbc.DBConnecter;
import player.Player;
import services.Service;
import shop.ShopService;
import npc.Npc;

public class Santa extends Npc {

    public Santa(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            List<String> menu = new ArrayList<>(Arrays.asList(
                    "Cửa hàng",
                    "Tiệm\nHớt tóc",
                    "Quà\nĐiểm danh",
                    "Điểm\nDanh",
                    "Danh\nhiệu"));
            if (!player.inventory.itemsDaBan.isEmpty()) {
                menu.add(1, "Mua lại\nvật phẩm\nđã bán [" + player.inventory.itemsDaBan.size() + "/20]");
            }

            String[] menus = menu.toArray(new String[0]);

            createOtherMenu(player, ConstNpc.BASE_MENU,
                    "Xin chào, ta có một số vật phẩm đặc biệt cậu có muốn xem không?", menus);
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            if (this.mapId == 5 || this.mapId == 13 || this.mapId == 20) {
                if (player.iDMark.isBaseMenu()) {
                    boolean mualaivp = player.inventory.itemsDaBan.isEmpty();
                    if (mualaivp && select >= 2) {
                        select += 1; // Bỏ qua case 2
                    }
                    switch (select) {
                        case 0 -> // Cửa hàng
                            ShopService.gI().opendShop(player, "SANTA", false);
                        case 1 -> // Mua lại vật phẩm đã bán
                        {
                            if (!player.inventory.itemsDaBan.isEmpty()) {
                                ShopService.gI().opendShop(player, "ITEMS_DABAN", false);
                            } else {
                                ShopService.gI().opendShop(player, "SANTA_HEAD", false);
                            }
                        }
                        case 2 -> // Tiệm hớt tóc
                            ShopService.gI().opendShop(player, "SANTA_HEAD", false);
                      
                        case 3 -> // Quà Điểm danh
                            ShopService.gI().opendShop(player, "DIEM_DANH", false);
                        case 4 -> { // Điểm danh
                            this.createOtherMenu(player, 8386,
                                    "|7| [Điểm danh hàng ngày để nhận về những phần quà vô cùng hấp dẫn]\n|0| "
                                    + "Bạn đã điểm danh được: " + player.getSession().diemdanh + " ngày",
                                    "Đồng ý", "Không");
                        }
                        case 5 -> // danh hiệu
                            ShopService.gI().opendShop(player, "SANTA_DANH_HIEU", false);
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
                                e.printStackTrace();
                                Service.gI().sendThongBao(player, "Đã xảy ra lỗi khi điểm danh");
                            }
                        }
                    }
                }
            }
        }
    }
}
