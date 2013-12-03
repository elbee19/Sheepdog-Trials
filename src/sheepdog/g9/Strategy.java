package sheepdog.g9;

import java.util.LinkedList;

public abstract class Strategy {
    int id;
    LinkedList<Strategy> strategyStack;

    public Strategy(int id, LinkedList<Strategy> strategyStack) {
        this.id = id;
        this.strategyStack = strategyStack;
    }

    // optional
    public String name;
    // optional: things to be done when switching to this strategy
    public void init(int id) {}
    // optional
    public String toString() {
        return "";
    }

    // required
    public abstract Point move(Point[] dogs, Point[] sheeps);

}