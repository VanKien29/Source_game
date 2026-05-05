package player.Bot;

import boss.Boss;
import boss.BossManager;
import consts.ConstNpc;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import map.ItemMap;
import map.Zone;
import mob.Mob;
import npc.Npc;
import player.Pet;
import player.Player;
import services.ItemMapService;
import services.MapService;
import services.PlayerService;
import services.SkillService;
import services.TaskService;
import services.func.ChangeMapService;
import skill.Skill;
import task.SubTaskMain;
import task.TaskMain;
import utils.Util;

public class SmartBotAI {

    private static final int PARTY_SIZE = 4;
    private static final int KAME_ISLAND_MAP = 5;
    private static final int MAX_KAME_POPULATION = 35;
    private static final int MAX_NORMAL_MAP_POPULATION = 24;
    private static final int MAX_HOT_FUTURE_MAP_POPULATION = 8;
    private static final int MIN_ZONE_POPULATION_SPREAD = 2;
    private static final int MAX_ZONE_POPULATION_SPREAD = 4;
    private static final int[] VILLAGE_AND_EARLY_MAPS = {
        0, 1, 2, 3, 4, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20,
        21, 22, 23, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 42, 43, 44, 52
    };
    private static final int[] MID_TRAIN_MAPS = {
        24, 25, 26, 45, 46, 47, 48, 49, 50, 51, 63, 64, 65, 66, 67, 68, 69, 70,
        71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83
    };
    private static final int[] LATE_TRAIN_MAPS = {
        92, 93, 94, 96, 97, 98, 99, 100, 102, 103, 104, 105, 106, 107, 108, 109,
        110, 111, 122, 123, 124, 126, 131, 132, 133, 139, 140, 145, 153, 155, 156,
        157, 158, 159, 160, 161, 162, 163, 164, 165, 166, 167, 168, 169, 170, 171,
        172, 176, 177, 178, 179, 180, 181, 182, 183
    };
    private static final int[] PET_TRAIN_MAPS = {
        1, 2, 3, 4, 6, 8, 9, 10, 11, 12, 13, 15, 16, 17, 18, 19, 20,
        27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 42, 43, 44, 45, 46,
        47, 48, 49, 50, 51, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73,
        74, 75, 76, 77, 78, 79, 80, 81, 82, 83
    };
    private static final int[] FALLBACK_MAPS = {0, 1, 2, 7, 8, 9, 14, 15, 16, 21, 22, 23, 24, 25, 26};
    private static SmartBotAI instance;

    public static SmartBotAI gI() {
        if (instance == null) {
            instance = new SmartBotAI();
        }
        return instance;
    }

    public void update(Bot bot) {
        if (bot == null || bot.beforeDispose) {
            return;
        }
        long now = System.currentTimeMillis();
        if (now < bot.smartNextThinkAt) {
            return;
        }
        bot.smartNextThinkAt = now + Util.nextInt(350, 900);
        if (bot.zone == null) {
            moveToTrainMap(bot, now);
            return;
        }
        if (isKameSocialMap(bot)) {
            idleOnKame(bot);
            return;
        }
        pickNearbyItem(bot);
        tryAutoNpcTask(bot);
        if (bot.isDie()) {
            return;
        }
        if (shouldHuntBoss(bot, now) && huntBoss(bot)) {
            return;
        }
        trainMob(bot, now);
    }

    public void updateParties(List<Bot> source) {
        List<Bot> smartBots = new ArrayList<>();
        for (Bot bot : new ArrayList<>(source)) {
            if (bot != null && bot.isSmartBot()) {
                smartBots.add(bot);
            }
        }
        smartBots.sort(Comparator.comparingInt(this::powerBracket).thenComparingLong(bot -> bot.id));
        int partyId = 1;
        int index = 0;
        while (index < smartBots.size()) {
            int bracket = powerBracket(smartBots.get(index));
            List<Bot> group = new ArrayList<>();
            while (index < smartBots.size() && powerBracket(smartBots.get(index)) == bracket && group.size() < PARTY_SIZE) {
                group.add(smartBots.get(index++));
            }
            if (!group.isEmpty()) {
                Bot leader = group.get(0);
                for (Bot bot : group) {
                    bot.smartPartyId = partyId;
                    bot.smartLeaderId = leader.id;
                }
                partyId++;
            }
        }
    }

