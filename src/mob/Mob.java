package mob;

/*
 *
 *
 * @author CongHoan
 */
import consts.ConstItem;
import services.InventoryService;
import services.Service;
import services.TaskService;
import services.ItemMapService;
import consts.ConstMap;
import consts.ConstMob;
import consts.ConstTask;

import consts.cn;
import event.EventManager;
import item.Item;
import map.ItemMap;

import java.util.List;

import map.Zone;
import player.Location;
import player.Pet;
import player.Player;
import network.Message;

import java.io.IOException;

import server.Maintenance;
import utils.Util;

import java.util.ArrayList;

import models.Achievement.AchievementService;
import models.Training.TrainingService;
import npc.npc_manifest.TienCap;
import server.ServerNotify;
import services.ItemService;
import services.MapService;
import skill.Skill;

import utils.TimeUtil;

public class Mob {

    public int id;
    public Zone zone;
    public int tempId;
    public String name;
    public byte level;

    public List<Player> temporaryEnemies = new ArrayList<>();

    public MobPoint point;
    public MobEffectSkill effectSkill;
    public Location location;

    public byte pDame;
    public int pTiemNang;
    private long maxTiemNang;

    public long lastTimeDie;
    public int lvMob = 0;
    public int status = 5;
    public int type = 1;

    private long lastTimeAttackPlayer;
    private long timeAttack = 2000;
    public long lastTimePhucHoi = System.currentTimeMillis();
    public long lastTimeSendEffect = System.currentTimeMillis();

    public Mob(Mob mob) {
        this.point = new MobPoint(this);
        this.effectSkill = new MobEffectSkill(this);
        this.location = new Location();
        this.id = mob.id;
        this.tempId = mob.tempId;
        this.level = mob.level;
        this.point.setHpFull(mob.point.getHpFull());
        this.point.sethp((this.point.getHpFull()));
        this.location.x = mob.location.x;
        this.location.y = mob.location.y;
        this.pDame = mob.pDame;
        this.pTiemNang = mob.pTiemNang;
        this.type = mob.type;
        this.setTiemNang();
    }

    private void sendMobAttackMe(Player player, int dame) {
        if (!player.isPet && !player.isNewPet && !player.isBot) {
            Message msg;
            try {// =)) khong biet fix kkk
                msg = new Message(-11);
                msg.writer().writeByte(this.id);
                msg.writer().writeInt(dame); //dame
                player.sendMessage(msg);
                msg.cleanup();
            } catch (Exception e) {
            }
        }
    }

    private void hutItem(Player player, List<ItemMap> items) {
        if (!player.isPet && !player.isNewPet && !player.isBot) {
            if (player.charms.tdThuHut > System.currentTimeMillis()) {
                for (ItemMap item : items) {
                    ItemMapService.gI().pickItem(player, item.itemMapId, true);
                }
            }
            // Mỗi lần nhặt item, tăng tiến độ danh hiệu 1291 (Váy Dop Độ)
            updateTaskDopDo(player);
        } else {
            if (((Pet) player).master.charms.tdThuHut > System.currentTimeMillis()) {
                for (ItemMap item : items) {
                    ItemMapService.gI().pickItem(((Pet) player).master, item.itemMapId, true);
                }
            }
            // Mỗi lần nhặt item, tăng tiến độ danh hiệu 1291 (Váy Dop Độ)
            updateTaskDopDo(((Pet) player).master);
        }
    }

    private void updateTaskDopDo(Player player) {
        if (player != null && player.playerTask != null && player.playerTask.taskdh != null) {
            if (player.playerTask.taskdh.VeChai < 500) {
                player.playerTask.taskdh.VeChai++;
                player.playerTask.taskdh.ResetTime = System.currentTimeMillis();
                int percentDone = (int) ((double) player.playerTask.taskdh.VeChai / 500 * 100);
                // Chỉ thông báo nếu chưa ạt 100%
            }
        }
    }

    public Mob() {
        this.point = new MobPoint(this);
        this.effectSkill = new MobEffectSkill(this);
        this.location = new Location();
    }

    public void setTiemNang() {
        this.maxTiemNang = (int) (long) this.point.getHpFull() * (long) (this.pTiemNang + Util.nextInt(-2, 2)) / 100L;
    }

    public boolean isDie() {
        return this.point.gethp() <= 0;
    }

    public void setDie() {
        this.lastTimePhucHoi = System.currentTimeMillis();
        this.lastTimeDie = System.currentTimeMillis();
    }

    public void addTemporaryEnemies(Player pl) {
        if (pl != null && !temporaryEnemies.contains(pl)) {
            temporaryEnemies.add(pl);
        }
    }

