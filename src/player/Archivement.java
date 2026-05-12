package player;

import item.Item;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;

import java.io.IOException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jdbc.DBConnecter;
import jdbc.daos.NDVSqlFetcher;

import network.Message;
import org.json.simple.JSONObject;
import services.ItemService;
import services.Service;
import utils.Logger;

public class Archivement {

    public String info1;
    public String info2;
    public short money;
    public boolean isFinish;
    public boolean isRecieve;
    public int[] rewardIconIds = new int[0];
    public int[] rewardQuantities = new int[0];

    private static final class RewardPreview {

        private final int iconId;
        private final int quantity;

        private RewardPreview(int iconId, int quantity) {
            this.iconId = iconId;
            this.quantity = quantity;
        }
    }

    public String getInfo1() {
        return info1;
    }

    public void setInfo1(String info1) {
        this.info1 = info1;
    }

    public String getInfo2() {
        return info2;
    }

    public void setInfo2(String info2) {
        this.info2 = info2;
    }

    public short getMoney() {
        return money;
    }

    public void setMoney(short money) {
        this.money = money;
    }

    public boolean isFinish() {
        return isFinish;
    }

    public void setFinish(boolean finish) {
        isFinish = finish;
    }

    public boolean isRecieve() {
        return isRecieve;
    }

    public void setRecieve(boolean recieve) {
        isRecieve = recieve;
    }
    public static Archivement gI = null;
    public final static int[] GIADOLACHIADOI = {
        20_000, // 20k
        50_000, // 50k
        100_000, // 100k
        200_000, // 200k
        300_000, // 300k
        500_000, // 500k
        700_000, // 700k
        1_000_000, // 1000k
//        1_300_000, // 1300k
        1_500_000, // 1500k
        2_000_000, // 2000k
//        2_500_000, // 2500k
//        3_000_000, // 3000k
//        3_500_000 // 3500k
    };

    public static Archivement gI() {
        if (gI == null) {
            return new Archivement();
        }
        return gI;
    }

    public Archivement() {
    }

    public Archivement(String info1, String info2, short money, boolean isFinish, boolean isRecieve) {
        this.info1 = info1;
        this.info2 = info2;
        this.money = money;
        this.isFinish = isFinish;
        this.isRecieve = isRecieve;
    }

    public void Show(Player pl) {
        Message msg = null;
        try {
            msg = new Message(-76);
            msg.writer().writeByte(0); // action
            msg.writer().writeByte(pl.archivementList.size());
            for (int i = 0; i < pl.archivementList.size(); i++) {

                Archivement archivement = pl.archivementList.get(i);
                if (pl.getSession().version <= 231 || pl.getSession().version > 235) {
                    msg.writer().writeUTF(archivement.getInfo1());
                    msg.writer().writeUTF(archivement.getInfo2());
                    msg.writer().writeShort(archivement.getMoney()); //money
                    msg.writer().writeBoolean(archivement.isFinish);
                    msg.writer().writeBoolean(archivement.isRecieve);
                    writeRewardPreview(msg, archivement);

                } else {
                    msg.writer().writeUTF(archivement.getInfo1());
                    msg.writer().writeUTF(archivement.getInfo2());
                    msg.writer().writeShort(archivement.getMoney()); //money
                    msg.writer().writeUTF("");
                    msg.writer().writeBoolean(archivement.isFinish);
                    msg.writer().writeBoolean(archivement.isRecieve);
                    msg.writer().writeShort(10895);//res icon
                    writeRewardPreview(msg, archivement);
                }

            }
            pl.sendMessage(msg);
            msg.cleanup();
            pl.typeRecvieArchiment = 1;
        } catch (IOException e) {

            e.getStackTrace();
        } finally {
            if (msg != null) {
                msg.cleanup();
                msg = null;
            }
        }
    }

    private void writeRewardPreview(Message msg, Archivement archivement) throws IOException {
        int count = archivement.rewardIconIds == null ? 0 : Math.min(archivement.rewardIconIds.length, 255);
        msg.writer().writeByte(count);
        for (int i = 0; i < count; i++) {
            msg.writer().writeShort(archivement.rewardIconIds[i]);
            int quantity = 1;
            if (archivement.rewardQuantities != null && i < archivement.rewardQuantities.length) {
                quantity = archivement.rewardQuantities[i];
            }
            msg.writer().writeInt(quantity);
        }
    }

