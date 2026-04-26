package boss.boss_manifest.Hatchiyac;

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
import services.ItemService;
import services.Service;
import utils.Util;

public class Hatchiyac extends Boss {

    private long st;

    public Hatchiyac() throws Exception {
        super(BossID.Hatchiyac, true, true, BossesData.Hatchiyac);
    }

    @Override
    public void joinMap() {
        super.joinMap();
        st = System.currentTimeMillis();
    }

    @Override
    public void reward(Player plKill) {
        this.pointBoss(plKill);
        if (Util.isTrue(50, 100)) {
            ItemMap it = new ItemMap(this.zone, 1839, 1, this.location.x, this.zone.map.yPhysicInTop(this.location.x,
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
    }

    @Override
    public synchronized long injured(Player plAtt, long damage, boolean piercing, boolean isMobAttack) {
        if (plAtt == null) {
            return 0;
        }
        if (!this.isDie()) {

            if (damage > 100_000_000) {
                damage = 100_000_000;
            }
            this.nPoint.subHP(damage);
            if (isDie()) {
                this.setDie(plAtt);
                die(plAtt);
            }
            return damage;
        } else {
            return 0;
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
