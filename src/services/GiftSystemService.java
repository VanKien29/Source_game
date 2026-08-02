package services;

import item.Item;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import jdbc.DBConnecter;
import jdbc.daos.NDVSqlFetcher;
import network.Message;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import player.Player;

public class GiftSystemService {

    private static final byte CMD_ATTENDANCE = 3;
    private static final byte CMD_LEVEL = 4;
    private static final byte CMD_ONLINE = 5;
    private static final byte CMD_PACKAGE = 7;
    private static final byte CMD_MAIL = 9;
    private static final byte CMD_FIRST_TOPUP = 13;

    private static final Reward[] ATTENDANCE_REWARDS = {
        new Reward(457, 500), new Reward(17, 1), new Reward(861, 10), new Reward(16, 1),
        new Reward(987, 1), new Reward(984, 1), new Reward(987, 1), new Reward(17, 1),
        new Reward(457, 1), new Reward(381, 1), new Reward(17, 1)
    };
    private static final int[] ATTENDANCE_DAYS = {7, 14, 21, 30, 45, 60};

    private static final ProgressReward[] LEVEL_REWARDS = {
        new ProgressReward(10, new Reward(457, 5), new Reward(861, 5), new Reward(17, 1),
                new Reward(16, 1), new Reward(381, 1), new Reward(987, 1)),
        new ProgressReward(30, new Reward(457, 10), new Reward(861, 10), new Reward(17, 2),
                new Reward(16, 1), new Reward(987, 1), new Reward(984, 1)),
        new ProgressReward(50, new Reward(457, 20), new Reward(861, 15), new Reward(17, 2),
                new Reward(16, 2), new Reward(381, 1), new Reward(984, 1)),
        new ProgressReward(80, new Reward(457, 30), new Reward(861, 20), new Reward(17, 3),
                new Reward(381, 1), new Reward(987, 1), new Reward(984, 1)),
        new ProgressReward(120, new Reward(457, 40), new Reward(861, 30), new Reward(16, 3),
                new Reward(17, 4), new Reward(987, 2), new Reward(381, 2)),
        new ProgressReward(150, new Reward(457, 50), new Reward(861, 50), new Reward(16, 5),
                new Reward(17, 5), new Reward(984, 2), new Reward(987, 3))
    };

    private static final ProgressReward[] ONLINE_REWARDS = {
        new ProgressReward(10, new Reward(457, 5)),
        new ProgressReward(30, new Reward(457, 10)),
        new ProgressReward(60, new Reward(861, 5)),
        new ProgressReward(90, new Reward(17, 1)),
        new ProgressReward(120, new Reward(16, 1)),
        new ProgressReward(180, new Reward(381, 1))
    };

    private static final PackageReward[] DAILY_PACKAGES = {
        new PackageReward(1, "G\u00f3i ng\u00e0y 1", "Nh\u1eadn nhanh v\u1eadt ph\u1ea9m d\u00f9ng h\u1eb1ng ng\u00e0y", 10000, new Reward(457, 50), new Reward(861, 5)),
        new PackageReward(2, "G\u00f3i ng\u00e0y 2", "Th\u00eam t\u00e0i nguy\u00ean luy\u1ec7n t\u1eadp", 20000, new Reward(457, 100), new Reward(17, 2)),
        new PackageReward(3, "G\u00f3i ng\u00e0y 3", "G\u00f3i \u0111\u1ea7y \u0111\u1ee7 cho ng\u00e0y m\u1edbi", 50000, new Reward(457, 200), new Reward(381, 1)),
        new PackageReward(4, "G\u00f3i ng\u00e0y 4", "G\u00f3i t\u0103ng t\u1ed1c luy\u1ec7n t\u1eadp", 50000, new Reward(457, 5), new Reward(861, 10), new Reward(17, 15), new Reward(16, 20), new Reward(381, 25)),
        new PackageReward(5, "G\u00f3i ng\u00e0y 5", "G\u00f3i cao c\u1ea5p cho ng\u00e0y m\u1edbi", 100000, new Reward(457, 5), new Reward(861, 10), new Reward(17, 15), new Reward(16, 20), new Reward(381, 25))
    };

    private static final PackageReward[] VIP_PACKAGES = {
        new PackageReward(101, "G\u00f3i \u01b0u \u0111\u00e3i 1", "Mua m\u1ed9t l\u1ea7n cho t\u00e0i kho\u1ea3n", 50000, new Reward(457, 250), new Reward(987, 1)),
        new PackageReward(102, "G\u00f3i \u01b0u \u0111\u00e3i 2", "\u01afu \u0111\u00e3i s\u1ed1 l\u01b0\u1ee3ng c\u00f3 h\u1ea1n", 100000, new Reward(457, 500), new Reward(984, 1)),
        new PackageReward(103, "G\u00f3i \u01b0u \u0111\u00e3i 3", "G\u00f3i VIP \u0111\u1eb7c bi\u1ec7t", 200000, new Reward(457, 1000), new Reward(16, 5))
    };

    private static final ProgressReward[] FIRST_TOPUP_REWARDS = {
        new ProgressReward(50000, new Reward(457, 50), new Reward(17, 10),
                new Reward(16, 10), new Reward(381, 1))
    };

    private static GiftSystemService instance;

    private final Map<Long, PlayerGiftState> states = new HashMap<>();
    private volatile GiftRuntimeConfig config;
    private long configLoadedAt;
    private static final long CONFIG_RELOAD_MS = 0L;

    public static GiftSystemService gI() {
        if (instance == null) {
            instance = new GiftSystemService();
        }
        return instance;
    }

    public void handle(Player player, Message msg) throws IOException {
        if (player == null || msg.reader().available() < 2) {
            return;
        }
        byte type = msg.reader().readByte();
        byte action = msg.reader().readByte();
        switch (type) {
            case CMD_ATTENDANCE:
                handleAttendance(player, action, msg);
                break;
            case CMD_LEVEL:
                handleProgress(player, action, msg, false);
                break;
            case CMD_ONLINE:
                handleProgress(player, action, msg, true);
                break;
            case CMD_PACKAGE:
                handlePackage(player, action, msg);
                break;
            case CMD_MAIL:
                handleMail(player, action);
                break;
            case CMD_FIRST_TOPUP:
                handleFirstTopup(player, action, msg);
                break;
            default:
                Service.gI().sendThongBao(player, "GiftSystem: msg kh\u00f4ng h\u1ee3p l\u1ec7");
                break;
        }
    }

