package sheepdog.g3;

import java.util.ArrayList;

import sheepdog.g3.Calculator.SIDE;
import sheepdog.sim.Point;

public class SweeperBrain extends DogBrain{
	
	boolean movementToggle = false;
//	int DOG_GAP = 4;
	static boolean dogsAtEnd = false; 
	protected static final Point GAP = new Point(50, 50);
	double DOG_SHEEP_MIN_DIST = 0.2;
	int SWEEP_TILL_X = 52;
	int UPPER_LOWER_X = 51;
	
	public SweeperBrain(int id, boolean advanced, int nblacks) {
        super(id, advanced, nblacks);
    }
	
	public Point getBasicMove(Point[] dogs, Point[] sheeps) {
		
		ArrayList<Integer> undeliveredIndices = Calculator.undeliveredBlackSheep(sheeps);
		Point me = dogs[mId];
//        System.out.println("Dogs at end: "+ dogsAtEnd);
        //If dog on the left side of fence, move dog towards the gap
        if(Calculator.getSide(dogs[mId].x) == SIDE.BLACK_GOAL_SIDE)
        {
            return Calculator.getMoveTowardPoint(me, GAP);
        }
//        System.out.println("**************************************Position of last dog: "+dogs[dogs.length-1]);
        
        Player.sweeperComplete = sweeperComplete(dogs);
        
        if(dogsAtEnd && me.x>SWEEP_TILL_X)
        {
        	double rightMost = calculateRightMostSheep(sheeps);
//        	double currX = dogs[mId].x;
//        	System.out.println("MID is: " + mId);
        	if(!movementToggle)
        	{
        		movementToggle = !movementToggle;
        		return Calculator.getMoveTowardPoint(me, new Point(rightMost+DOG_SHEEP_MIN_DIST, me.y));
        	}
        	movementToggle = !movementToggle;
        	return Calculator.getMoveTowardPoint(me, new Point(me.x-0.5, me.y));
        }
        else if(mId !=0 && mId != dogs.length - 1 && dogsAtEnd)
        {
        	return me;
        }
        else if(mId == 0 && dogsAtEnd && dogs[mId].y < 49)
        {
        	double lowestY = calculateLowestY(sheeps, undeliveredIndices);
        	double newY = 0;
        	if(lowestY - DOG_SHEEP_MIN_DIST > 0)
        		newY = lowestY - DOG_SHEEP_MIN_DIST;
       		return Calculator.getMoveTowardPoint(me, new Point(UPPER_LOWER_X, newY));
        	
        }
        else if(mId==dogs.length-1 && dogsAtEnd && dogs[mId].y > 51)
        {
        	double highestY = calculateHighestY(sheeps, undeliveredIndices);
        	double newY = 100;
        	if(highestY + DOG_SHEEP_MIN_DIST < 100)
        		newY = highestY + DOG_SHEEP_MIN_DIST;
       		return Calculator.getMoveTowardPoint(me, new Point(UPPER_LOWER_X, newY));
        }
        
        if(dogs[dogs.length-1].x==100 && dogs[dogs.length-1].y==100 && !dogsAtEnd)
        {
//        	System.out.println("Last dog reached");
        	dogsAtEnd = true;
        }
        return Calculator.getMoveTowardPoint(me, new Point(100, (double)mId * 100 / (dogs.length-1)));
        
	}

	@Override
	public Point getAdvancedMove(Point[] dogs, Point[] whiteSheep,
			Point[] blackSheep) {
		// TODO Auto-generated method stub
		return null;
	}
	
	double calculateRightMostSheep(Point[] sheep)
	{
		double rightMost = 0;
		for(Point i: sheep)
		{
			if(i.x > rightMost)
				rightMost = i.x;
		}
		return rightMost;
	}
	
	double calculateLowestY(Point[] sheep, ArrayList<Integer> undeliveredIndices)
	{
		double lowestY = 100;
		int count = 0;
		for(Point i: sheep)
		{
			if(i.y < lowestY && i.x<SWEEP_TILL_X && undeliveredIndices.contains(count))
				lowestY = i.y;
			count++;
		}
		return lowestY;
	}
	
	double calculateHighestY(Point[] sheep, ArrayList<Integer> undeliveredIndices)
	{
		double highestY = 0;
		int count = 0;
		for(Point i: sheep)
		{
			if(i.y > highestY && i.x<SWEEP_TILL_X && undeliveredIndices.contains(count))
				highestY = i.y;
			count++;
		}
		return highestY;
	}
	
	boolean sweeperComplete(Point [] dogs) {
		if (dogs[0].y >= 47.5 && dogs[dogs.length - 1].y <= 52.5 && dogsAtEnd)
			return true;
		return false;
	}
}
