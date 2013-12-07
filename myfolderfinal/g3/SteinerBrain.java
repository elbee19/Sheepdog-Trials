package sheepdog.g3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import sheepdog.g3.Calculator.SIDE;
import sheepdog.sim.Point;

public class SteinerBrain extends sheepdog.g3.DogBrain {
	
	ArrayList<Point> steinerPoints = new ArrayList<Point>();
	private double DOG_SHEEP_MIN_DIST = 1; 
    private static final double SHEEP_RUN_SPEED = 1.000;
    private static final double SHEEP_WALK_SPEED = 0.100;
    protected static final Point GAP = new Point(50, 50);
    private double STEINER_RADIUS = 4;
    
    ArrayList<ArrayList<Point>> steinerSheep = new ArrayList<ArrayList<Point>>();
    ArrayList<Integer> undeliveredIndices = new ArrayList<Integer>();
    
    static int removal = 8;
	
	//Constructor
	public SteinerBrain(int id, boolean advanced, int nblacks) {
        super(id,advanced,nblacks);
//        System.out.println("Constructor called");
        
        //level 0
        steinerPoints.add(new Point(50,50));
        
        //level 1
        steinerPoints.add(new Point(62,25));
        steinerPoints.add(new Point(62,75));
        
        //level 2
        steinerPoints.add(new Point(75,12));
        steinerPoints.add(new Point(75,37));
        steinerPoints.add(new Point(75,62));
        steinerPoints.add(new Point(75,87));
        
        //level 3
        steinerPoints.add(new Point(87,11));
        steinerPoints.add(new Point(87,22));
        steinerPoints.add(new Point(87,33));
        steinerPoints.add(new Point(87,44));
        steinerPoints.add(new Point(87,55));
        steinerPoints.add(new Point(87,66));
        steinerPoints.add(new Point(87,77));
        steinerPoints.add(new Point(87,88));
        for(int i=0;i<steinerPoints.size();i++)
        {
        	steinerSheep.add(new ArrayList<Point>());
//        	System.out.println("Inside for..");
        }
//        System.out.println(steinerSheep);
    }
	@Override
	public Point getBasicMove(Point[] dogs, Point[] sheeps) {
		
		Point me = dogs[mId];
        
		
        //If dog on the left side of fence, move dog towards the gap
        if(Calculator.getSide(dogs[mId].x) == SIDE.BLACK_GOAL_SIDE)
        {
            return Calculator.getMoveTowardPoint(me, GAP);
        }
        steinerSheep = new ArrayList<ArrayList<Point>>();
//		Point moveLocation = null;
		for(int i=0;i<steinerPoints.size();i++)
        {
        	steinerSheep.add(new ArrayList<Point>());
//        	System.out.println("Inside for..");
        }
		undeliveredIndices = Calculator.undeliveredWhiteSheep(sheeps);
		assignSheeptoPoints(sheeps);
		
		System.out.println(steinerSheep);
		for(int i=steinerPoints.size()-1;i>=0;i--)
//		for(int i=0;i<steinerPoints.size();i++)
		{
			for(int j=0;j<steinerSheep.get(i).size();j++)
			{
				Point current = steinerSheep.get(i).get(j);
				if(Calculator.dist(steinerPoints.get(i), current) > STEINER_RADIUS)
					return pushSheepToPoint(me, current, sheeps, steinerPoints.get(i));
//				else
//					continue;	
			}
		}
		
		System.out.println("Size Before: "+steinerPoints.size());
		int size = steinerPoints.size();
		for(int i=size-1;i>=size-removal;i--)
		{
//			System.out.println("Value of i: "+i);
			steinerPoints.remove(i);
		}
		
		removal/=2;
		System.out.println("Size After: "+steinerPoints.size());
		return new Point(50,50);
	}
	
	
	public void assignSheeptoPoints(Point[] sheep)
	{
//		int count = 0;
		for(int l=0;l<sheep.length;l++)
//		for(Point i: sheep)
		{
			Point i= sheep[l];
			if(!undeliveredIndices.contains(l))
			{
//				System.out.println("************************************");
//				System.out.println("Delivered!!!");
//				System.out.println("************************************");
				continue;
			}
			
			double minDist = Double.MAX_VALUE;
			int minIndex = -1;
			Point minPoint = null;
			for(int k=steinerPoints.size()-1;k>=0;k--)
//			for(Point j: steinerPoints)
			{
				Point j = steinerPoints.get(k);
//				System.out.println(j);
				if(j.x > i.x)
					continue;
				double tempDist = 0;
//				if(j.y <50)
//					tempDist = Calculator.dist(i, new Point(j.x-STEINER_RADIUS,j.y+STEINER_RADIUS));
//				else
//					tempDist = Calculator.dist(i, new Point(j.x-STEINER_RADIUS,j.y-STEINER_RADIUS));
				tempDist = Calculator.dist(i, j);
				if(tempDist<minDist)
				{
//					System.out.println(steinerSheep.get(innerCount));
					minDist = tempDist;
					minPoint = i;
					minIndex = k;
				}
			}
			steinerSheep.get(minIndex).add(minPoint);
//			System.out.println("Min point: "+minPoint);
//			System.out.println("Min Index: "+minIndex);
//			count++;
		}
//		System.out.println(steinerSheep);
	}
	@Override
    public Point getAdvancedMove(Point[] dogs, Point[] whiteSheep,
            Point[] blackSheep) {
        // Do nothing, this brain is only for a basic player
        return null;
    }
	
	public Point pushSheepToPoint(Point me, Point targetSheep, Point[] sheeps, Point SteinerPoint) {	
//		 Point targetSheep = sheeps[sheepID];
//		System.out.println("Push sheep to point");
        targetSheep = anticipateSheepMovement(me, targetSheep); 
        
        double anglePointToSheep = Calculator.getAngleOfTrajectory(SteinerPoint, targetSheep);
        Point idealLocation = Calculator.getMoveInDirection(targetSheep, anglePointToSheep, DOG_SHEEP_MIN_DIST);
        Point moveLocation = Calculator.getMoveTowardPoint(me, idealLocation);
        makePointValid(moveLocation);
        return moveLocation;	
	}
	
	private Point anticipateSheepMovement(Point me, Point targetSheep) {
        double angleDogToSheep = Calculator.getAngleOfTrajectory(me, targetSheep);
        if (Calculator.withinRunDistance(targetSheep, me)) {
            targetSheep = Calculator.getMoveInDirection(targetSheep, angleDogToSheep, SHEEP_RUN_SPEED);
        }
        else if (Calculator.withinWalkDistance(targetSheep, me)) {
            targetSheep = Calculator.getMoveInDirection(targetSheep, angleDogToSheep, SHEEP_WALK_SPEED);
        }
        return targetSheep;
    }
}