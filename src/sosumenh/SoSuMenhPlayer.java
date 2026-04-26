package sosumenh;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import player.Player;

/*
 * @author BTH Cute Phô Mai Que cc xấu hơn đức
 */
@Data
public class SoSuMenhPlayer {

    private int point;
    private int level;
    private boolean vip;

    public Player player;

    // ===== FIX: KHÔNG BAO GIỜ ĐỂ NULL =====
    public List<SoSuMenhTaskMain> ssmTaskMain;

    public boolean reward[] = new boolean[20];
    public boolean rewardVip[] = new boolean[20];

    public SoSuMenhPlayer(Player player) {
        this.player = player;

        // ===== INIT LIST NGAY TẠI ĐÂY =====
        this.ssmTaskMain = new ArrayList<>();
    }

    public void addlevel(int count) {
        level += count;
        addPoint(count * 100);
    }

    public void addPoint(int point) {
        this.point += point;
        level = this.point / 100;
    }

    public SoSuMenhTaskMain getTaskById(int id) {
        if (ssmTaskMain == null) {
            return null;
        }
        for (SoSuMenhTaskMain ssm : ssmTaskMain) {
            if (ssm.idTask == id) {
                return ssm;
            }
        }
        return null;
    }

    public void addCountTask(int id) {
        if (ssmTaskMain == null) {
            return;
        }

        SoSuMenhTaskMain ssm = getTaskById(id);
        if (ssm == null || ssm.finish) {
            return;
        }

        SoSuMenhTaskTemplate smmtem =
                SoSuMenhManager.getInstance().findById(id);
        if (smmtem == null) {
            return;
        }

        // ===== CỘNG TIẾN ĐỘ =====
        ssm.countTask++;

        if (ssm.countTask >= smmtem.getMaxCount()) {
            ssm.countTask = smmtem.getMaxCount();
            ssm.finish = true;

            addPoint(smmtem.getPoint());
        }
    }
}