    public void injured(Player plAtt, long damage, boolean dieWhenHpFull) {
        if (this.tempId == ConstMob.MAY_DO_SUC_MANH) {
            if (this.isDie()) {
                return;
            }
            if (plAtt != null
                    && plAtt.charms != null
                    && plAtt.charms.tdManhMe > System.currentTimeMillis()) {
                damage = damage * 2 / 5;
            }
            if (damage <= 0) {
                damage = 1;
            }
            long damageShow = damage;
            long appliedDamage = damageShow;
            if (appliedDamage >= this.point.hp) {
                appliedDamage = this.point.hp;
            }
            this.point.hp -= appliedDamage;
            addTemporaryEnemies(plAtt);
            if (this.isDie()) {
                this.status = 0;
                this.setDie();
                this.temporaryEnemies.clear();
                if (plAtt != null) {
                    this.sendMobDieAffterAttacked(plAtt, (int) damageShow);
                    TaskService.gI().checkDoneTaskKillMob(plAtt, this);
                    TaskService.gI().checkDoneSideTaskKillMob(plAtt, this);
                    TaskService.gI().checkDoneClanTaskKillMob(plAtt, this);
                    AchievementService.gI().checkDoneTaskKillMob(plAtt, this);
                }
            } else {
                this.sendMobStillAliveAffterAttacked(
                        damageShow,
                        plAtt != null && plAtt.nPoint != null && plAtt.nPoint.isCrit
                );
            }
            if (plAtt != null) {
                if (plAtt.isPl() && plAtt.satellite != null && plAtt.satellite.isDefend) {
                    plAtt.satellite.isDefend = false;
                }
                if (plAtt.playerSkill == null
                        || plAtt.playerSkill.skillSelect == null
                        || (plAtt.playerSkill.skillSelect.template.id != Skill.QUA_CAU_KENH_KHI
                        && plAtt.playerSkill.skillSelect.template.id != Skill.TU_SAT)) {

                    long tn = getTiemNangForPlayer(plAtt, damageShow);
                    Service.gI().addSMTN(plAtt, (byte) 2, tn, true);
                    TrainingService.gI().tangTnsmLuyenTap(plAtt, tn);
                }
            }
            return;
        }
        if (!this.isDie()) {
            if (damage >= this.point.hp) {
                damage = this.point.hp;
            }
            if (!dieWhenHpFull) {
                if (this.point.hp == this.point.maxHp && damage >= this.point.hp) {
                    damage = this.point.hp - 1;
                }
                if ((this.tempId == ConstMob.MOC_NHAN || this.tempId == ConstMob.BU_NHIN_MA_QUAI)
                        && damage > this.point.maxHp / 10) {
                    damage = this.point.maxHp / 10;
                }
            }
            if (MapService.gI().isMapKhiGasHuyDiet(this.zone.map.mapId)) {
                boolean mob76Die = true;
                for (Mob mob : this.zone.mobs) {
                    if (!mob.isDie() && mob.tempId == ConstMob.CO_MAY_HUY_DIET) {
                        mob76Die = false;
                        break;
                    }
                }
                if (!mob76Die && plAtt != null && plAtt.playerSkill != null && plAtt.playerSkill.skillSelect != null) {
                    switch (plAtt.playerSkill.skillSelect.template.id) {
                        case Skill.LIEN_HOAN, Skill.ANTOMIC, Skill.MASENKO, Skill.KAMEJOKO ->
                            damage = 1;
                    }
                }
            }
            if (!dieWhenHpFull && !isBigBoss() && !MapService.gI().isMapPhoBan(this.zone.map.mapId) && this.lvMob > 0
                    && plAtt != null && plAtt.charms.tdOaiHung < System.currentTimeMillis()) {
                damage = (int) ((this.point.maxHp <= 20000000 ? this.point.maxHp * 10 : 2000000000) * (10.0 / 100));
                this.mobAttackPlayer(plAtt);
            }
            if (plAtt != null && plAtt.isBoss && this.tempId > 0 && Util.isTrue(1, 2)
                    && Util.canDoWithTime(lastTimeAttackPlayer, 2500)) {
                this.mobAttackPlayer(plAtt);
                lastTimeAttackPlayer = System.currentTimeMillis();
            }
            long prevHp = (long) this.point.hp;
            this.point.hp -= damage;
            addTemporaryEnemies(plAtt);
            if (this.isDie()) {
                this.status = 0;
                this.setDie();
                this.temporaryEnemies.clear();
                if (plAtt != null) {
                    this.sendMobDieAffterAttacked(plAtt, (int) damage);
                    TaskService.gI().checkDoneTaskKillMob(plAtt, this);
                    TaskService.gI().checkSoSuMenhKillMob(plAtt, this);
                    TaskService.gI().checkDoneSideTaskKillMob(plAtt, this);
                    TaskService.gI().checkDoneClanTaskKillMob(plAtt, this);
                    AchievementService.gI().checkDoneTaskKillMob(plAtt, this);
                }
                if (plAtt != null && !plAtt.isPet) {
                    plAtt.tienCapKill++;
                    // TienCap.saveTienCap(plAtt);
                }
                if (this.id == 13) {
                    this.zone.isbulon1Alive = false;
                }
                if (this.id == 14) {
                    this.zone.isbulon2Alive = false;
                }
            } else {
                this.sendMobStillAliveAffterAttacked(damage,
                        plAtt != null ? (plAtt.nPoint != null && plAtt.nPoint.isCrit) : false);
            }
            if (plAtt != null) {
                if (plAtt.isPl() && plAtt.satellite != null && plAtt.satellite.isDefend) {
                    plAtt.satellite.isDefend = false;
                }
                if (plAtt.playerSkill == null
                        || plAtt.playerSkill.skillSelect == null
                        || (plAtt.playerSkill.skillSelect.template.id != Skill.QUA_CAU_KENH_KHI
                        && plAtt.playerSkill.skillSelect.template.id != Skill.TU_SAT)) {
                    Service.gI().addSMTN(plAtt, (byte) 2, getTiemNangForPlayer(plAtt, damage), true);
                    TrainingService.gI().tangTnsmLuyenTap(plAtt, getTiemNangForPlayer(plAtt, damage));
                }
            }
        }
    }

    public long getTiemNangForPlayer(Player pl, long dame) {
        if (pl == null) {
            return 0;
        }

        int levelPlayer = Service.gI().getCurrLevel(pl);
        int n = levelPlayer - this.level;
        boolean isBanDoKhoBau = false;
        if (pl.zone != null && pl.zone.map != null) {
            isBanDoKhoBau = MapService.gI().isMapBanDoKhoBau(pl.zone.map.mapId);
        }
        if (isBanDoKhoBau) {
            n = 0;
        }

        if (pl.nPoint != null && pl.nPoint.power < 40_000_000_000L) {
            n = 0;
        }

        long pDameHit = dame * 100 / point.getHpFull();
        long tiemNang = pDameHit * maxTiemNang / 100;
        if (tiemNang <= 0) {
            tiemNang = 1;
        }
        if (tiemNang >= 1000000000) {
            tiemNang = 999999999;
        }

        if (n >= 0) {
            for (int i = 0; i < n; i++) {
                long sub = tiemNang * 10 / 100;
                if (sub <= 0) {
                    sub = 1;
                }
                tiemNang -= sub;
            }
        } else {
            for (int i = 0; i < -n; i++) {
                long add = tiemNang * 10 / 100;
                if (add <= 0) {
                    add = 1;
                }
                tiemNang += add;
            }
        }

        if (tiemNang <= 0) {
            tiemNang = 1;
        }

        if (pl.nPoint != null) {
            tiemNang = (int) pl.nPoint.calSucManhTiemNang(tiemNang);
        } else {
            return 0;
        }
        switch (this.tempId) {
            case ConstMob.MAY_DO_SUC_MANH -> {
                if (pl.nPoint != null && pl.nPoint.dame > 1000) {
                    tiemNang = 20;
                } else {
                    tiemNang = 1;
                }
            }
        }

        // Kiểm tra null trước khi nhân đôi tiemNang cho map đặc biệt
        if (pl.zone != null && pl.zone.map != null
                && (pl.zone.map.mapId == 122 || pl.zone.map.mapId == 123 || pl.zone.map.mapId == 124)) {
            tiemNang *= 2;
        }

        return tiemNang;
    }