    private void handleAttendance(Player player, byte action, Message msg) {
        GiftRuntimeConfig cfg = config();
        PlayerGiftState state = state(player);
        loadPlayerStateInto(player, state);
        if (action == 1) {
            if (checkedToday(player, state)) {
                sendNotice(player, CMD_ATTENDANCE, action, text("attendance_already", "H\u00f4m nay b\u1ea1n \u0111\u00e3 \u0111i\u1ec3m danh r\u1ed3i"));
                return;
            }
            Reward[] attendanceRewards = cfg.attendanceRewards();
            if (attendanceRewards.length == 0) {
                sendNotice(player, CMD_ATTENDANCE, action, text("reward_missing", "Ch\u01b0a c\u00f3 c\u1ea5u h\u00ecnh ph\u1ea7n th\u01b0\u1edfng"));
                sendAttendance(player);
                return;
            }
            Reward reward = attendanceRewards[(int) (System.currentTimeMillis() % attendanceRewards.length)];
            if (InventoryService.gI().getCountEmptyBag(player) < 1) {
                sendNotice(player, CMD_ATTENDANCE, action, text("bag_full", "H\u00e0nh trang kh\u00f4ng \u0111\u1ee7 ch\u1ed7"));
                return;
            }
            if (!markAttendance(player, state)) {
                sendNotice(player, CMD_ATTENDANCE, action, text("attendance_already", "H\u00f4m nay b\u1ea1n \u0111\u00e3 \u0111i\u1ec3m danh r\u1ed3i"));
                return;
            }
            Reward[] claimedRewards = new Reward[]{reward};
            giveRewards(player, claimedRewards);
            sendNotice(player, CMD_ATTENDANCE, action, text("attendance_success", "\u0110i\u1ec3m danh th\u00e0nh c\u00f4ng"), claimedRewards);
            return;
        }
        sendAttendance(player);
    }

    private void handleProgress(Player player, byte action, Message msg, boolean online) throws IOException {
        GiftRuntimeConfig cfg = config();
        PlayerGiftState state = state(player);
        loadPlayerStateInto(player, state);
        ProgressReward[] rewards = online ? cfg.onlineRewards() : cfg.levelRewards();
        Set<Long> claimed = online ? state.onlineClaims : state.levelClaims;
        if (online) {
            resetDailyStateIfNeeded(player, state);
        }
        long current = online ? onlineMinutesToday(state)
                : Math.max(player.cap, player.nPoint != null ? player.nPoint.power / 1000000000L : 0L);
        if (action == 2 && msg.reader().available() > 0) {
            long threshold = parseLong(msg.reader().readUTF());
            ProgressReward entry = findProgress(rewards, threshold);
            if (entry == null || current < threshold) {
                sendNotice(player, online ? CMD_ONLINE : CMD_LEVEL, (byte) 1, text("progress_not_reached", "B\u1ea1n ch\u01b0a \u0111\u1ea1t m\u1ed1c n\u00e0y"));
                return;
            }
            if (claimed.contains(threshold)) {
                sendNotice(player, online ? CMD_ONLINE : CMD_LEVEL, (byte) 1, text("progress_claimed", "B\u1ea1n \u0111\u00e3 nh\u1eadn m\u1ed1c n\u00e0y r\u1ed3i"));
                return;
            }
            if (!giveRewards(player, entry.rewards)) {
                sendNotice(player, online ? CMD_ONLINE : CMD_LEVEL, (byte) 1, text("bag_full", "H\u00e0nh trang kh\u00f4ng \u0111\u1ee7 ch\u1ed7"));
                return;
            }
            claimed.add(threshold);
            savePlayerState(player, state);
            sendNotice(player, online ? CMD_ONLINE : CMD_LEVEL, (byte) 1, text("claim_success", "Nh\u1eadn qu\u00e0 th\u00e0nh c\u00f4ng"), entry.rewards);
            return;
        }
        sendProgress(player, online, current, rewards, claimed);
    }

    private void handlePackage(Player player, byte action, Message msg) throws IOException {
        GiftRuntimeConfig cfg = config();
        PlayerGiftState state = state(player);
        loadPlayerStateInto(player, state);
        resetDailyStateIfNeeded(player, state);
        if ((action == 1 || action == 3) && msg.reader().available() >= 4) {
            int id = msg.reader().readInt();
            PackageReward pack = findPackage(action == 1 ? cfg.dailyPackages() : cfg.vipPackages(), id);
            if (pack == null) {
                sendNotice(player, CMD_PACKAGE, action, text("package_missing", "G\u00f3i kh\u00f4ng t\u1ed3n t\u1ea1i"));
                return;
            }
            synchronized (state) {
                Set<Integer> claimed = action == 1 ? state.dailyPackages : state.vipPackages;
                if (claimed.contains(id)) {
                    sendNotice(player, CMD_PACKAGE, action, text("package_claimed", "B\u1ea1n \u0111\u00e3 mua g\u00f3i n\u00e0y r\u1ed3i"));
                    return;
                }
                List<Item> items = createRewardItems(player, pack.rewards);
                if (items == null) {
                    sendNotice(player, CMD_PACKAGE, action, text("bag_full", "H\u00e0nh trang kh\u00f4ng \u0111\u1ee7 ch\u1ed7"));
                    return;
                }
                if (!chargeCash(player, pack.price)) {
                    sendNotice(player, CMD_PACKAGE, action, text("cash_not_enough", "Kh\u00f4ng \u0111\u1ee7 cash \u0111\u1ec3 mua g\u00f3i n\u00e0y"));
                    return;
                }
                addRewardItems(player, items);
                claimed.add(id);
                savePlayerState(player, state);
                sendNotice(player, CMD_PACKAGE, action, text("package_success", "Mua g\u00f3i th\u00e0nh c\u00f4ng"), pack.rewards);
                return;
            }
        }
        sendPackages(player);
    }

    private void handleMail(Player player, byte action) {
        if (action == 1) {
            List<Item> claimed = new ArrayList<>();
            if (player.inventory != null && player.inventory.itemsMailBox != null) {
                for (int i = player.inventory.itemsMailBox.size() - 1; i >= 0; i--) {
                    Item item = player.inventory.itemsMailBox.get(i);
                    if (item != null && item.isNotNullItem() && InventoryService.gI().addItemBag(player, item)) {
                        claimed.add(item);
                        player.inventory.itemsMailBox.remove(i);
                    }
                }
            }
            if (claimed.isEmpty()) {
                sendNotice(player, CMD_MAIL, action, text("mail_empty", "H\u00f2m th\u01b0 kh\u00f4ng c\u00f3 v\u1eadt ph\u1ea9m ho\u1eb7c h\u00e0nh trang \u0111\u00e3 \u0111\u1ea7y"));
            } else {
                NDVSqlFetcher.updateMailBox(player);
                InventoryService.gI().sendItemBag(player);
                sendMailNotice(player, text("mail_claim_success", "Nh\u1eadn v\u1eadt ph\u1ea9m trong h\u00f2m th\u01b0 th\u00e0nh c\u00f4ng"), claimed);
            }
            return;
        }
        sendMail(player);
    }

