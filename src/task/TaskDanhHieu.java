package task;

import item.Item;
import player.Player;
import services.InventoryService;
import services.Service;
import shop.ItemShop;

/**
 *
 * @author CongHoan
 */
public class TaskDanhHieu {

    public int Nap;
    public int VeChai;
    public int MocSachTui;
    public int FanCung;
    public int GoDauTre;
    public int GoDauTre1;
    public int GoDauTre2;
    public int XMas;
    public int EmDepEmXinh;
    public int AnBamTraXanh;
    public int TayNhanhHonNao;

    public long ResetTime;

    public TaskDanhHieu() {
        this.Nap = 0;             // Nạp Tích Lũy 1 triệu
        this.VeChai = 0;         // Nhặt đồ
        this.MocSachTui = 0;       // Hạ gục ăn trộm
        this.FanCung = 0;           // điểm danh 7 ngày
        this.GoDauTre = 0;        // k có cách kiếm
        this.GoDauTre1 = 0;      // k có cách kiếm
        this.GoDauTre2 = 0;        // k có cách kiếm
        this.XMas = 0;         // k có cách kiếm
        this.EmDepEmXinh = 0;      //    k có cách kiếm
        this.AnBamTraXanh = 0;          // k có cách kiếm
        this.TayNhanhHonNao = 0;             // k có cách kiếm
    }

    public boolean CheckItem(Player player, ItemShop itemShop, int itemId) {
        Item existingItem = InventoryService.gI().findItemInAllInventories(player, itemId);
        if (existingItem != null) {
            Service.gI().sendThongBao(player, "Bạn đã sở hữu danh hiệu này rồi.");
            return false;
        }
        int required = 0;
        int current = 0;
        switch (itemId) {
            case 1289 -> {
                required = 5000;
                current = player.playerTask.taskdh.Nap;
            }
            case 1291 -> {
                required = 5000;
                current = player.playerTask.taskdh.VeChai;
            }
            case 1296 -> {
                required = 20;
                current = player.playerTask.taskdh.MocSachTui;
            }
            case 1299 -> {
                required = 7;
                current = player.playerTask.taskdh.FanCung;
            }
            case 1392 -> {
                required = 999999;
                current = player.playerTask.taskdh.GoDauTre;
            }
            case 1393 -> {
                required = 999999;
                current = player.playerTask.taskdh.GoDauTre1;
            }
            case 1394 -> {
                required = 999999;
                current = player.playerTask.taskdh.GoDauTre2;
            }
            case 1457 -> {
                required = 3;
                current = player.playerTask.taskdh.XMas;
            }
            case 1514 -> {
                required = 1;
                current = player.playerTask.taskdh.EmDepEmXinh;
            }
            case 1297 -> {
                required = 10;
                current = player.playerTask.taskdh.AnBamTraXanh;
            }
            case 1673 -> {
                required = 500;
                current = player.playerTask.taskdh.TayNhanhHonNao;
            }
            default -> {
                return true;
            }
        }
        if (current < required) {
            Service.gI().sendThongBao(player, "Bạn chưa mở khoá danh hiệu này");
            return false;
        } else {
            Service.gI().sendThongBao(player, "Bạn đã sở hữu danh hiệu này");
            return true;
        }
    }
}
