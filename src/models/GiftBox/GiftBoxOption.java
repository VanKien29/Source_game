package models.GiftBox;

public final class GiftBoxOption {

    private final int id;
    private final int min;
    private final int max;

    public GiftBoxOption(int id, int min, int max) {
        this.id = id;
        this.min = Math.min(min, max);
        this.max = Math.max(min, max);
    }

    public static GiftBoxOption option(int id, int param) {
        return new GiftBoxOption(id, param, param);
    }

    public static GiftBoxOption option(int id, int min, int max) {
        return new GiftBoxOption(id, min, max);
    }

    public static GiftBoxOption[] options(GiftBoxOption... options) {
        return options == null ? new GiftBoxOption[0] : options;
    }

    public int getId() {
        return id;
    }

    public int getRandomParam() {
        return min == max ? min : utils.Util.nextInt(min, max);
    }
}