    private void handleFirstTopup(Player player, byte action, Message msg) throws IOException {
        GiftRuntimeConfig cfg = config();
        PlayerGiftState state = state(player);
        loadPlayerStateInto(player, state);
        long current = player.getSession() == null ? 0L : Math.max(0, player.getSession().danap);
        ProgressReward[] rewards = cfg.firstTopupRewards();
        if (action == 1 && msg.reader().available() > 0) {
            long threshold = parseLong(msg.reader().readUTF());
            ProgressReward entry = findProgress(rewards, threshold);
            if (entry == null || current < threshold) {
                sendNotice(player, CMD_FIRST_TOPUP, action, text("topup_not_reached", "B\u1ea1n ch\u01b0a n\u1ea1p \u0111\u1ee7 m\u1ed1c n\u00e0y"));
                return;
            }
            if (state.firstTopupClaims.contains(threshold)) {
                sendNotice(player, CMD_FIRST_TOPUP, action, text("topup_claimed", "B\u1ea1n \u0111\u00e3 nh\u1eadn m\u1ed1c n\u1ea1p n\u00e0y"));
                return;
            }
            if (!giveRewards(player, entry.rewards)) {
                sendNotice(player, CMD_FIRST_TOPUP, action, text("bag_full", "H\u00e0nh trang kh\u00f4ng \u0111\u1ee7 ch\u1ed7"));
                return;
            }
            state.firstTopupClaims.add(threshold);
            savePlayerState(player, state);
            sendNotice(player, CMD_FIRST_TOPUP, action, text("topup_success", "Nh\u1eadn qu\u00e0 n\u1ea1p \u0111\u1ea7u th\u00e0nh c\u00f4ng"), entry.rewards);
            return;
        }
        sendFirstTopup(player, current, rewards, state.firstTopupClaims);
    }

    private void sendAttendance(Player player) {
        GiftRuntimeConfig cfg = config();
        PlayerGiftState state = state(player);
        loadPlayerStateInto(player, state);
        try {
            Message msg = new Message(GiftSystemService.CMD());
            msg.writer().writeByte(CMD_ATTENDANCE);
            msg.writer().writeByte(0);
            msg.writer().writeByte(checkedToday(player, state) ? 1 : 0);
            msg.writer().writeInt(attendanceDays(player, state));
            writeRewards(msg, cfg.attendanceRewards());
            ProgressReward[] milestones = cfg.attendanceMilestones();
            msg.writer().writeByte(milestones.length);
            for (ProgressReward milestone : milestones) {
                int day = (int) milestone.threshold;
                msg.writer().writeInt(day);
                msg.writer().writeByte(attendanceDays(player, state) >= day ? 1 : 0);
                writeRewards(msg, milestone.rewards);
            }
            msg.writer().writeUTF(text("attendance_welcome", "Ch\u00e0o m\u1eebng b\u1ea1n, h\u00e3y \u0111i\u1ec3m danh h\u1eb1ng ng\u00e0y \u0111\u1ec3 nh\u1eadn nh\u1eefng ph\u1ea7n th\u01b0\u1edfng h\u1ea5p d\u1eabn!"));
            msg.writer().writeUTF(text("attendance_random", "Nh\u1eadn ng\u1eabu nhi\u00ean 1 trong c\u00e1c v\u1eadt ph\u1ea9m d\u01b0\u1edbi \u0111\u00e2y"));
            msg.writer().writeUTF(text("attendance_info", "\u0110i\u1ec3m danh h\u1eb1ng ng\u00e0y \u0111\u1ec3 nh\u1eadn qu\u00e0 t\u01b0\u01a1ng \u1ee9ng."));
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception ignored) {
        }
    }

    private void sendProgress(Player player, boolean online, long current, ProgressReward[] rewards, Set<Long> claimed) {
        if (online) {
            PlayerGiftState state = state(player);
            resetDailyStateIfNeeded(player, state);
            claimed = state.onlineClaims;
        }
        try {
            Message msg = new Message(GiftSystemService.CMD());
            msg.writer().writeByte(online ? CMD_ONLINE : CMD_LEVEL);
            msg.writer().writeByte(0);
            msg.writer().writeUTF(String.valueOf(current));
            msg.writer().writeByte(rewards.length);
            for (ProgressReward reward : rewards) {
                msg.writer().writeUTF(String.valueOf(reward.threshold));
                msg.writer().writeByte(claimed.contains(reward.threshold) ? 2 : current >= reward.threshold ? 1 : 0);
                writeRewards(msg, reward.rewards);
            }
            msg.writer().writeUTF(online
                    ? text("online_info", "Online \u0111\u1ee7 th\u1eddi gian \u0111\u1ec3 nh\u1eadn qu\u00e0 t\u01b0\u01a1ng \u1ee9ng.")
                    : text("level_info", "\u0110\u1ea1t m\u1ed1c level \u0111\u1ec3 nh\u1eadn qu\u00e0 t\u01b0\u01a1ng \u1ee9ng."));
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception ignored) {
        }
    }

    private void sendPackages(Player player) {
        GiftRuntimeConfig cfg = config();
        PlayerGiftState state = state(player);
        loadPlayerStateInto(player, state);
        resetDailyStateIfNeeded(player, state);
        try {
            Message msg = new Message(GiftSystemService.CMD());
            msg.writer().writeByte(CMD_PACKAGE);
            msg.writer().writeByte(0);
            writePackages(msg, cfg.dailyPackages(), state.dailyPackages);
            writePackages(msg, cfg.vipPackages(), state.vipPackages);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception ignored) {
        }
    }

    private void sendMail(Player player) {
        try {
            Message msg = new Message(GiftSystemService.CMD());
            msg.writer().writeByte(CMD_MAIL);
            msg.writer().writeByte(0);
            msg.writer().writeUTF(text("mail_sender", "H\u1ec7 Th\u1ed1ng"));
            msg.writer().writeUTF(text("mail_title", "H\u00f2m th\u01b0 v\u1eadt ph\u1ea9m"));
            msg.writer().writeUTF(text("mail_body", "V\u1eadt ph\u1ea9m do h\u1ec7 th\u1ed1ng v\u00e0 qu\u1ea3n tr\u1ecb vi\u00ean g\u1eedi s\u1ebd xu\u1ea5t hi\u1ec7n t\u1ea1i \u0111\u00e2y."));
            writeItemRewards(msg, player.inventory == null ? null : player.inventory.itemsMailBox);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception ignored) {
        }
    }

    private void sendMailNotice(Player player, String text, List<Item> rewards) {
        try {
            Message msg = new Message(GiftSystemService.CMD());
            msg.writer().writeByte(CMD_MAIL);
            msg.writer().writeByte(1);
            msg.writer().writeUTF(text);
            writeItemRewards(msg, rewards);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception ignored) {
        }
    }

    private void sendFirstTopup(Player player, long current, ProgressReward[] rewards, Set<Long> claimed) {
        try {
            Message msg = new Message(GiftSystemService.CMD());
            msg.writer().writeByte(CMD_FIRST_TOPUP);
            msg.writer().writeByte(0);
            msg.writer().writeUTF(String.valueOf(current));
            msg.writer().writeByte(rewards.length);
            for (ProgressReward reward : rewards) {
                msg.writer().writeUTF(String.valueOf(reward.threshold));
                msg.writer().writeByte(claimed.contains(reward.threshold) ? 2 : current >= reward.threshold ? 1 : 0);
                writeRewards(msg, reward.rewards);
            }
            msg.writer().writeUTF(text("topup_info", "T\u00edch l\u0169y n\u1ea1p l\u1ea7n \u0111\u1ea7u theo t\u1ed5ng gi\u00e1 tr\u1ecb account.danap."));
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception ignored) {
        }
    }

