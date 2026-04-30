package models.Combine.manifest;

import consts.ConstFont;
import consts.ConstNpc;
import item.Item;
import models.Combine.CombineService;
import player.Player;
import player.SetClothes;
import services.InventoryService;
import services.Service;
import utils.Util;

public class NangCapLevelKichHoat {

    public static final int OPTION_SKH_LEVEL_START = SetClothes.OPTION_SKH_LEVEL_START;
    public static final int OPTION_SKH_FULL_SET_BONUS = SetClothes.OPTION_SKH_FULL_SET_BONUS;
    public static final int OPTION_SKH_FULL_SET_BONUS_INACTIVE = 263;
    private static final int ITEM_THOI_VANG = 457;
    private static final int ITEM_DA_SKH_THUONG = 1742;
    private static final int ITEM_DA_SKH_VIP = 1743;
    private static final int MAX_SKH_LEVEL = 5;

    private static final int[] EFFECT_PERCENT = {25, 50, 75, 100, 125, 150};
    private static final int[] FULL_SET_PERCENT = {0, 1, 3, 5, 8, 12};
    private static final int[] NORMAL_RATIO = {0, 70, 50, 30, 15, 5};

    private static int getGold(int nextLevel) {
        return nextLevel * 100_000_000;
    }

    private static int getThoiVang(int nextLevel) {
        return nextLevel;
    }

    public static int getEffectPercent(int level) {
        if (level < 0) {
            return EFFECT_PERCENT[0];
        }
        if (level >= EFFECT_PERCENT.length) {
            return EFFECT_PERCENT[EFFECT_PERCENT.length - 1];
        }
        return EFFECT_PERCENT[level];
    }

    public static int getEffectOptionParam(int optionId, int level) {
        return isPercentEffectOption(optionId) ? getEffectPercent(level) : 0;
    }

    public static int getFullSetPercent(int level) {
        if (level < 0) {
            return FULL_SET_PERCENT[0];
        }
        if (level >= FULL_SET_PERCENT.length) {
            return FULL_SET_PERCENT[FULL_SET_PERCENT.length - 1];
        }
        return FULL_SET_PERCENT[level];
    }

    public static int getDisplayOptionId(Player player, Item item, Item.ItemOption option) {
        if (option == null || option.optionTemplate == null
                || option.optionTemplate.id != OPTION_SKH_FULL_SET_BONUS) {
            return option == null || option.optionTemplate == null ? 0 : option.optionTemplate.id;
        }
        return isFullSetBonusActive(player, item) ? OPTION_SKH_FULL_SET_BONUS : OPTION_SKH_FULL_SET_BONUS_INACTIVE;
    }

    private static boolean isFullSetBonusActive(Player player, Item item) {
        if (player == null || player.inventory == null || item == null || !item.isNotNullItem()) {
            return false;
        }
        int itemLevel = SetClothes.getSKHLevel(item);
        if (itemLevel <= 0) {
            return false;
        }
        int itemSetIndex = getSKHSetIndex(item);
        if (itemSetIndex < 0) {
            return false;
        }
        boolean equippedItem = false;
        int minLevel = MAX_SKH_LEVEL;
        for (int i = 0; i < 5; i++) {
            Item bodyItem = player.inventory.itemsBody.get(i);
            if (bodyItem == item) {
                equippedItem = true;
            }
            if (bodyItem == null || !bodyItem.isNotNullItem() || getSKHSetIndex(bodyItem) != itemSetIndex) {
                return false;
            }
            minLevel = Math.min(minLevel, SetClothes.getSKHLevel(bodyItem));
        }
        return equippedItem && minLevel == itemLevel;
    }

    private static int getSKHSetIndex(Item item) {
        for (Item.ItemOption io : item.itemOptions) {
            if (io != null && io.optionTemplate != null) {
                int setIndex = SetClothes.getOldSKHSetIndex(io.optionTemplate.id);
                if (setIndex >= 0) {
                    return setIndex;
                }
            }
        }
        return -1;
    }

