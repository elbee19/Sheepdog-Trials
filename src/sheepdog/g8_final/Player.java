package sheepdog.g8_final;

import sheepdog.sim.Point;

import java.util.*;

public class Player extends sheepdog.sim.Player {

    /*
    Possible improvements:

    If dogs >= xx then do wall strategy

    More zones, different configurations
    How to decide when to move dogs to different zones? Load balancing
    Find a way to split zones into smaller zones dynamically
     */
    private int nblacks;
    private boolean mode;

    final static double MAX_SPEED = 1.99;//2.0;

    private Point[] dogs;
    private Point[] sheeps;

    ArrayList<Zone> zones= new ArrayList<Zone>();
    HashMap<Integer, Integer> dogToZone = new HashMap<Integer, Integer>();

    public void init(int nblacks, boolean mode) {
        this.nblacks = nblacks;
        this.mode = mode;
        id = id - 1;
    }
    
    public Point move(Point[] dogs,
                      Point[] sheeps) {

        Point moveLocation;

        // Generate the new configuration and assign dogs to zones. Zones that appear first in the configuration
        // will have more dogs assigned to them
        if (zones.isEmpty()) {
            zones = new ZoneConfig().getConfiguration(dogs.length);
            for (int i = 0; i < dogs.length; i++) {
                int zoneID = i % (zones.size());
                dogToZone.put(i, zoneID);
            }
        }

        this.dogs = dogs;
        this.sheeps = sheeps;

        if (mode == true) {
            Point[] tmp = new Point[nblacks];
            for (int i = 0; i < nblacks; i++) {
                tmp[i] = sheeps[i];
            }
            this.sheeps = tmp;
        }

        // bring back all the white sheep
        ArrayList<Integer> undeliveredBlackSheep = Calculator.undeliveredBlackSheep(sheeps, nblacks);
        if (mode == true && undeliveredBlackSheep.size() == 0) {
            this.sheeps = sheeps;
            ArrayList<Integer> undeliveredWhiteSheep = Calculator.undeliveredWhiteSheepAdvanced(sheeps, nblacks);
            if (dogs[id].x > 50.2) {
                return Calculator.getMoveTowardPoint(dogs[id], new Point(50.1, 50));
            } else if (Calculator.pointsEqual(dogs[id], new Point(50.1, 50))) {
                return Calculator.getMoveTowardPoint(dogs[id], new Point(30, 50));
            }
            if (id < undeliveredWhiteSheep.size()) {
                return chaseSheepTowardGoal(id, undeliveredWhiteSheep.get(id), Zone.GATE);
            } else {
                return Calculator.getMoveTowardPoint(dogs[id], new Point(0, 50));
            }
        }

        Point currentDogPoint = dogs[id];

        //move dogs through gate
        if (currentDogPoint.x < 50){
            return moveDogTowardGate(currentDogPoint);
        }

        moveLocation = getNextPositionBasedOnZone(id);
        makePointValid(currentDogPoint, moveLocation);

        return moveLocation;
    }

    public Point moveDogTowardGate(Point dogPoint) {
        double distanceFromGate = Calculator.dist(dogPoint, Zone.GATE);
        if (distanceFromGate<MAX_SPEED){
            dogPoint.x += distanceFromGate*(Zone.GATE.x-dogPoint.x)/distanceFromGate;
            dogPoint.y += distanceFromGate*(Zone.GATE.y-dogPoint.y)/distanceFromGate;
            return dogPoint;
        }
        dogPoint.x += MAX_SPEED*(Zone.GATE.x-dogPoint.x)/distanceFromGate;
        dogPoint.y += MAX_SPEED*(Zone.GATE.y-dogPoint.y)/distanceFromGate;
        return dogPoint;
    }

