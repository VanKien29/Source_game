package server;

import utils.Logger;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

/**
 * Anti-DDoS tích hợp cho NRO Game Server (Windows VPS)
 *
 * Giải thích đơn giản:
 *   L3/L4 = chặn IP ở Windows Firewall (OS) → packet xấu không vào được Java
 *   L7    = kiểm tra trong game (kết nối nhiều, spam packet, nhiều nick cùng IP)
 *
 * Khi phát hiện tấn công:
 *   1) Java từ chối kết nối / đóng session (L7)
 *   2) Tạo rule Windows Firewall chặn IP đó (L3/L4)
 *
 * Lưu ý: Server phải chạy bằng quyền Administrator để tạo/xóa firewall rule.
 */
public class AntiDDoS {

    // ── Ngưỡng an toàn (khó ban nhầm người chơi thường) ───────────────────────
    // Chỉ ban khi spam RẤT NHIỀU lần, không ban vì 1 lần vượt nhẹ.
    private static final int  MAX_CONN_PER_IP_PER_SEC = 10;   // 10 kết nối mới/giây mới tính xấu
    private static final int  MAX_SESSION_PER_IP       = 8;   // cho phép quán net / nhiều nick cùng IP
    private static final int  MAX_PKT_PER_SEC          = 150; // đủ cho đánh nhau / skill spam
    private static final int  BAN_THRESHOLD_CONN       = 50;  // phải vi phạm 50 lần mới ban
    private static final int  BAN_THRESHOLD_PKT        = 800; // phải spam rất nặng mới ban
    private static final long BAN_DURATION_MS          = 300_000L; // ban 5 phút
    private static final long CLEANUP_INTERVAL_MS      = 30_000L;  // dọn sớm hơn để đỡ RAM
    private static final int  MAX_TRACKED_IPS          = 1500;     // trần track IP (tránh phình khi DDoS)
    private static final long EVENT_THROTTLE_MS        = 5_000L;   // 1 IP chỉ log spam 1 lần / 5s

    private static final String BANLIST_FILE = "banlist.txt";
    private static final String WHITELIST_FILE = "whitelist.txt";
    private static final String FW_RULE_PREFIX = "NRO_AntiDDoS_";

    private static final AntiDDoS INSTANCE = new AntiDDoS();
    public static AntiDDoS gI() { return INSTANCE; }

    private final ConcurrentHashMap<String, IpRecord> records = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> banList = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> banReasons = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> lastEventAt = new ConcurrentHashMap<>();
    private final Set<String> whitelist = ConcurrentHashMap.newKeySet();
    private final AtomicLong totalBlocked = new AtomicLong(0);
    private final AtomicInteger firewallRules = new AtomicInteger(0);
    private volatile boolean firewallOk = false;

    /** Log sự kiện gần đây (nhỏ để đỡ RAM) */
    private final Deque<String> eventLog = new ConcurrentLinkedDeque<>();
    private static final int MAX_EVENT_LOG = 40;

    private AntiDDoS() {
        loadBanList();
        loadWhitelist();
        // Re-apply firewall rules for persisted bans
        for (Map.Entry<String, Long> e : banList.entrySet()) {
            if (System.currentTimeMillis() < e.getValue()) {
                addFirewallBlock(e.getKey());
            }
        }
        firewallOk = canUseFirewall();
        startCleanupTask();
        Logger.warning("[AntiDDoS] Started | L7=ON | L3/L4 WindowsFirewall=" + (firewallOk ? "ON" : "OFF (cần chạy Admin)\n"));
    }

    // ── PUBLIC API ────────────────────────────────────────────────────────────

    public boolean allowConnection(String ip) {
        if (whitelist.contains(ip)) return true;
        if (isBanned(ip)) {
            totalBlocked.incrementAndGet();
            return false; // đã ban → không tạo record mới (tiết kiệm RAM khi DDoS)
        }

        IpRecord r = getRecord(ip);
        if (r == null) {
            totalBlocked.incrementAndGet();
            return false; // quá tải track → từ chối tạm
        }
        r.onNewConnection();
        long now = System.currentTimeMillis();

        if (r.connInWindow(now) > MAX_CONN_PER_IP_PER_SEC) {
            r.connViolations.incrementAndGet();
            addEventThrottled(ip, "SPAM CONN " + ip + " (" + r.connViolations.get() + ")");
            if (r.connViolations.get() >= BAN_THRESHOLD_CONN) {
                ban(ip, "Connection flood");
            }
            totalBlocked.incrementAndGet();
            return false;
        }

        if (r.activeSessions.get() >= MAX_SESSION_PER_IP) {
            addEventThrottled(ip, "SPAM SESSION " + ip + " (sess=" + r.activeSessions.get() + ")");
            totalBlocked.incrementAndGet();
            return false;
        }

        r.activeSessions.incrementAndGet();
        return true;
    }

