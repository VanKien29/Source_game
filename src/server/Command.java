package server;

/**
 * @author CongHoan
 */
//import minigame.Taixiu.TaiXiu;
import boss.AnTromManager;
import boss.BossManager;
import boss.BrolyManager;
import boss.ChristmasEventManager;
import boss.GasDestroyManager;
import boss.OtherBossManager;
import boss.RedRibbonHQManager;
import boss.SnakeWayManager;
import boss.TreasureUnderSeaManager;
import boss.TrungThuEventManager;
import consts.ConstNpc;
import item.Item;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import minigame.LuckyNumber.LuckyNumber;
import models.GiftCode.GiftCodeManager;
import models.ShenronEvent.ShenronEvent;
import models.ShenronEvent.ShenronEventManager;
import player.Bot.BotManager;
import player.Pet;
import player.Player;

import services.InventoryService;
import services.ItemService;
import services.NpcService;
import services.PetService;
import services.Service;
import services.SkillService;
import services.TaskService;
import services.func.ChangeMapService;
import services.func.Input;
import services.func.TopService;
import skill.Skill;
import utils.Util;

public class Command {

    private static Command instance;

    public static Command gI() {
        if (instance == null) {
            instance = new Command();
        }
        return instance;
    }

    public void chat(Player player, String text) {
        if (!check(player, text)) {
            Service.gI().chat(player, text);
        }
    }

