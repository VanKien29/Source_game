package sosumenh;

import item.Item;
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
import lombok.Getter;
import network.Message;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import player.Archivement;
import player.Player;
import services.ItemService;
import services.Service;
import utils.Logger;

/**
 * @author BTH Cute Phô Mai Que
 */
public class SoSuMenhService {

    private static class RewardPreview {

        private final int iconId;
        private final int quantity;

        private RewardPreview(int iconId, int quantity) {
            this.iconId = iconId;
            this.quantity = quantity;
        }
    }

    public static SoSuMenhService gI() {
        return instance;
    }

    public void receive(int index, Player player) {
        Archivement achievement = player.archivementList.get(index);

        if (achievement == null) {
            Service.gI().sendThongBao(player, "Không có phần thưởng");
            return;
        }

        if (achievement.isRecieve) {
            Service.gI().sendThongBaoOK(player, "Nhận rồi đừng nhận nữa");
            return;
        }
        sendMessage(player, index);
        achievement.setRecieve(true);
        giveReward(player, index + 1, "items");
        Service.gI().sendThongBao(player, "Nhận thành công, vui lòng kiểm tra hòm thư");
    }

    public void receiveVip(int index, Player player) {
        Archivement achievement = player.archivementList.get(index);

        if (achievement == null) {
            Service.gI().sendThongBao(player, "Không có phần thưởng");
            return;
        }

        if (achievement.isRecieve) {
            Service.gI().sendThongBaoOK(player, "Nhận rồi đừng nhận nữa");
            return;
        }
        sendMessage(player, index);
        achievement.setRecieve(true);
        giveReward(player, index + 1, "items2");
        Service.gI().sendThongBao(player, "Nhận thành công, vui lòng kiểm tra hòm thư");
    }