    public void onSessionClosed(String ip) {
        IpRecord r = records.get(ip);
        if (r != null && r.activeSessions.get() > 0) {
            r.activeSessions.decrementAndGet();
        }
    }

    public boolean allowPacket(String ip) {
        if (whitelist.contains(ip)) return true;
        if (isBanned(ip)) {
            totalBlocked.incrementAndGet();
            return false;
        }

        IpRecord r = getRecord(ip);
        if (r == null) return false;
        long now = System.currentTimeMillis();
        r.onPacket(now);

        if (r.pktInWindow(now) > MAX_PKT_PER_SEC) {
            r.pktViolations.incrementAndGet();
            addEventThrottled(ip, "SPAM PACKET " + ip + " (" + r.pktViolations.get() + ")");
            if (r.pktViolations.get() >= BAN_THRESHOLD_PKT) {
                ban(ip, "Packet flood");
            }
            totalBlocked.incrementAndGet();
            return false;
        }
        return true;
    }

    public boolean isBanned(String ip) {
        Long until = banList.get(ip);
        if (until == null) return false;
        if (System.currentTimeMillis() > until) {
            unban(ip);
            return false;
        }
        return true;
    }

    /** Ban IP: L7 (Java) + L3/L4 (Windows Firewall) */
    public void ban(String ip, String reason) {
        if (whitelist.contains(ip)) return;
        if (ip == null || ip.isEmpty() || "127.0.0.1".equals(ip) || "::1".equals(ip)) return;

        long until = System.currentTimeMillis() + BAN_DURATION_MS;
        boolean first = banList.put(ip, until) == null;
        banReasons.put(ip, reason == null ? "unknown" : reason);
        saveBanList();
        if (first) {
            addFirewallBlock(ip);
            addEvent("BAN " + ip + " | " + reason);
            records.remove(ip); // bỏ track sau khi ban → giảm RAM
            lastEventAt.remove(ip);
        }
        Logger.warning("[AntiDDoS] BAN " + ip + " | " + reason + " | until " + new Date(until));
    }

    public void unban(String ip) {
        if (banList.remove(ip) != null) {
            banReasons.remove(ip);
            lastEventAt.remove(ip);
            removeFirewallBlock(ip);
            saveBanList();
            addEvent("UNBAN " + ip);
            Logger.warning("[AntiDDoS] UNBAN " + ip);
        }
    }

    public void addWhitelist(String ip) {
        if (ip == null || ip.isEmpty()) return;
        whitelist.add(ip.trim());
        unban(ip.trim());
        saveWhitelist();
    }

    public long getTotalBlocked() { return totalBlocked.get(); }
    public int getBanCount() { return banList.size(); }
    public int getTrackCount() { return records.size(); }
    public int getFirewallRuleCount() { return firewallRules.get(); }
    public boolean isFirewallOk() { return firewallOk; }

    /** Clear hết ban + firewall rules do AntiDDoS tạo */
    public void clearFirewall() {
        Set<String> ips = new HashSet<>(banList.keySet());
        for (String ip : ips) {
            removeFirewallBlock(ip);
        }
        banList.clear();
        banReasons.clear();
        records.clear();
        totalBlocked.set(0);
        saveBanList();
        // Dọn rule còn sót theo prefix
        clearAllFirewallRulesByPrefix();
        firewallRules.set(0);
        firewallOk = canUseFirewall();
        addEvent("CLEAR FIREWALL (admin)");
        Logger.warning("[AntiDDoS] Cleared all bans + Windows Firewall rules");
    }

    public String getPanelStatusText() {
        return "AntiDDoS L3/L4:" + (firewallOk ? "ON" : "OFF")
                + " L7:ON"
                + " Ban:" + getBanCount()
                + " FW:" + getFirewallRuleCount()
                + " Block:" + getTotalBlocked();
    }

    /** Danh sách IP đang bị ban: "ip | lý do | còn Xs" */
    public List<String> getBannedIpLines() {
        long now = System.currentTimeMillis();
        List<String> list = new ArrayList<>();
        for (Map.Entry<String, Long> e : banList.entrySet()) {
            String ip = e.getKey();
            long leftSec = Math.max(0, (e.getValue() - now) / 1000L);
            String reason = banReasons.getOrDefault(ip, "?");
            list.add(ip + " | " + reason + " | còn " + leftSec + "s");
        }
        list.sort(String::compareTo);
        return list;
    }