    private int powerBracket(Bot bot) {
        long power = bot.nPoint == null ? 0 : bot.nPoint.power;
        if (power < 20_000_000L) {
            return 0;
        }
        if (power < 100_000_000L) {
            return 1;
        }
        if (power < 1_000_000_000L) {
            return 2;
        }
        return 3;
    }

    private boolean shouldHuntBoss(Bot bot, long now) {
        if (!isPartyLeader(bot) || bot.zone == null || bot.zone.map == null
                || bot.nPoint == null || bot.nPoint.power < 100_000_000L) {
            return false;
        }
        if (now < bot.smartNextBossAt) {
            return false;
        }
        bot.smartNextBossAt = now + Util.nextInt(240_000, 480_000);
        return Util.isTrue(3, 100);
    }

    private boolean huntBoss(Bot bot) {
        Boss target = chooseBoss(bot);
        if (target == null || target.zone == null || target.zone.map == null) {
            return false;
        }
        if (bot.zone == null || !bot.zone.equals(target.zone)) {
            return false;
        }
        selectCombatSkill(bot, true);
        if (bot.playerSkill.skillSelect == null || !bot.UseLastTimeSkill()) {
            return true;
        }
        int moveX = clampMapX(bot.zone, target.location.x + Util.nextInt(-45, 45));
        PlayerService.gI().playerMove(bot, moveX, target.location.y);
        SkillService.gI().useSkill(bot, target, null, -1, null);
        return true;
    }

    private Boss chooseBoss(Bot bot) {
        List<Boss> candidates = new ArrayList<>();
        for (Boss boss : BossManager.gI().getBosses()) {
            if (boss == null || boss.zone == null || boss.zone.map == null || boss.location == null
                    || boss.isDie() || !boss.zone.equals(bot.zone) || isBlockedBossMap(boss.zone.map.mapId)) {
                continue;
            }
            if (safeGetMapCanJoin(bot, boss.zone.map.mapId, boss.zone.zoneId) != null) {
                candidates.add(boss);
            }
        }
        if (candidates.isEmpty()) {
            return null;
        }
        return candidates.get(Util.nextInt(0, candidates.size() - 1));
    }

    private boolean isBlockedBossMap(int mapId) {
        return MapService.gI().isMapDoanhTrai(mapId)
                || MapService.gI().isMapBanDoKhoBau(mapId)
                || MapService.gI().isMapKhiGasHuyDiet(mapId)
                || MapService.gI().isMapConDuongRanDoc(mapId)
                || MapService.gI().isMapBlackBallWar(mapId)
                || MapService.gI().isMapClanNamekWar(mapId)
                || MapService.gI().isMapMaBu(mapId)
                || MapService.gI().isMapMabu2H(mapId)
                || MapService.gI().isMapSieuThanhThuy(mapId);
    }

    private void trainMob(Bot bot, long now) {
        if (moveToTrainMap(bot, now)) {
            return;
        }
        if (bot.smartMode == Bot.SMART_MODE_PET_TRAIN && bot.pet != null
                && bot.fusion.typeFusion == 0) {
            trainPet(bot, now);
            return;
        }
        Mob mob = chooseMob(bot);
        if (mob == null) {
            bot.smartNextMapAt = Math.max(bot.smartNextMapAt, now + Util.nextInt(180_000, 360_000));
            roamAround(bot, now);
            return;
        }
        selectCombatSkill(bot, false);
        if (bot.playerSkill.skillSelect == null || !bot.UseLastTimeSkill()) {
            roamAround(bot, now);
            return;
        }
        int moveX = clampMapX(bot.zone, mob.location.x + Util.nextInt(-35, 35));
        PlayerService.gI().playerMove(bot, moveX, mob.location.y);
        SkillService.gI().useSkill(bot, null, mob, -1, null);
    }

