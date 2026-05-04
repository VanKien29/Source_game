package boss.boss_manifest.ClanNamekWar;

import boss.Boss;
import boss.BossData;
import consts.ConstClanNamekWar;
import consts.ConstPlayer;
import map.Zone;
import models.ClanNamekWar.ClanNamekWarMatch;
import player.Player;
import services.Service;
import services.func.ChangeMapService;
import skill.Skill;

public class NamekShieldPillarBoss extends Boss {

    private final ClanNamekWarMatch match;
    private final int index;
    private final Zone spawnZone;

    public NamekShieldPillarBoss(ClanNamekWarMatch match, int index, Zone zone) throws Exception {
        super(-251000 - match.id * 10 - index, true, true, new BossData(
                "Tru La Chan Namek " + (index + 1),
                ConstPlayer.TRAI_DAT,
                new short[]{138, 139, 140, -1, -1, -1},
                1,
                new long[]{ConstClanNamekWar.SHIELD_PILLAR_HP},
                new int[]{ConstClanNamekWar.MIDDLE_MAP_ID},
                new int[][]{{Skill.DRAGON, 1, 1000}},
                new String[]{},
                new String[]{},
                new String[]{},
                0));
        this.match = match;
        this.index = index;
        this.spawnZone = zone;
        this.zone = zone;
    }

    @Override
    public void joinMap() {
        int x = ConstClanNamekWar.SHIELD_PILLAR_X[index % ConstClanNamekWar.SHIELD_PILLAR_X.length];
        int y = ConstClanNamekWar.SHIELD_PILLAR_Y[index % ConstClanNamekWar.SHIELD_PILLAR_Y.length];
        ChangeMapService.gI().goToMap(this, this.spawnZone);
        this.zone = this.spawnZone;
        this.location.x = x;
        this.location.y = y;
        this.zone.load_Me_To_Another(this);
        Service.gI().sendFlagBag(this);
        this.changeToTypePK();
        this.changeStatus(boss.BossStatus.AFK);
    }

    @Override
    public void afk() {
        this.nPoint.mp = this.nPoint.mpg;
    }

    @Override
    public synchronized long injured(Player plAtt, long damage, boolean piercing, boolean isMobAttack) {
        if (plAtt == null || !match.isAttacker(plAtt)) {
            return 0;
        }
        return super.injured(plAtt, damage, true, isMobAttack);
    }

    @Override
    public void die(Player plKill) {
        match.onShieldPillarKilled(this);
        if (this.zone != null) {
            Service.gI().sendThongBao(this.zone.getPlayers(), this.name + " đã bị phá hủy.");
        }
        this.setRuntimeDisabled(true);
    }
}