    /** IP đang bị track (spam nhẹ / đang nghi ngờ) */
    public List<String> getSuspectIpLines() {
        List<String> list = new ArrayList<>();
        for (Map.Entry<String, IpRecord> e : records.entrySet()) {
            IpRecord r = e.getValue();
            int connV = r.connViolations.get();
            int pktV = r.pktViolations.get();
            int sess = r.activeSessions.get();
            if (connV > 0 || pktV > 0 || sess > 1) {
                list.add(e.getKey()
                        + " | sess=" + sess
                        + " | spamConn=" + connV
                        + " | spamPkt=" + pktV);
            }
        }
        list.sort(String::compareTo);
        return list;
    }

    /** Log sự kiện mới nhất (mới → cũ) */
    public List<String> getRecentEvents(int limit) {
        List<String> list = new ArrayList<>();
        int max = Math.min(limit, MAX_EVENT_LOG);
        for (String s : eventLog) {
            list.add(s);
            if (list.size() >= max) break;
        }
        return list;
    }

    private void addEventThrottled(String ip, String msg) {
        long now = System.currentTimeMillis();
        Long last = lastEventAt.get(ip);
        if (last != null && now - last < EVENT_THROTTLE_MS) return;
        lastEventAt.put(ip, now);
        addEvent(msg);
    }

    private void addEvent(String msg) {
        String line = new java.text.SimpleDateFormat("HH:mm:ss").format(new Date()) + " " + msg;
        eventLog.addFirst(line);
        while (eventLog.size() > MAX_EVENT_LOG) {
            eventLog.pollLast();
        }
    }

    // ── Windows Firewall (L3/L4) ──────────────────────────────────────────────

    private String ruleName(String ip) {
        return FW_RULE_PREFIX + ip.replace(":", "_");
    }

    private boolean canUseFirewall() {
        try {
            Process p = Runtime.getRuntime().exec(new String[]{
                "cmd", "/c", "netsh advfirewall show currentprofile | findstr /I \"State\""
            });
            int code = p.waitFor();
            return code == 0;
        } catch (Exception e) {
            return false;
        }
    }

    private void addFirewallBlock(String ip) {
        if (!isValidIp(ip)) return;
        try {
            String name = ruleName(ip);
            // Xóa rule cũ nếu có rồi tạo mới
            runCmdSilent("netsh advfirewall firewall delete rule name=\"" + name + "\"");
            int code = runCmd("netsh advfirewall firewall add rule name=\"" + name
                    + "\" dir=in action=block remoteip=" + ip
                    + " enable=yes profile=any");
            if (code == 0) {
                firewallRules.incrementAndGet();
                firewallOk = true;
                Logger.warning("[AntiDDoS] Firewall BLOCK " + ip);
            } else {
                firewallOk = false;
                Logger.warning("[AntiDDoS] Không tạo được firewall rule cho " + ip
                        + " (hãy chạy server bằng Administrator)");
            }
        } catch (Exception e) {
            firewallOk = false;
            Logger.warning("[AntiDDoS] Firewall error: " + e.getMessage());
        }
    }

    private void removeFirewallBlock(String ip) {
        if (!isValidIp(ip)) return;
        try {
            int code = runCmd("netsh advfirewall firewall delete rule name=\"" + ruleName(ip) + "\"");
            if (code == 0 && firewallRules.get() > 0) {
                firewallRules.decrementAndGet();
            }
        } catch (Exception ignored) {
        }
    }

