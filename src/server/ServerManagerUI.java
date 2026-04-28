package server;

import HoandzManager.HoandzManager;
import consts.cn;
import item.Item;
import item.Item.ItemOption;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.prefs.Preferences;
import org.json.JSONArray;
import org.json.JSONObject;

import jdbc.daos.EventDAO;
import jdbc.daos.NDVSqlFetcher;
import jdbc.daos.PlayerDAO;
import models.Consign.ConsignShopManager;
import network.SessionManager;
import player.Bot.BotManager;
import services.ClanService;
import utils.Logger;
import player.Player;
import services.ItemService;
import services.Service;

public class ServerManagerUI extends JFrame {

    private JLabel ssCountLabel;
    private JLabel plCountLabel;
    private JLabel threadCountLabel;
    private JLabel messageLabel;
    private JLabel countdownLabel;
    private JLabel info;

    public ServerManagerUI() {
        Preferences.userNodeForPackage(ServerManagerUI.class);
        setTitle("CODE BY VKIEN " + cn.SV);
        ImageIcon icon = new ImageIcon(getClass().getResource("icon.png"));
        setIconImage(icon.getImage());
        setSize(820, 520);
        setResizable(false);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                confirmExit();
            }
        });

        initUI();
        setVisible(true);

        ServerManager.gI().run();
        HoandzManager.getInstance().startAutoSave();
    }

    private void initUI() {
        // Khung tổng thể
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        root.setBackground(new Color(245, 247, 250));
        setContentPane(root);

        // ====== Ô THỐNG KÊ (chia ô) ======
        JPanel statsBox = new JPanel(new GridLayout(2, 2, 12, 8));
        statsBox.setBorder(createBoxBorder("Thống kê"));
        statsBox.setBackground(Color.WHITE);

        threadCountLabel = styledStatLabel("Số Thread : 0");
        plCountLabel    = styledStatLabel("Online : 0 | Bot : 0");
        ssCountLabel    = styledStatLabel("Session : 0");
        info            = styledStatLabel("Thông tin khác ...");

        statsBox.add(threadCountLabel);
        statsBox.add(plCountLabel);
        statsBox.add(ssCountLabel);
        statsBox.add(info);

        root.add(statsBox, BorderLayout.NORTH);

        // ====== KHU VỰC NÚT (chia 2x2 ô) ======
        JPanel gridBoxes = new JPanel(new GridLayout(2, 2, 10, 10));
        gridBoxes.setOpaque(false);

        // Ô: QUẢN LÝ SERVER
        JPanel serverBox = createBox("Quản lý Server",
                createRainbowButton("Bảo trì", e -> showMaintenanceDialog()),
                createRainbowButton("Lưu Data", this::saveData),
                createRainbowButton("Clear Firewall", e -> {
//                    network.server.HOANDZServer.firewall.clear();
//                    network.server.HOANDZServer.firewallDownDataGame.clear();
                    JOptionPane.showMessageDialog(this, "Đã clear firewall");
                }),
                createRainbowButton("Update Shop", e -> {
                    Manager.gI().updateShop();
                    JOptionPane.showMessageDialog(this, "Update Shop Thành Công");
                }),
                createRainbowButton("Thông báo", e -> showBroadcastDialog())
        );

        // Ô: BUFF / ADMIN TOOL
        JPanel adminBox = createBox("Buff / Admin Tool",
                createRainbowButton("Buff VND", e -> createFormBuffVND()),
                createRainbowButton("Buff Hộp Thư", e -> createFromMailBox()),
                createRainbowButton("Buff JSON", e -> showBuffJsonDialog())
        );

        // Ô: THỜI GIAN / MESSAGE (placeholder gọn UI)
        JPanel infoBox = createBox("Thông báo phụ");
        messageLabel = new JLabel(" ", SwingConstants.LEFT);
        countdownLabel = new JLabel(" ", SwingConstants.LEFT);
        styleInfoLabel(messageLabel);
        styleInfoLabel(countdownLabel);
        infoBox.add(messageLabel);
        infoBox.add(countdownLabel);

        // Ô: HƯỚNG DẪN NHANH
        JPanel helpBox = createBox("Gợi ý thao tác");
        helpBox.add(makeHint("• Bảo trì: Dừng connect, lưu dữ liệu, thoát an toàn"));
        helpBox.add(makeHint("• Buff VND: Nhập tên & số tiền để cộng vào tài khoản"));
        helpBox.add(makeHint("• Buff Hộp Thư: Gửi item (id1,id2,...) + option a-b,c-d + số lượng"));
        helpBox.add(makeHint("• Thông báo: Gửi popup cho toàn bộ người chơi online"));
        helpBox.add(makeHint("• Update Shop: Reload tất cả shop trong game"));

        // thêm 4 ô vào lưới
        gridBoxes.add(serverBox);
        gridBoxes.add(adminBox);
        gridBoxes.add(infoBox);
        gridBoxes.add(helpBox);

        root.add(gridBoxes, BorderLayout.CENTER);

        // ====== CHẠY CẬP NHẬT THỐNG KÊ ======
        startScheduledTasks();
    }

    // ====== TẠO BORDER Ô ======
    private TitledBorder createBoxBorder(String title) {
        TitledBorder tb = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 230), 1, true),
                "  " + title + "  "
        );
        tb.setTitleFont(new Font("Segoe UI", Font.BOLD, 13));
        tb.setTitleColor(new Color(55, 71, 79));
        return tb;
    }

    // ====== TẠO Ô (PANEL) ======
    private JPanel createBox(String title, JComponent... children) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        p.setOpaque(true);
        p.setBackground(Color.WHITE);
        p.setBorder(createBoxBorder(title));
        for (JComponent c : children) {
            p.add(c);
        }
        return p;
    }

    // ====== LABEL THỐNG KÊ ======
    private JLabel styledStatLabel(String text) {
        JLabel lb = new JLabel(text);
        lb.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lb.setForeground(new Color(33, 33, 33));
        return lb;
    }

    // ====== LABEL INFO ======
    private void styleInfoLabel(JLabel lb) {
        lb.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lb.setForeground(new Color(66, 66, 66));
    }

    private JLabel makeHint(String text) {
        JLabel lb = new JLabel(text);
        styleInfoLabel(lb);
        return lb;
    }

    // ====== NÚT 7 MÀU (RAINBOW) ======
