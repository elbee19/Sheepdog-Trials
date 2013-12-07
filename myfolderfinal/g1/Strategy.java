package sheepdog.g1;
import sheepdog.sim.Point;
import sheepdog.sim.Sheepdog;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class Strategy{
	
	private Point dog[];
	private Point newDog[];
	private Point sheep[];
	private LinkedList<Integer> orderedSheep;
	private ArrayList<LinkedList<Integer>> orderedSheeps;
	private int targSheep[];
	private int nblacks;
	private int numDog;
	private int numSheep;

	
	private int strategy;
	private boolean mode;
	public Queue<Formation> steps;
	private boolean initSteps;
	private boolean initSteps2;
	private boolean initSteps2b;
	private int stepsIter;

	public Strategy(int strategy, boolean mode, int nblacks) {
		this.dog = null;
		this.sheep = null;
		this.strategy = strategy;
		this.mode = mode;
		this.nblacks = nblacks;
		initSteps();
		stepsIter = 0;
	}

	public void updateInfo(Point[] dog, Point[] sheep){
		this.dog = dog;
		this.sheep = sheep;
		this.numDog = dog.length;
		this.numSheep = sheep.length;

		strategy = 1;
		
		if(numDog < 5)
			strategy = 3;
		
		if(mode){
			strategy = 2;
		}

		if(strategy == 1)
			strategyOne();
		else if(strategy == 2)
			strategyTwo();
		else if (strategy == 3)
			strategyThree();

	}
	
	public void initSteps(){
		this.initSteps = true;
		this.initSteps2 = true;
		this.initSteps2b = true;
	}
	
	public int sheepsOnRight(int toggle){
		int start = sheepStart(toggle);
		int stop = sheepStop(toggle);
		
		int count = 0;
		for(int i=start; i<stop; i++){
			if(sheep[i].x > 50)
				count++;
		}
		return count;
	}

	public Point getDogPos(int id){
		return newDog[id-1];
	}
	
	public Point getFurthestDog() {
		Point furthest = dog[0];
		for(Point p : dog) {
			if(p.x > furthest.x)
				furthest = p;
		}
		return furthest;
	}

	public double farthestSheepDist() {
		double max = Formation.distance(sheep[0], new Point(50., 50.));
		for(int i=1; i<sheep.length; i++) {
			double curr = Formation.distance(sheep[i], new Point(50., 50.));
			if(curr > max)
				max = curr;
		}
		return max;
	}

	//Effective for large numbers of sheeps to herd.
	public void strategyOne() {
		//System.out.println("In Strat1");
		Formation exec = null;
		if(initSteps) {
			steps = new LinkedList<Formation>();

			//Check boolean
			double spacing = (numDog/2-1)*3.5;
				if(spacing > 50)
					spacing = 50;
				
			int sweeps = (int)Math.ceil(50./spacing);

			//dot
			steps.add(new Formation(new double[]{50.0, 50.0}, Formation.Dot, dog, 20.0));
			
			
			//Sandwich
			if(numDog % 2 == 1)
				sweeps--;
			
			for(int i=0; i<sweeps-1; i++){
				steps.add(new Formation(new double[]{100.0, spacing, 50. + i*spacing}, Formation.Sandwich, dog, 20.0));
				steps.add(new Formation(new double[]{4.0, spacing, 50. + i*spacing}, Formation.Sandwich, dog, 2.0));
			}
			
			steps.add(new Formation(new double[]{100.0, spacing, 100 - spacing}, Formation.Sandwich, dog, 20.0));
			steps.add(new Formation(new double[]{4.0, spacing, 100 - spacing}, Formation.Sandwich, dog, 2.0));
			
			//VPusher -> Vertical Line
			steps.add(new Formation(new double[]{100.0}, Formation.VPusher, dog, 20.0));
			if(numDog % 2 == 1){
				steps.add(new Formation(new double[]{50. + numDog - 1}, Formation.VPusher, dog, 2.0));
			}else{
				steps.add(new Formation(new double[]{50. + numDog -1}, Formation.VPusher, dog, 2.0));
			}
		
			
			steps.add(new Formation(new double[]{50.0, 50 - spacing/2, spacing}, Formation.Vertical, dog, 1.0));
			initSteps = false;
		}
		
		//System.out.println(steps.toString());
		
		if( (exec = steps.peek()) == null) {
			//System.out.println("About to Return");
			return;
		}
		
		if(!exec.isDone()){
			//System.out.println("Exec is not done"+ exec.formType);
			newDog = exec.getMove(dog);
		}
		else {
			//System.out.println("Exec is done" + exec.formType);
			steps.remove();
			if(steps.peek() == null) {
				initSteps = true;
				if(sheepsOnRight(0) <= numDog) {
					strategy = 2;
					initSteps2 = true;
				}
				//strategyTwo();
			}
		}
		
		//System.out.println("Got to the end");
	}		

	public void strategyTwo() {
		//System.out.println("In strat2");
		boolean right = true;
		boolean left = false;
		boolean phase2 = false; //this phase is to grab white sheep from left to right
		//determine if phase2
		if(mode && orderedSheepInit(0, right).size() == 0){
			phase2 = true;
		}

		if(!phase2){
			//set dogs to go to dot
			initSteps2b = true;
			if(initSteps2) {
		
				steps = new LinkedList<Formation>();
				
				//toggle 0=black, 1=white, 2=all
				if(mode)
					orderedSheep = orderedSheepInit(0, right);
				else
					orderedSheep = orderedSheepInit(2, right);
				
				targSheep = new int[numDog];
				
				for(int i=0; i < targSheep.length; i++) {
					if(orderedSheep.peek() != null){
						if(mode)
							targSheep[i] = Integer.valueOf(orderedSheep.pollLast());
						else
							targSheep[i] = Integer.valueOf(orderedSheep.poll());
					}
					else
						targSheep[i] = targSheep[i-1];
				}
				steps.add(new Formation(dog, oneOnOne(targSheep, right)));
				initSteps2 = false;
			}
	
			//call getMove for oneOnOne
			steps.peek().formation = oneOnOne(targSheep, right);
			newDog = steps.peek().getMove(dog);
			
			for(int i=0; i < targSheep.length; i++) {
				if(sheep[targSheep[i]].x < 50) {
					orderedSheep = orderedSheepUpdate(orderedSheep, right);
					if(orderedSheep.size() > 0){
						if(mode)
							targSheep[i] = Integer.valueOf(orderedSheep.pollLast());
						else
							targSheep[i] = Integer.valueOf(orderedSheep.poll());
					}else{
						if(mode)
							targSheep[i] = orderedSheepInit(0, right).pollLast();
						else
							targSheep[i] = orderedSheepInit(2, right).poll();
					}
				}
			}
		}
		if(phase2){ //start phase2
			initSteps2 = true;
			if(initSteps2b){
				steps = new LinkedList<Formation>();
				
				//all dogs stack ontop of each other
				targSheep = new int[numDog];
				orderedSheep = orderedSheepInit(1, left);
				targSheep[0] = Integer.valueOf(orderedSheep.poll());
				for(int i=1; i<numDog; i++)
					targSheep[i] = targSheep[0];
				steps.add(new Formation(dog, oneOnOne(targSheep, left)));
				initSteps2b = false;
			}
			
			steps.peek().formation = oneOnOne(targSheep, left);
			newDog = steps.peek().getMove(dog);
			
			if(sheep[targSheep[0]].x > 50) {
				orderedSheep = orderedSheepUpdate(orderedSheep, left);
				if(orderedSheep.size() > 0)
					targSheep[0] = Integer.valueOf(orderedSheep.poll());
			}
			for(int i=1; i<targSheep.length; i++){
				targSheep[i] = targSheep[0];
			}
		}
	}

	public void strategyThree() {		
		boolean right = true;
		boolean left = false;
		boolean phase2 = false; //this phase is to grab white sheep from left to right
	
		//detect 2nd phase
		if(mode && orderedSheepInit(0, right).size() == 0){
			phase2 = true;
		}
		
		if(!phase2){
			initSteps2b = true;

			steps = new LinkedList<Formation>();

			//initialize orderedSheeps
			orderedSheeps = new ArrayList<LinkedList<Integer>>();
			for(int i=0; i<numDog; i++){
				orderedSheeps.add(new LinkedList<Integer>());
			}
				
			//assign sheeps to dogs based on angle to (50, 50)
			//toggle 0=black, 1=white, 2=all
			int start, stop;
			if(mode){
				start = sheepStart(0);
				stop = sheepStop(0);
			}else{
				start = sheepStart(2);
				stop = sheepStop(2);
			}
			for(int i=start; i<stop; i++){
				double angle = Math.atan2(sheep[i].x - 50., sheep[i].y - 50.) * 180 / Math.PI;
				double perAngle = 180. / numDog;
				int dogIndex = (int)(angle/perAngle);
				if(sheep[i].x > 50)
					orderedSheeps.get(dogIndex).add(i);
			}
		
			//sort each sheepList in orderedSheeps
			//assign sheep from each list to dog
			targSheep = new int[numDog];
			for(int i=0; i < numDog; i++) {
				orderedSheeps.set(i, orderedSheepUpdate(orderedSheeps.get(i), right));
				if(orderedSheeps.get(i).size() > 0){
					targSheep[i] = Integer.valueOf(orderedSheeps.get(i).poll());
				}else{
						if(mode)
							targSheep[i] = orderedSheepInit(0, right).pollLast();
						else
							targSheep[i] = orderedSheepInit(2, right).pollLast();
				}
			}
			steps.add(new Formation(dog, oneOnOne(targSheep, right)));
	
			//call getMove for oneOnOne
			steps.peek().formation = oneOnOne(targSheep, right);
			newDog = steps.peek().getMove(dog);
			
		}
		if(phase2){ //start phase2
			initSteps2 = true;
			if(initSteps2b){
				steps = new LinkedList<Formation>();
				
				//all dogs stack ontop of each other
				targSheep = new int[numDog];
				orderedSheep = orderedSheepInit(1, left);
				targSheep[0] = Integer.valueOf(orderedSheep.poll());
				for(int i=1; i<numDog; i++)
					targSheep[i] = targSheep[0];
				steps.add(new Formation(dog, oneOnOne(targSheep, left)));
				initSteps2b = false;
			}
			
			steps.peek().formation = oneOnOne(targSheep, left);
			newDog = steps.peek().getMove(dog);
		
			if(sheep[targSheep[0]].x > 50) {
				orderedSheep = orderedSheepUpdate(orderedSheep, left);
				if(orderedSheep.size() > 0)
					targSheep[0] = Integer.valueOf(orderedSheep.poll());
			}
			for(int i=1; i<targSheep.length; i++){
				targSheep[i] = targSheep[0];
			}
		}

	}
	
	public double leftMost(double x){
		double currLeft = 100.;
		for(int i=0; i<numSheep; i++){
			if(sheep[i].x < currLeft && sheep[i].x >= x)
				currLeft = sheep[i].x;
		}
		return currLeft;
	}

	public LinkedList<Integer> orderedSheepInit(int toggle, boolean right){
		int start = sheepStart(toggle);
		int stop = sheepStop(toggle);
		
		//distance of sheep to center of map
		double dist[] = new double[numSheep];
		for(int i=0; i<dist.length; i++){
			dist[i] = Formation.distance(sheep[i], new Point(50., 50.));
		}
		LinkedList<Integer> oSheep = new LinkedList<Integer>();
		//sort into linked list
		for(int i=start; i<stop; i++){
			//if list is empty, add
			if(oSheep.size() == 0 &&
					((right && sheep[i].x > 50.) || (!right && sheep[i].x < 50.))){
				oSheep.add(i);
			}else{
				for(int j=0; j<oSheep.size(); j++){
					if((right && sheep[i].x > 50.) || (!right && sheep[i].x < 50.)){
						//if list is not empty, add if greater
						if(dist[i] > dist[oSheep.get(j)]){
							oSheep.add(j, i);
							break;
						}
						//if list is not empty, add if less than everything
						if(j == oSheep.size()-1){
							oSheep.add(i);
							break;
						}
					}else{
				//		System.out.println("uh oh: "+sheep[i].x);
					}
				}
			}
		}
		return oSheep;
	}

	public LinkedList<Integer> orderedSheepUpdate(LinkedList<Integer> uSheep, boolean right){
		//distance of sheep to center of map
		double dist[] = new double[numSheep];
		for(int i=0; i<dist.length; i++){
			dist[i] = Formation.distance(sheep[i], new Point(50., 50.));
		}
		LinkedList<Integer> oSheep = new LinkedList<Integer>();
		
		//if uSheep is empty; return empty list
		if(uSheep.size() == 0)
			return oSheep;

		//sort into linked list
		oSheep.add(uSheep.pop());
		while(uSheep.size() > 0){
			int i = uSheep.pop();
			for(int j=0; j<oSheep.size(); j++){
				if((right && sheep[i].x > 50.) || (!right && sheep[i].x < 50.)){
					if(dist[i] > dist[oSheep.get(j)]){
						oSheep.add(j, i);
						break;
					}
					if(j == oSheep.size()-1){
						oSheep.add(i);
						break;
					}
				}else{
					System.out.println("hi " +sheep[i].x);
				}
			}
		}
		return oSheep;
	}
	
	public Point[] oneOnOne(int[] sheepIndex, boolean right){
		this.dog = dog;
		double delta = 0.05;

		//targetSheeps
		Point[] tSheep = new Point[numDog];
		for(int i=0; i<numDog; i++)
			tSheep[i] = new Point(sheep[sheepIndex[i]].x, sheep[sheepIndex[i]].y);	

		//final Position of dogs
		Point[] fPos = new Point[numDog];
		for(int i=0; i<numDog; i++){

			//set delta to avoid not hopping over sheep
			if(dog[i].x < sheep[sheepIndex[i]].x)
				delta = (right ? 2.5 : 1);
			else
				delta = (right ? 1 : 2.5);
			
			Point vector = new Point(tSheep[i].x - 50., tSheep[i].y - 50.);
			Point origin = new Point(0, 0);
			double dist = Formation.distance(origin, vector);
			double factor = (dist + delta) / dist;
	
			fPos[i] = new Point(50. + vector.x * factor, 50. + vector.y * factor);
			//set boundary
			if(right){
				if(fPos[i].x < 50)
					fPos[i].x = 50;
			}else{
				if(fPos[i].x > 50)
					fPos[i].x = 50;
			}
			if(fPos[i].x > 100)
				fPos[i].x = 100;
			if(fPos[i].y > 100)
				fPos[i].y = 100;
			if(fPos[i].y < 0)
				fPos[i].y = 0;
		}
		return fPos;
	}
	
	//returns index of sheepStart
	//0 = black, 1 = white, 2 = all
	public int sheepStart(int i){
		if(i==0)
			return 0;
		else if(i==1)
			return nblacks;
		else
			return 0;
	}
	//returns index of sheepStop
	//0 = black, 1 = white, 2 = all
	public int sheepStop(int i){
		if(i==0)
			return nblacks;
		else if(i==1)
			return sheep.length;
		else
			return sheep.length;
	}
}
