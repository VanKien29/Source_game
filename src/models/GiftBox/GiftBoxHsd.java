package models.GiftBox;

public final class GiftBoxHsd {

    private final int days;
    private final int weight;

    public GiftBoxHsd(int days, int weight) {
        this.days = Math.max(0, days);
        this.weight = Math.max(0, weight);
    }

    public static GiftBoxHsd rate(int days, int weight) {
        return new GiftBoxHsd(days, weight);
    }

    public static GiftBoxHsd[] rates(GiftBoxHsd... rates) {
        return rates == null ? new GiftBoxHsd[0] : rates;
    }

    public int getDays() {
        return days;
    }

    public int getWeight() {
        return weight;
    }
}
