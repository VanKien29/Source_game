package jdbc.daos;

/*
 *
 *
 * @author CongHoan
 */

import item.Item;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import shop.ItemShop;
import shop.Shop;
import shop.TabShop;
import services.ItemService;
import utils.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ShopDAO {

    public static List<Shop> getShops(Connection con) {
        List<Shop> list = new ArrayList<>();
        try {
            PreparedStatement ps = con.prepareStatement("select * from shop order by npc_id asc");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Shop shop = new Shop();
                shop.id = rs.getInt("id");
                shop.npcId = rs.getByte("npc_id");
                shop.tagName = rs.getString("tag_name");
                shop.typeShop = rs.getByte("type_shop");
                loadShopTab(con, shop);
                list.add(shop);
            }
            try {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
            } catch (SQLException ex) {
            }
        } catch (Exception e) {
            Logger.logException(ShopDAO.class, e);
        }
        return list;
    }

    private static void loadShopTab(Connection con, Shop shop) {
        try {
            PreparedStatement ps = con
                    .prepareStatement("select * from tab_shop where shop_id = ? order by tab_index asc");
            ps.setInt(1, shop.id);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                TabShop tab = new TabShop();
                tab.shop = shop;
                tab.id = rs.getInt("id");
                tab.name = rs.getString("tab_name").replaceAll("<>", "\n");
                tab.index = rs.getInt("tab_index");
                loadItemShop(con, tab);
                shop.tabShops.add(tab);
            }
            try {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
            } catch (SQLException ex) {
            }
        } catch (Exception e) {
            Logger.logException(ShopDAO.class, e);
        }
    }

    private static void loadItemShop(Connection con, TabShop tabShop) {
        try {
            PreparedStatement ps = con.prepareStatement("select * from tab_shop where tab_index = ? and shop_id = ?");
            ps.setInt(1, tabShop.index);
            ps.setInt(2, tabShop.shop.id);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Object parsedItems = JSONValue.parse(rs.getString("items"));
                if (!(parsedItems instanceof JSONArray)) {
                    Logger.warning("Bỏ qua tab_shop #" + rs.getInt("id")
                            + " vì cột items không phải JSON array hợp lệ\n");
                    continue;
                }
                JSONArray dataArray = (JSONArray) parsedItems;
                for (Object o : dataArray) {
                    if (!(o instanceof JSONObject)) {
                        Logger.warning("Bỏ qua một item không hợp lệ trong tab_shop #" + rs.getInt("id") + "\n");
                        continue;
                    }
                    try {
                        JSONObject dataObject = (JSONObject) o;
                        ItemShop itemShop = new ItemShop();
                        itemShop.tabShop = tabShop;
                        itemShop.id = tabShop.itemShops.size() + 1;
                        itemShop.temp = ItemService.gI()
                                .getTemplate(Short.parseShort(String.valueOf(dataObject.get("temp_id"))));
                        itemShop.isNew = Boolean.parseBoolean(String.valueOf(dataObject.get("is_new")));
                        itemShop.cost = Integer.parseInt(String.valueOf(dataObject.get("cost")));
                        itemShop.iconSpec = Integer.parseInt(String.valueOf(dataObject.get("item_spec")));
                        itemShop.typeSell = Byte.parseByte(String.valueOf(dataObject.get("type_sell")));
                        Object parsedOptions = dataObject.get("options");
                        if (parsedOptions instanceof JSONArray) {
                            JSONArray options = (JSONArray) parsedOptions;
                            for (Object option : options) {
                                if (!(option instanceof JSONObject)) {
                                    continue;
                                }
                                JSONObject opt = (JSONObject) option;
                                itemShop.options.add(new Item.ItemOption(
                                        Integer.parseInt(String.valueOf(opt.get("id"))),
                                        Integer.parseInt(String.valueOf(opt.get("param")))));
                            }
                        }
                        if (Boolean.parseBoolean(String.valueOf(dataObject.get("is_sell")))) {
                            tabShop.itemShops.add(itemShop);
                        }
                    } catch (Exception exception) {
                        Logger.warning("Bỏ qua item lỗi trong tab_shop #" + rs.getInt("id")
                                + ": " + exception.getMessage() + "\n");
                    }
                }
            }
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException ex) {
            }
        } catch (Exception e) {
            Logger.logException(ShopDAO.class, e);
        }
    }

    // private static void loadItemShopOption(Connection con, ItemShop itemShop) {
    // try {
    // PreparedStatement ps = con.prepareStatement("select * from item_shop_option
    // where item_shop_id = ?");
    // ps.setInt(1, itemShop.id);
    // ResultSet rs = ps.executeQuery();
    // while (rs.next()) {
    // itemShop.options.add(new Item.ItemOption(rs.getInt("option_id"),
    // rs.getInt("param")));
    // }
    // try {
    // if (rs != null) {
    // rs.close();
    // }
    // if (ps != null) {
    // ps.close();
    // }
    // } catch (SQLException ex) {
    // }
    // } catch (Exception e) {
    // Logger.logException(ShopDAO.class, e);
    // }
    // }

}
