package sheepdog.g6;

import sheepdog.sim.Point;
import java.util.ArrayList;
import java.util.*;

public class Player extends sheepdog.sim.Player {

    private int nblacks;
    private boolean mode;
    public int state;
    public boolean movedPastThresholdDistance;
    public Point pos;
    public int ndogs;
    public ArrayList<ArrayList<Point>> partitions;
    public ArrayList<ArrayList<Integer>> sheepsForDog;
    public boolean partitionOnce = true;
    public boolean useTempDistance;
    public boolean finalMode;
    public final double MAX_SPEED = 1.98;
    public final Point MIDPOINT = new Point(50, 50);
    public int ticks;
    static double OPEN_LEFT = 49.0; // left side of center openning
    static double OPEN_RIGHT = 51.0;
    static double dimension = 100.0; // dimension of the map

    public void init(int nblacks, boolean mode) {
        this.nblacks = nblacks;
        this.mode = mode;
        this.state = 0;
	this.ticks = 0;
	this.finalMode = false;
        this.useTempDistance = false;
    }
    
    // Return: the next position
    // my position: dogs[id-1]
    public Point move(Point[] dogs, Point[] sheeps) {
    //System.out.println("state:" + this.state);
	this.ndogs = dogs.length;
    sheeps=updateToNewSheep(sheeps,dogs);
	if (ticks++ % 20 == 0) {
	    sheepsForDog = new ArrayList<ArrayList<Integer>>();
	    for (int i = 0; i < dogs.length; i++) {
		sheepsForDog.add(new ArrayList<Integer>());
	    }
	    this.partitions = new ArrayList<ArrayList<Point>>();
	    for (int i = 0; i < dogs.length; i++) {
		//System.out.println(sheepsForDog.get(i).size());
	    }
	    createPartitions(dogs, sheeps);
	}	
	if (!partitionOnce) {
	    this.partitions = new ArrayList<ArrayList<Point>>();
	}
        setPos(dogs[id-1]);
        Point next = new Point();
	if (finalMode || mode && areAllBlacksLeftOfGate(sheeps)) {
	    finalMode = true;
	    if (!areAllBlacksLeftOfGate(sheeps)) {
		if (isClosestTo(dogs, MIDPOINT) && dogs[id-1].x < 50 || dogs[id-1].x >= 50) {
		    Point black = getBlackLeftOfGate(sheeps);
		    Point dest = chaseTowards(black, MIDPOINT, dogs);
		    return move_straight(dogs[id-1], dest, MAX_SPEED);
		}
	    }
	    ArrayList<Point> whiteSheep = getAllWhitesLeftOfGate(sheeps);
	    Point dest = null;
	    if (whiteSheep.size()!=0){
	    dest = dogs[id-1].x > 50 ? MIDPOINT : chaseTowards(whiteSheep.get(id % whiteSheep.size()), MIDPOINT, dogs);
	    }
	    else {
	    	dest = new Point();
	    	dest.x = dogs[id-1].x;
	    	dest.y = dogs[id-1].y;
	    }
	    Point tmp=move_straight(dogs[id-1], dest, MAX_SPEED);
	    if (dogs[id-1].x<50) {
        	if (tmp.x>50 && (dogs[id].y<49 ||dogs[id].y>51))
		    tmp.x=dogs[id].x;
	    }
	    if (dogs[id-1].x>50) {
        	if (tmp.x<50 && (dogs[id].y<49 ||dogs[id].y>51))
		    tmp.x=dogs[id].x;
	    }
	    return tmp;
	}
        if (true){
            //System.out.println("in the baseline mode");
            Point current = dogs[id - 1];
            double length;
            double next_x = 0;
            double next_y = 0;
            boolean target = false;
            if (current.x < 50) {
                length = Math.sqrt(Math.pow((current.x - 51), 2)
                           + Math.pow((current.y - 51), 2));

                next_x = current.x + ((50.5 - current.x) / length) * 1.99;
                next_y = current.y + ((50.5 - current.y) / length) * 1.99;
                current.x = next_x;
                current.y = next_y;

                return current;

            } 
            else {
                if (!partitionOnce) {
                    createPartitions(dogs, sheeps);
                }
                next = this.many_dogs_strategy(dogs, sheeps);
                movedPastThresholdDistance = false;
            }
        } else {
            next = baseline(dogs, sheeps);
            //System.out.printf("the value of next (%f, %f)",next.x, next.y);
        }
        if (dogs[id-1].x<50) {
        	//System.out.println("in the fix part");
        	if (next.x>50 && (dogs[id-1].y<49 ||dogs[id-1].y>51))
        			next.x=50;
        }
        if (dogs[id-1].x>50) {
        	if (next.x<50 && (dogs[id-1].y<49 ||dogs[id-1].y>51))
        			next.x=50;
        }
        return next;
    }

