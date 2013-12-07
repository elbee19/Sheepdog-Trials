package sheepdog.g8;

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
    HashMap<Integer, Integer> originalDogToZone = new HashMap<Integer, Integer>();
    HashMap<Integer, Integer> dogToZone = new HashMap<Integer, Integer>();

    public void init(int nblacks, boolean mode) {
        this.nblacks = nblacks;
        this.mode = mode;
        id = id - 1;
    }
    
    public Point move(Point[] dogs,
                      Point[] sheeps) {

        Point moveLocation;
        Point currentDogPoint = dogs[id];

        // Generate the new configuration and assign dogs to zones. Zones that appear first in the configuration
        // will have more dogs assigned to them
        if (zones.isEmpty()) {
            zones = new ZoneConfig().getConfiguration(dogs.length);
            for (int i = 0; i < dogs.length; i++) {
                int zoneID = i % (zones.size());
                dogToZone.put(i, zoneID);
		originalDogToZone.put(i, zoneID);	//this hash map keeps track of original assignments. is important for zone reassignment.
            }
        }

        this.dogs = dogs;
        this.sheeps = sheeps;

	//System.out.println("I am dog " + id + " and my zone is " + dogToZone.get(id));
        /* begin hacky solution to advanced mode */
        if (mode == true) {
            Point[] tmp = new Point[nblacks];
            for (int i = 0; i < nblacks; i++) {
                tmp[i] = sheeps[i];
            }
            this.sheeps = tmp;
        }

        // bring back all the white sheep
        ArrayList<Integer> undeliveredBlackSheep = Utils.undeliveredBlackSheep(sheeps, nblacks);
        if (mode == true && undeliveredBlackSheep.size() == 0) {
            this.sheeps = sheeps;
            ArrayList<Integer> undeliveredWhiteSheep = Utils.undeliveredWhiteSheepAdvanced(sheeps, nblacks);
            if (dogs[id].x > 50.2) {
                return Utils.getMoveTowardPoint(dogs[id], new Point(50.1, 50));
            } else if (Utils.pointsEqual(dogs[id], new Point(50.1, 50))) {
                return Utils.getMoveTowardPoint(dogs[id], new Point(30, 50));
            }
            if (id < undeliveredWhiteSheep.size()) {
                return chaseSheepTowardGoal(id, undeliveredWhiteSheep.get(id), Zone.GATE);
            } else {
                return Utils.getMoveTowardPoint(dogs[id], new Point(0, 50));
            }
        }
        /* end hacky solution to advanced mode */

        //move dogs through gate
        if (currentDogPoint.x < 50){
            return moveDogTowardGate(currentDogPoint);
        }

        moveLocation = getNextPositionBasedOnZone(id);
        moveLocation = makePointValid(currentDogPoint, moveLocation);

	//todo: if I am sitting on the gate and not moving, get the heck out of the way
	if (currentDogPoint.x>=50 && currentDogPoint.x<=52 && currentDogPoint.y<=54 && currentDogPoint.y>=46){
	//	System.out.println("currently, I am in the way of the gate");
		if (moveLocation.x == currentDogPoint.x && moveLocation.y == currentDogPoint.y){
			//System.out.println("I am on the gate and not moving--need to get away from the gate!");
			//just move dog down 2 spaces on Y
			moveLocation.y -= MAX_SPEED;
		}
		
	}
        return moveLocation;
    }

    public Point moveDogTowardGate(Point dogPoint) {
        double distanceFromGate = Utils.dist(dogPoint, Zone.GATE);
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
        //System.out.println("getting next position based on zone");
	Point currentPosition = dogs[dogNum];
        int zoneNumber = dogToZone.get(dogNum);
        Zone myZone = zones.get(zoneNumber);
	//System.out.println("got my zone");
        /*
        If there are more dogs than zones, then there will be multiple dogs per zone. Therefore we will be assigning
        multiple dogs to a zone, and tier determines which sheep they target. Tier = 0 means that they target the farthest sheep in that zone,
        tier = 1 means they will get the second farthest sheep, etc. To assign tiers we just take all the dogs in the zone and sort them
        by their ID and then assign sequentially.
        */
        ArrayList<Integer> dogsInThisZone = myZone.getDogIndices(dogs);
        Collections.sort(dogsInThisZone);
        int tier = dogsInThisZone.indexOf(dogNum);
        ArrayList<Integer> sortedSheep = getDistanceSortedIndices(currentPosition/*myZone.getGoal()*/, myZone.getSheepIndices(this.sheeps));
	/*System.out.println("sheeps closest to me, in order: " );	
	for (int i = 0; i < sortedSheep.size(); i++){
		int currentIndex = sortedSheep.get(i);
		System.out.println("sheep at: " + this.sheeps[currentIndex].x + ", " + this.sheeps[currentIndex].y + "dist- " + Utils.dist(currentPosition, this.sheeps[currentIndex]));
	}*/
	//System.out.println("got sorted sheep");
	
		//NEW: if the dog's original zone is different than its current zone, and the dog's original zone is not empty, dog should be reassigned to original zone
		int originalZoneNumber = originalDogToZone.get(dogNum);
		if (originalZoneNumber != zoneNumber){
			if (zones.get(originalZoneNumber).hasSheep(this.sheeps)){
				//System.out.println("My current zone is " + zoneNumber + " but my original zone, " + originalZoneNumber + ", has no sheep. Going back to that zone");
				dogToZone.put(dogNum, originalZoneNumber);
				zoneNumber = originalZoneNumber;
				myZone = zones.get(originalZoneNumber);
			}
		}
		//end new
		
        if (myZone.hasSheep(this.sheeps)) {
            if (tier == -1) {
                tier = 0;
            } else if (tier >= sortedSheep.size()) {
                // If a dog is delivering sheep to the goal and has nothing to deliver, then get it out of the way
                if (Utils.pointsEqual(myZone.goalPoint, Zone.GATE)) {
                    	//System.out.println("I am moving toward doghouse!");
			return Utils.getMoveTowardPoint(currentPosition, Zone.DOGHOUSE);
                } else {
		    //System.out.println("I have nothing to deliver, staying at current position");
                    return currentPosition;
                }
            }
	    //System.out.println("chasing sheep toward goal");
            return chaseSheepTowardGoal(dogNum, sortedSheep.get(tier), myZone.getGoal());
        // The dog's zone is currently empty, reassign the dog's zone
        } else {
            /*
            Find the zone with the fewest number of sheep that is > 0 and move the dog to that location
            But don't move it to a zone that is a goal zone because that introduces clogging
            */

            // distribute the dogs more evenly?
 	    //System.out.println("my zone is empty (line 153), trying to reassign!");
            
	 /*   if (Utils.pointsEqual(myZone.goalPoint, Zone.GATE)) {
                System.out.println("My zone is the zone closest to the goal, not reassigning. But I am not moving, so this is bad--TO FIX!");
		return currentPosition;
            }
	*/
            /*ArrayList<Integer> sortedZones = getNumSheepSortedZones();
            for (int i = 0; i < sortedZones.size(); i++) {
                Zone tmpZone = zones.get(sortedZones.get(i));
                if (Utils.pointsEqual(tmpZone.goalPoint, Zone.GATE)) {
                    continue;
                }

                int numSheep = zones.get(sortedZones.get(i)).numSheep(sheeps);
                if (numSheep > 0) {
                    dogToZone.put(dogNum, sortedZones.get(i));
                    return Utils.getMoveTowardPoint(dogs[dogNum], tmpZone.getCenter());
                }
            }*/
		//NEW: we always want a dog to move toward a middle zone if its zone is empty. here, we hard code what the dog should do based on what its zone is
		int totalZones = zones.size();
		if (totalZones == 1){
			//System.out.println("Code should never get here unless we have completed the scenario");
		}
		else if (totalZones == 2){
			if (zoneNumber == 1){
				dogToZone.put(dogNum, 0);
				//System.out.println("I have zone 1, now taking zone 0"); 
			}
			else{
				//System.out.println("I am in zone 0 and it is empty, but I will have sheep soon so I am not moving!");
				return currentPosition;
			}
		}
		else if (totalZones == 3){
			if (zoneNumber == 1 || zoneNumber == 2){
				dogToZone.put(dogNum, 0);
				//System.out.println("I have zone " + zoneNumber + ", now taking zone 0");
			}
			else{
				//System.out.println("I am in zone 0 and it is empty, but I will have sheep soon!");
				return currentPosition;
			}
		}
		else if (totalZones == 4){
			if (zoneNumber != 1){
				dogToZone.put(dogNum, 1);
				//System.out.println("I have zone " + zoneNumber + ", now taking zone 1");
			}
			else {
				//System.out.println("I am in zone 0 and it is empty, but I will have sheep soon!");
				return currentPosition;
			}	
		}
		else if (totalZones == 5){
			if (zoneNumber == 0){
				if (zones.get(1).hasSheep(this.sheeps)){
					dogToZone.put(dogNum, 1);
				//	System.out.println("I have zone " + zoneNumber + ", now taking zone 1");
				}
				else if (zones.get(2).hasSheep(this.sheeps)){
					dogToZone.put(dogNum, 2);
				//	System.out.println("I have zone " + zoneNumber + ", now taking zone 2");
				}
				else {
				//	System.out.println("I am in zone 0 and it is empty, but so are the top and bottom goal zones. Somewhere will have sheep soon!");
					return currentPosition;
				}

			}
			else if (zoneNumber == 1){
				if (zones.get(3).hasSheep(this.sheeps)){
					dogToZone.put(dogNum, 3);
				//	System.out.println("I have zone " + zoneNumber + ", now taking zone 3");
				}
				else if (zones.get(2).hasSheep(this.sheeps)){
					dogToZone.put(dogNum, 2);
				//	System.out.println("I have zone " + zoneNumber + ", now taking zone 2");
				}
				else {
				//	System.out.println("I am in the top goal zone and it is empty, but so are 3 and 2. Somewhere will have sheep soon!");
					return currentPosition;
				}
			}
			else if (zoneNumber == 2){
				if (zones.get(4).hasSheep(this.sheeps)){
					dogToZone.put(dogNum, 4);
				//	System.out.println("I have zone " + zoneNumber + ", now taking zone 4");
				}
				else if (zones.get(2).hasSheep(this.sheeps)){
					dogToZone.put(dogNum, 2);
				//	System.out.println("I have zone " + zoneNumber + ", now taking zone 2");
				}
				else {
				//	System.out.println("I am in the bottomgoal zone and it is empty, but so are 4 and 2. Somewhere will have sheep soon!");
					return currentPosition;
				}
			}
			else if (zoneNumber == 3){
				if (zones.get(0).hasSheep(this.sheeps)){
					dogToZone.put(dogNum, 0);
				//	System.out.println("I have zone " + zoneNumber + ", now taking zone 0");
				}
				else if (zones.get(1).hasSheep(this.sheeps)){
					dogToZone.put(dogNum, 1);
					//System.out.println("I have zone " + zoneNumber + ", now taking zone 1");
				}
				else {
					//System.out.println("I am in zone 3 and it is empty but so are 0 and 1. Somewhere will have sheep soon!");
					return currentPosition;
				}
			}
			else if (zoneNumber == 4){
				if (zones.get(0).hasSheep(this.sheeps)){
					dogToZone.put(dogNum, 0);
					//System.out.println("I have zone " + zoneNumber + ", now taking zone 0");
				}
				else if (zones.get(2).hasSheep(this.sheeps)){
					dogToZone.put(dogNum, 2);
					//System.out.println("I have zone " + zoneNumber + ", now taking zone 2");
				}
				else {
					//System.out.println("I am zone 4 and it is empty but so are 0 and 2. Somewhere will have sheep soon!");
					return currentPosition;
				}
			}
		}
		else if (totalZones == 6){
				if (zoneNumber == 0){
					if (zones.get(3).hasSheep(this.sheeps)){
						dogToZone.put(dogNum, 3);
						//System.out.println("I have zone " + zoneNumber + ", now taking zone 3");
					
					}
					else if (zones.get(2).hasSheep(this.sheeps)){
						dogToZone.put(dogNum, 2);
						//System.out.println("I have zone " + zoneNumber + ", now taking zone 2");
					}
					else if (zones.get(1).hasSheep(this.sheeps)){
						dogToZone.put(dogNum, 1);
						//System.out.println("I have zone " + zoneNumber + ", now taking zone 1");
					}
					else {
						//System.out.println("I am zone 0 and it is empty but so are 1, 3 and 2. Somewhere will have sheep soon!");
						return currentPosition;
					}
				}
				else if (zoneNumber == 1){
					if (zones.get(2).hasSheep(this.sheeps)){
						dogToZone.put(dogNum, 2);
						//System.out.println("I have zone " + zoneNumber + ", now taking zone 2");
					
					}
					else if (zones.get(3).hasSheep(this.sheeps)){
						dogToZone.put(dogNum, 3);
						//System.out.println("I have zone " + zoneNumber + ", now taking zone 3");
					}
					else if (zones.get(0).hasSheep(this.sheeps)){
						dogToZone.put(dogNum, 0);
						//System.out.println("I have zone " + zoneNumber + ", now taking zone 0");
					}
					else {
						//System.out.println("I am zone 1 and it is empty but so are 3, 0 and 2. Somewhere will have sheep soon!");
						return currentPosition;
					}
				}
				else if (zoneNumber == 2){
					if (zones.get(3).hasSheep(this.sheeps)){
						dogToZone.put(dogNum, 3);
						//System.out.println("I have zone " + zoneNumber + ", now taking zone 3");
					
					}
					else {
						//System.out.println("I am zone 2 and it is empty but so is 3. Somewhere will have sheep soon!");
						return currentPosition;
					}
				}
				else if (zoneNumber == 3){
					if (zones.get(2).hasSheep(this.sheeps)){
						dogToZone.put(dogNum, 2);
						//System.out.println("I have zone " + zoneNumber + ", now taking zone 2");
					
					}
					else {
						//System.out.println("I am zone 3 and it is empty but so is 2. Somewhere will have sheep soon!");
						return currentPosition;
					}
				}
				else if (zoneNumber == 4){
					if (zones.get(2).hasSheep(this.sheeps)){
						dogToZone.put(dogNum, 2);
						//System.out.println("I have zone " + zoneNumber + ", now taking zone 2");
					
					}
					else if (zones.get(1).hasSheep(this.sheeps)){
						dogToZone.put(dogNum, 1);
						//System.out.println("I have zone " + zoneNumber + ", now taking zone 1");
					}
					else if (zones.get(3).hasSheep(this.sheeps)){
						dogToZone.put(dogNum, 3);
						//System.out.println("I have zone " + zoneNumber + ", now taking zone 3");
					}
					else {
						//System.out.println("I am zone 4 and it is empty but so are 3, 1 and 2. Somewhere will have sheep soon!");
						return currentPosition;
					}
				}
				else if (zoneNumber == 5){
					if (zones.get(3).hasSheep(this.sheeps)){
						dogToZone.put(dogNum, 3);
						//System.out.println("I have zone " + zoneNumber + ", now taking zone 3");
					
					}
					else if (zones.get(0).hasSheep(this.sheeps)){
						dogToZone.put(dogNum, 0);
						//System.out.println("I have zone " + zoneNumber + ", now taking zone 0");
					}
					else if (zones.get(2).hasSheep(this.sheeps)){
						dogToZone.put(dogNum, 2);
						//System.out.println("I have zone " + zoneNumber + ", now taking zone 2");
					}
					else {
						//System.out.println("I am zone 5 and it is empty but so are 3, 0 and 2. Somewhere will have sheep soon!");
						return currentPosition;
					}
				}
			}	
				else if (totalZones == 7){
					if (zoneNumber == 0){
						if (zones.get(1).hasSheep(this.sheeps)){
							dogToZone.put(dogNum, 1);
							//System.out.println("I have zone " + zoneNumber + ", now taking zone 1");
						
						}
						else if (zones.get(2).hasSheep(this.sheeps)){
							dogToZone.put(dogNum, 2);
							//System.out.println("I have zone " + zoneNumber + ", now taking zone 2");
						}
	
						else {
							//System.out.println("I am zone 0 and it is empty but so are 1 and 2. Somewhere will have sheep soon!");
							return currentPosition;
						}
					}
					else if (zoneNumber == 1){
						if (zones.get(2).hasSheep(this.sheeps)){
							dogToZone.put(dogNum, 2);
							//System.out.println("I have zone " + zoneNumber + ", now taking zone 2");
						}
	
						else {
							//System.out.println("I am zone 1 and it is empty but so is 2. Somewhere will have sheep soon!");
							return currentPosition;
						}
					}
					else if (zoneNumber == 2){
						if (zones.get(1).hasSheep(this.sheeps)){
							dogToZone.put(dogNum, 1);
							//System.out.println("I have zone " + zoneNumber + ", now taking zone 1");
						}
	
						else {
							//System.out.println("I am zone 2 and it is empty but so is 1. Somewhere will have sheep soon!");
							return currentPosition;
						}
					}
					
					else if (zoneNumber == 3){
						if (zones.get(0).hasSheep(this.sheeps)){
							dogToZone.put(dogNum, 0);
							//System.out.println("I have zone " + zoneNumber + ", now taking zone 0");
						}
						else if (zones.get(5).hasSheep(this.sheeps)){
							dogToZone.put(dogNum, 5);
							//System.out.println("I have zone " + zoneNumber + ", now taking zone 5");
						}
						else {
							//System.out.println("I am zone 3 and it is empty but so are 0 and 5. Somewhere will have sheep soon!");
							return currentPosition;
						}
					}
					else if (zoneNumber == 4){
						if (zones.get(0).hasSheep(this.sheeps)){
							dogToZone.put(dogNum, 0);
							//System.out.println("I have zone " + zoneNumber + ", now taking zone 0");
						}
						else if (zones.get(6).hasSheep(this.sheeps)){
							dogToZone.put(dogNum, 6);
							//System.out.println("I have zone " + zoneNumber + ", now taking zone 6");
						}
						else {
							//System.out.println("I am zone 4 and it is empty but so are 0 and 6. Somewhere will have sheep soon!");
							return currentPosition;
						}
					}
					else if (zoneNumber == 5){
						if (zones.get(1).hasSheep(this.sheeps)){
							dogToZone.put(dogNum, 1);
							//System.out.println("I have zone " + zoneNumber + ", now taking zone 1");
						}
						else if (zones.get(3).hasSheep(this.sheeps)){
							dogToZone.put(dogNum, 3);
							//System.out.println("I have zone " + zoneNumber + ", now taking zone 3");
						}
						else if (zones.get(2).hasSheep(this.sheeps)){
							dogToZone.put(dogNum, 2);
							//System.out.println("I have zone " + zoneNumber + ", now taking zone 2");
						}
						else {
							//System.out.println("I am zone 5 and it is empty but so are 5, 3, and 2. Somewhere will have sheep soon!");
							return currentPosition;
						}
					}
					else if (zoneNumber == 6){
						if (zones.get(2).hasSheep(this.sheeps)){
							dogToZone.put(dogNum, 2);
							//System.out.println("I have zone " + zoneNumber + ", now taking zone 2");
						}
						else if (zones.get(4).hasSheep(this.sheeps)){
							dogToZone.put(dogNum, 4);
							//System.out.println("I have zone " + zoneNumber + ", now taking zone 4");
						}
						else if (zones.get(1).hasSheep(this.sheeps)){
							dogToZone.put(dogNum, 1);
							//System.out.println("I have zone " + zoneNumber + ", now taking zone 1");
						}
						else {
							//System.out.println("I am zone 6 and it is empty but so are 4, 2, and 1. Somewhere will have sheep soon!");
							return currentPosition;
						}
					}
				}
				else {//total zones > 7
					if (zoneNumber == 0){
						if (zones.get(3).hasSheep(this.sheeps)){
							dogToZone.put(dogNum, 3);
							//System.out.println("I have zone " + zoneNumber + ", now taking zone 3");
						}
						else if (zones.get(1).hasSheep(this.sheeps)){
							dogToZone.put(dogNum, 1);
							//System.out.println("I have zone " + zoneNumber + ", now taking zone 1");
						}
						else if (zones.get(2).hasSheep(this.sheeps)){
							dogToZone.put(dogNum, 2);
							//System.out.println("I have zone " + zoneNumber + ", now taking zone 2");
						}
						else {
							//System.out.println("I am zone 0 and it is empty but so are 1, 3, and 2. Somewhere will have sheep soon!");
							return currentPosition;
						}
					}
					else if (zoneNumber == 1){
						if (zones.get(2).hasSheep(this.sheeps)){
							dogToZone.put(dogNum, 2);
							//System.out.println("I have zone " + zoneNumber + ", now taking zone 2");
						}
						else if (zones.get(0).hasSheep(this.sheeps)){
							dogToZone.put(dogNum, 0);
							//System.out.println("I have zone " + zoneNumber + ", now taking zone 0");
						}
						else if (zones.get(3).hasSheep(this.sheeps)){
							dogToZone.put(dogNum, 3);
							//System.out.println("I have zone " + zoneNumber + ", now taking zone 3");
						}
						else {
							//System.out.println("I am zone 1 and it is empty but so are 0, 3, and 2. Somewhere will have sheep soon!");
							return currentPosition;
						}
					}
					else if (zoneNumber == 2){
						if (zones.get(3).hasSheep(this.sheeps)){
							dogToZone.put(dogNum, 3);
							//System.out.println("I have zone " + zoneNumber + ", now taking zone 3");
						}
						else {
							//System.out.println("I am zone 2 and it is empty but so is 3. Somewhere will have sheep soon!");
							return currentPosition;
						}
					}
					else if (zoneNumber == 3){
						if (zones.get(2).hasSheep(this.sheeps)){
							dogToZone.put(dogNum, 2);
							//System.out.println("I have zone " + zoneNumber + ", now taking zone 2");
						}
						else {
							//System.out.println("I am zone 3 and it is empty but so is 2. Somewhere will have sheep soon!");
							return currentPosition;
						}
					}
					else if (zoneNumber == 4){
						if (zones.get(1).hasSheep(this.sheeps)){
							dogToZone.put(dogNum, 1);
							//System.out.println("I have zone " + zoneNumber + ", now taking zone 1");
						}
						else if (zones.get(6).hasSheep(this.sheeps)){
							dogToZone.put(dogNum, 6);
							//System.out.println("I have zone " + zoneNumber + ", now taking zone 6");
						}
						else if (zones.get(2).hasSheep(this.sheeps)){
							dogToZone.put(dogNum, 2);
							//System.out.println("I have zone " + zoneNumber + ", now taking zone 2");
						}
						else {
							//System.out.println("I am zone 4 and it is empty but so are 1, 6, and 2. Somewhere will have sheep soon!");
							return currentPosition;
						}
					}
					else if (zoneNumber == 5){
						if (zones.get(0).hasSheep(this.sheeps)){
							dogToZone.put(dogNum, 0);
							//System.out.println("I have zone " + zoneNumber + ", now taking zone 0");
						}
						else if (zones.get(7).hasSheep(this.sheeps)){
							dogToZone.put(dogNum, 7);
							//System.out.println("I have zone " + zoneNumber + ", now taking zone 7");
						}
						else if (zones.get(3).hasSheep(this.sheeps)){
							dogToZone.put(dogNum, 3);
							//System.out.println("I have zone " + zoneNumber + ", now taking zone 3");
						}
						else {
							//System.out.println("I am zone 5 and it is empty but so are 7,3,0. Somewhere will have sheep soon!");
							return currentPosition;
						}
					}
					else if (zoneNumber == 6){
						if (zones.get(2).hasSheep(this.sheeps)){
							dogToZone.put(dogNum, 2);
							//System.out.println("I have zone " + zoneNumber + ", now taking zone 2");
						}
						else if (zones.get(4).hasSheep(this.sheeps)){
							dogToZone.put(dogNum, 4);
							//System.out.println("I have zone " + zoneNumber + ", now taking zone 4");
						}
						else {
							//System.out.println("I am zone 6 and it is empty but so are 2,4. Somewhere will have sheep soon!");
							return currentPosition;
						}
					}
					else{
						if (zones.get(3).hasSheep(this.sheeps)){
							dogToZone.put(dogNum, 3);
							//System.out.println("I have zone " + zoneNumber + ", now taking zone 3");
						}
						else if (zones.get(5).hasSheep(this.sheeps)){
							dogToZone.put(dogNum, 5);
							//System.out.println("I have zone " + zoneNumber + ", now taking zone 5");
						}
						else {
							//System.out.println("I am zone 5 and it is empty but so are 3,7. Somewhere will have sheep soon!");
							return currentPosition;
						}
					}
				}
		}
		
		//System.out.println("finished else...");
           	 return Utils.getMoveTowardPoint(currentPosition, myZone.getCenter());
		
    }

    public Point chaseSheepTowardGoal(int dogNum, int sheepNum, Point goal) {
        Point dogPoint = dogs[dogNum];
        Point sheepPoint = sheeps[sheepNum];
        sheepPoint = anticipateSheepMovement(dogPoint, sheepPoint);

        double angleGapToSheep = Utils.getAngleOfTrajectory(goal, sheepPoint);
        Point idealLocation = Utils.getMoveInDirection(sheepPoint, angleGapToSheep, 1.0);
        Point moveLocation = Utils.getMoveTowardPoint(dogPoint, idealLocation);

        return moveLocation;
   }

    private Point anticipateSheepMovement(Point me, Point targetSheep) {
        double angleDogToSheep = Utils.getAngleOfTrajectory(me, targetSheep);
        if (Utils.withinRunDistance(targetSheep, me)) {
            targetSheep = Utils.getMoveInDirection(targetSheep, angleDogToSheep, 1.0/*SHEEP_RUN_SPEED*/);
        }
        else if (Utils.withinWalkDistance(targetSheep, me)) {
            targetSheep = Utils.getMoveInDirection(targetSheep, angleDogToSheep, .1 /*sheep walk*/);
        }
        return targetSheep;
    }

    public static Point makePointValid(Point current, Point destination) {
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
	
	return destination;
    }

    // Sorts the list of sheep based on their distance away from pt, farthest first
    protected ArrayList<Integer> getDistanceSortedIndices(final Point pt, ArrayList<Integer> sheepToCheck ) {
        Collections.sort(sheepToCheck, new Comparator<Integer>() {
            @Override
            public int compare(Integer arg0, Integer arg1) {
                return (int) (-1.0*Math.signum(Utils.dist(sheeps[arg1], pt) - Utils.dist(sheeps[arg0], pt)));
            }
        });
        /*System.out.println("sheep, in order closest to me:");
	for (int i = 0; i < sheepToCheck.size();i++){
		System.out.println("sheep at position " + sheepToCheck.get(i).x + ", " + sheepToCheck.get(i).y);
	}*/
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
