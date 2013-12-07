package sheepdog.g9;

public class Point {
    public double x;
    public double y;

    public Point() { x = 0; y = 0; }

    public Point(double xx, double yy) {
        x = xx;
        y = yy;
    }

    public Point(sheepdog.sim.Point p) {
        x = p.x;
        y = p.y;
    }

    public double dot(Point p) {
        return x * p.x + y * p.y;
    }
    
    public Point scale(double d) {
        x = x * d;
        y = y * d;
        return this;
    }

    public double distance(Point p) {
        return Math.sqrt((p.x - x)*(p.x - x) + (p.y - y)*(p.y - y));
    }

    public boolean equals(Point o) {
        return o.x == x && o.y == y;
    }
    
    public sheepdog.sim.Point toSimPoint() {
        return new sheepdog.sim.Point(x, y);
    }

    public String toString() {
        return String.format("x = %f, y = %f", x, y);
    }
}
