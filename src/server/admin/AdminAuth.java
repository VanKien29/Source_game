package server.admin;

import com.sun.net.httpserver.HttpExchange;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class AdminAuth {

    private final AdminRuntimeConfig config;
    private final Map<String, Long> usedNonces = new ConcurrentHashMap<>();

    public AdminAuth(AdminRuntimeConfig config) {
        this.config = config;
    }

    public AdminResponse verify(HttpExchange exchange, String body) {
        String ip = exchange.getRemoteAddress().getAddress().getHostAddress();
        if (!config.allowedIps.contains(ip)) {
            return AdminResponse.fail(403, "IP_DENIED", "IP khong duoc phep goi runtime API");
        }

        String key = header(exchange, "X-Game-Admin-Key");
        String timestamp = header(exchange, "X-Game-Admin-Timestamp");
        String nonce = header(exchange, "X-Game-Admin-Nonce");
        String signature = header(exchange, "X-Game-Admin-Signature");

        if (!constantEquals(config.key, key)) {
            return AdminResponse.fail(401, "BAD_KEY", "Runtime key khong hop le");
        }
        if (timestamp == null || nonce == null || signature == null || nonce.length() < 8 || nonce.length() > 128) {
            return AdminResponse.fail(401, "MISSING_AUTH", "Thieu thong tin xac thuc runtime");
        }

        long ts;
        try {
            ts = Long.parseLong(timestamp);
            if (ts < 1000000000000L) {
                ts *= 1000L;
            }
        } catch (Exception e) {
            return AdminResponse.fail(401, "BAD_TIMESTAMP", "Timestamp khong hop le");
        }

        long now = System.currentTimeMillis();
        cleanupNonces(now);
        if (Math.abs(now - ts) > config.allowedSkewMillis) {
            return AdminResponse.fail(401, "STALE_REQUEST", "Request runtime da het hieu luc");
        }
        Long previous = usedNonces.putIfAbsent(nonce, now);
        if (previous != null) {
            return AdminResponse.fail(409, "REPLAY_REQUEST", "Request runtime bi lap lai");
        }

        String canonical = exchange.getRequestMethod().toUpperCase()
                + "\n" + exchange.getRequestURI().getPath()
                + "\n" + timestamp
                + "\n" + nonce
                + "\n" + body;
        String expected = hmacSha256Hex(config.secret, canonical);
        if (!constantEquals(expected, signature)) {
            return AdminResponse.fail(401, "BAD_SIGNATURE", "Chu ky runtime khong hop le");
        }
        return null;
    }

    private void cleanupNonces(long now) {
        Iterator<Map.Entry<String, Long>> iterator = usedNonces.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Long> entry = iterator.next();
            if (now - entry.getValue() > config.allowedSkewMillis * 2) {
                iterator.remove();
            }
        }
    }

    private static String header(HttpExchange exchange, String name) {
        String value = exchange.getRequestHeaders().getFirst(name);
        return value == null ? null : value.trim();
    }

    private static String hmacSha256Hex(String secret, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                builder.append(String.format("%02x", b & 0xff));
            }
            return builder.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean constantEquals(String left, String right) {
        if (left == null || right == null) {
            return false;
        }
        return MessageDigest.isEqual(left.getBytes(StandardCharsets.UTF_8), right.getBytes(StandardCharsets.UTF_8));
    }
}
