package services;

/*
 *
 *
 * @author CongHoan
 */
import models.Template;
import models.Template.ItemOptionTemplate;
import jdbc.DBConnecter;
import item.Item;
import map.ItemMap;
import player.Player;
import shop.ItemShop;
import server.Manager;
import utils.TimeUtil;
import utils.Util;
import item.Item.ItemOption;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.stream.Collectors;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import map.Zone;
import models.Combine.CombineService;
import models.Combine.manifest.NangCapLevelKichHoat;
import shop.Shop;
import shop.TabShop;

public class ItemService {

    private static ItemService i;

    public static ItemService gI() {
        if (i == null) {
            i = new ItemService();
        }
        return i;
    }

    public short getItemIdByIcon(short IconID) {
        for (int i = 0; i < Manager.ITEM_TEMPLATES.size(); i++) {
            if (Manager.ITEM_TEMPLATES.get(i).iconID == IconID) {
                return Manager.ITEM_TEMPLATES.get(i).id;
            }
        }
        return -1;
    }

    public Item createItemNull() {
        Item item = new Item();
        return item;
    }

    public void removeAndAddOptionTemplate(List<Item.ItemOption> itemOptions, int removeId) {
        int id = 0;
        int param = 0;
        Random random = new Random();

        if (removeId == 231) {
            int randomChoice = random.nextInt(100);

            id = (randomChoice < 99) ? 93 : 73;
            param = Util.nextInt(3, 15);

            itemOptions.removeIf(io -> io.optionTemplate.id == 231);

            itemOptions.add(new ItemOption(new Item.ItemOption(id, param)));
        }
    }

    public void removeOption249(List<ItemOption> itemOptions, int purchasedOptionId) {
        if (purchasedOptionId == 249) {
            itemOptions.removeIf(io -> io.optionTemplate.id == 249);
        }
    }

    public void removeOption39(List<ItemOption> itemOptions, int purchasedOptionId) {
        if (purchasedOptionId == 39) {
            itemOptions.removeIf(io -> io.optionTemplate.id == 39);
        }
    }

    public void removeOption76(List<ItemOption> itemOptions, int purchasedOptionId) {
        if (purchasedOptionId == 76) {
            itemOptions.removeIf(io -> io.optionTemplate.id == 76);
        }
    }

    public Item createItemFromItemShop(ItemShop itemShop) {
        Item item = new Item();
        item.template = itemShop.temp;
        item.quantity = 1;
        item.content = item.getContent();
        item.info = item.getInfo();
        for (Item.ItemOption io : itemShop.options) {
            item.itemOptions.add(new Item.ItemOption(io));
            removeAndAddOptionTemplate(item.itemOptions, new Item.ItemOption(io).optionTemplate.id);
            removeOption249(item.itemOptions, new ItemOption(io).optionTemplate.id);
            removeOption76(item.itemOptions, new ItemOption(io).optionTemplate.id);
            removeOption39(item.itemOptions, new ItemOption(io).optionTemplate.id);
        }
        return item;
    }

    public Item copyItem(Item item) {
        Item it = new Item();
        it.itemOptions = new ArrayList<>();
        it.template = item.template;
        it.info = item.info;
        it.content = item.content;

        it.quantity = item.quantity;
        it.createTime = item.createTime;
        for (Item.ItemOption io : item.itemOptions) {
            it.itemOptions.add(new Item.ItemOption(io));
        }
        return it;
    }

    public Item createNewItem(short tempId) {
        return createNewItem(tempId, 1);
    }

    public Item otpts(short tempId) {
        return otpts(tempId, 1);
    }

    public Item createNewItem(short tempId, int quantity) {

        Item item = new Item();
        item.template = getTemplate(tempId);
        if (item.template == null) {
            throw new IllegalArgumentException("Item template không tồn tại: " + tempId);
        }
        item.quantity = quantity;
        item.createTime = System.currentTimeMillis();

        item.content = item.getContent();
        item.info = item.getInfo();
        return item;
    }

    public Item createNewItemLock(int tempId) {
        return createNewItemLock(tempId, 1);
    }

    public Item createNewItemLock(int tempId, int quantity) {
        Item item = new Item();
        item.template = getTemplate(tempId);
        if (item.template == null) {
            throw new IllegalArgumentException("Item template không tồn tại: " + tempId);
        }
        item.quantity = quantity;
        item.createTime = System.currentTimeMillis();
        item.content = item.getContent();
        item.info = item.getInfo();
        item.itemOptions.add(new ItemOption(30, 1));
        return item;
    }

    public Item otpts(short tempId, int quantity) {
        Item item = new Item();
        item.template = getTemplate(tempId);
        if (item.template == null) {
            throw new IllegalArgumentException("Item template không tồn tại: " + tempId);
        }
        item.quantity = quantity;
        item.createTime = System.currentTimeMillis();
        if (item.template.type == 0) {
            item.itemOptions.add(new ItemOption(21, 80));
            item.itemOptions.add(new ItemOption(47, Util.nextInt(2000, 2500)));
        }
        if (item.template.type == 1) {
            item.itemOptions.add(new ItemOption(21, 80));
            item.itemOptions.add(new ItemOption(22, Util.nextInt(120, 180)));
        }
        if (item.template.type == 2) {
            item.itemOptions.add(new ItemOption(21, 80));
            item.itemOptions.add(new ItemOption(0, Util.nextInt(11500, 17000)));
        }
        if (item.template.type == 3) {
            item.itemOptions.add(new ItemOption(21, 80));
            item.itemOptions.add(new ItemOption(23, Util.nextInt(120, 182)));
        }
        if (item.template.type == 4) {
            item.itemOptions.add(new ItemOption(21, 80));
            item.itemOptions.add(new ItemOption(14, Util.nextInt(20, 25)));
        }
        item.content = item.getContent();
        item.info = item.getInfo();
        return item;
    }

    public Item createItemSetKichHoat(int tempId, int quantity) {
        Item item = new Item();
        item.template = getTemplate(tempId);
        if (item.template == null) {
            throw new IllegalArgumentException("Item template không tồn tại: " + tempId);
        }
        item.quantity = quantity;
        item.itemOptions = createItemNull().itemOptions;
        item.createTime = System.currentTimeMillis();
        item.content = item.getContent();
        item.info = item.getInfo();
        return item;
    }

    public Item createItemDoHuyDiet(int tempId, int quantity) {
        Item item = new Item();
        item.template = getTemplate(tempId);
        if (item.template == null) {
            throw new IllegalArgumentException("Item template không tồn tại: " + tempId);
        }
        item.quantity = quantity;
        item.itemOptions = createItemNull().itemOptions;
        item.createTime = System.currentTimeMillis();
        item.content = item.getContent();
        item.info = item.getInfo();
        return item;
    }

    public Item createItemFromItemMap(ItemMap itemMap) {
        Item item = createNewItem(itemMap.itemTemplate.id, itemMap.quantity);
        item.itemOptions = itemMap.options;
        return item;
    }

    public ItemOptionTemplate getItemOptionTemplate(int id) {
        return Manager.ITEM_OPTION_TEMPLATES.get(id);
    }

    public Template.ItemTemplate getTemplate(int id) {
        if (id < 0) {
            return null;
        }
        if (!Manager.ITEM_TEMPLATE_IDS.contains(id)) {
            return null;
        }
        if (id < Manager.ITEM_TEMPLATES.size()) {
            Template.ItemTemplate template = Manager.ITEM_TEMPLATES.get(id);
            if (template != null && template.id == id) {
                return template;
            }
        }
        for (Template.ItemTemplate template : Manager.ITEM_TEMPLATES) {
            if (template != null && template.id == id) {
                return template;
            }
        }
        return null;
    }

    public boolean isItemActivation(Item item) {
        return false;
    }

    public int getPercentTrainArmor(Item item) {
        if (item != null) {
            switch (item.template.id) {
                case 529:
                case 534:
                    return 10;
                case 530:
                case 535:
                    return 20;
                case 531:
                case 536:
                    return 30;
                case 1771:
                    return 40;
                default:
                    return 0;
            }
        } else {
            return 0;
        }
    }

    public boolean isTrainArmor(Item item) {
        if (item != null) {
            switch (item.template.id) {
                case 529:
                case 534:
                case 530:
                case 535:
                case 531:
                case 536:
                case 1771:
                    return true;
                default:
                    return false;
            }
        } else {
            return false;
        }
    }

    public boolean isOutOfDateTime(Item item) {
        if (item != null) {
            for (Item.ItemOption io : item.itemOptions) {
                if (io.optionTemplate.id == 93) {
                    int dayPass = (int) TimeUtil.diffDate(new Date(), new Date(item.createTime), TimeUtil.DAY);
                    if (dayPass != 0) {
                        io.param -= dayPass;
                        if (io.param <= 0) {
                            return true;
                        } else {
                            item.createTime = System.currentTimeMillis();
                        }
                    }
                }
            }
        }
        return false;
    }

    public int randomSKHId(byte gender) {
        if (gender == 3) {
            gender = 2;
        }
        int[][] options = {{128, 129, 127}, {131, 132, 130}, {133, 135, 134}};
        int skhv1 = 25;
        int skhv2 = 35;
        int skhc = 40;
        int skhId = -1;
        int rd = Util.nextInt(1, 100);
        if (rd <= skhv1) {
            skhId = 0;
        } else if (rd <= skhv1 + skhv2) {
            skhId = 1;
        } else if (rd <= skhv1 + skhv2 + skhc) {
            skhId = 2;
        }
        if (gender == 0 && skhId == 0 && Util.isTrue(50, 100)) {
            return 214;
        }
        return options[gender][skhId];
    }

