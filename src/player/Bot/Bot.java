package player.Bot;

import consts.ConstPlayer;
import java.util.Random;
import jdbc.daos.PlayerDAO;
import map.Map;
import map.Zone;
import models.ClanNamekWar.ClanNamekWarService;
import models.Template.SkillTemplate;
import player.Player;
import server.Manager;
import services.EffectSkillService;
import services.MapService;
import services.PlayerService;
import services.Service;
import services.SkillService;
import services.func.ChangeMapService;
import skill.NClass;
import skill.Skill;
import utils.Util;

/**
 *
 * @author Bùi Công Hoan
 */
public class Bot extends Player {

    public static final int TYPE_TRAIN_MOB = 0;
    public static final int TYPE_SHOP = 1;
    public static final int TYPE_HUNT_BOSS = 2;
    public static final int TYPE_SMART = 3;
    public static final int TYPE_CLAN_NAMEK_WAR = 99;
    public static final int SMART_MODE_FARM = 0;
    public static final int SMART_MODE_SOCIAL = 1;
    public static final int SMART_MODE_PET_TRAIN = 2;

    private short head_;
    private short body_;
    private short leg_;
    private short flag_;
    private int type;
    private int index_ = 0;
    public ShopBot shop;
    public Sanb boss;
    public Mobb mo1;
    public long clanWarNextMoveAt;
    public long clanWarNextSkillAt;
    public long clanWarNextRouteAt;
    public int clanWarRouteStep;
    public long smartNextThinkAt;
    public long smartNextMapAt;
    public long smartNextBossAt;
    public long smartNextRoamAt;
    public int smartPartyId;
    public long smartLeaderId;
    public int smartPreferredMapId = -1;
    public int smartTargetMobId = -1;
    public int smartMode = SMART_MODE_FARM;

    private Player plAttack;

    private int[] TraiDat = new int[]{1, 2, 3, 4, 6, 29, 30, 28, 27, 42};
    private int[] Namec = new int[]{8, 9, 10, 11, 12, 13, 33, 34, 32, 31};
    private int[] XayDa = new int[]{15, 16, 17, 18, 19, 20, 37, 36, 35, 44, 52};

    public Bot(short head, short body, short leg, int type, String name, ShopBot shop, short flag) {
        this.head_ = head;
        this.body_ = body;
        this.leg_ = leg;
        this.shop = shop;
        this.name = name;
        this.id = new Random().nextInt(2000000000);
        this.type = type;
        this.isBot = true;
        this.flag_ = flag;
        this.head = head;

    }

    public int MapToPow() {
        Random random = new Random();
        long power = this.nPoint.power;
        int mapId = 21;
        if (power < 20000000) {
            if (this.gender == 0) {
                mapId = TraiDat[random.nextInt(TraiDat.length)];
            } else if (this.gender == 1) {
                mapId = Namec[random.nextInt(Namec.length)];
            } else {
                mapId = XayDa[random.nextInt(XayDa.length)];
            }
        } else if (power < 100000000) {
            mapId = 62 + random.nextInt(15);
        } else if (power < 1000000000) {
            if (Util.isTrue(30, 100)) {
                mapId = 91 + random.nextInt(3);
            } else if (Util.isTrue(30, 100)) {
                mapId = 95 + random.nextInt(5);
            } else {
                mapId = 102 + random.nextInt(2);
            }
        } else {
            if (Util.isTrue(30, 100)) {
                mapId = 104 + random.nextInt(6);
            } else if (Util.isTrue(30, 100)) {
                mapId = 173 + random.nextInt(3);
            } else {
                mapId = 157 + random.nextInt(2);
            }
        }
        return mapId;
    }

    public void joinMap() {
        Zone zone = getRandomZone(MapToPow());
        if (zone != null) {
            ChangeMapService.gI().goToMap(this, zone);
            this.zone.load_Me_To_Another(this);
            this.mo1.lastTimeChanM = System.currentTimeMillis();
        }
    }

    public Zone getRandomZone(int mapId) {
        Map map = MapService.gI().getMapById(mapId);
        Zone zone = null;
        try {
            if (map != null) {
                zone = map.zones.stream()
                        .filter(z -> z.getNumOfPlayers() == 0)
                        .findFirst()
                        .orElseGet(() -> {
                            Zone randomZone = map.zones.get(Util.nextInt(0, map.zones.size() - 1));
                            return randomZone.isFullPlayer() ? null : randomZone;
                        });
            }
        } catch (Exception e) {
        }
        if (zone != null) {
            this.index_ = 0;
            return zone;
        } else {
            this.index_ += 1;
            if (this.index_ >= 20) {
                BotManager.gI().bot.remove(this);
                ChangeMapService.gI().exitMap(this);
                return null;
            } else {
                return getRandomZone(MapToPow());
            }
        }
    }

    public int getBotType() {
        return this.type;
    }

    public boolean isSmartBot() {
        return this.type == TYPE_SMART;
    }

    private boolean isProfileOutfitBot() {
        return this.type == TYPE_SMART || this.type == TYPE_CLAN_NAMEK_WAR;
    }

    @Override
    public short getHead() {
        if (isProfileOutfitBot()) {
            return super.getHead();
        }
        return effectSkill.isMonkey ? (short) ConstPlayer.HEADMONKEY[effectSkill.levelMonkey - 1] : this.head_;
    }

