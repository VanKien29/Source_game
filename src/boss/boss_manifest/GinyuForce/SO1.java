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
import services.Service;
import utils.Util;

public class SO1 extends Boss {

    private long st;

    public SO1() throws Exception {
        super(BossID.SO_1, false, true, BossesData.SO_1);
    }

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
        super.reward(plKill);
        if (plKill != null && !plKill.isPet) {
            plKill.tienCapKillBoss++;
          //  TienCap.saveTienCap(plKill); // lưu luôn vào DB
        }
        if (this.currentLevel == 1) {
            return;
        }
        if (Util.isTrue(50, 100)) {
            ItemMap it = new ItemMap(this.zone, 615, 1, this.location.x, this.zone.map.yPhysicInTop(this.location.x,
                    this.location.y - 24), plKill.id);
            it.options.add(new Item.ItemOption(49, 10));
            it.options.add(new Item.ItemOption(77, 10));
            it.options.add(new Item.ItemOption(101, 10));
            Service.gI().dropItemMap(this.zone, it);
        }
        if (Util.isTrue(30, 100)) {
            ItemMap it = new ItemMap(this.zone, Util.nextInt(748,751), 1, this.location.x, this.zone.map.yPhysicInTop(this.location.x,
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
    protected void notifyJoinMap() {
        if (this.currentLevel == 1) {
            return;
        }
        super.notifyJoinMap();
    }

    @Override
    public void doneChatS() {
        this.changeStatus(BossStatus.AFK);
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

    @Override
    public void doneChatE() {
        if (this.parentBoss == null || this.parentBoss.bossAppearTogether == null
                || this.parentBoss.bossAppearTogether[this.parentBoss.currentLevel] == null) {
            return;
        }
        for (Boss boss : this.parentBoss.bossAppearTogether[this.parentBoss.currentLevel]) {
            if (boss.id == BossID.SO_2 && !boss.isDie()) {
                return;
            }
        }
        this.parentBoss.changeStatus(BossStatus.ACTIVE);
    }

}
