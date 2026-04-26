package boss.boss_manifest.Nappa;

/*
 *
 *
 * @author CongHoan
 */

import boss.Boss;
import boss.BossID;
import boss.BossStatus;
import boss.BossesData;
import item.Item;
import map.ItemMap;
import player.Player;
import services.Service;
import services.TaskService;
import sosumenh.SoSuMenhService;
import utils.Util;

public class Kuku extends Boss {

    private long st;

    public Kuku() throws Exception {
        super(BossID.KUKU, true, true, BossesData.KUKU);
    }

    @Override
    public void joinMap() {
        super.joinMap();
        st = System.currentTimeMillis();
    }
    @Override
    public void reward(Player plKill) {
        TaskService.gI().checkDoneTaskKillBoss(plKill, this);
//        if (Util.isTrue(50, 100)) {
//            ItemMap it = new ItemMap(this.zone, 1194, 1, this.location.x, this.zone.map.yPhysicInTop(this.location.x,
//                    this.location.y - 24), plKill.id);
//            it.options.add(new Item.ItemOption(73, 0));
//            Service.gI().dropItemMap(this.zone, it);
//        }
        if (Util.isTrue(30, 100)) {
            ItemMap it = new ItemMap(this.zone, Util.nextInt(748,751), 1, this.location.x, this.zone.map.yPhysicInTop(this.location.x,
                    this.location.y - 24), plKill.id);
            it.options.add(new Item.ItemOption(86, 0));
            Service.gI().dropItemMap(this.zone, it);
        }
        SoSuMenhService.gI().updateProgress(plKill, 11, 1);
        if (plKill != null && !plKill.isPet) {
            plKill.tienCapKillBoss++;
          //  TienCap.saveTienCap(plKill); // lưu luôn vào DB
        }
    }

    @Override
    public void autoLeaveMap() {
        if (Util.canDoWithTime(st, 900000)) {
            this.changeStatus(BossStatus.LEAVE_MAP);
        }
        // if (this.zone != null && this.zone.getNumOfPlayers() > 0) {
        // st = System.currentTimeMillis();
        // }
    }
}
