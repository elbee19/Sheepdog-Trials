package sheepdog.g9;

// a static class for helper methods
public class PlayerUtils {
    // abuse for constants too
    public static final Point GATE = new Point(50, 50);
    public static final double DIMENSION = 100;

    public static double GATEOPENLEFT = 49; // left side of center openning
    public static double GATEOPENRIGHT = 51; // right side of center opening


    public static final double DOGSPEED = 20;
    public static final double SHEEPWALKSPEED = 1;
    public static final double SHEEPRUNSPEED = 10;
    public static final double SHEEPCLUSTERSPEED = 0.5;
    public static final double TIMEUNIT = 0.1;

    public static final double WALKDISTANCE = 10;
    public static final double RUNDISTANCE = 2;
    public static final double CLUSTERDISTANCE = 1;
    // Used for fetch strategy
    public static final double SMALLDISTANCE = 0.001;


    public static double vectorLength(double dx, double dy) {
        return Math.sqrt(dx * dx + dy * dy);
    }

    public static Point moveDogTo(Point from, Point to) {
        double dx = to.x - from.x;
        double dy = to.y - from.y;
        double d = vectorLength(dx, dy);
        if (d <= DOGSPEED * TIMEUNIT)
            return to;
        else {
            double scale = (DOGSPEED * TIMEUNIT - SMALLDISTANCE)/ d;
            double x = from.x + scale * dx;
            double y = from.y + scale * dy;
            if (x > 100)
                x = 100;
            if (y > 100)
                y = 100;
            return new Point(x, y);
        } 
    }

    public static int findASheep(Point[] sheeps) {
        int minsheep = -1;
        double mindist = Double.MAX_VALUE;
        for (int i = 0; i < sheeps.length; i++) {
            if (sheeps[i].x > 50) {
                double d = distance(sheeps[i], GATE);
                if (d < mindist && d != 0) { // ignore overlapping dog
                    mindist = d;
                    minsheep = i;
                }
            }
        }
        return minsheep;
    }

    public static Point PredictNextMove(int id, Point[] dogs, Point[] sheeps) {
        return moveSheep(id, dogs, sheeps);
    }

    public static Point getTargetDogPoint(Point targetSheepPoint) {
        if (targetSheepPoint.equals(GATE))
            return new Point(50 + SMALLDISTANCE, 50);
        double dx = targetSheepPoint.x - GATE.x;
        double dy = targetSheepPoint.y - GATE.y;
        double d = vectorLength(dx, dy);
        double sin = dx/d;
        double cos = dy/d;
        return new Point(targetSheepPoint.x + SMALLDISTANCE * Math.abs(sin),
                         targetSheepPoint.y + SMALLDISTANCE * cos);
    }


    // The following code is copied from simulator

    public static double distance(Point a, Point b) {
        return Math.sqrt((a.x-b.x) * (a.x-b.x) +
                         (a.y-b.y) * (a.y-b.y));
    }

    static Point getClosestDog(int sheepId, Point[] dogs, Point[] sheeps) {
        int mindog = -1;
        double mindist = Double.MAX_VALUE;
        for (int i = 0; i < dogs.length; ++i) {
            double d = distance(sheeps[sheepId], dogs[i]);
            if (d < mindist && d != 0) { // ignore overlapping dog
                mindist = d;
                mindog = i;
            }
        }
        return dogs[mindog];
    }
    
