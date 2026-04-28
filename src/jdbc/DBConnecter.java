package jdbc;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;
import utils.Logger;
import static utils.Logger.RED;
import static utils.Logger.RESET;

public class DBConnecter {

    // ⚙️ Thông tin cơ bản
    private static String DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String URL
            = "jdbc:mysql://%s:%s/%s?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true&useSSL=false&allowPublicKeyRetrieval=true";

    private static String DB_HOST = "localhost";
    private static String DB_PORT = "3306";
    public static String DB_DATA = "nro";
    public static String DB_USER = "root";
    private static String DB_PASSWORD = "";

    // ⚙️ Hikari pool config
    private static int MIN_CONN = 5;
    private static int MAX_CONN = 200;
    private static long MAX_LIFE_TIME = 1800000L; // 30 phút
    private static long IDLE_TIMEOUT = 600000L;   // 10 phút
    private static long CONN_TIMEOUT = 30000L;    // 30 giây
    private static long LEAK_DETECTION_THRESHOLD = 120000L; // 2 phút

    private static final HikariConfig config = new HikariConfig();
    private static final HikariDataSource ds;

    public static Connection getConnectionServer() throws SQLException {
        return ds.getConnection();
    }

    public static void close() {
        if (ds != null && !ds.isClosed()) {
            ds.close();
        }
    }

    private static void loadProperties() {
        Properties properties = new Properties();
        try (InputStream input = new FileInputStream("data/config/config.properties")) {
            properties.load(input);
            DRIVER = properties.getProperty("database.driver", DRIVER);
            DB_HOST = properties.getProperty("database.host", DB_HOST);
            DB_PORT = properties.getProperty("database.port", DB_PORT);
            DB_DATA = properties.getProperty("database.name", DB_DATA);
            DB_USER = properties.getProperty("database.user", DB_USER);
            DB_PASSWORD = properties.getProperty("database.pass", DB_PASSWORD);
            MIN_CONN = Integer.parseInt(properties.getProperty("database.min", String.valueOf(MIN_CONN)));
            MAX_CONN = Integer.parseInt(properties.getProperty("database.max", String.valueOf(MAX_CONN)));
            MAX_LIFE_TIME = Long.parseLong(properties.getProperty("database.lifetime", String.valueOf(MAX_LIFE_TIME)));
            IDLE_TIMEOUT = Long.parseLong(properties.getProperty("database.idleTimeout", String.valueOf(IDLE_TIMEOUT)));
            CONN_TIMEOUT = Long.parseLong(properties.getProperty("database.connectionTimeout", String.valueOf(CONN_TIMEOUT)));
            LEAK_DETECTION_THRESHOLD = Long.parseLong(properties.getProperty("database.leakDetectionThreshold", String.valueOf(LEAK_DETECTION_THRESHOLD)));

            System.out.print("\033[2J\033[H");
            System.out.flush();
            Logger.warning(RED
                    + "  _    _        ____       ____        _    _    \n"
                    + " | |  | |      / __ \\       /\\        | \\ | |  \n"
                    + " | |__| |     | |  | |     /  \\       |  \\| |  \n"
                    + " |  __  |     | |  | |    / /\\ \\      |     |  \n"
                    + " | |  | |     | |__| |   / ____ \\     | \\   | \n"
                    + " |_|  |_|      \\____/   /_/    \\_\\    |_| \\_|\n" + RESET);

        } catch (IOException | NumberFormatException e) {
            Logger.log("[4;31m", "Không thể load file properties!\n");
        }
    }

    // ✅ Execute query helpers
    public static NDVResultSet executeQuery(String query) throws Exception {
        try (Connection connection = getConnectionServer(); PreparedStatement ps = connection.prepareStatement(query)) {
            return new ResultSetImpl(ps.executeQuery());
        } catch (Exception e) {
            Logger.log("[4;31m", "Có lỗi xảy ra khi thực thi câu lệnh: " + query + "\n");
            throw e;
        }
    }

    public static NDVResultSet executeQuery(String query, Object... params) throws Exception {
        try (Connection connection = getConnectionServer(); PreparedStatement ps = connection.prepareStatement(query)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            return new ResultSetImpl(ps.executeQuery());
        } catch (Exception e) {
            Logger.log("[4;31m", "Có lỗi xảy ra khi thực thi câu lệnh: " + query + "\n");
            throw e;
        }
    }

    public static int executeUpdate(String query, Object... params) throws Exception {
        if (query.toLowerCase().startsWith("insert") && query.endsWith("()")) {
            StringBuilder placeholder = new StringBuilder("(");
            for (int i = 0; i < params.length; i++) {
                placeholder.append("?");
                if (i < params.length - 1) {
                    placeholder.append(",");
                }
            }
            placeholder.append(")");
            query = query.replace("()", placeholder.toString());
        }
        try (Connection connection = getConnectionServer(); PreparedStatement ps = connection.prepareStatement(query)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            return ps.executeUpdate();
        } catch (Exception e) {
            Logger.log("[4;31m", "Có lỗi xảy ra khi thực thi câu lệnh: " + query + "\n");
            throw e;
        }
    }

    // ✅ Khởi tạo HikariCP
    static {
        loadProperties();
        config.setDriverClassName(DRIVER);
        config.setJdbcUrl(String.format(URL, DB_HOST, DB_PORT, DB_DATA));
        config.setUsername(DB_USER);
        config.setPassword(DB_PASSWORD);
        config.setMinimumIdle(MIN_CONN);
        config.setMaximumPoolSize(MAX_CONN);
        config.setMaxLifetime(MAX_LIFE_TIME);
        config.setIdleTimeout(IDLE_TIMEOUT);
        config.setConnectionTimeout(CONN_TIMEOUT);

        // ⚙️ Tối ưu hiệu suất
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("useLocalSessionState", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        config.addDataSourceProperty("cacheResultSetMetadata", "true");
        config.addDataSourceProperty("cacheServerConfiguration", "true");
        config.addDataSourceProperty("elideSetAutoCommits", "true");
        config.addDataSourceProperty("maintainTimeStats", "false");
        if (LEAK_DETECTION_THRESHOLD > 0) {
            config.setLeakDetectionThreshold(LEAK_DETECTION_THRESHOLD);
        }

        ds = new HikariDataSource(config);
    }
}
