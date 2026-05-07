package boss;

/*
 *
 *
 * @author CongHoan
 */
import HoandzManager.Functions;
import boss.boss_manifest.AnTrom.AnTrom;
import boss.boss_manifest.AnTrom.AnTromTV;
import boss.boss_manifest.Black.BlackGoku;
import boss.boss_manifest.Nappa.Rambo;
import boss.boss_manifest.Nappa.MapDauDinh;
import boss.boss_manifest.Nappa.Kuku;
import boss.boss_manifest.Android.Android19;
import boss.boss_manifest.Android.Pic;
import boss.boss_manifest.Android.Android14;
import boss.boss_manifest.Android.Poc;
import boss.boss_manifest.Android.Android13;
import boss.boss_manifest.Android.KingKong;
import boss.boss_manifest.Android.DrKore;
import boss.boss_manifest.Android.Android15;
import boss.boss_manifest.GoldenFrieza.DeathBeam1;
import boss.boss_manifest.GoldenFrieza.DeathBeam2;
import boss.boss_manifest.GoldenFrieza.DeathBeam3;
import boss.boss_manifest.GoldenFrieza.DeathBeam4;
import boss.boss_manifest.GoldenFrieza.DeathBeam5;
import boss.boss_manifest.GoldenFrieza.GoldenFrieza;
import boss.boss_manifest.Cooler.Cooler;
import boss.boss_manifest.Cell.SieuBoHung;
import boss.boss_manifest.Cell.XenBoHung;
//import boss.boss_manifest.BrolyFix.Broly;
//import boss.boss_manifest.BrolyFix.BrolySuper;
import boss.boss_manifest.Broly.Broly;
import boss.boss_manifest.Broly.SuperBroly;
import boss.boss_manifest.ChristmasEvent.OngGiaNoel;
import boss.boss_manifest.TaoPaiPai.TaoPaiPai;
import boss.boss_manifest.Frieza.Fide;
import boss.boss_manifest.HungVuongEvent.SonTinh;
import boss.boss_manifest.HungVuongEvent.ThuyTinh;
import boss.boss_manifest.HalloweenEvent.BiMa;
import boss.boss_manifest.HalloweenEvent.Doi;
import boss.boss_manifest.HalloweenEvent.MaTroi;
import boss.boss_manifest.TrungThuEvent.KhiDot;
import boss.boss_manifest.TrungThuEvent.NguyetThan;
import boss.boss_manifest.TrungThuEvent.NhatThan;
import boss.boss_manifest.MajinBuu12H.Mabu;
import boss.boss_manifest.MajinBuu12H.BuiBui;
import boss.boss_manifest.MajinBuu12H.BuiBui2;
import boss.boss_manifest.MajinBuu12H.Cadic;
import boss.boss_manifest.MajinBuu12H.Drabura;
import boss.boss_manifest.MajinBuu12H.Drabura2;
import boss.boss_manifest.MajinBuu12H.Drabura3;
import boss.boss_manifest.MajinBuu12H.Goku;
import boss.boss_manifest.MajinBuu12H.Yacon;
import boss.boss_manifest.MajinBuu14H.Mabu2H;
import boss.boss_manifest.MajinBuu14H.SuperBu;
import boss.boss_manifest.GinyuForce.SO1;
import boss.boss_manifest.GinyuForce.SO2;
import boss.boss_manifest.GinyuForce.SO3;
import boss.boss_manifest.GinyuForce.SO4;
import boss.boss_manifest.GinyuForce.TDT;
import boss.boss_manifest.NamekGinyuForce.SO1_NM;
import boss.boss_manifest.NamekGinyuForce.SO2_NM;
import boss.boss_manifest.NamekGinyuForce.SO3_NM;
import boss.boss_manifest.NamekGinyuForce.SO4_NM;
import boss.boss_manifest.NamekGinyuForce.TDT_NM;
import boss.boss_manifest.Earth.BIDO;
import boss.boss_manifest.Earth.BOJACK;
import boss.boss_manifest.Earth.BUJIN;
import boss.boss_manifest.Earth.KOGU;
import boss.boss_manifest.Earth.SUPER_BOJACK;
import boss.boss_manifest.Earth.ZANGYA;
import boss.boss_manifest.Yardart.CHIENBINH0;
import boss.boss_manifest.Yardart.CHIENBINH1;
import boss.boss_manifest.Yardart.CHIENBINH2;
import boss.boss_manifest.Yardart.CHIENBINH3;
import boss.boss_manifest.Yardart.CHIENBINH4;
import boss.boss_manifest.Yardart.CHIENBINH5;
import boss.boss_manifest.Yardart.DOITRUONG5;
import boss.boss_manifest.Yardart.TANBINH0;
import boss.boss_manifest.Yardart.TANBINH1;
import boss.boss_manifest.Yardart.TANBINH2;
import boss.boss_manifest.Yardart.TANBINH3;
import boss.boss_manifest.Yardart.TANBINH4;
import boss.boss_manifest.Yardart.TANBINH5;
import boss.boss_manifest.Yardart.TAPSU0;
import boss.boss_manifest.Yardart.TAPSU1;
import boss.boss_manifest.Yardart.TAPSU2;
import boss.boss_manifest.Yardart.TAPSU3;
import boss.boss_manifest.Yardart.TAPSU4;
import boss.boss_manifest.Cell.XENCON1;
import boss.boss_manifest.Cell.XENCON2;
import boss.boss_manifest.Cell.XENCON3;
import boss.boss_manifest.Cell.XENCON4;
import boss.boss_manifest.Cell.XENCON5;
import boss.boss_manifest.Cell.XENCON6;
import boss.boss_manifest.Cell.XENCON7;
import boss.boss_manifest.Cumber.Cumber;
import boss.boss_manifest.DaiTuongBroly.DaiTuongBroly;
import boss.boss_manifest.Hatchiyac.Hatchiyac;
import boss.boss_manifest.Doraemon.*;
import boss.boss_manifest.BrolySsj.*;
import boss.boss_manifest.Cumber.Nyasu;
import boss.boss_manifest.Wolves.*;
import boss.boss_manifest.LunarNewYearEvent.Bena;
import boss.boss_manifest.LunarNewYearEvent.LanCon;
import player.Player;
import network.Message;
import services.MapService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import jdbc.DBConnecter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import map.Zone;
import server.Maintenance;
import services.func.ChangeMapService;
import utils.Logger;
import boss.boss_manifest.DaiTuongBroly.DaiTuongBroly;
import boss.boss_manifest.Hatchiyac.Hatchiyac;
import boss.boss_manifest.Doraemon.*;
import boss.boss_manifest.BrolySsj.*;
import boss.boss_manifest.Cumber.Jessie;
import boss.boss_manifest.Wolves.*;
import boss.boss_manifest.LunarNewYearEvent.Bena;
import boss.boss_manifest.LunarNewYearEvent.LanCon;
import player.Player;
import network.Message;
import services.MapService;
import java.util.ArrayList;
import java.util.List;
import map.Zone;
import server.Maintenance;
import utils.Logger;
import boss.boss_manifest.DaiTuongBroly.DaiTuongBroly;
import boss.boss_manifest.Hatchiyac.Hatchiyac;
import boss.boss_manifest.Doraemon.*;
import boss.boss_manifest.BrolySsj.*;
import boss.boss_manifest.Cumber.Nyasu;
import boss.boss_manifest.Wolves.*;
import boss.boss_manifest.LunarNewYearEvent.Bena;
import boss.boss_manifest.LunarNewYearEvent.LanCon;
import player.Player;
import network.Message;
import services.MapService;
import java.util.ArrayList;
import java.util.List;
import map.Zone;
import server.Maintenance;
import utils.Logger;
import boss.boss_manifest.DaiTuongBroly.DaiTuongBroly;
import boss.boss_manifest.Hatchiyac.Hatchiyac;
import boss.boss_manifest.Doraemon.*;
import boss.boss_manifest.BrolySsj.*;
import boss.boss_manifest.Cumber.James;
import boss.boss_manifest.Wolves.*;
import boss.boss_manifest.LunarNewYearEvent.Bena;
import boss.boss_manifest.LunarNewYearEvent.LanCon;
import player.Player;
import network.Message;
import services.MapService;
import java.util.ArrayList;
import java.util.List;
import map.Zone;
import server.Maintenance;
import utils.Logger;
import boss.boss_manifest.DaiTuongBroly.DaiTuongBroly;
import boss.boss_manifest.Hatchiyac.Hatchiyac;
import boss.boss_manifest.Doraemon.*;
import boss.boss_manifest.BrolySsj.*;
import boss.boss_manifest.Cumber.Nyasu;
import boss.boss_manifest.Wolves.*;
import boss.boss_manifest.LunarNewYearEvent.Bena;
import boss.boss_manifest.LunarNewYearEvent.LanCon;
import player.Player;
import network.Message;
import services.MapService;
import java.util.ArrayList;
import java.util.List;
import map.Zone;
import server.Maintenance;
import utils.Logger;
import boss.boss_manifest.DaiTuongBroly.DaiTuongBroly;
import boss.boss_manifest.Hatchiyac.Hatchiyac;
import boss.boss_manifest.Doraemon.*;
import boss.boss_manifest.BrolySsj.*;
import boss.boss_manifest.Cumber.Jessie;
import boss.boss_manifest.Wolves.*;
import boss.boss_manifest.LunarNewYearEvent.Bena;
import boss.boss_manifest.LunarNewYearEvent.LanCon;
import player.Player;
import network.Message;
import services.MapService;
import java.util.ArrayList;
import java.util.List;
import map.Zone;
import server.Maintenance;
import utils.Logger;
import boss.boss_manifest.DaiTuongBroly.DaiTuongBroly;
import boss.boss_manifest.Hatchiyac.Hatchiyac;
import boss.boss_manifest.Doraemon.*;
import boss.boss_manifest.BrolySsj.*;
import boss.boss_manifest.Cumber.Nyasu;
import boss.boss_manifest.Wolves.*;
import boss.boss_manifest.LunarNewYearEvent.Bena;
import boss.boss_manifest.LunarNewYearEvent.LanCon;
import player.Player;
import network.Message;
import services.MapService;
import java.util.ArrayList;
import java.util.List;
import map.Zone;
import server.Maintenance;
import utils.Logger;
import boss.boss_manifest.DaiTuongBroly.DaiTuongBroly;
import boss.boss_manifest.Hatchiyac.Hatchiyac;
import boss.boss_manifest.Doraemon.*;
import boss.boss_manifest.BrolySsj.*;
import boss.boss_manifest.Wolves.*;
import boss.boss_manifest.LunarNewYearEvent.Bena;
import boss.boss_manifest.LunarNewYearEvent.LanCon;
import player.Player;
import network.Message;
import services.MapService;
import java.util.ArrayList;
import java.util.List;
import map.Zone;
import server.Maintenance;
import utils.Logger;



