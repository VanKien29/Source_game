package boss.boss_manifest.Training;

/*
 *
 *
 * @author CongHoan
 */

import boss.BossID;
import boss.BossStatus;
import boss.BossesData;
import static boss.BossType.PHOBAN;
import player.Player;
import services.func.ChangeMapService;
import utils.Util;

public class MrPoPo extends TrainingBoss {

    private long lastTimeBay;
    private long lastTimeBay2;
    private int hitCount;

    public MrPoPo(Player player) throws Exception {
        super(PHOBAN, BossID.MRPOPO, BossesData.MRPOPO);
        this.playerAtt = player;
    }

    @Override
    public void joinMap() {
        if (playerAtt.zone != null) {
            this.zone = playerAtt.zone;
            ChangeMapService.gI().changeMap(this, this.zone, 295, 408);
            this.changeStatus(BossStatus.CHAT_S);
        }
    }

    @Override
    public void afk() {
        if (Util.canDoWithTime(lastTimeAFK, 15000)) {
            this.changeStatus(BossStatus.LEAVE_MAP);
        }
    }

    @Override
    public boolean chatS() {
        if (Util.canDoWithTime(lastTimeChatS, timeChatS)) {
            if (this.doneChatS) {
                return true;
            }
            String textChat = this.data[this.currentLevel].getTextS()[playerAtt.isThachDau ? 1 : 0];
            int prefix = Integer.parseInt(textChat.substring(1, textChat.lastIndexOf("|")));
            textChat = textChat.substring(textChat.lastIndexOf("|") + 1);
            if (!this.chat(prefix, textChat)) {
                return false;
            }
            this.moveToPlayer(playerAtt);
            this.lastTimeChatS = System.currentTimeMillis();
            this.timeChatS = 100;
            doneChatS = true;
        }
        return false;
    }

    @Override
    public synchronized long injured(Player plAtt, long damage, boolean piercing, boolean isMobAttack) {
        if (this.isDie()) {
            return 0;
        }
        if (!piercing && Util.isTrue(400, 1000)) {
            this.chat("Xí hụt");
            return 0;
        }
        if (plAtt != null && plAtt.idNRNM != -1) {
            return 1;
        }
        if (damage <= 0) {
            return 0;
        }

        int hitRequired = playerAtt != null && playerAtt.isThachDau ? 3 : 1;
        hitCount++;
        if (hitCount >= hitRequired) {
            long damageHit = Math.max(1, this.nPoint.hp);
            this.nPoint.hp = 0;
            this.setDie(plAtt);
            die(plAtt);
            return damageHit;
        }
        if (this.nPoint.hp > 1) {
            this.nPoint.subHP(1);
        }
        return 1;
    }

    @Override
    public void buffPea() {
    }

    @Override
    public void bayLungTung() {
        if (Util.canDoWithTime(lastTimeBay, 3000)) {
            goToXY(playerAtt.location.x + (Util.getOne(-1, 1) * Util.nextInt(20, 80)),
                    this.location.y + Util.getOne(-100, 10), false);
            lastTimeBay = System.currentTimeMillis();
        }
        if (Util.canDoWithTime(lastTimeBay2, 4000)) {
            goToXY(playerAtt.location.x + (Util.getOne(-1, 1) * Util.nextInt(20, 80)),
                    this.location.y + Util.getOne(-100, 10), false);
            lastTimeBay2 = System.currentTimeMillis();
        }
    }

}
