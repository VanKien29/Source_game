package npc.npc_manifest;

/**
 *
 * @author CongHoan
 */

import consts.ConstNpc;
import consts.ConstTask;
import map.Map;
import npc.Npc;
import player.Player;
import services.MapService;
import services.NpcService;
import services.Service;
import services.TaskService;
import services.func.ChangeMapService;
import utils.Util;

public class Calick extends Npc {

    private final byte COUNT_CHANGE = 25;
    private int count;

    public Calick(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (TaskService.gI().checkDoneTaskTalkNpc(player, this)) {
            return;
        }
        player.iDMark.setIndexMenu(ConstNpc.BASE_MENU);
        if (TaskService.gI().getIdTask(player) < ConstTask.TASK_23_0) {
            Service.gI().hideWaitDialog(player);
            Service.gI().sendThongBao(player, "Hãy hoàn thành nhiệm vụ 22 trước");
            return;
        }

        if (this.mapId == 102) {
            this.createOtherMenu(player, ConstNpc.BASE_MENU,
                    "Chào chú, cháu có thể giúp gì?",
                    "Kể\nChuyện", "Quay về\nQuá khứ");
        } else {
            this.createOtherMenu(player, ConstNpc.BASE_MENU,
                    "Chào chú, cháu có thể giúp gì?", "Kể\nChuyện", "Đi đến\nTương lai", "Từ chối");
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (this.mapId == 102) {
            if (player.iDMark.isBaseMenu()) {
                if (select == 0) {
                    //kể chuyện
                    NpcService.gI().createTutorial(player, tempId, this.avartar, ConstNpc.CALICK_KE_CHUYEN);
                } else if (select == 1) {
                    //về quá khứ
                    ChangeMapService.gI().goToQuaKhu(player);
                }
            }
        } else if (player.iDMark.isBaseMenu()) {
            switch (select) {
                case 0 -> //kể chuyện
                    NpcService.gI().createTutorial(player, tempId, this.avartar, ConstNpc.CALICK_KE_CHUYEN);
                case 1 -> {
                    //đến tương lai
                    if (TaskService.gI().getIdTask(player) >= ConstTask.TASK_23_0) {
                        ChangeMapService.gI().goToTuongLai(player);
                    } else {
                        Service.gI().sendThongBao(player, "Hãy hoàn thành nhiệm vụ 22 trước");
                    }
                }
                default ->
                    Service.gI().sendThongBao(player, "Không thể thực hiện");
            }
        }
    }
}
