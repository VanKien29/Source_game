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

public class NDVDB {

    // ⚙️ Thông tin cơ bản
    private static String DRIVER = "com.mysql.cj.jdbc.Driver"; // ✅ driver mới
    private static final String URL =
            "jdbc:mysql://%s:%s/%s?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true&useSSL=false&allowPublicKeyRetrieval=true";

    private static String DB_HOST = "localhost";
    private static String DB_PORT = "3306";
    private static String DB_SERVER = "ngocrong_user";
    public static String DB_DATA = "ngocrong_data";
    public static String DB_USER = "root";
    private static String DB_PASSWORD = "";

    // ⚙️ Hikari config
    private static int MIN_CONN = 3;
    private static int MAX_CONN = 15;
    private static long MAX_LIFE_TIME = 1800000L; // 30 phút
    private static long IDLE_TIMEOUT = 600000L;   // 10 phút
    private static long CONN_TIMEOUT = 30000L;    // 30 giây

    private static final HikariConfig config = new HikariConfig();
    private static final HikariDataSource ds;

    private static final HikariConfig config2 = new HikariConfig();
    private static final HikariDataSource ds2;

    public static Connection getConnectionServer() throws SQLException {
        return ds.getConnection();
    }

    public static Connection getConnectionDATA() throws SQLException {
        return ds2.getConnection();
    }

    public static void close() {
        if (ds != null && !ds.isClosed()) ds.close();
        if (ds2 != null && !ds2.isClosed()) ds2.close();
    }

    private static void loadProperties() {
        Properties properties = new Properties();
        try (InputStream input = new FileInputStream("data/config/config.properties")) {
            properties.load(input);
            DRIVER = properties.getProperty("database.driver", DRIVER);
            DB_HOST = properties.getProperty("database.host", DB_HOST);
            DB_PORT = properties.getProperty("database.port", DB_PORT);
            DB_SERVER = properties.getProperty("database.server", DB_SERVER);
            DB_DATA = properties.getProperty("database.data", DB_DATA);
            DB_USER = properties.getProperty("database.user", DB_USER);
            DB_PASSWORD = properties.getProperty("database.pass", DB_PASSWORD);
            MIN_CONN = Integer.parseInt(properties.getProperty("database.min", String.valueOf(MIN_CONN)));
            MAX_CONN = Integer.parseInt(properties.getProperty("database.max", String.valueOf(MAX_CONN)));
            MAX_LIFE_TIME = Long.parseLong(properties.getProperty("database.lifetime", String.valueOf(MAX_LIFE_TIME)));

            Logger.log("\u001B[32m", "[NDVDB] Config loaded successfully!\n");
        } catch (IOException | NumberFormatException e) {
            Logger.log("\u001B[31m", "Không thể load file properties!\n");
        }
    }

    public static NDVResultSet executeQuery(String query) throws Exception {
        try (Connection connection = getConnectionServer();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            return new ResultSetImpl(preparedStatement.executeQuery());
        } catch (Exception e) {
            try (Connection connection = getConnectionDATA();
                 PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                return new ResultSetImpl(preparedStatement.executeQuery());
            } catch (Exception ex) {
                Logger.log("\u001B[31m", "Có lỗi xảy ra khi thực thi câu lệnh: " + query + "\n");
                throw e;
            }
        }
    }

    public static NDVResultSet executeQuery(String query, Object... params) throws Exception {
        try (Connection connection = getConnectionServer();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            for (int i = 0; i < params.length; i++) preparedStatement.setObject(i + 1, params[i]);
            return new ResultSetImpl(preparedStatement.executeQuery());
        } catch (Exception e) {
            try (Connection connection = getConnectionDATA();
                 PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                for (int i = 0; i < params.length; i++) preparedStatement.setObject(i + 1, params[i]);
                return new ResultSetImpl(preparedStatement.executeQuery());
            } catch (Exception ex) {
                Logger.log("\u001B[31m", "Có lỗi xảy ra khi thực thi câu lệnh: " + query + "\n");
                throw e;
            }
        }
    }

    public static int executeUpdate(String query, Object... params) throws Exception {
        if (query.toLowerCase().startsWith("insert") && query.endsWith("()")) {
            StringBuilder placeholder = new StringBuilder("(");
            for (int i = 0; i < params.length; i++) {
                placeholder.append("?");
                if (i < params.length - 1) placeholder.append(",");
            }
            placeholder.append(")");
            query = query.replace("()", placeholder.toString());
        }
        try (Connection connection = getConnectionServer();
             PreparedStatement ps = connection.prepareStatement(query)) {
            for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
            return ps.executeUpdate();
        } catch (Exception e) {
            try (Connection connection = getConnectionDATA();
                 PreparedStatement ps = connection.prepareStatement(query)) {
                for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
                return ps.executeUpdate();
            } catch (Exception ex) {
                Logger.log("\u001B[31m", "Có lỗi xảy ra khi thực thi câu lệnh: " + query + "\n");
                throw e;
            }
        }
    }

    static {
        loadProperties();

        // 🧩 Cấu hình DB 1
        config.setDriverClassName(DRIVER);
        config.setJdbcUrl(String.format(URL, DB_HOST, DB_PORT, DB_SERVER));
        config.setUsername(DB_USER);
        config.setPassword(DB_PASSWORD);
        config.setMinimumIdle(MIN_CONN);
        config.setMaximumPoolSize(MAX_CONN);
        config.setMaxLifetime(MAX_LIFE_TIME);
        config.setIdleTimeout(IDLE_TIMEOUT);
        config.setConnectionTimeout(CONN_TIMEOUT);
        config.setLeakDetectionThreshold(10000);
        addCommonProperties(config);
        ds = new HikariDataSource(config);

        // 🧩 Cấu hình DB 2
        config2.setDriverClassName(DRIVER);
        config2.setJdbcUrl(String.format(URL, DB_HOST, DB_PORT, DB_DATA));
        config2.setUsername(DB_USER);
        config2.setPassword(DB_PASSWORD);
        config2.setMinimumIdle(MIN_CONN);
        config2.setMaximumPoolSize(MAX_CONN);
        config2.setMaxLifetime(MAX_LIFE_TIME);
        config2.setIdleTimeout(IDLE_TIMEOUT);
        config2.setConnectionTimeout(CONN_TIMEOUT);
        config2.setLeakDetectionThreshold(10000);
        addCommonProperties(config2);
        ds2 = new HikariDataSource(config2);

        System.out.println("[HikariCP] NDVDB connected: user=" + DB_SERVER + " data=" + DB_DATA + " Pool=" + MAX_CONN);
    }

    private static void addCommonProperties(HikariConfig cfg) {
        cfg.addDataSourceProperty("cachePrepStmts", "true");
        cfg.addDataSourceProperty("prepStmtCacheSize", "250");
        cfg.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        cfg.addDataSourceProperty("useServerPrepStmts", "true");
        cfg.addDataSourceProperty("useLocalSessionState", "true");
        cfg.addDataSourceProperty("rewriteBatchedStatements", "true");
        cfg.addDataSourceProperty("cacheResultSetMetadata", "true");
        cfg.addDataSourceProperty("cacheServerConfiguration", "true");
        cfg.addDataSourceProperty("elideSetAutoCommits", "true");
        cfg.addDataSourceProperty("maintainTimeStats", "false");
    }
}
