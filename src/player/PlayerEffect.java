package player;

public class PlayerEffect {

    public static int baseDaiGiaMoiNhu = 1000;
    public static int baseOngThanVeChai = 100;
    public static int baseBiMocSachTui = 1;
    public static int basePhanCung = 100;
    public static int baseGoDauTre = 100;
    public static int baseGoDauTre1 = 100;
    public static int baseGoDauTre2 = 100;
    public static int baseXMas = 100;
    public static int baseEmDepEmXinh = 100;
    public static int baseAnBamTraXanh = 100;
    public static int baseTayNhanhHonNao = 100;

    private int pointDaiGiaMoiNhu;
    private int pointOngThanVeChai;
    private int pointBiMocSachTui;
    private int pointPhanCung;
    private int pointGoDauTre;
    private int pointGoDauTre1;
    private int pointGoDauTre2;
    private int pointXMas;
    private int pointEmDepEmXinh;
    private int pointAnBamTraXanh;
    private int pointTayNhanhHonNao;

    private Player player;

    public PlayerEffect(Player player) {
        this.player = player;
    }

    public void setPointDaiGiaMoiNhu(int value) {
        this.pointDaiGiaMoiNhu = value;
    }

    public void setPointOngThanVeChai(int value) {
        this.pointOngThanVeChai = value;
    }

    public void setPointBiMocSachTui(int value) {
        this.pointBiMocSachTui = value;
    }

    public void setPointPhanCung(int value) {
        this.pointPhanCung = value;
    }

    public void setPointGoDauTre(int value) {
        this.pointGoDauTre = value;
    }

    public void setPointGoDauTre1(int value) {
        this.pointGoDauTre1 = value;
    }

    public void setPointGoDauTre2(int value) {
        this.pointGoDauTre2 = value;
    }

    public void setPointXMas(int value) {
        this.pointXMas = value;
    }

    public void setPointEmDepEmXinh(int value) {
        this.pointEmDepEmXinh = value;
    }

    public void setPointAnBamTraXanh(int value) {
        this.pointAnBamTraXanh = value;
    }

    public void setPointTayNhanhHonNao(int value) {
        this.pointTayNhanhHonNao = value;
    }

    public void addPointDaiGiamMoiNhu(int value) {
        this.pointDaiGiaMoiNhu += value;
    }

    public void addPointOngThanVeChai() {
        this.pointOngThanVeChai++;
    }

    public void addPointBiMocSachTui() {
        this.pointBiMocSachTui++;
    }

    public void addPointPhanCung() {
        this.pointPhanCung++;
    }

    public void addPointGoDauTre() {
        this.pointGoDauTre++;
    }

    public void addPointGoDauTre1() {
        this.pointGoDauTre1++;
    }

    public void addPointGoDauTre2() {
        this.pointGoDauTre2++;
    }

    public void addPointXMas() {
        this.pointXMas++;
    }

    public void addPointEmDepEmXinh() {
        this.pointEmDepEmXinh++;
    }

    public void addPointAnBamTraXanh() {
        this.pointAnBamTraXanh++;
    }

    public void addPointTayNhanhHonNao() {
        this.pointTayNhanhHonNao++;
    }

    public void subPointEffectDaiGia(int point) {
        pointDaiGiaMoiNhu -= point;
    }

    public void subPointEffectOngThanVeChai(int point) {
        pointOngThanVeChai -= point;
    }

    public void subPointEffectBiMocSachTui(int point) {
        pointBiMocSachTui -= point;
    }

    public void subPointEffectPhanCung(int point) {
        pointPhanCung -= point;
    }

    public void subPointEffectGoDauTre(int point) {
        pointGoDauTre -= point;
    }

    public void subPointEffectGoDauTre1(int point) {
        pointGoDauTre1 -= point;
    }

    public void subPointEffectGoDauTre2(int point) {
        pointGoDauTre2 -= point;
    }

    public void subPointEffectXMas(int point) {
        pointXMas -= point;
    }

    public void subPointEffectEmDepEmXinh(int point) {
        pointEmDepEmXinh -= point;
    }

    public void subPointEffectAnBamTraXanh(int point) {
        pointAnBamTraXanh -= point;
    }

    public void subPointEffectTayNhanhHonNao(int point) {
        pointTayNhanhHonNao -= point;
    }

    public int getPointDaiGiaMoiNhu() {
        return pointDaiGiaMoiNhu;
    }

    public int getPointOngThanVeChai() {
        return pointOngThanVeChai;
    }

    public int getPointBiMocSachTui() {
        return pointBiMocSachTui;
    }

    public int getPointPhanCung() {
        return pointPhanCung;
    }

    public int getPointGoDauTre() {
        return pointGoDauTre;
    }

    public int getPointGoDauTre1() {
        return pointGoDauTre1;
    }

    public int getPointGoDauTre2() {
        return pointGoDauTre2;
    }

    public int getPointXMas() {
        return pointXMas;
    }

    public int getPointEmDepEmXinh() {
        return pointEmDepEmXinh;
    }

    public int getPointAnBamTraXanh() {
        return pointAnBamTraXanh;
    }

    public int getPointTayNhanhHonNao() {
        return pointTayNhanhHonNao;
    }

    public boolean isEff(int pointEff, int pointBase) {
        return pointEff >= pointBase;
    }
}