    public Point baseline(Point[] dogs, Point[] sheeps) {
    //System.out.println("in the baseline mode");
    Point current = dogs[id - 1];
    double length;
    double next_x = 0;
    double next_y = 0;
    boolean target = false;
    int targetNum=0;
    if (!mode) {
    	targetNum=sheeps.length;
    }
    else {
    	targetNum=nblacks;
    }
    if (current.x < 50) {
        length = Math.sqrt(Math.pow((current.x - 51), 2)
                   + Math.pow((current.y - 51), 2));

        next_x = current.x + ((50.5 - current.x) / length) * 1.99;
        next_y = current.y + ((50.5 - current.y) / length) * 1.99;

    } 
    else  {
        for (int i = 0; i < targetNum; i++) {
            if ((i % dogs.length) == id - 1 & sheeps[i].x > 50) {
                Point dest = chaseCloseTowards(sheeps[i],MIDPOINT,dogs);
                return this.move_straight(dogs[id-1], dest, MAX_SPEED);
                // length = Math.sqrt(Math.pow((current.x - sheeps[i].x), 2)
                //            + Math.pow((current.y - sheeps[i].y), 2));
                // if (length > 0.5) {
                //     System.out.printf("here it is dog %d", id);
                //     System.out.printf("we are chasing sheep %d", i);
                //     double offsetx;
                //     double offsety;
                //     double tmplength;
                //     tmplength = Math.sqrt(Math.pow((sheeps[i].x - 50), 2)
                //                   + Math.pow((sheeps[i].y - 50), 2));
                //     offsetx = (sheeps[i].x - 50) * (1 + tmplength)
                //         / tmplength + 50;
                //     offsety = (sheeps[i].y - 50) * (1 + tmplength)
                //         / tmplength + 50;
                //     length = Math.sqrt(Math.pow((current.x - offsetx), 2)
                //                + Math.pow((current.y - offsety), 2));
                //     next_x = current.x + ((offsetx - current.x) / length)
                //         * 1.99;
                //     next_y = current.y + ((offsety - current.y) / length)
                //         * 1.99;
                //     if (next_x >= 100)
                //         next_x = 100;
                //     if (next_y >= 100)
                //         next_y = 100;
                //     if (next_x < 0)
                //         next_x = 0;
                //     if (next_y < 0)
                //         next_y = 0;
                //     break;
                // } 
                // else {
                // System.out.println("the distance is less than 2");
                // length = Math.sqrt(Math.pow((current.x - 50), 2)
                //            + Math.pow((current.y - 50), 2));
                // next_x = current.x + ((50 - current.x) / length) * 1.98;
                // next_y = current.y + ((50 - current.y) / length) * 1.98;
                // break;
                // }
            }
        }
        if (!target) {

	    Point dest = chaseClosestTowards(sheeps, MIDPOINT,dogs);
	    dest = chaseClosestTowards(sheeps, MIDPOINT,dogs);
            if (!isClosestTo(dogs, dest)) {
                ArrayList<Point> group = getCorrespondingSheep(sheeps);
                dest = chaseFurthestFromGoal(sheeps, MIDPOINT,dogs);
                return this.move_straight(dogs[id-1], dest, MAX_SPEED);
            }
            return this.move_straight(dogs[id-1], dest, MAX_SPEED);
	    
	    /*
            System.out.printf("\nelse idle dog?\n");
            System.out.printf("here it is dog %d", id);

            // token[i]=true;
            target = true;
            length = Math.sqrt(Math.pow((current.x - 50), 2)
                       + Math.pow((current.y - 60), 2));
            if (length > 1) {

                next_x = current.x + ((50 - current.x) / length) * 1.98;
                next_y = current.y + ((60 - current.y) / length) * 1.98;
                if (next_x >= 100)
                next_x = 100;
                if (next_y >= 100)
                next_y = 100;
                if (next_x < 0)
                next_x = 0;
                if (next_y < 0)
                next_y = 0;

            } 
            else {
                next_x = current.x;
                next_y = current.y;

            }
	    */
        }
    }
    current.x = next_x;
    current.y = next_y;

    return current;
    }

