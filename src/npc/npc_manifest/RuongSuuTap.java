package npc.npc_manifest;

import consts.ConstNpc;
import npc.Npc;
import player.Inventory;
import player.Player;
import services.InventoryService;
import services.ItemService;
import services.Service;
import utils.Util;

/**
 *
 * @author CongHoan
 */
public class RuongSuuTap extends Npc {

    private static final long PRICE_GOLD = 1_500_000_000; // 1.5 tỷ vàng
    private static final int MAX_SLOTS = 40;

    public RuongSuuTap(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            player.iDMark.setTypeBox(Inventory.TYPE_COLLECTION_BOX);
            this.createOtherMenu(player, ConstNpc.BASE_MENU,
                    "Vàng bạc châu báu gì cứ yên tâm giao hết cho tôi",
                    "Mở rương",
                    "Nâng cấp\nrương",
                    "Thông tin\nCần biết",
                    "Từ chối"
            );

        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            if (player.iDMark.isBaseMenu()) {
                switch (select) {
                    case 0 -> {
                        InventoryService.gI().openBox(player);
                    }
                    case 1 -> {
                        if (player.inventory.itemsBoxCollection.size() + 1 > Inventory.MAX_ITEM_BOX_COLLECTION) {
                            Service.gI().sendThongBao(player, "Rương đã mở rộng tối đa!");
                            return;
                        }
                        createOtherMenu(player, ConstNpc.MO_RONG_COLLECTION_BOX, "Bạn có chắc chắn muốn mở thêm ô [" + (player.inventory.itemsBoxCollection.size() + 1) + "] với giá " + Util.numberToMoney(Inventory.PRICE_SLOT_COLLECTION_BOX) + " vàng", "Đồng ý", "Từ chối");
                    }
                    case 2 -> {
                        this.createOtherMenu(player, 3422,
                                "|0| Thông Tin về chỉ số Rương Sưu Tầm"
                                + "\n|7|Vật phẩm có thể bỏ: Cải trang, Đeo lưng, Pet, Ván bay"
                                + "\n|7|Bỏ đủ 20 vật phẩm tăng 1% sức đánh, 3% hp, ki"
                                + "\n|7|Bỏ đủ 40 vật phẩm tăng 3% sức đánh, 7% hp, ki"
                                + "\n|0|Cảm Ơn Đã Đọc Mãi Yêu Ngọc Rồng Hdpe",
                                "Đóng");
                    }
                }
            } else if (player.iDMark.getIndexMenu() == ConstNpc.MO_RONG_COLLECTION_BOX) {
                if (select == 0) {
                    if (player.inventory.gold < Inventory.PRICE_SLOT_COLLECTION_BOX) {
                        Service.gI().sendThongBao(player, "Không đủ vàng để mở rộng rương!");
                        return;
                    }
                    player.inventory.gold -= Inventory.PRICE_SLOT_COLLECTION_BOX;
                    player.inventory.itemsBoxCollection.add(ItemService.gI().createItemNull());
                    Service.gI().sendMoney(player);
                    InventoryService.gI().sendItemBox(player);
                    Service.gI().sendThongBao(player, "Mở rộng rương thành công.");
                }
            }
        }
    }
}
