package models.GiftBox;

public final class GiftBoxReward {

    private final int itemId;
    private final int weight;
    private final int quantityMin;
    private final int quantityMax;
    private final GiftBoxOption[] fixedOptions;
    private final GiftBoxHsd[] hsdRates;
    private final GiftBoxOption[][] randomOptionGroups;

    public GiftBoxReward(int itemId, int weight, int quantityMin, int quantityMax,
            GiftBoxOption[] fixedOptions, GiftBoxHsd[] hsdRates,
            GiftBoxOption[]... randomOptionGroups) {
        this.itemId = itemId;
        this.weight = Math.max(0, weight);
        this.quantityMin = Math.max(1, Math.min(quantityMin, quantityMax));
        this.quantityMax = Math.max(this.quantityMin, quantityMax);
        this.fixedOptions = fixedOptions == null ? new GiftBoxOption[0] : fixedOptions;
        this.hsdRates = hsdRates == null ? new GiftBoxHsd[0] : hsdRates;
        this.randomOptionGroups = randomOptionGroups == null ? new GiftBoxOption[0][] : randomOptionGroups;
    }

    public static GiftBoxReward giftReward(int itemId, int weight,
            GiftBoxOption[] fixedOptions, GiftBoxHsd[] hsdRates,
            GiftBoxOption[]... randomOptionGroups) {
        return new GiftBoxReward(itemId, weight, 1, 1, fixedOptions, hsdRates, randomOptionGroups);
    }

    public int getItemId() {
        return itemId;
    }

    public int getWeight() {
        return weight;
    }

    public int getQuantityMin() {
        return quantityMin;
    }

    public int getQuantityMax() {
        return quantityMax;
    }

    public GiftBoxOption[] getFixedOptions() {
        return fixedOptions;
    }

    public GiftBoxHsd[] getHsdRates() {
        return hsdRates;
    }

    public GiftBoxOption[][] getRandomOptionGroups() {
        return randomOptionGroups;
    }
}
