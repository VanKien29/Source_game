package player.Bot;

import consts.ConstItem;
import consts.ConstPlayer;
import java.util.ArrayList;
import java.util.List;
import item.Item;
import jdbc.DBConnecter;
import jdbc.NDVResultSet;
import models.Template;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import player.Fusion;
import player.Inventory;
import player.Pet;
import server.Manager;
import services.ItemService;
import services.TaskService;
import shop.ItemShop;
import shop.Shop;
import shop.TabShop;
import skill.Skill;
import task.TaskMain;
import utils.Logger;
import utils.SkillUtil;
import utils.Util;

public class BotProfileService {

    private static BotProfileService instance;
    private static final String LETTERS = "abcdefghijklmnopqrstuvwxyz";
    private static final String[] PLAYER_NAME_SEEDS = {
        "songoha", "songoku", "kakarot", "gohan", "vegeta", "vuatuat", "kkksv", "bwwfn",
        "facebook", "zalo", "blackgoku", "broly", "kamejoko", "majin", "picolo", "kidbuu",
        "trumkame", "namc", "xayda", "tdst", "songohan", "minhokko", "jinwo", "bearbu",
        "tomkame", "skibidi", "vuong", "satthu", "kameone", "xinkai", "habos", "kakarotx"
    };
    private static final long MIN_SMART_POWER = 3_000_000_000L;
    private static final long MAX_SMART_POWER = 50_000_000_000L;
    private static final long MIN_SMART_HP = 100L;
    private static final long MAX_SMART_HP = 1_000_000L;
    private static final String[] BOT_COSTUME_SHOPS = {
        "SANTA", "SANTA_PHUKIEN", "SANTA_HEAD",
        "SHOP_XU_KRAI", "SHOP_VND", "SHOP_TV"
    };

    public static BotProfileService gI() {
        if (instance == null) {
            instance = new BotProfileService();
        }
        return instance;
    }

    public Bot createSmartBot() {
        Bot bot = new Bot((short) 0, (short) 0, (short) 0, Bot.TYPE_SMART, "BotSmart", null, (short) -1);
        bot.id = 1_000_000_000L + Util.nextInt(1, 999_999_999);
        if (!applyRandomPlayerProfile(bot)) {
            applyFallbackProfile(bot);
        }
        finishProfile(bot);
        return bot;
    }

    private boolean applyRandomPlayerProfile(Bot bot) {
        NDVResultSet rs = null;
        try {
            rs = queryRandomPlayerProfile(true);
            if (rs == null || rs.getRows() <= 0 || !rs.first()) {
                if (rs != null) {
                    rs.dispose();
                }
                rs = queryRandomPlayerProfile(false);
            }
            if (rs == null || rs.getRows() <= 0 || !rs.first()) {
                return false;
            }
            bot.name = buildBotName(rs.getString("name"));
            bot.gender = rs.getByte("gender");
            bot.head = rs.getShort("head");
            if (bot.head == -1) {
                bot.head = defaultHead(bot.gender);
            }
            loadInventory(bot, rs.getString("data_inventory"));
            loadPoints(bot, rs.getString("data_point"));
            loadBody(bot, rs.getString("items_body"));
            loadBag(bot, rs.getString("items_bag"));
            loadItemList(bot.inventory.itemsBox, rs.getString("items_box"), Inventory.MAX_ITEMS_BOX);
            loadItemList(bot.inventory.itemsBoxCollection, rs.getString("items_box_collection"), Inventory.MAX_ITEM_BOX_COLLECTION);
            loadItemList(bot.inventory.itemsBoxCrackBall, rs.getString("items_box_lucky_round"), Inventory.MAX_ITEMS_BOX);
            loadItemList(bot.inventory.itemsMailBox, rs.getString("item_mails_box"), Inventory.MAX_ITEMS_BOX);
            loadSkills(bot, rs.getString("skills"), rs.getString("skills_shortcut"));
            loadTask(bot, rs.getString("data_task"));
            loadPet(bot, rs.getString("pet"));
            bot.pointfusion.setHpFusion(rs.getInt("hp_point_fusion"));
            bot.pointfusion.setMpFusion(rs.getInt("mp_point_fusion"));
            bot.pointfusion.setDameFusion(rs.getInt("dame_point_fusion"));
            return true;
        } catch (Exception e) {
            Logger.logException(BotProfileService.class, e, "Khong the copy profile player cho smart bot");
            return false;
        } finally {
            if (rs != null) {
                rs.dispose();
            }
        }
    }