    public static boolean isSKHItem(Item item) {
        if (item == null || !item.isNotNullItem() || item.template == null || item.template.type > 4) {
            return false;
        }
        for (Item.ItemOption io : item.itemOptions) {
            if (io != null && io.optionTemplate != null && SetClothes.isOldSKHOption(io.optionTemplate.id)) {
                return true;
            }
        }
        return false;
    }

    public static void ensureLevelZero(Item item) {
        if (!isSKHItem(item)) {
            return;
        }
        if (getLevelOption(item) == null) {
            setSKHLevel(item, 0);
        } else {
            syncLevelDependentOptions(item, SetClothes.getSKHLevel(item));
        }
    }

    private static Item getSKHItem(Player player) {
        for (Item item : player.combine.itemsCombine) {
            if (isSKHItem(item)) {
                return item;
            }
        }
        return null;
    }

    private static Item getDaSKH(Player player) {
        for (Item item : player.combine.itemsCombine) {
            if (item != null && item.isNotNullItem()
                    && (item.template.id == ITEM_DA_SKH_THUONG || item.template.id == ITEM_DA_SKH_VIP)) {
                return item;
            }
        }
        return null;
    }

    public static void showInfoCombine(Player player) {
        if (player.combine == null || player.combine.itemsCombine == null || player.combine.itemsCombine.size() != 2) {
            Service.gI().sendDialogMessage(player, "Can 1 trang bi SKH va 1 da SKH thuong/VIP");
            return;
        }

        Item trangBi = getSKHItem(player);
        Item daSKH = getDaSKH(player);
        if (trangBi == null || daSKH == null) {
            Service.gI().sendDialogMessage(player, "Can 1 trang bi SKH va 1 da SKH thuong/VIP");
            return;
        }

        ensureLevelZero(trangBi);
        int level = SetClothes.getSKHLevel(trangBi);
        if (level >= MAX_SKH_LEVEL) {
            Service.gI().sendDialogMessage(player, "Trang bi SKH da dat level toi da");
            return;
        }

        int nextLevel = level + 1;
        int gold = getGold(nextLevel);
        int thoiVang = getThoiVang(nextLevel);
        Item tv = InventoryService.gI().findItemBag(player, ITEM_THOI_VANG);
        boolean vip = daSKH.template.id == ITEM_DA_SKH_VIP;
        int ratio = vip ? 100 : NORMAL_RATIO[nextLevel];

        StringBuilder text = new StringBuilder();
        text.append(ConstFont.BOLD_BLUE).append(trangBi.template.name)
                .append("\n").append(ConstFont.BOLD_GREEN).append("Level hiện tại: ").append(level)
                .append("\n").append(ConstFont.BOLD_BLUE).append("Sau nâng cấp: Level ").append(nextLevel)
                .append(" (+").append(EFFECT_PERCENT[nextLevel]).append("% chỉ số SKH)")
                .append("\n").append(ConstFont.BOLD_BLUE).append("Mặc đủ 5 món level ").append(nextLevel)
                .append(": ").append(FULL_SET_PERCENT[nextLevel] > 0 ? "+" + FULL_SET_PERCENT[nextLevel] + "% SĐ HP KI" : "Không tăng SĐ HP KI")
                .append("\n").append(ConstFont.BOLD_RED).append("Tỉ lệ thành công: ").append(ratio).append("%")
                .append("\n").append(ConstFont.BOLD_BLUE).append("Đã sử dụng: ").append(vip ? "Đá Kích Hoạt Vip" : "Đá Kích Hoạt Thường")
                .append("\n").append(ConstFont.BOLD_BLUE).append(vip ? "Đá Vip giữ nguyên sao pha lê và cấp" : "Đá thường sẽ mất sao pha lê và cấp")
                .append("\n");
        text.append(player.inventory.gold >= gold ? ConstFont.BOLD_BLUE : ConstFont.BOLD_RED)
                .append("Cần ").append(Util.numberToMoney(gold)).append(" vàng\n");
        text.append(tv != null && tv.quantity >= thoiVang ? ConstFont.BOLD_BLUE : ConstFont.BOLD_RED)
                .append("Cần ").append(thoiVang).append(" thỏi vàng\n");
        text.append(daSKH.quantity >= 1 ? ConstFont.BOLD_BLUE : ConstFont.BOLD_RED)
                .append("Cần 1 ").append(daSKH.template.name);

        if (player.inventory.gold < gold || tv == null || tv.quantity < thoiVang) {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, text.toString(), "Đóng");
            return;
        }
        CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE,
                text.toString(), "Nâng cấp", "Đóng");
    }

    public static void startCombine(Player player) {
        if (player.combine.itemsCombine.size() != 2) {
            return;
        }
        Item trangBi = getSKHItem(player);
        Item daSKH = getDaSKH(player);
        if (trangBi == null || daSKH == null) {
            return;
        }
        ensureLevelZero(trangBi);
        int level = SetClothes.getSKHLevel(trangBi);
        if (level >= MAX_SKH_LEVEL) {
            return;
        }

        int nextLevel = level + 1;
        int gold = getGold(nextLevel);
        int thoiVang = getThoiVang(nextLevel);
        Item tv = InventoryService.gI().findItemBag(player, ITEM_THOI_VANG);
        if (player.inventory.gold < gold || tv == null || tv.quantity < thoiVang || daSKH.quantity < 1) {
            return;
        }

        boolean vip = daSKH.template.id == ITEM_DA_SKH_VIP;
        boolean success = vip || Util.isTrue(NORMAL_RATIO[nextLevel], 100);
        if (!vip) {
            removeCrystalAndUpgradeLevel(trangBi);
        }
        if (success) {
            setSKHLevel(trangBi, nextLevel);
            CombineService.gI().sendEffectSuccessCombine(player);
        } else {
            CombineService.gI().sendEffectFailCombine(player);
        }

        player.inventory.gold -= gold;
        InventoryService.gI().subQuantityItemsBag(player, tv, thoiVang);
        InventoryService.gI().subQuantityItemsBag(player, daSKH, 1);
        InventoryService.gI().sendItemBag(player);
        Service.gI().sendMoney(player);
        CombineService.gI().reOpenItemCombine(player);
    }

    private static void setSKHLevel(Item item, int level) {
        item.itemOptions.removeIf(io -> io != null && io.optionTemplate != null
                && io.optionTemplate.id >= SetClothes.OPTION_SKH_LEVEL_START
                && io.optionTemplate.id <= SetClothes.OPTION_SKH_LEVEL_END);
        item.itemOptions.add(new Item.ItemOption(OPTION_SKH_LEVEL_START + level, 0));
        syncLevelDependentOptions(item, level);
    }

    private static void syncLevelDependentOptions(Item item, int level) {
        syncEffectOptionParams(item, level);
        item.itemOptions.removeIf(io -> io != null && io.optionTemplate != null
                && (io.optionTemplate.id == OPTION_SKH_FULL_SET_BONUS
                || io.optionTemplate.id == OPTION_SKH_FULL_SET_BONUS_INACTIVE));
        if (FULL_SET_PERCENT[level] > 0) {
            item.itemOptions.add(new Item.ItemOption(OPTION_SKH_FULL_SET_BONUS, FULL_SET_PERCENT[level]));
        }
        sortSKHOptions(item);
    }

    private static void syncEffectOptionParams(Item item, int level) {
        int missingEffectOptionId = 0;
        for (Item.ItemOption io : item.itemOptions) {
            if (io == null || io.optionTemplate == null) {
                continue;
            }
            int optionId = io.optionTemplate.id;
            if (isSetNameOption(optionId)) {
                missingEffectOptionId = getEffectOptionId(optionId);
            } else if (isEffectOption(optionId)) {
                io.param = getEffectOptionParam(optionId, level);
                missingEffectOptionId = 0;
            }
        }
        if (missingEffectOptionId > 0) {
            item.itemOptions.add(new Item.ItemOption(missingEffectOptionId, getEffectOptionParam(missingEffectOptionId, level)));
        }
    }

    private static void sortSKHOptions(Item item) {
        java.util.List<Item.ItemOption> baseOptions = new java.util.ArrayList<>();
        java.util.List<Item.ItemOption> levelOptions = new java.util.ArrayList<>();
        java.util.List<Item.ItemOption> setOptions = new java.util.ArrayList<>();
        java.util.List<Item.ItemOption> effectOptions = new java.util.ArrayList<>();
        java.util.List<Item.ItemOption> bonusOptions = new java.util.ArrayList<>();
        java.util.List<Item.ItemOption> lockOptions = new java.util.ArrayList<>();
        java.util.List<Item.ItemOption> otherOptions = new java.util.ArrayList<>();
        for (Item.ItemOption io : item.itemOptions) {
            if (io == null || io.optionTemplate == null) {
                otherOptions.add(io);
                continue;
            }
            int optionId = io.optionTemplate.id;
            if (optionId >= SetClothes.OPTION_SKH_LEVEL_START && optionId <= SetClothes.OPTION_SKH_LEVEL_END) {
                levelOptions.add(io);
            } else if (isSetNameOption(optionId)) {
                setOptions.add(io);
            } else if (isEffectOption(optionId)) {
                effectOptions.add(io);
            } else if (optionId == OPTION_SKH_FULL_SET_BONUS || optionId == OPTION_SKH_FULL_SET_BONUS_INACTIVE) {
                bonusOptions.add(io);
            } else if (optionId == 30) {
                lockOptions.add(io);
            } else {
                baseOptions.add(io);
            }
        }
        item.itemOptions.clear();
        item.itemOptions.addAll(baseOptions);
        item.itemOptions.addAll(levelOptions);
        item.itemOptions.addAll(setOptions);
        item.itemOptions.addAll(effectOptions);
        item.itemOptions.addAll(bonusOptions);
        item.itemOptions.addAll(lockOptions);
        item.itemOptions.addAll(otherOptions);
    }

    private static boolean isSetNameOption(int optionId) {
        return getEffectOptionId(optionId) > 0;
    }

    private static int getEffectOptionId(int optionId) {
        switch (optionId) {
            case 127:
                return 139;
            case 128:
                return 140;
            case 129:
                return 141;
            case 130:
                return 142;
            case 131:
                return 143;
            case 132:
                return 144;
            case 133:
                return 136;
            case 134:
                return 137;
            case 135:
                return 138;
            case 250:
                return 253;
            case 251:
                return 254;
            default:
                return 0;
        }
    }

    private static boolean isPercentEffectOption(int optionId) {
        switch (optionId) {
            case 136:
            case 138:
            case 253:
            case 140:
            case 141:
            case 142:
            case 143:
            case 144:
            case 254:
                return true;
            default:
                return false;
        }
    }

    private static boolean isEffectOption(int optionId) {
        switch (optionId) {
            case 136:
            case 137:
            case 138:
            case 139:
            case 140:
            case 141:
            case 142:
            case 143:
            case 144:
            case 253:
            case 254:
                return true;
            default:
                return false;
        }
    }

    private static Item.ItemOption getLevelOption(Item item) {
        for (Item.ItemOption io : item.itemOptions) {
            if (io != null && io.optionTemplate != null
                    && io.optionTemplate.id >= SetClothes.OPTION_SKH_LEVEL_START
                    && io.optionTemplate.id <= SetClothes.OPTION_SKH_LEVEL_END) {
                return io;
            }
        }
        return null;
    }

    private static void removeCrystalAndUpgradeLevel(Item item) {
        for (int optionId = 95; optionId <= 102; optionId++) {
            item.removeOption(optionId);
        }
        item.removeOption(107);
        item.removeOption(72);
        item.removeOption(209);
    }
}
