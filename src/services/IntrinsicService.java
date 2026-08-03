package services;

/*
 *
 *
 * @author CongHoan
 */

import consts.ConstNpc;
import intrinsic.Intrinsic;
import item.Item;
import player.Player;
import server.Manager;
import network.Message;
import utils.Util;
import java.util.List;

public class IntrinsicService {

    private static IntrinsicService I;
    private static final int[] COST_OPEN = { 10, 20, 40, 80, 160, 320, 640, 1280 };
    private static final int ITEM_THOI_VANG = 457;
    private static final int ITEM_XU_ELITE = 1705;
    private static final int COST_OPEN_VIP = 100;
    private static final int RARE_HIGH_PARAM_INTRINSIC_ID = 23;
    private static final int RARE_HIGH_PARAM_START_PERCENT = 60;
    private static final int RARE_HIGH_PARAM_CHANCE = 10;

    public static IntrinsicService gI() {
        if (IntrinsicService.I == null) {
            IntrinsicService.I = new IntrinsicService();
        }
        return IntrinsicService.I;
    }

    public List<Intrinsic> getIntrinsics(byte playerGender) {
        switch (playerGender) {
            case 0:
                return Manager.INTRINSIC_TD;
            case 1:
                return Manager.INTRINSIC_NM;
            default:
                return Manager.INTRINSIC_XD;
        }
    }

    public Intrinsic getIntrinsicById(int id) {
        for (Intrinsic intrinsic : Manager.INTRINSICS) {
            if (intrinsic.id == id) {
                return new Intrinsic(intrinsic);
            }
        }
        return null;
    }

