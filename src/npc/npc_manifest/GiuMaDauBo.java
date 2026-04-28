package npc.npc_manifest;

/**
 *
 * @author CongHoan
 */
import clan.Clan;
import clan.ClanMember;
import consts.ConstNpc;
import consts.ConstTask;
import item.Item;
import java.util.ArrayList;
import npc.Npc;
import player.Player;
import server.Client;
import services.ClanService;
import services.InventoryService;
import services.ItemService;
import services.Service;
import services.TaskService;
import services.func.TopService;
import shop.ShopService;
import utils.Util;

public class GiuMaDauBo extends Npc {

    public int qua = 1;

    public GiuMaDauBo(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            Clan clan = player.clan;
            if (clan != null) {
                this.createOtherMenu(player, ConstNpc.BASE_MENU,
                        "Chào bạn, bạn muốn sử dụng chức năng bang hội hay làm nhiệm vụ bang?",
                        "Chức năng bang hội", "Nhiệm vụ Bang\n[" + player.playerTask.clanTask.leftTask + "/" + ConstTask.MAX_CLAN_TASK + "]");
            } else {
                this.createOtherMenu(player, ConstNpc.BASE_MENU,
                        "Bạn chưa tham gia bang hội.\nHãy tham gia bang để sử dụng chức năng này.",
                        "Đóng");
            }

        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            if (player.iDMark.isBaseMenu()) {
                switch (select) {
                    case 0 -> { // Chức năng bang hội
                        Clan clan = player.clan;
                        if (clan == null) {
                            Service.gI().sendThongBao(player, "Bạn chưa tham gia bang hội!");
                            return;
                        }
                        if (clan.level < 2) {
                            this.createOtherMenu(player, 111, "Bạn đang ở bang " + clan.name + "\n"
                                    + "\b|5|Cấp độ bang: " + clan.level + "\n"
                                    + "\b|3|Bang chủ: " + clan.getLeader().name + "\n"
                                    + "\b|7|Tăng 20% TNSM Level " + clan.level + "/2 Để mở khóa\n"
                                    + "\b|7|Tăng 1% SĐ,HP,KI Level " + clan.level + "/3 Để mở khóa\n"
                                    + "\b|7|Shop Bang Hội Level " + clan.level + "/5 Để mở khóa\n"
                                    + "\b|7|Tăng 5% SĐ,HP,KI Level " + clan.level + "/8 Để mở khóa\n"
                                    + "\b|7|Nâng chỉ số Level " + clan.level + "/10 Để mở khóa\n",
                                    "Nâng cấp\nBang hội", "Quyên Góp\nBang Hội");
                        } else if (clan.level < 3) {
                            this.createOtherMenu(player, 111, "Bạn đang ở bang " + clan.name + "\n"
                                    + "\b|5|Cấp độ bang: " + clan.level + "\n"
                                    + "\b|3|Bang chủ: " + clan.getLeader().name + "\n"
                                    + "\b|1|Tăng 20% TNSM Đã mở khóa\n"
                                    + "\b|7|Tăng 1% SĐ,HP,KI Level " + clan.level + "/3 Để mở khóa\n"
                                    + "\b|7|Shop Bang Hội Level " + clan.level + "/5 Để mở khóa\n"
                                    + "\b|7|Tăng 5% SĐ,HP,KI Level " + clan.level + "/8 Để mở khóa\n"
                                    + "\b|7|Nâng chỉ số Level " + clan.level + "/10 Để mở khóa\n",
                                    "Nâng cấp\nBang hội", "Quyên Góp\nBang Hội");
                        } else if (clan.level < 5) {
                            this.createOtherMenu(player, 111, "Bạn đang ở bang " + clan.name + "\n"
                                    + "\b|5|Cấp độ bang: " + clan.level + "\n"
                                    + "\b|3|Bang chủ: " + clan.getLeader().name + "\n"
                                    + "\b|1|Tăng 20% TNSM Đã mở khóa\n"
                                    + "\b|1|Tăng 1% SĐ,HP,KI Đã mở khóa\n"
                                    + "\b|7|Shop Bang Hội Level " + clan.level + "/5 Để mở khóa\n"
                                    + "\b|7|Tăng 5% SĐ,HP,KI Level " + clan.level + "/8 Để mở khóa\n"
                                    + "\b|7|Nâng chỉ số Level " + clan.level + "/10 Để mở khóa\n",
                                    "Nâng cấp\nBang hội", "Quyên Góp\nBang Hội");
                        } else if (clan.level < 8) {
                            this.createOtherMenu(player, 111, "Bạn đang ở bang " + clan.name + "\n"
                                    + "\b|5|Cấp độ bang: " + clan.level + "\n"
                                    + "\b|3|Bang chủ: " + clan.getLeader().name + "\n"
                                    + "\b|1|Tăng 20% TNSM Đã mở khóa\n"
                                    + "\b|1|Tăng 1% SĐ,HP,KI Đã mở khóa\n"
                                    + "\b|1|Shop Bang Hội Đã mở khóa\n"
                                    + "\b|7|Tăng 5% SĐ,HP,KI Level " + clan.level + "/8 Để mở khóa\n"
                                    + "\b|7|Nâng chỉ số Level " + clan.level + "/10 Để mở khóa\n",
                                    "Nâng cấp\nBang hội", "Quyên Góp\nBang Hội", "SHOP BANG");
                        } else if (clan.level < 10) {
                            this.createOtherMenu(player, 111, "Bạn đang ở bang " + clan.name + "\n"
                                    + "\b|5|Cấp độ bang: " + clan.level + "\n"
                                    + "\b|3|Bang chủ: " + clan.getLeader().name + "\n"
                                    + "\b|1|Tăng 20% TNSM Đã mở khóa\n"
                                    + "\b|1|Tăng 1% SĐ,HP,KI Đã mở khóa\n"
                                    + "\b|1|Shop Bang Hội Đã mở khóa\n"
                                    + "\b|1|Tăng 5% SĐ,HP,KI Đã mở khóa\n"
                                    + "\b|7|Nâng chỉ số Level " + clan.level + "/10 Để mở khóa\n",
                                    "Nâng cấp\nBang hội", "Quyên Góp\nBang Hội", "SHOP BANG");
                        } else {
                            this.createOtherMenu(player, 111, "Bạn đang ở bang " + clan.name + "\n"
                                    + "\b|5|Cấp độ bang: " + clan.level + "\n"
                                    + "\b|3|Bang chủ: " + clan.getLeader().name + "\n"
                                    + "\b|1|Tăng 20% TNSM Đã mở khóa\n"
                                    + "\b|1|Tăng 1% SĐ,HP,KI Đã mở khóa\n"
                                    + "\b|1|Shop Bang Hội Đã mở khóa\n"
                                    + "\b|1|Tăng 5% SĐ,HP,KI Đã mở khóa\n"
                                    + "\b|1|Nâng chỉ số Đã mở khóa\n",
                                    "Nâng cấp\nBang hội", "Quyên Góp\nBang Hội", "SHOP BANG", "Nâng chỉ số");
                        }
                    }
                    case 1 -> { // Nhiệm vụ Bang
                        if (player.playerTask.clanTask.template != null) {
                            if (player.playerTask.clanTask.isDone()) {
                                createOtherMenu(player, 113, "Nhiệm vụ đã hoàn thành, hãy nhận " + ((player.playerTask.clanTask.level + 1) * 10) + " capsule bang", "Nhận\nthưởng", "Đóng");
                                break;
                            }
                            createOtherMenu(player, 113, "Nhiệm vụ hiện tại: " + player.playerTask.clanTask.getName() + ". Đã hạ được " + player.playerTask.clanTask.count, "OK", "Hủy bỏ\nNhiệm vụ\nnày");
                        } else {
                            TaskService.gI().changeClanTask(this, player, (byte) Util.nextInt(5));
                        }
                    }
                }
            } else if (player.iDMark.getIndexMenu() == 111) {
                switch (select) {
                    case 0 -> { // Nâng cấp Bang hội
                        Clan clan = player.clan;
                        if (clan == null) {
                            Service.gI().sendThongBao(player, "Bạn chưa tham gia bang hội!");
                            return;
                        }
                        int level = clan.level;
                        if (clan.isLeader(player)) {
                            if (level > 10) {
                                Service.gI().sendThongBao(player, "Đang ở cấp độ cao nhất.");
                                return;
                            }
                            String npcSay = "Cần " + Util.chiaNho(ClanService.gI().capsule(clan)) + " capsule bang [đang có " + Util.chiaNho(clan.capsuleClan) + " capsule bang] để nâng cấp bang hội lên cấp " + (level + 1);
                            npcSay += "\n+1 tối đa số lượng thành viên";
                            if (level > 1) {
                                npcSay += "\n+1 ô trống tối đa rương bang.";
                            }
                            createOtherMenu(player, 112, npcSay, "Đồng ý", "Từ chối");
                        } else {
                            Service.gI().sendThongBao(player, "Chỉ có bang chủ mới đủ đẳng cấp!");
                        }
                    }
                    case 1 -> { // Quyên Góp Bang Hội
                        if (player.getSession().actived) {
                            createOtherMenu(player, 115, "Quyên góp bang hội để nhận ngay Chiến lực bang và Ticket Bang Hội\n"
                                    + "\b|1|Chan nhẹ: Nhận 1 xu elite và 9 Capsule Bang\n"
                                    + "\b|3|Chan Vừa: Nhận 5 xu elite và 49 Capsule Bang\n"
                                    + "\b|5|Chan Mạnh: Nhận 10 xu elite và 100 Capsule Bang\n",
                                    "Chan nhẹ\n10 thỏi vàng", "Chan vừa\n49 Thỏi vàng", "Chan Mạnh luôn!\n99 Thỏi vàng");
                        } else {
                            Service.gI().sendThongBao(player, "Cần mở thành viên để quyên góp!");
                        }
                    }
                    case 2 -> { // SHOP BANG
                        ShopService.gI().opendShop(player, "SHOP_BANG", false);
                    }
                    case 3 -> { // Nâng chỉ số
                        if (player.clan == null) {
                            Service.gI().sendThongBao(player, "Bạn chưa tham gia bang hội!");
                            return;
                        }
                        createOtherMenu(player, 116, "Nâng cấp bang hội giúp ngươi mở khóa giới hạn nâng cấp chỉ số\n"
                                + "Mỗi lần nâng sẽ tiêu hao 1 Ticket Bang Hội, nâng crit sẽ tiêu hao 10 Ticket Bang Hội\n"
                                + "\b|7|Tăng thêm HP: " + player.hpbang + "/" + 5000 * player.clan.level + "\n"
                                + "\b|5|Tăng thêm MP: " + player.mpbang + "/" + 5000 * player.clan.level + "\n"
                                + "\b|3|Tăng thêm DAME: " + player.damebang + "/" + 200 * player.clan.level + "\n"
                                + "\b|7|Tăng thêm Crit: " + player.critbang + "/" + player.clan.level + "\n",
                                "Nâng HP\n+100HP", "Nâng MP\n+100", "Nâng DAME\n+5", "Nâng Crit\n+1");
                    }
                }
            } else if (player.iDMark.getIndexMenu() == 112) {
                Clan clan = player.clan;
                if (clan != null && clan.isLeader(player)) {
                    if (clan.level > 9) {
                        Service.gI().sendThongBao(player, "Đang ở cấp độ cao nhất.");
                        return;
                    }
                    int capsuleCan = ClanService.gI().capsule(clan);
                    int capsuleBang = clan.capsuleClan;
                    if (capsuleBang >= capsuleCan) {
                        clan.capsuleClan -= capsuleCan;
                        clan.level++;
                        clan.maxMember++;
                        Service.gI().sendThongBao(player, "Chúc mừng bang hội của bạn đã lên cấp " + clan.level);
                        for (ClanMember cm : clan.getMembers()) {
                            Player pl = Client.gI().getPlayer(cm.id);
                            if (pl != null) {
                                ClanService.gI().sendMyClan(player);
                            }
                        }
                    } else {
                        Service.gI().sendThongBao(player, "Không đủ capsule bang, cần " + Util.chiaNho(capsuleCan - capsuleBang) + " capsule bang nữa.");
                    }
                }
            } else if (player.iDMark.getIndexMenu() == 113) {
                if (player.playerTask.clanTask.template != null) {
                    switch (select) {
                        case 0 -> {
                            if (player.playerTask.clanTask.isDone()) {
                                TaskService.gI().payClanTask(player);
                            }
                        }
                        case 1 -> {
                            if (!player.playerTask.clanTask.isDone()) {
                                createOtherMenu(player, 114, "Bạn có chắc muốn hủy nhiệm vụ này?\nNếu hủy nhiệm vụ bạn sẽ mất 1 lượt nhiệm vụ trong ngày.", "Đồng ý", "Từ chối");
                            }
                        }
                    }
                }
            } else if (player.iDMark.getIndexMenu() == 114) {
                if (player.playerTask.clanTask.template != null && select == 0 && !player.playerTask.clanTask.isDone()) {
                    TaskService.gI().removeClanTask(player);
                }
            } else if (player.iDMark.getIndexMenu() == 115) {
                switch (select) {
                    case 0 -> {
                        Item tv = InventoryService.gI().findItemBag(player, 457);
                        if (tv == null || tv.quantity < 10) {
                            Service.gI().sendThongBao(player, "Cần 10 thỏi vàng");
                            return;
                        }
                        InventoryService.gI().subQuantityItemsBag(player, tv, 10);
                        Item xubang = ItemService.gI().createNewItem((short) 1636, 1);
                        if (xubang == null || xubang.template == null) {
                            Service.gI().sendThongBao(player, "Lỗi khi tạo Ticket Bang Hội!");
                            return;
                        }
                        InventoryService.gI().addItemBag(player, xubang);
                        InventoryService.gI().sendItemBag(player);
                        player.clan.capsuleClan += 9;
                        for (ClanMember cm : player.clan.getMembers()) {
                            if (cm.id == player.id) {
                                cm.memberPoint += 9;
                                cm.clanPoint += 9;
                                break;
                            }
                        }
                        for (ClanMember cm : player.clan.getMembers()) {
                            Player pl = Client.gI().getPlayer(cm.id);
                            if (pl != null) {
                                ClanService.gI().sendMyClan(player);
                            }
                        }
                        Service.gI().sendThongBao(player, "Bạn nhận được x1 Ticket Bang Hội và 9 Capsule Bang");
                    }
                    case 1 -> {
                        Item tv = InventoryService.gI().findItemBag(player, 457);
                        if (tv == null || tv.quantity < 49) {
                            Service.gI().sendThongBao(player, "Cần 49 thỏi vàng");
                            return;
                        }
                        InventoryService.gI().subQuantityItemsBag(player, tv, 49);
                        Item xubang = ItemService.gI().createNewItem((short) 1636, 5);
                        if (xubang == null || xubang.template == null) {
                            Service.gI().sendThongBao(player, "Lỗi khi tạo Ticket Bang Hội!");
                            return;
                        }
                        InventoryService.gI().addItemBag(player, xubang);
                        InventoryService.gI().sendItemBag(player);
                        player.clan.capsuleClan += 49;
                        for (ClanMember cm : player.clan.getMembers()) {
                            if (cm.id == player.id) {
                                cm.memberPoint += 49;
                                cm.clanPoint += 49;
                                break;
                            }
                        }
                        for (ClanMember cm : player.clan.getMembers()) {
                            Player pl = Client.gI().getPlayer(cm.id);
                            if (pl != null) {
                                ClanService.gI().sendMyClan(player);
                            }
                        }
                        Service.gI().sendThongBao(player, "Bạn nhận được x5 Ticket Bang Hội và 49 Capsule Bang");
                    }
                    case 2 -> {
                        Item tv = InventoryService.gI().findItemBag(player, 457);
                        if (tv == null || tv.quantity < 99) {
                            Service.gI().sendThongBao(player, "Cần 99 thỏi vàng");
                            return;
                        }
                        InventoryService.gI().subQuantityItemsBag(player, tv, 99);
                        Item xubang = ItemService.gI().createNewItem((short) 1636, 10);
                        if (xubang == null || xubang.template == null) {
                            Service.gI().sendThongBao(player, "Lỗi khi tạo Ticket Bang Hội!");
                            return;
                        }
                        InventoryService.gI().addItemBag(player, xubang);
                        InventoryService.gI().sendItemBag(player);
                        player.clan.capsuleClan += 100;
                        for (ClanMember cm : player.clan.getMembers()) {
                            if (cm.id == player.id) {
                                cm.memberPoint += 100;
                                cm.clanPoint += 100;
                                break;
                            }
                        }
                        for (ClanMember cm : player.clan.getMembers()) {
                            Player pl = Client.gI().getPlayer(cm.id);
                            if (pl != null) {
                                ClanService.gI().sendMyClan(player);
                            }
                        }
                        Service.gI().sendThongBao(player, "Bạn nhận được x10 Ticket Bang Hội và 100 điểm chiến lực bang");
                    }
                }
            } else if (player.iDMark.getIndexMenu() == 116) {
                switch (select) {
                    case 0 -> {
                        Item xub = InventoryService.gI().findItemBag(player, 1636);
                        if (xub == null) {
                            Service.gI().sendThongBao(player, "Cần 1 Ticket Bang Hội");
                            return;
                        }
                        if (player.clan == null) {
                            Service.gI().sendThongBao(player, "Bạn chưa tham gia bang hội.");
                            return;
                        }
                        if (player.hpbang < 5000 * player.clan.level) {
                            InventoryService.gI().subQuantityItemsBag(player, xub, 1);
                            player.hpbang += 100;
                            InventoryService.gI().sendItemBag(player);
                            Service.gI().sendThongBao(player, "Nâng cấp chỉ số thành công");
                            createOtherMenu(player, 116, "Nâng cấp bang hội giúp ngươi mở khóa giới hạn nâng cấp chỉ số\n"
                                    + "Mỗi lần nâng sẽ tiêu hao 1 Ticket Bang Hội, nâng crit sẽ tiêu hao 10 Ticket Bang Hội\n"
                                    + "\b|7|Tăng thêm HP: " + player.hpbang + "/" + 5000 * player.clan.level + "\n"
                                    + "\b|5|Tăng thêm MP: " + player.mpbang + "/" + 5000 * player.clan.level + "\n"
                                    + "\b|3|Tăng thêm DAME: " + player.damebang + "/" + 200 * player.clan.level + "\n"
                                    + "\b|7|Tăng thêm Crit: " + player.critbang + "/" + player.clan.level + "\n",
                                    "Nâng HP\n+100HP", "Nâng MP\n+100", "Nâng DAME\n+5", "Nâng Crit\n+1");
                        } else {
                            Service.gI().sendThongBao(player, "Chỉ số thêm đã max, cần nâng cấp bang hội để mở giới hạn");
                        }
                    }
                    case 1 -> {
                        Item xub = InventoryService.gI().findItemBag(player, 1636);
                        if (xub == null) {
                            Service.gI().sendThongBao(player, "Cần 1 Ticket Bang Hội");
                            return;
                        }
                        if (player.clan == null) {
                            Service.gI().sendThongBao(player, "Bạn chưa tham gia bang hội.");
                            return;
                        }
                        if (player.mpbang < 5000 * player.clan.level) {
                            InventoryService.gI().subQuantityItemsBag(player, xub, 1);
                            player.mpbang += 100;
                            InventoryService.gI().sendItemBag(player);
                            Service.gI().sendThongBao(player, "Nâng cấp chỉ số thành công");
                            createOtherMenu(player, 116, "Nâng cấp bang hội giúp ngươi mở khóa giới hạn nâng cấp chỉ số\n"
                                    + "Mỗi lần nâng sẽ tiêu hao 1 Ticket Bang Hội, nâng crit sẽ tiêu hao 10 Ticket Bang Hội\n"
                                    + "\b|7|Tăng thêm HP: " + player.hpbang + "/" + 5000 * player.clan.level + "\n"
                                    + "\b|5|Tăng thêm MP: " + player.mpbang + "/" + 5000 * player.clan.level + "\n"
                                    + "\b|3|Tăng thêm DAME: " + player.damebang + "/" + 200 * player.clan.level + "\n"
                                    + "\b|7|Tăng thêm Crit: " + player.critbang + "/" + player.clan.level + "\n",
                                    "Nâng HP\n+100HP", "Nâng MP\n+100", "Nâng DAME\n+5", "Nâng Crit\n+1");
                        } else {
                            Service.gI().sendThongBao(player, "Chỉ số thêm đã max, cần nâng cấp bang hội để mở giới hạn");
                        }
                    }
                    case 2 -> {
                        Item xub = InventoryService.gI().findItemBag(player, 1636);
                        if (xub == null) {
                            Service.gI().sendThongBao(player, "Cần 1 Ticket Bang Hội");
                            return;
                        }
                        if (player.clan == null) {
                            Service.gI().sendThongBao(player, "Bạn chưa tham gia bang hội.");
                            return;
                        }
                        if (player.damebang < 200 * player.clan.level) {
                            InventoryService.gI().subQuantityItemsBag(player, xub, 1);
                            player.damebang += 5;
                            InventoryService.gI().sendItemBag(player);
                            Service.gI().sendThongBao(player, "Nâng cấp chỉ số thành công");
                            createOtherMenu(player, 116, "Nâng cấp bang hội giúp ngươi mở khóa giới hạn nâng cấp chỉ số\n"
                                    + "Mỗi lần nâng sẽ tiêu hao 1 Ticket Bang Hội, nâng crit sẽ tiêu hao 10 Ticket Bang Hội\n"
                                    + "\b|7|Tăng thêm HP: " + player.hpbang + "/" + 5000 * player.clan.level + "\n"
                                    + "\b|5|Tăng thêm MP: " + player.mpbang + "/" + 5000 * player.clan.level + "\n"
                                    + "\b|3|Tăng thêm DAME: " + player.damebang + "/" + 200 * player.clan.level + "\n"
                                    + "\b|7|Tăng thêm Crit: " + player.critbang + "/" + player.clan.level + "\n",
                                    "Nâng HP\n+100HP", "Nâng MP\n+100", "Nâng DAME\n+5", "Nâng Crit\n+1");
                        } else {
                            Service.gI().sendThongBao(player, "Chỉ số thêm đã max, cần nâng cấp bang hội để mở giới hạn");
                        }
                    }
                    case 3 -> {
                        Item xub = InventoryService.gI().findItemBag(player, 1636);
                        if (xub == null || xub.quantity < 10) {
                            Service.gI().sendThongBao(player, "Cần 10 Ticket Bang Hội");
                            return;
                        }
                        if (player.clan == null) {
                            Service.gI().sendThongBao(player, "Bạn chưa tham gia bang hội.");
                            return;
                        }
                        if (player.critbang < player.clan.level) {
                            InventoryService.gI().subQuantityItemsBag(player, xub, 10);
                            player.critbang += 1;
                            InventoryService.gI().sendItemBag(player);
                            Service.gI().sendThongBao(player, "Nâng cấp chỉ số thành công");
                            createOtherMenu(player, 116, "Nâng cấp bang hội giúp ngươi mở khóa giới hạn nâng cấp chỉ số\n"
                                    + "Mỗi lần nâng sẽ tiêu hao 1 Ticket Bang Hội, nâng crit sẽ tiêu hao 10 Ticket Bang Hội\n"
                                    + "\b|7|Tăng thêm HP: " + player.hpbang + "/" + 5000 * player.clan.level + "\n"
                                    + "\b|5|Tăng thêm MP: " + player.mpbang + "/" + 5000 * player.clan.level + "\n"
                                    + "\b|3|Tăng thêm DAME: " + player.damebang + "/" + 200 * player.clan.level + "\n"
                                    + "\b|7|Tăng thêm Crit: " + player.critbang + "/" + player.clan.level + "\n",
                                    "Nâng HP\n+100HP", "Nâng MP\n+100", "Nâng DAME\n+5", "Nâng Crit\n+1");
                        } else {
                            Service.gI().sendThongBao(player, "Chỉ số thêm đã max, cần nâng cấp bang hội để mở giới hạn");
                        }
                    }
                }
            }
        }
    }
}
