package services.func;

/*
 *
 *
 * @author CongHoan
 */
import consts.ConstItem;
import models.Combine.CombineService;
import models.ClanNamekWar.ClanNamekWarService;
import models.ShenronEvent.ShenronEventService;
import models.Card.Card;
import models.Card.RadarService;
import models.Card.RadarCard;
import consts.ConstMap;
import item.Item;
import consts.ConstNpc;
import consts.ConstPlayer;
import item.Item.ItemOption;
import map.Zone;
import player.Inventory;
import services.*;
import player.Player;
import skill.Skill;
import network.Message;
import utils.SkillUtil;
import utils.TimeUtil;
import utils.Util;
import server.io.MySession;
import utils.Logger;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import jdbc.daos.PlayerDAO;
import npc.specialnpc.MabuEgg;

public class UseItem {

    private static final int ITEM_BOX_TO_BODY_OR_BAG = 0;
    private static final int ITEM_BAG_TO_BOX = 1;
    private static final int ITEM_BODY_TO_BOX = 3;
    private static final int ITEM_BAG_TO_BODY = 4;
    private static final int ITEM_BODY_TO_BAG = 5;
    private static final int ITEM_BAG_TO_PET_BODY = 6;
    private static final int ITEM_BODY_PET_TO_BAG = 7;

    private static final byte DO_USE_ITEM = 0;
    private static final byte DO_THROW_ITEM = 1;
    private static final byte ACCEPT_THROW_ITEM = 2;
    private static final byte ACCEPT_USE_ITEM = 3;

    private static UseItem instance;

    private int randClothes(int level) {
        return ConstItem.LIST_ITEM_CLOTHES[Util.nextInt(0, 2)][Util.nextInt(0, 4)][level - 1];
    }

    private UseItem() {

    }

    public static UseItem gI() {
        if (instance == null) {
            instance = new UseItem();
        }
        return instance;
    }

    public void getItem(MySession session, Message msg) {
        Player player = session.player;
        if (player == null) {
            return;
        }
        TransactionService.gI().cancelTrade(player);
        try {
            int type = msg.reader().readByte();
            int index = msg.reader().readByte();
            if (index == -1) {
                return;
            }
            switch (type) {
                case ITEM_BOX_TO_BODY_OR_BAG:
                    InventoryService.gI().itemBoxToBodyOrBag(player, index);
                    TaskService.gI().checkDoneTaskGetItemBox(player);
                    break;
                case ITEM_BAG_TO_BOX:
                    InventoryService.gI().itemBagToBox(player, index);
                    break;
                case ITEM_BODY_TO_BOX:
                    InventoryService.gI().itemBodyToBox(player, index);
                    break;
                case ITEM_BAG_TO_BODY:
                    InventoryService.gI().itemBagToBody(player, index);
                    break;
                case ITEM_BODY_TO_BAG:
                    InventoryService.gI().itemBodyToBag(player, index);
                    break;
                case ITEM_BAG_TO_PET_BODY:
                    InventoryService.gI().itemBagToPetBody(player, index);
                    break;
                case ITEM_BODY_PET_TO_BAG:
                    InventoryService.gI().itemPetBodyToBag(player, index);
                    break;
            }
            if (player.setClothes != null) {
                player.setClothes.setup();
            }
            if (player.pet != null) {
                player.pet.setClothes.setup();
            }
            player.setClanMember();
            Service.gI().sendFlagBag(player);
            Service.gI().point(player);
            Service.gI().sendSpeedPlayer(player, -1);
        } catch (Exception e) {
            Logger.logException(UseItem.class, e);

        }
    }

    public Item finditem(Player player, int iditem) {
        for (Item item : player.inventory.itemsBag) {
            if (item.isNotNullItem() && item.template.id == iditem) {
                return item;
            }
        }
        return null;
    }

