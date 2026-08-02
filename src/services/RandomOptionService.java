package services;

import item.Item;
import item.Item.ItemOption;
import utils.Logger;
import utils.Util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Loads hidden-option groups from the database and rolls one group when an
 * item is equipped for the first time.
 *
 * <p>Option 210 is the marker already present in item_option_template. Its
 * param is the hidden-option group id. The group stores roll_count and the
 * candidate option/param rows. The marker must be the last configured option
 * on a new item; rolled options are appended after it.</p>
 */
public final class RandomOptionService {

    /** Existing item_option_template.id for the hidden-option marker. */
    public static final int RANDOM_OPTION_COUNT_ID = 210;

    private static final String CREATE_GROUP_TABLE = "CREATE TABLE IF NOT EXISTS hidden_option_group ("
            + "id INT NOT NULL AUTO_INCREMENT,"
            + "name VARCHAR(255) NOT NULL,"
            + "roll_count SMALLINT NOT NULL DEFAULT 1,"
            + "is_active TINYINT(1) NOT NULL DEFAULT 1,"
            + "PRIMARY KEY (id)"
            + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";

    private static final String CREATE_DETAIL_TABLE = "CREATE TABLE IF NOT EXISTS hidden_option_group_detail ("
            + "id INT NOT NULL AUTO_INCREMENT,"
            + "group_id INT NOT NULL,"
            + "option_id INT NOT NULL,"
            + "param INT NOT NULL DEFAULT 0,"
            + "param_min INT NULL,"
            + "param_max INT NULL,"
            + "sort_order INT NOT NULL DEFAULT 0,"
            + "is_active TINYINT(1) NOT NULL DEFAULT 1,"
            + "PRIMARY KEY (id),"
            + "KEY idx_hidden_option_group_detail_group (group_id),"
            + "CONSTRAINT fk_hidden_option_group_detail_group "
            + "FOREIGN KEY (group_id) REFERENCES hidden_option_group(id) ON DELETE CASCADE"
            + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";

    private static final Random RANDOM = new Random();
    private static volatile Map<Integer, HiddenOptionGroup> groups = Collections.emptyMap();

    private RandomOptionService() {
    }

    /** Creates the tables and loads active groups into memory. */
    public static synchronized void load(Connection connection) {
        Map<Integer, HiddenOptionGroup> loadedGroups = new HashMap<>();
        try {
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate(CREATE_GROUP_TABLE);
                statement.executeUpdate(CREATE_DETAIL_TABLE);
            }
            ensureParamRangeColumns(connection);

            String sql = "SELECT g.id, g.name, g.roll_count, d.id AS detail_id, "
                    + "d.option_id, d.param, d.param_min, d.param_max "
                    + "FROM hidden_option_group g "
                    + "LEFT JOIN hidden_option_group_detail d "
                    + "ON d.group_id = g.id AND d.is_active = 1 "
                    + "WHERE g.is_active = 1 "
                    + "ORDER BY g.id, d.sort_order, d.id";
            try (PreparedStatement statement = connection.prepareStatement(sql);
                    ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    int groupId = resultSet.getInt("id");
                    HiddenOptionGroup group = loadedGroups.get(groupId);
                    if (group == null) {
                        String name = resultSet.getString("name");
                        group = new HiddenOptionGroup(
                                groupId,
                                name == null ? "" : name,
                                resultSet.getInt("roll_count"));
                        loadedGroups.put(groupId, group);
                    }

                    int detailId = resultSet.getInt("detail_id");
                    if (!resultSet.wasNull()) {
                        int paramMin = resultSet.getInt("param_min");
                        if (resultSet.wasNull()) {
                            paramMin = resultSet.getInt("param");
                        }
                        Number maxValue = (Number) resultSet.getObject("param_max");
                        Integer paramMax = maxValue == null ? null : maxValue.intValue();
                        group.options.add(new HiddenOption(
                                resultSet.getInt("option_id"),
                                paramMin,
                                paramMax));
                    }
                }
            }