    private void sendNotice(Player player, byte type, byte action, String text) {
        sendNotice(player, type, action, text, null);
    }

    private void sendNotice(Player player, byte type, byte action, String text, Reward[] rewards) {
        try {
            Message msg = new Message(GiftSystemService.CMD());
            msg.writer().writeByte(type);
            msg.writer().writeByte(action);
            msg.writer().writeUTF(text);
            if (rewards != null && rewards.length > 0) {
                writeRewards(msg, rewards);
            }
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception ignored) {
        }
    }

    private void writeRewards(Message msg, Reward[] rewards) throws IOException {
        msg.writer().writeByte(rewards.length);
        for (Reward reward : rewards) {
            msg.writer().writeInt(reward.itemId);
            msg.writer().writeInt(reward.amount);
            int optionCount = Math.min(255, reward.options.length);
            msg.writer().writeByte(optionCount);
            for (int i = 0; i < optionCount; i++) {
                msg.writer().writeInt(reward.options[i].id);
                msg.writer().writeInt(reward.options[i].param);
            }
        }
    }

    private void writeItemRewards(Message msg, List<Item> rewards) throws IOException {
        List<Item> valid = new ArrayList<>();
        if (rewards != null) {
            for (Item item : rewards) {
                if (item != null && item.isNotNullItem()) {
                    valid.add(item);
                }
            }
        }
        msg.writer().writeByte(Math.min(255, valid.size()));
        for (int i = 0; i < valid.size() && i < 255; i++) {
            Item item = valid.get(i);
            msg.writer().writeInt(item.template.id);
            msg.writer().writeInt(item.quantity);
            int optionCount = item.itemOptions == null ? 0 : Math.min(255, item.itemOptions.size());
            msg.writer().writeByte(optionCount);
            for (int j = 0; j < optionCount; j++) {
                Item.ItemOption option = item.itemOptions.get(j);
                msg.writer().writeInt(option.optionTemplate.id);
                msg.writer().writeInt(RandomOptionService.getDisplayParam(option));
            }
        }
    }

    private void writePackages(Message msg, PackageReward[] packages, Set<Integer> claimed) throws IOException {
        msg.writer().writeByte(packages.length);
        for (PackageReward pack : packages) {
            msg.writer().writeInt(pack.id);
            msg.writer().writeUTF(pack.label);
            msg.writer().writeUTF(pack.description);
            msg.writer().writeInt(pack.price);
            msg.writer().writeByte(claimed.contains(pack.id) ? 2 : 1);
            writeRewards(msg, pack.rewards);
        }
    }

    private boolean giveRewards(Player player, Reward[] rewards) {
        List<Item> items = createRewardItems(player, rewards);
        if (items == null) {
            return false;
        }
        addRewardItems(player, items);
        return true;
    }

    private List<Item> createRewardItems(Player player, Reward[] rewards) {
        if (rewards == null) {
            return new ArrayList<>();
        }
        if (InventoryService.gI().getCountEmptyBag(player) < rewards.length) {
            return null;
        }
        List<Item> items = new ArrayList<>();
        for (Reward reward : rewards) {
            try {
                Item item = ItemService.gI().createNewItem((short) reward.itemId, reward.amount);
                if (item == null || !item.isNotNullItem()) {
                    Service.gI().sendThongBao(player, "Kh\u00f4ng t\u1ea1o \u0111\u01b0\u1ee3c item " + reward.itemId);
                    return null;
                }
                for (RewardOption option : reward.options) {
                    if (ItemService.gI().getItemOptionTemplate(option.id) != null) {
                        item.itemOptions.add(new Item.ItemOption(option.id, option.param));
                    }
                }
                items.add(item);
            } catch (Exception e) {
                Service.gI().sendThongBao(player, "Kh\u00f4ng t\u1ea1o \u0111\u01b0\u1ee3c item " + reward.itemId);
                return null;
            }
        }
        return items;
    }

    private void addRewardItems(Player player, List<Item> items) {
        for (Item item : items) {
            InventoryService.gI().addItemBag(player, item);
        }
        InventoryService.gI().sendItemBag(player);
    }

