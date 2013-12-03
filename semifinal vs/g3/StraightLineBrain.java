package sheepdog.g3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import sheepdog.g3.Calculator.SIDE;
import sheepdog.sim.Point;

public abstract class StraightLineBrain extends sheepdog.g3.DogBrain{

    private double DOG_SHEEP_MIN_DIST = 1; 
    private static final double SHEEP_RUN_SPEED = 1.000;
    private static final double SHEEP_WALK_SPEED = 0.100;
    protected static final Point GAP = new Point(50, 50);
    int prevClosestSheep = -1;
    boolean prevWhichSheep = true;
    public StraightLineBrain(int id, boolean advanced, int nblacks) {
        super(id,advanced,nblacks);
    }

    // Returns a new position of dog
    public Point getBasicMove(Point[] dogs, Point[] sheeps)
    {
        Point me = dogs[mId];
        
        //If dog on the left side of fence, move dog towards the gap
        if(Calculator.getSide(dogs[mId].x) == SIDE.WHITE_GOAL_SIDE)
        {
            return Calculator.getMoveTowardPoint(me, GAP);
        }
        else {
            ArrayList<Integer> undeliveredIndices = Calculator.undeliveredWhiteSheep(sheeps);
            
            int chosenSheepIndex = chooseSheep(dogs, sheeps, undeliveredIndices);

            
            Point targetSheep = sheeps[chosenSheepIndex];
            targetSheep = anticipateSheepMovement(me, targetSheep); 
            
            double angleGapToSheep = Calculator.getAngleOfTrajectory(GAP, targetSheep);
            Point idealLocation = Calculator.getMoveInDirection(targetSheep, angleGapToSheep, DOG_SHEEP_MIN_DIST);
            Point moveLocation = Calculator.getMoveTowardPoint(me, idealLocation);
            makePointValid(moveLocation);
            return moveLocation;
        }

    }

    protected abstract int chooseSheep(Point[] dogs, Point[] sheeps, ArrayList<Integer> undeliveredIndices);

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
    
    protected ArrayList<Integer> getDistanceSortedIndices(final Point[] sheeps, final Point pt, ArrayList<Integer> sheepToCheck ) {
        Collections.sort(sheepToCheck, new Comparator<Integer>() {
            @Override
            public int compare(Integer arg0, Integer arg1) {
                return (int) Math.signum(Calculator.dist(sheeps[arg0], pt) - Calculator.dist(sheeps[arg1], pt));
            }
            
        });
        return sheepToCheck;
    }

    @Override
    public Point getAdvancedMove(Point[] dogs, Point[] whiteSheep,
            Point[] blackSheep) {
        // Do nothing, this brain is only for a basic player
        return null;
    }
}
