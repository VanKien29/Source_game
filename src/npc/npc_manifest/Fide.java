package npc.npc_manifest;

import consts.ConstNpc;
import consts.ConstTranhNgocNamek;
import consts.cn;
import item.Item;
import java.text.NumberFormat;
import java.util.Locale;
import jdbc.daos.PlayerDAO;
import models.DragonNamecWar.TranhNgocService;
import npc.Npc;
import player.Player;
import services.PetService;
import services.Service;
import services.TaskService;
import services.func.TopService;
import shop.ShopService;
import utils.Util;

public class Fide extends Npc {

    public Fide(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }
    private String formatCoin(long coin) {
        return NumberFormat.getInstance(new Locale("vi", "VN")).format(coin);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            TaskService.gI().checkDoneTaskTalkNpc(player, this);
            if (mapId == 5) {
                createOtherMenu(player, ConstNpc.BASE_MENU,
                        "|0| Ta chỉ bán đồ cho người giàu, ngươi có tiền không đó ???\n|6|"
                        + "Số điểm săn boss hiện có: "
                        + formatCoin(player.event.getEventPointBHM()) + " điểm.",
                        "Cửa hàng\n tiện lợi", "Cửa hàng\n cao cấp",
                        "Cửa hàng\n điểm boss", "Xem top\n kill boss", "Cửa hàng\n thỏi vàng");
            } else if (mapId == ConstTranhNgocNamek.MAP_ID) {
                if (player.iDMark.getTranhNgoc() == 1) {
                    this.createOtherMenu(player, ConstNpc.BASE_MENU,
                            "Phắn đê !Ta không nói chuyện với sinh vật hạ đẳng", "Đóng");
                    return;
                }
                this.createOtherMenu(player, ConstNpc.BASE_MENU,
                        "Hãy mang ngọc rồng về cho ta", "Đưa ngọc", "Đóng");
            }
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            if (player.iDMark.getIndexMenu() == ConstNpc.BASE_MENU) {
                if (this.mapId == 5) {
                    switch (select) {
//                        case 0 -> {
//                            ShopService.gI().opendShop(player, "SANTA_PHUKIEN", false);
//                        }
                        case 4 -> {
                            ShopService.gI().opendShop(player, "SHOP_TV", false);
                        }
                        case 0 -> {
                            ShopService.gI().opendShop(player, "SHOP_NANGCAP", false);
                        }
                        case 1 -> {
                            ShopService.gI().opendShop(player, "SHOP_XU_KRAI", false);
                        }
                        case 2 -> {
                            ShopService.gI().opendShop(player, "SHOP_DIEMSANBOSS", false);
                        }
                        case 3 -> {
                            TopService.showListTop(player, 8);
                        }
                    }
                }
            } else if (this.mapId == ConstTranhNgocNamek.MAP_ID) {
                switch (select) {
                    case 0 -> {
                        if (player.iDMark.getTranhNgoc() == 2 && player.isHoldNamecBallTranhDoat) {
                            if (!Util.canDoWithTime(player.lastTimePickItem, 20000)) {
                                Service.gI().sendThongBao(player, "Vui lòng đợi "
                                        + ((player.lastTimePickItem + 20000 - System.currentTimeMillis()) / 1000)
                                        + " giây để có thể trả");
                                return;
                            }
                            TranhNgocService.getInstance().dropBall(player, (byte) 2);
                            player.zone.pointRed++;
                            if (player.zone.pointRed > ConstTranhNgocNamek.MAX_POINT) {
                                player.zone.pointRed = ConstTranhNgocNamek.MAX_POINT;
                            }
                            TranhNgocService.getInstance().sendUpdatePoint(player);
                        }
                    }
                }
            }
        }
    }
}
