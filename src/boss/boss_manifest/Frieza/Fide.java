package boss.boss_manifest.Frieza;

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
import utils.Util;

public class Fide extends Boss {

    private long st;

    public Fide() throws Exception {
        super(BossID.FIDE, BossesData.FIDE_DAI_CA_1, BossesData.FIDE_DAI_CA_2, BossesData.FIDE_DAI_CA_3);
    }

    @Override
    public void reward(Player plKill) {
        this.pointBoss(plKill);
        TaskService.gI().checkDoneTaskKillBoss(plKill, this);
        if (Util.isTrue(15, 100)) {
            ItemMap it = new ItemMap(this.zone, Util.nextInt(17, 20), 1, this.location.x,
                    this.zone.map.yPhysicInTop(this.location.x,
                            this.location.y - 24),
                    plKill.id);
            Service.gI().dropItemMap(this.zone, it);
        }
         if (Util.isTrue(30, 100)) {
            ItemMap it = new ItemMap(this.zone, Util.nextInt(748,751), 1, this.location.x, this.zone.map.yPhysicInTop(this.location.x,
                    this.location.y - 24), plKill.id);
            it.options.add(new Item.ItemOption(86, 0));
            Service.gI().dropItemMap(this.zone, it);
        }
         if (plKill != null && !plKill.isPet) {
            plKill.recordTienCapBossKill(this);
          //  TienCap.saveTienCap(plKill); // lưu luôn vào DB
        }
    }

    @Override
    public void joinMap() {
        super.joinMap(); // To change body of generated methods, choose Tools | Templates.
        st = System.currentTimeMillis();
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

}
