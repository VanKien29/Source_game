package shop;

/*
 *
 *
 * @author CongHoan
 */
import consts.ConstAchievement;
import item.Item;
import npc.specialnpc.MagicTree;
import player.Inventory;
import player.Player;
import network.Message;
import jdbc.daos.PlayerDAO;
import item.Item.ItemOption;

import java.util.ArrayList;

import server.Manager;
import services.InventoryService;
import services.ItemService;
import services.Service;
import utils.Logger;
import utils.Util;

import java.util.List;
import jdbc.DBConnecter;
import jdbc.NDVResultSet;

import models.Achievement.AchievementService;
import services.func.Input;
import services.func.VatPhamDaBan;
import task.TaskDanhHieu;
import task.TaskPlayer;

public class ShopService {

    private static final byte COST_GOLD = 0;
    private static final byte COST_GEM = 1;
    private static final byte COST_RUBY = 3;
    private static final byte COST_COUPON = 4;

    private static final byte NORMAL_SHOP = 0;
    private static final byte SPEC_SHOP = 3;

    private static ShopService I;

    public static ShopService gI() {
        if (ShopService.I == null) {
            ShopService.I = new ShopService();
        }
        return ShopService.I;
    }

    public void opendShop(Player player, String tagName, boolean allGender) {
        if (tagName.equals("ITEMS_LUCKY_ROUND")) {
            openShopType4(player, tagName, player.inventory.itemsBoxCrackBall);
            return;
        } else if (tagName.equals("ITEMS_MAIL_BOX")) {
            openShopType4(player, tagName, player.inventory.itemsMailBox);
            return;
        } else if (tagName.equals("ITEMS_DABAN")) {
            openShopType8(player, tagName, player.inventory.itemsDaBan);
            return;
        }
        try {
            Shop shop = this.getShop(tagName);
            for (TabShop tabShop : shop.tabShops) {
                for (ItemShop item : tabShop.itemShops) {
                    switch (item.temp.id) {
                        case 1627:// hành trang
                            if (player.inventory.itemsBag.size() >= 35) {
                                item.cost = ((player.inventory.itemsBag.size() - 35) + 1) * 2;
                            } else {
                                item.cost = 1;
                            }
                            break;
                    }
                }
            }
            shop = this.resolveShop(player, shop, allGender);
            switch (shop.typeShop) {
                case NORMAL_SHOP:
                    openShopType0(player, shop);
                    break;
                case SPEC_SHOP:
                    openShopType3(player, shop);
                    break;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Service.gI().sendThongBao(player, ex.getMessage());
        }
    }

    private Shop getShop(String tagName) throws Exception {
        for (Shop s : Manager.SHOPS) {
            if (s.tagName != null && s.tagName.equals(tagName)) {
                return s;
            }
        }
        throw new Exception("Shop " + tagName + " không tồn tại!");
    }

    private Shop resolveShop(Player player, Shop shop, boolean allGender) {
        if (shop.tagName != null
                && (shop.tagName.equals("BUA_1H") || shop.tagName.equals("BUA_8H") || shop.tagName.equals("BUA_1M"))) {
            return this.resolveShopBua(player, new Shop(shop));
        }
        // Xử lý danh hiệu shop
        if (shop.id == 24) {
            Shop newShop = allGender ? new Shop(shop) : new Shop(shop, player);
            for (TabShop tabShop : newShop.tabShops) {
                if (tabShop.id == 28) {
                    newShop = DanhHieu(player, newShop);
                } else if (tabShop.id == 29) {
                    newShop = SoHuu(player, newShop);
                }
            }
            return newShop;
        }
        return allGender ? new Shop(shop) : new Shop(shop, player);
    }

    private Shop resolveShopBua(Player player, Shop s) {
        for (TabShop tabShop : s.tabShops) {
            for (ItemShop item : tabShop.itemShops) {
                long min = 0;
                switch (item.temp.id) {
                    case 213:
                        long timeTriTue = player.charms.tdTriTue;
                        long current = System.currentTimeMillis();
                        min = (timeTriTue - current) / 60000;

                        break;
                    case 214:
                        min = (player.charms.tdManhMe - System.currentTimeMillis()) / 60000;
                        break;
                    case 215:
                        min = (player.charms.tdDaTrau - System.currentTimeMillis()) / 60000;
                        break;
                    case 216:
                        min = (player.charms.tdOaiHung - System.currentTimeMillis()) / 60000;
                        break;
                    case 217:
                        min = (player.charms.tdBatTu - System.currentTimeMillis()) / 60000;
                        break;
                    case 218:
                        min = (player.charms.tdDeoDai - System.currentTimeMillis()) / 60000;
                        break;
                    case 219:
                        min = (player.charms.tdThuHut - System.currentTimeMillis()) / 60000;
                        break;
                    case 522:
                        min = (player.charms.tdDeTu - System.currentTimeMillis()) / 60000;
                        break;
                    case 671:
                        min = (player.charms.tdTriTue3 - System.currentTimeMillis()) / 60000;
                        break;
                    case 672:
                        min = (player.charms.tdTriTue4 - System.currentTimeMillis()) / 60000;
                        break;
                }
                if (min > 0) {
                    item.options.clear();
                    if (min >= 1440) {
                        item.options.add(new Item.ItemOption(63, (int) min / 1440));
                    } else if (min >= 60) {
                        item.options.add(new Item.ItemOption(64, (int) min / 60));
                    } else {
                        item.options.add(new Item.ItemOption(65, (int) min));
                    }
                }
            }
        }
        return s;
    }

    private Shop DanhHieu(Player player, Shop s) {
        if (player.playerTask == null) {
            player.playerTask = new TaskPlayer();
        }
        if (player.playerTask.taskdh == null) {
            player.playerTask.taskdh = new TaskDanhHieu();
        }
        for (TabShop tabShop : s.tabShops) {
            if (tabShop.id != 28) {
                continue;
            }
            for (ItemShop item : tabShop.itemShops) {
                int required = 0;
                int current = 0;
                int percentDone = 0;
                long min = 0;
                switch (item.temp.id) {
                    case 1289 -> {
                        // Nạp VND: tiến độ quy đổi tích lũy (1,000,000 = 100%)
                        required = 1000000;
                        current = player.playerTask.taskdh.Nap;
                    }
                    case 1291 -> {
                        // Váy Dop Độ: cập nhật ở updateTaskDopDo()
                        required = 500;
                        current = player.playerTask.taskdh.VeChai;
                    }
                    case 1296 -> {
                        // Mốc sách túi: ăn trộm 20 lần
                        required = 20;
                        current = player.playerTask.taskdh.MocSachTui;
                    }
                    case 1299 -> {
                        // Fan Cùng: điểm danh từ SQL (7 ngày)
                        required = 7;
                        current = player.getSession().diemdanh;
                    }
                    case 1392 -> {
                        required = 999999;
                        current = player.playerTask.taskdh.GoDauTre;
                    }
                    case 1393 -> {
                        required = 999999;
                        current = player.playerTask.taskdh.GoDauTre1;
                    }
                    case 1394 -> {
                        required = 999999;
                        current = player.playerTask.taskdh.GoDauTre2;
                    }
                    case 1457 -> {
                        required = 3;
                        current = player.playerTask.taskdh.XMas;
                    }
                    case 1514 -> {
                        required = 1;
                        current = player.playerTask.taskdh.EmDepEmXinh;
                    }
                    case 1297 -> {
                        required = 10;
                        current = player.playerTask.taskdh.AnBamTraXanh;
                    }
                    case 1673 -> {
                        required = 500;
                        current = player.playerTask.taskdh.TayNhanhHonNao;
                    }
                    default -> {
                        continue;
                    }
                }
                percentDone = (required > 0) ? (int) ((double) current / required * 100) : 100;
                boolean hasProgressOption = false;
                item.options.removeIf(opt -> opt.optionTemplate.id == 220);
                // Thêm option 220 mới với % hiện tại
                if (percentDone >= 100) {
                    item.options.add(new Item.ItemOption(220, 100));
                } else {
                    item.options.add(new Item.ItemOption(220, percentDone));
                }
                if (hasProgressOption && min > 0) {
                    if (min >= 1440 * 3) {
                        item.options.add(new Item.ItemOption(63, (int) min / 1440));
                    } else if (min >= 1440) {
                        item.options.add(new Item.ItemOption(63, (int) min / 1440));
                    } else if (min >= 60) {
                        item.options.add(new Item.ItemOption(64, (int) min / 60));
                    } else {
                        item.options.add(new Item.ItemOption(65, (int) min));
                    }
                }
            }
        }
        return s;
    }

    private Shop SoHuu(Player player, Shop s) {
        for (TabShop tabShop : s.tabShops) {
            if (tabShop.id != 29) {
                continue;
            }
            // Remove items không pass filter (dùng Iterator để tránh
            // ConcurrentModificationException)
            tabShop.itemShops.removeIf(item -> {
                // Tab sở hữu: chỉ hiện khi đã đủ điều kiện 100% để nhận/mua.
                if (!checkDanhHieuProgress(player, item.temp.id)) {
                    return true; // Remove
                }
                // Item pass filter, add time options
                long min = 1 * 24 * 60;
                boolean hasDayOption = false;
                for (Item.ItemOption option : item.options) {
                    if (option.optionTemplate.id == 63) {
                        hasDayOption = true;
                        break;
                    }
                }
                if (min > 0 && !hasDayOption) {
                    if (min >= 1440) {
                        item.options.add(new Item.ItemOption(63, (int) min / 1440));
                    } else if (min >= 60) {
                        item.options.add(new Item.ItemOption(64, (int) min / 60));
                    } else {
                        item.options.add(new Item.ItemOption(65, (int) min));
                    }
                }
                return false; // Keep this item
            });
        }
        return s;
    }

    private int ItemDanhHieu(Player player, TabShop tab) {
        int count = 0;
        for (ItemShop itemShop : tab.itemShops) {
            if (tab.id == 29 && CheckDanhHieu(player, itemShop)) {
                count++;
            }
        }
        return count;
    }

    private boolean CheckDanhHieu(Player player, ItemShop itemShop) {
        if (itemShop.temp.id == 1289) {
            Item napItem = InventoryService.gI().findItemInAllInventories(player, 1289);
            return napItem != null;
        }
        if (itemShop.temp.id == 1291) {
            Item napItem = InventoryService.gI().findItemInAllInventories(player, 1291);
            return napItem != null;
        }
        if (itemShop.temp.id == 1296) {
            Item napItem = InventoryService.gI().findItemInAllInventories(player, 1296);
            return napItem != null;
        }
        if (itemShop.temp.id == 1299) {
            Item napItem = InventoryService.gI().findItemInAllInventories(player, 1299);
            return napItem != null;
        }
        if (itemShop.temp.id == 1392) {
            Item napItem = InventoryService.gI().findItemInAllInventories(player, 1392);
            return napItem != null;
        }
        if (itemShop.temp.id == 1393) {
            Item napItem = InventoryService.gI().findItemInAllInventories(player, 1393);
            return napItem != null;
        }
        if (itemShop.temp.id == 1394) {
            Item napItem = InventoryService.gI().findItemInAllInventories(player, 1394);
            return napItem != null;
        }
        if (itemShop.temp.id == 1457) {
            Item napItem = InventoryService.gI().findItemInAllInventories(player, 1457);
            return napItem != null;
        }
        if (itemShop.temp.id == 1514) {
            Item napItem = InventoryService.gI().findItemInAllInventories(player, 1514);
            return napItem != null;
        }
        if (itemShop.temp.id == 1297) {
            Item napItem = InventoryService.gI().findItemInAllInventories(player, 1297);
            return napItem != null;
        }
        if (itemShop.temp.id == 1673) {
            Item napItem = InventoryService.gI().findItemInAllInventories(player, 1673);
            return napItem != null;
        }
        return true;
    }

    private boolean checkDanhHieuProgress(Player player, int itemTempId) {
        if (player.playerTask == null) {
            player.playerTask = new TaskPlayer();
        }
        if (player.playerTask.taskdh == null) {
            player.playerTask.taskdh = new TaskDanhHieu();
        }
        int required = 0;
        int current = 0;
        switch (itemTempId) {
            case 1289 -> {
                // Nạp VND: kiểm tra tiến độ quy đổi tích lũy >= 1,000,000
                required = 1000000;
                current = player.playerTask.taskdh.Nap;
            }
            case 1291 -> {
                // Váy Dop Độ: cập nhật ở updateTaskDopDo()
                required = 500;
                current = player.playerTask.taskdh.VeChai;
            }
            case 1296 -> {
                // Mốc sách túi: ăn trộm 20 lần
                required = 20;
                current = player.playerTask.taskdh.MocSachTui;
            }
            case 1299 -> {
                // Fan Cùng: điểm danh từ SQL (7 ngày)
                required = 7;
                current = player.getSession().diemdanh;
            }
            case 1392 -> {
                required = 999999;
                current = player.playerTask.taskdh.GoDauTre;
            }
            case 1393 -> {
                required = 999999;
                current = player.playerTask.taskdh.GoDauTre1;
            }
            case 1394 -> {
                required = 999999;
                current = player.playerTask.taskdh.GoDauTre2;
            }
            case 1457 -> {
                required = 3;
                current = player.playerTask.taskdh.XMas;
            }
            case 1514 -> {
                required = 1;
                current = player.playerTask.taskdh.EmDepEmXinh;
            }
            case 1297 -> {
                required = 10;
                current = player.playerTask.taskdh.AnBamTraXanh;
            }
            case 1673 -> {
                required = 500;
                current = player.playerTask.taskdh.TayNhanhHonNao;
            }
            default -> {
                return true;
            }
        }
        return current >= required;
    }

    private void openShopType0(Player player, Shop shop) {
        if (shop != null) {
            player.iDMark.setShopOpen(shop);
            player.iDMark.setTagNameShop(shop.tagName);
            Message msg = null;
            try {
                msg = new Message(-44);
                msg.writer().writeByte(NORMAL_SHOP);
                msg.writer().writeByte(shop.tabShops.size());
                for (TabShop tab : shop.tabShops) {
                    msg.writer().writeUTF(tab.name);
                    msg.writer().writeByte(tab.itemShops.size());
                    for (ItemShop itemShop : tab.itemShops) {
                        msg.writer().writeShort(itemShop.temp.id);
                        if (itemShop.typeSell == COST_GOLD) {
                            msg.writer().writeInt(itemShop.cost);
                            msg.writer().writeInt(0);
                        } else if (itemShop.typeSell == COST_GEM) {
                            msg.writer().writeInt(0);
                            msg.writer().writeInt(itemShop.cost);
                        } else if (itemShop.typeSell == COST_RUBY) {
                            msg.writer().writeInt(0);
                            msg.writer().writeInt(itemShop.cost);
                        } else if (itemShop.typeSell == COST_COUPON) {
                            msg.writer().writeInt(0);
                            msg.writer().writeInt(itemShop.cost);
                        }
                        msg.writer().writeByte(itemShop.options.size());
                        // mở option item cho src
                        for (Item.ItemOption option : itemShop.options) {
                            msg.writer().writeInt(option.optionTemplate.id);
                            msg.writer().writeInt(option.param);
                        }
                        msg.writer().writeByte(itemShop.isNew ? 1 : 0);
                        if (itemShop.temp.type == 5) {
                            msg.writer().writeByte(1);
                            msg.writer().writeShort(itemShop.temp.head);
                            msg.writer().writeShort(itemShop.temp.body);
                            msg.writer().writeShort(itemShop.temp.leg);
                            msg.writer().writeShort(-1);
                        } else {
                            msg.writer().writeByte(0);
                        }
                    }
                }
                player.sendMessage(msg);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (msg != null) {
                    msg.cleanup();
                }
            }
        }
    }

    private void openShopType3(Player player, Shop shop) {
        player.iDMark.setShopOpen(shop);
        player.iDMark.setTagNameShop(shop.tagName);
        if (shop != null) {
            Message msg = null;
            try {
                msg = new Message(-44);
                msg.writer().writeByte(SPEC_SHOP);
                msg.writer().writeByte(shop.tabShops.size());
                for (TabShop tab : shop.tabShops) {
                    msg.writer().writeUTF(tab.name);
                    msg.writer().writeByte(tab.itemShops.size());
                    for (ItemShop itemShop : tab.itemShops) {
                        msg.writer().writeShort(itemShop.temp.id);
                        if (shop.id == 30) {
                            msg.writer().writeShort(itemShop.iconSpec);
                        } else {
                            msg.writer()
                                    .writeShort(
                                            ItemService.gI().createNewItem((short) itemShop.iconSpec).template.iconID);
                        }
                        msg.writer().writeInt(itemShop.cost);
                        msg.writer().writeByte(itemShop.options.size());
                        // mở option item cho src
                        for (Item.ItemOption option : itemShop.options) {
                            msg.writer().writeInt(option.optionTemplate.id);
                            msg.writer().writeInt(option.param);
                        }
                        msg.writer().writeByte(itemShop.isNew ? 1 : 0);
                        if (itemShop.temp.type == 5) {
                            msg.writer().writeByte(1);
                            msg.writer().writeShort(itemShop.temp.head);
                            msg.writer().writeShort(itemShop.temp.body);
                            msg.writer().writeShort(itemShop.temp.leg);
                            msg.writer().writeShort(-1);
                        } else {
                            msg.writer().writeByte(0);
                        }
                    }
                }
                player.sendMessage(msg);
            } catch (Exception e) {
                Logger.logException(ShopService.class, e);
            } finally {
                if (msg != null) {
                    msg.cleanup();
                }
            }
        }
    }

    private void openShopType4(Player player, String tagName, List<Item> items) {
        if (items == null) {
            return;
        }
        player.iDMark.setTagNameShop(tagName);
        Message msg = null;
        try {
            msg = new Message(-44);
            msg.writer().writeByte(4);
            msg.writer().writeByte(1);
            msg.writer().writeUTF(items.size() + "Vật\nphẩm");
            msg.writer().writeByte(items.size());
            for (Item item : items) {
                msg.writer().writeShort(item.template.id);
                msg.writer().writeUTF("Ngọc Rồng Hdpe");
                msg.writer().writeByte(item.itemOptions.size() + 1);
                // mở option item cho src
                for (Item.ItemOption io : item.itemOptions) {
                    msg.writer().writeInt(io.optionTemplate.id);
                    msg.writer().writeInt(io.param);
                }
                if (item.quantity > 1) {
                    msg.writer().writeInt(31);
                    msg.writer().writeInt(item.quantity);
                } else {
                    msg.writer().writeInt(73);
                    msg.writer().writeInt(0);
                }
                msg.writer().writeByte(1);
                if (item.template.type == 5) {
                    msg.writer().writeByte(1);
                    msg.writer().writeShort(item.template.head);
                    msg.writer().writeShort(item.template.body);
                    msg.writer().writeShort(item.template.leg);
                    msg.writer().writeShort(-1);
                } else {
                    msg.writer().writeByte(0);
                }
            }
            player.sendMessage(msg);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    private void openShopType8(Player player, String tagName, List<Item> items) {
        if (items == null) {
            return;
        }
        player.iDMark.setTagNameShop(tagName);
        Message msg = null;
        try {
            msg = new Message(-44);
            msg.writer().writeByte(8);
            msg.writer().writeByte(1);
            msg.writer().writeUTF("Mua lại\n[" + items.size() + "/20]");
            msg.writer().writeByte(items.size());
            for (Item item : items) {
                int giamualaingoc = item.template.gem / 2;
                int giamualaivang = giamualaingoc == 0
                        ? (int) item.template.gold / 2 > 0 ? (int) item.template.gold / 2 : item.quantity * 100
                        : 0;
                msg.writer().writeShort(item.template.id);
                msg.writer().writeInt(giamualaivang);
                msg.writer().writeInt(giamualaingoc);
                msg.writer().writeInt(item.quantity);
                msg.writer().writeByte(item.itemOptions.size());
                // mở option item cho src
                for (Item.ItemOption io : item.itemOptions) {
                    msg.writer().writeInt(io.optionTemplate.id);
                    msg.writer().writeInt(io.param);
                }
                msg.writer().writeByte(0);
                if (item.template.type == 5) {
                    msg.writer().writeByte(1);
                    msg.writer().writeShort(item.template.head);
                    msg.writer().writeShort(item.template.body);
                    msg.writer().writeShort(item.template.leg);
                    msg.writer().writeShort(-1);
                } else {
                    msg.writer().writeByte(0);
                }
            }
            player.sendMessage(msg);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void takeItem(Player player, byte type, int tempId) {
        String tagName = player.iDMark.getTagNameShop();
        if (tagName == null || tagName.length() <= 0) {
            return;
        }
        if (tagName.equals("ITEMS_LUCKY_ROUND")) {
            getItemSideBoxLuckyRound(player, player.inventory.itemsBoxCrackBall, type, tempId);
            return;
        } else if (tagName.equals("ITEMS_MAIL_BOX")) {
            getItemSideMailsBox(player, player.inventory.itemsMailBox, type, tempId);
            return;
        } else if (tagName.equals("ITEMS_REWARD")) {

            return;
        } else if (tagName.equals("ITEMS_DABAN")) {
            buyItemDaBan(player, player.inventory.itemsDaBan, type, tempId);
            return;
        } else if (tagName.equals("BILL")) {
            buyItemHD(player, tempId);
            return;
        }

        if (player.iDMark.getShopOpen() == null) {
            Service.gI().sendThongBao(player, "Không thể thực hiện");
            return;
        }
        if (tagName.equals("BUA_1H") || tagName.equals("BUA_8H") || tagName.equals("BUA_1M")) {
            buyItemBua(player, tempId);
        } else if (tagName.equals("SHOP_VND")) {
            buyItemVND(player, tempId);
        } else if (tagName.equals("DIEM_DANH")) {
            buyItemDiemDanh(player, tempId);
        } else if (tagName.equals("SHOP_NHS")) {
            buyItemNHS(player, tempId);
        } else if (tagName.equals("SHOP_DIEMSANBOSS")) {
            buyItemBHM(player, tempId);
            // } else if (tagName.equals("SHOP_QUY_LAO")) {
            // buyItemQuyLao(player, tempId);
        } else if (tagName.equals("SANTA_HEAD")) {
            Item itS = ItemService.gI().createNewItem((short) tempId);
            player.head = (short) itS.template.head;
            Service.gI().Send_Caitrang(player);
            Service.gI().sendThongBao(player, "Đổi kiểu tóc thành công");
        } else {
            buyItem(player, tempId, type);
        }
        Service.gI().sendMoney(player);
    }

    private ItemShop getItemShopByClientTab(Shop shop, int itemTempId, byte clientTabIndex) {
        if (shop == null) {
            return null;
        }
        int zeroBasedIndex = clientTabIndex;
        if (zeroBasedIndex >= 0 && zeroBasedIndex < shop.tabShops.size()) {
            TabShop selectedTab = shop.tabShops.get(zeroBasedIndex);
            for (ItemShop item : selectedTab.itemShops) {
                if (item.temp.id == itemTempId) {
                    return item;
                }
            }
        }

        int oneBasedIndex = clientTabIndex - 1;
        if (oneBasedIndex >= 0 && oneBasedIndex < shop.tabShops.size()) {
            TabShop selectedTab = shop.tabShops.get(oneBasedIndex);
            for (ItemShop item : selectedTab.itemShops) {
                if (item.temp.id == itemTempId) {
                    return item;
                }
            }
        }

        return shop.getItemShop(itemTempId);
    }

    private boolean subMoneyByItemShop(Player player, ItemShop is) {
        int gold = 0;
        int gem = 0;
        int ruby = 0;
        int coupon = 0;
        switch (is.typeSell) {
            case COST_GOLD ->
                gold = is.cost;
            case COST_GEM ->
                gem = is.cost;
            case COST_RUBY ->
                ruby = is.cost;
            case COST_COUPON ->
                coupon = is.cost;

        }
        if (player.inventory.gold < gold) {
            Service.gI().sendThongBao(player, "Bạn không có đủ vàng");
            return false;
        } else if (player.inventory.gem < gem) {
            Service.gI().sendThongBao(player, "Bạn không có đủ ngọc");
            return false;
        } else if (player.inventory.ruby < ruby) {
            Service.gI().sendThongBao(player, "Bạn không có đủ hồng ngọc");
            return false;
        } else if (player.inventory.coupon < coupon) {
            Service.gI().sendThongBao(player, "Bạn không có đủ điểm");
            return false;
        }
        player.inventory.gold -= gold;
        player.inventory.gem -= gem;
        player.inventory.ruby -= ruby;
        player.inventory.coupon -= coupon;
        return true;
    }

    private boolean subMoneyByItemShopV2(Player player, ItemShop is) {
        int gold = 0;
        int gem = 0;
        int ruby = 0;
        int coupon = 0;
        switch (is.typeSell) {
            case COST_GOLD ->
                gold = is.cost;
            case COST_GEM ->
                gem = is.cost;
            case COST_RUBY ->
                ruby = is.cost;
            case COST_COUPON ->
                coupon = is.cost;

        }
        if (player.inventory.gold < gold) {
            Service.gI().sendThongBaoOK(player,
                    "Bạn không đủ vàng, còn thiếu " + Util.numberToMoney(player.inventory.gold - gold));
            return false;
        } else if (player.inventory.gem < gem) {
            Service.gI().sendThongBaoOK(player,
                    "Bạn không đủ ngọc, còn thiếu " + Util.numberToMoney(player.inventory.gem - gem));
            return false;
        } // else if (player.inventory.ruby < ruby) {
          // Service.gI().sendThongBaoOK(player,
          // "Bạn không đủ hồng ngọc, còn thiếu " +
          // Util.numberToMoney(player.inventory.ruby - ruby));
          // return false;
          // }
        else if (player.inventory.coupon < coupon) {
            Service.gI().sendThongBaoOK(player,
                    "Bạn không đủ điểm, còn thiếu " + Util.numberToMoney(player.inventory.coupon - coupon));
            return false;
        }
        player.inventory.gold -= gold;
        player.inventory.gem -= gem;
        // player.inventory.ruby -= ruby;
        player.inventory.coupon -= coupon;
        Service.gI().sendMoney(player);
        return true;
    }

    /**
     * Mua bùa
     *
     * @param player     người chơi
     * @param itemTempId id template vật phẩm
     */
    private void buyItemBua(Player player, int itemTempId) {
        Shop shop = player.iDMark.getShopOpen();
        ItemShop is = shop.getItemShop(itemTempId);
        if (is == null) {
            Service.gI().sendThongBao(player, "Không thể thực hiện");
            return;
        }
        if (!subMoneyByItemShop(player, is)) {
            return;
        }
        InventoryService.gI().addItemBag(player, ItemService.gI().createItemFromItemShop(is));
        InventoryService.gI().sendItemBag(player);
        opendShop(player, shop.tagName, true);
    }

    private void buyItemVND(Player player, int itemTempId) {
        Shop shop = player.iDMark.getShopOpen();
        ItemShop is = shop.getItemShop(itemTempId);
        int pointExchange = 0;
        int evPoint = player.getSession().cash;
        if (is == null) {
            Service.gI().sendThongBao(player, "Item shop bị lỗi vui lòng báo admin");
            return;
        }
        if (InventoryService.gI().getCountEmptyBag(player) == 0) {
            Service.gI().sendThongBao(player, "Hành trang đầy rồi dọn bớt đi");
            return;
        }
        for (ItemOption io : is.options) {
            if (io.optionTemplate.id == 249) {
                pointExchange = io.param;
            }
        }
        if (pointExchange > 0) {
            if (evPoint >= pointExchange * 1000) {
                PlayerDAO.subcash(player, pointExchange * 1000);
                InventoryService.gI().addItemBag(player, ItemService.gI().createItemFromItemShop(is));
                InventoryService.gI().sendItemBag(player);
                Service.gI().sendThongBao(player,
                        "Bạn đã đổi thành công " + ItemService.gI().createItemFromItemShop(is).template.name);
                opendShop(player, shop.tagName, true);
            } else {
                Service.gI().sendThongBao(player, "Bạn còn thiếu " + (pointExchange * 1000 - evPoint) + " VND");
            }
        }
    }

    private void buyItemDiemDanh(Player player, int itemTempId) {
        Shop shop = player.iDMark.getShopOpen();
        ItemShop is = shop.getItemShop(itemTempId);
        if (is == null) {
            Service.gI().sendThongBao(player, "Item shop bị lỗi vui lòng báo admin");
            return;
        }
        int pointExchange = is.options.stream()
                .filter(io -> io.optionTemplate.id == 39)
                .findFirst()
                .map(io -> io.param)
                .orElse(0);
        if (InventoryService.gI().getCountEmptyBag(player) == 0) {
            Service.gI().sendThongBao(player, "Hành trang đầy rồi dọn bớt đi");
            return;
        }
        if (pointExchange > 0 && player.getSession().diemdanh >= pointExchange) {
            try {
                String checkQuery = "SELECT COUNT(*) FROM history_items_diemdanh WHERE account_id = ? AND item_temp_id = ?";
                NDVResultSet resultSet = DBConnecter.executeQuery(checkQuery, player.getSession().userId, itemTempId);
                if (resultSet.next() && resultSet.getInt(1) > 0) {
                    Service.gI().sendThongBao(player, "Bạn đã nhận vật phẩm này rồi!");
                    return;
                }
                InventoryService.gI().addItemBag(player, ItemService.gI().createItemFromItemShop(is));
                InventoryService.gI().sendItemBag(player);
                String insertQuery = "INSERT INTO history_items_diemdanh (account_id, item_temp_id) VALUES (?, ?)";
                DBConnecter.executeUpdate(insertQuery, player.getSession().userId, itemTempId);
                Service.gI().sendThongBao(player,
                        "Bạn đã nhận thành công " + ItemService.gI().createItemFromItemShop(is).template.name);
                opendShop(player, shop.tagName, true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Service.gI().sendThongBao(player, "Cần điểm danh thêm " + (pointExchange - player.getSession().diemdanh)
                    + " ngày để nhận vật phẩm này!");
        }
    }

    private void buyItemNHS(Player player, int itemTempId) {
        Shop shop = player.iDMark.getShopOpen();
        ItemShop is = shop.getItemShop(itemTempId);
        int pointExchange = 0;
        int evPoint = player.event.getEventPointNHS();
        if (is == null) {
            Service.gI().sendThongBao(player, "Item shop bị lỗi vui lòng báo admin");
            return;
        }
        if (InventoryService.gI().getCountEmptyBag(player) == 0) {
            Service.gI().sendThongBao(player, "Hành trang đầy rồi dọn bớt đi");
            return;
        }
        for (ItemOption io : is.options) {
            if (io.optionTemplate.id == 76) {
                pointExchange = io.param;
            }
        }
        if (pointExchange > 0) {
            if (evPoint >= pointExchange) {
                player.event.subEventPointNHS(pointExchange);
                InventoryService.gI().addItemBag(player, ItemService.gI().createItemFromItemShop(is));
                InventoryService.gI().sendItemBag(player);
                Service.gI().sendThongBao(player,
                        "Bạn đã đổi thành công " + ItemService.gI().createItemFromItemShop(is).template.name);
                opendShop(player, shop.tagName, true);
            } else {
                Service.gI().sendThongBao(player, "Bạn còn thiếu " + (pointExchange - evPoint) + " điểm");
            }
        }
    }

    private void buyItemBHM(Player player, int itemTempId) {
        Shop shop = player.iDMark.getShopOpen();
        ItemShop is = shop.getItemShop(itemTempId);
        int pointExchange = 0;
        int evPoint = player.event.getEventPointBHM();
        if (is == null) {
            Service.gI().sendThongBao(player, "Item shop bị lỗi vui lòng báo admin");
            return;
        }
        if (InventoryService.gI().getCountEmptyBag(player) == 0) {
            Service.gI().sendThongBao(player, "Hành trang đầy rồi dọn bớt đi");
            return;
        }
        for (ItemOption io : is.options) {
            if (io.optionTemplate.id == 76) {
                pointExchange = io.param;
            }
        }
        if (pointExchange > 0) {
            if (evPoint >= pointExchange) {
                player.event.subEventPointBHM(pointExchange);
                InventoryService.gI().addItemBag(player, ItemService.gI().createItemFromItemShop(is));
                InventoryService.gI().sendItemBag(player);
                Service.gI().sendThongBao(player,
                        "Bạn đã đổi thành công " + ItemService.gI().createItemFromItemShop(is).template.name);
                opendShop(player, shop.tagName, true);
            } else {
                Service.gI().sendThongBao(player, "Bạn còn thiếu " + (pointExchange - evPoint) + " điểm");
            }
        }
    }

    private void buyItemQuyLao(Player player, int itemTempId) {
        Shop shop = player.iDMark.getShopOpen();
        ItemShop is = shop.getItemShop(itemTempId);
        int pointExchange = 0;
        int evPoint = player.event.getEventPointQuyLao();
        if (is == null) {
            Service.gI().sendThongBao(player, "Item shop bị lỗi vui lòng báo admin");
            return;
        }
        if (InventoryService.gI().getCountEmptyBag(player) == 0) {
            Service.gI().sendThongBao(player, "Hành trang đầy rồi dọn bớt đi");
            return;
        }
        for (ItemOption io : is.options) {
            if (io.optionTemplate.id == 76) {
                pointExchange = io.param;
            }
        }
        if (pointExchange > 0) {
            if (evPoint >= pointExchange) {
                player.event.subEventPointQuyLao(pointExchange);
                InventoryService.gI().addItemBag(player, ItemService.gI().createItemFromItemShop(is));
                InventoryService.gI().sendItemBag(player);
                Service.gI().sendThongBao(player,
                        "Bạn đã đổi thành công " + ItemService.gI().createItemFromItemShop(is).template.name);
                opendShop(player, shop.tagName, true);
            } else {
                Service.gI().sendThongBao(player, "Bạn còn thiếu " + (pointExchange - evPoint) + " điểm");
            }
        }
    }

    /**
     * Mua vật phẩm trong cửa hàng
     *
     * @param player     người chơi
     * @param itemTempId id template vật phẩm
     */
    public void buyItem(Player player, int itemTempId, byte clientTabIndex) {
        Shop shop = player.iDMark.getShopOpen();
        ItemShop is = getItemShopByClientTab(shop, itemTempId, clientTabIndex);
        int[][] listDauThan = { { 13, 293 }, { 60, 294 }, { 61, 295 }, { 62, 296 }, { 63, 297 }, { 64, 298 },
                { 65, 299 }, { 352, 596 }, { 523, 597 } };
        boolean isDanhHieuItem = itemTempId == 1289 || itemTempId == 1291 || itemTempId == 1296 || itemTempId == 1299
                || itemTempId == 1392 || itemTempId == 1393 || itemTempId == 1394 || itemTempId == 1457
                || itemTempId == 1514 || itemTempId == 1297 || itemTempId == 1673;
        if (is == null) {
            Service.gI().sendThongBao(player, "Không thể thực hiện");
            return;
        }
        if (InventoryService.gI().getCountEmptyBag(player) == 0) {
            Service.gI().sendThongBao(player, "Hành trang đã đầy");
            return;
        }

        if (itemTempId == 711 && !InventoryService.gI().findItemSkinQuyLaoKame(player)) {
            Service.gI().sendThongBao(player, "Bạn phải có cải trang thành Quy Lão Kame mới có thể đổi.");
            return;
        }

        // Danh hiệu: chỉ cần kiểm tra hoàn thành/chưa hoàn thành, không phân biệt tab 28/29.
        if (isDanhHieuItem) {
            if (!checkDanhHieuProgress(player, itemTempId)) {
                Service.gI().sendThongBao(player, "Bạn chưa hoàn thành điều kiện danh hiệu này");
                return;
            }
           // Service.gI().sendThongBao(player, "Bạn đã hoàn thành điều kiện danh hiệu này");
            if (CheckDanhHieu(player, is)) {
                Service.gI().sendThongBao(player, "Bạn đã sở hữu danh hiệu này rồi");
                return;
            }
        }

        if (buyMoRongHanhTrang(player, is)) {
            return;
        }

        if (shop.typeShop == ShopService.NORMAL_SHOP) {
            if (!subMoneyByItemShop(player, is)) {
                return;
            }
        } else if (shop.typeShop == ShopService.SPEC_SHOP) {
            if (!this.subIemByItemShop(player, is)) {
                return;
            }
        } else if (shop.tagName.equals("SHOP_VND")) {
            if (!subMoneyByItemShop(player, is)) {
                return;
            }

        } else if (shop.tagName.equals("SHOP_NHS")) {
            if (!subMoneyByItemShop(player, is)) {
                return;
            }

        } else if (shop.tagName.equals("SHOP_BHM")) {
            if (!subMoneyByItemShop(player, is)) {
                return;
            }

        } else if (shop.tagName.equals("SHOP_QUY_LAO")) {
            if (!subMoneyByItemShop(player, is)) {
                return;
            }

        }
        Item item = ItemService.gI().createItemFromItemShop(is);
        item = buyMagicPean(player, listDauThan, item);
        if (item.template.id == 1523 || item.template.id == 1524) {
            item = ItemService.gI().createNewItem((short) 521);
            item.itemOptions.addAll(is.options);
        }
        InventoryService.gI().addItemBag(player, item);
        InventoryService.gI().sendItemBag(player);

        // Danh hiệu 1289: nhận xong thì reset tiến độ để người chơi tích lũy lại từ
        // đầu.
        if (itemTempId == 1289) {
            if (player.playerTask == null) {
                player.playerTask = new TaskPlayer();
            }
            if (player.playerTask.taskdh == null) {
                player.playerTask.taskdh = new TaskDanhHieu();
            }
            player.playerTask.taskdh.Nap = 0;
            player.effect.setPointDaiGiaMoiNhu(0);
        }

        Service.gI().sendThongBao(player, "Mua thành công " + is.temp.name);
    }

    private boolean buyMoRongHanhTrang(Player player, ItemShop itemShop) {
        boolean isBuy = false;
        if (itemShop.temp.id == 518 || itemShop.temp.id == 517 || itemShop.temp.id == 1627) {
            if (itemShop.temp.id == 1627 && player.inventory.itemsBag.size() >= 150) {
                Service.gI().sendThongBao(player, "Đã đạt mức tối đa");
                Service.gI().sendMoney(player);
                return true;
            }
            if (itemShop.temp.id == 517 && player.inventory.itemsBag.size() >= 100) {
                Service.gI().sendThongBao(player, "Đã đạt mức tối đa");
                Service.gI().sendMoney(player);
                return true;
            }
            if (itemShop.temp.id == 518 && player.inventory.itemsBox.size() >= 100) {
                Service.gI().sendThongBao(player, "Đã đạt mức tối đa");
                Service.gI().sendMoney(player);
                return true;
            }
            if (subMoneyByItemShop(player, itemShop)) {
                Item item = ItemService.gI().createItemFromItemShop(itemShop);
                InventoryService.gI().addItemBag(player, item);
                InventoryService.gI().sendItemBag(player);
                opendShop(player, itemShop.tabShop.shop.tagName, true);
                Service.gI().sendThongBao(player, "Bạn đã mua thành công");
            }
            isBuy = true;
        }
        return isBuy;
    }

    private Item buyMagicPean(Player player, int[][] listDauThan, Item item) {
        for (int i = 0; i < listDauThan.length; i++) {
            if (item.template.id == listDauThan[i][1]) {
                item = ItemService.gI().createNewItem((short) listDauThan[i][0]);
                item.itemOptions.add(new Item.ItemOption(player.magicTree.level - 1 > 1 ? 2 : 48,
                        MagicTree.PEA_PARAM[player.magicTree.level - 1]));
                item.quantity = 30;
                return item;
            }
        }
        return item;
    }

    private boolean subIemByItemShop(Player pl, ItemShop itemShop) {

        long cost = itemShop.cost;

        // ==== CASE 1: ĐIỂM SỰ KIỆN ====
        if (itemShop.iconSpec == 14117) {
            if (pl.inventory.coupon >= cost) {
                pl.inventory.coupon -= cost;
                return true;
            }
            Service.gI().sendThongBao(pl, "Không đủ điểm sự kiện");
            return false;
        }

        // ==== CASE 2: VÀNG ====
        if (itemShop.iconSpec == 76 || itemShop.iconSpec == 188
                || itemShop.iconSpec == 189 || itemShop.iconSpec == 190) {

            if (pl.inventory.gold >= cost) {
                pl.inventory.gold -= cost;
                return true;
            }
            Service.gI().sendThongBao(pl, "Không đủ vàng");
            return false;
        }

        // ==== CASE 3: HỒNG NGỌC ====
        if (itemShop.iconSpec == 861) {
            if (pl.inventory.ruby >= cost) {
                pl.inventory.ruby -= cost;
                return true;
            }
            Service.gI().sendThongBao(pl, "Không đủ hồng ngọc");
            return false;
        }

        // ===================================================
        // ==== CASE 4: ITEM (iconSpec = ITEM ID) ============
        // ===================================================
        // ===== ITEM 457 (ưu tiên trừ 457 -> 1810) =====
        if (itemShop.iconSpec == 457) {

            long total = 0;

            // đếm tổng trước
            for (Item item : pl.inventory.itemsBag) {
                if (item == null || !item.isNotNullItem()) {
                    continue;
                }

                if (item.template.id == 457 || item.template.id == 1810) {
                    total += item.quantity;
                }
            }

            if (total < cost) {
                Service.gI().sendThongBao(pl, "Không đủ vật phẩm thanh toán");
                return false;
            }

            long need = cost;

            // trừ 457 trước
            for (Item item : pl.inventory.itemsBag) {
                if (item == null || !item.isNotNullItem()) {
                    continue;
                }

                if (item.template.id == 457) {
                    int sub = (int) Math.min(item.quantity, need);
                    InventoryService.gI().subQuantityItemsBag(pl, item, sub);
                    need -= sub;
                    if (need <= 0) {
                        return true;
                    }
                }
            }

            // trừ 1810 sau
            for (Item item : pl.inventory.itemsBag) {
                if (item == null || !item.isNotNullItem()) {
                    continue;
                }

                if (item.template.id == 1810) {
                    int sub = (int) Math.min(item.quantity, need);
                    InventoryService.gI().subQuantityItemsBag(pl, item, sub);
                    need -= sub;
                    if (need <= 0) {
                        return true;
                    }
                }
            }

            return true;
        }

        // ===== ITEM THƯỜNG =====
        long total = 0;

        // kiểm tra đủ trước
        for (Item item : pl.inventory.itemsBag) {
            if (item == null || !item.isNotNullItem()) {
                continue;
            }

            if (item.template.id == itemShop.iconSpec) {
                total += item.quantity;
            }
        }

        if (total < cost) {
            Service.gI().sendThongBao(pl, "Không đủ vật phẩm thanh toán");
            return false;
        }

        long need = cost;

        // đủ rồi mới trừ
        for (Item item : pl.inventory.itemsBag) {
            if (item == null || !item.isNotNullItem()) {
                continue;
            }

            if (item.template.id == itemShop.iconSpec) {
                int sub = (int) Math.min(item.quantity, need);
                InventoryService.gI().subQuantityItemsBag(pl, item, sub);
                need -= sub;

                if (need <= 0) {
                    return true;
                }
            }
        }

        return true;
    }

    public void showConfirmSellItem(Player pl, int where, int index) {
        Item item = null;
        if (where == 0) {
            if (index < 0) {
                Service.gI().sendThongBao(pl, "Không thể thực hiện");
                return;
            }
            item = pl.inventory.itemsBody.get(index);
        } else {
            // if (pl.getSession().version < 220) {
            // index -= (pl.inventory.itemsBody.size() - 7);
            // }
            item = pl.inventory.itemsBag.get(index);
        }
        if (item != null && item.isNotNullItem()) {
            if (item.template.id == 570) {
                Service.gI().sendThongBao(pl, "Bạn không thể bán vật phẩm này");
                return;
            }
            int quantity = item.quantity;
            int cost = item.template.gold;
            if (item.template.id == 457) {
                if (quantity > 1) {
                    Input.gI().createFormBanSLL(pl);
                    return;
                }
                quantity = 1;
            }
            if (item.template.id == 1810) {
                if (quantity > 1) {
                    Input.gI().createFormBanSLL2(pl);
                    return;
                }
                quantity = 1;
            } else {
                cost /= 4;
            }
            if (cost == 0) {
                cost = 1;
            }
            cost *= quantity;

            String text = "Bạn có muốn bán\nx" + quantity
                    + " " + item.template.name + "\nvới giá là " + Util.numberToMoney(cost) + " vàng?";
            Message msg = null;
            try {
                msg = new Message(7);
                msg.writer().writeByte(where);
                msg.writer().writeShort(index);
                msg.writer().writeUTF(text);
                pl.sendMessage(msg);
            } catch (Exception e) {
            } finally {
                if (msg != null) {
                    msg.cleanup();
                }
            }
        }
    }

    public void sellItem(Player pl, int where, int index) {
        if (pl.iDMark.getShopOpen() == null || pl.iDMark.getTagNameShop() == null) {
            Service.gI().sendThongBao(pl, "Không thể thực hiện");
            return;
        }
        if (index < 0) {
            Service.gI().sendThongBao(pl, "Không thể thực hiện");
            return;
        }
        Item item = null;
        if (where == 0) {
            item = pl.inventory.itemsBody.get(index);
        } else {
            item = pl.inventory.itemsBag.get(index);
        }
        if (item != null) {
            if (item.template.id == 570) {
                Service.gI().sendThongBao(pl, "Bạn không thể bán vật phẩm này");
                return;
            }
            if (InventoryService.gI().getParam(pl, 93, item.template.id) > 0) {
                Service.gI().sendThongBao(pl, "Bạn không thể bán vật phẩm có hạn sử dụng");
                return;
            }
            int quantity = item.quantity;
            int cost = item.template.gold;
            if (item.template.id == 457) {
                quantity = 1;
            } else {
                cost /= 4;
            }
            if (cost == 0) {
                cost = 1;
            }
            cost *= quantity;

            if (pl.inventory.gold + cost > Inventory.LIMIT_GOLD) {
                Service.gI().sendThongBao(pl, "Vàng sau khi bán vượt quá giới hạn");
                return;
            }
            pl.inventory.gold += cost;
            Service.gI().sendMoney(pl);
            Service.gI().sendThongBao(pl, "Đã bán " + item.template.name
                    + " thu được " + Util.numberToMoney(cost) + " vàng");

            // Add vật phẩm đã bán
            if (item.template.id != 457) {
                VatPhamDaBan.gI().addItem(pl, item);
            }
            if (where == 0) {
                InventoryService.gI().subQuantityItemsBody(pl, item, quantity);
                InventoryService.gI().sendItemBody(pl);
                Service.gI().Send_Caitrang(pl);
            } else {
                InventoryService.gI().subQuantityItemsBag(pl, item, quantity);
                InventoryService.gI().sendItemBag(pl);
            }
            if ("BUNMA".equals(pl.iDMark.getTagNameShop())
                    || "DENDE".equals(pl.iDMark.getTagNameShop())
                    || "APPULE".equals(pl.iDMark.getTagNameShop())) {
                AchievementService.gI().checkDoneTask(pl, ConstAchievement.TRUM_NHAT_VE_CHAI);
            }
        } else {
            Service.gI().sendThongBao(pl, "Không thể thực hiện");
        }
    }

    private void getItemSideBoxLuckyRound(Player player, List<Item> items, byte type, int index) {
        if (items == null) {
            return;
        }
        if (index < 0 || index >= items.size()) {
            Service.gI().sendThongBao(player, "Không thể thực hiện");
            return;
        }
        Item item = items.get(index);
        switch (type) {
            case 0: // nhận
                if (item.isNotNullItem()) {
                    if (InventoryService.gI().getCountEmptyBag(player) != 0) {
                        InventoryService.gI().addItemBag(player, item);
                        Service.gI().sendThongBao(player,
                                "Bạn nhận được " + (item.template.id == 189
                                        ? Util.numberToMoney(item.quantity) + " vàng"
                                        : item.template.name));
                        InventoryService.gI().sendItemBag(player);
                        items.remove(index);
                    } else {
                        Service.gI().sendThongBao(player, "Hành trang đã đầy");
                    }
                } else {
                    Service.gI().sendThongBao(player, "Không thể thực hiện");
                }
                break;
            case 1: // xóa
                items.remove(index);
                Service.gI().sendThongBao(player, "Xóa vật phẩm thành công");
                break;
            case 2: // nhận hết
                for (int i = items.size() - 1; i >= 0; i--) {
                    item = items.get(i);
                    if (InventoryService.gI().addItemBag(player, item)) {
                        Service.gI().sendThongBao(player,
                                "Bạn nhận được " + (item.template.id == 189
                                        ? Util.numberToMoney(item.quantity) + " vàng"
                                        : item.template.name));
                        items.remove(i);
                    }
                }
                InventoryService.gI().sendItemBag(player);
                break;
        }
        openShopType4(player, player.iDMark.getTagNameShop(), items);
    }

    private void getItemSideMailsBox(Player player, List<Item> items, byte type, int index) {
        if (items == null) {
            return;
        }
        if (index < 0 || index >= items.size()) {
            Service.gI().sendThongBao(player, "Không thể thực hiện");
            return;
        }
        Item item = items.get(index);
        switch (type) {
            case 0: // nhận
                if (item.isNotNullItem()) {
                    if (InventoryService.gI().getCountEmptyBag(player) != 0) {
                        InventoryService.gI().addItemBag(player, item);
                        Service.gI().sendThongBao(player,
                                "Bạn nhận được " + (item.template.id == 189
                                        ? Util.numberToMoney(item.quantity) + " vàng"
                                        : item.template.name));
                        InventoryService.gI().sendItemBag(player);
                        items.remove(index);
                    } else {
                        Service.gI().sendThongBao(player, "Hành trang đã đầy");
                    }
                } else {
                    Service.gI().sendThongBao(player, "Không thể thực hiện");
                }
                break;
            case 1: // xóa
                items.remove(index);
                Service.gI().sendThongBao(player, "Xóa vật phẩm thành công");
                break;
            case 2: // nhận hết
                for (int i = items.size() - 1; i >= 0; i--) {
                    item = items.get(i);
                    if (InventoryService.gI().addItemBag(player, item)) {
                        Service.gI().sendThongBao(player,
                                "Bạn nhận được " + (item.template.id == 189
                                        ? Util.numberToMoney(item.quantity) + " vàng"
                                        : item.template.name));
                        items.remove(i);
                    }
                }
                InventoryService.gI().sendItemBag(player);
                break;
        }
        openShopType4(player, player.iDMark.getTagNameShop(), items);
    }

    private void buyItemDaBan(Player player, List<Item> items, byte type, int index) {
        if (items == null) {
            return;
        }
        if (index >= items.size()) {
            Service.gI().sendThongBao(player, "Không thể thực hiện");
            return;
        }
        Item item = items.get(index);
        int giamualaingoc = item.template.gem / 2;
        int giamualaivang = giamualaingoc == 0
                ? (int) item.template.gold / 2 > 0 ? (int) item.template.gold / 2 : item.quantity * 100
                : 0;
        if (giamualaivang > 0 && player.inventory.gold < giamualaivang) {
            Service.gI().sendThongBao(player, "Bạn không có đủ vàng!");
            return;
        }
        if (giamualaingoc > 0 && player.inventory.gem < giamualaingoc) {
            Service.gI().sendThongBao(player, "Bạn không có đủ ngọc xanh!");
            return;
        }
        player.inventory.gem -= giamualaingoc;
        player.inventory.gold -= giamualaivang;
        Service.gI().sendMoney(player);
        if (item.isNotNullItem()) {
            if (InventoryService.gI().getCountEmptyBag(player) != 0) {
                InventoryService.gI().addItemBag(player, item);
                Service.gI().sendThongBao(player,
                        "Bạn nhận được " + (item.template.id == 189
                                ? Util.numberToMoney(item.quantity) + " vàng"
                                : item.template.name));
                InventoryService.gI().sendItemBag(player);
                items.remove(index);
            } else {
                Service.gI().sendThongBao(player, "Hành trang đã đầy");
            }
        } else {
            Service.gI().sendThongBao(player, "Không thể thực hiện");
        }
        openShopType8(player, player.iDMark.getTagNameShop(), items);
    }

    private void buyItemHD(Player player, int itemTempId) {
        Shop shop = player.iDMark.getShopOpen();
        ItemShop is = shop.getItemShop(itemTempId);
        if (is == null) {
            Service.gI().sendThongBao(player, "Không thể thực hiện");
            return;
        }
        Item item = ItemService.gI().createItemFromItemShop(is);
        if (shop.typeShop == ShopService.SPEC_SHOP) {
            if (!this.subIemByItemShop(player, is)) {
                return;
            }
        }
        if (InventoryService.gI().getCountEmptyBag(player) < 1) {
            Service.gI().sendThongBao(player, "Hành trang đã đầy, không thể chứa thêm.");
            return;
        }
        if (!subMoneyByItemShopV2(player, is)) {
            return;
        }
        if (item.template.level == 14) {
            Item doAn = player.inventory.itemsBag.stream()
                    .filter(it -> it != null && it.template != null
                            && (it.template.id == 663 || it.template.id == 664 || it.template.id == 665
                                    || it.template.id == 666 || it.template.id == 667)
                            && it.quantity >= 99)
                    .findFirst().orElse(null);
            if (doAn != null) {
                InventoryService.gI().subQuantityItemsBag(player, doAn, 99);
            } else {
                Service.gI().sendThongBao(player, "Không có đủ thức ăn");
                return;
            }
        }
        if (player.inventory.itemsBody.stream()
                .noneMatch(it -> it != null && it.template != null && it.template.level == 13)) {
            Service.gI().sendThongBao(player, "Không có đủ set thần");
            return;
        }
        int param = 0;
        if (item.template.level == 14) {
            if (Util.isTrue(25, 100)) {
                param = Util.nextInt(11, 15);
            } else if (Util.isTrue(25, 75)) {
                param = Util.nextInt(5, 10);
            } else {
                param = Util.nextInt(0, 4);
            }
        }
        List<ItemOption> itemoptions = new ArrayList<>();
        if (!item.itemOptions.isEmpty()) {
            for (ItemOption ios : item.itemOptions) {
                if (item.template.level == 14 && InventoryService.gI().optionCanUpgrade(ios.optionTemplate.id)
                        && param > 0) {
                    int id = ios.optionTemplate.id;
                    int param1 = ios.param + (ios.param * param) / 100;
                    itemoptions.add(new ItemOption(id, param1));
                } else if (ios.optionTemplate.id != 164) {
                    itemoptions.add(new ItemOption(ios.optionTemplate.id, ios.param));
                }
            }
        } else {
            itemoptions.add(new ItemOption(73, (short) 0)); // Default option if none
        }
        item.itemOptions.clear();
        item.itemOptions.addAll(itemoptions);
        InventoryService.gI().addItemBag(player, item);
        InventoryService.gI().sendItemBag(player);
        Service.gI().sendThongBao(player, "Mua thành công " + is.temp.name);
    }
}
