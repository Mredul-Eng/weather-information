public class LamportClock {
    private int time;

    public LamportClock() {
        this.time = 0;
    }

    public synchronized int getTime() {
        return time;
    }

    public synchronized void updateTime(int receivedTime) {
        time = Math.max(time, receivedTime) + 1;
    }

    public synchronized void increaseTime() {
        time++;
    }
    public int send(){
        increaseTime();
        return time;
    }
}
