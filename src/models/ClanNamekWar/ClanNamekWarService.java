package models.ClanNamekWar;

import clan.Clan;
import consts.ConstClanNamekWar;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import item.Item;
import jdbc.DBConnecter;
import jdbc.NDVResultSet;
import jdbc.daos.ClanNamekWarDAO;
import map.ItemMap;
import map.Zone;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import player.Inventory;
import player.Bot.Bot;
import player.Bot.BotManager;
import player.Player;
import services.EffectSkillService;
import server.ServerManager;
import services.ItemService;
import services.MapService;
import services.PlayerService;
import services.Service;
import services.SkillService;
import services.func.ChangeMapService;
import skill.Skill;
import utils.Logger;
import utils.SkillUtil;
import utils.Util;

public class ClanNamekWarService implements Runnable {

    public enum Phase {
        REGISTERING, MATCHING, FIGHTING, FINISHED
    }

    private static ClanNamekWarService instance;

    private final List<Clan> registeredClans = Collections.synchronizedList(new ArrayList<>());
    private final List<ClanNamekWarMatch> matches = Collections.synchronizedList(new ArrayList<>());
    private final Map<Long, Long> controlImmuneUntil = new ConcurrentHashMap<>();

    private Phase phase = Phase.REGISTERING;
    private int nextMatchId = 1;
    private long lastUpdate;

    public static ClanNamekWarService gI() {
        if (instance == null) {
            instance = new ClanNamekWarService();
        }
        return instance;
    }

    public void register(Player player) {
        if (!ConstClanNamekWar.isConfigured()) {
            Service.gI().sendThongBao(player, "Sự kiện chưa cấu hình map.");
            return;
        }
        if (phase != Phase.REGISTERING) {
            Service.gI().sendThongBao(player, "Đã hết thời gian đăng ký.");
            return;
        }
        if (player.clan == null) {
            Service.gI().sendThongBao(player, "Cần có bang hội để đăng ký.");
            return;
        }
        if (!player.clan.isLeader(player)) {
            Service.gI().sendThongBao(player, "Chỉ bang chủ mới được đăng ký.");
            return;
        }
        if (player.clan.members.size() < ConstClanNamekWar.MIN_CLAN_MEMBERS_REGISTER) {
            Service.gI().sendThongBao(player, "Bang cần tối thiểu "
                    + ConstClanNamekWar.MIN_CLAN_MEMBERS_REGISTER + " thành viên.");
            return;
        }
        synchronized (registeredClans) {
            if (registeredClans.contains(player.clan)) {
                Service.gI().sendThongBao(player, "Bang hội của bạn đã đăng ký.");
                return;
            }
            registeredClans.add(player.clan);
        }
        ClanNamekWarDAO.saveRegistration(getSeasonId(), player.clan, player);
        Service.gI().sendThongBao(player, "Đăng ký Bảo Vệ Trưởng Lão Namek thành công.");
    }

    public void showRegistrationInfo(Player player) {
        StringBuilder sb = new StringBuilder("Danh sách bang đã đăng ký:\n");
        synchronized (registeredClans) {
            if (registeredClans.isEmpty()) {
                sb.append("Chưa có bang nào.");
            } else {
                for (int i = 0; i < registeredClans.size(); i++) {
                    sb.append(i + 1).append(". ").append(registeredClans.get(i).name).append("\n");
                }
            }
        }
        Service.gI().sendThongBao(player, sb.toString());
    }

    public void startMatching() {
        startMatching(null);
    }

    public void startMatching(Player requester) {
        if (!ConstClanNamekWar.isConfigured()) {
            notifyRequester(requester, "Sự kiện chưa cấu hình map.");
            return;
        }
        if (phase != Phase.REGISTERING) {
            notifyRequester(requester, "Không thể ghép cặp lúc này. Trạng thái hiện tại: " + phase + ".");
            return;
        }
        List<Clan> clans;
        synchronized (registeredClans) {
            clans = new ArrayList<>(registeredClans);
        }
        if (clans.size() < 2) {
            notifyRequester(requester, "Cần tối thiểu 2 bang đã đăng ký để ghép cặp. Hiện có " + clans.size() + " bang.");
            return;
        }
        phase = Phase.MATCHING;
        Collections.shuffle(clans);
        clearMatches();
        for (int i = 0; i + 1 < clans.size(); i += 2) {
            matches.add(new ClanNamekWarMatch(nextMatchId++, 1, clans.get(i), clans.get(i + 1)));
        }
        phase = matches.isEmpty() ? Phase.FINISHED : Phase.FIGHTING;
        startWaitingMatches();
        notifyRequester(requester, "Đã ghép " + matches.size() + " cặp đấu."
                + (clans.size() % 2 == 1 ? " Có 1 bang lẻ tạm thời chưa đấu." : ""));
    }

