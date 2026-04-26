package npc.npc_manifest;

import consts.ConstNpc;
import item.Item;
import java.text.NumberFormat;
import java.util.Locale;
import jdbc.daos.PlayerDAO;
import npc.Npc;
import player.Player;
import services.InventoryService;
import services.Service;
import services.TaskService;
import services.ItemService;
import services.PetService;
import utils.Util;

/**
 *
 * @author CongHoan
 */
public class TongMonSu extends Npc {

    public TongMonSu(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            TaskService.gI().checkDoneTaskTalkNpc(player, this);
            if (mapId == 5) {
                createOtherMenu(player, ConstNpc.BASE_MENU,
                        "|5|Xin chào, ta là Tông Môn Sư\nNgươi muốn làm gì nào?",
                        "Cửa hàng\nĐệ Tử", "Nâng Cấp\nĐệ Tử", "Đổi Hành\nTinh Đệ");
            }
        }
    }

    private boolean tryChargeCoin(Player player, int cost) {
        if (player.getSession() == null) {
            Service.gI().sendThongBao(player, "Có lỗi xảy ra, thử lại sau!");
            return false;
        }
        if (player.getSession().cash < cost) {
            Service.gI().sendThongBao(player, "Bạn không đủ " + cost + " COIN!");
            return false;
        }

        PlayerDAO.subcash(player, cost);
        Service.gI().sendMoney(player);

        return true;
    }

    private void addItemToBag(Player player, Item item) {
        InventoryService.gI().addItemBag(player, item);
        InventoryService.gI().sendItemBag(player);
    }

    private String formatCoin(long coin) {
        return NumberFormat.getInstance(new Locale("vi", "VN")).format(coin);
    }

    private boolean requirePetForVipBuy(Player player, int requiredType, int requiredCap, String requiredName) {
        if (player.pet == null) {
            Service.gI().sendThongBao(player, "Bạn chưa có đệ tử để thực hiện!");
            return false;
        }

        boolean validType = switch (requiredType) {
            case 1 ->
                player.pet.typePet == 1;
            case 2 ->
                player.pet.typePet == 2 || player.pet.typePet == 3 || player.pet.typePet == 4;
            case 5 ->
                player.pet.typePet == 5;
            case 6 ->
                player.pet.typePet == 6;
            default ->
                true;
        };

        if (!validType) {
            Service.gI().sendThongBao(player, "Cần có đệ " + requiredName + " để mua tiếp!");
            return false;
        }

        if (player.cap < requiredCap) {
            Service.gI().sendThongBao(player,
                "Cần đệ " + requiredName + " đạt cấp " + requiredCap
                    + " (hiện tại cấp " + player.cap + ") để mua!");
            return false;
        }

        return true;
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (!canOpenNpc(player)) {
            return;
        }

        // ================= MENU GỐC =================
        if (player.iDMark.getIndexMenu() == ConstNpc.BASE_MENU && this.mapId == 5) {
            switch (select) {
                // Cửa hàng đệ tử
                case 0 -> {
                    if (player.getSession() != null) {
                        this.createOtherMenu(
                                player,
                                13000,
                                "|7|LƯỜI THÌ NẠP CHỨ HOÀN TOÀN CÓ THỂ CÀY ĐƯỢC!"
                                        + "\b|7|Toàn bộ cách kiếm và điều kiện đều được ghi tại web của game!"
                                        // + "\b|3|Đổi đệ thì tháo đồ đệ ra, mất tự chịu nha!"
                                        // + "\n|5|Chỉ số hợp thể chỉ có tác dụng với bông tai!"
                                        + "\b|5|Đệ Mabu tăng 5% chỉ số: 36k"
                                        + "\b|5|Đệ Beerus tăng 10% chỉ số: 120k"
                                        + "\b|5|Đệ Goku Daima tăng 20% chỉ số: 255k"
                                        + "\b|5|Đệ Android 21 tăng 35% chỉ số: 380k"
                                        + "\b|5|Đệ Broly SSJ3 tăng 50% chỉ số: 555k"
                                        + "\n\b|7|Bạn đang có: " + formatCoin(player.getSession().cash) + " COIN",
                                "Đệ Mabu",
                                "Đệ\nBình Hút",
                                "Đệ\nGoku Daima",
                                "Đệ\nAndroid 21",
                                "Đệ\nBroly SSJ3"
                        // "CHI TIẾT"
                        );
                    }
                }
                // Nâng cấp đệ tử
                case 1 -> {
                    if (player.pet == null) {
                        Service.gI().sendThongBao(player, "Bạn chưa có đệ tử để nâng cấp!");
                        return;
                    }

                    Item dathuctinh = InventoryService.gI().findItemBag(player, 1964); // Đá Thức Tỉnh
                    Item cainit = InventoryService.gI().findItemBag(player, 1959); // Cái nịt
                    Item thoivang = InventoryService.gI().findItemBag(player, 457); // Thỏi vàng
                    int DaThucTinh = dathuctinh != null ? dathuctinh.quantity : 0;
                    int CaiNit = cainit != null ? cainit.quantity : 0;
                    int ThoiVang = thoivang != null ? thoivang.quantity : 0;

                    int levelPet = player.level;
                    int da = 10 + (levelPet * 1); // Boss Points (level-up)
                    int xuThaoVang = 200 + (levelPet * 40); // Thỏi vàng
                    int xuCaiNit = 3 * (levelPet + 1); // Cái nịt
                    int daCap = 100 + (player.cap * 250); // Đá Thức Tỉnh (cap-up)

                    if (player.cap == 10 && player.level == 10) {
                        Service.gI().sendThongBao(player, "Đệ tử của bạn đã đạt cấp tối đa!");
                        return;
                    }

                    String msg;
                    if (player.level < 10) {
                        msg = "Pet hiện tại: " + player.pet.name.replaceAll("^\\$+| Cấp \\d+ Level \\d+$", "").trim()
                                + " Cấp " + player.cap + " Level " + player.level
                                + "\nSau nâng cấp: Cấp " + player.cap + " Level " + (player.level + 1)
                                + "\nCần " + da + " Điểm Boss, " + xuCaiNit + " Cái nịt, " + xuThaoVang
                                + " Thỏi vàng để tăng 1 Level"
                                + "\nBạn hiện có: " + formatCoin(player.event.getEventPointBHM()) + " Điểm Boss, "
                                + formatCoin(CaiNit) + " Cái nịt, " + formatCoin(ThoiVang) + " Thỏi vàng"
                                + "\n|7|Lưu Ý: Mỗi 1 level tăng 2% chỉ số hợp thể!";
                    } else {
                        msg = "Pet hiện tại: " + player.pet.name.replaceAll("^\\$+| Cấp \\d+ Level \\d+$", "").trim()
                                + " Cấp " + player.cap + " Level " + player.level
                                + "\nSau nâng cấp: Cấp " + (player.cap + 1) + " Level 0"
                                + "\nCần " + daCap + " Đá Thức Tỉnh, " + xuCaiNit + " Cái nịt để tăng 1 Cấp"
                                + "\nBạn hiện có: " + formatCoin(DaThucTinh) + " Đá Thức Tỉnh, " + formatCoin(CaiNit)
                                + " Cái nịt"
                                + "\n|7|Lưu Ý: Mỗi 1 cấp tăng 2% chỉ số hợp thể!";
                    }

                    this.createOtherMenu(player, 14000, msg, "Nâng cấp", "Từ chối");
                }
                case 2 -> {
                    if (player.pet == null) {
                        Service.gI().sendThongBao(player, "Bạn chưa có đệ tử!");
                        return;
                    }
                    if (player.pet.gender == player.gender) {
                        Service.gI().sendThongBao(player, "Đệ tử đã cùng hành tinh với sư phụ rồi.");
                        return;
                    }

                    String planetName;
                    switch (player.gender) {
                        case 0 ->
                            planetName = "Trái Đất";
                        case 1 ->
                            planetName = "Namek";
                        case 2 ->
                            planetName = "Xayda";
                        default ->
                            planetName = "Không xác định";
                    }

                    this.createOtherMenu(
                            player,
                            15000,
                            "|5|Đổi hành tinh đệ tử?\n"
                                    + "Sau khi đổi, đệ sẽ thuộc hành tinh " + planetName + " giống sư phụ."
                                    + "\n|7|Chỉ đổi hành tinh, giữ nguyên toàn bộ sức mạnh, cấp, level và chỉ số.",
                            "Đổi ngay",
                            "Hủy bỏ");
                }
            }
        } // ================= MENU 13000: SHOP ĐỆ (MUA BẰNG COIN, NHẬN ITEM)
          // =================
        else if (player.iDMark.getIndexMenu() == 13000) {
            switch (select) {
                // Đệ Mabu
                case 0 -> {
                    int cost = 36_000;
                    if (!tryChargeCoin(player, cost)) {
                        return;
                    }

                    Item egg = ItemService.gI().createNewItem((short) 568);
                    egg.quantity = 1;
                    addItemToBag(player, egg);
                    Service.gI().sendThongBao(player, "Mua thành công Trứng Đệ Mabu.");
                }

                // Đệ Bình Hút
                case 1 -> {
                    if (!requirePetForVipBuy(player, 1, 1, "Mabư")) {
                        return;
                    }

                    int cost = 120_000;
                    if (!tryChargeCoin(player, cost)) {
                        return;
                    }

                    int random = Util.nextInt(3);
                    switch (random) {
                        case 0 ->
                            PetService.gI().createUubPet(player);
                        case 1 ->
                            PetService.gI().createKidBeerPet(player);
                        default ->
                            PetService.gI().createJirenPet(player);
                    }
                    PetService.gI().resetPetUpgrade(player);
                    Service.gI().sendThongBao(player, "Mua thành công! Bạn đã nhận đệ Bình Hút.");
                }

                // Đệ Goku Daima
                case 2 -> {
                    if (!requirePetForVipBuy(player, 2, 2, "Bình Hút")) {
                        return;
                    }

                    int cost = 255_000;
                    if (!tryChargeCoin(player, cost)) {
                        return;
                    }

                    PetService.gI().createFideNhiPet(player);
                    PetService.gI().resetPetUpgrade(player);
                    Service.gI().sendThongBao(player, "Mua thành công! Bạn đã nhận đệ Goku Daima.");
                }

                // Đệ Android 21
                case 3 -> {
                    if (!requirePetForVipBuy(player, 5, 3, "Goku Daima")) {
                        return;
                    }

                    int cost = 380_000;
                    if (!tryChargeCoin(player, cost)) {
                        return;
                    }

                    PetService.gI().createXenNhiPet(player);
                    PetService.gI().resetPetUpgrade(player);
                    Service.gI().sendThongBao(player, "Mua thành công! Bạn đã nhận đệ Android 21.");
                }

                // Đệ Broly SSJ3
                case 4 -> {
                    if (!requirePetForVipBuy(player, 6, 5, "Android 21")) {
                        return;
                    }

                    int cost = 555_000;
                    if (!tryChargeCoin(player, cost)) {
                        return;
                    }

                    PetService.gI().createBuNhiPet(player);
                    PetService.gI().resetPetUpgrade(player);
                    Service.gI().sendThongBao(player, "Mua thành công! Bạn đã nhận đệ Broly SSJ3.");
                }

                // CHI TIẾT
                // case 5 -> {
                // String detail
                // = "|7|TÓM TẮT CÁCH KIẾM & ĐIỀU KIỆN ĐỆ\n"
                // + "|5|Mabu (+5%): Săn Boss 22h tại TP Vegeta.\n ĐK cần đệ thường.\n\n"
                // + "|5|Beerus (+10%): Săn Boss 22h tại TP Vegeta rơi bình hút\n → Đến Osin
                // farm kilis.\n ĐK cần Mabu 40 tỷ, cấp 1 và 3000 kilis.\n\n"
                // + "|5|Goku Daima (+20%): BDKB map 110 tìm trứng Goku Daima\n → Có trứng cần
                // săn Cumber tích kill.\n ĐK cần Bình Hút 80 tỷ, cấp 3 và 500 kill.\n\n"
                // + "|5|Android 21 (+35%): Săn Sên thị trấn tìm trứng Android 21\n → Có trứng
                // cần farm quái tại rừng cây.\n ĐK cần Goku Daima 90 tỷ, cấp 5 và 15999 kill
                // quái.\n\n"
                // + "|5|Broly SSJ3 (+50%): Săn Super Broly tìm trứng Broly SSJ3\n → Có trứng
                // cần kiếm mảnh boss và mảnh quái.\n ĐK cần Android 21 110 tỷ, cấp 8 và 999
                // mảnh quái và boss.\n"
                // + "\n|7|Lưu ý: Mua đệ Vip vẫn cần có đủ điều kiện của đệ mới có thể dùng!!!";
                // this.createOtherMenu(player, 13000, detail,
                // "Đệ\nMabư",
                // "Đệ\nBình Hút",
                // "Đệ\nGoku Daima",
                // "Đệ\nAndroid 21",
                // "Đệ\nBroly SSJ3",
                // "Quay lại");
                // }
            }
        } // ================= MENU 14000: NÂNG CẤP ĐỆ TỬ =================
        else if (player.iDMark.getIndexMenu() == 14000) {
            switch (select) {
                case 0 -> {
                    if (player.pet == null) {
                        Service.gI().sendThongBao(player, "Bạn chưa có đệ tử!");
                        return;
                    }
                    Item dathuctinh = InventoryService.gI().findItemBag(player, 1964); // Đá Thức Tỉnh
                    Item cainit = InventoryService.gI().findItemBag(player, 1959); // Cái nịt
                    Item thoivang = InventoryService.gI().findItemBag(player, 457); // Thỏi vàng
                    int DaThucTinh = dathuctinh != null ? dathuctinh.quantity : 0;
                    int CaiNit = cainit != null ? cainit.quantity : 0;
                    int ThoiVang = thoivang != null ? thoivang.quantity : 0;

                    int levelPet = player.level;
                    int da = 10 + (levelPet * 1); // Boss Points (level-up): 10-19
                    int xuThaoVang = 200 + (levelPet * 40); // Riêng cho Thỏi vàng
                    int xuCaiNit = 3 * (levelPet + 1); // Cái nịt
                    int daCap = 100 + (player.cap * 250); // Đá Thức Tỉnh (cap-up)

                    if (player.cap == 10 && player.level == 10) {
                        Service.gI().sendThongBao(player, "Đệ tử của bạn đã đạt cấp tối đa!");
                        return;
                    }
                    if (player.level < 10) {
                        if (player.event.getEventPointBHM() >= da && CaiNit >= xuCaiNit && ThoiVang >= xuThaoVang) {
                            player.event.subEventPointBHM(da);
                            player.level++;
                            String[] parts = player.pet.name.split(" Cấp ");
                            String baseName = parts[0].replaceAll("^\\$+", "").trim();
                            player.pet.name = "$" + baseName + " Cấp " + player.cap + " Level " + player.level;
                            Service.gI().sendThongBao(player, "Đệ tử đã lên Level " + player.level + "!");
                            InventoryService.gI().subQuantityItemsBag(player, cainit, xuCaiNit);
                            InventoryService.gI().subQuantityItemsBag(player, thoivang, xuThaoVang);
                            InventoryService.gI().sendItemBag(player);
                        } else {
                            Service.gI().sendThongBao(player, "Không đủ điểm Boss, Cái nịt hoặc Thỏi vàng!");
                            return;
                        }
                    }
                    if (player.level >= 10 && player.cap < 10) {
                        if (DaThucTinh >= daCap && CaiNit >= xuCaiNit) {
                            int cap = player.cap;
                            int tile = switch (cap) {
                                case 0 ->
                                    100;
                                case 1 ->
                                    80;
                                case 2 ->
                                    50;
                                case 3 ->
                                    40;
                                case 4, 5, 7 ->
                                    35;
                                case 6 ->
                                    30;
                                case 8 ->
                                    20;
                                case 9 ->
                                    10;
                                default ->
                                    5;
                            };
                            InventoryService.gI().subQuantityItemsBag(player, dathuctinh, daCap);
                            InventoryService.gI().subQuantityItemsBag(player, cainit, xuCaiNit);
                            InventoryService.gI().sendItemBag(player);
                            if (Util.isTrue(tile, 100)) {
                                player.cap++;
                                player.level = 0;
                                String[] parts = player.pet.name.split(" Cấp ");
                                String baseName = parts[0].replaceAll("^\\$+", "").trim();
                                player.pet.name = "$" + baseName + " Cấp " + player.cap + " Level " + player.level;
                                Service.gI().sendThongBao(player, "Nâng cấp thành công! Đệ tử cấp " + player.cap + "!");
                            } else {
                                String message = "Nâng cấp thất bại! Đệ tử vẫn cấp " + player.cap;
                                if (Util.isTrue(40, 100)) {
                                    if (player.level > 0) {
                                        player.level--;
                                    }
                                    message += "\nKhông may! Level giảm còn " + player.level;
                                }
                                Service.gI().sendThongBao(player, message);
                            }
                        } else {
                            Service.gI().sendThongBao(player, "Không đủ Đá Thức Tỉnh hoặc Cái nịt!");
                        }
                    }
                }
                case 1 ->
                    Service.gI().sendThongBao(player, "Đã hủy nâng cấp!");
            }
        } else if (player.iDMark.getIndexMenu() == 15000) {
            switch (select) {
                case 0 -> {
                    PetService.gI().changePetPlanetToMaster(player);
                }
                case 1 -> {
                    Service.gI().sendThongBao(player, "Đã hủy đổi hành tinh đệ tử!");
                }
            }
        }
    }
}
