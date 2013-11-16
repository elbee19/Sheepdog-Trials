package sheepdog.g2;

import sheepdog.sim.Point;

enum State {
	INIT,CROSSINGSHEEP,CROSSEDSHEEP;
}

public class OneDogPlayer extends sheepdog.sim.Player {

	//Constants
	private final Point gatePoint=new Point(50,50);
	
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
		
		if(currState==State.INIT)
			moveTowardsDestination(dogs[0]);
		
		println(String.format("Moving to x=%f, y=%f",dogs[0].x,dogs[0].y));
		return dogs[0];
	}
	
	public void moveTowardsDestination(Point currPos)
	{
		moveTowardsDestination(currPos, 2.0);
	}
	
	public void moveTowardsDestination(Point currPos, double distance)
	{
		double maxDist=distance(currPos,currDestination);
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
		
		println(String.format("Ashlesha stfu = %f",Math.sqrt(xdist*xdist+ydist*ydist)));
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
