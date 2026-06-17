package npc.npc_manifest;

import consts.ConstNpc;
import consts.ConstTranhNgocNamek;
import consts.cn;
import item.Item;
import java.text.NumberFormat;
import java.util.Locale;
import jdbc.daos.PlayerDAO;
import models.DragonNamecWar.TranhNgocService;
import npc.Npc;
import player.Player;
import services.InventoryService;
import services.PetService;
import services.Service;
import services.TaskService;
import services.func.TopService;
import shop.ShopService;
import utils.Util;

public class Fide extends Npc {

    private static final int FIDE_EXTRA_MENU = 74000;
    private static final int CONFIRM_REMOVE_LIMITED_ITEMS = 74001;
    private static final int MAX_REMOVE_LIMITED_ITEMS = 5;
    private static final int MAX_PREVIEW_ITEM_NAME_LENGTH = 28;

    public Fide(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    private String formatCoin(long coin) {
        return NumberFormat.getInstance(new Locale("vi", "VN")).format(coin);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            TaskService.gI().checkDoneTaskTalkNpc(player, this);
            if (mapId == 5) {
                createOtherMenu(player, ConstNpc.BASE_MENU,
                        "|0| Ta chỉ bán đồ cho người giàu, ngươi có tiền không đó ???\n|6|"
                        + "Số điểm săn boss hiện có: "
                        + formatCoin(player.event.getEventPointBHM()) + " điểm.",
                        "Cửa hàng\n tiện lợi", "Cửa hàng\n cao cấp",
                        "Cửa hàng\n điểm boss", "Xem top\n kill boss", "Chức năng\nkhác");
            } else if (mapId == ConstTranhNgocNamek.MAP_ID) {
                if (player.iDMark.getTranhNgoc() == 1) {
                    this.createOtherMenu(player, ConstNpc.BASE_MENU,
                            "Phắn đê !Ta không nói chuyện với sinh vật hạ đẳng", "Đóng");
                    return;
                }
                this.createOtherMenu(player, ConstNpc.BASE_MENU,
                        "Hãy mang ngọc rồng về cho ta", "Đưa ngọc", "Đóng");
            }
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            if (player.iDMark.getIndexMenu() == ConstNpc.BASE_MENU) {
                if (this.mapId == 5) {
                    switch (select) {
//                        case 0 -> {
//                            ShopService.gI().opendShop(player, "SANTA_PHUKIEN", false);
//                        }
                        case 4 -> {
                            createOtherMenu(player, FIDE_EXTRA_MENU,
                                    "Ngươi muốn dùng chức năng nào?",
                                    "Cửa hàng\nthỏi vàng", "Xóa đồ\nhạn dùng", "Từ chối");
                        }
                        case 0 -> {
                            ShopService.gI().opendShop(player, "SHOP_NANGCAP", false);
                        }
                        case 1 -> {
                            ShopService.gI().opendShop(player, "SHOP_XU_KRAI", false);
                        }
                        case 2 -> {
                            ShopService.gI().opendShop(player, "SHOP_DIEMSANBOSS", false);
                        }
                        case 3 -> {
                            TopService.showListTop(player, 8);
                        }
                    }
                }
            } else if (player.iDMark.getIndexMenu() == FIDE_EXTRA_MENU) {
                switch (select) {
                    case 0 -> {
                        ShopService.gI().opendShop(player, "SHOP_TV", false);
                    }
                    case 1 -> {
                        showRemoveLimitedItemsConfirm(player);
                    }
                }
            } else if (player.iDMark.getIndexMenu() == CONFIRM_REMOVE_LIMITED_ITEMS) {
                if (select == 0) {
                    int removed = removeLimitedItemsInBag(player);
                    if (removed > 0) {
                        InventoryService.gI().sendItemBag(player);
                        Service.gI().sendThongBao(player,
                                "Đã xóa " + removed + " vật phẩm có hạn sử dụng khỏi hành trang.");
                    } else {
                        Service.gI().sendThongBao(player,
                                "Không có vật phẩm có hạn sử dụng cần xóa trong hành trang.");
                    }
                }
            } else if (this.mapId == ConstTranhNgocNamek.MAP_ID) {
                switch (select) {
                    case 0 -> {
                        if (player.iDMark.getTranhNgoc() == 2 && player.isHoldNamecBallTranhDoat) {
                            if (!Util.canDoWithTime(player.lastTimePickItem, 20000)) {
                                Service.gI().sendThongBao(player, "Vui lòng đợi "
                                        + ((player.lastTimePickItem + 20000 - System.currentTimeMillis()) / 1000)
                                        + " giây để có thể trả");
                                return;
                            }
                            TranhNgocService.getInstance().dropBall(player, (byte) 2);
                            player.zone.pointRed++;
                            if (player.zone.pointRed > ConstTranhNgocNamek.MAX_POINT) {
                                player.zone.pointRed = ConstTranhNgocNamek.MAX_POINT;
                            }
                            TranhNgocService.getInstance().sendUpdatePoint(player);
                        }
                    }
                }
            }
        }
    }

