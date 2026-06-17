package server;

/*
 *
 *
 * @author CongHoan
 */
import models.Card.OptionCard;
import models.Card.RadarService;
import models.Card.RadarCard;
import models.Consign.ConsignItem;
import models.Consign.ConsignShopManager;
import jdbc.DBConnecter;
import consts.ConstPlayer;
import consts.ConstMap;
import data.DataGame;
import jdbc.daos.ShopDAO;
import models.Template.*;
import clan.Clan;
import clan.ClanMember;
import consts.ConstSQL;

import static data.DataGame.MAP_MOUNT_NUM;
import encrypt.ImageUtil;

import models.GiftCode.GiftCode;
import models.GiftCode.GiftCodeManager;
import intrinsic.Intrinsic;
import item.Item;
import item.Item.ItemOption;
import map.WayPoint;
import npc.Npc;
import npc.NpcFactory;

import shop.Shop;
import skill.NClass;
import skill.Skill;

import task.SideTaskTemplate;
import task.SubTaskMain;
import task.TaskMain;
import services.ItemService;
import services.MapService;
import utils.Logger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import map.EffectMap;
import map.Zone;

import matches.TOP;
import utils.Util;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import npc.NonInteractiveNPC;
import power.CaptionManager;
import power.PowerLimitManager;
import sosumenh.SoSuMenhManager;
import task.ClanTaskTemplate;
import task.KolTaskTemplate;

public final class Manager {

    private static Manager instance;

    public static byte SERVER = 1;
    public static byte SECOND_WAIT_LOGIN = 5;
    public static int MAX_PER_IP = 10;
    public static int MAX_PLAYER = 2000;
    public static byte RATE_EXP_SERVER = 1;
    public static boolean DEBUG = false;
    public static boolean LOCAL = false;
    public static boolean TEST = false;
    public static boolean DAO_AUTO_UPDATER = false;

    public static MapTemplate[] MAP_TEMPLATES;
    public static final List<map.Map> MAPS = new ArrayList<>();
    public static final List<ItemOptionTemplate> ITEM_OPTION_TEMPLATES = new ArrayList<>();
    public static final List<ArrHead2Frames> ARR_HEAD_2_FRAMES = new ArrayList<>();
    public static final Map<String, Byte> IMAGES_BY_NAME = new HashMap<>();
    public static final List<ItemTemplate> ITEM_TEMPLATES = new ArrayList<>();
    public static final Set<Integer> ITEM_TEMPLATE_IDS = new HashSet<>();
    public static final List<MobTemplate> MOB_TEMPLATES = new ArrayList<>();
    public static final List<NpcTemplate> NPC_TEMPLATES = new ArrayList<>();
    public static final List<TaskMain> TASKS = new ArrayList<>();
    public static final List<SideTaskTemplate> SIDE_TASKS_TEMPLATE = new ArrayList<>();
    public static final List<ClanTaskTemplate> CLAN_TASKS_TEMPLATE = new ArrayList<>();
    public static final List<AchievementTemplate> ACHIEVEMENT_TEMPLATE = new ArrayList<>();
    public static final List<Intrinsic> INTRINSICS = new ArrayList<>();
    public static final List<Intrinsic> INTRINSIC_TD = new ArrayList<>();
    public static final List<Intrinsic> INTRINSIC_NM = new ArrayList<>();
    public static final List<Intrinsic> INTRINSIC_XD = new ArrayList<>();
    public static final List<HeadAvatar> HEAD_AVATARS = new ArrayList<>();
    public static final List<BgItem> BG_ITEMS = new ArrayList<>();
    public static final List<FlagBag> FLAGS_BAGS = new ArrayList<>();
    public static final List<NClass> NCLASS = new ArrayList<>();
    public static final List<Npc> NPCS = new ArrayList<>();
    public static List<Shop> SHOPS = new ArrayList<>();
    public static final List<Clan> CLANS = new ArrayList<>();
    public static final List<String> NOTIFY = new ArrayList<>();
    public static final List<KolTaskTemplate> KOL_TASKS_TEMPLATE = new ArrayList<>();
    public static final Map<Integer, List<RuntimeMapDropRule>> MAP_DROP_RULES = new HashMap<>();

    public static List<TOP> topSM;
    public static List<TOP> topNap;
    public static List<TOP> topPhaoBong;
    public static List<TOP> topLixi;
    public static List<TOP> topSanBoss;
    // public static List<TOP> topDuaSM;
    // public static List<TOP> topDuaNap;
    public static List<TOP> topSD;
    public static List<TOP> topSSM;
    public static List<TOP> topHP;
    public static List<TOP> topKI;
    public static List<TOP> topNV;
    public static List<TOP> topSK;
    public static List<TOP> topPVP;
    public static List<TOP> topNHS;
    public static List<TOP> topDC;
    public static List<TOP> topVDST;
    public static List<TOP> topWHIS;
    public static long timeRealTop = 0;

    public static final short[][] trangBiKichHoat = {
        {0, 6, 21, 27},
        {1, 7, 22, 28},
        {2, 8, 23, 29}
    };

    public static final short[][] trangBiKichHoatVip = {
        {555, 556, 562, 563},
        {557, 558, 564, 565},
        {559, 560, 566, 567}
    };

    public static final int[][][] LIST_DO_KHAC_4MON = {
        { // TD
            {137, 138, 139, 230, 231, 232, 233, 555},
            {141, 142, 143, 242, 243, 244, 245, 556},
            {145, 146, 147, 254, 255, 256, 257, 562},
            {149, 150, 151, 266, 267, 268, 269, 563}
        },
        { // NM
            {153, 154, 155, 234, 235, 236, 237, 557},
            {157, 158, 159, 246, 247, 248, 249, 558},
            {161, 162, 163, 258, 259, 260, 261, 564},
            {165, 166, 167, 270, 271, 272, 273, 565}
        },
        { // XD
            {169, 170, 171, 238, 239, 240, 241, 559},
            {173, 174, 175, 250, 251, 252, 253, 560},
            {177, 178, 179, 262, 263, 264, 265, 566},
            {181, 182, 183, 274, 275, 276, 277, 567}
        }
    };

    public static final short[][] DO_THAN_4MON = {
        {(short) 555, (short) 556, (short) 562, (short) 563}, // TD
        {(short) 557, (short) 558, (short) 564, (short) 565}, // NM
        {(short) 559, (short) 560, (short) 566, (short) 567} // XD
    };
    
    public static int randomDoKichHoat4Mon(int gender, int type) {
        int[] list = LIST_DO_KHAC_4MON[gender][type];
        int tiLeDoThan = 3;
        int rand = Util.nextInt(100); 
        if (rand < tiLeDoThan) {
            return list[list.length - 1];
        }
        return list[Util.nextInt(list.length - 1)];
    }
//    public static final short[][][] trangBiKichHoatVip = {
//        {{555, 555, 555, 555, 555, 650}, {556, 556, 556, 556, 556, 651}, {562, 562, 562, 562, 562, 657}, {563, 563, 563, 563, 563, 658}}, // Trái Đất
//        {{557, 557, 557, 557, 557, 652}, {558, 558, 558, 558, 558, 653}, {564, 564, 564, 564, 564, 659}, {565, 565, 565, 565, 565, 660}}, // Namec
//        {{559, 559, 559, 559, 559, 654}, {560, 560, 560, 560, 560, 655}, {566, 566, 566, 566, 566, 661}, {567, 567, 567, 567, 567, 662}} // Xayda
//    };

    public static Manager gI() {
        if (instance == null) {
            instance = new Manager();
        }
        return instance;
    }

    private Manager() {
        try {
            loadProperties();
        } catch (IOException ex) {
            Logger.logException(Manager.class, ex, "Lỗi load properites");
            System.exit(0);
        }
        // ImageUtil.initImage();
        this.loadDatabase();
        NpcFactory.createNpcConMeo();
        NpcFactory.createNpcRongThieng();
        this.initMap();
        System.out.println("Finish connect Server: " + DBConnecter.DB_DATA);
    }

    private void initMap() {
        int[][] tileTyleTop = readTileIndexTileType(ConstMap.TILE_TOP);
        for (MapTemplate mapTemp : MAP_TEMPLATES) {
            int[][] tileMap = readTileMap(mapTemp.id);
            int[] tileTop = tileTyleTop[mapTemp.tileId - 1];
            map.Map map = new map.Map(mapTemp.id,
                    mapTemp.name, mapTemp.planetId, mapTemp.tileId, mapTemp.bgId,
                    mapTemp.bgType, mapTemp.type, tileMap, tileTop,
                    mapTemp.zones,
                    mapTemp.maxPlayerPerZone, mapTemp.wayPoints, mapTemp.effectMaps);
            MAPS.add(map);
            map.initMob(mapTemp.mobTemp, mapTemp.mobLevel, mapTemp.mobHp, mapTemp.mobX, mapTemp.mobY);
            map.initNpc(mapTemp.npcId, mapTemp.npcX, mapTemp.npcY);
            // new Thread(map, "Update map " + map.mapName).start();
        }
        new Thread(() -> {
            try {
                while (!Maintenance.isRunning) {
                    long st = System.currentTimeMillis();
                    for (map.Map map : MAPS) {
                        for (Zone zone : map.zones) {
                            try {
                                zone.update();
                            } catch (Exception e) {

                            }
                        }
                    }
                    long timeDo = System.currentTimeMillis() - st;
                    long frame = PerformanceConfig.gI().mapTickMillis;
                    long sleep = frame - timeDo;
                    if (sleep > 0) {
                        Thread.sleep(sleep);
                    }
                }
            } catch (Exception ex) {

            }
        },
                "Update maps").start();
        new NonInteractiveNPC().initNonInteractiveNPC();
        Logger.success("Initialize map successfully!\n");
    }