    private boolean moveToTrainMap(Bot bot, long now) {
        int targetMap = chooseTrainMap(bot, now);
        if (targetMap < 0) {
            return false;
        }
        if (bot.zone != null && bot.zone.map != null && bot.zone.map.mapId == targetMap) {
            return false;
        }
        Zone zone = getBalancedZone(bot, targetMap);
        if (zone == null) {
            return false;
        }
        int x = randomMapX(zone);
        int y = zone.map.yPhysicInTop(x, 100);
        moveToZone(bot, zone, x, y);
        return true;
    }

    private int chooseTrainMap(Bot bot, long now) {
        if (bot.smartMode == Bot.SMART_MODE_SOCIAL && canUseTrainMap(bot, KAME_ISLAND_MAP)) {
            bot.smartPreferredMapId = KAME_ISLAND_MAP;
            bot.smartNextMapAt = now + Util.nextInt(1_800_000, 3_600_000);
            return KAME_ISLAND_MAP;
        }
        Bot leader = getLeader(bot);
        if (leader != null && leader != bot && leader.zone != null && leader.zone.map != null
                && now >= bot.smartNextMapAt && Util.isTrue(20, 100)
                && canUseTrainMap(bot, leader.zone.map.mapId)) {
            return leader.zone.map.mapId;
        }
        int taskMap = getCurrentTaskMap(bot);
        if (taskMap >= 0 && bot.smartMode == Bot.SMART_MODE_FARM && Util.isTrue(2, 100)
                && !isHotFutureMap(taskMap) && canUseTrainMap(bot, taskMap)) {
            bot.smartPreferredMapId = taskMap;
            bot.smartNextMapAt = now + Util.nextInt(420_000, 720_000);
            return taskMap;
        }
        if (bot.smartPreferredMapId >= 0 && now < bot.smartNextMapAt
                && canUseTrainMap(bot, bot.smartPreferredMapId)) {
            return bot.smartPreferredMapId;
        }
        int mapId = chooseWeightedTrainMap(bot);
        if (mapId >= 0) {
            bot.smartPreferredMapId = mapId;
            bot.smartNextMapAt = now + Util.nextInt(480_000, 900_000);
            return mapId;
        }
        return bot.zone != null && bot.zone.map != null ? bot.zone.map.mapId : -1;
    }

    private int chooseWeightedTrainMap(Bot bot) {
        if (bot.smartMode == Bot.SMART_MODE_PET_TRAIN) {
            int petMap = randomValidMap(bot, PET_TRAIN_MAPS);
            if (petMap >= 0) {
                return petMap;
            }
        }
        for (int i = 0; i < 12; i++) {
            int roll = Util.nextInt(1, 100);
            int mapId;
            if (roll <= 35) {
                mapId = randomValidMap(bot, VILLAGE_AND_EARLY_MAPS);
            } else if (roll <= 70) {
                mapId = randomValidMap(bot, MID_TRAIN_MAPS);
            } else {
                mapId = randomValidMap(bot, LATE_TRAIN_MAPS);
            }
            if (mapId >= 0) {
                return mapId;
            }
        }
        return randomValidMap(bot, FALLBACK_MAPS);
    }

