package npc.npc_manifest;

import consts.ConstNpc;
import item.Item;
import java.text.NumberFormat;
import java.util.Locale;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import jdbc.DBConnecter;
import jdbc.daos.PlayerDAO;
import npc.Npc;
import player.Player;
import services.InventoryService;
import services.Service;

public class TienCap extends Npc {

    // ID đá Tiến cấp (hoặc Thỏi Vàng dùng làm đá, tùy bạn cấu hình)
    private static final short DA_TIEN_CAP_ID = 1965;

    private static final String SQL_UPDATE_PLAYER_TIEN_CAP = "UPDATE player SET tien_cap_gold = ?, tien_cap_kill = ?, tien_cap_killboss = ?, tien_cap_level = ? WHERE id = ?";

    private static final String SQL_SELECT_TIEN_CAP = "SELECT * FROM tien_cap WHERE id = ?";

    private static final int MENU_MUA_VUOT_CAP = 31001;

    // Lưu tiến độ Tiến cấp vào DB (gọi chỗ nào bạn muốn auto-save)
    public static void saveTienCap(Player player) {
        // Nên hạn chế gọi hàm này quá thường xuyên (ví dụ mỗi lần nhặt đồ)
        try (Connection con = DBConnecter.getConnectionServer();
                PreparedStatement ps = con.prepareStatement(SQL_UPDATE_PLAYER_TIEN_CAP)) {

            ps.setInt(1, player.tienCapGold);
            ps.setInt(2, player.tienCapKill);
            ps.setInt(3, player.tienCapKillBoss);
            ps.setInt(4, player.tienCapLevel);
            ps.setInt(5, (int) player.id);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public TienCap(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (!canOpenNpc(player)) {
            return;
        }
        showTienCapInfo(player);
    }

    private String formatCoin(long coin) {
        return NumberFormat.getInstance(new Locale("vi", "VN")).format(coin);
    }

    private int getMuaVuotCapCost(int currentLevel) {
        return switch (currentLevel) {
            case 0 ->
                20_000;
            case 1 ->
                50_000;
            case 2 ->
                100_000;
            case 3 ->
                200_000;
            case 4 ->
                300_000;
            case 5 ->
                400_000;
            case 6 ->
                500_000;
            case 7 ->
                600_000;
            case 8 ->
                700_000;
            default ->
                800_000;
        };
    }

    private boolean tryChargeCoin(Player player, int cost) {
        if (player.getSession() == null) {
            Service.gI().sendThongBao(player, "Có lỗi xảy ra, thử lại sau!");
            return false;
        }
        if (player.getSession().cash < cost) {
            Service.gI().sendThongBao(player, "Bạn không đủ " + formatCoin(cost) + " COIN!");
            return false;
        }
        PlayerDAO.subcash(player, cost);
        Service.gI().sendMoney(player);
        return true;
    }

    private void showTienCapInfo(Player player) {
        int nextLevel = player.tienCapLevel + 1;
        if (nextLevel > 7) {
            Service.gI().sendThongBao(player, "Con đã Tiến cấp tối đa rồi!");
            return;
        }

        String dieukien1 = "";
        String dieukien2 = "";
        String dieukien3 = "";
        String dieukien4 = "";
        String info = "";
        int[] amounts = new int[] { 0, 0, 0, 0 };

        try (Connection con = DBConnecter.getConnectionServer();
                PreparedStatement ps = con.prepareStatement(SQL_SELECT_TIEN_CAP)) {

            ps.setInt(1, nextLevel);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    dieukien1 = rs.getString("dieukien1");
                    dieukien2 = rs.getString("dieukien2");
                    dieukien3 = rs.getString("dieukien3");
                    dieukien4 = rs.getString("dieukien4");
                    info = rs.getString("info");
                    amounts = parseSoluong(rs.getString("soluong"));
                } else {
                    this.npcChat(player, "Chưa cấu hình nhiệm vụ Tiến cấp level " + nextLevel);
                    return;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            this.npcChat(player, "Có lỗi khi tải nhiệm vụ Tiến cấp");
            return;
        }

        int requireFullSet = amounts[0];
        int requireGoldBar = amounts[1];
        int requirePowerBillion = amounts[2];
        int requireKill = amounts[3];

        // Tiến độ
        // Kiểm tra dieukien1 để xác định là số sao hay cấp độ
        int fullSetCount = dieukien1.toLowerCase().contains("cấp")
                ? countCapOnSet(player, requireFullSet)
                : countStarOnSet(player, requireFullSet);
        int goldProgress = Math.min(getTienCapGold(player), requireGoldBar);
        long powerCurrentBillion = player.nPoint.power / 1_000_000_000L;
        long powerProgress = Math.min(powerCurrentBillion, requirePowerBillion);
        // Kiểm tra dieukien4 để xác định là kill Boss hay Pem Quái
        int killProgress = dieukien4.toLowerCase().contains("boss")
                ? Math.min(getTienCapKillBoss(player), requireKill)
                : Math.min(getTienCapKill(player), requireKill);

        StringBuilder sb = new StringBuilder();
        sb.append("Tiến cấp giúp con khai mở các thuộc tính và các ô trang bị\n\n");
        sb.append("Nhiệm vụ Tiến cấp cấp ").append(nextLevel).append(":\n\n");
        sb.append(dieukien1).append("    TIẾN ĐỘ--").append(fullSetCount).append("/5\n");
        sb.append(dieukien2).append("    TIẾN ĐỘ--").append(goldProgress).append("/")
                .append(requireGoldBar).append("\n");
        sb.append(dieukien3).append("    TIẾN ĐỘ--").append(powerProgress).append("/")
                .append(requirePowerBillion).append(" tỉ Sức mạnh\n");
        sb.append(dieukien4).append("    TIẾN ĐỘ--").append(killProgress).append("/")
                .append(requireKill).append("\n\n");

        int stoneNeed = nextLevel; // level 2 cần 2 viên, level 3 cần 3 viên,...
        int stoneHave = 0;
        Item da = InventoryService.gI().findItemBag(player, DA_TIEN_CAP_ID);
        if (da != null) {
            stoneHave = da.quantity;
        }
        sb.append(stoneHave).append("/").append(stoneNeed)
                .append(" Đá Tiến Cấp ( Săn Sói Bergamo ) ").append("\n\n");
        sb.append(info);

        this.createOtherMenu(player, ConstNpc.TIEN_CAP, sb.toString(),
                "Tiến cấp\nngay", "Mua vượt\ncấp", "Đóng");
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (!canOpenNpc(player)) {
            return;
        }
        switch (player.iDMark.getIndexMenu()) {
            case ConstNpc.TIEN_CAP -> {
                if (select == 0) {
                    thucHienTienCap(player);
                } else if (select == 1) {
                    moMenuMuaVuotCap(player);
                }
            }
            case MENU_MUA_VUOT_CAP -> {
                if (select == 0) {
                    muaVuotCap(player);
                }
            }
        }
    }

    private void moMenuMuaVuotCap(Player player) {
        int nextLevel = player.tienCapLevel + 1;
        if (nextLevel > 7) {
            Service.gI().sendThongBao(player, "Con đã Tiến cấp tối đa rồi!");
            return;
        }
        if (player.getSession() == null) {
            Service.gI().sendThongBao(player, "Có lỗi xảy ra, thử lại sau!");
            return;
        }
        int cost = getMuaVuotCapCost(player.tienCapLevel);
        this.createOtherMenu(
                player,
                MENU_MUA_VUOT_CAP,
                "|5|Mua vượt cấp Tiến Cấp?"
                        + "\nTừ cấp " + player.tienCapLevel + " lên cấp " + nextLevel
                        + "\n|7|Giá: " + formatCoin(cost) + " COIN"
                        + "\n\b|7|Bạn đang có: " + formatCoin(player.getSession().cash) + " COIN",
                "Mua ngay", "Đóng");
    }

    private void muaVuotCap(Player player) {
        int nextLevel = player.tienCapLevel + 1;
        if (nextLevel > 7) {
            Service.gI().sendThongBao(player, "Con đã Tiến cấp tối đa rồi!");
            return;
        }

        int cost = getMuaVuotCapCost(player.tienCapLevel);
        if (!tryChargeCoin(player, cost)) {
            return;
        }

        player.tienCapLevel = (byte) nextLevel;
        player.bodySlotExtra = player.tienCapLevel;
        if (player.bodySlotExtra > 7) {
            player.bodySlotExtra = 7;
        }

        // Lên cấp mới thì reset tiến độ cũ để tránh giữ dữ liệu nhiệm vụ của cấp trước.
        player.tienCapGold = 0;
        player.tienCapKill = 0;
        player.tienCapKillBoss = 0;

        try (Connection con = DBConnecter.getConnectionServer();
                PreparedStatement ps = con.prepareStatement(SQL_UPDATE_PLAYER_TIEN_CAP)) {

            ps.setInt(1, player.tienCapGold);
            ps.setInt(2, player.tienCapKill);
            ps.setInt(3, player.tienCapKillBoss);
            ps.setInt(4, player.tienCapLevel);
            ps.setInt(5, (int) player.id);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            Service.gI().sendThongBao(player, "Có lỗi khi lưu dữ liệu Tiến cấp!");
            return;
        }

        Service.gI().sendThongBao(player,
                "Mua vượt cấp thành công! Đã tăng lên cấp " + player.tienCapLevel
                        + " và trừ " + formatCoin(cost) + " COIN.");
    }

    private void thucHienTienCap(Player player) {
        int nextLevel = player.tienCapLevel + 1;
        if (nextLevel > 7) {
            Service.gI().sendThongBao(player, "Con đã Tiến cấp tối đa rồi!");
            return;
        }

        String dieukien1 = "";
        String dieukien2 = "";
        String dieukien3 = "";
        String dieukien4 = "";
        int[] amounts = new int[] { 0, 0, 0, 0 };

        try (Connection con = DBConnecter.getConnectionServer();
                PreparedStatement ps = con.prepareStatement(SQL_SELECT_TIEN_CAP)) {

            ps.setInt(1, nextLevel);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    dieukien1 = rs.getString("dieukien1");
                    dieukien2 = rs.getString("dieukien2");
                    dieukien3 = rs.getString("dieukien3");
                    dieukien4 = rs.getString("dieukien4");
                    amounts = parseSoluong(rs.getString("soluong"));
                } else {
                    Service.gI().sendThongBao(player, "Chưa cấu hình nhiệm vụ Tiến cấp level " + nextLevel);
                    return;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            Service.gI().sendThongBao(player, "Có lỗi khi tải nhiệm vụ Tiến cấp");
            return;
        }

        int requireFullSet = amounts[0];
        int requireGoldBar = amounts[1];
        int requirePowerBillion = amounts[2];
        int requireKill = amounts[3];

        // Kiểm tra dieukien1 để xác định là số sao hay cấp độ
        int fullSetCount = dieukien1.toLowerCase().contains("cấp")
                ? countCapOnSet(player, requireFullSet)
                : countStarOnSet(player, requireFullSet);
        if (fullSetCount < 5) {
            Service.gI().sendThongBao(player, "Chưa hoàn thành: " + dieukien1);
            return;
        }

        if (getTienCapGold(player) < requireGoldBar) {
            Service.gI().sendThongBao(player, "Chưa hoàn thành: " + dieukien2);
            return;
        }

        long powerCurrentBillion = player.nPoint.power / 1_000_000_000L;
        if (powerCurrentBillion < requirePowerBillion) {
            Service.gI().sendThongBao(player, "Chưa hoàn thành: " + dieukien3);
            return;
        }

        // Kiểm tra dieukien4 để xác định là kill Boss hay Pem Quái
        int killValue = dieukien4.toLowerCase().contains("boss")
                ? getTienCapKillBoss(player)
                : getTienCapKill(player);
        if (killValue < requireKill) {
            Service.gI().sendThongBao(player, "Chưa hoàn thành: " + dieukien4);
            return;
        }

        // Check đá Tiến cấp
        int stoneNeed = nextLevel;
        Item da = InventoryService.gI().findItemBag(player, DA_TIEN_CAP_ID);
        if (da == null || da.quantity < stoneNeed) {
            Service.gI().sendThongBao(player,
                    "Không đủ Đá Tiến Cấp ");
            return;
        }

        // Trừ đá
        InventoryService.gI().subQuantityItemsBag(player, da, stoneNeed);
        InventoryService.gI().sendItemBag(player);

        // Tăng level Tiến cấp
        player.tienCapLevel++;

        // Mở thêm ô trang bị bằng bodySlotExtra (KHÔNG đụng vào nangcap/cap)
        player.bodySlotExtra = player.tienCapLevel;
        if (player.bodySlotExtra > 7) {
            player.bodySlotExtra = 7;
        }

        // Reset tiến độ nhiệm vụ
        player.tienCapGold = 0;
        player.tienCapKill = 0;
        player.tienCapKillBoss = 0;

        // Update DB sau khi thay đổi
        try (Connection con = DBConnecter.getConnectionServer();
                PreparedStatement ps = con.prepareStatement(SQL_UPDATE_PLAYER_TIEN_CAP)) {

            ps.setInt(1, player.tienCapGold);
            ps.setInt(2, player.tienCapKill);
            ps.setInt(3, player.tienCapKillBoss);
            ps.setInt(4, player.tienCapLevel);
            ps.setInt(5, (int) player.id);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        Service.gI().sendThongBao(player,
                "Tiến cấp thành công! Đã mở khóa thêm 1 ô trang bị và tăng chỉ số.");
    }

    // ================= HỖ TRỢ =================

    private int[] parseSoluong(String soluong) {
        int[] out = new int[] { 0, 0, 0, 0 };
        if (soluong == null) {
            return out;
        }
        soluong = soluong.replace("[", "").replace("]", "").trim();
        if (soluong.isEmpty()) {
            return out;
        }
        String[] parts = soluong.split(",");
        for (int i = 0; i < parts.length && i < 4; i++) {
            try {
                out[i] = Integer.parseInt(parts[i].trim());
            } catch (Exception e) {
                out[i] = 0;
            }
        }
        return out;
    }

    // option sao pha lê = 107
    private static final int OPTION_STAR_ID = 107;
    private static final int OPTION_CAP_ID = 72;

    private int countStarOnSet(Player player, int requireStar) {
        int count = 0;
        // 5 món đầu: mũ, áo, quần, găng, giày
        for (int i = 0; i < 5; i++) {
            Item it = player.inventory.itemsBody.get(i);
            if (it != null && it.isNotNullItem()) {
                for (Item.ItemOption op : it.itemOptions) {
                    if (op != null && op.optionTemplate.id == OPTION_STAR_ID
                            && op.param >= requireStar) {
                        count++;
                        break;
                    }
                }
            }
        }
        return count;
    }

    private int countCapOnSet(Player player, int requireStar) {
        int count = 0;
        // 5 món đầu: mũ, áo, quần, găng, giày
        for (int i = 0; i < 5; i++) {
            Item it = player.inventory.itemsBody.get(i);
            if (it != null && it.isNotNullItem()) {
                for (Item.ItemOption op : it.itemOptions) {
                    if (op != null && op.optionTemplate.id == OPTION_CAP_ID
                            && op.param >= requireStar) {
                        count++;
                        break;
                    }
                }
            }
        }
        return count;
    }

    // Tổng % HP/SD/KI cộng dồn theo level Tiến cấp
    // 1–6: mỗi level +1%, level 7 thêm +3% (tổng 9%)
    public static int getPercentHpSdKi(int level) {
        int p = 0;
        if (level >= 1)
            p += 1;
        if (level >= 2)
            p += 1;
        if (level >= 3)
            p += 1;
        if (level >= 4)
            p += 1;
        if (level >= 5)
            p += 1;
        if (level >= 6)
            p += 1;
        if (level >= 7)
            p += 3;
        return p;
    }

    // CRIT cộng dồn theo level Tiến cấp
    // level 6: +2 CRIT, level 7: +5 CRIT (tổng 7)
    public static int getCritBonus(int level) {
        int crit = 0;
        if (level >= 6) {
            crit += 2;
        }
        if (level >= 7) {
            crit += 5;
        }
        return crit;
    }

    private int getTienCapGold(Player player) {
        return player.tienCapGold;
    }

    private int getTienCapKill(Player player) {
        return player.tienCapKill;
    }

    private int getTienCapKillBoss(Player player) {
        return player.tienCapKillBoss;
    }
}
