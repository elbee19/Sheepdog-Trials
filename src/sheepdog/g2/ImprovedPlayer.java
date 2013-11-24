package sheepdog.g2;

import java.util.Arrays;
import java.util.Random;

import sheepdog.sim.Point;

public class ImprovedPlayer extends sheepdog.sim.Player {

	//Constants
	private final Point gatePoint=new Point(50,50);
	private final double epsilon=0.01;
	private final int stepsToPursue=1;
	private int tooCloseThreshold;
	
	//Bookkeeping
	Point currDestination;
	State currState;
	int sheepFocus; //x
	boolean firstCall;
	int[] tooClose;
	int stepsPursued;
	
	//Others
	Random r;
	@Override
	public void init(int nblacks, boolean mode) {
		// TODO Auto-generated method stub
		
	}
	public ImprovedPlayer()
	{
		stepsPursued=0;
		currState=State.INIT;
		currDestination=new Point();
		sheepFocus=-1;
		firstCall=true;
		
		setDestination(gatePoint);
	}
	
	@Override
	public Point move(Point[] dogs, Point[] sheeps) {
		
		if(firstCall)
		{
			r=new Random();
			tooClose=new int[dogs.length];
			tooCloseThreshold=(new Random()).nextInt(5);
		}
		
		if(currState==State.INIT){
			
			moveTowardsDestination(false, dogs[this.id-1]);
			
			if(distance(dogs[this.id-1],gatePoint)<=epsilon)
			{
				System.out.println("State change to: ALIGNDOG");
				this.currState=State.MANIPULATESHEEP;
			}
		}
		
		else if(currState == State.MANIPULATESHEEP){
			//pursue for no of ticks and then continue
			
			//Find sheep with minimum distGate-distToSheep
			if(stepsPursued==0)
				selectSheep(sheeps, dogs[this.id-1]); //sets sheepFocus
			
			println(String.format("selected sheep %d",sheepFocus));
			
			//Position behind towards gate, set counter to 0 [check this by distSheep<distDog and aligned]
			manipulateSheep(sheeps[sheepFocus], dogs[this.id-1]);
			stepsPursued++;
			if(stepsPursued==stepsToPursue)
				stepsPursued=0;
		}
		
		println(String.format("Steps pursued=%d, total=%d",stepsPursued,stepsToPursue));
		
		applyCorrection(dogs[this.id-1]);
		
		return dogs[this.id-1];
		
		/*assert dogs.length != 1;
		
		if(currState==State.INIT){
			
			moveTowardsDestination(false, dogs[this.id-1]);
			
			if(distance(dogs[this.id-1],gatePoint)<=epsilon)
			{
				System.out.println("State change to: ALIGNDOG");
				this.currState=State.MANIPULATESHEEP;
			}
		}
		
		else if(currState == State.MANIPULATESHEEP){
			
			Point current = dogs[this.id-1];
			int selectedSheep = selectSheep(sheeps, current);
			manipulateSheep(sheeps[selectedSheep], current);
		}

		incrementIfClose(dogs,sheeps);
		
		return dogs[this.id-1];*/
	}
	
	public void applyCorrection(Point p)
	{
		if(p.x>=100)
			p.x=99.99;
		if(p.y<=0)
			p.y=0.01;
		if(p.y>=100)
			p.y=99.99;
	}
	
	public void incrementIfClose(Point[] dogs,Point[] sheep)
	{
		for(int i=0;i<tooClose.length;i++)
		{
			if(this.id-1==i)
				continue;
			if(distance(dogs[i],dogs[this.id-1])<0.01)
			{
				tooClose[i]++;
				if(tooClose[i]==tooCloseThreshold)
				{
					tooClose[i]=0;
					sheepFocus=r.nextInt(sheep.length);
					while(sheepFocus==i || sheep[sheepFocus].x<50)
						sheepFocus=r.nextInt(sheep.length);
						
				}
			}
			else
				tooClose[i]=0;
		}
		println(String.format("For dog#%d, tooCloseThreshold=%d",this.id-1,tooCloseThreshold));
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
		println(String.format("Moving to positionx=%f, y=%f",currPos.x,currPos.y));
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

	public void selectSheep(Point[] sheeps, Point dog){
		
		int maxSheepIndex=-1;
		double maxSheepDist=-99999999999.0;
		
		for(int i=0;i<sheeps.length;i++)
		//for(Point sheep : sheeps)
		{
			if(sheeps[i].x<50)
				continue;
			double sheepGateDist=distance(gatePoint, sheeps[i]);
			double sheepDogDist=distance(sheeps[i],dog);
			double diffDist=3*sheepGateDist-sheepDogDist;
			
			println(String.format("Sheep %d dist %f max=%f",i,diffDist,maxSheepDist));
			
			if(diffDist>maxSheepDist)
			{
				
				maxSheepIndex=i;
				maxSheepDist=diffDist;
			}
		}
		
		sheepFocus = maxSheepIndex;
		/*if(sheepFocus!=-1 && sheeps[sheepFocus].x>=50)
			return sheepFocus;
		
		int sheepChosen=r.nextInt(sheeps.length);
		
		while(sheeps[sheepChosen].x<50)
		{
			sheepChosen=r.nextInt(sheeps.length);
		}
		
		sheepFocus=sheepChosen;
		return sheepChosen;*/
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
		
		double dist=Math.sqrt(Math.pow(a.x-b.x, 2)+Math.pow(a.y-b.y, 2));
		println(String.format("%f,%f - %f,%f=%f",a.x,a.y,b.x,b.y,dist));
		return dist;
	}
	
	public void println(String s)
	{
		System.out.println(s);
	}
}
