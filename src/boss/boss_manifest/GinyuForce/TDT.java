package boss.boss_manifest.GinyuForce;

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
import npc.npc_manifest.TienCap;
import player.Player;
import services.EffectSkillService;
import services.Service;
import utils.Util;

public class TDT extends Boss {

    private long st;

    private long lastBodyChangeTime;

    public TDT() throws Exception {
        super(BossID.TIEU_DOI_TRUONG, false, true, BossesData.TIEU_DOI_TRUONG);
    }

//    private void bodyChangePlayerInMap() {
//        if (this.zone != null) {
//            for (Player pl : this.zone.getPlayers()) {
//                if (Util.isTrue(5, 10) && pl.effectSkill != null && !pl.effectSkill.isBodyChangeTechnique) {
//                    EffectSkillService.gI().setIsBodyChangeTechnique(pl);
//                }
//            }
//        }
//    }
    @Override
    public void moveTo(int x, int y) {
        if (this.currentLevel == 1) {
            return;
        }
        super.moveTo(x, y);
    }

    @Override
    public void reward(Player plKill) {
        this.pointBoss(plKill);
        if (plKill != null && !plKill.isPet) {
            plKill.recordTienCapBossKill(this);
        //    TienCap.saveTienCap(plKill); // lưu luôn vào DB
        }
        super.reward(plKill);
//        if (this.currentLevel == 1) {
//            return;
//        }
        if (Util.isTrue(30, 100)) {
            ItemMap it = new ItemMap(this.zone, 617, 1, this.location.x, this.zone.map.yPhysicInTop(this.location.x,
                    this.location.y - 24), plKill.id);
            it.options.add(new Item.ItemOption(49, 10));
            it.options.add(new Item.ItemOption(77, 10));
            it.options.add(new Item.ItemOption(101, 10));
            Service.gI().dropItemMap(this.zone, it);
        }
        if (Util.isTrue(30, 100)) {
            ItemMap it = new ItemMap(this.zone, Util.nextInt(748, 751), 1, this.location.x, this.zone.map.yPhysicInTop(this.location.x,
                    this.location.y - 24), plKill.id);
            it.options.add(new Item.ItemOption(86, 0));
            Service.gI().dropItemMap(this.zone, it);
        }
//        if (Util.isTrue(30, 100)) {
//            ItemMap it = new ItemMap(this.zone, 1438, 1, this.location.x, this.zone.map.yPhysicInTop(this.location.x,
//                    this.location.y - 24), plKill.id);
//            Service.gI().dropItemMap(this.zone, it);
//        }
    }

    @Override
    public synchronized long injured(Player plAtt, long damage, boolean piercing, boolean isMobAttack) {
        if (Util.isTrue(3, 10)) {
            this.chat("Xí Hụt");
            return 0;
        }
        if (!this.isDie()) {
            damage = this.nPoint.subDameInjureWithDeff(damage / 2);
            if (piercing) {
                damage = 1;
            }
            this.nPoint.subHP(damage);
            if (this.nPoint.hp <= 0) {
                this.setDie(plAtt);
                this.die(plAtt);
            }

            return damage;
        } else {
            return 0;
        }
    }

    @Override
    protected void notifyJoinMap() {
        if (this.currentLevel == 1) {
            return;
        }
        super.notifyJoinMap();
    }

    @Override
    public void attack() {
//        if (Util.canDoWithTime(lastBodyChangeTime, 10000)) {
//            bodyChangePlayerInMap();
//            this.chat("Lạy bố tha con đi");
//            this.lastBodyChangeTime = System.currentTimeMillis();
//        }
        super.attack();
    }

    @Override
    public void joinMap() {
        super.joinMap();
        st = System.currentTimeMillis();
    }

    @Override
    public void doneChatS() {
        this.changeStatus(BossStatus.AFK);
    }

    @Override
    public void autoLeaveMap() {
        try {
            // Nếu boss đã chết thì rời map ngay
            if (this.isDie()) {
                this.leaveMapNew();
                return;
            }
            if (Util.canDoWithTime(st, 900000)) {
                this.leaveMapNew();
            }
            if (this.zone != null && this.zone.getNumOfPlayers() > 0) {
                st = System.currentTimeMillis();
            }
        } catch (Exception e) {
            System.err.println("[BossError] " + this.name + " autoLeaveMap error: " + e.getMessage());
        }
    }
}
