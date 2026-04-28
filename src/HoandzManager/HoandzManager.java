/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package HoandzManager;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import jdbc.daos.PlayerDAO;

import server.Client;

/**
 *
 * @author Lucy An Trom
 */
public class HoandzManager {

    private static HoandzManager instance = null;

    // Static method
    // Static method to create instance of Singleton class
    public static synchronized HoandzManager getInstance() {
        if (instance == null) {
            instance = new HoandzManager();
        }
        return instance;
    }

    private ScheduledExecutorService scheduler;

    public void startAutoSave() {
        // int plcount = Client.gI().getPlayers().size();
        // int sscount = HoandzSessionManager.gI().getSessions().size();
        // if (sscount >= plcount + 10) {
        // Maintenance.gI().start(60);
        // }
        scheduler = Executors.newSingleThreadScheduledExecutor();

        scheduler.scheduleAtFixedRate(() -> {
            try {
                handleAutoSave();
            } catch (Exception e) {
                System.out.println("[AutoSaveManager] start autosave error: " + e.getLocalizedMessage());
            }
        }, 60, 90, TimeUnit.SECONDS);
    }

    public void handleAutoSave() {
        // System.out.println("[AutoSaveManager] start autosave sucessfully !!");
        //
        Client.gI().getPlayers().forEach(player -> {
            // long st = System.currentTimeMillis();
            PlayerDAO.updatePlayer(player);
            // Logger.success(TimeUtil.getCurrHour() + "h" + TimeUtil.getCurrMin() + "m: Tự
            // động lưu dữ liệu người chơi thành công! " + (System.currentTimeMillis() - st)
            // + "ms\n");

        });
    }

}