    private void sendMessage(Player player, int index) {
        Message msg = null;
        try {
            msg = new Message(-76);
            msg.writer().writeByte(1); // Action
            msg.writer().writeByte(index); // Index
            player.sendMessage(msg);
        } catch (IOException e) {
            Logger.logException(this.getClass(), e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    private void giveReward(Player player, int index, String rewardColumn) {
        try (Connection connection = DBConnecter.getConnectionServer(); PreparedStatement ps = connection.prepareStatement("SELECT * FROM so_su_menh_reward")) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int level = rs.getInt("level");
                    if (level == index) {
                        JSONArray dataArray = (JSONArray) JSONValue.parse(rs.getString(rewardColumn));
                        for (Object obj : dataArray) {
                            JSONObject dataObject = (JSONObject) JSONValue.parse(obj.toString());
                            Item item = createItem(dataObject);
                            player.inventory.itemsMailBox.add(item);
                        }
                        if (NDVSqlFetcher.updateMailBox(player)) {
                            Service.gI().sendThongBao(player, "Bạn vừa nhận quà về mail thành công");
                            if (rewardColumn.equals("items")) {
                                player.sosumenhplayer.reward[level - 1] = true;
                            } else {
                                player.sosumenhplayer.rewardVip[level - 1] = true;
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Item createItem(JSONObject dataObject) {
        int tempId = Integer.parseInt(dataObject.get("temp_id").toString());
        int quantity = Integer.parseInt(dataObject.get("quantity").toString());
        Item item = ItemService.gI().createNewItem((short) tempId);
        item.quantity = quantity;

        JSONArray optionsArray = (JSONArray) dataObject.get("options");
        for (Object optionObj : optionsArray) {
            JSONObject optionObject = (JSONObject) optionObj;
            int param = Integer.parseInt(optionObject.get("param").toString());
            int optionId = Integer.parseInt(optionObject.get("id").toString());
            item.itemOptions.add(new Item.ItemOption(optionId, param));
        }
        return item;
    }

    private Map<Integer, List<RewardPreview>> loadRewardPreviews(String rewardColumn) {
        Map<Integer, List<RewardPreview>> previews = new HashMap<>();
        String sql = "SELECT level, " + rewardColumn + " FROM so_su_menh_reward ORDER BY level";
        try (Connection connection = DBConnecter.getConnectionServer();
                PreparedStatement ps = connection.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int level = rs.getInt("level");
                JSONArray dataArray = (JSONArray) JSONValue.parse(rs.getString(rewardColumn));
                List<RewardPreview> rewards = new ArrayList<>();
                if (dataArray != null) {
                    for (Object obj : dataArray) {
                        JSONObject dataObject = (JSONObject) JSONValue.parse(obj.toString());
                        int tempId = Integer.parseInt(dataObject.get("temp_id").toString());
                        int quantity = Integer.parseInt(dataObject.get("quantity").toString());
                        rewards.add(new RewardPreview(ItemService.gI().getTemplate(tempId).iconID, quantity));
                    }
                }
                previews.put(level, rewards);
            }
        } catch (Exception e) {
            Logger.logException(this.getClass(), e);
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

    private void writeRewardPreview(Message msg, Archivement achievement) throws IOException {
        int count = Math.min(achievement.rewardIconIds.length, achievement.rewardQuantities.length);
        count = Math.min(count, 127);
        msg.writer().writeByte(count);
        for (int i = 0; i < count; i++) {
            msg.writer().writeShort(achievement.rewardIconIds[i]);
            msg.writer().writeInt(achievement.rewardQuantities[i]);
        }
    }

    public void showRewardInfo(Player player, int index, int rewardIndex, boolean isVip) {
        Message msg = null;
        String rewardColumn = isVip ? "items2" : "items";
        try (Connection connection = DBConnecter.getConnectionServer();
                PreparedStatement ps = connection.prepareStatement("SELECT " + rewardColumn + " FROM so_su_menh_reward WHERE level = ?")) {
            ps.setInt(1, index + 1);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    Service.gI().sendThongBao(player, "Không tìm thấy phần thưởng");
                    return;
                }
                JSONArray dataArray = (JSONArray) JSONValue.parse(rs.getString(rewardColumn));
                if (dataArray == null || rewardIndex < 0 || rewardIndex >= dataArray.size()) {
                    Service.gI().sendThongBao(player, "Không tìm thấy phần thưởng");
                    return;
                }
                JSONObject dataObject = (JSONObject) JSONValue.parse(String.valueOf(dataArray.get(rewardIndex)));
                int tempId = Integer.parseInt(String.valueOf(dataObject.get("temp_id")));
                int quantity = Integer.parseInt(String.valueOf(dataObject.get("quantity")));
                if (ItemService.gI().getTemplate(tempId) == null) {
                    Service.gI().sendThongBao(player, "Không tìm thấy vật phẩm");
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
                player.sendMessage(msg);
            }
        } catch (Exception e) {
            Logger.logException(this.getClass(), e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void loadAchievements(Player player, boolean isVip) {
        if (player.getSession() == null) {
            return;
        }
        player.archivementList.clear();
        Map<Integer, List<RewardPreview>> rewardPreviews = loadRewardPreviews(isVip ? "items2" : "items");
        for (int i = 0; i < 20; i++) {
            Archivement achievement = new Archivement();
            achievement.setInfo1("Cấp sổ " + (i + 1));
            achievement.setInfo2("Cấp hiện tại: " + player.sosumenhplayer.getLevel() + "/" + (i + 1));
            achievement.setFinish(player.sosumenhplayer.getLevel() >= (i + 1));
            if (isVip) {
                achievement.setFinish(player.sosumenhplayer.getLevel() >= (i + 1) && isVip == player.sosumenhplayer.isVip());
            }
            achievement.setMoney((short) 0);
            achievement.setRecieve(isVip ? player.sosumenhplayer.rewardVip[i] : player.sosumenhplayer.reward[i]);
            applyRewardPreview(achievement, rewardPreviews.get(i + 1));
            player.archivementList.add(achievement);
        }
        show(player, isVip ? 4 : 3);
    }

    public void show(Player player, int type) {
        Message msg = null;
        try {
            msg = new Message(-76);
            msg.writer().writeByte(0); // Action
            msg.writer().writeByte(player.archivementList.size());

            for (Archivement achievement : player.archivementList) {
                msg.writer().writeUTF(achievement.getInfo1());
                msg.writer().writeUTF(achievement.getInfo2());
                msg.writer().writeShort(achievement.getMoney());
                msg.writer().writeBoolean(achievement.isFinish);
                msg.writer().writeBoolean(achievement.isRecieve);
                writeRewardPreview(msg, achievement);
            }

            player.sendMessage(msg);
            player.typeRecvieArchiment = type;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void updateProgress(Player player, int idTask, int amount) {
        for (SoSuMenhTaskMain task : player.sosumenhplayer.ssmTaskMain) {
            if (task.idTask == idTask && !task.finish) {
                task.countTask += amount;
                SoSuMenhTaskTemplate smmtem = SoSuMenhManager.getInstance().findById(idTask);
                if (task.countTask >= task.maxCount) {
                    task.countTask = task.maxCount;
                    task.finish = true;

                    if (smmtem != null) {
                        player.sosumenhplayer.addPoint(smmtem.getPoint());
                        Service.gI().sendThongBao(player,
                                "Hoàn thành nhiệm vụ: " + smmtem.getTask()
                                + " (+ " + smmtem.getPoint() + " điểm)");
                    }
                }
                break;
            }
        }
    }

    @Getter
    private static final SoSuMenhService instance = new SoSuMenhService();
}
