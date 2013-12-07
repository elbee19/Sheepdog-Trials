package sheepdog.g3;

import java.util.Arrays;

import sheepdog.sim.Point;

public abstract class DogBrain {
  protected int mId;
  protected boolean mAdvanced;
  protected int mNblacks;
  protected double DOG_SHEEP_MIN_DIST = 1; 
  private static final double SHEEP_RUN_SPEED = 1.000;
  private static final double SHEEP_WALK_SPEED = 0.100;
  protected static final Point GAP = new Point(50, 50);
  
  public DogBrain(int id, boolean advanced, int nblacks) {
    mId = id - 1;
    mAdvanced = advanced;
    mNblacks = nblacks;
  }

  public abstract Point getAdvancedMove(Point[] dogs, Point[] whiteSheep, Point[] blackSheep);
  
  public abstract Point getBasicMove(Point[] dogs, Point[] sheep);
  
  public Point getMove(Point[] dogs, Point[] sheeps) {
      if (mAdvanced) {
          Point[] blackSheep = Arrays.copyOfRange(sheeps, 0, mNblacks);
          Point[] whiteSheep = Arrays.copyOfRange(sheeps, mNblacks, sheeps.length);
          return getAdvancedMove(dogs, whiteSheep, blackSheep);
      }
      else {
          return getBasicMove(dogs, sheeps);
      }
  }
  
  public void makePointValid(Point pt) {
      if (pt.x > 100) { pt.x = 100; }
      else if (pt.x < 0) { pt.x = 0; }
      if (pt.y > 100) { pt.y = 100; }
      else if (pt.y < 0) { pt.y = 0; }
  }
  
  protected Point forceSheepToMove(Point sheep, Point me) {
      sheep = anticipateSheepMovement(me, sheep);
      double angleGapToSheep = Calculator.getAngleOfTrajectory(GAP, sheep);
      Point idealLocation = Calculator.getMoveInDirection(sheep, angleGapToSheep, DOG_SHEEP_MIN_DIST);
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