    public boolean areAllBlacksLeftOfGate(Point[] sheeps) {
	for (int i = 0; i < nblacks; i++) {
	    if (sheeps[i].x >= 50) {
		return false;
	    }
	}
	return true;
    }

    public ArrayList<Point> getAllWhitesLeftOfGate(Point[] sheeps) {
	ArrayList<Point> result = new ArrayList<Point>();
	for (int i = nblacks; i < sheeps.length; i++) {
	    if (sheeps[i].x < 50) {
		result.add(sheeps[i]);
	    }
	}
	return result;
    }

    public Point getBlackLeftOfGate(Point[] sheeps) {
	for (int i = 0; i < nblacks; i++) {
	    if (sheeps[i].x > 50) {
		return sheeps[i];
	    }
	}
	return null;
    }

    private Point[] updateToNewSheep(Point[] sheeps,Point[] dogs) {
        Sheepdog sheepdog=new Sheepdog(dogs.length, sheeps.length, nblacks, false);
        sheepdog.sheeps=sheeps;
        sheepdog.dogs=dogs;
        
        Point[] newSheeps = new Point[sheeps.length];
        for (int i = 0; i < sheeps.length; ++i) {
            // compute its velocity vector
            newSheeps[i] = sheepdog.moveSheep(i);
        }
        return newSheeps;
    }


    public void createPartitions(Point[] dogs, Point[] sheeps) {
        for (int i = 0; i < dogs.length; i++) {
            partitions.add(new ArrayList<Point>());
        }
        for (int idx = 0; idx < sheeps.length; idx++) {
            if ((!mode || idx < nblacks) && sheeps[idx].x >= 50) {
                Point s = sheeps[idx];
                for (int i = 0; i < dogs.length; i++) {
                    if (i == dogs.length - 1) {
                        partitions.get(i).add(s);
                        sheepsForDog.get(i).add(idx);
                        break;
                    }
                    double angle = Math.PI * ((double) (i + 1) / ndogs) - Math.PI / 2;
                    double line = 50 + (s.x - 50) * Math.tan(angle);
                    if (s.y < line) {
                        partitions.get(i).add(s);
                        sheepsForDog.get(i).add(idx);
                        break;
                    }
                }
            }
        }
    }

    /*
        double[] y_pos = evenSpread();
        for (int idx=0; idx<dogs.length; idx++) {
            ArrayList<Point> group = new ArrayList<Point>();
            double range_bottom, range_top;
            if (idx-1 < 0) {
                range_bottom = 0.0;
            }
            else {
                range_bottom = (y_pos[idx-1] + y_pos[idx])/2.0;
            }
            if (idx+1 >= dogs.length) {
                range_top = 100.0;
            }
            else {    
                range_top = (y_pos[idx] + y_pos[idx+1])/2.0;
            }
            for (int i=0; i<sheeps.length; i++) {
                if (sheeps[i].y >= range_bottom && sheeps[i].y <= range_top) {
                    group.add(sheeps[i]);
                }
            }
            this.partitions.add(group);
        }
    */

    public int wrap(int x) {
        if (x >= ndogs) {
            return x % ndogs;
        }
        else {
            return x;
        }
    }
    

    public ArrayList<Point> getCorrespondingSheep(Point[] sheep) {
        ArrayList<Integer> sheepIndices;
    	ArrayList<Point> correspondingSheep = new ArrayList<Point>();
        int k = -1;
        while((correspondingSheep.size() == 0 || allCLoseToFence(correspondingSheep, 10.0)) && k < ndogs) {
            sheepIndices = sheepsForDog.get(wrap(id + k));
            correspondingSheep = new ArrayList<Point>();
            for (int i : sheepIndices) {
                correspondingSheep.add(sheep[i]);
            }
            k++;
        }
    	return correspondingSheep;
    }