    public void update() {
        if (zone.isGoldenFriezaAlive && TimeUtil.is21H()) {
            if (!isDie()) {
                startDie();
                return;
            }
        }
        if (!this.isDie() && this.tempId == ConstMob.CO_MAY_HUY_DIET && Util.canDoWithTime(lastTimeSendEffect, 1000)) {
            sendEffect(55);
            lastTimeSendEffect = System.currentTimeMillis();
        }

        if (this.isDie() && !Maintenance.isRunning && !isBigBoss()) {
            switch (zone.map.type) {
                case ConstMap.MAP_DOANH_TRAI:
                    if (this.tempId == ConstMob.BULON && this.zone.isTUTAlive
                            && Util.canDoWithTime(lastTimeDie, 10000)) {
                        this.hoiSinh();
                        this.hoiSinhMobPhoBan();
                        if (this.id == 13) {
                            this.zone.isbulon1Alive = true;
                        }
                        if (this.id == 14) {
                            this.zone.isbulon2Alive = true;
                        }
                    }
                    break;
                case ConstMap.MAP_BAN_DO_KHO_BAU:
                    break;
                case ConstMap.MAP_CON_DUONG_RAN_DOC:
                    break;
                case ConstMap.MAP_KHI_GAS_HUY_DIET:
                    break;
                case ConstMap.MAP_TAY_KARIN:
                    break;
                default:
                    if (this.zone.isGoldenFriezaAlive && TimeUtil.is21H()) {
                        return;
                    }
                    if (Util.canDoWithTime(lastTimeDie, 5000)) {
                        this.hoiSinh();
                        this.sendMobHoiSinh();
                    }
                    if (Util.canDoWithTime(lastTimePhucHoi, 30000) && !isDie()) {
                        lastTimePhucHoi = System.currentTimeMillis();
                        long hpMax = this.point.maxHp;
                        if (this.point.hp < hpMax) {
                            hoi_hp(hpMax / 10);
                        } else {
                            this.sendMobHoiSinh();
                        }
                    }
            }
        }

        effectSkill.update();
        attack();
    }

    public boolean isBigBoss() {
        return (this.tempId == ConstMob.HIRUDEGARN
                || this.tempId == ConstMob.VUA_BACH_TUOC
                || this.tempId == ConstMob.ROBOT_BAO_VE
                || this.tempId == ConstMob.GAU_TUONG_CUOP
                || this.tempId == ConstMob.VOI_CHIN_NGA
                || this.tempId == ConstMob.GA_CHIN_CUA
                || this.tempId == ConstMob.NGUA_CHIN_LMAO
                || this.tempId == ConstMob.PIANO
                || this.tempId == ConstMob.KONG
                || this.tempId == ConstMob.GOZILLA);
    }

    public void attack() {
        Player player = getPlayerCanAttack();
        if (!isDie() && !effectSkill.isHaveEffectSkill() && tempId != ConstMob.MOC_NHAN
                && tempId != ConstMob.MAY_DO_SUC_MANH
                && tempId != ConstMob.BU_NHIN_MA_QUAI && tempId != ConstMob.CO_MAY_HUY_DIET && !this.isBigBoss()
                && (this.lvMob < 1 || MapService.gI().isMapPhoBan(this.zone.map.mapId))
                && Util.canDoWithTime(lastTimeAttackPlayer, timeAttack)) {
            if (player != null) {
                this.mobAttackPlayer(player);
            }
            this.lastTimeAttackPlayer = System.currentTimeMillis();
        }
    }

    public Player getPlayerCanAttack() {
        Player plAttack = getFirstPlayerCanAttack();
        if (plAttack != null) {
            return plAttack;
        }
        int distance = 100;
        try {
            List<Player> players = this.zone.getNotBosses();
            for (Player pl : players) {
                if (!pl.isDie() && !pl.isBoss && !pl.isNewPet && (pl.satellite == null || !pl.satellite.isDefend)
                        && (pl.effectSkin == null || !pl.effectSkin.isVoHinh)
                        && (this.tempId > 18 || (this.tempId > 9 && this.type == 4)) || isBigBoss()) {
                    int dis = Util.getDistance(pl, this);
                    if (dis <= distance || isBigBoss()) {
                        plAttack = pl;
                        distance = dis;
                    }
                }
            }
            this.timeAttack = 2000;
        } catch (Exception e) {

        }
        return plAttack;
    }

    private Player getFirstPlayerCanAttack() {
        Player plAtt = null;
        try {
            List<Player> playersMap = zone.getHumanoids();
            int dis = 300;
            if (playersMap != null) {
                for (Player plAttt : playersMap) {
                    if (plAttt.isDie() || plAttt.isBoss || (plAttt.satellite != null && plAttt.satellite.isDefend)
                            || (plAttt.effectSkin != null && plAttt.effectSkin.isVoHinh)
                            || !this.temporaryEnemies.contains(plAttt)) {
                        continue;
                    }
                    int d = Util.getDistance(plAttt, this);
                    if (d <= dis) {
                        dis = d;
                        plAtt = plAttt;
                    }
                }
            }
            this.timeAttack = 1000;
        } catch (Exception e) {

        }
        return plAtt;
    }

