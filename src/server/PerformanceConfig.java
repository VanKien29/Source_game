package server;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

public class PerformanceConfig {

    private static final PerformanceConfig INSTANCE = load();

    public final long botTickMillis;
    public final long botPartyUpdateMillis;
    public final long mapTickMillis;
    public final long eventCheckMillis;

    private PerformanceConfig(Properties props) {
        this.botTickMillis = readLong(props, "performance.bot_tick_ms", 300, 150, 2000);
        this.botPartyUpdateMillis = readLong(props, "performance.bot_party_update_ms", 15000, 5000, 60000);
        this.mapTickMillis = readLong(props, "performance.map_tick_ms", 1000, 500, 3000);
        this.eventCheckMillis = readLong(props, "performance.event_check_ms", 5000, 1000, 60000);
    }

    public static PerformanceConfig gI() {
        return INSTANCE;
    }

    private static PerformanceConfig load() {
        Properties props = new Properties();
        String path = System.getProperty("admin.runtime.config", "admin-runtime.properties");
        File file = new File(path);
        if (file.exists()) {
            try (FileInputStream input = new FileInputStream(file)) {
                props.load(input);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return new PerformanceConfig(props);
    }

    private static long readLong(Properties props, String key, long fallback, long min, long max) {
        String envKey = "GAME_" + key.toUpperCase().replace('.', '_');
        String value = System.getenv(envKey);
        if (value == null || value.trim().isEmpty()) {
            value = props.getProperty(key);
        }
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        try {
            long parsed = Long.parseLong(value.trim());
            return Math.max(min, Math.min(max, parsed));
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