    public ArrayList<Point> getCorrespondingSheep(Point[] sheep, double dist) {
        ArrayList<Integer> sheepIndices;
        ArrayList<Point> correspondingSheep = new ArrayList<Point>();
        int k = -1;
        while((correspondingSheep.size() == 0 || allCLoseToFence(correspondingSheep, dist)) && k < ndogs) {
            sheepIndices = sheepsForDog.get(wrap(id + k));
            correspondingSheep = new ArrayList<Point>();
            for (int i : sheepIndices) {
                correspondingSheep.add(sheep[i]);
            }
            k++;
        }
        return correspondingSheep;
    }

    public Point many_dogs_strategy(Point[] dogs, Point[] sheeps) {
        if (allCLoseToFence(sheeps, 7.5)) {
            state = 9;
        }
       switch (state) {
        case 0:
            if (isWithinRange(MIDPOINT, 4.0)) {
                this.state = 4;
            }
            return this.move_straight(dogs[id-1], MIDPOINT, MAX_SPEED);
        case 1: // unused
            if (isWithinRange(new Point(100, 50), 6.0)) {
                this.state = 2;
            }
            return this.move_straight(dogs[id-1], new Point(100, 50), MAX_SPEED);
        case 2: // unused
            double[] x_pos = evenSpread();
            Point dest = new Point(100, x_pos[id-1]);
            if (isWithinRange(dest, 2.0)) {
                this.state = 3;
            }
            return this.move_straight(dogs[id-1], dest, MAX_SPEED);
        case 3: // unused
            double[] y_pos = evenSpread();
            Point[] destinations = new Point[ndogs];
            for (int i=0; i<y_pos.length; i++) {
                destinations[i] = new Point(100, y_pos[i]);
            }
            if (allInRange(dogs, destinations, 2.0)) {
                this.state = 4;
            }
            return dogs[id-1];
        case 4:
            double x = pos.x;
            double y = pos.y;

            // dest = chaseClosestTowards(sheeps, MIDPOINT);
            ArrayList<Point> group = getCorrespondingSheep(sheeps);
            // System.out.println("hahaha, group size");
            //System.out.println(group.size());
            if (group.size() == 0) {
                // System.out.println("boom");
                this.state = 5;
                //System.out.println(this.pos.x + " , " + this.pos.y);
                return this.pos;
            }
            if (!useTempDistance && checkIfPast(dogs[id-1], group)) {
                useTempDistance = true;
            }
            //System.out.println("before the chase Further");
            dest = chaseFurthestFromGoal(group, MIDPOINT,dogs);
            if (dest.x == 50 && dest.y == 50) {
                this.state = 5;
                return this.pos;
            }
            //System.out.println("after the chase Further");
            if (distanceFrom(MIDPOINT) < 3 && movedPastThresholdDistance) {
                this.state = 6;
                //System.out.println("boom");
                return this.move_straight(dogs[id-1], dest, MAX_SPEED);
            }
            //System.out.println("before the state 4 return");
            return this.move_straight(dogs[id-1], dest, MAX_SPEED);
        case 5:
            group = getCorrespondingSheep(sheeps);
            dest = chaseClosestTowards(group, MIDPOINT,dogs);
            //if (distanceFrom(MIDPOINT) < 3) {
                // this.state = 6;
            //}
            if (group.size() == 0 || !isClosestTo(dogs, dest)) {
                group = getCorrespondingSheep(sheeps);
                this.state = 8;
                dest = chaseFurthestFromGoal(sheeps, MIDPOINT,dogs);
                return this.move_straight(dogs[id-1], dest, MAX_SPEED);
            }
            return this.move_straight(dogs[id-1], dest, MAX_SPEED);
        case 6:
            dest = chaseClosestTowards(sheeps, new Point(50.0, 50.0),dogs);
            if (distanceFrom(new Point(40.0, 50.0)) < 3) {
                this.state = 7;
            }
            return this.move_straight(dogs[id-1], dest, MAX_SPEED);
        case 7:
            if (isWithinRange(MIDPOINT, 3.0)) {
                this.state = 8;
            }
            return this.move_straight(dogs[id-1], MIDPOINT, MAX_SPEED);
        case 8:
            dest = chaseFurthestFromGoal(sheeps, MIDPOINT,dogs);
            if (distanceFrom(dest) < 2) {
                this.state = 5;
            }
            return this.move_straight(dogs[id-1], dest, MAX_SPEED);
        case 9:
            sheepsForDog = new ArrayList<ArrayList<Integer>>();
            for (int i = 0; i < dogs.length; i++) {
            sheepsForDog.add(new ArrayList<Integer>());
            }
            this.partitions = new ArrayList<ArrayList<Point>>();
            for (int i = 0; i < dogs.length; i++) {
            //System.out.println(sheepsForDog.get(i).size());
            }
            createPartitions(dogs, sheeps);

            ArrayList<Integer> sheepIndices = sheepsForDog.get(id-1);
            group = new ArrayList<Point>();
            for (int i : sheepIndices) {
                group.add(sheeps[i]);
            }
            if (group.size() == 0) {
                dest = chaseFurthestFromGoal(sheeps, MIDPOINT,dogs);
                return this.move_straight(dogs[id-1], dest, MAX_SPEED);
            }
            dest = chaseFurthestFromGoal(group, MIDPOINT,dogs);
            return this.move_straight(dogs[id-1], dest, MAX_SPEED);

        }

       return dogs[id-1];
    }

