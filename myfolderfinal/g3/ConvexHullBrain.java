package sheepdog.g3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import sheepdog.g3.Calculator.SIDE;
import sheepdog.sim.Point;

public class ConvexHullBrain extends DogBrain {
    private final double MAX_DIFF = 10.0;
    double zoneAngleSize;
    double myAngleStart;
    double myAngleEnd;
    boolean clockwise = false;
    

    public ConvexHullBrain(int id, boolean advanced, int nblacks) {
        super(id, advanced, nblacks);
        
    }

    @Override
    public Point getAdvancedMove(Point[] dogs, Point[] whiteSheep,
            Point[] blackSheep) {
        // TODO Expand strategy to advanced game
        return null;
    }

    @Override
    public Point getBasicMove(Point[] dogs, Point[] sheep) {
        Point me = dogs[mId];
        zoneAngleSize = Math.PI/dogs.length;
        myAngleStart = -Math.PI/2 + mId * zoneAngleSize;
        myAngleEnd = myAngleStart + zoneAngleSize;
        if (Calculator.getSide(me.x) == SIDE.BLACK_GOAL_SIDE) {
            return Calculator.getMoveTowardPoint(me, GAP);
        }
        ArrayList<Integer> undeliveredIndices = Calculator.undeliveredBlackSheep(sheep);
        ArrayList<Integer> hull = getConvexHull(sheep, undeliveredIndices);
        
        boolean massed = sheepMassed(sheep, hull);
        Point sheepTarget;
        if (massed) {
            sheepTarget = getFurthestSheep(undeliveredIndices, sheep, dogs);
        }
        else {
            sheepTarget = getSheepToTravelTo(hull, sheep, dogs);
        }
        
        if (sheepTarget.x == 50.0) {
            return Calculator.getMoveTowardPoint(me, sheepTarget);
        }
        return forceSheepToMove(sheepTarget, me);
    }

    private boolean sheepMassed(Point[] sheep, ArrayList<Integer> hull) {
        boolean massed = false;
        Point min = sheep[hull.get(0)];
        Point max = sheep[hull.get(0)];
        for (int i : hull) {
            if (sheep[i].y < Calculator.FIELD_SIZE * 0.5 && sheep[i].y < min.y) { min = sheep[i]; }
            if (sheep[i].y > Calculator.FIELD_SIZE * 0.5 && sheep[i].y < max.y) { max = sheep[i]; }
        }
        double diffTop = Math.abs(max.y - Calculator.FIELD_SIZE);
        double diffBottom = Math.abs(min.y - Calculator.FIELD_SIZE);
        if (diffTop < MAX_DIFF || diffBottom < MAX_DIFF) {
            massed = true;
        }
        return massed;
    }
    
    private Point getFurthestSheep(ArrayList<Integer> undeliveredIndices,
            Point[] sheep, Point[] dogs) {
        
        double greatestDist = 0.0;
        int greatestIndex = -1;
        for (int i : undeliveredIndices) {
            if (Calculator.dist(GAP, sheep[i]) > greatestDist) {
                greatestIndex = i;
                greatestDist = Calculator.dist(GAP, sheep[i]);
            }
        }
        
        return sheep[greatestIndex];
    }

    /**
     * Generates a convex hull for a given array of sheep
     * @param sheep
     * @return An array list of the points making up the convex hull
     */
    private ArrayList<Integer> getConvexHull(Point[] sheep, ArrayList<Integer> undeliveredIndices) {
        ArrayList<Integer> hull = new ArrayList<Integer>();
        Point max = sheep[undeliveredIndices.get(0)], min = sheep[undeliveredIndices.get(0)];

        for(Integer i : undeliveredIndices) {
            Point p = sheep[i];
            if (p.y > max.y) { max = p; }
            if (p.y < min.y) { min = p; }
        }
        Point wallMax = new Point(Calculator.FIELD_SIZE*0.5, max.y);
        Point wallMin = new Point(Calculator.FIELD_SIZE*0.5, min.y);
        if (wallMax.y > 50) { wallMax.y = 50.0; }
        if (wallMin.y > 50) { wallMin.y = 50.0; }
        Point p1 = wallMin;
        
        for(;;) {
            Point p2 = null;
            double minAngle = Math.PI+1;
            int minIndex = -1;
            for (Integer i : undeliveredIndices) {
                Point candidate = sheep[i];
                double angle = Calculator.getAngleOfTrajectory(p1, candidate);
                
                //solve atan2 problem
                if(angle<0)
                	angle+=2*Math.PI;
                
                if (angle < minAngle) {
                    minAngle = angle;
                    p2 = candidate;
                    minIndex = i;
                }
            }
            hull.add(minIndex);
            if (undeliveredIndices.size() > 0) {
                undeliveredIndices.remove(new Integer(minIndex));
            }
            if (p2.y == max.y) {
                break;
            }
            p1 = sheep[hull.get(hull.size()-1)];
        }
        return hull;
        
    }
    
    /**
     * Chooses the next sheep this dog should move toward
     */
    private Point getSheepToTravelTo(ArrayList<Integer> hull, Point[] sheeps, Point[] dogs) {
        ArrayList<Integer> choices = new ArrayList<Integer>();
        double maxLeftAngle = -Math.PI;
        int leftmost = -1;
        double minRightAngle = Math.PI;
        int rightmost = -1;
        
        // Find sheep just before, just after, and within the angle
        for(Integer sheepIndex : hull) {
            Point sheep = sheeps[sheepIndex];
            double sheepAngle = Calculator.getAngleOfTrajectory(GAP, sheep);
            System.out.println("Sheep Angle: " + sheepAngle + " start: " + myAngleStart + "end: " + myAngleEnd);
            System.out.println("MaxLeftAngle: " + maxLeftAngle + " MinRightAngle: " + minRightAngle);
            if (sheepAngle > myAngleStart && sheepAngle < myAngleEnd) {
                System.out.println("New choice " + sheep);
                choices.add(sheepIndex);
            }
            else if (sheepAngle <= myAngleStart && sheepAngle > maxLeftAngle) {
                System.out.println("New leftmost " + sheep);
                maxLeftAngle = sheepAngle;
                leftmost = sheepIndex;
            }
            else if (sheepAngle >= myAngleEnd && sheepAngle < minRightAngle) {
                System.out.println("New rightmost " + sheep);
                minRightAngle = sheepAngle;
                rightmost = sheepIndex;
            }
        }
        if (leftmost >= 0) {
            choices.add(leftmost);
        }
        if (rightmost >= 0) {
            choices.add(rightmost);
        }
        
        int nextGoal = -1;
        double dogAngle = Calculator.getAngleOfTrajectory(GAP, dogs[mId]);
        if (dogAngle == 0 || choices.size() == 1) { return sheeps[choices.get(0)]; } 
        double leastDist = Calculator.FIELD_SIZE;
      
        for (Integer sheepIndex : choices) {
            Point sheep = sheeps[sheepIndex];
            double dogToSheep = Calculator.dist(dogs[mId], sheep);
            boolean closetSheep = dogToSheep < leastDist;
            
            if (closetSheep) {
                leastDist = dogToSheep;
                nextGoal = sheepIndex;
            }
        }
        return sheeps[nextGoal];
    }
    
    /**
     * Chooses the best location to stand within Calculator.DOG_MAX_SPEED of a while
     * moving along a line toward b
     */
    private Point getStepAlongPathTo(Point a, Point b) {
        return null;
    }

}
