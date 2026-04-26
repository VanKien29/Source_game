package server.admin;

public class AdminResponse {

    public final int status;
    public final boolean ok;
    public final String code;
    public final String message;
    public final String dataJson;

    public AdminResponse(int status, boolean ok, String code, String message) {
        this(status, ok, code, message, null);
    }

    public AdminResponse(int status, boolean ok, String code, String message, String dataJson) {
        this.status = status;
        this.ok = ok;
        this.code = code;
        this.message = message;
        this.dataJson = dataJson;
    }

    public static AdminResponse ok(String code, String message) {
        return new AdminResponse(200, true, code, message);
    }

    public static AdminResponse ok(String code, String message, String dataJson) {
        return new AdminResponse(200, true, code, message, dataJson);
    }

    public static AdminResponse fail(int status, String code, String message) {
        return new AdminResponse(status, false, code, message);
    }

    public String toJson(String requestId) {
        return "{"
                + "\"ok\":" + ok + ","
                + "\"code\":\"" + escape(code) + "\","
                + "\"message\":\"" + escape(message) + "\","
                + "\"request_id\":\"" + escape(requestId) + "\""
                + (dataJson != null && !dataJson.isBlank() ? ",\"data\":" + dataJson : "")
                + "}";
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
    }
}
