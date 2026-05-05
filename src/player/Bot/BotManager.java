
package player.Bot;

import java.util.ArrayList;
import java.util.List;
import server.ServerManager;

/**
 *
 * @author Bùi Công Hoan
 */
public class BotManager implements Runnable {

    public static BotManager i;
    
    public List<Bot> bot =  new ArrayList<>();
    private long lastSmartPartyUpdate;
    
    
    public static BotManager gI(){
        if(i == null){
            i = new BotManager();
        }
            return i;
    }
       @Override
    public void run() {
        while (ServerManager.isRunning) {
            try {
                long st = System.currentTimeMillis();
                if (System.currentTimeMillis() - lastSmartPartyUpdate >= 5_000) {
                    SmartBotAI.gI().updateParties(this.bot);
                    lastSmartPartyUpdate = System.currentTimeMillis();
                }
                for (Bot bot : new ArrayList<>(this.bot)) {
                    if (bot != null) {
                        bot.update();
                    }
                }
                long sleep = 150 - (System.currentTimeMillis() - st);
                Thread.sleep(Math.max(10, sleep));
            } catch (Exception ignored) {
            }

        }
    }

    public void openPemQuaiMenuAdmin() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    public void openBanItemMenuAdmin() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    public void openSanBossMenuAdmin() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    public void summonToPlayerAdmin(int count) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}
