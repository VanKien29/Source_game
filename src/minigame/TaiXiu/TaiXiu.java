//package minigame.Taixiu;
//
//import com.mysql.jdbc.CallableStatement;
//import consts.ConstTask;
//import item.Item;
//import java.io.IOException;
//import java.sql.Connection;
//import java.sql.PreparedStatement;
//import java.sql.SQLException;
//import java.util.ArrayList;
//import java.util.List;
//import jdbc.DBConnecter;
//import jdbc.daos.PlayerDAO;
//import network.Message;
//import player.Player;
//import server.Client;
//import services.ChatGlobalService;
//import services.InventoryService;
//import services.ItemService;
//import services.Service;
//import utils.Logger;
//import utils.Util;
//import java.sql.ResultSet;
//import java.sql.Connection;
//import java.sql.SQLException;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.Set;
//import java.util.concurrent.Executors;
//import java.util.concurrent.ScheduledExecutorService;
//import java.util.concurrent.TimeUnit;
//import map.Map;
//import services.TaskService;
//
///**
// *
// * @author PPC
// */
//public class TaiXiu implements Runnable {
//
//    Set<Integer> processedPlayers = new HashSet<>();
//    public long goldTai;
//    public long goldXiu;
//    public long goldTaiReal;
//    public long goldXiuReal;
//    public boolean ketquaTai = false;
//    public boolean ketquaXiu = false;
//    public boolean ketquaTamhoa = false;
//    public boolean balanceGold = false; // Biến để bật/tắt cân bằng tiền♥
//    public boolean resultSetByAdmin = false; // Flag để kiểm tra kết quả có được set bởi admin hay không
//    public boolean baotri = false;
//    public boolean resultDiceSetByAdmin = false;
//    public long lastTimeEnd;
//    public byte troll = 0;//muốn nhiêu % xuất hiến troll thì ghi 0% là tắt♥
//    public List<Player> PlayersTai = new ArrayList<>();
//    public List<Player> PlayersXiu = new ArrayList<>();
//    private static TaiXiu instance;
//    public int x, y, z;
//    private Thread balanceThread; // Luồng để cập nhật cân bằng tiền
//    private ScheduledExecutorService scheduler;
//    private ScheduledExecutorService updateExecutor;
//    private long lastBalanceTime = 0;
//    private int forceImbalanceCount = 0; // Để lâu lâu cho 1 bên cao hẳn
//    private int tamHoaMultiplier = 0; // Số X từsetResult 2-88
//
//// 1. Thêm biến để lưu tỉ lệ thắng cho bên ít tiền hơn
//    public int winRateForLessMoneyBet = 50; // Mặc định là 50%♥
//
//// 2. Thêm biến để lưu kết quả đã được thiết lập 
//    public String forcedResult = null; // "TAI", "XIU" hoặc null (để ngẫu nhiên)
//
//    class Pair<K, V> {
//
//        private K key;
//        private V value;
//
//        public Pair(K key, V value) {
//            this.key = key;
//            this.value = value;
//        }
//
//        public K getKey() {
//            return key;
//        }
//
//        public V getValue() {
//            return value;
//        }
//    }
//
//    public static TaiXiu gI() {
//        if (instance == null) {
//            instance = new TaiXiu();
//        }
//        return instance;
//    }
//// 3. Thêm phương thức để thiết lập tỉ lệ thắng cho bên ít tiền
//
//    public void setWinRateForLessMoneyBet(int percentage) {
//        if (percentage >= 0 && percentage <= 100) {
//            this.winRateForLessMoneyBet = percentage;
//            Logger.log("Đã thiết lập tỉ lệ thắng cho bên ít tiền thành " + percentage + "%");
//        } else {
//            Logger.error("Tỉ lệ không hợp lệ: " + percentage + ". Phải từ 0-100%");
//        }
//    }
//
//// 4. Thêm phương thức để đặt kết quả là Tài
//    public void setResultToTai() {
//        this.forcedResult = "TAI";
//        this.resultSetByAdmin = true;
//        Logger.log("Đã thiết lập kết quả tiếp theo là: TÀI");
//    }
//
//// 5. Thêm phương thức để đặt kết quả là Xỉu
//    public void setResultToXiu() {
//        this.forcedResult = "XIU";
//        this.resultSetByAdmin = true;
//        Logger.log("Đã thiết lập kết quả tiếp theo là: XỈU");
//    }
//
//    public void addPlayerXiu(Player pl) {
//        if (!PlayersXiu.contains(pl)) {  // Sử dụng contains thay vì equals
//            PlayersXiu.add(pl);
//        }
//    }
//
//// Constructor khởi tạo luồng cân bằng tiền
//    public TaiXiu() {
//        scheduler = Executors.newSingleThreadScheduledExecutor();
//        scheduler.scheduleAtFixedRate(this::balanceGoldLoop, 0, 1, TimeUnit.SECONDS);
//        // khởi tạo executor để gửi thông tin vàng mỗi 100ms
//        // this.updateExecutor = Executors.newSingleThreadScheduledExecutor();
//        //   this.updateExecutor.scheduleAtFixedRate(this::sendBetUpdateToAll, 0, 100, TimeUnit.MILLISECONDS);
//    }
//
//// Thêm phương thức để dừng scheduler khi cần
//    public void shutdown() {
//        if (scheduler != null && !scheduler.isShutdown()) {
//            scheduler.shutdown();
//        }
//        if (updateExecutor != null & !updateExecutor.isShutdown()) {
//            updateExecutor.shutdown();
//        }
//    }
//
//    // Luồng riêng để cân bằng tiền và gửi cập nhật
//    private void balanceGoldLoop() {
//        while (!Thread.currentThread().isInterrupted()) {
//            try {
//                if (balanceGold) {
//                    balanceTaiXiuGold();
//                    sendBetUpdateToAll();
//
//                }
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                Thread.currentThread().interrupt(); // Reset interrupt flag
//                break; // Thoát khỏi vòng lặp
//            } catch (Exception e) {
//                Logger.error("Lỗi trong luồng cân bằng tiền Tài Xỉu: " + e.getMessage());
//                e.printStackTrace();
//            }
//        }
//    }
//
//    private void sendBetUpdateToAll() {
//        for (Player pl : Client.gI().getPlayers()) {
//            if (pl != null && Client.gI().getPlayer(pl.name) != null && pl.session != null) {
//                // Gửi thông tin cập nhật tổng tiền cược mà không cần liên quan đến người chơi cụ thể
//                Message msg = null;
//                try {
//                    msg = new Message(-109); // Message cập nhật thông tin
//                    msg.writer().writeShort(1);
//                    msg.writer().writeByte(5); // Type 5: thông tin cược hiện tại
//
//                    msg.writer().writeLong(pl.goldTai);
//                    msg.writer().writeLong(pl.goldXiu);
//                    // Gửi tổng tiền cược của mỗi bên
//                    msg.writer().writeLong(TaiXiu.gI().goldTai);
//                    msg.writer().writeLong(TaiXiu.gI().goldXiu);
//
//                    // Gửi message cho client
//                    Service.gI().sendMessage(pl, msg);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                } finally {
//                    if (msg != null) {
//                        msg.cleanup();
//                    }
//                }
//            }
//        }
//    }
//// Thêm hàm xử lý chat Tài Xỉu
//
//    public void handleTXChat(Player player, Message msg) {
//        try {
//            String content = msg.reader().readUTF();
//
//            // Kiểm tra spam
//            long currentTime = System.currentTimeMillis();
//            if (currentTime - player.lastTimeTXChat < 3000) { // Giới hạn 3 giây/1 tin nhắn
//                Service.gI().sendThongBao(player, "Vui lòng đợi "
//                        + ((3000 - (currentTime - player.lastTimeTXChat)) / 1000)
//                        + " giây để chat tiếp");
//                return;
//            }
//
//            // Kiểm tra nội dung tin nhắn
//            if (content.length() > 60) {
//                content = content.substring(0, 60);
//            }
//
//            // Kiểm tra từ khóa nhạy cảm (có thể mở rộng)
//            String[] bannedWords = {"dm", "dit", "dcm", "clm", "cc"};
//            for (String word : bannedWords) {
//                if (content.toLowerCase().contains(word)) {
//                    Service.gI().sendThongBao(player, "Nội dung chat chứa từ ngữ không phù hợp!");
//                    return;
//                }
//            }
//
//            // Cập nhật thời gian chat cuối
//            player.lastTimeTXChat = currentTime;
//
//            // Xác định loại người chơi - CHỈ CÓ MỘT LOẠI duy nhất cho mỗi người chơi
//            int playerType = getPlayerType(player);
//
//            // Gửi tin nhắn tới tất cả người chơi trong phòng TX
//            Message msgChat = new Message(105);
//            msgChat.writer().writeUTF(player.name);  // Tên người gửi
//            msgChat.writer().writeUTF(content);      // Nội dung tin nhắn
//            msgChat.writer().writeInt(playerType);   // Loại người chơi (0: thường, 1: admin, 2: hệ thống)
//
//            // Gửi cho tất cả người chơi online
//            for (Player p : Client.gI().getPlayers()) {
//                if (p != null && p.session != null) {
//                    Service.gI().sendMessage(p, msgChat);
//                }
//            }
//
//            msgChat.cleanup();
//        } catch (Exception e) {
//            Logger.error("Lỗi xử lý chat Tài Xỉu: " + e.getMessage());
//            e.printStackTrace();
//        }
//    }
//
//    private int getPlayerType(Player player) {
//        // Admin có playerType = 1, người chơi thường = 0
//        if (player.isAdmin()) {
//            return 1;
//        }
//
//        // Check trường hợp đặc biệt khác nếu cần
//        // if (player.isMod()) return 2;
//        // if (player.isVIP()) return 3;
//        return 0; // Mặc định là người chơi thường
//    }
//
//    public void sendWinAmount(Player pl, long amount, boolean iswin) {
//        Message msg = null;
//        try {
//            msg = new Message(-109); // Message cập nhật thông tin
//            msg.writer().writeShort(1);
//            msg.writer().writeByte(6); // Type 5: thông tin cược hiện tại
//
//            // Gửi tổng tiền cược của mỗi bên
//            msg.writer().writeLong(amount);
//            msg.writer().writeBoolean(iswin);
//
//            // Gửi message cho client
//            Service.gI().sendMessage(pl, msg);
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            if (msg != null) {
//                msg.cleanup();
//            }
//        }
//
//    }
//
////    public void CheckLogOutPlayer(Player pl) {
////        long currentTime = System.currentTimeMillis();
////        long remainingTime = (lastTimeEnd - currentTime) / 1000;
////        if (remainingTime > 3 && (pl.goldTai > 0 || pl.goldXiu > 0)) {
////            // Hoàn vàng cho người chơi
////            pl.inventory.gold += pl.goldTai + pl.goldXiu;
////            Logger.log("Hoàn vàng tdxd " + (pl.goldTai + pl.goldXiu) + " cho " + pl.name);
////
////            // Trừ vàng ở cửa Tài nếu có
////            if (pl.goldTai > 0) {
////                this.goldTai -= pl.goldTai;
////                this.goldTaiReal -= pl.goldTai;
////                // Xóa người chơi khỏi danh sách Tài
////                PlayersTai.remove(pl);  // Sử dụng trực tiếp remove thay vì qua hàm
////                pl.goldTai = 0;  // Reset giá trị vàng Tài
////            }
////
////            // Trừ vàng ở cửa Xỉu nếu có
////            if (pl.goldXiu > 0) {
////                this.goldXiu -= pl.goldXiu;
////                this.goldXiuReal -= pl.goldXiu;
////                // Xóa người chơi khỏi danh sách Xỉu
////                PlayersXiu.remove(pl);  // Sử dụng trực tiếp remove thay vì qua hàm
////                pl.goldXiu = 0;  // Reset giá trị vàng Xỉu
////            }
////
////            // Cập nhật tiền cho client (nếu có thể)
////            if (pl.session != null) {
////                Service.gI().sendMoney(pl);
////            }
////            // Thông báo reset cho người chơi
////            for (Player pl1 : Client.gI().getPlayers()) {
////                if (pl1 != null && Client.gI().getPlayer(pl1.name) != null) {
////                    sendResetInfo(pl1);
////                    sendTimeInfo(pl1);
////                    // Cập nhật thông tin cược cho tất cả người chơi
////                    sendBetUpdateToAll();
////                }
////            }
////
////        }
////    }
//    public void removePlayerXiu(Player pl) {
//        PlayersXiu.remove(pl);  // Loại bỏ điều kiện không cần thiết
//    }
//
//    public void removePlayerTai(Player pl) {
//        PlayersTai.remove(pl);  // Loại bỏ điều kiện không cần thiết
//    }
//
//    public void addPlayerTai(Player pl) {
//        if (!PlayersTai.contains(pl)) {  // Sử dụng contains thay vì equals
//            PlayersTai.add(pl);
//        }
//    }
//// Thêm phương thức set kết quả xúc xắc
//
//    public void setResult(int x, int y, int z) {
//        this.x = x;
//        this.y = y;
//        this.z = z;
//        this.resultDiceSetByAdmin = true; // Đánh dấu rằng xúc xắc đã được set bởi admin
//    }
//
//    public void removePlayerXiu1(Player pl) {
//        if (PlayersXiu.equals(pl)) {
//            PlayersXiu.remove(pl);
//        }
//    }
//
//    public void removePlayerTai1(Player pl) {
//        if (PlayersTai.equals(pl)) {
//            PlayersTai.remove(pl);
//        }
//    }
//
//    @Override
//    public void run() {
//        while (!Thread.currentThread().isInterrupted()) {
//            try {
//                long currentTime = System.currentTimeMillis();
//                long remainingTime = (TaiXiu.gI().lastTimeEnd - currentTime) / 1000;
//
//                if (remainingTime <= 0) {
//                    // Kiểm tra nếu có kết quả bị ép buộc
//
//                    // Kiểm tra nếu admin đã set kết quả xúc xắc
//                    if (!resultDiceSetByAdmin) {
//                        // Nếu admin chưa set kết quả xúc xắc, thì mới tiến hành random
//                        if (forcedResult != null) {
//                            // Nếu có thiết lập kết quả cụ thể
//                            generateForcedResult();
//                        } else {
//                            // Nếu không có kết quả bị ép buộc, áp dụng tỉ lệ thắng cho bên ít tiền
//                            applyWinRateForLessMoneyBet();
//                        }
//                    }
//
//                    int tong = (x + y + z);
//
//                    // Xác định kết quả
//                    ketquaTamhoa = (x == y && y == z);
//                    ketquaXiu = (tong > 3 && tong < 11 && !ketquaTamhoa);
//                    ketquaTai = (tong > 10 && !ketquaTamhoa);
//                    resultDiceSetByAdmin = false;
//                    // Nếu là Tam Hoa, random số X từ 2-88
//                    // Nếu là Tam Hoa, random số X dựa trên tổng gold real
//                    if (ketquaTamhoa) {
//                        if (tong > 3 && tong < 11 && goldXiuReal < 1_000_000_000 || tong > 10 && goldTaiReal < 1_000_000_000) {
//                            if (Util.isTrue(7, 8)) {
//                                tamHoaMultiplier = Util.nextInt(2, 5); // Tổng dưới 1 tỷ: 0-88
//                            } else {
//                                tamHoaMultiplier = Util.nextInt(2, 88);
//                            }
//                        } else {
//                            tamHoaMultiplier = Util.nextInt(2, 5); // Tổng từ 1 tỷ trở lên: 2-5
//                        }
//                    } else {
//                        tamHoaMultiplier = 0; // Reset nếu không phải Tam Hoa
//                    }
//
//                    Thread.sleep(2000);
//
//                    // Gửi kết quả cho người chơi
//                    for (Player pl : Client.gI().getPlayers()) {
//                        if (pl != null && Client.gI().getPlayer(pl.name) != null) {
//                            sendResult(pl);
//                            if (ketquaTamhoa) {
//                                sendTamHoaMultiplier(pl, tamHoaMultiplier, tong > 10);
//                            }
//                        }
//                    }
//
//                    Thread.sleep(10000);
//                    // Xử lý kết quả game
//                    processGameResult();
//
//                    // Reset trạng thái
//                    resetGameState();
//
//                    // Đặt thời gian kết thúc mới
//                    TaiXiu.gI().lastTimeEnd = System.currentTimeMillis() + 60000;//chinh time
//
//                    // Thông báo reset cho người chơi
//                    for (Player pl : Client.gI().getPlayers()) {
//                        if (pl != null && Client.gI().getPlayer(pl.name) != null) {
//                            sendResetInfo(pl);
//                            sendTimeInfo(pl);
//                        }
//                    }
//                }
//
//            } catch (Exception e) {
//                // Ghi log lỗi nhưng không dừng luồng
//                Logger.error("Lỗi trong luồng Tài Xỉu: " + e.getMessage());
//                e.printStackTrace();
//            }
//        }
//    }
//// 7. Phương thức tạo kết quả theo cửa đã chỉ định (Tài hoặc Xỉu)
//
//    private void generateForcedResult() {
//        boolean wantTai = "TAI".equals(forcedResult);
//
//        // Tạo kết quả tài hoặc xỉu theo yêu cầu
//        if (wantTai) {
//            // Tạo kết quả Tài (tổng > 10)
//            do {
//                x = Util.nextInt(1, 6);
//                y = Util.nextInt(1, 6);
//                z = Util.nextInt(1, 6);
//            } while ((x + y + z) <= 10 || (x == y && y == z)); // Không phải Xỉu và không phải Tam Hoa
//        } else {
//            // Tạo kết quả Xỉu (tổng từ 4-10)
//            do {
//                x = Util.nextInt(1, 6);
//                y = Util.nextInt(1, 6);
//                z = Util.nextInt(1, 6);
//            } while ((x + y + z) > 10 || (x == y && y == z)); // Không phải Tài và không phải Tam Hoa
//        }
//
//        // Reset lại giá trị forcedResult sau khi đã sử dụng
//        forcedResult = null;
//        resultSetByAdmin = false;
//    }
//
//// 8. Phương thức áp dụng tỉ lệ thắng cho bên ít tiền hơn
//    private void applyWinRateForLessMoneyBet() {
//        // Xác định bên nào ít tiền hơn
//        boolean taiHasLessBet = goldTaiReal < goldXiuReal;
//
//        // Kiểm tra xem có áp dụng tỉ lệ thắng hay không
//        if (Util.nextInt(1, 100) <= winRateForLessMoneyBet && winRateForLessMoneyBet != 50) {
//            // Áp dụng tỉ lệ thắng: bên ít tiền sẽ thắng
//            if (taiHasLessBet) {
//                // Tạo kết quả Tài (tổng > 10)
//                do {
//                    x = Util.nextInt(1, 6);
//                    y = Util.nextInt(1, 6);
//                    z = Util.nextInt(1, 6);
//                } while ((x + y + z) <= 10 || (x == y && y == z)); // Không phải Xỉu và không phải Tam Hoa
//            } else {
//                // Tạo kết quả Xỉu (tổng từ 4-10)
//                do {
//                    x = Util.nextInt(1, 6);
//                    y = Util.nextInt(1, 6);
//                    z = Util.nextInt(1, 6);
//                } while ((x + y + z) > 10 || (x == y && y == z)); // Không phải Tài và không phải Tam Hoa
//            }
//        } else {
//            // Không áp dụng tỉ lệ thắng: kết quả ngẫu nhiên
//            x = Util.nextInt(1, 6);
//            y = Util.nextInt(1, 6);
//            z = Util.nextInt(1, 6);
//        }
//    }
//// 9. Sửa đổi phương thức resetGameState để reset kết quả bị ép buộc
//
//    private void resetGameState() {
//        for (Player pl : TaiXiu.gI().PlayersTai) {
//            if (pl != null) {
//                pl.goldTai = 0;
//            }
//        }
//        for (Player pl : TaiXiu.gI().PlayersXiu) {
//            if (pl != null) {
//                pl.goldXiu = 0;
//            }
//        }
//        resultSetByAdmin = false; // Reset flag sau khi xử lý xong ván
//        forcedResult = null;      // Reset kết quả bị ép buộc
//        ketquaXiu = false;
//        ketquaTai = false;
//        ketquaTamhoa = false;
//        TaiXiu.gI().goldTai = 0;
//        TaiXiu.gI().goldXiu = 0;
//        TaiXiu.gI().goldTaiReal = 0;
//        TaiXiu.gI().goldXiuReal = 0;
//        TaiXiu.gI().PlayersTai.clear();
//        TaiXiu.gI().PlayersXiu.clear();
//        tamHoaMultiplier = 0; // Reset số X
//    }
//
//    // Thêm phương thức gửi thông tin số X khi Tam Hoa
//    private void sendTamHoaMultiplier(Player pl, int multiplier, boolean isTai) {
//        Message msg = null;
//        try {
//            msg = new Message(-109); // Message cập nhật thông tin
//            msg.writer().writeShort(1);
//            msg.writer().writeByte(7); // Type 7: Thông tin Tam Hoa multiplier
//            msg.writer().writeInt(multiplier); // Gửi số X
//            msg.writer().writeBoolean(isTai); // Gửi thông tin là Tam Hoa Tài hay Xỉu
//            Service.gI().sendMessage(pl, msg);
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            if (msg != null) {
//                msg.cleanup();
//            }
//        }
//    }
//// Thêm hàm này vào TaiXiu.java
//
//    public void sendResetInfo(Player pl) {
//        Message msg = null;
//        try {
//            msg = new Message(-109); // Message cập nhật thông tin
//            msg.writer().writeShort(1);
//            msg.writer().writeByte(3); // Type 3: Reset tiền các cửa
//
//            // Không cần gửi thêm thông tin gì vì client sẽ hiểu đây là lệnh reset
//            // Gửi message cho client
//            Service.gI().sendMessage(pl, msg);
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            if (msg != null) {
//                msg.cleanup();
//            }
//        }
//    }
//
//    public void baotri() {
//        ketquaXiu = false;
//        ketquaTai = false;
//        ketquaTamhoa = false;
//        TaiXiu.gI().goldTaiReal = 0;
//        TaiXiu.gI().goldXiuReal = 0;
//        TaiXiu.gI().PlayersTai.clear();
//        TaiXiu.gI().PlayersXiu.clear();
//        TaiXiu.gI().lastTimeEnd = System.currentTimeMillis() + 100000;
//        this.baotri = true;
//    }
//
//    public void tatbaotri() {
//        ketquaXiu = false;
//        ketquaTai = false;
//        ketquaTamhoa = false;
//        TaiXiu.gI().goldTai = 0;
//        TaiXiu.gI().goldXiu = 0;
//        TaiXiu.gI().goldTaiReal = 0;
//        TaiXiu.gI().goldXiuReal = 0;
//        TaiXiu.gI().PlayersTai.clear();
//        TaiXiu.gI().PlayersXiu.clear();
//        TaiXiu.gI().lastTimeEnd = System.currentTimeMillis() + 100000;
//        this.baotri = false;
//    }
//    // Thêm các phương thức này vào class TaiXiu hiện có
//
//// Thêm hàm này vào TaiXiu.java
//    public void sendTimeInfo(Player pl) {
//        try {
//            // Tính thời gian còn lại
//            int timeRemaining = (int) ((this.lastTimeEnd - System.currentTimeMillis()) / 1000);
//            if (timeRemaining < 0) {
//                timeRemaining = 0;
//            }
//
//            // Gửi thông tin thời gian
//            Message msg = new Message(-109); // Message cập nhật thông tin
//            msg.writer().writeShort(1);
//            msg.writer().writeByte(0); // type 0: thông tin thời gian
//            msg.writer().writeInt(timeRemaining);
//            Service.gI().sendMessage(pl, msg);
//            msg.cleanup();
//
//            sendBetUpdateToAll();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void sendCurrentBetInfo(Player pl) {
//        Message msg = null;
//        try {
//            msg = new Message(-109); // Sử dụng command 103 như các message khác
//            msg.writer().writeShort(1);
//            msg.writer().writeByte(5); // type 5: thông tin cược hiện tại       
//            // Gửi tổng tiền cược của mỗi bên
//            msg.writer().writeLong(pl.goldTai);
//            msg.writer().writeLong(pl.goldXiu);
//            msg.writer().writeLong(TaiXiu.gI().goldTai);
//            msg.writer().writeLong(TaiXiu.gI().goldXiu);
//            Service.gI().sendMessage(pl, msg);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            Logger.error("Lỗi gửi thông tin cược hiện tại: " + e.getMessage());
//        } finally {
//            if (msg != null) {
//                msg.cleanup();
//            }
//        }
//    }
//// Thêm hàm này vào TaiXiu.java
//
//    public void sendResult(Player pl) {
//        try {
//            // Gửi kết quả xúc xắc
//            // Gửi thông tin thời gian
//            Message msg = new Message(-109); // Message cập nhật thông tin
//            msg.writer().writeShort(1);
//            msg.writer().writeByte(1); // type 1: kết quả xúc xắc
//            msg.writer().writeByte(x); // giá trị xúc xắc 1
//            msg.writer().writeByte(y); // giá trị xúc xắc 2
//            msg.writer().writeByte(z); // giá trị xúc xắc 3
//            msg.writer().writeBoolean(ketquaTai); // kết quả Tài
//            msg.writer().writeBoolean(ketquaTamhoa); // kết quả Tam Hoa
//            msg.writer().writeByte(troll); // kết quả Tam Hoa
//            Service.gI().sendMessage(pl, msg);
//            msg.cleanup();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//  
//    public void handleTaiXiuBet(Player player, Message message) {
//        try {
//
//            long currentTime = System.currentTimeMillis();
//            long remainingTime = (TaiXiu.gI().lastTimeEnd - currentTime) / 1000;
//            if (remainingTime < 3) {
//                return;
//            }
//            // Đọc thông tin từ message
//            boolean isTai = message.reader().readBoolean(); // Loại cược (true = Tài, false = Xỉu)
//            long betAmount = message.reader().readLong(); // Số tiền đặt cược
//            int charID = message.reader().readInt(); // ID nhân vật
//
//            // Kiểm tra ID nhân vật có khớp không
//            if (charID != player.id) {
//                Service.gI().sendThongBao(player, "Dữ liệu không hợp lệ!");
//                return;
//            }
//            if (isTai && player.goldXiu > 0 || !isTai && player.goldTai > 0) {
//                Service.gI().sendThongBao(player, "Tạm Thời Khoá Đặt 2 Cửa Đợi Bão!!!");
//                return;
//            }
//
//            if (player.inventory.gold < betAmount) {
//                return;
//            }
//            // Kiểm tra thời giadn giữa các lần đặt cược
//            if (player.lastBetTime > 0 && currentTime - player.lastBetTime < 500) { // 500ms giữa các lần đặt cược
//                Service.gI().sendThongBao(player, "Bạn đặt cược quá nhanh, vui lòng thử lại sau!");
//                return;
//            }
//
//            // Cập nhật thời gian đặt cược gần nhất
//            player.lastBetTime = currentTime;
//            // Xử lý đặt cược
//            player.inventory.gold -= betAmount;
//
//            if (isTai) {
//                // Đặt Tài
//                player.goldTai += betAmount;
//                TaiXiu.gI().goldTai += betAmount;
//                TaiXiu.gI().goldTaiReal += betAmount;
//                TaiXiu.gI().addPlayerTai(player);
//
//            } else {
//                // Đặt Xỉu
//                player.goldXiu += betAmount;
//                TaiXiu.gI().goldXiu += betAmount;
//                TaiXiu.gI().goldXiuReal += betAmount;
//                TaiXiu.gI().addPlayerXiu(player);
//
//            }
//
//            // Cập nhật database và client
//            Service.gI().sendMoney(player);
//            PlayerDAO.updatePlayer(player);
//
//            // Gửi thông tin cập nhật đặt cược về client
//            sendBetResultToClient(player, isTai, betAmount);
//            sendBetUpdateToAll();
//        } catch (Exception e) {
//            e.printStackTrace();
//            Service.gI().sendThongBao(player, "Lỗi khi đặt cược!");
//        }
//    }
//    // Hàm gửi kết quả đặt cược về client
//
//    private void sendBetResultToClient(Player player, boolean isTai, long betAmount) {
//        Message msg = null;
//        try {
//
//            msg = new Message(-109); // Message cập nhật thông tin
//            msg.writer().writeShort(1);
//            msg.writer().writeByte(2); // Type 2: Cập nhật thông tin đặt cược
//
//            // Gửi số xu hiện tại của người chơi
//            msg.writer().writeLong(player.inventory.gold);
//
//            // Gửi thông tin đặt cược
//            msg.writer().writeBoolean(isTai);
//            msg.writer().writeLong(betAmount);
//
//            // Gửi tổng tiền cược của mỗi bên
//            msg.writer().writeLong(TaiXiu.gI().goldTai);
//            msg.writer().writeLong(TaiXiu.gI().goldXiu);
//
//            // Gửi message cho client
//            player.session.sendMessage(msg);
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            if (msg != null) {
//                msg.cleanup();
//            }
//        }
//    }
//    // Sửa phương thức sendHistoryToPlayer trong TaiXiu.java
//// Thêm vào class TaiXiu.java hoặc service xử lý tin nhắn
//
//    public void sendSoiCauHistoryToPlayer(Player player) throws IOException {
//        Message msg = null;
//        Connection conn = null;
//        PreparedStatement ps = null;
//        ResultSet rs = null;
//
//        try {
//            // Gửi thông tin thời gian
//            msg = new Message(-109); // Message cập nhật thông tin
//            msg.writer().writeShort(1);
//            msg.writer().writeByte(8); // Type 8: dữ liệu soi cầu chi tiết
//
//            conn = DBConnecter.getConnectionServer();
//
//            // Lấy lịch sử chi tiết hơn từ database (50 phiên gần nhất)
//            ps = conn.prepareStatement(
//                    "SELECT "
//                    + "game_id, "
//                    + "dice1, "
//                    + "dice2, "
//                    + "dice3, "
//                    + "CASE "
//                    + "  WHEN (dice1 = dice2 AND dice2 = dice3) THEN 'TAM_HOA' "
//                    + "  WHEN dice1 + dice2 + dice3 > 10 THEN 'TAI' "
//                    + "  ELSE 'XIU' "
//                    + "END as result, "
//                    + "(dice1 = dice2 AND dice2 = dice3) as is_tam_hoa "
//                    + "FROM taixiu_games "
//                    + "ORDER BY game_id DESC "
//                    + "LIMIT 50"
//            );
//            rs = ps.executeQuery();
//
//            List<String> histories = new ArrayList<>();
//            while (rs.next()) {
//                int gameId = rs.getInt("game_id");
//                int dice1 = rs.getInt("dice1");
//                int dice2 = rs.getInt("dice2");
//                int dice3 = rs.getInt("dice3");
//                String result = rs.getString("result");
//                boolean isTamHoa = rs.getBoolean("is_tam_hoa");
//
//                String history = isTamHoa
//                        ? String.format("%d:%d:%d:%d:%s:TamHoa", gameId, dice1, dice2, dice3, result)
//                        : String.format("%d:%d:%d:%d:%s", gameId, dice1, dice2, dice3, result);
//
//                histories.add(history);
//            }
//
//            // Ghi số lượng kết quả
//            msg.writer().writeShort(histories.size());
//
//            // Ghi từng kết quả
//            for (String hist : histories) {
//                msg.writer().writeUTF(hist);
//            }
//
//            // Gửi message
//            Service.gI().sendMessage(player, msg);
//
//        } catch (SQLException e) {
//            Logger.error("Lỗi gửi lịch sử soi cầu Tài Xỉu: " + e.getMessage());
//            e.printStackTrace();
//        } finally {
//            // Đóng tài nguyên
//            try {
//                if (rs != null) {
//                    rs.close();
//                }
//                if (ps != null) {
//                    ps.close();
//                }
//                if (conn != null) {
//                    conn.close();
//                }
//                if (msg != null) {
//                    msg.cleanup();
//                }
//            } catch (SQLException e) {
//                Logger.error("Lỗi đóng kết nối: " + e.getMessage());
//            }
//        }
//    }
//// Thêm hàm này vào class TaiXiu.java
//
//    public void sendRankingToPlayer(Player player) throws IOException {
//        Message msg = null;
//        Connection conn = null;
//        PreparedStatement ps = null;
//        ResultSet rs = null;
//
//        try {
//
//            msg = new Message(-109); // Message cập nhật thông tin
//            msg.writer().writeShort(1);
//            msg.writer().writeByte(9); // Type 9: dữ liệu bảng xếp hạng
//
//            conn = DBConnecter.getConnectionServer();
//
//            // Lấy top 10 người chơi thắng nhiều nhất
//            ps = conn.prepareStatement(
//                    "SELECT player_name, total_win_amount FROM taixiu_player_stats "
//                    + "ORDER BY total_win_amount DESC LIMIT 10"
//            );
//            rs = ps.executeQuery();
//
//            // Đếm số lượng người chơi
//            ArrayList<Pair<String, Long>> rankings = new ArrayList<>();
//            while (rs.next()) {
//                String playerName = rs.getString("player_name");
//                long winAmount = rs.getLong("total_win_amount");
//                rankings.add(new Pair<>(playerName, winAmount));
//            }
//
//            // Ghi số lượng người chơi
//            msg.writer().writeByte(rankings.size());
//
//            // Ghi thông tin từng người chơi
//            for (Pair<String, Long> rank : rankings) {
//                msg.writer().writeUTF(rank.getKey());   // Tên người chơi
//                msg.writer().writeLong(rank.getValue()); // Số tiền thắng
//            }
//
//            // Gửi message
//            Service.gI().sendMessage(player, msg);
//
//        } catch (SQLException e) {
//            Logger.error("Lỗi gửi bảng xếp hạng Tài Xỉu: " + e.getMessage());
//            e.printStackTrace();
//        } finally {
//            // Đóng tài nguyên
//            try {
//                if (rs != null) {
//                    rs.close();
//                }
//                if (ps != null) {
//                    ps.close();
//                }
//                if (conn != null) {
//                    conn.close();
//                }
//                if (msg != null) {
//                    msg.cleanup();
//                }
//            } catch (SQLException e) {
//                Logger.error("Lỗi đóng kết nối: " + e.getMessage());
//            }
//        }
//    }
//
//    public void sendHistoryToPlayer(Player player) throws IOException {
//        Message msg = null;
//        Connection conn = null;
//        PreparedStatement ps = null;
//        ResultSet rs = null;
//
//        try {
//
//            msg = new Message(-109); // Message cập nhật thông tin
//            msg.writer().writeShort(1);
//            msg.writer().writeByte(4); // Type 4 gửi lịch sử
//
//            conn = DBConnecter.getConnectionServer();
//
//            // Lấy game_id lớn nhất để tính phiên hiện tại
//            ps = conn.prepareStatement(
//                    "SELECT MAX(game_id) as max_game_id FROM taixiu_games"
//            );
//            rs = ps.executeQuery();
//            int currentPhien = rs.next() ? (rs.getInt("max_game_id") + 1) : 1;
//            rs.close();
//            ps.close();
//
//            // Lấy lịch sử với trường is_tam_hoa để biết chính xác khi nào là Tam Hoa
//            ps = conn.prepareStatement(
//                    "SELECT "
//                    + "game_id, "
//                    + "CASE "
//                    + "  WHEN (dice1 = dice2 AND dice2 = dice3) THEN 'TAM_HOA' "
//                    + "  WHEN dice1 + dice2 + dice3 > 10 THEN 'TAI' "
//                    + "  ELSE 'XIU' "
//                    + "END as result, "
//                    + "(dice1 = dice2 AND dice2 = dice3) as is_tam_hoa "
//                    + "FROM taixiu_games "
//                    + "ORDER BY game_id DESC "
//                    + "LIMIT 10"
//            );
//            rs = ps.executeQuery();
//
//            List<String> histories = new ArrayList<>();
//            while (rs.next()) {
//                // Format: game_id:result[:TamHoa]
//                int gameId = rs.getInt("game_id");
//                String result = rs.getString("result");
//                boolean isTamHoa = rs.getBoolean("is_tam_hoa");
//
//                // Nếu là Tam Hoa, luôn thêm phần thứ 3 vào chuỗi để client xử lý dễ hơn
//                String history = isTamHoa
//                        ? String.format("%d:%s:TamHoa", gameId, result)
//                        : String.format("%d:%s", gameId, result);
//
//                histories.add(history);
//            }
//
//            // Ghi số phiên hiện tại
//            msg.writer().writeInt(currentPhien);
//
//            // Ghi số lượng kết quả
//            msg.writer().writeByte(histories.size());
//
//            // Ghi từng kết quả
//            for (String hist : histories) {
//                msg.writer().writeUTF(hist);
//            }
//
//            // Gửi message
//            Service.gI().sendMessage(player, msg);
//
//        } catch (SQLException e) {
//            Logger.error("Lỗi gửi lịch sử Tài Xỉu: " + e.getMessage());
//            e.printStackTrace();
//        } finally {
//            // Đóng tài nguyên
//            try {
//                if (rs != null) {
//                    rs.close();
//                }
//                if (ps != null) {
//                    ps.close();
//                }
//                if (conn != null) {
//                    conn.close();
//                }
//                if (msg != null) {
//                    msg.cleanup();
//                }
//            } catch (SQLException e) {
//                Logger.error("Lỗi đóng kết nối: " + e.getMessage());
//            }
//        }
//    }
//
//    public boolean processGameResult() {
//        Connection conn = null;
//        PreparedStatement ps = null;
//        ResultSet rs = null;
//
//        try {
//            // Xác định kết quả phiên
//            int totalValue = x + y + z;
//            boolean isTamHoa = (x == y && y == z);
//            boolean isTai = totalValue > 10 && !isTamHoa;
//            boolean isXiu = totalValue <= 10 && !isTamHoa;
//            boolean isTamHoaTai = isTamHoa && totalValue > 10;
//            boolean isTamHoaXiu = isTamHoa && totalValue <= 10;
//
//            // Lưu thông tin phiên vào database
//            conn = DBConnecter.getConnectionServer();
//            String insertGameSql = "INSERT INTO taixiu_games (dice1, dice2, dice3, total_value, result, total_tai_bet, total_xiu_bet, total_players, start_time) "
//                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
//            PreparedStatement psGame = conn.prepareStatement(insertGameSql, PreparedStatement.RETURN_GENERATED_KEYS);
//            psGame.setInt(1, x);
//            psGame.setInt(2, y);
//            psGame.setInt(3, z);
//            psGame.setInt(4, totalValue);
//
//            // Xác định kết quả để lưu vào trường result
//            String result;
//            if (isTamHoa) {
//                result = "TAM_HOA";
//            } else if (isTai) {
//                result = "TAI";
//            } else {
//                result = "XIU";
//            }
//            psGame.setString(5, result);
//
//            psGame.setLong(6, goldTaiReal);
//            psGame.setLong(7, goldXiuReal);
//            psGame.setInt(8, PlayersTai.size() + PlayersXiu.size());
//            psGame.setTimestamp(9, new java.sql.Timestamp(System.currentTimeMillis()));
//
//            psGame.executeUpdate();
//
//            // Lấy ID của bản ghi vừa được chèn
//            int gameId = -1;
//            rs = psGame.getGeneratedKeys();
//            if (rs.next()) {
//                gameId = rs.getInt(1);
//            }
//            rs.close();
//            psGame.close();
//
//            if (gameId == -1) {
//                Logger.error("Không thể tạo ván chơi mới trong database\n");
//                return false;
//            }
//
//            // Tạo HashSet để theo dõi người chơi đã xử lý
//            Set<Integer> processedPlayers = new HashSet<>();
//
//            //  Logger.log("=== BẮT ĐẦU XỬ LÝ KẾT QUẢ GAME " + gameId + " ===");
//            //  Logger.log("Kết quả: " + result + " (Xúc xắc: " + x + "-" + y + "-" + z + ")");
//            // XỬ LÝ NGƯỜI THẮNG TÀI
//            if (isTai || isTamHoaTai) {
//                //  Logger.log("Xử lý người thắng TÀI: " + PlayersTai.size() + " người chơi");
//
//                for (Player player : PlayersTai) {
//                    if (player == null) {
//                        continue;
//                    }
//
//                    int playerId = (int) player.id;
//                    if (processedPlayers.contains(playerId)) {
//                        continue;
//                    }
//
//                    //   Logger.log("Xử lý player thắng TÀI: " + player.name + " (ID: " + playerId + ")");
//                    // Tính tiền thắng
//                    long winAmount = isTamHoaTai
//                            ? player.goldTai * tamHoaMultiplier
//                            : (long) (player.goldTai * 1.9);
//
//                    //   Logger.log("Player " + player.name + " cược TÀI: " + player.goldTai + ", thắng: " + winAmount);
//                    // Kiểm tra người chơi có online không
//                    if (Client.gI().getPlayer(player.id) == null) {
//                        // Người chơi offline, lưu phần thưởng
//                        saveOfflineReward(playerId, winAmount, gameId);
//                    } else {
//                        // Người chơi online, xử lý như bình thường
//                        player.inventory.gold += winAmount;
//                        Service.gI().sendMoney(player);
//                        sendWinAmount(player, winAmount, true);
//
////                    if (isTamHoaTai) {
////                        Service.gI().sendThongBao(player, "Tam Hoa Tài! Bạn thắng " + Util.format(winAmount) + " xu với x" + tamHoaMultiplier);
////                    }
//                    }
//
//                    // Tính lợi nhuận ròng
//                    long totalBet = player.goldTai + (PlayersXiu.contains(player) ? player.goldXiu : 0);
//                    long netProfit = winAmount - totalBet;
//
//                    //    Logger.log("Net profit cho " + player.name + ": " + netProfit);
//                    // Ghi log cược thắng
//                    insertTaiXiuBet(gameId, player, player.goldTai, "TAI", winAmount, true);
//
//                    // Ghi log cược Xỉu nếu có (thua)
//                    if (PlayersXiu.contains(player)) {
//                        insertTaiXiuBet(gameId, player, player.goldXiu, "XIU", 0, false);
//                    }
//
//                    // Cập nhật thống kê
//                    updatePlayerProfit(player, netProfit);
//
//                    processedPlayers.add(playerId);
//                }
//            }
//
//            // XỬ LÝ NGƯỜI THẮNG XỈU - PHẦN QUAN TRỌNG BỊ THIẾU
//            if (isXiu || isTamHoaXiu) {
//                //   Logger.log("Xử lý người thắng XỈU: " + PlayersXiu.size() + " người chơi");
//
//                for (Player player : PlayersXiu) {
//                    if (player == null) {
//                        continue;
//                    }
//
//                    int playerId = (int) player.id;
//                    if (processedPlayers.contains(playerId)) {
//                        continue;
//                    }
//
//                    //     Logger.log("Xử lý player thắng XỈU: " + player.name + " (ID: " + playerId + ")");
//                    // Tính tiền thắng
//                    long winAmount = isTamHoaXiu
//                            ? player.goldXiu * tamHoaMultiplier
//                            : (long) (player.goldXiu * 1.9);
//
//                    //  Logger.log("Player " + player.name + " cược XỈU: " + player.goldXiu + ", thắng: " + winAmount);
//                    // Kiểm tra người chơi có online không
//                    if (Client.gI().getPlayer(player.id) == null) {
//                        // Người chơi offline, lưu phần thưởng
//                        saveOfflineReward(playerId, winAmount, gameId);
//                    } else {
//                        // Người chơi online, xử lý như bình thường
//                        player.inventory.gold += winAmount;
//                        Service.gI().sendMoney(player);
//                        sendWinAmount(player, winAmount, true);
//
//                        if (isTamHoaXiu) {
//                            Service.gI().sendThongBao(player, "Tam Hoa Xỉu! Bạn thắng " + Util.format(winAmount) + " xu với x" + tamHoaMultiplier);
//                        }
//                    }
//
//                    // PHẦN QUAN TRỌNG BỊ THIẾU TRONG CODE CŨ:
//                    // Tính lợi nhuận ròng
//                    long totalBet = player.goldXiu + (PlayersTai.contains(player) ? player.goldTai : 0);
//                    long netProfit = winAmount - totalBet;
//
//                    //  Logger.log("Net profit cho " + player.name + ": " + netProfit);
//                    // Ghi log cược thắng
//                    insertTaiXiuBet(gameId, player, player.goldXiu, "XIU", winAmount, true);
//
//                    // Ghi log cược Tài nếu có (thua)
//                    if (PlayersTai.contains(player)) {
//                        insertTaiXiuBet(gameId, player, player.goldTai, "TAI", 0, false);
//                    }
//
//                    // Cập nhật thống kê - PHẦN NÀY BỊ THIẾU
//                    updatePlayerProfit(player, netProfit);
//
//                    processedPlayers.add(playerId);
//                }
//            }
//
//            // XỬ LÝ NGƯỜI THUA TÀI (chưa được xử lý)
//            //   Logger.log("Xử lý người thua TÀI");
//            for (Player player : PlayersTai) {
//                if (player == null || Client.gI().getPlayer(player.id) == null || player.inventory == null) {
//                    continue;
//                }
//
//                int playerId = (int) player.id;
//                if (processedPlayers.contains(playerId)) {
//                    continue; // Đã xử lý rồi
//                }
//
//                //  Logger.log("Xử lý player thua TÀI: " + player.name + " (ID: " + playerId + ")");
//                // Ghi log cược thua
//                insertTaiXiuBet(gameId, player, player.goldTai, "TAI", 0, false);
//
//                // Tính lợi nhuận âm (thua)
//                long netProfit = -player.goldTai;
//
//                //    Logger.log("Net profit (thua) cho " + player.name + ": " + netProfit);
//                // Cập nhật thống kê
//                updatePlayerProfit(player, netProfit);
//
//                processedPlayers.add(playerId);
//            }
//
//            // XỬ LÝ NGƯỜI THUA XỈU (chưa được xử lý)
//            //   Logger.log("Xử lý người thua XỈU");
//            for (Player player : PlayersXiu) {
//                if (player == null || Client.gI().getPlayer(player.id) == null || player.inventory == null) {
//                    continue;
//                }
//
//                int playerId = (int) player.id;
//                if (processedPlayers.contains(playerId)) {
//                    continue; // Đã xử lý rồi
//                }
//
//                //   Logger.log("Xử lý player thua XỈU: " + player.name + " (ID: " + playerId + ")");
//                // Ghi log cược thua
//                insertTaiXiuBet(gameId, player, player.goldXiu, "XIU", 0, false);
//
//                // Tính lợi nhuận âm (thua)
//                long netProfit = -player.goldXiu;
//
//                Logger.log("Net profit (thua) cho " + player.name + ": " + netProfit);
//
//                // Cập nhật thống kê
//                updatePlayerProfit(player, netProfit);
//
//                processedPlayers.add(playerId);
//            }
//
//            //       Logger.log("=== KẾT THÚC XỬ LÝ KẾT QUẢ GAME " + gameId + " ===");
//            //   Logger.log("Tổng số người chơi đã xử lý: " + processedPlayers.size());
//            return true;
//        } catch (SQLException e) {
//            Logger.error("Lỗi khi xử lý kết quả ván chơi: " + e.getMessage());
//            e.printStackTrace();
//            return false;
//        } finally {
//            // Đóng tài nguyên
//            try {
//                if (rs != null) {
//                    rs.close();
//                }
//                if (ps != null) {
//                    ps.close();
//                }
//                if (conn != null) {
//                    conn.close();
//                }
//            } catch (SQLException e) {
//                Logger.error("Lỗi khi đóng kết nối: " + e.getMessage());
//            }
//        }
//    }
//
//    public void checkPendingRewards(Player player) {
//        try (Connection conn = DBConnecter.getConnectionServer()) {
//            // Bắt đầu transaction
//            conn.setAutoCommit(false);
//
//            try {
//                // Lấy danh sách phần thưởng chưa nhận
//                List<PendingReward> pendingRewards = new ArrayList<>();
//                try (PreparedStatement ps = conn.prepareStatement(
//                        "SELECT id, win_amount, game_id FROM taixiu_pending_rewards "
//                        + "WHERE player_id = ? AND claimed = 0")) {
//                    ps.setInt(1, (int) player.id);
//                    try (ResultSet rs = ps.executeQuery()) {
//                        while (rs.next()) {
//                            PendingReward reward = new PendingReward();
//                            reward.id = rs.getInt("id");
//                            reward.winAmount = rs.getLong("win_amount");
//                            reward.gameId = rs.getInt("game_id");
//                            pendingRewards.add(reward);
//                        }
//                    }
//                }
//
//                if (pendingRewards.isEmpty()) {
//                    return; // Không có phần thưởng chưa nhận
//                }
//
//                // Tổng hợp phần thưởng
//                long totalWinAmount = 0;
//                List<Integer> claimedRewardIds = new ArrayList<>();
//
//                for (PendingReward reward : pendingRewards) {
//                    totalWinAmount += reward.winAmount;
//                    claimedRewardIds.add(reward.id);
//                }
//
//                // Cộng tiền trực tiếp vào tài khoản người chơi
//                player.inventory.gold += totalWinAmount;
//                Service.gI().sendMoney(player);
//
//                // Đánh dấu đã nhận tất cả phần thưởng
//                for (int rewardId : claimedRewardIds) {
//                    try (PreparedStatement updatePs = conn.prepareStatement(
//                            "UPDATE taixiu_pending_rewards SET claimed = 1 WHERE id = ?")) {
//                        updatePs.setInt(1, rewardId);
//                        updatePs.executeUpdate();
//                    }
//                }
//
//                // Thông báo cho người chơi
//                Service.gI().sendThongBao(player, "Bạn đã nhận được " + Util.format(totalWinAmount) + " xu từ phần thưởng Tài Xỉu trước đó!");
//
//                // Ghi log
//                Logger.log("Người chơi " + player.name + " đã nhận " + totalWinAmount + " xu từ phần thưởng Tài Xỉu offline");
//
//                // Commit transaction
//                conn.commit();
//            } catch (SQLException e) {
//                // Rollback nếu có lỗi
//                conn.rollback();
//                Logger.error("Lỗi kiểm tra phần thưởng Tài Xỉu chưa nhận: " + e.getMessage());
//                e.printStackTrace();
//            }
//        } catch (SQLException e) {
//            Logger.error("Lỗi kết nối database khi kiểm tra phần thưởng Tài Xỉu: " + e.getMessage());
//            e.printStackTrace();
//        }
//    }
//
//// Lớp để lưu thông tin phần thưởng chưa nhận
//    private static class PendingReward {
//
//        int id;
//        long winAmount;
//        int gameId;
//    }
//
//    private void updatePlayerProfit(Player player, long netProfit) {
//        Connection conn = null;
//        PreparedStatement ps = null;
//        ResultSet rs = null;
//
//        try {
//            conn = DBConnecter.getConnectionServer();
//
//            // Log thông tin player trước khi xử lý
//            ///   Logger.log("=== UPDATING PLAYER PROFIT ===");
//            // Logger.log("Player ID: " + player.id + ", Name: " + player.name);
//            // Logger.log("Net Profit: " + netProfit);
//            // Kiểm tra xem đã có bản ghi chưa
//            ps = conn.prepareStatement("SELECT * FROM taixiu_player_stats WHERE player_id = ?");
//            ps.setInt(1, (int) player.id);
//            rs = ps.executeQuery();
//
//            boolean hasRecord = rs.next();
//
//            if (hasRecord) {
//                long currentWinAmount = rs.getLong("total_win_amount");
//                long currentLossAmount = rs.getLong("total_loss_amount");
//                long currentProfit = rs.getLong("profit");
//
//                //     Logger.log("Existing record - Win: " + currentWinAmount
//                //      + ", Loss: " + currentLossAmount + ", Profit: " + currentProfit);
//            } else {
//                Logger.log("No existing record found, creating new one");
//            }
//
//            rs.close();
//            ps.close();
//
//            // Xác định số tiền đặt tài/xỉu
//            long taiBetAmount = PlayersTai.contains(player) ? player.goldTai : 0;
//            long xiuBetAmount = PlayersXiu.contains(player) ? player.goldXiu : 0;
//            long totalBetAmount = taiBetAmount + xiuBetAmount;
//
//            //  Logger.log("Bet amounts - Tai: " + taiBetAmount + ", Xiu: " + xiuBetAmount + ", Total: " + totalBetAmount);
//            // Tính số tiền thắng/thua ĐÚNG
//            long winAmount = 0;
//            long lossAmount = 0;
//
//            if (netProfit > 0) {
//                // Thắng: winAmount = số tiền nhận được (bao gồm cả tiền cược ban đầu)
//                winAmount = totalBetAmount + netProfit;
//                lossAmount = 0;
//            } else {
//                // Thua: lossAmount = số tiền đã cược
//                winAmount = 0;
//                lossAmount = totalBetAmount;
//            }
//
//            //     Logger.log("Calculated - Win Amount: " + winAmount + ", Loss Amount: " + lossAmount);
//            if (hasRecord) {
//                // Cập nhật bản ghi hiện có
//                String sql = "UPDATE taixiu_player_stats SET "
//                        + "total_tai_bets = total_tai_bets + ?, "
//                        + "total_xiu_bets = total_xiu_bets + ?, "
//                        + "total_win_amount = total_win_amount + ?, "
//                        + "total_loss_amount = total_loss_amount + ?, "
//                        + "profit = profit + ?, "
//                        + "last_play_time = ? "
//                        + "WHERE player_id = ?";
//
//                ps = conn.prepareStatement(sql);
//                ps.setLong(1, taiBetAmount);
//                ps.setLong(2, xiuBetAmount);
//                ps.setLong(3, winAmount);
//                ps.setLong(4, lossAmount);
//                ps.setLong(5, netProfit);
//                ps.setTimestamp(6, new java.sql.Timestamp(System.currentTimeMillis()));
//                ps.setInt(7, (int) player.id);
//
//                int rowsUpdated = ps.executeUpdate();
//                //     Logger.log("Updated " + rowsUpdated + " rows for player " + player.name);
//
//            } else {
//                // Tạo bản ghi mới
//                String sql = "INSERT INTO taixiu_player_stats (player_id, player_name, "
//                        + "total_tai_bets, total_xiu_bets, "
//                        + "total_win_amount, total_loss_amount, profit, "
//                        + "last_play_time) "
//                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
//
//                ps = conn.prepareStatement(sql);
//                ps.setInt(1, (int) player.id);
//                ps.setString(2, player.name);
//                ps.setLong(3, taiBetAmount);
//                ps.setLong(4, xiuBetAmount);
//                ps.setLong(5, winAmount);
//                ps.setLong(6, lossAmount);
//                ps.setLong(7, netProfit);
//                ps.setTimestamp(8, new java.sql.Timestamp(System.currentTimeMillis()));
//
//                int rowsInserted = ps.executeUpdate();
//                // Logger.log("Inserted " + rowsInserted + " new record for player " + player.name);
//            }
//
//            // Verify kết quả sau khi update
//            ps.close();
//            ps = conn.prepareStatement("SELECT total_win_amount, total_loss_amount, profit FROM taixiu_player_stats WHERE player_id = ?");
//            ps.setInt(1, (int) player.id);
//            rs = ps.executeQuery();
//
//            if (rs.next()) {
////                Logger.log("AFTER UPDATE - Win: " + rs.getLong("total_win_amount")
////                        + ", Loss: " + rs.getLong("total_loss_amount")
////                        + ", Profit: " + rs.getLong("profit"));
//            }
//
//            // Logger.log("=== END UPDATING PLAYER PROFIT ===");
//        } catch (SQLException e) {
//            Logger.error("Lỗi khi cập nhật lợi nhuận người chơi " + player.name + ": " + e.getMessage());
//            e.printStackTrace();
//        } finally {
//            try {
//                if (rs != null) {
//                    rs.close();
//                }
//                if (ps != null) {
//                    ps.close();
//                }
//                if (conn != null) {
//                    conn.close();
//                }
//            } catch (SQLException e) {
//                Logger.error("Lỗi khi đóng kết nối: " + e.getMessage());
//            }
//        }
//    }
//
//    public void insertTaiXiuBet(int gameId, Player player, long betAmount, String betType, long winAmount, boolean isWin) {
//        Connection conn = null;
//        PreparedStatement ps = null;
//
//        try {
//            conn = DBConnecter.getConnectionServer();
//
//            // Sử dụng câu lệnh INSERT trực tiếp thay vì stored procedure
//            String sql = "INSERT INTO taixiu_bets (game_id, player_id, player_name, bet_amount, bet_type, win_amount, is_win, bet_time) "
//                    + "VALUES (?, ?, ?, ?, ?, ?, ?, NOW())";
//
//            ps = conn.prepareStatement(sql);
//            ps.setInt(1, gameId);
//            ps.setInt(2, (int) player.id);
//            ps.setString(3, player.name);
//            ps.setLong(4, betAmount);
//            ps.setString(5, betType);
//            ps.setLong(6, winAmount);
//            ps.setBoolean(7, isWin);
//
//            // Thực thi
//            ps.executeUpdate();
//
//        } catch (SQLException e) {
//            Logger.error("Lỗi khi thêm lượt cược của người chơi " + player.name + ": " + e.getMessage());
//            e.printStackTrace();
//        } finally {
//            // Đóng tài nguyên
//            try {
//                if (ps != null) {
//                    ps.close();
//                }
//                if (conn != null) {
//                    conn.close();
//                }
//            } catch (SQLException e) {
//                Logger.error("Lỗi khi đóng kết nối: " + e.getMessage());
//            }
//        }
//    }
//
//    private void balanceTaiXiuGold() {
//        long currentTime = System.currentTimeMillis();
//        long remainingTime = (TaiXiu.gI().lastTimeEnd - currentTime) / 1000;
//        if (remainingTime < 3 || remainingTime > 27) {
//            return;
//        }
//
//        // Chỉ cho phép tăng mỗi 1-2 giây 1 lần
//        int delay = Util.nextInt(500, 2000);
//        if (currentTime - lastBalanceTime < delay) {
//            return;
//        }
//        lastBalanceTime = currentTime;
//
//        final long MAX_PER_ADD = 20_000_000_000L; // 50 tỉ
//        long totalReal = goldTaiReal + goldXiuReal;
//
//        // Xác định số tiền tăng mỗi lần
//        long minBet, maxBet;
//        if (totalReal >= 20_000000000L) {
//            minBet = Math.max(1_00_000_000L, 20_000000000L / 5); // ít nhất 1 tỉ hoặc 1/5 tổng gold real
//            maxBet = Math.min(MAX_PER_ADD, 20_000000000L * 2);    // tối đa 2 lần tổng gold real, không quá 50 tỉ
//        } else if (totalReal >= 10_000000000L) {
//            minBet = Math.max(1_00_000_000L, 10_000000000L / 5); // ít nhất 1 tỉ hoặc 1/5 tổng gold real
//            maxBet = Math.min(MAX_PER_ADD, 10_000000000L * 2);    // tối đa 2 lần tổng gold real, không quá 50 tỉ
//        } else if (totalReal >= 5_000000000L) {
//            minBet = Math.max(1_00_000_000L, 5_000000000L / 5); // ít nhất 1 tỉ hoặc 1/5 tổng gold real
//            maxBet = Math.min(MAX_PER_ADD, 5_000000000L * 2);    // tối đa 2 lần tổng gold real, không quá 50 tỉ
//        } else if (totalReal >= 1_000000000L) {
//            minBet = Math.max(100_000_000L, 1_000000000L / 5); // ít nhất 1 tỉ hoặc 1/5 tổng gold real
//            maxBet = Math.min(MAX_PER_ADD, 1_000000000L * 2);    // tối đa 2 lần tổng gold real, không quá 50 tỉ
//        } else {
//            minBet = 1_000_000L; // 1 triệu
//            maxBet = 100_000_000L; // 50 tỉ
//        }
//
//        // Random số tiền tăng
//        long betAmount = randomNiceGold(minBet, maxBet);
//
//        // Logic cân 2 bên: 
//        // - Nếu lệch nhiều, ưu tiên tăng bên yếu.
//        // - Lâu lâu (5-10 lần mới 1 lần) cho 1 bên cao hẳn để tạo sự tự nhiên.
//        long diff = Math.abs(goldTai - goldXiu);
//
//        boolean forceImbalance = false;
//        if (forceImbalanceCount > 0) {
//            forceImbalance = true;
//            forceImbalanceCount--;
//        } else if (Util.isTrue(10, 100)) { // 10% số lần tăng sẽ cho lệch hẳn
//            forceImbalance = true;
//            forceImbalanceCount = Util.nextInt(3, 8); // Lệch liên tục 3-8 lần
//        }
//
//        boolean betOnTai;
//        if (forceImbalance) {
//            // Lệch hẳn về 1 bên: random bên nào, tăng vào bên đó
//            betOnTai = Util.isTrue(50, 100);
//        } else if (diff > maxBet) {
//            // Nếu lệch nhiều thì tăng vào bên yếu
//            betOnTai = goldTai < goldXiu;
//        } else {
//            // Bình thường random, nhưng xác suất tăng vào bên yếu cao hơn
//            if (goldTai < goldXiu) {
//                betOnTai = Util.isTrue(65, 100);
//            } else if (goldTai > goldXiu) {
//                betOnTai = Util.isTrue(35, 100);
//            } else {
//                betOnTai = Util.isTrue(50, 100);
//            }
//        }
//
//        if (betOnTai) {
//            goldTai += betAmount;
//        } else {
//            goldXiu += betAmount;
//        }
//    }
//
//// Hàm random số vàng lẻ/số đẹp trong khoảng a-b
//    private long randomNiceGold(long min, long max) {
//        if (max < min) {
//            return min;
//        }
//        long amount = Util.nextInt((int) (min / 1_000_000L), (int) (max / 1_000_000L)) * 1_000_000L;
//        // 30% ra số đẹp/phong thủy
//        int style = Util.nextInt(1, 100);
//        if (style <= 10) {
//            amount = 6_888_000_000L;
//        } else if (style <= 20) {
//            amount = 8_888_000_000L;
//        } else if (style <= 30) {
//            amount = 9_999_000_000L;
//        }
//        if (amount < min) {
//            amount = min;
//        }
//        if (amount > max) {
//            amount = max;
//        }
//        return amount;
//    }
//
//// Phương thức mới: Tạo mức tiền cơ sở cho các ván chênh lệch cực lớn
//    private long generateBaseAmount() {
//        // Chọn một mức tiền cơ sở hợp lý (thường từ 10M đến 500M)
//        int rand = Util.nextInt(1, 100);
//
//        if (rand <= 50) {  // 50% cơ hội: 10M-50M
//            return Util.nextInt(10, 50) * 1000000L;
//        } else if (rand <= 80) {  // 30% cơ hội: 50M-200M
//            return Util.nextInt(50, 200) * 1000000L;
//        } else if (rand <= 95) {  // 15% cơ hội: 200M-500M
//            return Util.nextInt(200, 500) * 1000000L;
//        } else {  // 5% cơ hội: 500M-1B
//            return Util.nextInt(500, 1000) * 1000000L;
//        }
//    }
//
//    private void saveOfflineReward(int playerId, long winAmount, int gameId) {
//        try (Connection conn = DBConnecter.getConnectionServer(); PreparedStatement ps = conn.prepareStatement(
//                "INSERT INTO taixiu_pending_rewards (player_id, win_amount, game_id, created_at) VALUES (?, ?, ?, ?)")) {
//            ps.setInt(1, playerId);
//            ps.setLong(2, winAmount);
//            ps.setInt(3, gameId);
//            ps.setTimestamp(4, new java.sql.Timestamp(System.currentTimeMillis()));
//            ps.executeUpdate();
//            Logger.log("Đã lưu phần thưởng offline cho người chơi ID " + playerId + ": " + winAmount + " xu");
//        } catch (SQLException e) {
//            Logger.error("Lỗi lưu phần thưởng offline: " + e.getMessage());
//        }
//    }
//
//// Phương thức mới: Thêm biến động nhỏ để trông tự nhiên hơn
//    private void addNaturalVariation() {
//        // Thêm biến động ngẫu nhiên ±5% để trông tự nhiên hơn
//        double taiVariation = 1.0 + (Util.nextInt(-5, 5) / 100.0);
//        double xiuVariation = 1.0 + (Util.nextInt(-5, 5) / 100.0);
//
//        goldTai = roundToNiceNumber((long) (goldTai * taiVariation));
//        goldXiu = roundToNiceNumber((long) (goldXiu * xiuVariation));
//    }
//// Tạo số tiền cược tròn và tự nhiên (1K, 10K, 100K, 1M, 10M, v.v.)
//    // Tạo số tiền cược tròn và tự nhiên (1K, 10K, 100K, 1M, 10M, v.v.)
//
//    private long generateNiceAmount() {
//        // Thêm xác suất để tạo số tiền cực lớn cho các ván chênh lệch
//        boolean isExtremeAmount = Util.isTrue(10, 100); // 10% xác suất ra số tiền cực lớn
//
//        if (isExtremeAmount) {
//            // Tạo các số tiền cực lớn cho các ván chênh lệch
//            int rand = Util.nextInt(1, 100);
//
//            if (rand <= 40) {  // 40% xác suất: 500M-1B
//                int base = Util.nextInt(5, 10);
//                return base * 100000000;  // 500M-1B
//            } else if (rand <= 70) {  // 30% xác suất: 1B-3B
//                int base = Util.nextInt(10, 30);
//                return base * 100000000;  // 1B-3B
//            } else if (rand <= 90) {  // 20% xác suất: 3B-5B
//                int base = Util.nextInt(30, 50);
//                return base * 100000000;  // 3B-5B
//            } else {  // 10% xác suất: 5B-10B
//                int base = Util.nextInt(50, 100);
//                return base * 100000000;  // 5B-10B
//            }
//        }
//
//        // Logic cũ cho các số tiền thông thường
//        int rand = Util.nextInt(1, 1000);
//        long amount;
//
//        if (rand <= 250) {  // 25% xác suất: 1K-20K
//            int base = Util.nextInt(1, 20);
//            amount = base * 1000;
//        } else if (rand <= 500) {  // 25% xác suất: 50K-900K
//            int base = Util.nextInt(5, 90) * 10;
//            amount = base * 1000;
//        } else if (rand <= 750) {  // 25% xác suất: 1M-9M
//            int base = Util.nextInt(1, 9);
//            amount = base * 1000000;
//        } else if (rand <= 900) {  // 15% xác suất: 10M-90M
//            int base = Util.nextInt(1, 9) * 10;
//            amount = base * 1000000;
//        } else if (rand <= 970) {  // 7% xác suất: 100M-500M
//            int base = Util.nextInt(10, 50) * 10;
//            amount = base * 1000000;
//        } else if (rand <= 990) {  // 2% xác suất: 1B-2B
//            int base = Util.nextInt(10, 20);
//            amount = base * 100000000;
//        } else {  // 1% xác suất: số đặc biệt
//            // Các số tròn đẹp: 888K, 8.888M, 88.88M, 888.8M
//            int choice = Util.nextInt(1, 5);
//            switch (choice) {
//                case 1:
//                    amount = 888000;
//                    break;           // 888K
//                case 2:
//                    amount = 8888000;
//                    break;          // 8.888M
//                case 3:
//                    amount = 88880000;
//                    break;         // 88.88M
//                case 4:
//                    amount = 888800000;
//                    break;        // 888.8M
//                default:
//                    amount = 1000000000;
//                    break;      // 1B
//            }
//        }
//
//        return amount;
//    }
//
//// Làm tròn số tiền thành số "đẹp" và dễ đọc
//    private long roundToNiceNumber(long amount) {
//        if (amount <= 0) {
//            return 0;
//        }
//
//        // Làm tròn lên đến 1000 gần nhất
//        long rounded = ((amount + 999) / 1000) * 1000;
//
//        // Xử lý các trường hợp đặc biệt để có số đẹp
//        if (rounded >= 10000000) { // >= 10M
//            // Làm tròn đến 1M gần nhất
//            rounded = ((rounded + 999999) / 1000000) * 1000000;
//        } else if (rounded >= 1000000) { // >= 1M
//            // Làm tròn đến 100K gần nhất
//            rounded = ((rounded + 99999) / 100000) * 100000;
//        } else if (rounded >= 100000) { // >= 100K
//            // Làm tròn đến 10K gần nhất
//            rounded = ((rounded + 9999) / 10000) * 10000;
//        }
//
//        return rounded;
//    }
//}
