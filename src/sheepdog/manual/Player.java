package sheepdog.manual;

import java.io.*;
import java.util.*;
import sheepdog.sim.Point;

public class Player extends sheepdog.sim.Player {
    
    // Return: the next position
    // my position: dogs[id-1]
    public Point move(Point[] dogs, // positions of dogs
                      Point[] sheeps) { // positions of the sheeps
        Point current = dogs[id-1];

        double x = current.x;
        double y = current.y;
        BufferedReader reader = null;
        try {
            // read input from user
            reader = new BufferedReader(new InputStreamReader(System.in));

            System.out.print("Input x,y:");
            
            String line = reader.readLine();
            String[] fields = line.split(" ");
            x = Double.parseDouble(fields[0]);
            y = Double.parseDouble(fields[1]);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new Point(x, y);
    }

}