    private void mobAttackPlayer(Player player) {
        long dameMob = Util.maxIntValue(this.point.getDameAttack());
        if (player.effectSkill != null && player.effectSkill.isShielding) {
            dameMob = 0; // Nếu đang sử dụng khiên, sát thương là 0
        } else {
            if (player.charms != null && player.charms.tdDaTrau > System.currentTimeMillis()) {
                dameMob /= 2;
            }
            if (player.isPet && ((Pet) player).master.charms != null
                    && ((Pet) player).master.charms.tdDeTu > System.currentTimeMillis()) {
                dameMob /= 2;
            }
            if (this.lvMob > 0 && !MapService.gI().isMapPhoBan(this.zone.map.mapId)) {
                dameMob = (long) (player.nPoint.hpMax * (10.0 / 100));
            }
            if (player.satellite != null && player.satellite.isDefend) {
                dameMob -= dameMob / 5;
            }
            if (player.itemTime != null && player.itemTime.isUseCMS) {
                dameMob = (long) Math.round(dameMob * 0.1);
            }
            if (this.lvMob > 0 && player.charms.tdOaiHung > System.currentTimeMillis()) {
                dameMob = 0;
            }
        }
        long dame = player.injured(null, Util.maxIntValue(dameMob), false, true);

        this.sendMobAttackMe(player, dame);
        this.sendMobAttackPlayer(player);
        this.phanSatThuong(player, dame);
    }

    private void sendMobAttackMe(Player player, long dame) {
        if (!player.isPet && !player.isNewPet) {
            Message msg;
            try {
                msg = new Message(-11);
                msg.writer().writeByte(this.id);
                msg.writeLongByHoandz(Util.maxIntValue(dame), cn.readInt); // dame
                player.sendMessage(msg);
                msg.cleanup();
            } catch (Exception e) {
            }
        }
    }

