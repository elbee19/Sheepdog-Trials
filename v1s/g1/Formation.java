package sheepdog.g1;

import sheepdog.sim.Point;

public class Formation{

	public Point[] dog;
	public int numDog;
	public Point[] formation;

	public Formation(double offset, int formation, Point[] dog){

		this.dog = dog;
		this.numDog = dog.length;
		this.formation = new Point[numDog];

		switch (formation){
			case 0: setFormationZero(offset);
					break;
			case 1: setFormationOne(offset);
					break;
			case 2: setFormationTwo(offset);
					break;
			default: setFormationThree(offset);
					break;
		}
	}

	//dog stack
	//offset = x
	public void setFormationZero(double offset){
		
		for(int i=0; i<numDog; i++){
			formation[i] = new Point(offset, 50.);
		}
		
	}
	
	//vertical line
	//offset = x
	public void setFormationOne(double offset){

		for(int i=0; i<numDog; i++){
			formation[i] = new Point(offset, (double)(i)*100./(double)(numDog-1));
		}
	}

	//top down horizontal line; x between 50 and 75;
	//offset = dist between top&down
	public void setFormationTwo(double offset){
		//down
		for(int i=0; i<numDog/2; i++){
			formation[i] = new Point((double)(i)*30./(double)(numDog/2)+50, 50. - offset/2.);
		}
		//top
		for(int i=numDog/2; i<numDog; i++){
			formation[i] = new Point((double)(i-numDog/2)*30./(double)(numDog/2)+50, 50 + offset/2.);
		}

	}

	//top down with pusher;
	//offset = distance between pusher and entrance
	public void setFormationThree(double offset){
		//down
		for(int i=0; i<numDog/2-1; i++){
			formation[i] = new Point((double)(i)*30./(double)(numDog/2)+50, 48.);
		}
		//top
		for(int i=numDog/2; i<numDog-1; i++){
			formation[i] = new Point((double)(i-(numDog/2))*30./(double)(numDog/2)+50, 52.);
		}
		
		//pusher
		formation[numDog/2-1] = new Point(offset, 51.);
		formation[numDog-1] = new Point(offset, 49.);

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

