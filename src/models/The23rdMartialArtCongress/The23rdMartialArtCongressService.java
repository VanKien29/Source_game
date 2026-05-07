package models.The23rdMartialArtCongress;

/*
 *
 *
 * @author CongHoan
 */

import map.Zone;
import map.Map;
import player.Player;
import network.Message;
import services.MapService;
import services.Service;
import services.func.ChangeMapService;

public class The23rdMartialArtCongressService {

    private static The23rdMartialArtCongressService i;

    public static The23rdMartialArtCongressService gI() {
        if (i == null) {
            i = new The23rdMartialArtCongressService();
        }
        return i;
    }

    public boolean startChallenge(Player player) {
        if (The23rdMartialArtCongressManager.gI().plCheck(player)) {
            Service.gI().sendThongBao(player, "Bạn đang trong trận đấu rồi");
            return false;
        }
        Zone zone = getMapChallenge(129);
        if (zone != null) {
            ChangeMapService.gI().changeMap(player, zone, player.location.x, 360);
            setTimeout(() -> {
                The23rdMartialArtCongress mc = new The23rdMartialArtCongress();
                mc.setZone(zone);
                mc.setPlayer(player);
                mc.setNpc(zone.getNpc());
                mc.setRound(player.levelWoodChest);
                mc.toTheNextRound();
                The23rdMartialArtCongressManager.gI().add(mc);
                Service.gI().sendThongBao(player, "Số thứ tự của ngươi là 1 chuẩn bị thi đấu nhé.");
                Service.gI().releaseCooldownSkill(player);
                player.isPKDHVT = true;
                player.lastTimePKDHVT23 = System.currentTimeMillis();
                mc.endChallenge = false;
            }, 500);
            return true;
        } else {
            Service.gI().sendThongBao(player, "Hiện không có võ đài trống, vui lòng thử lại sau");
        }
        return false;
    }

    public static void setTimeout(Runnable runnable, int delay) {
        new Thread(() -> {
            try {
                Thread.sleep(delay);
                runnable.run();
            } catch (Exception e) {
            }
        }).start();
    }

    public void sendTypePK(Player player, Player boss) {
        Message msg;
        try {
            msg = Service.gI().messageSubCommand((byte) 35);
            msg.writer().writeInt((int) boss.id);
            msg.writer().writeByte(3);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public Zone getMapChallenge(int mapId) {
        Map map = MapService.gI().getMapById(mapId);
        if (map == null || map.zones == null || map.zones.isEmpty()) {
            return null;
        }
        for (Zone zone : map.zones) {
            if (zone != null && zone.getNumOfBosses() < 1) {
                return zone;
            }
        }
        return null;
    }
}
