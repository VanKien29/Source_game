package task;

/*
 *
 *
 * @author CongHoan
 */

public class TaskPlayer {

    public TaskMain taskMain;

    public SideTask sideTask;

    public ClanTask clanTask;
    
    public KolTask kolTask;
    
    public TaskDanhHieu taskdh;

    public TaskPlayer() {
        this.sideTask = new SideTask();
        this.clanTask = new ClanTask();
        this.taskMain = new TaskMain();
        this.kolTask = new KolTask();
    }

    public void dispose() {
        this.taskMain = null;
        this.sideTask = null;
        this.clanTask = null;
        this.taskdh = null;
        if (this.kolTask != null) {
            this.kolTask.dispose();
            this.kolTask = null;
        }
    }

}
