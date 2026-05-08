package npc.npc_manifest;

import boss.Boss;
import boss.BossID;
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
    private static final int MAX_TIEN_CAP_LEVEL = 7;
    private static final int ITEM_BONG_TAI_CAP_2 = 921;
    private static final int ITEM_CHAN_MENH_LEVEL_5 = 1737;
    private static final long ONE_BILLION_POWER = 1_000_000_000L;

    private static final String SQL_UPDATE_PLAYER_TIEN_CAP = "UPDATE player SET tien_cap_gold = ?, tien_cap_kill = ?, tien_cap_killboss = ?, tien_cap_level = ? WHERE id = ?";

    private static final String SQL_SELECT_TIEN_CAP = "SELECT * FROM tien_cap WHERE id = ?";

    private static final int MENU_MUA_VUOT_CAP = 31001;

    private static class Requirement {

        final String label;
        final long current;
        final long required;
        final String suffix;

        Requirement(String label, long current, long required) {
            this(label, current, required, "");
        }

        Requirement(String label, long current, long required, String suffix) {
            this.label = label;
            this.current = Math.max(0, current);
            this.required = Math.max(1, required);
            this.suffix = suffix;
        }

        boolean isDone() {
            return current >= required;
        }

        long progress() {
            return Math.min(current, required);
        }
    }

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

    public static void recordGoldPick(Player player, int quantity) {
        if (player == null || player.isPet || quantity <= 0) {
            return;
        }
        int nextLevel = player.tienCapLevel + 1;
        if (nextLevel >= 1 && nextLevel <= 3) {
            player.tienCapGold += quantity;
        }
    }

    public static void recordMobKill(Player player) {
        if (player == null || player.isPet) {
            return;
        }
        int nextLevel = player.tienCapLevel + 1;
        if (nextLevel == 1 || nextLevel == 6) {
            player.tienCapKill++;
        }
    }

    public static void recordBossKill(Player player, Object bossObject) {
        if (player == null || player.isPet) {
            return;
        }
        int nextLevel = player.tienCapLevel + 1;
        int bossId = bossObject instanceof Boss boss ? (int) boss.id : 0;
        if (nextLevel == 2) {
            player.tienCapKillBoss++;
        } else if ((nextLevel == 4 || nextLevel == 7) && isBlackGokuBoss(bossId)) {
            player.tienCapKillBoss++;
        } else if (nextLevel == 5 && isXenConBoss(bossId)) {
            player.tienCapKillBoss++;
        }
    }

    private static boolean isBlackGokuBoss(int bossId) {
        return bossId == BossID.BLACK_GOKU;
    }

    private static boolean isXenConBoss(int bossId) {
        return bossId >= BossID.XEN_CON_7 && bossId <= BossID.XEN_CON_1;
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
        if (nextLevel > MAX_TIEN_CAP_LEVEL) {
            Service.gI().sendThongBao(player, "Con đã Tiến cấp tối đa rồi!");
            return;
        }

        TienCapConfig config = loadTienCapConfig(player, nextLevel, false);
        if (config == null) {
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Tiến cấp giúp con khai mở các thuộc tính và các ô trang bị\n\n");
        sb.append("Nhiệm vụ Tiến cấp cấp ").append(nextLevel).append(":\n\n");
        for (Requirement requirement : buildRequirements(player, config)) {
            sb.append(requirement.label)
                    .append("    TIẾN ĐỘ--")
                    .append(requirement.progress())
                    .append("/")
                    .append(requirement.required)
                    .append(requirement.suffix)
                    .append("\n");
        }
        sb.append("\n");

        int stoneNeed = nextLevel; // level 2 cần 2 viên, level 3 cần 3 viên,...
        int stoneHave = 0;
        Item da = InventoryService.gI().findItemBag(player, DA_TIEN_CAP_ID);
        if (da != null) {
            stoneHave = da.quantity;
        }
        sb.append(stoneHave).append("/").append(stoneNeed)
                .append(" Đá Tiến Cấp ( Săn Sói Bergamo ) ").append("\n\n");
        sb.append(config.info);

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
        if (nextLevel > MAX_TIEN_CAP_LEVEL) {
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
        if (nextLevel > MAX_TIEN_CAP_LEVEL) {
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
        if (nextLevel > MAX_TIEN_CAP_LEVEL) {
            Service.gI().sendThongBao(player, "Con đã Tiến cấp tối đa rồi!");
            return;
        }

        TienCapConfig config = loadTienCapConfig(player, nextLevel, true);
        if (config == null) {
            return;
        }

        for (Requirement requirement : buildRequirements(player, config)) {
            if (!requirement.isDone()) {
                Service.gI().sendThongBao(player, "Chưa hoàn thành: " + requirement.label);
                return;
            }
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

    private static class TienCapConfig {

        int level;
        String dieukien1;
        String dieukien2;
        String dieukien3;
        String dieukien4;
        String info;
        int[] amounts;
    }

    private TienCapConfig loadTienCapConfig(Player player, int nextLevel, boolean notify) {
        TienCapConfig config = new TienCapConfig();
        config.level = nextLevel;
        config.amounts = new int[] { 0, 0, 0, 0 };
        try (Connection con = DBConnecter.getConnectionServer();
                PreparedStatement ps = con.prepareStatement(SQL_SELECT_TIEN_CAP)) {

            ps.setInt(1, nextLevel);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    String message = "Chưa cấu hình nhiệm vụ Tiến cấp level " + nextLevel;
                    if (notify) {
                        Service.gI().sendThongBao(player, message);
                    } else {
                        this.npcChat(player, message);
                    }
                    return null;
                }
                config.dieukien1 = rs.getString("dieukien1");
                config.dieukien2 = rs.getString("dieukien2");
                config.dieukien3 = rs.getString("dieukien3");
                config.dieukien4 = rs.getString("dieukien4");
                config.info = rs.getString("info");
                config.amounts = parseSoluong(rs.getString("soluong"));
                return config;
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (notify) {
                Service.gI().sendThongBao(player, "Có lỗi khi tải nhiệm vụ Tiến cấp");
            } else {
                this.npcChat(player, "Có lỗi khi tải nhiệm vụ Tiến cấp");
            }
            return null;
        }
    }

    private Requirement[] buildRequirements(Player player, TienCapConfig config) {
        int[] amount = config.amounts;
        return switch (config.level) {
            case 1 ->
                new Requirement[] {
                    new Requirement(config.dieukien1, hasPet(player) ? 1 : 0, 1),
                    new Requirement(config.dieukien2, getTienCapGold(player), amount[1]),
                    new Requirement(config.dieukien3, getPowerBillion(player), amount[2], " tỉ Sức mạnh"),
                    new Requirement(config.dieukien4, getTienCapKill(player), amount[3])
                };
            case 2 ->
                new Requirement[] {
                    new Requirement(config.dieukien1, countStarOnSet(player, amount[0]), 5),
                    new Requirement(config.dieukien2, getTienCapGold(player), amount[1]),
                    new Requirement(config.dieukien3, getPowerBillion(player), amount[2], " tỉ Sức mạnh"),
                    new Requirement(config.dieukien4, getTienCapKillBoss(player), amount[3])
                };
            case 3 ->
                new Requirement[] {
                    new Requirement(config.dieukien1, hasMabuPet(player) ? 1 : 0, 1),
                    new Requirement(config.dieukien2, countStarOnSet(player, amount[0]), 5),
                    new Requirement(config.dieukien3, getTienCapGold(player), amount[1]),
                    new Requirement(config.dieukien4, getPowerBillion(player), amount[2], " tỉ Sức mạnh")
                };
            case 4 ->
                new Requirement[] {
                    new Requirement(config.dieukien1, countCapOnSet(player, amount[0]), 5),
                    new Requirement(config.dieukien2, getPowerBillion(player), amount[1], " tỉ Sức mạnh"),
                    new Requirement(config.dieukien3, getCompletedMainTask(player), amount[2]),
                    new Requirement(config.dieukien4, getTienCapKillBoss(player), amount[3])
                };
            case 5 ->
                new Requirement[] {
                    new Requirement(config.dieukien1, hasBinhHutPetOrBetter(player) ? 1 : 0, 1),
                    new Requirement(config.dieukien2, hasItemInAllInventories(player, ITEM_BONG_TAI_CAP_2) ? 1 : 0, 1),
                    new Requirement(config.dieukien3, getPowerBillion(player), amount[2], " tỉ Sức mạnh"),
                    new Requirement(config.dieukien4, getTienCapKillBoss(player), amount[3])
                };
            case 6 ->
                new Requirement[] {
                    new Requirement(config.dieukien1, countStarOnSet(player, amount[0]), 5),
                    new Requirement(config.dieukien2, hasItemInAllInventories(player, ITEM_CHAN_MENH_LEVEL_5) ? 1 : 0, 1),
                    new Requirement(config.dieukien3, getPowerBillion(player), amount[2], " tỉ Sức mạnh"),
                    new Requirement(config.dieukien4, getTienCapKill(player), amount[3])
                };
            case 7 ->
                new Requirement[] {
                    new Requirement(config.dieukien1, getWhisLevel(player), amount[0]),
                    new Requirement(config.dieukien2, getPetPowerBillion(player), amount[1], " tỉ Sức mạnh"),
                    new Requirement(config.dieukien3, getSoSuMenhLevel(player), amount[2]),
                    new Requirement(config.dieukien4, getTienCapKillBoss(player), amount[3])
                };
            default ->
                new Requirement[0];
        };
    }

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

    private boolean hasPet(Player player) {
        return player != null && player.pet != null;
    }

    private boolean hasMabuPet(Player player) {
        return hasPet(player) && player.pet.typePet == 1;
    }

    private boolean hasBinhHutPetOrBetter(Player player) {
        return hasPet(player) && player.pet.typePet >= 2;
    }

    private boolean hasItemInAllInventories(Player player, int itemId) {
        return player != null && InventoryService.gI().findItemInAllInventories(player, itemId) != null;
    }

    private long getPowerBillion(Player player) {
        return player == null || player.nPoint == null ? 0 : player.nPoint.power / ONE_BILLION_POWER;
    }

    private long getPetPowerBillion(Player player) {
        return !hasPet(player) || player.pet.nPoint == null ? 0 : player.pet.nPoint.power / ONE_BILLION_POWER;
    }

    private int getCompletedMainTask(Player player) {
        if (player == null || player.playerTask == null || player.playerTask.taskMain == null) {
            return 0;
        }
        int currentTask = player.playerTask.taskMain.id;
        return Math.max(0, currentTask - 1);
    }

    private int getWhisLevel(Player player) {
        return player == null || player.traning == null ? 0 : player.traning.getTop();
    }

    private int getSoSuMenhLevel(Player player) {
        return player == null || player.sosumenhplayer == null ? 0 : player.sosumenhplayer.getLevel();
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
