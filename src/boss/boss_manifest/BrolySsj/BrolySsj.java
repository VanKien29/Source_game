package boss.boss_manifest.BrolySsj;

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
import services.ItemService;
import services.Service;
import services.TaskService;
import utils.Util;

public class BrolySsj extends Boss {

    private long st;

    public BrolySsj() throws Exception {
        super(BossID.BROLY_SSJ, BossesData.SSJ_1, BossesData.SSJ_2);
    }

    @Override
    public void reward(Player plKill) {
        this.pointBoss(plKill);
        if (Util.isTrue(20, 100)) {
            ItemMap it = ItemService.gI().randDoTL(this.zone, 1, this.location.x,
                    this.zone.map.yPhysicInTop(this.location.x,
                            this.location.y - 24),
                    plKill.id);
            Service.gI().dropItemMap(this.zone, it);
        }
        if (plKill != null && Util.isTrue(100, 100)) {
            ItemMap it = new ItemMap(this.zone, 1959, Util.nextInt(1, 5), this.location.x, this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24), plKill.id);
            Service.gI().dropItemMap(this.zone, it);
        }
        if (plKill != null && !plKill.isPet) {
            plKill.tienCapKillBoss++;
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
