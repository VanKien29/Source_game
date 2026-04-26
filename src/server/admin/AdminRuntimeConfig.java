package server.admin;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

public class AdminRuntimeConfig {

    public final boolean enabled;
    public final String host;
    public final int port;
    public final String key;
    public final String secret;
    public final long allowedSkewMillis;
    public final int commandTimeoutMillis;
    public final Set<String> allowedIps;

    private AdminRuntimeConfig(Properties props) {
        this.enabled = Boolean.parseBoolean(read(props, "enabled", "true"));
        this.host = read(props, "host", "127.0.0.1");
        this.port = Integer.parseInt(read(props, "port", "19091"));
        this.key = read(props, "key", "web-admin");
        this.secret = read(props, "secret", "");
        this.allowedSkewMillis = Long.parseLong(read(props, "allowed_skew_seconds", "60")) * 1000L;
        this.commandTimeoutMillis = Integer.parseInt(read(props, "command_timeout_ms", "10000"));
        this.allowedIps = new HashSet<>(Arrays.asList(read(props, "allowed_ips", "127.0.0.1,0:0:0:0:0:0:0:1").split(",")));
    }

    public static AdminRuntimeConfig load() {
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
        return new AdminRuntimeConfig(props);
    }

    public boolean canStart() {
        return enabled && secret != null && secret.trim().length() >= 32;
    }

    private static String read(Properties props, String key, String fallback) {
        String envKey = "GAME_RUNTIME_" + key.toUpperCase().replace('.', '_');
        String env = System.getenv(envKey);
        if (env != null && !env.trim().isEmpty()) {
            return env.trim();
        }
        String value = props.getProperty(key);
        return value != null && !value.trim().isEmpty() ? value.trim() : fallback;
    }
}
