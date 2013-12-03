package sheepdog.g9;

import java.util.LinkedList;

public class Player extends sheepdog.sim.Player {
    private LinkedList<Strategy> strategyStack;
    private boolean strategyInit;

    public void init(int nblacks, boolean mode) {
        Global.nblacks = nblacks;
        Global.mode = mode;
        strategyInit = false;
        strategyStack = new LinkedList<Strategy>();
    }

    // Return: the next position
    // my position: dogs[id-1]
    public sheepdog.sim.Point move(sheepdog.sim.Point[] posDogs, // positions of dogs
                                   sheepdog.sim.Point[] posSheeps) { // positions of the sheeps
        int numDogs = posDogs.length;
        Point[] dogs = new Point[numDogs];
        for (int i = 0; i < numDogs; i++) {
            dogs[i] = new Point(posDogs[i]);
        }
        int numSheeps = posSheeps.length;
        Point[] sheeps = new Point[numSheeps];
        for (int i = 0; i < numSheeps; i++) {
            sheeps[i] = new Point(posSheeps[i]);
        }

        if (!strategyInit) {
            // condition to use strategy should be put here
            double fetchEst = Fetch.estimate(dogs, sheeps);
            double sweepEst = Sweep.estimate(dogs, sheeps);
            System.out.println(fetchEst + " " + sweepEst);
            if (Global.mode) {
                double inverseFetchEst = fetchEst / Global.nblacks * (sheeps.length - Global.nblacks);
                if (fetchEst > sweepEst + inverseFetchEst) {
                    Sweep sweep = new Sweep (id, strategyStack);
                    strategyStack.push(sweep);
                }
            }
            else {
                if (fetchEst > sweepEst) {
                    Sweep sweep = new Sweep (id, strategyStack);
                    strategyStack.push(sweep);
                }
            }
            strategyInit = true;
        }

        if(strategyStack.isEmpty()) {
            double x = PlayerUtils.GATE.x;
            double y = PlayerUtils.GATEOPENLEFT +
                (PlayerUtils.GATEOPENRIGHT - PlayerUtils.GATEOPENLEFT) *
                ((double)id / (dogs.length + 1));
            Fetch fetch = new Fetch(id, strategyStack, -1, new Point(x, y));
            strategyStack.push(fetch);
        }

        Point moveTo;
        Strategy currentStrategy = strategyStack.getLast();
        moveTo = currentStrategy.move(dogs, sheeps);
        System.out.println(currentStrategy.toString());
        return moveTo.toSimPoint();
    }
}
