package models.ClanNamekWar;

import boss.boss_manifest.ClanNamekWar.NamekElderBoss;
import boss.boss_manifest.ClanNamekWar.NamekShieldPillarBoss;
import clan.Clan;
import consts.ConstClanNamekWar;
import consts.ConstNpc;
import java.util.ArrayList;
import java.util.List;
import jdbc.daos.ClanNamekWarDAO;
import map.ItemMap;
import map.Zone;
import npc.Npc;
import npc.NpcFactory;
import player.Bot.Bot;
import player.Bot.BotManager;
import player.Player;
import services.ItemMapService;
import services.NpcService;
import services.PlayerService;
import services.Service;
import services.func.ChangeMapService;
import utils.Util;

public class ClanNamekWarMatch {

    public enum State {
        WAITING, RUNNING, FINISHED
    }

    public final int id;
    public final int round;
    public final Clan clanA;
    public final Clan clanB;
    public final List<Player> participantsA = new ArrayList<>();
    public final List<Player> participantsB = new ArrayList<>();

    public State state = State.WAITING;
    public int turn = 0;
    public boolean isTestMatch;
    public Clan attacker;
    public Clan defender;
    public long turnStartTime;
    public long damageClanA;
    public long damageClanB;
    public boolean elderKilled;
    public NamekElderBoss elderBoss;
    private long pendingSpawnElderAt;
    private final List<NamekShieldPillarBoss> shieldPillars = new ArrayList<>();
    private boolean bypassDefenseGate;
    private boolean testTeamAAttacks;
    private int maxTurns = 1;
    private int shieldPillarsDestroyed;
    private long defenseGateOpenUntil;
    private boolean forceTurnEnd;
    private int defenderKillCount;
    private int elderHealUsed;
    private ItemMap dragonEnergy;
    private long dragonEnergyExpireAt;
    private long nextDragonEnergySpawnAt;
    private long nextPlanetQuakeAt;
    private long planetQuakeEndAt;
    private long lastPlanetQuakeTick;

    private Zone defenseZone;
    private Zone middleZone;
    private Zone attackZone;

    public ClanNamekWarMatch(int id, int round, Clan clanA, Clan clanB) {
        this.id = id;
        this.round = round;
        this.clanA = clanA;
        this.clanB = clanB;
    }

    public void setZones(Zone defenseZone, Zone middleZone, Zone attackZone) {
        this.defenseZone = defenseZone;
        this.middleZone = middleZone;
        this.attackZone = attackZone;
        ensureDefenseSupportNpc();
    }

    public void startTurn(int turn, List<Player> clanAPlayers, List<Player> clanBPlayers) {
        this.turn = turn;
        this.attacker = turn == 1 ? clanA : clanB;
        this.defender = turn == 1 ? clanB : clanA;
        this.elderKilled = false;
        this.bypassDefenseGate = false;
        this.shieldPillarsDestroyed = 0;
        this.defenseGateOpenUntil = 0;
        this.turnStartTime = System.currentTimeMillis();
        this.state = State.RUNNING;
        this.forceTurnEnd = false;
        prepareTurnRuntime();

        if (this.participantsA.isEmpty()) {
            this.participantsA.addAll(clanAPlayers);
        }
        if (this.participantsB.isEmpty()) {
            this.participantsB.addAll(clanBPlayers);
        }
        applyTeamsToMaps(clanA == attacker);
        sendRoleInstructions(participantsA, clanA == attacker);
        sendRoleInstructions(participantsB, clanB == attacker);
        spawnShieldPillars();
        scheduleSpawnElder();
        sendMatchMessage("Lượt " + turn + ": " + getClanName(attacker) + " tấn công, "
                + getClanName(defender) + " bảo vệ Trưởng Lão.");
    }

    public void startTestTurn(List<Player> attackers, List<Player> defenders) {
        startTestTurn(attackers, defenders, true);
    }