    private NDVResultSet queryRandomPlayerProfile(boolean requirePet) throws Exception {
        String petCondition = requirePet
                ? "and p.pet is not null and p.pet <> '' and p.pet <> '[]' and length(p.pet) > 30 "
                : "";
        return DBConnecter.executeQuery(
                "select p.id, p.name, p.gender, p.head, p.data_inventory, p.data_point, "
                + "p.items_body, p.items_bag, p.items_box, p.items_box_collection, "
                + "p.items_box_lucky_round, p.item_mails_box, p.skills, p.skills_shortcut, p.data_task, p.pet, "
                + "p.hp_point_fusion, p.mp_point_fusion, p.dame_point_fusion "
                + "from player p join account a on p.account_id = a.id "
                + "where a.ban = 0 and a.is_admin = 0 "
                + "and p.items_body is not null and p.items_bag is not null "
                + "and p.skills is not null and p.data_point is not null "
                + petCondition
                + "order by rand() limit 1");
    }

    private void applyFallbackProfile(Bot bot) {
        bot.name = buildBotName(null);
        bot.gender = (byte) Util.nextInt(0, 2);
        bot.head = defaultHead(bot.gender);
        bot.inventory.gold = 0;
        bot.inventory.gem = 0;
        bot.inventory.ruby = 0;
        bot.nPoint.limitPower = 1;
        bot.nPoint.power = 1_000;
        bot.nPoint.tiemNang = 1_000;
        bot.nPoint.dameg = 10;
        bot.nPoint.hpg = 1_000;
        bot.nPoint.mpg = 1_000;
        bot.nPoint.hpMax = 1_000;
        bot.nPoint.mpMax = 1_000;
        bot.nPoint.hp = bot.nPoint.hpMax;
        bot.nPoint.mp = bot.nPoint.mpMax;
        bot.nPoint.maxStamina = 20_000;
        bot.nPoint.stamina = 20_000;
        bot.nPoint.defg = 0;
        bot.nPoint.critg = 0;
        fillBagSlots(bot, 20);
        bot.leakSkill();
        bot.playerTask.taskMain = TaskService.gI().getTaskMainById(bot, 0);
    }

    private void finishProfile(Bot bot) {
        if (bot.inventory.itemsBag.isEmpty()) {
            fillBagSlots(bot, 20);
        }
        if (bot.playerTask.taskMain == null) {
            bot.playerTask.taskMain = TaskService.gI().getTaskMainById(bot, 0);
        }
        if (bot.playerSkill.skills.isEmpty()) {
            bot.leakSkill();
        }
        chooseDefaultSkill(bot);
        normalizeSmartPower(bot);
        removeCopiedPorata2(bot);
        equipShopVisual(bot);
        bot.nPoint.initPowerLimit();
        if (bot.pet != null) {
            bot.pet.nPoint.initPowerLimit();
            bot.pet.nPoint.calPoint();
            clampSmartHp(bot.pet.nPoint);
            bot.pet.nPoint.hp = bot.pet.nPoint.hpMax;
            bot.pet.nPoint.mp = bot.pet.nPoint.mpMax;
        }
        preparePetFusion(bot);
        assignSmartMode(bot);
        bot.nPoint.calPoint();
        clampSmartHp(bot.nPoint);
        bot.nPoint.hp = bot.nPoint.hpMax;
        bot.nPoint.mp = bot.nPoint.mpMax;
    }

