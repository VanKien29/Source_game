package npc;

/*
 *
 *
 * @author CongHoan
 */

import consts.ConstNpc;
import consts.ConstTask;
import player.Player;
import server.Manager;
import services.TaskService;
import java.util.ArrayList;
import java.util.List;
import services.MapService;
import utils.Util;

public class NpcManager {
    
    private static int calickchangemap = 0;
    private static long calickTimeChange = 0;

    public static Npc getByIdAndMap(int id, int mapId) {
        for (Npc npc : Manager.NPCS) {
            if (npc.tempId == id && npc.mapId == mapId) {
                return npc;
            }
        }
        return null;
    }

    public static Npc getNpc(byte tempId) {
        for (Npc npc : Manager.NPCS) {
            if (npc.tempId == tempId) {
                return npc;
            }
        }
        return null;
    }

    public static boolean canSeeCalick(Player player) {
        return player != null && TaskService.gI().getIdTask(player) >= ConstTask.TASK_23_0;
    }

    public static List<Npc> getNpcsByMapPlayer(Player player) {
        List<Npc> list = new ArrayList<>();
        if (player.zone != null) {
            for (Npc npc : player.zone.map.npcs) {
                if (npc.tempId == ConstNpc.QUA_TRUNG && player.mabuEgg == null
                        && player.zone.map.mapId == (21 + player.gender)) {
                    continue;
                } else if (npc.tempId == ConstNpc.CALICK && !canSeeCalick(player)) {
                    continue;
                } else if (npc.tempId == ConstNpc.CALICK && player.zone.map.mapId != 102 && MapService.gI().isMapCalick(player.zone.map.mapId)) {
                    if (System.currentTimeMillis() >= calickTimeChange) {
                        calickchangemap = Util.nextInt(27, 29);
                        calickTimeChange = System.currentTimeMillis() + 6000;
                    }
                    if (player.zone.map.mapId != calickchangemap) {
                        continue;
                    }
                    npc.cx = Util.nextInt(20, player.zone.map.mapWidth -20);
                    npc.cy = player.zone.map.yPhysicInTop(npc.cx, 0);
                }
                list.add(npc);
            }
        }
        return list;
    }
}
