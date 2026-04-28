/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package npc.npc_manifest;

import consts.ConstNpc;
import consts.ConstTranhNgocNamek;
import consts.cn;
import item.Item;
import npc.Npc;
import player.Player;
import services.InventoryService;
import services.ItemService;
import services.PetService;
import services.Service;
import services.TaskService;
import shop.ShopService;
import utils.Util;

/**
 *
 * @author CongHoan
 */
public class PhoAnhHoan extends Npc {

    public PhoAnhHoan(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            TaskService.gI().checkDoneTaskTalkNpc(player, this);
            if (mapId == 14) {
                createOtherMenu(player, ConstNpc.BASE_MENU,
                        "|5|Xin chào, đá bát phở không bạn ơi?",
                        "Đá bát phở", "Sờ cậu vàng", "Hốt cậu vàng", "Đóng");
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
                case 0 ->
                    ShopService.gI().opendShop(player, "PHO_ANH_HOAN", false);
                case 1 -> {
                    this.createOtherMenu(player, 1,
                            "Sờ Cậu Vàng mất 20 tỷ vàng\n"
                            + "Có quà bất ngờ cho cậu.\n"
                            +"|4|1 Hộp quà Ván Bay thì sao??",
                            "Đồng ý",
                            "Từ chối");

                }
                case 2 -> {
                    this.createOtherMenu(player, 2,
                            "|7|Ngươi muốn bắt chó của ta ư??\n",
                            "Đồng ý",
                            "Từ chối");

                }
            }
        } else if (player.iDMark.getIndexMenu() == 1) {
            switch (select) {
                case 0 -> {
                    if (player.inventory.gold < 20_000_000_000L) {
                        Service.gI().sendThongBao(player, "Bạn không đủ 20 tỷ vàng để sờ Cậu Vàng!");
                        return;
                    }

                    // Trừ vàng
                    player.inventory.gold -= 20_000_000_000L;
                    Service.gI().sendMoney(player);

                    // 70% xịt, 30% trúng
                    if (Util.isTrue(30, 100)) {
                        int[] listQua = {1592, 1592, 1592};
                        int idItem = listQua[Util.nextInt(0, listQua.length - 1)];
                        Item qua = ItemService.gI().createNewItem((short) idItem, 1);
                        InventoryService.gI().addItemBag(player, qua);
                        InventoryService.gI().sendItemBag(player);
                        Service.gI().sendThongBao(player,
                                "Trời ơi, bạn thật may mắn! Nhận được " + qua.template.name + " từ phở Anh Hoan!");
                    } else {
                        Service.gI().sendThongBao(player, "Xịt rồi! Chúc bạn may mắn lần sau!");
                    }
                }
            }
        } else if (player.iDMark.getIndexMenu() == 2) {
            switch (select) {
                case 0 ->
                    Service.gI().sendThongBao(player, "Cậu vàng ở bên kia cơ mà!");
            }
        }
    }
}
