package sheepdog.g2;

import java.util.Arrays;
import java.util.Random;

import sheepdog.sim.Point;

public class ImprovedPlayerOld extends sheepdog.sim.Player {

	//config
	int nblacks;
	boolean mode;
	
	//Constants
	private final Point gatePoint=new Point(50,50);
	private final double epsilon=0.01;
	private final int stepsToPursue=3;
	
	private int tooCloseThreshold;
	private boolean takeCareOfYellowSheep=true;
	
	//Bookkeeping
	Point currDestination;
	State currState;
	int sheepFocus;
	boolean firstCall;
	int[] tooClose;
	int stepsPursued;
	boolean chasingSheepOut;
	
	double closestWhiteSheep;
	double closestBlackSheep;
	double furthestBlackSheep;
	int closestWhitePoint;
	int closestBlackPoint;
	int furthestBlackPoint;
	
	//Others
	Random r;
	
	public void init(int nblacks, boolean mode) {
		this.nblacks=nblacks;
		this.mode=mode;
	}
	
	public ImprovedPlayerOld()
	{
		stepsPursued=0;
		currState=State.INIT;
		currDestination=new Point();
		sheepFocus=-1;
		firstCall=true;
		r=new Random();

		closestWhiteSheep=Double.MAX_VALUE;
		closestBlackSheep=Double.MAX_VALUE;
		furthestBlackSheep=Double.MIN_VALUE;
		
		setDestination(gatePoint);
	}
	
	@Override
	public Point move(Point[] dogs, Point[] sheeps) {
		
		if(firstCall)
		{
			tooClose=new int[dogs.length];
			tooCloseThreshold=(new Random()).nextInt(5);
			firstCall=false;
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
			
			boolean[] sheepsInSector=mapSheep2(dogs, sheeps, this.id-1); //This method calculates the closest and furthest black sheeps as well, hence call right at the start
			double ownDistance=distance(gatePoint,dogs[this.id-1]);
			
			boolean preConditions=takeCareOfYellowSheep && mode && this.id-1==0;
			
			if( preConditions && chasingSheepOut)
			{
				double gateTargetDistance = distance(sheeps[sheepFocus],gatePoint);
				if(gateTargetDistance>20 && gateTargetDistance>furthestBlackSheep+2) //Parameters
				{
					chasingSheepOut=false;
				}
			}
			
			println(String.format("--- %b Closestblack %f furthestblack %f closestwhite %f",preConditions,closestBlackSheep,furthestBlackSheep,closestWhiteSheep));
			
			if( preConditions &&
			(chasingSheepOut || ((/*closestBlackSheep<=3.0 ||*/ furthestBlackSheep<=30.0) && closestWhiteSheep<furthestBlackSheep)) //Check if an obnoxious sheep should be pushed out //parameters
			&& sheeps[closestWhitePoint].x>50 ) //Don't consider sheep if it was already pushed
			{
				println("Chasing Yellow out now");
				
				if(!chasingSheepOut)
				{
					chasingSheepOut=true;
					sheepFocus=closestWhitePoint;
					stepsPursued=0;
				}
				
				chaseSheepOut(sheeps[sheepFocus], dogs[this.id-1]);
			}
			else //Do the usual thing
			{
				println("------Doing the usual thing");
				
				//pursue for no of ticks and then continue
				if(stepsPursued==0)
				{
					selectSheep(sheeps, dogs[this.id-1], sheepsInSector); //sets sheepFocus
					
					if(sheepFocus==-1)
					{
						/*if(dogs[this.id-1].x>46)
							dogs[this.id-1].x-=1.99;*/
						for(int i=0;i<dogs.length;i++)
						{
							if(i==this.id-1)
								continue;
							
							sheepsInSector=mapSheep2(dogs, sheeps, i);
							selectSheep(sheeps, dogs[this.id-1], sheepsInSector);
							if(sheepFocus!=-1)
								break;
						}
						if(sheepFocus==-1)
							return dogs[this.id-1];
					}
					
				}
				
				println(String.format("selected sheep %d",sheepFocus));
				
				//Position behind towards gate, set counter to 0 [check this by distSheep<distDog and aligned]
				manipulateSheep(sheeps[sheepFocus], dogs[this.id-1]);
				stepsPursued++;
				if(stepsPursued==stepsToPursue)
					stepsPursued=0;
				
			}
			
			
		}
		
		println(String.format("Steps pursued=%d, total=%d",stepsPursued,stepsToPursue));
		
		applyCorrection(dogs[this.id-1]);
		return dogs[this.id-1];
		
	}
	
	public void applyCorrection(Point p)
	{
		if(p.x>=100)
			p.x=99.99;
		if(p.y<=0)
			p.y=0.01;
		if(p.y>=100)
			p.y=99.99;
		
		//useless
		if(p.x<0)
			p.x=0.01;
	}
	