private JButton createRainbowButton(String text, AbstractAction action) {
    return createRainbowButton(text, (java.awt.event.ActionListener) action);
}
    private JButton createRainbowButton(String text, java.awt.event.ActionListener listener) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                // Nền bo tròn + màu hiện tại
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);

                // Viền mờ
                g2.setColor(new Color(255, 255, 255, 50));
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 18, 18);

                // Chữ
                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                String t = getText();
                int tx = (getWidth() - g2.getFontMetrics().stringWidth(t)) / 2;
                int ty = (getHeight() + g2.getFontMetrics().getAscent()) / 2 - 3;
                g2.drawString(t, tx, ty);
                g2.dispose();
            }
            @Override
            protected void paintBorder(Graphics g) { /* no-op */ }
        };
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setForeground(Color.WHITE);
        button.setPreferredSize(new Dimension(140, 40));

        // 7 màu rainbow
        Color[] rainbow = new Color[]{
                new Color(244, 67, 54),   // red
                new Color(255, 152, 0),   // orange
                new Color(255, 235, 59),  // yellow
                new Color(76, 175, 80),   // green
                new Color(33, 150, 243),  // blue
                new Color(156, 39, 176),  // purple
                new Color(233, 30, 99)    // pink
        };

        // Timer đổi màu mỗi 250ms
        final int[] idx = {0};
        Timer rainbowTimer = new Timer(250, e -> {
            button.setBackground(rainbow[idx[0]]);
            idx[0] = (idx[0] + 1) % rainbow.length;
            button.repaint();
        });
        rainbowTimer.start();

        // Hover: tăng sáng nhẹ (không dừng rainbow)
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBorder(BorderFactory.createLineBorder(new Color(255, 255, 255, 160), 2, true));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBorder(null);
            }
        });

        // Action
        if (listener != null) button.addActionListener(listener);
        return button;
    }

    // ====== CẬP NHẬT THỐNG KÊ ======
    private void startScheduledTasks() {
        ScheduledExecutorService threadCountExecutor = Executors.newSingleThreadScheduledExecutor();
        threadCountExecutor.scheduleAtFixedRate(() -> {
            int threadCount = Thread.activeCount();
            SwingUtilities.invokeLater(() -> threadCountLabel.setText("Số thread: " + threadCount));
        }, 1, 1, TimeUnit.SECONDS);

        ScheduledExecutorService plCountExecutor = Executors.newSingleThreadScheduledExecutor();
        plCountExecutor.scheduleAtFixedRate(() -> {
            int plcount = Client.gI().getPlayers().size();
            int botcount = BotManager.gI().bot.size();
            SwingUtilities.invokeLater(() -> plCountLabel.setText("Online : " + plcount + " | Bot : " + botcount));
        }, 5, 1, TimeUnit.SECONDS);

        ScheduledExecutorService ssCountExecutor = Executors.newSingleThreadScheduledExecutor();
        ssCountExecutor.scheduleAtFixedRate(() -> {
            int sscount = SessionManager.gI().getSessions().size();
            SwingUtilities.invokeLater(() -> ssCountLabel.setText("Session : " + sscount));
        }, 5, 1, TimeUnit.SECONDS);
    }

    // ====== HÀNH ĐỘNG ======
    private void saveData(ActionEvent e) {
        Logger.success("Đang tiến hành lưu data\n");
        network.server.HOANDZServer.gI().stopConnect();

        Maintenance.isRunning = false;
        try {
            Logger.error("Đang tiến hành lưu data bang hội\n");
            ClanService.gI().close();
            Thread.sleep(1000);
            Logger.success("Lưu dữ liệu bang hội thành công\n");
        } catch (Exception ex) {
            Logger.error("Lỗi lưu dữ liệu bang hội\n");
        }
        try {
            Logger.error("Đang tiến hành lưu data ký gửi\n");
            ConsignShopManager.gI().save();
            Thread.sleep(1000);
            Logger.success("Lưu dữ liệu ký gửi thành công\n");
        } catch (Exception ex) {
            Logger.error("Lỗi lưu dữ liệu ký gửi\n");
        }

        try {
            Logger.error("Đang tiến hành đẩy người chơi\n");
            Client.gI().close();
            EventDAO.save();
            Thread.sleep(1000);
            Logger.success("Lưu dữ liệu người dùng thành công\n");
        } catch (Exception ex) {
            Logger.error("Lỗi lưu dữ liệu người dùng\n");
        }
        System.exit(0);
    }

    private void showMaintenanceDialog() {
        int dialogButton = JOptionPane.YES_NO_OPTION;
        int ok = JOptionPane.showConfirmDialog(this, "Bắt đầu bảo trì?", "Bảo trì", dialogButton);
        if (ok == JOptionPane.YES_OPTION) {
            Logger.error("Server tiến hành bảo trì");
            Maintenance.gI().start(15);
        }
    }

    private void confirmExit() {
        int dialogButton = JOptionPane.YES_NO_OPTION;
        int ok = JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn thoát chương trình?", "Thoát", dialogButton);
        if (ok == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }

    // ====== Broadcast toàn server ======
    private void showBroadcastDialog() {
        String msg = JOptionPane.showInputDialog(this,
                "Nhập nội dung thông báo toàn server:",
                "Thông báo",
                JOptionPane.PLAIN_MESSAGE);
        if (msg == null || msg.trim().isEmpty()) return;

        int count = 0;
        for (Player p : Client.gI().getPlayers()) {
            try {
                if (p != null) {
                    Service.gI().sendThongBao(p, msg);
                    count++;
                }
            } catch (Exception ignore) {}
        }
        JOptionPane.showMessageDialog(this,
                "Đã gửi thông báo cho " + count + " người chơi.",
                "Thành công",
                JOptionPane.INFORMATION_MESSAGE);
    }

    // ====== Buff VND ======
    public void createFormBuffVND() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 6, 6));
        JTextField nameField = new JTextField();
        JTextField vndField = new JTextField();

        panel.add(new JLabel("Tên người chơi:"));
        panel.add(nameField);
        panel.add(new JLabel("VNĐ cần buff:"));
        panel.add(vndField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Buff VNĐ",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                String name = nameField.getText().trim();
                int addcash = Integer.parseInt(vndField.getText().trim());

                Player player = NDVSqlFetcher.loadPlayerByName(name);
                if (player != null) {
                    buffVND(name, addcash);
                } else {
                    JOptionPane.showMessageDialog(this, "Người chơi không tồn tại", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập số hợp lệ", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void buffVND(String name, int addcash) {
        try {
            if (PlayerDAO.addcash(name, addcash)) {
                JOptionPane.showMessageDialog(this, "Bạn đã buff cho " + name + " " + addcash + " VNĐ");
                Player player = Client.gI().getPlayerByName(name);
                if (player != null) {
                    player.getSession().cash += addcash;
                    Service.gI().sendThongBao(player, "Bạn vừa được cộng " + addcash + " VNĐ bởi Admin");
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Đã có lỗi xảy ra", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ====== Buff Hộp Thư ======
public void createFromMailBox() {
    JPanel panel = new JPanel(new GridLayout(0, 2, 6, 6));

    JTextField playerNameField = new JTextField();
    JTextField itemIdField = new JTextField();
    JTextField quantityField = new JTextField();
    JTextField optionIdsField = new JTextField();
    JTextField paramsField = new JTextField();

    panel.add(new JLabel("Tên người chơi:"));
    panel.add(playerNameField);
    panel.add(new JLabel("ID Trang Bị:"));
    panel.add(itemIdField);
    panel.add(new JLabel("Số lượng:"));
    panel.add(quantityField);

    panel.add(new JLabel("ID Option (vd: 50-77-103):"));
    panel.add(optionIdsField);
    panel.add(new JLabel("Param (vd: 11-11-11):"));
    panel.add(paramsField);

    int result = JOptionPane.showConfirmDialog(this, panel, "Buff Hộp Thư",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

    if (result == JOptionPane.OK_OPTION) {
        try {
            String playerName = playerNameField.getText().trim();
            String itemId = itemIdField.getText().trim();
            int quantity = Integer.parseInt(quantityField.getText().trim());

            if (quantity > 99999) {
                JOptionPane.showMessageDialog(this, "Số lượng tối đa là 99999", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String[] optionIds = optionIdsField.getText().trim().split("-");
            String[] params = paramsField.getText().trim().split("-");

            if (optionIds.length != params.length) {
                JOptionPane.showMessageDialog(this, "Số lượng Option ID và Param không khớp!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Ghép lại thành chuỗi "id-param,id-param,..."
            StringBuilder optionBuilder = new StringBuilder();
            for (int i = 0; i < optionIds.length; i++) {
                if (optionBuilder.length() > 0) {
                    optionBuilder.append(",");
                }
                optionBuilder.append(optionIds[i]).append("-").append(params[i]);
            }

            String options = optionBuilder.toString();
            sendItemToMailBox(playerName, itemId, options, quantity);

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập số hợp lệ", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}


    private void sendItemToMailBox(String playerName, String itemIds, String option, int quantity) {
        new Thread(() -> {
            if (playerName.equalsIgnoreCase("all")) {
                List<Player> allPlayers = NDVSqlFetcher.getAllPlayer();
                for (Player pBuffItem : allPlayers) {
                    if (pBuffItem != null) {
                        processItemBuff(pBuffItem, itemIds, option, quantity);
                    }
                }
            } else {
                Player pBuffItem = NDVSqlFetcher.loadPlayerByName(playerName);
                if (pBuffItem != null) {
                    processItemBuff(pBuffItem, itemIds, option, quantity);
                } else {
                    SwingUtilities.invokeLater(() ->
                            JOptionPane.showMessageDialog(this, "Người chơi không tồn tại", "Lỗi", JOptionPane.ERROR_MESSAGE));
                }
            }
        }).start();
    }

    private void processItemBuff(Player pBuffItem, String itemIds, String option, int quantity) {
        String[] itemIdsArray = itemIds.split(" ");
        for (String itemId : itemIdsArray) {
            int idItemBuff = Integer.parseInt(itemId.trim());
            Item itembuff = ItemService.gI().createNewItem((short) idItemBuff, quantity);

            if (option != null && !option.isEmpty()) {
                String[] options = option.split(" ");
                for (String opt : options) {
                    String[] optItem = opt.split("-");
                    int optID = Integer.parseInt(optItem[0].trim());
                    int param = Integer.parseInt(optItem[1].trim());
                    itembuff.itemOptions.add(new ItemOption(optID, param));
                }
            }

            pBuffItem.inventory.itemsMailBox.add(itembuff);
            if (NDVSqlFetcher.updateMailBox(pBuffItem)) {
                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(this,
                                "Đã gửi " + itembuff.template.name + " đến " + pBuffItem.name,
                                "Thành công", JOptionPane.INFORMATION_MESSAGE));
            }
        }
    }

    // ====== BUFF HÀNG LOẠT JSON ======
    private void showBuffJsonDialog() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        
        JLabel instructionLabel = new JLabel("<html>Nhập JSON theo định dạng:<br>" +
                "[{\"temp_id\":457,\"quantity\":750,\"options\":[{\"id\":50,\"param\":35}]},...]</html>");
        instructionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        
        JTextArea jsonTextArea = new JTextArea(10, 50);
        jsonTextArea.setFont(new Font("Courier New", Font.PLAIN, 12));
        jsonTextArea.setLineWrap(true);
        jsonTextArea.setWrapStyleWord(true);
        
        JScrollPane scrollPane = new JScrollPane(jsonTextArea);
        
        JPanel playerPanel = new JPanel(new BorderLayout(6, 6));
        JLabel playerLabel = new JLabel("Tên người chơi (hoặc 'all' cho tất cả):");
        JTextField playerNameField = new JTextField();
        playerPanel.add(playerLabel, BorderLayout.WEST);
        playerPanel.add(playerNameField, BorderLayout.CENTER);
        
        panel.add(instructionLabel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(playerPanel, BorderLayout.SOUTH);
        
        int result = JOptionPane.showConfirmDialog(this, panel, "Buff Hàng Loạt JSON",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            String playerName = playerNameField.getText().trim();
            String jsonInput = jsonTextArea.getText().trim();
            
            if (playerName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập tên người chơi", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (jsonInput.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập JSON", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            processBuffJson(playerName, jsonInput);
        }
    }
    
    private void processBuffJson(String playerName, String jsonInput) {
        new Thread(() -> {
            try {
                JSONArray jsonArray = new JSONArray(jsonInput);
                
                if (playerName.equalsIgnoreCase("all")) {
                    List<Player> allPlayers = NDVSqlFetcher.getAllPlayer();
                    for (Player player : allPlayers) {
                        if (player != null) {
                            processJsonItemsForPlayer(player, jsonArray);
                        }
                    }
                } else {
                    Player player = NDVSqlFetcher.loadPlayerByName(playerName);
                    if (player != null) {
                        processJsonItemsForPlayer(player, jsonArray);
                    } else {
                        SwingUtilities.invokeLater(() ->
                                JOptionPane.showMessageDialog(this, "Người chơi không tồn tại", "Lỗi", JOptionPane.ERROR_MESSAGE));
                    }
                }
                
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Lỗi JSON: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();
    }
    
    private void processJsonItemsForPlayer(Player player, JSONArray jsonArray) {
        final int[] successCount = {0};
        
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject itemObj = jsonArray.getJSONObject(i);
                
                int tempId = itemObj.getInt("temp_id");
                int quantity = itemObj.getInt("quantity");
                JSONArray optionsArray = itemObj.optJSONArray("options");
                
                Item item = ItemService.gI().createNewItem((short) tempId, quantity);
                
                if (optionsArray != null && optionsArray.length() > 0) {
                    for (int j = 0; j < optionsArray.length(); j++) {
                        JSONObject optObj = optionsArray.getJSONObject(j);
                        int optId = optObj.getInt("id");
                        int param = optObj.getInt("param");
                        item.itemOptions.add(new ItemOption(optId, param));
                    }
                }
                
                player.inventory.itemsMailBox.add(item);
                successCount[0]++;
                
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        
        if (NDVSqlFetcher.updateMailBox(player)) {
            SwingUtilities.invokeLater(() ->
                    JOptionPane.showMessageDialog(this,
                            "Đã gửi " + successCount[0] + " item đến " + player.name,
                            "Thành công", JOptionPane.INFORMATION_MESSAGE));
        } else {
            SwingUtilities.invokeLater(() ->
                    JOptionPane.showMessageDialog(this,
                            "Lỗi lưu dữ liệu hộp thư cho " + player.name,
                            "Lỗi", JOptionPane.ERROR_MESSAGE));
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ServerManagerUI::new);
    }
}
