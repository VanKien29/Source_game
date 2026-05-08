package boss.boss_manifest.Wolves;

/*
 *
 *
 * @author CongHoan
 */
import boss.boss_manifest.BrolySsj.*;
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

public class Baconsoi extends Boss {

    private long st;

    public Baconsoi() throws Exception {
        super(BossID.BA_CON_SOI, false, true, BossesData.SOI_1, BossesData.SOI_2, BossesData.SOI_3);
    }

    @Override
    public void reward(Player plKill) {
        this.pointBoss(plKill);
        if (Util.isTrue(5, 100)) {
            ItemMap it = ItemService.gI().randDoTL(this.zone, 1, this.location.x,
                    this.zone.map.yPhysicInTop(this.location.x,
                            this.location.y - 24),
                    plKill.id);
            Service.gI().dropItemMap(this.zone, it);
        }
        if (Util.isTrue(20, 100)) {
            ItemMap it = new ItemMap(this.zone, 1965, 1, this.location.x, this.zone.map.yPhysicInTop(this.location.x,
                    this.location.y - 24), plKill.id);
            it.options.add(new Item.ItemOption(30, 0));
            Service.gI().dropItemMap(this.zone, it);
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
