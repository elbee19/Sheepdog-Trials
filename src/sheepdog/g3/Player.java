package sheepdog.g3;

import java.util.ArrayList;
import java.util.Arrays;

import sheepdog.g3.Calculator.SIDE;
import sheepdog.sim.Point;

public class Player extends sheepdog.sim.Player  {
    private int mNblacks;
    private boolean mMode;
    private DogBrain mBrain = null;
    boolean sweeperComplete = false;
    boolean hullComplete = false;
    private static final double STRAIGHT_LINE_PER_SHEEP = 66.482;
    private static final double HULL_COEFFICIENT = 669.9174713;
    private static final double HULL_DOGS_COEFF = 16.12311084;
    private static final double HULL_NSHEEP_COEFF = -3.596776743E-1;
    private static final double HULL_LNSHEEP_COEFF = 187.436683;
    private static final double HULL_LNSHEEPDOG_COEFF = 747.8933213;
    private static final double HULL_OFFSET = -897.5060212;
    private static final double SWEEP_MIN_DOGS = 26;
    private static final double SWEEP_DOGS_COEFF = -60.90625;
    private static final double SWEEP_SHEEP_COEFF =  9.493333333E-2;
    private static final double SWEEP_OFFSET = 2237.554167;

    @Override
    public void init(int nblacks, boolean mode) {
        mNblacks = nblacks;
        mMode = mode;
        sweeperComplete = false;
        //    mBrain = new StraightLineBrainGap(id, mode, nblacks);
        //    mBrain = new StraightLineBrainFar(id, mode, nblacks);
//            mBrain = new StraightLineBrainMe(id, mode, nblacks);
        //    mBrain = new SteinerBrain(id, mode, nblacks);
//            mBrain = new ConvexHullBrain(id, mode, nblacks);
//        mBrain = new SweeperBrain(id, mMode, mNblacks);

    }

    @Override
    public Point move(Point[] dogs, Point[] sheeps) {
        if(!sweeperComplete) {
            sweeperComplete = isSweeperComplete(dogs,sheeps);
        }
        int nSheeps;
        boolean existsUndeliveredBlackSheep;
        if (mMode) {
            int nUndeliveredBlackSheep = Calculator.undeliveredBlackSheep(Arrays.copyOfRange(sheeps, 0, mNblacks)).size();
            int nUndeliveredWhiteSheep = Calculator.undeliveredWhiteSheep(Arrays.copyOfRange(sheeps, mNblacks, sheeps.length)).size();
            existsUndeliveredBlackSheep = nUndeliveredBlackSheep > 0;
            nSheeps = nUndeliveredBlackSheep + nUndeliveredWhiteSheep;
        }
        else {
            nSheeps = Calculator.undeliveredBlackSheep(sheeps).size();
            existsUndeliveredBlackSheep = nSheeps > 0;
        }
        
        double straightLineEstimate = STRAIGHT_LINE_PER_SHEEP * nSheeps / dogs.length;
        double lnSheeps = Math.log(nSheeps);
        double lnSheepsDogs = Math.log(nSheeps) / dogs.length;
        double hullEstimate = HULL_NSHEEP_COEFF * nSheeps 
                + HULL_DOGS_COEFF * dogs.length 
                + HULL_LNSHEEP_COEFF * lnSheeps 
                + HULL_LNSHEEPDOG_COEFF * lnSheepsDogs 
                + HULL_OFFSET;
        double sweepEstimate = SWEEP_DOGS_COEFF * dogs.length 
                + SWEEP_SHEEP_COEFF * nSheeps 
                + SWEEP_OFFSET;

        if(mMode 
            || sweeperComplete 
            || dogs.length < 26 
            || straightLineEstimate < sweepEstimate 
            || hullEstimate > sweepEstimate) {
            if(!hullComplete && hullEstimate < straightLineEstimate  && existsUndeliveredBlackSheep) {
                mBrain = new ConvexHullBrain(id, mMode, mNblacks);
            }
            else {
                hullComplete = true;
                mBrain = new StraightLineBrainMe(id, mMode, mNblacks);
            }
        }
        else {
            mBrain = new SweeperBrain(id, mMode, mNblacks);
        }

        return mBrain.getMove(dogs, sheeps);
    }

    boolean isSweeperComplete(Point [] dogs, Point[] sheeps) {

        ArrayList<Integer> undeliveredIndices = new ArrayList<Integer>();

        if(mMode)
        {
            Point[] blackSheep = Arrays.copyOfRange(sheeps, 0, mNblacks);
            for(int i=0;i<blackSheep.length;i++)
            {
                if(blackSheep[i].x > 50)
                    undeliveredIndices.add(i);
            }
        }
        else
        {
            for(int i=0;i<sheeps.length;i++)
            {
                if(sheeps[i].x > 50)
                    undeliveredIndices.add(i);
            }
        }

        if((float)undeliveredIndices.size()/sheeps.length <= 0.15)
            return true;
        else
            return false;
    }

}