    public Point getNextPositionBasedOnZone(int dogNum){
        Point currentPosition = dogs[dogNum];
        int zoneNumber = dogToZone.get(dogNum);
        Zone myZone = zones.get(zoneNumber);

        /*
        If there are more dogs than zones, then there will be multiple dogs per zone. Therefore we will be assigning
        multiple dogs to a zone, and tier determines which sheep they target. Tier = 0 means that they target the farthest sheep in that zone,
        tier = 1 means they will get the second farthest sheep, etc. To assign tiers we just take all the dogs in the zone and sort them
        by their ID and then assign sequentially.
        */
        ArrayList<Integer> dogsInThisZone = myZone.getDogIndices(dogs);
        Collections.sort(dogsInThisZone);

        ArrayList<Integer> sortedSheep = getDistanceSortedIndices(myZone.getGoal(), myZone.getSheepIndices(this.sheeps));
        int tier = dogsInThisZone.indexOf(dogNum);

        if (myZone.hasSheep(this.sheeps)) {
            if (tier == -1) {
                tier = 0;
            } else if (tier >= sortedSheep.size()) {
                // If a dog is delivering sheep to the goal and has nothing to deliver, then get it out of the way
                if (Calculator.pointsEqual(myZone.goalPoint, Zone.GATE)) {
                    return Calculator.getMoveTowardPoint(currentPosition, Zone.DOGHOUSE);
                }
            }
            return chaseSheepTowardGoal(dogNum, sortedSheep.get(tier), myZone.getGoal());
        } else { // The dog's zone is currently empty, reassign the dog's zone
            // Find the zone with the fewest number of sheep that is > 0 and move the dog to that location
            // But don't move it to a zone that is a goal zone because that introduces clogging


            /*
            CURRENTLY DOES NOT WORK, DOGS DON'T LEAVE THEIR ZONE
             */

            /*
            if (Calculator.pointsEqual(myZone.goalPoint, Zone.GATE)) {
                return dogs[dogNum];
            }

            ArrayList<Integer> sortedZones = getNumSheepSortedZones();
            for (int i = 0; i < sortedZones.size(); i++) {
                int numSheep = zones.get(sortedZones.get(i)).numSheep(sheeps);
                if (numSheep > 0 && !Calculator.pointsEqual(zones.get(i).goalPoint, Zone.GATE)) {
                    Zone zoneToMoveTo = zones.get(i);
                    dogToZone.put(dogNum, i);
                    return currentPosition;

//                    // move the dog to the farthest sheep in that zone instead
//                    sortedSheep = getDistanceSortedIndices(zoneToMoveTo.getGoal(), zoneToMoveTo.getSheepIndices(this.sheeps));
//                    return Calculator.getMoveTowardPoint(dogs[dogNum], sheeps[sortedSheep.get(tier)]);
                }
            }
            */
            return Calculator.getMoveTowardPoint(currentPosition, myZone.getCenter());
		}
    }

    public Point chaseSheepTowardGoal(int dogNum, int sheepNum, Point goal) {
        Point dogPoint = dogs[dogNum];
        Point sheepPoint = sheeps[sheepNum];
        sheepPoint = anticipateSheepMovement(dogPoint, sheepPoint);

        double angleGapToSheep = Calculator.getAngleOfTrajectory(goal, sheepPoint);
        Point idealLocation = Calculator.getMoveInDirection(sheepPoint, angleGapToSheep, 1.0);
        Point moveLocation = Calculator.getMoveTowardPoint(dogPoint, idealLocation);

        return moveLocation;
   }

    private Point anticipateSheepMovement(Point me, Point targetSheep) {
        double angleDogToSheep = Calculator.getAngleOfTrajectory(me, targetSheep);
        if (Calculator.withinRunDistance(targetSheep, me)) {
            targetSheep = Calculator.getMoveInDirection(targetSheep, angleDogToSheep, 1.0/*SHEEP_RUN_SPEED*/);
        }
        else if (Calculator.withinWalkDistance(targetSheep, me)) {
            targetSheep = Calculator.getMoveInDirection(targetSheep, angleDogToSheep, .1 /*sheep walk*/);
        }
        return targetSheep;
    }

    public static void makePointValid(Point current, Point destination) {
        // prevent crossing the fence
        if (current.x > 50.0 && destination.x < 50.0) {
            destination.x = 50.01;
        } else if (current.x < 50.0 && destination.x > 50.0) {
            destination.x = 49.99;
        }
        if (destination.x > 100) { destination.x = 100; }
        else if (destination.x < 0) { destination.x = 0; }
        if (destination.y > 100) { destination.y = 100; }
        else if (destination.y < 0) { destination.y = 0; }
    }

    // Sorts the list of sheep based on their distance away from pt, farthest first
    protected ArrayList<Integer> getDistanceSortedIndices(final Point pt, ArrayList<Integer> sheepToCheck ) {
        Collections.sort(sheepToCheck, new Comparator<Integer>() {
            @Override
            public int compare(Integer arg0, Integer arg1) {
                return (int) Math.signum(Calculator.dist(sheeps[arg1], pt) - Calculator.dist(sheeps[arg0], pt));
            }
        });
        return sheepToCheck;
    }

    // Sort the zones by how many sheep they have
    protected ArrayList<Integer> getNumSheepSortedZones() {
        ArrayList<Integer> zoneIndices = new ArrayList<Integer>();
        for (int i = 0; i < zones.size(); i++) {
            zoneIndices.add(i);
        }

        Collections.sort(zoneIndices, new Comparator<Integer>() {
            @Override
            public int compare(Integer arg0, Integer arg1) {
                return (int) Math.signum(zones.get(arg0).numSheep(sheeps) - zones.get(arg1).numSheep(sheeps));
            }
        });
        return zoneIndices;
    }
}
