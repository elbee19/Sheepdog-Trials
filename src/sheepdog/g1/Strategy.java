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
	private int numDog;
	private int numSheep;
	private int strategy;
	public Queue<Formation> steps;
	private boolean initSteps;
	private int stepsIter;
	
	public Strategy(int strategy) {
		this.dog = null;
		this.sheep = null;
		this.strategy = strategy;
		initSteps = true;
		stepsIter = 0;
	}

	public Strategy(Point[] dog, Point[] sheep, int strategy){
		this.dog = dog;
		this.sheep = sheep;
		this.strategy = strategy;
		this.numDog = dog.length;
		this.numSheep = sheep.length;
		initSteps = true;
		stepsIter = 0;
	}

	public void updateInfo(Point[] dog, Point[] sheep){
		this.dog = dog;
		this.sheep = sheep;
		this.numDog = dog.length;
		this.numSheep = sheep.length;
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
	
	//Effective for large numbers of sheeps to herd.
	public void strategyOne() {
		Formation exec = null;
		if(initSteps) {
			steps = new LinkedList<Formation>();

			//Check boolean
			double spacing = (numDog/2)*2.5;
				if(spacing > 50)
					spacing = 50;

			int sweeps = (int)Math.ceil(50./spacing);

			//dot
			steps.add(new Formation(new double[]{50.0, 50.0}, Formation.Dot, dog, 20.0));
			
			//Sandwich
			for(int i=0; i<sweeps-1; i++){
				steps.add(new Formation(new double[]{100.0, spacing, 50. + i*spacing}, Formation.Sandwich, dog, 20.0));
				steps.add(new Formation(new double[]{4.0, spacing, 50. + i*spacing}, Formation.Sandwich, dog, 2.0));
			}

			steps.add(new Formation(new double[]{100.0, spacing, 100 - spacing}, Formation.Sandwich, dog, 20.0));
			steps.add(new Formation(new double[]{4.0, spacing, 100 - spacing}, Formation.Sandwich, dog, 2.0));
			
			//VPusher -> Vertical Line
			steps.add(new Formation(new double[]{100.0}, Formation.VPusher, dog, 20.0));
			steps.add(new Formation(new double[]{50. + numDog -2}, Formation.VPusher, dog, 2.0));
			steps.add(new Formation(new double[]{50.0, 50 - spacing/2, spacing}, Formation.Vertical, dog, 1.0));
			initSteps = false;
		}
		
		if( (exec = steps.peek()) == null)
			return;
		
		double speed = 1.0;
		double offset = 0.0;
		
		if(!exec.isDone())
			newDog = exec.getMove(dog);
		else {
			steps.remove();
			
			if(steps.peek() == null)
				initSteps = true;
		}
		
		/*
		if(initSteps) {
			stepsIter = 0;
			strategySteps = new boolean[7];
			for(int i=0; i < strategySteps.length; i++)
				strategySteps[i] = false;
			initSteps = false;
		}

		//Formation Zero
		Formation zero = new Formation(50.0, 0, dog);
		if(!strategySteps[0] && !zero.isDone()) {
			speed = 20.0;
			newDog = zero.getMove(dog, speed);
			return;
		}
		else if(!strategySteps[0]) {
			strategySteps[stepsIter] = true;
			stepsIter++;
		}
		
		//Formation One
		Formation one = new Formation(100.0, 1, dog);
		if(!strategySteps[1] && !one.isDone()) {
			if(getFurthestDog().x > 90.0)
				speed = 5.0;
			else
				speed = 20.0;	//Move dog into Line formation, set it at maximum speed. Can Jiacheng make the variable public in Sheepdog.java?
			newDog = one.getMove(dog, speed);
			System.out.println("Formation1, 100");
			return;
		}
		else if(!strategySteps[1])  {
			strategySteps[stepsIter] = true;
			stepsIter++;
		}
		
		//Formation One
		Formation one2 = new Formation(75.0, 1, dog);
		if(!strategySteps[2] && !one2.isDone()) {
			speed = 1.0;	//Move dog into Line formation, set it at maximum speed. Can Jiacheng make the variable public in Sheepdog.java?
			newDog = one2.getMove(dog, speed);
			System.out.println("Formation1, 75");
			return;
		}
		else if(!strategySteps[2]) {
			strategySteps[stepsIter] = true;
			stepsIter++;
		}
		
		//Formation Two. 
		Formation two = new Formation(100.0, 2, dog);
		if(!strategySteps[3] && !two.isDone()) {
			speed = 20.;
			newDog = two.getMove(dog, speed);
			System.out.println("Formation2, 100");
			return;
		}
		else if(!strategySteps[3]) {
			strategySteps[stepsIter] = true;
			stepsIter++;
		}
		
		//Formation Two.
		Formation two2 = new Formation(4, 2, dog);
		if(!strategySteps[4] && !two2.isDone()) {
			speed = 1.0;
			newDog = two2.getMove(dog, speed);
			return;
		}
		else if(!strategySteps[4]) {
			strategySteps[stepsIter] = true;
			stepsIter++;
		}
		
		//Formation Three
		Formation three = new Formation(75.0, 3, dog);
		if(!strategySteps[5] && !three.isDone()) {
			speed = 20.;
			newDog = three.getMove(dog, speed);
			return;
		}
		else if(!strategySteps[5]) {
			strategySteps[stepsIter] = true;
			stepsIter++;
		}
		
		//Formation Three
		Formation three2 = new Formation(50.0, 3, dog);
		if(!strategySteps[6] && !three2.isDone()) {
			speed = 3.0;
			newDog = three2.getMove(dog, speed);
			return;
		}
		else if(!strategySteps[6]) {
			strategySteps[stepsIter] = true;
			stepsIter++;
		}
		
		if(strategySteps[6])
			initSteps = true;
			
		*/
	}
}
