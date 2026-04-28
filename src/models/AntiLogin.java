package models;

import utils.Util;

/*
 *
 *
 * @author CongHoan
 */
public class AntiLogin {

    private static final byte MAX_WRONG = 5;
    private static final int TIME_ANTI = 900000;

    private long lastTimeLogin;
    private int timeCanLogin;

    public byte wrongLogin;

    public boolean canLogin() {
        if (lastTimeLogin != -1) {
            if (Util.canDoWithTime(lastTimeLogin, timeCanLogin)) {
                this.reset();
                return true;
            }
        }
        return wrongLogin < MAX_WRONG;
    }

    public void wrong() {
        wrongLogin++;
        if (wrongLogin >= MAX_WRONG) {
            this.lastTimeLogin = System.currentTimeMillis();
            this.timeCanLogin = TIME_ANTI;
        }
    }

    public void reset() {
        this.wrongLogin = 0;
        this.lastTimeLogin = -1;
        this.timeCanLogin = 0;
    }

    public String getNotifyWrongCount() {
        int remain = MAX_WRONG - wrongLogin;
        if (remain <= 0) {
            return "Bạn đã nhập sai quá số lần cho phép. Tài khoản tạm thời bị khóa.";
        }
        return "Tài khoản hoặc mật khẩu không chính xác. Bạn còn " + remain + " lần thử lại.";
    }

    public String getNotifyCannotLogin() {
        long now = System.currentTimeMillis();
        long timeLeft = (lastTimeLogin + timeCanLogin) - now;
        if (timeLeft <= 0) {
            return "Bạn có thể đăng nhập lại ngay bây giờ.";
        }
        long minutesLeft = timeLeft / 60000;
        if (minutesLeft <= 0) {
            minutesLeft = 1;
        }
        return "Bạn đã đăng nhập sai quá nhiều lần. Vui lòng thử lại sau " + minutesLeft + " phút.";
    }
}