    public int dogsNearby(Point[] dogs) {
        int count = 0;
        for (int i=0; i<dogs.length; i++) {
            if (i != id-1 && distanceFrom(dogs[i]) < 5) {
                count++;
            }
        }
        return count;
    }



    public boolean isClosestTo(Point[] dogs, Point dest) {
        double closest_dist = 200;
        int closest_idx = 0;
        double dist_to;
        for (int i=0; i<dogs.length; i++) {
            dist_to = distanceBetween(dogs[i], dest);
            if (dist_to <= closest_dist) {
                closest_dist = dist_to;
                closest_idx = i;
            }
        }
        if (closest_idx == id-1) {
            return true;
        }
        else {
            return false;
        }
    }

    public boolean checkIfPast(Point dog, ArrayList<Point> sheeps) {
    for (Point s : sheeps) {
        if (distanceBetween(s, MIDPOINT) > distanceFrom(MIDPOINT)) {
            return false;
        }
    }
    return true;
    }

    public Point chaseFurthestFromGoal(Point[] sheeps, Point dest, Point[] dogs) {
        ArrayList<Point> temp = new ArrayList<Point>();
        for (int i=0; i<sheeps.length; i++) {
            if (!mode || i < nblacks) {
                temp.add(sheeps[i]);
            }
        }
        return chaseFurthestFromGoal(temp, dest, dogs);
    }

    public Point chaseFurthestFromGoal(ArrayList<Point>sheeps, Point dest, Point[] dogs) {
        double tmpdis;
        double furthest_dist = -100;
        Point furthest_sheep = dest;
        double dist_to;
        Point current = dogs[id-1];
        boolean sheepOnRightSide = false;
        for (Point sheep : sheeps) {
            //tmpdis = ;
            if (!useTempDistance) {
                dist_to = distanceBetween(sheep, dest);
            }
            else {
                dist_to = 1.0*distanceBetween(sheep, dest) - 0.8*distanceBetween(sheep,current);
            }
            if (dist_to >= furthest_dist && !(sheep.x < 50.0)) {
                furthest_dist = dist_to;
                furthest_sheep = sheep;
            }
        sheepOnRightSide |= sheep.x > 50;
        }
    if (furthest_dist == 0 && sheepOnRightSide) {
        useTempDistance = false;
        return chaseFurthestFromGoal(sheeps, dest, dogs);
    }
        return chaseTowards(furthest_sheep, dest,dogs);
    }

    public Point chaseClosestTowards(ArrayList<Point> sheeps, Point dest, Point[] dogs) {
    	Point[] sheepsArray = new Point[sheeps.size()];
    	sheepsArray = sheeps.toArray(sheepsArray);
    	return chaseClosestTowards(sheepsArray, dest, dogs);
    }