    private int randomValidMap(Bot bot, int[] mapIds) {
        if (mapIds == null || mapIds.length == 0) {
            return -1;
        }
        int bestMap = -1;
        int bestPopulation = Integer.MAX_VALUE;
        int attempts = Math.max(16, mapIds.length);
        for (int i = 0; i < attempts; i++) {
            int mapId = mapIds[Util.nextInt(0, mapIds.length - 1)];
            if (canUseTrainMap(bot, mapId)) {
                int population = getMapPopulation(mapId);
                if (population < bestPopulation || (population == bestPopulation && Util.isTrue(1, 2))) {
                    bestMap = mapId;
                    bestPopulation = population;
                }
            }
        }
        return bestMap;
    }

    private boolean canUseTrainMap(Bot bot, int mapId) {
        int population = getMapPopulation(mapId);
        if (mapId == KAME_ISLAND_MAP) {
            return population < MAX_KAME_POPULATION && getBalancedZone(bot, mapId) != null;
        }
        if (isHotFutureMap(mapId) && population >= MAX_HOT_FUTURE_MAP_POPULATION) {
            return false;
        }
        if (population >= MAX_NORMAL_MAP_POPULATION) {
            return false;
        }
        return getBalancedZone(bot, mapId) != null;
    }

    private int getCurrentTaskMap(Bot bot) {
        TaskMain task = bot.playerTask == null ? null : bot.playerTask.taskMain;
        if (task == null || task.subTasks == null || task.subTasks.isEmpty()
                || task.index < 0 || task.index >= task.subTasks.size()) {
            return -1;
        }
        SubTaskMain subTask = task.subTasks.get(task.index);
        return subTask.mapId;
    }

    private void tryAutoNpcTask(Bot bot) {
        TaskMain task = bot.playerTask == null ? null : bot.playerTask.taskMain;
        if (task == null || task.subTasks == null || task.subTasks.isEmpty()
                || task.index < 0 || task.index >= task.subTasks.size()) {
            return;
        }
        SubTaskMain subTask = task.subTasks.get(task.index);
        if (subTask.npcId == -1 || bot.zone == null || bot.zone.map == null || subTask.mapId != bot.zone.map.mapId) {
            return;
        }
        long now = System.currentTimeMillis();
        if (task.lastTime != 0 && now - task.lastTime < 4_000) {
            return;
        }
        task.lastTime = now;
        TaskService.gI().doneTask(bot, TaskService.gI().getIdTask(bot));
    }

    private Mob chooseMob(Bot bot) {
        if (bot.zone == null || bot.zone.mobs == null || bot.zone.mobs.isEmpty()) {
            return null;
        }
        for (Mob mob : bot.zone.mobs) {
            if (mob != null && mob.id == bot.smartTargetMobId && !mob.isDie()) {
                return mob;
            }
        }
        List<Mob> candidates = new ArrayList<>();
        for (Mob mob : bot.zone.mobs) {
            if (mob != null && mob.location != null && !mob.isDie()) {
                candidates.add(mob);
            }
        }
        if (candidates.isEmpty()) {
            return null;
        }
        Mob target = candidates.get(Util.nextInt(0, candidates.size() - 1));
        bot.smartTargetMobId = target.id;
        return target;
    }

    private void pickNearbyItem(Bot bot) {
        if (bot.zone == null || bot.zone.items == null || bot.zone.items.isEmpty()) {
            return;
        }
        List<ItemMap> snapshot = new ArrayList<>(bot.zone.items);
        for (ItemMap itemMap : snapshot) {
            if (itemMap == null || itemMap.itemTemplate == null || itemMap.isBlackBall || itemMap.isNamecBall
                    || itemMap.isNamecBallTranhDoat) {
                continue;
            }
            if (itemMap.playerId != -1 && itemMap.playerId != bot.id) {
                continue;
            }
            if (Util.getDistance(bot.location.x, bot.location.y, itemMap.x, itemMap.y) <= 120) {
                ItemMapService.gI().pickItem(bot, itemMap.itemMapId, false);
                return;
            }
        }
    }