    public void doItem(Player player, Message _msg) {
        TransactionService.gI().cancelTrade(player);
        Message msg = null;
        byte type;
        try {
            type = _msg.reader().readByte();
            int where = _msg.reader().readByte();
            int index = _msg.reader().readByte();
            switch (type) {
                case DO_USE_ITEM:
                    if (player != null && player.inventory != null) {
                        if (index != -1) {
                            if (index < 0) {
                                return;
                            }
                            Item item = player.inventory.itemsBag.get(index);
                            if (item.isNotNullItem()) {
                                if (item.template.type == 7) {
                                    msg = new Message(-43);
                                    msg.writer().writeByte(type);
                                    msg.writer().writeByte(where);
                                    msg.writer().writeByte(index);
                                    msg.writer().writeUTF("Bạn chắc chắn học "
                                            + player.inventory.itemsBag.get(index).template.name + "?");
                                    player.sendMessage(msg);
                                } else if (item.template.id == 570) {
                                    // if (!Util.isAfterMidnight(player.lastTimeRewardWoodChest)) {
                                    // Service.gI().sendThongBao(player, "Hãy chờ đến ngày mai");
                                    // return;
                                    // }
                                    msg = new Message(-43);
                                    msg.writer().writeByte(type);
                                    msg.writer().writeByte(where);
                                    msg.writer().writeByte(index);
                                    msg.writer().writeUTF("Bạn chắc muốn mở\n"
                                            + player.inventory.itemsBag.get(index).template.name + " ?");
                                    player.sendMessage(msg);
                                } else if (item.template.type == 22) {
                                    if (player.zone.items.stream()
                                            .filter(it -> it != null && it.itemTemplate.type == 22).count() > 2) {
                                        Service.gI().sendThongBaoOK(player, "Mỗi map chỉ đặt được 3 Vệ Tinh");
                                        return;
                                    }
                                    msg = new Message(-43);
                                    msg.writer().writeByte(type);
                                    msg.writer().writeByte(where);
                                    msg.writer().writeByte(index);
                                    msg.writer().writeUTF("Bạn chắc muốn dùng\n"
                                            + player.inventory.itemsBag.get(index).template.name + " ?");
                                    player.sendMessage(msg);
                                } else {
                                    UseItem.gI().useItem(player, item, index);
                                }
                            }
                        } else {
                            int iditem = _msg.reader().readShort();
                            Item item = finditem(player, iditem);
                            UseItem.gI().useItem(player, item, index);
                        }
                    }
                    break;
                case DO_THROW_ITEM:
                    if (!(player.zone.map.mapId == 21 || player.zone.map.mapId == 22 || player.zone.map.mapId == 23)) {
                        Item item = null;
                        if (index < 0) {
                            return;
                        }
                        if (where == 0) {
                            item = player.inventory.itemsBody.get(index);
                        } else {
                            item = player.inventory.itemsBag.get(index);
                        }

                        if (item.isNotNullItem() && item.template.id == 570) {
                            Service.gI().sendThongBao(player, "Không thể bỏ vật phẩm này.");
                            return;
                        }
                        if (!item.isNotNullItem()) {
                            return;
                        }
                        msg = new Message(-43);
                        msg.writer().writeByte(type);
                        msg.writer().writeByte(where);
                        msg.writer().writeByte(index);
                        msg.writer().writeUTF("Bạn chắc chắn muốn vứt " + item.template.name + "?");
                        player.sendMessage(msg);
                    } else {
                        Service.gI().sendThongBao(player, "Không thể thực hiện");
                    }
                    break;
                case ACCEPT_THROW_ITEM:
                    InventoryService.gI().throwItem(player, where, index);
                    Service.gI().point(player);
                    InventoryService.gI().sendItemBag(player);
                    break;
                case ACCEPT_USE_ITEM:
                    UseItem.gI().useItem(player, player.inventory.itemsBag.get(index), index);
                    break;
            }
        } catch (Exception e) {
            Logger.logException(UseItem.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    private void useItem(Player pl, Item item, int indexBag) {
        if (item != null && item.isNotNullItem()) {
            if (item.template.id == 570) {
                int time = (int) TimeUtil.diffDate(new Date(), new Date(item.createTime), TimeUtil.DAY);
                if (time == 0) {
                    Service.gI().sendThongBao(pl, "Hãy chờ đến ngày mai");
                } else {
                    openRuongGo(pl, item);
                }
                return;
            }
            if (item.template.strRequire <= pl.nPoint.power) {
                switch (item.template.type) {

                    case 21:
                        InventoryService.gI().itemBagToBody(pl, indexBag);
                        //    PetService.Pet2(pl, pl.getHeadThuCung(), pl.getBodyThuCung(), pl.getLegThuCung());
                        Service.gI().point(pl);
                        break;
                    case 33: // card
                        UseCard(pl, item);
                        break;
                    case 7: // sách học, nâng skill
                        learnSkill(pl, item);
                        break;
                    case 6: // đậu thần
                        this.eatPea(pl);
                        break;
                    case 12: // ngọc rồng các loại
                        controllerCallRongThan(pl, item);
                        break;
                    case 23: // thú cưỡi mới
                    case 24: // thú cưỡi cũ
                        InventoryService.gI().itemBagToBody(pl, indexBag);
                        break;
                    case 11: // item bag
                        InventoryService.gI().itemBagToBody(pl, indexBag);

                        Service.gI().sendFlagBag(pl);

                        break;
                    case 75:
                        InventoryService.gI().itemBagToBody(pl, indexBag);
                        Service.gI().sendchienlinh(pl, (short) (item.template.iconID - 1));
                        break;
                    case 72: {
                        InventoryService.gI().itemBagToBody(pl, indexBag);
                        Service.gI().sendPetFollow(pl, (short) (item.template.iconID - 1));
                        break;
                    }
                    case 98: {
                        InventoryService.gI().itemBagToBody(pl, indexBag);
                        Service.gI().sendEffPlayer(pl);
                        break;
                    }
                    case 99: {
                        InventoryService.gI().itemBagToBody(pl, indexBag);
                        Service.gI().sendEffPlayer(pl);
                        break;
                    }
                    default:
                        switch (item.template.id) {
                            case 992: // Nhan thoi khong
                                pl.type = 2;
                                pl.maxTime = 5;
                                Service.gI().Transport(pl);
                                break;
                            case 361:
                                pl.idGo = (short) Util.nextInt(0, 6);
                                NgocRongNamecService.gI().menuCheckTeleNamekBall(pl);
                                InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                                InventoryService.gI().sendItemBag(pl);
                                break;
                            // case 942:
                            // InventoryService.gI().itemBagToBody(pl, indexBag);
                            // PetService.Pet2(pl, 966, 967, 968);
                            // Service.gI().point(pl);
                            // break;
                            // case 943:
                            // InventoryService.gI().itemBagToBody(pl, indexBag);
                            // PetService.Pet2(pl, 969, 970, 971);
                            // Service.gI().point(pl);
                            // break;
                            // case 944:
                            // InventoryService.gI().itemBagToBody(pl, indexBag);
                            // PetService.Pet2(pl, 972, 973, 974);
                            // Service.gI().point(pl);
                            // break;
                            // case 967:
                            // InventoryService.gI().itemBagToBody(pl, indexBag);
                            // PetService.Pet2(pl, 1050, 1051, 1052);
                            // Service.gI().point(pl);
                            // break;
                            // case 1107:
                            // InventoryService.gI().itemBagToBody(pl, indexBag);
                            // PetService.Pet2(pl, 1183, 1184, 1185);
                            // Service.gI().point(pl);
                            // break;
                            // case 1686:
                            // InventoryService.gI().itemBagToBody(pl, indexBag);
                            // PetService.Pet2(pl, 1572, 1573, 1574);
                            // Service.gI().point(pl);
                            // break;
                            // case 1683:
                            // InventoryService.gI().itemBagToBody(pl, indexBag);
                            // PetService.Pet2(pl, 1561, 1562, 1563);
                            // Service.gI().point(pl);
                            // break;
                            // case 1682:
                            // InventoryService.gI().itemBagToBody(pl, indexBag);
                            // PetService.Pet2(pl, 1558, 1559, 1560);
                            // Service.gI().point(pl);
                            // break;
                            // case 942:
                            // case 943:
                            // case 944:
                            // case 967:
                            // case 1107:
                            // case 1686:
                            // case 1683:
                            // case 1682:
                            // InventoryService.gI().itemBagToBody(pl, indexBag);
                            // PetService.Pet2(pl, item.template.head, item.template.body,
                            // item.template.leg);
                            // Service.gI().point(pl);
                            // break;
                            case 211: // nho tím
                            case 212: // nho xanh
                                eatGrapes(pl, item);
                                break;
                            case 342:
                            case 343:
                            case 344:
                            case 345:
                                if (pl.zone.items.stream().filter(it -> it != null && it.itemTemplate.type == 22)
                                        .count() < 3) {
                                    Service.gI().dropSatellite(pl, item, pl.zone, pl.location.x, pl.location.y);
                                    InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                                } else {
                                    Service.gI().sendThongBaoOK(pl, "Mỗi map chỉ đặt được 3 Vệ Tinh");
                                }
                                break;
                            case 380: // cskb
                                openCSKB(pl, item);
                                break;
                            case 381: // cuồng nộ
                            case 382: // bổ huyết
                            case 383: // bổ khí
                            case 384: // giáp xên
                            case 385: // ẩn danh
                            case 379: // máy dò capsule
                            case 638: // commeson
                            case 2075: // rocket
                            case 2160: // Nồi cơm điện
                            case 579:
                            case 1045: // đuôi khỉ
                            case 663: // bánh pudding
                            case 664: // xúc xíc
                            case 665: // kem dâu
                            case 666: // mì ly
                            case 667: // sushi
                            case 1150:
                            case 1151:
                            case 1152:
                            case 1153:
                            case 1154:
                            case 1628:
                            case 764:
                            case 1731:
                            case 1727:
                            case 1728:
                            case 1729:
                            case 1730:
                            case 1195:
                            case 1196:
                            case 752:
                            case 753:
                            case 1614:
                            case 1615:
                            case 1616:
                            case 1836:
                            case 1837:
                            case 1838:
                                useItemTime(pl, item);
                                break;
                            case 1560:
                                openRuongNgocRong(pl, item);
                                break;
                            case 1770:
                                ruongnro(pl, item);
                                break;
                            case 880:
                            case 881:
                            case 882:
                                if (pl.itemTime.isEatMeal2) {
                                    Service.gI().sendThongBao(pl, "Chỉ được sử dụng 1 cái");
                                    break;
                                }
                                useItemTime(pl, item);
                                break;
                            case 899:
                            case 900:
                            case 902:
                            case 903:
                                if (pl.itemTime.isEatMeal3) {
                                    Service.gI().sendThongBao(pl, "Chỉ được sử dụng 1 cái");
                                    break;
                                }
                                useItemTime(pl, item);
                                break;
                            case 1732:
                                openxukrai(pl, item, 100);
                                break;
                            case 521: // tdlt
                                useTDLT(pl, item);
                                break;
                            case 454: // bông tai
                                UseItem.gI().usePorata(pl);
                                break;
                            case 921: // bông tai
                                UseItem.gI().usePorata2(pl);
                                // UseItem.gI().usePorataGogeta(pl);
                                break;
                            case 1774: // bông tai
                                UseItem.gI().usePorata3(pl);
                                // UseItem.gI().usePorataGogeta(pl);
                                break;
                            case 193: // gói 10 viên capsule
                                openCapsuleUI(pl);
                                InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                            case 194: // capsule đặc biệt
                                openCapsuleUI(pl);
                                break;
                            case 401: // đổi đệ tử
                                changePet(pl, item);
                                break;
                            case 402: // sách nâng chiêu 1 đệ tử
                            case 403: // sách nâng chiêu 2 đệ tử
                            case 404: // sách nâng chiêu 3 đệ tử
                            case 759: // sách nâng chiêu 4 đệ tử
                                upSkillPet(pl, item);
                                break;
                            /// thay skill 2 đệ tử
                            case 1744:
                                replaceSkill2(pl, item, Skill.KAMEJOKO, "Kamejoko");
                                break;
                            case 1745:
                                replaceSkill2(pl, item, Skill.ANTOMIC, "Antomic");
                                break;
                            case 1746:
                                replaceSkill2(pl, item, Skill.MASENKO, "Masenko");
                                break;
                            /// thay skill 3 đệ tử
                            case 1747:
                                replaceSkill3(pl, item, Skill.THAI_DUONG_HA_SAN, "Thái dương hạ sang");
                                break;
                            case 1748:
                                replaceSkill3(pl, item, Skill.TAI_TAO_NANG_LUONG, "Tái tạo năng lượng");
                                break;
                            case 1749:
                                replaceSkill3(pl, item, Skill.KAIOKEN, "Kaioken");
                                break;
                            /// thay skill 4 đệ tử
                            case 1750:
                                replaceSkill4(pl, item, Skill.BIEN_KHI, "Biến hình");
                                break;
                            case 1751:
                                replaceSkill4(pl, item, Skill.DE_TRUNG, "Đẻ trứng");
                                break;
                            case 1752:
                                replaceSkill4(pl, item, Skill.KHIEN_NANG_LUONG, "Khiên năng lượng");
                                break;
                            case 1753:
                                int[] skillItems = {
                                    1744, 1745,
                                    1747, 1748,
                                    1750, 1751,
                                    1746, 1746, 1746, // Masenko
                                    1749, 1749, 1749, // Kaioken
                                    1752, 1752, 1752 // Khiên năng lượng
                                };

                                if (InventoryService.gI().getCountEmptyBag(pl) == 0) {
                                    Service.gI().sendThongBaoOK(pl, "Cần 1 ô hành trang để mở");
                                    return;
                                }

                                Item sachthayskill = ItemService.gI()
                                        .createNewItem((short) skillItems[Util.nextInt(skillItems.length)]);

                                sachthayskill.itemOptions.add(new Item.ItemOption(30, 1));

                                InventoryService.gI().addItemBag(pl, sachthayskill);
                                InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                                InventoryService.gI().sendItemBag(pl);

                                Service.gI().sendThongBao(pl, "Bạn đã nhận được " + sachthayskill.template.name);
                                break;

                            case 568: // QUẢ TRỨNG MABU
                                if (pl.mabuEgg != null) {
                                    Service.gI().sendThongBao(pl, "Con đang có trứng Mabư chưa mở ở nhà rồi.");
                                    break;
                                }
                                MabuEgg.createMabuEgg(pl);
                                InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                                InventoryService.gI().sendItemBag(pl);
                                NpcService.gI().createMenuConMeo(pl,
                                        ConstNpc.THONG_BAO_TRUNG_MABU,
                                        ConstNpc.CON_MEO,
                                        "Bạn đã mang trứng Mabư về nhà.\n\n"
                                        + "Nếu vào nhà mà KHÔNG thấy trứng,\n"
                                        + "hãy xóa dữ liệu game và vào lại.",
                                        "OK", "Đóng");
                                break;
                            case 726:
                                UseItem.gI().ItemManhGiay(pl, item);
                                break;
                            case 1811: {
                                int rand = Util.nextInt(1, 100);
                                short itemId;

                                if (rand <= 98) {
                                    int r = Util.nextInt(1, 100);
                                    if (r <= 40) {
                                        itemId = 1812;
                                    } else if (r <= 70) {
                                        itemId = 1813;
                                    } else if (r <= 90) {
                                        itemId = 1814;
                                    } else {
                                        itemId = 1815;
                                    }
                                } else {
                                    int r = Util.nextInt(1, 100);
                                    if (r <= 60) {
                                        itemId = 1820;
                                    } else if (r <= 90) {
                                        itemId = 1821;
                                    } else {
                                        itemId = 1822;
                                    }
                                }

                                Item it = ItemService.gI().createNewItem(itemId);
                                it.itemOptions.add(new ItemOption(30, 0));
                                InventoryService.gI().addItemBag(pl, it);
                                InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                                InventoryService.gI().sendItemBag(pl);
                                break;
                            }

                            case 1812:
                            case 1813:
                            case 1814:
                            case 1815:
                            case 1820:
                            case 1821:
                            case 1822:
                            case 1823: {
                                int addVND = switch (item.template.id) {
                                    case 1812 ->
                                        2000;
                                    case 1813 ->
                                        5000;
                                    case 1814 ->
                                        10000;
                                    case 1815 ->
                                        20000;
                                    case 1820 ->
                                        50000;
                                    case 1821 ->
                                        100000;
                                    case 1822 ->
                                        200000;
                                    case 1823 ->
                                        500000;
                                    default ->
                                        0;
                                };

                                if (addVND > 0) {
                                    PlayerDAO.addvnd(pl, addVND);
                                    Service.gI().sendThongBao(pl, "Bạn nhận được " + Util.numberToMoney(addVND) + " Coin");
                                }

                                InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                                InventoryService.gI().sendItemBag(pl);
                                break;
                            }

                            case 727:
                            case 728:
                                UseItem.gI().ItemSieuThanThuy(pl, item);
                                break;
                            case 648:
                                ItemService.gI().OpenItem648(pl, item);
                                break;
                            case 1592:
                                ItemService.gI().OpenItem1592(pl, item);
                                break;
                            case 1951: // đổi đệ tử
                                NpcService.gI().createMenuConMeo(pl,
                                        ConstNpc.THONG_BAO_DE_TYPE3,
                                        -1,
                                        "|7|Nhớ tháo đồ đệ tử trước khi sài bình hút năng lượng",
                                        "Ok", "Đóng");
                                break;
                            case 1955: // đổi đệ tử
                                NpcService.gI().createMenuConMeo(pl,
                                        ConstNpc.THONG_BAO_DE_TYPE5,
                                        -1,
                                        "|7|Cần phải có đủ 500 kill Cumber và nhớ tháo đồ đệ tử trước khi sài",
                                        "Ok", "Đóng");
                                break;
                            case 1956: // đổi đệ tử
                                NpcService.gI().createMenuConMeo(pl,
                                        ConstNpc.THONG_BAO_DE_TYPE6,
                                        -1,
                                        "|7|Cần phải có đủ 15999 kill Quái và nhớ tháo đồ đệ tử trước khi sài",
                                        "Ok", "Đóng");
                                break;
                            case 1957: // đổi đệ tử
                                NpcService.gI().createMenuConMeo(pl,
                                        ConstNpc.THONG_BAO_DE_TYPE7,
                                        -1,
                                        "|7|Cần phải có đủ 999 Mảnh quái, 999 điểm boss Black Goku và nhớ tháo đồ đệ tử trước khi sài",
                                        "Ok", "Đóng");
                                break;
                            case 1759:
                                ItemService.gI().OpenItem1759(pl, item);
                                break;
                            case 1757:
                                ItemService.gI().OpenItem1757(pl, item);
                                break;
                            case 1576:
                                ItemService.gI().OpenItem1576(pl, item);
                                break;
                            case 1758:
                                ItemService.gI().OpenItem1758(pl, item);
                                break;
                            case 736:
                                ItemService.gI().OpenItem736(pl, item);
                                break;
                            case 987:
                                Service.gI().sendThongBao(pl, "Bảo vệ trang bị không bị rớt cấp"); // đá bảo vệ
                                break;
                            case 2006:
                                Input.gI().createFormChangeNameByItem(pl);
                                break;
                            case 1623:
                                TaskService.gI().sendNextTaskMain(pl);
                                break;
                            case 1806:
                                ItemService.gI().OpenItem1806(pl, item);
                                break;
                            case 1967:
                                ItemService.gI().OpenItem1967(pl, item);
                                break;
                            case 1228:
                                NpcService.gI().createMenuConMeo(pl, ConstNpc.HOP_QUA_THAN_LINH, -1,
                                        "Chọn hành tinh của đồ thần linh muốn nhận.",
                                        "Trái đất", "Namek", "Xayda");
                                break;
                            case 1626: {
                                int[] listItem = {856, 943, 942};
                                if (InventoryService.gI().getCountEmptyBag(pl) == 0) {
                                    Service.gI().sendThongBaoOK(pl, "Cần 1 ô hành trang để mở");
                                    return;
                                }
                                Item phuKien = ItemService.gI().createNewItem((short) listItem[Util.nextInt(2)]);
                                if (phuKien.template.id == 856) {
                                    phuKien.itemOptions.add(new Item.ItemOption(50, 10));
                                    phuKien.itemOptions.add(new Item.ItemOption(77, 10));
                                    phuKien.itemOptions.add(new Item.ItemOption(103, 10));
                                } else if (phuKien.template.id == 943) {
                                    phuKien.itemOptions.add(new Item.ItemOption(50, 10));
                                } else if (phuKien.template.id == 942) {
                                    phuKien.itemOptions.add(new Item.ItemOption(77, 10));
                                    phuKien.itemOptions.add(new Item.ItemOption(103, 10));
                                }
                                if (Util.isTrue(95, 100)) {
                                    phuKien.itemOptions.add(new Item.ItemOption(93, Util.nextInt(1, 5)));
                                }
                                InventoryService.gI().addItemBag(pl, phuKien);
                                InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                                InventoryService.gI().sendItemBag(pl);
                                Service.gI().sendThongBao(pl, "Bạn đã nhận được " + phuKien.template.name);
                            }
                            break;
                            // case 1628: {
                            // Player player = pl;
                            // if (player.pet != null) {
                            // if (player.pet.playerSkill.skills.get(1).skillId != -1) {
                            // player.pet.openSkill2();
                            // } else {
                            // Service.gI().sendThongBao(player, "Ít nhất đệ tử ngươi phải có chiêu 2
                            // chứ!");
                            // return;
                            // }
                            // } else {
                            // Service.gI().sendThongBao(player, "Ngươi làm gì có đệ tử?");
                            // return;
                            // }
                            // }
                            // break;
                            case 1629: {
                                Player player = pl;
                                if (player.pet != null) {
                                    if (player.pet.playerSkill.skills.get(2).skillId != -1) {
                                        player.pet.openSkill3();
                                    } else {
                                        Service.gI().sendThongBao(player, "Ít nhất đệ tử ngươi phải có chiêu 3 chứ!");
                                        return;
                                    }
                                } else {
                                    Service.gI().sendThongBao(player, "Ngươi làm gì có đệ tử?");
                                    return;
                                }
                            }
                            break;
                            case 1630: {
                                Player player = pl;
                                if (player.pet != null) {
                                    if (player.pet.playerSkill.skills.get(3).skillId != -1) {
                                        player.pet.openSkill4();
                                    } else {
                                        Service.gI().sendThongBao(player, "Ít nhất đệ tử ngươi phải có chiêu 4 chứ!");
                                        return;
                                    }
                                } else {
                                    Service.gI().sendThongBao(player, "Ngươi làm gì có đệ tử?");
                                    return;
                                }
                            }
                            break;
                            case 628: {
                                int ct = Util.nextInt(618, 626);
                                Item caiTrangHaiTac = ItemService.gI().createNewItem((short) ct);
                                caiTrangHaiTac.itemOptions.add(new Item.ItemOption(93, 30));
                                caiTrangHaiTac.itemOptions.add(new Item.ItemOption(50, 15));
                                caiTrangHaiTac.itemOptions.add(new Item.ItemOption(77, 15));
                                caiTrangHaiTac.itemOptions.add(new Item.ItemOption(103, 15));
                                caiTrangHaiTac.itemOptions.add(new Item.ItemOption(149, 1));
                                InventoryService.gI().addItemBag(pl, caiTrangHaiTac);
                                InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                                Service.gI().sendThongBao(pl,
                                        "Bạn đã nhận được cải trang " + caiTrangHaiTac.template.name);
                            }
                            break;
                            case 1440: {
                                int ct = Util.nextInt(441, 447);
                                Item caiTrangHaiTac = ItemService.gI().createNewItem((short) ct);
                                caiTrangHaiTac.itemOptions.add(new Item.ItemOption(93, 30));
                                InventoryService.gI().addItemBag(pl, caiTrangHaiTac);
                                InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                                Service.gI().sendThongBao(pl, "Bạn đã nhận được " + caiTrangHaiTac.template.name);
                            }
                            break;
                            case 1453: {
                                int ct = Util.nextInt(1416, 1422);
                                Item caiTrangHaiTac = ItemService.gI().createNewItem((short) ct);
                                caiTrangHaiTac.itemOptions.add(new Item.ItemOption(93, 30));
                                InventoryService.gI().addItemBag(pl, caiTrangHaiTac);
                                InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                                Service.gI().sendThongBao(pl, "Bạn đã nhận được " + caiTrangHaiTac.template.name);
                            }
                            break;
                            case 1536: {
                            }
                            break;
                            case 1703:// set tl kh
                                UseItem.gI().Hopdothanlinh(pl, item);
                                break;  //Name: HOANDZ
                            case 1704:// set hd kh
                                UseItem.gI().Hopdohuydiet(pl, item);
                                break;  //Name: HOANDZ
                            case 1968:// set kh
                                UseItem.gI().Hopdoskh(pl, item);
                                break;  //Name: HOANDZ
                        }
                        break;
                }
                TaskService.gI().checkDoneTaskUseItem(pl, item);
                InventoryService.gI().sendItemBag(pl);
            } else {
                Service.gI().sendThongBaoOK(pl, "Sức mạnh không đủ yêu cầu");
            }
        }
    }

    public void openRuongGo(Player pl, Item item) {
        List<String> textRuongGo = new ArrayList<>();
        int time = (int) TimeUtil.diffDate(new Date(), new Date(item.createTime), TimeUtil.DAY);
        if (time != 0) {
            Item itemReward = null;
            int param = item.itemOptions.get(0).param;
            int gold = 0;
            int[] listItem = {441, 442, 443, 444, 445, 446, 447, 220, 221, 222, 223, 224, 225};
            int[] listClothesReward;
            int[] listItemReward;
            String text = "Bạn nhận được\n";
            if (param < 8) {
                gold = 100000 * param;
                listClothesReward = new int[]{randClothes(param)};
                listItemReward = Util.pickNRandInArr(listItem, 3);
            } else if (param < 10) {
                gold = 250000 * param;
                listClothesReward = new int[]{randClothes(param), randClothes(param)};
                listItemReward = Util.pickNRandInArr(listItem, 4);
            } else {
                gold = 500000 * param;
                listClothesReward = new int[]{randClothes(param), randClothes(param), randClothes(param)};
                listItemReward = Util.pickNRandInArr(listItem, 5);
                int ruby = Util.nextInt(1, 5);
                pl.inventory.ruby += ruby;
                textRuongGo.add(text + "|1| " + ruby + " Hồng Ngọc");
            }
            for (var i : listClothesReward) {
                itemReward = ItemService.gI().createNewItem((short) i);
                RewardService.gI().initBaseOptionClothes(itemReward.template.id, itemReward.template.type,
                        itemReward.itemOptions);
                RewardService.gI().initStarOption(itemReward, new RewardService.RatioStar[]{
                    new RewardService.RatioStar((byte) 1, 1, 2), new RewardService.RatioStar((byte) 2, 1, 3),
                    new RewardService.RatioStar((byte) 3, 1, 4), new RewardService.RatioStar((byte) 4, 1, 5),});
                InventoryService.gI().addItemBag(pl, itemReward);
                textRuongGo.add(text + itemReward.info);
            }
            for (var i : listItemReward) {
                itemReward = ItemService.gI().createNewItem((short) i);
                RewardService.gI().initBaseOptionSaoPhaLe(itemReward);
                itemReward.quantity = Util.nextInt(1, 5);
                InventoryService.gI().addItemBag(pl, itemReward);
                textRuongGo.add(text + itemReward.info);
            }
            if (param == 11) {
                itemReward = ItemService.gI().createNewItem((short) ConstItem.MANH_NHAN);
                itemReward.quantity = Util.nextInt(1, 3);
                InventoryService.gI().addItemBag(pl, itemReward);
                textRuongGo.add(text + itemReward.info);
            }
            NpcService.gI().createMenuConMeo(pl, ConstNpc.RUONG_GO, -1,
                    "Bạn nhận được\n|1|+" + Util.numberToMoney(gold) + " vàng", "OK [" + textRuongGo.size() + "]");
            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
            pl.inventory.addGold(gold);
            InventoryService.gI().sendItemBag(pl);
            PlayerService.gI().sendInfoHpMpMoney(pl);
        }
    }

    // public void openRuongGo(Player player) {
    // Item ruongGo = InventoryService.gI().findItemBag(player, 570);
    // if (ruongGo != null) {
    // int level = InventoryService.gI().getParam(player, 72, 570);
    // if (InventoryService.gI().getCountEmptyBag(player) < level) {
    // Service.gI().sendThongBao(player, "Cần ít nhất " + level + " ô trống trong
    // hành trang");
    // } else {
    // player.itemsWoodChest.clear();
    // if (level == 0) {
    // InventoryService.gI().subQuantityItemsBag(player, ruongGo, 1);
    // InventoryService.gI().sendItemBag(player);
    // Item item = ItemService.gI().createNewItem((short) 673);
    // InventoryService.gI().addItemBag(player, item);
    // InventoryService.gI().sendItemBag(player);
    // Service.gI().sendThongBao(player, "Bạn vừa nhận được set Lính Thủy Đánh
    // Bạc!");
    // return;
    // }
    // int rand = Util.nextInt(0, 6);
    // Item level1 = ItemService.gI().createNewItem(((short) (441 + rand)));
    // level1.itemOptions.add(new Item.ItemOption(95 + rand, (rand == 3 || rand ==
    // 4) ? 3 : 5));
    // level1.quantity = Util.nextInt(1, level * 2);
    // player.itemsWoodChest.add(level1);
    // if (level > 1) {
    // rand = Util.nextInt(0, 4);
    // Item level2 = ItemService.gI().createNewItem(((short) (220 + rand)));
    // level2.itemOptions.add(new Item.ItemOption(71 - rand, 0));
    // level2.quantity = Util.nextInt(1, level * 3);
    // player.itemsWoodChest.add(level2);
    // }
    // if (level > 2) {
    // Item level3 = ItemService.gI().createNewItem((short) 20); // 7 sao
    // level3.quantity = Util.nextInt(1, level);
    // player.itemsWoodChest.add(level3);
    // }
    // if (level > 3) {
    // Item level4 = ItemService.gI().createNewItem((short) 19); // 6 sao
    // level4.quantity = Util.nextInt(1, level);
    // player.itemsWoodChest.add(level4);
    // }
    // if (level > 4) {
    // Item level5 = ItemService.gI().createNewItem((short) 18); // 5 sao
    // level5.quantity = Util.nextInt(1, level);
    // player.itemsWoodChest.add(level5);
    // }
    // if (level > 5) {
    // Item level6 = ItemService.gI().createNewItem((short) 17); // 4 sao
    // level6.quantity = Util.nextInt(1, level);
    // player.itemsWoodChest.add(level6);
    // }
    // if (level > 6) {
    // Item level7 = ItemService.gI().createNewItem((short) 16); // 3 sao
    // level7.quantity = Util.nextInt(1, level);
    // player.itemsWoodChest.add(level7);
    // }
    // if (level > 7) {
    // Item level8 = ItemService.gI().createNewItem((short) 457); // thoi vang
    // level8.quantity = Util.nextInt(100, level * 100);
    // player.itemsWoodChest.add(level8);
    // }
    // if (level > 8) {
    // Item level9 = ItemService.gI().createNewItem((short) 1229); // bi kiep tuyet
    // ky
    // level9.quantity = Util.nextInt(15, level + 15);
    // player.itemsWoodChest.add(level9);
    // }
    // if (level > 9) {
    // int[] itemId = {1229, 2026, 2036, 2037, 2038, 2039, 2040, 2019, 2020, 2021,
    // 2022, 2023, 2024};
    // byte[] option = {77, 80, 81, 103, 50, 94, 5};
    // byte[] option_v2 = {14, 16, 17, 19, 27, 28, 5, 47, 87}; //77 %hp // 80 //81
    // //103 //50 //94 //5 % sdcm
    // byte optionid;
    // byte optionid_v2;
    // byte param;
    // Item level10 = ItemService.gI().createNewItem((short) itemId[Util.nextInt(0,
    // 12)]);
    // level10.itemOptions.clear();
    // optionid = option[Util.nextInt(0, 6)];
    // param = (byte) Util.nextInt(5, 10);
    // level10.itemOptions.add(new Item.ItemOption(optionid, param));
    // if (Util.isTrue(20, 100)) {
    // optionid_v2 = option_v2[Util.nextInt(option_v2.length)];
    // level10.itemOptions.add(new Item.ItemOption(optionid_v2, param));
    // }
    // level10.itemOptions.add(new Item.ItemOption(30, 0));
    // level10.itemOptions.add(new Item.ItemOption(93, Util.nextInt(level, 30)));
    // level10.quantity = 1;
    // player.itemsWoodChest.add(level10);
    // }
    // if (level > 10) {
    // byte[] option = {77, 80, 81, 103, 50, 94, 5};
    // byte[] option_v2 = {14, 16, 17, 19, 27, 28, 5, 47, 87}; //77 %hp // 80 //81
    // //103 //50 //94 //5 % sdcm
    // byte optionid;
    // byte optionid_v2;
    // byte param;
    // Item level11 = ItemService.gI().createNewItem((short) Util.nextInt(2112,
    // 2118));
    // level11.itemOptions.clear();
    // optionid = option[Util.nextInt(0, 6)];
    // param = (byte) Util.nextInt(5, 10);
    // level11.itemOptions.add(new Item.ItemOption(optionid, param));
    // if (Util.isTrue(20, 100)) {
    // optionid_v2 = option_v2[Util.nextInt(option_v2.length)];
    // level11.itemOptions.add(new Item.ItemOption(optionid_v2, param));
    // }
    // level11.itemOptions.add(new Item.ItemOption(30, 0));
    // if (Util.isTrue(90, 100)) {
    // level11.itemOptions.add(new Item.ItemOption(93, Util.nextInt(level, 30)));
    // }
    // level11.quantity = 1;
    // player.itemsWoodChest.add(level11);
    // }
    // if (level > 11) {
    // byte[] option = {77, 80, 81, 103, 50, 94, 5};
    // byte[] option_v2 = {14, 16, 17, 19, 27, 28, 5, 47, 87}; //77 %hp // 80 //81
    // //103 //50 //94 //5 % sdcm
    // byte optionid;
    // byte optionid_v2;
    // byte param;
    // Item level12 = ItemService.gI().createNewItem((short) Util.nextInt(2108,
    // 2122));
    // level12.itemOptions.clear();
    // optionid = option[Util.nextInt(0, 6)];
    // param = (byte) Util.nextInt(5, 10);
    // level12.itemOptions.add(new Item.ItemOption(optionid, param));
    // if (Util.isTrue(20, 100)) {
    // optionid_v2 = option_v2[Util.nextInt(option_v2.length)];
    // level12.itemOptions.add(new Item.ItemOption(optionid_v2, param));
    // }
    // level12.itemOptions.add(new Item.ItemOption(30, 0));
    // if (Util.isTrue(50, 100)) {
    // level12.itemOptions.add(new Item.ItemOption(93, Util.nextInt(level, 30)));
    // }
    // level12.quantity = 1;
    // player.itemsWoodChest.add(level12);
    // }
    // InventoryService.gI().subQuantityItemsBag(player, ruongGo, 1);
    // InventoryService.gI().sendItemBag(player);
    // for (Item it : player.itemsWoodChest) {
    // InventoryService.gI().addItemBag(player, it);
    // }
    // InventoryService.gI().sendItemBag(player);
    // player.indexWoodChest = player.itemsWoodChest.size() - 1;
    // int i = player.indexWoodChest;
    // if (i < 0) {
    // return;
    // }
    // Item itemWoodChest = player.itemsWoodChest.get(i);
    // player.indexWoodChest--;
    // String info = "|1|" + itemWoodChest.template.name;
    // String info2 = "\n|2|";
    // if (!itemWoodChest.itemOptions.isEmpty()) {
    // for (Item.ItemOption io : itemWoodChest.itemOptions) {
    // if (io.optionTemplate.id != 102 && io.optionTemplate.id != 73) {
    // info2 += io.getOptionString() + "\n";
    // }
    // }
    // }
    // info = (info2.length() > "\n|2|".length() ? (info + info2).trim() :
    // info.trim()) + "\n|0|" + itemWoodChest.template.description;
    // NpcService.gI().createMenuConMeo(player, ConstNpc.RUONG_GO, -1, "Bạn nhận
    // được\n"
    // + info.trim(), "OK" + (i > 0 ? " [" + i + "]" : ""));
    // }
    // }
    // }
    private void changePet(Player player, Item item) {
        if (player.pet != null) {
            int gender = player.pet.gender + 1;
            if (gender > 2) {
                gender = 0;
            }
            PetService.gI().changeNormalPet(player, gender);
            InventoryService.gI().subQuantityItemsBag(player, item, 1);
        } else {
            Service.gI().sendThongBao(player, "Không thể thực hiện");
        }
    }

    private void eatGrapes(Player pl, Item item) {
        int percentCurrentStatima = pl.nPoint.stamina * 100 / pl.nPoint.maxStamina;
        if (percentCurrentStatima > 50) {
            Service.gI().sendThongBao(pl, "Thể lực vẫn còn trên 50%");
            return;
        } else if (item.template.id == 211) {
            pl.nPoint.stamina = pl.nPoint.maxStamina;
            Service.gI().sendThongBao(pl, "Thể lực của bạn đã được hồi phục 100%");
        } else if (item.template.id == 212) {
            pl.nPoint.stamina += (pl.nPoint.maxStamina * 20 / 100);
            Service.gI().sendThongBao(pl, "Thể lực của bạn đã được hồi phục 20%");
        }
        InventoryService.gI().subQuantityItemsBag(pl, item, 1);
        InventoryService.gI().sendItemBag(pl);
        PlayerService.gI().sendCurrentStamina(pl);
    }

    private void openCSKB(Player pl, Item item) {
        if (InventoryService.gI().getCountEmptyBag(pl) > 0) {
            short[] temp = {381, 382, 383, 384, 385};
            short[] specialItems = {1150, 1151, 1152, 1153, 1154};
            short[] icon = new short[2];
            icon[0] = item.template.iconID;
            int rand = Util.nextInt(1, 100);

            if (rand <= 90) {
                byte index = (byte) Util.nextInt(0, temp.length - 1);
                Item it = ItemService.gI().createNewItem(temp[index]);
                it.itemOptions.add(new ItemOption(73, 0));
                InventoryService.gI().addItemBag(pl, it);
                icon[1] = it.template.iconID;
            } else {
                byte index = (byte) Util.nextInt(0, specialItems.length - 1);
                Item it = ItemService.gI().createNewItem(specialItems[index]);
                it.itemOptions.add(new ItemOption(73, 0));
                InventoryService.gI().addItemBag(pl, it);
                icon[1] = it.template.iconID;
            }
            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
            InventoryService.gI().sendItemBag(pl);

            CombineService.gI().sendEffectOpenItem(pl, icon[0], icon[1]);
        } else {
            Service.gI().sendThongBao(pl, "Hàng trang đã đầy");
        }
    }

    private void useItemTime(Player pl, Item item) {
        switch (item.template.id) {
            case 1614:
                pl.itemTime.lastTimeKhongLo = System.currentTimeMillis();
                pl.itemTime.nuocMiaKhongLo = true;
                break;
            case 1615:
                pl.itemTime.lastTimeThom = System.currentTimeMillis();
                pl.itemTime.nuocMiaThom = true;
                break;
            case 1616:
                pl.itemTime.lastTimeSauRieng = System.currentTimeMillis();
                pl.itemTime.nuocMiaSauRieng = true;
                break;
            case 1836:
                pl.itemTime.lastTimePhoTaiHanh = System.currentTimeMillis();
                pl.itemTime.isUsePhoTaiHanh = true;
                break;
            case 1837:
                pl.itemTime.lastTimePhoNam = System.currentTimeMillis();
                pl.itemTime.isUsePhoNam = true;
                break;
            case 1838:
                pl.itemTime.lastTimePhoTaiBo = System.currentTimeMillis();
                pl.itemTime.isUsePhoTaiBo = true;
                break;
            case 1731:
                if (pl.itemTime.isUseLoX5 == true || pl.itemTime.isUseLoX7 == true || pl.itemTime.isUseLoX10 == true
                        || pl.itemTime.isUseLoX15 == true) {
                    Service.gI().sendThongBao(pl, "Bạn đang sử dụng nước thánh rồi");
                    return;
                }
                pl.itemTime.lastTimeLoX2 = System.currentTimeMillis();
                pl.itemTime.isUseLoX2 = true;
                break;
            case 1727:
                if (pl.itemTime.isUseLoX2 == true || pl.itemTime.isUseLoX7 == true || pl.itemTime.isUseLoX10 == true
                        || pl.itemTime.isUseLoX15 == true) {
                    Service.gI().sendThongBao(pl, "Bạn đang sử dụng nước thánh rồi");
                    return;
                }
                pl.itemTime.lastTimeLoX5 = System.currentTimeMillis();
                pl.itemTime.isUseLoX5 = true;
                break;
            case 1728:
                if (pl.itemTime.isUseLoX5 == true || pl.itemTime.isUseLoX2 == true || pl.itemTime.isUseLoX10 == true
                        || pl.itemTime.isUseLoX15 == true) {
                    Service.gI().sendThongBao(pl, "Bạn đang sử dụng nước thánh rồi");
                    return;
                }
                pl.itemTime.lastTimeLoX7 = System.currentTimeMillis();
                pl.itemTime.isUseLoX7 = true;
                break;
            case 1729:
                if (pl.itemTime.isUseLoX5 == true || pl.itemTime.isUseLoX7 == true || pl.itemTime.isUseLoX2 == true
                        || pl.itemTime.isUseLoX15 == true) {
                    Service.gI().sendThongBao(pl, "Bạn đang sử dụng nước thánh rồi");
                    return;
                }
                pl.itemTime.lastTimeLoX10 = System.currentTimeMillis();
                pl.itemTime.isUseLoX10 = true;
                break;
            case 1730:
                if (pl.itemTime.isUseLoX5 == true || pl.itemTime.isUseLoX7 == true || pl.itemTime.isUseLoX10 == true
                        || pl.itemTime.isUseLoX2 == true) {
                    Service.gI().sendThongBao(pl, "Bạn đang sử dụng nước thánh rồi");
                    return;
                }
                pl.itemTime.lastTimeLoX15 = System.currentTimeMillis();
                pl.itemTime.isUseLoX15 = true;
                break;
            case 764:
                pl.itemTime.lastTimeKhauTrang = System.currentTimeMillis();
                pl.itemTime.isUseKhauTrang = true;
                break;
            case 1628:
                pl.itemTime.lastTimeBuax2DeTu = System.currentTimeMillis();
                pl.itemTime.isUseBuax2DeTu = true;
                break;
            case 382: // bổ huyết
                pl.itemTime.lastTimeBoHuyet = System.currentTimeMillis();
                pl.itemTime.isUseBoHuyet = true;
                break;
            case 383: // bổ khí
                pl.itemTime.lastTimeBoKhi = System.currentTimeMillis();
                pl.itemTime.isUseBoKhi = true;
                break;
            case 384: // giáp xên
                pl.itemTime.lastTimeGiapXen = System.currentTimeMillis();
                pl.itemTime.isUseGiapXen = true;
                break;
            case 381: // cuồng nộ
                pl.itemTime.lastTimeCuongNo = System.currentTimeMillis();
                pl.itemTime.isUseCuongNo = true;
                Service.gI().point(pl);
                break;
            case 385: // ẩn danh
                pl.itemTime.lastTimeAnDanh = System.currentTimeMillis();
                pl.itemTime.isUseAnDanh = true;
                break;
            case 379: // máy dò capsule
                pl.itemTime.lastTimeUseMayDo = System.currentTimeMillis();
                pl.itemTime.isUseMayDo = true;
                break;
            case 1150:// cn
                pl.itemTime.lastTimeCuongNo2 = System.currentTimeMillis();
                pl.itemTime.isUseCuongNo2 = true;
                Service.gI().point(pl);

                break;
            case 752:// cn
                pl.itemTime.lastTimeBanhtet = System.currentTimeMillis();
                pl.itemTime.isBanhtet = true;
                Service.gI().point(pl);
                break;
            case 753:// cn
                pl.itemTime.lastTimeBanhchung = System.currentTimeMillis();
                pl.itemTime.isBanhchung = true;
                Service.gI().point(pl);
                break;
            case 1195:// cn
                pl.itemTime.lastTimeXimuoihoadao = System.currentTimeMillis();
                pl.itemTime.isXimuoihoadao = true;
                Service.gI().point(pl);
                break;
            case 1196:// cn
                pl.itemTime.lastTimeXimuoihoamai = System.currentTimeMillis();
                pl.itemTime.isXimuoihoamai = true;
                Service.gI().point(pl);
                break;
            case 1151:// bo huyet
                pl.itemTime.lastTimeBoKhi2 = System.currentTimeMillis();
                pl.itemTime.isUseBoKhi2 = true;
                break;
            case 1152:// bo khi
                pl.itemTime.lastTimeBoHuyet2 = System.currentTimeMillis();
                pl.itemTime.isUseBoHuyet2 = true;
                break;
            case 1153:// gx
                pl.itemTime.lastTimeGiapXen2 = System.currentTimeMillis();
                pl.itemTime.isUseGiapXen2 = true;
                break;
            case 1154:// an danh
                pl.itemTime.lastTimeAnDanh2 = System.currentTimeMillis();
                pl.itemTime.isUseAnDanh2 = true;
                break;
            case 638: // Commeson
                pl.itemTime.lastTimeUseCMS = System.currentTimeMillis();
                pl.itemTime.isUseCMS = true;
                break;
            case 2160: // Nồi cơm điện
                pl.itemTime.lastTimeUseNCD = System.currentTimeMillis();
                pl.itemTime.isUseNCD = true;
                break;
            case 579:
            case 1045: // Đuôi khỉ
                pl.itemTime.lastTimeUseDK = System.currentTimeMillis();
                pl.itemTime.isUseDK = true;
                break;
            case 663: // bánh pudding
            case 664: // xúc xíc
            case 665: // kem dâu
            case 666: // mì ly
            case 667: // sushi
                pl.itemTime.lastTimeEatMeal = System.currentTimeMillis();
                pl.itemTime.isEatMeal = true;
                ItemTimeService.gI().removeItemTime(pl, pl.itemTime.iconMeal);
                pl.itemTime.iconMeal = item.template.iconID;
                break;
            case 880:
            case 881:
            case 882:
                pl.itemTime.lastTimeEatMeal2 = System.currentTimeMillis();
                pl.itemTime.isEatMeal2 = true;
                ItemTimeService.gI().removeItemTime(pl, pl.itemTime.iconMeal2);
                pl.itemTime.iconMeal2 = item.template.iconID;
                break;
            case 889:
            case 900:
            case 902:
            case 903:
                pl.itemTime.lastTimeEatMeal3 = System.currentTimeMillis();
                pl.itemTime.isEatMeal3 = true;
                ItemTimeService.gI().removeItemTime(pl, pl.itemTime.iconMeal3);
                pl.itemTime.iconMeal3 = item.template.iconID;
                break;
            case 1109: // máy dò đồ
                pl.itemTime.lastTimeUseMayDo2 = System.currentTimeMillis();
                pl.itemTime.isUseMayDo2 = true;
                break;
        }
        Service.gI().point(pl);
        ItemTimeService.gI().sendAllItemTime(pl);
        InventoryService.gI().subQuantityItemsBag(pl, item, 1);
        InventoryService.gI().sendItemBag(pl);
    }

    private void controllerCallRongThan(Player pl, Item item) {
        int tempId = item.template.id;
        if (tempId >= SummonDragon.NGOC_RONG_1_SAO && tempId <= SummonDragon.NGOC_RONG_7_SAO) {
            switch (tempId) {
                case SummonDragon.NGOC_RONG_1_SAO:
                case SummonDragon.NGOC_RONG_2_SAO:
                case SummonDragon.NGOC_RONG_3_SAO:
                    SummonDragon.gI().openMenuSummonShenron(pl, (byte) (tempId - 13));
                    break;
                default:
                    NpcService.gI().createMenuConMeo(pl, ConstNpc.TUTORIAL_SUMMON_DRAGON,
                            -1, "Bạn chỉ có thể gọi rồng từ ngọc 3 sao, 2 sao, 1 sao", "Hướng\ndẫn thêm\n(mới)", "OK");
                    break;
            }
        } else if (tempId >= ShenronEventService.NGOC_RONG_1_SAO && tempId <= ShenronEventService.NGOC_RONG_7_SAO) {
            ShenronEventService.gI().openMenuSummonShenron(pl, 0);
        }
    }

    private void learnSkill(Player pl, Item item) {
        Message msg;
        try {
            if (item.template.id >= 1334 && item.template.id <= 1351) {
                learnSkillSuperNew(pl, item);
            } else if (item.template.id >= 1356 && item.template.id <= 1376) {
                if (item.template.id == 1356 || item.template.id == 1363 || item.template.id == 1370) {
                    if (item.template.id >= 1363 && item.template.id <= 1369) {
                        SkillService.gI().learSkillSpecial(pl, Skill.PHAN_THAN);
                    }
                    InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                    return;
                }
                learnSkillNew2(pl, item);
            } else {
                if (item.template.gender == pl.gender || item.template.gender == 3) {
                    String[] subName = item.template.name.split("");
                    byte level = Byte.parseByte(subName[subName.length - 1]);
                    Skill curSkill = SkillUtil.getSkillByItemID(pl, item.template.id);
                    if (curSkill.point == 7) {
                        Service.gI().sendThongBao(pl, "Kỹ năng đã đạt tối đa!");
                    } else {
                        if (curSkill.point == 0) {
                            if (level == 1) {
                                curSkill = SkillUtil.createSkill(SkillUtil.getTempSkillSkillByItemID(item.template.id),
                                        level);
                                SkillUtil.setSkill(pl, curSkill);
                                InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                                msg = Service.gI().messageSubCommand((byte) 23);
                                msg.writer().writeShort(curSkill.skillId);
                                pl.sendMessage(msg);
                                msg.cleanup();
                            } else {
                                Skill skillNeed = SkillUtil
                                        .createSkill(SkillUtil.getTempSkillSkillByItemID(item.template.id), level);
                                Service.gI().sendThongBao(pl, "Vui lòng học " + skillNeed.template.name + " cấp "
                                        + skillNeed.point + " trước!");
                            }
                        } else {
                            if (curSkill.point + 1 == level) {
                                curSkill = SkillUtil.createSkill(SkillUtil.getTempSkillSkillByItemID(item.template.id),
                                        level);
                                // System.out.println(curSkill.template.name + " - " + curSkill.point);
                                SkillUtil.setSkill(pl, curSkill);
                                InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                                msg = Service.gI().messageSubCommand((byte) 62);
                                msg.writer().writeShort(curSkill.skillId);
                                pl.sendMessage(msg);
                                msg.cleanup();
                            } else {
                                Service.gI().sendThongBao(pl, "Vui lòng học " + curSkill.template.name + " cấp "
                                        + (curSkill.point + 1) + " trước!");
                            }
                        }
                        InventoryService.gI().sendItemBag(pl);
                    }
                } else {
                    Service.gI().sendThongBao(pl, "Không thể thực hiện");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void learnSkillNew2(Player pl, Item item) {
        Message msg;
        try {
            if (item.template.gender == pl.gender || item.template.gender == 3) {
                byte level = SkillUtil.getLevelSkillByItemID(item.template.id);
                Skill curSkill = SkillUtil.getSkillByItemID(pl, item.template.id);
                if (curSkill == null) {
                    SkillService.gI().learSkillSpecial(pl,
                            (byte) SkillUtil.getSkillByItemID(pl, item.template.id).skillId);
                    InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                    return;
                } else {
                    if (curSkill.point == 7) {
                        Service.gI().sendThongBao(pl, "Kỹ năng đã đạt tối đa!");
                    } else {
                        if (curSkill.point == 0) {
                            if (level == 1) {
                                curSkill = SkillUtil.createSkill(SkillUtil.getTempSkillSkillByItemID(item.template.id),
                                        level);
                                SkillUtil.setSkill(pl, curSkill);
                                InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                                msg = Service.gI().messageSubCommand((byte) 23);
                                msg.writer().writeShort(curSkill.skillId);
                                pl.sendMessage(msg);
                                msg.cleanup();
                                if (curSkill.template.id == Skill.SUPER_NAMEC
                                        || curSkill.template.id == Skill.SUPER_SAIYAN
                                        || curSkill.template.id == Skill.SUPER_TRAI_DAT) {
                                    curSkill = SkillUtil.createSkill(Skill.GONG, level);
                                    SkillUtil.setSkill(pl, curSkill);
                                    InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                                    msg = Service.gI().messageSubCommand((byte) 23);
                                    msg.writer().writeShort(curSkill.skillId);
                                    pl.sendMessage(msg);
                                    msg.cleanup();
                                }
                            } else {
                                Skill skillNeed = SkillUtil
                                        .createSkill(SkillUtil.getTempSkillSkillByItemID(item.template.id), level);
                                Service.gI().sendThongBao(pl, "Vui lòng học " + skillNeed.template.name + " cấp "
                                        + skillNeed.point + " trước!");
                            }
                        } else {
                            if (curSkill.point + 1 == level) {
                                curSkill = SkillUtil.createSkill(SkillUtil.getTempSkillSkillByItemID(item.template.id),
                                        level);
                                // System.out.println(curSkill.template.name + " - " + curSkill.point);
                                SkillUtil.setSkill(pl, curSkill);
                                InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                                msg = Service.gI().messageSubCommand((byte) 62);
                                msg.writer().writeShort(curSkill.skillId);
                                pl.sendMessage(msg);
                                msg.cleanup();
                            } else {
                                Service.gI().sendThongBao(pl, "Vui lòng học " + curSkill.template.name + " cấp "
                                        + (curSkill.point + 1) + " trước!");
                            }
                        }
                        InventoryService.gI().sendItemBag(pl);
                    }
                }
            } else {
                Service.gI().sendThongBao(pl, "Không thể thực hiện");
            }
        } catch (Exception e) {

        }
    }

    private void learnSkillSuperNew(Player pl, Item item) {
        Message msg;
        try {
            if (item.template.gender == pl.gender || item.template.gender == 3) {
                byte level = SkillUtil.getLevelSkillByItemID(item.template.id);
                Skill curSkill = SkillUtil.getSkillByItemID(pl, item.template.id);
                if (curSkill.point == 6) {
                    Service.gI().sendThongBao(pl, "Kỹ năng đã đạt tối đa!");
                } else {
                    if (curSkill.point == 0) {
                        if (level == 1) {
                            curSkill = SkillUtil.createSkill(SkillUtil.getTempSkillSkillByItemID(item.template.id),
                                    level);
                            SkillUtil.setSkill(pl, curSkill);
                            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                            msg = Service.gI().messageSubCommand((byte) 23);
                            msg.writer().writeShort(curSkill.skillId);
                            pl.sendMessage(msg);
                            msg.cleanup();
                            SkillService.gI().learSkillSpecial(pl, (byte) 30);
                        } else {
                            Skill skillNeed = SkillUtil
                                    .createSkill(SkillUtil.getTempSkillSkillByItemID(item.template.id), level);
                            if (level > 1) {
                                Item itemNew = ItemService.gI().createNewItem((short) (item.template.id - 1));
                                String name = itemNew.template.name;
                                String desiredName = name.substring(5);
                                Service.gI().sendThongBao(pl, "Vui lòng học " + desiredName + " trước!");
                            } else {
                                Service.gI().sendThongBao(pl, "Vui lòng học " + skillNeed.template.name + " trước!");
                            }
                        }
                    } else {
                        if (curSkill.point + 1 == level) {
                            curSkill = SkillUtil.createSkill(SkillUtil.getTempSkillSkillByItemID(item.template.id),
                                    level);
                            SkillUtil.setSkill(pl, curSkill);
                            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                            msg = Service.gI().messageSubCommand((byte) 62);
                            msg.writer().writeShort(curSkill.skillId);
                            pl.sendMessage(msg);
                            msg.cleanup();
                        } else {
                            if (level > 1) {
                                Item itemNew = ItemService.gI().createNewItem((short) (item.template.id - 1));
                                String name = itemNew.template.name;
                                String desiredName = name.substring(5);
                                Service.gI().sendThongBao(pl, "Vui lòng học " + desiredName + " trước!");
                            } else {
                                Service.gI().sendThongBao(pl, "Vui lòng học " + curSkill.template.name + " trước!");
                            }
                        }
                    }
                    InventoryService.gI().sendItemBag(pl);
                }
            } else {
                Service.gI().sendThongBao(pl, "Không thể thực hiện");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void useTDLT(Player pl, Item item) {
        if (pl.itemTime.isUseTDLT) {
            ItemTimeService.gI().turnOffTDLT(pl, item);
        } else {
            ItemTimeService.gI().turnOnTDLT(pl, item);
        }
    }

    // private void usePorataGogeta(Player pl) {
    // if (pl.pet == null || pl.fusion.typeFusion == 4) {
    // Service.gI().sendThongBao(pl, "Không thể thực hiện");
    // } else {
    // if (pl.fusion.typeFusion == ConstPlayer.NON_FUSION) {
    // pl.pet.fusionGogeta(true);
    // } else {
    // pl.pet.unFusion();
    // }
    // }
    // }
    private void usePorata2(Player pl) {
        if (pl.pet == null || pl.fusion.typeFusion == 4) {
            Service.gI().sendThongBao(pl, "Không thể thực hiện");
        } else {
            if (pl.fusion.typeFusion == ConstPlayer.NON_FUSION) {
                pl.pet.fusion2(true);
            } else {
                pl.pet.unFusion();
            }
        }
    }

    private void usePorata3(Player pl) {
        if (pl.pet == null || pl.fusion.typeFusion == 4) {
            Service.gI().sendThongBao(pl, "Không thể thực hiện");
        } else {
            if (pl.fusion.typeFusion == ConstPlayer.NON_FUSION) {
                pl.pet.fusion3(true);
            } else {
                pl.pet.unFusion();
            }
        }
    }

    private void usePorata(Player pl) {
        if (pl.pet == null || pl.fusion.typeFusion == 4) {
            Service.gI().sendThongBao(pl, "Không thể thực hiện");
        } else {
            if (pl.fusion.typeFusion == ConstPlayer.NON_FUSION) {
                pl.pet.fusion(true);
            } else {
                pl.pet.unFusion();
            }
        }
    }

    private void openCapsuleUI(Player pl) {
        pl.iDMark.setTypeChangeMap(ConstMap.CHANGE_CAPSULE);
        ChangeMapService.gI().openChangeMapTab(pl);
    }

    private void openxukrai(Player pl, Item item, int soluong) {
        if (pl == null || item == null) {
            System.out.println("Lỗi: Người chơi hoặc vật phẩm không hợp lệ." + pl + "");
            return;
        }
        if (soluong <= 0) {
            System.out.println("Lỗi: Số lượng xu ocean không hợp lệ.");
            return;
        }
        if (InventoryService.gI().getCountEmptyBag(pl) < 1) {
            Service.gI().sendThongBao(pl, "Cần 1 ô hành trang mới có thể đổi!!!");
            return;
        }
        Item xukrai = ItemService.gI().createNewItem((short) 1705, soluong);
        xukrai.itemOptions.add(new Item.ItemOption(30, 1));
        InventoryService.gI().addItemBag(pl, xukrai);
        InventoryService.gI().subQuantityItemsBag(pl, item, 1);
        Service.gI().sendThongBao(pl, "Bạn đã nhận được " + soluong + " xu ocean");

    }

    public void openRuongNgocRong(Player pl, Item item) {
        int nr = Util.nextInt(14, 20);
        int[] vp = {220, 221, 222, 223, 224, 225};
        int[] vpVip = {1682, 1683, 1688, 1689, 1677, 1678, 1013, 1021, 1022};
        Item item2 = null;
        if (Util.isTrue(80, 100)) {
            item2 = ItemService.gI().createNewItem((short) nr);
            item2.quantity = 1;
            item2.itemOptions.add(new Item.ItemOption(30, 1));
        } else if (Util.isTrue(80, 100)) {
            item2 = ItemService.gI().createNewItem((short) vp[Util.nextInt(0, vp.length - 1)]);
            item2.quantity = 1;
            item2.itemOptions.add(new Item.ItemOption(30, 1));
        } else {
            item2 = ItemService.gI().createNewItem((short) vpVip[Util.nextInt(0, vpVip.length - 1)]);
            item2.quantity = 1;
            item2.itemOptions.add(new Item.ItemOption(93, 30));
            item2.itemOptions.add(new Item.ItemOption(50, 10));
            item2.itemOptions.add(new Item.ItemOption(77, 10));
            item2.itemOptions.add(new Item.ItemOption(103, 10));
            item2.itemOptions.add(new Item.ItemOption(101, 10));

        }
        InventoryService.gI().addItemBag(pl, item2);
        InventoryService.gI().sendItemBag(pl);
        InventoryService.gI().subQuantityItemsBag(pl, item, 1);
        Service.gI().sendThongBao(pl, "Bạn mở rương nhận được " + item2.template.name);
    }

    public void ruongnro(Player pl, Item item) {
        int nr = Util.nextInt(14, 20);
        Item item2 = null;
        item2 = ItemService.gI().createNewItem((short) nr);
        item2.itemOptions.add(new Item.ItemOption(30, 1));
        InventoryService.gI().addItemBag(pl, item2);
        InventoryService.gI().sendItemBag(pl);
        InventoryService.gI().subQuantityItemsBag(pl, item, 1);
        Service.gI().sendThongBao(pl, "Bạn mở rương nhận được " + item2.template.name);
    }

    public void ComfirmNhanVIP(Player player, boolean receivedPet) {
        if (InventoryService.gI().getCountEmptyBag(player) > 5) {
            Item itemqua;
            Item itemqua1;
            Item itemqua2;
            Item itemqua3;
            try {
                int time = 5;

                // Kiểm tra nếu người chơi đã nhận quà VIP 1 hoặc VIP 2 rồi
//                if (player.getSession().hasReceivedVIP) {
//                    Service.gI().sendThongBao(player, "Bạn Đã Sở Hữu VIP Rồi!");
//                    return;
//                }
                if (player.getSession().hasReceivedVIPLevel(1)) {
                    Service.gI().sendThongBao(player, "Bạn Đã Nhận VIP 1 Rồi!");
                    return;
                }

                // Nếu đủ 150 gem, tiến hành VIP 1
                if (player.getSession().cash >= 20000) {
                    Service.gI().sendThongBao(player, "Tiến Hành Nhận\nVIP 1 \nSau " + time + " Giây!");
                    while (time > 0) {
                        time--;
                        Thread.sleep(1000);
                        Service.gI().sendThongBao(player, "|7|" + time);
                    }

                    // Đánh dấu đã nhận VIP 1
                    PlayerDAO.subcash(player, 20000);
                    player.inventory.gold += 500_000_000;
//                    player.getSession().vip = 1;
                    PlayerDAO.subVIP(player, 1);
                    player.getSession().setReceivedVIPLevel(1, true);
                    Service.gI().sendMoney(player);
                    itemqua = ItemService.gI().createNewItem((short) 459, 1); //phieu giam gia
                    itemqua.itemOptions.add(new ItemOption(112, 80));
                    itemqua.itemOptions.add(new ItemOption(93, 90));
                    itemqua.itemOptions.add(new ItemOption(30, 0));

                    Item itemGold = ItemService.gI().createNewItem((short) 457, 70);
                    Item itemGold1 = ItemService.gI().createNewItem((short) 987, 30); //đá bảo vệ
                    Item itemXiMuoi = ItemService.gI().createNewItem((short) 1195, 5); //xí muội tăng 20% sd
                    Item itemXiMuoi2 = ItemService.gI().createNewItem((short) 1196, 5); //xí muội tăng 20% hp ki
                    //    Item itemSkillDe = ItemService.gI().createNewItem((short) 1753, 3); //hộp skill đệ
                    Item itemThe = ItemService.gI().createNewItem((short) 956, 20); // manh doi truong vang
                    Item itemThe1 = ItemService.gI().createNewItem((short) 1204, 20);
                    Item itemSpl = ItemService.gI().createNewItem((short) 447, 15); //spl tnsm
                    itemSpl.itemOptions.add(new ItemOption(101, 5));
                    itemSpl.itemOptions.add(new ItemOption(30, 1));

//                    itemqua3 = ItemService.gI().createNewItem((short) 1504, 1);
//                    itemqua3.itemOptions.add(new ItemOption(50, Util.nextInt(25, 30)));
//                    itemqua3.itemOptions.add(new ItemOption(77, 24));
//                    itemqua3.itemOptions.add(new ItemOption(103, 24));
//                    itemqua3.itemOptions.add(new ItemOption(5, 15));
//                    itemqua3.itemOptions.add(new ItemOption(14, 10));
//                    itemqua3.itemOptions.add(new ItemOption(94, 7));
//                    itemqua3.itemOptions.add(new ItemOption(93, 3));
//                    itemqua3.itemOptions.add(new ItemOption(30, 0));
//                    itemqua2 = ItemService.gI().createNewItem((short) 1550, 1);
//                    itemqua2.itemOptions.add(new ItemOption(50, 13));
//                    itemqua2.itemOptions.add(new ItemOption(77, 13));
//                    itemqua2.itemOptions.add(new ItemOption(103, 13));
//                    itemqua2.itemOptions.add(new ItemOption(5, 9));
//                    itemqua2.itemOptions.add(new ItemOption(14, 5));
//                    itemqua2.itemOptions.add(new ItemOption(94, 4));
//                    itemqua3.itemOptions.add(new ItemOption(93, 3));
//                    itemqua2.itemOptions.add(new ItemOption(30, 0));
//                    if (receivedPet) {
//                        PetService.gI().createNormalPet(player);
//                    }
                    switch (player.gender) {
                        case 0:
                            itemqua1 = ItemService.gI().createNewItem((short) 227);
                            itemqua1.itemOptions.add(new ItemOption(50, Util.nextInt(15, 25)));
                            itemqua1.itemOptions.add(new ItemOption(77, Util.nextInt(5, 15)));
                            itemqua1.itemOptions.add(new ItemOption(103, Util.nextInt(5, 15)));
                            itemqua1.itemOptions.add(new ItemOption(97, Util.nextInt(5, 15)));
                            itemqua1.itemOptions.add(new ItemOption(14, Util.nextInt(1, 10)));
                            break;
                        case 1:
                            itemqua1 = ItemService.gI().createNewItem((short) 228);
                            itemqua1.itemOptions.add(new ItemOption(50, Util.nextInt(15, 25)));
                            itemqua1.itemOptions.add(new ItemOption(77, Util.nextInt(5, 15)));
                            itemqua1.itemOptions.add(new ItemOption(103, Util.nextInt(5, 15)));
                            itemqua1.itemOptions.add(new ItemOption(97, Util.nextInt(5, 15)));
                            itemqua1.itemOptions.add(new ItemOption(14, Util.nextInt(1, 10)));
                            break;
                        case 2:
                            itemqua1 = ItemService.gI().createNewItem((short) 229);
                            itemqua1.itemOptions.add(new ItemOption(50, Util.nextInt(15, 25)));
                            itemqua1.itemOptions.add(new ItemOption(77, Util.nextInt(5, 15)));
                            itemqua1.itemOptions.add(new ItemOption(103, Util.nextInt(5, 15)));
                            itemqua1.itemOptions.add(new ItemOption(97, Util.nextInt(5, 15)));
                            itemqua1.itemOptions.add(new ItemOption(14, Util.nextInt(1, 10)));
                            break;
                        default:
                            Service.gI().sendThongBao(player, "Giới tính không hợp lệ, không thể nhận quà!");
                            return;
                    }

                    Service.gI().sendThongBao(player, "Bạn vừa nhận được 500Tr vàng");
                    Service.gI().sendThongBao(player, "Bạn Đã Nhận Được " + itemqua.template.name);
                    Service.gI().sendThongBao(player, "Bạn Đã Nhận Được " + itemGold.template.name);
                    Service.gI().sendThongBao(player, "Bạn Đã Nhận Được " + itemGold1.template.name);
                    Service.gI().sendThongBao(player, "Bạn Đã Nhận Được " + itemqua1.template.name);
                    Service.gI().sendThongBao(player, "Bạn Đã Nhận Được " + itemSpl.template.name);
                    Service.gI().sendThongBao(player, "Bạn Đã Nhận Được " + itemXiMuoi.template.name);
                    //          Service.gI().sendThongBao(player, "Bạn Đã Nhận Được " + itemSkillDe.template.name);
                    Service.gI().sendThongBao(player, "Bạn Đã Nhận Được " + itemThe.template.name);
                    Service.gI().sendThongBao(player, "Bạn Đã Nhận Được " + itemThe1.template.name);
                    Service.gI().sendThongBao(player, "Bạn Đã Nhận Được " + itemXiMuoi2.template.name);
//                    Service.gI().sendThongBao(player, "Bạn vừa nhận được đệ tử");

                    InventoryService.gI().addItemBag(player, itemqua);
                    InventoryService.gI().addItemBag(player, itemqua1);
                    InventoryService.gI().addItemBag(player, itemSpl);
                    InventoryService.gI().addItemBag(player, itemGold1);
                    InventoryService.gI().addItemBag(player, itemGold);
                    InventoryService.gI().addItemBag(player, itemXiMuoi);
                    InventoryService.gI().addItemBag(player, itemThe);
                    InventoryService.gI().addItemBag(player, itemThe1);
                    //      InventoryService.gI().addItemBag(player, itemSkillDe);
                    InventoryService.gI().addItemBag(player, itemXiMuoi2);
                    InventoryService.gI().sendItemBag(player);
                } else {
                    Service.gI().sendThongBao(player, "Bạn Chưa Đủ Coin Để Nhận Vip Nạp Này!");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Service.gI().sendThongBao(player, "Bạn phải có ít nhất 6 ô trống hành trang");
        }
    }

    public void ComfirmNhanVIP2(Player player, boolean receivedPet) {
        if (InventoryService.gI().getCountEmptyBag(player) > 7) {
            Item itemqua;
            Item itemqua1;
            Item itemqua2;
            Item itemqua3;
            try {
                int time = 5;
//                if (player.getSession().hasReceivedVIP) {
//                    Service.gI().sendThongBao(player, "Bạn Đã Sở Hữu VIP Rồi!");
//                    return;
//                }
                if (player.getSession().hasReceivedVIPLevel(2)) {
                    Service.gI().sendThongBao(player, "Bạn Đã Nhận VIP 2 Rồi!");
                    return;
                }

                if (player.getSession().cash >= 50000) {
                    Service.gI().sendThongBao(player, "Tiến Hành Nhận\nVIP 2 \nSau " + time + " Giây!");
                    while (time > 0) {
                        time--;
                        Thread.sleep(1000);
                        Service.gI().sendThongBao(player, "|7|" + time);
                    }

                    PlayerDAO.subcash(player, 50000);
                    player.inventory.gold += 1_000_000_000;
//                    player.getSession().vip = 2;
                    PlayerDAO.subVIP(player, 2);
                    player.getSession().setReceivedVIPLevel(2, true);
                    Service.gI().sendMoney(player);
                    itemqua = ItemService.gI().createNewItem((short) 459, 3);
                    itemqua.itemOptions.add(new ItemOption(112, 80));
                    itemqua.itemOptions.add(new ItemOption(93, 90));
                    itemqua.itemOptions.add(new ItemOption(30, 0));
                    Item itemGold = ItemService.gI().createNewItem((short) 457, 150);
                    Item itemGold1 = ItemService.gI().createNewItem((short) 987, 50); //đá bảo vệ
                    Item itemXiMuoi = ItemService.gI().createNewItem((short) 1195, 10); //xí muội tăng 20% sd
                    Item itemXiMuoi2 = ItemService.gI().createNewItem((short) 1196, 10); //xí muội tăng 20% hp ki
                    //  Item itemSkillDe = ItemService.gI().createNewItem((short) 1753, 5); //hộp skill đệ
                    Item itemThe = ItemService.gI().createNewItem((short) 956, 30); // manh doi truong vang
                    Item itemThe1 = ItemService.gI().createNewItem((short) 1204, 30);
                    Item itemSpl = ItemService.gI().createNewItem((short) 447, 15); //spl tnsm
                    itemSpl.itemOptions.add(new ItemOption(101, 5));
                    itemSpl.itemOptions.add(new ItemOption(30, 1));

//                    if (receivedPet) {
//                        PetService.gI().createNormalPet(player);
//                    }
                    itemqua3 = ItemService.gI().createNewItem((short) 1504, 1);
                    itemqua3.itemOptions.add(new ItemOption(50, Util.nextInt(20, 25)));
                    itemqua3.itemOptions.add(new ItemOption(77, Util.nextInt(20, 25)));
                    itemqua3.itemOptions.add(new ItemOption(103, Util.nextInt(20, 25)));
//                    itemqua3.itemOptions.add(new ItemOption(5, 5));
                    itemqua3.itemOptions.add(new ItemOption(14, 10));
                    itemqua3.itemOptions.add(new ItemOption(94, 7));
                    itemqua3.itemOptions.add(new ItemOption(93, 14));
                    itemqua3.itemOptions.add(new ItemOption(30, 0));
//                    itemqua2 = ItemService.gI().createNewItem((short) 1722, 1);
//                    itemqua2.itemOptions.add(new ItemOption(50, 13));
//                    itemqua2.itemOptions.add(new ItemOption(77, 13));
//                    itemqua2.itemOptions.add(new ItemOption(103, 13));
//                    itemqua2.itemOptions.add(new ItemOption(5, 9));
//                    itemqua2.itemOptions.add(new ItemOption(14, 5));
//                    itemqua2.itemOptions.add(new ItemOption(94, 4));
//                    itemqua2.itemOptions.add(new ItemOption(93, 14));
//                    itemqua2.itemOptions.add(new ItemOption(30, 0));

                    switch (player.gender) {
                        case 0:
                            itemqua1 = ItemService.gI().createNewItem((short) 227);
                            itemqua1.itemOptions.add(new ItemOption(50, Util.nextInt(15, 25)));
                            itemqua1.itemOptions.add(new ItemOption(77, Util.nextInt(5, 15)));
                            itemqua1.itemOptions.add(new ItemOption(103, Util.nextInt(5, 15)));
                            itemqua1.itemOptions.add(new ItemOption(97, Util.nextInt(5, 15)));
                            itemqua1.itemOptions.add(new ItemOption(14, Util.nextInt(1, 10)));
                            break;
                        case 1:
                            itemqua1 = ItemService.gI().createNewItem((short) 228);
                            itemqua1.itemOptions.add(new ItemOption(50, Util.nextInt(15, 25)));
                            itemqua1.itemOptions.add(new ItemOption(77, Util.nextInt(5, 15)));
                            itemqua1.itemOptions.add(new ItemOption(103, Util.nextInt(5, 15)));
                            itemqua1.itemOptions.add(new ItemOption(97, Util.nextInt(5, 15)));
                            itemqua1.itemOptions.add(new ItemOption(14, Util.nextInt(1, 10)));
                            break;
                        case 2:
                            itemqua1 = ItemService.gI().createNewItem((short) 229);
                            itemqua1.itemOptions.add(new ItemOption(50, Util.nextInt(15, 25)));
                            itemqua1.itemOptions.add(new ItemOption(77, Util.nextInt(5, 15)));
                            itemqua1.itemOptions.add(new ItemOption(103, Util.nextInt(5, 15)));
                            itemqua1.itemOptions.add(new ItemOption(97, Util.nextInt(5, 15)));
                            itemqua1.itemOptions.add(new ItemOption(14, Util.nextInt(1, 10)));
                            break;
                        default:
                            Service.gI().sendThongBao(player, "Giới tính không hợp lệ, không thể nhận quà!");
                            return;
                    }

                    Service.gI().sendThongBao(player, "Bạn vừa nhận được 1 Tỉ vàng");
                    Service.gI().sendThongBao(player, "Bạn Đã Nhận Được " + itemqua.template.name);
                    Service.gI().sendThongBao(player, "Bạn Đã Nhận Được " + itemqua1.template.name);
//                    Service.gI().sendThongBao(player, "Bạn Đã Nhận Được " + itemqua2.template.name);
                    Service.gI().sendThongBao(player, "Bạn Đã Nhận Được " + itemGold.template.name);
                    Service.gI().sendThongBao(player, "Bạn Đã Nhận Được " + itemGold1.template.name);
                    Service.gI().sendThongBao(player, "Bạn Đã Nhận Được " + itemXiMuoi.template.name);
                    //     Service.gI().sendThongBao(player, "Bạn Đã Nhận Được " + itemSkillDe.template.name);
                    Service.gI().sendThongBao(player, "Bạn Đã Nhận Được " + itemqua3.template.name);
                    Service.gI().sendThongBao(player, "Bạn Đã Nhận Được " + itemThe.template.name);
                    Service.gI().sendThongBao(player, "Bạn Đã Nhận Được " + itemThe1.template.name);
                    Service.gI().sendThongBao(player, "Bạn Đã Nhận Được " + itemSpl.template.name);
                    Service.gI().sendThongBao(player, "Bạn Đã Nhận Được " + itemXiMuoi2.template.name);
//                    Service.gI().sendThongBao(player, "Bạn vừa nhận được đệ tử");

                    InventoryService.gI().addItemBag(player, itemqua);
                    InventoryService.gI().addItemBag(player, itemqua1);
//                    InventoryService.gI().addItemBag(player, itemqua2);
                    InventoryService.gI().addItemBag(player, itemGold);
                    InventoryService.gI().addItemBag(player, itemGold1);
                    InventoryService.gI().addItemBag(player, itemXiMuoi);
                    //   InventoryService.gI().addItemBag(player, itemSkillDe);
                    InventoryService.gI().addItemBag(player, itemThe);
                    InventoryService.gI().addItemBag(player, itemThe1);
                    InventoryService.gI().addItemBag(player, itemqua3);
                    InventoryService.gI().addItemBag(player, itemSpl);
                    InventoryService.gI().addItemBag(player, itemXiMuoi2);
                    InventoryService.gI().sendItemBag(player);
                } else {
                    Service.gI().sendThongBao(player, "Bạn Chưa Đủ Coin Để Nhận Vip Này!");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Service.gI().sendThongBao(player, "Bạn phải có ít nhất 8 ô trống hành trang");
        }
    }

    public void ComfirmNhanVIP3(Player player, boolean receivedPet) {
        if (InventoryService.gI().getCountEmptyBag(player) > 11) {
            Item itemqua;
            Item itemqua1;
            Item itemqua2;
            Item itemqua3;
            Item itemqua4;
            Item itemqua5;
            try {
                int time = 5;
//                if (player.getSession().hasReceivedVIP) {
//                    Service.gI().sendThongBao(player, "Bạn Đã Sở Hữu VIP Rồi!");
//                    return;
//                }
                if (player.getSession().hasReceivedVIPLevel(3)) {
                    Service.gI().sendThongBao(player, "Bạn Đã Nhận VIP 3 Rồi!");
                    return;
                }

                if (player.getSession().cash >= 150000) {
                    Service.gI().sendThongBao(player, "Tiến Hành Nhận\nVIP 3 \nSau " + time + " Giây!");
                    while (time > 0) {
                        time--;
                        Thread.sleep(1000);
                        Service.gI().sendThongBao(player, "|7|" + time);
                    }

                    PlayerDAO.subcash(player, 150000);
                    player.inventory.gold += 1_500_000_000;
//                    player.getSession().vip = 3;
                    PlayerDAO.subVIP(player, 3);
                    player.getSession().setReceivedVIPLevel(3, true);
//                    player.effect.addPointBiMocSachTui();
                    Service.gI().sendMoney(player);
                    itemqua = ItemService.gI().createNewItem((short) 459, 3);
                    itemqua.itemOptions.add(new ItemOption(112, 80));
                    itemqua.itemOptions.add(new ItemOption(93, 90));
                    itemqua.itemOptions.add(new ItemOption(30, 0));
                    Item itemGold = ItemService.gI().createNewItem((short) 457, 350);
                    Item itemXu = ItemService.gI().createNewItem((short) 1705, 250); //Xu
                    Item itemGold1 = ItemService.gI().createNewItem((short) 987, 50); //đá bảo vệ
                    Item itemXiMuoi = ItemService.gI().createNewItem((short) 1195, 15); //xí muội tăng 20% sd
                    Item itemXiMuoi2 = ItemService.gI().createNewItem((short) 1196, 15); //xí muội tăng 20% hp ki
                    //   Item itemSkillDe = ItemService.gI().createNewItem((short) 1753, 10); //hộp skill đệ
                    Item itemThe = ItemService.gI().createNewItem((short) 956, 50); // manh doi truong vang
                    Item itemThe1 = ItemService.gI().createNewItem((short) 1204, 50);
                    Item itemSpl = ItemService.gI().createNewItem((short) 447, 15); //spl tnsm
                    itemSpl.itemOptions.add(new ItemOption(101, 5));
                    itemSpl.itemOptions.add(new ItemOption(30, 1));

//                    if (receivedPet) {
//                        PetService.gI().createNormalPet(player);
//                    }
                    //  itemqua2 = ItemService.gI().createNewItem((short) 956, 50);
//                    itemqua2 = ItemService.gI().createNewItem((short) 710);
//                    itemqua2.itemOptions.add(new ItemOption(50, 22));
//                    itemqua2.itemOptions.add(new ItemOption(77, 20));
//                    itemqua2.itemOptions.add(new ItemOption(103, 20));
//                    itemqua2.itemOptions.add(new ItemOption(159, 3));
//                    itemqua2.itemOptions.add(new ItemOption(160, 35));
//                    itemqua2.itemOptions.add(new ItemOption(30, 0));
                    itemqua3 = ItemService.gI().createNewItem((short) 742, 1);
                    itemqua3.itemOptions.add(new ItemOption(50, Util.nextInt(25, 29)));
                    itemqua3.itemOptions.add(new ItemOption(77, Util.nextInt(25, 29)));
                    itemqua3.itemOptions.add(new ItemOption(103, Util.nextInt(25, 29)));
//                    itemqua3.itemOptions.add(new ItemOption(5, Util.nextInt(3, 10)));
                    itemqua3.itemOptions.add(new ItemOption(14, 10));
                    itemqua3.itemOptions.add(new ItemOption(94, 7));
                    itemqua3.itemOptions.add(new ItemOption(93, 21));
                    itemqua3.itemOptions.add(new ItemOption(30, 0));

                    itemqua4 = ItemService.gI().createNewItem((short) 1318, 1);
                    itemqua4.itemOptions.add(new ItemOption(50, Util.nextInt(5, 9)));
                    itemqua4.itemOptions.add(new ItemOption(77, Util.nextInt(5, 9)));
                    itemqua4.itemOptions.add(new ItemOption(103, Util.nextInt(5, 9)));
//                    itemqua4.itemOptions.add(new ItemOption(5, 9));
                    itemqua4.itemOptions.add(new ItemOption(14, 5));
                    itemqua4.itemOptions.add(new ItemOption(94, 4));
                    itemqua4.itemOptions.add(new ItemOption(93, 21));
                    itemqua4.itemOptions.add(new ItemOption(30, 0));
                    itemqua4.itemOptions.add(new ItemOption(93, 15));
//                    itemqua4 = ItemService.gI().createNewItem((short) 1625, 1);
//                    itemqua4.itemOptions.add(new ItemOption(50, 9));
//                    itemqua4.itemOptions.add(new ItemOption(77, 9));
//                    itemqua4.itemOptions.add(new ItemOption(103, 9));
//                    itemqua4.itemOptions.add(new ItemOption(5, 7));
//                    itemqua4.itemOptions.add(new ItemOption(93, 21));
//                    itemqua4.itemOptions.add(new ItemOption(30, 0));
                    switch (player.gender) {
                        case 0:
                            itemqua1 = ItemService.gI().createNewItem((short) 227);
                            itemqua1.itemOptions.add(new ItemOption(50, Util.nextInt(15, 25)));
                            itemqua1.itemOptions.add(new ItemOption(77, Util.nextInt(5, 15)));
                            itemqua1.itemOptions.add(new ItemOption(103, Util.nextInt(5, 15)));
                            itemqua1.itemOptions.add(new ItemOption(97, Util.nextInt(5, 15)));
                            itemqua1.itemOptions.add(new ItemOption(14, Util.nextInt(1, 10)));
                            break;
                        case 1:
                            itemqua1 = ItemService.gI().createNewItem((short) 228);
                            itemqua1.itemOptions.add(new ItemOption(50, Util.nextInt(15, 25)));
                            itemqua1.itemOptions.add(new ItemOption(77, Util.nextInt(5, 15)));
                            itemqua1.itemOptions.add(new ItemOption(103, Util.nextInt(5, 15)));
                            itemqua1.itemOptions.add(new ItemOption(97, Util.nextInt(5, 15)));
                            itemqua1.itemOptions.add(new ItemOption(14, Util.nextInt(1, 10)));
                            break;
                        case 2:
                            itemqua1 = ItemService.gI().createNewItem((short) 229);
                            itemqua1.itemOptions.add(new ItemOption(50, Util.nextInt(15, 25)));
                            itemqua1.itemOptions.add(new ItemOption(77, Util.nextInt(5, 15)));
                            itemqua1.itemOptions.add(new ItemOption(103, Util.nextInt(5, 15)));
                            itemqua1.itemOptions.add(new ItemOption(97, Util.nextInt(5, 15)));
                            itemqua1.itemOptions.add(new ItemOption(14, Util.nextInt(1, 10)));
                            break;
                        default:
                            Service.gI().sendThongBao(player, "Giới tính không hợp lệ, không thể nhận quà!");
                            return;
                    }

                    Service.gI().sendThongBao(player, "Bạn vừa nhận được 1 Tỉ 5 vàng");
                    Service.gI().sendThongBao(player, "Bạn Đã Nhận Được " + itemqua.template.name);
                    Service.gI().sendThongBao(player, "Bạn Đã Nhận Được " + itemqua1.template.name);
                    //      Service.gI().sendThongBao(player, "Bạn Đã Nhận Được " + itemqua2.template.name);
                    Service.gI().sendThongBao(player, "Bạn Đã Nhận Được " + itemGold.template.name);
                    Service.gI().sendThongBao(player, "Bạn Đã Nhận Được " + itemGold1.template.name);
                    Service.gI().sendThongBao(player, "Bạn Đã Nhận Được " + itemXiMuoi.template.name);
                    //    Service.gI().sendThongBao(player, "Bạn Đã Nhận Được " + itemSkillDe.template.name);
                    Service.gI().sendThongBao(player, "Bạn Đã Nhận Được " + itemXu.template.name);
                    Service.gI().sendThongBao(player, "Bạn Đã Nhận Được " + itemqua3.template.name);
                    Service.gI().sendThongBao(player, "Bạn Đã Nhận Được " + itemqua4.template.name);
                    Service.gI().sendThongBao(player, "Bạn Đã Nhận Được " + itemThe.template.name);
                    Service.gI().sendThongBao(player, "Bạn Đã Nhận Được " + itemThe1.template.name);
                    Service.gI().sendThongBao(player, "Bạn Đã Nhận Được " + itemXiMuoi2.template.name);
//                    Service.gI().sendThongBao(player, "Bạn vừa nhận được đệ tử");

                    InventoryService.gI().addItemBag(player, itemqua);
                    InventoryService.gI().addItemBag(player, itemqua1);
                    InventoryService.gI().addItemBag(player, itemXu);
                    InventoryService.gI().addItemBag(player, itemGold);
                    InventoryService.gI().addItemBag(player, itemGold1);
                    InventoryService.gI().addItemBag(player, itemXiMuoi);
                    //       InventoryService.gI().addItemBag(player, itemSkillDe);
                    InventoryService.gI().addItemBag(player, itemThe);
                    InventoryService.gI().addItemBag(player, itemThe1);
                    InventoryService.gI().addItemBag(player, itemqua3);
                    InventoryService.gI().addItemBag(player, itemqua4);
                    InventoryService.gI().addItemBag(player, itemXiMuoi2);
                    InventoryService.gI().sendItemBag(player);
                } else {
                    Service.gI().sendThongBao(player, "Bạn Chưa Đủ Coin Để Nhận Vip Này!");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Service.gI().sendThongBao(player, "Bạn phải có ít nhất 11 ô trống hành trang");
        }
    }

//    public void ComfirmNhanVIP4(Player player, boolean receivedPet) {
//        if (InventoryService.gI().getCountEmptyBag(player) > 10) {
//            Item itemqua;
//            Item itemqua1;
//            Item itemqua2;
//            Item itemqua3;
//            Item itemqua4;
//            Item itemqua5;
//            try {
//                int time = 5;
////                if (player.getSession().hasReceivedVIP) {
////                    Service.gI().sendThongBao(player, "Bạn Đã Sở Hữu VIP Rồi!");
////                    return;
////                }
//                if (player.getSession().hasReceivedVIPLevel(4)) {
//                    Service.gI().sendThongBao(player, "Bạn Đã Nhận VIP 4 Rồi!");
//                    return;
//                }
//
//                if (player.getSession().cash >= 400000) { // Nếu đủ 350 gem, tiến hành VIP 2
//                    Service.gI().sendThongBao(player, "Tiến Hành Nhận\nVIP 4 \nSau " + time + " Giây!");
//                    while (time > 0) {
//                        time--;
//                        Thread.sleep(1000);
//                        Service.gI().sendThongBao(player, "|7|" + time);
//                    }
//
//                    player.getSession().setReceivedVIPLevel(4, true); // đánh dấu đã nhận VIP 1
//                    PlayerDAO.subcash(player, 400000);
//                    player.inventory.gold += 5_500_000_000L;
////                    player.getSession().vip = 4;
//                    PlayerDAO.subVIP(player, 4);
////                    player.effect.addPointBiMocSachTui();
//                    Service.gI().sendMoney(player);
//                    itemqua = ItemService.gI().createNewItem((short) 459, 5);
//                    itemqua.itemOptions.add(new ItemOption(112, 80));
//                    itemqua.itemOptions.add(new ItemOption(93, 90));
//                    itemqua.itemOptions.add(new ItemOption(30, 0));
//                    Item itemGold = ItemService.gI().createNewItem((short) 457, 129);
//                    Item itemGold1 = ItemService.gI().createNewItem((short) 987, 500);
//                    Item itemGold2 = ItemService.gI().createNewItem((short) 1283, 200);
//                    Item itemGold3 = ItemService.gI().createNewItem((short) 956, 150);
//                    Item itemGold4 = ItemService.gI().createNewItem((short) 1655, 10);
//                    if (receivedPet) {
//                        PetService.gI().createNormalPet(player);
//                    }
//                    //  itemqua2 = ItemService.gI().createNewItem((short) 956, 50);
//                    itemqua2 = ItemService.gI().createNewItem((short) 1630);
//                    itemqua2.itemOptions.add(new ItemOption(50, 14));
//                    itemqua2.itemOptions.add(new ItemOption(77, 14));
//                    itemqua2.itemOptions.add(new ItemOption(103, 14));
//                    itemqua2.itemOptions.add(new ItemOption(5, 9));
//                    itemqua2.itemOptions.add(new ItemOption(94, 7));
//                    itemqua2.itemOptions.add(new ItemOption(30, 0));
//                    itemqua3 = ItemService.gI().createNewItem((short) 1784, 1);
//                    itemqua3.itemOptions.add(new ItemOption(50, 25));
//                    itemqua3.itemOptions.add(new ItemOption(77, 24));
//                    itemqua3.itemOptions.add(new ItemOption(103, 24));
//                    itemqua3.itemOptions.add(new ItemOption(5, 15));
//                    itemqua3.itemOptions.add(new ItemOption(14, 10));
//                    itemqua3.itemOptions.add(new ItemOption(94, 7));
//                    itemqua3.itemOptions.add(new ItemOption(93, 21));
//                    itemqua3.itemOptions.add(new ItemOption(30, 0));
//                    itemqua4 = ItemService.gI().createNewItem((short) 1722, 1);
//                    itemqua4.itemOptions.add(new ItemOption(50, 13));
//                    itemqua4.itemOptions.add(new ItemOption(77, 13));
//                    itemqua4.itemOptions.add(new ItemOption(103, 13));
//                    itemqua4.itemOptions.add(new ItemOption(5, 9));
//                    itemqua4.itemOptions.add(new ItemOption(14, 5));
//                    itemqua4.itemOptions.add(new ItemOption(94, 4));
//                    itemqua4.itemOptions.add(new ItemOption(93, 21));
//                    itemqua4.itemOptions.add(new ItemOption(30, 0));
//                    itemqua4 = ItemService.gI().createNewItem((short) 1625, 1);
//                    itemqua4.itemOptions.add(new ItemOption(50, 9));
//                    itemqua4.itemOptions.add(new ItemOption(77, 9));
//                    itemqua4.itemOptions.add(new ItemOption(103, 9));
//                    itemqua4.itemOptions.add(new ItemOption(5, 7));
//                    itemqua4.itemOptions.add(new ItemOption(93, 21));
//                    itemqua4.itemOptions.add(new ItemOption(30, 0));
//
//                    switch (player.gender) {
//                        case 0:
//                            itemqua1 = ItemService.gI().createNewItem((short) 227);
//                            itemqua1.itemOptions.add(new ItemOption(50, Util.nextInt(10, 25)));
//                            itemqua1.itemOptions.add(new ItemOption(77, Util.nextInt(5, 15)));
//                            itemqua1.itemOptions.add(new ItemOption(103, Util.nextInt(5, 15)));
//                            itemqua1.itemOptions.add(new ItemOption(97, Util.nextInt(5, 15)));
//                            itemqua1.itemOptions.add(new ItemOption(14, Util.nextInt(1, 10)));
//                            break;
//                        case 1:
//                            itemqua1 = ItemService.gI().createNewItem((short) 228);
//                            itemqua1.itemOptions.add(new ItemOption(50, Util.nextInt(10, 25)));
//                            itemqua1.itemOptions.add(new ItemOption(77, Util.nextInt(5, 15)));
//                            itemqua1.itemOptions.add(new ItemOption(103, Util.nextInt(5, 15)));
//                            itemqua1.itemOptions.add(new ItemOption(97, Util.nextInt(5, 15)));
//                            itemqua1.itemOptions.add(new ItemOption(14, Util.nextInt(1, 10)));
//                            break;
//                        case 2:
//                            itemqua1 = ItemService.gI().createNewItem((short) 229);
//                            itemqua1.itemOptions.add(new ItemOption(50, Util.nextInt(10, 25)));
//                            itemqua1.itemOptions.add(new ItemOption(77, Util.nextInt(5, 15)));
//                            itemqua1.itemOptions.add(new ItemOption(103, Util.nextInt(5, 15)));
//                            itemqua1.itemOptions.add(new ItemOption(97, Util.nextInt(5, 15)));
//                            itemqua1.itemOptions.add(new ItemOption(14, Util.nextInt(1, 10)));
//                            break;
//                        default:
//                            Service.gI().sendThongBao(player, "Giới tính không hợp lệ, không thể nhận quà!");
//                            return;
//                    }
//
//                    Service.gI().sendThongBao(player, "Bạn vừa nhận được 1 Tỉ 5 vàng");
//                    Service.gI().sendThongBao(player, "Bạn Đã Nhận Được " + itemqua.template.name);
//                    Service.gI().sendThongBao(player, "Bạn Đã Nhận Được " + itemqua1.template.name);
//                    Service.gI().sendThongBao(player, "Bạn Đã Nhận Được " + itemqua2.template.name);
//                    Service.gI().sendThongBao(player, "Bạn Đã Nhận Được " + itemGold.template.name);
//                    Service.gI().sendThongBao(player, "Bạn Đã Nhận Được " + itemGold1.template.name);
//                    Service.gI().sendThongBao(player, "Bạn Đã Nhận Được " + itemGold2.template.name);
//                    Service.gI().sendThongBao(player, "Bạn Đã Nhận Được " + itemGold3.template.name);
//                    Service.gI().sendThongBao(player, "Bạn Đã Nhận Được " + itemGold4.template.name);
//                    Service.gI().sendThongBao(player, "Bạn Đã Nhận Được " + itemqua3.template.name);
//                    Service.gI().sendThongBao(player, "Bạn Đã Nhận Được " + itemqua4.template.name);
//                    Service.gI().sendThongBao(player, "Bạn vừa nhận được đệ tử");
//
//                    InventoryService.gI().addItemBag(player, itemqua);
//                    InventoryService.gI().addItemBag(player, itemqua1);
//                    InventoryService.gI().addItemBag(player, itemqua2);
//                    InventoryService.gI().addItemBag(player, itemGold);
//                    InventoryService.gI().addItemBag(player, itemGold1);
//                    InventoryService.gI().addItemBag(player, itemGold2);
//                    InventoryService.gI().addItemBag(player, itemGold3);
//                    InventoryService.gI().addItemBag(player, itemGold4);
//                    InventoryService.gI().addItemBag(player, itemqua3);
//                    InventoryService.gI().addItemBag(player, itemqua4);
//                    InventoryService.gI().sendItemBag(player);
//                } else {
//                    Service.gI().sendThongBao(player, "Bạn Chưa Đủ Coin Để Nhận Vip Này!");
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        } else {
//            Service.gI().sendThongBao(player, "Bạn phải có ít nhất 11 ô trống hành trang");
//        }
//    }
    public void choseMapCapsule(Player pl, int index) {
        if (ClanNamekWarService.gI().isLockedInRunningMatch(pl)) {
            Service.gI().sendThongBao(pl, "Không thể sử dụng Capsule trong trận Bảo Vệ Trưởng Lão Namek.");
            Service.gI().hideWaitDialog(pl);
            return;
        }
        if (pl.zone != null && MapService.gI().isMapPhoBan(pl.zone.map.mapId)) {
            Service.gI().sendThongBao(pl, "Không thể sử dụng Capsule trong bản đồ này");
            Service.gI().hideWaitDialog(pl);
            return;
        }

        if (pl.idNRNM != -1) {
            Service.gI().sendThongBao(pl, "Không thể mang ngọc rồng này lên Phi thuyền");
            Service.gI().hideWaitDialog(pl);
            return;
        }

        int zoneId = -1;
        if (index > pl.mapCapsule.size() - 1 || index < 0) {
            Service.gI().sendThongBao(pl, "Không thể thực hiện");
            Service.gI().hideWaitDialog(pl);
            return;
        }
        Zone zoneChose = pl.mapCapsule.get(index);
        // Kiểm tra số lượng người trong khu

        if (zoneChose.getNumOfPlayers() > 25
                || MapService.gI().isMapDoanhTrai(zoneChose.map.mapId)
                || MapService.gI().isMapMaBu(zoneChose.map.mapId)
                || MapService.gI().isMapHuyDiet(zoneChose.map.mapId)
                || MapService.gI().isMapPhoBan(zoneChose.map.mapId)) {
            Service.gI().sendThongBao(pl, "Hiện tại không thể vào được khu!");
            return;
        }
        if (index != 0 || zoneChose.map.mapId == 21
                || zoneChose.map.mapId == 22
                || zoneChose.map.mapId == 23) {
            pl.mapBeforeCapsule = pl.zone;
        } else {
            zoneId = pl.mapBeforeCapsule != null ? pl.mapBeforeCapsule.zoneId : -1;
            pl.mapBeforeCapsule = null;
        }
        pl.changeMapVIP = true;
        ChangeMapService.gI().changeMapBySpaceShip(pl, pl.mapCapsule.get(index).map.mapId, zoneId, -1);
    }

    public void eatPea(Player player) {
        if (!Util.canDoWithTime(player.lastTimeEatPea, 1000)) {
            return;
        }
        player.lastTimeEatPea = System.currentTimeMillis();
        Item pea = null;
        for (Item item : player.inventory.itemsBag) {
            if (item.isNotNullItem() && item.template.type == 6) {
                pea = item;
                break;
            }
        }
        if (pea != null) {
            long hpKiHoiPhuc = 0;
            int lvPea = Integer.parseInt(pea.template.name.substring(13));
            for (Item.ItemOption io : pea.itemOptions) {
                if (io.optionTemplate.id == 2) {
                    hpKiHoiPhuc = io.param * 1000;
                    break;
                }
                if (io.optionTemplate.id == 48) {
                    hpKiHoiPhuc = io.param;
                    break;
                }
            }
            player.nPoint.setHp(Util.maxIntValue(player.nPoint.hp + hpKiHoiPhuc));
            player.nPoint.setMp(Util.maxIntValue(player.nPoint.mp + hpKiHoiPhuc));
            PlayerService.gI().sendInfoHpMp(player);
            Service.gI().sendInfoPlayerEatPea(player);
            if (player.pet != null && player.zone.equals(player.pet.zone) && !player.pet.isDie()) {
                int statima = 100 * lvPea;
                player.pet.nPoint.stamina += statima;
                if (player.pet.nPoint.stamina > player.pet.nPoint.maxStamina) {
                    player.pet.nPoint.stamina = player.pet.nPoint.maxStamina;
                }
                player.pet.nPoint.setHp(Util.maxIntValue(player.pet.nPoint.hp + hpKiHoiPhuc));
                player.pet.nPoint.setMp(Util.maxIntValue(player.pet.nPoint.mp + hpKiHoiPhuc));
                Service.gI().sendInfoPlayerEatPea(player.pet);
                Service.gI().chatJustForMe(player, player.pet, "Cám ơn sư phụ");
            }

            InventoryService.gI().subQuantityItemsBag(player, pea, 1);
            InventoryService.gI().sendItemBag(player);
        }
    }

    private void upSkillPet(Player pl, Item item) {
        if (pl.pet == null) {
            Service.gI().sendThongBao(pl, "Không thể thực hiện");
            return;
        }
        try {
            switch (item.template.id) {
                case 402: // skill 1
                    if (SkillUtil.upSkillPet(pl.pet.playerSkill.skills, 0)) {
                        Service.gI().chatJustForMe(pl, pl.pet, "Cám ơn sư phụ");
                        InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                    } else {
                        Service.gI().sendThongBao(pl, "Không thể thực hiện");
                    }
                    break;
                case 403: // skill 2
                    if (SkillUtil.upSkillPet(pl.pet.playerSkill.skills, 1)) {
                        Service.gI().chatJustForMe(pl, pl.pet, "Cám ơn sư phụ");
                        InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                    } else {
                        Service.gI().sendThongBao(pl, "Không thể thực hiện");
                    }
                    break;
                case 404: // skill 3
                    if (SkillUtil.upSkillPet(pl.pet.playerSkill.skills, 2)) {
                        Service.gI().chatJustForMe(pl, pl.pet, "Cám ơn sư phụ");
                        InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                    } else {
                        Service.gI().sendThongBao(pl, "Không thể thực hiện");
                    }
                    break;
                case 759: // skill 4
                    if (SkillUtil.upSkillPet(pl.pet.playerSkill.skills, 3)) {
                        Service.gI().chatJustForMe(pl, pl.pet, "Cám ơn sư phụ");
                        InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                    } else {
                        Service.gI().sendThongBao(pl, "Không thể thực hiện");
                    }
                    break;

            }

        } catch (Exception e) {
            Service.gI().sendThongBao(pl, "Không thể thực hiện");
        }
    }

    private void ItemManhGiay(Player pl, Item item) {
        if (pl.winSTT && !Util.isAfterMidnight(pl.lastTimeWinSTT)) {
            Service.gI().sendThongBao(pl, "Hãy gặp thần mèo Karin để sử dụng");
            return;
        } else if (pl.winSTT && Util.isAfterMidnight(pl.lastTimeWinSTT)) {
            pl.winSTT = false;
            pl.callBossPocolo = false;
            pl.zoneSieuThanhThuy = null;
        }
        NpcService.gI().createMenuConMeo(pl, item.template.id, 564,
                "Đây chính là dấu hiệu riêng của...\nĐại Ma Vương Pôcôlô\nĐó là một tên quỷ dữ đội lốt người, một kẻ đại gian ác\ncó sức mạnh vô địch và lòng tham không đáy...\nĐối phó với hắn không phải dễ\nCon có chắc chắn muốn tìm hắn không?",
                "Đồng ý", "Từ chối");
    }

    private void ItemSieuThanThuy(Player pl, Item item) {
        long tnsm = 5_000_000;
        int n = 0;
        switch (item.template.id) {
            case 727:
                n = 2;
                break;
            case 728:
                n = 10;
                break;
        }
        InventoryService.gI().subQuantityItemsBag(pl, item, 1);
        InventoryService.gI().sendItemBag(pl);
        if (Util.isTrue(50, 100)) {
            Service.gI().sendThongBao(pl, "Bạn đã bị chết vì độc của thuốc tăng lực siêu thần thủy.");
            pl.setDie();
        } else {
            for (int i = 0; i < n; i++) {
                Service.gI().addSMTN(pl, (byte) 2, tnsm, true);
            }
        }
    }

    private void Hopdothanlinh(Player pl, Item item) {// hop qua do thần linh
        NpcService.gI().createMenuConMeo(pl, ConstNpc.MENU_OPTION_USE_ITEM1703, -1, "Chọn hành tinh của Bạn đi", "Set trái đất",
                "Set namec", "Set xayda", "Từ chổi");
    }

    private void Hopdohuydiet(Player pl, Item item) {// hop qua do huy diet
        NpcService.gI().createMenuConMeo(pl, ConstNpc.MENU_OPTION_USE_ITEM1704, -1, "Chọn hành tinh của Bạn đi", "Set trái đất",
                "Set namec", "Set xayda", "Từ chổi");
    }
    private void Hopdoskh(Player pl, Item item) {// hop qua do skh
        NpcService.gI().createMenuConMeo(pl, ConstNpc.MENU_OPTION_USE_ITEM1968, -1, "Chọn hành tinh của Bạn đi", "Set trái đất",
                "Set namec", "Set xayda", "Từ chổi");
    }

    public void UseCard(Player pl, Item item) {
        RadarCard radarTemplate = RadarService.gI().RADAR_TEMPLATE.stream().filter(c -> c.Id == item.template.id)
                .findFirst().orElse(null);
        if (radarTemplate == null) {
            return;
        }
        if (radarTemplate.Require != -1) {
            RadarCard radarRequireTemplate = RadarService.gI().RADAR_TEMPLATE.stream()
                    .filter(r -> r.Id == radarTemplate.Require).findFirst().orElse(null);
            if (radarRequireTemplate == null) {
                return;
            }
            Card cardRequire = pl.Cards.stream().filter(r -> r.Id == radarRequireTemplate.Id).findFirst().orElse(null);
            if (cardRequire == null || cardRequire.Level < radarTemplate.RequireLevel) {
                Service.gI().sendThongBao(pl, "Bạn cần sưu tầm " + radarRequireTemplate.Name + " ở cấp độ "
                        + radarTemplate.RequireLevel + " mới có thể sử dụng thẻ này");
                return;
            }
        }
        Card card = pl.Cards.stream().filter(r -> r.Id == item.template.id).findFirst().orElse(null);
        if (card == null) {
            Card newCard = new Card(item.template.id, (byte) 1, radarTemplate.Max, (byte) -1, radarTemplate.Options);
            if (pl.Cards.add(newCard)) {
                RadarService.gI().RadarSetAmount(pl, newCard.Id, newCard.Amount, newCard.MaxAmount);
                RadarService.gI().RadarSetLevel(pl, newCard.Id, newCard.Level);
                InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                InventoryService.gI().sendItemBag(pl);
            }
        } else {
            if (card.Level >= 2) {
                Service.gI().sendThongBao(pl, "Thẻ này đã đạt cấp tối đa");
                return;
            }
            card.Amount++;
            if (card.Amount >= card.MaxAmount) {
                card.Amount = 0;
                if (card.Level == -1) {
                    card.Level = 1;
                } else {
                    card.Level++;
                }
                Service.gI().point(pl);
            }
            RadarService.gI().RadarSetAmount(pl, card.Id, card.Amount, card.MaxAmount);
            RadarService.gI().RadarSetLevel(pl, card.Id, card.Level);
            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
            InventoryService.gI().sendItemBag(pl);
        }
    }

    private void replaceSkill2(Player pl, Item item, int skillId, String skillName) {
        if (pl.pet != null) {
            if (pl.pet.playerSkill.skills.get(1).skillId != -1) {
                Skill skill = SkillUtil.createSkill(skillId, 1);
                skill.coolDown = 1000;
                pl.pet.playerSkill.skills.set(1, skill);
                Service.gI().sendThongBao(pl, "Thay thành công chiêu 2 đệ tử thành " + skillName + "!");
            } else {
                Service.gI().sendThongBao(pl, "Ít nhất đệ tử ngươi phải có chiêu 2 chứ!");
                return;
            }
        } else {
            Service.gI().sendThongBao(pl, "Ngươi làm gì có đệ tử?");
            return;
        }
        InventoryService.gI().subQuantityItemsBag(pl, item, 1);
    }

    private void replaceSkill3(Player pl, Item item, int skillId, String skillName) {
        if (pl.pet != null) {
            if (pl.pet.playerSkill.skills.get(2).skillId != -1) {
                Skill skill = SkillUtil.createSkill(skillId, 1);
                skill.coolDown = 1000;
                pl.pet.playerSkill.skills.set(2, skill);
                Service.gI().sendThongBao(pl, "Thay thành công chiêu 3 đệ tử thành " + skillName + "!");
            } else {
                Service.gI().sendThongBao(pl, "Ít nhất đệ tử ngươi phải có chiêu 3 chứ!");
                return;
            }
        } else {
            Service.gI().sendThongBao(pl, "Ngươi làm gì có đệ tử?");
            return;
        }
        InventoryService.gI().subQuantityItemsBag(pl, item, 1);
    }

    private void replaceSkill4(Player pl, Item item, int skillId, String skillName) {
        if (pl.pet != null) {
            if (pl.pet.playerSkill.skills.get(3).skillId != -1) {
                Skill skill = SkillUtil.createSkill(skillId, 1);
                skill.coolDown = 1000;
                pl.pet.playerSkill.skills.set(3, skill);
                Service.gI().sendThongBao(pl, "Thay thành công chiêu 4 đệ tử thành " + skillName + "!");
            } else {
                Service.gI().sendThongBao(pl, "Ít nhất đệ tử ngươi phải có chiêu 4 chứ!");
                return;
            }
        } else {
            Service.gI().sendThongBao(pl, "Ngươi làm gì có đệ tử?");
            return;
        }
        InventoryService.gI().subQuantityItemsBag(pl, item, 1);
    }
}
