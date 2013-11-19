package sheepdog.g9;

public abstract class Strategy {
    int id;
    int nblacks;
    boolean mode;

    public Strategy(int id, int nblacks, boolean mode) {
        this.nblacks = nblacks;
        this.mode = mode;
        this.id = id;
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