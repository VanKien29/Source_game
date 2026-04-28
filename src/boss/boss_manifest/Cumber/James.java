/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package boss.boss_manifest.Cumber;

import boss.Boss;
import boss.BossID;
import boss.BossesData;
import item.Item;
import map.ItemMap;
import player.Player;
import services.EffectSkillService;
import services.ItemService;
import services.Service;
import utils.Util;

/**
 *
 * @author CongHoan
 */
public class James extends Boss {

    public James() throws Exception {
        super(BossID.JAMES, false, true, BossesData.JAMES);
    }

    @Override
    public void reward(Player plKill) {
        this.pointBoss(plKill);
        if (Util.isTrue(20, 100)) {
            ItemMap it = new ItemMap(this.zone, 1788, 1, this.location.x + Util.nextInt(-15, 15),
                    this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24), plKill.id);
            it.options.add(new Item.ItemOption(50, Util.nextInt(20, 26))); //sd
            it.options.add(new Item.ItemOption(77, Util.nextInt(25, 32))); // hp
            it.options.add(new Item.ItemOption(103, Util.nextInt(25, 32))); // ki
            it.options.add(new Item.ItemOption(101, Util.nextInt(50, 100))); // tnsm
            it.options.add(new Item.ItemOption(93, Util.nextInt(1, 5))); // hsd
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
    }

    @Override
    public synchronized long injured(Player plAtt, long damage, boolean piercing, boolean isMobAttack) {
        if (Util.isTrue(1, 10)) {
            this.chat("Xí Hụt");
            return 0;
        }
        if (!this.isDie()) {
             if (this.currentLevel != 0) {
                damage /= 3;
              }
            damage = this.nPoint.subDameInjureWithDeff(damage / 2);
            if (!piercing && effectSkill.isShielding) {
                if (damage > nPoint.hpMax) {
                    EffectSkillService.gI().breakShield(this);
                }
                damage = 1;
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
            this.leaveMapNew();
        }
        if (this.zone != null && this.zone.getNumOfPlayers() > 0) {
            st = System.currentTimeMillis();
        }
    }

    @Override
    public void joinMap() {
        super.joinMap(); //To change body of generated methods, choose Tools | Templates.
        st = System.currentTimeMillis();
    }
    private long st;

    @Override
    public void wakeupAnotherBossWhenDisappear() {
        if (this.parentBoss != null) {
            this.parentBoss.changeToTypePK();
        }
    }

}
