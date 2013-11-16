package sheepdog.dumb;

import sheepdog.sim.Point;

public class Player extends sheepdog.sim.Player {
    
    // Return: the next position
    // my position: dogs[id-1]
    public Point move(Point[] dogs, // positions of dogs
                      Point[] sheeps) { // positions of the sheeps
        Point current = dogs[id-1];

        current.x+=5;
        return current;
    }

}
