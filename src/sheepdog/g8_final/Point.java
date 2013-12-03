package sheepdog.g8_final;

public class Point {
    public double x;
    public double y;

    public static double epsilon = 1e-6;

    public Point() { x = 0; y = 0; }

    public Point(double xx, double yy) {
        x = xx;
        y = yy;
    }

    public Point(Point o) {
        this.x = o.x;
        this.y = o.y;
    }

    public boolean equals(Point o) {
        int a = Math.abs(5);
        return Math.abs(o.x - x) < epsilon && Math.abs(o.y - y) < epsilon;
    }
}
