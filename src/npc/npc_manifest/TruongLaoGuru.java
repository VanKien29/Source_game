package npc.npc_manifest;

/**
 *
 * @author CongHoan
 */
import clan.Clan;
import consts.ConstClanNamekWar;
import consts.ConstNpc;
import consts.ConstPlayer;
import consts.ConstTranhNgocNamek;
import item.Item;
import java.util.ArrayList;
import models.ClanNamekWar.ClanNamekWarService;
import models.DragonNamecWar.TranhNgoc;
import models.DragonNamecWar.TranhNgocService;
import npc.Npc;
import player.Player;
import server.Manager;
import services.InventoryService;
import services.NpcService;
import services.RewardService;
import services.Service;
import services.TaskService;
import services.func.ChangeMapService;
import services.func.Input;
import shop.ShopService;
import utils.Util;

public class TruongLaoGuru extends Npc {

    public TruongLaoGuru(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            if (ConstClanNamekWar.isRegistrationMap(this.mapId)) {
                if (player.isAdmin()) {
                    createOtherMenu(player, ConstNpc.BASE_MENU,
                            "Bảo Vệ Trưởng Lão Namek\nChỉ bang chủ mới có thể đăng ký.\nThành viên đứng tại khu đăng ký sẽ được đưa vào trận khi bắt đầu.",
                            "Đăng ký", "Danh sách", "Ghép cặp", "Test", "Đóng");
                } else {
                    createOtherMenu(player, ConstNpc.BASE_MENU,
                            "Bảo Vệ Trưởng Lão Namek\nChỉ bang chủ mới có thể đăng ký.\nThành viên đứng tại khu đăng ký sẽ được đưa vào trận khi bắt đầu.",
                            "Đăng ký", "Danh sách", "Đóng");
                }
                return;
            }
            if (this.mapId == ConstClanNamekWar.DEFENSE_MAP_ID) {
                createOtherMenu(player, ConstNpc.BASE_MENU,
                        ClanNamekWarService.gI().getDefenseSupportInfo(player),
                        "Hồi máu\nTrưởng Lão", "Đóng");
                return;
            }
            if (!TaskService.gI().checkDoneTaskTalkNpc(player, this)) {
                if (player.gender != ConstPlayer.NAMEC) {
                    NpcService.gI().createTutorial(player, tempId, avartar,
                            "Con hãy về hành tinh của mình mà thể hiện");
                    return;
                }
                ArrayList<String> menu = new ArrayList<>();
                if (!player.canReward) {
                    menu.add("Nhiệm vụ");
                    menu.add("Học\nKỹ năng");
                    Clan clan = player.clan;
                    if (clan != null) {
                        menu.add("Về khu\nvực bang");
                        if (clan.isLeader(player)) {
                            menu.add("Giải tán\nBang hội");
                        }
                    }
                } else {
                    menu.add("Giao\nLân con");
                }
                String[] menus = menu.toArray(String[]::new);
                createOtherMenu(player, ConstNpc.BASE_MENU,
                        "Chào con, ta rất vui khi gặp được con\nCon muốn làm gì nào ?", menus);

            }
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            if (ConstClanNamekWar.isRegistrationMap(this.mapId) && player.iDMark.isBaseMenu()) {
                switch (select) {
                    case 0 ->
                        ClanNamekWarService.gI().register(player);
                    case 1 ->
                        ClanNamekWarService.gI().showRegistrationInfo(player);
                    case 2 -> {
                        if (player.isAdmin()) {
                            ClanNamekWarService.gI().startMatching(player);
                        }
                    }
                    case 3 -> {
                        if (player.isAdmin()) {
                            ClanNamekWarService.gI().startAdminTest(player);
                        }
                    }
                }
                return;
            }
            if (this.mapId == ConstClanNamekWar.DEFENSE_MAP_ID && player.iDMark.isBaseMenu()) {
                if (select == 0) {
                    ClanNamekWarService.gI().healElderFromNpc(player);
                }
                return;
            }
            if (player.canReward) {
                RewardService.gI().rewardLancon(player);
                return;
            }
            if (player.iDMark.isBaseMenu()) {

                switch (select) {
                    case 0 ->
                        NpcService.gI().createTutorial(player, tempId, avartar,
                                player.playerTask.taskMain.subTasks.get(player.playerTask.taskMain.index).name);
                    case 1 ->
                        Service.gI().sendThongBao(player, "Bạn đã học hết các kỹ năng");
                    case 2 -> {
                        Clan clan = player.clan;
                        if (clan != null) {
                            ChangeMapService.gI().changeMapNonSpaceship(player, 153, Util.nextInt(100, 200), 432);
                        }
                    }
                    case 3 -> {
                        Clan clan = player.clan;
                        if (clan != null) {
                            if (clan.isLeader(player)) {
                                createOtherMenu(player, 3, "Con có chắc muốn giải tán bang hội không?", "Đồng ý",
                                        "Từ chối");
                            }
                        }
                    }
                }
            } else if (player.iDMark.getIndexMenu() == 3) {
                Clan clan = player.clan;
                if (clan != null) {
                    if (clan.isLeader(player)) {
                        if (select == 0) {
                            Input.gI().createFormGiaiTanBangHoi(player);
                        }
                    }
                }
            }
        }
    }
}
