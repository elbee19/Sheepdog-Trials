package sheepdog.g3;

import java.util.ArrayList;

import sheepdog.sim.Point;

public class StraightLineBrainGap extends StraightLineBrain {

    public StraightLineBrainGap(int id, boolean advanced, int nblacks) {
        super(id, advanced, nblacks);
    }

    @Override
    protected int chooseSheep(Point[] dogs, Point[] sheeps, ArrayList<Integer> undeliveredIndices) {
        ArrayList<Integer> sortedIndices = getDistanceSortedIndices(sheeps, GAP, undeliveredIndices);
        return sortedIndices.get(Math.min(mId, sortedIndices.size()-1));   
    }

}
