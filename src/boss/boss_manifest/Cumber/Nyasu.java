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
public class Nyasu extends Boss {

    public Nyasu() throws Exception {
        super(BossID.NYASU, false, true, BossesData.NYASU);
    }

    @Override
    public void reward(Player plKill) {
        this.pointBoss(plKill);
        // đoạn này rơi đeo lưng pokemon
        if (Util.isTrue(20, 100)) {
            int[] itemIds = {1778, 1779, 1780, 1781};
            for (int itemId : itemIds) {
                ItemMap it = new ItemMap(this.zone, itemId, 1,
                        this.location.x + Util.nextInt(-15, 15),
                        this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24), plKill.id);
                it.options.add(new Item.ItemOption(50, Util.nextInt(5, 11))); // sd
                it.options.add(new Item.ItemOption(77, Util.nextInt(5, 11))); // hp
                it.options.add(new Item.ItemOption(103, Util.nextInt(5, 11))); // ki
                it.options.add(new Item.ItemOption(101, Util.nextInt(30, 50))); // tnsm
                it.options.add(new Item.ItemOption(93, Util.nextInt(1, 5))); // hsd
                Service.gI().dropItemMap(this.zone, it);
            }
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
            // if (this.currentLevel != 0) {
            //    damage /= 3;
            //  }
            damage = 1 + (long)(Math.random() * 5_000_000); // 1 → 1,000,000

//            damage = this.nPoint.subDameInjureWithDeff(damage / 2);
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
    public void chatM() {
        if (Util.isTrue(60, 61)) {
            super.chatM();
            return;
        }
        if (this.bossAppearTogether == null || this.bossAppearTogether[this.currentLevel] == null) {
            return;
        }
        for (Boss boss : this.bossAppearTogether[this.currentLevel]) {
            if ((boss.id == BossID.JAMES || boss.id == BossID.JESSIE) && !boss.isDie()) {
                this.chat("Hút năng lượng của nó, mau lên");
                boss.chat("Tuân lệnh đại ca, hê hê hê");
                break;
            }
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
    public void doneChatS() {
        for (Boss boss : this.bossAppearTogether[this.currentLevel]) {
            if (boss.id == BossID.JAMES || boss.id == BossID.JESSIE) {
                boss.changeToTypePK();
            }
        }
    }

    @Override
    public void changeToTypePK() {
        super.changeToTypePK();
        this.chat("Mau đền mạng cho thằng em trai ta");
    }
}
