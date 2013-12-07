package sheepdog.g2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import sheepdog.sim.Point;

public class ImprovedPlayer extends sheepdog.sim.Player {

	//Constants
	private final Point gatePoint=new Point(50,50);
	private final double epsilon=0.01;
	private final int stepsToPursue=4;
	private int tooCloseThreshold;
	private final double initialRadius=1000.0;
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
				System.out.println("State change to: BRING_SHEEP_IN");
				this.currState=State.BRING_SHEEP_IN;
			}
		}
		else if(currState == State.BRING_SHEEP_IN){
			
			boolean[] sheepsForThisDog=mapSheep2(dogs,sheeps);
			
			int minAngleSheepReturns=minAngleSheep(sheeps,sheepsForThisDog);
			println("!! Minanglesheep="+minAngleSheepReturns);
			
			if(sheepFocus == -1)
			{
				System.out.println("State change to: PUSH_SHEEP");
				this.currState=State.PUSH_SHEEP;
			}
			
			else
			{
				println("!! Manipulating at index="+sheepFocus);
				manipulateSheep(sheeps[sheepFocus], dogs[this.id-1]);
			}
			
		}
		if(currState == State.PUSH_SHEEP){
			
			boolean[] sheepsForThisDog=mapSheep2(dogs,sheeps);
			
			//pursue for no of ticks and then continue
			//Find sheep with minimum distGate-distToSheep
			if(stepsPursued==0)
			{
				selectSheep(sheeps , dogs[this.id-1], sheepsForThisDog); //sets sheepFocus
				
				while(sheepFocus==-1)
				{
					int otherDogId=r.nextInt(dogs.length);
					
					sheepsForThisDog=mapSheep2(dogs,sheeps,otherDogId);
					selectSheep(sheeps , dogs[otherDogId], sheepsForThisDog);
				}
			}
			
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

		moveTowardsDestination(true, currPos);
	}
	
	public double getThetaFromGate(Point p)
	{
		return Math.atan((p.y-gatePoint.y)/(p.x-gatePoint.x));
	}

	public void selectSheep(Point[] sheeps, Point dog,boolean[] sheepsInSector){
		
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
		if(sheepFocus==-1)
		{
			println("--------------------------------Look into this");
			//sheepFocus=0;
		}
		
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
		//System.out.println(s);
	}
	
	//Ashlesha
	public int minAngleSheep(Point[] sheeps,boolean[] sheepsInSector)
	{
		if(sheepFocus!=-1 && distance(sheeps[sheepFocus], gatePoint) > initialRadius && sheepsInSector[sheepFocus])
		{
			return sheepFocus;
		}
		
		int sheepChosen=-1;
		double minAngle = Double.MAX_VALUE;
		double angle;

		/* while(sheeps[sheepChosen].x<50 && distance(sheeps[sheepChosen], gatePoint) < radius)
		{
			sheepChosen=r.nextInt(sheeps.length);
			System.out.println("Sheep Chosen: " + sheepChosen);
		} */

		for(int i=0; i<sheeps.length; i++){
			if(sheeps[i].x > 50 && distance(sheeps[i], gatePoint) > initialRadius && sheepsInSector[i]){
				double xmod=sheeps[i].x - gatePoint.x;
				double ymod=sheeps[i].y - gatePoint.y;
				
				if(xmod!=0)
					angle=Math.atan(ymod/xmod);
				else
				{
					if(sheeps[i].y>50)
						angle=Math.toRadians(90);
					else
						angle=Math.toRadians(-90);
				}
				
				if (angle < minAngle){
					//xSystem.out.println("Angle: " + angle);
					sheepChosen = i;
					minAngle = angle;
				}
			}
		}
		
		if(sheepChosen!=-1)
		{
			println(String.format("Dog %d chose sheep %f,%f at %f",this.id-1,sheeps[sheepChosen].x,sheeps[sheepChosen].y,Math.toDegrees(minAngle)));
			println(String.format("-- this was amongst sheeps:%s",sheepArrayToString(sheeps)));
		}
		else
			println("sheepChosen is -1");
		
		//xSystem.out.println("Minimum Angle: " + minAngle);
		sheepFocus=sheepChosen;
		return sheepChosen;
	}

	//Pranita
	
	public boolean[] mapSheep2(Point[] dogs, Point[] sheep) {
		
		return mapSheep2(dogs,sheep,this.id-1);
	}
	
	public boolean[] mapSheep2(Point[] dogs, Point[] sheep, int dogSectorId) {
		
		//List<Point> sheepsInThisSector=new ArrayList<Point>();
		//Point[] sheepsToReturn;
		
		boolean[] sheepsInSector=new boolean[sheep.length];
		double angle=180/(1.0*dogs.length);
		
		double currentSectorStart=(dogSectorId)*angle-90;
		double currentSectorEnd=currentSectorStart+angle;
		
		int i=0;
		for(Point p:sheep)
		{
			double angleP=Math.toDegrees(Math.atan((p.y-gatePoint.y)/(p.x-gatePoint.x)));
			//println(String.format("Angle for sheep %f,%f is %f",p.x,p.y,angleP));
			//println(String.format("Range for this dog is [%f,%f) %n",currentSectorStart,currentSectorEnd));
			if(angleP>=currentSectorStart && angleP<currentSectorEnd)
				sheepsInSector[i]=true;
			i++;
		}
		
		return sheepsInSector;
		/*
		sheepsToReturn=new Point[sheepsInThisSector.size()];
		int i=0;
		for(Point p:sheepsInThisSector)
			sheepsToReturn[i++]=p;
		
		printMappingOutput(sheepsToReturn);
		
		return sheepsToReturn;*/
		
	}
	
	public Point[] mapSheep(Point[] dogs, Point[] sheep) {
		
		if(dogs.length==1)
			return sheep;

	    //boolean b = false;
	    //if(!b)
	    //x
		/*System.out.println("Sheep positions:");
	    for (int i = 0; i < sheep.length; i++) {
	        System.out.println(sheep[i].x + "," + sheep[i].y);
	    }*/

	    //int num_dogs = 5;
		
	    int num_dogs = dogs.length;
	    boolean even = false;
	    Point[] position_points = new Point[num_dogs - 1];

	    for (int i = 0; i < num_dogs - 1; i++)
	        position_points[i] = new Point(0, 0);

	    double circumference = 20 * Math.PI;

	    double sector_part = circumference / num_dogs;
	    double angle = (sector_part * 180) / circumference;
	    //xSystem.out.println("Angle :" + angle);
	    if ((num_dogs % 2) == 0)
	        even = true;
	    if (even) {
	        int a = (int)(num_dogs / 2);
	        position_points[a - 1].x = 100;
	        position_points[a - 1].y = 50;

	    }

	    if (even) {
	        int j = (int)(num_dogs / 2) - 1;
	        //System.out.println("initial k value:"+j);
	        int k = j;
	        int p = 1;
	        int count = 0;
	        double theta = angle;
	        while (count != (num_dogs - 1) / 2) //while(theta<=90)
	        {

	            if (theta <= 45) {
	                double length = 50 / Math.cos(Math.toRadians(theta));
	                //xSystem.out.println("Length1:" + length);
	                position_points[k - p].x = 100;
	                position_points[k - p].y = 50 - length * Math.sin(Math.toRadians(theta));
	                position_points[k + p].x = 100;
	                position_points[k + p].y = 50 + length * Math.sin(Math.toRadians(theta));
	                theta += angle;
	                //System.out.println(position_points[1].x + "      " + position_points[1].y);
	                p++;
	            } else {
	                //System.out.println(theta);
	                //System.out.println(Math.sin(30));
	                //xSystem.out.println("k = " + k + "p = " + p);
	                double length = 50 / Math.sin(Math.toRadians(theta));
	                //xSystem.out.println("Length2:" + length);
	                position_points[k - p].x = 50 + length * Math.cos(Math.toRadians(theta));
	                position_points[k - p].y = 0; //50 + length*Math.sin(theta);
	                position_points[k + p].x = 50 + length * Math.cos(Math.toRadians(theta));
	                position_points[k + p].y = 100; //50 + length*Math.sin(-theta);
	                theta += angle;
	                p++;
	            }

	            count += 1;
	        }

	    } else {
	        int j = (int)(num_dogs / 2);
	        //System.out.println("initial k value:"+j);
	        int k = j;
	        int p = 1;
	        int count = 0;
	        double theta = angle;
	        while (count != (num_dogs - 1) / 2) //while(theta<=90)
	        {

	            if (theta <= 45) {
	                double length = 50 / Math.cos(Math.toRadians(theta));
	                //xSystem.out.println("Length1:" + length);
	                position_points[k - p].x = 100;
	                position_points[k - p].y = 50 - length * Math.sin(Math.toRadians(theta));
	                position_points[k].x = 100;
	                position_points[k].y = 50 + length * Math.sin(Math.toRadians(theta));
	                theta += angle;
	                //xSystem.out.println(position_points[1].x + "      " + position_points[1].y);
	                k++;
	                p += 2;
	            } else {
	                //System.out.println(theta);
	                //System.out.println(Math.sin(30));
	                //xSystem.out.println("k = " + k + "p = " + p);
	                double length = 50 / Math.sin(Math.toRadians(theta));
	                //xSystem.out.println("Length2:" + length);
	                position_points[k - p].x = 50 + length * Math.cos(Math.toRadians(theta));
	                position_points[k - p].y = 0; //50 + length*Math.sin(theta);
	                position_points[k].x = 50 + length * Math.cos(Math.toRadians(theta));
	                position_points[k].y = 100; //50 + length*Math.sin(-theta);
	                theta += angle;
	                k++;
	                p += 2;
	            }

	            count += 1;
	        }

	    }

//x
	    /*for (int i = 0; i < position_points.length; i++)
	        System.out.println("X coord:" + position_points[i].x + "Y coord:" + position_points[i].y);
*/

	    ArrayList < Point > Sheeplist = new ArrayList < Point > ();
	    Point gate = new Point(50, 50);

	    if (id == 1) {
	        Point p = new Point(50, 0);
	        for (int i = 0; i < sheep.length; i++)
	            if (!isRight(gate, position_points[0], sheep[i])) //isRight(gate,p,sheep[i]) && 
	                Sheeplist.add(sheep[i]);

	    } else if (id == dogs.length) {
	        Point p = new Point(50, 100);
	        //Point a = new Point(50,100);
	        //Point b = new Point(100,100);
	        for (int i = 0; i < sheep.length; i++)
	            if (!isRight(gate, position_points[position_points.length - 1], sheep[i])) //isRight(gate,p,sheep[i]) && 
	                Sheeplist.add(sheep[i]);
	    } else {
	        boolean first_half_line1 = false, first_half_line2 = false;
	        int line1 = id - 2;
	        int line2 = id - 1;
	        if (position_points[line1].y <= 50)
	            first_half_line1 = true;
	        if (position_points[line2].y <= 50)
	            first_half_line2 = true;

	        for (int i = 0; i < sheep.length; i++) {
	            if (first_half_line1 && first_half_line2)
	                if (isRight(gate, position_points[id - 2], sheep[i]) && !isRight(gate, position_points[id - 1], sheep[i]))
	                    Sheeplist.add(sheep[i]);
	            if (!first_half_line1 && !first_half_line2)
	                if (!isRight(gate, position_points[id - 2], sheep[i]) && isRight(gate, position_points[id - 1], sheep[i]))
	                    Sheeplist.add(sheep[i]);
	            if (first_half_line1 && !first_half_line2)
	                if (isRight(gate, position_points[id - 2], sheep[i]) && isRight(gate, position_points[id - 1], sheep[i]))
	                    Sheeplist.add(sheep[i]);
	        }
	    }

//x
	    /*System.out.println("My ID:" + id);
	    for (Point item: Sheeplist) {
	        System.out.println(item.x + "  ,  " + item.y);
	    }
	    System.out.println("done");*/

	    
	    Point[] sheepsToReturn=new Point[Sheeplist.size()];
	    int i=0;
	    for(Point oneSheep:Sheeplist)
	    	sheepsToReturn[i++]=oneSheep;
	    
	    printMappingOutput(sheepsToReturn);
	    
	    return sheepsToReturn;

	}
	
	public void printMappingOutput(Point[] sheeps)
	{
		println(String.format("For dog %d the sheeps are:%s",this.id-1,sheepArrayToString(sheeps)));
	}
	
	public String sheepArrayToString(Point[] sheeps)
	{
		String ret="";
		for(Point p: sheeps)
			ret+=(String.format("[ %f, %f],",p.x,p.y));
		
		return ret;
	}

	public boolean isRight(Point a, Point b, Point c) {
	    double slope = (b.y - a.y) / (b.x - a.x);
	    double c1 = a.y - slope * a.x;
	    double xcoord = (c.y - c1) / slope;
	    if (c.x > xcoord)
	        return true;

	    return false;
	}

}