    private void clearAllFirewallRulesByPrefix() {
        try {
            // Liệt kê rule rồi xóa những rule có prefix NRO_AntiDDoS_
            Process p = Runtime.getRuntime().exec(new String[]{
                "cmd", "/c",
                "netsh advfirewall firewall show rule name=all | findstr /I \"" + FW_RULE_PREFIX + "\""
            });
            try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (line.toLowerCase().startsWith("rule name:") || line.toLowerCase().startsWith("tên quy tắc:")) {
                        int idx = line.indexOf(':');
                        if (idx >= 0) {
                            String name = line.substring(idx + 1).trim();
                            if (name.startsWith(FW_RULE_PREFIX)) {
                                runCmdSilent("netsh advfirewall firewall delete rule name=\"" + name + "\"");
                            }
                        }
                    }
                }
            }
            p.waitFor();
        } catch (Exception ignored) {
        }
    }

    private int runCmd(String cmd) throws Exception {
        Process p = Runtime.getRuntime().exec(new String[]{"cmd", "/c", cmd});
        // Drain streams để process không treo
        try (BufferedReader out = new BufferedReader(new InputStreamReader(p.getInputStream()));
             BufferedReader err = new BufferedReader(new InputStreamReader(p.getErrorStream()))) {
            while (out.readLine() != null) { }
            while (err.readLine() != null) { }
        }
        return p.waitFor();
    }

    private void runCmdSilent(String cmd) {
        try { runCmd(cmd); } catch (Exception ignored) { }
    }

    private boolean isValidIp(String ip) {
        if (ip == null) return false;
        // IPv4 đơn giản
        return ip.matches("\\d{1,3}(\\.\\d{1,3}){3}");
    }

    // ── INTERNAL ──────────────────────────────────────────────────────────────

    private IpRecord getRecord(String ip) {
        IpRecord exist = records.get(ip);
        if (exist != null) return exist;
        // Trần track IP: khi DDoS nhiều IP lạ sẽ không phình vô hạn
        if (records.size() >= MAX_TRACKED_IPS) {
            pruneOldestRecords(200);
            if (records.size() >= MAX_TRACKED_IPS) return null;
        }
        return records.computeIfAbsent(ip, k -> new IpRecord());
    }

    private void pruneOldestRecords(int removeCount) {
        List<Map.Entry<String, IpRecord>> list = new ArrayList<>(records.entrySet());
        list.sort(Comparator.comparingLong(e -> e.getValue().lastSeen));
        int n = Math.min(removeCount, list.size());
        for (int i = 0; i < n; i++) {
            Map.Entry<String, IpRecord> e = list.get(i);
            if (e.getValue().activeSessions.get() == 0) {
                records.remove(e.getKey());
                lastEventAt.remove(e.getKey());
            }
        }
    }

    private void startCleanupTask() {
        Thread t = new Thread(() -> {
            while (true) {
                try { Thread.sleep(CLEANUP_INTERVAL_MS); } catch (InterruptedException ignored) { }
                cleanup();
            }
        }, "AntiDDoS-Cleanup");
        t.setDaemon(true);
        t.start();
    }

    private void cleanup() {
        long now = System.currentTimeMillis();
        long stale = now - 60_000L; // IP không hoạt động > 60s → xóa track
        records.entrySet().removeIf(e ->
                e.getValue().lastSeen < stale && e.getValue().activeSessions.get() == 0);
        lastEventAt.entrySet().removeIf(e -> now - e.getValue() > 120_000L);

        if (records.size() > MAX_TRACKED_IPS) {
            pruneOldestRecords(records.size() - MAX_TRACKED_IPS + 100);
        }

        List<String> expired = new ArrayList<>();
        for (Map.Entry<String, Long> e : banList.entrySet()) {
            if (now > e.getValue()) expired.add(e.getKey());
        }
        for (String ip : expired) {
            unban(ip);
        }
        // Không gọi canUseFirewall mỗi vòng (netsh nặng) — chỉ giữ trạng thái cũ
    }

    private void loadBanList() {
        try (BufferedReader br = new BufferedReader(new FileReader(BANLIST_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    try {
                        long until = Long.parseLong(parts[1].trim());
                        if (System.currentTimeMillis() < until) {
                            banList.put(parts[0].trim(), until);
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        } catch (IOException ignored) {
        }
    }

    private synchronized void saveBanList() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(BANLIST_FILE))) {
            banList.forEach((ip, until) -> pw.println(ip + "," + until));
        } catch (IOException ignored) {
        }
    }

    private void loadWhitelist() {
        try (BufferedReader br = new BufferedReader(new FileReader(WHITELIST_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String ip = line.trim();
                if (!ip.isEmpty()) whitelist.add(ip);
            }
        } catch (IOException ignored) {
        }
        whitelist.add("127.0.0.1");
        whitelist.add("::1");
    }

    private void saveWhitelist() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(WHITELIST_FILE))) {
            whitelist.forEach(pw::println);
        } catch (IOException ignored) {
        }
    }

    private static class IpRecord {
        final AtomicInteger activeSessions = new AtomicInteger(0);
        final AtomicInteger connViolations = new AtomicInteger(0);
        final AtomicInteger pktViolations = new AtomicInteger(0);
        volatile long lastSeen = System.currentTimeMillis();
        private final long[] connTimes = new long[32];
        private final long[] pktTimes = new long[128];
        private int connIdx = 0, pktIdx = 0;

        synchronized void onNewConnection() {
            long now = System.currentTimeMillis();
            connTimes[connIdx % connTimes.length] = now;
            connIdx++;
            lastSeen = now;
        }

        synchronized void onPacket(long now) {
            pktTimes[pktIdx % pktTimes.length] = now;
            pktIdx++;
            lastSeen = now;
        }

        synchronized int connInWindow(long now) {
            long window = now - 1000L;
            int count = 0;
            for (long t : connTimes) if (t > window) count++;
            return count;
        }

        synchronized int pktInWindow(long now) {
            long window = now - 1000L;
            int count = 0;
            for (long t : pktTimes) if (t > window) count++;
            return count;
        }
    }
}