    public void sendInfoIntrinsic(Player player) {
        Message msg;
        try {
            msg = new Message(112);
            msg.writer().writeByte(0);
            msg.writer().writeShort(player.playerIntrinsic.intrinsic.icon);
            msg.writer().writeUTF(player.playerIntrinsic.intrinsic.getName());
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public void showAllIntrinsic(Player player) {
        List<Intrinsic> listIntrinsic = getIntrinsics(player.gender);
        Message msg;
        try {
            msg = new Message(112);
            msg.writer().writeByte(1);
            msg.writer().writeByte(1); // count tab
            msg.writer().writeUTF("Nội tại");
            msg.writer().writeByte(listIntrinsic.size() - 1);
            for (int i = 1; i < listIntrinsic.size(); i++) {
                msg.writer().writeShort(listIntrinsic.get(i).icon);
                msg.writer().writeUTF(listIntrinsic.get(i).getDescription());
            }
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public void settltd(Player player) {
        NpcService.gI().createMenuConMeo(player, ConstNpc.SET_TLTD, -1,
                "chọn lẹ đi để tau đi chơi với ny", "Món\nThiên Xin Hăn", "Món\nGenki", "Món\nKamejoko", "Từ chối");

    }

    public void settlnm(Player player) {
        NpcService.gI().createMenuConMeo(player, ConstNpc.SET_TLNM, -1,
                "chọn lẹ đi để tau đi chơi với ny", "Món\nPicolo", "Món\nỐc Tiêu", "Món\nPikkoro Daimao", "Món\nLiên Hoàn","Từ chối");

    }

    public void settlxd(Player player) {
        NpcService.gI().createMenuConMeo(player, ConstNpc.SET_TLXD, -1,
                "chọn lẹ đi để tau đi chơi với ny", "Món\nKakarot", "Món\nCadic", "Món\nNappa", "Từ chối");

    }

    public void sethdtd(Player player) {
        NpcService.gI().createMenuConMeo(player, ConstNpc.SET_HDTD, -1,
                "chọn lẹ đi để tau đi chơi với ny", "Món\nTien Xin Han", "Món\nGenki", "Món\nKamejoko", "Từ chối");

    }

    public void sethdnm(Player player) {
        NpcService.gI().createMenuConMeo(player, ConstNpc.SET_HDNM, -1,
                "chọn lẹ đi để tau đi chơi với ny", "Món\nPicolo", "Món\nỐc Tiêu", "Món\nPikkoro Daimao","Món\nLiên Hoàn","Từ chối");

    }

    public void sethdxd(Player player) {
        NpcService.gI().createMenuConMeo(player, ConstNpc.SET_HDXD, -1,
                "chọn lẹ đi để tau đi chơi với ny", "Món\nKakarot", "Món\nCadic", "Món\nNappa", "Từ chối");

    }
    
    public void setkhtd(Player player) {
        NpcService.gI().createMenuConMeo(player, ConstNpc.SET_KHTD, -1,
                "chọn lẹ đi để tau đi chơi với ny", "Món\nThiên Xin Hăn", "Món\nGenki", "Món\nKamejoko", "Từ chối");

    }

    public void setkhnm(Player player) {
        NpcService.gI().createMenuConMeo(player, ConstNpc.SET_KHNM, -1,
                "chọn lẹ đi để tau đi chơi với ny", "Món\nPicolo", "Món\nỐc Tiêu", "Món\nPikkoro Daimao", "Món\nLiên Hoàn","Từ chối");

    }

    public void setkhxd(Player player) {
        NpcService.gI().createMenuConMeo(player, ConstNpc.SET_KHXD, -1,
                "chọn lẹ đi để tau đi chơi với ny", "Món\nKakarot", "Món\nCadic", "Món\nNappa", "Từ chối");

    }

    public void showMenu(Player player) {
        NpcService.gI().createMenuConMeo(player, ConstNpc.INTRINSIC, -1,
                "Nội tại là một kỹ năng bị động hỗ trợ đặc biệt\nBạn có muốn mở hoặc thay đổi nội tại không?",
                "Xem\ntất cả\nNội Tại", "Mở\nNội Tại", "Mở VIP", "Từ chối");
    }

    public void showConfirmOpen(Player player) {
        NpcService.gI().createMenuConMeo(player, ConstNpc.CONFIRM_OPEN_INTRINSIC, -1,
                "Bạn muốn đổi Nội Tại khác\nvới giá là "
                        + getCostOpen(player) + " thỏi vàng ?",
                "Mở\nNội Tại", "Từ chối");
    }

    public void showConfirmOpenVip(Player player) {
        NpcService.gI().createMenuConMeo(player, ConstNpc.CONFIRM_OPEN_INTRINSIC_VIP, -1,
                "Bạn có muốn mở Nội Tại\nvới giá là " + COST_OPEN_VIP
                        + " xu elite và\ntái lập giá mở thường quay lại ban đầu không?",
                "Mở\nNội VIP", "Từ chối");
    }

    private void changeIntrinsic(Player player) {
        List<Intrinsic> listIntrinsic = getIntrinsics(player.gender);
        player.playerIntrinsic.intrinsic = new Intrinsic(listIntrinsic.get(Util.nextInt(1, listIntrinsic.size() - 1)));
        player.playerIntrinsic.intrinsic.param1 = randomParam1(player.playerIntrinsic.intrinsic);
        player.playerIntrinsic.intrinsic.param2 = (short) Util.nextInt(player.playerIntrinsic.intrinsic.paramFrom2,
                player.playerIntrinsic.intrinsic.paramTo2);
        Service.gI().sendThongBao(player, "Bạn nhận được Nội tại:\n" + player.playerIntrinsic.intrinsic.getName()
                .substring(0, player.playerIntrinsic.intrinsic.getName().indexOf(" [")));
        sendInfoIntrinsic(player);
    }

    private short randomParam1(Intrinsic intrinsic) {
        if (intrinsic.id != RARE_HIGH_PARAM_INTRINSIC_ID || intrinsic.paramTo1 <= intrinsic.paramFrom1) {
            return (short) Util.nextInt(intrinsic.paramFrom1, intrinsic.paramTo1);
        }
        int highStart = intrinsic.paramFrom1
                + (intrinsic.paramTo1 - intrinsic.paramFrom1 + 1) * RARE_HIGH_PARAM_START_PERCENT / 100;
        highStart = Math.max(intrinsic.paramFrom1 + 1, Math.min(highStart, intrinsic.paramTo1));
        if (Util.isTrue(RARE_HIGH_PARAM_CHANCE, 100)) {
            return (short) Util.nextInt(highStart, intrinsic.paramTo1);
        }
        return (short) Util.nextInt(intrinsic.paramFrom1, highStart - 1);
    }

    public void open(Player player) {
        if (player.nPoint.power >= 10000000000L) {
            int itemRequire = getCostOpen(player);
            Item thoiVang = InventoryService.gI().findItemBag(player, ITEM_THOI_VANG);
            if (thoiVang != null && thoiVang.quantity >= itemRequire) {
                InventoryService.gI().subQuantityItemsBag(player, thoiVang, itemRequire);
                InventoryService.gI().sendItemBag(player);
                changeIntrinsic(player);
                if (player.playerIntrinsic.countOpen < COST_OPEN.length - 1) {
                    player.playerIntrinsic.countOpen++;
                }
            } else {
                int have = thoiVang == null ? 0 : thoiVang.quantity;
                Service.gI().sendThongBao(player, "Bạn không đủ thỏi vàng, còn thiếu "
                        + (itemRequire - have) + " thỏi vàng nữa");
            }
        } else {
            Service.gI().sendThongBao(player, "Yêu cầu sức mạnh tối thiểu 10 tỷ");
        }
    }

    public void openVip(Player player) {
        if (player.nPoint.power >= 10000000000L) {
            Item xuElite = InventoryService.gI().findItemBag(player, ITEM_XU_ELITE);
            if (xuElite != null && xuElite.quantity >= COST_OPEN_VIP) {
                InventoryService.gI().subQuantityItemsBag(player, xuElite, COST_OPEN_VIP);
                InventoryService.gI().sendItemBag(player);
                changeIntrinsic(player);
                player.playerIntrinsic.countOpen = 0;
            } else {
                int have = xuElite == null ? 0 : xuElite.quantity;
                Service.gI().sendThongBao(player, "Bạn không có đủ xu elite, còn thiếu "
                        + (COST_OPEN_VIP - have) + " xu elite nữa");
            }
        } else {
            Service.gI().sendThongBao(player, "Yêu cầu sức mạnh tối thiểu 10 tỷ");
        }
    }

    private int getCostOpen(Player player) {
        int countOpen = Math.max(0, Math.min(player.playerIntrinsic.countOpen, COST_OPEN.length - 1));
        return COST_OPEN[countOpen];
    }

}
