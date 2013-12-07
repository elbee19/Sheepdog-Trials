package sheepdog.g3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import sheepdog.g3.Calculator.SIDE;
import sheepdog.sim.Point;

public abstract class StraightLineBrain extends sheepdog.g3.DogBrain{
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
        if(Calculator.getSide(dogs[mId].x) == SIDE.BLACK_GOAL_SIDE)
        {
            return Calculator.getMoveTowardPoint(me, GAP);
        }
        
        ArrayList<Integer> undeliveredIndices = Calculator.undeliveredBlackSheep(sheeps);

        return moveASheep(dogs, sheeps, me, undeliveredIndices);

    }
    
    @Override
    public Point getAdvancedMove(Point[] dogs, Point[] whiteSheep,
            Point[] blackSheep) {
        Point me = dogs[mId];
        SIDE mySide = Calculator.getSide(me.x);
        ArrayList<Integer> undeliveredIndices;
        Point[] sheeps;
        
        // Decide which side I'm on and which side to work at
        if (mySide == SIDE.BLACK_GOAL_SIDE) {
            undeliveredIndices = Calculator.undeliveredWhiteSheep(whiteSheep);
            sheeps = whiteSheep;
        }
        else if (mySide == SIDE.WHITE_GOAL_SIDE){
            undeliveredIndices = Calculator.undeliveredBlackSheep(blackSheep);
            sheeps = blackSheep;
        }
        else {
            ArrayList<Integer> blackIndices = Calculator.undeliveredBlackSheep(blackSheep);
            ArrayList<Integer> whiteIndices = Calculator.undeliveredWhiteSheep(whiteSheep);
            if (blackIndices.size() > whiteIndices.size()) {
                undeliveredIndices = blackIndices;
                sheeps = blackSheep;
            }
            else {
                undeliveredIndices = whiteIndices;
                sheeps = whiteSheep;
            }
        }
        
        // If there are no sheep to be delivered on this side go toward the gap
        if (undeliveredIndices.size() == 0) {
            return Calculator.getMoveTowardPoint(me, GAP);
        }
        
        return moveASheep(dogs, sheeps, me, undeliveredIndices);
    }

    protected abstract int chooseSheep(Point[] dogs, Point[] sheeps, ArrayList<Integer> undeliveredIndices);

    protected ArrayList<Integer> getDistanceSortedIndices(final Point[] sheeps, final Point pt, ArrayList<Integer> sheepToCheck ) {
        Collections.sort(sheepToCheck, new Comparator<Integer>() {
            @Override
            public int compare(Integer arg0, Integer arg1) {
                return (int) Math.signum(Calculator.dist(sheeps[arg0], pt) - Calculator.dist(sheeps[arg1], pt));
            }
            
        });
        return sheepToCheck;
    }

    private Point moveASheep(Point[] dogs, Point[] sheeps, Point me,
            ArrayList<Integer> undeliveredIndices) {
        int chosenSheepIndex = chooseSheep(dogs, sheeps, undeliveredIndices);
        
        Point targetSheep = sheeps[chosenSheepIndex];
        return forceSheepToMove(targetSheep, me);
    }
}