    public void OpenItem736(Player player, Item itemUse) {
        try {
            if (InventoryService.gI().getCountEmptyBag(player) <= 1) {
                Service.gI().sendThongBao(player, "Bạn phải có ít nhất 2 ô trống hành trang");
                return;
            }
            short[] icon = new short[2];
            int rd = Util.nextInt(1, 100);
            int rac = 50;
            int ruby = 20;
            int dbv = 10;
            int vb = 10;
            int bh = 5;
            int ct = 5;
            Item item = randomRac();
            if (rd <= rac) {
                item = randomRac();
            } else if (rd <= rac + ruby) {
                item = createItemSetKichHoat(861, 1);
            } else if (rd <= rac + ruby + dbv) {
                item = daBaoVe();
            } else if (rd <= rac + ruby + dbv + vb) {
                item = vanBay2011(true);
            } else if (rd <= rac + ruby + dbv + vb + bh) {
                item = phuKien2011(true);
            } else if (rd <= rac + ruby + dbv + vb + bh + ct) {
                item = caitrang2011(true);
            }
            if (item.template.id == 861) {
                item.quantity = Util.nextInt(10, 30);
            }
            icon[0] = itemUse.template.iconID;
            icon[1] = item.template.iconID;
            InventoryService.gI().subQuantityItemsBag(player, itemUse, 1);
            InventoryService.gI().addItemBag(player, item);
            InventoryService.gI().sendItemBag(player);
            player.inventory.event++;
            Service.gI().sendThongBao(player, "Bạn đã nhận được " + item.template.name);
            CombineService.gI().sendEffectOpenItem(player, icon[0], icon[1]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void OpenItem648(Player player, Item itemUse) {
        try {
            if (InventoryService.gI().getCountEmptyBag(player) <= 1) {
                Service.gI().sendThongBao(player, "Bạn phải có ít nhất 2 ô trống hành trang");
                return;
            }
            short[] icon = new short[2];
            int rd = Util.nextInt(1, 100);
            int rac = 50;
            int ruby = 20;
            int dbv = 10;
            int vb = 10;
            int bh = 5;
            int ct = 5;
            Item item = randomRac();
            if (rd <= rac) {
                item = randomRac2();
            } else if (rd <= rac + ruby) {
                item = createItemSetKichHoat(861, 1);
            } else if (rd <= rac + ruby + dbv) {
                item = vatphamsk(true);
            } else if (rd <= rac + ruby + dbv + vb) {
                item = vanBayChrimas(true);
            } else if (rd <= rac + ruby + dbv + vb + bh) {
                item = phuKienChristmas(true);
            } else if (rd <= rac + ruby + dbv + vb + bh + ct) {
                item = caitrangChristmas(true);
            }
            if (item.template.id == 861) {
                item.quantity = Util.nextInt(10, 30);
            }
            icon[0] = itemUse.template.iconID;
            icon[1] = item.template.iconID;
            InventoryService.gI().subQuantityItemsBag(player, itemUse, 1);
            InventoryService.gI().addItemBag(player, item);
            InventoryService.gI().sendItemBag(player);
            player.inventory.event++;
            Service.gI().sendThongBao(player, "Bạn đã nhận được " + item.template.name);
            CombineService.gI().sendEffectOpenItem(player, icon[0], icon[1]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void OpenItem1759(Player player, Item itemUse) {
        try {
            if (InventoryService.gI().getCountEmptyBag(player) <= 1) {
                Service.gI().sendThongBao(player, "Bạn phải có ít nhất 2 ô trống hành trang");
                return;
            }
            short[] icon = new short[2];
            int rd = Util.nextInt(1, 100);
            int rac = 20;
            int ruby = 20;
            int dbv = 20;
            int vb = 20;
            int bh = 20;
            Item item = randomda();
            if (rd <= rac) {
                item = caitrangchuot(true);
            } else if (rd <= rac + ruby) {
                item = caitrangho(true);
            } else if (rd <= rac + ruby + dbv) {
                item = randomda();
            } else if (rd <= rac + ruby + dbv + vb) {
                item = petran(true);
            }
            icon[0] = itemUse.template.iconID;
            icon[1] = item.template.iconID;
            InventoryService.gI().subQuantityItemsBag(player, itemUse, 1);
            InventoryService.gI().addItemBag(player, item);
            InventoryService.gI().sendItemBag(player);
            Service.gI().sendThongBao(player, "Bạn đã nhận được " + item.template.name);
            CombineService.gI().sendEffectOpenItem(player, icon[0], icon[1]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void OpenItem1594(Player player, Item itemUse) {
        try {
            if (InventoryService.gI().getCountEmptyBag(player) <= 1) {
                Service.gI().sendThongBao(player, "Bạn phải có ít nhất 2 ô trống hành trang");
                return;
            }
            short[] icon = new short[2];
            int rd = Util.nextInt(1, 100);
            int vp1 = 20;
            int vp2 = 20;
            int vp3 = 20;
            int vp4 = 20;
            int vp5 = 20;
            Item item = randomRac();
            if (rd <= vp1) {
                item = luoihaihong(true);
            } else if (rd <= vp1 + vp2) {
                item = luoihaihong(true);
            } else if (rd <= vp1 + vp2 + vp3) {
                item = luoihaihong(true);
            } else if (rd <= vp1 + vp2 + vp3 + vp4) {
                item = luoihaihong(true);
            } else if (rd <= vp1 + vp2 + vp3 + vp4 + vp5) {
                item = luoihaihong(true);
            }
            icon[0] = itemUse.template.iconID;
            icon[1] = item.template.iconID;
            InventoryService.gI().subQuantityItemsBag(player, itemUse, 1);
            InventoryService.gI().addItemBag(player, item);
            InventoryService.gI().sendItemBag(player);
            Service.gI().sendThongBao(player, "Bạn đã nhận được " + item.template.name);
            CombineService.gI().sendEffectOpenItem(player, icon[0], icon[1]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void OpenItem1592(Player player, Item itemUse) {
        try {
            if (InventoryService.gI().getCountEmptyBag(player) <= 1) {
                Service.gI().sendThongBao(player, "Bạn phải có ít nhất 2 ô trống hành trang");
                return;
            }
            short[] icon = new short[2];
            int rd = Util.nextInt(1, 100);
            int vp1 = 100;
            Item item = randomRac();
            if (rd <= vp1) {
                item = pilong(true);
            }
            icon[0] = itemUse.template.iconID;
            icon[1] = item.template.iconID;
            InventoryService.gI().subQuantityItemsBag(player, itemUse, 1);
            InventoryService.gI().addItemBag(player, item);
            InventoryService.gI().sendItemBag(player);
            Service.gI().sendThongBao(player, "Bạn đã nhận được " + item.template.name);
            CombineService.gI().sendEffectOpenItem(player, icon[0], icon[1]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void OpenItem1757(Player player, Item itemUse) {
        try {
            if (InventoryService.gI().getCountEmptyBag(player) <= 1) {
                Service.gI().sendThongBao(player, "Bạn phải có ít nhất 2 ô trống hành trang");
                return;
            }
            short[] icon = new short[2];
            int rd = Util.nextInt(1, 100);
            int rac = 20;
            int ruby = 20;
            int dbv = 20;
            int vb = 20;
            int bh = 20;
            Item item = randomda();
            if (rd <= rac) {
                item = cobaolixi(true);
            } else if (rd <= rac + ruby) {
                item = caitrangthantai(true);
            } else if (rd <= rac + ruby + dbv) {
                item = thoivangbay(true);
            } else if (rd <= rac + ruby + dbv + vb) {
                item = bunmathanhlich(true);
            }
            icon[0] = itemUse.template.iconID;
            icon[1] = item.template.iconID;
            InventoryService.gI().subQuantityItemsBag(player, itemUse, 1);
            InventoryService.gI().addItemBag(player, item);
            InventoryService.gI().sendItemBag(player);
            player.event.addEventPointLixi(1);
            Service.gI().sendThongBao(player, "Bạn đã nhận được " + item.template.name);
            CombineService.gI().sendEffectOpenItem(player, icon[0], icon[1]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void OpenItem1806(Player player, Item itemUse) {
        try {

            if (InventoryService.gI().getCountEmptyBag(player) < 5) {
                Service.gI().sendThongBao(player, "Bạn phải có ít nhất 5 ô trống hành trang");
                return;
            }

            int[][] items = {
                {0, 6, 21, 27, 12},
                {1, 7, 22, 28, 12},
                {2, 8, 23, 29, 12}
            };

            int select = player.gender;

            if (select < 0 || select >= items.length) {
                return;
            }

            short[] icon = new short[2];
            icon[0] = itemUse.template.iconID;

            for (int itemId : items[select]) {

                Item itemNew = createItemSetKichHoat(itemId, 1);

                // ===== option riêng theo item =====
                if (itemId == 0 || itemId == 1 || itemId == 2) {
                    itemNew.itemOptions.add(new Item.ItemOption(47, 3));
                } else if (itemId == 6 || itemId == 7 || itemId == 8) {
                    itemNew.itemOptions.add(new Item.ItemOption(6, 20));
                } else if (itemId == 21 || itemId == 22 || itemId == 23) {
                    itemNew.itemOptions.add(new Item.ItemOption(0, 3));
                } else if (itemId == 27 || itemId == 28 || itemId == 29) {
                    itemNew.itemOptions.add(new Item.ItemOption(7, 10));
                } else if (itemId == 12) {
                    itemNew.itemOptions.add(new Item.ItemOption(14, 1));
                }

                // option set hdpe
                itemNew.itemOptions.add(new Item.ItemOption(179, 0));
                itemNew.itemOptions.add(new Item.ItemOption(180, 0));

                // option kích hoạt
                itemNew.itemOptions.add(new Item.ItemOption(30, 1));

                InventoryService.gI().addItemBag(player, itemNew);

                icon[1] = itemNew.template.iconID;
            }

            InventoryService.gI().subQuantityItemsBag(player, itemUse, 1);
            InventoryService.gI().sendItemBag(player);

            player.inventory.event++;

            Service.gI().sendThongBao(player, "Bạn đã nhận được set tnsm Horizon!");
            CombineService.gI().sendEffectOpenItem(player, icon[0], icon[1]);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void OpenItem1576(Player player, Item itemUse) {
        try {
            if (InventoryService.gI().getCountEmptyBag(player) <= 1) {
                Service.gI().sendThongBao(player, "Bạn phải có ít nhất 2 ô trống hành trang");
                return;
            }
            short[] icon = new short[2];
            int rd = Util.nextInt(1, 100);
            int rac = 20;
            int ruby = 20;
            int dbv = 20;
            int vb = 20;
            int bh = 20;
            Item item = daolong(true);
            if (rd <= rac) {
                item = daolong(true);
            } else if (rd <= rac + ruby) {
                item = bena(true);
            } else if (rd <= rac + ruby + dbv) {
                item = cacheprong(true);
            } else if (rd <= rac + ruby + dbv + vb) {
                item = caitrangtet(true);
            }
            icon[0] = itemUse.template.iconID;
            icon[1] = item.template.iconID;
            InventoryService.gI().subQuantityItemsBag(player, itemUse, 1);
            InventoryService.gI().addItemBag(player, item);
            InventoryService.gI().sendItemBag(player);
            player.event.addEventPointPhaobong(1);
            Service.gI().sendThongBao(player, "Bạn đã nhận được " + item.template.name);
            CombineService.gI().sendEffectOpenItem(player, icon[0], icon[1]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void OpenItem1758(Player player, Item itemUse) {
        try {
            if (InventoryService.gI().getCountEmptyBag(player) <= 1) {
                Service.gI().sendThongBao(player, "Bạn phải có ít nhất 2 ô trống hành trang");
                return;
            }
            short[] icon = new short[2];
            int rd = Util.nextInt(1, 100);
            int rac = 20;
            int ruby = 20;
            int dbv = 20;
            int vb = 20;
            int bh = 20;
            Item item = randomda();
            if (rd <= rac) {
                item = cobaolixi(true);
            } else if (rd <= rac + ruby) {
                item = caitrangthantai(true);
            } else if (rd <= rac + ruby + dbv) {
                item = thoivangbay(true);
            } else if (rd <= rac + ruby + dbv + vb) {
                item = bunmathanhlich(true);
            }
            icon[0] = itemUse.template.iconID;
            icon[1] = item.template.iconID;
            InventoryService.gI().subQuantityItemsBag(player, itemUse, 1);
            InventoryService.gI().addItemBag(player, item);
            InventoryService.gI().sendItemBag(player);
            player.event.addEventPointLixi(1);
            Service.gI().sendThongBao(player, "Bạn đã nhận được " + item.template.name);
            CombineService.gI().sendEffectOpenItem(player, icon[0], icon[1]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Item itemSKH(int itemId, int skhId) {
        Item item = createItemSetKichHoat(itemId, 1);
        if (item != null) {
            item.itemOptions.addAll(ItemService.gI().getListOptionItemShop((short) itemId));
            item.itemOptions.add(new Item.ItemOption(skhId, 1));
            item.itemOptions.add(new Item.ItemOption(optionIdSKH(skhId), 1));
            item.itemOptions.add(new Item.ItemOption(30, 1));
        }
        return item;
    }

    public int optionItemSKH(int typeItem) {
        switch (typeItem) {
            case 0:
                return 47;
            case 1:
                return 6;
            case 2:
                return 0;
            case 3:
                return 7;
            default:
                return 14;
        }
    }

    public int pagramItemSKH(int typeItem) {
        switch (typeItem) {
            case 0:
            case 2:
                return Util.nextInt(5);
            case 1:
            case 3:
                return Util.nextInt(20, 30);
            default:
                return Util.nextInt(3);
        }
    }

    public int optionIdSKH(int skhId) {
        switch (skhId) {
            case 127: // Set Taiyoken
                return 139;
            case 128: // Set Genki
                return 140;
            case 129: // Set Kamejoko
                return 141;
            case 130: // Set KI
                return 142;
            case 131: // Set Dame
                return 143;
            case 132: // Set Summon
                return 144;
            case 133: // Set Galick
                return 136;
            case 134: // Set Monkey
                return 137;
            case 135: // Set HP
                return 138;
            case 251: // Set Lien Hoàn
                return 254;

        }
        return 0;
    }

    public Item itemDHD(int itemId, int dhdId) {
        Item item = createItemSetKichHoat(itemId, 1);
        if (item != null) {
            item.itemOptions.add(new Item.ItemOption(dhdId, 1));
            item.itemOptions.add(new Item.ItemOption(optionIdDHD(dhdId), 1));
            item.itemOptions.add(new Item.ItemOption(30, 1));
        }
        return item;
    }

    public int optionIdDHD(int skhId) {
        switch (skhId) {
            case 127: // Set Taiyoken
                return 139;
            case 128: // Set Genki
                return 140;
            case 129: // Set Kamejoko
                return 141;
            case 130: // Set KI
                return 142;
            case 131: // Set Dame
                return 143;
            case 132: // Set Summon
                return 144;
            case 133: // Set Galick
                return 136;
            case 134: // Set Monkey
                return 137;
            case 135: // Set HP
                return 138;
            case 251: // Set Lien Hoàn
                return 254;

        }
        return 0;
    }

    public Item randomCS_DHD(int itemId, int gender) {
        Item it = createItemSetKichHoat(itemId, 1);
        List<Integer> ao = Arrays.asList(650, 652, 654);
        List<Integer> quan = Arrays.asList(651, 653, 655);
        List<Integer> gang = Arrays.asList(657, 659, 661);
        List<Integer> giay = Arrays.asList(658, 660, 662);
        int nhd = 656;
        if (ao.contains(itemId)) {
            it.itemOptions
                    .add(new Item.ItemOption(47, Util.highlightsItem(gender == 2, new Random().nextInt(1001) + 1800))); // áo
            // từ
            // 1800-2800
            // giáp
        }
        if (quan.contains(itemId)) {
            it.itemOptions
                    .add(new Item.ItemOption(22, Util.highlightsItem(gender == 0, new Random().nextInt(16) + 85))); // hp
            // 85-100k
        }
        if (gang.contains(itemId)) {
            it.itemOptions
                    .add(new Item.ItemOption(0, Util.highlightsItem(gender == 2, new Random().nextInt(150) + 8500))); // 8500-10000
        }
        if (giay.contains(itemId)) {
            it.itemOptions
                    .add(new Item.ItemOption(23, Util.highlightsItem(gender == 1, new Random().nextInt(11) + 80))); // ki
            // 80-90k
        }
        if (nhd == itemId) {
            it.itemOptions.add(new Item.ItemOption(14, new Random().nextInt(3) + 17)); // chí mạng 17-19%
        }
        it.itemOptions.add(new Item.ItemOption(21, 80));// yêu cầu sm 80 tỉ
        it.itemOptions.add(new Item.ItemOption(30, 1));// ko the gd
        return it;
    }

    // Cải trang sự kiện 20/11
    public Item caitrang2011(boolean rating) {
        Item item = createItemSetKichHoat(680, 1);
        item.itemOptions.add(new Item.ItemOption(76, 1));// VIP
        item.itemOptions.add(new Item.ItemOption(77, 28));// hp 28%
        item.itemOptions.add(new Item.ItemOption(103, 25));// ki 25%
        item.itemOptions.add(new Item.ItemOption(147, 24));// sd 26%
        if (Util.isTrue(995, 1000) && rating) {// tỉ lệ ra hsd
            item.itemOptions.add(new Item.ItemOption(93, new Random().nextInt(3) + 1));// hsd
        }
        return item;
    }

    // Cải trang sự kiện giáng sinh
    public Item caitrangChristmas(boolean rating) {
        Item item = createItemSetKichHoat(Util.nextInt(386, 394), 1);
        item.itemOptions.add(new Item.ItemOption(77, Util.nextInt(15, 51)));
        item.itemOptions.add(new Item.ItemOption(103, Util.nextInt(15, 51)));
        item.itemOptions.add(new Item.ItemOption(147, Util.nextInt(15, 20)));
        item.itemOptions.add(new Item.ItemOption(95, Util.nextInt(15, 51)));
        item.itemOptions.add(new Item.ItemOption(5, Util.nextInt(1, 30)));
        item.itemOptions.add(new Item.ItemOption(106, 0));// sd 26%
        if (Util.isTrue(995, 1000) && rating) {// tỉ lệ ra hsd
            item.itemOptions.add(new Item.ItemOption(93, new Random().nextInt(3) + 1));// hsd
        }
        return item;
    }

    public Item luoihaihong(boolean rating) {
        Item item = createItemSetKichHoat(1124, 1);
        item.itemOptions.add(new Item.ItemOption(50, Util.nextInt(5, 15)));
        item.itemOptions.add(new Item.ItemOption(77, Util.nextInt(1, 7)));
        item.itemOptions.add(new Item.ItemOption(103, Util.nextInt(1, 7)));
        item.itemOptions.add(new Item.ItemOption(5, Util.nextInt(5, 12)));
        if (Util.isTrue(95, 100) && rating) {// tỉ lệ ra hsd
            item.itemOptions.add(new Item.ItemOption(93, new Random().nextInt(3) + 1));//hsd
        }
        return item;
    }

    public Item pilong(boolean rating) {
        Item item = createItemSetKichHoat(1468, 1);
        item.itemOptions.add(new Item.ItemOption(50, Util.nextInt(1, 5)));
        item.itemOptions.add(new Item.ItemOption(77, Util.nextInt(1, 5)));
        item.itemOptions.add(new Item.ItemOption(103, Util.nextInt(1, 5)));
        item.itemOptions.add(new Item.ItemOption(30, 0));
        if (Util.isTrue(95, 100) && rating) {// tỉ lệ ra hsd
            item.itemOptions.add(new Item.ItemOption(93, new Random().nextInt(3) + 1));//hsd
        }
        return item;
    }

    // 610 - bong hoa
    // Phụ kiện bó hoa 20/11
    public Item phuKien2011(boolean rating) {
        Item item = createItemSetKichHoat(954, 1);
        item.itemOptions.add(new Item.ItemOption(77, new Random().nextInt(5) + 5));
        item.itemOptions.add(new Item.ItemOption(103, new Random().nextInt(5) + 5));
        item.itemOptions.add(new Item.ItemOption(147, new Random().nextInt(5) + 5));
        if (Util.isTrue(1, 100)) {
            item.itemOptions.get(Util.nextInt(item.itemOptions.size() - 1)).param = 10;
        }
        item.itemOptions.add(new Item.ItemOption(30, 1));// ko the gd
        if (Util.isTrue(995, 1000) && rating) {// tỉ lệ ra hsd
            item.itemOptions.add(new Item.ItemOption(93, new Random().nextInt(3) + 1));// hsd
        }
        return item;
    }

    public Item phuKienChristmas(boolean rating) {
        Item item = createItemSetKichHoat(745, 1);
        item.itemOptions.add(new Item.ItemOption(77, new Random().nextInt(25) + 5));
        item.itemOptions.add(new Item.ItemOption(103, new Random().nextInt(25) + 5));
        item.itemOptions.add(new Item.ItemOption(147, new Random().nextInt(25) + 5));
        if (Util.isTrue(1, 100)) {
            item.itemOptions.get(Util.nextInt(item.itemOptions.size() - 1)).param = 10;
        }
        item.itemOptions.add(new Item.ItemOption(30, 1));// ko the gd
        if (Util.isTrue(995, 1000) && rating) {// tỉ lệ ra hsd
            item.itemOptions.add(new Item.ItemOption(93, new Random().nextInt(3) + 1));// hsd
        }
        return item;
    }

    public Item petran(boolean rating) {
        Item item = createItemSetKichHoat(1760, 1);
        item.itemOptions.add(new Item.ItemOption(50, new Random().nextInt(10) + 5));
        item.itemOptions.add(new Item.ItemOption(77, new Random().nextInt(10) + 5));
        item.itemOptions.add(new Item.ItemOption(103, new Random().nextInt(10) + 5));
        if (Util.isTrue(995, 1000) && rating) {// tỉ lệ ra hsd
            item.itemOptions.add(new Item.ItemOption(93, new Random().nextInt(3) + 1));// hsd
        }
        return item;
    }

    public Item cobaolixi(boolean rating) {
        Item item = createItemSetKichHoat(1478, 1);
        item.itemOptions.add(new Item.ItemOption(50, new Random().nextInt(15) + 5));
        item.itemOptions.add(new Item.ItemOption(77, new Random().nextInt(15) + 5));
        item.itemOptions.add(new Item.ItemOption(103, new Random().nextInt(15) + 5));
        if (Util.isTrue(995, 1000) && rating) {// tỉ lệ ra hsd
            item.itemOptions.add(new Item.ItemOption(93, new Random().nextInt(3) + 1));// hsd
        }
        return item;
    }

    public Item daolong(boolean rating) {
        Item item = createItemSetKichHoat(1502, 1);
        item.itemOptions.add(new Item.ItemOption(50, new Random().nextInt(15) + 5));
        item.itemOptions.add(new Item.ItemOption(77, new Random().nextInt(15) + 5));
        item.itemOptions.add(new Item.ItemOption(103, new Random().nextInt(15) + 5));
        if (Util.isTrue(995, 1000) && rating) {// tỉ lệ ra hsd
            item.itemOptions.add(new Item.ItemOption(93, new Random().nextInt(3) + 1));// hsd
        }
        return item;
    }

    public Item bena(boolean rating) {
        Item item = createItemSetKichHoat(1760, 1);
        item.itemOptions.add(new Item.ItemOption(50, new Random().nextInt(15) + 5));
        item.itemOptions.add(new Item.ItemOption(77, new Random().nextInt(15) + 5));
        item.itemOptions.add(new Item.ItemOption(103, new Random().nextInt(15) + 5));
        if (Util.isTrue(995, 1000) && rating) {// tỉ lệ ra hsd
            item.itemOptions.add(new Item.ItemOption(93, new Random().nextInt(3) + 1));// hsd
        }
        return item;
    }

    public Item cacheprong(boolean rating) {
        Item item = createItemSetKichHoat(1487, 1);
        item.itemOptions.add(new Item.ItemOption(50, new Random().nextInt(15) + 5));
        item.itemOptions.add(new Item.ItemOption(77, new Random().nextInt(15) + 5));
        item.itemOptions.add(new Item.ItemOption(103, new Random().nextInt(15) + 5));
        item.itemOptions.add(new Item.ItemOption(84, new Random().nextInt(15) + 5));
        if (Util.isTrue(995, 1000) && rating) {// tỉ lệ ra hsd
            item.itemOptions.add(new Item.ItemOption(93, new Random().nextInt(3) + 1));// hsd
        }
        return item;
    }

    public Item caitrangtet(boolean rating) {
        Item item = createItemSetKichHoat(Util.nextInt(1498, 1500), 1);
        item.itemOptions.add(new Item.ItemOption(50, new Random().nextInt(30) + 5));
        item.itemOptions.add(new Item.ItemOption(77, new Random().nextInt(30) + 5));
        item.itemOptions.add(new Item.ItemOption(103, new Random().nextInt(30) + 5));
        item.itemOptions.add(new Item.ItemOption(14, new Random().nextInt(1) + 10));
        item.itemOptions.add(new Item.ItemOption(101, new Random().nextInt(10) + 40));
        item.itemOptions.add(new Item.ItemOption(95, new Random().nextInt(10) + 20));
        if (Util.isTrue(995, 1000) && rating) {// tỉ lệ ra hsd
            item.itemOptions.add(new Item.ItemOption(93, new Random().nextInt(3) + 1));// hsd
        }
        return item;
    }

    public Item caitrangthantai(boolean rating) {
        Item item = createItemSetKichHoat(Util.nextInt(1484, 1486), 1);
        item.itemOptions.add(new Item.ItemOption(50, new Random().nextInt(25) + 5));
        item.itemOptions.add(new Item.ItemOption(77, new Random().nextInt(25) + 5));
        item.itemOptions.add(new Item.ItemOption(103, new Random().nextInt(25) + 5));
        item.itemOptions.add(new Item.ItemOption(14, new Random().nextInt(1) + 10));
        item.itemOptions.add(new Item.ItemOption(101, new Random().nextInt(10) + 40));
        item.itemOptions.add(new Item.ItemOption(95, new Random().nextInt(10) + 20));
        if (Util.isTrue(995, 1000) && rating) {// tỉ lệ ra hsd
            item.itemOptions.add(new Item.ItemOption(93, new Random().nextInt(3) + 1));// hsd
        }
        return item;
    }

    public Item thoivangbay(boolean rating) {
        Item item = createItemSetKichHoat(1477, 1);
        item.itemOptions.add(new Item.ItemOption(50, new Random().nextInt(5) + 5));
        item.itemOptions.add(new Item.ItemOption(77, new Random().nextInt(5) + 5));
        item.itemOptions.add(new Item.ItemOption(103, new Random().nextInt(5) + 5));
        item.itemOptions.add(new Item.ItemOption(84, 0));
        if (Util.isTrue(995, 1000) && rating) {// tỉ lệ ra hsd
            item.itemOptions.add(new Item.ItemOption(93, new Random().nextInt(3) + 1));// hsd
        }
        return item;
    }

    public Item bunmathanhlich(boolean rating) {
        Item item = createItemSetKichHoat(1483, 1);
        item.itemOptions.add(new Item.ItemOption(50, new Random().nextInt(25) + 5));
        item.itemOptions.add(new Item.ItemOption(77, new Random().nextInt(25) + 5));
        item.itemOptions.add(new Item.ItemOption(103, new Random().nextInt(25) + 5));
        item.itemOptions.add(new Item.ItemOption(14, new Random().nextInt(1) + 10));
        item.itemOptions.add(new Item.ItemOption(101, new Random().nextInt(10) + 40));
        item.itemOptions.add(new Item.ItemOption(95, new Random().nextInt(10) + 20));
        if (Util.isTrue(995, 1000) && rating) {// tỉ lệ ra hsd
            item.itemOptions.add(new Item.ItemOption(93, new Random().nextInt(3) + 1));// hsd
        }
        return item;
    }

    public Item caitrangchuot(boolean rating) {
        Item item = createItemSetKichHoat(Util.nextInt(754, 756), 1);
        item.itemOptions.add(new Item.ItemOption(77, 20));
        item.itemOptions.add(new Item.ItemOption(27, 50));
        item.itemOptions.add(new Item.ItemOption(50, 15));
        item.itemOptions.add(new Item.ItemOption(93, Util.nextInt(1, 7)));// hsds
        return item;
    }

    public Item caitrangho(boolean rating) {
        Item item = createItemSetKichHoat(Util.nextInt(952, 953), 1);
        item.itemOptions.add(new Item.ItemOption(77, 22));
        item.itemOptions.add(new Item.ItemOption(103, 22));
        item.itemOptions.add(new Item.ItemOption(50, 23));
        item.itemOptions.add(new Item.ItemOption(94, 10));
        item.itemOptions.add(new Item.ItemOption(27, 11));
        item.itemOptions.add(new Item.ItemOption(93, Util.nextInt(1, 7)));// hsds
        return item;
    }

    public Item vanBay2011(boolean rating) {
        Item item = createItemSetKichHoat(795, 1);
        item.itemOptions.add(new Item.ItemOption(89, 1));
        item.itemOptions.add(new Item.ItemOption(30, 1));// ko the gd
        if (Util.isTrue(950, 1000) && rating) {// tỉ lệ ra hsd
            item.itemOptions.add(new Item.ItemOption(93, new Random().nextInt(3) + 1));// hsd
        }
        return item;
    }

    public Item daBaoVe() {
        Item item = createItemSetKichHoat(987, 1);
        item.itemOptions.add(new Item.ItemOption(30, 1));// ko the gd
        return item;
    }

    public Item randomRac() {
        short[] racs = {20, 19, 18, 17};
        Item item = createItemSetKichHoat(racs[Util.nextInt(racs.length - 1)], 1);
        if (optionRac(item.template.id) != 0) {
            item.itemOptions.add(new Item.ItemOption(optionRac(item.template.id), 1));
        }
        return item;
    }

    public Item randomda() {
        Item item = createItemSetKichHoat(Util.nextInt(1074, 1083), 1);
        return item;
    }

    public Item randomRac2() {
        short[] racs = {585, 704, 2048, 379, 384, 385, 381, 828, 829, 830, 831, 832, 833, 834, 835, 836, 837, 838, 839,
            840, 841, 842, 934, 935};
        int idItem = racs[Util.nextInt(racs.length - 1)];
        if (Util.isTrue(1, 100)) {
            idItem = 956;
        }
        Item item = createItemSetKichHoat(idItem, 1);
        if (optionRac(item.template.id) != 0) {
            item.itemOptions.add(new Item.ItemOption(optionRac(item.template.id), 1));
        }
        return item;
    }

    public Item vanBayChrimas(boolean rating) {
        Item item = createItemSetKichHoat(746, 1);
        item.itemOptions.add(new Item.ItemOption(89, 1));
        item.itemOptions.add(new Item.ItemOption(30, 1));// ko the gd
        if (Util.isTrue(950, 1000) && rating) {// tỉ lệ ra hsd
            item.itemOptions.add(new Item.ItemOption(93, new Random().nextInt(3) + 1));// hsd
        }
        return item;
    }

    public byte optionRac(short itemId) {
        switch (itemId) {
            case 220:
                return 71;
            case 221:
                return 70;
            case 222:
                return 69;
            case 224:
                return 67;
            case 223:
                return 68;
            default:
                return 0;
        }
    }

    public Item vatphamsk(boolean hsd) {
        int[] itemId = {2025, 2026, 2036, 2037, 2038, 2039, 2040, 2019, 2020, 2021, 2022, 2023, 2024, 954, 955, 952,
            953, 924, 860, 742};
        byte[] option = {77, 80, 81, 103, 50, 94, 5};
        byte[] option_v2 = {14, 16, 17, 19, 27, 28, 47, 87}; // 77 %hp // 80 //81 //103 //50 //94 //5 % sdcm
        byte optionid = 0;
        byte optionid_v2 = 0;
        byte param = 0;
        Item lt = ItemService.gI().createNewItem((short) itemId[Util.nextInt(itemId.length)]);
        lt.itemOptions.clear();
        optionid = option[Util.nextInt(0, 6)];
        param = (byte) Util.nextInt(5, 15);
        lt.itemOptions.add(new Item.ItemOption(optionid, param));
        if (Util.isTrue(1, 100)) {
            optionid_v2 = option_v2[Util.nextInt(option_v2.length)];
            lt.itemOptions.add(new Item.ItemOption(optionid_v2, param));
        }
        if (Util.isTrue(999, 1000) && hsd) {
            lt.itemOptions.add(new Item.ItemOption(93, Util.nextInt(1, 7)));
        }
        lt.itemOptions.add(new Item.ItemOption(30, 0));
        return lt;
    }

    public void openBoxVip(Player player) {
        if (InventoryService.gI().getCountEmptyBag(player) <= 1) {
            Service.gI().sendThongBao(player, "Bạn phải có ít nhất 2 ô trống hành trang");
            return;
        }
        if (player.inventory.event < 3000) {
            Service.gI().sendThongBao(player, "Bạn không đủ bông...");
            return;
        }
        Item item;
        if (Util.isTrue(45, 100)) {
            item = caitrang2011(false);
        } else {
            item = phuKien2011(false);
        }
        short[] icon = new short[2];
        icon[0] = 6983;
        icon[1] = item.template.iconID;
        InventoryService.gI().addItemBag(player, item);
        InventoryService.gI().sendItemBag(player);
        player.inventory.event -= 3000;
        Service.gI().sendThongBao(player, "Bạn đã nhận được " + item.template.name);
        CombineService.gI().sendEffectOpenItem(player, icon[0], icon[1]);
    }

    public void giaobong(Player player, int quantity) {
        if (quantity > 10000) {
            return;
        }
        try {
            Item itemUse = InventoryService.gI().findItem(player.inventory.itemsBag, 610);
            if (itemUse.quantity < quantity) {
                Service.gI().sendThongBao(player, "Bạn không đủ bông...");
                return;
            }
            InventoryService.gI().subQuantityItemsBag(player, itemUse, quantity);
            Item item = createItemSetKichHoat(736, (quantity / 100));
            item.itemOptions.add(new Item.ItemOption(30, 1));// ko the gd
            InventoryService.gI().addItemBag(player, item);
            InventoryService.gI().sendItemBag(player);
            Service.gI().sendThongBao(player, "Bạn đã nhận được x" + (quantity / 100) + " " + item.template.name);
        } catch (Exception e) {
            Service.gI().sendThongBao(player, "Bạn không đủ bông...");
        }
    }

    public Item PK_WC(int itemId) {
        Item phukien = createItemSetKichHoat(itemId, 1);
        int co = 983;
        int cup = 982;
        int bong = 966;
        if (cup == itemId) {
            phukien.itemOptions.add(new Item.ItemOption(77, new Random().nextInt(6) + 5)); // hp 5-10%
        }
        if (co == itemId) {
            phukien.itemOptions.add(new Item.ItemOption(103, new Random().nextInt(6) + 5)); // ki 5-10%
        }
        if (bong == itemId) {
            phukien.itemOptions.add(new Item.ItemOption(50, new Random().nextInt(6) + 5)); // sd 5- 10%
        }
        phukien.itemOptions.add(new Item.ItemOption(192, 1));// WORLDCUP
        phukien.itemOptions.add(new Item.ItemOption(193, 1));// (2 món kích hoạt ....)
        if (Util.isTrue(99, 100)) {// tỉ lệ ra hsd
            phukien.itemOptions.add(new Item.ItemOption(93, new Random().nextInt(2) + 1));// hsd
        }
        return phukien;
    }

    // Cải trang Gohan WC
    public Item CT_WC(boolean rating) {
        Item caitrang = createItemSetKichHoat(883, 1);
        caitrang.itemOptions.add(new Item.ItemOption(77, 30));// hp 30%
        caitrang.itemOptions.add(new Item.ItemOption(103, 15));// ki 15%
        caitrang.itemOptions.add(new Item.ItemOption(50, 20));// sd 20%
        caitrang.itemOptions.add(new Item.ItemOption(192, 1));// WORLDCUP
        caitrang.itemOptions.add(new Item.ItemOption(193, 1));// (2 món kích hoạt ....)
        if (Util.isTrue(99, 100) && rating) {// tỉ lệ ra hsd
            caitrang.itemOptions.add(new Item.ItemOption(93, new Random().nextInt(2) + 1));// hsd
        }
        return caitrang;
    }

    public void openDTS(Player player) {
        // check sl đồ tl, đồ hd
        if (player.combine.itemsCombine.stream().filter(item -> item.template.id >= 555 && item.template.id <= 567)
                .count() < 1) {
            Service.gI().sendThongBao(player, "Thiếu đồ thần linh");
            return;
        }
        if (player.combine.itemsCombine.stream().filter(item -> item.template.id >= 650 && item.template.id <= 662)
                .count() < 2) {
            Service.gI().sendThongBao(player, "Thiếu đồ hủy diệt");
            return;
        }
        if (player.combine.itemsCombine.size() != 3) {
            Service.gI().sendThongBao(player, "Thiếu đồ");
            return;
        }
        if (InventoryService.gI().getCountEmptyBag(player) > 0) {
            Item itemTL = player.combine.itemsCombine.stream()
                    .filter(item -> item.template.id >= 555 && item.template.id <= 567).findFirst().get();
            List<Item> itemHDs = player.combine.itemsCombine.stream()
                    .filter(item -> item.template.id >= 650 && item.template.id <= 662).collect(Collectors.toList());
            short[][] itemIds = {{1048, 1051, 1054, 1057, 1060}, {1049, 1052, 1055, 1058, 1061},
            {1050, 1053, 1056, 1059, 1062}}; // thứ tự td - 0,nm - 1, xd - 2

            Item itemTS = DoThienSu(itemIds[player.gender][itemTL.template.type], player.gender);
            InventoryService.gI().addItemBag(player, itemTS);

            InventoryService.gI().subQuantityItemsBag(player, itemTL, 1);
            itemHDs.forEach(item -> InventoryService.gI().subQuantityItemsBag(player, item, 1));

            InventoryService.gI().sendItemBag(player);
            Service.gI().sendThongBao(player, "Bạn đã nhận được " + itemTS.template.name);
        } else {
            Service.gI().sendThongBao(player, "Bạn phải có ít nhất 1 ô trống hành trang");
        }
    }

    public Item DoThienSu(int itemId, int gender) {
        Item dots = createItemSetKichHoat(itemId, 1);
        List<Integer> ao = Arrays.asList(1048, 1049, 1050);
        List<Integer> quan = Arrays.asList(1051, 1052, 1053);
        List<Integer> gang = Arrays.asList(1054, 1055, 1056);
        List<Integer> giay = Arrays.asList(1057, 1058, 1059);
        List<Integer> nhan = Arrays.asList(1060, 1061, 1062);
        // áo
        if (ao.contains(itemId)) {
            dots.itemOptions
                    .add(new Item.ItemOption(47, Util.highlightsItem(gender == 2, new Random().nextInt(1201) + 2800))); // áo
            // từ
            // 2800-4000
            // giáp
        }
        // quần
        if (Util.isTrue(80, 100)) {
            if (quan.contains(itemId)) {
                dots.itemOptions
                        .add(new Item.ItemOption(22, Util.highlightsItem(gender == 0, new Random().nextInt(11) + 120))); // hp
                // 120k-130k
            }
        } else {
            if (quan.contains(itemId)) {
                dots.itemOptions
                        .add(new Item.ItemOption(22, Util.highlightsItem(gender == 0, new Random().nextInt(21) + 130))); // hp
                // 130-150k
                // 15%
            }
        }
        // găng
        if (Util.isTrue(80, 100)) {
            if (gang.contains(itemId)) {
                dots.itemOptions.add(
                        new Item.ItemOption(0, Util.highlightsItem(gender == 2, new Random().nextInt(651) + 9350))); // 9350-10000
            }
        } else {
            if (gang.contains(itemId)) {
                dots.itemOptions.add(
                        new Item.ItemOption(0, Util.highlightsItem(gender == 2, new Random().nextInt(1001) + 10000))); // gang
                // 15%
                // 10-11k
                // -xayda
                // 12k1
            }
        }
        // giày
        if (Util.isTrue(80, 100)) {
            if (giay.contains(itemId)) {
                dots.itemOptions
                        .add(new Item.ItemOption(23, Util.highlightsItem(gender == 1, new Random().nextInt(21) + 90))); // ki
                // 90-110k
            }
        } else {
            if (giay.contains(itemId)) {
                dots.itemOptions
                        .add(new Item.ItemOption(23, Util.highlightsItem(gender == 1, new Random().nextInt(21) + 110))); // ki
                // 110-130k
            }
        }

        if (nhan.contains(itemId)) {
            dots.itemOptions
                    .add(new Item.ItemOption(14, Util.highlightsItem(gender == 1, new Random().nextInt(3) + 18))); // nhẫn
            // 18-20%
        }
        dots.itemOptions.add(new Item.ItemOption(21, 120));
        dots.itemOptions.add(new Item.ItemOption(30, 1));
        return dots;
    }

    public List<Item.ItemOption> getListOptionItemShop(short id) {
        List<Item.ItemOption> list = new ArrayList<>();
        Manager.SHOPS.forEach(shop -> shop.tabShops.forEach(tabShop -> tabShop.itemShops.forEach(itemShop -> {
            if (itemShop.temp.id == id && list.isEmpty()) {
                list.addAll(itemShop.options);
            }
        })));
        return list;
    }

    public int randTempItemKichHoat(int gender) {
        int[][][] items = {{{0, 33}, {1, 41}, {2, 49}}, {{6, 35}, {7, 43}, {8, 51}},
        {{27, 30}, {28, 47}, {29, 55}}, {{21, 24}, {22, 46}, {23, 53}},
        {{12, 57}, {12, 57}, {12, 57}}};
        // a w j g rd
        int type;
        if (Util.isTrue(10, 100)) {
            type = 4; // rada
        } else if (Util.isTrue(30, 100)) {
            type = 3; // gang
        } else if (Util.isTrue(50, 100)) {
            type = 1; // quan
        } else if (Util.isTrue(70, 100)) {
            type = 0; // ao
        } else {
            type = 2; // giay
        }

        return items[type][gender][Util.nextInt(1)];
    }

    public int[] randOptionItemKichHoat(int gender) {
        int op1;
        int op2;
        switch (gender) {
            case 0 -> {
                if (Util.isTrue(70, 100)) {
                    op1 = 128;
                    op2 = 140;
                } else if (Util.isTrue(50, 100)) {
                    op1 = 127;
                    op2 = 139;
                } else if (Util.isTrue(40, 100)) {
                    op1 = 129;
                    op2 = 141;
                } else {
                    op1 = 129;
                    op2 = 141;
                }
            }
            case 1 -> {
                if (Util.isTrue(70, 100)) {
                    op1 = 130;
                    op2 = 142;
                } else if (Util.isTrue(50, 100)) {
                    op1 = 131;
                    op2 = 143;
                } else if (Util.isTrue(40, 100)) {
                    op1 = 251;
                    op2 = 254;
                } else {
                    op1 = 132;
                    op2 = 144;
                }
            }
            default -> {
                if (Util.isTrue(70, 100)) {
                    op1 = 134;
                    op2 = 137;
                } else if (Util.isTrue(50, 100)) {
                    op1 = 135;
                    op2 = 138;
                } else {
                    op1 = 133;
                    op2 = 136;
                }
            }
        }
        int[] options = {op1, op2};
        return options;
    }

    public int[] randOptionItemKichHoatNew(int gender) {
        int op1;
        int op2;
        int op3;
        int op4;
        switch (gender) {
            case 0 -> {

                op1 = 245;
                op2 = 246;
                op3 = 247;
                op4 = 248;

            }
            case 1 -> {

                op1 = 237;
                op2 = 238;
                op3 = 239;
                op4 = 240;

            }
            default -> {

                op1 = 241;
                op2 = 242;
                op3 = 243;
                op4 = 244;

            }
        }
        int[] options = {op1, op2, op3, op4};
        return options;
    }

    public ItemMap randDoTL(Zone zone, int quantity, int x, int y, long id) {
        short idTempTL, type;
        short[] ao = {555, 557, 559};
        short[] quan = {556, 558, 560};
        short[] gang = {562, 564, 566};
        short[] giay = {563, 565, 567};
        short[] nhan = {561};
        short[] options = {34, 35, 36, 86, 87, 208};
        if (Util.isTrue(10, 100)) {
            idTempTL = nhan[0];
            type = 4; // rada
        } else if (Util.isTrue(15, 100)) {
            idTempTL = gang[Util.nextInt(3)];
            type = 2; // gang
        } else if (Util.isTrue(25, 100)) {
            idTempTL = quan[Util.nextInt(3)];
            type = 1; // quan
        } else if (Util.isTrue(70, 100)) {
            idTempTL = ao[Util.nextInt(3)];
            type = 0; // ao
        } else {
            idTempTL = giay[Util.nextInt(3)];
            type = 3; // giay
        }
        int tiLe = Util.nextInt(100, 115);
        List<ItemOption> itemoptions = new ArrayList<>();
        switch (type) {
            case 0 ->
                itemoptions.add(new ItemOption(47, Util.nextInt(800, 900) * tiLe / 100));
            case 1 -> {
                int chiso = Util.nextInt(46000, 49000) * tiLe / 100;
                itemoptions.add(new ItemOption(22, chiso / 1000));
                itemoptions.add(new ItemOption(27, chiso * 125 / 1000));
            }
            case 2 ->
                itemoptions.add(new ItemOption(0, Util.nextInt(4300, 4500) * tiLe / 100));
            case 3 -> {
                int chiso = Util.nextInt(46000, 49000) * tiLe / 100;
                itemoptions.add(new ItemOption(23, chiso / 1000));
                itemoptions.add(new ItemOption(28, chiso * 125 / 1000));
            }
            case 4 ->
                itemoptions.add(new ItemOption(14, Util.nextInt(14, 17) * tiLe / 100));
        }
        if (Util.isTrue(90, 100)) {
            itemoptions.add(new ItemOption(options[Util.nextInt(options.length)], 0));
        }
        itemoptions.add(new ItemOption(21, Util.nextInt(15, 40)));
        ItemMap it = new ItemMap(zone, idTempTL, quantity, x, y, id);
        it.options.clear();
        it.options.addAll(itemoptions);
        return it;
    }

    public int getOptionParamItemShop(short id, int optionId) {
        for (Shop shop : Manager.SHOPS) {
            for (TabShop tabShop : shop.tabShops) {
                for (ItemShop itemShop : tabShop.itemShops) {
                    if (itemShop.temp.id != id) {
                        continue;
                    }
                    for (ItemOption itemOption : itemShop.options) {
                        if (itemOption.optionTemplate.id == optionId) {
                            return itemOption.param;
                        }
                    }
                }
            }
        }
        return -1;
    }

    public List<Item.ItemOption> getListOptionItemShop(int level, int gender, int type) {
        List<Item.ItemOption> list = new ArrayList<>();
        Manager.SHOPS.forEach(shop -> shop.tabShops.forEach(tabShop -> tabShop.itemShops.forEach(itemShop -> {
            if (itemShop.temp.level == level && itemShop.temp.gender == gender && itemShop.temp.type == type
                    && list.isEmpty()) {
                for (ItemOption io : itemShop.options) {
                    list.add(new ItemOption(io.optionTemplate.id, io.param));
                }
            }
        })));
        return list;
    }

    public Item getAngelItem(int gender, int type) {
        int tempId = 1048 + type * 3 + gender;
        Item angelItem = createNewItem((short) tempId);
        for (Item.ItemOption io : getListOptionItemShop(14, type == 4 ? 3 : gender, type)) {
            if (io.isOptionCanUpgrade()) {
                int param = (int) (io.param * 1.2);
                angelItem.itemOptions.add(new ItemOption(io.optionTemplate.id, param));
            }
        }
        int param = switch (type) {
            case 0 ->
                62;
            case 1 ->
                66;
            case 2 ->
                70;
            case 3 ->
                64;
            default ->
                68;
        };
        angelItem.itemOptions.add(new ItemOption(21, param));
        angelItem.itemOptions.add(new ItemOption(30, 0));
        return angelItem;
    }

    public Item otptl(short tempId) {
        return otptl(tempId, 1);
    }

    public Item otphd(short tempId) {
        return otphd(tempId, 1);
    }

    public Item otpkh(short tempId) {
        return otpkh(tempId, 1);
    }

    public Item otptl(short tempId, int quantity) {
        Item item = new Item();
        item.template = getTemplate(tempId);
        if (item.template == null) {
            throw new IllegalArgumentException("Item template không tồn tại: " + tempId);
        }
        item.quantity = quantity;
        item.createTime = System.currentTimeMillis();
        if (item.template.type == 0) {
            item.itemOptions.add(new ItemOption(21, 19));
            item.itemOptions.add(new ItemOption(47, Util.nextInt(800, 1200)));
        }
        if (item.template.type == 1) {
            item.itemOptions.add(new ItemOption(21, 19));
            item.itemOptions.add(new ItemOption(22, Util.nextInt(36, 48)));
        }
        if (item.template.type == 2) {
            item.itemOptions.add(new ItemOption(21, 19));
            item.itemOptions.add(new ItemOption(0, Util.nextInt(5500, 7800)));
        }
        if (item.template.type == 3) {
            item.itemOptions.add(new ItemOption(21, 19));
            item.itemOptions.add(new ItemOption(23, Util.nextInt(32, 46)));
        }
        if (item.template.type == 4) {
            item.itemOptions.add(new ItemOption(21, 19));
            item.itemOptions.add(new ItemOption(14, Util.nextInt(13, 17)));
        }
        item.content = item.getContent();
        item.info = item.getInfo();
        return item;
    }

    public Item otphd(short tempId, int quantity) {
        Item item = new Item();
        item.template = getTemplate(tempId);
        if (item.template == null) {
            throw new IllegalArgumentException("Item template không tồn tại: " + tempId);
        }
        item.quantity = quantity;
        item.createTime = System.currentTimeMillis();
        if (item.template.type == 0) {
            item.itemOptions.add(new ItemOption(21, 80));
            item.itemOptions.add(new ItemOption(47, Util.nextInt(1200, 2100)));
        }
        if (item.template.type == 1) {
            item.itemOptions.add(new ItemOption(21, 80));
            item.itemOptions.add(new ItemOption(22, Util.nextInt(60, 80)));
        }
        if (item.template.type == 2) {
            item.itemOptions.add(new ItemOption(21, 80));
            item.itemOptions.add(new ItemOption(0, Util.nextInt(8500, 11000)));
        }
        if (item.template.type == 3) {
            item.itemOptions.add(new ItemOption(21, 80));
            item.itemOptions.add(new ItemOption(23, Util.nextInt(59, 82)));
        }
        if (item.template.type == 4) {
            item.itemOptions.add(new ItemOption(21, 80));
            item.itemOptions.add(new ItemOption(14, Util.nextInt(15, 19)));
        }
        item.content = item.getContent();
        item.info = item.getInfo();
        return item;
    }

    public Item otpkh(short tempId, int quantity) {
        Item item = new Item();
        item.template = getTemplate(tempId);
        if (item.template == null) {
            throw new IllegalArgumentException("Item template không tồn tại: " + tempId);
        }
        item.quantity = quantity;
        item.createTime = System.currentTimeMillis();
        if (item.template.type == 0) {
            item.itemOptions.add(new ItemOption(47, 3));
        }
        if (item.template.type == 1) {
            item.itemOptions.add(new ItemOption(6, 20));
        }
        if (item.template.type == 2) {
            item.itemOptions.add(new ItemOption(0, 3));
        }
        if (item.template.type == 3) {
            item.itemOptions.add(new ItemOption(7, 20));
        }
        if (item.template.type == 4) {
            item.itemOptions.add(new ItemOption(14, 1));
        }
        item.content = item.getContent();
        item.info = item.getInfo();
        return item;
    }

    public void settlkaio(Player player) {
        for (int i = 0; i < 1; i++) {
            Item hq = InventoryService.gI().findItem(player.inventory.itemsBag, 1703 + i);
            int[] dotl = new int[]{555, 556, 562, 563, 561, 555, 556, 563, 561};
            int ramdom = new Random().nextInt(dotl.length);
            Item ao = ItemService.gI().otptl((short) dotl[ramdom]);
            ao.itemOptions.add(new Item.ItemOption(127, 0));
            ao.itemOptions.add(new Item.ItemOption(139, 0));
            ao.itemOptions.add(new Item.ItemOption(30, 0));
            if (InventoryService.gI().getCountEmptyBag(player) > 1) {
                InventoryService.gI().addItemBag(player, ao);
                InventoryService.gI().sendItemBag(player);
                Service.gI().sendThongBao(player, "Bạn đã nhận được 1 món Thần Linh ");
                InventoryService.gI().subQuantityItemsBag(player, hq, 1);
                InventoryService.gI().sendItemBag(player);
            } else {
                Service.gI().sendThongBao(player, "Bạn phải có ít nhất 1 ô trống hành trang");
            }
        }
    }

    public void settlgenki(Player player) {
        for (int i = 0; i < 1; i++) {
            Item hq = InventoryService.gI().findItem(player.inventory.itemsBag, 1703 + i);
            int[] dotl = new int[]{555, 556, 562, 563, 561, 555, 556, 563, 561};
            int ramdom = new Random().nextInt(dotl.length);
            Item ao = ItemService.gI().otptl((short) dotl[ramdom]);
            ao.itemOptions.add(new Item.ItemOption(128, 0));
            ao.itemOptions.add(new Item.ItemOption(140, 0));
            ao.itemOptions.add(new Item.ItemOption(30, 0));
            if (InventoryService.gI().getCountEmptyBag(player) > 1) {
                InventoryService.gI().addItemBag(player, ao);
                InventoryService.gI().sendItemBag(player);
                Service.gI().sendThongBao(player, "Bạn đã nhận được món Thần Linh ");
                InventoryService.gI().subQuantityItemsBag(player, hq, 1);
                InventoryService.gI().sendItemBag(player);
            } else {
                Service.gI().sendThongBao(player, "Bạn phải có ít nhất 1 ô trống hành trang");
            }
        }
    }

    public void settlson(Player player) {
        for (int i = 0; i < 1; i++) {
            Item hq = InventoryService.gI().findItem(player.inventory.itemsBag, 1703 + i);
            int[] dotl = new int[]{555, 556, 562, 563, 561, 555, 556, 563, 561};
            int ramdom = new Random().nextInt(dotl.length);
            Item ao = ItemService.gI().otptl((short) dotl[ramdom]);
            ao.itemOptions.add(new Item.ItemOption(129, 0));
            ao.itemOptions.add(new Item.ItemOption(141, 0));
            ao.itemOptions.add(new Item.ItemOption(30, 0));
            if (InventoryService.gI().getCountEmptyBag(player) > 1) {
                InventoryService.gI().addItemBag(player, ao);
                InventoryService.gI().sendItemBag(player);
                Service.gI().sendThongBao(player, "Bạn đã nhận được món Thần Linh ");
                InventoryService.gI().subQuantityItemsBag(player, hq, 1);
                InventoryService.gI().sendItemBag(player);
            } else {
                Service.gI().sendThongBao(player, "Bạn phải có ít nhất 1 ô trống hành trang");
            }
        }
    }

    public void settlpico(Player player) {
        for (int i = 0; i < 1; i++) {
            Item hq = InventoryService.gI().findItem(player.inventory.itemsBag, 1703 + i);
            int[] dotl = new int[]{557, 558, 564, 565, 561, 557, 558, 565, 561};
            int ramdom = new Random().nextInt(dotl.length);
            Item ao = ItemService.gI().otptl((short) dotl[ramdom]);
            ao.itemOptions.add(new Item.ItemOption(130, 0));
            ao.itemOptions.add(new Item.ItemOption(142, 0));
            ao.itemOptions.add(new Item.ItemOption(30, 0));
            if (InventoryService.gI().getCountEmptyBag(player) > 1) {
                InventoryService.gI().addItemBag(player, ao);
                InventoryService.gI().sendItemBag(player);
                Service.gI().sendThongBao(player, "Bạn đã nhận được món Thần Linh ");
                InventoryService.gI().subQuantityItemsBag(player, hq, 1);
                InventoryService.gI().sendItemBag(player);
            } else {
                Service.gI().sendThongBao(player, "Bạn phải có ít nhất 1 ô trống hành trang");
            }
        }
    }

    public void settloctieu(Player player) {
        for (int i = 0; i < 1; i++) {
            Item hq = InventoryService.gI().findItem(player.inventory.itemsBag, 1703 + i);
            int[] dotl = new int[]{557, 558, 564, 565, 561, 557, 558, 565, 561};
            int ramdom = new Random().nextInt(dotl.length);
            Item ao = ItemService.gI().otptl((short) dotl[ramdom]);
            ao.itemOptions.add(new Item.ItemOption(131, 0));
            ao.itemOptions.add(new Item.ItemOption(143, 0));
            ao.itemOptions.add(new Item.ItemOption(30, 0));
            if (InventoryService.gI().getCountEmptyBag(player) > 4) {
                InventoryService.gI().addItemBag(player, ao);
                InventoryService.gI().sendItemBag(player);
                Service.gI().sendThongBao(player, "Bạn đã nhận được món Thần Linh ");
                InventoryService.gI().subQuantityItemsBag(player, hq, 1);
                InventoryService.gI().sendItemBag(player);
            } else {
                Service.gI().sendThongBao(player, "Bạn phải có ít nhất 1 ô trống hành trang");
            }
        }
    }

    public void settlpiko(Player player) {
        for (int i = 0; i < 1; i++) {
            Item hq = InventoryService.gI().findItem(player.inventory.itemsBag, 1703 + i);
            int[] dotl = new int[]{557, 558, 564, 565, 561, 557, 558, 565, 561};
            int ramdom = new Random().nextInt(dotl.length);
            Item ao = ItemService.gI().otptl((short) dotl[ramdom]);
            ao.itemOptions.add(new Item.ItemOption(132, 0));
            ao.itemOptions.add(new Item.ItemOption(144, 0));
            ao.itemOptions.add(new Item.ItemOption(30, 0));
            if (InventoryService.gI().getCountEmptyBag(player) > 1) {
                InventoryService.gI().addItemBag(player, ao);
                InventoryService.gI().sendItemBag(player);
                Service.gI().sendThongBao(player, "Bạn đã nhận được món Thần Linh ");
                InventoryService.gI().subQuantityItemsBag(player, hq, 1);
                InventoryService.gI().sendItemBag(player);
            } else {
                Service.gI().sendThongBao(player, "Bạn phải có ít nhất 1 ô trống hành trang");
            }
        }
    }

    public void settllienhoan(Player player) {
        for (int i = 0; i < 1; i++) {
            Item hq = InventoryService.gI().findItem(player.inventory.itemsBag, 1703 + i);
            int[] dotl = new int[]{557, 558, 564, 565, 561, 557, 558, 565, 561};
            int ramdom = new Random().nextInt(dotl.length);
            Item ao = ItemService.gI().otptl((short) dotl[ramdom]);
            ao.itemOptions.add(new Item.ItemOption(251, 0));
            ao.itemOptions.add(new Item.ItemOption(254, 0));
            ao.itemOptions.add(new Item.ItemOption(30, 0));
            if (InventoryService.gI().getCountEmptyBag(player) > 1) {
                InventoryService.gI().addItemBag(player, ao);
                InventoryService.gI().sendItemBag(player);
                Service.gI().sendThongBao(player, "Bạn đã nhận được món Thần Linh ");
                InventoryService.gI().subQuantityItemsBag(player, hq, 1);
                InventoryService.gI().sendItemBag(player);
            } else {
                Service.gI().sendThongBao(player, "Bạn phải có ít nhất 1 ô trống hành trang");
            }
        }
    }

    public void settlgalick(Player player) {
        for (int i = 0; i < 1; i++) {
            Item hq = InventoryService.gI().findItem(player.inventory.itemsBag, 1703 + i);
            int[] dotl = new int[]{559, 560, 566, 567, 561, 559, 560, 567, 561};
            int ramdom = new Random().nextInt(dotl.length);
            Item ao = ItemService.gI().otptl((short) dotl[ramdom]);
            ao.itemOptions.add(new Item.ItemOption(133, 0));
            ao.itemOptions.add(new Item.ItemOption(136, 0));
            ao.itemOptions.add(new Item.ItemOption(30, 0));
            if (InventoryService.gI().getCountEmptyBag(player) > 1) {
                InventoryService.gI().addItemBag(player, ao);
                InventoryService.gI().sendItemBag(player);
                Service.gI().sendThongBao(player, "Bạn đã nhận được món thần linh ");
                InventoryService.gI().subQuantityItemsBag(player, hq, 1);
                InventoryService.gI().sendItemBag(player);
            } else {
                Service.gI().sendThongBao(player, "Bạn phải có ít nhất 1 ô trống hành trang");
            }
        }
    }

    public void settlcadick(Player player) {
        for (int i = 0; i < 1; i++) {
            Item hq = InventoryService.gI().findItem(player.inventory.itemsBag, 1703 + i);
            int[] dotl = new int[]{559, 560, 566, 567, 561, 559, 560, 567, 561};
            int ramdom = new Random().nextInt(dotl.length);
            Item ao = ItemService.gI().otptl((short) dotl[ramdom]);
            ao.itemOptions.add(new Item.ItemOption(134, 0));
            ao.itemOptions.add(new Item.ItemOption(137, 0));
            ao.itemOptions.add(new Item.ItemOption(30, 0));
            if (InventoryService.gI().getCountEmptyBag(player) > 1) {
                InventoryService.gI().addItemBag(player, ao);
                InventoryService.gI().sendItemBag(player);
                Service.gI().sendThongBao(player, "Bạn đã nhận được món Thàn linh ");
                InventoryService.gI().subQuantityItemsBag(player, hq, 1);
                InventoryService.gI().sendItemBag(player);
            } else {
                Service.gI().sendThongBao(player, "Bạn phải có ít nhất 1 ô trống hành trang");
            }
        }
    }

    public void settlnappa(Player player) {
        for (int i = 0; i < 1; i++) {
            Item hq = InventoryService.gI().findItem(player.inventory.itemsBag, 1703 + i);
            int[] dotl = new int[]{559, 560, 566, 567, 561, 559, 560, 567, 561};
            int ramdom = new Random().nextInt(dotl.length);
            Item ao = ItemService.gI().otptl((short) dotl[ramdom]);
            ao.itemOptions.add(new Item.ItemOption(135, 0));
            ao.itemOptions.add(new Item.ItemOption(138, 0));
            ao.itemOptions.add(new Item.ItemOption(30, 0));
            if (InventoryService.gI().getCountEmptyBag(player) > 1) {
                InventoryService.gI().addItemBag(player, ao);
                InventoryService.gI().sendItemBag(player);
                Service.gI().sendThongBao(player, "Bạn đã nhận được món thần linh ");
                InventoryService.gI().subQuantityItemsBag(player, hq, 1);
                InventoryService.gI().sendItemBag(player);
            } else {
                Service.gI().sendThongBao(player, "Bạn phải có ít nhất 1 ô trống hành trang");
            }
        }
    }

    public void sethdkaio(Player player) {
        for (int i = 0; i < 1; i++) {
            Item hq = InventoryService.gI().findItem(player.inventory.itemsBag, 1704 + i);
            int[] dohd = new int[]{650, 651, 657, 658, 656, 650, 651, 658, 656};
            int ramdom = new Random().nextInt(dohd.length);
            Item ao = ItemService.gI().otphd((short) dohd[ramdom]);
            ao.itemOptions.add(new Item.ItemOption(127, 0));
            ao.itemOptions.add(new Item.ItemOption(139, 0));
            ao.itemOptions.add(new Item.ItemOption(30, 0));
            if (InventoryService.gI().getCountEmptyBag(player) > 1) {
                InventoryService.gI().addItemBag(player, ao);
                InventoryService.gI().sendItemBag(player);
                Service.gI().sendThongBao(player, "Bạn đã nhận được món Hủy Diệt ");
                InventoryService.gI().subQuantityItemsBag(player, hq, 1);
                InventoryService.gI().sendItemBag(player);
            } else {
                Service.gI().sendThongBao(player, "Bạn phải có ít nhất 1 ô trống hành trang");
            }
        }
    }

    public void sethdgenki(Player player) {
        for (int i = 0; i < 1; i++) {
            Item hq = InventoryService.gI().findItem(player.inventory.itemsBag, 1704 + i);
            int[] dohd = new int[]{650, 651, 657, 658, 656, 650, 651, 658, 656};
            int ramdom = new Random().nextInt(dohd.length);
            Item ao = ItemService.gI().otphd((short) dohd[ramdom]);
            ao.itemOptions.add(new Item.ItemOption(128, 0));
            ao.itemOptions.add(new Item.ItemOption(140, 0));
            ao.itemOptions.add(new Item.ItemOption(30, 0));
            if (InventoryService.gI().getCountEmptyBag(player) > 1) {
                InventoryService.gI().addItemBag(player, ao);
                InventoryService.gI().sendItemBag(player);
                Service.gI().sendThongBao(player, "Bạn đã nhận được món Hủy Diệt ");
                InventoryService.gI().subQuantityItemsBag(player, hq, 1);
                InventoryService.gI().sendItemBag(player);
            } else {
                Service.gI().sendThongBao(player, "Bạn phải có ít nhất 1 ô trống hành trang");
            }
        }
    }

    public void sethdson(Player player) {
        for (int i = 0; i < 1; i++) {
            Item hq = InventoryService.gI().findItem(player.inventory.itemsBag, 1704 + i);
            int[] dohd = new int[]{650, 651, 657, 658, 656, 650, 651, 658, 656};
            int ramdom = new Random().nextInt(dohd.length);
            Item ao = ItemService.gI().otphd((short) dohd[ramdom]);
            ao.itemOptions.add(new Item.ItemOption(129, 0));
            ao.itemOptions.add(new Item.ItemOption(141, 0));
            ao.itemOptions.add(new Item.ItemOption(30, 0));
            if (InventoryService.gI().getCountEmptyBag(player) > 1) {
                InventoryService.gI().addItemBag(player, ao);
                InventoryService.gI().sendItemBag(player);
                Service.gI().sendThongBao(player, "Bạn đã nhận được món Hủy Diệt ");
                InventoryService.gI().subQuantityItemsBag(player, hq, 1);
                InventoryService.gI().sendItemBag(player);
            } else {
                Service.gI().sendThongBao(player, "Bạn phải có ít nhất 1 ô trống hành trang");
            }
        }
    }

    public void sethdpico(Player player) {
        for (int i = 0; i < 1; i++) {
            Item hq = InventoryService.gI().findItem(player.inventory.itemsBag, 1704 + i);
            int[] dohd = new int[]{652, 653, 659, 660, 656, 652, 653, 660, 656};
            int ramdom = new Random().nextInt(dohd.length);
            Item ao = ItemService.gI().otphd((short) dohd[ramdom]);
            ao.itemOptions.add(new Item.ItemOption(130, 0));
            ao.itemOptions.add(new Item.ItemOption(142, 0));
            ao.itemOptions.add(new Item.ItemOption(30, 0));
            if (InventoryService.gI().getCountEmptyBag(player) > 1) {
                InventoryService.gI().addItemBag(player, ao);
                InventoryService.gI().sendItemBag(player);
                Service.gI().sendThongBao(player, "Bạn đã nhận được món Hủy Diệt ");
                InventoryService.gI().subQuantityItemsBag(player, hq, 1);
                InventoryService.gI().sendItemBag(player);
            } else {
                Service.gI().sendThongBao(player, "Bạn phải có ít nhất 1 ô trống hành trang");
            }
        }
    }

    public void sethdoctieu(Player player) {
        for (int i = 0; i < 1; i++) {
            Item hq = InventoryService.gI().findItem(player.inventory.itemsBag, 1704 + i);
            int[] dohd = new int[]{652, 653, 659, 660, 656, 652, 653, 660, 656};
            int ramdom = new Random().nextInt(dohd.length);
            Item ao = ItemService.gI().otphd((short) dohd[ramdom]);
            ao.itemOptions.add(new Item.ItemOption(131, 0));
            ao.itemOptions.add(new Item.ItemOption(143, 0));
            ao.itemOptions.add(new Item.ItemOption(30, 0));
            if (InventoryService.gI().getCountEmptyBag(player) > 1) {
                InventoryService.gI().addItemBag(player, ao);
                InventoryService.gI().sendItemBag(player);
                Service.gI().sendThongBao(player, "Bạn đã nhận được món Hủy Diệt ");
                InventoryService.gI().subQuantityItemsBag(player, hq, 1);
                InventoryService.gI().sendItemBag(player);
            } else {
                Service.gI().sendThongBao(player, "Bạn phải có ít nhất 1 ô trống hành trang");
            }
        }
    }

    public void sethdpiko(Player player) {
        for (int i = 0; i < 1; i++) {
            Item hq = InventoryService.gI().findItem(player.inventory.itemsBag, 1704 + i);
            int[] dohd = new int[]{652, 653, 659, 660, 656, 652, 653, 660, 656};
            int ramdom = new Random().nextInt(dohd.length);
            Item ao = ItemService.gI().otphd((short) dohd[ramdom]);
            ao.itemOptions.add(new Item.ItemOption(132, 0));
            ao.itemOptions.add(new Item.ItemOption(144, 0));
            ao.itemOptions.add(new Item.ItemOption(30, 0));
            if (InventoryService.gI().getCountEmptyBag(player) > 4) {
                InventoryService.gI().addItemBag(player, ao);
                InventoryService.gI().sendItemBag(player);
                Service.gI().sendThongBao(player, "Bạn đã nhận được món Hủy DIệt ");
                InventoryService.gI().subQuantityItemsBag(player, hq, 1);
                InventoryService.gI().sendItemBag(player);
            } else {
                Service.gI().sendThongBao(player, "Bạn phải có ít nhất 1 ô trống hành trang");
            }
        }
    }

    public void sethdlienhoan(Player player) {
        for (int i = 0; i < 1; i++) {
            Item hq = InventoryService.gI().findItem(player.inventory.itemsBag, 1704 + i);
            int[] dohd = new int[]{652, 653, 659, 660, 656, 652, 653, 660, 656};
            int ramdom = new Random().nextInt(dohd.length);
            Item ao = ItemService.gI().otphd((short) dohd[ramdom]);
            ao.itemOptions.add(new Item.ItemOption(251, 0));
            ao.itemOptions.add(new Item.ItemOption(254, 0));
            ao.itemOptions.add(new Item.ItemOption(30, 0));
            if (InventoryService.gI().getCountEmptyBag(player) > 4) {
                InventoryService.gI().addItemBag(player, ao);
                InventoryService.gI().sendItemBag(player);
                Service.gI().sendThongBao(player, "Bạn đã nhận được món Hủy DIệt ");
                InventoryService.gI().subQuantityItemsBag(player, hq, 1);
                InventoryService.gI().sendItemBag(player);
            } else {
                Service.gI().sendThongBao(player, "Bạn phải có ít nhất 1 ô trống hành trang");
            }
        }
    }

    public void sethdcadick(Player player) {
        for (int i = 0; i < 1; i++) {
            Item hq = InventoryService.gI().findItem(player.inventory.itemsBag, 1704 + i);
            int[] dohd = new int[]{654, 655, 661, 662, 656, 654, 655, 662, 656};
            int ramdom = new Random().nextInt(dohd.length);
            Item ao = ItemService.gI().otphd((short) dohd[ramdom]);
            ao.itemOptions.add(new Item.ItemOption(133, 0));
            ao.itemOptions.add(new Item.ItemOption(136, 0));
            ao.itemOptions.add(new Item.ItemOption(30, 0));
            if (InventoryService.gI().getCountEmptyBag(player) > 1) {
                InventoryService.gI().addItemBag(player, ao);
                InventoryService.gI().sendItemBag(player);
                Service.gI().sendThongBao(player, "Bạn đã nhận được món Hủy Diệt ");
                InventoryService.gI().subQuantityItemsBag(player, hq, 1);
                InventoryService.gI().sendItemBag(player);
            } else {
                Service.gI().sendThongBao(player, "Bạn phải có ít nhất 1 ô trống hành trang");
            }
        }
    }

    public void sethdcadic(Player player) {
        for (int i = 0; i < 1; i++) {
            Item hq = InventoryService.gI().findItem(player.inventory.itemsBag, 1704 + i);
            int[] dohd = new int[]{654, 655, 661, 662, 656, 654, 655, 662, 656};
            int ramdom = new Random().nextInt(dohd.length);
            Item ao = ItemService.gI().otphd((short) dohd[ramdom]);
            ao.itemOptions.add(new Item.ItemOption(134, 0));
            ao.itemOptions.add(new Item.ItemOption(137, 0));
            ao.itemOptions.add(new Item.ItemOption(30, 0));
            if (InventoryService.gI().getCountEmptyBag(player) > 1) {
                InventoryService.gI().addItemBag(player, ao);
                InventoryService.gI().sendItemBag(player);
                Service.gI().sendThongBao(player, "Bạn đã nhận được món Hủy Diệt ");
                InventoryService.gI().subQuantityItemsBag(player, hq, 1);
                InventoryService.gI().sendItemBag(player);
            } else {
                Service.gI().sendThongBao(player, "Bạn phải có ít nhất 1 ô trống hành trang");
            }
        }
    }

    public void sethdnappa(Player player) {
        for (int i = 0; i < 1; i++) {
            Item hq = InventoryService.gI().findItem(player.inventory.itemsBag, 1704 + i);
            int[] dohd = new int[]{654, 655, 661, 662, 656, 654, 655, 662, 656};
            int ramdom = new Random().nextInt(dohd.length);
            Item ao = ItemService.gI().otphd((short) dohd[ramdom]);
            ao.itemOptions.add(new Item.ItemOption(135, 0));
            ao.itemOptions.add(new Item.ItemOption(138, 0));
            ao.itemOptions.add(new Item.ItemOption(30, 0));
            if (InventoryService.gI().getCountEmptyBag(player) > 1) {
                InventoryService.gI().addItemBag(player, ao);
                InventoryService.gI().sendItemBag(player);
                Service.gI().sendThongBao(player, "Bạn đã nhận được món Hủy Diệt ");
                InventoryService.gI().subQuantityItemsBag(player, hq, 1);
                InventoryService.gI().sendItemBag(player);
            } else {
                Service.gI().sendThongBao(player, "Bạn phải có ít nhất 1 ô trống hành trang");
            }
        }
    }

    public void setkhkaio(Player player) {
        for (int i = 0; i < 1; i++) {
            Item skh = InventoryService.gI().findItem(player.inventory.itemsBag, 1968 + i);
            int[] doskh = new int[]{0, 6, 21, 27, 12};
            int ramdom = new Random().nextInt(doskh.length);
            Item ao = ItemService.gI().otpkh((short) doskh[ramdom]);
            ao.itemOptions.add(new Item.ItemOption(127, 0));
            ao.itemOptions.add(new Item.ItemOption(139, 0));
            NangCapLevelKichHoat.ensureLevelZero(ao);
            ao.itemOptions.add(new Item.ItemOption(30, 0));
            if (InventoryService.gI().getCountEmptyBag(player) > 1) {
                InventoryService.gI().addItemBag(player, ao);
                InventoryService.gI().sendItemBag(player);
                Service.gI().sendThongBao(player, "Bạn đã nhận được 1 món");
                InventoryService.gI().subQuantityItemsBag(player, skh, 1);
                InventoryService.gI().sendItemBag(player);
            } else {
                Service.gI().sendThongBao(player, "Bạn phải có ít nhất 1 ô trống hành trang");
            }
        }
    }

    public void setkhgenki(Player player) {
        for (int i = 0; i < 1; i++) {
            Item skh = InventoryService.gI().findItem(player.inventory.itemsBag, 1968 + i);
            int[] doskh = new int[]{0, 6, 21, 27, 12};
            int ramdom = new Random().nextInt(doskh.length);
            Item ao = ItemService.gI().otpkh((short) doskh[ramdom]);
            ao.itemOptions.add(new Item.ItemOption(128, 0));
            ao.itemOptions.add(new Item.ItemOption(140, 0));
            NangCapLevelKichHoat.ensureLevelZero(ao);
            ao.itemOptions.add(new Item.ItemOption(30, 0));
            if (InventoryService.gI().getCountEmptyBag(player) > 1) {
                InventoryService.gI().addItemBag(player, ao);
                InventoryService.gI().sendItemBag(player);
                Service.gI().sendThongBao(player, "Bạn đã nhận được món");
                InventoryService.gI().subQuantityItemsBag(player, skh, 1);
                InventoryService.gI().sendItemBag(player);
            } else {
                Service.gI().sendThongBao(player, "Bạn phải có ít nhất 1 ô trống hành trang");
            }
        }
    }

    public void setkhson(Player player) {
        for (int i = 0; i < 1; i++) {
            Item skh = InventoryService.gI().findItem(player.inventory.itemsBag, 1968 + i);
            int[] doskh = new int[]{0, 6, 21, 27, 12};
            int ramdom = new Random().nextInt(doskh.length);
            Item ao = ItemService.gI().otpkh((short) doskh[ramdom]);
            ao.itemOptions.add(new Item.ItemOption(129, 0));
            ao.itemOptions.add(new Item.ItemOption(141, 0));
            NangCapLevelKichHoat.ensureLevelZero(ao);
            ao.itemOptions.add(new Item.ItemOption(30, 0));
            if (InventoryService.gI().getCountEmptyBag(player) > 1) {
                InventoryService.gI().addItemBag(player, ao);
                InventoryService.gI().sendItemBag(player);
                Service.gI().sendThongBao(player, "Bạn đã nhận được món");
                InventoryService.gI().subQuantityItemsBag(player, skh, 1);
                InventoryService.gI().sendItemBag(player);
            } else {
                Service.gI().sendThongBao(player, "Bạn phải có ít nhất 1 ô trống hành trang");
            }
        }
    }

    public void setkhpico(Player player) {
        for (int i = 0; i < 1; i++) {
            Item skh = InventoryService.gI().findItem(player.inventory.itemsBag, 1968 + i);
            int[] doskh = new int[]{1, 7, 22, 28, 12};
            int ramdom = new Random().nextInt(doskh.length);
            Item ao = ItemService.gI().otpkh((short) doskh[ramdom]);
            ao.itemOptions.add(new Item.ItemOption(130, 0));
            ao.itemOptions.add(new Item.ItemOption(142, 0));
            NangCapLevelKichHoat.ensureLevelZero(ao);
            ao.itemOptions.add(new Item.ItemOption(30, 0));
            if (InventoryService.gI().getCountEmptyBag(player) > 1) {
                InventoryService.gI().addItemBag(player, ao);
                InventoryService.gI().sendItemBag(player);
                Service.gI().sendThongBao(player, "Bạn đã nhận được món");
                InventoryService.gI().subQuantityItemsBag(player, skh, 1);
                InventoryService.gI().sendItemBag(player);
            } else {
                Service.gI().sendThongBao(player, "Bạn phải có ít nhất 1 ô trống hành trang");
            }
        }
    }

    public void setkhoctieu(Player player) {
        for (int i = 0; i < 1; i++) {
            Item skh = InventoryService.gI().findItem(player.inventory.itemsBag, 1968 + i);
            int[] doskh = new int[]{1, 7, 22, 28, 12};
            int ramdom = new Random().nextInt(doskh.length);
            Item ao = ItemService.gI().otpkh((short) doskh[ramdom]);
            ao.itemOptions.add(new Item.ItemOption(131, 0));
            ao.itemOptions.add(new Item.ItemOption(143, 0));
            NangCapLevelKichHoat.ensureLevelZero(ao);
            ao.itemOptions.add(new Item.ItemOption(30, 0));
            if (InventoryService.gI().getCountEmptyBag(player) > 4) {
                InventoryService.gI().addItemBag(player, ao);
                InventoryService.gI().sendItemBag(player);
                Service.gI().sendThongBao(player, "Bạn đã nhận được món");
                InventoryService.gI().subQuantityItemsBag(player, skh, 1);
                InventoryService.gI().sendItemBag(player);
            } else {
                Service.gI().sendThongBao(player, "Bạn phải có ít nhất 1 ô trống hành trang");
            }
        }
    }

    public void setkhpiko(Player player) {
        for (int i = 0; i < 1; i++) {
            Item skh = InventoryService.gI().findItem(player.inventory.itemsBag, 1968 + i);
            int[] doskh = new int[]{1, 7, 22, 28, 12};
            int ramdom = new Random().nextInt(doskh.length);
            Item ao = ItemService.gI().otpkh((short) doskh[ramdom]);
            ao.itemOptions.add(new Item.ItemOption(132, 0));
            ao.itemOptions.add(new Item.ItemOption(144, 0));
            NangCapLevelKichHoat.ensureLevelZero(ao);
            ao.itemOptions.add(new Item.ItemOption(30, 0));
            if (InventoryService.gI().getCountEmptyBag(player) > 1) {
                InventoryService.gI().addItemBag(player, ao);
                InventoryService.gI().sendItemBag(player);
                Service.gI().sendThongBao(player, "Bạn đã nhận được món");
                InventoryService.gI().subQuantityItemsBag(player, skh, 1);
                InventoryService.gI().sendItemBag(player);
            } else {
                Service.gI().sendThongBao(player, "Bạn phải có ít nhất 1 ô trống hành trang");
            }
        }
    }

    public void setkhlienhoan(Player player) {
        for (int i = 0; i < 1; i++) {
            Item skh = InventoryService.gI().findItem(player.inventory.itemsBag, 1968 + i);
            int[] doskh = new int[]{1, 7, 22, 28, 12};
            int ramdom = new Random().nextInt(doskh.length);
            Item ao = ItemService.gI().otpkh((short) doskh[ramdom]);
            ao.itemOptions.add(new Item.ItemOption(251, 0));
            ao.itemOptions.add(new Item.ItemOption(254, 0));
            NangCapLevelKichHoat.ensureLevelZero(ao);
            ao.itemOptions.add(new Item.ItemOption(30, 0));
            if (InventoryService.gI().getCountEmptyBag(player) > 1) {
                InventoryService.gI().addItemBag(player, ao);
                InventoryService.gI().sendItemBag(player);
                Service.gI().sendThongBao(player, "Bạn đã nhận được món");
                InventoryService.gI().subQuantityItemsBag(player, skh, 1);
                InventoryService.gI().sendItemBag(player);
            } else {
                Service.gI().sendThongBao(player, "Bạn phải có ít nhất 1 ô trống hành trang");
            }
        }
    }

    public void setkhgalick(Player player) {
        for (int i = 0; i < 1; i++) {
            Item skh = InventoryService.gI().findItem(player.inventory.itemsBag, 1968 + i);
            int[] doskh = new int[]{2, 8, 23, 29, 12};
            int ramdom = new Random().nextInt(doskh.length);
            Item ao = ItemService.gI().otpkh((short) doskh[ramdom]);
            ao.itemOptions.add(new Item.ItemOption(133, 0));
            ao.itemOptions.add(new Item.ItemOption(136, 0));
            NangCapLevelKichHoat.ensureLevelZero(ao);
            ao.itemOptions.add(new Item.ItemOption(30, 0));
            if (InventoryService.gI().getCountEmptyBag(player) > 1) {
                InventoryService.gI().addItemBag(player, ao);
                InventoryService.gI().sendItemBag(player);
                Service.gI().sendThongBao(player, "Bạn đã nhận được món");
                InventoryService.gI().subQuantityItemsBag(player, skh, 1);
                InventoryService.gI().sendItemBag(player);
            } else {
                Service.gI().sendThongBao(player, "Bạn phải có ít nhất 1 ô trống hành trang");
            }
        }
    }

    public void setkhcadick(Player player) {
        for (int i = 0; i < 1; i++) {
            Item skh = InventoryService.gI().findItem(player.inventory.itemsBag, 1968 + i);
            int[] doskh = new int[]{2, 8, 23, 29, 12};
            int ramdom = new Random().nextInt(doskh.length);
            Item ao = ItemService.gI().otpkh((short) doskh[ramdom]);
            ao.itemOptions.add(new Item.ItemOption(134, 0));
            ao.itemOptions.add(new Item.ItemOption(137, 0));
            NangCapLevelKichHoat.ensureLevelZero(ao);
            ao.itemOptions.add(new Item.ItemOption(30, 0));
            if (InventoryService.gI().getCountEmptyBag(player) > 1) {
                InventoryService.gI().addItemBag(player, ao);
                InventoryService.gI().sendItemBag(player);
                Service.gI().sendThongBao(player, "Bạn đã nhận được món");
                InventoryService.gI().subQuantityItemsBag(player, skh, 1);
                InventoryService.gI().sendItemBag(player);
            } else {
                Service.gI().sendThongBao(player, "Bạn phải có ít nhất 1 ô trống hành trang");
            }
        }
    }

    public void setkhnappa(Player player) {
        for (int i = 0; i < 1; i++) {
            Item skh = InventoryService.gI().findItem(player.inventory.itemsBag, 1968 + i);
            int[] doskh = new int[]{2, 8, 23, 29, 12};
            int ramdom = new Random().nextInt(doskh.length);
            Item ao = ItemService.gI().otpkh((short) doskh[ramdom]);
            ao.itemOptions.add(new Item.ItemOption(135, 0));
            ao.itemOptions.add(new Item.ItemOption(138, 0));
            NangCapLevelKichHoat.ensureLevelZero(ao);
            ao.itemOptions.add(new Item.ItemOption(30, 0));
            if (InventoryService.gI().getCountEmptyBag(player) > 1) {
                InventoryService.gI().addItemBag(player, ao);
                InventoryService.gI().sendItemBag(player);
                Service.gI().sendThongBao(player, "Bạn đã nhận được món ");
                InventoryService.gI().subQuantityItemsBag(player, skh, 1);
                InventoryService.gI().sendItemBag(player);
            } else {
                Service.gI().sendThongBao(player, "Bạn phải có ít nhất 1 ô trống hành trang");
            }
        }
    }

    public void OpenDHD2(Player player, int gender, int itemtype) {

        int[][] items = {
            {650, 651, 657, 658, 656},
            {652, 653, 659, 660, 656},
            {654, 655, 661, 662, 656}
        }; // td, namec,xd
        Item item = randomCS_DHD(items[gender][itemtype], gender);

        if (item != null && InventoryService.gI().getCountEmptyBag(player) > 0) {
            InventoryService.gI().addItemBag(player, item);
            InventoryService.gI().sendItemBag(player);
            Service.gI().sendThongBao(player, "Bạn đã nhận được " + item.template.name);
        } else {
            Service.gI().sendThongBao(player, "Bạn phải có ít nhất 1 ô trống hành trang");
        }
    }

    public boolean openConfiguredGiftBox(Player pl, Item itemUse) {
        if (pl == null || itemUse == null || !itemUse.isNotNullItem()) {
            return false;
        }

        GiftBoxConfig config = loadGiftBoxConfig(itemUse.template.id);
        if (config == null) {
            return false;
        }

        if (InventoryService.gI().getCountEmptyBag(pl) < config.minEmptySlots) {
            Service.gI().sendThongBao(pl, "Bạn phải có ít nhất " + config.minEmptySlots + " ô trống hành trang");
            return true;
        }

        GiftBoxReward reward = selectGiftBoxReward(config.id);
        if (reward == null) {
            Service.gI().sendThongBao(pl, "Hộp quà này chưa có phần thưởng");
            return true;
        }

        int quantity = reward.quantityMin;
        if (reward.quantityMax > reward.quantityMin) {
            quantity = Util.nextInt(reward.quantityMin, reward.quantityMax);
        }

        Item itemReward = ItemService.gI().createNewItem((short) reward.itemId, quantity);
        for (GiftBoxOption option : reward.options) {
            int param = option.paramMin;
            if (option.paramMax > option.paramMin) {
                param = Util.nextInt(option.paramMin, option.paramMax);
            }
            itemReward.itemOptions.add(new Item.ItemOption(option.id, param));
        }
        applyGiftBoxOptionGroups(itemReward, reward.optionGroups);

        InventoryService.gI().addItemBag(pl, itemReward);
        InventoryService.gI().sendItemBag(pl);
        InventoryService.gI().subQuantityItemsBag(pl, itemUse, 1);

        String message = config.successMessage == null || config.successMessage.isEmpty()
                ? "Bạn mở rương nhận được {item}"
                : config.successMessage;
        Service.gI().sendThongBao(pl, message.replace("{item}", itemReward.template.name));
        return true;
    }

    private GiftBoxConfig loadGiftBoxConfig(int itemId) {
        String sql = "SELECT id, min_empty_slots, success_message FROM gift_box_configs WHERE item_id = ? AND active = 1 LIMIT 1";
        try (Connection con = DBConnecter.getConnectionServer(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                GiftBoxConfig config = new GiftBoxConfig();
                config.id = rs.getInt("id");
                config.minEmptySlots = Math.max(1, rs.getInt("min_empty_slots"));
                config.successMessage = rs.getString("success_message");
                return config;
            }
        } catch (Exception ignored) {
            return null;
        }
    }

    private GiftBoxReward selectGiftBoxReward(int configId) {
        String sql = "SELECT reward_item_id, quantity_min, quantity_max, chance_weight, options_json, option_groups_json "
                + "FROM gift_box_rewards WHERE gift_box_config_id = ? ORDER BY sort_order, id";
        List<GiftBoxReward> rewards = new ArrayList<>();
        int totalWeight = 0;

        try (Connection con = DBConnecter.getConnectionServer(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, configId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    GiftBoxReward reward = new GiftBoxReward();
                    reward.itemId = rs.getInt("reward_item_id");
                    reward.quantityMin = Math.max(1, rs.getInt("quantity_min"));
                    reward.quantityMax = Math.max(reward.quantityMin, rs.getInt("quantity_max"));
                    reward.weight = Math.max(1, rs.getInt("chance_weight"));
                    reward.options = parseGiftBoxOptions(rs.getString("options_json"));
                    reward.optionGroups = parseGiftBoxOptionGroups(rs.getString("option_groups_json"));
                    rewards.add(reward);
                    totalWeight += reward.weight;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        if (rewards.isEmpty() || totalWeight <= 0) {
            return null;
        }

        int roll = Util.nextInt(1, totalWeight);
        int running = 0;
        for (GiftBoxReward reward : rewards) {
            running += reward.weight;
            if (roll <= running) {
                return reward;
            }
        }
        return rewards.get(rewards.size() - 1);
    }

    private void applyGiftBoxOptionGroups(Item itemReward, List<GiftBoxOptionGroup> groups) {
        if (itemReward == null || groups == null || groups.isEmpty()) {
            return;
        }

        for (GiftBoxOptionGroup group : groups) {
            GiftBoxOptionEntry entry = selectGiftBoxOptionEntry(group);
            if (entry == null || entry.options == null || entry.options.isEmpty()) {
                continue;
            }

            for (GiftBoxOption option : entry.options) {
                int param = option.paramMin;
                if (option.paramMax > option.paramMin) {
                    param = Util.nextInt(option.paramMin, option.paramMax);
                }
                itemReward.itemOptions.add(new Item.ItemOption(option.id, param));
            }
        }
    }

    private GiftBoxOptionEntry selectGiftBoxOptionEntry(GiftBoxOptionGroup group) {
        if (group == null || group.entries == null || group.entries.isEmpty()) {
            return null;
        }

        int totalWeight = 0;
        for (GiftBoxOptionEntry entry : group.entries) {
            totalWeight += Math.max(0, entry.weight);
        }
        if (totalWeight <= 0) {
            return null;
        }

        int roll = Util.nextInt(1, totalWeight);
        int running = 0;
        for (GiftBoxOptionEntry entry : group.entries) {
            int weight = Math.max(0, entry.weight);
            if (weight <= 0) {
                continue;
            }
            running += weight;
            if (roll <= running) {
                return entry;
            }
        }
        return group.entries.get(group.entries.size() - 1);
    }

    private List<GiftBoxOption> parseGiftBoxOptions(String rawOptions) {
        List<GiftBoxOption> options = new ArrayList<>();
        Object parsed = JSONValue.parse(rawOptions == null || rawOptions.trim().isEmpty() ? "[]" : rawOptions);
        if (!(parsed instanceof JSONArray)) {
            return options;
        }

        JSONArray array = (JSONArray) parsed;
        for (Object value : array) {
            if (!(value instanceof JSONObject)) {
                continue;
            }

            JSONObject object = (JSONObject) value;
            GiftBoxOption option = new GiftBoxOption();
            option.id = jsonInt(object, "id", -1);
            if (option.id < 0) {
                continue;
            }
            option.paramMin = jsonInt(object, "param_min", jsonInt(object, "param", 0));
            option.paramMax = jsonInt(object, "param_max", option.paramMin);
            if (option.paramMax < option.paramMin) {
                int temp = option.paramMin;
                option.paramMin = option.paramMax;
                option.paramMax = temp;
            }
            options.add(option);
        }
        return options;
    }

    private List<GiftBoxOptionGroup> parseGiftBoxOptionGroups(String rawGroups) {
        List<GiftBoxOptionGroup> groups = new ArrayList<>();
        Object parsed = JSONValue.parse(rawGroups == null || rawGroups.trim().isEmpty() ? "[]" : rawGroups);
        if (!(parsed instanceof JSONArray)) {
            return groups;
        }

        JSONArray array = (JSONArray) parsed;
        for (Object value : array) {
            if (!(value instanceof JSONObject)) {
                continue;
            }

            JSONObject object = (JSONObject) value;
            Object entriesValue = object.get("entries");
            if (!(entriesValue instanceof JSONArray)) {
                continue;
            }

            GiftBoxOptionGroup group = new GiftBoxOptionGroup();
            JSONArray entries = (JSONArray) entriesValue;
            for (Object entryValue : entries) {
                if (!(entryValue instanceof JSONObject)) {
                    continue;
                }

                JSONObject entryObject = (JSONObject) entryValue;
                GiftBoxOptionEntry entry = new GiftBoxOptionEntry();
                entry.weight = Math.max(0, jsonInt(entryObject, "chance_weight", jsonInt(entryObject, "weight", 1)));
                Object optionsValue = entryObject.get("options");
                entry.options = parseGiftBoxOptionsValue(optionsValue);
                group.entries.add(entry);
            }

            if (!group.entries.isEmpty()) {
                groups.add(group);
            }
        }
        return groups;
    }

    private List<GiftBoxOption> parseGiftBoxOptionsValue(Object parsed) {
        List<GiftBoxOption> options = new ArrayList<>();
        if (!(parsed instanceof JSONArray)) {
            return options;
        }

        JSONArray array = (JSONArray) parsed;
        for (Object value : array) {
            if (!(value instanceof JSONObject)) {
                continue;
            }

            JSONObject object = (JSONObject) value;
            GiftBoxOption option = new GiftBoxOption();
            option.id = jsonInt(object, "id", -1);
            if (option.id < 0) {
                continue;
            }
            option.paramMin = jsonInt(object, "param_min", jsonInt(object, "param", 0));
            option.paramMax = jsonInt(object, "param_max", option.paramMin);
            if (option.paramMax < option.paramMin) {
                int temp = option.paramMin;
                option.paramMin = option.paramMax;
                option.paramMax = temp;
            }
            options.add(option);
        }
        return options;
    }

    private int jsonInt(JSONObject object, String key, int defaultValue) {
        Object value = object.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value != null) {
            try {
                return Integer.parseInt(String.valueOf(value));
            } catch (NumberFormatException ignored) {
            }
        }
        return defaultValue;
    }

    private static class GiftBoxConfig {

        int id;
        int minEmptySlots;
        String successMessage;
    }

    private static class GiftBoxReward {

        int itemId;
        int quantityMin;
        int quantityMax;
        int weight;
        List<GiftBoxOption> options = new ArrayList<>();
        List<GiftBoxOptionGroup> optionGroups = new ArrayList<>();
    }

    private static class GiftBoxOption {

        int id;
        int paramMin;
        int paramMax;
    }

    private static class GiftBoxOptionGroup {

        List<GiftBoxOptionEntry> entries = new ArrayList<>();
    }

    private static class GiftBoxOptionEntry {

        int weight;
        List<GiftBoxOption> options = new ArrayList<>();
    }

    public void OpenItem1967(Player pl, Item item) {
        int nr = Util.nextInt(17, 20);
        int[] vp = {220, 221, 222, 223, 224, 225};
        int[] vpVip = {1966};
        Item item2 = null;
        if (Util.isTrue(60, 100)) {
            item2 = ItemService.gI().createNewItem((short) nr);
            item2.quantity = 1;
            item2.itemOptions.add(new Item.ItemOption(30, 1));
        } else if (Util.isTrue(50, 100)) {
            item2 = ItemService.gI().createNewItem((short) vp[Util.nextInt(0, vp.length - 1)]);
            item2.quantity = 1;
            item2.itemOptions.add(new Item.ItemOption(30, 1));
        } else {
            item2 = ItemService.gI().createNewItem((short) vpVip[Util.nextInt(0, vpVip.length - 1)]);
            item2.quantity = 1;

            // Tỉ lệ HSD cho item 1966
            // 1 ngày  : 35%
            // 3 ngày  : 25%
            // 5 ngày  : 18%
            // 7 ngày  : 12%
            // 15 ngày : 8%
            int tiLeHsd = Util.nextInt(1, 100);

            if (tiLeHsd <= 35) {
                item2.itemOptions.add(new Item.ItemOption(93, 1));
            } else if (tiLeHsd <= 60) {
                item2.itemOptions.add(new Item.ItemOption(93, 3));
            } else if (tiLeHsd <= 78) {
                item2.itemOptions.add(new Item.ItemOption(93, 5));
            } else if (tiLeHsd <= 90) {
                item2.itemOptions.add(new Item.ItemOption(93, 7));
            } else if (tiLeHsd <= 98) {
                item2.itemOptions.add(new Item.ItemOption(93, 15));
            } else {
                // Vĩnh viễn: không add option 93
            }

            int combo = Util.nextInt(1, 3);
            if (combo == 1) {
                item2.itemOptions.add(new Item.ItemOption(50, Util.nextInt(1, 5)));
                item2.itemOptions.add(new Item.ItemOption(0, Util.nextInt(500, 5000)));
            } else if (combo == 2) {
                item2.itemOptions.add(new Item.ItemOption(77, Util.nextInt(1, 5)));
                item2.itemOptions.add(new Item.ItemOption(6, Util.nextInt(5000, 50000)));
            } else {
                item2.itemOptions.add(new Item.ItemOption(103, Util.nextInt(1, 5)));
                item2.itemOptions.add(new Item.ItemOption(7, Util.nextInt(5000, 50000)));
            }
        }
        InventoryService.gI().addItemBag(pl, item2);
        InventoryService.gI().sendItemBag(pl);
        InventoryService.gI().subQuantityItemsBag(pl, item, 1);
        Service.gI().sendThongBao(pl, "Bạn mở rương nhận được " + item2.template.name);
    }
}
