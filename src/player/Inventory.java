package player;

/*
 *
 *
 * @author CongHoan
 */
import java.util.ArrayList;
import java.util.List;
import item.Item;
import item.Item.ItemOption;
import services.ItemService;

public class Inventory {

    public static final byte TYPE_NORMAL_BOX = 0;
    public static final byte TYPE_COLLECTION_BOX = 1;
    public static final byte MAX_ITEM_BOX_COLLECTION = 40;
    public static final long PRICE_SLOT_COLLECTION_BOX = 500_000_000_000L;

    public static final long LIMIT_GOLD = 200_000_000_000_000L;
    public static final long LIMIT_GEM = 200_000_000_000_000L;
    public static final long LIMIT_RUBY = 200_000_000_000_000L;
    public static final int MAX_ITEMS_BAG = 100;
    public static final int MAX_ITEMS_BOX = 100;
    public static final int BODY_SLOT_COUNT = 13;     // 0..12
    public static final int BASE_BODY_SLOT_COUNT = 6; // 6 ô mặc định
    public Item trainArmor;
    public List<String> giftCode;
    public List<Item> itemsBody;
    public List<Item> itemsBag;
    public List<Item> itemsBox;

    public List<Item> itemsMailBox;
    public List<Item> itemsBoxCollection;

    public List<Item> itemsBoxCrackBall;
    public List<Item> itemsDaBan;

    public long gold;
    public long gem;
    public long ruby;
    public int coupon;
    public int event;

    public Inventory() {
        itemsBody = new ArrayList<>();
        itemsBag = new ArrayList<>();
        itemsBox = new ArrayList<>();
        itemsBoxCrackBall = new ArrayList<>();
        itemsMailBox = new ArrayList<>();
        itemsDaBan = new ArrayList<>();
        giftCode = new ArrayList<>();
        itemsBoxCollection = new ArrayList<>();
        for (int i = 0; i < BODY_SLOT_COUNT; i++) {
            itemsBody.add(ItemService.gI().createItemNull());
        }
    }

    public long getGemAndRuby() {
        return this.gem + this.ruby;
    }

    public int getParam(Item it, int id) {
        for (ItemOption op : it.itemOptions) {
            if (op != null && op.optionTemplate.id == id) {
                return op.param;
            }
        }
        return 0;
    }

    public boolean haveOption(List<Item> l, int index, int id) {
        Item it = l.get(index);
        if (it != null && it.isNotNullItem()) {
            return it.itemOptions.stream().anyMatch(op -> op != null && op.optionTemplate.id == id);
        }
        return false;
    }

    public void subGemAndRuby(long num) {
        this.ruby -= num;
        if (this.ruby < 0) {
            this.gem += this.ruby;
            this.ruby = 0;
        }
    }

    public void addGold(long gold) {
        this.gold += gold;
        if (this.gold > LIMIT_GOLD) {
            this.gold = LIMIT_GOLD;
        }
    }

    public void addGem(long gem) {
        this.gem += gem;
        if (this.gem > LIMIT_GEM) {
            this.gem = LIMIT_GEM;
        }
    }

    public void addRuby(long ruby) {
        this.ruby += ruby;
        if (this.ruby > LIMIT_RUBY) {
            this.ruby = LIMIT_RUBY;
        }
    }

    public static long clampGem(long gem) {
        if (gem < 0) {
            return 0;
        }
        return Math.min(gem, LIMIT_GEM);
    }

    public static long clampRuby(long ruby) {
        if (ruby < 0) {
            return 0;
        }
        return Math.min(ruby, LIMIT_RUBY);
    }

    public void dispose() {
        if (this.trainArmor != null) {
            this.trainArmor.dispose();
        }
        this.trainArmor = null;
        if (this.itemsBody != null) {
            for (Item it : this.itemsBody) {
                it.dispose();
            }
            this.itemsBody.clear();
        }
        if (this.itemsBag != null) {
            for (Item it : this.itemsBag) {
                it.dispose();
            }
            this.itemsBag.clear();
        }
        if (this.itemsBox != null) {
            for (Item it : this.itemsBox) {
                it.dispose();
            }
            this.itemsBox.clear();
        }
        if (this.itemsBoxCrackBall != null) {
            for (Item it : this.itemsBoxCrackBall) {
                it.dispose();
            }
            this.itemsBoxCrackBall.clear();
        }
        if (this.itemsMailBox != null) {
            for (Item it : this.itemsMailBox) {
                it.dispose();
            }
            this.itemsMailBox.clear();
        }
        if (this.itemsDaBan != null) {
            for (Item it : this.itemsDaBan) {
                it.dispose();
            }
            this.itemsDaBan.clear();
        }
        if (this.itemsBoxCollection != null) {
            this.itemsBoxCollection.forEach(Item::dispose);
            this.itemsBoxCollection.clear();
        }
        this.itemsBody = null;
        this.itemsBag = null;
        this.itemsBox = null;
        this.itemsBoxCrackBall = null;
        this.itemsMailBox = null;
        this.itemsDaBan = null;
    }

}
