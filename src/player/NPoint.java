package player;

/*
 *
 *
 * @author CongHoan
 */
import models.Card.Card;
import models.Card.OptionCard;
import consts.ConstPlayer;
import consts.ConstRatio;
import intrinsic.Intrinsic;
import item.Item;
import item.Item.ItemOption;
import skill.Skill;
import server.Manager;
import services.EffectSkillService;
import services.ItemService;
import services.MapService;
import services.PlayerService;
import services.Service;
import services.TaskService;
import utils.Logger;
import utils.SkillUtil;
import utils.Util;

import java.util.ArrayList;
import java.util.List;

import jdbc.daos.EventDAO;
import lombok.Setter;
import mob.Mob;
import npc.npc_manifest.TienCap;
import power.PowerLimit;
import power.PowerLimitManager;
import utils.TimeUtil;

public class NPoint {

    public static final byte MAX_LIMIT = 13;
    private static final long[] EARLY_CRIT_TIEM_NANG_COST = {
        50_000_000L,
        250_000_000L,
        1_000_000_000L,
        5_000_000_000L,
        15_000_000_000L
    };

    @Setter
    private Player player;

    public NPoint(Player player) {
        this.player = player;
        this.tlHp = new ArrayList<>();
        this.tlMp = new ArrayList<>();
        this.tlDame = new ArrayList<>();
        this.tlDameAttMob = new ArrayList<>();
        this.tlTNSM = new ArrayList<>();
        this.tlDameCrit = new ArrayList<>();
    }

    public boolean isCrit;
    public boolean isCrit100;
    public boolean isCritTele;

    private Intrinsic intrinsic;
    private int percentDameIntrinsic;
    public long dameAfter;
    private PowerLimit powerLimit;
    /*-----------------------Chỉ số cơ bản------------------------------------*/
    public byte numAttack;
    public short stamina, maxStamina;

    public byte limitPower;
    public long power;
    public long tiemNang;

    public long hp, hpMax, hpg;
    public long mp, mpMax, mpg;
    public long dame, dameg;
    public int def, defg;
    public int crit, critg;
    public byte speed = 5;

    public int hpbang, mpbang, damebang, critbang;

    public boolean teleport;

    public boolean khangTDHS;

    public void initPowerLimit() {
        powerLimit = PowerLimitManager.getInstance().get(limitPower);
    }

    /**
     * Chỉ số cộng thêm
     */
    public int hpAdd, mpAdd, dameAdd, defAdd, critAdd, hpHoiAdd, mpHoiAdd;

    /**
     * //+#% sức đánh chí mạng
     */
    public List<Integer> tlDameCrit;
    public int tlSDCM;

    /**
     * Tỉ lệ hp, mp cộng thêm
     */
    public List<Integer> tlHp, tlMp;

    /**
     * Tỉ lệ giáp cộng thêm
     */
    public byte tlDef;

    /**
     * Tỉ lệ sức đánh/ sức đánh khi đánh quái
     */
    public List<Integer> tlDame, tlDameAttMob;

    /**
     * Lượng hp, mp hồi mỗi 30s, mp hồi cho người khác
     */
    public long hpHoi, mpHoi, mpHoiCute;

    /**
     * Tỉ lệ hp, mp hồi cộng thêm
     */
    public short tlHpHoi, tlMpHoi;

    /**
     * Tỉ lệ hp, mp hồi bản thân và đồng đội cộng thêm
     */
    public short tlHpHoiBanThanVaDongDoi, tlMpHoiBanThanVaDongDoi;

    /**
     * Tỉ lệ hút hp, mp khi đánh, hp khi đánh quái
     */
    public short tlHutHp, tlHutMp, tlHutHpMob;

    /**
     * Tỉ lệ hút hp, mp xung quanh mỗi 5s
     */
    public short tlHutHpMpXQ;

    /**
     * Tỉ lệ phản sát thương
     */
    public short tlPST;

    /**
     * Tỉ lệ tiềm năng sức mạnh
     */
    public List<Integer> tlTNSM;

    /**
     * Tỉ lệ vàng cộng thêm
     */
    public short tlGold;

    /**
     * Tỉ lệ né đòn
     */
    public short tlNeDon;

    public short tlBom;

    public short tlGiap;

    public short tlxgcc;

    public short tlxgc;

    public short tlchinhxac;

    public short tlTNSMPet;
    public short xChuong;

    public short setltdb;
    public short setTinhAn;
    public short setNhatAn;
    public short setNguyetAn;

    /**
     * Tỉ lệ sức đánh đẹp cộng thêm cho bản thân và người xung quanh
     */
    public int tlSexyDame;

    /**
     * Tỉ lệ giảm sức đánh
     */
    public short tlSubSD;

    public int voHieuChuong;
    public int csbang;

    /*------------------------Effect skin-------------------------------------*/
    public Item trainArmor;
    public boolean wearingTrainArmor;

    public boolean wearingVoHinh;
    public boolean isKhongLanh;
    public boolean islinhthuydanhbac;
    public boolean isTinhAn;
    public boolean isNhatAn;
    public boolean isNguyetAn;
    public boolean isTanHinh;
    public boolean isHoaDa;
    public boolean isLamCham;
    public boolean isDoSPL;
    public boolean isThoBulma;
    public boolean isDietQuy;
    public boolean isBunmaTocMau;
    public boolean isTiecBaiBien;
    public short tlHpGiamODo;
    public boolean isBanthan;
    public boolean isKiemZ2;
    public boolean isBoCao;
    public boolean isHoaTiemThuong;
    public boolean isPhongHoaLuan;
    public boolean isBatGioi;
    public boolean isPicoloTH;
    public boolean isRong7sao;
    public boolean isVanTeNuoc;

    public boolean isGogeta;
    public boolean isKamiOren;

    public int tlSpeed;

    public int levelBT;

    /*-------------------------------------------------------------------------*/
    /**
     * Tính toán mọi chỉ số sau khi có thay đổi
     */
    public void calPoint() {
        if (this.player.pet != null) {
            this.player.pet.nPoint.setPointWhenWearClothes();
        }
        this.setPointWhenWearClothes();
    }

    private void setPointWhenWearClothes() {
        resetPoint();
        if (this.player.rewardBlackBall.timeOutOfDateReward[2] > System.currentTimeMillis()) {
            tlHutHp += RewardBlackBall.R3S_1;
        }
        if (this.player.rewardBlackBall.timeOutOfDateReward[3] > System.currentTimeMillis()) {
            tlPST += RewardBlackBall.R4S_2;
        }
        if (this.player.rewardBlackBall.timeOutOfDateReward[4] > System.currentTimeMillis()) {
            tlDameCrit.add(RewardBlackBall.R5S_1);
            tlSDCM += RewardBlackBall.R5S_1;
        }
        if (this.player.rewardBlackBall.timeOutOfDateReward[6] > System.currentTimeMillis()) {
            tlNeDon += RewardBlackBall.R7S_1;
        }

        Card card = player.Cards.stream().filter(r -> r != null && r.Used == 1).findFirst().orElse(null);
        if (card != null) {
            for (OptionCard io : card.Options) {
                if (io.active == card.Level || (card.Level == -1 && io.active == 0)) {
                    switch (io.id) {
                        case 0: // Tấn công +#
                            this.dameAdd += io.param;
                            break;
                        case 2: // HP, KI+#000
                            this.hpAdd += io.param * 1000;
                            this.mpAdd += io.param * 1000;
                            break;
                        case 3:// vô hiệu chưởng
                            this.voHieuChuong += io.param;
                            break;
                        case 5: // +#% sức đánh chí mạng
                            this.tlDameCrit.add(io.param);
                            this.tlSDCM += io.param;
                            break;
                        case 6: // HP+#
                            this.hpAdd += io.param;
                            break;
                        case 7: // KI+#
                            this.mpAdd += io.param;
                            break;
                        case 8: // Hút #% HP, KI xung quanh mỗi 5 giây
                            this.tlHutHpMpXQ += io.param;
                            break;
                        case 14: // Chí mạng+#%
                            this.critAdd += io.param;
                            break;
                        case 16: // Speed
                        case 114:
                        case 148:
                            this.tlSpeed += io.param;
                            break;
                        case 18: // Chinh xac
                            this.tlchinhxac += io.param;
                            break;
                        case 19: // Tấn công+#% khi đánh quái
                            this.tlDameAttMob.add(io.param);
                            break;
                        case 22: // HP+#K
                            this.hpAdd += io.param * 1000;
                            break;
                        case 23: // MP+#K
                            this.mpAdd += io.param * 1000;
                            break;
                        case 27: // +# HP/30s
                            this.hpHoiAdd += io.param;
                            break;
                        case 28: // +# KI/30s
                            this.mpHoiAdd += io.param;
                            break;
                        case 33: // dịch chuyển tức thời
                            this.teleport = true;
                            break;
                        case 34:
                            this.setTinhAn += 1;
                            break;
                        case 35:
                            this.setNguyetAn += 1;
                            break;
                        case 36:
                            this.setNhatAn += 1;
                            break;
                        case 47: // Giáp+#
                            this.defAdd += io.param;
                            break;
                        case 48: // HP/KI+#
                            this.hpAdd += io.param;
                            this.mpAdd += io.param;
                            break;
                        case 49: // Tấn công+#%
                        case 50: // Sức đánh+#%
                            this.tlDame.add(io.param);
                            break;
                        case 77: // HP+#%
                            this.tlHp.add(io.param);
                            break;
                        case 80: // HP+#%/30s
                            this.tlHpHoi += io.param;
                            break;
                        case 81: // MP+#%/30s
                            this.tlMpHoi += io.param;
                            break;
                        case 88: // Cộng #% exp khi đánh quái
                            this.tlTNSM.add(io.param);
                            break;
                        case 94: // Giáp #%
                            this.tlDef += io.param;
                            break;
                        case 95: // Biến #% tấn công thành HP
                            this.tlHutHp += io.param;
                            break;
                        case 96: // Biến #% tấn công thành MP
                            this.tlHutMp += io.param;
                            break;
                        case 97: // Phản #% sát thương
                            this.tlPST += io.param;
                            break;
                        case 98: // Xuyen giap chuong
                            this.tlxgc += io.param;
                            break;
                        case 99: // Xuyen giap can chien
                            this.tlxgcc += io.param;
                            break;
                        case 100: // +#% vàng từ quái
                            this.tlGold += io.param;
                            break;
                        case 101: // +#% TN,SM
                            this.tlTNSM.add(io.param);
                            break;
                        case 103: // KI +#%
                            this.tlMp.add(io.param);
                            break;
                        case 104: // Biến #% tấn công quái thành HP
                            this.tlHutHpMob += io.param;
                            break;
                        case 105: // Vô hình khi không đánh quái và boss
                            this.wearingVoHinh = true;
                            break;
                        case 106: // Không ảnh hưởng bởi cái lạnh
                            this.isKhongLanh = true;
                            break;
                        case 108: // #% Né đòn
                            this.tlNeDon += io.param;
                            break;
                        case 109: // Hôi, giảm #% HP
                            this.tlHpGiamODo += io.param;
                            break;
                        case 116: // Kháng thái dương hạ san
                            this.khangTDHS = true;
                            break;
                        case 153: // Kháng thái dương hạ san
                            this.tlBom += io.param;
                            break;
                        case 117: // Đẹp +#% SĐ cho mình và người xung quanh
                            if (io.param > this.tlSexyDame) {
                                this.tlSexyDame = io.param;
                            }
                            break;
                        case 147: // +#% sức đánh
                            this.tlDame.add(io.param);
                            break;
                        case 156: // Giảm 50% sức đánh, HP, KI và +#% SM, TN, vàng từ quái
                            this.tlSubSD += 50;
                            this.tlTNSM.add(io.param);
                            this.tlGold += io.param;
                            break;
                        case 162: // Cute hồi #% KI/s bản thân và xung quanh
                            this.mpHoiCute += io.param;
                            break;
                        case 173: // Phục hồi #% HP và KI cho đồng đội
                            this.tlHpHoiBanThanVaDongDoi += io.param;
                            this.tlMpHoiBanThanVaDongDoi += io.param;
                            break;

                    }
                }
            }
        }

        // Bông tai cấp 2
        if (this.player.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA2) {
            this.player.inventory.itemsBag.stream().filter(it -> it.isNotNullItem() && it.template.id == 921)
                    .findFirst().ifPresent(btc2 -> {
                        for (ItemOption io : btc2.itemOptions) {
                            addOption(io);
                            if (io.optionTemplate.id == 72) {
                                this.levelBT = io.param;
                            }
                        }
                    });
        }
        if (player.clan != null) {
            int a = player.clan.level;
            if (a >= 2) {
                this.tlTNSM.add(20);
            }
            if (a >= 3) {
                this.csbang += 1;
            }
            if (a >= 8) {
                this.csbang += 5;
            }
        }
        // Bông tai cấp 3
        if (this.player.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA3) {
            this.player.inventory.itemsBag.stream().filter(it -> it.isNotNullItem() && it.template.id == 1774)
                    .findFirst().ifPresent(btc3 -> {
                        for (ItemOption io : btc3.itemOptions) {
                            addOption(io);
                            if (io.optionTemplate.id == 72) {
                                this.levelBT = io.param;
                            }
                        }
                    });
        }

        this.player.setClothes.worldcup = 0;
        for (Item item : this.player.inventory.itemsBody) {
            if (item.isNotNullItem()) {
                switch (item.template.id) {
                    case 966:
                    case 982:
                    case 983:
                    case 883:
                    case 904:
                        player.setClothes.worldcup++;
                }
                if (item.template.id >= 592 && item.template.id <= 594) {
                    teleport = true;
                }
                for (ItemOption io : item.itemOptions) {
                    addOption(io);
                }
            }
        }
        applyIntrinsicGoldDrop();
        setDameTrainArmor();
        setBasePoint();
        setOutfitFusion();
        setPet();
        //  setLinhThu();
        setCaiTrang();
        setDeoLung();
        setThuCuoi();

        setSpeed();
    }