    private short defaultHead(byte gender) {
        return switch (gender) {
            case ConstPlayer.NAMEC -> 9;
            case ConstPlayer.XAYDA -> 6;
            default -> 64;
        };
    }

    private String buildBotName(String sourceName) {
        String clean = cleanName(sourceName);
        if (isUsableName(clean) && Util.isTrue(45, 100)) {
            return mutateName(clean);
        }
        String name = PLAYER_NAME_SEEDS[Util.nextInt(0, PLAYER_NAME_SEEDS.length - 1)];
        if (Util.isTrue(45, 100) && name.length() < 11) {
            name += randomLetters(Util.nextInt(1, 3));
        }
        return name.length() > 12 ? name.substring(0, 12) : name;
    }

    private String cleanName(String sourceName) {
        if (sourceName == null) {
            return "";
        }
        return sourceName.toLowerCase().replaceAll("[^a-z]", "");
    }

    private boolean isUsableName(String name) {
        return name != null && name.length() >= 4 && name.length() <= 12
                && !name.contains("admin") && !name.contains("bot");
    }

    private String mutateName(String name) {
        String result = name;
        if (result.length() > 10) {
            result = result.substring(0, Util.nextInt(8, 10));
        }
        if (Util.isTrue(35, 100) && result.length() < 11) {
            result += randomLetters(Util.nextInt(1, 2));
        }
        return result;
    }

