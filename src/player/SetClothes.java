package player;

/*
 *
 *
 * @author CongHoan
 */
import item.Item;

public class SetClothes {

    public static final int OPTION_SKH_LEVEL_START = 256;
    public static final int OPTION_SKH_LEVEL_END = 261;
    public static final int OPTION_SKH_FULL_SET_BONUS = 262;
    private static final int[] SKH_LEVEL_EFFECT_PERCENT = {25, 50, 75, 100, 125, 150};
    private static final int[] SKH_LEVEL_FULL_SET_PERCENT = {0, 1, 3, 5, 8, 12};

    private Player player;

    public SetClothes(Player player) {
        this.player = player;
    }

    public byte songoku;
    public byte thienXinHang;
    public byte kirin;
    public byte kaioken;
    public byte thanVuTruKaio;

    public byte ocTieu;
    public byte pikkoroDaimao;
    public byte picolo;
    public byte lienHoan;
    public byte nail;

    public byte kakarot;
    public byte cadic;
    public byte nappa;
    public byte giamSatThuong;
    public byte cadicM;
    
    public byte hdpe;
    public byte skhFullSetLevel;

    public byte worldcup;
    public byte setDHD;

    public boolean godClothes;
    public int ctHaiTac = -1;
    public int ctDietQuy = -1;
    public int ctBunmaTocMau = -1;
    public int ctTiecbaiBien = -1;

    public void setup() {
        setDefault();
        setupSKT();
        setupSKHNew();
        setupSKHLevel();
        this.godClothes = true;
        for (int i = 0; i < 5; i++) {
            Item item = this.player.inventory.itemsBody.get(i);
            if (item.isNotNullItem()) {
                if (item.template.id > 567 || item.template.id < 555) {
                    this.godClothes = false;
                    break;
                }
            } else {
                this.godClothes = false;
                break;
            }
        }
        Item ct = this.player.inventory.itemsBody.get(5);
        if (ct.isNotNullItem()) {
            switch (ct.template.id) {
                case 618:
                case 619:
                case 620:
                case 621:
                case 622:
                case 623:
                case 624:
                case 626:
                case 627:
                    this.ctHaiTac = ct.template.id;
                    break;
                case 1087:
                case 1088:
                case 1089:
                case 1090:
                case 1091:
                    this.ctDietQuy = ct.template.id;
                    break;
                case 1208:
                case 1209:
                case 1210:
                    this.ctBunmaTocMau = ct.template.id;
                    break;
                case 1234:
                case 1235:
                case 1236:
                    this.ctTiecbaiBien = ct.template.id;
                    break;

            }
        }

    }

    private void setupSKT() {
        for (int i = 0; i < 5; i++) {
            Item item = this.player.inventory.itemsBody.get(i);
            if (item.isNotNullItem()) {
                boolean isActSet = false;
                for (Item.ItemOption io : item.itemOptions) {
                    switch (io.optionTemplate.id) {
                        case 129:
                        case 141:
                            isActSet = true;
                            songoku++;
                            break;
                        case 127:
                        case 139:
                            isActSet = true;
                            thienXinHang++;
                            break;
                        case 128:
                        case 140:
                            isActSet = true;
                            kirin++;
                            break;
                        case 131:
                        case 143:
                            isActSet = true;
                            ocTieu++;
                            break;
                        case 132:
                        case 144:
                            isActSet = true;
                            pikkoroDaimao++;
                            break;
                        case 130:
                        case 142:
                            isActSet = true;
                            picolo++;
                            break;
                        case 135:
                        case 138:
                            isActSet = true;
                            nappa++;
                            break;
                        case 133:
                        case 136:
                            isActSet = true;
                            kakarot++;
                            break;
                        case 134:
                        case 137:
                            isActSet = true;
                            cadic++;
                            break;
                        case 179:
                        case 180:
                            isActSet = true;
                            hdpe++;
                            break;
                        case 250:
                        case 253:
                            isActSet = true;
                            kaioken++;
                            break;
                        case 251:
                        case 254:
                            isActSet = true;
                            lienHoan++;
                            break;
                        case 252:
                        case 255:
                            isActSet = true;
                            giamSatThuong++;
                            break;

                        case 21:
                            if (io.param == 80) {
                                setDHD++;
                            }
                            break;
                    }

                    if (isActSet) {
                        break;
                    }
                }
            } else {
                break;
            }
        }
    }

    private void setupSKHNew() {
        for (int i = 0; i < 5; i++) {
            Item item = this.player.inventory.itemsBody.get(i);
            if (item.isNotNullItem()) {
                boolean isActSet = false;
                for (Item.ItemOption io : item.itemOptions) {
                    switch (io.optionTemplate.id) {
                        case 245:
                        case 246:
                        case 247:
                        case 248:
                            isActSet = true;
                            thanVuTruKaio++;
                            break;
                        case 237:
                        case 238:
                        case 239:
                        case 240:
                            isActSet = true;
                            nail++;
                            break;
                        case 241:
                        case 242:
                        case 243:
                        case 244:
                            isActSet = true;
                            cadicM++;
                            break;
                    }

                    if (isActSet) {
                        break;
                    }
                }
            } else {
                break;
            }
        }
    }

