package server.admin;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import boss.BossManager;
import data.DataGame;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.Executors;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import models.Template.ItemTemplate;
import server.Manager;

public class AdminHttpServer {

    private static AdminHttpServer instance;
    private HttpServer server;
    private AdminRuntimeConfig config;
    private AdminAuth auth;

    public static synchronized AdminHttpServer gI() {
        if (instance == null) {
            instance = new AdminHttpServer();
        }
        return instance;
    }

    public synchronized void start() {
        if (server != null) {
            return;
        }
        config = AdminRuntimeConfig.load();
        if (!config.canStart()) {
            System.err.println("[AdminRuntime] Disabled. Configure secret with at least 32 characters.");
            return;
        }
        try {
            auth = new AdminAuth(config);
            server = HttpServer.create(new InetSocketAddress(config.host, config.port), 0);
            server.createContext("/internal/runtime/health", this::handleHealth);
            server.createContext("/internal/runtime/items/refresh", this::handleRefreshItem);
            server.createContext("/internal/runtime/shop/reload", this::handleReloadShop);
            server.createContext("/internal/runtime/bosses", this::handleBossesList);
            server.createContext("/internal/runtime/bosses/create", this::handleBossCreate);
            server.createContext("/internal/runtime/bosses/action", this::handleBossAction);
            server.createContext("/internal/runtime/bosses/update", this::handleBossUpdate);
            server.createContext("/internal/runtime/bosses/configs", this::handleBossConfigs);
            server.createContext("/internal/runtime/map-mobs", this::handleMapMobs);
            server.createContext("/internal/runtime/buffs/mail", this::handleBuffMail);
            server.createContext("/internal/runtime/buffs/account", this::handleBuffAccount);
            server.createContext("/internal/runtime/player/inventory", this::handlePlayerInventory);
            server.setExecutor(Executors.newFixedThreadPool(2));
            server.start();
            System.out.println("[AdminRuntime] Listening on http://" + config.host + ":" + config.port);
        } catch (Exception e) {
            server = null;
            e.printStackTrace();
        }
    }

    private void handleHealth(HttpExchange exchange) throws IOException {
        handle(exchange, "GET", () -> AdminResponse.ok("RUNTIME_OK", "Game runtime API đang hoạt động"));
    }

    private void handleRefreshItem(HttpExchange exchange) throws IOException {
        handle(exchange, "POST", () -> RuntimeCommandExecutor.gI().run("item.refresh", config.commandTimeoutMillis, () -> {
            JSONObject body = parseBody(readBodySafely(exchange));
            int itemId = intValue(body.get("item_id"), -1);
            ItemTemplate item = Manager.runtimeReloadItemTemplate(itemId);
            if (item == null) {
                return AdminResponse.fail(404, "ITEM_REFRESH_FAILED", "Không tìm thấy item để reload");
            }
            int itemVersion = DataGame.bumpItemVersion();
            int onlinePlayers = DataGame.refreshIcon(item.iconID);
            String dataJson = "{\"item_id\":" + item.id
                    + ",\"icon_id\":" + item.iconID
                    + ",\"item_version\":" + itemVersion
                    + ",\"online_players\":" + onlinePlayers + "}";
            return AdminResponse.ok("ITEM_REFRESHED", "Đã reload item và làm mới icon trong game", dataJson);
        }));
    }

    private void handleReloadShop(HttpExchange exchange) throws IOException {
        handle(exchange, "POST", () -> RuntimeCommandExecutor.gI().run("shop.reload", config.commandTimeoutMillis, () -> {
            boolean ok = Manager.gI().updateShop();
            return ok
                    ? AdminResponse.ok("SHOP_RELOADED", "Reload shop thành công")
                    : AdminResponse.fail(500, "SHOP_RELOAD_FAILED", "Reload shop thất bại");
        }));
    }

    private void handleBossesList(HttpExchange exchange) throws IOException {
        handle(exchange, "GET", () -> AdminResponse.ok("BOSSES_LISTED", "Lấy danh sách boss thành công", BossManager.runtimeBossesJson()));
    }