    private void trainPet(Bot bot, long now) {
        if (bot.pet == null || bot.zone == null || bot.zone.map == null) {
            return;
        }
        bot.pet.status = Pet.ATTACK;
        if (bot.pet.zone == null || !bot.pet.zone.equals(bot.zone)) {
            bot.pet.joinMapMaster();
        }
        Mob mob = chooseMob(bot);
        if (mob == null) {
            bot.smartNextMapAt = Math.max(bot.smartNextMapAt, now + Util.nextInt(180_000, 360_000));
            roamAround(bot, now);
            return;
        }
        if (Util.getDistance(bot.location.x, bot.location.y, mob.location.x, mob.location.y) > 260) {
            int moveX = clampMapX(bot.zone, mob.location.x + Util.nextInt(-90, 90));
            PlayerService.gI().playerMove(bot, moveX, mob.location.y);
        } else {
            roamAround(bot, now);
        }
    }

    private boolean isKameSocialMap(Bot bot) {
        return bot != null && bot.zone != null && bot.zone.map != null
                && bot.zone.map.mapId == KAME_ISLAND_MAP;
    }

    private void idleOnKame(Bot bot) {
        bot.smartTargetMobId = -1;
        if (bot.pet != null && bot.fusion.typeFusion == 0) {
            bot.pet.status = Pet.FOLLOW;
        }
    }

    private void selectCombatSkill(Bot bot, boolean includeControl) {
        int[] attackSkills = new int[]{
            Skill.DRAGON, Skill.DEMON, Skill.GALICK, Skill.KAMEJOKO, Skill.MASENKO,
            Skill.ANTOMIC, Skill.DE_TRUNG, Skill.BIEN_KHI, Skill.LIEN_HOAN,
            Skill.KHIEN_NANG_LUONG
        };
        Skill selected = randomKnownSkill(bot, attackSkills);
        if (selected == null) {
            int defaultSkill = bot.gender == 0 ? Skill.DRAGON : bot.gender == 1 ? Skill.DEMON : Skill.GALICK;
            selected = bot.playerSkill.getSkillbyId(defaultSkill);
        }
        bot.playerSkill.skillSelect = selected;
    }

    private Skill randomKnownSkill(Player player, int[] skillIds) {
        List<Skill> candidates = new ArrayList<>();
        for (int skillId : skillIds) {
            Skill skill = player.playerSkill.getSkillbyId(skillId);
            if (skill != null && skill.skillId != -1) {
                candidates.add(skill);
            }
        }
        return candidates.isEmpty() ? null : candidates.get(Util.nextInt(0, candidates.size() - 1));
    }

    private void moveToZone(Bot bot, Zone zone, int x, int y) {
        if (zone == null || zone.map == null) {
            return;
        }
        x = clampMapX(zone, x);
        y = y <= 0 ? zone.map.yPhysicInTop(x, 100) : y;
        if (bot.zone == null) {
            bot.location.x = x;
            bot.location.y = y;
            ChangeMapService.gI().goToMap(bot, zone);
            zone.load_Me_To_Another(bot);
        } else {
            ChangeMapService.gI().changeMap(bot, zone, x, y);
        }
        bot.smartTargetMobId = -1;
        if (bot.pet != null && bot.fusion.typeFusion == 0) {
            bot.pet.joinMapMaster();
        }
        PlayerService.gI().playerMove(bot, bot.location.x, bot.location.y);
    }

