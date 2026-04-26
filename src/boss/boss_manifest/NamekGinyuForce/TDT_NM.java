package boss.boss_manifest.NamekGinyuForce;

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
import utils.Util;

public class TDT_NM extends Boss {

    private long st;

    public TDT_NM() throws Exception {
        super(BossID.TIEU_DOI_TRUONG_NM, false, true, BossesData.TIEU_DOI_TRUONG_NM);
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
        if (this.currentLevel == 1) {
            return;
        }
         if (Util.isTrue(20, 100)) {
            ItemMap it = new ItemMap(this.zone, 617, 1, this.location.x, this.zone.map.yPhysicInTop(this.location.x,
                    this.location.y - 24), plKill.id);
            it.options.add(new Item.ItemOption(49, 10));
            it.options.add(new Item.ItemOption(77, 10));
            it.options.add(new Item.ItemOption(101, 10));
            Service.gI().dropItemMap(this.zone, it);
        } 
         if (Util.isTrue(50, 100)) {
            ItemMap it = new ItemMap(this.zone, Util.nextInt(748,751), 1, this.location.x, this.zone.map.yPhysicInTop(this.location.x,
                    this.location.y - 24), plKill.id);
            it.options.add(new Item.ItemOption(86, 0));
            Service.gI().dropItemMap(this.zone, it);
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
        if (Util.canDoWithTime(st, 900000)) {
            this.leaveMapNew();
        }
        if (this.zone != null && this.zone.getNumOfPlayers() > 0) {
            st = System.currentTimeMillis();
        }
    }

}