    static Point moveSheep(int sheepId, Point[] dogs, Point[] sheeps) {
        Point thisSheep = sheeps[sheepId];
        double dspeed = 0;
        Point closestDog = getClosestDog(sheepId, dogs, sheeps);
        double dist = distance(thisSheep, closestDog);
        assert dist > 0;

        if (dist < RUNDISTANCE)
            dspeed = SHEEPRUNSPEED * TIMEUNIT;
        else if (dist < WALKDISTANCE)
            dspeed = SHEEPWALKSPEED * TIMEUNIT;
        
        // offset from dogs
        double ox_dog = (thisSheep.x - closestDog.x) / dist * dspeed;
        double oy_dog = (thisSheep.y - closestDog.y) / dist * dspeed;

        // offset from clustering
        double ox_cluster = 0, oy_cluster = 0;

        // aggregate offsets then normalize
        for (int i = 0; i < sheeps.length; ++i) {
            // skip this sheep itself
            if (i == sheepId) continue;

            double d = distance(thisSheep, sheeps[i]);

            // ignore overlapping sheep
            if (d < CLUSTERDISTANCE && d > 0) {
                // add an unit vector to x-axis, y-axis
                ox_cluster += ((thisSheep.x - sheeps[i].x) / d);
                oy_cluster += ((thisSheep.y - sheeps[i].y) / d);
            }
        }
        // normalize by length
        double l = vectorLength(ox_cluster, oy_cluster);
        if (l != 0) {
            ox_cluster = ox_cluster / l * SHEEPCLUSTERSPEED * TIMEUNIT;
            oy_cluster = oy_cluster / l * SHEEPCLUSTERSPEED * TIMEUNIT;
        }

        double ox = ox_dog + ox_cluster, oy = oy_dog + oy_cluster;
        
        Point npos = updatePosition(thisSheep, ox, oy);
        return npos;

    }

    // update the current point according to the offsets
    static Point updatePosition(Point now, double ox, double oy) {
        double nx = now.x + ox, ny = now.y + oy;
        
        // hit the left fence        
        if (nx < 0) {
            System.err.println("SHEEP HITS THE LEFT FENCE!!!");

            // move the point to the left fence
            Point temp = new Point(0, now.y);
            // how much we have already moved in x-axis?
            double moved = 0 - now.x;
            // how much we still need to move
            // BUT in opposite direction
            double ox2 = -(ox - moved); 
            return updatePosition(temp, ox2, oy);
        }
        // hit the right fence
        if (nx > DIMENSION) {
            System.err.println("SHEEP HITS THE RIGHT FENCE!!!");

            // move the point to the right fence
            Point temp = new Point(DIMENSION, now.y);
            double moved = (DIMENSION - now.x);
            double ox2 = -(ox - moved);
            return updatePosition(temp, ox2, oy);
        }
        // hit the up fence
        if (ny < 0) {
            System.err.println("SHEEP HITS THE UP FENCE!!!");

            // move the point to the up fence
            Point temp = new Point(now.x, 0);
            double moved = 0 - now.y;
            double oy2 = -(oy - moved);
            return updatePosition(temp, ox, oy2);
        }
        // hit the bottom fence
        if (ny > DIMENSION) {
            System.err.println("SHEEP HITS THE BOTTOM FENCE!!!");

            Point temp = new Point(now.x, DIMENSION);
            double moved = (DIMENSION - now.y);
            double oy2 = -(oy - moved);
            return updatePosition(temp, ox, oy2);
        }

        assert nx >= 0 && nx <= DIMENSION;
        assert ny >= 0 && ny <= DIMENSION;
        
        // hit the middle fence
        if (hitTheFence(now.x, now.y, nx, ny)) {
            System.err.println("SHEEP HITS THE CENTER FENCE!!!");
            //            System.err.println(nx + " " + ny);
            //            System.err.println(ox + " " + oy);

            // move the point to the fence
            Point temp = new Point(50, now.y);
            double moved = (50 - now.x);
            double ox2 = -(ox-moved);
            return updatePosition(temp, ox2, oy);
        }

        // otherwise, we are good
        return new Point(nx, ny);
    }

    // up side is 0
    // bottom side is 1
    // at the fence 2
    static int getSide(double x, double y) {
        if (x < DIMENSION * 0.5)
            return 0;
        else if (x > DIMENSION * 0.5)
            return 1;
        else
            return 2;
    }
    static int getSide(Point p) {
        return getSide(p.x, p.y);
    }


    static boolean hitTheFence(double x1, double y1,
                        double x2, double y2) {
        // on the same side
        if (getSide(x1,y1) == getSide(x2, y2))
            return false;

        // one point is on the fence
        if (getSide(x1,y1) == 2 || getSide(x2,y2) == 2)
            return false;
        
        // compute the intersection with (50, y3)
        // (y3-y1)/(50-x1) = (y2-y1)/(x2-x1)

        double y3 = (y2-y1)/(x2-x1)*(50-x1)+y1;

        assert y3 >= 0 && y3 <= DIMENSION;

        // pass the openning?
        if (y3 >= GATEOPENLEFT && y3 <= GATEOPENRIGHT)
            return false;
        else
            return true;
    }

}