    public void startAdminTest(Player player) {
        if (player == null || !player.isAdmin()) {
            return;
        }
        if (!ConstClanNamekWar.isConfigured()) {
            Service.gI().sendThongBao(player, "Sự kiện chưa cấu hình map.");
            return;
        }
        if (player.clan == null) {
            Service.gI().sendThongBao(player, "Cần có bang hội để test.");
            return;
        }
        Zone defenseZone = getZone(ConstClanNamekWar.DEFENSE_MAP_ID, 0);
        Zone middleZone = getZone(ConstClanNamekWar.MIDDLE_MAP_ID, 0);
        Zone attackZone = getZone(ConstClanNamekWar.ATTACK_MAP_ID, 0);
        if (defenseZone == null || middleZone == null || attackZone == null) {
            Service.gI().sendThongBao(player, "Không tìm thấy zone map sự kiện.");
            return;
        }
        clearMatches();
        phase = Phase.FIGHTING;
        ClanNamekWarMatch match = new ClanNamekWarMatch(nextMatchId++, 0, player.clan, null);
        match.setZones(defenseZone, middleZone, attackZone);
        match.isTestMatch = true;
        synchronized (matches) {
            matches.add(match);
        }
        List<Player> attackers = new ArrayList<>();
        List<Player> defenders = new ArrayList<>();
        attackers.add(player);
        attackers.addAll(createTestBots(player.clan, attackZone, ConstClanNamekWar.ATTACKER_FLAG, 4, "Fide"));
        defenders.addAll(createTestBots(null, defenseZone, ConstClanNamekWar.DEFENDER_FLAG, 5, "Namek"));
        match.startTestTurn(attackers, defenders, false);
        Service.gI().sendThongBao(player, "Đã tạo trận test đủ cơ chế thật. Hệ thống sẽ random phe tấn công ở lượt 1 và tự đánh 2 lượt.");
    }

    public boolean isWarMap(int mapId) {
        return ConstClanNamekWar.isWarMap(mapId);
    }

    public boolean canApplyControl(Player target) {
        if (target == null || target.zone == null || !isWarMap(target.zone.map.mapId)) {
            return true;
        }
        return System.currentTimeMillis() >= controlImmuneUntil.getOrDefault(target.id, 0L);
    }

    public int normalizeControlTime(Player target, int originalTime) {
        if (target == null || target.zone == null || !isWarMap(target.zone.map.mapId)) {
            return originalTime;
        }
        int reduced = originalTime * ConstClanNamekWar.CONTROL_DURATION_PERCENT / 100;
        return Math.min(reduced, ConstClanNamekWar.CONTROL_MAX_TIME_MS);
    }

    public void markControlApplied(Player target) {
        if (target != null && target.zone != null && isWarMap(target.zone.map.mapId)) {
            controlImmuneUntil.put(target.id, System.currentTimeMillis() + ConstClanNamekWar.CONTROL_IMMUNE_MS);
        }
    }

    public void grantControlImmunity(List<Player> players, long durationMs) {
        long endAt = System.currentTimeMillis() + durationMs;
        for (Player player : players) {
            if (player == null || player.zone == null || !isWarMap(player.zone.map.mapId)) {
                continue;
            }
            controlImmuneUntil.put(player.id, endAt);
            clearCurrentControl(player);
            if (player.isPl()) {
                Service.gI().sendThongBao(player, "Bạn được miễn khống chế trong " + (durationMs / 1000) + " giây.");
            }
        }
    }

    private void clearCurrentControl(Player player) {
        if (player == null || player.effectSkill == null) {
            return;
        }
        if (player.effectSkill.useTroi) {
            EffectSkillService.gI().removeUseTroi(player);
        }
        if (player.effectSkill.anTroi) {
            EffectSkillService.gI().removeAnTroi(player);
        }
        if (player.effectSkill.isStun) {
            EffectSkillService.gI().removeStun(player);
        }
        if (player.effectSkill.isThoiMien) {
            EffectSkillService.gI().removeThoiMien(player);
        }
        if (player.effectSkill.isBlindDCTT) {
            EffectSkillService.gI().removeBlindDCTT(player);
        }
        if (player.effectSkill.isSocola) {
            EffectSkillService.gI().removeSocola(player);
        }
        if (player.effectSkill.isStone) {
            EffectSkillService.gI().removeStone(player);
        }
    }