    private boolean chargeCash(Player player, int cash) {
        if (cash <= 0) {
            return true;
        }
        if (player == null || player.getSession() == null || player.getSession().cash < cash) {
            return false;
        }
        String sql = "UPDATE account SET cash = cash - ? WHERE id = ? AND cash >= ?";
        try (Connection con = DBConnecter.getConnectionServer();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, cash);
            ps.setInt(2, player.getSession().userId);
            ps.setInt(3, cash);
            if (ps.executeUpdate() <= 0) {
                return false;
            }
            player.getSession().cash -= cash;
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    private PlayerGiftState state(Player player) {
        return states.computeIfAbsent(player.id, id -> loadPlayerState(player));
    }

    private boolean markAttendance(Player player, PlayerGiftState state) {
        if (player.getSession() == null) {
            return false;
        }
        LocalDate today = LocalDate.now();
        try (Connection con = DBConnecter.getConnectionServer()) {
            ensurePhucLoiTables(con);
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE player SET phuc_loi_attendance_days = COALESCE(phuc_loi_attendance_days, 0) + 1, phuc_loi_last_attendance = ? "
                    + "WHERE id = ? AND (COALESCE(phuc_loi_attendance_days, 0) <= 0 OR phuc_loi_last_attendance IS NULL OR phuc_loi_last_attendance != ?)")) {
				java.sql.Date sqlToday = java.sql.Date.valueOf(today);
				ps.setDate(1, sqlToday);
				ps.setLong(2, player.id);
				ps.setDate(3, sqlToday);
                if (ps.executeUpdate() <= 0) {
                    loadPlayerStateInto(player, state);
                    return false;
                }
                state.attendanceDays++;
                state.lastAttendanceDay = today.toString();
                savePlayerState(player, state);
                return true;
            }
        } catch (SQLException e) {
            return false;
        }
    }

    private boolean checkedToday(Player player, PlayerGiftState state) {
        LocalDate today = LocalDate.now();
        if (player.getSession() != null) {
            loadPlayerStateInto(player, state);
        }
        if (state.attendanceDays > 0 && today.toString().equals(state.lastAttendanceDay)) {
            return true;
        }
        return false;
    }

    private int attendanceDays(Player player, PlayerGiftState state) {
        return state.attendanceDays;
    }

    private GiftRuntimeConfig config() {
        long now = System.currentTimeMillis();
        GiftRuntimeConfig current = config;
        if (current != null && now - configLoadedAt < CONFIG_RELOAD_MS) {
            return current;
        }
        synchronized (this) {
            if (config != null && now - configLoadedAt < CONFIG_RELOAD_MS) {
                return config;
            }
            config = loadConfig();
            configLoadedAt = now;
            return config;
        }
    }

    private GiftRuntimeConfig loadConfig() {
        GiftRuntimeConfig cfg = new GiftRuntimeConfig();
        try (Connection con = DBConnecter.getConnectionServer()) {
            ensurePhucLoiTables(con);
            seedDefaultConfigIfEmpty(con);
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT type, ref_id, label, description, price, rewards_json, msg_key, msg_value "
                    + "FROM phuc_loi_config WHERE active = 1 ORDER BY sort_order ASC, id ASC");
                    ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String type = rs.getString("type");
                    if ("message".equals(type)) {
                        cfg.messages.put(rs.getString("msg_key"), rs.getString("msg_value"));
                        continue;
                    }
                    long refId = rs.getLong("ref_id");
                    Reward[] rewards = parseRewards(rs.getString("rewards_json"));
                    if ("attendance_daily".equals(type)) {
                        cfg.attendanceDaily.addAll(toList(rewards));
                    } else if ("attendance_milestone".equals(type)) {
                        cfg.attendanceMilestones.add(new ProgressReward(refId, rewards));
                    } else if ("level".equals(type)) {
                        cfg.level.add(new ProgressReward(refId, rewards));
                    } else if ("online".equals(type)) {
                        cfg.online.add(new ProgressReward(refId, rewards));
                    } else if ("daily_package".equals(type)) {
                        cfg.dailyPackages.add(new PackageReward((int) refId, rs.getString("label"), rs.getString("description"), rs.getInt("price"), rewards));
                    } else if ("vip_package".equals(type)) {
                        cfg.vipPackages.add(new PackageReward((int) refId, rs.getString("label"), rs.getString("description"), rs.getInt("price"), rewards));
                    } else if ("first_topup".equals(type)) {
                        cfg.firstTopup.add(new ProgressReward(refId, rewards));
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return cfg;
    }

    private String text(String key, String fallback) {
        GiftRuntimeConfig cfg = config();
        String value = cfg.messages.get(key);
        return value == null || value.trim().isEmpty() ? fallback : value;
    }

    private List<Reward> toList(Reward[] rewards) {
        List<Reward> list = new ArrayList<>();
        if (rewards != null) {
            for (Reward reward : rewards) {
                list.add(reward);
            }
        }
        return list;
    }

    private Reward[] parseRewards(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return new Reward[0];
        }
        Object parsed = JSONValue.parse(raw);
        if (parsed instanceof JSONArray) {
            JSONArray array = (JSONArray) parsed;
            List<Reward> rewards = new ArrayList<>();
            for (Object item : array) {
                if (item instanceof JSONObject) {
                    JSONObject obj = (JSONObject) item;
                    int itemId = jsonInt(obj, "item_id", jsonInt(obj, "temp_id", 0));
                    int amount = jsonInt(obj, "amount", jsonInt(obj, "quantity", 1));
                    if (itemId > 0 && amount > 0) {
                        rewards.add(new Reward(itemId, amount, parseRewardOptions(obj.get("options"))));
                    }
                }
            }
            return rewards.toArray(new Reward[0]);
        }
        List<Reward> rewards = new ArrayList<>();
        for (String token : raw.split("[,;\\n]+")) {
            String[] pair = token.trim().split("[:x\\s]+");
            if (pair.length >= 2) {
                int itemId = parseInt(pair[0]);
                int amount = parseInt(pair[1]);
                if (itemId > 0 && amount > 0) {
                    rewards.add(new Reward(itemId, amount));
                }
            }
        }
        return rewards.toArray(new Reward[0]);
    }

    private RewardOption[] parseRewardOptions(Object raw) {
        if (!(raw instanceof JSONArray)) {
            return new RewardOption[0];
        }
        JSONArray array = (JSONArray) raw;
        List<RewardOption> options = new ArrayList<>();
        for (Object value : array) {
            if (value instanceof JSONObject) {
                JSONObject option = (JSONObject) value;
                int id = jsonInt(option, "id", 0);
                int param = jsonInt(option, "param", 0);
                if (id >= 0 && param >= 0) {
                    options.add(new RewardOption(id, param));
                }
            } else if (value instanceof JSONArray) {
                JSONArray option = (JSONArray) value;
                if (option.size() >= 2) {
                    int id = parseInt(String.valueOf(option.get(0)));
                    int param = parseInt(String.valueOf(option.get(1)));
                    if (id >= 0 && param >= 0) {
                        options.add(new RewardOption(id, param));
                    }
                }
            }
        }
        return options.toArray(new RewardOption[0]);
    }

    private int jsonInt(JSONObject obj, String key, int fallback) {
        Object value = obj.get(key);
        if (value == null) {
            return fallback;
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private int parseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (Exception ignored) {
            return 0;
        }
    }

    private PlayerGiftState loadPlayerState(Player player) {
        PlayerGiftState state = new PlayerGiftState();
        loadPlayerStateInto(player, state);
        return state;
    }

    private void resetDailyStateIfNeeded(Player player, PlayerGiftState state) {
        String today = LocalDate.now().toString();
        if (today.equals(state.dailyResetDay)) {
            return;
        }
        state.onlineClaims.clear();
        state.dailyPackages.clear();
        state.dailyResetDay = today;
        savePlayerState(player, state);
    }

    private long onlineMinutesToday(PlayerGiftState state) {
        long startOfDay = LocalDate.now().atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
        long start = Math.max(state.loginMillis, startOfDay);
        return Math.max(0L, (System.currentTimeMillis() - start) / 60000L);
    }

    private void loadPlayerStateInto(Player player, PlayerGiftState state) {
        if (player == null || player.getSession() == null) {
            return;
        }
        try (Connection con = DBConnecter.getConnectionServer()) {
            ensurePhucLoiTables(con);
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT phuc_loi_attendance_days, phuc_loi_last_attendance, phuc_loi_level_claims, phuc_loi_online_claims, "
                    + "phuc_loi_daily_package_claims, phuc_loi_vip_package_claims, phuc_loi_daily_reset_date, "
                    + "phuc_loi_first_topup_claims FROM player WHERE id = ? LIMIT 1")) {
				ps.setLong(1, player.id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        state.attendanceDays = rs.getInt("phuc_loi_attendance_days");
                        java.sql.Date last = rs.getDate("phuc_loi_last_attendance");
                        state.lastAttendanceDay = last == null ? "" : last.toLocalDate().toString();
                        state.levelClaims.clear();
                        state.levelClaims.addAll(parseLongSet(rs.getString("phuc_loi_level_claims")));
                        state.onlineClaims.clear();
                        state.onlineClaims.addAll(parseLongSet(rs.getString("phuc_loi_online_claims")));
                        state.dailyPackages.clear();
                        state.dailyPackages.addAll(parseIntSet(rs.getString("phuc_loi_daily_package_claims")));
                        state.vipPackages.clear();
                        state.vipPackages.addAll(parseIntSet(rs.getString("phuc_loi_vip_package_claims")));
                        java.sql.Date resetDate = rs.getDate("phuc_loi_daily_reset_date");
                        state.dailyResetDay = resetDate == null ? "" : resetDate.toLocalDate().toString();
                        state.firstTopupClaims.clear();
                        state.firstTopupClaims.addAll(parseLongSet(rs.getString("phuc_loi_first_topup_claims")));
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

    private void savePlayerState(Player player, PlayerGiftState state) {
        if (player == null || player.getSession() == null || state == null) {
            return;
        }
        try (Connection con = DBConnecter.getConnectionServer()) {
            ensurePhucLoiTables(con);
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE player SET phuc_loi_attendance_days = ?, phuc_loi_last_attendance = ?, phuc_loi_level_claims = ?, "
                    + "phuc_loi_online_claims = ?, phuc_loi_daily_package_claims = ?, phuc_loi_vip_package_claims = ?, "
                    + "phuc_loi_daily_reset_date = ?, phuc_loi_first_topup_claims = ? WHERE id = ?")) {
                ps.setInt(1, state.attendanceDays);
                if (state.lastAttendanceDay == null || state.lastAttendanceDay.trim().isEmpty()) {
                    ps.setDate(2, null);
                } else {
                    ps.setDate(2, java.sql.Date.valueOf(state.lastAttendanceDay));
                }
                ps.setString(3, joinLongs(state.levelClaims));
                ps.setString(4, joinLongs(state.onlineClaims));
                ps.setString(5, joinInts(state.dailyPackages));
                ps.setString(6, joinInts(state.vipPackages));
                if (state.dailyResetDay == null || state.dailyResetDay.trim().isEmpty()) {
                    ps.setDate(7, null);
                } else {
                    ps.setDate(7, java.sql.Date.valueOf(state.dailyResetDay));
                }
                ps.setString(8, joinLongs(state.firstTopupClaims));
				ps.setLong(9, player.id);
                ps.executeUpdate();
            }
        } catch (Exception ignored) {
        }
    }

    private Set<Long> parseLongSet(String raw) {
        Set<Long> set = new HashSet<>();
        if (raw != null && !raw.trim().isEmpty()) {
            for (String token : raw.split(",")) {
                long value = parseLong(token.trim());
                if (value > 0) {
                    set.add(value);
                }
            }
        }
        return set;
    }

    private Set<Integer> parseIntSet(String raw) {
        Set<Integer> set = new HashSet<>();
        if (raw != null && !raw.trim().isEmpty()) {
            for (String token : raw.split(",")) {
                int value = parseInt(token.trim());
                if (value > 0) {
                    set.add(value);
                }
            }
        }
        return set;
    }

    private String joinLongs(Set<Long> values) {
        List<Long> list = new ArrayList<>(values);
        list.sort(Comparator.naturalOrder());
        StringBuilder sb = new StringBuilder();
        for (Long value : list) {
            if (sb.length() > 0) {
                sb.append(',');
            }
            sb.append(value);
        }
        return sb.toString();
    }

    private String joinInts(Set<Integer> values) {
        List<Integer> list = new ArrayList<>(values);
        list.sort(Comparator.naturalOrder());
        StringBuilder sb = new StringBuilder();
        for (Integer value : list) {
            if (sb.length() > 0) {
                sb.append(',');
            }
            sb.append(value);
        }
        return sb.toString();
    }

    private void ensurePhucLoiTables(Connection con) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "CREATE TABLE IF NOT EXISTS phuc_loi_config ("
                + "id INT AUTO_INCREMENT PRIMARY KEY,"
                + "type VARCHAR(32) NOT NULL,"
                + "ref_id BIGINT NOT NULL DEFAULT 0,"
                + "label VARCHAR(255) DEFAULT '',"
                + "description TEXT,"
                + "price INT NOT NULL DEFAULT 0,"
                + "rewards_json TEXT NOT NULL,"
                + "msg_key VARCHAR(64) NOT NULL DEFAULT '',"
                + "msg_value TEXT,"
                + "sort_order INT NOT NULL DEFAULT 0,"
                + "active TINYINT(1) NOT NULL DEFAULT 1,"
                + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,"
                + "UNIQUE KEY uk_phuc_loi_config_key (type, ref_id, msg_key)"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4")) {
            ps.executeUpdate();
        }
        dropColumn(con, "phuc_loi_config", "cash");
        ensureColumn(con, "phuc_loi_config", "msg_key", "VARCHAR(64) NOT NULL DEFAULT ''");
        ensureColumn(con, "phuc_loi_config", "msg_value", "TEXT");
		dropIndex(con, "phuc_loi_config", "uk_phuc_loi_config");
		ensureIndex(con, "phuc_loi_config", "uk_phuc_loi_config_key", "UNIQUE KEY uk_phuc_loi_config_key (type, ref_id, msg_key)");
        ensurePlayerGiftColumns(con);
        migrateOldGiftMessages(con);
        migrateOldGiftPlayerData(con);
        dropTable(con, "phuc_loi_message");
        dropTable(con, "phuc_loi_player");
	}

    private void ensurePlayerGiftColumns(Connection con) {
        ensureColumn(con, "player", "phuc_loi_attendance_days", "INT NOT NULL DEFAULT 0");
        ensureColumn(con, "player", "phuc_loi_last_attendance", "DATE NULL");
        ensureColumn(con, "player", "phuc_loi_level_claims", "TEXT");
        ensureColumn(con, "player", "phuc_loi_online_claims", "TEXT");
        ensureColumn(con, "player", "phuc_loi_daily_package_claims", "TEXT");
        ensureColumn(con, "player", "phuc_loi_vip_package_claims", "TEXT");
        ensureColumn(con, "player", "phuc_loi_daily_reset_date", "DATE NULL");
        ensureColumn(con, "player", "phuc_loi_first_topup_claims", "TEXT");
    }

    private void migrateOldGiftMessages(Connection con) {
        if (!tableExists(con, "phuc_loi_message")) {
            return;
        }
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT IGNORE INTO phuc_loi_config (type, ref_id, label, description, price, rewards_json, msg_key, msg_value, sort_order, active) "
                + "SELECT 'message', 0, '', '', 0, '', msg_key, msg_value, 10000, active FROM phuc_loi_message")) {
            ps.executeUpdate();
        } catch (Exception ignored) {
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT IGNORE INTO phuc_loi_config (type, ref_id, label, description, price, rewards_json, msg_key, msg_value, sort_order, active) "
                    + "SELECT 'message', 0, '', '', 0, '', msg_key, msg_value, 10000, 1 FROM phuc_loi_message")) {
                ps.executeUpdate();
            } catch (Exception ignoredFallback) {
            }
        }
    }

    private void migrateOldGiftPlayerData(Connection con) {
        if (!tableExists(con, "phuc_loi_player")) {
            return;
        }
        try (PreparedStatement ps = con.prepareStatement(
                "UPDATE player p JOIN phuc_loi_player old ON p.id = old.player_id SET "
                + "p.phuc_loi_attendance_days = IF(COALESCE(p.phuc_loi_attendance_days, 0) = 0, old.attendance_days, p.phuc_loi_attendance_days), "
                + "p.phuc_loi_last_attendance = IF(p.phuc_loi_last_attendance IS NULL, old.last_attendance, p.phuc_loi_last_attendance), "
                + "p.phuc_loi_level_claims = IF(p.phuc_loi_level_claims IS NULL OR p.phuc_loi_level_claims = '', old.level_claims, p.phuc_loi_level_claims), "
                + "p.phuc_loi_online_claims = IF(p.phuc_loi_online_claims IS NULL OR p.phuc_loi_online_claims = '', old.online_claims, p.phuc_loi_online_claims), "
                + "p.phuc_loi_daily_package_claims = IF(p.phuc_loi_daily_package_claims IS NULL OR p.phuc_loi_daily_package_claims = '', old.daily_package_claims, p.phuc_loi_daily_package_claims), "
                + "p.phuc_loi_vip_package_claims = IF(p.phuc_loi_vip_package_claims IS NULL OR p.phuc_loi_vip_package_claims = '', old.vip_package_claims, p.phuc_loi_vip_package_claims)")) {
            ps.executeUpdate();
        } catch (Exception ignored) {
        }
    }

    private boolean tableExists(Connection con, String table) {
        try (PreparedStatement ps = con.prepareStatement("SHOW TABLES LIKE ?")) {
            ps.setString(1, table);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (Exception ignored) {
        }
        try (ResultSet rs = con.getMetaData().getTables(null, null, table, null)) {
            return rs.next();
        } catch (Exception ignored) {
            return false;
        }
    }

    private void dropTable(Connection con, String table) {
        try (PreparedStatement ps = con.prepareStatement("DROP TABLE IF EXISTS " + table)) {
            ps.executeUpdate();
        } catch (Exception ignored) {
        }
    }

	private void ensureColumn(Connection con, String table, String column, String definition) {
		try (PreparedStatement ps = con.prepareStatement("ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition)) {
			ps.executeUpdate();
		} catch (Exception ignored) {
		}
	}

	private void dropColumn(Connection con, String table, String column) {
		try (PreparedStatement ps = con.prepareStatement("ALTER TABLE " + table + " DROP COLUMN " + column)) {
			ps.executeUpdate();
		} catch (Exception ignored) {
		}
	}

	private void dropIndex(Connection con, String table, String index) {
		try (PreparedStatement ps = con.prepareStatement("ALTER TABLE " + table + " DROP INDEX " + index)) {
			ps.executeUpdate();
		} catch (Exception ignored) {
		}
	}

	private void ensureIndex(Connection con, String table, String index, String definition) {
		try (PreparedStatement ps = con.prepareStatement("ALTER TABLE " + table + " ADD " + definition)) {
			ps.executeUpdate();
		} catch (Exception ignored) {
		}
	}

    private void seedDefaultConfigIfEmpty(Connection con) throws SQLException {
        int count = 0;
        try (PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM phuc_loi_config WHERE type <> 'message'");
                ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                count = rs.getInt(1);
            }
        }
        if (count == 0) {
            insertConfig(con, "attendance_daily", 0, "", "", 0,
                    "[{\"item_id\":457,\"amount\":500},{\"item_id\":17,\"amount\":1},{\"item_id\":861,\"amount\":10},{\"item_id\":16,\"amount\":1},{\"item_id\":987,\"amount\":1},{\"item_id\":984,\"amount\":1},{\"item_id\":987,\"amount\":1},{\"item_id\":17,\"amount\":1},{\"item_id\":457,\"amount\":1},{\"item_id\":381,\"amount\":1},{\"item_id\":17,\"amount\":1}]", 1);
            int[] days = {7, 14, 21, 30, 45, 60};
            for (int i = 0; i < days.length; i++) {
                insertConfig(con, "attendance_milestone", days[i], "", "", 0,
                        "[{\"item_id\":457,\"amount\":" + (days[i] * 10) + "}]", 10 + i);
            }
            seedProgress(con, "level", LEVEL_REWARDS, 100);
            seedProgress(con, "online", ONLINE_REWARDS, 200);
            seedPackages(con, "daily_package", DAILY_PACKAGES, 300);
            seedPackages(con, "vip_package", VIP_PACKAGES, 400);
        }
        int firstTopupCount = 0;
        try (PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM phuc_loi_config WHERE type = 'first_topup'");
                ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                firstTopupCount = rs.getInt(1);
            }
        }
        if (firstTopupCount == 0) {
            seedProgress(con, "first_topup", FIRST_TOPUP_REWARDS, 500);
        }
        int messageCount = 0;
        try (PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM phuc_loi_config WHERE type = 'message'");
                ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                messageCount = rs.getInt(1);
            }
        }
        if (messageCount == 0) {
            insertMessage(con, "attendance_already", "H\u00f4m nay b\u1ea1n \u0111\u00e3 \u0111i\u1ec3m danh r\u1ed3i");
            insertMessage(con, "attendance_success", "\u0110i\u1ec3m danh th\u00e0nh c\u00f4ng");
            insertMessage(con, "bag_full", "H\u00e0nh trang kh\u00f4ng \u0111\u1ee7 ch\u1ed7");
            insertMessage(con, "reward_missing", "Ch\u01b0a c\u00f3 c\u1ea5u h\u00ecnh ph\u1ea7n th\u01b0\u1edfng");
            insertMessage(con, "progress_not_reached", "B\u1ea1n ch\u01b0a \u0111\u1ea1t m\u1ed1c n\u00e0y");
            insertMessage(con, "progress_claimed", "B\u1ea1n \u0111\u00e3 nh\u1eadn m\u1ed1c n\u00e0y r\u1ed3i");
            insertMessage(con, "claim_success", "Nh\u1eadn qu\u00e0 th\u00e0nh c\u00f4ng");
            insertMessage(con, "package_missing", "G\u00f3i kh\u00f4ng t\u1ed3n t\u1ea1i");
            insertMessage(con, "package_claimed", "B\u1ea1n \u0111\u00e3 mua g\u00f3i n\u00e0y r\u1ed3i");
            insertMessage(con, "cash_not_enough", "Kh\u00f4ng \u0111\u1ee7 cash \u0111\u1ec3 mua g\u00f3i n\u00e0y");
            insertMessage(con, "package_success", "Mua g\u00f3i th\u00e0nh c\u00f4ng");
            insertMessage(con, "attendance_welcome", "Ch\u00e0o m\u1eebng b\u1ea1n, h\u00e3y \u0111i\u1ec3m danh h\u1eb1ng ng\u00e0y \u0111\u1ec3 nh\u1eadn nh\u1eefng ph\u1ea7n th\u01b0\u1edfng h\u1ea5p d\u1eabn!");
            insertMessage(con, "attendance_random", "Nh\u1eadn ng\u1eabu nhi\u00ean 1 trong c\u00e1c v\u1eadt ph\u1ea9m d\u01b0\u1edbi \u0111\u00e2y");
            insertMessage(con, "attendance_info", "\u0110i\u1ec3m danh h\u1eb1ng ng\u00e0y \u0111\u1ec3 nh\u1eadn qu\u00e0 t\u01b0\u01a1ng \u1ee9ng.");
            insertMessage(con, "level_info", "\u0110\u1ea1t m\u1ed1c level \u0111\u1ec3 nh\u1eadn qu\u00e0 t\u01b0\u01a1ng \u1ee9ng.");
            insertMessage(con, "online_info", "Online \u0111\u1ee7 th\u1eddi gian \u0111\u1ec3 nh\u1eadn qu\u00e0 t\u01b0\u01a1ng \u1ee9ng.");
            insertMessage(con, "mail_sender", "H\u1ec7 Th\u1ed1ng");
            insertMessage(con, "mail_title", "H\u00f2m th\u01b0 v\u1eadt ph\u1ea9m");
            insertMessage(con, "mail_body", "V\u1eadt ph\u1ea9m do h\u1ec7 th\u1ed1ng v\u00e0 qu\u1ea3n tr\u1ecb vi\u00ean g\u1eedi s\u1ebd xu\u1ea5t hi\u1ec7n t\u1ea1i \u0111\u00e2y.");
            insertMessage(con, "mail_empty", "H\u00f2m th\u01b0 kh\u00f4ng c\u00f3 v\u1eadt ph\u1ea9m ho\u1eb7c h\u00e0nh trang \u0111\u00e3 \u0111\u1ea7y");
            insertMessage(con, "mail_claim_success", "Nh\u1eadn v\u1eadt ph\u1ea9m trong h\u00f2m th\u01b0 th\u00e0nh c\u00f4ng");
            insertMessage(con, "topup_not_reached", "B\u1ea1n ch\u01b0a n\u1ea1p \u0111\u1ee7 m\u1ed1c n\u00e0y");
            insertMessage(con, "topup_claimed", "B\u1ea1n \u0111\u00e3 nh\u1eadn m\u1ed1c n\u1ea1p n\u00e0y");
            insertMessage(con, "topup_success", "Nh\u1eadn qu\u00e0 n\u1ea1p \u0111\u1ea7u th\u00e0nh c\u00f4ng");
            insertMessage(con, "topup_info", "T\u00edch l\u0169y n\u1ea1p l\u1ea7n \u0111\u1ea7u theo t\u1ed5ng gi\u00e1 tr\u1ecb account.danap.");
        }
    }

    private void seedProgress(Connection con, String type, ProgressReward[] data, int offset) throws SQLException {
        for (int i = 0; i < data.length; i++) {
            insertConfig(con, type, data[i].threshold, "", "", 0, rewardsJson(data[i].rewards), offset + i);
        }
    }

    private void seedPackages(Connection con, String type, PackageReward[] data, int offset) throws SQLException {
        for (int i = 0; i < data.length; i++) {
            insertConfig(con, type, data[i].id, data[i].label, data[i].description, data[i].price, rewardsJson(data[i].rewards), offset + i);
        }
    }

    private void insertConfig(Connection con, String type, long refId, String label, String description, int price, String rewardsJson, int sortOrder) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT IGNORE INTO phuc_loi_config (type, ref_id, label, description, price, rewards_json, sort_order, active) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, 1)")) {
            ps.setString(1, type);
            ps.setLong(2, refId);
            ps.setString(3, label);
            ps.setString(4, description);
            ps.setInt(5, price);
            ps.setString(6, rewardsJson);
            ps.setInt(7, sortOrder);
            ps.executeUpdate();
        }
    }

    private void insertMessage(Connection con, String key, String value) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT IGNORE INTO phuc_loi_config (type, ref_id, label, description, price, rewards_json, msg_key, msg_value, sort_order, active) "
                + "VALUES ('message', 0, '', '', 0, '', ?, ?, 10000, 1)")) {
            ps.setString(1, key);
            ps.setString(2, value);
            ps.executeUpdate();
        }
    }

    private String rewardsJson(Reward[] rewards) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < rewards.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append("{\"item_id\":").append(rewards[i].itemId).append(",\"amount\":").append(rewards[i].amount).append('}');
        }
        sb.append(']');
        return sb.toString();
    }

    private ProgressReward findProgress(ProgressReward[] rewards, long threshold) {
        for (ProgressReward reward : rewards) {
            if (reward.threshold == threshold) {
                return reward;
            }
        }
        return null;
    }

    private PackageReward findPackage(PackageReward[] packages, int id) {
        for (PackageReward pack : packages) {
            if (pack.id == id) {
                return pack;
            }
        }
        return null;
    }

    private long parseLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (Exception ignored) {
            return 0L;
        }
    }

    private static int CMD() {
        return 99;
    }

    private static class Reward {
        final int itemId;
        final int amount;
        final RewardOption[] options;

        Reward(int itemId, int amount) {
            this(itemId, amount, new RewardOption[0]);
        }

        Reward(int itemId, int amount, RewardOption[] options) {
            this.itemId = itemId;
            this.amount = amount;
            this.options = options == null ? new RewardOption[0] : options;
        }
    }

    private static class RewardOption {
        final int id;
        final int param;

        RewardOption(int id, int param) {
            this.id = id;
            this.param = param;
        }
    }

    private static class ProgressReward {
        final long threshold;
        final Reward[] rewards;

        ProgressReward(long threshold, Reward... rewards) {
            this.threshold = threshold;
            this.rewards = rewards;
        }
    }

    private static class PackageReward {
        final int id;
        final String label;
        final String description;
        final int price;
        final Reward[] rewards;

        PackageReward(int id, String label, String description, int price, Reward... rewards) {
            this.id = id;
            this.label = label;
            this.description = description;
            this.price = price;
            this.rewards = rewards;
        }
    }

    private static class GiftRuntimeConfig {
        final List<Reward> attendanceDaily = new ArrayList<>();
        final List<ProgressReward> attendanceMilestones = new ArrayList<>();
        final List<ProgressReward> level = new ArrayList<>();
        final List<ProgressReward> online = new ArrayList<>();
        final List<PackageReward> dailyPackages = new ArrayList<>();
        final List<PackageReward> vipPackages = new ArrayList<>();
        final List<ProgressReward> firstTopup = new ArrayList<>();
        final Map<String, String> messages = new HashMap<>();

        Reward[] attendanceRewards() {
            return attendanceDaily.toArray(new Reward[0]);
        }

        ProgressReward[] attendanceMilestones() {
            return attendanceMilestones.toArray(new ProgressReward[0]);
        }

        ProgressReward[] levelRewards() {
            return level.toArray(new ProgressReward[0]);
        }

        ProgressReward[] onlineRewards() {
            return online.toArray(new ProgressReward[0]);
        }

        PackageReward[] dailyPackages() {
            return dailyPackages.toArray(new PackageReward[0]);
        }

        PackageReward[] vipPackages() {
            return vipPackages.toArray(new PackageReward[0]);
        }

        ProgressReward[] firstTopupRewards() {
            return firstTopup.isEmpty() ? FIRST_TOPUP_REWARDS : firstTopup.toArray(new ProgressReward[0]);
        }
    }

    private static class PlayerGiftState {
        int attendanceDays;
        String lastAttendanceDay = "";
        final Set<Long> levelClaims = new HashSet<>();
        final Set<Long> onlineClaims = new HashSet<>();
        final Set<Integer> dailyPackages = new HashSet<>();
        final Set<Integer> vipPackages = new HashSet<>();
        final Set<Long> firstTopupClaims = new HashSet<>();
        String dailyResetDay = "";
        final long loginMillis = System.currentTimeMillis();

        boolean checkedToday() {
            return attendanceDays > 0 && LocalDate.now().toString().equals(lastAttendanceDay);
        }
    }
}
