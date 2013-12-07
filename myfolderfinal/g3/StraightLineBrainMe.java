package sheepdog.g3;

import java.util.ArrayList;

import sheepdog.sim.Point;

public class StraightLineBrainMe extends StraightLineBrain {

    public StraightLineBrainMe(int id, boolean advanced, int nblacks) {
        super(id, advanced, nblacks);
    }

    @Override
    protected int chooseSheep(Point[] dogs, Point[] sheeps, ArrayList<Integer> undeliveredIndices) {
        Point me = dogs[mId];
        int chosenSheepIndex = -1;
        
        ArrayList<Integer> sortedIndices = getDistanceSortedIndices(sheeps, me, undeliveredIndices);
        ArrayList<Integer> dogDistances[] = new ArrayList[dogs.length];
        dogDistances[0] = sortedIndices;
        ArrayList<Integer> dogIndices = new ArrayList<Integer>(dogs.length);
        for (int i = 0; i < dogs.length; i++) { dogIndices.add(i); }
        
        for (int i : sortedIndices) {
            getDistanceSortedIndices(dogs, sheeps[i], dogIndices);
            int bestDog = dogIndices.get(0); 
            if (bestDog == mId) {
                chosenSheepIndex = i;
                break;
            }
            else {
                if (dogDistances[bestDog] == null) {
                    dogDistances[bestDog] = getDistanceSortedIndices(sheeps, dogs[bestDog], undeliveredIndices);
                }
                if (dogDistances[bestDog].indexOf(i) > bestDog) {
                    chosenSheepIndex = i;
                    break;
                }
            }
        }
        
        if (chosenSheepIndex == -1) {
            chosenSheepIndex = sortedIndices.get(0);
        }
        
        return chosenSheepIndex;
    }
    

}
