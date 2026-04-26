package boss.boss_manifest.Cooler;

/*
 *
 *
 * @author CongHoan
 */
import boss.Boss;
import boss.BossID;
import boss.BossesData;

import item.Item;
import map.ItemMap;
import player.Player;
import services.EffectSkillService;
import services.Service;
import utils.Util;

import java.util.Random;
import services.ItemService;
import services.TaskService;

public class Cooler extends Boss {

    private long st;

    public Cooler() throws Exception {
        super(BossID.COOLER, BossesData.COOLER, BossesData.COOLER_2);
    }

    @Override
    public void reward(Player plKill) {
        this.pointBoss(plKill);
//        if (TaskService.gI().getIdTask(plKill) == ConstTask.TASK_31_0) {
//            Service.gI().dropItemMap(this.zone, new ItemMap(zone, 992, 1, this.location.x, this.location.y, plKill.id));
//        }
        if (Util.isTrue(20, 100)) {
            ItemMap it = ItemService.gI().randDoTL(this.zone, 1, this.location.x,
                    this.zone.map.yPhysicInTop(this.location.x,
                            this.location.y - 24),
                    plKill.id);
            Service.gI().dropItemMap(this.zone, it);
        }
        if (plKill != null && !plKill.isPet) {
            plKill.tienCapKillBoss++;
          //  TienCap.saveTienCap(plKill); // lưu luôn vào DB
        }
        if (plKill != null && Util.isTrue(100, 100)) {
            ItemMap it = new ItemMap(this.zone, 1959, Util.nextInt(1, 5), this.location.x, this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24), plKill.id);
            Service.gI().dropItemMap(this.zone, it);
        }
//        if (Util.isTrue(5, 50)) {
//            for (int i = 0; i < Util.nextInt(25, 50); i++) {
//                ItemMap it = new ItemMap(this.zone, 1229, 1, this.location.x + Util.nextInt(-15, 15),
//                        this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24), plKill.id);
//                Service.gI().dropItemMap(this.zone, it);
//            }
//        }
    }

    @Override
    public synchronized long injured(Player plAtt, long damage, boolean piercing, boolean isMobAttack) {
        if (!this.isDie()) {
            if (piercing) {
                damage /= 100;
            }
            if (Util.isTrue(200, 1000)) {
                this.chat("Xí hụt");
                return 0;
            }
            damage = this.nPoint.subDameInjureWithDeff(damage);
            if (effectSkill.isShielding) {
                if (damage > nPoint.hpMax) {
                    EffectSkillService.gI().breakShield(this);
                }
                damage = 1;
            }
            if (damage > 10_000_000) {
                damage = Util.nextInt(9_000_000, 10_000_000);
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
    public void joinMap() {
        super.joinMap();
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
