package sheepdog.g3;

import java.util.ArrayList;

import sheepdog.g3.Calculator.SIDE;
import sheepdog.sim.Point;

public class StraightLineBrain extends sheepdog.g3.DogBrain{

    private double DOG_SHEEP_MIN_DIST = 1; //TODO: tune parameter
    private static final double SHEEP_RUN_SPEED = 1.000;
    private static final double SHEEP_WALK_SPEED = 0.100;
    int prevClosestSheep = -1;
    boolean prevWhichSheep = true;
    public StraightLineBrain(int id, boolean advanced, int nblacks) {
        super(id,advanced,nblacks);
    }

    // Returns the center of the gap in the fence
    public Point getGapCoordinates()
    {
        Point gap = new Point(50,50);
        return gap;
    }

    // Returns a new position of dog
    public Point getBasicMove(Point[] dogs, Point[] sheeps)
    {
        Point me = dogs[mId];
        
        //If dog on the left side of fence, move dog towards the gap
        if(Calculator.getSide(dogs[mId].x) == SIDE.WHITE_GOAL_SIDE)
        {
            return Calculator.getMoveTowardPoint(me, getGapCoordinates());
        }
        else {
            Point gap = getGapCoordinates();
            ArrayList<Integer> undeliveredIndices = Calculator.undeliveredWhiteSheep(sheeps);
            int chosenSheepIndex = getClosestSheepToPoint(sheeps, gap, undeliveredIndices);
            Point targetSheep = sheeps[chosenSheepIndex];
            
            targetSheep = anticipateSheepMovement(me, targetSheep); 
            
            double angleGapToSheep = Calculator.getAngleOfTrajectory(gap, targetSheep);
            Point idealLocation = Calculator.getMoveInDirection(targetSheep, angleGapToSheep, DOG_SHEEP_MIN_DIST);
            Point moveLocation = Calculator.getMoveTowardPoint(me, idealLocation);
            makePointValid(moveLocation);
            return moveLocation;
        }

    }

    private Point anticipateSheepMovement(Point me, Point targetSheep) {
        double angleDogToSheep = Calculator.getAngleOfTrajectory(me, targetSheep);
        if (Calculator.withinRunDistance(targetSheep, me)) {
            targetSheep = Calculator.getMoveInDirection(targetSheep, angleDogToSheep, SHEEP_RUN_SPEED);
        }
        else if (Calculator.withinWalkDistance(targetSheep, me)) {
            targetSheep = Calculator.getMoveInDirection(targetSheep, angleDogToSheep, SHEEP_WALK_SPEED);
        }
        return targetSheep;
    }

    private int getClosestSheepToPoint(Point[] sheeps, Point pt,
            ArrayList<Integer> sheepToCheck) {
        int closestSheepToPoint = sheepToCheck.get(0);
        double minDistance = Calculator.dist(sheeps[closestSheepToPoint], pt);
        for (Integer sheepIndex : sheepToCheck) {
            double distance = Calculator.dist(sheeps[sheepIndex], pt);
            if (distance <= minDistance) {
                closestSheepToPoint = sheepIndex;
                minDistance = distance;
            }
        }
        return closestSheepToPoint;
    }

    @Override
    public Point getAdvancedMove(Point[] dogs, Point[] whiteSheep,
            Point[] blackSheep) {
        // Do nothing, this brain is only for a basic player
        return null;
    }
}