    public boolean checktongnap(Player pl, int index) {
        if (index == 0 && pl.getSession().danap >= GIADOLACHIADOI[0]) {
            return true;
        }
        if (index == 1 && pl.getSession().danap >= GIADOLACHIADOI[1]) {
            return true;
        }
        if (index == 2 && pl.getSession().danap >= GIADOLACHIADOI[2]) {
            return true;
        }
        if (index == 3 && pl.getSession().danap >= GIADOLACHIADOI[3]) {
            return true;
        }
        if (index == 4 && pl.getSession().danap >= GIADOLACHIADOI[4]) {
            return true;
        }
        if (index == 5 && pl.getSession().danap >= GIADOLACHIADOI[5]) {
            return true;
        }
        if (index == 6 && pl.getSession().danap >= GIADOLACHIADOI[6]) {
            return true;
        }
        if (index == 7 && pl.getSession().danap >= GIADOLACHIADOI[7]) {
            return true;
        }
        if (index == 8 && pl.getSession().danap >= GIADOLACHIADOI[8]) {
            return true;
        }
        if (index == 9 && pl.getSession().danap >= GIADOLACHIADOI[9]) {
            return true;
        }
//        if (index == 10 && pl.getSession().danap >= GIADOLACHIADOI[10]) {
//            return true;
//        }
//        if (index == 11 && pl.getSession().danap >= GIADOLACHIADOI[11]) {
//            return true;
//        }
//        if (index == 12 && pl.getSession().danap >= GIADOLACHIADOI[12]) {
//            return true;
//        }
//        if (index == 13 && pl.getSession().danap >= GIADOLACHIADOI[13]) {
//            return true;
//        }
        return false;
    }

    public void receiveGem(int index, Player pl) {
        Archivement temp = pl.archivementList.get(index);
        if (temp.isRecieve) {
            Service.gI().sendThongBaoOK(pl, "Nhận rồi đừng nhận nữua");
            return;
        }
        if (temp != null) {
            Message msg = null;
            try {
                msg = new Message(-76);
                msg.writer().writeByte(1); // action
                msg.writer().writeByte(index); // index
                pl.sendMessage(msg);
                msg.cleanup();
            } catch (IOException e) {
                e.printStackTrace();
                Logger.logException(this.getClass(), e);
            } finally {
                if (msg != null) {
                    msg.cleanup();
                    msg = null;
                }
            }

            pl.archivementList.get(index).setRecieve(true);
            try {
                JSONArray dataArray = new JSONArray();

                for (Archivement arr : pl.archivementList) {
                    dataArray.add(arr.isRecieve ? "1" : "0");
                }
                String inventory = dataArray.toJSONString();
                dataArray.clear();
                DBConnecter.executeUpdate("update player set Achievement = ? where id = ?", inventory, pl.id);
                nhanQua(pl, index + 1);
                System.out.println("Player " + pl.name + " Nhận quà thành công");

            } catch (Exception e) {
                e.printStackTrace();
            }
            Service.gI().sendThongBao(pl, "Nhận thành công, vui lòng kiểm tra hòm thư ");
        } else {
            Service.gI().sendThongBao(pl, "Không có phần thưởng");
        }
    }

