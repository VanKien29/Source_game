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
import utils.Util;

public class NamekElderBoss extends Boss {

    private final ClanNamekWarMatch match;
    private final Zone spawnZone;

    public NamekElderBoss(ClanNamekWarMatch match, Zone zone) throws Exception {
        super(-250000 - match.id, true, true, new BossData(
                "Truong Lao Namek",
                ConstPlayer.NAMEC,
                new short[]{14, 15, 16, -1, -1, -1},
                500_000,
                new long[]{ConstClanNamekWar.ELDER_HP},
                new int[]{ConstClanNamekWar.DEFENSE_MAP_ID},
                new int[][]{{Skill.DRAGON, 7, 1000}, {Skill.MASENKO, 7, 1500}},
                new String[]{"|-1|Hãy bảo vệ người Namek..."},
                new String[]{},
                new String[]{"|-1|Namek... giao lại cho các con..."},
                0));
        this.match = match;
        this.spawnZone = zone;
        this.zone = zone;
    }

    @Override
    public void joinMap() {
        ChangeMapService.gI().goToMap(this, this.spawnZone);
        this.zone = this.spawnZone;
        this.location.x = ConstClanNamekWar.ELDER_SPAWN_X;
        this.location.y = ConstClanNamekWar.ELDER_SPAWN_Y;
        this.zone.load_Me_To_Another(this);
        Service.gI().sendFlagBag(this);
        this.changeToTypePK();
        this.changeStatus(boss.BossStatus.CHAT_S);
        if (this.zone != null) {
            Service.gI().sendThongBao(this.zone.getPlayers(), "Trưởng Lão Namek đã xuất hiện.");
        }
    }

    @Override
    public void doneChatS() {
        this.changeToTypePK();
        this.changeStatus(boss.BossStatus.ACTIVE);
        Service.gI().setPos(this, ConstClanNamekWar.ELDER_SPAWN_X, ConstClanNamekWar.ELDER_SPAWN_Y);
    }

    @Override
    public void afk() {
        this.nPoint.mp = this.nPoint.mpg;
    }

    @Override
    public void active() {
        this.nPoint.mp = this.nPoint.mpg;
        if (Util.canDoWithTime(this.lastTimeAttack, 1000)) {
            Service.gI().setPos(this, ConstClanNamekWar.ELDER_SPAWN_X, ConstClanNamekWar.ELDER_SPAWN_Y);
            this.lastTimeAttack = System.currentTimeMillis();
        }
    }

    @Override
    public synchronized long injured(Player plAtt, long damage, boolean piercing, boolean isMobAttack) {
        if (plAtt == null || !match.canDamageElder(plAtt)) {
            return 0;
        }
        long realDamage = super.injured(plAtt, damage, true, isMobAttack);
        match.recordElderDamage(plAtt, realDamage);
        return realDamage;
    }

    @Override
    public void die(Player plKill) {
        match.onElderKilled();
        if (this.zone != null) {
            Service.gI().sendThongBao(this.zone.getPlayers(), "Trưởng Lão Namek đã bị hạ.");
        }
        this.setRuntimeDisabled(true);
    }
}