    public void startTestTurn(List<Player> attackers, List<Player> defenders, boolean bypassDefenseGate) {
        this.isTestMatch = true;
        this.maxTurns = 2;
        this.turn = 1;
        this.testTeamAAttacks = Util.isTrue(1, 2);
        this.attacker = this.testTeamAAttacks ? clanA : clanB;
        this.defender = this.testTeamAAttacks ? clanB : clanA;
        this.elderKilled = false;
        this.bypassDefenseGate = bypassDefenseGate;
        this.shieldPillarsDestroyed = bypassDefenseGate ? ConstClanNamekWar.SHIELD_PILLAR_X.length : 0;
        this.defenseGateOpenUntil = bypassDefenseGate ? Long.MAX_VALUE : 0;
        this.turnStartTime = System.currentTimeMillis();
        this.state = State.RUNNING;
        this.forceTurnEnd = false;
        this.participantsA.clear();
        this.participantsB.clear();
        this.participantsA.addAll(attackers);
        this.participantsB.addAll(defenders);
        startCurrentTestTurn();
    }

    public void startNextTestTurn() {
        this.turn++;
        this.elderKilled = false;
        this.testTeamAAttacks = !this.testTeamAAttacks;
        this.attacker = this.testTeamAAttacks ? clanA : clanB;
        this.defender = this.testTeamAAttacks ? clanB : clanA;
        this.shieldPillarsDestroyed = bypassDefenseGate ? ConstClanNamekWar.SHIELD_PILLAR_X.length : 0;
        this.defenseGateOpenUntil = bypassDefenseGate ? Long.MAX_VALUE : 0;
        this.turnStartTime = System.currentTimeMillis();
        this.state = State.RUNNING;
        this.forceTurnEnd = false;
        startCurrentTestTurn();
    }

    private void startCurrentTestTurn() {
        prepareTurnRuntime();
        applyTeamsToMaps(testTeamAAttacks);
        sendRoleInstructions(participantsA, testTeamAAttacks);
        sendRoleInstructions(participantsB, !testTeamAAttacks);
        scheduleSpawnElder();
        if (!bypassDefenseGate) {
            spawnShieldPillars();
        }
        sendMatchMessage(bypassDefenseGate
                ? "Test lượt " + turn + ": đội " + (testTeamAAttacks ? "A" : "B") + " là phe tấn công."
                : "Test lượt " + turn + ": đội " + (testTeamAAttacks ? "A" : "B")
                + " là phe tấn công, cần phá trụ lá chắn trước khi đánh Trưởng Lão.");
    }

    public void update() {
        if (this.state != State.RUNNING) {
            return;
        }
        long now = System.currentTimeMillis();
        if (pendingSpawnElderAt > 0 && now >= pendingSpawnElderAt) {
            pendingSpawnElderAt = 0;
            spawnElder();
        }
        if (!bypassDefenseGate && defenseGateOpenUntil > 0 && now > defenseGateOpenUntil
                && !elderKilled) {
            defenseGateOpenUntil = 0;
            shieldPillarsDestroyed = 0;
            sendMatchMessage("Lá chắn Trưởng Lão đã phục hồi. Phe công cần phá trụ lại.");
            spawnShieldPillars();
        }
        updateDragonEnergy(now);
        updatePlanetQuake(now);
    }

    private void prepareTurnRuntime() {
        long now = System.currentTimeMillis();
        this.defenderKillCount = 0;
        this.elderHealUsed = 0;
        removeDragonEnergy();
        this.nextDragonEnergySpawnAt = now + ConstClanNamekWar.DRAGON_ENERGY_SPAWN_DELAY_MS;
        this.dragonEnergyExpireAt = 0;
        this.nextPlanetQuakeAt = now + ConstClanNamekWar.PLANET_QUAKE_INTERVAL_MS;
        this.planetQuakeEndAt = 0;
        this.lastPlanetQuakeTick = 0;
    }

    private void applyTeamsToMaps(boolean teamAAttacks) {
        movePlayers(participantsA, teamAAttacks, bypassDefenseGate && teamAAttacks);
        movePlayers(participantsB, !teamAAttacks, bypassDefenseGate && !teamAAttacks);
    }

