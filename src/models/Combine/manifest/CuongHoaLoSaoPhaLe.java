package models.Combine.manifest;

import consts.ConstFont;
import consts.ConstNpc;
import item.Item;
import item.Item.ItemOption;
import models.Combine.CombineService;
import player.Player;
import services.InventoryService;
import services.Service;
import utils.Util;

public class CuongHoaLoSaoPhaLe {

    public static void showInfoCombine(Player player) {
        if (player.combine.itemsCombine.size() != 3) {
            Service.gI().sendDialogMessage(player, "Cần 1 trang bị có ô sao pha lê thứ 8 trở lên chưa cường hóa\n1 đá Hematite\n1 dùi đục");
            return;
        }
        Item hematite = null;
        Item duiDuc = null;
        Item trangBi = null;

        for (Item item : player.combine.itemsCombine) {
            if (item.template.type < 5 || item.template.type == 32) {
                trangBi = item;
            } else if (item.template.id == 1438) {
                duiDuc = item;
            } else if (item.template.id == 1423 || item.template.id == 1441) {
                hematite = item;
            }
        }

        if (trangBi == null || duiDuc == null || hematite == null) {
            Service.gI().sendDialogMessage(player, "Cần 1 trang bị có ô sao pha lê thứ 8 trở lên chưa cường hóa\n1 đá Hematite\n1 dùi đục");
            return;
        }

        int star = trangBi.getOptionParam(107);
        int starCuongHoa = trangBi.getOptionParam(228);

        if (star < 8 || star == starCuongHoa) {
            Service.gI().sendDialogMessage(player, "Cần 1 trang bị có ô sao pha lê thứ 8 trở lên chưa cường hóa\n1 đá Hematite\n1 dùi đục");
            return;
        }
        int targetStarCuongHoa = starCuongHoa + 1;
        if (targetStarCuongHoa < 8) {
            targetStarCuongHoa = 8;
        } else if (targetStarCuongHoa > 9) {
            targetStarCuongHoa = 9;
        }
        StringBuilder text = new StringBuilder();
        text.append(ConstFont.BOLD_BLUE).append("Cường hóa\n");
        text.append(ConstFont.BOLD_BLUE).append("Ô Sao Pha lê thứ ").append(targetStarCuongHoa).append("\n");
        text.append(ConstFont.BOLD_GREEN).append("Cần 1 Hematite\n");
        text.append(ConstFont.BOLD_GREEN).append(trangBi.template.name).append("\n");
        text.append(ConstFont.BOLD_GREEN).append("Tỉ lệ thành công: 50%\n");
        text.append(player.inventory.getGemAndRuby() >= 50 ? ConstFont.BOLD_BLUE : ConstFont.BOLD_RED).append("Cần 50 ngọc");
        if (player.inventory.getGemAndRuby() < 50) {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, text.toString(), "Còn thiếu\n" + Util.numberToMoney(50 - player.inventory.getGemAndRuby()) + " ngọc");
            return;
        }
        CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, text.toString(), "Cường hóa", "Từ chối");
    }

    public static void cuongHoaLoSaoPhaLe(Player player) {
        if (InventoryService.gI().getCountEmptyBag(player) == 0) {
            Service.gI().sendThongBao(player, "Cần ít nhất 1 ô trống trong hành trang.");
            return;
        }

        if (player.inventory.gold < 500_000_000) {
            Service.gI().sendThongBao(player, "Con cần thêm vàng để cường hóa...");
            return;
        }

        if (player.combine.itemsCombine.isEmpty()) {
            Service.gI().sendThongBao(player, "Không có vật phẩm để cường hóa.");
            return;
        }

        Item item = null;       // Trang bị
        Item hematite = null;   // Đá hematite
        Item duiDuc = null;     // Dùi đục

        for (Item i : player.combine.itemsCombine) {
            if (i.template.type <= 5 || i.template.type == 32) {
                item = i;
            } else if (i.template.id == 1423) {
                hematite = i;
            } else if (i.template.id == 1438) {
                duiDuc = i;
            }
        }

        if (item == null || hematite == null || duiDuc == null
                || hematite.quantity < 1 || duiDuc.quantity < 1) {
            Service.gI().sendThongBao(player, "Thiếu vật phẩm hoặc số lượng không đủ.");
            return;
        }

        int star = 0;
        ItemOption opt228 = null;
        boolean hasOption218 = false;
        boolean hasOption102With7 = false;

        for (ItemOption io : item.itemOptions) {
            if (io.optionTemplate.id == 107) {
                star = io.param;
            } else if (io.optionTemplate.id == 228) {
                opt228 = io;
            } else if (io.optionTemplate.id == 218) {
                hasOption218 = true;
            } else if (io.optionTemplate.id == 102 && io.param == 7) {
                hasOption102With7 = true;
            }
        }

        if ((opt228 == null || opt228.param < 8) && !hasOption102With7) {
            Service.gI().sendThongBao(player, "Trang bị cần có đủ 7 lỗ để cường hóa.");
            return;
        }

        if (star < 8) {
            Service.gI().sendThongBao(player, "Vui lòng nâng cấp trang bị lên 8 hoặc 9 sao trước khi cường hóa.");
            return;
        }

        if (star == 8) {
            if (opt228 != null && opt228.param >= 8) {
                Service.gI().sendThongBao(player, "Trang bị đã có lỗ thứ 8.");
                return;
            }
        } else if (star == 9) {
            if (opt228 != null && opt228.param == 9) {
                Service.gI().sendThongBao(player, "Không thể cường hóa thêm.");
                return;
            }
        } else {
            Service.gI().sendThongBao(player, "Chỉ có thể cường hóa khi trang bị đạt 8 hoặc 9 sao.");
            return;
        }

        boolean firstHitSuccess = Util.isTrue(50, 100); // 50% cho hit mở lỗ 8
        boolean secondHitSuccess = Util.isTrue(50, 250); // 20% cho hit từ lỗ 8 lên 9
        player.inventory.gold -= 500_000_000;

        if (star == 8) {
            if (firstHitSuccess) {
                if (opt228 == null) {
                    if (!hasOption218) {
                        item.itemOptions.add(new ItemOption(218, 0));
                    }
                    item.itemOptions.add(new ItemOption(228, 8));
                } else {
                    opt228.param = 8;
                    if (!hasOption218) {
                        item.itemOptions.add(new ItemOption(218, 0));
                    }
                }
                CombineService.gI().sendEffectSuccessCombine(player);
            } else {
                CombineService.gI().sendEffectFailCombine(player);
            }

        } else if (star == 9) {
            // star == 9
            if (opt228 == null) {
                if (hasOption102With7) {
                    if (firstHitSuccess) {
                        if (!hasOption218) {
                            item.itemOptions.add(new ItemOption(218, 0));
                        }
                        item.itemOptions.add(new ItemOption(228, 8));
                        CombineService.gI().sendEffectSuccessCombine(player);
                    } else {
                        CombineService.gI().sendEffectFailCombine(player);
                    }
                } else {
                    CombineService.gI().sendEffectFailCombine(player);
                }

            } else if (opt228.param == 8) {
                if (secondHitSuccess) {
                    opt228.param = 9;
                    CombineService.gI().sendEffectSuccessCombine(player);
                } else {
                    CombineService.gI().sendEffectFailCombine(player);
                }

            } else {
                Service.gI().sendThongBao(player, "Trang bị không đủ điều kiện để cường hóa lên lỗ tiếp theo.");
                return;
            }
        }

        // Trừ vật phẩm (thành công hay thất bại đều trừ giống cấp 9 hiện tại)
        InventoryService.gI().subQuantityItemsBag(player, hematite, 1);
        InventoryService.gI().subQuantityItemsBag(player, duiDuc, 1);
        Service.gI().sendMoney(player);
        InventoryService.gI().sendItemBag(player);
        CombineService.gI().reOpenItemCombine(player);
    }
}