    private void loadDatabase() {
        long st = System.currentTimeMillis();
        JSONArray dataArray;
        JSONObject dataObject;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try (Connection con2 = DBConnecter.getConnectionServer();) {
            // load clan
            ps = con2.prepareStatement("select * from clan");
            rs = ps.executeQuery();
            while (rs.next()) {
                Clan clan = new Clan();
                clan.id = rs.getInt("id");
                clan.name = rs.getString("name");
                clan.name2 = rs.getString("name_2");
                clan.slogan = rs.getString("slogan");
                clan.imgId = rs.getByte("img_id");
                clan.powerPoint = rs.getLong("power_point");
                clan.maxMember = rs.getByte("max_member");
                clan.capsuleClan = rs.getInt("clan_point");
                clan.level = rs.getByte("level");
                if (clan.level < 1) {
                    clan.level = 1;
                }
                clan.createTime = (int) (rs.getTimestamp("create_time").getTime() / 1000);
                dataArray = (JSONArray) JSONValue.parse(rs.getString("members"));
                for (int i = 0; i < dataArray.size(); i++) {
                    dataObject = (JSONObject) JSONValue.parse(String.valueOf(dataArray.get(i)));
                    ClanMember cm = new ClanMember();
                    cm.clan = clan;
                    cm.id = Integer.parseInt(String.valueOf(dataObject.get("id")));
                    cm.name = String.valueOf(dataObject.get("name"));
                    cm.head = Short.parseShort(String.valueOf(dataObject.get("head")));
                    cm.body = Short.parseShort(String.valueOf(dataObject.get("body")));
                    cm.leg = Short.parseShort(String.valueOf(dataObject.get("leg")));
                    cm.role = Byte.parseByte(String.valueOf(dataObject.get("role")));
                    cm.donate = Integer.parseInt(String.valueOf(dataObject.get("donate")));
                    cm.receiveDonate = Integer.parseInt(String.valueOf(dataObject.get("receive_donate")));
                    cm.memberPoint = Integer.parseInt(String.valueOf(dataObject.get("member_point")));
                    cm.clanPoint = Integer.parseInt(String.valueOf(dataObject.get("clan_point")));
                    cm.joinTime = Integer.parseInt(String.valueOf(dataObject.get("join_time")));
                    cm.timeAskPea = Long.parseLong(String.valueOf(dataObject.get("ask_pea_time")));
                    try {
                        cm.powerPoint = Long.parseLong(String.valueOf(dataObject.get("power")));
                    } catch (NumberFormatException e) {
                    }
                    clan.addClanMember(cm);
                }
                dataArray.clear();
                CLANS.add(clan);
            }

            ps = con2.prepareStatement("select id from clan order by id desc limit 1");
            rs = ps.executeQuery();
            if (rs.next()) {
                Clan.NEXT_ID = rs.getInt("id") + 1;
            }

            Logger.success("Successfully loaded clan (" + CLANS.size() + "), clan next id: " + Clan.NEXT_ID + "\n");

            // Load item ki gui
            ps = con2.prepareStatement("SELECT * FROM shop_ky_gui");
            rs = ps.executeQuery();
            while (rs.next()) {
                int i = rs.getInt("id");
                int idPl = rs.getInt("player_id");
                byte tab = rs.getByte("tab");
                short itemId = rs.getShort("item_id");
                int gold = rs.getInt("gold");
                int gem = rs.getInt("gem");
                int quantity = rs.getInt("quantity");
                long isTime = rs.getLong("lasttime");
                boolean isBuy = rs.getByte("isBuy") == 1;
                List<Item.ItemOption> op = new ArrayList<>();
                JSONArray jsa2 = (JSONArray) JSONValue.parse(rs.getString("itemOption"));
                for (int j = 0; j < jsa2.size(); ++j) {
                    JSONObject jso2 = (JSONObject) jsa2.get(j);
                    int idOptions = Integer.parseInt(jso2.get("id").toString());
                    int param = Integer.parseInt(jso2.get("param").toString());
                    op.add(new Item.ItemOption(idOptions, param));
                }
                ConsignShopManager.gI().listItem
                        .add(new ConsignItem(i, itemId, idPl, tab, gold, gem, quantity, isTime, op, isBuy));
            }
            Logger.success("Successfully loaded Consign Item (" + ConsignShopManager.gI().listItem.size() + ")\n");

            // Load giftcode
            ps = con2.prepareStatement("SELECT * FROM giftcode");
            rs = ps.executeQuery();
            while (rs.next()) {
                GiftCode giftcode = new GiftCode();
                giftcode.code = rs.getString("code");
                giftcode.id = rs.getInt("id");
                giftcode.countLeft = rs.getInt("count_left");
                giftcode.mtv = rs.getInt("mtv");
                if (giftcode.countLeft == -1) {
                    giftcode.countLeft = 999999999;
                }
                giftcode.datecreate = rs.getTimestamp("datecreate");
                giftcode.dateexpired = rs.getTimestamp("expired");
                JSONArray jar = (JSONArray) JSONValue.parse(rs.getString("detail"));
                if (jar != null) {
                    for (int i = 0; i < jar.size(); ++i) {
                        JSONObject jsonObj = (JSONObject) jar.get(i);

                        int id = Integer.parseInt(jsonObj.get("temp_id").toString());
                        int quantity = Integer.parseInt(jsonObj.get("quantity").toString());

                        JSONArray option = (JSONArray) jsonObj.get("options");
                        ArrayList<ItemOption> optionList = new ArrayList<>();

                        if (option != null) {
                            for (int u = 0; u < option.size(); u++) {
                                JSONObject jsonobject = (JSONObject) option.get(u);
                                int optionId = Integer.parseInt(jsonobject.get("id").toString());
                                int param = Integer.parseInt(jsonobject.get("param").toString());
                                optionList.add(new Item.ItemOption(optionId, param));
                            }
                        }
                        giftcode.option.put(id, optionList);
                        giftcode.detail.put(id, quantity);
                    }
                }
                GiftCodeManager.gI().listGiftCode.add(giftcode);
            }
            Logger.success("Successfully loaded giftcode (" + GiftCodeManager.gI().listGiftCode.size() + ")\n");

        } catch (Exception ex) {

        }

        try (Connection con = DBConnecter.getConnectionServer();) {
            // load part
            ps = con.prepareStatement("select * from part");
            rs = ps.executeQuery();
            List<Part> parts = new ArrayList<>();
            while (rs.next()) {
                Part part = new Part();
                part.id = rs.getShort("id");
                part.type = rs.getByte("type");
                dataArray = (JSONArray) JSONValue.parse(rs.getString("data").replaceAll("\\\"", ""));
                for (int j = 0; j < dataArray.size(); j++) {
                    JSONArray pd = (JSONArray) JSONValue.parse(String.valueOf(dataArray.get(j)));
                    part.partDetails.add(new PartDetail(Short.parseShort(String.valueOf(pd.get(0))),
                            Byte.parseByte(String.valueOf(pd.get(1))),
                            Byte.parseByte(String.valueOf(pd.get(2)))));
                    pd.clear();
                }
                parts.add(part);
                dataArray.clear();
            }
            DataOutputStream dos = new DataOutputStream(new FileOutputStream("data/update_data/part"));
            dos.writeShort(parts.size());
            for (Part part : parts) {
                dos.writeByte(part.type);
                for (PartDetail partDetail : part.partDetails) {
                    dos.writeShort(partDetail.iconId);
                    dos.writeByte(partDetail.dx);
                    dos.writeByte(partDetail.dy);
                }
            }
            dos.flush();
            Logger.success("Successfully loaded part (" + parts.size() + ")\n");

            // load bg item template
            ps = con.prepareStatement("select * from bg_item_template");
            rs = ps.executeQuery();
            while (rs.next()) {
                BgItem bgItem = new BgItem();
                bgItem.id = rs.getInt("id");
                bgItem.layer = rs.getByte("layer");
                bgItem.dx = rs.getShort("dx");
                bgItem.dy = rs.getShort("dy");
                bgItem.idImage = rs.getShort("image_id");
                BG_ITEMS.add(bgItem);
            }
            Logger.success("Successfully loaded bg item template (" + BG_ITEMS.size() + ")\n");

            // load array head 2 frames
            ps = con.prepareStatement("select * from array_head_2_frames");
            rs = ps.executeQuery();
            while (rs.next()) {
                ArrHead2Frames arrHead2Frames = new ArrHead2Frames();
                dataArray = (JSONArray) JSONValue.parse(rs.getString("data"));
                for (int i = 0; i < dataArray.size(); i++) {
                    arrHead2Frames.frames.add(Integer.valueOf(dataArray.get(i).toString()));
                }
                ARR_HEAD_2_FRAMES.add(arrHead2Frames);
            }
            Logger.success("Successfully loaded arr head 2 frames (" + ARR_HEAD_2_FRAMES.size() + ")\n");

            SoSuMenhManager.getInstance().loading();
            Logger.success("Loaded SoSuMenh tasks: " + SoSuMenhManager.getInstance().list.size());

            // load skill
            ps = con.prepareStatement("select * from skill_template order by nclass_id, slot");
            rs = ps.executeQuery();
            byte nClassId = -1;
            NClass nClass = null;
            while (rs.next()) {
                byte id = rs.getByte("nclass_id");
                if (id != nClassId) {
                    nClassId = id;
                    nClass = new NClass();
                    nClass.name = id == ConstPlayer.TRAI_DAT ? "Trái Đất" : id == ConstPlayer.NAMEC ? "Namếc" : "Xayda";
                    nClass.classId = nClassId;
                    NCLASS.add(nClass);
                }
                SkillTemplate skillTemplate = new SkillTemplate();
                skillTemplate.classId = nClassId;
                skillTemplate.id = rs.getByte("id");
                skillTemplate.name = rs.getString("name");
                skillTemplate.maxPoint = rs.getByte("max_point");
                skillTemplate.manaUseType = rs.getByte("mana_use_type");
                skillTemplate.type = rs.getByte("type");
                skillTemplate.iconId = rs.getShort("icon_id");
                skillTemplate.damInfo = rs.getString("dam_info");
                nClass.skillTemplatess.add(skillTemplate);

                dataArray = (JSONArray) JSONValue.parse(
                        rs.getString("skills")
                                .replaceAll("\\[\"", "[")
                                .replaceAll("\"\\[", "[")
                                .replaceAll("\"\\]", "]")
                                .replaceAll("\\]\"", "]")
                                .replaceAll("\\}\",\"\\{", "},{"));
                for (int j = 0; j < dataArray.size(); j++) {
                    JSONObject dts = (JSONObject) JSONValue.parse(String.valueOf(dataArray.get(j)));
                    Skill skill = new Skill();
                    skill.template = skillTemplate;
                    skill.skillId = Short.parseShort(String.valueOf(dts.get("id")));
                    skill.point = Byte.parseByte(String.valueOf(dts.get("point")));
                    skill.powRequire = Long.parseLong(String.valueOf(dts.get("power_require")));
                    skill.manaUse = Integer.parseInt(String.valueOf(dts.get("mana_use")));
                    skill.coolDown = Integer.parseInt(String.valueOf(dts.get("cool_down")));
                    skill.dx = Integer.parseInt(String.valueOf(dts.get("dx")));
                    skill.dy = Integer.parseInt(String.valueOf(dts.get("dy")));
                    skill.maxFight = Integer.parseInt(String.valueOf(dts.get("max_fight")));
                    skill.damage = Short.parseShort(String.valueOf(dts.get("damage")));
                    skill.price = Short.parseShort(String.valueOf(dts.get("price")));
                    skill.moreInfo = String.valueOf(dts.get("info"));
                    skillTemplate.skillss.add(skill);
                }
            }
            Logger.success("Successfully loaded skill (" + NCLASS.size() + ")\n");

            // load head avatar
            ps = con.prepareStatement("select * from head_avatar");
            rs = ps.executeQuery();
            while (rs.next()) {
                HeadAvatar headAvatar = new HeadAvatar(rs.getInt("head_id"), rs.getInt("avatar_id"));
                HEAD_AVATARS.add(headAvatar);
            }
            Logger.success("Successfully loaded head avatar (" + HEAD_AVATARS.size() + ")\n");

            // load flag bag
            ps = con.prepareStatement("select * from flag_bag");
            rs = ps.executeQuery();
            while (rs.next()) {
                FlagBag flagBag = new FlagBag();
                flagBag.id = rs.getInt("id");
                flagBag.name = rs.getString("name");
                flagBag.gold = rs.getInt("gold");
                flagBag.gem = rs.getInt("gem");
                flagBag.iconId = rs.getShort("icon_id");
                String[] iconData = rs.getString("icon_data").split(",");
                flagBag.iconEffect = new short[iconData.length];
                for (int j = 0; j < iconData.length; j++) {
                    flagBag.iconEffect[j] = Short.parseShort(iconData[j].trim());
                }
                FLAGS_BAGS.add(flagBag);
            }
            Logger.success("Successfully loaded flag bag (" + FLAGS_BAGS.size() + ")\n");

            // load intrinsic
            ps = con.prepareStatement("select * from intrinsic");
            rs = ps.executeQuery();
            while (rs.next()) {
                Intrinsic intrinsic = new Intrinsic();
                intrinsic.id = rs.getByte("id");
                intrinsic.name = rs.getString("name");
                intrinsic.paramFrom1 = rs.getShort("param_from_1");
                intrinsic.paramTo1 = rs.getShort("param_to_1");
                intrinsic.paramFrom2 = rs.getShort("param_from_2");
                intrinsic.paramTo2 = rs.getShort("param_to_2");
                intrinsic.icon = rs.getShort("icon");
                intrinsic.gender = rs.getByte("gender");
                switch (intrinsic.gender) {
                    case ConstPlayer.TRAI_DAT ->
                        INTRINSIC_TD.add(intrinsic);
                    case ConstPlayer.NAMEC ->
                        INTRINSIC_NM.add(intrinsic);
                    case ConstPlayer.XAYDA ->
                        INTRINSIC_XD.add(intrinsic);
                    default -> {
                        INTRINSIC_TD.add(intrinsic);
                        INTRINSIC_NM.add(intrinsic);
                        INTRINSIC_XD.add(intrinsic);
                    }
                }
                INTRINSICS.add(intrinsic);
            }
            Logger.success("Successfully loaded intrinsic (" + INTRINSICS.size() + ")\n");

            // load task
            ps = con.prepareStatement("SELECT id, task_main_template.name, detail, "
                    + "task_sub_template.name AS 'sub_name', max_count, notify, npc_id, map "
                    + "FROM task_main_template JOIN task_sub_template ON task_main_template.id = "
                    + "task_sub_template.task_main_id");
            rs = ps.executeQuery();
            int taskId = -1;
            TaskMain task = null;
            while (rs.next()) {
                int id = rs.getInt("id");
                if (id != taskId) {
                    taskId = id;
                    task = new TaskMain();
                    task.id = taskId;
                    task.name = rs.getString("name");
                    task.detail = rs.getString("detail");
                    TASKS.add(task);
                }
                SubTaskMain subTask = new SubTaskMain();
                subTask.name = rs.getString("sub_name");
                subTask.maxCount = rs.getShort("max_count");
                subTask.notify = rs.getString("notify");
                subTask.npcId = rs.getByte("npc_id");
                subTask.mapId = rs.getShort("map");
                task.subTasks.add(subTask);
            }
            Logger.success("Successfully loaded task (" + TASKS.size() + ")\n");

            // load side task
            ps = con.prepareStatement("select * from side_task_template");
            rs = ps.executeQuery();
            while (rs.next()) {
                SideTaskTemplate sideTask = new SideTaskTemplate();
                sideTask.id = rs.getInt("id");
                sideTask.name = rs.getString("name");
                String[] mc1 = rs.getString("max_count_lv1").split("-");
                String[] mc2 = rs.getString("max_count_lv2").split("-");
                String[] mc3 = rs.getString("max_count_lv3").split("-");
                String[] mc4 = rs.getString("max_count_lv4").split("-");
                String[] mc5 = rs.getString("max_count_lv5").split("-");
                sideTask.count[0][0] = Integer.parseInt(mc1[0]);
                sideTask.count[0][1] = Integer.parseInt(mc1[1]);
                sideTask.count[1][0] = Integer.parseInt(mc2[0]);
                sideTask.count[1][1] = Integer.parseInt(mc2[1]);
                sideTask.count[2][0] = Integer.parseInt(mc3[0]);
                sideTask.count[2][1] = Integer.parseInt(mc3[1]);
                sideTask.count[3][0] = Integer.parseInt(mc4[0]);
                sideTask.count[3][1] = Integer.parseInt(mc4[1]);
                sideTask.count[4][0] = Integer.parseInt(mc5[0]);
                sideTask.count[4][1] = Integer.parseInt(mc5[1]);
                SIDE_TASKS_TEMPLATE.add(sideTask);
            }
            Logger.success("Successfully loaded side task (" + SIDE_TASKS_TEMPLATE.size() + ")\n");

            // load clan task
            ps = con.prepareStatement("select * from clan_task_template");
            rs = ps.executeQuery();
            while (rs.next()) {
                ClanTaskTemplate clanTask = new ClanTaskTemplate();
                clanTask.id = rs.getInt("id");
                clanTask.name = rs.getString("name");
                String[] mc1 = rs.getString("max_count_lv1").split("-");
                String[] mc2 = rs.getString("max_count_lv2").split("-");
                String[] mc3 = rs.getString("max_count_lv3").split("-");
                String[] mc4 = rs.getString("max_count_lv4").split("-");
                String[] mc5 = rs.getString("max_count_lv5").split("-");
                clanTask.count[0][0] = Integer.parseInt(mc1[0]);
                clanTask.count[0][1] = Integer.parseInt(mc1[1]);
                clanTask.count[1][0] = Integer.parseInt(mc2[0]);
                clanTask.count[1][1] = Integer.parseInt(mc2[1]);
                clanTask.count[2][0] = Integer.parseInt(mc3[0]);
                clanTask.count[2][1] = Integer.parseInt(mc3[1]);
                clanTask.count[3][0] = Integer.parseInt(mc4[0]);
                clanTask.count[3][1] = Integer.parseInt(mc4[1]);
                clanTask.count[4][0] = Integer.parseInt(mc5[0]);
                clanTask.count[4][1] = Integer.parseInt(mc5[1]);
                CLAN_TASKS_TEMPLATE.add(clanTask);
            }
            Logger.success("Successfully loaded clan task (" + CLAN_TASKS_TEMPLATE.size() + ")\n");

            // load achievement template
            ps = con.prepareStatement("select * from achievement_template");
            rs = ps.executeQuery();
            while (rs.next()) {
                ACHIEVEMENT_TEMPLATE.add(new AchievementTemplate(rs.getString("info1"), rs.getString("info2"),
                        rs.getInt("money"), rs.getLong("max_count")));
            }
            Logger.success("Successfully loaded achievement (" + ACHIEVEMENT_TEMPLATE.size() + ")\n");

            // load item template
            ps = con.prepareStatement("select * from item_template order by id");
            rs = ps.executeQuery();
            while (rs.next()) {
                ItemTemplate itemTemp = new ItemTemplate();
                itemTemp.id = rs.getShort("id");
                if (itemTemp.id < 0) {
                    continue;
                }
                itemTemp.type = rs.getByte("type");
                itemTemp.gender = rs.getByte("gender");
                itemTemp.name = rs.getString("name");
                itemTemp.description = rs.getString("description");
                itemTemp.level = rs.getByte("level");
                itemTemp.iconID = rs.getShort("icon_id");
                itemTemp.part = rs.getShort("part");
                itemTemp.isUpToUp = rs.getBoolean("is_up_to_up");
                itemTemp.strRequire = rs.getInt("power_require");
                itemTemp.gold = rs.getInt("gold");
                itemTemp.gem = rs.getInt("gem");
                itemTemp.head = rs.getInt("head");
                itemTemp.body = rs.getInt("body");
                itemTemp.leg = rs.getInt("leg");
                ITEM_TEMPLATE_IDS.add((int) itemTemp.id);
                while (ITEM_TEMPLATES.size() < itemTemp.id) {
                    ITEM_TEMPLATES.add(createMissingItemTemplate((short) ITEM_TEMPLATES.size()));
                }
                if (ITEM_TEMPLATES.size() == itemTemp.id) {
                    ITEM_TEMPLATES.add(itemTemp);
                } else {
                    ITEM_TEMPLATES.set(itemTemp.id, itemTemp);
                }
            }
            Logger.success("Successfully loaded map item template (" + ITEM_TEMPLATES.size() + ")\n");

            // load item option template
            ps = con.prepareStatement("select id, name, type from item_option_template order by id");
            rs = ps.executeQuery();
            while (rs.next()) {
                ItemOptionTemplate optionTemp = new ItemOptionTemplate();
                optionTemp.id = rs.getInt("id");
                optionTemp.name = rs.getString("name");
                optionTemp.type = rs.getInt("type");
                ITEM_OPTION_TEMPLATES.add(optionTemp);
            }
            Logger.success("Successfully loaded map item option template (" + ITEM_OPTION_TEMPLATES.size() + ")\n");

            //load kol task
            ps = con.prepareStatement("select * from task_kol_template");
            rs = ps.executeQuery();
            while (rs.next()) {
                KOL_TASKS_TEMPLATE.add(new KolTaskTemplate(rs.getInt("id"), rs.getString("info"), rs.getInt("max_count")));
            }
            Logger.success("Successfully loaded KOL task (" + KOL_TASKS_TEMPLATE.size() + ")\n");

            // load shop
            SHOPS = ShopDAO.getShops(con);
            Logger.success("Successfully loaded shop (" + SHOPS.size() + ")\n");

            // load notify
            ps = con.prepareStatement("select * from notify order by id desc");
            rs = ps.executeQuery();
            while (rs.next()) {
                NOTIFY.add(rs.getString("name") + "<>" + rs.getString("text"));
            }
            Logger.success("Successfully loaded notify (" + NOTIFY.size() + ")\n");

            // load image by name
            ps = con.prepareStatement("select name, n_frame from img_by_name");
            rs = ps.executeQuery();
            while (rs.next()) {
                IMAGES_BY_NAME.put(rs.getString("name"), rs.getByte("n_frame"));
            }
            Logger.success("Successfully loaded images by name (" + IMAGES_BY_NAME.size() + ")\n");

            // Load mount
            for (ItemTemplate item : ITEM_TEMPLATES) {
                if (item.type == 23 && getNFrameImageByName("mount_" + item.part + "_0") != 0) {
                    MAP_MOUNT_NUM.put(item.id, (short) (item.part + 30000));
                }
            }

            Logger.success("Successfully loaded mount (" + MAP_MOUNT_NUM.size() + ")\n");

            PowerLimitManager.getInstance().load();
            CaptionManager.getInstance().load();

            // load mob template
            ps = con.prepareStatement("select * from mob_template");
            rs = ps.executeQuery();
            while (rs.next()) {
                MobTemplate mobTemp = new MobTemplate();
                mobTemp.id = rs.getByte("id");
                mobTemp.type = rs.getByte("type");
                mobTemp.name = rs.getString("name");
                mobTemp.hp = rs.getInt("hp");
                mobTemp.rangeMove = rs.getByte("range_move");
                mobTemp.speed = rs.getByte("speed");
                mobTemp.dartType = rs.getByte("dart_type");
                mobTemp.percentDame = rs.getByte("percent_dame");
                mobTemp.percentTiemNang = rs.getByte("percent_tiem_nang");
                MOB_TEMPLATES.add(mobTemp);
            }
            Logger.success("Successfully loaded mob template (" + MOB_TEMPLATES.size() + ")\n");

            // load npc template
            ps = con.prepareStatement("select * from npc_template");
            rs = ps.executeQuery();
            while (rs.next()) {
                NpcTemplate npcTemp = new NpcTemplate();
                npcTemp.id = rs.getByte("id");
                npcTemp.name = rs.getString("name");
                npcTemp.head = rs.getShort("head");
                npcTemp.body = rs.getShort("body");
                npcTemp.leg = rs.getShort("leg");
                npcTemp.avatar = rs.getInt("avatar");
                NPC_TEMPLATES.add(npcTemp);
            }
            Logger.success("Successfully loaded npc template (" + NPC_TEMPLATES.size() + ")\n");

            // load map template
            ps = con.prepareStatement("select count(id) from map_template");
            rs = ps.executeQuery();
            if (rs.next()) {
                int countRow = rs.getShort(1);
                MAP_TEMPLATES = new MapTemplate[countRow];
                ps = con.prepareStatement("select * from map_template");
                rs = ps.executeQuery();
                short i = 0;
                while (rs.next()) {
                    MapTemplate mapTemplate = new MapTemplate();
                    int mapId = rs.getInt("id");
                    String mapName = rs.getString("name");
                    mapTemplate.id = mapId;
                    mapTemplate.name = mapName;
                    mapTemplate.type = rs.getByte("type");
                    mapTemplate.planetId = rs.getByte("planet_id");
                    mapTemplate.bgType = rs.getByte("bg_type");
                    mapTemplate.tileId = rs.getByte("tile_id");
                    mapTemplate.bgId = rs.getByte("bg_id");
                    mapTemplate.zones = rs.getByte("zones");
                    mapTemplate.maxPlayerPerZone = rs.getByte("max_player");
                    // load waypoints
                    dataArray = (JSONArray) JSONValue.parse(rs.getString("waypoints")
                            .replaceAll("\\[\"\\[", "[[")
                            .replaceAll("\\]\"\\]", "]]")
                            .replaceAll("\",\"", ","));
                    for (int j = 0; j < dataArray.size(); j++) {
                        WayPoint wp = new WayPoint();
                        JSONArray dtwp = (JSONArray) JSONValue.parse(String.valueOf(dataArray.get(j)));
                        wp.name = String.valueOf(dtwp.get(0));
                        wp.minX = Short.parseShort(String.valueOf(dtwp.get(1)));
                        wp.minY = Short.parseShort(String.valueOf(dtwp.get(2)));
                        wp.maxX = Short.parseShort(String.valueOf(dtwp.get(3)));
                        wp.maxY = Short.parseShort(String.valueOf(dtwp.get(4)));
                        wp.isEnter = Byte.parseByte(String.valueOf(dtwp.get(5))) == 1;
                        wp.isOffline = Byte.parseByte(String.valueOf(dtwp.get(6))) == 1;
                        wp.goMap = Short.parseShort(String.valueOf(dtwp.get(7)));
                        wp.goX = Short.parseShort(String.valueOf(dtwp.get(8)));
                        wp.goY = Short.parseShort(String.valueOf(dtwp.get(9)));
                        mapTemplate.wayPoints.add(wp);
                        dtwp.clear();
                    }
                    dataArray.clear();
                    // load mobs
                    dataArray = (JSONArray) JSONValue.parse(rs.getString("mobs").replaceAll("\\\"", ""));
                    mapTemplate.mobTemp = new byte[dataArray.size()];
                    mapTemplate.mobLevel = new byte[dataArray.size()];
                    mapTemplate.mobHp = new long[dataArray.size()];
                    mapTemplate.mobX = new short[dataArray.size()];
                    mapTemplate.mobY = new short[dataArray.size()];
                    for (int j = 0; j < dataArray.size(); j++) {
                        JSONArray dtm = (JSONArray) JSONValue.parse(String.valueOf(dataArray.get(j)));
                        mapTemplate.mobTemp[j] = Byte.parseByte(String.valueOf(dtm.get(0)));
                        mapTemplate.mobLevel[j] = Byte.parseByte(String.valueOf(dtm.get(1)));
                        mapTemplate.mobHp[j] = Long.parseLong(String.valueOf(dtm.get(2)));
                        mapTemplate.mobX[j] = Short.parseShort(String.valueOf(dtm.get(3)));
                        mapTemplate.mobY[j] = Short.parseShort(String.valueOf(dtm.get(4)));
                        dtm.clear();
                    }
                    dataArray.clear();
                    // load npcs
                    dataArray = (JSONArray) JSONValue.parse(rs.getString("npcs").replaceAll("\\\"", ""));
                    mapTemplate.npcId = new byte[dataArray.size()];
                    mapTemplate.npcX = new short[dataArray.size()];
                    mapTemplate.npcY = new short[dataArray.size()];
                    for (int j = 0; j < dataArray.size(); j++) {
                        JSONArray dtn = (JSONArray) JSONValue.parse(String.valueOf(dataArray.get(j)));
                        mapTemplate.npcId[j] = Byte.parseByte(String.valueOf(dtn.get(0)));
                        mapTemplate.npcX[j] = Short.parseShort(String.valueOf(dtn.get(1)));
                        mapTemplate.npcY[j] = Short.parseShort(String.valueOf(dtn.get(2)));
                        dtn.clear();
                    }
                    dataArray.clear();
                    // load eff
                    dataArray = (JSONArray) JSONValue.parse(rs.getString("effect"));
                    for (int j = 0; j < dataArray.size(); j++) {
                        EffectMap em = new EffectMap();
                        dataObject = (JSONObject) JSONValue.parse(dataArray.get(j).toString());
                        em.setKey(String.valueOf(dataObject.get("key")));
                        em.setValue(String.valueOf(dataObject.get("value")));
                        mapTemplate.effectMaps.add(em);
                    }
//                    EffectMap em = new EffectMap();
//                    em.setKey("beff");
//                    em.setValue("15");
//                    mapTemplate.effectMaps.add(em);

                    dataArray.clear();
                    MAP_TEMPLATES[i++] = mapTemplate;
                }
                Logger.success("Successfully loaded map template (" + MAP_TEMPLATES.length + ")\n");
            }

            ensureMapDropRuleTable(con);
            loadMapDropRules(con);
            Logger.success("Successfully loaded map drop rules (" + MAP_DROP_RULES.size() + " maps)\n");

            ps = con.prepareStatement("select * from radar");
            rs = ps.executeQuery();
            while (rs.next()) {
                RadarCard rd = new RadarCard();
                rd.Id = rs.getShort("id");
                rd.IconId = rs.getShort("iconId");
                rd.Rank = rs.getByte("rank");
                rd.Max = rs.getByte("max");
                rd.Type = rs.getByte("type");
                rd.Template = rs.getShort("mob_id");
                rd.Name = rs.getString("name");
                rd.Info = rs.getString("info");
                JSONArray arr = (JSONArray) JSONValue.parse(rs.getString("body"));
                for (int i = 0; i < arr.size(); i++) {
                    JSONObject ob = (JSONObject) arr.get(i);
                    if (ob != null) {
                        rd.Head = Short.parseShort(ob.get("head").toString());
                        rd.Body = Short.parseShort(ob.get("body").toString());
                        rd.Leg = Short.parseShort(ob.get("leg").toString());
                        rd.Bag = Short.parseShort(ob.get("bag").toString());
                    }
                }
                rd.Options.clear();
                arr = (JSONArray) JSONValue.parse(rs.getString("options"));
                for (int i = 0; i < arr.size(); i++) {
                    JSONObject ob = (JSONObject) arr.get(i);
                    if (ob != null) {
                        rd.Options.add(new OptionCard(Integer.parseInt(ob.get("id").toString()),
                                Short.parseShort(ob.get("param").toString()),
                                Byte.parseByte(ob.get("activeCard").toString())));
                    }
                }
                // rd.Require = rs.getShort("require");
                // rd.RequireLevel = rs.getShort("require_level");
                rd.AuraId = rs.getShort("aura_id");
                RadarService.gI().RADAR_TEMPLATE.add(rd);
            }
            Logger.success("Successfully loaded radar template (" + RadarService.gI().RADAR_TEMPLATE.size() + ")\n");

            File directory = new File("data/icon/x4");
            if (directory.isDirectory()) {
                Optional<File> maxFile = Arrays.stream(directory.listFiles())
                        .filter(File::isFile)
                        .filter(file -> file.getName().endsWith(".png"))
                        .max(Comparator.comparingInt(file -> {
                            String name = file.getName();
                            return Integer.valueOf(name.substring(0, name.length() - 4));
                        }));
                if (maxFile.isPresent()) {
                    String fileName = maxFile.get().getName();
                    short maxVersion = Short.parseShort(fileName.substring(0, fileName.length() - 4));
                    DataGame.maxSmallVersion = (short) (maxVersion + 1);
                    Logger.success("Successfully loaded max small version (" + DataGame.maxSmallVersion + ")\n");
                }
            }

            // === LOAD TOPS ===
            try (Connection conTop = DBConnecter.getConnectionServer()) {
                topNV = realTop(ConstSQL.TOP_NV, conTop);
                Logger.success("Successfully loaded task top (" + topNV.size() + ")\n");

                topSM = realTop(ConstSQL.TOP_SM, conTop);
                Logger.success("Successfully loaded power top (" + topSM.size() + ")\n");

                topPhaoBong = realTop(ConstSQL.TOP_PHAO_BONG, conTop);
                Logger.success("Successfully loaded pháo bông top (" + topPhaoBong.size() + ")\n");

                topLixi = realTop(ConstSQL.TOP_LIXI, conTop);
                Logger.success("Successfully loaded lì xì top (" + topLixi.size() + ")\n");

                topSanBoss = realTop(ConstSQL.TOP_SB, conTop);
                Logger.success("Successfully loaded kill boss top (" + topSanBoss.size() + ")\n");

                topNap = realTop(ConstSQL.TOP_DUA_NAP, conTop);
                Logger.success("Successfully loaded nạp top (" + topNap.size() + ")\n");

                topWHIS = realTop(ConstSQL.TOP_WHIS, conTop);
                Logger.success("Successfully loaded WHIS top (" + topWHIS.size() + ")\n");

                topVDST = realTop(ConstSQL.TOP_VDST, conTop);
                Logger.success("Successfully loaded VDST top (" + topVDST.size() + ")\n");

                topSSM = realTop(ConstSQL.TOP_SO_SU_MENH, conTop);
                Manager.topSSM = topSSM;
                Logger.success("Successfully loaded topSSM top (" + topSSM.size() + ")\n");

            } catch (Exception e) {
                Logger.logException(Manager.class, e, "Error loading top data");
            }
            Manager.timeRealTop = System.currentTimeMillis();

        } catch (Exception e) {
            Logger.logException(Manager.class, e, "Database loading error");
            System.exit(0);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
            } catch (SQLException ex) {
            }
        }

