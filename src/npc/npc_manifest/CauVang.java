package npc.npc_manifest;

import consts.ConstNpc;
import item.Item;
import models.Combine.CombineService;
import npc.Npc;
import player.Player;
import services.InventoryService;
import services.ItemService;
import services.Service;
import services.TaskService;
import services.func.ChangeMapService;
import utils.Util;

/**
 *
 * @author BCHoan
 */
public class CauVang extends Npc {

    public CauVang(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            TaskService.gI().checkDoneTaskTalkNpc(player, this);
            if (mapId == 14) {
                createOtherMenu(player, ConstNpc.BASE_MENU,
                        "|5|Huhuu mình chỉ là 1 con chó mà GÂU GÂU GÂU Ẳng Ẳng Ẳng\n"
                        + "Đừng bắt mình nha bờ dô!",
                        "Bắt cậu vàng", "Đóng");
            }
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (!canOpenNpc(player)) {
            return;
        }

        if (player.iDMark.getIndexMenu() == ConstNpc.BASE_MENU && this.mapId == 14) {
            switch (select) {
                case 0 -> {
                    this.createOtherMenu(player, 1,
                            "Cậu Vàng đang vùng vẫy và có tỉ lệ thất bại.\n"
                            + "Nếu bị bắt, dân làng sẽ đánh cậu đó!\n"
                            + "Cần 1 Thòng Lọng để bắt.",
                            "Đồng ý", "Từ chối");
                }
            }
        } else if (player.iDMark.getIndexMenu() == 1) {
            switch (select) {
                case 0 -> {
                    Item thongLong = InventoryService.gI().findItemBag(player, 1839);
                    if (thongLong == null || thongLong.quantity < 1) {
                        Service.gI().sendThongBao(player, "Bạn cần 1 Thòng Lọng để bắt Cậu Vàng!");
                        return;
                    }
                    InventoryService.gI().subQuantityItemsBag(player, thongLong, 1);
                    InventoryService.gI().sendItemBag(player);
                    if (Util.isTrue(30, 100)) {
                        Item cauvang = ItemService.gI().createNewItem((short) 1835, 1);
                        cauvang.itemOptions.add(new Item.ItemOption(50, Util.nextInt(8, 15)));
                        cauvang.itemOptions.add(new Item.ItemOption(77, Util.nextInt(8, 15)));
                        cauvang.itemOptions.add(new Item.ItemOption(103, Util.nextInt(8, 15)));
                        cauvang.itemOptions.add(new Item.ItemOption(101, Util.nextInt(10, 20)));
                        cauvang.itemOptions.add(new Item.ItemOption(114, 75));
                        cauvang.itemOptions.add(new Item.ItemOption(93, Util.nextInt(1, 5)));
                        InventoryService.gI().addItemBag(player, cauvang);
                        InventoryService.gI().sendItemBag(player);

                        Service.gI().sendThongBao(player,
                                "Bạn đã bắt trộm được Cậu Vàng! Hãy bỏ " + cauvang.template.name + "vào bao bì!");
                        Util.setTimeout(() -> {
                            Service.gI().chat(player, "Bắt được rồi đem đi ra SANTA bán thôi :V");
                        }, 3000);
                        ChangeMapService.gI().changeMapBySpaceShip(player, 5, -1, 1156);
                    } else {
                        Service.gI().sendThongBao(player,
                                "Phở Anh Hoan sắp phát hiện ra bạn, hãy bình tĩnh lại!");
                        Util.setTimeout(() -> {
                            CombineService.gI().cauVang.npcChat(player, "Ẳng Ẳng! Cậu vàng chạy đây!!!");
                        }, 800);
                    }
                }
            }
        }
    }
}