    public Point chaseClosestTowards(Point[] sheeps, Point dest, Point[] dogs) {
        double closest_dist = 200;
        int closest_idx = 0;
        double dist_to;
        for (int i=0; i<sheeps.length; i++) {
            if (!mode || i < nblacks) {
                dist_to = distanceFrom(sheeps[i]);
                if (dist_to <= closest_dist && !(sheeps[i].x < 45.0)) {
                    closest_dist = dist_to;
                    closest_idx = i;
                }
            }
        }
        if (closest_idx != 0) {
            Point closestSheep = sheeps[closest_idx];
            return chaseTowards(closestSheep, dest, dogs);
        } 
        else {
            return chaseFurthestFromGoal(sheeps, dest, dogs);
        }
    }

    public Point chaseCloseTowards(Point sheep, Point dest, Point[] dogs) {
        Vector dir = new Vector(sheep, dest);

        // handle case that sheep is very close to fence
        // we use the wall to our benefit
        if (distanceFromBorder(sheep) < 0.5) {
            Vector temp = dir.get_unit();
            temp.times(0.01);
            temp.plus(sheep);
            return temp.toPoint();
        }
        else {
            dir.reverse();
            Vector temp = dir.get_unit();
            temp.times(0.01);
            temp.plus(sheep);
            return temp.toPoint(); 
        }
    }

    public Point chaseTowards(Point sheep, Point dest, Point[] dogs) {
        Vector dir = new Vector(sheep, dest);

        // handle case that sheep is very close to fence
        // we use the wall to our benefit
        if (distanceFromBorder(sheep) < 0.5) {
            // System.out.println(distanceFromBorder(sheep));
            Vector temp = dir.get_unit();
            temp.times(0.1);
            temp.plus(sheep);
            return temp.toPoint();
            // Point current = dogs[id-1];
            // Point next = new Point();
            // double length;
            // length=Math.sqrt(Math.pow((current.x-50),2)+Math.pow((current.y-50),2));
            // next.x=current.x+((50-current.x)/length)*2;
            //next.y=current.y+((50-current.y)/length)*2;   
            //return dir.toPoint();
            //return next;
        }
        else {
            dir.reverse();
            Vector temp = dir.get_unit();
            temp.times(0.5);
            temp.plus(sheep);
            return temp.toPoint(); 
        }
    }

    public double distFromLine(Point p, double m, double b) {
        double t1 = Math.abs(p.y - m*p.x - b);
        double t2 = Math.sqrt(Math.pow(m, 2) + 1);
        // |y - m*x - b|/(m^2 - 1)^(1/2)
        return t1/t2;
    }

    public double distanceFromBorder(Point p) {
        double m, b, dist;
        double min_dist = 101.00;

        m = getM(TOP_LEFT, TOP_RIGHT);
        b = getB(TOP_LEFT, TOP_RIGHT);
        dist = distFromLine(p, m, b);
        if (dist <= min_dist)
            min_dist = dist;

        m = getM(TOP_LEFT, BOTTOM_LEFT);
        b = getB(TOP_LEFT, BOTTOM_LEFT);
        dist = distFromLine(p, m, b);
        if (dist <= min_dist)
            min_dist = dist;

        m = getM(BOTTOM_LEFT, BOTTOM_RIGHT);
        b = getB(BOTTOM_LEFT, BOTTOM_RIGHT);
        dist = distFromLine(p, m, b);
        if (dist <= min_dist)
            min_dist = dist;

        m = getM(TOP_RIGHT, BOTTOM_RIGHT);
        b = getB(TOP_RIGHT, BOTTOM_RIGHT);
        dist = distFromLine(p, m, b);
        if (dist <= min_dist)
            min_dist = dist;

        return min_dist;
    }

    public boolean isValid(Point dest) {
        if (dest.x > 100 || dest.x < 0 || dest.y > 100 || dest.y < 0 || hitTheFence(pos, dest)) {
            return false;
        }
        else {
            return true;
        }
    }

    public double[] evenSpread() {
        double[] pos = new double[ndogs];
        for (int d = 0; d < ndogs; ++d) {
            pos[d] = 1.0 * (d+1) / (ndogs+1) * dimension;
        }
        return pos;
    }