    private String randomLetters(int length) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            builder.append(LETTERS.charAt(Util.nextInt(0, LETTERS.length() - 1)));
        }
        return builder.toString();
    }

    private void loadInventory(Bot bot, String dataInventory) {
        try {
            JSONArray dataArray = (JSONArray) JSONValue.parse(dataInventory);
            if (dataArray == null || dataArray.size() < 3) {
                return;
            }
            bot.inventory.gold = Long.parseLong(String.valueOf(dataArray.get(0)));
            bot.inventory.gem = Inventory.clampGem(Long.parseLong(String.valueOf(dataArray.get(1))));
            bot.inventory.ruby = Inventory.clampRuby(Long.parseLong(String.valueOf(dataArray.get(2))));
            bot.inventory.coupon = dataArray.size() > 3 ? Integer.parseInt(String.valueOf(dataArray.get(3))) : 0;
        } catch (Exception ignored) {
        }
    }

    private void loadPoints(Bot bot, String dataPoint) {
        try {
            JSONArray dataArray = (JSONArray) JSONValue.parse(dataPoint);
            bot.nPoint.limitPower = Byte.parseByte(String.valueOf(dataArray.get(0)));
            bot.nPoint.power = Long.parseLong(String.valueOf(dataArray.get(1)));
            bot.nPoint.tiemNang = Long.parseLong(String.valueOf(dataArray.get(2)));
            bot.nPoint.stamina = Short.parseShort(String.valueOf(dataArray.get(3)));
            bot.nPoint.maxStamina = Short.parseShort(String.valueOf(dataArray.get(4)));
            bot.nPoint.hpg = Long.parseLong(String.valueOf(dataArray.get(5)));
            bot.nPoint.mpg = Long.parseLong(String.valueOf(dataArray.get(6)));
            bot.nPoint.dameg = Long.parseLong(String.valueOf(dataArray.get(7)));
            bot.nPoint.defg = Integer.parseInt(String.valueOf(dataArray.get(8)));
            bot.nPoint.critg = Byte.parseByte(String.valueOf(dataArray.get(9)));
            bot.nPoint.hpMax = dataArray.size() > 11 ? Long.parseLong(String.valueOf(dataArray.get(11))) : bot.nPoint.hpg;
            bot.nPoint.mpMax = dataArray.size() > 12 ? Long.parseLong(String.valueOf(dataArray.get(12))) : bot.nPoint.mpg;
            bot.nPoint.hp = bot.nPoint.hpMax;
            bot.nPoint.mp = bot.nPoint.mpMax;
        } catch (Exception ignored) {
        }
    }

    private void loadBody(Bot bot, String itemsBody) {
        bot.inventory.itemsBody.clear();
        JSONArray dataArray = (JSONArray) JSONValue.parse(itemsBody);
        int sizeFromDb = dataArray == null ? 0 : dataArray.size();
        for (int i = 0; i < Inventory.BODY_SLOT_COUNT; i++) {
            Item item = ItemService.gI().createItemNull();
            if (i < sizeFromDb) {
                item = parseItem(dataArray.get(i));
            }
            bot.inventory.itemsBody.add(item);
        }
    }

    private void loadBag(Bot bot, String itemsBag) {
        bot.inventory.itemsBag.clear();
        JSONArray dataArray = (JSONArray) JSONValue.parse(itemsBag);
        if (dataArray == null || dataArray.isEmpty()) {
            fillBagSlots(bot, 20);
            return;
        }
        for (Object obj : dataArray) {
            bot.inventory.itemsBag.add(parseItem(obj));
        }
    }

    private void loadItemList(List<Item> target, String itemsJson, int maxSize) {
        if (target == null) {
            return;
        }
        target.clear();
        JSONArray dataArray = (JSONArray) JSONValue.parse(itemsJson);
        if (dataArray == null) {
            return;
        }
        for (int i = 0; i < dataArray.size() && i < maxSize; i++) {
            target.add(parseItem(dataArray.get(i)));
        }
    }

    private void fillBagSlots(Bot bot, int count) {
        bot.inventory.itemsBag.clear();
        for (int i = 0; i < count; i++) {
            bot.inventory.itemsBag.add(ItemService.gI().createItemNull());
        }
    }

    private Item parseItem(Object raw) {
        Item item = ItemService.gI().createItemNull();
        try {
            JSONArray dataItem = (JSONArray) JSONValue.parse(String.valueOf(raw));
            short tempId = Short.parseShort(String.valueOf(dataItem.get(0)));
            if (tempId == -1) {
                return item;
            }
            item = ItemService.gI().createNewItem(tempId, Integer.parseInt(String.valueOf(dataItem.get(1))));
            JSONArray options = (JSONArray) JSONValue.parse(String.valueOf(dataItem.get(2)).replaceAll("\"", ""));
            if (options != null) {
                for (Object optObj : options) {
                    JSONArray opt = (JSONArray) JSONValue.parse(String.valueOf(optObj));
                    item.itemOptions.add(new Item.ItemOption(
                            Integer.parseInt(String.valueOf(opt.get(0))),
                            Integer.parseInt(String.valueOf(opt.get(1)))));
                }
            }
            if (dataItem.size() > 3) {
                item.createTime = Long.parseLong(String.valueOf(dataItem.get(3)));
            }
            if (ItemService.gI().isOutOfDateTime(item)) {
                item = ItemService.gI().createItemNull();
            }
        } catch (Exception ignored) {
            item = ItemService.gI().createItemNull();
        }
        return item;
    }

    private void loadSkills(Bot bot, String skillsJson, String shortcutsJson) {
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
                        if (dataSkill.size() > 3) {
                            skill.currLevel = Short.parseShort(String.valueOf(dataSkill.get(3)));
                        }
                        bot.playerSkill.skills.add(skill);
                    }
                } catch (Exception ignored) {
                }
            }
        }
        try {
            JSONArray shortcuts = (JSONArray) JSONValue.parse(shortcutsJson);
            for (int i = 0; shortcuts != null && i < shortcuts.size() && i < bot.playerSkill.skillShortCut.length; i++) {
                bot.playerSkill.skillShortCut[i] = Byte.parseByte(String.valueOf(shortcuts.get(i)));
            }
        } catch (Exception ignored) {
        }
        chooseDefaultSkill(bot);
    }

    private void chooseDefaultSkill(Bot bot) {
        Skill selected = null;
        for (byte shortcut : bot.playerSkill.skillShortCut) {
            Skill skill = bot.playerSkill.getSkillbyId(shortcut);
            if (skill != null && skill.skillId != -1 && skill.damage > 0) {
                selected = skill;
                break;
            }
        }
        if (selected == null) {
            int defaultSkill = bot.gender == ConstPlayer.TRAI_DAT ? Skill.DRAGON
                    : bot.gender == ConstPlayer.NAMEC ? Skill.DEMON : Skill.GALICK;
            selected = bot.playerSkill.getSkillbyId(defaultSkill);
        }
        if (selected == null && !bot.playerSkill.skills.isEmpty()) {
            selected = bot.playerSkill.skills.get(0);
        }
        bot.playerSkill.skillSelect = selected;
    }

    private void loadTask(Bot bot, String dataTask) {
        try {
            JSONArray dataArray = (JSONArray) JSONValue.parse(dataTask);
            if (dataArray == null || dataArray.size() < 3) {
                return;
            }
            TaskMain taskMain = TaskService.gI().getTaskMainById(bot,
                    Integer.parseInt(String.valueOf(dataArray.get(0))));
            if (taskMain == null || taskMain.subTasks.isEmpty()) {
                return;
            }
            taskMain.index = Integer.parseInt(String.valueOf(dataArray.get(1)));
            if (taskMain.index < 0) {
                taskMain.index = 0;
            } else if (taskMain.index >= taskMain.subTasks.size()) {
                taskMain.index = taskMain.subTasks.size() - 1;
            }
            taskMain.subTasks.get(taskMain.index).count = Short.parseShort(String.valueOf(dataArray.get(2)));
            boostTaskProgress(taskMain);
            taskMain.lastTime = dataArray.size() > 3 ? Long.parseLong(String.valueOf(dataArray.get(3))) : System.currentTimeMillis();
            bot.playerTask.taskMain = taskMain;
        } catch (Exception ignored) {
        }
    }

    private void boostTaskProgress(TaskMain taskMain) {
        try {
            if (taskMain == null || taskMain.subTasks == null || taskMain.subTasks.isEmpty()
                    || taskMain.index < 0 || taskMain.index >= taskMain.subTasks.size()) {
                return;
            }
            var subTask = taskMain.subTasks.get(taskMain.index);
            if (subTask.maxCount <= 2 || subTask.count >= subTask.maxCount) {
                return;
            }
            int add = Math.max(1, subTask.maxCount / Util.nextInt(10, 18));
            subTask.count = (short) Math.min(subTask.maxCount - 1, subTask.count + add);
        } catch (Exception ignored) {
        }
    }

    private void loadPet(Bot bot, String petJson) {
        try {
            JSONArray petData = (JSONArray) JSONValue.parse(petJson);
            if (petData == null || petData.isEmpty() || petData.size() < 4) {
                return;
            }
            JSONArray dataArray = (JSONArray) JSONValue.parse(String.valueOf(petData.get(0)));
            Pet pet = new Pet(bot);
            pet.id = -bot.id;
            pet.typePet = Byte.parseByte(String.valueOf(dataArray.get(0)));
            pet.gender = Byte.parseByte(String.valueOf(dataArray.get(1)));
            pet.name = String.valueOf(dataArray.get(2));
            bot.fusion.typeFusion = Byte.parseByte(String.valueOf(dataArray.get(3)));
            bot.fusion.lastTimeFusion = System.currentTimeMillis()
                    - (Fusion.TIME_FUSION - Integer.parseInt(String.valueOf(dataArray.get(4))));
            pet.status = Byte.parseByte(String.valueOf(dataArray.get(5)));

            loadPetPoints(pet, String.valueOf(petData.get(1)));
            loadPetBody(pet, String.valueOf(petData.get(2)));
            loadPetSkills(pet, String.valueOf(petData.get(3)));
            if (bot.fusion.typeFusion == ConstPlayer.NON_FUSION && pet.status == Pet.FUSION) {
                pet.status = Pet.ATTACK;
            } else if (bot.fusion.typeFusion != ConstPlayer.NON_FUSION) {
                pet.status = Pet.FUSION;
            }
            bot.pet = pet;
        } catch (Exception e) {
            bot.pet = null;
            bot.fusion.typeFusion = ConstPlayer.NON_FUSION;
        }
    }

    private void loadPetPoints(Pet pet, String dataPoint) {
        try {
            JSONArray dataArray = (JSONArray) JSONValue.parse(dataPoint);
            pet.nPoint.limitPower = Byte.parseByte(String.valueOf(dataArray.get(0)));
            pet.nPoint.power = Long.parseLong(String.valueOf(dataArray.get(1)));
            pet.nPoint.tiemNang = Long.parseLong(String.valueOf(dataArray.get(2)));
            pet.nPoint.stamina = Short.parseShort(String.valueOf(dataArray.get(3)));
            pet.nPoint.maxStamina = Short.parseShort(String.valueOf(dataArray.get(4)));
            pet.nPoint.hpg = Long.parseLong(String.valueOf(dataArray.get(5)));
            pet.nPoint.mpg = Long.parseLong(String.valueOf(dataArray.get(6)));
            pet.nPoint.dameg = Long.parseLong(String.valueOf(dataArray.get(7)));
            pet.nPoint.defg = Integer.parseInt(String.valueOf(dataArray.get(8)));
            pet.nPoint.critg = Integer.parseInt(String.valueOf(dataArray.get(9)));
            pet.nPoint.hp = Long.parseLong(String.valueOf(dataArray.get(10)));
            pet.nPoint.mp = Long.parseLong(String.valueOf(dataArray.get(11)));
        } catch (Exception ignored) {
        }
    }

    private void loadPetBody(Pet pet, String bodyJson) {
        pet.inventory.itemsBody.clear();
        JSONArray dataArray = (JSONArray) JSONValue.parse(bodyJson);
        int sizeFromDb = dataArray == null ? 0 : dataArray.size();
        int expectedSlots = Math.max(getPetBodySlotCount(pet.typePet), sizeFromDb);
        for (int i = 0; i < expectedSlots; i++) {
            Item item = ItemService.gI().createItemNull();
            if (i < sizeFromDb) {
                item = parseItem(dataArray.get(i));
            }
            pet.inventory.itemsBody.add(item);
        }
    }

    private int getPetBodySlotCount(byte typePet) {
        return switch (typePet) {
            case 1 -> 7;
            case 2, 3, 4 -> 8;
            case 5 -> 9;
            case 6 -> 10;
            case 7 -> 11;
            case 8 -> 12;
            case 9 -> 13;
            default -> 6;
        };
    }

    private void loadPetSkills(Pet pet, String skillsJson) {
        pet.playerSkill.skills.clear();
        JSONArray dataArray = (JSONArray) JSONValue.parse(skillsJson);
        if (dataArray != null) {
            for (Object obj : dataArray) {
                try {
                    JSONArray skillTemp = (JSONArray) JSONValue.parse(String.valueOf(obj));
                    int tempId = Integer.parseInt(String.valueOf(skillTemp.get(0)));
                    byte point = Byte.parseByte(String.valueOf(skillTemp.get(1)));
                    Skill skill = point > 0 ? SkillUtil.createSkill(tempId, point) : SkillUtil.createSkillLevel0(tempId);
                    if (skill != null) {
                        skill.lastTimeUseThisSkill = skillTemp.size() > 2 ? Long.parseLong(String.valueOf(skillTemp.get(2))) : 0;
                        if (skillTemp.size() > 3) {
                            skill.currLevel = Short.parseShort(String.valueOf(skillTemp.get(3)));
                        }
                        switch (skill.template.id) {
                            case Skill.KAMEJOKO, Skill.MASENKO, Skill.ANTOMIC ->
                                skill.coolDown = 1000;
                        }
                        pet.playerSkill.skills.add(skill);
                    }
                } catch (Exception ignored) {
                }
            }
        }
        if (pet.playerSkill.skills.isEmpty()) {
            int defaultSkill = pet.gender == ConstPlayer.TRAI_DAT ? Skill.DRAGON
                    : pet.gender == ConstPlayer.NAMEC ? Skill.DEMON : Skill.GALICK;
            Skill skill = SkillUtil.createSkill(defaultSkill, 1);
            if (skill != null) {
                pet.playerSkill.skills.add(skill);
            }
        }
    }

    private void equipShopVisual(Bot bot) {
        Item costume = chooseShopCostume(bot);
        if (bot.inventory.itemsBody.size() > 5) {
            bot.inventory.itemsBody.set(5, costume != null
                    ? ItemService.gI().copyItem(costume)
                    : ItemService.gI().createItemNull());
        }
        if (bot.pet != null && bot.pet.inventory.itemsBody.size() > 5) {
            Item petCostume = chooseShopCostume(bot.pet.gender, bot.pet.nPoint.power);
            bot.pet.inventory.itemsBody.set(5, petCostume != null
                    ? ItemService.gI().copyItem(petCostume)
                    : ItemService.gI().createItemNull());
        }
    }

    private Item chooseShopCostume(Bot bot) {
        return chooseShopCostume(bot.gender, bot.nPoint.power);
    }

    private Item chooseShopCostume(byte gender, long power) {
        List<ItemShop> candidates = new ArrayList<>();
        for (Shop shop : Manager.SHOPS) {
            if (!isBotCostumeShop(shop)) {
                continue;
            }
            for (TabShop tabShop : shop.tabShops) {
                if (tabShop == null || tabShop.itemShops == null) {
                    continue;
                }
                for (ItemShop itemShop : tabShop.itemShops) {
                    if (itemShop == null) {
                        continue;
                    }
                    Template.ItemTemplate template = itemShop.temp;
                    if (template != null && template.type == 5
                            && (template.gender == gender || template.gender == 3)
                            && template.head != -1 && template.body != -1 && template.leg != -1
                            && template.strRequire <= power) {
                        candidates.add(itemShop);
                    }
                }
            }
        }
        if (candidates.isEmpty()) {
            return null;
        }
        return ItemService.gI().createItemFromItemShop(candidates.get(Util.nextInt(0, candidates.size() - 1)));
    }

    private boolean isBotCostumeShop(Shop shop) {
        if (shop == null || shop.tagName == null || shop.tabShops == null) {
            return false;
        }
        for (String tagName : BOT_COSTUME_SHOPS) {
            if (shop.tagName.equals(tagName)) {
                return true;
            }
        }
        return false;
    }

    private void normalizeSmartPower(Bot bot) {
        long targetPower = Util.nextLong(MIN_SMART_POWER, MAX_SMART_POWER);
        normalizePoint(bot.nPoint, targetPower, false);
        if (bot.pet != null) {
            long petPower = targetPower * Util.nextInt(35, 80) / 100;
            normalizePoint(bot.pet.nPoint, Math.max(1_000_000_000L, petPower), true);
        }
    }

    private void normalizePoint(player.NPoint point, long targetPower, boolean pet) {
        if (point == null) {
            return;
        }
        long power = Math.max(MIN_SMART_POWER, Math.min(MAX_SMART_POWER, targetPower));
        int billion = (int) Math.max(1, power / 1_000_000_000L);
        point.limitPower = player.NPoint.MAX_LIMIT;
        point.power = power;
        point.tiemNang = Math.max(point.power / 3, point.tiemNang > 0 ? Math.min(point.tiemNang, point.power) : 0);
        point.maxStamina = 20_000;
        point.stamina = point.maxStamina;
        point.hpg = Util.nextLong(MIN_SMART_HP, MAX_SMART_HP);
        point.mpg = (pet ? 80_000L : 120_000L) + (long) billion * Util.nextInt(pet ? 10_000 : 18_000, pet ? 22_000 : 35_000);
        point.dameg = (pet ? 5_000L : 8_000L) + (long) billion * Util.nextInt(pet ? 350 : 650, pet ? 850 : 1_300);
        point.defg = Util.nextInt(pet ? 30 : 50, pet ? 350 : 500);
        point.critg = Util.nextInt(3, pet ? 8 : 10);
        point.hpMax = point.hpg;
        point.mpMax = point.mpg;
        point.hp = point.hpMax;
        point.mp = point.mpMax;
        point.initPowerLimit();
    }

    private void clampSmartHp(player.NPoint point) {
        if (point == null) {
            return;
        }
        point.hpMax = Math.max(MIN_SMART_HP, Math.min(MAX_SMART_HP, point.hpMax));
        point.hp = point.hpMax;
    }

    private void preparePetFusion(Bot bot) {
        if (bot.pet == null) {
            bot.fusion.typeFusion = ConstPlayer.NON_FUSION;
            return;
        }
        if (bot.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA2
                || bot.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA3) {
            bot.fusion.typeFusion = ConstPlayer.NON_FUSION;
        }
        if (bot.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA || Util.isTrue(75, 100)) {
            bot.fusion.typeFusion = ConstPlayer.HOP_THE_PORATA;
            bot.fusion.lastTimeFusion = System.currentTimeMillis();
            bot.pet.status = Pet.FUSION;
            return;
        }
        if (ownsItem(bot, ConstItem.BONG_TAI_PORATA)) {
            bot.fusion.typeFusion = ConstPlayer.HOP_THE_PORATA;
            bot.fusion.lastTimeFusion = System.currentTimeMillis();
            bot.pet.status = Pet.FUSION;
        } else if (bot.pet.status == Pet.FUSION) {
            bot.pet.status = Pet.ATTACK;
        }
    }

    private void assignSmartMode(Bot bot) {
        bot.smartMode = Bot.SMART_MODE_FARM;
        if (bot.pet != null && bot.fusion.typeFusion == ConstPlayer.NON_FUSION && Util.isTrue(22, 100)) {
            bot.smartMode = Bot.SMART_MODE_PET_TRAIN;
            bot.pet.status = Pet.ATTACK;
            return;
        }
        if (Util.isTrue(4, 100)) {
            bot.smartMode = Bot.SMART_MODE_SOCIAL;
        }
    }

    private boolean ownsItem(Bot bot, int templateId) {
        return hasItem(bot.inventory.itemsBody, templateId)
                || hasItem(bot.inventory.itemsBag, templateId)
                || hasItem(bot.inventory.itemsBox, templateId)
                || hasItem(bot.inventory.itemsBoxCollection, templateId)
                || hasItem(bot.inventory.itemsBoxCrackBall, templateId)
                || hasItem(bot.inventory.itemsMailBox, templateId);
    }

    private boolean hasItem(List<Item> items, int templateId) {
        if (items == null) {
            return false;
        }
        for (Item item : items) {
            if (item != null && item.isNotNullItem() && item.template.id == templateId) {
                return true;
            }
        }
        return false;
    }

    private void removeCopiedPorata2(Bot bot) {
        removeItemTemplate(bot.inventory.itemsBody, ConstItem.BONG_TAI_PORATA_CAP_2);
        removeItemTemplate(bot.inventory.itemsBag, ConstItem.BONG_TAI_PORATA_CAP_2);
        removeItemTemplate(bot.inventory.itemsBox, ConstItem.BONG_TAI_PORATA_CAP_2);
        removeItemTemplate(bot.inventory.itemsBoxCollection, ConstItem.BONG_TAI_PORATA_CAP_2);
        removeItemTemplate(bot.inventory.itemsBoxCrackBall, ConstItem.BONG_TAI_PORATA_CAP_2);
        removeItemTemplate(bot.inventory.itemsMailBox, ConstItem.BONG_TAI_PORATA_CAP_2);
    }

    private void removeItemTemplate(List<Item> items, int templateId) {
        if (items == null) {
            return;
        }
        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            if (item != null && item.isNotNullItem() && item.template.id == templateId) {
                items.set(i, ItemService.gI().createItemNull());
            }
        }
    }

}