    public ClanNamekWarMatch findMatch(Player player) {
        synchronized (matches) {
            for (ClanNamekWarMatch match : matches) {
                if (match.state != ClanNamekWarMatch.State.FINISHED && match.isParticipant(player)) {
                    return match;
                }
            }
        }
        return null;
    }

    public boolean tryPickDragonEnergy(Player player, ItemMap itemMap) {
        synchronized (matches) {
            for (ClanNamekWarMatch match : matches) {
                if (match.state != ClanNamekWarMatch.State.FINISHED
                        && match.tryPickDragonEnergy(player, itemMap)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void onPlayerKilled(Player killer, Player victim) {
        if (killer == null || victim == null || killer.equals(victim)) {
            return;
        }
        ClanNamekWarMatch killerMatch = findMatch(killer);
        if (killerMatch == null || killerMatch.state != ClanNamekWarMatch.State.RUNNING) {
            return;
        }
        ClanNamekWarMatch victimMatch = findMatch(victim);
        if (killerMatch == victimMatch) {
            killerMatch.onPlayerKilled(killer, victim);
        }
    }

    public String getDefenseSupportInfo(Player player) {
        ClanNamekWarMatch match = findMatch(player);
        if (match == null || match.state != ClanNamekWarMatch.State.RUNNING) {
            return "Chưa có trận Bảo Vệ Trưởng Lão Namek đang diễn ra.";
        }
        return match.getDefenseSupportInfo(player);
    }

    public void healElderFromNpc(Player player) {
        ClanNamekWarMatch match = findMatch(player);
        if (match == null || match.state != ClanNamekWarMatch.State.RUNNING) {
            Service.gI().sendThongBao(player, "Bạn không ở trong trận Bảo Vệ Trưởng Lão Namek đang diễn ra.");
            return;
        }
        match.healElderByDefender(player);
    }

    public boolean canEnterDefenseMap(Player player, Zone currentZone, Zone zoneJoin) {
        if (player == null || currentZone == null || zoneJoin == null
                || zoneJoin.map.mapId != ConstClanNamekWar.DEFENSE_MAP_ID) {
            return true;
        }
        ClanNamekWarMatch match = findMatch(player);
        if (match == null || match.state != ClanNamekWarMatch.State.RUNNING || !match.isParticipant(player)) {
            return true;
        }
        if (!ConstClanNamekWar.isWarMap(currentZone.map.mapId)) {
            return true;
        }
        if (match.isDefender(player)) {
            return true;
        }
        return match.isDefenseGateOpen() || currentZone.map.mapId == ConstClanNamekWar.DEFENSE_MAP_ID;
    }

    public boolean canEnterAttackMap(Player player, Zone currentZone, Zone zoneJoin) {
        if (player == null || currentZone == null || zoneJoin == null
                || zoneJoin.map.mapId != ConstClanNamekWar.ATTACK_MAP_ID) {
            return true;
        }
        ClanNamekWarMatch match = findMatch(player);
        if (match == null || match.state != ClanNamekWarMatch.State.RUNNING || !match.isParticipant(player)) {
            return true;
        }
        if (!ConstClanNamekWar.isWarMap(currentZone.map.mapId)) {
            return true;
        }
        return !match.isDefender(player) || currentZone.map.mapId == ConstClanNamekWar.ATTACK_MAP_ID;
    }

    public void forceDefenderWin(Player player) {
        if (player == null || !player.isAdmin()) {
            return;
        }
        ClanNamekWarMatch match = findMatch(player);
        if (match == null || match.state != ClanNamekWarMatch.State.RUNNING) {
            Service.gI().sendThongBao(player, "Bạn không ở trong trận Bảo Vệ Trưởng Lão Namek đang diễn ra.");
            return;
        }
        if (!match.isDefender(player)) {
            Service.gI().sendThongBao(player, "Lệnh này chỉ dùng khi bạn đang ở phe thủ.");
            return;
        }
        match.forceDefenderWin(player);
        Service.gI().sendThongBao(player, "Đã kết thúc nhanh lượt phe thủ.");
    }

    public void updateTestBot(Bot bot) {
        if (bot == null) {
            return;
        }
        ClanNamekWarMatch match = findMatch(bot);
        if (match == null || match.state != ClanNamekWarMatch.State.RUNNING) {
            return;
        }
        if (bot.zone == null || bot.isDie()) {
            reviveAndReturnTestBot(bot, match);
            return;
        }
        if (bot.playerSkill.skills.isEmpty()) {
            bot.leakSkill();
            ensureClanWarBotSkills(bot);
        }
        routeTestBot(bot, match);
        Player target = chooseBotTarget(bot, match);
        if (target == null || target.location == null || target.zone == null || !target.zone.equals(bot.zone)) {
            roamTestBot(bot);
            return;
        }
        chooseClanWarBotSkill(bot, target);
        long now = System.currentTimeMillis();
        if (now < bot.clanWarNextSkillAt || !bot.UseLastTimeSkill()) {
            return;
        }
        bot.clanWarNextSkillAt = now + Util.nextInt(900, 1800);
        int moveX = target.location.x + Util.nextInt(-45, 45);
        moveX = clampMapX(bot.zone, moveX);
        PlayerService.gI().playerMove(bot, moveX, target.location.y);
        SkillService.gI().useSkill(bot, target, null, -1, null);
    }

    private void reviveAndReturnTestBot(Bot bot, ClanNamekWarMatch match) {
        Zone spawnZone = match.isAttacker(bot) ? match.getAttackZone() : match.getDefenseZone();
        int x = match.isAttacker(bot) ? ConstClanNamekWar.ATTACKER_SPAWN_X : ConstClanNamekWar.DEFENDER_SPAWN_X;
        int y = match.isAttacker(bot) ? ConstClanNamekWar.ATTACKER_SPAWN_Y : ConstClanNamekWar.DEFENDER_SPAWN_Y;
        if (spawnZone == null) {
            return;
        }
        if (bot.zone == null) {
            bot.location.x = x + Util.nextInt(-60, 60);
            bot.location.y = y;
            ChangeMapService.gI().goToMap(bot, spawnZone);
            spawnZone.load_Me_To_Another(bot);
        } else if (bot.zone != spawnZone) {
            ChangeMapService.gI().changeMap(bot, spawnZone, x + Util.nextInt(-60, 60), y);
        }
        Service.gI().hsChar(bot, bot.nPoint.hpMax, bot.nPoint.mpMax);
        Service.gI().changeFlag(bot, match.isAttacker(bot)
                ? ConstClanNamekWar.ATTACKER_FLAG : ConstClanNamekWar.DEFENDER_FLAG);
        bot.clanWarNextRouteAt = System.currentTimeMillis() + Util.nextInt(1200, 2500);
        bot.clanWarNextSkillAt = System.currentTimeMillis() + Util.nextInt(800, 1500);
    }

    private void routeTestBot(Bot bot, ClanNamekWarMatch match) {
        long now = System.currentTimeMillis();
        if (now < bot.clanWarNextRouteAt || bot.zone == null) {
            return;
        }
        bot.clanWarNextRouteAt = now + Util.nextInt(3500, 6500);
        if (match.isAttacker(bot)) {
            routeAttackerBot(bot, match);
        } else if (match.isDefender(bot)) {
            routeDefenderBot(bot, match);
        }
    }

    private void routeAttackerBot(Bot bot, ClanNamekWarMatch match) {
        if (bot.zone.map.mapId == ConstClanNamekWar.ATTACK_MAP_ID) {
            moveBotToZone(bot, match.getMiddleZone(), randomMapX(match.getMiddleZone()), ConstClanNamekWar.ATTACKER_SPAWN_Y);
            return;
        }
        if (bot.zone.map.mapId == ConstClanNamekWar.MIDDLE_MAP_ID && match.canAttackerReachDefense()) {
            moveBotToZone(bot, match.getDefenseZone(), ConstClanNamekWar.ELDER_SPAWN_X - 120,
                    ConstClanNamekWar.ELDER_SPAWN_Y);
            return;
        }
        if (bot.zone.map.mapId == ConstClanNamekWar.DEFENSE_MAP_ID && !match.canAttackerReachDefense()) {
            moveBotToZone(bot, match.getMiddleZone(), randomMapX(match.getMiddleZone()), ConstClanNamekWar.ATTACKER_SPAWN_Y);
        }
    }

    private void routeDefenderBot(Bot bot, ClanNamekWarMatch match) {
        if (bot.zone.map.mapId == ConstClanNamekWar.ATTACK_MAP_ID) {
            moveBotToZone(bot, match.getDefenseZone(), ConstClanNamekWar.DEFENDER_SPAWN_X,
                    ConstClanNamekWar.DEFENDER_SPAWN_Y);
            return;
        }
        bot.clanWarRouteStep++;
        if (bot.zone.map.mapId == ConstClanNamekWar.DEFENSE_MAP_ID && bot.clanWarRouteStep % 3 == 0) {
            moveBotToZone(bot, match.getMiddleZone(), ConstClanNamekWar.SHIELD_PILLAR_X[0] + Util.nextInt(-80, 80),
                    ConstClanNamekWar.SHIELD_PILLAR_Y[0]);
        } else if (bot.zone.map.mapId == ConstClanNamekWar.MIDDLE_MAP_ID && bot.clanWarRouteStep % 2 == 0) {
            moveBotToZone(bot, match.getDefenseZone(), ConstClanNamekWar.ELDER_SPAWN_X + Util.nextInt(-160, 120),
                    ConstClanNamekWar.ELDER_SPAWN_Y);
        }
    }

    private void roamTestBot(Bot bot) {
        long now = System.currentTimeMillis();
        if (bot.zone == null || now < bot.clanWarNextMoveAt) {
            return;
        }
        bot.clanWarNextMoveAt = now + Util.nextInt(900, 1600);
        int x = clampMapX(bot.zone, bot.location.x + Util.nextInt(-90, 90));
        int y = bot.zone.map.yPhysicInTop(x, bot.location.y);
        if (y <= 0) {
            y = bot.location.y;
        }
        PlayerService.gI().playerMove(bot, x, y);
    }

    private void moveBotToZone(Bot bot, Zone zone, int x, int y) {
        if (zone == null) {
            return;
        }
        ChangeMapService.gI().changeMap(bot, zone, clampMapX(zone, x), y);
        PlayerService.gI().playerMove(bot, bot.location.x, bot.location.y);
    }

    private int randomMapX(Zone zone) {
        if (zone == null) {
            return 100;
        }
        return Util.nextInt(100, Math.max(100, zone.map.mapWidth - 100));
    }

    private int clampMapX(Zone zone, int x) {
        if (zone == null || zone.map == null) {
            return x;
        }
        int minX = 40;
        int maxX = Math.max(minX, zone.map.mapWidth - 40);
        return Math.max(minX, Math.min(maxX, x));
    }

    private void chooseClanWarBotSkill(Bot bot, Player target) {
        Skill selected = null;
        if (target != null && !target.isBoss && Util.isTrue(45, 100)) {
            selected = randomKnownSkill(bot, new int[]{
                Skill.THAI_DUONG_HA_SAN, Skill.TROI, Skill.THOI_MIEN, Skill.SOCOLA, Skill.DICH_CHUYEN_TUC_THOI
            });
        }
        if (selected == null) {
            selected = randomKnownSkill(bot, new int[]{
                Skill.DRAGON, Skill.DEMON, Skill.GALICK, Skill.KAMEJOKO, Skill.MASENKO
            });
        }
        if (selected == null && !bot.playerSkill.skills.isEmpty()) {
            selected = bot.playerSkill.skills.get(Util.nextInt(0, bot.playerSkill.skills.size() - 1));
        }
        if (selected != null) {
            bot.playerSkill.skillSelect = selected;
        }
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

    @Override
    public void run() {
        while (ServerManager.isRunning) {
            try {
                if (System.currentTimeMillis() - lastUpdate >= 1000) {
                    updateMatches();
                    lastUpdate = System.currentTimeMillis();
                }
                Thread.sleep(200);
            } catch (Exception e) {
                Logger.logException(ClanNamekWarService.class, e);
            }
        }
    }

    private void updateMatches() {
        if (phase != Phase.FIGHTING) {
            return;
        }
        synchronized (matches) {
            for (ClanNamekWarMatch match : matches) {
                match.update();
                if (match.state == ClanNamekWarMatch.State.RUNNING && (match.elderKilled || match.isTurnTimeout())) {
                    match.finishTurn();
                    if (match.isTestMatch && match.hasNextTurn()) {
                        match.startNextTestTurn();
                    } else if (match.turn == 1 && match.clanB != null) {
                        match.startTurn(2, match.participantsA, match.participantsB);
                    } else {
                        match.finishMatch();
                        match.kickAllToRegisterMap();
                        if (!match.isTestMatch) {
                            ClanNamekWarDAO.saveMatchResult(getSeasonId(), match);
                        }
                    }
                }
            }
        }
    }

    private void clearMatches() {
        synchronized (matches) {
            for (ClanNamekWarMatch match : matches) {
                if (match != null) {
                    match.dispose();
                    match.kickAllToRegisterMap();
                }
            }
            matches.clear();
        }
    }

    private void startWaitingMatches() {
        synchronized (matches) {
            int zoneIndex = 0;
            for (ClanNamekWarMatch match : matches) {
                Zone defenseZone = getZone(ConstClanNamekWar.DEFENSE_MAP_ID, zoneIndex);
                Zone middleZone = getZone(ConstClanNamekWar.MIDDLE_MAP_ID, zoneIndex);
                Zone attackZone = getZone(ConstClanNamekWar.ATTACK_MAP_ID, zoneIndex);
                if (defenseZone == null || middleZone == null || attackZone == null) {
                    continue;
                }
                match.setZones(defenseZone, middleZone, attackZone);
                match.startTurn(1, collectPlayers(match.clanA), collectPlayers(match.clanB));
                zoneIndex++;
            }
        }
    }

    private List<Player> collectPlayers(Clan clan) {
        List<Player> players = new ArrayList<>();
        if (clan == null) {
            return players;
        }
        synchronized (clan.membersInGame) {
            for (Player player : clan.membersInGame) {
                if (player != null && player.zone != null
                        && player.zone.map.mapId == ConstClanNamekWar.REGISTRATION_MAP_ID) {
                    players.add(player);
                    if (players.size() >= ConstClanNamekWar.MAX_PLAYERS_PER_CLAN) {
                        break;
                    }
                }
            }
        }
        return players;
    }

    private Zone getZone(int mapId, int zoneIndex) {
        map.Map map = MapService.gI().getMapById(mapId);
        if (map == null || map.zones.isEmpty()) {
            return null;
        }
        return map.zones.get(zoneIndex % map.zones.size());
    }

    private List<Player> createTestBots(Clan clan, Zone zone, byte flag, int count, String prefix) {
        List<Player> players = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Bot bot = new Bot((short) 64, (short) 59, (short) 60, 99, prefix + "Bot" + Util.nextInt(1000, 9999),
                    null, (short) 0);
            bot.clan = clan;
            if (!applyRandomDbProfile(bot, prefix)) {
                applyFallbackBotProfile(bot);
            }
            ensureClanWarBotSkills(bot);
            bot.location.x = flag == ConstClanNamekWar.ATTACKER_FLAG
                    ? ConstClanNamekWar.ATTACKER_SPAWN_X + Util.nextInt(-80, 80)
                    : ConstClanNamekWar.DEFENDER_SPAWN_X + Util.nextInt(-80, 80);
            bot.location.y = flag == ConstClanNamekWar.ATTACKER_FLAG
                    ? ConstClanNamekWar.ATTACKER_SPAWN_Y
                    : ConstClanNamekWar.DEFENDER_SPAWN_Y;
            ChangeMapService.gI().goToMap(bot, zone);
            zone.load_Me_To_Another(bot);
            Service.gI().changeFlag(bot, flag);
            PlayerService.gI().playerMove(bot, bot.location.x, bot.location.y);
            BotManager.gI().bot.add(bot);
            players.add(bot);
        }
        return players;
    }

    private boolean applyRandomDbProfile(Bot bot, String prefix) {
        NDVResultSet rs = null;
        try {
            rs = DBConnecter.executeQuery(
                    "select id, name, gender, head, items_body, skills, data_point from player "
                    + "where items_body is not null and skills is not null order by rand() limit 1");
            if (!rs.first()) {
                return false;
            }
            bot.name = prefix + "-" + rs.getString("name") + "-" + Util.nextInt(10, 99);
            bot.gender = rs.getByte("gender");
            bot.head = rs.getShort("head");
            if (bot.head == -1) {
                bot.head = switch (bot.gender) {
                    case 0 -> 64;
                    case 1 -> 9;
                    default -> 6;
                };
            }
            loadBotPoints(bot, rs.getString("data_point"));
            loadBotBody(bot, rs.getString("items_body"));
            loadBotSkills(bot, rs.getString("skills"));
            return true;
        } catch (Exception e) {
            Logger.logException(ClanNamekWarService.class, e, "Không thể lấy profile DB cho bot test clan war");
            return false;
        } finally {
            if (rs != null) {
                rs.dispose();
            }
        }
    }

    private void applyFallbackBotProfile(Bot bot) {
        bot.gender = 1;
        bot.head = 64;
        bot.nPoint.limitPower = 8;
        bot.nPoint.power = 2_000_000_000L;
        bot.nPoint.tiemNang = 2_000_000_000L;
        bot.nPoint.dameg = 500_000;
        bot.nPoint.mpg = 2_000_000_000;
        bot.nPoint.mpMax = 2_000_000_000;
        bot.nPoint.mp = 2_000_000_000;
        bot.nPoint.hpg = 20_000_000;
        bot.nPoint.hpMax = 20_000_000;
        bot.nPoint.hp = 20_000_000;
        bot.nPoint.maxStamina = 20_000;
        bot.nPoint.stamina = 20_000;
        bot.nPoint.critg = 10;
        bot.nPoint.defg = 10;
        bot.leakSkill();
    }

    private void loadBotPoints(Bot bot, String dataPoint) {
        try {
            JSONArray dataArray = (JSONArray) JSONValue.parse(dataPoint);
            bot.nPoint.limitPower = Byte.parseByte(String.valueOf(dataArray.get(0)));
            bot.nPoint.power = Long.parseLong(String.valueOf(dataArray.get(1)));
            bot.nPoint.tiemNang = Long.parseLong(String.valueOf(dataArray.get(2)));
            bot.nPoint.stamina = Short.parseShort(String.valueOf(dataArray.get(3)));
            bot.nPoint.maxStamina = Short.parseShort(String.valueOf(dataArray.get(4)));
            bot.nPoint.hpg = Math.max(20_000_000L, Long.parseLong(String.valueOf(dataArray.get(5))));
            bot.nPoint.mpg = Math.max(20_000_000L, Long.parseLong(String.valueOf(dataArray.get(6))));
            bot.nPoint.dameg = Math.max(300_000L, Long.parseLong(String.valueOf(dataArray.get(7))));
            bot.nPoint.defg = Integer.parseInt(String.valueOf(dataArray.get(8)));
            bot.nPoint.critg = Byte.parseByte(String.valueOf(dataArray.get(9)));
            bot.nPoint.hpMax = Math.max(bot.nPoint.hpg, Long.parseLong(String.valueOf(dataArray.get(11))));
            bot.nPoint.mpMax = Math.max(bot.nPoint.mpg, Long.parseLong(String.valueOf(dataArray.get(12))));
            bot.nPoint.hp = bot.nPoint.hpMax;
            bot.nPoint.mp = bot.nPoint.mpMax;
        } catch (Exception e) {
            bot.nPoint.hpg = 20_000_000;
            bot.nPoint.hpMax = 20_000_000;
            bot.nPoint.hp = 20_000_000;
            bot.nPoint.mpg = 20_000_000;
            bot.nPoint.mpMax = 20_000_000;
            bot.nPoint.mp = 20_000_000;
            bot.nPoint.dameg = 500_000;
        }
    }

    private void loadBotBody(Bot bot, String itemsBody) {
        bot.inventory.itemsBody.clear();
        JSONArray dataArray = (JSONArray) JSONValue.parse(itemsBody);
        int sizeFromDb = dataArray == null ? 0 : dataArray.size();
        for (int i = 0; i < Inventory.BODY_SLOT_COUNT; i++) {
            Item item = ItemService.gI().createItemNull();
            try {
                if (i < sizeFromDb) {
                    JSONArray dataItem = (JSONArray) JSONValue.parse(String.valueOf(dataArray.get(i)));
                    short tempId = Short.parseShort(String.valueOf(dataItem.get(0)));
                    if (tempId != -1) {
                        item = ItemService.gI().createNewItem(tempId,
                                Integer.parseInt(String.valueOf(dataItem.get(1))));
                        JSONArray options = (JSONArray) JSONValue.parse(String.valueOf(dataItem.get(2)).replaceAll("\"", ""));
                        for (Object optObj : options) {
                            JSONArray opt = (JSONArray) JSONValue.parse(String.valueOf(optObj));
                            item.itemOptions.add(new Item.ItemOption(
                                    Integer.parseInt(String.valueOf(opt.get(0))),
                                    Integer.parseInt(String.valueOf(opt.get(1)))));
                        }
                        item.createTime = Long.parseLong(String.valueOf(dataItem.get(3)));
                        if (ItemService.gI().isOutOfDateTime(item)) {
                            item = ItemService.gI().createItemNull();
                        }
                    }
                }
            } catch (Exception ignored) {
                item = ItemService.gI().createItemNull();
            }
            bot.inventory.itemsBody.add(item);
        }
    }

    private void loadBotSkills(Bot bot, String skillsJson) {
        bot.playerSkill.skills.clear();
        JSONArray dataArray = (JSONArray) JSONValue.parse(skillsJson);
        if (dataArray != null) {
            for (Object obj : dataArray) {
                try {
                    JSONArray dataSkill = (JSONArray) JSONValue.parse(String.valueOf(obj));
                    int tempId = Integer.parseInt(String.valueOf(dataSkill.get(0)));
                    byte point = Byte.parseByte(String.valueOf(dataSkill.get(1)));
                    Skill skill = point > 0 ? SkillUtil.createSkill(tempId, point) : SkillUtil.createSkillLevel0(tempId);
                    if (skill != null) {
                        skill.lastTimeUseThisSkill = 0;
                        bot.playerSkill.skills.add(skill);
                    }
                } catch (Exception ignored) {
                }
            }
        }
        if (bot.playerSkill.skills.isEmpty()) {
            bot.leakSkill();
        }
    }

    private void ensureClanWarBotSkills(Bot bot) {
        ensureBotSkill(bot, Skill.THAI_DUONG_HA_SAN, 7);
        ensureBotSkill(bot, Skill.TROI, 7);
        ensureBotSkill(bot, Skill.THOI_MIEN, 7);
        ensureBotSkill(bot, Skill.SOCOLA, 7);
        ensureBotSkill(bot, Skill.DICH_CHUYEN_TUC_THOI, 7);
        if (randomKnownSkill(bot, new int[]{Skill.DRAGON, Skill.DEMON, Skill.GALICK, Skill.KAMEJOKO, Skill.MASENKO}) == null) {
            int defaultSkill = bot.gender == 0 ? Skill.DRAGON : bot.gender == 1 ? Skill.DEMON : Skill.GALICK;
            ensureBotSkill(bot, defaultSkill, 7);
        }
        if (bot.playerSkill.skillSelect == null || bot.playerSkill.skillSelect.skillId == -1) {
            bot.playerSkill.skillSelect = randomKnownSkill(bot, new int[]{
                Skill.DRAGON, Skill.DEMON, Skill.GALICK, Skill.KAMEJOKO, Skill.MASENKO,
                Skill.THAI_DUONG_HA_SAN, Skill.TROI, Skill.THOI_MIEN, Skill.SOCOLA
            });
        }
    }

    private void ensureBotSkill(Bot bot, int skillId, int level) {
        Skill current = bot.playerSkill.getSkillbyId(skillId);
        if (current != null && current.skillId != -1) {
            return;
        }
        Skill skill = SkillUtil.createSkill(skillId, level);
        if (skill != null) {
            if (current != null) {
                bot.playerSkill.skills.remove(current);
            }
            skill.lastTimeUseThisSkill = 0;
            bot.playerSkill.skills.add(skill);
        }
    }

    private Player chooseBotTarget(Bot bot, ClanNamekWarMatch match) {
        Player pillar = match.getShieldPillarTarget(bot);
        if (pillar != null) {
            return pillar;
        }
        if (bot.cFlag == ConstClanNamekWar.ATTACKER_FLAG && match.elderBoss != null && match.elderBoss.zone != null
                && match.elderBoss.zone.equals(bot.zone) && !match.elderBoss.isDie()) {
            return match.elderBoss;
        }
        List<Player> candidates = new ArrayList<>();
        for (Player player : bot.zone.getPlayers()) {
            if (player != null && !player.equals(bot) && !player.isDie()
                    && player.cFlag != 0 && player.cFlag != bot.cFlag
                    && SkillService.gI().canAttackPlayer(bot, player)) {
                candidates.add(player);
            }
        }
        if (candidates.isEmpty()) {
            return null;
        }
        return candidates.get(Util.nextInt(0, candidates.size() - 1));
    }

    private int getSeasonId() {
        return Integer.parseInt(LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE));
    }

    private void notifyRequester(Player requester, String text) {
        if (requester != null) {
            Service.gI().sendThongBao(requester, text);
        }
    }
}
