package Lighwheight;

public class LamportClock {
    int c;
    public LamportClock() {
        c = 0;
    }
    public int getValue() {
        return c;
    }
    public void tick() { //internal events
        c = c + 1;
    }
    public void sendAction() {
        c = c + 1; //include c in message
    }
    public void receiveAction(int src, int sentValue) {
        c = Math.max(c, sentValue) + 1;
    }
}