            groups = Collections.unmodifiableMap(loadedGroups);
            Logger.success("Successfully loaded hidden option groups (" + groups.size() + ")\n");
        } catch (Exception exception) {
            groups = Collections.emptyMap();
            Logger.logException(RandomOptionService.class, exception,
                    "Cannot load hidden option groups");
        }
    }

    public static int getLoadedGroupCount() {
        return groups.size();
    }

    /**
     * Rolls the configured group if the item has not been rolled yet.
     *
     * @return true when the item is valid for equipping
     */
    public static boolean randomizeOnEquip(Item item) {
        int markerIndex = findMarkerIndex(item);
        if (markerIndex < 0) {
            return true;
        }

        ItemOption marker = item.itemOptions.get(markerIndex);
        HiddenOptionGroup group = groups.get(marker.param);
        if (group == null || group.rollCount <= 0 || group.options.isEmpty()) {
            return false;
        }

        int rolledCount = countRolledOptions(item, markerIndex, group);
        if (rolledCount == group.rollCount) {
            return true;
        }
        if (rolledCount != 0 || group.options.size() < group.rollCount) {
            return false;
        }

        List<HiddenOption> candidates = new ArrayList<>(group.options);
        Collections.shuffle(candidates, RANDOM);

        List<ItemOption> rolledOptions = new ArrayList<>();
        for (int i = 0; i < group.rollCount; i++) {
            HiddenOption candidate = candidates.get(i);
            ItemOption option = new ItemOption(candidate.optionId, candidate.rollParam());
            if (option.optionTemplate == null) {
                return false;
            }
            rolledOptions.add(option);
        }
        item.itemOptions.addAll(rolledOptions);
        return true;
    }

    /**
     * Counts only the generated options at the end of the item. Older items
     * may have a normal option after marker 210; that option must not be
     * mistaken for an already rolled hidden stat.
     */
    private static int countRolledOptions(Item item, int markerIndex, HiddenOptionGroup group) {
        int count = 0;
        for (int index = item.itemOptions.size() - 1; index > markerIndex; index--) {
            ItemOption option = item.itemOptions.get(index);
            if (option == null || option.optionTemplate == null
                    || !group.containsOption(option.optionTemplate.id)) {
                break;
            }
            count++;
        }
        return count;
    }

    /** Removes the marker when the item leaves the body. */
    public static void removeMarkerOnUnequip(Item item) {
        int markerIndex = findMarkerIndex(item);
        if (markerIndex >= 0) {
            item.itemOptions.remove(markerIndex);
        }
    }

    /** Returns the value that should be sent to the client for an option. */
    public static int getDisplayParam(ItemOption option) {
        if (!isMarker(option)) {
            return option == null ? 0 : option.param;
        }
        HiddenOptionGroup group = groups.get(option.param);
        return group == null ? option.param : group.rollCount;
    }

    /** Returns an option line using roll_count for the marker display. */
    public static String getOptionString(ItemOption option) {
        if (option == null || option.optionTemplate == null) {
            return "";
        }
        return Util.replace(option.optionTemplate.name, "#",
                String.valueOf(getDisplayParam(option)));
    }

    /**
     * Returns options that should be sent for a body tooltip. The marker is
     * visible, while rolled options remain active on the server but hidden on
     * the equipped item.
     */
    public static List<ItemOption> getDisplayOptions(Item item, boolean equipped) {
        if (item == null || item.itemOptions == null || !equipped) {
            return item == null || item.itemOptions == null
                    ? new ArrayList<>()
                    : item.itemOptions;
        }

        int markerIndex = findMarkerIndex(item);
        if (markerIndex < 0) {
            return item.itemOptions;
        }
        return new ArrayList<>(item.itemOptions.subList(0, markerIndex + 1));
    }

    public static String getInfo(Item item, boolean equipped) {
        StringBuilder info = new StringBuilder();
        for (ItemOption option : getDisplayOptions(item, equipped)) {
            if (info.length() > 0) {
                info.append("\n");
            }
            info.append(getOptionString(option));
        }
        return info.toString();
    }

    private static boolean isMarker(ItemOption option) {
        return option != null && option.optionTemplate != null
                && option.optionTemplate.id == RANDOM_OPTION_COUNT_ID;
    }

    private static int findMarkerIndex(Item item) {
        if (item == null || item.itemOptions == null) {
            return -1;
        }
        for (int i = 0; i < item.itemOptions.size(); i++) {
            if (isMarker(item.itemOptions.get(i))) {
                return i;
            }
        }
        return -1;
    }

    private static void ensureParamRangeColumns(Connection connection) throws SQLException {
        addColumnIfMissing(connection, "param_min", "INT NULL AFTER param");
        addColumnIfMissing(connection, "param_max", "INT NULL AFTER param_min");
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(
                    "UPDATE hidden_option_group_detail SET param_min = param "
                            + "WHERE param_min IS NULL");
        }
    }

    private static void addColumnIfMissing(Connection connection, String column, String definition)
            throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("ALTER TABLE hidden_option_group_detail ADD COLUMN "
                    + column + " " + definition);
        } catch (SQLException exception) {
            String message = exception.getMessage();
            if (exception.getErrorCode() != 1060
                    && (message == null || !message.toLowerCase().contains("duplicate column"))) {
                throw exception;
            }
        }
    }

    private static final class HiddenOptionGroup {

        private final int id;
        private final String name;
        private final int rollCount;
        private final List<HiddenOption> options = new ArrayList<>();

        private HiddenOptionGroup(int id, String name, int rollCount) {
            this.id = id;
            this.name = name;
            this.rollCount = rollCount;
        }

        private boolean containsOption(int optionId) {
            for (HiddenOption option : options) {
                if (option.optionId == optionId) {
                    return true;
                }
            }
            return false;
        }
    }

    private static final class HiddenOption {

        private final int optionId;
        private final int paramMin;
        private final Integer paramMax;

        private HiddenOption(int optionId, int paramMin, Integer paramMax) {
            this.optionId = optionId;
            this.paramMin = paramMin;
            this.paramMax = paramMax;
        }

        private int rollParam() {
            if (paramMax == null || paramMax <= paramMin) {
                return paramMin;
            }

            long range = (long) paramMax - paramMin + 1L;
            return (int) (paramMin + RANDOM.nextLong(range));
        }
    }
}
