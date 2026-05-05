package consts;

public class ConstClanNamekWar {

    public static final String EVENT_NAME = "Bảo Vệ Trưởng Lão Namek";

    // TODO: dien map id sau khi them map vao data/sql.
    public static final int REGISTRATION_MAP_ID = 13;
    public static final int DEFENSE_MAP_ID = 174;
    public static final int MIDDLE_MAP_ID = 179;
    public static final int ATTACK_MAP_ID = 180;

    public static final int MIN_CLAN_MEMBERS_REGISTER = 5;
    public static final int MAX_PLAYERS_PER_CLAN = 20;
    public static final int TURN_SECONDS = 10 * 60;
    public static final int RESPAWN_DELAY_MS = 3_000;

    public static final byte DEFENDER_FLAG = 1;
    public static final byte ATTACKER_FLAG = 2;

    public static final int DEFENDER_SPAWN_X = 127;
    public static final int DEFENDER_SPAWN_Y = 408;
    public static final int ATTACKER_SPAWN_X = 1453;
    public static final int ATTACKER_SPAWN_Y = 384;
    public static final int ELDER_SPAWN_X = 890;
    public static final int ELDER_SPAWN_Y = 432;
    public static final int[] SHIELD_PILLAR_X = new int[]{350, 935};
    public static final int[] SHIELD_PILLAR_Y = new int[]{384, 384};

    public static final long ELDER_HP = 2_000_000_000L;
    public static final boolean ELDER_DAMAGE_CAP_ENABLED = false;
    public static final int ELDER_DAMAGE_CAP_PERCENT = 1;
    public static final long ELDER_DAMAGE_MIN_CAP = 200_000L;
    public static final long SHIELD_PILLAR_HP = 200_000_000L;
    public static final int DEFENSE_GATE_OPEN_MS = 90_000;

    public static final int REWARD_WINNER = 1;
    public static final int REWARD_LOSER = 2;
    public static final int REWARD_DRAW = 3;

    public static final int CONTROL_DURATION_PERCENT = 50;
    public static final int CONTROL_MAX_TIME_MS = 3_000;
    public static final int CONTROL_IMMUNE_MS = 7_000;

    public static final int DEFENDER_HEAL_KILL_REQUIRE = 20;
    public static final int DEFENDER_HEAL_PERCENT = 15;
    public static final int DEFENDER_HEAL_MAX_TIMES = 2;

    public static final int DRAGON_ENERGY_ITEM_ID = 14;
    public static final int DRAGON_ENERGY_SPAWN_DELAY_MS = 15_000;
    public static final int DRAGON_ENERGY_RESPAWN_MS = 90_000;
    public static final int DRAGON_ENERGY_LIFE_MS = 30_000;
    public static final int DRAGON_ENERGY_IMMUNE_MS = 5_000;

    public static final int PLANET_QUAKE_INTERVAL_MS = 60_000;
    public static final int PLANET_QUAKE_DURATION_MS = 10_000;
    public static final int PLANET_QUAKE_TICK_MS = 800;
    public static final int PLANET_QUAKE_MOVE_RANGE = 4;

    private ConstClanNamekWar() {
    }

    public static boolean isConfigured() {
        return REGISTRATION_MAP_ID >= 0 && DEFENSE_MAP_ID >= 0 && MIDDLE_MAP_ID >= 0 && ATTACK_MAP_ID >= 0;
    }

    public static boolean isRegistrationMap(int mapId) {
        return mapId == REGISTRATION_MAP_ID;
    }

    public static boolean isWarMap(int mapId) {
        return mapId == DEFENSE_MAP_ID || mapId == MIDDLE_MAP_ID || mapId == ATTACK_MAP_ID;
    }

    public static long capElderDamage(long currentHp, long originalDamage) {
        if (!ELDER_DAMAGE_CAP_ENABLED) {
            return originalDamage;
        }
        if (originalDamage <= 0 || currentHp <= 0) {
            return 0;
        }
        long cap = currentHp * ELDER_DAMAGE_CAP_PERCENT / 100;
        cap = Math.max(cap, ELDER_DAMAGE_MIN_CAP);
        return Math.min(originalDamage, cap);
    }
}