    private void sendMobAttackPlayer(Player player) {
        Message msg;
        try {
            msg = new Message(-10);
            msg.writer().writeByte(this.id);
            msg.writer().writeInt((int) player.id);
            msg.writeLongByHoandz(Util.maxIntValue(player.nPoint.hp), cn.readInt); // hp
            Service.gI().sendMessAnotherNotMeInMap(player, msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public void hoiSinh() {
        this.status = 5;
        this.point.hp = this.point.maxHp;
        this.setTiemNang();
    }

    public int lvMob() {
        if (this.tempId == ConstMob.MAY_DO_SUC_MANH) {
            this.lvMob = 0;
            this.point.hp = this.point.maxHp;
            return this.lvMob;
        }
        for (Mob mobMap : this.zone.mobs) {
            if (mobMap.lvMob > 0) {
                return 0;
            }
        }
        this.lvMob = this.tempId > 18 && !isBigBoss() ? (Util.isTrue(10, 100) ? 1 : 0) : 0;
        this.point.hp = this.lvMob > 0 ? (this.point.maxHp <= 20_000_000 ? this.point.maxHp * 10 : 2_000_000_000) : this.point.maxHp;
        return this.lvMob;
    }

    public void sendMobHoiSinh() {
        Message msg = null;
        try {
            msg = new Message(-13);
            msg.writer().writeByte(this.id);
            msg.writer().writeByte(this.tempId);
            msg.writer().writeByte(lvMob());
            msg.writeLongByHoandz(Util.maxIntValue(this.point.hp), cn.readInt);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            this.sendMobMaxHp(this.point.hp);
        } catch (Exception e) {
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void hoi_hp(long hp) {
        Message msg = null;
        try {
            this.point.sethp((this.point.gethp() + hp));
            long HP = hp > 0 ? 1 : Math.abs(hp);
            msg = new Message(-9);
            msg.writer().writeByte(this.id);
            msg.writeLongByHoandz(Util.maxIntValue(this.point.gethp()), cn.readInt);
            msg.writeLongByHoandz(Util.maxIntValue(HP), cn.readInt);
            msg.writer().writeBoolean(false);
            msg.writer().writeByte(-1);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
        } catch (Exception e) {
        } finally {
            if (msg != null) {
                msg.cleanup();
                msg = null;
            }
        }
    }

    public void sendEffect(int Effect) {
        Message msg = null;
        try {
            msg = new Message(-9);
            msg.writer().writeByte(this.id);
            msg.writeLongByHoandz(Util.maxIntValue(this.point.gethp()), cn.readInt);
            msg.writeLongByHoandz(Util.maxIntValue(this.point.gethp()), cn.readInt);
            msg.writer().writeBoolean(false);
            msg.writer().writeByte(Effect);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
        } catch (Exception e) {
        } finally {
            if (msg != null) {
                msg.cleanup();
                msg = null;
            }
        }
    }

    private void sendMobDieAffterAttacked(Player plKill, long dameHit) {
        Message msg;
        try {
            msg = new Message(-12);
            msg.writer().writeByte(this.id);
            msg.writeLongByHoandz(Util.maxIntValue(dameHit), cn.readInt);
            msg.writer().writeBoolean(plKill.nPoint.isCrit); // crit
            List<ItemMap> items = mobReward(plKill, this.dropItemTask(plKill), msg);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            msg.cleanup();
            hutItem(plKill, items);
        } catch (Exception e) {
        }
    }

    private List<ItemMap> mobReward(Player player, ItemMap itemTask, Message msg) {
        if (player.isBot) {
            return null;
        }
        List<ItemMap> itemReward = new ArrayList<>();
        try {
            itemReward = this.getItemMobReward(player, this.location.x + Util.nextInt(-10, 10),
                    this.zone.map.yPhysicInTop(this.location.x, this.location.y));
            if (itemTask != null) {
                itemReward.add(itemTask);
            }
            msg.writer().writeByte(itemReward.size()); // sl item roi
            for (ItemMap itemMap : itemReward) {
                msg.writer().writeShort(itemMap.itemMapId);// itemmapid
                msg.writer().writeShort(itemMap.itemTemplate.id); // id item
                msg.writer().writeShort(itemMap.x); // xend item
                msg.writer().writeShort(itemMap.y); // yend item
                msg.writer().writeInt((int) itemMap.playerId); // id nhan vat
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return itemReward;
    }

    public List<ItemMap> getItemMobReward(Player player, int x, int yEnd) {
        List<ItemMap> list = new ArrayList<>();

        if (player.isBoss) {
            return list;
        }

        // if (player.isPl() && Util.isTrue(1, 10000) && this.tempId == 0) {
        // short itTemp = (short) ItemService.gI().randTempItemKichHoat(player.gender);
        // ItemMap it = new ItemMap(zone, itTemp, 1, x, yEnd, player.id);
        // List<Item.ItemOption> ops = ItemService.gI().getListOptionItemShop(itTemp);
        // if (!ops.isEmpty()) {
        // it.options = ops;
        // }
        // it.options.add(new Item.ItemOption(210, 0));
        // it.options.add(new Item.ItemOption(216, 0));
        // it.options.add(new Item.ItemOption(30, 0));
        // list.add(it);
        // }
        if (this.tempId == 0) {
            return list;
        }
        int mapid = player.zone.map.mapId;

        if (EventManager.CHRISTMAS) {
            Player pl = player;
            if (pl.isPet) {
                pl = ((Pet) pl).master;
            }
            if (pl.isPet) {
                pl = ((Pet) pl).master;
            }
            if (Util.isTrue(1, 100)) {
                if (pl.itemEvent != null && pl.itemEvent.canDropTatVoGiangSinh(100)) {
                    list.add(new ItemMap(zone, 649, 1, x, yEnd, player.id));
                }
            }
        }
        if (mapid == 5 || mapid == 13) {
            Player pl = player;
            if (pl.isPet) {
                pl = ((Pet) pl).master;
            }
            if (pl.isPet) {
                pl = ((Pet) pl).master;
            }
            if (Util.isTrue(1, 500)) {
                if (pl.itemEvent != null && pl.itemEvent.canDropBinhNuoc(100)) {
                    list.add(new ItemMap(zone, 456, 1, x, yEnd, pl.id));
                }
            }
        }
        if (MapService.gI().isMapCadic(this.zone.map.mapId) && player != null) {
            // tỉ lệ 1/555 mỗi lần up quái
            if (Util.isTrue(1, 100)) {
                Item item = InventoryService.gI().findItemBag(player, 1951);
                if (item != null) {
                    Item.ItemOption kilisOption = item.getOptionById(255);
                    if (kilisOption != null) {
                        kilisOption.param += 1;
                    } else {
                        item.itemOptions.add(new Item.ItemOption(255, 1));
                    }
                    InventoryService.gI().sendItemBag(player);
                }
            }
        }
        if (MapService.gI().isMapRungCay(this.zone.map.mapId) && player != null) {
            // tỉ lệ 1/555 mỗi lần up quái
            if (Util.isTrue(1, 200)) {
                Item item = InventoryService.gI().findItemBag(player, 1956);
                if (item != null) {
                    Item.ItemOption killMobOption = item.getOptionById(191);
                    if (killMobOption != null) {
                        killMobOption.param += 1;
                    } else {
                        item.itemOptions.add(new Item.ItemOption(191, 1));
                    }
                    InventoryService.gI().sendItemBag(player);
                }
            }
        }

        if (EventManager.INTERNATIONAL_WOMANS_DAY) {
            Player pl = player;
            if (pl.isPet) {
                pl = ((Pet) pl).master;
            }
            if (pl.isPet) {
                pl = ((Pet) pl).master;
            }
            if (Util.isTrue(1, 50)) {
                if (pl.itemEvent != null && pl.itemEvent.canDropHoaHong(100)) {
                    list.add(new ItemMap(zone, 610, 1, x, yEnd, player.id));
                }
            }
        }
        if (Util.isTrue(10, 800)) {
            list.add(new ItemMap(zone, 18, 1, x, yEnd, player.id));
        }
        if (Util.isTrue(10, 700)) {
            list.add(new ItemMap(zone, 19, 1, x, yEnd, player.id));
        }
        if (Util.isTrue(10, 700)) {
            list.add(new ItemMap(zone, 20, 1, x, yEnd, player.id));
        }
        
//        if (Util.isTrue(1, 400)) {
//            ItemMap it = new ItemMap(zone, 1530, 1, x, yEnd, player.id);
//            it.options.add(new Item.ItemOption(30, 0));
//            list.add(it);
//
//        }
        if (EventManager.HALLOWEEN) {
            if (MapService.gI().isMapEventHalloween(mapid)) {
                if (Util.isTrue(1, 50)) {
                    list.add(new ItemMap(zone, 707, 1, x, yEnd, player.id));
                } else if (Util.isTrue(1, 50)) {
                    list.add(new ItemMap(zone, 708, 1, x, yEnd, player.id));
                }
            }
        }

        if (player.itemTime.isUseMayDo && (Util.isTrue(15, 200))
                && this.tempId > 57 && this.tempId < 66) {
            list.add(new ItemMap(zone, 380, 1, x, yEnd, player.id));
        }
        if (player.itemTime.isUseMayDo2 && Util.isTrue(15, 100) && this.tempId > 80 && this.tempId < 81) {
            list.add(new ItemMap(zone, 1110, 1, x, yEnd, player.id));
        }

        if (player.isPl() && TaskService.gI().getIdTask(player) == ConstTask.TASK_8_1) {
            if (player.gender == 0 && this.tempId == 11 || player.gender == 1 && this.tempId == 12
                    || player.gender == 2 && this.tempId == 10) {
                list.add(new ItemMap(zone, 20, 1, x, yEnd, player.id));
            }
        }
        if (Util.isTrue(1, 190)) {
            ItemMap it = new ItemMap(zone, ConstItem.THIT_HEO, 1, x, yEnd, player.id);
            it.options.add(new Item.ItemOption(86, 0));
            list.add(it);
        }
        if (Util.isTrue(1, 190)) {
            ItemMap it = new ItemMap(zone, ConstItem.THUNG_NEP, 1, x, yEnd, player.id);
            it.options.add(new Item.ItemOption(86, 0));
            list.add(it);
        }
        if (Util.isTrue(1, 190)) {
            ItemMap it = new ItemMap(zone, 750, 1, x, yEnd, player.id);
            it.options.add(new Item.ItemOption(86, 0));
            list.add(it);
        }
        if (Util.isTrue(1, 190) && this.tempId != 0) {
            ItemMap it = new ItemMap(zone, ConstItem.LA_DONG, 1, x, yEnd, player.id);
            it.options.add(new Item.ItemOption(86, 0));
            list.add(it);
        }
        if (Util.isTrue(1, 190) && MapService.gI().isMapPhoBan(mapid)) {
            ItemMap it = new ItemMap(zone, ConstItem.DAU_XANH, 1, x, yEnd, player.id);
            it.options.add(new Item.ItemOption(86, 0));
            list.add(it);
        }
        if (Util.isTrue(1, 300)) {
            ItemMap it = new ItemMap(zone, ConstItem.TRUNG_VIT_MUOI, 1, x, yEnd, player.id);
            it.options.add(new Item.ItemOption(86, 0));
            list.add(it);
        }

        if (MapService.gI().isMapPhoBan(mapid) && this.tempId != 22) {
            // if (Util.isTrue(10, 100) || (player.actived() && Util.isTrue(20, 50))) {
            // list.add(new ItemMap(zone, 2029, 1, x, yEnd, player.id));
            // }
            // if (Util.isTrue(50, 100) || (player.actived() && Util.isTrue(10, 50))) {
            // list.add(new ItemMap(zone, 2055, 1, x, yEnd, player.id));
            // }
        }
//        if (MapService.gI().isMapNguHanhSon(mapid) && this.tempId != 22) {
//            if (Util.isTrue(20, 100) || (player.actived() && Util.isTrue(50, 100))) {
//                player.event.addEventPointNHS(1);
//            }
//        }
//        if (MapService.gI().isMapDoanhTrai(mapid)) {
//            if (Util.isTrue(100, 100)) {
//                if (Util.isTrue(100, 100)) {
//                    ItemMap it = new ItemMap(zone, 457, 100, x, yEnd, player.id);
//                    list.add(it);
//                    plAtt.playerTask.kolTask.addCount();
//                }
//            }
//        }
        if (MapService.gI().isMapNguHanhSon(mapid)) {
            if (Util.isTrue(1, 80)) { //1/160
                ItemMap it = new ItemMap(zone, 457, Util.nextInt(1, 3), x, yEnd, player.id);
                list.add(it);
            }
        }
        if (Util.isTrue(1, 150)) { //1/250
            ItemMap it1612 = new ItemMap(zone, 457, Util.nextInt(1, 3), x, yEnd, player.id);
            list.add(it1612);
        }

        if (MapService.gI().isMapUpPorata(mapid)) {
            if (Util.isTrue(1, 60)) {
                ItemMap it = new ItemMap(zone, 934, Util.nextInt(1, 3), x, yEnd, player.id);
                it.options.add(new Item.ItemOption(30, 0));
                list.add(it);
            } else if (Util.isTrue(1, 240)) {
                ItemMap it = new ItemMap(zone, 935, Util.nextInt(1, 3), x, yEnd, player.id);
                it.options.add(new Item.ItemOption(30, 0));
                list.add(it);
            } else if (Util.isTrue(1, 25)) {
                ItemMap it = new ItemMap(zone, 933, 1, x, yEnd, player.id);
                it.options.add(new Item.ItemOption(31, Util.nextInt(1, 10)));
                it.options.add(new Item.ItemOption(30, 0));
                list.add(it);
            }
        }

        if (MapService.gI().isMapHanhTinhNgucTu(mapid)) {
            if (Util.isTrue(1, 50)) { //1/150
                ItemMap it = new ItemMap(zone, 1285, Util.nextInt(1, 3), x, yEnd, player.id);
                it.options.add(new Item.ItemOption(30, 0));
                list.add(it);
            } else if (Util.isTrue(1, 150)) { //1/250
                ItemMap it = new ItemMap(zone, 1282, Util.nextInt(1, 3), x, yEnd, player.id);
                it.options.add(new Item.ItemOption(30, 0));
                list.add(it);
            } else if (Util.isTrue(1, 20)) { // 1/40
                ItemMap it = new ItemMap(zone, 1281, 1, x, yEnd, player.id);
                it.options.add(new Item.ItemOption(30, 0));
                list.add(it);
            }
        }

        // Vang roi
//        if (Util.isTrue(1, 1000) || (Manager.TEST && Util.isTrue(1, 10)) || (player.actived() && Util.isTrue(1, 10))) {
//
//            ItemMap it = new ItemMap(zone, 457, 1, x, yEnd, player.id);
//
//            list.add(it);
//        }
//         Da ngu sac
        if (mapid >= 80 && mapid <= 80 && Util.isTrue(1, 200)) {
            list.add(new ItemMap(zone, 674, 1, x, yEnd, player.id));
        }
//        // Set kich hoat
//        if (((Util.isTrue(1, 100)))
//                && MapService.gI().isMapUpSKH(mapid)) {
//            short itTemp = (short) ItemService.gI().randTempItemKichHoat(player.gender);
//            ItemMap it = new ItemMap(zone, itTemp, 1, x, yEnd, player.id);
//            List<Item.ItemOption> ops = ItemService.gI().getListOptionItemShop(itTemp);
//            if (!ops.isEmpty()) {
//                it.options = ops;
//            }
//
//            int[] opsrand = ItemService.gI().randOptionItemKichHoat(player.gender);
//            it.options.add(new Item.ItemOption(opsrand[0], 0));
//            it.options.add(new Item.ItemOption(opsrand[1], 0));
//
//            it.options.add(new Item.ItemOption(30, 0));
//            list.add(it);
//        }
//
//        // Set kich hoat Vip
//        if (((Util.isTrue(1, 1000)) || (Manager.TEST && Util.isTrue(1, 1000)) || Util.isTrue(1, 1000))
//                && MapService.gI().isMapUpSKH(mapid)) {
//            short itTemp = (short) ItemService.gI().randTempItemKichHoat(player.gender);
//            ItemMap it = new ItemMap(zone, itTemp, 1, x, yEnd, player.id);
//            List<Item.ItemOption> ops = ItemService.gI().getListOptionItemShop(itTemp);
//            if (!ops.isEmpty()) {
//                it.options = ops;
//            }
//
//            int[] opsrand = ItemService.gI().randOptionItemKichHoatNew(player.gender);
//            it.options.add(new Item.ItemOption(opsrand[0], 0));
//            it.options.add(new Item.ItemOption(opsrand[1], 0));
//            it.options.add(new Item.ItemOption(opsrand[2], 0));
//            it.options.add(new Item.ItemOption(opsrand[3], 0));
//            it.options.add(new Item.ItemOption(30, 0));
//            list.add(it);
//        }

        // Sao pha le
        if (Util.isTrue(1, 350) || (player.nPoint.isDoSPL && Util.isTrue(3, 400))) {
            int rand = Util.nextInt(0, 6);
            ItemMap it = new ItemMap(zone, 441 + rand, 1, x, yEnd, player.id);
            it.options.add(new Item.ItemOption(95 + rand, (rand == 3 || rand == 4) ? 3 : 5));
            list.add(it);
        }

         // khúc mía
        if (Util.isTrue(5, 200)) {
            ItemMap it1612 = new ItemMap(zone, 1612, 1, x, yEnd, player.id);
            list.add(it1612);
        }
        
        if (Util.isTrue(1, 220)) {
            ItemMap it447 = new ItemMap(zone, 447, 1, x, yEnd, player.id);
//            it447.options.add(new Item.ItemOption(95,5));
            list.add(it447);
        }

        // Da nang cap
        if (Util.isTrue(1, 250) || (Util.isTrue(1, 200) && MapService.gI().isMapTuongLai(mapid))
                || (player.actived() && Util.isTrue(1, 190))) {
            int rand = Util.nextInt(0, 4);
            ItemMap it = new ItemMap(zone, 220 + rand, 1, x, yEnd, player.id);
            it.options.add(new Item.ItemOption(71 - rand, 0));
            list.add(it);
        }

        if (MapService.gI().isMapCold(mapid)) {
            if (player.isPet) {
                player = ((Pet) player).master;
            }
            // if (player.isPet) {
            // player = ((Pet) player).master;
            // }
            if (Util.isTrue(1, 60000)) {
                ItemMap it = ItemService.gI().randDoTL(this.zone, 1, x, yEnd, player.id);
                list.add(it);
                ServerNotify.gI().notify(player.name + " vừa nhặt được " + it.itemTemplate.name + " tại "
                        + this.zone.map.mapName + " khu " + this.zone.zoneId);
            }
            if (Util.isTrue(1, 110) && InventoryService.gI().fullSetThan(player)) {
                ItemMap it = new ItemMap(zone, Util.nextInt(663, 667), 1, x, yEnd, player.id);
                list.add(it);
            }
            if (Util.isTrue(1, 400)) {
                ItemMap it = new ItemMap(zone, 1613, 1, x, yEnd, player.id);
                list.add(it);
            }
        }

//        if (MapService.gI().isMapTuongLai(mapid)
//                && ((Util.isTrue(1, 1000) || (player.actived() && Util.isTrue(1, 300))))
//                && InventoryService.gI().fullSetThan(player)) {
//            ItemMap it = new ItemMap(zone, Util.nextInt(663, 667), 1, x, yEnd, player.id);
//            it.options.add(new Item.ItemOption(30, 0));
//            list.add(it);
//        }
        // if (Util.isTrue(1, 100000) || (player.actived() && Util.isTrue(1, 10000))) {
        // list.add(new ItemMap(zone, 457, 1, x, yEnd, player.id));
        // }
        // Manh thien su
        //if ((Util.isTrue(1, 250) || (player.actived() && Util.isTrue(1, 180))) 
        if ((Util.isTrue(1, 150) || (player.actived() && Util.isTrue(1, 120))) 
                && MapService.gI().isMapHanhTinhThucVat(mapid) && InventoryService.gI().findItemNTK(player)) {
            list.add(new ItemMap(zone, Util.nextInt(1066, 1070), 1, x, yEnd, player.id));
        }
        if (Util.isTrue(1, 200) || (player.actived() && Util.isTrue(1, 100))) {
            list.add(new ItemMap(zone, 861, 1, x, yEnd, player.id));
        }
        // if (player.nPoint.power >= 80000000000L) {
        // if (player.zone.map.mapId == 155) {
        // list.add(new ItemMap(zone, 2055, 1, x, yEnd, player.id));
        // } else {
        // if (Util.isTrue(1, 500) || (player.actived() && Util.isTrue(1, 100))) {
        // list.add(new ItemMap(zone, 2051, 1, x, yEnd, player.id));
        // }
        // if (Util.isTrue(1, 1000) || (player.actived() && Util.isTrue(1, 200))) {
        // list.add(new ItemMap(zone, 2052, 1, x, yEnd, player.id));
        // }
        // }
        // }
        return list;
    }

    private ItemMap dropItemTask(Player player) {
        ItemMap itemMap = null;
        switch (tempId) {
            case ConstMob.KHUNG_LONG:
            case ConstMob.LON_LOI:
            case ConstMob.QUY_DAT:
                if (TaskService.gI().getIdTask(player) == ConstTask.TASK_2_0) {
                    itemMap = new ItemMap(zone, 73, 1, location.x, location.y, player.id);
                }
                break;
            case ConstMob.THAN_LAN_ME:
                if (TaskService.gI().getIdTask(player) == ConstTask.TASK_8_1) {
                    if (Util.isTrue(1, 3)) {
                        itemMap = new ItemMap(zone, 20, 1, location.x, location.y, player.id);
                    } else {
                        Service.gI().sendThongBao(player,
                                "Con thằn lằn mẹ này không giữ ngọc, hãy tìm con thằn lằn mẹ khác");
                    }
                }
            case ConstMob.OC_MUON_HON:
                if (TaskService.gI().getIdTask(player) == ConstTask.TASK_14_1) {
                    if (Util.isTrue(1, 3)) {
                        itemMap = new ItemMap(zone, 85, 1, location.x, location.y, player.id);
                    } else {
                        Service.gI().sendThongBao(player,
                                "Con ốc mượn hồn này không giữ truyện tranh, hãy thử tìm con ốc mượn hồn khác");
                    }
                }
            case ConstMob.HEO_XAYDA_ME:
                if (TaskService.gI().getIdTask(player) == ConstTask.TASK_14_1) {
                    if (Util.isTrue(1, 3)) {
                        itemMap = new ItemMap(zone, 85, 1, location.x, location.y, player.id);
                    } else {
                        Service.gI().sendThongBao(player,
                                "Con heo xayda mẹ này không giữ truyện tranh, hãy thử tìm con heo xayda mẹ khác");
                    }
                }
            case ConstMob.OC_SEN:
                if (TaskService.gI().getIdTask(player) == ConstTask.TASK_14_1) {
                    if (Util.isTrue(1, 3)) {
                        itemMap = new ItemMap(zone, 85, 1, location.x, location.y, player.id);
                    } else {
                        Service.gI().sendThongBao(player,
                                "Con ốc xên này không giữ truyện tranh, hãy thử tìm con ốc xên khác");
                    }
                }
        }
        if (itemMap != null) {
            return itemMap;
        }
        return null;
    }

    private void sendMobStillAliveAffterAttacked(long dameHit, boolean crit) {
        Message msg;
        try {
            msg = new Message(-9);
            msg.writer().writeByte(this.id);
            msg.writeLongByHoandz(Util.maxIntValue(this.point.gethp()), cn.readInt);
            msg.writeLongByHoandz(Util.maxIntValue(dameHit), cn.readInt);
            msg.writer().writeBoolean(crit); // chí mạng
            msg.writer().writeInt(-1);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public void hoiSinhMobPhoBan() {
        this.point.hp = this.point.maxHp;
        this.setTiemNang();
        Message msg;
        try {
            msg = new Message(-13);
            msg.writer().writeByte(this.id);
            msg.writer().writeByte(this.tempId);
            msg.writer().writeByte(this.lvMob); // level mob
            msg.writeLongByHoandz(Util.maxIntValue(this.point.hp), cn.readInt);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public void hoiSinhMobTayKarin() {
        this.point.hp = this.point.maxHp;
        this.maxTiemNang = 1;
        Message msg;
        try {
            msg = new Message(-13);
            msg.writer().writeByte(this.id);
            msg.writer().writeByte(this.tempId);
            msg.writer().writeByte(this.lvMob); // level mob
            msg.writeLongByHoandz(Util.maxIntValue(this.point.hp), cn.readInt);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    public void sendSieuQuai(int type) {
        Message msg;
        try {
            msg = new Message(-75);
            msg.writer().writeByte(this.id);
            msg.writer().writeByte(type);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    public void sendDisable(boolean bool) {
        Message msg;
        try {
            msg = new Message(81);
            msg.writer().writeByte(this.id);
            msg.writer().writeBoolean(bool);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    public void sendDoneMove(boolean bool) {
        Message msg;
        try {
            msg = new Message(82);
            msg.writer().writeByte(this.id);
            msg.writer().writeBoolean(bool);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    public void sendFire(boolean bool) {
        Message msg;
        try {
            msg = new Message(85);
            msg.writer().writeByte(this.id);
            msg.writer().writeBoolean(bool);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    public void sendIce(boolean bool) {
        Message msg;
        try {
            msg = new Message(86);
            msg.writer().writeByte(this.id);
            msg.writer().writeBoolean(bool);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    public void sendWind(boolean bool) {
        Message msg;
        try {
            msg = new Message(87);
            msg.writer().writeByte(this.id);
            msg.writer().writeBoolean(bool);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    public void sendMobMaxHp(long maxHp) {
        Message msg;
        try {
            msg = new Message(87);
            msg.writer().writeByte(this.id);
            msg.writeLongByHoandz(Util.maxIntValue(maxHp), cn.readInt);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    private void phanSatThuong(Player plTarget, long dame) {
        if (plTarget.nPoint == null) {
            return;
        }
        int percentPST = plTarget.nPoint.tlPST;
        if (percentPST != 0) {
            long damePST = Util.maxIntValue(dame * percentPST / 100L);
            Message msg;
            try {
                msg = new Message(-9);
                msg.writer().writeByte(this.id);
                if (damePST >= this.point.hp) {
                    damePST = this.point.hp - 1;
                }
                long hpMob = Util.maxIntValue(this.point.hp);
                injured(null, damePST, true);
                damePST = hpMob - this.point.hp;
                msg.writeLongByHoandz(Util.maxIntValue(this.point.hp), cn.readInt);
                msg.writeLongByHoandz(Util.maxIntValue(damePST), cn.readInt);
                msg.writer().writeBoolean(false);
                msg.writer().writeByte(36);
                Service.gI().sendMessAllPlayerInMap(this.zone, msg);
                msg.cleanup();
            } catch (IOException e) {
            }
        }
    }

    public void startDie() {
        Message msg;
        try {
            setDie();
            this.point.hp = -1;
            this.status = 0;
            msg = new Message(-12);
            msg.writer().writeByte(this.id);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }
}
