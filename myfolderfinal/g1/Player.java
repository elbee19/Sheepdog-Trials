package sheepdog.g1;

import java.io.*;
import java.util.*;

import sheepdog.sim.Point;
import sheepdog.sim.Sheepdog;

public class Player extends sheepdog.sim.Player {
    private int nblacks;
    private boolean mode;
    private Strategy strategyOne;
    public void init(int nblacks, boolean mode) {
        this.nblacks = nblacks;
        this.mode = mode;
        int strat = mode ? 2 : 1;
        
        strategyOne= new Strategy(strat, mode, nblacks);
    }
    
    // Return: the next position
    // my position: dogs[id-1]
    public Point move(Point[] dogs, // positions of dogs
                      Point[] sheeps) { // positions of the sheeps
        Point current = dogs[id-1];

        strategyOne.updateInfo(dogs, sheeps);
        return strategyOne.getDogPos(id); 
        
        }

}
