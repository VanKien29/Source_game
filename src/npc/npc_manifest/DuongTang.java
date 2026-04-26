package npc.npc_manifest;

import consts.ConstNpc;
import models.Combine.CombineService;
import npc.Npc;
import player.Player;
import server.Manager;
import services.Service;
import services.func.ChangeMapService;
import utils.Util;

/**
 *
 * @author CongHoan
 */
public class DuongTang extends Npc {

    public DuongTang(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            switch (mapId) {
                case 0 -> {
                    createOtherMenu(player, ConstNpc.BASE_MENU, "|7|Vào map để úp thỏi vàng"
                            + "\b|3|Lưu Ý: Cần mở thành viên (10k) để vào map",
                            "Đồng ý"
                    );
                }
                case 123 -> {
                    createOtherMenu(player, ConstNpc.BASE_MENU, "Ra khỏi ngôi làng này sẽ gặp ngọn núi ngũ hành sơn",
                            //                            "Nâng ngọc bội",
                            "Về\nLàng Aru", "Đóng");
                }
                default ->
                    super.openBaseMenu(player);
            }
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            if (player.iDMark.isBaseMenu()) {
                switch (mapId) {
                    case 0 -> {
                        if (select == 0) {
                            if (player.getSession().actived) {
                                ChangeMapService.gI().changeMapNonSpaceship(player, 123, 53, 384);
                            } else {
                                Service.gI().sendThongBao(player, "Cần Mở Thành Viên Để Vào Map");
                            }
                        }
                    }
                    case 123 -> {
                        switch (select) {
//                            case 0 ->
//                                CombineService.gI().openTabCombine(player, CombineService.NANG_NGOC_BOI);
//                            case 0 ->
//                                Service.gI().showListTop(player, Manager.topngocboi);
                            case 0 ->
                                ChangeMapService.gI().changeMapNonSpaceship(player, 0, Util.nextInt(700, 800), 432);
//                            case 2 ->
//                                ShopService.gI().opendShop(player, "SHOP_NGOCBOI", false);
                            default -> {
                            }
                        }
                    }
                }
            }
            //  else if (player.iDMark.getIndexMenu() == ConstNpc.MENU_START_COMBINE) {
//                switch (player.combine.typeCombine) {
////                    case CombineService.NANG_NGOC_BOI -> {
////                        switch (select) {
////                            case 0 ->
////                                CombineService.gI().startCombine(player);
////                        }
////                    }
//
//                }
//            }
        }
    }

}