public class BossManager implements Runnable {

    private static BossManager instance;
    public static byte ratioReward = 10;

    public static BossManager gI() {
        if (instance == null) {
            instance = new BossManager();
        }
        return instance;
    }

    public List<Boss> getBosses() {

        return this.bosses;

    }

    public BossManager() {
        this.bosses = new ArrayList<>();
    }

    protected final List<Boss> bosses;
    private static final Map<Integer, BossData> RUNTIME_CUSTOM_TEMPLATES = new HashMap<>();
    private static final Map<String, JSONObject> RUNTIME_TEMPLATE_OVERRIDES = new ConcurrentHashMap<>();
    private static final String RUNTIME_CONFIG_TABLE = "boss_runtime_config";
    private static final String TEMPLATE_CONFIG_TABLE = "boss_template_config";
    private static final String SPAWN_RULE_TABLE = "boss_spawn_rule";
    private static boolean runtimeConfigsApplied;

    public void addBoss(Boss boss) {
        synchronized (this.bosses) {
            this.bosses.add(boss);
        }
    }

    public void removeBoss(Boss boss) {
        synchronized (this.bosses) {
            this.bosses.remove(boss);
        }
    }

    public static synchronized String runtimeBossesJson() {
        StringBuilder json = new StringBuilder();
        json.append("{\"bosses\":[");
        boolean first = true;
        for (BossManager manager : runtimeManagers()) {
            String managerKey = runtimeManagerKey(manager);
            List<Boss> snapshot = runtimeBossSnapshot(manager);
            for (int i = 0; i < snapshot.size(); i++) {
                Boss boss = snapshot.get(i);
                if (boss == null) {
                    continue;
                }
                if (!first) {
                    json.append(',');
                }
                first = false;
                json.append(runtimeBossJson(managerKey, i, boss));
            }
        }
        json.append("],\"catalog\":").append(runtimeBossCatalogJson()).append('}');
        return json.toString();
    }

    public static synchronized String runtimeCreateBoss(int bossId, int count) {
        return runtimeCreateBoss(bossId, count, true);
    }

    private static String runtimeCreateBoss(int bossId, int count, boolean persist) {
        int total = Math.max(1, Math.min(count, 50));
        StringBuilder created = new StringBuilder("[");
        boolean first = true;
        int createdCount = 0;
        for (int i = 0; i < total; i++) {
            Boss boss = BossManager.gI().createBoss(bossId);
            if (boss == null) {
                continue;
            }
            RuntimeBossRef ref = findRuntimeBossRef(boss);
            if (ref == null) {
                continue;
            }
            if (!first) {
                created.append(',');
            }
            first = false;
            createdCount++;
            created.append(runtimeBossJson(ref.managerKey, ref.index, boss));
        }
        created.append(']');
        if (persist && createdCount > 0) {
            persistBossSpawn(bossId, createdCount);
        }
        return "{\"created\":" + created + "}";
    }

    public static synchronized String runtimeCreateCustomBoss(JSONObject payload) {
        return runtimeCreateCustomBoss(payload, true);
    }

    private static String runtimeCreateCustomBoss(JSONObject payload, boolean persist) {
        int bossId = intValue(payload.get("boss_id"), 0);
        if (bossId == 0) {
            return "{\"created\":[]}";
        }

        JSONArray groupMembers = payload.get("group_members") instanceof JSONArray
                ? (JSONArray) payload.get("group_members")
                : new JSONArray();
        int[] childIds = new int[groupMembers.size()];
        for (int i = 0; i < groupMembers.size(); i++) {
            JSONObject childPayload = groupMembers.get(i) instanceof JSONObject
                    ? (JSONObject) groupMembers.get(i)
                    : new JSONObject();
            int childId = intValue(childPayload.get("boss_id"), bossId - i - 1);
            childIds[i] = childId;
            childPayload.put("boss_id", childId);
            if (!childPayload.containsKey("type_appear")) {
                childPayload.put("type_appear", "APPEAR_WITH_ANOTHER");
            }
            RUNTIME_CUSTOM_TEMPLATES.put(childId, runtimeBossDataFromPayload(childPayload, null, null));
        }

        BossData parentData = runtimeBossDataFromPayload(payload, null, childIds.length > 0 ? childIds : null);
        RUNTIME_CUSTOM_TEMPLATES.put(bossId, parentData);

        int total = Math.max(1, Math.min(intValue(payload.get("count"), 1), 50));
        StringBuilder created = new StringBuilder("[");
        boolean first = true;
        int createdCount = 0;
        for (int i = 0; i < total; i++) {
            Boss boss = BossManager.gI().createBoss(bossId);
            RuntimeBossRef ref = boss == null ? null : findRuntimeBossRef(boss);
            if (ref == null) {
                continue;
            }
            if (!first) {
                created.append(',');
            }
            first = false;
            createdCount++;
            created.append(runtimeBossJson(ref.managerKey, ref.index, boss));
        }
        created.append(']');
        if (persist && createdCount > 0) {
            persistBossCustom(payload, bossId, createdCount);
        }
        return "{\"created\":" + created + "}";
    }

    public static synchronized boolean runtimeSetEnabled(String managerKey, int index, boolean enabled) {
        Boss boss = runtimeBoss(managerKey, index);
        if (boss == null) {
            return false;
        }
        boss.setRuntimeDisabled(!enabled);
        persistBossState(managerKey, index, boss, enabled, false);
        return true;
    }

    public static synchronized boolean runtimeSetGroupEnabled(String managerKey, int index, boolean enabled) {
        Boss boss = runtimeBoss(managerKey, index);
        if (boss == null) {
            return false;
        }
        for (Boss member : runtimeGroupMembers(runtimeGroupRoot(boss))) {
            member.setRuntimeDisabled(!enabled);
            RuntimeBossRef ref = findRuntimeBossRef(member);
            if (ref != null) {
                persistBossState(ref.managerKey, ref.index, member, enabled, false);
            }
        }
        return true;
    }

    public static synchronized boolean runtimeDeleteBoss(String managerKey, int index) {
        BossManager manager = runtimeManager(managerKey);
        Boss boss = runtimeBoss(managerKey, index);
        if (manager == null || boss == null) {
            return false;
        }
        boss.setRuntimeDisabled(true);
        persistBossState(managerKey, index, boss, false, true);
        manager.removeBoss(boss);
        return true;
    }

    public static synchronized boolean runtimeDeleteGroup(String managerKey, int index) {
        Boss boss = runtimeBoss(managerKey, index);
        if (boss == null) {
            return false;
        }
        List<Boss> members = runtimeGroupMembers(runtimeGroupRoot(boss));
        for (Boss member : members) {
            member.setRuntimeDisabled(true);
            RuntimeBossRef ref = findRuntimeBossRef(member);
            if (ref != null) {
                persistBossState(ref.managerKey, ref.index, member, false, true);
            }
        }
        for (Boss member : members) {
            RuntimeBossRef ref = findRuntimeBossRef(member);
            BossManager manager = ref == null ? null : runtimeManager(ref.managerKey);
            if (manager != null) {
                manager.removeBoss(member);
            }
        }
        return true;
    }

    public static synchronized boolean runtimeRespawnBoss(String managerKey, int index) {
        Boss boss = runtimeBoss(managerKey, index);
        if (boss == null) {
            return false;
        }
        boss.runtimeDisabled = false;
        try {
            if (boss.zone != null) {
                ChangeMapService.gI().exitMap(boss);
                boss.zone = null;
            }
        } catch (Exception ignored) {
        }
        boss.changeStatus(BossStatus.RESPAWN);
        return true;
    }

    public static synchronized boolean runtimeRespawnGroup(String managerKey, int index) {
        Boss boss = runtimeBoss(managerKey, index);
        if (boss == null) {
            return false;
        }
        Boss root = runtimeGroupRoot(boss);
        for (Boss member : runtimeGroupMembers(root)) {
            member.runtimeDisabled = false;
            try {
                if (member.zone != null) {
                    ChangeMapService.gI().exitMap(member);
                }
            } catch (Exception ignored) {
            }
            member.zone = null;
            member.lastZone = null;
            member.changeStatus(BossStatus.REST);
        }
        root.lastTimeRest = 0;
        root.changeStatus(BossStatus.RESPAWN);
        return true;
    }

    public static synchronized boolean runtimeUpdateBoss(JSONObject payload) {
        return runtimeUpdateBoss(payload, true);
    }

    private static boolean runtimeUpdateBoss(JSONObject payload, boolean persist) {
        String managerKey = stringValue(payload.get("manager"));
        int index = intValue(payload.get("index"), -1);
        Boss boss = runtimeBoss(managerKey, index);
        if (boss == null) {
            return false;
        }
        int targetLevel = intValue(payload.get("level_index"), Math.max(boss.currentLevel, 0));
        targetLevel = Math.max(0, Math.min(targetLevel, boss.data.length - 1));
        boolean editingCurrentLevel = targetLevel == Math.max(0, Math.min(Math.max(boss.currentLevel, 0), boss.data.length - 1));
        boolean hasTemplateChanges = hasBossTemplateChanges(payload);
        if (hasTemplateChanges) {
            applyBossTemplateUpdate((int) boss.id, targetLevel, payload);
            if (persist) {
                persistBossTemplateOverride((int) boss.id, targetLevel, payload);
            }
        }
        if (payload.containsKey("enabled")) {
            boss.setRuntimeDisabled(!boolValue(payload.get("enabled"), true));
        }
        if (payload.containsKey("hp") && boss.nPoint != null && editingCurrentLevel) {
            boss.nPoint.hp = Math.max(1, longValue(payload.get("hp"), boss.nPoint.hp));
        }
        if (payload.containsKey("status")) {
            try {
                boss.changeStatus(BossStatus.valueOf(stringValue(payload.get("status"))));
            } catch (Exception ignored) {
            }
        }
        if (persist && payload.containsKey("enabled")) {
            persistBossOverride(managerKey, index, boss, targetLevel, runtimeStatePayload(managerKey, index, targetLevel, !boss.runtimeDisabled), !boss.runtimeDisabled, false);
        }
        return true;
    }

