package sheepdog.g8_final;
import sheepdog.sim.Point;

import java.util.ArrayList;

public class Zone{

	Point upperLeft;
	Point upperRight;
	Point lowerLeft;
	Point lowerRight;
	double horizontalLength;
	double verticalLength;
	Point goalPoint;
	boolean isEmpty;

    public static Point GATE = new Point (50, 50);
    public static Point DOGHOUSE = new Point (100, 50);

	public Zone(Point upperLeft, Point goalPoint, double w, double h){
		horizontalLength = w;
		verticalLength = h;
		this.upperLeft = upperLeft;
		this.upperRight = new Point(upperLeft.x+horizontalLength, upperLeft.y);
		this.lowerLeft = new Point(upperLeft.x, upperLeft.y+verticalLength);
		this.lowerRight = new Point(upperLeft.x+horizontalLength, upperLeft.y+verticalLength);
		this.goalPoint = goalPoint;
		isEmpty = false;
	}

    // Adds two zones together to create a new zone that is the size of the combined zones
    // Note: doesn't handle all combinations
    public static Zone addZones(Zone... zones) {
        Point upperLeft = null;
        double width = 0;
        double height = 0;
        Point goalPoint = null;

        Zone leftMostZone = null;
        double leftDistance = 100.0;
        Zone topMostZone = null;
        double topDistance = 100.0;


        for (Zone z: zones) {
            if (z.isMiddleZone()) {
                goalPoint = z.goalPoint;
            }

            if (z.upperLeft.x < leftDistance) {
                leftDistance = z.upperLeft.x;
                leftMostZone = z;
            }

            if (z.upperLeft.y < topDistance) {
                topDistance = z.upperLeft.y;
                topMostZone = z;
            }
        }

        if (zones.length == 2) {
            if (Calculator.pointsEqual(zones[0].goalPoint, zones[1].goalPoint)) {
                upperLeft = topMostZone.upperLeft;
                goalPoint = topMostZone.goalPoint;
                width = 25.0;
                height = 33.333;
            } else {
                upperLeft = leftMostZone.upperLeft;
                goalPoint = leftMostZone.goalPoint;
                width = 50.0;
                height = 33.333;
            }
        } else if (zones.length == 3) {
            upperLeft = topMostZone.upperLeft;
            height = 100.0;
            width = 33.333;
        } else if (zones.length == 6) {
            upperLeft = new Point(50, 0);
            goalPoint = new Point(50, 50);
            width = 50.0;
            height = 100.0;
        }

        return new Zone(upperLeft, goalPoint, width, height);
    }

    public Point getCenter() {
        return new Point((upperLeft.x + lowerRight.x) / 2, (upperLeft.y + lowerRight.y) /2);
    }

    public boolean isMiddleZone() {
        if (Calculator.pointsEqual(this.goalPoint, GATE))
            return true;
        return false;
    }

	public Point getUpperRight(){
		return upperRight;
	}

	public Point getUpperLeft(){
		return upperLeft;
	}

	public Point getLowerLeft(){
		return lowerLeft;
	}

	public Point getLowerRight(){
		return lowerRight;
	}

	public double getHorizontalLength(){
		return horizontalLength;
	}

	public double getVerticalLength(){
		return verticalLength;
	}

	public Point getGoal(){
		return goalPoint;
	}

	public boolean containsSheep(Point p){
		if (p.x >= upperLeft.x && p.x <= upperRight.x && p.y >= upperRight.y && p.y <= lowerRight.y)
			return true;
		return false;
	}

    public ArrayList<Integer> getSheepIndices(Point[] sheeps) {
        ArrayList<Integer> sheepIndices = new ArrayList<Integer>();

        for (int i = 0; i < sheeps.length; i++) {
            if (this.containsSheep(sheeps[i])) {
                sheepIndices.add(i);

            }
        }
        return sheepIndices;
    }

    public ArrayList<Integer> getDogIndices(Point[] dogs) {
        ArrayList<Integer> dogIndices = new ArrayList<Integer>();

        for (int i = 0; i < dogs.length; i++) {
            if (this.containsSheep(dogs[i])) {
                dogIndices.add(i);

            }
        }
        return dogIndices;
    }

    public int numSheep(Point[] sheeps) {
        return getSheepIndices(sheeps).size();
    }

    public int numDogs(Point[] dogs) {
        return getSheepIndices(dogs).size();
    }

	public boolean hasSheep(Point[] sheeps) {
		return getSheepIndices(sheeps).size() > 0;
	}

    public boolean hasNoDogs(Point[] dogs) {
        return getSheepIndices(dogs).size() == 0;
    }
	
	public void setEmpty(boolean b){
		isEmpty = b;
	}

}