    public boolean check(Player player, String text) {
        if (player.isAdmin()) {
            System.out.println(
                    "[ADMIN COMMAND] "
                    + "Name=" + player.name
                    + " | ID=" + player.id
                    + " | Text=\"" + text + "\""
                    + " | Map=" + player.zone.map.mapName
            );
        }
        if (player.isAdmin()) {
            if (text.equals("code")) {
                GiftCodeManager.gI().checkInfomationGiftCode(player);
                return true;
            } else if (text.equals("a")) {
                BossManager.gI().showListBoss(player);
                return true;
            } else if (text.equals("brl")) {
                BrolyManager.gI().showListBoss(player);
                return true;
            } else if (text.equals("bot")) {
                NpcService.gI().createMenuConMeo(player, 206783, 206783, "|7| Menu bot\n"
                        + "Player online : " + Client.gI().getPlayers().size() + "\n"
                        + "Bot online : " + BotManager.gI().bot.size(),
                        "Bot\nPem Quái", "Bot\nBán Item", "Bot\nSăn Boss", "Đóng");
            } else if (text.equals("at")) {
                AnTromManager.gI().showListBoss(player);
                return true;
            } else if (text.equals("mapboss2")) {
                OtherBossManager.gI().showListBoss(player);
                return true;
            } else if (text.equals("mapdt")) {
                RedRibbonHQManager.gI().showListBoss(player);
                return true;
            } else if (text.equals("mapbdkb")) {
                TreasureUnderSeaManager.gI().showListBoss(player);
                return true;
            } else if (text.equals("mapcdrd")) {
                SnakeWayManager.gI().showListBoss(player);
                return true;
            } else if (text.equals("mapkghd")) {
                GasDestroyManager.gI().showListBoss(player);
                return true;
            } else if (text.equals("maptrungthu")) {
                TrungThuEventManager.gI().showListBoss(player);
                return true;
            } else if (text.equals("mapnoel")) {
                ChristmasEventManager.gI().showListBoss(player);
                return true;
            } else if (text.equals("hsk")) {
                Service.gI().releaseCooldownSkill(player);
                return true;
            } //             else if (text.startsWith("sp")) {
            //                try {
            //                    long power = Long.parseLong(text.replaceAll("up", ""));
            //                    Service.gI().addSMTN(player, (byte) 2, power, false);
            //                    return true;
            //                } catch (Exception e) {
            //                }
            //            } else if (text.equals("battu")) {
            //                if (player.isBattu) {
            //                    player.isBattu = false;
            //                } else {
            //                    player.isBattu = true;
            //                }
            //                Service.gI().sendThongBao(player, "Bất tử" + (player.isBattu ? ": ON" : ": OFF"));
            //                return true;
            //            } else if (text.startsWith("dt")) {
            //                try {
            //                    long power = Long.parseLong(text.replaceAll("upp", ""));
            //                    Service.gI().addSMTN(player.pet, (byte) 2, power, false);
            //                    return true;
            //                } catch (Exception e) {
            //                }
            //            } 
            //            else if (text.equals("test")) {
            //                switch (player.gender) {
            //                    case 0 ->
            //                        SkillService.gI().learSkillSpecial(player, Skill.SUPER_KAME, 1);
            //                    case 2 ->
            //                        SkillService.gI().learSkillSpecial(player, Skill.LIEN_HOAN_CHUONG, 1);
            //                    default ->
            //                        SkillService.gI().learSkillSpecial(player, Skill.MA_PHONG_BA, 1);
            //                }
            //                return true;
            //            } 
            //            else if (text.equals("test2")) {
            //                switch (player.gender) {
            //                    case 0 -> {
            //                        SkillService.gI().learSkillSpecial(player, Skill.PHAN_THAN, 6);
            //                    }
            //                    case 2 -> {
            //                        SkillService.gI().learSkillSpecial(player, Skill.PHAN_THAN, 6);
            //                    }
            //                    default -> {
            //                        SkillService.gI().learSkillSpecial(player, Skill.PHAN_THAN, 6);
            //                    }
            //                }
            //                return true;
            //            } 
            else if (text.equals("dragon")) {
                ShenronEvent shenron = new ShenronEvent();
                shenron.setPlayer(player);
                ShenronEventManager.gI().add(shenron);
                player.shenronEvent = shenron;
                shenron.setZone(player.zone);
                shenron.activeShenron(true, ShenronEvent.DRAGON_EVENT);
                shenron.sendWhishesShenron();
                return true;
            } else if (text.equals("topxu")) {
                Service.gI().sendThongBaoOK(player, TopService.getTopQuocVuong());
                return true;
            } else if (text.equals("ad")) { // menu admin kiendeptrai
                Input.gI().createFormAdmin(player);//đây là lệnh admin ( ctrl click chuột phải vào createFormAdmin
                return true;
            } // else if (text.equals("daucatmoi")) {
            //                for (int i = 0; i < 10; i++) {
            //                    ServerNotify.gI().notify("BOSS Nro vừa xuất hiện tại nhà anh ấy");
            //                }
            //                return true;
            //            }
            else if (text.equals("adb")) { // buf hộp thư
                Input.gI().createFromMailBox(player);
                return true;
            } else if (text.startsWith("m")) {
                String mapIdStr = text.replaceFirst("m", "").trim();
                if (mapIdStr.isEmpty()) {
                    //   Service.gI().sendThongBao(player, "Vui lòng nhập id map. Ví dụ: m 5");
                    return true;
                }
                try {
                    int mapId = Integer.parseInt(mapIdStr);
                    ChangeMapService.gI().changeMapInYard(player, mapId, -1, -1);
                } catch (NumberFormatException e) {
                    Service.gI().sendThongBao(player, "Id map không hợp lệ. Ví dụ: m 5");
                }
                return true;
            }
            if (text.startsWith("dmg")) {
                try {
                    long dameg = Integer.parseInt(text.replaceAll("dmg", ""));
                    player.nPoint.dameg = dameg;
                    Service.gI().point(player);
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (text.startsWith("hpg")) {
                try {
                    long hpg = Integer.parseInt(text.replaceAll("hpg", ""));
                    player.nPoint.hpg = hpg;
                    Service.gI().point(player);
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (text.startsWith("mpg")) {
                try {
                    long mpg = Integer.parseInt(text.replaceAll("mpg", ""));
                    player.nPoint.mpg = mpg;
                    Service.gI().point(player);
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (text.startsWith("defg")) {
                try {
                    int defg = Integer.parseInt(text.replaceAll("defg", ""));
                    player.nPoint.defg = defg;
                    Service.gI().point(player);
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
//             if (text.equals("tx")) {
//                player.isMenuProcessing = false;
//                showTaiXiuStats(player);
//                return true;
//            }
            if (text.startsWith("crg")) {
                try {
                    int critg = Integer.parseInt(text.replaceAll("crg", ""));
                    player.nPoint.critg = critg;
                    Service.gI().point(player);
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (text.startsWith("adminnnv")) {
                try {
                    int idTask = Integer.parseInt(text.replaceAll("nnv", ""));
                    player.playerTask.taskMain.id = idTask - 1;
                    player.playerTask.taskMain.index = 0;
                    TaskService.gI().sendNextTaskMain(player);
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (text.startsWith("kq")) {
                Service.gI().sendThongBao(player, "Kết quả Lucky Round tiếp theo là: " + LuckyNumber.RESULT);
                return true;
            }

            if (text.startsWith("gender_")) {
                byte idGender = Byte.parseByte(text.replaceAll("gender_", ""));
                player.gender = idGender;
                return true;
            }
            if (text.startsWith("i")) {
                String[] parts = text.split(" ");
                if (parts.length >= 2) {
                    short id = Short.parseShort(parts[1]);
                    int quantity = 1; // mặc định = 1
                    if (parts.length >= 3) {
                        quantity = Integer.parseInt(parts[2]); // nếu có nhập thì lấy giá trị nhập
                    }

                    Item item = ItemService.gI().createNewItem(id, quantity);
                    List<Item.ItemOption> ops = ItemService.gI().getListOptionItemShop((short) id);
                    if (!ops.isEmpty()) {
                        item.itemOptions = ops;
                    }
                    InventoryService.gI().addItemBag(player, item);
                    InventoryService.gI().sendItemBag(player);
                    Service.gI().sendThongBao(player,
                            "GET " + item.template.name + " [" + item.template.id + "] x" + quantity + " SUCCESS !");
                    return true;
                } else {
                    //Service.gI().sendThongBao(player, "Lỗi cú pháp! Ví dụ: i <id> [số lượng]");
                    return true;
                }
            }
            if (text.startsWith("i")) {
                String[] parts = text.split(" ");
                if (parts.length >= 3) {
                    short id = Short.parseShort(parts[1]);
                    int quantity = Integer.parseInt(parts[2]);
                    Item item = ItemService.gI().createNewItem(id, quantity);
                    List<Item.ItemOption> ops = ItemService.gI().getListOptionItemShop((short) id);
                    if (!ops.isEmpty()) {
                        item.itemOptions = ops;
                    }
                    InventoryService.gI().addItemBag(player, item);
                    InventoryService.gI().sendItemBag(player);
                    Service.gI().sendThongBao(player,
                            "GET " + item.template.name + " [" + item.template.id + "] SUCCESS !");
                    return true;
                } else {
                    Service.gI().sendThongBao(player, "Lỗi");
                    return true;
                }
            }
//            else if (text.startsWith("i ")) {
//                int itemId = Integer.parseInt(text.replace("i ", ""));
//                Item item = ItemService.gI().createNewItem(((short) itemId));
//                List<Item.ItemOption> ops = ItemService.gI().getListOptionItemShop((short) itemId);
//                if (!ops.isEmpty()) {
//                    item.itemOptions = ops;
//                }
//                InventoryService.gI().addItemBag(player, item);
//                InventoryService.gI().sendItemBag(player);
//                Service.gI().sendThongBao(player, "GET " + item.template.name + " ["
//                        + item.template.id + "] SUCCESS !");
//                return true;
//            } 
//            else if (text.equals("item")) {
//                Input.gI().createFormGiveItem(player);
//                return true;
//            } else if (text.equals("getitem")) {
//                Input.gI().createFormGetItem(player);
//                return true;
//            } else if (text.equals("d")) {
//                Service.gI().setPos(player, player.location.x, player.location.y + 10);
//                return true;
//            }
        }
        if (text.startsWith("ten con la ")) {
            PetService.gI().changeNamePet(player, text.replaceAll("ten con la ", ""));
        }

        if (player.pet != null) {
            switch (text) {
                case "di theo", "follow" ->
                    player.pet.changeStatus(Pet.FOLLOW);
                case "bao ve", "protect" ->
                    player.pet.changeStatus(Pet.PROTECT);
                case "tan cong", "attack" ->
                    player.pet.changeStatus(Pet.ATTACK);
                case "ve nha", "go home" ->
                    player.pet.changeStatus(Pet.GOHOME);
                case "bien hinh" ->
                    player.pet.transform();
            }
        }
        return false;
    }
//     public void showTaiXiuStats(Player player) {
//        // Kiểm tra nếu người chơi đang xử lý một menu khác
//        if (player.isMenuProcessing) {
//            return;
//        }
//        // Tạo thông tin menu
//        StringBuilder menuText = new StringBuilder();
//        long currentTime = System.currentTimeMillis();
//        long remainingTime = (TaiXiu.gI().lastTimeEnd - currentTime) / 1000;
//        if (remainingTime < 0) {
//            remainingTime = 0;
//        }
//        menuText.append("Thống kê Tài Xỉu\n")
//                .append("Thời gian còn lại: ").append(remainingTime).append(" giây\n")
//                .append("Tổng tiền Tài: ").append(Util.numberToMoney(TaiXiu.gI().goldTaiReal)).append(" vàng\n")
//                .append("Tổng tiền Xỉu: ").append(Util.numberToMoney(TaiXiu.gI().goldXiuReal)).append(" vàng\n")
//                .append("Số người chơi Tài: ").append(TaiXiu.gI().PlayersTai.size()).append("\n")
//                .append("Số người chơi Xỉu: ").append(TaiXiu.gI().PlayersXiu.size()).append("\n")
//                .append("Kết quả hiện tại: ").append(TaiXiu.gI().x).append(" - ")
//                .append(TaiXiu.gI().y).append(" - ").append(TaiXiu.gI().z).append("\n")
//                .append("Cân bằng tiền: ").append(TaiXiu.gI().balanceGold ? "ON" : "OFF").append("\n");
//
//        // Hiển thị tỉ lệ bên ít tiền thắng nếu có
//        if (TaiXiu.gI().winRateForLessMoneyBet > 0) {
//            menuText.append("Tỉ lệ bên ít thắng: ").append(TaiXiu.gI().winRateForLessMoneyBet).append("%\n");
//        }
//
//        // Hiển thị kết quả đã được thiết lập nếu có
//        if (TaiXiu.gI().resultSetByAdmin) {
//            int tong = TaiXiu.gI().x + TaiXiu.gI().y + TaiXiu.gI().z;
//            boolean isTamHoa = (TaiXiu.gI().x == TaiXiu.gI().y && TaiXiu.gI().y == TaiXiu.gI().z);
//            boolean isTai = tong > 10 && !isTamHoa;
//            boolean isXiu = !isTai && !isTamHoa;
//
//            if (isTai) {
//                menuText.append("Đã đặt kết quả: TÀI");
//            } else if (isXiu) {
//                menuText.append("Đã đặt kết quả: XỈU");
//            } else if (isTamHoa) {
//                menuText.append("Đã đặt kết quả: TAM HOA");
//            }
//        }
//
//        // Hiển thị menu với các tùy chọn mới
//        NpcService.gI().createMenuConMeo(player, ConstNpc.MENU_TX, -1, menuText.toString(),
//                new String[]{"Cân bằng\nTiền", "Set Kết quả", "Tỉ lệ\nbên ít thắng", "Tài thắng", "Xỉu thắng", "Đóng"});
//
//        // Nếu đã có một timer cập nhật, hủy nó
//        if (player.taiXiuUpdateTimer != null) {
//            player.taiXiuUpdateTimer.cancel();
//            player.taiXiuUpdateTimer = null;
//        }
//
//        // Tạo timer mới để cập nhật menu
//        player.taiXiuUpdateTimer = new Timer();
//        player.taiXiuUpdateTimer.schedule(new TimerTask() {
//            public void run() {
//                // Kiểm tra nếu người chơi vẫn đang xem menu này
//                if (player.session != null
//                        && player.iDMark.getIndexMenu() == ConstNpc.MENU_TX
//                        && !player.isMenuProcessing) {
//                    showTaiXiuStats(player);
//                } else {
//                    // Hủy timer nếu người chơi đã thoát menu hoặc đang xử lý
//                    if (player.taiXiuUpdateTimer != null) {
//                        player.taiXiuUpdateTimer.cancel();
//                        player.taiXiuUpdateTimer = null;
//                    }
//                }
//            }
//        }, 1000); // Cập nhật sau 1 giây
//    }
}