    private static boolean hasBossTemplateChanges(JSONObject payload) {
        return payload.containsKey("name")
                || payload.containsKey("template_name")
                || payload.containsKey("gender")
                || payload.containsKey("outfit")
                || payload.containsKey("hp_max")
                || payload.containsKey("dame")
                || payload.containsKey("seconds_rest")
                || payload.containsKey("map_join")
                || payload.containsKey("skill_temp")
                || payload.containsKey("type_appear")
                || payload.containsKey("bosses_appear_together")
                || payload.containsKey("text_s")
                || payload.containsKey("text_m")
                || payload.containsKey("text_e");
    }

    private static JSONObject templatePayload(JSONObject source) {
        JSONObject payload = new JSONObject();
        copyIfPresent(source, payload, "name");
        copyIfPresent(source, payload, "template_name");
        copyIfPresent(source, payload, "gender");
        copyIfPresent(source, payload, "outfit");
        copyIfPresent(source, payload, "hp_max");
        copyIfPresent(source, payload, "dame");
        copyIfPresent(source, payload, "seconds_rest");
        copyIfPresent(source, payload, "map_join");
        copyIfPresent(source, payload, "skill_temp");
        copyIfPresent(source, payload, "type_appear");
        copyIfPresent(source, payload, "bosses_appear_together");
        copyIfPresent(source, payload, "text_s");
        copyIfPresent(source, payload, "text_m");
        copyIfPresent(source, payload, "text_e");
        return payload;
    }

    private static JSONObject runtimeStatePayload(String managerKey, int index, int level, boolean enabled) {
        JSONObject payload = new JSONObject();
        payload.put("manager", managerKey);
        payload.put("index", index);
        payload.put("level_index", level);
        payload.put("enabled", enabled);
        return payload;
    }

    private static void copyIfPresent(JSONObject source, JSONObject target, String key) {
        if (source != null && source.containsKey(key)) {
            target.put(key, source.get(key));
        }
    }

    private static void applyBossTemplateUpdate(int bossId, int level, JSONObject source) {
        JSONObject payload = templatePayload(source);
        if (payload.isEmpty()) {
            return;
        }
        rememberBossTemplateOverride(bossId, level, payload);
        for (BossManager manager : runtimeManagers()) {
            for (Boss member : runtimeBossSnapshot(manager)) {
                if (member != null && member.id == bossId) {
                    applyBossTemplatePayload(member, level, payload);
                }
            }
        }
    }

    private static void rememberBossTemplateOverride(int bossId, int level, JSONObject source) {
        JSONObject payload = templatePayload(source);
        if (payload.isEmpty()) {
            return;
        }
        String key = templateOverrideKey(bossId, level);
        JSONObject merged = RUNTIME_TEMPLATE_OVERRIDES.containsKey(key)
                ? copyJson(RUNTIME_TEMPLATE_OVERRIDES.get(key))
                : new JSONObject();
        merged.putAll(payload);
        merged.put("boss_id", bossId);
        merged.put("level_index", level);
        RUNTIME_TEMPLATE_OVERRIDES.put(key, merged);
    }

    private static String templateOverrideKey(int bossId, int level) {
        return bossId + ":" + Math.max(0, level);
    }

    private static void applyKnownTemplateOverrides(Boss boss) {
        if (boss == null || boss.data == null) {
            return;
        }
        int bossId = (int) boss.id;
        for (int level = 0; level < boss.data.length; level++) {
            JSONObject payload = RUNTIME_TEMPLATE_OVERRIDES.get(templateOverrideKey(bossId, level));
            if (payload != null) {
                applyBossTemplatePayload(boss, level, payload);
            }
        }
    }

    private static void applyBossTemplatePayload(Boss boss, int level, JSONObject payload) {
        if (boss == null || boss.data == null || level < 0 || level >= boss.data.length) {
            return;
        }
        BossData data = runtimeEditableBossData(boss, level);
        if (payload.containsKey("name")) {
            String name = stringValue(payload.get("name")).trim();
            if (!name.isEmpty()) {
                data.setName(name);
            }
        }
        if (payload.containsKey("template_name")) {
            String name = stringValue(payload.get("template_name")).trim();
            if (!name.isEmpty()) {
                data.setName(name);
            }
        }
        if (payload.containsKey("gender")) {
            data.setGender((byte) Math.max(0, Math.min(2, intValue(payload.get("gender"), data.getGender()))));
        }
        if (payload.containsKey("outfit")) {
            data.setOutfit(shortArrayValue(payload.get("outfit"), data.getOutfit(), 6));
        }
        if (payload.containsKey("map_join")) {
            data.setMapJoin(intArrayValue(payload.get("map_join"), data.getMapJoin()));
        }
        if (payload.containsKey("seconds_rest")) {
            data.setSecondsRest(Math.max(0, intValue(payload.get("seconds_rest"), data.getSecondsRest())));
        }
        if (payload.containsKey("skill_temp")) {
            data.setSkillTemp(skillArrayValue(payload.get("skill_temp"), data.getSkillTemp()));
        }
        if (payload.containsKey("type_appear")) {
            data.setTypeAppear(appearTypeValue(payload.get("type_appear"), data.getTypeAppear()));
        }
        if (payload.containsKey("bosses_appear_together")) {
            data.setBossesAppearTogether(intArrayValue(payload.get("bosses_appear_together"), data.getBossesAppearTogether()));
            rebuildBossAppearTogether(boss, level, data.getBossesAppearTogether());
        }
        if (payload.containsKey("text_s")) {
            data.setTextS(stringArrayValue(payload.get("text_s"), data.getTextS()));
        }
        if (payload.containsKey("text_m")) {
            data.setTextM(stringArrayValue(payload.get("text_m"), data.getTextM()));
        }
        if (payload.containsKey("text_e")) {
            data.setTextE(stringArrayValue(payload.get("text_e"), data.getTextE()));
        }
        if (payload.containsKey("hp_max")) {
            data.setHp(new long[]{Math.max(1, longValue(payload.get("hp_max"), data.getHp() != null && data.getHp().length > 0 ? data.getHp()[0] : 1))});
        }
        if (payload.containsKey("dame")) {
            data.setDame(Math.max(1, longValue(payload.get("dame"), data.getDame())));
        }

        int currentLevel = Math.max(0, Math.min(Math.max(boss.currentLevel, 0), boss.data.length - 1));
        if (level == currentLevel) {
            boss.name = data.getName();
            boss.secondsRest = data.getSecondsRest();
            if (boss.nPoint != null) {
                long hpMax = data.getHp() != null && data.getHp().length > 0 ? Math.max(1, data.getHp()[0]) : Math.max(1, boss.nPoint.hpg);
                boss.nPoint.hpg = hpMax;
                boss.nPoint.hpMax = hpMax;
                if (boss.nPoint.hp > hpMax) {
                    boss.nPoint.hp = hpMax;
                }
                boss.nPoint.dameg = Math.max(1, data.getDame());
                boss.nPoint.dame = boss.nPoint.dameg;
            }
        }
    }

    private static void rebuildBossAppearTogether(Boss boss, int level, int[] childIds) {
        if (boss == null || boss.bossAppearTogether == null || level < 0 || level >= boss.bossAppearTogether.length) {
            return;
        }
        Boss[] oldChildren = boss.bossAppearTogether[level];
        if (oldChildren != null) {
            for (Boss child : oldChildren) {
                if (child == null) {
                    continue;
                }
                child.setRuntimeDisabled(true);
                RuntimeBossRef ref = findRuntimeBossRef(child);
                BossManager manager = ref == null ? null : runtimeManager(ref.managerKey);
                if (manager != null) {
                    manager.removeBoss(child);
                }
            }
        }
        if (childIds == null || childIds.length == 0) {
            boss.bossAppearTogether[level] = null;
            return;
        }
        Boss[] children = new Boss[childIds.length];
        for (int i = 0; i < childIds.length; i++) {
            Boss child = BossManager.gI().createBoss(childIds[i]);
            if (child != null) {
                child.parentBoss = boss;
                child.lv = i;
                children[i] = child;
            }
        }
        boss.bossAppearTogether[level] = children;
    }

    public static synchronized void runtimeApplyPersistentConfigs() {
        if (runtimeConfigsApplied) {
            return;
        }
        runtimeConfigsApplied = true;
        try (Connection con = DBConnecter.getConnectionServer()) {
            ensureBossConfigTables(con);
            int configApplied = 0;
            configApplied += loadBossTemplateConfigs(con);
            configApplied += applyBossSpawnRules(con);
            try (PreparedStatement ps = con.prepareStatement(
                    "select * from " + RUNTIME_CONFIG_TABLE + " where active = 1 order by id asc");
                    ResultSet rs = ps.executeQuery()) {
                int applied = 0;
                while (rs.next()) {
                    if (applyBossRuntimeConfigRow(rs)) {
                        applied++;
                    }
                }
                if (applied > 0) {
                    Logger.log("[BossRuntime] Applied " + applied + " persistent boss configs\n");
                }
            }
            if (configApplied > 0) {
                Logger.log("[BossRuntime] Applied " + configApplied + " structured boss configs\n");
            }
        } catch (Exception e) {
            Logger.logException(BossManager.class, e);
        }
    }

    private static boolean applyBossRuntimeConfigRow(ResultSet rs) {
        try {
            String type = stringValue(rs.getString("config_type"));
            JSONObject payload = parseJsonObject(rs.getString("payload"));
            int bossId = rs.getInt("boss_id");
            if ("spawn".equals(type)) {
                int count = Math.max(1, intValue(payload.get("count"), 1));
                runtimeCreateBoss(bossId, count, false);
                return true;
            }
            if ("custom".equals(type)) {
                if (!payload.containsKey("boss_id")) {
                    payload.put("boss_id", bossId);
                }
                runtimeCreateCustomBoss(payload, false);
                return true;
            }
            if ("template".equals(type)) {
                int level = Math.max(0, rs.getInt("level_index"));
                applyBossTemplateUpdate(bossId, level, payload);
                return true;
            }
            if (!"override".equals(type)) {
                return false;
            }

            String managerKey = stringValue(rs.getString("manager_key"));
            int index = rs.getInt("runtime_index");
            Boss boss = runtimeBoss(managerKey, index);
            if (boss == null) {
                return false;
            }
            boolean enabled = rs.getInt("enabled") == 1;
            boolean deleted = rs.getInt("deleted") == 1;
            if (deleted) {
                BossManager manager = runtimeManager(managerKey);
                boss.setRuntimeDisabled(true);
                if (manager != null) {
                    manager.removeBoss(boss);
                }
                return true;
            }
            payload.put("manager", managerKey);
            payload.put("index", index);
            payload.put("level_index", rs.getInt("level_index"));
            payload.put("enabled", enabled);
            return runtimeUpdateBoss(payload, false);
        } catch (Exception e) {
            Logger.logException(BossManager.class, e);
            return false;
        }
    }