    public Point move_straight(Point start, Point dest, double speed) {
        boolean valid = false; 
        Vector dir = new Vector(start, dest);
        // if its closer than the speed there is no need to do anything
        // just go there
        if (dir.magnitude() <= speed && isValid(dest)) {
        return dest;
        }
        // otherwise we apply velocity
        while (!valid) {
            dir = new Vector(start, dest);
            dir = dir.get_unit();
            dir.times(speed);
            dir.plus(start);
            if (isValid(dir.toPoint()) || speed <= 0.1) {
                valid = true;
            }
            else {
                speed = speed - 0.1;
            }
            //System.out.println("stage while");
        }
        if (speed <= 0.1) {
            //System.out.println("if speed<0.1");
            Point p = dir.toPoint();
            if (p.y < 0.0)
                return new Point(p.x, 0.0);
            else if (p.x < 0.00)
                return new Point(0.0, p.y);
            else if (p.y > 100.0)
                return new Point(p.x, 100.0);
            else if (p.x > 100.0)
                return new Point(100.0, p.y);
            else
                return new Point(50, p.y);
        }
        else {
            //System.out.println("the end else");
            //System.out.printf("x is %f",dir.toPoint().x);
            //System.out.printf("y is %f", dir.toPoint().y);
           return dir.toPoint(); 
        }   
    }

    public boolean isWithinRange(Point dest, double range) {
        double dist = distanceFrom(dest);
        return dist <= range;
    }

    public double distanceFrom(Point dest) {
        Vector trayectory = new Vector(pos, dest);
        return trayectory.magnitude();
    }

    public double distanceBetween(Point start, Point dest) {
        Vector trayectory = new Vector(start, dest);
        return trayectory.magnitude();
    }

    private void setPos(Point pos) {
        this.pos = pos;
    }

    public boolean allInRange(Point[] dogs, Point[] points, double range) {
        for (int i=0; i<points.length; i++) {
            Vector trayectory = new Vector(dogs[i], points[i]);
            if (trayectory.magnitude() >= range) {
                return false;
            }
        }
        return true;
    }

    public boolean allCLoseToFence(Point[] sheeps, double range) {
        for (int i=0; i<sheeps.length; i++) {
            Point sheep = sheeps[i];
            Vector trayectory = new Vector(MIDPOINT, sheep);
            //System.out.println("bam! " + trayectory.magnitude());
            if (trayectory.magnitude() >= range) {
                //System.out.println("bam! " + trayectory.magnitude());
                return false;
            }
        }
        return true;
    }

    public boolean allCLoseToFence(ArrayList<Point> sheeps, double range) {
        for (Point sheep : sheeps) {
            Vector trayectory = new Vector(MIDPOINT, sheep);
            //System.out.println("bam! " + trayectory.magnitude());
            if (trayectory.magnitude() >= range) {
                //System.out.println("bam! " + trayectory.magnitude());
                return false;
            }
        }
        return true;
    }

    int getSide(double x, double y) {
        if (x < dimension * 0.5)
            return 0;
        else if (x > dimension * 0.5)
            return 1;
        else
            return 2;
    }

    int getSide(Point p) {
        return getSide(p.x, p.y);
    }

    boolean hitTheFence(Point p1, Point p2) {
        return hitTheFence(p1.x, p1.y, p2.x, p2.y);
     }

    boolean hitTheFence(double x1, double y1, double x2, double y2) {
        if (getSide(x1,y1) == getSide(x2, y2))
            return false;
        if (getSide(x1,y1) == 2 || getSide(x2,y2) == 2)
            return false;
        
        double y3 = (y2-y1)/(x2-x1)*(50-x1)+y1;

        assert y3 >= 0 && y3 <= 100;

        if (y3 >= OPEN_LEFT && y3 <= OPEN_RIGHT)
            return false;
        else{
            //System.out.println("fence");
            return true;
        }
    }

    public double getM(Point a, Point b) {
        return (b.y - a.y)/(b.x - a.x);
    }

    public double getB(Point a, Point b) {
        double m = getM(a, b);
        return a.y - m*a.x;
    }

    // BORDER POINTS
    public final Point TOP_LEFT = new Point(0, 0);
    public final Point TOP_RIGHT = new Point(100, 0);
    public final Point BOTTOM_LEFT = new Point(0, 100);
    public final Point BOTTOM_RIGHT = new Point(100, 100);
}