        Logger.log(Logger.PURPLE, "Total database loading time: " + (System.currentTimeMillis() - st) + " (ms)\n");
    }

    public synchronized boolean updateShop() {
        try (Connection con = DBConnecter.getConnectionServer();) {
            SHOPS = ShopDAO.getShops(con);
            System.out.println("[AdminRuntime] Reload shop thanh cong. Total shops: " + SHOPS.size());
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public static List<TOP> realTop(String query, Connection con) {
        int i = 0;
        List<TOP> tops = new ArrayList<>();
        JSONArray dataArray;
        try {
            PreparedStatement ps = con.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                short head = Util.getHead((byte) rs.getInt("gender"));
                short body = (short) (rs.getInt("gender") == 1 ? 59 : 57);
                short leg = (short) (rs.getInt("gender") == 1 ? 60 : 58);
                dataArray = (JSONArray) JSONValue.parse(rs.getString("items_body"));
                JSONArray dataItem = (JSONArray) JSONValue.parse(dataArray.get(0).toString());
                if (dataItem != null && dataItem.get(0) != null) {
                    Item item;
                    short tempId = Short.parseShort(String.valueOf(dataItem.get(0)));
                    if (tempId != -1) {
                        item = ItemService.gI().createNewItem(tempId,
                                Integer.parseInt(String.valueOf(dataItem.get(1))));
                        body = (short) item.template.part;
                    }
                }
                dataItem = (JSONArray) JSONValue.parse(dataArray.get(1).toString());
                if (dataItem != null && dataItem.get(0) != null) {
                    Item item;
                    short tempId = Short.parseShort(String.valueOf(dataItem.get(0)));
                    if (tempId != -1) {
                        item = ItemService.gI().createNewItem(tempId,
                                Integer.parseInt(String.valueOf(dataItem.get(1))));
                        leg = (short) item.template.part;
                    }
                }
                dataItem = (JSONArray) JSONValue.parse(dataArray.get(5).toString());
                if (dataItem != null && dataItem.get(0) != null) {
                    Item item;
                    short tempId = Short.parseShort(String.valueOf(dataItem.get(0)));
                    if (tempId != -1) {
                        item = ItemService.gI().createNewItem(tempId,
                                Integer.parseInt(String.valueOf(dataItem.get(1))));
                        if (item.template.head != -1) {
                            head = (short) item.template.head;
                        }
                        if (item.template.body != -1) {
                            body = (short) item.template.body;
                        }
                        if (item.template.leg != -1) {
                            leg = (short) item.template.leg;
                        }
                    }
                }
                dataArray.clear();
                TOP top = TOP.builder().name(rs.getString("name")).gender(rs.getByte("gender")).head(head).body(body)
                        .leg(leg).build();
                switch (query) {
                    case ConstSQL.TOP_NV -> {
                        top.setNv(rs.getByte("nv"));
                        top.setSubnv(rs.getByte("subnv"));
                        top.setLasttime(rs.getLong("lasttime"));
                    }
                    case ConstSQL.TOP_DC -> {
                        top.setDicanh(rs.getInt("dicanh"));
                        top.setJuventus(rs.getInt("juventus"));
                    }

                    case ConstSQL.TOP_SM -> {
                        top.setPower(rs.getLong("sm"));
                    }
                    case ConstSQL.TOP_PHAO_BONG -> {
                        top.setPhaobong(rs.getInt("phaobong"));
                    }
                    case ConstSQL.TOP_LIXI -> {
                        top.setLixi(rs.getInt("lixi"));
                    }
                    case ConstSQL.TOP_SB -> {
                        top.setPointsBoss(rs.getInt("event_point_boss"));
                    }
                    // case ConstSQL.TOP_DUA_SM -> {
                    // top.setPower(rs.getLong("sm"));
                    // }
                    case ConstSQL.TOP_DUA_NAP -> {
                        top.setCash(rs.getInt("danap"));
                    }
                    case ConstSQL.TOP_SO_SU_MENH -> {
                        top.setCash(rs.getInt("point_value"));
                    }
                    // case ConstSQL.TOP_DUA_QUOC_VUONG -> {
                    // top.setThoivang(rs.getInt("thoi_vang"));
                    // }
                    case ConstSQL.TOP_WHIS -> {
                        top.setLasttime(rs.getLong("lasttime"));
                        top.setLevel(rs.getInt("top"));
                        top.setTime(rs.getInt("time"));

                        // switch (i) {
                        // case 0 ->
                        // top1Whis = rs.getLong("id");
                        // case 1 ->
                        // top2Whis = rs.getLong("id");
                        // case 2 ->
                        // top3Whis = rs.getLong("id");
                        // }
                        i++;
                    }
                    case ConstSQL.TOP_VDST -> {
                        top.setDivdst(rs.getInt("time"));
                        top.setLasttime(rs.getLong("lasttime"));
                        i++;
                    }
                }
                tops.add(top);
            }
        } catch (NumberFormatException | SQLException e) {
            e.printStackTrace();
        }
        return tops;
    }

    public void loadProperties() throws IOException {
        Properties properties = new Properties();
        properties.load(new FileInputStream("data/config/config.properties"));
        Object value;
        if ((value = properties.get("server.sv")) != null) {
            SERVER = Byte.parseByte(String.valueOf(value));
        }
        if ((value = properties.get("server.name")) != null) {
            String name = String.valueOf(value);
            ServerManager.NAME = name;
        }
        if ((value = properties.get("server.port")) != null) {
            ServerManager.PORT = Integer.parseInt(String.valueOf(value));
        }
        String linkServer = "";
        if ((value = properties.get("server.ip")) != null) {
            ServerManager.IP = String.valueOf(value);
            linkServer += ServerManager.NAME + ":" + ServerManager.IP + ":" + ServerManager.PORT + ":0,";
        }
        for (int i = 1; i <= 10; i++) {
            value = properties.get("server.sv" + i);
            if (value != null) {
                linkServer += String.valueOf(value) + ":0,";
            }
        }
        DataGame.LINK_IP_PORT = linkServer.substring(0, linkServer.length() - 1);
        if ((value = properties.get("server.waitlogin")) != null) {
            SECOND_WAIT_LOGIN = Byte.parseByte(String.valueOf(value));
        }
        if ((value = properties.get("server.maxperip")) != null) {
            MAX_PER_IP = Integer.parseInt(String.valueOf(value));
        }
        if ((value = properties.get("server.maxplayer")) != null) {
            MAX_PLAYER = Integer.parseInt(String.valueOf(value));
        }
        if ((value = properties.get("server.debug")) != null) {
            DEBUG = Byte.parseByte(String.valueOf(value)) != 0;
        }
        if ((value = properties.get("server.expserver")) != null) {
            RATE_EXP_SERVER = Byte.parseByte(String.valueOf(value));
        }
        if ((value = properties.get("server.local")) != null) {
            LOCAL = String.valueOf(value).toLowerCase().equals("true");
        }
        if ((value = properties.get("server.test")) != null) {
            TEST = String.valueOf(value).toLowerCase().equals("true");
        }
        if ((value = properties.get("server.daoautoupdater")) != null) {
            DAO_AUTO_UPDATER = String.valueOf(value).toLowerCase().equals("true");
        }
    }

    /**
     * @param tileTypeFocus tile type: top, bot, left, right...
     * @return [tileMapId][tileType]
     */
    private int[][] readTileIndexTileType(int tileTypeFocus) {
        int[][] tileIndexTileType = null;
        try {
            DataInputStream dis = new DataInputStream(new FileInputStream("data/map/tile_set_info"));
            int numTileMap = dis.readByte();
            tileIndexTileType = new int[numTileMap][];
            for (int i = 0; i < numTileMap; i++) {
                int numTileOfMap = dis.readByte();
                for (int j = 0; j < numTileOfMap; j++) {
                    int tileType = dis.readInt();
                    int numIndex = dis.readByte();
                    if (tileType == tileTypeFocus) {
                        tileIndexTileType[i] = new int[numIndex];
                    }
                    for (int k = 0; k < numIndex; k++) {
                        int typeIndex = dis.readByte();
                        if (tileType == tileTypeFocus) {
                            tileIndexTileType[i][k] = typeIndex;

                        }
                    }
                }
            }
        } catch (IOException e) {
            Logger.logException(MapService.class, e);
        }
        return tileIndexTileType;
    }

    /**
     * @param mapId mapId
     * @return tile map for paint
     */
    private int[][] readTileMap(int mapId) {
        int[][] tileMap = null;
        try {
            try (DataInputStream dis = new DataInputStream(new FileInputStream("data/map/tile_map_data/" + mapId))) {
                int w = dis.readByte();
                int h = dis.readByte();
                tileMap = new int[h][w];
                for (int[] tm : tileMap) {
                    for (int j = 0; j < tm.length; j++) {
                        tm[j] = dis.readByte();
                    }
                }
            }
        } catch (IOException e) {
        }
        return tileMap;
    }

    public static Clan getClanById(int id) throws Exception {
        for (Clan clan : CLANS) {
            if (clan.id == id) {
                return clan;
            }
        }
        throw new Exception("Không tìm thấy clan id: " + id);
    }

    public static void addClan(Clan clan) {
        CLANS.add(clan);
    }

    public static int getNumClan() {
        return CLANS.size();

    }

    public static synchronized String runtimeMapMobsJson() {
        StringBuilder json = new StringBuilder("{");
        json.append("\"maps\":[");
        if (MAP_TEMPLATES != null) {
            for (int i = 0; i < MAP_TEMPLATES.length; i++) {
                if (i > 0) {
                    json.append(',');
                }
                json.append(runtimeMapTemplateJson(MAP_TEMPLATES[i]));
            }
        }
        json.append("],\"mob_templates\":").append(runtimeMobTemplatesJson()).append('}');
        return json.toString();
    }

    public static synchronized String runtimeSaveMapMobs(JSONObject body) {
        int mapId = intValue(body.get("map_id"), -1);
        MapTemplate template = findMapTemplate(mapId);
        if (template == null) {
            return "{\"saved\":false,\"message\":\"Khong tim thay map\"}";
        }
        int maxPlayer = Math.max(1, Math.min(100, intValue(body.get("max_player"), template.maxPlayerPerZone)));
        int zonesConfig = Math.max(1, Math.min(120, intValue(body.get("zones"), template.zones)));
        Object rawMobs = normalizeJsonBodyValue(body.get("mobs"));
        Object rawWayPoints = normalizeJsonBodyValue(body.get("waypoints"));
        Object rawNpcs = normalizeJsonBodyValue(body.get("npcs"));
        Object rawDropRules = normalizeJsonBodyValue(body.get("drop_rules"));
        if (!(rawMobs instanceof JSONArray)) {
            return "{\"saved\":false,\"message\":\"Du lieu mobs khong hop le\"}";
        }
        JSONArray mobs = (JSONArray) rawMobs;

        byte[] mobTemp = new byte[mobs.size()];
        byte[] mobLevel = new byte[mobs.size()];
        long[] mobHp = new long[mobs.size()];
        short[] mobX = new short[mobs.size()];
        short[] mobY = new short[mobs.size()];
        JSONArray persistMobs = new JSONArray();
        java.util.Map<Integer, Integer> mobPercentDameUpdates = new java.util.LinkedHashMap<>();
        List<WayPoint> wayPoints;
        RuntimeNpcData npcData;
        List<RuntimeMapDropRule> dropRules;
        try {
            wayPoints = rawWayPoints == null
                    ? cloneWayPoints(template.wayPoints)
                    : runtimeWayPointsFromPayload(rawWayPoints);
            npcData = rawNpcs == null
                    ? runtimeNpcDataFromTemplate(template)
                    : runtimeNpcDataFromPayload(rawNpcs);
            dropRules = rawDropRules == null
                    ? cloneMapDropRules(mapId)
                    : runtimeMapDropRulesFromPayload(mapId, rawDropRules);
        } catch (Exception e) {
            return "{\"saved\":false,\"message\":\"" + escapeJson(e.getMessage()) + "\"}";
        }

        for (int i = 0; i < mobs.size(); i++) {
            JSONObject mob = mapMobObject(mobs.get(i));
            int tempId = intValue(mob.get("temp_id"), intValue(mob.get("temp"), -1));
            if (tempId < Byte.MIN_VALUE || tempId > Byte.MAX_VALUE) {
                return "{\"saved\":false,\"message\":\"Mob template vuot gioi han map mob: " + tempId + "\"}";
            }
            if (getMobTemplateByTemp(tempId) == null) {
                return "{\"saved\":false,\"message\":\"Mob template khong ton tai: " + tempId + "\"}";
            }
            int level = Math.max(0, Math.min(127, intValue(mob.get("level"), 1)));
            long hp = Math.max(1, longValue(mob.get("hp"), 1));
            int x = Math.max(0, Math.min(Short.MAX_VALUE, intValue(mob.get("x"), 0)));
            int y = Math.max(0, Math.min(Short.MAX_VALUE, intValue(mob.get("y"), 0)));
            int percentDame = Math.max(0, Math.min(100, intValue(mob.get("percent_dame"),
                    intValue(mob.get("dame"), mobTemplatePercentDame(tempId)))));

            mobTemp[i] = (byte) tempId;
            mobLevel[i] = (byte) level;
            mobHp[i] = hp;
            mobX[i] = (short) x;
            mobY[i] = (short) y;
            mobPercentDameUpdates.put(tempId, percentDame);

            JSONArray row = new JSONArray();
            row.add(tempId);
            row.add(level);
            row.add(hp);
            row.add(x);
            row.add(y);
            persistMobs.add(row);
        }

        String persistWayPoints = serializeWayPoints(wayPoints);
        String persistNpcs = serializeNpcs(npcData);

        template.mobTemp = mobTemp;
        template.mobLevel = mobLevel;
        template.mobHp = mobHp;
        template.mobX = mobX;
        template.mobY = mobY;
        template.maxPlayerPerZone = (byte) maxPlayer;
        template.zones = (byte) zonesConfig;
        template.wayPoints = wayPoints;
        template.npcId = npcData.npcId;
        template.npcX = npcData.npcX;
        template.npcY = npcData.npcY;

        try (Connection con = DBConnecter.getConnectionServer(); PreparedStatement mapPs = con.prepareStatement("update map_template set mobs = ?, max_player = ?, zones = ?, waypoints = ?, npcs = ? where id = ?"); PreparedStatement mobTemplatePs = con.prepareStatement("update mob_template set percent_dame = ? where id = ?"); PreparedStatement deleteDropRulePs = con.prepareStatement("delete from map_drop_rule where map_id = ?"); PreparedStatement insertDropRulePs = con.prepareStatement("insert into map_drop_rule (map_id, item_id, quantity_min, quantity_max, chance_numerator, chance_denominator, mob_temp_id, options_text, active, note) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
            con.setAutoCommit(false);
            ensureMapDropRuleTable(con);

            mapPs.setString(1, persistMobs.toJSONString());
            mapPs.setInt(2, maxPlayer);
            mapPs.setInt(3, zonesConfig);
            mapPs.setString(4, persistWayPoints);
            mapPs.setString(5, persistNpcs);
            mapPs.setInt(6, mapId);
            mapPs.executeUpdate();

            for (java.util.Map.Entry<Integer, Integer> entry : mobPercentDameUpdates.entrySet()) {
                mobTemplatePs.setInt(1, entry.getValue());
                mobTemplatePs.setInt(2, entry.getKey());
                mobTemplatePs.addBatch();
                updateMobTemplatePercentDame(entry.getKey(), entry.getValue());
            }
            if (!mobPercentDameUpdates.isEmpty()) {
                mobTemplatePs.executeBatch();
            }

            if (rawDropRules != null) {
                deleteDropRulePs.setInt(1, mapId);
                deleteDropRulePs.executeUpdate();
                for (RuntimeMapDropRule rule : dropRules) {
                    insertDropRulePs.setInt(1, mapId);
                    insertDropRulePs.setInt(2, rule.itemId);
                    insertDropRulePs.setInt(3, rule.quantityMin);
                    insertDropRulePs.setInt(4, rule.quantityMax);
                    insertDropRulePs.setInt(5, rule.chanceNumerator);
                    insertDropRulePs.setInt(6, rule.chanceDenominator);
                    if (rule.mobTempId == null) {
                        insertDropRulePs.setNull(7, Types.INTEGER);
                    } else {
                        insertDropRulePs.setInt(7, rule.mobTempId);
                    }
                    insertDropRulePs.setString(8, serializeItemOptions(rule.options));
                    insertDropRulePs.setInt(9, rule.active ? 1 : 0);
                    insertDropRulePs.setString(10, rule.note);
                    insertDropRulePs.addBatch();
                }
                if (!dropRules.isEmpty()) {
                    insertDropRulePs.executeBatch();
                }
                MAP_DROP_RULES.put(mapId, dropRules);
            }
            con.commit();
        } catch (Exception e) {
            Logger.logException(Manager.class, e);
            return "{\"saved\":false,\"message\":\"Luu map_template.mobs that bai\"}";
        }

        map.Map runtimeMap = findRuntimeMap(mapId);
        if (runtimeMap != null) {
            runtimeMap.updateZoneMaxPlayers(maxPlayer);
            runtimeMap.reloadMobs(mobTemp, mobLevel, mobHp, mobX, mobY);
            runtimeMap.reloadWayPoints(wayPoints);
            runtimeMap.reloadNpcs(npcData.npcId, npcData.npcX, npcData.npcY);
        }

        return "{\"saved\":true,\"map\":" + runtimeMapTemplateJson(template) + "}";
    }

    private static String runtimeMapTemplateJson(MapTemplate template) {
        if (template == null) {
            return "{}";
        }
        map.Map runtimeMap = findRuntimeMap(template.id);
        int zoneCount = runtimeMap != null && runtimeMap.zones != null ? runtimeMap.zones.size() : template.zones;
        int playerCount = 0;
        if (runtimeMap != null && runtimeMap.zones != null) {
            for (Zone zone : runtimeMap.zones) {
                playerCount += zone.getNumOfPlayers();
            }
        }
        return new StringBuilder("{")
                .append("\"id\":").append(template.id).append(',')
                .append("\"name\":\"").append(escapeJson(template.name)).append("\",")
                .append("\"planet_id\":").append(template.planetId).append(',')
                .append("\"type\":").append(template.type).append(',')
                .append("\"bg_id\":").append(template.bgId).append(',')
                .append("\"bg_type\":").append(template.bgType).append(',')
                .append("\"tile_id\":").append(template.tileId).append(',')
                .append("\"zones\":").append(zoneCount).append(',')
                .append("\"zones_config\":").append(template.zones).append(',')
                .append("\"zones_forced\":").append(isZoneCountForcedByType(template.type)).append(',')
                .append("\"max_player\":").append(template.maxPlayerPerZone).append(',')
                .append("\"player_count\":").append(playerCount).append(',')
                .append("\"width\":").append(runtimeMap != null ? runtimeMap.mapWidth : 0).append(',')
                .append("\"height\":").append(runtimeMap != null ? runtimeMap.mapHeight : 0).append(',')
                .append("\"mob_count\":").append(template.mobTemp == null ? 0 : template.mobTemp.length).append(',')
                .append("\"waypoints\":").append(runtimeWaypointsJson(template)).append(',')
                .append("\"npcs\":").append(runtimeNpcsJson(template)).append(',')
                .append("\"fixed_items\":").append(runtimeFixedItemsJson(template.id)).append(',')
                .append("\"drop_rules\":").append(runtimeMapDropRulesJson(template.id)).append(',')
                .append("\"mobs\":").append(runtimeMapMobsJson(template, runtimeMap))
                .append('}')
                .toString();
    }

    private static String runtimeWaypointsJson(MapTemplate template) {
        StringBuilder json = new StringBuilder("[");
        if (template.wayPoints != null) {
            for (int i = 0; i < template.wayPoints.size(); i++) {
                if (i > 0) {
                    json.append(',');
                }
                WayPoint wayPoint = template.wayPoints.get(i);
                json.append('{')
                        .append("\"name\":\"").append(escapeJson(wayPoint.name)).append("\",")
                        .append("\"min_x\":").append(wayPoint.minX).append(',')
                        .append("\"min_y\":").append(wayPoint.minY).append(',')
                        .append("\"max_x\":").append(wayPoint.maxX).append(',')
                        .append("\"max_y\":").append(wayPoint.maxY).append(',')
                        .append("\"go_map\":").append(wayPoint.goMap).append(',')
                        .append("\"go_x\":").append(wayPoint.goX).append(',')
                        .append("\"go_y\":").append(wayPoint.goY).append(',')
                        .append("\"is_enter\":").append(wayPoint.isEnter).append(',')
                        .append("\"is_offline\":").append(wayPoint.isOffline)
                        .append('}');
            }
        }
        json.append(']');
        return json.toString();
    }

    private static String runtimeNpcsJson(MapTemplate template) {
        StringBuilder json = new StringBuilder("[");
        int count = template.npcId == null ? 0 : template.npcId.length;
        for (int i = 0; i < count; i++) {
            if (i > 0) {
                json.append(',');
            }
            NpcTemplate npcTemplate = getNpcTemplateByTemp(template.npcId[i]);
            json.append('{')
                    .append("\"id\":").append(template.npcId[i]).append(',')
                    .append("\"name\":\"").append(escapeJson(npcTemplate == null ? "" : npcTemplate.name)).append("\",")
                    .append("\"avatar\":").append(npcTemplate == null ? -1 : npcTemplate.avatar).append(',')
                    .append("\"head\":").append(npcTemplate == null ? -1 : npcTemplate.head).append(',')
                    .append("\"body\":").append(npcTemplate == null ? -1 : npcTemplate.body).append(',')
                    .append("\"leg\":").append(npcTemplate == null ? -1 : npcTemplate.leg).append(',')
                    .append("\"x\":").append(template.npcX[i]).append(',')
                    .append("\"y\":").append(template.npcY[i])
                    .append('}');
        }
        json.append(']');
        return json.toString();
    }

    private static String runtimeFixedItemsJson(int mapId) {
        int[][] fixedItems;
        switch (mapId) {
            case 21 ->
                fixedItems = new int[][]{{74, 1, 633, 315}};
            case 22 ->
                fixedItems = new int[][]{{74, 1, 56, 315}};
            case 23 ->
                fixedItems = new int[][]{{74, 1, 633, 320}};
            case 42 ->
                fixedItems = new int[][]{{78, 1, 70, 288}};
            case 43 ->
                fixedItems = new int[][]{{78, 1, 70, 264}};
            case 44 ->
                fixedItems = new int[][]{{78, 1, 70, 288}};
            case 85 ->
                fixedItems = new int[][]{{372, 1, 0, 0}};
            case 86 ->
                fixedItems = new int[][]{{373, 1, 0, 0}};
            case 87 ->
                fixedItems = new int[][]{{374, 1, 0, 0}};
            case 88 ->
                fixedItems = new int[][]{{375, 1, 0, 0}};
            case 89 ->
                fixedItems = new int[][]{{376, 1, 0, 0}};
            case 90 ->
                fixedItems = new int[][]{{377, 1, 0, 0}};
            case 91 ->
                fixedItems = new int[][]{{378, 1, 0, 0}};
            default ->
                fixedItems = new int[0][];
        }
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < fixedItems.length; i++) {
            if (i > 0) {
                json.append(',');
            }
            int[] row = fixedItems[i];
            ItemTemplate itemTemplate = getItemTemplateByTemp(row[0]);
            json.append('{')
                    .append("\"id\":").append(row[0]).append(',')
                    .append("\"name\":\"").append(escapeJson(itemTemplate == null ? "" : itemTemplate.name)).append("\",")
                    .append("\"icon_id\":").append(itemTemplate == null ? -1 : itemTemplate.iconID).append(',')
                    .append("\"quantity\":").append(row[1]).append(',')
                    .append("\"x\":").append(row[2]).append(',')
                    .append("\"y\":").append(row[3]).append(',')
                    .append("\"per_zone\":true")
                    .append('}');
        }
        json.append(']');
        return json.toString();
    }

    private static String runtimeMapDropRulesJson(int mapId) {
        List<RuntimeMapDropRule> rules = MAP_DROP_RULES.get(mapId);
        if (rules == null || rules.isEmpty()) {
            return "[]";
        }
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < rules.size(); i++) {
            if (i > 0) {
                json.append(',');
            }
            RuntimeMapDropRule rule = rules.get(i);
            ItemTemplate itemTemplate = getItemTemplateByTemp(rule.itemId);
            MobTemplate mobTemplate = rule.mobTempId == null ? null : getMobTemplateByTemp(rule.mobTempId);
            json.append('{')
                    .append("\"item_id\":").append(rule.itemId).append(',')
                    .append("\"item_name\":\"").append(escapeJson(itemTemplate == null ? "" : itemTemplate.name)).append("\",")
                    .append("\"icon_id\":").append(itemTemplate == null ? -1 : itemTemplate.iconID).append(',')
                    .append("\"quantity_min\":").append(rule.quantityMin).append(',')
                    .append("\"quantity_max\":").append(rule.quantityMax).append(',')
                    .append("\"chance_numerator\":").append(rule.chanceNumerator).append(',')
                    .append("\"chance_denominator\":").append(rule.chanceDenominator).append(',')
                    .append("\"mob_temp_id\":").append(rule.mobTempId == null ? -1 : rule.mobTempId).append(',')
                    .append("\"mob_name\":\"").append(escapeJson(mobTemplate == null ? "" : mobTemplate.name)).append("\",")
                    .append("\"active\":").append(rule.active).append(',')
                    .append("\"note\":\"").append(escapeJson(rule.note)).append("\",")
                    .append("\"options\":").append(runtimeItemOptionsJson(rule.options))
                    .append('}');
        }
        json.append(']');
        return json.toString();
    }

    private static String runtimeMapMobsJson(MapTemplate template, map.Map runtimeMap) {
        StringBuilder json = new StringBuilder("[");
        int count = template.mobTemp == null ? 0 : template.mobTemp.length;
        for (int i = 0; i < count; i++) {
            if (i > 0) {
                json.append(',');
            }
            int tempId = template.mobTemp[i];
            MobTemplate mobTemplate = getMobTemplateByTemp(tempId);
            mob.Mob liveMob = firstZoneMob(runtimeMap, i);
            json.append('{')
                    .append("\"index\":").append(i).append(',')
                    .append("\"temp_id\":").append(tempId).append(',')
                    .append("\"name\":\"").append(escapeJson(mobTemplate == null ? "" : mobTemplate.name)).append("\",")
                    .append("\"type\":").append(mobTemplate == null ? 0 : mobTemplate.type).append(',')
                    .append("\"level\":").append(template.mobLevel[i]).append(',')
                    .append("\"hp\":").append(template.mobHp[i]).append(',')
                    .append("\"x\":").append(template.mobX[i]).append(',')
                    .append("\"y\":").append(template.mobY[i]).append(',')
                    .append("\"template_hp\":").append(mobTemplate == null ? 0 : mobTemplate.hp).append(',')
                    .append("\"percent_dame\":").append(mobTemplate == null ? 0 : mobTemplate.percentDame).append(',')
                    .append("\"percent_tiem_nang\":").append(mobTemplate == null ? 0 : mobTemplate.percentTiemNang).append(',')
                    .append("\"live_hp\":").append(liveMob != null ? liveMob.point.gethp() : 0).append(',')
                    .append("\"live_status\":").append(liveMob != null ? liveMob.status : -1).append(',')
                    .append("\"alive\":").append(liveMob != null && !liveMob.isDie())
                    .append('}');
        }
        json.append(']');
        return json.toString();
    }

    private static String runtimeMobTemplatesJson() {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < MOB_TEMPLATES.size(); i++) {
            if (i > 0) {
                json.append(',');
            }
            MobTemplate mob = MOB_TEMPLATES.get(i);
            json.append('{')
                    .append("\"id\":").append(mob.id).append(',')
                    .append("\"name\":\"").append(escapeJson(mob.name)).append("\",")
                    .append("\"type\":").append(mob.type).append(',')
                    .append("\"hp\":").append(mob.hp).append(',')
                    .append("\"range_move\":").append(mob.rangeMove).append(',')
                    .append("\"speed\":").append(mob.speed).append(',')
                    .append("\"dart_type\":").append(mob.dartType).append(',')
                    .append("\"percent_dame\":").append(mob.percentDame).append(',')
                    .append("\"percent_tiem_nang\":").append(mob.percentTiemNang)
                    .append('}');
        }
        json.append(']');
        return json.toString();
    }

    private static JSONObject mapMobObject(Object raw) {
        if (raw instanceof JSONObject) {
            return (JSONObject) raw;
        }
        JSONObject object = new JSONObject();
        if (raw instanceof JSONArray) {
            JSONArray row = (JSONArray) raw;
            object.put("temp_id", row.size() > 0 ? row.get(0) : 0);
            object.put("level", row.size() > 1 ? row.get(1) : 1);
            object.put("hp", row.size() > 2 ? row.get(2) : 1);
            object.put("x", row.size() > 3 ? row.get(3) : 0);
            object.put("y", row.size() > 4 ? row.get(4) : 0);
        }
        return object;
    }

    private static Object normalizeJsonBodyValue(Object value) {
        if (value instanceof String) {
            return JSONValue.parse(String.valueOf(value));
        }
        return value;
    }

    private static List<WayPoint> cloneWayPoints(List<WayPoint> source) {
        List<WayPoint> wayPoints = new ArrayList<>();
        if (source == null) {
            return wayPoints;
        }
        for (WayPoint origin : source) {
            if (origin == null) {
                continue;
            }
            WayPoint clone = new WayPoint();
            clone.name = origin.name;
            clone.minX = origin.minX;
            clone.minY = origin.minY;
            clone.maxX = origin.maxX;
            clone.maxY = origin.maxY;
            clone.isEnter = origin.isEnter;
            clone.isOffline = origin.isOffline;
            clone.goMap = origin.goMap;
            clone.goX = origin.goX;
            clone.goY = origin.goY;
            wayPoints.add(clone);
        }
        return wayPoints;
    }

    private static List<WayPoint> runtimeWayPointsFromPayload(Object raw) {
        List<WayPoint> wayPoints = new ArrayList<>();
        if (!(raw instanceof JSONArray)) {
            return wayPoints;
        }
        JSONArray rows = (JSONArray) raw;
        for (Object entry : rows) {
            JSONObject row = entry instanceof JSONObject ? (JSONObject) entry : new JSONObject();
            if (!(entry instanceof JSONObject) && entry instanceof JSONArray) {
                JSONArray array = (JSONArray) entry;
                row.put("name", array.size() > 0 ? array.get(0) : "");
                row.put("min_x", array.size() > 1 ? array.get(1) : 0);
                row.put("min_y", array.size() > 2 ? array.get(2) : 0);
                row.put("max_x", array.size() > 3 ? array.get(3) : 0);
                row.put("max_y", array.size() > 4 ? array.get(4) : 0);
                row.put("is_enter", array.size() > 5 ? array.get(5) : 0);
                row.put("is_offline", array.size() > 6 ? array.get(6) : 0);
                row.put("go_map", array.size() > 7 ? array.get(7) : 0);
                row.put("go_x", array.size() > 8 ? array.get(8) : 0);
                row.put("go_y", array.size() > 9 ? array.get(9) : 0);
            }

            WayPoint wayPoint = new WayPoint();
            wayPoint.name = String.valueOf(row.getOrDefault("name", ""));
            wayPoint.minX = (short) Math.max(0, Math.min(Short.MAX_VALUE, intValue(row.get("min_x"), 0)));
            wayPoint.minY = (short) Math.max(0, Math.min(Short.MAX_VALUE, intValue(row.get("min_y"), 0)));
            wayPoint.maxX = (short) Math.max(0, Math.min(Short.MAX_VALUE, intValue(row.get("max_x"), 0)));
            wayPoint.maxY = (short) Math.max(0, Math.min(Short.MAX_VALUE, intValue(row.get("max_y"), 0)));
            wayPoint.isEnter = intValue(row.get("is_enter"), 0) == 1 || Boolean.parseBoolean(String.valueOf(row.get("is_enter")));
            wayPoint.isOffline = intValue(row.get("is_offline"), 0) == 1 || Boolean.parseBoolean(String.valueOf(row.get("is_offline")));
            wayPoint.goMap = intValue(row.get("go_map"), 0);
            wayPoint.goX = (short) Math.max(0, Math.min(Short.MAX_VALUE, intValue(row.get("go_x"), 0)));
            wayPoint.goY = (short) Math.max(0, Math.min(Short.MAX_VALUE, intValue(row.get("go_y"), 0)));
            wayPoints.add(wayPoint);
        }
        return wayPoints;
    }

    private static RuntimeNpcData runtimeNpcDataFromTemplate(MapTemplate template) {
        RuntimeNpcData npcData = new RuntimeNpcData();
        int count = template.npcId == null ? 0 : template.npcId.length;
        npcData.npcId = new byte[count];
        npcData.npcX = new short[count];
        npcData.npcY = new short[count];
        for (int i = 0; i < count; i++) {
            npcData.npcId[i] = template.npcId[i];
            npcData.npcX[i] = template.npcX[i];
            npcData.npcY[i] = template.npcY[i];
        }
        return npcData;
    }

    private static RuntimeNpcData runtimeNpcDataFromPayload(Object raw) {
        RuntimeNpcData npcData = new RuntimeNpcData();
        if (!(raw instanceof JSONArray)) {
            npcData.npcId = new byte[0];
            npcData.npcX = new short[0];
            npcData.npcY = new short[0];
            return npcData;
        }
        JSONArray rows = (JSONArray) raw;
        npcData.npcId = new byte[rows.size()];
        npcData.npcX = new short[rows.size()];
        npcData.npcY = new short[rows.size()];
        for (int i = 0; i < rows.size(); i++) {
            Object entry = rows.get(i);
            JSONObject row = entry instanceof JSONObject ? (JSONObject) entry : new JSONObject();
            if (!(entry instanceof JSONObject) && entry instanceof JSONArray) {
                JSONArray array = (JSONArray) entry;
                row.put("id", array.size() > 0 ? array.get(0) : 0);
                row.put("x", array.size() > 1 ? array.get(1) : 0);
                row.put("y", array.size() > 2 ? array.get(2) : 0);
            }
            int npcId = intValue(row.get("id"), -1);
            if (npcId < Byte.MIN_VALUE || npcId > Byte.MAX_VALUE || getNpcTemplateByTemp(npcId) == null) {
                throw new IllegalArgumentException("NPC khong ton tai: " + npcId);
            }
            npcData.npcId[i] = (byte) npcId;
            npcData.npcX[i] = (short) Math.max(0, Math.min(Short.MAX_VALUE, intValue(row.get("x"), 0)));
            npcData.npcY[i] = (short) Math.max(0, Math.min(Short.MAX_VALUE, intValue(row.get("y"), 0)));
        }
        return npcData;
    }

    private static List<RuntimeMapDropRule> cloneMapDropRules(int mapId) {
        List<RuntimeMapDropRule> rules = MAP_DROP_RULES.get(mapId);
        List<RuntimeMapDropRule> clones = new ArrayList<>();
        if (rules == null) {
            return clones;
        }
        for (RuntimeMapDropRule rule : rules) {
            clones.add(rule.copy());
        }
        return clones;
    }

    private static List<RuntimeMapDropRule> runtimeMapDropRulesFromPayload(int mapId, Object raw) {
        List<RuntimeMapDropRule> rules = new ArrayList<>();
        if (!(raw instanceof JSONArray)) {
            return rules;
        }
        JSONArray rows = (JSONArray) raw;
        for (Object entry : rows) {
            JSONObject row = entry instanceof JSONObject ? (JSONObject) entry : new JSONObject();
            RuntimeMapDropRule rule = new RuntimeMapDropRule();
            rule.mapId = mapId;
            rule.itemId = intValue(row.get("item_id"), -1);
            if (getItemTemplateByTemp(rule.itemId) == null) {
                throw new IllegalArgumentException("Item drop khong ton tai: " + rule.itemId);
            }
            int mobTempId = intValue(row.get("mob_temp_id"), -1);
            if (mobTempId >= 0) {
                if (getMobTemplateByTemp(mobTempId) == null) {
                    throw new IllegalArgumentException("Mob temp cua drop khong ton tai: " + mobTempId);
                }
                rule.mobTempId = mobTempId;
            }
            rule.quantityMin = Math.max(1, intValue(row.get("quantity_min"), 1));
            rule.quantityMax = Math.max(rule.quantityMin, intValue(row.get("quantity_max"), rule.quantityMin));
            rule.chanceNumerator = Math.max(0, intValue(row.get("chance_numerator"), 1));
            rule.chanceDenominator = Math.max(1, intValue(row.get("chance_denominator"), 100));
            rule.active = intValue(row.get("active"), 1) == 1 || Boolean.parseBoolean(String.valueOf(row.get("active")));
            rule.note = String.valueOf(row.getOrDefault("note", ""));
            rule.options = itemOptionsFromPayload(normalizeJsonBodyValue(row.get("options")));
            rules.add(rule);
        }
        return rules;
    }

    private static List<Item.ItemOption> itemOptionsFromPayload(Object raw) {
        List<Item.ItemOption> options = new ArrayList<>();
        if (!(raw instanceof JSONArray)) {
            return options;
        }
        JSONArray rows = (JSONArray) raw;
        for (Object entry : rows) {
            JSONObject row = entry instanceof JSONObject ? (JSONObject) entry : new JSONObject();
            if (!(entry instanceof JSONObject) && entry instanceof JSONArray) {
                JSONArray array = (JSONArray) entry;
                row.put("id", array.size() > 0 ? array.get(0) : 0);
                row.put("param", array.size() > 1 ? array.get(1) : 0);
            }
            int id = intValue(row.get("id"), -1);
            if (id < 0) {
                continue;
            }
            options.add(new Item.ItemOption(id, intValue(row.get("param"), 0)));
        }
        return options;
    }

    private static String serializeWayPoints(List<WayPoint> wayPoints) {
        JSONArray rows = new JSONArray();
        if (wayPoints != null) {
            for (WayPoint wayPoint : wayPoints) {
                JSONArray row = new JSONArray();
                row.add(wayPoint.name == null ? "" : wayPoint.name);
                row.add((int) wayPoint.minX);
                row.add((int) wayPoint.minY);
                row.add((int) wayPoint.maxX);
                row.add((int) wayPoint.maxY);
                row.add(wayPoint.isEnter ? 1 : 0);
                row.add(wayPoint.isOffline ? 1 : 0);
                row.add(wayPoint.goMap);
                row.add((int) wayPoint.goX);
                row.add((int) wayPoint.goY);
                rows.add(row);
            }
        }
        return rows.toJSONString();
    }

    private static String serializeNpcs(RuntimeNpcData npcData) {
        JSONArray rows = new JSONArray();
        int count = npcData.npcId == null ? 0 : npcData.npcId.length;
        for (int i = 0; i < count; i++) {
            JSONArray row = new JSONArray();
            row.add((int) npcData.npcId[i]);
            row.add((int) npcData.npcX[i]);
            row.add((int) npcData.npcY[i]);
            rows.add(row);
        }
        return rows.toJSONString();
    }

    private static String serializeItemOptions(List<Item.ItemOption> options) {
        JSONArray rows = new JSONArray();
        if (options != null) {
            for (Item.ItemOption option : options) {
                JSONArray row = new JSONArray();
                row.add(option.optionTemplate.id);
                row.add(option.param);
                rows.add(row);
            }
        }
        return rows.toJSONString();
    }

    private static String runtimeItemOptionsJson(List<Item.ItemOption> options) {
        JSONArray rows = new JSONArray();
        if (options != null) {
            for (Item.ItemOption option : options) {
                JSONObject row = new JSONObject();
                row.put("id", option.optionTemplate.id);
                row.put("param", option.param);
                rows.add(row);
            }
        }
        return rows.toJSONString();
    }

    private static MapTemplate findMapTemplate(int mapId) {
        if (MAP_TEMPLATES == null) {
            return null;
        }
        for (MapTemplate mapTemplate : MAP_TEMPLATES) {
            if (mapTemplate != null && mapTemplate.id == mapId) {
                return mapTemplate;
            }
        }
        return null;
    }

    private static map.Map findRuntimeMap(int mapId) {
        for (map.Map map : MAPS) {
            if (map != null && map.mapId == mapId) {
                return map;
            }
        }
        return null;
    }

    private static mob.Mob firstZoneMob(map.Map map, int index) {
        if (map == null || map.zones == null || map.zones.isEmpty()) {
            return null;
        }
        Zone zone = map.zones.get(0);
        synchronized (zone.mobs) {
            if (index < 0 || index >= zone.mobs.size()) {
                return null;
            }
            return zone.mobs.get(index);
        }
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

    private static int mobTemplatePercentDame(int tempId) {
        MobTemplate mobTemplate = getMobTemplateByTemp(tempId);
        return mobTemplate == null ? 0 : mobTemplate.percentDame;
    }

    private static void updateMobTemplatePercentDame(int tempId, int percentDame) {
        MobTemplate mobTemplate = getMobTemplateByTemp(tempId);
        if (mobTemplate != null) {
            mobTemplate.percentDame = (byte) percentDame;
        }
    }

    private static boolean isZoneCountForcedByType(int mapType) {
        return mapType == ConstMap.MAP_OFFLINE
                || mapType == ConstMap.MAP_BLACK_BALL_WAR
                || mapType == ConstMap.MAP_MA_BU
                || mapType == ConstMap.MAP_MABU_14H
                || mapType == ConstMap.MAP_DOANH_TRAI
                || mapType == ConstMap.MAP_BAN_DO_KHO_BAU
                || mapType == ConstMap.MAP_CON_DUONG_RAN_DOC
                || mapType == ConstMap.MAP_KHI_GAS_HUY_DIET;
    }

    private static void ensureMapDropRuleTable(Connection con) throws SQLException {
        try (Statement st = con.createStatement()) {
            st.executeUpdate("create table if not exists map_drop_rule ("
                    + "id int primary key auto_increment,"
                    + "map_id int not null,"
                    + "item_id int not null,"
                    + "quantity_min int not null default 1,"
                    + "quantity_max int not null default 1,"
                    + "chance_numerator int not null default 1,"
                    + "chance_denominator int not null default 100,"
                    + "mob_temp_id int null,"
                    + "options_text text null,"
                    + "active tinyint(1) not null default 1,"
                    + "note varchar(255) null,"
                    + "created_at timestamp null default current_timestamp,"
                    + "updated_at timestamp null default current_timestamp on update current_timestamp,"
                    + "index idx_map_active (map_id, active),"
                    + "index idx_mob_temp (mob_temp_id))");
        }
    }

    private static void loadMapDropRules(Connection con) throws SQLException {
        MAP_DROP_RULES.clear();
        try (PreparedStatement ps = con.prepareStatement("select map_id, item_id, quantity_min, quantity_max, chance_numerator, chance_denominator, mob_temp_id, options_text, active, note from map_drop_rule order by map_id asc, id asc"); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                RuntimeMapDropRule rule = new RuntimeMapDropRule();
                rule.mapId = rs.getInt("map_id");
                rule.itemId = rs.getInt("item_id");
                rule.quantityMin = Math.max(1, rs.getInt("quantity_min"));
                rule.quantityMax = Math.max(rule.quantityMin, rs.getInt("quantity_max"));
                rule.chanceNumerator = Math.max(0, rs.getInt("chance_numerator"));
                rule.chanceDenominator = Math.max(1, rs.getInt("chance_denominator"));
                int mobTempId = rs.getInt("mob_temp_id");
                rule.mobTempId = rs.wasNull() ? null : mobTempId;
                rule.active = rs.getInt("active") == 1;
                rule.note = rs.getString("note");
                rule.options = itemOptionsFromPayload(JSONValue.parse(rs.getString("options_text")));
                MAP_DROP_RULES.computeIfAbsent(rule.mapId, key -> new ArrayList<>()).add(rule);
            }
        }
    }

    public static void appendRuntimeMapDropRewards(Zone zone, player.Player player, int mobTempId, int x, int yEnd, List<map.ItemMap> list) {
        if (zone == null || zone.map == null || player == null || list == null) {
            return;
        }
        List<RuntimeMapDropRule> rules = MAP_DROP_RULES.get(zone.map.mapId);
        if (rules == null || rules.isEmpty()) {
            return;
        }
        for (RuntimeMapDropRule rule : rules) {
            if (!rule.active) {
                continue;
            }
            if (rule.mobTempId != null && rule.mobTempId != mobTempId) {
                continue;
            }
            if (rule.chanceNumerator <= 0 || rule.chanceDenominator <= 0) {
                continue;
            }
            if (!Util.isTrue(rule.chanceNumerator, rule.chanceDenominator)) {
                continue;
            }
            int quantity = rule.quantityMin >= rule.quantityMax
                    ? rule.quantityMin
                    : Util.nextInt(rule.quantityMin, rule.quantityMax);
            map.ItemMap itemMap = new map.ItemMap(zone, rule.itemId, quantity, x, yEnd, player.id);
            if (rule.options != null) {
                for (Item.ItemOption option : rule.options) {
                    itemMap.options.add(new Item.ItemOption(option.optionTemplate.id, option.param));
                }
            }
            list.add(itemMap);
        }
    }

    private static String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\r", "\\r").replace("\n", "\\n");
    }

    public static MobTemplate getMobTemplateByTemp(int mobTempId) {
        for (MobTemplate mobTemp : MOB_TEMPLATES) {
            if (mobTemp.id == mobTempId) {
                return mobTemp;
            }
        }
        return null;
    }

    public static NpcTemplate getNpcTemplateByTemp(int npcTempId) {
        for (NpcTemplate npcTemplate : NPC_TEMPLATES) {
            if (npcTemplate.id == npcTempId) {
                return npcTemplate;
            }
        }
        return null;
    }

    public static ItemTemplate getItemTemplateByTemp(int itemTempId) {
        for (ItemTemplate itemTemplate : ITEM_TEMPLATES) {
            if (itemTemplate.id == itemTempId) {
                return itemTemplate;
            }
        }
        return null;
    }

    private static ItemTemplate createMissingItemTemplate(short id) {
        ItemTemplate itemTemplate = new ItemTemplate();
        itemTemplate.id = id;
        itemTemplate.type = 4;
        itemTemplate.gender = 3;
        itemTemplate.name = "Trống";
        itemTemplate.description = "";
        itemTemplate.iconID = 0;
        itemTemplate.part = -1;
        itemTemplate.isUpToUp = false;
        itemTemplate.strRequire = 0;
        return itemTemplate;
    }

    public static byte getNFrameImageByName(String name) {
        Object n = IMAGES_BY_NAME.get(name);
        if (n != null) {
            return Byte.parseByte(String.valueOf(n));
        } else {
            return 0;
        }
    }

    // Xử lý menu Top
    public static Timestamp timeSuKienDuaTop = Timestamp.valueOf("2024-06-10 23:59:59");
    public static String timeStartDuaTop = "10h ngày 25/5/2024";
    public static String timeEndDuaTop = "23h59 ngày 10/6/2024";

    public static String demTimeSuKien() {
        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime eventTime = timeSuKienDuaTop.toLocalDateTime();

        long daysRemaining = ChronoUnit.DAYS.between(currentTime, eventTime);
        if (daysRemaining > 0) {
            return "(" + daysRemaining + " ngày nữa)";
        } else {
            return "(Đã kết thúc)";
        }
    }
    // End xử lý menu top

    private static class RuntimeNpcData {

        private byte[] npcId = new byte[0];
        private short[] npcX = new short[0];
        private short[] npcY = new short[0];
    }

    private static class RuntimeMapDropRule {

        private int mapId;
        private int itemId;
        private int quantityMin;
        private int quantityMax;
        private int chanceNumerator;
        private int chanceDenominator;
        private Integer mobTempId;
        private List<Item.ItemOption> options = new ArrayList<>();
        private boolean active = true;
        private String note = "";

        private RuntimeMapDropRule copy() {
            RuntimeMapDropRule clone = new RuntimeMapDropRule();
            clone.mapId = this.mapId;
            clone.itemId = this.itemId;
            clone.quantityMin = this.quantityMin;
            clone.quantityMax = this.quantityMax;
            clone.chanceNumerator = this.chanceNumerator;
            clone.chanceDenominator = this.chanceDenominator;
            clone.mobTempId = this.mobTempId;
            clone.active = this.active;
            clone.note = this.note;
            for (Item.ItemOption option : this.options) {
                clone.options.add(new Item.ItemOption(option.optionTemplate.id, option.param));
            }
            return clone;
        }
    }
}