    private static void persistBossSpawn(int bossId, int count) {
        JSONObject payload = new JSONObject();
        payload.put("boss_id", bossId);
        payload.put("count", Math.max(1, count));
        persistBossConfig("spawn:" + bossId + ":" + UUID.randomUUID(), "spawn", "", -1, bossId, 0, true, false, payload);
    }

    private static void persistBossCustom(JSONObject source, int bossId, int count) {
        JSONObject payload = copyJson(source);
        payload.put("boss_id", bossId);
        payload.put("count", Math.max(1, count));
        persistBossConfig("custom:" + bossId + ":" + UUID.randomUUID(), "custom", "", -1, bossId, 0, true, false, payload);
    }

    private static void persistBossState(String managerKey, int index, Boss boss, boolean enabled, boolean deleted) {
        int level = boss == null ? 0 : Math.max(0, boss.currentLevel);
        JSONObject payload = runtimeStatePayload(managerKey, index, level, enabled);
        persistBossOverride(managerKey, index, boss, level, payload, enabled, deleted);
    }

    private static void persistBossTemplateOverride(int bossId, int level, JSONObject source) {
        JSONObject payload = templatePayload(source);
        if (payload.isEmpty()) {
            return;
        }
        payload.put("boss_id", bossId);
        payload.put("level_index", level);
        payload.put("active", true);
        try (Connection con = DBConnecter.getConnectionServer()) {
            ensureBossConfigTables(con);
            saveBossTemplateConfig(con, payload);
        } catch (Exception e) {
            Logger.logException(BossManager.class, e);
        }
        persistBossConfig("template:" + bossId, "template", "", -1, bossId, level, true, false, payload);
    }

    private static void persistBossOverride(String managerKey, int index, Boss boss, int level, JSONObject source, boolean enabled, boolean deleted) {
        JSONObject payload = copyJson(source);
        payload.put("manager", managerKey);
        payload.put("index", index);
        payload.put("level_index", level);
        payload.put("enabled", enabled);
        int bossId = boss == null ? intValue(payload.get("boss_id"), 0) : (int) boss.id;
        String key = "override:" + managerKey + ":" + index;
        persistBossConfig(key, "override", managerKey, index, bossId, level, enabled, deleted, payload);
    }

    private static void persistBossConfig(String key, String type, String managerKey, int index, int bossId, int level, boolean enabled, boolean deleted, JSONObject payload) {
        try (Connection con = DBConnecter.getConnectionServer()) {
            ensureBossConfigTables(con);
            if ("override".equals(type)) {
                try (PreparedStatement clear = con.prepareStatement(
                        "update " + RUNTIME_CONFIG_TABLE + " set active = 0 where config_key = ? and config_type = 'override'")) {
                    clear.setString(1, key);
                    clear.executeUpdate();
                }
            }
            try (PreparedStatement ps = con.prepareStatement(
                    "insert into " + RUNTIME_CONFIG_TABLE
                    + " (config_key, config_type, manager_key, runtime_index, boss_id, level_index, enabled, deleted, active, payload)"
                    + " values (?, ?, ?, ?, ?, ?, ?, ?, 1, ?)"
                    + " on duplicate key update config_type = values(config_type), manager_key = values(manager_key),"
                    + " runtime_index = values(runtime_index), boss_id = values(boss_id), enabled = values(enabled),"
                    + " deleted = values(deleted), active = 1, payload = values(payload), updated_at = current_timestamp")) {
                ps.setString(1, key);
                ps.setString(2, type);
                ps.setString(3, managerKey == null ? "" : managerKey);
                ps.setInt(4, index);
                ps.setInt(5, bossId);
                ps.setInt(6, Math.max(0, level));
                ps.setInt(7, enabled ? 1 : 0);
                ps.setInt(8, deleted ? 1 : 0);
                ps.setString(9, payload == null ? "{}" : payload.toJSONString());
                ps.executeUpdate();
            }
        } catch (Exception e) {
            Logger.logException(BossManager.class, e);
        }
    }

    private static void ensureBossRuntimeConfigTable(Connection con) throws Exception {
        try (Statement st = con.createStatement()) {
            st.executeUpdate(
                    "create table if not exists " + RUNTIME_CONFIG_TABLE + " ("
                    + "id bigint unsigned not null auto_increment,"
                    + "config_key varchar(191) not null,"
                    + "config_type varchar(24) not null,"
                    + "manager_key varchar(64) not null default '',"
                    + "runtime_index int not null default -1,"
                    + "boss_id int not null default 0,"
                    + "level_index int not null default 0,"
                    + "enabled tinyint(1) not null default 1,"
                    + "deleted tinyint(1) not null default 0,"
                    + "active tinyint(1) not null default 1,"
                    + "payload mediumtext null,"
                    + "created_at timestamp not null default current_timestamp,"
                    + "updated_at timestamp not null default current_timestamp on update current_timestamp,"
                    + "primary key (id),"
                    + "unique key uk_boss_runtime_config (config_key, level_index),"
                    + "key idx_boss_runtime_active (active, config_type),"
                    + "key idx_boss_runtime_ref (manager_key, runtime_index)"
                    + ") engine=InnoDB default charset=utf8mb4 collate=utf8mb4_general_ci");
        }
    }

    private static void ensureBossConfigTables(Connection con) throws Exception {
        ensureBossRuntimeConfigTable(con);
        try (Statement st = con.createStatement()) {
            st.executeUpdate(
                    "create table if not exists " + TEMPLATE_CONFIG_TABLE + " ("
                    + "id bigint unsigned not null auto_increment,"
                    + "boss_id int not null,"
                    + "level_index int not null default 0,"
                    + "active tinyint(1) not null default 1,"
                    + "payload mediumtext null,"
                    + "created_at timestamp not null default current_timestamp,"
                    + "updated_at timestamp not null default current_timestamp on update current_timestamp,"
                    + "primary key (id),"
                    + "unique key uk_boss_template_config (boss_id, level_index),"
                    + "key idx_boss_template_active (active, boss_id)"
                    + ") engine=InnoDB default charset=utf8mb4 collate=utf8mb4_general_ci");
            st.executeUpdate(
                    "create table if not exists " + SPAWN_RULE_TABLE + " ("
                    + "id bigint unsigned not null auto_increment,"
                    + "rule_key varchar(191) not null,"
                    + "boss_id int not null,"
                    + "manager_key varchar(64) not null default 'main',"
                    + "count int not null default 1,"
                    + "auto_spawn tinyint(1) not null default 1,"
                    + "active tinyint(1) not null default 1,"
                    + "payload mediumtext null,"
                    + "created_at timestamp not null default current_timestamp,"
                    + "updated_at timestamp not null default current_timestamp on update current_timestamp,"
                    + "primary key (id),"
                    + "unique key uk_boss_spawn_rule (rule_key),"
                    + "key idx_boss_spawn_active (active, auto_spawn)"
                    + ") engine=InnoDB default charset=utf8mb4 collate=utf8mb4_general_ci");
        }
    }

    public static synchronized String runtimeConfigsJson() {
        try (Connection con = DBConnecter.getConnectionServer()) {
            ensureBossConfigTables(con);
            return new StringBuilder("{")
                    .append("\"template\":").append(runtimeTemplateConfigsJson(con)).append(',')
                    .append("\"spawn\":").append(runtimeSpawnRulesJson(con)).append(',')
                    .append("\"runtime\":").append(runtimeLegacyConfigsJson(con))
                    .append('}')
                    .toString();
        } catch (Exception e) {
            Logger.logException(BossManager.class, e);
            return "{\"template\":[],\"spawn\":[],\"runtime\":[]}";
        }
    }

    public static synchronized String runtimeSaveConfig(JSONObject body) {
        String section = stringValue(body.get("section")).trim().toLowerCase();
        try (Connection con = DBConnecter.getConnectionServer()) {
            ensureBossConfigTables(con);
            switch (section) {
                case "template" -> saveBossTemplateConfig(con, body);
                case "spawn" -> {
                    saveBossSpawnRule(con, body);
                    if (boolValue(body.get("apply_now"), false)) {
                        runtimeCreateBoss(intValue(body.get("boss_id"), 0), Math.max(1, intValue(body.get("count"), 1)), false);
                    }
                }
                default -> {
                    return "{\"saved\":false,\"message\":\"Unknown boss config section\"}";
                }
            }
            return "{\"saved\":true,\"configs\":" + runtimeConfigsJson() + "}";
        } catch (Exception e) {
            Logger.logException(BossManager.class, e);
            return "{\"saved\":false,\"message\":\"" + escapeJson(e.getMessage()) + "\"}";
        }
    }

