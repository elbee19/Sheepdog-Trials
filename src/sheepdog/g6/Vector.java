package sheepdog.g6;

import sheepdog.sim.Point;
import java.util.*;

public class Vector {

    public Vector(double x, double y) {
        this.origin = null;
        this.dest = null;
        this.v = new Point(x, y);
    }

    public Vector(Point a, Point b) {
        this.origin = a;
        this.dest = b;
        this.v = new Point(dest.x-origin.x, dest.y-origin.y);
    }

    public double magnitude() {
        return Math.sqrt(Math.pow(v.x,2) + Math.pow(v.y,2));
    }

    public Point get() {
        return v;
    }

    public Vector get_unit() {
	if (magnitude() == 0) {
	    return new Vector(0, 0);
	}
        return new Vector(v.x/magnitude(), v.y/magnitude());
    }

    public void times(Vector other) {
        v.x = v.x*other.v.x;
        v.y = v.y*other.v.y;
    }

    public void times(double c) {
        v.x = v.x*c;
        v.y = v.y*c;
    }

    public void plus(Vector other) {
        v.x = v.x+other.v.x;
        v.y = v.y+other.v.y;
    }

    public void plus(Point other) {
        v.x = v.x+other.x;
        v.y = v.y+other.y;
    }

    public void reverse() {
        v.x = -1*v.x;
        v.y = -1*v.y;
    }

    public void divided(Vector other) {
        v.x = v.x/other.v.x;
        v.y = v.y/other.v.y;
    }

    public Point toPoint() {
        return new Point(v.x, v.y);
    }

    public String toString() {
        return "< " + v.x + " , " + v.y + " >";
    }

    private Point origin;
    private Point dest;
    private Point v;
}