    private void handleBossCreate(HttpExchange exchange) throws IOException {
        handle(exchange, "POST", () -> RuntimeCommandExecutor.gI().run("boss.create", config.commandTimeoutMillis, () -> {
            JSONObject body = parseBody(readBodySafely(exchange));
            int bossId = intValue(body.get("boss_id"), 0);
            int count = intValue(body.get("count"), 1);
            if (bossId == 0) {
                return AdminResponse.fail(422, "BOSS_ID_REQUIRED", "Thiếu boss_id");
            }
            String dataJson = body.containsKey("name") || body.containsKey("outfit") || body.containsKey("group_members") || body.containsKey("custom")
                    ? BossManager.runtimeCreateCustomBoss(body)
                    : BossManager.runtimeCreateBoss(bossId, count);
            return AdminResponse.ok("BOSS_CREATED", "Tạo boss runtime thành công", dataJson);
        }));
    }

    private void handleBossAction(HttpExchange exchange) throws IOException {
        handle(exchange, "POST", () -> RuntimeCommandExecutor.gI().run("boss.action", config.commandTimeoutMillis, () -> {
            JSONObject body = parseBody(readBodySafely(exchange));
            String manager = stringValue(body.get("manager"));
            int index = intValue(body.get("index"), -1);
            String action = stringValue(body.get("action"));
            boolean group = "group".equalsIgnoreCase(stringValue(body.get("scope")));
            boolean ok = switch (action) {
                case "enable" -> group ? BossManager.runtimeSetGroupEnabled(manager, index, true) : BossManager.runtimeSetEnabled(manager, index, true);
                case "disable" -> group ? BossManager.runtimeSetGroupEnabled(manager, index, false) : BossManager.runtimeSetEnabled(manager, index, false);
                case "delete" -> group ? BossManager.runtimeDeleteGroup(manager, index) : BossManager.runtimeDeleteBoss(manager, index);
                case "respawn" -> group ? BossManager.runtimeRespawnGroup(manager, index) : BossManager.runtimeRespawnBoss(manager, index);
                default -> false;
            };
            return ok
                    ? AdminResponse.ok("BOSS_ACTION_OK", "Thực hiện lệnh boss thành công")
                    : AdminResponse.fail(404, "BOSS_ACTION_FAILED", "Không tìm thấy boss hoặc action không hợp lệ");
        }));
    }

    private void handleBossUpdate(HttpExchange exchange) throws IOException {
        handle(exchange, "POST", () -> RuntimeCommandExecutor.gI().run("boss.update", config.commandTimeoutMillis, () -> {
            JSONObject body = parseBody(readBodySafely(exchange));
            boolean ok = BossManager.runtimeUpdateBoss(body);
            return ok
                    ? AdminResponse.ok("BOSS_UPDATED", "Cập nhật boss runtime thành công")
                    : AdminResponse.fail(404, "BOSS_UPDATE_FAILED", "Không tìm thấy boss");
        }));
    }

    private void handleBossConfigs(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        if ("GET".equalsIgnoreCase(method)) {
            handle(exchange, "GET", () -> AdminResponse.ok("BOSS_CONFIGS_LISTED", "Lấy cấu hình boss thành công", BossManager.runtimeConfigsJson()));
            return;
        }
        handle(exchange, "POST", () -> RuntimeCommandExecutor.gI().run("boss.config.save", config.commandTimeoutMillis, () -> {
            JSONObject body = parseBody(readBodySafely(exchange));
            String dataJson = BossManager.runtimeSaveConfig(body);
            JSONObject result = parseBody(dataJson);
            boolean saved = Boolean.parseBoolean(String.valueOf(result.get("saved")));
            return saved
                    ? AdminResponse.ok("BOSS_CONFIG_SAVED", "Lưu cấu hình boss thành công", dataJson)
                    : AdminResponse.fail(422, "BOSS_CONFIG_SAVE_FAILED", stringValue(result.get("message")));
        }));
    }

    private void handleMapMobs(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        if ("GET".equalsIgnoreCase(method)) {
            handle(exchange, "GET", () -> AdminResponse.ok("MAP_MOBS_LISTED", "Lấy danh sách map mob thành công", Manager.runtimeMapMobsJson()));
            return;
        }
        handle(exchange, "POST", () -> RuntimeCommandExecutor.gI().run("map_mob.save", config.commandTimeoutMillis, () -> {
            JSONObject body = parseBody(readBodySafely(exchange));
            String dataJson = Manager.runtimeSaveMapMobs(body);
            JSONObject result = parseBody(dataJson);
            boolean saved = Boolean.parseBoolean(String.valueOf(result.get("saved")));
            return saved
                    ? AdminResponse.ok("MAP_MOBS_SAVED", "Lưu và reload mob thành công", dataJson)
                    : AdminResponse.fail(422, "MAP_MOBS_SAVE_FAILED", stringValue(result.get("message")));
        }));
    }