    private void applyIntrinsicGoldDrop() {
        if (this.player.playerIntrinsic != null
                && this.player.playerIntrinsic.intrinsic != null
                && this.player.playerIntrinsic.intrinsic.id == 23) {
            this.tlGold += this.player.playerIntrinsic.intrinsic.param1;
        }
    }

    private void addOption(ItemOption io) {
        switch (io.optionTemplate.id) {
            case 0: // Tấn công +#
                this.dameAdd += io.param;
                break;
            case 2: // HP, KI+#000
                this.hpAdd += io.param * 1000;
                this.mpAdd += io.param * 1000;
                break;
            case 3:// vô hiệu chưởng
                this.voHieuChuong += io.param;
                break;
            case 5: // +#% sức đánh chí mạng
                this.tlDameCrit.add(io.param);
                this.tlSDCM += io.param;
                break;
            case 6: // HP+#
                this.hpAdd += io.param;
                break;
            case 7: // KI+#
                this.mpAdd += io.param;
                break;
            case 8: // Hút #% HP, KI xung quanh mỗi 5 giây
                this.tlHutHpMpXQ += io.param;
                break;
            case 14: // Chí mạng+#%
                this.critAdd += io.param;
                break;
            case 16: // Speed
            case 114:
            case 148:
                this.tlSpeed += io.param;
                break;
            case 18: // Chinh xac
                this.tlchinhxac += io.param;
                break;
            case 19: // Tấn công+#% khi đánh quái
                this.tlDameAttMob.add(io.param);
                break;
            case 22: // HP+#K
                this.hpAdd += io.param * 1000;
                break;
            case 23: // MP+#K
                this.mpAdd += io.param * 1000;
                break;
            case 24: // Làm chậm
                this.isLamCham = true;
                break;
            case 25: // Tàn hình
                this.isTanHinh = true;
                break;
            case 26: // Hóa đá
                this.isHoaDa = true;
                break;
            case 27: // +# HP/30s
                this.hpHoiAdd += io.param;
                break;
            case 28: // +# KI/30s
                this.mpHoiAdd += io.param;
                break;
            case 33: // dịch chuyển tức thời
                this.teleport = true;
                break;
            case 34:
                this.setTinhAn += 1;
                break;
            case 35:
                this.setNguyetAn += 1;
                break;
            case 36:
                this.setNhatAn += 1;
                break;
            case 47: // Giáp+#
                this.defAdd += io.param;
                break;
            case 48: // HP/KI+#
                this.hpAdd += io.param;
                this.mpAdd += io.param;
                break;
            case 49: // Tấn công+#%
            case 50: // Sức đánh+#%
                this.tlDame.add(io.param);
                break;
            case 77: // HP+#%
                this.tlHp.add(io.param);
                break;
            case 80: // HP+#%/30s
                this.tlHpHoi += io.param;
                break;
            case 81: // MP+#%/30s
                this.tlMpHoi += io.param;
                break;
            case 88: // Cộng #% exp khi đánh quái
                this.tlTNSM.add(io.param);
                break;
            case 94: // Giáp #%
                this.tlDef += io.param;
                break;
            case 95: // Biến #% tấn công thành HP
                this.tlHutHp += io.param;
                break;
            case 96: // Biến #% tấn công thành MP
                this.tlHutMp += io.param;
                break;
            case 97: // Phản #% sát thương
                this.tlPST += io.param;
                break;
            case 98: // Xuyen giap chuong
                this.tlxgc += io.param;
                break;
            case 99: // Xuyen giap can chien
                this.tlxgcc += io.param;
                break;
            case 100: // +#% vàng từ quái
                this.tlGold += io.param;
                break;
            case 101: // +#% TN,SM
                this.tlTNSM.add(io.param);
                break;
            case 103: // KI +#%
                this.tlMp.add(io.param);
                break;
            case 104: // Biến #% tấn công quái thành HP
                this.tlHutHpMob += io.param;
                break;
            case 105: // Vô hình khi không đánh quái và boss
                this.wearingVoHinh = true;
                break;
            case 106: // Không ảnh hưởng bởi cái lạnh
                this.isKhongLanh = true;
                break;
            case 108: // #% Né đòn
                this.tlNeDon += io.param;
                break;
            case 109: // Hôi, giảm #% HP
                this.tlHpGiamODo += io.param;
                break;
            case 110: // Do spl
                this.isDoSPL = true;
                break;
            case 116: // Kháng thái dương hạ san
                this.khangTDHS = true;
                break;

            case 117: // Đẹp +#% SĐ cho mình và người xung quanh
                if (io.param > this.tlSexyDame) {
                    this.tlSexyDame = io.param;
                }
                break;
            case 147: // +#% sức đánh
                this.tlDame.add(io.param);
                break;

            case 156: // Giảm 50% sức đánh, HP, KI và +#% SM, TN, vàng từ quái
                this.tlSubSD += 50;
                this.tlTNSM.add(io.param);
                this.tlGold += io.param;
                break;
            case 162: // Cute hồi #% KI/s bản thân và xung quanh
                this.mpHoiCute += io.param;
                break;
            case 159: // x chưởng
                this.xChuong = (short) io.param;
                break;
            case 160: // TNSM PET;
                this.tlTNSMPet += io.param;
                break;
            case 173: // Phục hồi #% HP và KI cho đồng đội
                this.tlHpHoiBanThanVaDongDoi += io.param;
                this.tlMpHoiBanThanVaDongDoi += io.param;
                break;
            case 176: //
                setInfoOption176();
                break;
            case 211:
                this.setltdb += 1;
                break;
            case 153: // % phát nổ sau khi chết
                this.tlBom += io.param;
                break;

        }
    }

    private void setSpeed() {
        if (player.isPl()) {
            speed = (byte) (5 + 3 * (tlSpeed / 100));
        }
    }

    private void setInfoOption176() {
        if (player.isPl()) {
            this.tlDame.add(10);
            speed = (byte) (5 + 3 * (50 / 100));
        }
    }

    private void setOutfitFusion() {
        if (this.player.inventory.itemsBody.size() < 6 || this.player.pet == null
                || this.player.pet.inventory.itemsBody.size() < 6) {
            return;
        }
        Item skin = this.player.inventory.itemsBody.get(5);
        Item pskin = this.player.pet.inventory.itemsBody.get(5);
        if (skin.isNotNullItem() && pskin.isNotNullItem()) {
            this.isGogeta = skin.template.id == 1693 && pskin.template.id == 1553
                    || skin.template.id == 1553 && pskin.template.id == 1693;
        } else {
            this.isGogeta = false;
        }
    }

    void setOutfitFusion2() {
        if (this.player.inventory.itemsBody.size() < 6 || this.player.pet == null || this.player.pet.inventory.itemsBody.size() < 6) {
            return;
        }
        Item skin = this.player.inventory.itemsBody.get(5);
        Item pskin = this.player.pet.inventory.itemsBody.get(5);
        if (skin.isNotNullItem() && pskin.isNotNullItem()) {
            this.isKamiOren = (skin.template.id == 1818 && pskin.template.id == 1817);
        } else {
            this.isKamiOren = false;
        }
    }