    private void movePlayers(List<Player> players, boolean isAttacker, boolean toDefenseBypass) {
        for (Player player : players) {
            if (player == null || player.zone == null) {
                continue;
            }
            Service.gI().changeFlag(player, isAttacker ? ConstClanNamekWar.ATTACKER_FLAG : ConstClanNamekWar.DEFENDER_FLAG);
            if (toDefenseBypass) {
                int x = ConstClanNamekWar.ELDER_SPAWN_X - 140;
                int y = ConstClanNamekWar.ELDER_SPAWN_Y;
                ChangeMapService.gI().changeMapNonSpaceship(player, ConstClanNamekWar.DEFENSE_MAP_ID, x, y);
            } else if (isAttacker) {
                ChangeMapService.gI().changeMapNonSpaceship(player, ConstClanNamekWar.ATTACK_MAP_ID,
                        ConstClanNamekWar.ATTACKER_SPAWN_X, ConstClanNamekWar.ATTACKER_SPAWN_Y);
            } else {
                ChangeMapService.gI().changeMapNonSpaceship(player, ConstClanNamekWar.DEFENSE_MAP_ID,
                        ConstClanNamekWar.DEFENDER_SPAWN_X, ConstClanNamekWar.DEFENDER_SPAWN_Y);
            }
        }
    }

    private void scheduleSpawnElder() {
        removeElder();
        pendingSpawnElderAt = System.currentTimeMillis() + 1200;
    }