    private void showRemoveLimitedItemsConfirm(Player player) {
        int count = countLimitedItemsInBag(player);
        if (count <= 0) {
            Service.gI().sendThongBao(player,
                    "Không có vật phẩm có hạn sử dụng cần xóa trong hành trang.");
            return;
        }
        createOtherMenu(player, CONFIRM_REMOVE_LIMITED_ITEMS,
                "Ta sẽ xóa tối đa " + MAX_REMOVE_LIMITED_ITEMS
                + " vật phẩm có hạn sử dụng trong hành trang.\n"
                + "Áp dụng cho: pet, đeo lưng, cải trang, ván bay.\n"
                + "Hiện tìm thấy: " + count + " vật phẩm.\n"
                + buildLimitedItemsPreview(player, count),
                "Đồng ý", "Từ chối");
    }

    private int countLimitedItemsInBag(Player player) {
        int count = 0;
        if (player == null || player.inventory == null || player.inventory.itemsBag == null) {
            return count;
        }
        for (Item item : player.inventory.itemsBag) {
            if (canRemoveLimitedItem(item)) {
                count++;
            }
        }
        return count;
    }

    private String buildLimitedItemsPreview(Player player, int totalCount) {
        StringBuilder preview = new StringBuilder("Sẽ xóa trước:\n");
        int count = 0;
        if (player == null || player.inventory == null || player.inventory.itemsBag == null) {
            return preview.append("Không có vật phẩm phù hợp.").toString();
        }
        for (Item item : player.inventory.itemsBag) {
            if (!canRemoveLimitedItem(item)) {
                continue;
            }
            count++;
            preview.append(count)
                    .append(". ")
                    .append(shortenItemName(item.template.name))
                    .append(" - ")
                    .append(getExpiryText(item))
                    .append("\n");
            if (count >= MAX_REMOVE_LIMITED_ITEMS) {
                break;
            }
        }
        if (count == 0) {
            preview.append("Không có vật phẩm phù hợp.");
        } else if (totalCount > count) {
            preview.append("... và các vật phẩm tiếp theo");
        }
        return preview.toString().trim();
    }

    private int removeLimitedItemsInBag(Player player) {
        int removed = 0;
        if (player == null || player.inventory == null || player.inventory.itemsBag == null) {
            return removed;
        }
        for (int i = 0; i < player.inventory.itemsBag.size() && removed < MAX_REMOVE_LIMITED_ITEMS; i++) {
            Item item = player.inventory.itemsBag.get(i);
            if (canRemoveLimitedItem(item)) {
                InventoryService.gI().removeItemBag(player, i);
                removed++;
            }
        }
        if (removed > 0) {
            InventoryService.gI().sortItems(player.inventory.itemsBag);
        }
        return removed;
    }

    private boolean canRemoveLimitedItem(Item item) {
        return item != null && item.isNotNullItem()
                && isRemovableLimitedItemType(item.template.type)
                && hasExpiryDate(item);
    }

    private boolean isRemovableLimitedItemType(int type) {
        return type == 5 || type == 11 || type == 21 || type == 23 || type == 24 || type == 72;
    }

    private boolean hasExpiryDate(Item item) {
        if (item.itemOptions == null) {
            return false;
        }
        for (Item.ItemOption option : item.itemOptions) {
            if (option != null && option.optionTemplate != null && option.haveExpiryDate()) {
                return true;
            }
        }
        return false;
    }

    private String getExpiryText(Item item) {
        Item.ItemOption expiryOption = getExpiryOption(item);
        if (expiryOption == null) {
            return "không rõ hạn";
        }
        if (expiryOption.optionTemplate.id == 93) {
            return expiryOption.param + " ngày";
        }
        return expiryOption.getOptionString();
    }

    private Item.ItemOption getExpiryOption(Item item) {
        if (item == null || item.itemOptions == null) {
            return null;
        }
        for (Item.ItemOption option : item.itemOptions) {
            if (option != null && option.optionTemplate != null && option.haveExpiryDate()) {
                return option;
            }
        }
        return null;
    }

    private String shortenItemName(String name) {
        if (name == null || name.length() <= MAX_PREVIEW_ITEM_NAME_LENGTH) {
            return name == null ? "Không rõ tên" : name;
        }
        return name.substring(0, MAX_PREVIEW_ITEM_NAME_LENGTH - 3) + "...";
    }
}