    @Override
    public short getBody() {
        if (isProfileOutfitBot()) {
            return super.getBody();
        }
        return effectSkill.isMonkey ? 193 : this.body_;
    }

    @Override
    public short getLeg() {
        if (isProfileOutfitBot()) {
            return super.getLeg();
        }
        return effectSkill.isMonkey ? 194 : this.leg_;
    }

    @Override
    public short getFlagBag() {
        if (isProfileOutfitBot()) {
            return super.getFlagBag();
        }
        return this.flag_;
    }

   

    @Override
    public void update() {
        super.update();
        this.updateBotPassive();
        this.increasePoint();
        switch (this.type) {
            case TYPE_TRAIN_MOB:
                this.mo1.update();
                break;
            case TYPE_SHOP:
                this.shop.update();
                break;
            case TYPE_HUNT_BOSS:
                this.boss.update();
                break;
            case TYPE_SMART:
                SmartBotAI.gI().update(this);
                break;
            case TYPE_CLAN_NAMEK_WAR:
                ClanNamekWarService.gI().updateTestBot(this);
                break;
        }
        if (this.isDie() && this.type != TYPE_CLAN_NAMEK_WAR) {
            Service.gI().hsChar(this, nPoint.hpMax, nPoint.mpMax);
        }
    }

    private void updateBotPassive() {
        try {
            if (nPoint != null) {
                nPoint.update();
            }
            if (fusion != null) {
                fusion.update();
            }
            if (effectSkill != null) {
                effectSkill.update();
            }
            if (mobMe != null) {
                mobMe.update();
            }
            if (effectSkin != null) {
                effectSkin.update();
            }
            if (satellite != null) {
                satellite.update();
            }
            if (pet != null) {
                pet.update();
            }
            if (newPet != null) {
                newPet.update();
            }
        } catch (Exception ignored) {
        }
    }

    public void leakSkill() {
        for (NClass n : Manager.gI().NCLASS) {
            if (n.classId == this.gender) {
                for (SkillTemplate Template : n.skillTemplatess) {
                    for (Skill skills : Template.skillss) {
                        Skill cloneSkill = new Skill(skills);
                        this.playerSkill.skills.add(cloneSkill);
                        break;
                    }
                }
                break;
            }
        }
    }

    public boolean UseLastTimeSkill() {
        if (this.playerSkill == null || this.playerSkill.skillSelect == null) {
            return false;
        }
        if (this.playerSkill.skillSelect.lastTimeUseThisSkillbot < (System.currentTimeMillis() - this.playerSkill.skillSelect.coolDown)) {
            this.playerSkill.skillSelect.lastTimeUseThisSkillbot = System.currentTimeMillis();
            return true;
        } else {
            return false;
        }
    }

    private void increasePoint() {
        long tiemNangUse = 0;
        int point = 0;
        if (this.nPoint != null) {
            if (Util.isTrue(50, 100)) {
                point = 100;
                int pointHp = point * 20;
                tiemNangUse = point * (2 * (this.nPoint.hpg + 1000) + pointHp - 20) / 2;
                if (doUseTiemNang(tiemNangUse)) {
                    this.nPoint.hpMax += point;
                    this.nPoint.hpg += point;
                    Service.gI().point(this);
                }
            } else {
                point = 10;
                tiemNangUse = point * (2 * this.nPoint.dameg + point - 1) / 2 * 100;
                if (doUseTiemNang(tiemNangUse)) {
                    this.nPoint.dameg += point;
                    Service.gI().point(this);
                }
            }
        }
    }

    private boolean doUseTiemNang(long tiemNang) {
        if (this.nPoint.tiemNang < tiemNang) {
            return false;
        } else {
            this.nPoint.tiemNang -= tiemNang;
            return true;
        }
    }

    public void useSkill(int skillId) {
        new Thread(() -> {
            switch (skillId) {
                case Skill.BIEN_KHI:
                    EffectSkillService.gI().sendEffectMonkey(this);
                    EffectSkillService.gI().setIsMonkey(this);
                    EffectSkillService.gI().sendEffectMonkey(this);

                    Service.gI().sendSpeedPlayer(this, 0);
                    Service.gI().Send_Caitrang(this);
                    Service.gI().sendSpeedPlayer(this, -1);
                    PlayerService.gI().sendInfoHpMp(this);
                    Service.gI().point(this);
                    Service.gI().Send_Info_NV(this);
                    Service.gI().sendInfoPlayerEatPea(this);
                    break;
                case Skill.QUA_CAU_KENH_KHI:
                    this.playerSkill.prepareQCKK = !this.playerSkill.prepareQCKK;
                    this.playerSkill.lastTimePrepareQCKK = System.currentTimeMillis();
                    SkillService.gI().sendPlayerPrepareSkill(this, 1000);
                    break;
                case Skill.MAKANKOSAPPO:
                    this.playerSkill.prepareLaze = !this.playerSkill.prepareLaze;
                    this.playerSkill.lastTimePrepareLaze = System.currentTimeMillis();
                    SkillService.gI().sendPlayerPrepareSkill(this, 3000);
                    break;
            }
        }).start();
    }

}
