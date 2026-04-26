package boss.boss_manifest.Doraemon;

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
import java.util.List;
import map.ItemMap;
import player.Player;
import services.ItemService;
import services.Service;
import utils.Util;

public class Nobita extends Boss {

    private long st;

    public Nobita() throws Exception {
        super(BossID.NOBITA, false, true, BossesData.NOBITA);
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
        if (Util.isTrue(75, 100)) {
            ItemMap it = new ItemMap(this.zone, 1725, Util.nextInt(1, 5), this.location.x, this.zone.map.yPhysicInTop(this.location.x,
                    this.location.y - 24), plKill.id);
            Service.gI().dropItemMap(this.zone, it);
        }
        if (Util.isTrue(1, 5)) {
            ItemMap it = new ItemMap(this.zone, 1423, 1, this.location.x, this.zone.map.yPhysicInTop(this.location.x,
                    this.location.y - 24), plKill.id);
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
    protected void notifyJoinMap() {
        if (this.currentLevel == 1) {
            return;
        }
        super.notifyJoinMap();
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
            if ((boss.id == BossID.DORAEMON || boss.id == BossID.XEKO || boss.id == BossID.XUKA || boss.id == BossID.CHAIEN) && !boss.isDie()) {
                return;
            }
        }
        this.parentBoss.changeStatus(BossStatus.ACTIVE);
    }

}
