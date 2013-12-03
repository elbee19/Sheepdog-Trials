package sheepdog.g1;

import sheepdog.sim.Point;

public class Formation{

	public Point[] dog;
	public int numDog;
	public Point[] formation;
	public double speed;

	public static final int Dot = 0;
	public static final int Vertical = 1;
	public static final int Sandwich = 2;
	public static final int Pusher = 3;
	public static final int VPusher = 4;

	public Formation(double[] offset, int formation, Point[] dog, double speed){

		this.dog = dog;
		this.numDog = dog.length;
		this.formation = new Point[numDog];
		this.speed = speed;

		switch (formation){
			case 0: formDot(offset);
					break;
			case 1: formVertical(offset);
					break;
			case 2: formSandwhich(offset);
					break;
			case 3: formPusher(offset);
					break;
			default: formVPusher(offset);
					break;
		}
	}

	//dog stack
	//offset[0] = x; offset[1] = y;
	public void formDot(double[] offset){
		
		for(int i=0; i<numDog; i++){
			formation[i] = new Point(offset[0], offset[1]);
		}
		
	}
	
	//vertical line
	//0 = x; 1 = y; 2 = distance
	public void formVertical(double[] offset){
/*
		for(int i=0; i<numDog; i++){
			formation[i] = new Point(offset[0], (double)(i)*offset[2]/(double)(numDog-1)+offset[1]);
		}
*/

		//top
		for(int i=0; i<numDog/2; i++){
			formation[i] = new Point(offset[0], (i)*offset[2]/(numDog-1)+offset[1]);
		}

		//down
		for(int i=numDog/2; i<numDog; i++){
			formation[i] = new Point(offset[0], (numDog/2-i)*offset[2]/(numDog-1)+offset[1]+offset[2]); 
		}
	}

	//top down horizontal line; x between 50 and 75;
	//0 = dist between top&down; 1 = length of sandwhich; 2 = x of first sheep
	public void formSandwhich(double[] offset){
		//down
		for(int i=0; i<numDog/2; i++){
			formation[i] = new Point((double)(i)*offset[1]/(double)(numDog/2-1)+offset[2], 50. - offset[0]/2.);
		}
		//top
		for(int i=numDog/2; i<numDog; i++){
			formation[i] = new Point((double)(i-numDog/2)*offset[1]/(double)(numDog/2-1)+offset[2], 50 + offset[0]/2.);
		}

	}

	//top down with pusher;
	//0 = distance between top&down; 1 = length of sandwhich; 2 = x of pusher
	public void formPusher(double[] offset){
		//down
		for(int i=0; i<numDog/2-1; i++){
			formation[i] = new Point((double)(i)*offset[1]/(double)(numDog/2-1)+50, 50-offset[0]/2.);
		}
		//top
		for(int i=numDog/2; i<numDog-1; i++){
			formation[i] = new Point((double)(i-(numDog/2))*offset[1]/(double)(numDog/2-1)+50, 50+offset[0]/2.);
		}
		
		//pusher
		formation[numDog/2-1] = new Point(offset[2], 51.);
		formation[numDog-1] = new Point(offset[2], 49.);

	}

	//sideways V
	//0 = x of farthest dog
	public void formVPusher(double[] offset){

		/* tries to make the form:   \
									 /
		*/

		// form: /

		for(int i=0; i<numDog/2; i++){
			formation[i] = new Point(offset[0] - 2*(numDog/2 - i - 1), 49 - 2*(numDog/2-1 - i));
		}
		
		for(int i=numDog/2; i<numDog; i++){
			formation[i] = new Point(offset[0] - 2*(numDog/2 - (i-(numDog/2)) - 1), 51 + 2*(numDog/2 - (i-(numDog/2-1))));
		}
	}

	public boolean isDone(){
		boolean[] filled = new boolean[numDog];
		
		for(int i=0; i<numDog; i++)
			filled[i] = false;
		
		//cross check dog and formation
		//if all formation position has a dog; return true
		for(int i=0; i<numDog; i++){
	//		for(int j=0; j<numDog; j++){
	//			if(this.formation[i].equals(this.dog[j]))
	//				filled[i] = true;
	//		}
			if(this.formation[i].equals(this.dog[i]))
				filled[i] = true;
		}
		
		for(int i=0; i<numDog; i++){
			if(!filled[i])
				return false;
		}
		return true;
	}

	public Point[] getMove(Point[] dog){
		return this.getMove(dog, this.speed);
	}

	public Point[] getMove(Point[] dog, double speed){
		
		this.dog = dog;
		double maxDist = speed * 0.1 - 0.05;
		Point[] nextPos = new Point[numDog];

		for(int i=0; i<numDog; i++){
			if(distance(dog[i], formation[i]) <= maxDist)
				nextPos[i] = new Point(formation[i].x, formation[i].y);
			else{
				Point vector = new Point(formation[i].x - dog[i].x, formation[i].y - dog[i].y);
				Point origin = new Point(0, 0);
				double factor = maxDist / distance(origin, vector);
				
				nextPos[i] = new Point(dog[i].x + vector.x * factor, dog[i].y + vector.y * factor);
			}
		}
		return nextPos;
	}
	
	public double distance(Point a, Point b){
		
		return Math.sqrt((a.x - b.x)*(a.x - b.x)+(a.y - b.y)*(a.y - b.y));
	}
}

