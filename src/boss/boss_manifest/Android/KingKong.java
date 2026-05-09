package boss.boss_manifest.Android;

/*
 *
 *
 * @author CongHoan
 */

import boss.Boss;
import boss.BossID;
import boss.BossStatus;
import boss.BossesData;
import map.ItemMap;
import player.Player;
import services.Service;
import services.TaskService;
import sosumenh.SoSuMenhService;
import utils.Util;

public class KingKong extends Boss {

    public KingKong() throws Exception {
        super(BossID.KING_KONG, BossesData.KING_KONG);
    }

    @Override
    public void reward(Player plKill) {
        int[] itemRan = new int[] { 380, 381, 382, 383, 384, 385 };
        int itemId = itemRan[2];
        if (Util.isTrue(20, 100)) {
            ItemMap it = new ItemMap(this.zone, itemId, 1, this.location.x, this.zone.map.yPhysicInTop(this.location.x,
                    this.location.y - 24), plKill.id);
            Service.gI().dropItemMap(this.zone, it);
        }
        if (Util.isTrue(5, 50)) {
            for (int i = 0; i < Util.nextInt(25, 50); i++) {
                ItemMap it = new ItemMap(this.zone, 1229, 1, this.location.x + Util.nextInt(-15, 15),
                        this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24), plKill.id);
                Service.gI().dropItemMap(this.zone, it);
            }
        }
        if (plKill != null && !plKill.isPet) {
            plKill.recordTienCapBossKill(this);
          //  TienCap.saveTienCap(plKill); // lưu luôn vào DB
        }
        TaskService.gI().checkDoneTaskKillBoss(plKill, this);
        SoSuMenhService.gI().updateProgress(plKill, 16, 1);
    }

    @Override
    public void autoLeaveMap() {
        if (Util.canDoWithTime(st, 900000)) {
            this.leaveMapNew();
        }
        if (this.zone != null && this.zone.getNumOfPlayers() > 0) {
            st = System.currentTimeMillis();
        }
    }

    @Override
    public void joinMap() {
        super.joinMap();
        st = System.currentTimeMillis();
    }

    private long st;

    @Override
    public void doneChatS() {
        this.changeStatus(BossStatus.AFK);
    }
}