    private static int loadBossTemplateConfigs(Connection con) throws Exception {
        int applied = 0;
        try (PreparedStatement ps = con.prepareStatement(
                "select * from " + TEMPLATE_CONFIG_TABLE + " where active = 1 order by id asc");
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int bossId = rs.getInt("boss_id");
                int level = Math.max(0, rs.getInt("level_index"));
                JSONObject payload = parseJsonObject(rs.getString("payload"));
                payload.put("boss_id", bossId);
                payload.put("level_index", level);
                applyBossTemplateUpdate(bossId, level, payload);
                applied++;
            }
        }
        return applied;
    }

    private static int applyBossSpawnRules(Connection con) throws Exception {
        int applied = 0;
        try (PreparedStatement ps = con.prepareStatement(
                "select * from " + SPAWN_RULE_TABLE + " where active = 1 and auto_spawn = 1 order by id asc");
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int bossId = rs.getInt("boss_id");
                int count = Math.max(1, Math.min(50, rs.getInt("count")));
                runtimeCreateBoss(bossId, count, false);
                applied++;
            }
        }
        return applied;
    }

    private static void saveBossTemplateConfig(Connection con, JSONObject body) throws Exception {
        int bossId = intValue(body.get("boss_id"), 0);
        int level = Math.max(0, intValue(body.get("level_index"), 0));
        JSONObject payload = templatePayload(body);
        payload.put("boss_id", bossId);
        payload.put("level_index", level);
        try (PreparedStatement ps = con.prepareStatement(
                "insert into " + TEMPLATE_CONFIG_TABLE + " (boss_id, level_index, active, payload) values (?, ?, ?, ?)"
                + " on duplicate key update active = values(active), payload = values(payload), updated_at = current_timestamp")) {
            ps.setInt(1, bossId);
            ps.setInt(2, level);
            ps.setInt(3, boolValue(body.get("active"), true) ? 1 : 0);
            ps.setString(4, payload.toJSONString());
            ps.executeUpdate();
        }
        if (boolValue(body.get("active"), true)) {
            applyBossTemplateUpdate(bossId, level, payload);
        }
    }

    private static void saveBossSpawnRule(Connection con, JSONObject body) throws Exception {
        int bossId = intValue(body.get("boss_id"), 0);
        String managerKey = stringValue(body.get("manager_key")).trim();
        if (managerKey.isEmpty()) {
            managerKey = "main";
        }
        String ruleKey = stringValue(body.get("rule_key")).trim();
        if (ruleKey.isEmpty()) {
            ruleKey = "spawn:" + managerKey + ":" + bossId;
        }
        JSONObject payload = copyJson(body);
        try (PreparedStatement ps = con.prepareStatement(
                "insert into " + SPAWN_RULE_TABLE + " (rule_key, boss_id, manager_key, count, auto_spawn, active, payload)"
                + " values (?, ?, ?, ?, ?, ?, ?)"
                + " on duplicate key update boss_id = values(boss_id), manager_key = values(manager_key), count = values(count),"
                + " auto_spawn = values(auto_spawn), active = values(active), payload = values(payload), updated_at = current_timestamp")) {
            ps.setString(1, ruleKey);
            ps.setInt(2, bossId);
            ps.setString(3, managerKey);
            ps.setInt(4, Math.max(1, Math.min(50, intValue(body.get("count"), 1))));
            ps.setInt(5, boolValue(body.get("auto_spawn"), true) ? 1 : 0);
            ps.setInt(6, boolValue(body.get("active"), true) ? 1 : 0);
            ps.setString(7, payload.toJSONString());
            ps.executeUpdate();
        }
    }

    private static String runtimeTemplateConfigsJson(Connection con) throws Exception {
        StringBuilder json = new StringBuilder("[");
        try (PreparedStatement ps = con.prepareStatement(
                "select * from " + TEMPLATE_CONFIG_TABLE + " order by boss_id asc, level_index asc, id asc");
                ResultSet rs = ps.executeQuery()) {
            boolean first = true;
            while (rs.next()) {
                if (!first) json.append(',');
                first = false;
                json.append('{')
                        .append("\"id\":").append(rs.getLong("id")).append(',')
                        .append("\"boss_id\":").append(rs.getInt("boss_id")).append(',')
                        .append("\"level_index\":").append(rs.getInt("level_index")).append(',')
                        .append("\"active\":").append(rs.getInt("active") == 1).append(',')
                        .append("\"payload\":").append(payloadJson(rs.getString("payload")))
                        .append('}');
            }
        }
        json.append(']');
        return json.toString();
    }

    private static String runtimeSpawnRulesJson(Connection con) throws Exception {
        StringBuilder json = new StringBuilder("[");
        try (PreparedStatement ps = con.prepareStatement(
                "select * from " + SPAWN_RULE_TABLE + " order by id asc");
                ResultSet rs = ps.executeQuery()) {
            boolean first = true;
            while (rs.next()) {
                if (!first) json.append(',');
                first = false;
                json.append('{')
                        .append("\"id\":").append(rs.getLong("id")).append(',')
                        .append("\"rule_key\":\"").append(escapeJson(rs.getString("rule_key"))).append("\",")
                        .append("\"boss_id\":").append(rs.getInt("boss_id")).append(',')
                        .append("\"manager_key\":\"").append(escapeJson(rs.getString("manager_key"))).append("\",")
                        .append("\"count\":").append(rs.getInt("count")).append(',')
                        .append("\"auto_spawn\":").append(rs.getInt("auto_spawn") == 1).append(',')
                        .append("\"active\":").append(rs.getInt("active") == 1).append(',')
                        .append("\"payload\":").append(payloadJson(rs.getString("payload")))
                        .append('}');
            }
        }
        json.append(']');
        return json.toString();
    }

    private static String runtimeLegacyConfigsJson(Connection con) throws Exception {
        StringBuilder json = new StringBuilder("[");
        try (PreparedStatement ps = con.prepareStatement(
                "select id, config_key, config_type, manager_key, runtime_index, boss_id, level_index, enabled, deleted, active, payload from " + RUNTIME_CONFIG_TABLE + " order by id desc limit 200");
                ResultSet rs = ps.executeQuery()) {
            boolean first = true;
            while (rs.next()) {
                if (!first) json.append(',');
                first = false;
                json.append('{')
                        .append("\"id\":").append(rs.getLong("id")).append(',')
                        .append("\"config_key\":\"").append(escapeJson(rs.getString("config_key"))).append("\",")
                        .append("\"config_type\":\"").append(escapeJson(rs.getString("config_type"))).append("\",")
                        .append("\"manager_key\":\"").append(escapeJson(rs.getString("manager_key"))).append("\",")
                        .append("\"runtime_index\":").append(rs.getInt("runtime_index")).append(',')
                        .append("\"boss_id\":").append(rs.getInt("boss_id")).append(',')
                        .append("\"level_index\":").append(rs.getInt("level_index")).append(',')
                        .append("\"enabled\":").append(rs.getInt("enabled") == 1).append(',')
                        .append("\"deleted\":").append(rs.getInt("deleted") == 1).append(',')
                        .append("\"active\":").append(rs.getInt("active") == 1).append(',')
                        .append("\"payload\":").append(payloadJson(rs.getString("payload")))
                        .append('}');
            }
        }
        json.append(']');
        return json.toString();
    }

    private static String payloadJson(String raw) {
        return parseJsonObject(raw).toJSONString();
    }

    private static JSONObject copyJson(JSONObject source) {
        JSONObject copy = new JSONObject();
        if (source != null) {
            copy.putAll(source);
        }
        return copy;
    }

    private static JSONObject parseJsonObject(String raw) {
        Object parsed = JSONValue.parse(raw == null || raw.trim().isEmpty() ? "{}" : raw);
        return parsed instanceof JSONObject ? (JSONObject) parsed : new JSONObject();
    }

    private static Boss runtimeBoss(String managerKey, int index) {
        BossManager manager = runtimeManager(managerKey);
        if (manager == null) {
            return null;
        }
        synchronized (manager.bosses) {
            if (index < 0 || index >= manager.bosses.size()) {
                return null;
            }
            return manager.bosses.get(index);
        }
    }

    private static RuntimeBossRef findRuntimeBossRef(Boss boss) {
        for (BossManager manager : runtimeManagers()) {
            List<Boss> snapshot = runtimeBossSnapshot(manager);
            for (int i = 0; i < snapshot.size(); i++) {
                if (snapshot.get(i) == boss) {
                    return new RuntimeBossRef(runtimeManagerKey(manager), i);
                }
            }
        }
        return null;
    }

    private static Boss runtimeGroupRoot(Boss boss) {
        Boss root = boss;
        while (root != null && root.parentBoss != null) {
            root = root.parentBoss;
        }
        return root == null ? boss : root;
    }

    private static List<Boss> runtimeGroupMembers(Boss root) {
        List<Boss> members = new ArrayList<>();
        collectRuntimeGroupMembers(root, members);
        return members;
    }

    private static void collectRuntimeGroupMembers(Boss boss, List<Boss> members) {
        if (boss == null || members.contains(boss)) {
            return;
        }
        members.add(boss);
        if (boss.bossAppearTogether == null) {
            return;
        }
        for (Boss[] levelMembers : boss.bossAppearTogether) {
            if (levelMembers == null) {
                continue;
            }
            for (Boss child : levelMembers) {
                collectRuntimeGroupMembers(child, members);
            }
        }
    }

    private static BossData runtimeEditableBossData(Boss boss, int level) {
        level = Math.max(0, Math.min(level, boss.data.length - 1));
        BossData copy = copyBossData(boss.data[level]);
        boss.data[level] = copy;
        return copy;
    }

    private static BossData copyBossData(BossData source) {
        BossData copy = new BossData(
                source.getName(),
                source.getGender(),
                copyShortArray(source.getOutfit()),
                source.getDame(),
                copyLongArray(source.getHp()),
                copyIntArray(source.getMapJoin()),
                copySkillArray(source.getSkillTemp()),
                copyStringArray(source.getTextS()),
                copyStringArray(source.getTextM()),
                copyStringArray(source.getTextE()),
                source.getSecondsRest());
        copy.setTypeAppear(source.getTypeAppear());
        copy.setBossesAppearTogether(copyIntArray(source.getBossesAppearTogether()));
        return copy;
    }

    private static BossData runtimeBossDataFromPayload(JSONObject payload, BossData fallback, int[] childIds) {
        String name = stringValue(payload.get("name")).trim();
        if (name.isEmpty()) {
            name = fallback != null ? fallback.getName() : "Runtime Boss";
        }
        byte gender = (byte) Math.max(0, Math.min(2, intValue(payload.get("gender"), fallback != null ? fallback.getGender() : 2)));
        short[] outfit = shortArrayValue(payload.get("outfit"), fallback != null ? fallback.getOutfit() : new short[]{180, 181, 182, -1, -1, -1}, 6);
        long dame = Math.max(1, longValue(payload.get("dame"), fallback != null ? fallback.getDame() : 10000));
        long hp = Math.max(1, longValue(payload.get("hp_max"), fallback != null && fallback.getHp() != null && fallback.getHp().length > 0 ? fallback.getHp()[0] : 1000000));
        int[] mapJoin = intArrayValue(payload.get("map_join"), fallback != null ? fallback.getMapJoin() : new int[]{5});
        int[][] skills = skillArrayValue(payload.get("skill_temp"), fallback != null ? fallback.getSkillTemp() : new int[][]{{0, 1, 1000}});
        String[] textS = stringArrayValue(payload.get("text_s"), fallback != null ? fallback.getTextS() : new String[]{});
        String[] textM = stringArrayValue(payload.get("text_m"), fallback != null ? fallback.getTextM() : new String[]{});
        String[] textE = stringArrayValue(payload.get("text_e"), fallback != null ? fallback.getTextE() : new String[]{});
        int secondsRest = Math.max(0, intValue(payload.get("seconds_rest"), fallback != null ? fallback.getSecondsRest() : 900));
        AppearType appearType = appearTypeValue(payload.get("type_appear"), fallback != null ? fallback.getTypeAppear() : AppearType.DEFAULT_APPEAR);

        BossData data = new BossData(name, gender, outfit, dame, new long[]{hp}, mapJoin, skills, textS, textM, textE, secondsRest);
        data.setTypeAppear(appearType);
        data.setBossesAppearTogether(childIds);
        return data;
    }

    private static Boss createRuntimeCustomBoss(int bossID) throws Exception {
        BossData template = RUNTIME_CUSTOM_TEMPLATES.get(bossID);
        return template == null ? null : new Boss(bossID, false, true, copyBossData(template));
    }

    private static AppearType appearTypeValue(Object value, AppearType fallback) {
        if (value == null) {
            return fallback == null ? AppearType.DEFAULT_APPEAR : fallback;
        }
        try {
            return AppearType.valueOf(String.valueOf(value).trim());
        } catch (Exception e) {
            return fallback == null ? AppearType.DEFAULT_APPEAR : fallback;
        }
    }

    private static int[] intArrayValue(Object value, int[] fallback) {
        if (value instanceof JSONArray) {
            JSONArray arr = (JSONArray) value;
            int[] result = new int[arr.size()];
            for (int i = 0; i < arr.size(); i++) {
                result[i] = intValue(arr.get(i), 0);
            }
            return result;
        }
        String raw = stringValue(value).trim();
        if (raw.isEmpty()) {
            return copyIntArray(fallback);
        }
        String[] parts = raw.split("[,;\\s]+");
        List<Integer> values = new ArrayList<>();
        for (String part : parts) {
            if (!part.trim().isEmpty()) {
                values.add(intValue(part.trim(), 0));
            }
        }
        int[] result = new int[values.size()];
        for (int i = 0; i < values.size(); i++) {
            result[i] = values.get(i);
        }
        return result.length == 0 ? copyIntArray(fallback) : result;
    }

    private static short[] shortArrayValue(Object value, short[] fallback, int minLength) {
        int[] ints = intArrayValue(value, null);
        if ((ints == null || ints.length == 0) && fallback != null) {
            return copyShortArray(fallback);
        }
        int length = Math.max(minLength, ints == null ? 0 : ints.length);
        short[] result = new short[length];
        for (int i = 0; i < result.length; i++) {
            result[i] = -1;
        }
        for (int i = 0; ints != null && i < ints.length && i < result.length; i++) {
            result[i] = (short) ints[i];
        }
        return result;
    }

    private static int[][] skillArrayValue(Object value, int[][] fallback) {
        if (value instanceof JSONArray) {
            JSONArray arr = (JSONArray) value;
            List<int[]> skills = new ArrayList<>();
            for (Object row : arr) {
                int[] parsed = intArrayValue(row, null);
                if (parsed != null && parsed.length >= 3) {
                    skills.add(new int[]{parsed[0], parsed[1], parsed[2]});
                }
            }
            return skillsToArray(skills, fallback);
        }
        String raw = stringValue(value).trim();
        if (raw.isEmpty()) {
            return copySkillArray(fallback);
        }
        List<int[]> skills = new ArrayList<>();
        for (String line : raw.split("\\r?\\n")) {
            int[] parsed = intArrayValue(line, null);
            if (parsed != null && parsed.length >= 3) {
                skills.add(new int[]{parsed[0], parsed[1], parsed[2]});
            }
        }
        return skillsToArray(skills, fallback);
    }

    private static int[][] skillsToArray(List<int[]> skills, int[][] fallback) {
        if (skills == null || skills.isEmpty()) {
            return copySkillArray(fallback);
        }
        int[][] result = new int[skills.size()][3];
        for (int i = 0; i < skills.size(); i++) {
            result[i] = skills.get(i);
        }
        return result;
    }

    private static String[] stringArrayValue(Object value, String[] fallback) {
        if (value instanceof JSONArray) {
            JSONArray arr = (JSONArray) value;
            String[] result = new String[arr.size()];
            for (int i = 0; i < arr.size(); i++) {
                result[i] = stringValue(arr.get(i));
            }
            return result;
        }
        String raw = stringValue(value);
        if (raw.trim().isEmpty()) {
            return copyStringArray(fallback);
        }
        return raw.replace("\r", "").split("\\n");
    }

    private static int[] copyIntArray(int[] source) {
        if (source == null) return null;
        int[] copy = new int[source.length];
        System.arraycopy(source, 0, copy, 0, source.length);
        return copy;
    }

    private static short[] copyShortArray(short[] source) {
        if (source == null) return null;
        short[] copy = new short[source.length];
        System.arraycopy(source, 0, copy, 0, source.length);
        return copy;
    }

    private static long[] copyLongArray(long[] source) {
        if (source == null) return null;
        long[] copy = new long[source.length];
        System.arraycopy(source, 0, copy, 0, source.length);
        return copy;
    }

    private static String[] copyStringArray(String[] source) {
        if (source == null) return null;
        String[] copy = new String[source.length];
        System.arraycopy(source, 0, copy, 0, source.length);
        return copy;
    }

    private static int[][] copySkillArray(int[][] source) {
        if (source == null) return null;
        int[][] copy = new int[source.length][];
        for (int i = 0; i < source.length; i++) {
            copy[i] = copyIntArray(source[i]);
        }
        return copy;
    }

    private static List<BossManager> runtimeManagers() {
        List<BossManager> managers = new ArrayList<>();
        managers.add(BossManager.gI());
        managers.add(YardartManager.gI());
        managers.add(FinalBossManager.gI());
        managers.add(SkillSummonedManager.gI());
        managers.add(BrolyManager.gI());
        managers.add(AnTromManager.gI());
        managers.add(OtherBossManager.gI());
        managers.add(RedRibbonHQManager.gI());
        managers.add(TreasureUnderSeaManager.gI());
        managers.add(SnakeWayManager.gI());
        managers.add(GasDestroyManager.gI());
        managers.add(TrungThuEventManager.gI());
        managers.add(HalloweenEventManager.gI());
        managers.add(ChristmasEventManager.gI());
        managers.add(HungVuongEventManager.gI());
        managers.add(LunarNewYearEventManager.gI());
        return managers;
    }

    private static BossManager runtimeManager(String key) {
        for (BossManager manager : runtimeManagers()) {
            if (runtimeManagerKey(manager).equals(key)) {
                return manager;
            }
        }
        return null;
    }

    private static String runtimeManagerKey(BossManager manager) {
        if (manager instanceof YardartManager) return "yardart";
        if (manager instanceof FinalBossManager) return "final";
        if (manager instanceof SkillSummonedManager) return "skill_summoned";
        if (manager instanceof BrolyManager) return "broly";
        if (manager instanceof AnTromManager) return "an_trom";
        if (manager instanceof OtherBossManager) return "other";
        if (manager instanceof RedRibbonHQManager) return "red_ribbon_hq";
        if (manager instanceof TreasureUnderSeaManager) return "treasure_under_sea";
        if (manager instanceof SnakeWayManager) return "snake_way";
        if (manager instanceof GasDestroyManager) return "gas_destroy";
        if (manager instanceof TrungThuEventManager) return "trung_thu_event";
        if (manager instanceof HalloweenEventManager) return "halloween_event";
        if (manager instanceof ChristmasEventManager) return "christmas_event";
        if (manager instanceof HungVuongEventManager) return "hung_vuong_event";
        if (manager instanceof LunarNewYearEventManager) return "lunar_new_year_event";
        return "main";
    }

    private static List<Boss> runtimeBossSnapshot(BossManager manager) {
        if (manager == null) {
            return new ArrayList<>();
        }
        synchronized (manager.bosses) {
            return new ArrayList<>(manager.bosses);
        }
    }

    private static String runtimeBossJson(String managerKey, int index, Boss boss) {
        BossData data = boss.data[Math.max(0, Math.min(Math.max(boss.currentLevel, 0), boss.data.length - 1))];
        Boss groupRoot = runtimeGroupRoot(boss);
        RuntimeBossRef rootRef = findRuntimeBossRef(groupRoot);
        List<Boss> groupMembers = runtimeGroupMembers(groupRoot);
        String groupKey = rootRef == null ? managerKey + ":" + index : rootRef.managerKey + ":" + rootRef.index;
        String groupRole = boss == groupRoot ? (groupMembers.size() > 1 ? "parent" : "solo") : "child";
        BossData rootData = groupRoot.data[Math.max(0, Math.min(Math.max(groupRoot.currentLevel, 0), groupRoot.data.length - 1))];
        StringBuilder json = new StringBuilder();
        json.append('{')
                .append("\"manager\":\"").append(escapeJson(managerKey)).append("\",")
                .append("\"index\":").append(index).append(',')
                .append("\"boss_id\":").append(boss.id).append(',')
                .append("\"name\":\"").append(escapeJson(boss.name != null ? boss.name : data.getName())).append("\",")
                .append("\"template_name\":\"").append(escapeJson(data.getName())).append("\",")
                .append("\"custom\":").append(RUNTIME_CUSTOM_TEMPLATES.containsKey((int) boss.id)).append(',')
                .append("\"class_name\":\"").append(escapeJson(boss.getClass().getName())).append("\",")
                .append("\"class_simple_name\":\"").append(escapeJson(boss.getClass().getSimpleName())).append("\",")
                .append("\"has_custom_attack\":").append(runtimeOverridesNoArgMethod(boss, "attack")).append(',')
                .append("\"has_custom_reward\":").append(runtimeOverridesPlayerMethod(boss, "reward")).append(',')
                .append("\"attack_owner\":\"").append(escapeJson(runtimeNoArgMethodOwner(boss, "attack"))).append("\",")
                .append("\"reward_owner\":\"").append(escapeJson(runtimePlayerMethodOwner(boss, "reward"))).append("\",")
                .append("\"gender\":").append(data.getGender()).append(',')
                .append("\"type_appear\":\"").append(escapeJson(String.valueOf(data.getTypeAppear()))).append("\",")
                .append("\"status\":\"").append(escapeJson(String.valueOf(boss.bossStatus))).append("\",")
                .append("\"enabled\":").append(!boss.runtimeDisabled).append(',')
                .append("\"current_level\":").append(boss.currentLevel).append(',')
                .append("\"levels\":").append(boss.data.length).append(',')
                .append("\"hp\":").append(boss.nPoint != null ? boss.nPoint.hp : 0).append(',')
                .append("\"hp_max\":").append(boss.nPoint != null ? boss.nPoint.hpg : 0).append(',')
                .append("\"dame\":").append(boss.nPoint != null ? boss.nPoint.dameg : data.getDame()).append(',')
                .append("\"data_hp\":").append(longArrayJson(data.getHp())).append(',')
                .append("\"data_dame\":").append(data.getDame()).append(',')
                .append("\"seconds_rest\":").append(data.getSecondsRest()).append(',')
                .append("\"map_id\":").append(boss.zone != null ? boss.zone.map.mapId : -1).append(',')
                .append("\"map_name\":\"").append(escapeJson(boss.zone != null ? boss.zone.map.mapName : "")).append("\",")
                .append("\"zone_id\":").append(boss.zone != null ? boss.zone.zoneId : -1).append(',')
                .append("\"outfit\":").append(intArrayJson(data.getOutfit())).append(',')
                .append("\"skill_temp\":").append(int2ArrayJson(data.getSkillTemp())).append(',')
                .append("\"text_s\":").append(stringArrayJson(data.getTextS())).append(',')
                .append("\"text_m\":").append(stringArrayJson(data.getTextM())).append(',')
                .append("\"text_e\":").append(stringArrayJson(data.getTextE())).append(',')
                .append("\"levels_data\":").append(runtimeBossLevelsJson(boss)).append(',')
                .append("\"group_key\":\"").append(escapeJson(groupKey)).append("\",")
                .append("\"group_name\":\"").append(escapeJson(rootData.getName())).append("\",")
                .append("\"group_role\":\"").append(escapeJson(groupRole)).append("\",")
                .append("\"group_size\":").append(groupMembers.size()).append(',')
                .append("\"group_parent_manager\":\"").append(escapeJson(rootRef == null ? managerKey : rootRef.managerKey)).append("\",")
                .append("\"group_parent_index\":").append(rootRef == null ? index : rootRef.index).append(',')
                .append("\"map_join\":").append(intArrayJson(data.getMapJoin()))
                .append('}');
        return json.toString();
    }

    private static String runtimeBossLevelsJson(Boss boss) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < boss.data.length; i++) {
            if (i > 0) {
                json.append(',');
            }
            json.append(runtimeBossLevelJson((int) boss.id, boss.data[i], i));
        }
        json.append(']');
        return json.toString();
    }

    private static String runtimeBossLevelJson(int bossId, BossData data, int level) {
        long hpMax = 0;
        long[] hp = data.getHp();
        if (hp != null && hp.length > 0) {
            hpMax = hp[0];
        }
        return new StringBuilder("{")
                .append("\"level_index\":").append(level).append(',')
                .append("\"name\":\"").append(escapeJson(data.getName())).append("\",")
                .append("\"gender\":").append(data.getGender()).append(',')
                .append("\"type_appear\":\"").append(escapeJson(String.valueOf(data.getTypeAppear()))).append("\",")
                .append("\"hp_max\":").append(hpMax).append(',')
                .append("\"data_hp\":").append(longArrayJson(data.getHp())).append(',')
                .append("\"dame\":").append(data.getDame()).append(',')
                .append("\"seconds_rest\":").append(data.getSecondsRest()).append(',')
                .append("\"outfit\":").append(intArrayJson(data.getOutfit())).append(',')
                .append("\"map_join\":").append(intArrayJson(data.getMapJoin())).append(',')
                .append("\"skill_temp\":").append(int2ArrayJson(data.getSkillTemp())).append(',')
                .append("\"text_s\":").append(stringArrayJson(data.getTextS())).append(',')
                .append("\"text_m\":").append(stringArrayJson(data.getTextM())).append(',')
                .append("\"text_e\":").append(stringArrayJson(data.getTextE())).append(',')
                .append("\"bosses_appear_together\":").append(intArrayJson(data.getBossesAppearTogether()))
                .append('}')
                .toString();
    }

    private static boolean runtimeOverridesNoArgMethod(Boss boss, String methodName) {
        try {
            return boss != null
                    && boss.getClass().getMethod(methodName).getDeclaringClass() != Boss.class;
        } catch (Exception ignored) {
            return false;
        }
    }

    private static boolean runtimeOverridesPlayerMethod(Boss boss, String methodName) {
        try {
            return boss != null
                    && boss.getClass().getMethod(methodName, Player.class).getDeclaringClass() != Boss.class;
        } catch (Exception ignored) {
            return false;
        }
    }

    private static String runtimeNoArgMethodOwner(Boss boss, String methodName) {
        try {
            return boss == null ? "" : boss.getClass().getMethod(methodName).getDeclaringClass().getSimpleName();
        } catch (Exception ignored) {
            return "";
        }
    }

    private static String runtimePlayerMethodOwner(Boss boss, String methodName) {
        try {
            return boss == null ? "" : boss.getClass().getMethod(methodName, Player.class).getDeclaringClass().getSimpleName();
        } catch (Exception ignored) {
            return "";
        }
    }

    private static String runtimeBossCatalogJson() {
        int[] ids = new int[]{
            BossID.TIEU_DOI_TRUONG, BossID.BOJACK, BossID.SUPER_BOJACK, BossID.KING_KONG,
            BossID.XEN_BO_HUNG, BossID.SIEU_BO_HUNG, BossID.KUKU, BossID.MAP_DAU_DINH,
            BossID.RAMBO, BossID.FIDE, BossID.ANDROID_14, BossID.DR_KORE, BossID.COOLER,
            BossID.BLACK_GOKU, BossID.GOLDEN_FRIEZA, BossID.AN_TROM, BossID.AN_TROM_TV,
            BossID.BROLY, BossID.SUPER_BROLY, BossID.CUMBER, BossID.NYASU, BossID.JAMES,
            BossID.JESSIE, BossID.DORAEMON, BossID.BROLY_SSJ, BossID.BA_CON_SOI, BossID.BE_NA
        };
        String[] names = new String[]{
            "Tiểu đội trưởng", "Bojack", "Siêu Bojack", "King Kong",
            "Xên bọ hung", "Siêu bọ hung", "Kuku", "Mập đầu đinh",
            "Rambo", "Fide", "Android 14", "Dr Kore", "Cooler",
            "Black Goku", "Golden Frieza", "Ăn trộm", "Ăn trộm TV",
            "Broly", "Super Broly", "Cumber", "Nyasu", "James",
            "Jessie", "Doraemon", "Broly SSJ", "Ba con sói", "Bé Na"
        };
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < ids.length; i++) {
            if (i > 0) json.append(',');
            String name = i < names.length ? names[i] : String.valueOf(ids[i]);
            json.append("{\"boss_id\":").append(ids[i]).append(",\"label\":\"").append(escapeJson(name)).append("\"}");
        }
        json.append(']');
        return json.toString();
    }

    private static String intArrayJson(short[] values) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; values != null && i < values.length; i++) {
            if (i > 0) json.append(',');
            json.append(values[i]);
        }
        json.append(']');
        return json.toString();
    }

    private static String intArrayJson(int[] values) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; values != null && i < values.length; i++) {
            if (i > 0) json.append(',');
            json.append(values[i]);
        }
        json.append(']');
        return json.toString();
    }

    private static String longArrayJson(long[] values) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; values != null && i < values.length; i++) {
            if (i > 0) json.append(',');
            json.append(values[i]);
        }
        json.append(']');
        return json.toString();
    }

    private static String int2ArrayJson(int[][] values) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; values != null && i < values.length; i++) {
            if (i > 0) json.append(',');
            json.append(intArrayJson(values[i]));
        }
        json.append(']');
        return json.toString();
    }

    private static String stringArrayJson(String[] values) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; values != null && i < values.length; i++) {
            if (i > 0) json.append(',');
            json.append('"').append(escapeJson(values[i])).append('"');
        }
        json.append(']');
        return json.toString();
    }

    private static String escapeJson(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\r", "\\r").replace("\n", "\\n");
    }

    private static String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private static int intValue(Object value, int fallback) {
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (Exception e) {
            return fallback;
        }
    }

    private static long longValue(Object value, long fallback) {
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (Exception e) {
            return fallback;
        }
    }

    private static boolean boolValue(Object value, boolean fallback) {
        if (value == null) return fallback;
        return Boolean.parseBoolean(String.valueOf(value));
    }

    private static class RuntimeBossRef {
        final String managerKey;
        final int index;

        RuntimeBossRef(String managerKey, int index) {
            this.managerKey = managerKey;
            this.index = index;
        }
    }

    public void loadBoss() {
    //    this.createBoss(BossID.DAITUONGBROLY);
     //   this.createBoss(BossID.Hatchiyac);
        this.createBoss(BossID.TIEU_DOI_TRUONG);
        //    this.createBoss(BossID.TIEU_DOI_TRUONG_NM);
        this.createBoss(BossID.BOJACK);
        this.createBoss(BossID.SUPER_BOJACK);
        this.createBoss(BossID.KING_KONG);
        this.createBoss(BossID.XEN_BO_HUNG);
        this.createBoss(BossID.SIEU_BO_HUNG);
        this.createBoss(BossID.KUKU, 5);
        this.createBoss(BossID.MAP_DAU_DINH, 5);
        this.createBoss(BossID.RAMBO, 5);
        this.createBoss(BossID.FIDE);
        this.createBoss(BossID.ANDROID_14);
        this.createBoss(BossID.DR_KORE);
        this.createBoss(BossID.COOLER);
        this.createBoss(BossID.Duong);
        this.createBoss(BossID.BLACK_GOKU, 5);
        this.createBoss(BossID.GOLDEN_FRIEZA, 5);
        this.createBoss(BossID.BLACK_GOKU);
        this.createBoss(BossID.AN_TROM, 2);
        this.createBoss(BossID.AN_TROM_TV, 2);
        this.createBoss(BossID.BROLY, 15);
        // this.createBoss(BossID.SUPER_BROLY, 3);
        this.createBoss(BossID.CUMBER);
        this.createBoss(BossID.NYASU);
        this.createBoss(BossID.JAMES);
        this.createBoss(BossID.JESSIE);
        this.createBoss(BossID.DORAEMON);
        this.createBoss(BossID.BROLY_SSJ);
        this.createBoss(BossID.BA_CON_SOI);
        this.createBoss(BossID.BE_NA, 5);
        runtimeApplyPersistentConfigs();
        ensureDefaultBoss(BossID.BA_CON_SOI);
    }

    private void ensureDefaultBoss(int bossID) {
        for (Boss boss : runtimeBossSnapshot(this)) {
            if (boss != null && boss.id == bossID) {
                return;
            }
        }
        this.createBoss(bossID);
    }

    public void createBoss(int bossID, int total) {
        for (int i = 0; i < total; i++) {
            createBoss(bossID);
        }
    }

    public Boss createBoss(int bossID) {
        try {
            Boss created = switch (bossID) {
                case BossID.BROLY_SSJ ->
                    new BrolySsj();
                case BossID.BA_CON_SOI ->
                    new Baconsoi();
                case BossID.XUKA ->
                    new Xuka();
                case BossID.XEKO ->
                    new Xeko();
                case BossID.CHAIEN ->
                    new Chaien();
                case BossID.NOBITA ->
                    new Nobita();
                case BossID.DORAEMON ->
                    new Doraemon();
                case BossID.BE_NA ->
                    new Bena();
//                case BossID.DAITUONGBROLY ->
//                    new DaiTuongBroly();
//                case BossID.Hatchiyac ->
//                    new Hatchiyac();
                case BossID.TAP_SU_0 ->
                    new TAPSU0();
                case BossID.TAP_SU_1 ->
                    new TAPSU1();
                case BossID.TAP_SU_2 ->
                    new TAPSU2();
                case BossID.TAP_SU_3 ->
                    new TAPSU3();
                case BossID.TAP_SU_4 ->
                    new TAPSU4();
                case BossID.TAN_BINH_5 ->
                    new TANBINH5();
                case BossID.TAN_BINH_0 ->
                    new TANBINH0();
                case BossID.TAN_BINH_1 ->
                    new TANBINH1();
                case BossID.TAN_BINH_2 ->
                    new TANBINH2();
                case BossID.TAN_BINH_3 ->
                    new TANBINH3();
                case BossID.TAN_BINH_4 ->
                    new TANBINH4();
                case BossID.CHIEN_BINH_5 ->
                    new CHIENBINH5();
                case BossID.CHIEN_BINH_0 ->
                    new CHIENBINH0();
                case BossID.CHIEN_BINH_1 ->
                    new CHIENBINH1();
                case BossID.CHIEN_BINH_2 ->
                    new CHIENBINH2();
                case BossID.CHIEN_BINH_3 ->
                    new CHIENBINH3();
                case BossID.CHIEN_BINH_4 ->
                    new CHIENBINH4();
                case BossID.DOI_TRUONG_5 ->
                    new DOITRUONG5();
                case BossID.SO_4 ->
                    new SO4();
                case BossID.SO_3 ->
                    new SO3();
                case BossID.SO_2 ->
                    new SO2();
                case BossID.SO_1 ->
                    new SO1();
                case BossID.TIEU_DOI_TRUONG ->
                    new TDT();
                //  case BossID.SO_4_NM ->
                //      new SO4_NM();
                //  case BossID.SO_3_NM ->
                //      new SO3_NM();
                //   case BossID.SO_2_NM ->
                //     new SO2_NM();
                //  case BossID.SO_1_NM ->
                //     new SO1_NM();
                // case BossID.TIEU_DOI_TRUONG_NM ->
                //      new TDT_NM();
                case BossID.BUJIN ->
                    new BUJIN();
                case BossID.KOGU ->
                    new KOGU();
                case BossID.ZANGYA ->
                    new ZANGYA();
                case BossID.BIDO ->
                    new BIDO();
                case BossID.BOJACK ->
                    new BOJACK();
                case BossID.SUPER_BOJACK ->
                    new SUPER_BOJACK();
                case BossID.KUKU ->
                    new Kuku();
                case BossID.MAP_DAU_DINH ->
                    new MapDauDinh();
                case BossID.RAMBO ->
                    new Rambo();
                case BossID.TAU_PAY_PAY_DONG_NAM_KARIN ->
                    new TaoPaiPai();
                case BossID.DRABURA ->
                    new Drabura();
                case BossID.BUI_BUI ->
                    new BuiBui();
                case BossID.BUI_BUI_2 ->
                    new BuiBui2();
                case BossID.YA_CON ->
                    new Yacon();
                case BossID.DRABURA_2 ->
                    new Drabura2();
                case BossID.GOKU ->
                    new Goku();
                case BossID.CADIC ->
                    new Cadic();
                case BossID.MABU_12H ->
                    new Mabu();
                case BossID.DRABURA_3 ->
                    new Drabura3();
                case BossID.MABU ->
                    new Mabu2H();
                case BossID.SUPERBU ->
                    new SuperBu();
                case BossID.FIDE ->
                    new Fide();
                case BossID.DR_KORE ->
                    new DrKore();
                case BossID.ANDROID_19 ->
                    new Android19();
                case BossID.ANDROID_13 ->
                    new Android13();
                case BossID.ANDROID_14 ->
                    new Android14();
                case BossID.ANDROID_15 ->
                    new Android15();
                case BossID.PIC ->
                    new Pic();
                case BossID.POC ->
                    new Poc();
                case BossID.KING_KONG ->
                    new KingKong();
                case BossID.XEN_BO_HUNG ->
                    new XenBoHung();
                case BossID.SIEU_BO_HUNG ->
                    new SieuBoHung();
                case BossID.XEN_CON_1 ->
                    new XENCON1();
                case BossID.XEN_CON_2 ->
                    new XENCON2();
                case BossID.XEN_CON_3 ->
                    new XENCON3();
                case BossID.XEN_CON_4 ->
                    new XENCON4();
                case BossID.XEN_CON_5 ->
                    new XENCON5();
                case BossID.XEN_CON_6 ->
                    new XENCON6();
                case BossID.XEN_CON_7 ->
                    new XENCON7();
                case BossID.COOLER ->
                    new Cooler();
                case BossID.BROLY ->
                    new Broly();
                case BossID.SUPER_BROLY ->
                    new SuperBroly();
                case BossID.AN_TROM ->
                    new AnTrom();
                case BossID.AN_TROM_TV ->
                    new AnTromTV();
                case BossID.KHIDOT ->
                    new KhiDot();
                case BossID.NGUYETTHAN ->
                    new NguyetThan();
                case BossID.NHATTHAN ->
                    new NhatThan();
                case BossID.GOLDEN_FRIEZA ->
                    new GoldenFrieza();
                case BossID.DEATH_BEAM_1 ->
                    new DeathBeam1();
                case BossID.DEATH_BEAM_2 ->
                    new DeathBeam2();
                case BossID.DEATH_BEAM_3 ->
                    new DeathBeam3();
                case BossID.DEATH_BEAM_4 ->
                    new DeathBeam4();
                case BossID.DEATH_BEAM_5 ->
                    new DeathBeam5();
                case BossID.BIMA ->
                    new BiMa();
                case BossID.MATROI ->
                    new MaTroi();
                case BossID.DOI ->
                    new Doi();
                case BossID.ONG_GIA_NOEL ->
                    new OngGiaNoel();
                case BossID.SON_TINH ->
                    new SonTinh();
                case BossID.THUY_TINH ->
                    new ThuyTinh();
                case BossID.LAN_CON ->
                    new LanCon();
                case BossID.BLACK_GOKU ->
                    new BlackGoku();
                case BossID.CUMBER ->
                    new Cumber();
                case BossID.NYASU ->
                    new Nyasu();
                case BossID.JESSIE ->
                    new Jessie();
                case BossID.JAMES ->
                    new James();
                default ->
                    createRuntimeCustomBoss(bossID);
            };
            applyKnownTemplateOverrides(created);
            return created;
        } catch (Exception e) {
            Logger.error(e + "\n");
            return null;
        }
    }

    public Boss getBoss(int id) {
        try {
            Boss boss;
            synchronized (this.bosses) {
                boss = this.bosses.get(id);
            }
            if (boss != null) {
                return boss;
            }
        } catch (Exception e) {
        }
        return null;
    }

    public void showListBoss(Player player) {
        if (!player.isAdmin()) {
            return;
        }
        player.iDMark.setMenuType(3);
        Message msg;
        try {
            msg = new Message(-96);
            msg.writer().writeByte(0);
            msg.writer().writeUTF("Boss");
            List<Boss> snapshot = runtimeBossSnapshot(this);
            msg.writer()
                    .writeByte((int) snapshot.stream()
                            .filter(boss -> !MapService.gI().isMapBossFinal(boss.data[0].getMapJoin()[0])
                            && !MapService.gI().isMapHuyDiet(boss.data[0].getMapJoin()[0])
                            && !MapService.gI().isMapYardart(boss.data[0].getMapJoin()[0])
                            && !MapService.gI().isMapMaBu(boss.data[0].getMapJoin()[0])
                            && !MapService.gI().isMapBlackBallWar(boss.data[0].getMapJoin()[0]))
                            .count());
            for (int i = 0; i < snapshot.size(); i++) {
                Boss boss = snapshot.get(i);
                if (MapService.gI().isMapBossFinal(boss.data[0].getMapJoin()[0])
                        || MapService.gI().isMapYardart(boss.data[0].getMapJoin()[0])
                        || MapService.gI().isMapHuyDiet(boss.data[0].getMapJoin()[0])
                        || MapService.gI().isMapMaBu(boss.data[0].getMapJoin()[0])
                        || MapService.gI().isMapBlackBallWar(boss.data[0].getMapJoin()[0])) {
                    continue;
                }
                msg.writer().writeInt(i);
                msg.writer().writeInt(i);
                msg.writer().writeShort(boss.data[0].getOutfit()[0]);
                if (player.getSession().version >= 214) {
                    msg.writer().writeShort(-1);
                }
                msg.writer().writeShort(boss.data[0].getOutfit()[1]);
                msg.writer().writeShort(boss.data[0].getOutfit()[2]);
                msg.writer().writeUTF(boss.data[0].getName());
                if (boss.zone != null) {
                    msg.writer().writeUTF(boss.bossStatus.toString());
                    msg.writer().writeUTF(
                            boss.zone.map.mapName + "(" + boss.zone.map.mapId + ") khu " + boss.zone.zoneId + "");
                } else {
                    msg.writer().writeUTF(boss.bossStatus.toString());
                    msg.writer().writeUTF("Chết rồi");
                }
            }
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public Boss getBossById(int bossId) {
        return runtimeBossSnapshot(this).stream().filter(boss -> boss.id == bossId && !boss.isDie()).findFirst().orElse(null);
    }

    public boolean checkBosses(Zone zone, int BossID) {
        return runtimeBossSnapshot(this).stream()
                .filter(boss -> boss.id == BossID && boss.zone != null && boss.zone.equals(zone) && !boss.isDie())
                .findFirst().orElse(null) != null;
    }

    public Player findBossClone(Player player) {
        return player.zone.getBosses().stream().filter(boss -> boss.id < -100_000_000 && !boss.isDie()).findFirst()
                .orElse(null);
    }

    public Boss getBossById(int bossId, int mapId, int zoneId) {
        return runtimeBossSnapshot(this).stream().filter(boss -> boss.id == bossId && boss.zone != null
                && boss.zone.map.mapId == mapId && boss.zone.zoneId == zoneId && !boss.isDie()).findFirst()
                .orElse(null);
    }

    @Override
    public void run() {
        while (!Maintenance.isRunning) {
            try {
                int delay = 150;
                long st = System.currentTimeMillis();
                List<Boss> snapshot = runtimeBossSnapshot(this);
                for (int i = snapshot.size() - 1; i >= 0; i--) {
                    try {
                        Boss boss = snapshot.get(i);
                        if (boss != null) {
                            boss.update();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                // if (delay - (System.currentTimeMillis() - st) > 0) {
                // Thread.sleep(delay - (System.currentTimeMillis() - st));
                // }
                Functions.sleep(Math.max(delay - (System.currentTimeMillis() - st), 10));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