    private void spawnElder() {
        if (this.state != State.RUNNING || this.defenseZone == null) {
            return;
        }
        removeElder();
        try {
            this.elderBoss = new NamekElderBoss(this, defenseZone);
            this.elderBoss.changeStatus(boss.BossStatus.RESPAWN);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isAttacker(Player player) {
        if (player == null || player.cFlag != ConstClanNamekWar.ATTACKER_FLAG) {
            return false;
        }
        return isParticipant(player);
    }

    public boolean isDefender(Player player) {
        return player != null && player.cFlag == ConstClanNamekWar.DEFENDER_FLAG && isParticipant(player);
    }

    public boolean canDamageElder(Player player) {
        return isAttacker(player) && (bypassDefenseGate || isDefenseGateOpen());
    }

    public boolean canAttackerReachDefense() {
        return bypassDefenseGate || isDefenseGateOpen();
    }

    public boolean isDefenseGateOpen() {
        return defenseGateOpenUntil > System.currentTimeMillis();
    }

    public Zone getDefenseZone() {
        return defenseZone;
    }

    public Zone getMiddleZone() {
        return middleZone;
    }

    public Zone getAttackZone() {
        return attackZone;
    }

    public Player getShieldPillarTarget(Player attackerPlayer) {
        if (!isAttacker(attackerPlayer) || attackerPlayer.zone == null) {
            return null;
        }
        for (NamekShieldPillarBoss pillar : shieldPillars) {
            if (pillar != null && pillar.zone != null && pillar.zone.equals(attackerPlayer.zone) && !pillar.isDie()) {
                return pillar;
            }
        }
        return null;
    }

    public boolean isParticipant(Player player) {
        return player != null && (participantsA.contains(player) || participantsB.contains(player));
    }

    public synchronized long recordElderDamage(Player attackerPlayer, long damage) {
        if (!isAttacker(attackerPlayer)) {
            return 0;
        }
        if (attacker == clanA) {
            damageClanA += damage;
        } else if (attacker == clanB) {
            damageClanB += damage;
        }
        return damage;
    }

    public void onElderKilled() {
        this.elderKilled = true;
    }

    public void onShieldPillarKilled(NamekShieldPillarBoss pillar) {
        if (!shieldPillars.remove(pillar)) {
            return;
        }
        shieldPillarsDestroyed++;
        sendMatchMessage("Đã phá " + shieldPillarsDestroyed + "/" + ConstClanNamekWar.SHIELD_PILLAR_X.length
                + " trụ lá chắn.");
        if (shieldPillarsDestroyed >= ConstClanNamekWar.SHIELD_PILLAR_X.length) {
            defenseGateOpenUntil = System.currentTimeMillis() + ConstClanNamekWar.DEFENSE_GATE_OPEN_MS;
            sendMatchMessage("Lá chắn Trưởng Lão đã mở trong " + (ConstClanNamekWar.DEFENSE_GATE_OPEN_MS / 1000)
                    + " giây.");
        }
    }

    public synchronized void onPlayerKilled(Player killer, Player victim) {
        if (this.state != State.RUNNING || !isDefender(killer) || !isAttacker(victim)) {
            return;
        }
        defenderKillCount++;
        int nextNeed = (elderHealUsed + 1) * ConstClanNamekWar.DEFENDER_HEAL_KILL_REQUIRE;
        if (elderHealUsed < ConstClanNamekWar.DEFENDER_HEAL_MAX_TIMES && defenderKillCount >= nextNeed) {
            sendTeamMessage(false, "Phe thủ đã hạ đủ " + nextNeed
                    + " phe công. Gặp Trưởng Lão Guru để hồi "
                    + ConstClanNamekWar.DEFENDER_HEAL_PERCENT + "% máu cho Trưởng Lão.");
        } else if (defenderKillCount % 5 == 0) {
            sendTeamMessage(false, "Phe thủ đã hạ " + defenderKillCount + "/" + nextNeed
                    + " phe công để mở lần hồi máu tiếp theo.");
        }
    }

    public String getDefenseSupportInfo(Player player) {
        if (!isDefender(player)) {
            return "Chỉ phe thủ mới có thể dùng hỗ trợ hồi máu Trưởng Lão.";
        }
        if (elderHealUsed >= ConstClanNamekWar.DEFENDER_HEAL_MAX_TIMES) {
            return "Phe thủ đã dùng tối đa " + ConstClanNamekWar.DEFENDER_HEAL_MAX_TIMES
                    + " lần hồi máu Trưởng Lão.";
        }
        int nextNeed = (elderHealUsed + 1) * ConstClanNamekWar.DEFENDER_HEAL_KILL_REQUIRE;
        return "Hồi máu Trưởng Lão Namek\n"
                + "Phe thủ đã hạ: " + defenderKillCount + "/" + nextNeed + " phe công\n"
                + "Mỗi mốc hồi " + ConstClanNamekWar.DEFENDER_HEAL_PERCENT + "% máu, tối đa "
                + ConstClanNamekWar.DEFENDER_HEAL_MAX_TIMES + " lần.";
    }

    public synchronized void healElderByDefender(Player player) {
        if (!isDefender(player)) {
            Service.gI().sendThongBao(player, "Chỉ phe thủ mới có thể hồi máu Trưởng Lão.");
            return;
        }
        if (elderBoss == null || elderBoss.zone == null || elderBoss.isDie()) {
            Service.gI().sendThongBao(player, "Trưởng Lão chưa xuất hiện hoặc đã bị hạ.");
            return;
        }
        if (elderHealUsed >= ConstClanNamekWar.DEFENDER_HEAL_MAX_TIMES) {
            Service.gI().sendThongBao(player, "Phe thủ đã dùng tối đa số lần hồi máu.");
            return;
        }
        int nextNeed = (elderHealUsed + 1) * ConstClanNamekWar.DEFENDER_HEAL_KILL_REQUIRE;
        if (defenderKillCount < nextNeed) {
            Service.gI().sendThongBao(player, "Cần hạ thêm " + (nextNeed - defenderKillCount)
                    + " phe công để hồi máu Trưởng Lão.");
            return;
        }
        long missingHp = elderBoss.nPoint.hpMax - elderBoss.nPoint.hp;
        if (missingHp <= 0) {
            Service.gI().sendThongBao(player, "Máu Trưởng Lão đang đầy.");
            return;
        }
        long healHp = elderBoss.nPoint.hpMax * ConstClanNamekWar.DEFENDER_HEAL_PERCENT / 100;
        healHp = Math.min(healHp, missingHp);
        elderHealUsed++;
        PlayerService.gI().hoiPhuc(elderBoss, healHp, 0);
        sendMatchMessage(player.name + " đã hồi " + Util.chiaNho(healHp)
                + " máu cho Trưởng Lão Namek (" + elderHealUsed + "/"
                + ConstClanNamekWar.DEFENDER_HEAL_MAX_TIMES + ").");
    }

    public synchronized boolean tryPickDragonEnergy(Player player, ItemMap itemMap) {
        if (itemMap == null || dragonEnergy == null || itemMap.itemMapId != dragonEnergy.itemMapId
                || itemMap.zone != middleZone) {
            return false;
        }
        if (this.state != State.RUNNING || player == null || !isParticipant(player)) {
            if (player != null) {
                Service.gI().sendThongBao(player, "Bạn không thuộc trận đang tranh Năng lượng rồng thiêng.");
            }
            return true;
        }
        boolean attackerTeam = isAttacker(player);
        if (!attackerTeam && !isDefender(player)) {
            Service.gI().sendThongBao(player, "Bạn chưa thuộc phe nào trong trận này.");
            return true;
        }
        ClanNamekWarService.gI().grantControlImmunity(getTeamPlayers(attackerTeam),
                ConstClanNamekWar.DRAGON_ENERGY_IMMUNE_MS);
        removeDragonEnergy();
        nextDragonEnergySpawnAt = System.currentTimeMillis() + ConstClanNamekWar.DRAGON_ENERGY_RESPAWN_MS;
        sendMatchMessage((attackerTeam ? "Phe công" : "Phe thủ")
                + " đã hấp thụ Năng lượng rồng thiêng, toàn phe miễn khống chế trong "
                + (ConstClanNamekWar.DRAGON_ENERGY_IMMUNE_MS / 1000) + " giây.");
        return true;
    }

    public boolean isTurnTimeout() {
        return forceTurnEnd || System.currentTimeMillis() - turnStartTime >= ConstClanNamekWar.TURN_SECONDS * 1000L;
    }

    public boolean hasNextTurn() {
        return turn < maxTurns;
    }

    public void finishTurn() {
        removeElder();
        removeShieldPillars();
        removeDragonEnergy();
        forceTurnEnd = false;
        planetQuakeEndAt = 0;
        sendMatchMessage("Kết thúc lượt " + turn + ". Sát thương " + getClanName(clanA) + ": " + damageClanA
                + " - " + getClanName(clanB) + ": " + damageClanB + ".");
    }

    public Clan getWinner() {
        if (clanB == null) {
            return clanA;
        }
        if (damageClanA > damageClanB) {
            return clanA;
        }
        if (damageClanB > damageClanA) {
            return clanB;
        }
        return null;
    }

    public void finishMatch() {
        this.state = State.FINISHED;
        removeElder();
        removeShieldPillars();
        removeDragonEnergy();
        Clan winner = getWinner();
        if (winner == null) {
            sendMatchMessage("Trận đấu hòa.");
        } else {
            sendMatchMessage("Bang " + winner.name + " chiến thắng.");
        }
        rewardParticipants(winner);
    }

    public void dispose() {
        this.state = State.FINISHED;
        this.forceTurnEnd = false;
        removeElder();
        removeShieldPillars();
        removeDragonEnergy();
    }

    public void forceDefenderWin(Player requester) {
        if (requester == null || !isDefender(requester) || this.state != State.RUNNING) {
            return;
        }
        this.forceTurnEnd = true;
        sendMatchMessage("Admin " + requester.name + " đã kết thúc nhanh lượt cho phe thủ.");
    }

    public void kickAllToRegisterMap() {
        kickPlayers(participantsA);
        kickPlayers(participantsB);
    }

    private void kickPlayers(List<Player> players) {
        for (Player player : players) {
            if (player == null || player.zone == null) {
                continue;
            }
            if (player instanceof Bot) {
                BotManager.gI().bot.remove((Bot) player);
                ChangeMapService.gI().exitMap(player);
                continue;
            }
            Service.gI().changeFlag(player, (byte) 0);
            ChangeMapService.gI().changeMapNonSpaceship(player, ConstClanNamekWar.REGISTRATION_MAP_ID,
                    player.location.x, player.location.y);
        }
    }

    private void sendMatchMessage(String text) {
        sendPlayers(participantsA, text);
        sendPlayers(participantsB, text);
    }

    private void sendPlayers(List<Player> players, String text) {
        for (Player player : players) {
            if (player != null) {
                Service.gI().sendThongBao(player, text);
            }
        }
    }

    private void sendRoleInstructions(List<Player> players, boolean isAttacker) {
        for (Player player : players) {
            if (player == null || !player.isPl()) {
                continue;
            }
            if (isAttacker) {
                NpcService.gI().createTutorial(player, -1,
                        "Bạn là PHE CÔNG\n"
                        + "- Xuất phát tại map " + ConstClanNamekWar.ATTACK_MAP_ID + "\n"
                        + "- Sang map " + ConstClanNamekWar.MIDDLE_MAP_ID + " phá đủ 2 trụ lá chắn\n"
                        + "- Sau đó vào map " + ConstClanNamekWar.DEFENSE_MAP_ID + " hạ Trưởng Lão trong "
                        + (ConstClanNamekWar.TURN_SECONDS / 60) + " phút.");
                Service.gI().sendThongBao(player,
                        "PHE CÔNG: Phá 2 trụ ở map " + ConstClanNamekWar.MIDDLE_MAP_ID
                        + " để mở đường vào map " + ConstClanNamekWar.DEFENSE_MAP_ID + ".");
            } else {
                String text = "Bạn là PHE THỦ\n"
                        + "- Bảo vệ Trưởng Lão tại map " + ConstClanNamekWar.DEFENSE_MAP_ID + " trong "
                        + (ConstClanNamekWar.TURN_SECONDS / 60) + " phút\n"
                        + "- Có thể sang map " + ConstClanNamekWar.MIDDLE_MAP_ID + " để chặn phe công\n"
                        + "- Không thể đánh Trưởng Lão và trụ lá chắn.";
                if (player.isAdmin()) {
                    text += "\n- Admin test nhanh: nhập 'thangthu' để kết thúc ngay lượt thủ.";
                }
                NpcService.gI().createTutorial(player, -1, text);
                Service.gI().sendThongBao(player,
                        "PHE THỦ: Giữ Trưởng Lão sống hết " + (ConstClanNamekWar.TURN_SECONDS / 60)
                        + " phút để thắng lượt này.");
            }
        }
    }

    private void removeElder() {
        pendingSpawnElderAt = 0;
        if (elderBoss != null) {
            elderBoss.setRuntimeDisabled(true);
            elderBoss = null;
        }
    }

    private void spawnShieldPillars() {
        removeShieldPillars();
        if (middleZone == null) {
            return;
        }
        for (int i = 0; i < ConstClanNamekWar.SHIELD_PILLAR_X.length; i++) {
            try {
                NamekShieldPillarBoss pillar = new NamekShieldPillarBoss(this, i, middleZone);
                shieldPillars.add(pillar);
                pillar.changeStatus(boss.BossStatus.RESPAWN);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        sendMatchMessage("Phe công cần phá " + ConstClanNamekWar.SHIELD_PILLAR_X.length
                + " trụ lá chắn tại map giữa để đánh Trưởng Lão.");
    }

    private void removeShieldPillars() {
        for (NamekShieldPillarBoss pillar : new ArrayList<>(shieldPillars)) {
            if (pillar != null) {
                pillar.setRuntimeDisabled(true);
            }
        }
        shieldPillars.clear();
    }

    private void updateDragonEnergy(long now) {
        if (dragonEnergy != null) {
            if (dragonEnergy.zone == null || now >= dragonEnergyExpireAt) {
                removeDragonEnergy();
                nextDragonEnergySpawnAt = now + ConstClanNamekWar.DRAGON_ENERGY_RESPAWN_MS;
                sendMatchMessage("Năng lượng rồng thiêng đã tan biến khỏi map giữa.");
            }
            return;
        }
        if (middleZone != null && nextDragonEnergySpawnAt > 0 && now >= nextDragonEnergySpawnAt) {
            spawnDragonEnergy(now);
        }
    }

    private void spawnDragonEnergy(long now) {
        int minX = 120;
        int maxX = Math.max(minX, middleZone.map.mapWidth - 120);
        int x = Util.nextInt(minX, maxX);
        int y = middleZone.map.yPhysicInTop(x, 100);
        if (y <= 0) {
            y = ConstClanNamekWar.SHIELD_PILLAR_Y[0];
        }
        dragonEnergy = new ItemMap(middleZone, ConstClanNamekWar.DRAGON_ENERGY_ITEM_ID, 1, x, y, -1);
        dragonEnergyExpireAt = now + ConstClanNamekWar.DRAGON_ENERGY_LIFE_MS;
        nextDragonEnergySpawnAt = 0;
        Service.gI().dropItemMap(middleZone, dragonEnergy);
        sendMatchMessage("Năng lượng rồng thiêng xuất hiện ở map giữa. Phe nào nhặt được sẽ miễn khống chế "
                + (ConstClanNamekWar.DRAGON_ENERGY_IMMUNE_MS / 1000) + " giây.");
    }

    private void removeDragonEnergy() {
        if (dragonEnergy != null) {
            try {
                if (dragonEnergy.zone != null) {
                    ItemMapService.gI().removeItemMapAndSendClient(dragonEnergy);
                }
            } catch (Exception ignored) {
            }
            dragonEnergy = null;
        }
        dragonEnergyExpireAt = 0;
    }

    private void updatePlanetQuake(long now) {
        if (nextPlanetQuakeAt > 0 && now >= nextPlanetQuakeAt) {
            planetQuakeEndAt = now + ConstClanNamekWar.PLANET_QUAKE_DURATION_MS;
            lastPlanetQuakeTick = 0;
            nextPlanetQuakeAt = now + ConstClanNamekWar.PLANET_QUAKE_INTERVAL_MS;
            sendMatchMessage("Sóng địa chấn hành tinh bắt đầu, mặt đất rung chuyển trong "
                    + (ConstClanNamekWar.PLANET_QUAKE_DURATION_MS / 1000) + " giây.");
        }
        if (planetQuakeEndAt <= 0) {
            return;
        }
        if (now >= planetQuakeEndAt) {
            planetQuakeEndAt = 0;
            return;
        }
        if (now - lastPlanetQuakeTick >= ConstClanNamekWar.PLANET_QUAKE_TICK_MS) {
            lastPlanetQuakeTick = now;
            shakePlayers(participantsA);
            shakePlayers(participantsB);
        }
    }

    private void shakePlayers(List<Player> players) {
        for (Player player : players) {
            if (player == null || !player.isPl() || player.zone == null || player.location == null || player.isDie()
                    || !ConstClanNamekWar.isWarMap(player.zone.map.mapId)) {
                continue;
            }
            int range = ConstClanNamekWar.PLANET_QUAKE_MOVE_RANGE;
            int x = player.location.x + Util.nextInt(-range, range);
            int minX = 40;
            int maxX = Math.max(minX, player.zone.map.mapWidth - 40);
            x = Math.max(minX, Math.min(maxX, x));
            Service.gI().setPos(player, x, player.location.y);
        }
    }

    private List<Player> getTeamPlayers(boolean attackerTeam) {
        byte flag = attackerTeam ? ConstClanNamekWar.ATTACKER_FLAG : ConstClanNamekWar.DEFENDER_FLAG;
        List<Player> result = new ArrayList<>();
        addTeamPlayers(result, participantsA, flag);
        addTeamPlayers(result, participantsB, flag);
        return result;
    }

    private void addTeamPlayers(List<Player> result, List<Player> source, byte flag) {
        for (Player player : source) {
            if (player != null && player.cFlag == flag) {
                result.add(player);
            }
        }
    }

    private void sendTeamMessage(boolean attackerTeam, String text) {
        sendPlayers(getTeamPlayers(attackerTeam), text);
    }

    private void ensureDefenseSupportNpc() {
        if (defenseZone == null || defenseZone.map == null) {
            return;
        }
        if (defenseZone.map.npcs == null) {
            defenseZone.map.npcs = new ArrayList<>();
        }
        synchronized (defenseZone.map.npcs) {
            for (Npc npc : defenseZone.map.npcs) {
                if (npc != null && npc.tempId == ConstNpc.TRUONG_LAO_GURU) {
                    return;
                }
            }
            Npc npc = NpcFactory.createNPC(ConstClanNamekWar.DEFENSE_MAP_ID, 1,
                    ConstClanNamekWar.DEFENDER_SPAWN_X + 80, ConstClanNamekWar.DEFENDER_SPAWN_Y,
                    ConstNpc.TRUONG_LAO_GURU);
            if (npc != null) {
                defenseZone.map.npcs.add(npc);
            }
        }
    }

    private void rewardParticipants(Clan winner) {
        if (round == 0) {
            return;
        }
        if (winner == null) {
            rewardPlayers(participantsA, ConstClanNamekWar.REWARD_DRAW);
            rewardPlayers(participantsB, ConstClanNamekWar.REWARD_DRAW);
            return;
        }
        rewardPlayers(participantsA, winner == clanA ? ConstClanNamekWar.REWARD_WINNER : ConstClanNamekWar.REWARD_LOSER);
        rewardPlayers(participantsB, winner == clanB ? ConstClanNamekWar.REWARD_WINNER : ConstClanNamekWar.REWARD_LOSER);
    }

    private void rewardPlayers(List<Player> players, int rewardType) {
        for (Player player : players) {
            if (player == null || player instanceof Bot) {
                continue;
            }
            if (ClanNamekWarDAO.giveRewardToPlayer(player, rewardType)) {
                Service.gI().sendThongBao(player, "Phần thưởng Bảo Vệ Trưởng Lão Namek đã được gửi vào hòm thư.");
            }
        }
    }

    private String getClanName(Clan clan) {
        return clan == null ? "Hệ thống" : clan.name;
    }
}
