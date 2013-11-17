package sheepdog.g2;

import sheepdog.sim.Point;

enum State {
	INIT, ALIGNDOG, PUSHSHEEP;
}

public class OneDogPlayer extends sheepdog.sim.Player {

	//Constants
	private final Point gatePoint=new Point(50,50);
	private final double epsilon=0.01;
	
	//Bookkeeping
	Point currDestination;
	State currState;
	
	public OneDogPlayer()
	{
		currState=State.INIT;
		currDestination=new Point();
		
		setDestination(gatePoint);
	}
	
	@Override
	public Point move(Point[] dogs, Point[] sheeps) {
		printStats();
		
		// TODO Auto-generated method stub
		assert dogs.length == 1;
		
		if(currState==State.INIT){
			moveTowardsDestination(false, dogs[0]);
			if(distance(dogs[0],gatePoint)<=epsilon)
			{
				System.out.println(distance(dogs[0],gatePoint));
				System.out.println("State: ALIGNDOG");
				this.currState=State.ALIGNDOG;
			}
		}
		else if(currState == State.ALIGNDOG){
			Point current = dogs[0];
			int lastSheep = selectSheep(sheeps, current);
			moveBehindSheep(sheeps[lastSheep], current);

			if (alignedDog(sheeps[lastSheep], current) && current.x > sheeps[lastSheep].x){
				System.out.println("State changed to PUSHSHEEP!!");
			}
			// currDestination = sheeps[lastSheep];
			// moveTowardsDestination(true, dogs[0]);
		}

		println(String.format("Moving to x=%f, y=%f",dogs[0].x,dogs[0].y));

		return dogs[0];
	}
	
	public void moveTowardsDestination(boolean extend, Point currPos)
	{
		moveTowardsDestination(extend, currPos, 2.0);
	}
	
	public void moveTowardsDestination(boolean extend, Point currPos, double distance)
	{
		double maxDist=distance(currPos,currDestination);
		if(extend)
			maxDist+=1;

		if(maxDist<distance)
			distance=maxDist;
		
		double xmod=currPos.x - currDestination.x;
		double ymod=currPos.y - currDestination.y;
				
		double theta=Math.atan(Math.abs(ymod/xmod));
		
		double xdist=distance*Math.cos(theta);
		double ydist=distance*Math.sin(theta);
		
		if(xmod<0)
		{
			currPos.x+=xdist;
		}
		else
		{
			currPos.x-=xdist;
		}
		
		if(ymod<0)
		{
			currPos.y+=ydist;
		}
		else
		{
			currPos.y-=ydist;
		}
	}

	public void moveBehindSheep(Point sheep, Point currPos){
		double xdiff = sheep.x - currPos.x;
		double ydiff = sheep.y - currPos.y;
		double theta = Math.atan(ydiff/xdiff);
		double dist = distance(gatePoint, sheep) + 1;

		currDestination.y = gatePoint.y + (dist * Math.sin(theta));
		currDestination.x = gatePoint.x + (dist * Math.cos(theta));
		System.out.println("Current Destination: " + currDestination.x + " " + currDestination.y);
		moveTowardsDestination(false, currPos);
	}

	public int selectSheep(Point[] sheeps, Point dog){
		int lastSheep = -1;
		double maxX = -1;
		if (dog.x >= 50){
			for (int i = 0; i < sheeps.length; i++){
				if (maxX < sheeps[i].x){
					maxX = sheeps[i].x;
					lastSheep = i;
				}
			}
		}
		return lastSheep;
	}
	
	public boolean alignedDog(Point sheep, Point dog){
		double m = (dog.y-sheep.y)/(dog.x-sheep.x);
		double c =	dog.y - (m*dog.x);
		double expectedY = gatePoint.x*m + c;
		if (Math.abs(gatePoint.y - expectedY) < epsilon)
			return true;
		else
			return false;
	}

	public void setDestination(Point p)
	{
		currDestination.x=p.x;
		currDestination.y=p.y;
	}
	
	public void printStats()
	{
		println("Dest="+this.currDestination.x+" "+this.currDestination.y);
		println("State="+this.currState);
	}
	
	public double distance(Point a, Point b)
	{
		return Math.sqrt(Math.pow(a.x-b.x, 2)+Math.pow(a.y-b.y, 2));
	}
	
	public void println(String s)
	{
		System.out.println(s);
	}
}
