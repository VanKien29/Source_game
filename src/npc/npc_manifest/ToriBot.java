package npc.npc_manifest;

import consts.ConstNpc;
import npc.Npc;
import player.Player;
import services.func.UseItem;

public class ToriBot extends Npc {

    public ToriBot(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            createOtherMenu(player, ConstNpc.BASE_MENU,
                    "Trong thời gian mùa 1 diễn ra\nNếu mua VIP sẽ được nhận\nnhiều ưu đãi hơn nữa.\nLưu ý: nâng cấp VIP chỉ được nâng 1 lần mỗi mùa",
                    "VIP 1", "VIP 2", "VIP 3", "Thông Tin\nKick vip", "Đóng");
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            if (this.mapId == 0 || this.mapId == 7 || this.mapId == 14) {
                if (player.iDMark.isBaseMenu()) {
                    switch (select) {
                        case 0 ->
                            createOtherMenu(player, 2,
                                    "|7|Nâng cấp VIP 1 bạn sẽ nhận được"
                                    + "\n|2|- 500tr vàng, 30 đá bảo vệ"
                                    + "\n- 70 thỏi vàng và 1 phiếu giảm giá 80%"
                                    + "\n- 30% may mắn, Avatar đẹp trai nhất vũ trụ"
                                    + "\n- 5 xí muội hoa mai"
                                    + "\n- 20 mảnh đội trưởng vàng"
                                    + "\n- 20 mảnh Rồng Thần Namek"
                                    + "\n- 15 spl tnsm và 3 hộp skill đệ tử",
                                    "20.000 vnd", "Đóng");

                        case 1 ->
                            createOtherMenu(player, 4,
                                    "|7|Nâng cấp VIP 2 bạn sẽ nhận được"
                                    + "\n|2|- 1 tỷ vàng, 50 đá bảo vệ"
                                    + "\n- 150 thỏi vàng và 1 phiếu giảm giá 80%"
                                    + "\n- 30% may mắn, Avatar đẹp trai nhất vũ trụ"
                                    + "\n- 10 xí muội hoa mai và 10 xí muội hoa đào"
                                    + "\n- 15 spl tnsm"
                                    + "\n- 30 mảnh đội trưởng vàng"
                                    + "\n- 30 mảnh Rồng Thần Namek"
                                    + "\n- Cải trang 20-25% chỉ số",
                                    "50.000 vnd", "Đóng");

                        case 2 ->
                            createOtherMenu(player, 6,
                                    "|7|Nâng cấp VIP 3 bạn sẽ nhận được"
                                    + "\n|2|- 1.5 tỷ vàng, 50 đá bảo vệ"
                                    + "\n- 350 thỏi vàng, 250 xu elite, 1 phiếu giảm giá 80%"
                                    + "\n- 30% may mắn, Avatar đẹp trai nhất vũ trụ"
                                    + "\n- 15 xí muội hoa mai và 15 xí muội hoa đào"
                                    + "\n- 15 spl tnsm"
                                    + "\n- 50 mảnh đội trưởng vàng"
                                    + "\n- 50 mảnh Rồng Thần Namek"
                                    + "\n- Cải trang 25-30% chỉ số"
                                    + "\n- Pet 5-10% chỉ số",
                                    "150.000 vnd", "Đóng");
                        case 3 ->
                            this.createOtherMenu(player, 3422,
                                    "|7| Kick Vip Đi nè"
                                    + (player.vip == 1 ? "\n|7|Status VIP : VIP 1" : player.vip == 2 ? "\n|7|Trạng Thái VIP : VIP 2" : player.vip == 3 ? "\n|7|Trạng Thái VIP : VIP 3" : "")
                                    + "\n|0|Cảm Ơn Đã Ủng Hộ Ngọc Rồng Hdpe",
                                    //  + (player.timevip > 0 ? "\nHạn còn : " + Util.msToThang(player.timevip) : ""),
                                    "Đóng");
                    }
                } else if (player.iDMark.getIndexMenu() == 2) {
                    if (select == 0) {
                        // if (player.pet != null) {
                        //     createOtherMenu(player, 8,
                        //             "|7|Nâng cấp VIP 1"
                        //             + "\n|2|- Bạn có muốn thay thế đệ tử hiện có thành đệ tử mới không ? đệ tử cũ sẽ biến mất lưu ý hãy tháo đồ nếu đổi đệ tử mới",
                        //             "Có", "Không");
                        // } else {
                        UseItem.gI().ComfirmNhanVIP(player, true);
                        // }
                    }
                } else if (player.iDMark.getIndexMenu() == 4) {
                    if (select == 0) {
                        // if (player.pet != null) {
                        //     createOtherMenu(player, 10,
                        //             "|7|Nâng cấp VIP 2"
                        //             + "\n|2|- Bạn có muốn thay thế đệ tử hiện có thành đệ tử mới không ? đệ tử cũ sẽ biến mất lưu ý hãy tháo đồ nếu đổi đệ tử mới",
                        //             "Có", "Không");
                        // } else {
                        UseItem.gI().ComfirmNhanVIP2(player, true);
                        // }
                    }
                } else if (player.iDMark.getIndexMenu() == 6) {
                    if (select == 0) {
                        // if (player.pet != null) {
                        //     createOtherMenu(player, 12,
                        //             "|7|Nâng cấp VIP 3"
                        //             + "\n|2|- Bạn có muốn thay thế đệ tử hiện có thành đệ tử mới không ? đệ tử cũ sẽ biến mất lưu ý hãy tháo đồ nếu đổi đệ tử mới",
                        //             "Có", "Không");
                        // } else {
                        UseItem.gI().ComfirmNhanVIP3(player, true);
                        // }
                    }
                    // } else if (player.iDMark.getIndexMenu() == 8) {
                    //     UseItem.gI().ComfirmNhanVIP(player, select == 0);
                    // } else if (player.iDMark.getIndexMenu() == 10) {
                    //     UseItem.gI().ComfirmNhanVIP2(player, select == 0);
                    // } else if (player.iDMark.getIndexMenu() == 12) {
                    //     UseItem.gI().ComfirmNhanVIP3(player, select == 0);
                }
            }
        }
    }
}
