package sheepdog.g3;

import java.util.ArrayList;
import java.util.Collections;

import sheepdog.sim.Point;

public class StraightLineBrainFar extends StraightLineBrain {

    public StraightLineBrainFar(int id, boolean advanced, int nblacks) {
        super(id, advanced, nblacks);
    }

    @Override
    protected int chooseSheep(Point[] dogs, Point[] sheeps, ArrayList<Integer> undeliveredIndices) {
      ArrayList<Integer> sortedIndices = getDistanceSortedIndices(sheeps, GAP, undeliveredIndices);
      Collections.reverse(sortedIndices);
      
      return sortedIndices.get(Math.min(mId, sortedIndices.size()-1));
    }

}
