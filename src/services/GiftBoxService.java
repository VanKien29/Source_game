package services;

import item.Item;
import models.GiftBox.GiftBoxHsd;
import models.GiftBox.GiftBoxOption;
import models.GiftBox.GiftBoxReward;
import player.Player;
import utils.Util;

public final class GiftBoxService {

    private static GiftBoxService instance;

    public static GiftBoxService gI() {
        if (instance == null) {
            instance = new GiftBoxService();
        }
        return instance;
    }

    private GiftBoxService() {
    }

    public void open(Player player, Item itemUse, GiftBoxReward... rewards) {
        if (player == null || itemUse == null || !itemUse.isNotNullItem()) {
            return;
        }
        if (InventoryService.gI().getCountEmptyBag(player) < 1) {
            Service.gI().sendThongBao(player, "Bạn phải có ít nhất 1 ô trống hành trang");
            return;
        }

        GiftBoxReward reward = selectReward(rewards);
        if (reward == null) {
            return;
        }

        int quantity = reward.getQuantityMin();
        if (reward.getQuantityMax() > reward.getQuantityMin()) {
            quantity = Util.nextInt(reward.getQuantityMin(), reward.getQuantityMax());
        }

        Item itemReward = ItemService.gI().createNewItem((short) reward.getItemId(), quantity);
        addOptions(itemReward, reward.getFixedOptions());

        GiftBoxOption[][] randomOptionGroups = reward.getRandomOptionGroups();
        if (randomOptionGroups.length > 0) {
            GiftBoxOption[] randomOptions = randomOptionGroups[Util.nextInt(0, randomOptionGroups.length - 1)];
            addOptions(itemReward, randomOptions);
        }

        GiftBoxHsd hsd = selectHsd(reward.getHsdRates());
        if (hsd != null && hsd.getDays() > 0) {
            itemReward.itemOptions.add(new Item.ItemOption(93, hsd.getDays()));
        }

        InventoryService.gI().addItemBag(player, itemReward);
        InventoryService.gI().sendItemBag(player);
        InventoryService.gI().subQuantityItemsBag(player, itemUse, 1);
        Service.gI().sendThongBao(player, "Bạn mở rương nhận được " + itemReward.template.name);
    }

    private GiftBoxReward selectReward(GiftBoxReward[] rewards) {
        if (rewards == null || rewards.length == 0) {
            return null;
        }

        int totalWeight = 0;
        for (GiftBoxReward reward : rewards) {
            totalWeight += reward.getWeight();
        }
        if (totalWeight <= 0) {
            return null;
        }

        int roll = Util.nextInt(1, totalWeight);
        int running = 0;
        for (GiftBoxReward reward : rewards) {
            running += reward.getWeight();
            if (roll <= running) {
                return reward;
            }
        }
        return rewards[rewards.length - 1];
    }

    private GiftBoxHsd selectHsd(GiftBoxHsd[] rates) {
        if (rates == null || rates.length == 0) {
            return null;
        }

        int totalWeight = 0;
        for (GiftBoxHsd rate : rates) {
            totalWeight += rate.getWeight();
        }
        if (totalWeight <= 0) {
            return null;
        }

        int roll = Util.nextInt(1, totalWeight);
        int running = 0;
        for (GiftBoxHsd rate : rates) {
            running += rate.getWeight();
            if (roll <= running) {
                return rate;
            }
        }
        return rates[rates.length - 1];
    }

    private void addOptions(Item item, GiftBoxOption[] options) {
        if (item == null || options == null) {
            return;
        }

        for (GiftBoxOption option : options) {
            item.itemOptions.add(new Item.ItemOption(option.getId(), option.getRandomParam()));
        }
    }
}
