package sheepdog.g2;

import sheepdog.sim.Point;


public class OneDogPlayer extends sheepdog.sim.Player {

	//Constants
	private final Point gatePoint=new Point(50,50);
	private final double epsilon=0.01;
	
	//Bookkeeping
	Point currDestination;
	State currState;
	int sheepFocus;
	@Override
	public void init(int nblacks, boolean mode) {
		// TODO Auto-generated method stub
		
	}
	
	public OneDogPlayer()
	{
		currState=State.INIT;
		currDestination=new Point();
		sheepFocus=-1;
		
		setDestination(gatePoint);
	}
	
	@Override
	public Point move(Point[] dogs, Point[] sheeps) {
		
		printStats(dogs,sheeps);
		
		assert dogs.length == 1;
		
		if(currState==State.INIT){
			
			moveTowardsDestination(false, dogs[0]);
			
			if(distance(dogs[0],gatePoint)<=epsilon)
			{
				System.out.println("State change to: ALIGNDOG");
				this.currState=State.MANIPULATESHEEP;
			}
		}
		
		else if(currState == State.MANIPULATESHEEP){
			
			Point current = dogs[0];
			int selectedSheep = selectSheep(sheeps, current);
			manipulateSheep(sheeps[selectedSheep], current);
		}

		println(String.format("Moving to x=%f, y=%f",dogs[0].x,dogs[0].y));
		
		return dogs[0];
	}
	
	public void moveTowardsDestination(boolean extend, Point currPos)
	{
		moveTowardsDestination(extend, currPos, 1.99);
	}
	
	public void moveTowardsDestination(boolean extend, Point currPos, double hopDistance)
	{
		double remainingDistance=distance(currPos,currDestination);
		if(extend)
			remainingDistance+=1;

		if(remainingDistance<hopDistance)
			hopDistance=remainingDistance;
		
		double xmod=currPos.x - currDestination.x;
		double ymod=currPos.y - currDestination.y;
				
		double theta=Math.atan(Math.abs(ymod/xmod));
		
		if(xmod==0)
			theta=Math.PI/2;
		
		double xdist=hopDistance*Math.cos(theta);
		double ydist=hopDistance*Math.sin(theta);
		
		println(String.format("xdist=%f, ydist=%f, currX=%f,currY=%f, theta=%f \n xmod=%f, ymod=%f",
				xdist,ydist,currPos.x,currPos.y,theta,xmod,ymod));
		
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
		
		println(String.format("Moving towards x=%f, y=%f",currDestination.x,currDestination.y));
		println(String.format("Current positionx=%f, y=%f",currPos.x,currPos.y));
	}

	public void manipulateSheep(Point sheep, Point currPos){
		
		double xdiff = sheep.x - gatePoint.x;
		double ydiff = sheep.y - gatePoint.y;
		double theta = Math.atan(ydiff/xdiff);
		double dist = distance(gatePoint, sheep) + 1;

		currDestination.y = gatePoint.y + (dist * Math.sin(theta));
		currDestination.x = gatePoint.x + (dist * Math.cos(theta));
		
		println(String.format("Dog will go to %f,%f",currDestination.x,currDestination.y));
		println(String.format("Theta for dog=%f, theta for sheep=%f",getThetaFromGate(currPos),getThetaFromGate(sheep)));
		println(String.format("Difference between the two is=%f",getThetaFromGate(currPos)-getThetaFromGate(sheep)));

		moveTowardsDestination(false, currPos);
	}
	
	public double getThetaFromGate(Point p)
	{
		return Math.atan((p.y-gatePoint.y)/(p.x-gatePoint.x));
	}

	public int selectSheep(Point[] sheeps, Point dog){
		if(sheepFocus!=-1 && sheeps[sheepFocus].x>=50)
			return sheepFocus;
		
		int rightMostSheep = -1;
		double maxX = -1;
		if (dog.x >= 50){
			for (int i = 0; i < sheeps.length; i++){
				if (maxX < sheeps[i].x){
					maxX = sheeps[i].x;
					rightMostSheep = i;
				}
			}
		}
		sheepFocus=rightMostSheep;
		return rightMostSheep;
	}
	
	public boolean dogOnLine(Point sheep, Point dog){
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
	
	public void printStats(Point[] dogs, Point[] sheep)
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