    private Zone getBalancedZone(Bot bot, int mapId) {
        map.Map gameMap = MapService.gI().getMapById(mapId);
        if (gameMap == null || gameMap.zones == null || gameMap.zones.isEmpty()) {
            return null;
        }
        if (MapService.gI().isMapOffline(mapId) || isBlockedBossMap(mapId)) {
            return null;
        }
        Zone allowed = safeGetMapCanJoin(bot, mapId, -1);
        if (allowed == null || allowed.map == null) {
            return allowed;
        }
        List<Zone> openZones = new ArrayList<>();
        List<Zone> leastBusyZones = new ArrayList<>();
        int leastBusyCount = Integer.MAX_VALUE;
        for (Zone zone : gameMap.zones) {
            if (zone == null) {
                continue;
            }
            int count = zone.getNumOfPlayers();
            if (!zone.isFullPlayer()) {
                openZones.add(zone);
                if (count < leastBusyCount) {
                    leastBusyZones.clear();
                    leastBusyZones.add(zone);
                    leastBusyCount = count;
                } else if (count == leastBusyCount) {
                    leastBusyZones.add(zone);
                }
            }
        }
        if (!openZones.isEmpty()) {
            int maxAcceptedCount = leastBusyCount + Util.nextInt(MIN_ZONE_POPULATION_SPREAD, MAX_ZONE_POPULATION_SPREAD);
            List<Zone> naturalZones = new ArrayList<>();
            for (Zone zone : openZones) {
                if (zone.getNumOfPlayers() <= maxAcceptedCount) {
                    naturalZones.add(zone);
                }
            }
            if (!naturalZones.isEmpty()) {
                return naturalZones.get(Util.nextInt(0, naturalZones.size() - 1));
            }
        }
        return leastBusyZones.isEmpty() ? null : leastBusyZones.get(Util.nextInt(0, leastBusyZones.size() - 1));
    }

    private void roamAround(Bot bot, long now) {
        if (bot.zone == null || bot.zone.map == null || now < bot.smartNextRoamAt) {
            return;
        }
        bot.smartNextRoamAt = now + Util.nextInt(2_500, 5_500);
        int x = clampMapX(bot.zone, bot.location.x + Util.nextInt(-90, 90));
        int y = bot.zone.map.yPhysicInTop(x, bot.location.y);
        PlayerService.gI().playerMove(bot, x, y);
    }

    private int randomMapX(Zone zone) {
        if (zone == null || zone.map == null || zone.map.mapWidth <= 160) {
            return 100;
        }
        if (zone.map.mapId == KAME_ISLAND_MAP) {
            int centerX = zone.map.mapWidth / 2;
            if (zone.map.npcs != null) {
                for (Npc npc : zone.map.npcs) {
                    if (npc != null && npc.tempId == ConstNpc.BA_HAT_MIT) {
                        centerX = npc.cx;
                        break;
                    }
                }
            }
            return clampMapX(zone, centerX + Util.nextInt(-220, 220));
        }
        return Util.nextInt(80, zone.map.mapWidth - 80);
    }

    private int clampMapX(Zone zone, int x) {
        if (zone == null || zone.map == null) {
            return x;
        }
        if (x < 40) {
            return 40;
        }
        return Math.min(x, zone.map.mapWidth - 40);
    }

    private boolean isPartyLeader(Bot bot) {
        return bot.smartLeaderId == 0 || bot.smartLeaderId == bot.id;
    }

    private Bot getLeader(Bot bot) {
        if (bot.smartLeaderId == 0 || bot.smartLeaderId == bot.id) {
            return bot;
        }
        for (Bot other : BotManager.gI().bot) {
            if (other != null && other.id == bot.smartLeaderId && other.isSmartBot()) {
                return other;
            }
        }
        return bot;
    }

    private Zone safeGetMapCanJoin(Bot bot, int mapId, int zoneId) {
        try {
            return MapService.gI().getMapCanJoin(bot, mapId, zoneId);
        } catch (Exception ignored) {
            return null;
        }
    }

    private boolean isHotFutureMap(int mapId) {
        return mapId == 100 || mapId == 103;
    }

    private int getMapPopulation(int mapId) {
        map.Map gameMap = MapService.gI().getMapById(mapId);
        if (gameMap == null || gameMap.zones == null) {
            return Integer.MAX_VALUE;
        }
        int population = 0;
        for (Zone zone : gameMap.zones) {
            if (zone != null) {
                population += zone.getNumOfPlayers();
            }
        }
        return population;
    }
}