    private void nhanQua(Player pl, int index) {
        Item item = null;
        JSONArray dataArray;
        JSONObject dataObject;
        try (Connection con2 = DBConnecter.getConnectionServer(); PreparedStatement ps = con2.prepareStatement("SELECT detail FROM moc_nap WHERE id = ?")) {
            ps.setInt(1, index);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    dataArray = (JSONArray) JSONValue.parse(rs.getString("detail"));
                    for (int i = 0; i < dataArray.size(); i++) {
                        dataObject = (JSONObject) JSONValue.parse(String.valueOf(dataArray.get(i)));
                        int tempid = Integer.parseInt(String.valueOf(dataObject.get("temp_id")));
                        int quantity = Integer.parseInt(String.valueOf(dataObject.get("quantity")));
                        item = ItemService.gI().createNewItem((short) tempid);
                        item.quantity = quantity;
                        JSONArray optionsArray = (JSONArray) dataObject.get("options");
                        for (int j = 0; j < optionsArray.size(); j++) {
                            JSONObject optionObject = (JSONObject) optionsArray.get(j);
                            int param = Integer.parseInt(String.valueOf(optionObject.get("param")));
                            int optionId = Integer.parseInt(String.valueOf(optionObject.get("id")));
                            item.itemOptions.add(new Item.ItemOption(optionId, param));
                        }
                        pl.inventory.itemsMailBox.add(item);
                    }
                    if (NDVSqlFetcher.updateMailBox(pl)) {
                        Service.gI().sendThongBao(pl, "Bạn vừa nhận quà về mail thành công");
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public void showRewardInfo(Player pl, int index, int rewardIndex) {
        Message msg = null;
        try (Connection con = DBConnecter.getConnectionServer(); PreparedStatement ps = con.prepareStatement("SELECT detail FROM moc_nap WHERE id = ?")) {
            ps.setInt(1, index + 1);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    Service.gI().sendThongBao(pl, "Không tìm thấy phần thưởng");
                    return;
                }
                JSONArray dataArray = (JSONArray) JSONValue.parse(rs.getString("detail"));
                if (dataArray == null || rewardIndex < 0 || rewardIndex >= dataArray.size()) {
                    Service.gI().sendThongBao(pl, "Không tìm thấy phần thưởng");
                    return;
                }
                JSONObject dataObject = (JSONObject) JSONValue.parse(String.valueOf(dataArray.get(rewardIndex)));
                int tempId = Integer.parseInt(String.valueOf(dataObject.get("temp_id")));
                int quantity = Integer.parseInt(String.valueOf(dataObject.get("quantity")));
                if (ItemService.gI().getTemplate(tempId) == null) {
                    Service.gI().sendThongBao(pl, "Không tìm thấy vật phẩm");
                    return;
                }
                JSONArray optionsArray = (JSONArray) dataObject.get("options");
                msg = new Message(-76);
                msg.writer().writeByte(2);
                msg.writer().writeShort(tempId);
                msg.writer().writeInt(quantity);
                int optionCount = optionsArray == null ? 0 : Math.min(optionsArray.size(), 255);
                msg.writer().writeByte(optionCount);
                for (int i = 0; i < optionCount; i++) {
                    JSONObject optionObject = (JSONObject) optionsArray.get(i);
                    int optionId = Integer.parseInt(String.valueOf(optionObject.get("id")));
                    int param = Integer.parseInt(String.valueOf(optionObject.get("param")));
                    msg.writer().writeShort(optionId);
                    msg.writer().writeInt(param);
                }
                pl.sendMessage(msg);
            }
        } catch (Exception e) {
            Logger.logException(this.getClass(), e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void getAchievement(Player player) {
        try {
            if (player.getSession() == null) {
                return;
            }

            Connection con = null;
            PreparedStatement ps = null;
            JSONValue jv = new JSONValue();
            JSONArray dataArray = null;
            con = DBConnecter.getConnectionServer();
            ps = con.prepareStatement("SELECT `Achievement` FROM `player` WHERE id = ? LIMIT 1");
            ps.setInt(1, (int) player.id);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String achievementData = rs.getString(1);
                    try {
                        dataArray = (JSONArray) jv.parse(achievementData);
                        if (dataArray != null && dataArray.size() != 10) {
                            if (dataArray.size() < 10) {
                                for (int j = dataArray.size(); j < 10; j++) {
                                    dataArray.add(0);
                                }
                            }

                            while (dataArray.size() > 10) {

                                dataArray.remove(10);

                            }

                        }
                        Map<Integer, List<RewardPreview>> rewardPreviews = loadRewardPreviews();
                        player.archivementList.clear();
                        if (dataArray != null) {

                            for (int i = 0; i < dataArray.size(); i++) {
                                try {
                                    Archivement achievement = new Archivement();
//                                    achievement.setInfo1("Mốc nạp ");
                                    achievement.setInfo1("Mốc nạp " + getNhiemVu(i));
                                    achievement.setInfo2("Đã nạp: " + getNhiemVu2(player, i) + "/" + getNhiemVu(i));
                                    achievement.setFinish(checktongnap(player, i));
                                    achievement.setMoney((short) getRuby(i));
                                    achievement.setRecieve(Integer.parseInt(String.valueOf(dataArray.get(i))) != 0);
                                    applyRewardPreview(achievement, rewardPreviews.get(i + 1));
                                    player.archivementList.add(achievement);

                                } catch (Exception ee) {
                                    ee.printStackTrace();
                                    return;
                                }
                            }

                        }
                        dataArray.clear();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                System.out.println("Player: " + player.name + " dang xem moc nap");
                Show(player);
                rs.close();
                ps.close();
                con.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Map<Integer, List<RewardPreview>> loadRewardPreviews() {
        Map<Integer, List<RewardPreview>> previews = new HashMap<>();
        try (Connection con = DBConnecter.getConnectionServer();
                PreparedStatement ps = con.prepareStatement("SELECT id, detail FROM moc_nap ORDER BY id");
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                JSONArray dataArray = (JSONArray) JSONValue.parse(rs.getString("detail"));
                List<RewardPreview> rewards = new ArrayList<>();
                if (dataArray != null) {
                    for (Object obj : dataArray) {
                        JSONObject dataObject = (JSONObject) JSONValue.parse(String.valueOf(obj));
                        int tempId = Integer.parseInt(String.valueOf(dataObject.get("temp_id")));
                        int quantity = Integer.parseInt(String.valueOf(dataObject.get("quantity")));
                        if (ItemService.gI().getTemplate(tempId) != null) {
                            rewards.add(new RewardPreview(ItemService.gI().getTemplate(tempId).iconID, quantity));
                        }
                    }
                }
                previews.put(rs.getInt("id"), rewards);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return previews;
    }

    private void applyRewardPreview(Archivement achievement, List<RewardPreview> rewards) {
        if (rewards == null || rewards.isEmpty()) {
            achievement.rewardIconIds = new int[0];
            achievement.rewardQuantities = new int[0];
            return;
        }
        achievement.rewardIconIds = new int[rewards.size()];
        achievement.rewardQuantities = new int[rewards.size()];
        for (int i = 0; i < rewards.size(); i++) {
            RewardPreview reward = rewards.get(i);
            achievement.rewardIconIds[i] = reward.iconId;
            achievement.rewardQuantities[i] = reward.quantity;
        }
    }

    public String getNhiemVu(int index) {
        switch (index) {
            case 0:
                return "" + GIADOLACHIADOI[0];
            case 1:
                return "" + GIADOLACHIADOI[1];
            case 2:
                return "" + GIADOLACHIADOI[2];
            case 3:
                return "" + GIADOLACHIADOI[3];
            case 4:
                return "" + GIADOLACHIADOI[4];
            case 5:
                return "" + GIADOLACHIADOI[5];
            case 6:
                return "" + GIADOLACHIADOI[6];
            case 7:
                return "" + GIADOLACHIADOI[7];
            case 8:
                return "" + GIADOLACHIADOI[8];
            case 9:
                return "" + GIADOLACHIADOI[9];
//            case 10:
//                return "" + GIADOLACHIADOI[10];
//            case 11:
//                return "" + GIADOLACHIADOI[11];
//            case 12:
//                return "" + GIADOLACHIADOI[12];
//            case 13:
//                return "" + GIADOLACHIADOI[13];
            default:
                return "";
        }
    }

    public String getNhiemVu2(Player player, int index) {
        switch (index) {
            case 0:
                return " " + player.getSession().danap + "";
            case 1:
                return " " + player.getSession().danap + "";
            case 2:
                return " " + player.getSession().danap + "";
            case 3:
                return " " + player.getSession().danap + "";
            case 4:
                return " " + player.getSession().danap + "";
            case 5:
                return " " + player.getSession().danap + "";
            case 6:
                return " " + player.getSession().danap + "";
            case 7:
                return " " + player.getSession().danap + "";
            case 8:
                return " " + player.getSession().danap + "";
            case 9:
                return " " + player.getSession().danap + "";
//            case 10:
//                return " " + player.getSession().danap + "";
//            case 11:
//                return " " + player.getSession().danap + "";
//            case 12:
//                return " " + player.getSession().danap + "";
//            case 13:
//                return " " + player.getSession().danap + "";
            default:
                return "";
        }
    }

    public int getRuby(int index) {
        switch (index) {
            case 0:
                return 0;
            case 1:
                return 0;
            case 2:
                return 0;
            case 3:
                return 0;
            case 4:
                return 0;
            case 5:
                return 0;
            case 6:
                return 0;
            case 7:
                return 0;
            case 8:
                return 0;
            case 9:
                return 0;
            case 10:
                return 0;
            case 11:
                return 0;
            case 12:
                return 0;
            case 13:
                return 0;

            default:
                return -1;
        }
    }

}