    private void setDameTrainArmor() {
        if (!this.player.isPet && !this.player.isBoss) {
            if (this.player.inventory.itemsBody.size() < 7) {
                return;
            }
            try {
                Item gtl = this.player.inventory.itemsBody.get(6);
                if (gtl.isNotNullItem()) {
                    this.wearingTrainArmor = true;
                    this.player.inventory.trainArmor = gtl;
                    this.tlSubSD += ItemService.gI().getPercentTrainArmor(gtl);
                } else {
                    if (this.player.inventory.trainArmor == null) {
                        gtl = this.player.inventory.itemsBag.stream()
                                .filter(item -> item.isNotNullItem() && item.template.type == 32
                                && item.itemOptions != null
                                && item.itemOptions.stream()
                                        .filter(io -> io.optionTemplate.id == 9 && io.param > 0).findFirst()
                                        .orElse(null) != null)
                                .findFirst().orElse(null);
                        if (gtl == null) {
                            return;
                        }
                        this.player.inventory.trainArmor = gtl;
                    }
                    this.wearingTrainArmor = false;
                    for (Item.ItemOption io : this.player.inventory.trainArmor.itemOptions) {
                        if (io.optionTemplate.id == 9 && io.param > 0) {
                            this.tlDame.add(ItemService.gI().getPercentTrainArmor(this.player.inventory.trainArmor));
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                Logger.error("Lỗi get giáp tập luyện " + this.player.name + "\n" + e + "\n");
            }
        }
    }

    public void setBasePoint() {
        // BẬT FLAG TRƯỚC KHI TÍNH CHỈ SỐ
        setCaiTrang();
        setDeoLung();
        setPet();
        setThuCuoi();
        // setLinhThu();
        setTinhNhatNguyetAn();

        //////// tính chỉ số
        setHpMax();
        setHp();
        setMpMax();
        setMp();
        setDame();
        setDef();
        setCrit();
        setHpHoi();
        setMpHoi();
        setLtdb();
        setThoBulma();
        setDietQuy();
        setTiecbaiBien();
        setBunmaTocMau();

        // setLinhThu();
    }

    private void setLtdb() {
        this.islinhthuydanhbac = this.setltdb >= 5;
    }

    private void setThoBulma() {
        this.isThoBulma = (this.player.inventory != null && this.player.inventory.itemsBody != null
                && this.player.inventory.itemsBody.size() >= 5 && this.player.inventory.itemsBody.get(5).isNotNullItem()
                && this.player.inventory.itemsBody.get(5).template.id == 584);
    }

    private void setDietQuy() {
        this.isDietQuy = (this.player.inventory != null && this.player.inventory.itemsBody != null
                && this.player.inventory.itemsBody.size() >= 5 && this.player.inventory.itemsBody.get(5).isNotNullItem()
                && this.player.inventory.itemsBody.get(5).template.id >= 1087
                && this.player.inventory.itemsBody.get(5).template.id <= 1091);
    }

    private void setBunmaTocMau() {
        this.isBunmaTocMau = (this.player.inventory != null && this.player.inventory.itemsBody != null
                && this.player.inventory.itemsBody.size() >= 5 && this.player.inventory.itemsBody.get(5).isNotNullItem()
                && this.player.inventory.itemsBody.get(5).template.id >= 1208
                && this.player.inventory.itemsBody.get(5).template.id <= 1210);
    }

    private void setTiecbaiBien() {
        this.isTiecBaiBien = (this.player.inventory != null && this.player.inventory.itemsBody != null
                && this.player.inventory.itemsBody.size() >= 5 && this.player.inventory.itemsBody.get(5).isNotNullItem()
                && this.player.inventory.itemsBody.get(5).template.id >= 1234
                && this.player.inventory.itemsBody.get(5).template.id <= 1236);
    }

    private void setTinhNhatNguyetAn() {
        this.isTinhAn = this.setTinhAn >= 5;
        this.isNhatAn = this.setNhatAn >= 5;
        this.isNguyetAn = this.setNguyetAn >= 5;
    }

    private void setHpHoi() {
        this.hpHoi = this.hpMax / 100;
        this.hpHoi += this.hpHoiAdd;

        // Kiểm tra giá trị tlHpHoi không vượt quá giới hạn
        if (this.tlHpHoi > 100) {
            this.tlHpHoi = 100;
        } else if (this.tlHpHoi < 0) {
            this.tlHpHoi = 0;
        }

        this.hpHoi += ((long) this.hpMax * this.tlHpHoi / 100);

        // Kiểm tra giá trị tlHpHoiBanThanVaDongDoi không vượt quá giới hạn
        if (this.tlHpHoiBanThanVaDongDoi > 100) {
            this.tlHpHoiBanThanVaDongDoi = 100;
        } else if (this.tlHpHoiBanThanVaDongDoi < 0) {
            this.tlHpHoiBanThanVaDongDoi = 0;
        }

        this.hpHoi += ((long) this.hpMax * this.tlHpHoiBanThanVaDongDoi / 100);
    }

    private void setMpHoi() {
        this.mpHoi = this.mpMax / 100;
        this.mpHoi += this.mpHoiAdd;

        // Kiểm tra giá trị tlMpHoi không vượt quá giới hạn
        if (this.tlMpHoi > 100) {
            this.tlMpHoi = 100;
        } else if (this.tlMpHoi < 0) {
            this.tlMpHoi = 0;
        }

        this.mpHoi += ((long) this.mpMax * this.tlMpHoi / 100);

        // Kiểm tra giá trị tlMpHoiBanThanVaDongDoi không vượt quá giới hạn
        if (this.tlMpHoiBanThanVaDongDoi > 100) {
            this.tlMpHoiBanThanVaDongDoi = 100;
        } else if (this.tlMpHoiBanThanVaDongDoi < 0) {
            this.tlMpHoiBanThanVaDongDoi = 0;
        }

        this.mpHoi += ((long) this.mpMax * this.tlMpHoiBanThanVaDongDoi / 100);
    }

    public long GetNpoinCap(long npoin) {
        if (!player.isPet && player.pet != null && player.pet.status == Pet.FUSION
                && player.fusion != null
                && (player.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA
                || player.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA2
                || player.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA3)) {
            npoin += npoin * (player.cap * 2L) / 100;
            return npoin;
        }
        return npoin;
    }

    private void setHpMax() {

        // --- SAFE GUARDS (avoid NPE when master/fusion is null) ---
        Pet petLocal = null;
        int typeFusionMaster = ConstPlayer.NON_FUSION;
        if (this.player != null && this.player.isPet) {
            petLocal = (Pet) this.player;
            if (petLocal.master != null && petLocal.master.fusion != null) {
                typeFusionMaster = petLocal.master.fusion.typeFusion;
            }
        }
        boolean isPorataMaster = typeFusionMaster == ConstPlayer.HOP_THE_PORATA
                || typeFusionMaster == ConstPlayer.HOP_THE_PORATA2
                || typeFusionMaster == ConstPlayer.HOP_THE_PORATA3;
        // ----------------------------------------------------------
        // Tính toán giới hạn hpMax
        long hpMax = Util.maxIntValue(this.hpg + this.hpAdd);

        // Áp dụng các yếu tố ảnh hưởng đến hpMax
        for (Integer tl : this.tlHp) {
            hpMax += (hpMax * tl / 100L);
        }
        if (!this.player.isPet) {
            hpMax = GetNpoinCap(hpMax);
        }

        int skhFullSetBonus = this.player.setClothes.getSKHFullSetBonusPercent();
        if (skhFullSetBonus > 0) {
            hpMax += hpMax * skhFullSetBonus / 100L;
        }

        // Xử lý set nappa
        if (this.player.setClothes.nappa == 5) {
            hpMax += hpMax * this.player.setClothes.getSKHLevelEffectPercent() / 100L;
        }
        if (this.player.itemTime != null && this.player.itemTime.nuocMiaSauRieng) {
            hpMax += (hpMax * 10L / 100L);
        }

        if (this.player.setClothes.cadicM >= 2) {
            hpMax += (hpMax * 50L / 100L);
        }
        int realItemCount = 0;
        for (Item it : this.player.inventory.itemsBoxCollection) {
            if (it != null && it.isNotNullItem()) {
                realItemCount++;
            }
        }
        if (realItemCount >= 40) {
            hpMax += hpMax * 7L / 100L;   // +7% HP
        } else if (realItemCount >= 20) {
            hpMax += hpMax * 3L / 100L;   // +3% HP
        }

        if (player != null && player.tienCapLevel > 0) {
            int percent = TienCap.getPercentHpSdKi(player.tienCapLevel);
            hpMax += hpMax * percent / 100L;
        }

        // Xử lý set worldcup
        if (this.player.setClothes.worldcup == 2) {
            hpMax += (hpMax * 10 / 100L);
        }
        if (this.isBatGioi && this.isBoCao) {
            hpMax += (hpMax * 5 / 100L);
            this.tlNeDon += 10;
        }

        // Xử lý rồng xương
        if (player.itemTime != null && player.itemTime.isUseRX) {
            hpMax += (hpMax * 10L / 100L);
        }

        // Xử lý set nhật ấn
        if (this.isNhatAn) {
            hpMax += (hpMax * 15L / 100L);
        }

        // Xử lý ngọc rồng đen 2 sao
        if (this.player.rewardBlackBall.timeOutOfDateReward[1] > System.currentTimeMillis()) {
            hpMax += (hpMax * RewardBlackBall.R2S_1 / 100L);
        }

        // Xử lý khỉ
        if (this.player.effectSkill.isMonkey) {
            if (!this.player.isPet || (this.player.isPet && ((Pet) this.player).status != Pet.FUSION)) {
                int percent = SkillUtil.getPercentHpMonkey(player.effectSkill.levelMonkey);
                hpMax += (hpMax * percent / 100L);
            }
        }
        /// pet fide nhí tăng 20%
        if (this.player.isPet && ((Pet) this.player).typePet == 5
                && isPorataMaster) {
            hpMax += (hpMax * 20 / 100L);
        }
        /// pet cell nhí tăng 35%
        if (this.player.isPet && ((Pet) this.player).typePet == 6
                && isPorataMaster) {
            hpMax += (hpMax * 35 / 100L);
        }
        /// pet bư nhí tăng 50%
        if (this.player.isPet && ((Pet) this.player).typePet == 7
                && isPorataMaster) {
            hpMax += (hpMax * 50 / 100L);
        }
        // Xử lý pet pic
        if (this.player.isPet && ((Pet) this.player).typePet == 3
                && isPorataMaster) {
            hpMax += (hpMax * 10 / 100L);
        }
        // Xử lý pet mabư
        if (this.player.isPet && ((Pet) this.player).typePet == 1
                && isPorataMaster) {
            hpMax += (hpMax * 5 / 100L);
        }
        // Xử lý pet berus
        if (this.player.isPet && ((Pet) this.player).typePet == 2
                && isPorataMaster) {
            hpMax += (hpMax * 10 / 100L);
        }
        // Xử lý pet black
        if (this.player.isPet && ((Pet) this.player).typePet == 4
                && isPorataMaster) {
            hpMax += (hpMax * 10 / 100L);
        }
        if (this.player.pet != null && this.player.fusion != null && this.player.fusion.typeFusion != ConstPlayer.NON_FUSION) {// Zalo:
            // 0395134812//Name:
            // HOAN
            if (this.player.pet.typePet >= 1) {
                hpMax += this.player.pet.nPoint.hpMax * this.player.getPointfusion().getHpFusion() / 100L;
            }
        }
        // Xử lý phù
        if (this.player.zone != null && MapService.gI().isMapBlackBallWar(this.player.zone.map.mapId)) {
            hpMax *= this.player.effectSkin.xHPKI;
        }

        // Xử lý thức ăn 2
        if (this.player.itemTime != null && this.player.itemTime.isEatMeal2 && this.player.itemTime.iconMeal2 == 8062) {
            hpMax += (hpMax * 5 / 100L);
        }
        if (this.player.itemTime != null && this.player.itemTime.isEatMeal2 && this.player.itemTime.iconMeal2 == 8061) {
            hpMax += (hpMax * 10 / 100L);
        }

        // Xử lý thức ăn 3
        if (this.player.itemTime != null && this.player.itemTime.isEatMeal3 && this.player.itemTime.iconMeal3 == 8244) {
            hpMax += (hpMax * 10 / 100L);
        }
        if (this.player.itemTime != null && this.player.itemTime.isUseLoX15) {
            hpMax -= (hpMax * 25 / 100L);
        } else if (this.player.itemTime != null && this.player.itemTime.isUseLoX10) {
            hpMax -= (hpMax * 20 / 100L);
        } else if (this.player.itemTime != null && this.player.itemTime.isUseLoX7) {
            hpMax -= (hpMax * 15 / 100L);
        } else if (this.player.itemTime != null && this.player.itemTime.isUseLoX5) {
            hpMax -= (hpMax * 10 / 100L);
        } else if (this.player.itemTime != null && this.player.itemTime.isUseLoX2) {
            hpMax -= (hpMax * 5 / 100L);
        }

        // Xử lý gogeta
        if (this.isGogeta || this.isKamiOren) {
            hpMax += (hpMax * 10 / 100L);
        }
        if (this.isGogeta || this.isKamiOren) {
            mpMax += (mpMax * 10 / 100L);
        }
        if (this.isGogeta || this.isKamiOren) {
            dame += (dame * 10 / 100L);
        }

        if (player.clan != null) {
            if (player.hpbang > 5000 * player.clan.level) {
                hpbang = 5000 * player.clan.level;
            } else {
                hpbang = player.hpbang;
            }
        }
        hpMax += hpbang;

        // Phù map mabu
        if (this.player.isPhuHoMapMabu) {
            hpMax += 1_000_000;
        }

        // Xử lý +hp đệ
        if (this.player.fusion != null && this.player.fusion.typeFusion != ConstPlayer.NON_FUSION) {
            hpMax += this.player.pet.nPoint.hpMax;
        }

        // Xử lý bổ huyết
        if (this.player.itemTime != null && this.player.itemTime.isUseBoHuyet && !this.player.itemTime.isUseBoHuyet2) {
            hpMax *= 2;
        }

        // Xử lý item sieu cap
        if (this.player.itemTime != null && this.player.itemTime.isUseBoHuyet2) {
            hpMax *= 2.2;
        }
        if (this.player.itemTime != null && this.player.itemTime.isUsePhoNam) {
            hpMax += (hpMax * 8 / 100L);
            this.tlNeDon = 7;
        }

        if (this.player.itemTime != null && this.player.itemTime.isXimuoihoamai) {
            hpMax += (hpMax * 20 / 100L);
        }

        // Xử lý huýt sáo
        if (!this.player.isPet || (this.player.isPet && ((Pet) this.player).status != Pet.FUSION)) {
            if (this.player.effectSkill.tiLeHPHuytSao != 0) {
                hpMax += (hpMax * this.player.effectSkill.tiLeHPHuytSao / 100L);
            }
        }

        // Xử lý chibi
        if (this.player.effectSkill != null && this.player.effectSkill.isChibi && this.player.typeChibi == 3) {
            hpMax *= 2;
        }

        // Xử lý map lạnh
        if (this.player.zone != null && MapService.gI().isMapCold(this.player.zone.map) && !this.isKhongLanh) {
            hpMax /= 2;
        }
        hpMax += hpMax * csbang / 100L;
        this.hpMax = hpMax;

        if (!this.player.isBoss && !this.player.isNewPet
                && TimeUtil.checkTime(EventDAO.getRemainingTimeToIncreaseHP())) {
            hpMax += hpMax / 10;
        }

        // if (hpMax > 2_000_000_000) {
        // hpMax = 2_000_000_000;
        // }
        this.hpMax = hpMax;
    }

    private void setHp() {
        // Giới hạn giá trị hp không vượt quá hpMax
        if (this.hp > this.hpMax) {
            this.hp = this.hpMax;
        }
    }

    private void setMpMax() {

        // --- SAFE GUARDS (avoid NPE when master/fusion is null) ---
        Pet petLocal = null;
        int typeFusionMaster = ConstPlayer.NON_FUSION;
        if (this.player != null && this.player.isPet) {
            petLocal = (Pet) this.player;
            if (petLocal.master != null && petLocal.master.fusion != null) {
                typeFusionMaster = petLocal.master.fusion.typeFusion;
            }
        }
        boolean isPorataMaster = typeFusionMaster == ConstPlayer.HOP_THE_PORATA
                || typeFusionMaster == ConstPlayer.HOP_THE_PORATA2
                || typeFusionMaster == ConstPlayer.HOP_THE_PORATA3;
        // ----------------------------------------------------------
        // Tính toán giới hạn mpMax
        long mpMax = Util.maxIntValue(this.mpg + this.mpAdd);

        // Áp dụng các yếu tố ảnh hưởng đến mpMax
        for (Integer tl : this.tlMp) {
            mpMax += (mpMax * tl / 100L);
        }
        int realItemCount = 0;
        for (Item it : this.player.inventory.itemsBoxCollection) {
            if (it != null && it.isNotNullItem()) {
                realItemCount++;
            }
        }
        if (realItemCount >= 40) {
            mpMax += mpMax * 7L / 100L;  // +7% MP
        } else if (realItemCount >= 20) {
            mpMax += mpMax * 3L / 100L;  // +3% MP
        }
        if (player != null && player.tienCapLevel > 0) {
            int percent = TienCap.getPercentHpSdKi(player.tienCapLevel);
            mpMax += mpMax * percent / 100L;
        }
        int skhFullSetBonus = this.player.setClothes.getSKHFullSetBonusPercent();
        if (skhFullSetBonus > 0) {
            mpMax += mpMax * skhFullSetBonus / 100L;
        }

        // Xử lý set picolo
        if (this.player.setClothes.ocTieu == 5) {
            mpMax += mpMax * this.player.setClothes.getSKHLevelEffectPercent() / 100L;
        }

        if (this.isPicoloTH && this.isKiemZ2) {
            mpMax += (mpMax * 10 / 100L);
        }

        // Xử lý set nguyệt ấn
        if (this.isNguyetAn) {
            mpMax += (mpMax * 15L / 100L);
        }
        if (this.player.itemTime != null && this.player.itemTime.nuocMiaThom) {
            mpMax += (mpMax * 10L / 100L);
        }

        // Xử lý ngọc rồng đen 6 sao
        if (this.player.rewardBlackBall.timeOutOfDateReward[5] > System.currentTimeMillis()) {
            mpMax += (mpMax * RewardBlackBall.R6S_1 / 100L);
        }

        // Xử lý set worldcup
        if (this.player.setClothes.worldcup == 2) {
            mpMax += (mpMax * 10 / 100L);
        }
        /// pet fide nhí tăng 20%
        if (this.player.isPet && ((Pet) this.player).typePet == 5
                && isPorataMaster) {
            mpMax += (mpMax * 20 / 100L);
        }
        /// pet cell nhí tăng 35%
        if (this.player.isPet && ((Pet) this.player).typePet == 6
                && isPorataMaster) {
            mpMax += (mpMax * 35 / 100L);
        }
        /// pet bư nhí tăng 50%
        if (this.player.isPet && ((Pet) this.player).typePet == 7
                && isPorataMaster) {
            mpMax += (mpMax * 50 / 100L);
        }
        // Xử lý pet pic
        if (this.player.isPet && ((Pet) this.player).typePet == 3
                && isPorataMaster) {
            mpMax += (mpMax * 10 / 100L);
        }
        // Xử lý pet mabư
        if (this.player.isPet && ((Pet) this.player).typePet == 1
                && isPorataMaster) {
            mpMax += (mpMax * 5 / 100L);
        }
        // Xử lý pet br
        if (this.player.isPet && ((Pet) this.player).typePet == 2
                && isPorataMaster) {
            mpMax += (mpMax * 10 / 100L);// MP berus
        }
        // Xử lý pet black
        if (this.player.isPet && ((Pet) this.player).typePet == 4
                && isPorataMaster) {
            mpMax += (mpMax * 10 / 100L);// MP black
        }
        if (this.player.pet != null && this.player.fusion != null && this.player.fusion.typeFusion != ConstPlayer.NON_FUSION) {// Zalo:
            // 0395134812//Name:
            // HOAN
            if (this.player.pet.typePet >= 1) {
                mpMax += this.player.pet.nPoint.mpMax * this.player.getPointfusion().getMpFusion() / 100L;
            }
        }
        // Xử lý phù
        if (this.player.zone != null && MapService.gI().isMapBlackBallWar(this.player.zone.map.mapId)) {
            mpMax *= this.player.effectSkin.xHPKI;
        }

        if (player.clan != null) {
            if (player.mpbang > 5000 * player.clan.level) {
                mpbang = 5000 * player.clan.level;
            } else {
                mpbang = player.mpbang;
            }
        }
        mpMax += mpbang;

        // Phù map mabu
        if (this.player.isPhuHoMapMabu) {
            mpMax += 1_000_000;
        }
        if (this.player.itemTime != null && this.player.itemTime.isUseLoX15) {
            mpMax -= (mpMax * 25 / 100L);
        } else if (this.player.itemTime != null && this.player.itemTime.isUseLoX10) {
            mpMax -= (mpMax * 20 / 100L);
        } else if (this.player.itemTime != null && this.player.itemTime.isUseLoX7) {
            mpMax -= (mpMax * 15 / 100L);
        } else if (this.player.itemTime != null && this.player.itemTime.isUseLoX5) {
            mpMax -= (mpMax * 10 / 100L);
        } else if (this.player.itemTime != null && this.player.itemTime.isUseLoX2) {
            mpMax -= (mpMax * 5 / 100L);
        }
        // Xử lý rồng xương
        if (player.itemTime != null && player.itemTime.isUseRX) {
            mpMax += (mpMax * 10L / 100L);
        }

        // Xử lý hợp thể
        if (this.player.fusion != null && this.player.fusion.typeFusion != 0) {
            mpMax += this.player.pet.nPoint.mpMax;
        }

        // Xử lý bổ khí
        if (this.player.itemTime != null && this.player.itemTime.isUseBoKhi && !this.player.itemTime.isUseBoKhi2) {
            mpMax *= 2;
        }
        if (this.player.itemTime != null && this.player.itemTime.isUsePhoTaiBo) {
            mpMax += (mpMax * 10 / 100L);
        }

        // Xử lý item sieu cap
        if (this.player.itemTime != null && this.player.itemTime.isUseBoKhi2) {
            mpMax *= 2.2;
        }
        if (this.player.itemTime != null && this.player.itemTime.isXimuoihoamai) {
            mpMax += (mpMax * 20 / 100L);
        }
        if (this.player.itemTime != null && this.player.itemTime.isEatMeal2 && this.player.itemTime.iconMeal2 == 8061) {
            mpMax += (mpMax * 10 / 100L);
        }

        if (!this.player.isBoss && !this.player.isNewPet
                && TimeUtil.checkTime(EventDAO.getRemainingTimeToIncreaseMP())) {
            mpMax += mpMax / 10;
        }
        if (!this.player.isPet) {
            mpMax = GetNpoinCap(mpMax);
        }

        // if (mpMax > 2_000_000_000) {
        // mpMax = 2_000_000_000;
        // }
        this.mpMax = mpMax;

        mpMax += mpMax * csbang / 100L;
        this.mpMax = mpMax;
    }

    private void setMp() {
        if (this.mp > this.mpMax) {
            this.mp = this.mpMax;
        }
    }

    public long getHP() {
        return this.hp <= this.hpMax ? this.hp : this.hpMax;
    }

    public void setHP(long hp) {
        if (hp > 0) {
            this.hp = (hp <= this.hpMax ? hp : this.hpMax);
        } else {
            player.setDie();
        }
    }

    public long getMP() {
        return this.mp <= this.mpMax ? this.mp : this.mpMax;
    }

    public void setMP(long mp) {
        if (mp > 0) {
            this.mp = (mp <= this.mpMax ? mp : this.mpMax);
        } else {
            this.mp = 0;
        }
    }

    private void setDame() {

        // --- SAFE GUARDS (avoid NPE when master/fusion is null) ---
        Pet petLocal = null;
        int typeFusionMaster = ConstPlayer.NON_FUSION;
        if (this.player != null && this.player.isPet) {
            petLocal = (Pet) this.player;
            if (petLocal.master != null && petLocal.master.fusion != null) {
                typeFusionMaster = petLocal.master.fusion.typeFusion;
            }
        }
        boolean isPorataMaster = typeFusionMaster == ConstPlayer.HOP_THE_PORATA
                || typeFusionMaster == ConstPlayer.HOP_THE_PORATA2
                || typeFusionMaster == ConstPlayer.HOP_THE_PORATA3;
        // ----------------------------------------------------------
        // Tính toán giới hạn dame
        long dame = Util.maxIntValue(this.dameg + this.dameAdd);

        // Áp dụng các yếu tố ảnh hưởng đến dame
        for (Integer tl : this.tlDame) {
            dame += (dame * tl / 100L);
        }
        if (!this.player.isPet) {
            dame = GetNpoinCap(dame);
        }
        if (this.player.itemTime != null && this.player.itemTime.nuocMiaKhongLo) {
            dame += (dame * 10L / 100L);
            this.tlSDCM = 7;
        }
        if (this.isPhongHoaLuan && this.isHoaTiemThuong) {
            dame += (dame * 5L / 100L);
            // this.tlSDCM = 7;
        }

        int realItemCount = 0;
        for (Item it : this.player.inventory.itemsBoxCollection) {
            if (it != null && it.isNotNullItem()) {
                realItemCount++;
            }
        }
        if (realItemCount >= 40) {
            dame += dame * 3L / 100L;
        } else if (realItemCount >= 20) {
            dame += dame * 1L / 100L;
        }

        if (player != null && player.tienCapLevel > 0) {
            int percent = TienCap.getPercentHpSdKi(player.tienCapLevel);
            dame += dame * percent / 100L;
        }
        // for (Integer tl : this.tlSDDep) {
        // dame += (dame * tl / 100L);
        // }
        /// pet fide nhí tăng 20%
        if (this.player.isPet && ((Pet) this.player).typePet == 5
                && isPorataMaster) {
            dame += (dame * 20 / 100L);
        }
        /// pet cell nhí tăng 35%
        if (this.player.isPet && ((Pet) this.player).typePet == 6
                && isPorataMaster) {
            dame += (dame * 35 / 100L);
        }
        /// pet bư nhí tăng 50%
        if (this.player.isPet && ((Pet) this.player).typePet == 7
                && isPorataMaster) {
            dame += (dame * 50 / 100L);
        }
        // Xử lý pet pic
        if (this.player.isPet && ((Pet) this.player).typePet == 3
                && isPorataMaster) {
            dame += (dame * 10 / 100L);
        }
        // Xử lý pet mabư
        if (this.player.isPet && ((Pet) this.player).typePet == 1
                && isPorataMaster) {
            dame += (dame * 5 / 100L);
        }
        // Xử lý pet br
        if (this.player.isPet && ((Pet) this.player).typePet == 2
                && isPorataMaster) {
            dame += (dame * 10 / 100L);
        }
        // Xử lý pet black
        if (this.player.isPet && ((Pet) this.player).typePet == 4
                && isPorataMaster) {
            dame += (dame * 10 / 100L);
        }
        if (this.player.pet != null && this.player.fusion != null && this.player.fusion.typeFusion != ConstPlayer.NON_FUSION) {// Zalo:
            // 0395134812//Name:
            // HOAN
            if (this.player.pet.typePet >= 1) {
                dame += this.player.pet.nPoint.dame * this.player.getPointfusion().getDameFusion() / 100L;
            }
        }
        // Xử lý set tinh ấn
        if (this.isTinhAn) {
            dame += (dame * 15L / 100L);
        }

        // Xử lý thức ăn
        if (!this.player.isPet && this.player.itemTime != null && this.player.itemTime.isEatMeal
                || this.player.isPet && this.player.itemTime != null && ((Pet) this.player).master.itemTime != null && ((Pet) this.player).master.itemTime.isEatMeal) {
            dame += (dame * 10 / 100L);
        }
        if (this.player.itemTime != null && this.player.itemTime.isUseLoX15) {
            dame -= (dame * 25 / 100L);
        } else if (this.player.itemTime != null && this.player.itemTime.isUseLoX10) {
            dame -= (dame * 20 / 100L);
        } else if (this.player.itemTime != null && this.player.itemTime.isUseLoX7) {
            dame -= (dame * 15 / 100L);
        } else if (this.player.itemTime != null && this.player.itemTime.isUseLoX5) {
            dame -= (dame * 10 / 100L);
        } else if (this.player.itemTime != null && this.player.itemTime.isUseLoX2) {
            dame -= (dame * 5 / 100L);
        }
        // Xử lý thức ăn 2
        if (this.player.itemTime != null && this.player.itemTime.isEatMeal2 && this.player.itemTime.iconMeal2 == 8060) {
            dame += (dame * 5 / 100L);
        }

        // Xử lý thức ăn 2
//        if (this.player.itemTime != null && this.player.itemTime.isEatMeal2 && this.player.itemTime.iconMeal2 == 8061) {
//            this.tlDameCrit.add(5);
//            this.tlSDCM += 5;
//        }
        // Xử lý cuồng nộ
        if (this.player.itemTime != null && this.player.itemTime.isUseCuongNo && !this.player.itemTime.isUseCuongNo2) {
            dame *= 2;
        }
        if (this.player.itemTime != null && this.player.itemTime.isXimuoihoadao) {
            dame += (dame * 20 / 100L);
        }
        if (this.player.itemTime != null && this.player.itemTime.isBanhtet) {
            dame += (dame * 15 / 100L);
        }
        if (this.player.itemTime != null && this.player.itemTime.isBanhchung) {
            dame += (dame * 25 / 100L);
        }
        if (this.player.itemTime != null && this.player.itemTime.isUsePhoTaiHanh) {
            dame += (dame * 5 / 100L);
        }
//        if (this.player.itemTime != null && this.player.itemTime.isXimuoihoamai) {
//            dame += (dame * 20 / 100L);
//        }
        if (this.player.itemTime != null && this.player.itemTime.isUseCuongNo2) {
            dame *= 2.2;
        }
        if (this.player.itemTime != null && this.player.itemTime.isUseKhauTrang) {
            this.tlDameAttMob.add(10);
        }

        if (this.player.itemTime != null && this.player.itemTime.isEatMeal3 && this.player.itemTime.iconMeal3 == 8247) {
            dame += (dame * 10 / 100L);
        }

        // Xử lý ngọc rồng đen 1 sao
        if (this.player.rewardBlackBall.timeOutOfDateReward[0] > System.currentTimeMillis()) {
            dame += (dame * RewardBlackBall.R1S_2 / 100L);
        }

        // Xử lý set worldcup
        if (this.player.setClothes.worldcup == 2) {
            dame += (dame * 10 / 100L);
        }
        int skhFullSetBonus = this.player.setClothes.getSKHFullSetBonusPercent();
        if (skhFullSetBonus > 0) {
            dame += dame * skhFullSetBonus / 100L;
        }
        // Xử lý set nail
        if (this.player.setClothes.nail >= 2) {
            this.tlSDCM += 50;
        }

        if (player.clan != null) {
            if (player.damebang > 200 * player.clan.level) {
                damebang = 200 * player.clan.level;
            } else {
                damebang = player.damebang;
            }
        }
        dame += damebang;

        // Phù map mabu
        if (this.player.isPhuHoMapMabu) {
            dame += 10_000;
        }

        // Xử lý rồng xương
        if (player.itemTime != null && player.itemTime.isUseRX) {
            dame += (dame * 10L / 100L);
        }

        // Xử lý phù
        if (this.player.zone != null && MapService.gI().isMapBlackBallWar(this.player.zone.map.mapId)) {
            dame *= this.player.effectSkin.xDame;
        }

        // Xử lý hợp thể
        if (this.player.fusion != null && this.player.fusion.typeFusion != 0) {
            if (this.player.pet != null && this.player.pet.nPoint != null) {
                dame += this.player.pet.nPoint.dame;
            }
        }

        // Xử lý khỉ
        if (this.player.effectSkill.isMonkey) {
            if (!this.player.isPet || (this.player.isPet && ((Pet) this.player).status != Pet.FUSION)) {
                int percent = SkillUtil.getPercentDameMonkey(player.effectSkill.levelMonkey);
                dame += (dame * percent / 100L);
            }
        }

        // Sức đánh đẹp
        dame += (dame * tlSexyDame / 100L);

        // Xử lý giảm dame
        dame -= (dame * tlSubSD / 100L);

        // Xử lý map cold
        if (this.player.zone != null && MapService.gI().isMapCold(this.player.zone.map) && !this.isKhongLanh) {
            dame /= 2;
        }
        dame += dame * csbang / 100L;

        if (!this.player.isBoss && !this.player.isNewPet
                && TimeUtil.checkTime(EventDAO.getRemainingTimeToIncreaseDame())) {
            dame += dame / 10;
        }

        // if (dame > 2_000_000_000) {
        // dame = 2_000_000_000;
        // }
        this.dame = dame;
    }

    private void setCaiTrang() {
        if (this.player.inventory.itemsBody.size() < 6) {
            return;
        }
        Item skin = this.player.inventory.itemsBody.get(5);

        // Đặt tất cả biến về false trước
        this.isBatGioi = false;
        this.isPicoloTH = false;
        // Nếu item hợp lệ, chỉ bật biến tương ứng
        if (skin.isNotNullItem()) {
            int id = skin.template.id;

            switch (id) {
                case 1698 ->
                    this.isBatGioi = true;
                case 1938 ->
                    this.isPicoloTH = true;
            }
        }
    }

    private void setDeoLung() {
        if (this.player.inventory.itemsBody.size() < 9) {
            return;
        }
        Item skin = this.player.inventory.itemsBody.get(8);

        // Đặt tất cả biến về false trước
//        this.isSamehada = false;
//        this.isEnma = false;
        this.isBanthan = false;
        this.isBoCao = false;
        this.isHoaTiemThuong = false;
        this.isKiemZ2 = false;
//        this.isthuykiem = false;
//        this.ishuyetkiem = false;
//        this.ishoakiem = false;
//        this.isKimcobong = false;
//        this.isThienlongdao = false;
//        this.isLabubu = false;
//        this.isSarigan = false;

        // Nếu item hợp lệ, chỉ bật biến tương ứng
        if (skin.isNotNullItem()) {
            switch (skin.template.id) {
//                case 1809 ->
//                    this.isSamehada = true;
//                case 1456 ->
//                    this.isEnma = true;
                case 1809 ->
                    this.isBanthan = true;
                case 1894 ->
                    this.isKiemZ2 = true;
                case 1699 ->
                    this.isBoCao = true;
                case 1680 ->
                    this.isHoaTiemThuong = true;
//                case 1850 ->
//                    this.isthuykiem = true;
//                case 1851 ->
//                    this.ishuyetkiem = true;
//                case 1852 ->
//                    this.ishoakiem = true;
//                case 1847 ->
//                    this.isKimcobong = true;
//                case 1802 ->
//                    this.isThienlongdao = true;
//                case 1694 ->
//                    this.isLabubu = true;
//                case 1813 ->
//                    this.isSarigan = true;
            }
        }
    }

    private void setPet() {
        if (this.player.inventory.itemsBody.size() < 8) {
            return;
        }
        Item skin = this.player.inventory.itemsBody.get(7);

        // Đặt tất cả biến về false trước
        this.isRong7sao = false;

        // Nếu item hợp lệ, chỉ bật biến tương ứng
        if (skin.isNotNullItem()) {
            switch (skin.template.id) {
                case 1881 ->
                    this.isRong7sao = true;
            }
        }
    }

    private void setThuCuoi() {
        if (this.player.inventory.itemsBody.size() < 10) {
            return;
        }
        Item skin = this.player.inventory.itemsBody.get(9);

        // Đặt tất cả biến về false trước
        this.isVanTeNuoc = false;
        this.isPhongHoaLuan = false;
        // Nếu item hợp lệ, chỉ bật biến tương ứng
        if (skin.isNotNullItem()) {
            switch (skin.template.id) {
                case 1563 ->
                    this.isVanTeNuoc = true;
                case 1676 ->
                    this.isPhongHoaLuan = true;
            }
        }
    }

    private void setDef() {
        this.def = this.defg * 4;
        this.def += this.defAdd;
        // Xử lý thức ăn 3
        if (this.player.itemTime != null && this.player.itemTime.isEatMeal3 && this.player.itemTime.iconMeal3 == 8246) {
            this.def += (this.def * 10 / 100L);
        }
        if (this.player.itemTime != null && this.player.itemTime.nuocMiaSauRieng) {
            this.def += 10;
            //  this.def += (this.def * 10 / 100L);
        }
    }

    private void setCrit() {
        this.crit = this.critg;
        this.crit += this.critAdd;
        if (this.crit > 110) {
            this.crit = 110;
        }
        // biến khỉ
        if (this.player.effectSkill.isMonkey) {
            this.crit = 110;
        }
        if (player.setClothes.thanVuTruKaio >= 2) {
            this.crit += 10;
        }
        if (this.player.itemTime != null && this.player.itemTime.nuocMiaThom) {
            this.crit += 10;
        }
        if (this.player.itemTime != null && this.player.itemTime.isUsePhoTaiHanh) {
            this.crit += 2;
        }
        if (player != null && player.tienCapLevel > 0) {
            crit += TienCap.getCritBonus(player.tienCapLevel);
        }
        if (player.clan != null) {
            if (player.critbang > player.clan.level) {
                critbang = player.clan.level;
            } else {
                critbang = player.critbang;
            }
        }
        crit += critbang;
        // Xử lý thức ăn 3
        if (this.player.itemTime != null && this.player.itemTime.isEatMeal3 && this.player.itemTime.iconMeal3 == 8244) {
            this.crit = this.crit + 5;
        }
        if (this.player.itemTime != null && this.player.itemTime.isBanhtet) {
            this.crit = this.crit + 15;
        }
        if (this.player.itemTime != null && this.player.itemTime.isBanhchung) {
            this.crit = this.crit + 25;
        }
        if (this.isPhongHoaLuan && this.isHoaTiemThuong) {
            this.crit = this.crit + 10;
        }
        if (this.isKiemZ2 && this.isPicoloTH) {
            this.crit = this.crit + 5;
        }
    }

    private void resetPoint() {
        this.voHieuChuong = 0;
        this.hpAdd = 0;
        this.mpAdd = 0;
        this.dameAdd = 0;
        this.defAdd = 0;
        this.critAdd = 0;
        this.tlHp.clear();
        this.tlMp.clear();
        this.tlDef = 0;
        this.tlDame.clear();
        this.tlDameCrit.clear();
        this.tlDameAttMob.clear();
        this.tlSDCM = 0;
        this.tlHpHoiBanThanVaDongDoi = 0;
        this.tlMpHoiBanThanVaDongDoi = 0;
        this.hpHoi = 0;
        this.mpHoi = 0;
        this.mpHoiCute = 0;
        this.tlHpHoi = 0;
        this.tlMpHoi = 0;
        this.tlHutHp = 0;
        this.tlHutMp = 0;
        this.tlHutHpMob = 0;
        this.tlHutHpMpXQ = 0;
        this.tlPST = 0;
        this.tlTNSM.clear();
        this.tlDameAttMob.clear();
        this.tlGold = 0;
        this.tlNeDon = 0;
        this.tlBom = 0;
        this.tlGiap = 0;
        this.tlxgcc = 0;
        this.tlxgc = 0;
        this.tlchinhxac = 0;
        this.tlTNSMPet = 0;
        this.xChuong = 0;
        this.setltdb = 0;
        this.setTinhAn = 0;
        this.setNhatAn = 0;
        this.setNguyetAn = 0;
        this.tlSexyDame = 0;
        this.tlSubSD = 0;
        this.tlHpGiamODo = 0;
        this.tlSpeed = 0;
        this.teleport = false;

        this.hpbang = 0;
        this.mpbang = 0;
        this.damebang = 0;
        this.critbang = 0;
        this.csbang = 0;

        this.wearingVoHinh = false;
        this.isKhongLanh = false;
        this.khangTDHS = false;
        this.isTanHinh = false;
        this.isHoaDa = false;
        this.isLamCham = false;
        this.isDoSPL = false;
        this.isThoBulma = false;
        this.isDietQuy = false;
        this.isBunmaTocMau = false;
        this.isTiecBaiBien = false;
    }

    public void addHp(long hp) {
        if (hp > 0) {
            long potentialHp = this.hp + hp;
            if (potentialHp > this.hpMax) {
                this.hp = this.hpMax;
            } else {
                this.hp = potentialHp;
            }
        }
    }

    public void addMp(long mp) {
        long potentialMp = this.mp + mp;

        if (potentialMp > this.mpMax) {
            this.mp = this.mpMax;
        } else if (potentialMp < 0) {
            this.mp = 0;
        } else {
            this.mp = potentialMp;
        }
    }

    public void setHp(long hp) {
        if (hp < 0) {
            this.hp = 0;
        } else {
            this.hp = hp;
        }
    }

    public void setMp(long mp) {
        // if (mp > this.mpMax) {
        // this.mp = this.mpMax;
        // } else
        if (mp < 0) {
            this.mp = 0;
        } else {
            this.mp = mp;
        }
    }

    private void setIsCrit() {
        if (intrinsic != null && intrinsic.id == 25 && this.getCurrPercentHP() <= intrinsic.param1) {
            isCrit = true;
        } else if (isCrit100) {
            isCrit100 = false;
            isCrit = true;
        } else {
            isCrit = Util.isTrue(this.crit, ConstRatio.PER100);
        }
    }

    public long getDameAttack(boolean isAttackMob) {
        setCaiTrang();

        setThuCuoi();
        setIsCrit();

        setPet();

        //  setLinhThu();
        setDeoLung();
        long dameAttack = this.dame;
        intrinsic = this.player.playerIntrinsic.intrinsic;
        percentDameIntrinsic = 0;
        int percentDameSkill = 0;
        int percentXDame = 0;
        Skill skillSelect = player.playerSkill.skillSelect;
        if (skillSelect.template.id != Skill.DICH_CHUYEN_TUC_THOI && isCritTele) {
            isCrit = true;
            isCritTele = false;
        }
        switch (skillSelect.template.id) {
            case Skill.DRAGON:
                if (intrinsic.id == 1) {
                    percentDameIntrinsic = intrinsic.param1;
                }
                percentDameSkill = skillSelect.damage;
                break;
            case Skill.KAMEJOKO:
                if (intrinsic.id == 2) {
                    percentDameIntrinsic = intrinsic.param1;
                }
                percentDameSkill = skillSelect.damage;
                if (this.player.setClothes.songoku == 5) {
                    percentXDame = this.player.setClothes.getSKHLevelEffectPercent();
                }
                break;
            case Skill.GALICK:
                if (intrinsic.id == 16) {
                    percentDameIntrinsic = intrinsic.param1;
                }
                percentDameSkill = skillSelect.damage;
                if (this.player.setClothes.kakarot == 5) {
                    percentXDame = this.player.setClothes.getSKHLevelEffectPercent();
                }
                break;
            case Skill.ANTOMIC:
                if (intrinsic.id == 17) {
                    percentDameIntrinsic = intrinsic.param1;
                }
                percentDameSkill = skillSelect.damage;
                break;
            case Skill.DEMON:
                if (intrinsic.id == 8) {
                    percentDameIntrinsic = intrinsic.param1;
                }
                percentDameSkill = skillSelect.damage;
                break;
            case Skill.MASENKO:
                if (intrinsic.id == 9) {
                    percentDameIntrinsic = intrinsic.param1;
                }
                if (this.player.setClothes.nail == 5) {
                    percentXDame = 50;
                }
                percentDameSkill = skillSelect.damage;
                break;
            case Skill.LIEN_HOAN:
                if (intrinsic.id == 13) {
                    percentDameIntrinsic = intrinsic.param1;
                }
                percentDameSkill = skillSelect.damage;
                if (this.player.setClothes.lienHoan == 5) {
                    percentXDame = this.player.setClothes.getSKHLevelEffectPercent();
                }
                break;
            case Skill.KAIOKEN:
                percentDameSkill = skillSelect.damage;
                if (this.player.setClothes.kaioken == 5) {
                    percentXDame = this.player.setClothes.getSKHLevelEffectPercent();
                } else if (player.setClothes.thanVuTruKaio == 5) {
                    percentXDame = 80;
                }
                break;
            case Skill.TU_SAT:
                percentDameSkill = skillSelect.damage;
                if (this.player.setClothes.cadicM == 4) {
                    percentXDame = 20;
                } else if (this.player.setClothes.cadicM == 5) {
                    percentXDame = 40;
                }
                break;
            case Skill.DICH_CHUYEN_TUC_THOI:
                isCrit = true;
                isCritTele = true;
                dameAttack = Util.nextLong(Util.maxIntValue((dameAttack - (dameAttack / 100 * 5))),
                        Util.maxIntValue((dameAttack + (dameAttack / 100 * 5))));
                break;
            case Skill.MAKANKOSAPPO:
                percentDameSkill = skillSelect.damage;
                long dameSkill = Util.maxIntValue((long) this.mpMax * percentDameSkill / 100);
                if (this.player.setClothes.picolo == 5) {
                    dameSkill += dameSkill * this.player.setClothes.getSKHLevelEffectPercent() / 100;
                }
                return dameSkill;
            case Skill.QUA_CAU_KENH_KHI:
                long hpmob = 0;
                long hppl = 0;

                for (Mob mob : this.player.zone.mobs) {
                    if (!mob.isDie() && Util.getDistance(this.player, mob) <= SkillUtil
                            .getRangeQCKK(this.player.playerSkill.skillSelect.point)) {
                        hpmob += mob.point.hp;
                    }
                }

                for (Player pl : this.player.zone.getHumanoids()) {
                    if (!pl.isDie() && this.player.id != pl.id && Util.getDistance(this.player, pl) <= SkillUtil
                            .getRangeQCKK(this.player.playerSkill.skillSelect.point)) {
                        hppl += pl.nPoint.hp;
                    }
                }

                long dameqckk = (hpmob * 10 / 100) + (hppl * 10 / 100) + this.dame * 10;

                if (this.player.setClothes.kirin == 5) {
                    dameqckk += dameqckk * this.player.setClothes.getSKHLevelEffectPercent() / 100;
                }

                dameqckk = dameqckk + (Util.nextInt(-5, 5) * dameqckk / 100);
                // if (dameqckk > 2_000_000_000) {
                // dameqckk = 2_000_000_000;
                // }
                return dameqckk;
            case Skill.DE_TRUNG:
                if (player.setClothes.pikkoroDaimao == 5) {
                    dameAttack += dameAttack * this.player.setClothes.getSKHLevelEffectPercent() / 100;
                }
                // if (dameAttack > 2_000_000_000) {
                // dameAttack = 2_000_000_000;
                // }
                return dameAttack;
        }

        if (intrinsic.id == 18 && this.player.effectSkill.isMonkey) {
            percentDameIntrinsic = intrinsic.param1;
        }

        if (percentDameSkill != 0) {
            dameAttack = dameAttack * percentDameSkill / 100;
        }

        dameAttack += (dameAttack * percentDameIntrinsic / 100);
        dameAttack += (dameAttack * dameAfter / 100);
        if (this.player.effectSkill != null && this.player.effectSkill.isDameBuff && tlSexyDame == 0) {
            int tiLeDame = this.player.effectSkill.tileDameBuff;
            dameAttack += (dameAttack * tiLeDame / 100L);
        }
        if (isAttackMob) {
            for (Integer tl : this.tlDameAttMob) {
                dameAttack += (dameAttack * tl / 100);
            }
            if (this.player.isPet && ((Pet) this.player).master.charms.tdDeTu > System.currentTimeMillis()) {
                dameAttack *= 2;
            }
        }

        dameAfter = 0;

        if (isCrit) {
            dameAttack *= 2;
            dameAttack += (dameAttack * tlSDCM / 100);
        }

        dameAttack += ((long) dameAttack * percentXDame / 100);

        long tempDameAttack = (long) (dameAttack / 100L * 5L);
        if (tempDameAttack <= 0) {
            tempDameAttack = 1;
        }
        dameAttack += (long) (Util.getOne(-1, 1) * Util.nextLong(tempDameAttack) + 1);

        if (player.effectSkin != null && player.effectSkin.isXChuong
                && (player.playerSkill.skillSelect.template.id == Skill.KAMEJOKO
                || player.playerSkill.skillSelect.template.id == Skill.ANTOMIC
                || player.playerSkill.skillSelect.template.id == Skill.MASENKO)) {
            dameAttack *= xChuong;
            player.effectSkin.isXDame = true;
            player.effectSkin.isXChuong = false;
            player.effectSkin.lastTimeXChuong = System.currentTimeMillis();
        }

        // if (dameAttack > 2_000_000_000) {
        // dameAttack = 2_000_000_000;
        // }
        return dameAttack;
    }

    public int getCurrPercentHP() {
        if (this.hpMax == 0) {
            return 100;
        }
        return (int) ((long) this.hp * 100 / this.hpMax);
    }

    public int getCurrPercentMP() {
        return (int) ((long) this.mp * 100 / this.mpMax);
    }

    public void setFullHpMp() {
        this.hp = this.hpMax;
        this.mp = this.mpMax;
    }

    public void subHP(long sub) {
        this.hp -= sub;
        if (this.hp <= 0) {
            this.hp = 0;
            this.setHp(0);
        }
    }

    public void subMP(long sub) {
        this.mp -= sub;
        if (this.mp <= 0) {
            this.mp = 0;
        }

        // if (this.mp > 2_000_000_000) {
        // this.mp = 2_000_000_000;
        // }
    }

    public long calSucManhTiemNang(long tiemNang) {
        if (player.zone == null || player.zone.map == null) {
            return 0; // hoặc giá trị mặc định nếu muốn
        }
        if (player.zone.map.type == 3) {
            return 0;
        }
        if (power < getPowerLimit()) {
            for (Integer tl : this.tlTNSM) {
                tiemNang += ((long) tiemNang * tl / 100);
            }
            if (this.player.cFlag != 0) {
                if (this.player.cFlag == 8) {
                    tiemNang += ((long) tiemNang * 10 / 100);
                } else {
                    tiemNang += ((long) tiemNang * 5 / 100);
                }
            }
            long tn = tiemNang;
            if (this.player.charms.tdTriTue > System.currentTimeMillis()) {
                tiemNang += tn;
            }
            if (this.player.charms.tdTriTue3 > System.currentTimeMillis()) {
                tiemNang += tn * 2;
            }
            if (this.player.charms.tdTriTue4 > System.currentTimeMillis()) {
                tiemNang += tn * 3;
            }
//            if (this.player.charms.tdTriTue4 > System.currentTimeMillis()) {
//                tiemNang += tn * 3;
//            }
            if (this.player.effectSkill.isChibi && this.player.typeChibi == 2) {
                tiemNang += tn * 2;
            }
            if (this.player.setClothes.hdpe == 5) {
                tiemNang += (tn * 100L / 100L);
            }
            if (this.player.getSession() != null && this.player.getSession().vip > 0
                    || this.player.isPet && ((Pet) this.player).master.getSession() != null
                    && ((Pet) this.player).master.getSession().vip > 0) {
                tiemNang += tn * 3;
            }
            if (this.player.itemTime != null && this.player.itemTime.isUseDK) {
                tiemNang += tn * 2;
            }
            if (this.player.itemTime != null && this.player.itemTime.isUseKhauTrang) {
                tiemNang += tn * 5 / 100;
            }
            if (this.player.itemTime != null && this.player.itemTime.isUseLoX2) {
                tiemNang += tn * 2;
            }
            if (this.player.itemTime != null && this.player.itemTime.isUseLoX5) {
                tiemNang += tn * 5;
            }
            if (this.player.itemTime != null && this.player.itemTime.isUseLoX7) {
                tiemNang += tn * 7;
            }
            if (this.player.itemTime != null && this.player.itemTime.isUseLoX10) {
                tiemNang += tn * 10;
            }
            if (this.player.itemTime != null && this.player.itemTime.isUseLoX15) {
                tiemNang += tn * 15;
            }
            if (this.player.satellite != null && this.player.satellite.isIntelligent) {
                tiemNang += tn / 5;
            }
            if (this.intrinsic != null && this.intrinsic.id == 24) {
                tiemNang += ((long) tiemNang * this.intrinsic.param1 / 100);
            }
            if (this.power >= 60000000000L) {
                tiemNang -= ((long) tiemNang * 80 / 100);
            }
            if (this.player.isPet) {
                if (((Pet) this.player).master.charms.tdDeTu > System.currentTimeMillis()) {
                    tiemNang += tn * 2;
                }
                if (((Pet) this.player).itemTime.lastTimeBuax2DeTu > System.currentTimeMillis()) {
                    tiemNang += tn * 2;
                }
                if (((Pet) this.player).master.nPoint != null && ((Pet) this.player).master.nPoint.tlTNSMPet > 0) {
                    tiemNang += tn / 100 * (((Pet) this.player).master.nPoint.tlTNSMPet + 100);
                }
            }

            if (TimeUtil.checkTime(EventDAO.getRemainingTimeToIncreasePotentialAndPower())) {
                tiemNang *= 2;
            }
            if (MapService.gI().isMapNguHanhSon(this.player.zone.map.mapId)) {
                tiemNang *= 1.5f; // tnsm nhs
            }
            if (MapService.gI().isMapBanDoKhoBau(this.player.zone.map.mapId)) {
                tiemNang *= 2;
            }
            tiemNang *= Manager.RATE_EXP_SERVER;
            tiemNang = calSubTNSM(tiemNang);
            if (tiemNang <= 0) {
                tiemNang = 1;
            }
        } else {
            tiemNang = 0;
        }
        return tiemNang;
    }

    public long calSubTNSM(long tiemNang) {

        tiemNang = (long) (tiemNang * 0.60);

        long power = player.nPoint.power;

        if (power < 1_500_000L) {
        } else if (power < 150_000_000L) {
            tiemNang = tiemNang * 90 / 100;
        } else if (power < 1_000_000_000L) {
            tiemNang = tiemNang * 80 / 100;
        } else if (power < 10_000_000_000L) {
            tiemNang = tiemNang * 65 / 100;
        } else if (power < 20_000_000_000L) {
            tiemNang = tiemNang * 50 / 100;
        } else if (power < 40_000_000_000L) {
            tiemNang = tiemNang * 35 / 100;
        } else if (power < 60_000_000_000L) {
            tiemNang = tiemNang / 35;
        } else if (power < 80_000_000_000L) {
            tiemNang = tiemNang / 45;
        } else if (power < 100_000_000_000L) {
            tiemNang = tiemNang / 50;
        } else if (power < 110_000_000_000L) {
            tiemNang = tiemNang / 60;
        } else if (power < 180_000_000_000L) {
            tiemNang = tiemNang / 70;
        } else {
            tiemNang = tiemNang / 200;
        }

        return tiemNang;
    }

    public short getTileHutHp(boolean isMob) {
        if (isMob) {
            return (short) (this.tlHutHp + this.tlHutHpMob);
        } else {
            return this.tlHutHp;
        }
    }

    public short getTiLeHutMp() {
        return this.tlHutMp;
    }

    public long subDameInjureWithDeff(long dame) {
        long def = this.def;
        // Vì tlDef là short => dùng trực tiếp
        long defPercentage = this.tlDef;

        // Giới hạn giống gốc: tối đa 85%
        if (defPercentage > 85) {
            defPercentage = 85;
        }

        dame -= def;
        if (dame < 0) {
            dame = 1;
        }
        return dame;
    }

    /*------------------------------------------------------------------------*/
    public boolean canOpenPower() {
        return this.power >= getPowerLimit();
    }

    public long getPowerLimit() {
        if (powerLimit != null) {
            return powerLimit.getPower();
        }
        return 0;
    }

    public long getPowerNextLimit() {
        PowerLimit powerLimit = PowerLimitManager.getInstance().get(limitPower + 1);
        if (powerLimit != null) {
            return powerLimit.getPower();
        }
        return 0;
    }

    // **************************************************************************
    // POWER - TIEM NANG
    public void powerUp(long power) {
        this.power += power;
        TaskService.gI().checkDoneTaskPower(player, this.power);
    }

    public void tiemNangUp(long tiemNang) {
        this.tiemNang += tiemNang;
    }

    public void increasePoint(byte type, short point, boolean manualForPet) {
        if (powerLimit == null) {
            return;
        }
        if (point <= 0) {
            return;
        }
        boolean updatePoint = false;
        long tiemNangUse = 0;
        if (type == 0) {
            int pointHp = point * 20;
            tiemNangUse = point * (2 * (this.hpg + 1000) + pointHp - 20) / 2;
            if ((this.hpg + pointHp) <= powerLimit.getHp()) {
                if (doUseTiemNang(tiemNangUse)) {
                    hpg += pointHp;
                    updatePoint = true;
                }
            } else {
                Service.gI().sendThongBao(player, "HP của bạn đã đạt mức tối đa");
                Service.gI().sendMoney(player);
                return;
            }
        }
        if (type == 1) {
            int pointMp = point * 20;
            tiemNangUse = point * (2 * (this.mpg + 1000) + pointMp - 20) / 2;
            if ((this.mpg + pointMp) <= powerLimit.getMp()) {
                if (doUseTiemNang(tiemNangUse)) {
                    mpg += pointMp;
                    updatePoint = true;
                }
            } else {
                Service.gI().sendThongBao(player, "KI của bạn đã đạt mức tối đa");
                Service.gI().sendMoney(player);
                return;
            }
        }
        if (type == 2) {
            tiemNangUse = point * (2 * this.dameg + point - 1) / 2 * 100;
            if ((this.dameg + point) <= powerLimit.getDamage()) {
                if (doUseTiemNang(tiemNangUse)) {
                    dameg += point;
                    updatePoint = true;
                }
            } else {
                Service.gI().sendThongBao(player, "Sức đánh của bạn đã đạt mức tối đa");
                Service.gI().sendMoney(player);
                return;
            }
        }
        if (type == 3) {
            tiemNangUse = 2 * (this.defg + 5) / 2 * 100000;
            if ((this.defg + point) <= powerLimit.getDefense()) {
                if (doUseTiemNang(tiemNangUse)) {
                    defg += point;
                    updatePoint = true;
                }
            } else {
                Service.gI().sendThongBao(player, "Giáp của bạn đã đạt mức tối đa");
                Service.gI().sendMoney(player);
                return;
            }
        }
        if (type == 4) {
            short pointCrit = 1;
            if ((this.critg + pointCrit) <= powerLimit.getCritical()) {
                tiemNangUse = getCritTiemNangUse(this.critg, pointCrit);
                if (doUseTiemNang(tiemNangUse)) {
                    critg += pointCrit;
                    updatePoint = true;
                }
            } else {
                Service.gI().sendThongBao(player, "Chí mạng của bạn đã đạt mức tối đa");
                Service.gI().sendMoney(player);
                return;
            }
        }
        if (updatePoint) {
            Service.gI().point(player);
        }
        if (manualForPet) {
            Service.gI().sendChiSoPetGoc(((Pet) player).master);
            Service.gI().showInfoPet(((Pet) player).master);
            Service.gI().point(((Pet) player).master);
        }
    }
//    public void increasePoint(byte type, short point, boolean manualForPet) {
//        if (powerLimit == null) {
//            return;
//        }
//        if (point <= 0) {
//            return;
//        }
//        boolean updatePoint = false;
//        long tiemNangUse = 0;
//        if (type == 0) {
//            int pointHp = point * 20;
//            tiemNangUse = point * (2 * (this.hpg + 1000) + pointHp - 20) / 2;
//            if ((this.hpg + pointHp) <= powerLimit.getHp()) {
//                if (doUseTiemNang(tiemNangUse)) {
//                    hpg += pointHp;
//                    updatePoint = true;
//                }
//            } else {
//                Service.gI().sendThongBao(player, "HP của bạn đã đạt mức tối đa");
//                Service.gI().sendMoney(player);
//                return;
//            }
//        }
//        if (type == 1) {
//            int pointMp = point * 20;
//            tiemNangUse = point * (2 * (this.mpg + 1000) + pointMp - 20) / 2;
//            if ((this.mpg + pointMp) <= powerLimit.getMp()) {
//                if (doUseTiemNang(tiemNangUse)) {
//                    mpg += pointMp;
//                    updatePoint = true;
//                }
//            } else {
//                Service.gI().sendThongBao(player, "KI của bạn đã đạt mức tối đa");
//                Service.gI().sendMoney(player);
//                return;
//            }
//        }
//        if (type == 2) {
//            tiemNangUse = point * (2 * this.dameg + point - 1) / 2 * 100;
//            if ((this.dameg + point) <= powerLimit.getDamage()) {
//                if (doUseTiemNang(tiemNangUse)) {
//                    dameg += point;
//                    updatePoint = true;
//                }
//            } else {
//                Service.gI().sendThongBao(player, "Sức đánh của bạn đã đạt mức tối đa");
//                Service.gI().sendMoney(player);
//                return;
//            }
//        }
//        if (type == 3) {
//            tiemNangUse = 2 * (this.defg + 5) / 2 * 100000;
//            if ((this.defg + point) <= powerLimit.getDefense()) {
//                if (doUseTiemNang(tiemNangUse)) {
//                    defg += point;
//                    updatePoint = true;
//                }
//            } else {
//                Service.gI().sendThongBao(player, "Giáp của bạn đã đạt mức tối đa");
//                Service.gI().sendMoney(player);
//                return;
//            }
//        }
//        if (type == 4) {
//            tiemNangUse = 50000000L;
//            for (int i = 0; i < this.critg; i++) {
//                tiemNangUse *= 5L;
//            }
//            if ((this.critg + point) <= powerLimit.getCritical()) {
//                if (doUseTiemNang(tiemNangUse)) {
//                    critg += point;
//                    updatePoint = true;
//                }
//            } else {
//                Service.gI().sendThongBao(player, "Chí mạng của bạn đã đạt mức tối đa");
//                Service.gI().sendMoney(player);
//                return;
//            }
//        }
//        if (updatePoint) {
//            Service.gI().point(player);
//        }
//        if (manualForPet) {
//            Service.gI().sendChiSoPetGoc(((Pet) player).master);
//            Service.gI().showInfoPet(((Pet) player).master);
//            Service.gI().point(((Pet) player).master);
//        }
//    }

    // public void increasePoint(byte type, short point) {
    // if (point <= 0 || point > 100) {
    // return;
    // }
    // long tiemNangUse;
    // if (type == 0) {
    // int pointHp = point * 20;
    // tiemNangUse = point * (2 * (this.hpg + 1000) + pointHp - 20) / 2;
    // if ((this.hpg + pointHp) <= getHpMpLimit()) {
    // if (doUseTiemNang(tiemNangUse)) {
    // hpg += pointHp;
    // }
    // } else {
    // Service.gI().sendThongBaoOK(player, "Vui lòng mở giới hạn sức mạnh");
    // return;
    // }
    // }
    // if (type == 1) {
    // int pointMp = point * 20;
    // tiemNangUse = point * (2 * (this.mpg + 1000) + pointMp - 20) / 2;
    // if ((this.mpg + pointMp) <= getHpMpLimit()) {
    // if (doUseTiemNang(tiemNangUse)) {
    // mpg += pointMp;
    // }
    // } else {
    // Service.gI().sendThongBaoOK(player, "Vui lòng mở giới hạn sức mạnh");
    // return;
    // }
    // }
    // if (type == 2) {
    // TaskService.gI().checkDoneTaskNangCS(player);
    // tiemNangUse = point * (2 * this.dameg + point - 1) / 2 * 100;
    // if ((this.dameg + point) <= getDameLimit()) {
    // if (doUseTiemNang(tiemNangUse)) {
    // dameg += point;
    // }
    // TaskService.gI().checkDoneTaskNangCS(player);
    // } else {
    // Service.gI().sendThongBaoOK(player, "Vui lòng mở giới hạn sức mạnh");
    // return;
    // }
    // }
    // if (type == 3) {
    // tiemNangUse = 2 * (this.defg + 5) / 2 * 100000;
    // if ((this.defg + point) <= getDefLimit()) {
    // if (doUseTiemNang(tiemNangUse)) {
    // defg += point;
    // }
    // } else {
    // Service.gI().sendThongBaoOK(player, "Vui lòng mở giới hạn sức mạnh");
    // return;
    // }
    // }
    // if (type == 4) {
    // tiemNangUse = 50000000L;
    // for (int i = 0; i < this.critg; i++) {
    // tiemNangUse *= 5L;
    // }
    // if ((this.critg + point) <= getCritLimit()) {
    // if (doUseTiemNang(tiemNangUse)) {
    // critg += point;
    // }
    // } else {
    // Service.gI().sendThongBaoOK(player, "Vui lòng mở giới hạn sức mạnh");
    // return;
    // }
    // }
    // Service.gI().point(player);
    // }
    private long getCritTiemNangUse(int startCrit, int point) {
        long total = 0;
        for (int i = 0; i < point; i++) {
            long cost = getCritTiemNangUseAt(startCrit + i);
            if (Long.MAX_VALUE - total < cost) {
                return Long.MAX_VALUE;
            }
            total += cost;
        }
        return total;
    }

    private long getCritTiemNangUseAt(int crit) {
        if (crit >= 0 && crit < EARLY_CRIT_TIEM_NANG_COST.length) {
            return EARLY_CRIT_TIEM_NANG_COST[crit];
        }
        int critCostLevel = Math.max(1, crit - 3);
        return 15000000000L * critCostLevel;
    }

    private boolean doUseTiemNang(long tiemNang) {
        if (this.tiemNang < tiemNang) {
            Service.gI().sendThongBaoOK(player, "Bạn không đủ tiềm năng");
            return false;
        }
        if (this.tiemNang >= tiemNang && this.tiemNang - tiemNang >= 0) {
            this.tiemNang -= tiemNang;
            TaskService.gI().checkDoneTaskUseTiemNang(player);
            return true;
        }
        return false;
    }

    public long getFullTN() {
        long tnhp = 0, tnki = 0, tnsd = 0, tng = 0, tncm = 0;

        if (hpg > 0) {
            tnhp = (((hpg / 20L) * (50L + (50L + (hpg / 20L) - 1L)) / 2L) * 20L);
        }
        if (mpg > 0) {
            tnki = (((mpg / 20L) * (50L + (50L + (mpg / 20L) - 1L)) / 2L) * 20L);
        }
        if (dameg > 0) {
            tnsd = ((dameg * (dameg - 1L) * 100L) / 2L);
        }
        if (defg > 0) {
            tng = ((defg * (500000L + (500000L + (defg - 1L) * 100000L))) / 2L);
        }
        if (critg > 0) {
            tncm = getCritTiemNangUse(0, this.critg);
        }
        return tnhp + tnki + tnsd + tng + tncm;
    }

    // --------------------------------------------------------------------------
    private long lastTimeHoiPhuc;
    private long lastTimeHoiStamina;

    public void update() {
        if (player != null && player.effectSkill != null) {
            if (player.effectSkill.isCharging && player.effectSkill.countCharging < 10) {
                int tiLeHoiPhuc = SkillUtil.getPercentCharge(player.playerSkill.skillSelect.point);
                if (player.effectSkill.isCharging && !player.isDie() && !player.effectSkill.isHaveEffectSkill()
                        && (hp < hpMax || mp < mpMax)) {
                    long hpRecovered = hpMax / 100 * tiLeHoiPhuc;
                    long mpRecovered = mpMax / 100 * tiLeHoiPhuc;

                    // if (hp + hpRecovered > 2_000_000_000) {
                    // hpRecovered = 2_000_000_000 - hp;
                    // }
                    // if (mp + mpRecovered > 2_000_000_000) {
                    // mpRecovered = 2_000_000_000 - mp;
                    // }
                    PlayerService.gI().hoiPhuc(player, Util.maxIntValue(hpRecovered), Util.maxIntValue(mpRecovered));

                    if (player.effectSkill.countCharging % 3 == 0) {
                        Service.gI().chat(player, "Phục hồi năng lượng " + getCurrPercentHP() + "%");
                    }
                } else {
                    EffectSkillService.gI().stopCharge(player);
                }
                if (++player.effectSkill.countCharging >= 10) {
                    EffectSkillService.gI().stopCharge(player);
                }
            }

            if (Util.canDoWithTime(lastTimeHoiPhuc, 30000)) {
                PlayerService.gI().hoiPhuc(this.player, Util.maxIntValue(hpHoi), Util.maxIntValue(mpHoi));
                this.lastTimeHoiPhuc = System.currentTimeMillis();
            }

            if (Util.canDoWithTime(lastTimeHoiStamina, 60000) && this.stamina < this.maxStamina) {
                this.stamina++;
                this.lastTimeHoiStamina = System.currentTimeMillis();

                if (!this.player.isBoss && !this.player.isPet) {
                    PlayerService.gI().sendCurrentStamina(this.player);
                }
            }
        }
    }

    public void dispose() {
        this.intrinsic = null;
        this.player = null;
        this.tlHp = null;
        this.tlMp = null;
        this.tlDame = null;
        this.tlDameAttMob = null;
        this.tlTNSM = null;
    }
}
