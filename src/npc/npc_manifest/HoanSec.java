package npc.npc_manifest;

import consts.ConstNpc;
import consts.ConstTask;
import item.Item;
import java.util.Random;
import jdbc.daos.PlayerDAO;
import map.Map;
import npc.Npc;
import player.Player;
import services.InventoryService;
import services.ItemService;
import services.MapService;
import services.NpcService;
import services.Service;
import services.TaskService;
import services.func.ChangeMapService;
import shop.ShopService;
import utils.Util;

public class HoanSec extends Npc {

    private final byte COUNT_CHANGE = 1;
    private int count;

    public HoanSec(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    private void checkyngo(Player player) {

        count++;
        if (this.count >= COUNT_CHANGE) {
            count = 0;
            this.map.npcs.remove(this);
            Map kyngo = MapService.gI().getMapForKyNgo();
            this.mapId = kyngo.mapId;
            this.cx = Util.nextInt(100, kyngo.mapWidth - 100);
            this.cy = kyngo.yPhysicInTop(this.cx, 0);
            this.map = kyngo;
            Service.gI().sendThongBao(player, "Có Lũ sẽ gặp lại nhau!!");
            this.map.npcs.add(this);

            System.out.println("Em Gái Vùng Lũ xuất hiện tại map " + kyngo.mapName);
        }

    }

    @Override
    public void openBaseMenu(Player player) {

        player.iDMark.setIndexMenu(ConstNpc.BASE_MENU);
        if (this.mapId != player.zone.map.mapId) {
            Service.gI().sendThongBao(player, "Có người kia vừa ủng hộ vùng lũ lụt rồi");
            Service.gI().hideWaitDialog(player);
            return;
        } else {
            this.createOtherMenu(player, ConstNpc.BASE_MENU,
                    "\b|5|Trời đất ơi! Mùa bão lũ lại tới, bà con bọn em khổ lắm rồi...\n"
                    + "\b|5|Đi ngang qua đây, nếu đại ca có lòng thì cho em xin chút gạo muối\n\n"
                    + " Còn không thì biếu luôn cho Admin cho đỡ buồn đời",
                    "Biếu Admin", "Gửi người dân\nvùng lũ");
        }

    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (player.iDMark.isBaseMenu()) {
            switch (select) {
                case 0 -> {
                    this.createOtherMenu(player, 1,
                            "Ơ kìa, tặng cho Admin thật à? Người đâu mà có tâm ghê!\n\n"
                            + "Tặng xong nhớ chụp ảnh gửi group để khoe cho oai nhé!\n\n"
                            + "Phải nhớ nói là ‘Hoan đẹp trai nhất vũ trụ’ để được nhân đôi phước lành nha",
                            "Tặng liền", "Từ Chối");
                }
                case 1 -> {
                    this.createOtherMenu(player, 2,
                            "\b|5|Một đồng của đại ca bằng cả bao nhiêu bữa cơm của dân vùng lũ đó nha!\n"
                            + "\n\b|7|Hiện tại đại ca đang có: " + player.getSession().cash + " VND\n\n"
                            + "|4|Chọn mức ủng hộ nào, làm phúc thì trời thương, hạn hán bão lũ trôi đi luôn!",
                            "Ủng hộ 5k (bằng 1 gói mì tôm)",
                            "Ủng hộ 20k (bằng 1 tô bún chả thịt nướng)",
                            "Ủng hộ 50k (đại gia thực thụ xuất hiện)",
                            "Tố giác lừa đảo",
                            "Từ chối");

                }
                case 2 -> {
                    int shop = Util.nextInt(1, 4);
                    if (shop == 1) {
                        ShopService.gI().opendShop(player, "Ky_ngo1", false);
                    } else if (shop == 2) {
                        ShopService.gI().opendShop(player, "Ky_ngo2", false);
                    } else if (shop == 3) {
                        ShopService.gI().opendShop(player, "ky_ngo3", false);
                    } else {
                        ShopService.gI().opendShop(player, "ky_ngo4", false);
                    }
                    checkyngo(player);
                }
                default ->
                    Service.gI().sendThongBao(player, "Không thể thực hiện");
            }
        } else if (player.iDMark.getIndexMenu() == 1) {
            switch (select) {
                case 0 -> {
                    int[] list = {17, 18, 19, 20, 457, 45737, 16, 1636, 1731};
                    Item bas = ItemService.gI().createNewItem((short) list[Util.nextInt(0, list.length - 1)], 1);
                    InventoryService.gI().addItemBag(player, bas);
                    InventoryService.gI().sendItemBag(player);
                    Service.gI().sendThongBao(player,
                            "Trời ơi, cảm động quá! Admin tặng lại ngươi x1 "
                            + bas.template.name + " coi như lộc ta ban, nhớ tiêu cho khéo nhé!");
                    checkyngo(player);
                }

            }
        } else if (player.iDMark.getIndexMenu() == 2) {
            switch (select) {
                case 0 -> {
                    if (player.getSession().cash < 5000) {
                        Service.gI().sendThongBao(player, "5k không có bày đặt ĐÔN NẾT nữa!!");
                        return;
                    }
                    PlayerDAO.subcash(player, 5000);
                    int sl = 1;
                    int[] list = {14, 15, 457, 1796, 457, 457, 16, 1788, 457, 457, 457, 457, 1536, 1636, 648, 1727, 1728, 457};

                    int qua = list[Util.nextInt(0, list.length - 1)];
                    if (qua == 16 || qua == 1636) {
                        sl = Util.nextInt(3, 8);
                    }
                    if (qua == 457 || qua == 457 || qua == 457 || qua == 457 || qua == 1796) {
                        sl = Util.nextInt(5, 15);
                    }
                    if (qua == 457) {
                        sl = Util.nextInt(30, 80);
                    }
                    Item bas = ItemService.gI().createNewItem((short) qua, sl);
                    InventoryService.gI().addItemBag(player, bas);
                    InventoryService.gI().sendItemBag(player);
                    Service.gI().sendThongBao(player, "Ủng hộ 5k mà tình nghĩa như núi! Dân vùng lũ khắc cốt ghi tâm, tặng lại ngươi x"
                            + sl + " " + bas.template.name + " để ăn lấy sức chống bão!");
                    checkyngo(player);
                }
                case 1 -> {
                    if (player.getSession().cash < 20000) {
                        Service.gI().sendThongBao(player, "20k mà cũng Không Có à??");
                        return;
                    }
                    PlayerDAO.subcash(player, 20000);
                    int sl = 1;
                    int[] list = {1536, 457, 1636, 720, 16, 457, 457, 457, 457, 457};

                    int qua = list[Util.nextInt(0, list.length - 1)];
                    if (Util.isTrue(10, 100)) {
                        qua = 1792;
                    }
                    if (qua == 16 || qua == 1636) {
                        sl = Util.nextInt(10, 20);
                    }
                    if (qua == 720) {
                        sl = Util.nextInt(5, 15);
                    }
                    if (qua == 457 || qua == 457 || qua == 457 || qua == 457 || qua == 457) {
                        sl = Util.nextInt(20, 50);
                    }
                    if (qua == 457) {
                        sl = Util.nextInt(150, 400);
                    }
                    Item bas = ItemService.gI().createNewItem((short) qua, sl);
                    InventoryService.gI().addItemBag(player, bas);
                    InventoryService.gI().sendItemBag(player);
                    Service.gI().sendThongBao(player, "20k của đại ca đủ cho cả xóm có bữa no! Bà con gửi tặng ngươi x"
                            + sl + " " + bas.template.name + " cùng ngàn lời cảm ơn chân thành!");
                    checkyngo(player);
                }
                case 2 -> {
                    if (player.getSession().cash < 50000) {
                        Service.gI().sendThongBao(player, "20k mà cũng Không Có à??");
                        return;
                    }
                    PlayerDAO.subcash(player, 50000);
                    int sl = 1;
                    int[] list = {457, 720, 1228};

                    int qua = list[Util.nextInt(0, list.length - 1)];
                    if (Util.isTrue(10, 100)) {
                        Random random = new Random();
                        int rand = random.nextInt(100);
                        if (rand < 50) {
                            qua = 1793;
                        } else if (rand < 80) {
                            if (player.gender == 0) {
                                qua = 1746;
                            } else if (player.gender == 2) {
                                qua = 1416;
                            } else {
                                qua = 1752;
                            }
                        } else {
                            qua = 1758;
                        }
                    }

                    if (qua == 720) {
                        sl = Util.nextInt(15, 40);
                    }
                    if (qua == 457) {
                        sl = Util.nextInt(300, 1000);
                    }
                    Item bas = ItemService.gI().createNewItem((short) qua, sl);
                    if (qua == 1758 || qua == 1752 || qua == 1416 || qua == 1746) {
                        bas.itemOptions.add(new Item.ItemOption(50, 20));
                        bas.itemOptions.add(new Item.ItemOption(77, 20));
                        bas.itemOptions.add(new Item.ItemOption(103, 20));
                        bas.itemOptions.add(new Item.ItemOption(5, 5));
                        bas.itemOptions.add(new Item.ItemOption(72, 1));
                    }
                    InventoryService.gI().addItemBag(player, bas);
                    InventoryService.gI().sendItemBag(player);
                    Service.gI().sendThongBao(player, "Ôi trời đất ơi! Đại gia thực sự rồi đây chứ còn ai nữa!\n"
                            + "Dân vùng lũ vui rớt nước mắt, tặng ngươi x" + sl + " "
                            + bas.template.name + " kèm tấm lòng khắc ghi muôn đời!");
                    checkyngo(player);
                }
                case 3 -> {
                    Service.gI().sendThongBao(player,
                            "|7|Tố giác lừa đảo hả?\nNgươi bị phạt 5000 thỏi vàng vì tội… nói xàm!");
                    checkyngo(player);
                }
            }
        }
    }
}