    private void handleBuffMail(HttpExchange exchange) throws IOException {
        handle(exchange, "POST", () -> RuntimeCommandExecutor.gI().run("buff.mail", config.commandTimeoutMillis, () -> {
            JSONObject body = parseBody(readBodySafely(exchange));
            String dataJson = AdminBuffService.buffMail(body);
            JSONObject result = parseBody(dataJson);
            boolean saved = Boolean.parseBoolean(String.valueOf(result.get("ok")));
            return saved
                    ? AdminResponse.ok("BUFF_MAIL_OK", stringValue(result.get("message")), dataJson)
                    : AdminResponse.fail(422, stringValue(result.get("code")), stringValue(result.get("message")));
        }));
    }

    private void handleBuffAccount(HttpExchange exchange) throws IOException {
        handle(exchange, "POST", () -> RuntimeCommandExecutor.gI().run("buff.account", config.commandTimeoutMillis, () -> {
            JSONObject body = parseBody(readBodySafely(exchange));
            String dataJson = AdminBuffService.buffAccount(body);
            JSONObject result = parseBody(dataJson);
            boolean saved = Boolean.parseBoolean(String.valueOf(result.get("ok")));
            return saved
                    ? AdminResponse.ok("BUFF_ACCOUNT_OK", stringValue(result.get("message")), dataJson)
                    : AdminResponse.fail(422, stringValue(result.get("code")), stringValue(result.get("message")));
        }));
    }

    private void handlePlayerInventory(HttpExchange exchange) throws IOException {
        handle(exchange, "POST", () -> RuntimeCommandExecutor.gI().run("player.inventory", config.commandTimeoutMillis, () -> {
            JSONObject body = parseBody(readBodySafely(exchange));
            String dataJson = AdminPlayerService.syncInventory(body);
            JSONObject result = parseBody(dataJson);
            boolean saved = Boolean.parseBoolean(String.valueOf(result.get("ok")));
            return saved
                    ? AdminResponse.ok("PLAYER_INVENTORY_OK", stringValue(result.get("message")), dataJson)
                    : AdminResponse.fail(422, stringValue(result.get("code")), stringValue(result.get("message")));
        }));
    }

    private void handle(HttpExchange exchange, String method, CommandSupplier supplier) throws IOException {
        String requestId = UUID.randomUUID().toString();
        String body = readBody(exchange);
        exchange.setAttribute("runtimeBody", body);

        if (!method.equalsIgnoreCase(exchange.getRequestMethod())) {
            send(exchange, AdminResponse.fail(405, "METHOD_NOT_ALLOWED", "Sai HTTP method"), requestId);
            return;
        }

        AdminResponse authError = auth.verify(exchange, body);
        if (authError != null) {
            send(exchange, authError, requestId);
            return;
        }

        long startedAt = System.currentTimeMillis();
        AdminResponse response = supplier.get();
        long elapsed = System.currentTimeMillis() - startedAt;
        System.out.println("[AdminRuntime] " + exchange.getRequestURI().getPath() + " " + response.code + " " + elapsed + "ms request_id=" + requestId);
        send(exchange, response, requestId);
    }

    private static String readBody(HttpExchange exchange) throws IOException {
        try (InputStream input = exchange.getRequestBody(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int read;
            while ((read = input.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
            return output.toString(StandardCharsets.UTF_8.name());
        }
    }

    private static String readBodySafely(HttpExchange exchange) {
        Object body = exchange.getAttribute("runtimeBody");
        return body == null ? "" : String.valueOf(body);
    }

    private static JSONObject parseBody(String body) {
        Object parsed = JSONValue.parse(body == null || body.isBlank() ? "{}" : body);
        return parsed instanceof JSONObject ? (JSONObject) parsed : new JSONObject();
    }

    private static String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private static int intValue(Object value, int fallback) {
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (Exception e) {
            return fallback;
        }
    }

    private static void send(HttpExchange exchange, AdminResponse response, String requestId) throws IOException {
        byte[] bytes = response.toJson(requestId).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.getResponseHeaders().set("X-Request-Id", requestId);
        exchange.sendResponseHeaders(response.status, bytes.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }

    private interface CommandSupplier {
        AdminResponse get();
    }
}
