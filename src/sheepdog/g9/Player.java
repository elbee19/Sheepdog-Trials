package sheepdog.g9;

public class Player extends sheepdog.sim.Player {
    private int nblacks;
    private boolean mode;
    private Fetch fetch;
    public void init(int nblacks, boolean mode) {
        this.nblacks = nblacks;
        this.mode = mode;
        fetch = new Fetch(id, nblacks, mode);
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
        // condition to use strategy should be put here
        Point moveTo;
        if (true) {
            moveTo = fetch.move(dogs, sheeps);
            System.out.print(fetch.toString());
            System.out.print(moveTo.toString());
        }
        return moveTo.toSimPoint();
    }

}