	public void incrementIfClose(Point[] dogs,Point[] sheep)
	{
		
		//TODO: This method was very useful for the one by one strategy, but right now 
		// we should change code so that we don't have to use this method anymore.
		
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
					while(sheepFocus==i || sheep[sheepFocus].x<50) //Randomly keep picking next sheep if you're overlapping
						sheepFocus=r.nextInt(sheep.length);
				}
			}
			else
				tooClose[i]=0;
		}
		//println(String.format("For dog#%d, tooCloseThreshold=%d",this.id-1,tooCloseThreshold));
	}
	
	public void moveTowardsDestination(boolean extend, Point currPos)
	{
		moveTowardsDestination(extend, currPos, 1.99);
	}
	
	public void moveTowardsDestination(boolean extend, Point currPos, double hopDistance)
	{
		//extend boolean is deprecated, and hence useless
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
		double dist = distance(gatePoint, sheep) + 1; //This is why the extend boolean is deprecated in the prev method

		currDestination.y = gatePoint.y + (dist * Math.sin(theta));
		currDestination.x = gatePoint.x + (dist * Math.cos(theta));
		
		/*dbg
		 * println(String.format("Dog will go to %f,%f",currDestination.x,currDestination.y));
		println(String.format("Theta for dog=%f, theta for sheep=%f",getThetaFromGate(currPos),getThetaFromGate(sheep)));
		println(String.format("Difference between the two is=%f",getThetaFromGate(currPos)-getThetaFromGate(sheep)));
*/
		//moveTowardsDestination(false, currPos);
		moveTowardsDestination(true, currPos);
	}
	
	public void chaseSheepOut(Point sheep, Point currPos){
		
		double xdiff = sheep.x - gatePoint.x;
		double ydiff = sheep.y - gatePoint.y;
		double theta = Math.atan(ydiff/xdiff);
		double dist = distance(gatePoint, sheep) - 1; //This is why the extend boolean is deprecated in the prev method

		currDestination.y = gatePoint.y + (dist * Math.sin(theta));
		currDestination.x = gatePoint.x + (dist * Math.cos(theta));
		
		println("Chasing out");
		println(String.format("Dog will go to %f,%f",currDestination.x,currDestination.y));
		println(String.format("Theta for dog=%f, theta for sheep=%f",getThetaFromGate(currPos),getThetaFromGate(sheep)));
		println(String.format("Difference between the two is=%f",getThetaFromGate(currPos)-getThetaFromGate(sheep)));

		moveTowardsDestination(false, currPos);
	}
	
	public double getThetaFromGate(Point p)
	{
		return Math.atan((p.y-gatePoint.y)/(p.x-gatePoint.x));
	}

	public void selectSheep(Point[] sheeps, Point dog, boolean[] sheepsInSector){
		
		int maxSheepIndex=-1;
		double maxSheepDist=-99999999999.0;
		
		for(int i=0;i<sheeps.length;i++)
		//for(Point sheep : sheeps)
		{
			if(sheeps[i].x<50 || sheepsInSector[i]==false)
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
		
		return dist;
	}
	
	public void println(String s)
	{
		System.out.println(s);
	}
	
public boolean[] mapSheep2(Point[] dogs, Point[] sheep) {
		
		return mapSheep2(dogs,sheep,this.id-1);
	}
	
	public boolean[] mapSheep2(Point[] dogs, Point[] sheep, int dogSectorId) {
		
	
		boolean[] sheepsInSector=new boolean[sheep.length];
		double angle=180/(1.0*dogs.length);
		
		double currentSectorStart=(dogSectorId)*angle-90;
		double currentSectorEnd=currentSectorStart+angle;
		
		int i=0;
		for(Point p:sheep)
		{	
			double gateDistance=distance(gatePoint,p);
			
			if(i<nblacks) //blacksheep
			{
				if(closestBlackSheep==Double.MAX_VALUE || gateDistance<=distance(gatePoint,sheep[closestBlackPoint]))
				{
					closestBlackSheep=gateDistance;
					closestBlackPoint=i;
				}
				
				if(furthestBlackSheep==Double.MIN_VALUE || gateDistance>=distance(gatePoint,sheep[furthestBlackPoint]))
				{
					furthestBlackSheep=gateDistance;
					furthestBlackPoint=i;
				}
			}
			else //white
			{
				if(closestWhiteSheep==Double.MAX_VALUE || gateDistance<distance(gatePoint,sheep[closestWhitePoint]))
				{
					closestWhiteSheep=gateDistance;
					closestWhitePoint=i;
				}
			}
			
			//System.out.println(String.format("mode %b blacks %d and current %d",mode,nblacks,i));
			if(mode && i>=nblacks)
			{
				i++;
				continue;
			}
			double angleP=Math.toDegrees(Math.atan((p.y-gatePoint.y)/(p.x-gatePoint.x)));
			//println(String.format("Angle for sheep %f,%f is %f",p.x,p.y,angleP));
			//println(String.format("Range for this dog is [%f,%f) %n",currentSectorStart,currentSectorEnd));
			if(angleP>=currentSectorStart && angleP<currentSectorEnd)
				sheepsInSector[i]=true;
			i++;
		}
		
		return sheepsInSector;
		
	}
}
