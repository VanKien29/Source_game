package npc.npc_manifest;

import consts.ConstNpc;
import item.Item;
import npc.Npc;
import player.Player;
import services.InventoryService;
import services.ItemService;
import services.Service;
import utils.Util;

/**
 *
 * @author CongHoan
 */
public class XeMia extends Npc {

    public XeMia(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            if (this.map.mapId == 0 || this.map.mapId == 7 || this.map.mapId == 14 || this.map.mapId == 5) {
                this.createOtherMenu(player, ConstNpc.BASE_MENU,
                        "|7|Cậu muốn uống gì nào ?"
                        + "\n|3|Ly mía sầu riêng tăng 10% hp và 10% giáp"
                        + "\n|3|Ly mía thơm tăng 10% ki 10% và chí mạng"
                        + "\n|3|Ly mía khổng lồ tăng 10% Sức đánh và 5% sức đánh chí mạng"
                        +"\n|4|Khúc mía up ở tất cả các Map!!"
                        +"\n|4|Nước đá up bên Cold vì bên đó rất Lạnh!!",
                        "Mua 1 ly\nnước mía", "Mua 10 ly\nnước mía");
            }
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            if (player.iDMark.isBaseMenu()) {
                switch (select) {
                    case 0:
                        Item khucMia = InventoryService.gI().findItemBag(player, 1612);
                        Item nuocDa = InventoryService.gI().findItemBag(player, 1613);
                        if ((khucMia != null && khucMia.quantity >= 5)
                                && (nuocDa != null && nuocDa.quantity >= 2)
                                && player.inventory.gold >= 5000000) {
                            createOtherMenu(player, ConstNpc.MENU_1_LY,
                                    "|2|Bạn muốn mua nước mía\n"
                                    + "|1|Khúc mía " + khucMia.quantity + "/5\n"
                                    + "Nước đá " + nuocDa.quantity + "/2\n"
                                    + "Giá vàng: 5.000.000",
                                    "Đồng ý", "Từ chối");
                            break;
                        } else {
                            String NpcSay = "|2|Bạn cần có đủ\n";
                            if (khucMia == null) {
                                NpcSay += "|7|Khúc mía " + "0/5\n";
                            } else {
                                NpcSay += "|1|Khúc mía " + khucMia.quantity + "/5\n";
                            }
                            if (nuocDa == null) {
                                NpcSay += "|7|Nước đá " + "0/2\n";
                            } else {
                                NpcSay += "|1|Nước đá " + nuocDa.quantity + "/2\n";
                            }
                            if (player.inventory.gold < 5000000) {
                                NpcSay += "|7|Còn thiếu vàng";
                            } else {
                                NpcSay += "|1|Giá vàng: 5.000.000\n";
                            }
                            createOtherMenu(player, ConstNpc.MENU_1_LY_2,
                                    NpcSay, "Từ chối");
                        }
                        break;
                    case 1:
                        Item khucMiaa = InventoryService.gI().findItemBag(player, 1612);
                        Item nuocDaa = InventoryService.gI().findItemBag(player, 1613);
                        if ((khucMiaa != null && khucMiaa.quantity >= 50)
                                && (nuocDaa != null && nuocDaa.quantity >= 20)
                                && player.inventory.gold >= 50000000) {
                            createOtherMenu(player, ConstNpc.MENU_10_LY,
                                    "|2|Bạn muốn mua nước mía\n"
                                    + "|1|Khúc mía " + khucMiaa.quantity + "/50\n"
                                    + "Nước đá " + nuocDaa.quantity + "/20\n"
                                    + "Giá vàng: 50.000.000",
                                    "Đồng ý", "Từ chối");
                            break;
                        } else {
                            String NpcSay = "|2|Bạn cần có đủ\n";
                            if (khucMiaa == null) {
                                NpcSay += "|7|Khúc mía " + "0/50\n";
                            } else {
                                NpcSay += "|1|Khúc mía " + khucMiaa.quantity + "/50\n";
                            }
                            if (nuocDaa == null) {
                                NpcSay += "|7|Nước đá " + "0/20\n";
                            } else {
                                NpcSay += "|1|Nước đá " + nuocDaa.quantity + "/20\n";
                            }
                            if (player.inventory.gold < 50000000) {
                                NpcSay += "|7|Còn thiếu vàng";
                            } else {
                                NpcSay += "|1|Giá vàng: 50.000.000\n";
                            }
                            createOtherMenu(player, ConstNpc.MENU_10_LY_2,
                                    NpcSay, "Từ chối");
                        }
                        break;
                }
            } else if (player.iDMark.getIndexMenu() == ConstNpc.MENU_1_LY) {
                switch (select) {
                    case 0:
                        Item khucMia = InventoryService.gI().findItemBag(player, 1612);
                        Item nuocDa = InventoryService.gI().findItemBag(player, 1613);
                        InventoryService.gI().subQuantityItemsBag(player, khucMia, 5);
                        InventoryService.gI().subQuantityItemsBag(player, nuocDa, 2);
                        player.inventory.gold -= 5000000;
                        Service.gI().sendMoney(player);
                        short itemId;
                        int random = Util.nextInt(1, 100);
                        if (random <= 50) {
                            itemId = 1614;
                        } else if (random <= 80) {
                            itemId = 1615;
                        } else {
                            itemId = 1616;
                        }
                        Item nuocMia = ItemService.gI().createNewItem(itemId);
                        nuocMia.itemOptions.add(new Item.ItemOption(30, 1));
                        nuocMia.itemOptions.add(new Item.ItemOption(87, 1));
                        nuocMia.itemOptions.add(new Item.ItemOption(93, 30));
//                      player.point_nuocmia++;
                        InventoryService.gI().addItemBag(player, nuocMia);
                        InventoryService.gI().sendItemBag(player);
                        Service.gI().sendThongBao(player, "Bạn nhận được " + nuocMia.template.name);
                        break;

                }

            } else if (player.iDMark.getIndexMenu() == ConstNpc.MENU_10_LY) {
                switch (select) {
                    case 0:
                        Item khucMia = InventoryService.gI().findItemBag(player, 1612);
                        Item nuocDa = InventoryService.gI().findItemBag(player, 1613);
                        InventoryService.gI().subQuantityItemsBag(player, khucMia, 50);
                        InventoryService.gI().subQuantityItemsBag(player, nuocDa, 20);
                        player.inventory.gold -= 50000000;
                        Service.gI().sendMoney(player);
                        short itemId;
                        int random = Util.nextInt(1, 100);
                        if (random <= 50) {
                            itemId = 1614;
                        } else if (random <= 80) {
                            itemId = 1615;
                        } else {
                            itemId = 1616;
                        }
                        Item nuocMia = ItemService.gI().createNewItem(itemId, 10);
                        nuocMia.itemOptions.add(new Item.ItemOption(30, 1));
                        nuocMia.itemOptions.add(new Item.ItemOption(87, 1));
                        nuocMia.itemOptions.add(new Item.ItemOption(93, 30));
//                      player.point_nuocmia+=10;
                        InventoryService.gI().addItemBag(player, nuocMia);
                        InventoryService.gI().sendItemBag(player);
                        Service.gI().sendThongBao(player, "Bạn nhận được " + nuocMia.template.name);
                        break;

                }
            }
        }
    }
}