    private void setupSKHLevel() {
        int[] count = new int[13];
        int[] minLevel = new int[13];
        for (int i = 0; i < minLevel.length; i++) {
            minLevel[i] = 5;
        }
        for (int i = 0; i < 5; i++) {
            Item item = this.player.inventory.itemsBody.get(i);
            if (!item.isNotNullItem()) {
                break;
            }
            int setIndex = -1;
            for (Item.ItemOption io : item.itemOptions) {
                setIndex = getOldSKHSetIndex(io.optionTemplate.id);
                if (setIndex >= 0) {
                    break;
                }
            }
            if (setIndex >= 0) {
                int level = getSKHLevel(item);
                count[setIndex]++;
                if (level < minLevel[setIndex]) {
                    minLevel[setIndex] = level;
                }
            }
        }
        for (int i = 0; i < count.length; i++) {
            if (count[i] == 5) {
                this.skhFullSetLevel = (byte) minLevel[i];
                return;
            }
        }
    }

    public static int getOldSKHSetIndex(int optionId) {
        switch (optionId) {
            case 129:
            case 141:
                return 0;
            case 127:
            case 139:
                return 1;
            case 128:
            case 140:
                return 2;
            case 131:
            case 143:
                return 3;
            case 132:
            case 144:
                return 4;
            case 130:
            case 142:
                return 5;
            case 135:
            case 138:
                return 6;
            case 133:
            case 136:
                return 7;
            case 134:
            case 137:
                return 8;
            case 250:
            case 253:
                return 9;
            case 251:
            case 254:
                return 10;
            case 252:
            case 255:
                return 11;
            case 179:
            case 180:
                return 12;
            default:
                return -1;
        }
    }

    public static boolean isOldSKHOption(int optionId) {
        return (optionId >= 127 && optionId <= 144)
                || (optionId >= 250 && optionId <= 255)
                || optionId == 179 || optionId == 180;
    }

    public static int getSKHLevel(Item item) {
        if (item == null || !item.isNotNullItem()) {
            return 0;
        }
        for (Item.ItemOption io : item.itemOptions) {
            if (io != null && io.optionTemplate != null
                    && io.optionTemplate.id >= OPTION_SKH_LEVEL_START
                    && io.optionTemplate.id <= OPTION_SKH_LEVEL_END) {
                if (io.optionTemplate.id == OPTION_SKH_LEVEL_START && io.param > 0) {
                    return Math.min(io.param, OPTION_SKH_LEVEL_END - OPTION_SKH_LEVEL_START);
                }
                return io.optionTemplate.id - OPTION_SKH_LEVEL_START;
            }
        }
        return 0;
    }

    public int getSKHLevelEffectPercent() {
        return SKH_LEVEL_EFFECT_PERCENT[this.skhFullSetLevel];
    }

    public int getSKHFullSetBonusPercent() {
        return SKH_LEVEL_FULL_SET_PERCENT[this.skhFullSetLevel];
    }

    private void setDefault() {
        this.songoku = 0;
        this.thienXinHang = 0;
        this.kirin = 0;
        this.kaioken = 0;
        this.ocTieu = 0;
        this.pikkoroDaimao = 0;
        this.picolo = 0;
        this.lienHoan = 0;
        this.kakarot = 0;
        this.cadic = 0;
        this.nappa = 0;
        this.hdpe = 0;
        this.giamSatThuong = 0;
        this.skhFullSetLevel = 0;

        this.thanVuTruKaio = 0;

        this.nail = 0;

        this.cadicM = 0;

        this.setDHD = 0;
        this.worldcup = 0;
        this.godClothes = false;
        this.ctHaiTac = -1;
        this.ctDietQuy = -1;
        this.ctBunmaTocMau = -1;
        this.ctTiecbaiBien = -1;
    }

    public boolean checkSetGod() {
        for (int i = 0; i < 5; i++) {
            Item item = this.player.inventory.itemsBody.get(i);
            if (item.isNotNullItem()) {
                if (item.template.id < 555 || item.template.id > 567) {
                    return false;
                }
            } else {
                return false;
            }
        }
        return true;
    }

    public boolean checkSetDes() {
        for (int i = 0; i < 5; i++) {
            Item item = this.player.inventory.itemsBody.get(i);
            if (item.isNotNullItem()) {
                if (item.template.id < 650 || item.template.id > 662) {

                    return false;
                }
            } else {
                return false;
            }
        }
        return true;
    }

    public int getSetKichHoatEffectId() {
        if (kakarot == 5 || cadic == 5 || nappa == 5 || giamSatThuong == 5 || cadicM == 5) {
            return 86;
        }
        if (ocTieu == 5 || pikkoroDaimao == 5 || picolo == 5 || lienHoan == 5 || nail == 5) {
            return 87;
        }
        if (songoku == 5 || thienXinHang == 5 || kirin == 5 || kaioken == 5 || thanVuTruKaio == 5) {
            return 88;
        }
        return -1;
    }

    public void dispose() {
        this.player = null;
    }
}
