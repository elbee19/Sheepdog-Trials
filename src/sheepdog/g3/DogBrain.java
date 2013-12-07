package sheepdog.g3;

import java.util.Arrays;

import sheepdog.g3.Calculator.SIDE;
import sheepdog.sim.Point;

public abstract class DogBrain {
  protected int mId;
  protected boolean mAdvanced;
  protected int mNblacks;
  protected double DOG_SHEEP_MIN_DIST = 1; 
  
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
  
  public void makePointValid(Point dest, Point src) {
      if (dest.x > 100) { dest.x = 100; }
      else if (dest.x < 0) { dest.x = 0; }
      if (dest.y > 100) { dest.y = 100; }
      else if (dest.y < 0) { dest.y = 0; }
      
      int srcFenceSide = (int) Math.signum(src.x - Calculator.FIELD_SIZE * .5);
      if (hitTheFence(src.x, src.y, dest.x, dest.y)) {
          dest.x = Calculator.FIELD_SIZE * .5 + srcFenceSide * .00000001; 
      }
  }
  
  private boolean hitTheFence(double x1, double y1, double x2, double y2) {
      // on the same side
      if (Calculator.getSide(x1) == Calculator.getSide(x2))
          return false;

      // one point is on the fence
      if (Calculator.getSide(x1) == SIDE.MIDDLE || Calculator.getSide(x2) == SIDE.MIDDLE)
          return false;

      // compute the intersection with (50, y3)
      // (y3-y1)/(50-x1) = (y2-y1)/(x2-x1)

      double y3 = (y2-y1)/(x2-x1)*(50-x1)+y1;

      assert y3 >= 0 && y3 <= Calculator.FIELD_SIZE;

      // pass the openning?
      if (y3 >= Calculator.FIELD_SIZE * .5 - 1 && y3 <= Calculator.FIELD_SIZE * .5 + 1)
          return false;
      else
          return true;
  }
  
  protected Point forceSheepToMove(Point sheep, Point me, Point[] dogs, Point[] sheeps) {
      sheep = Calculator.moveSheep(sheep, dogs, sheeps);
      double angleGapToSheep = Calculator.getAngleOfTrajectory(GAP, sheep);
      Point idealLocation = Calculator.getMoveInDirection(sheep, angleGapToSheep, DOG_SHEEP_MIN_DIST);
      Point moveLocation = Calculator.getMoveTowardPoint(me, idealLocation);
      makePointValid(moveLocation, me);
      return moveLocation;   
  }
  
  private Point anticipateSheepMovement(Point me, Point targetSheep) {
      double angleDogToSheep = Calculator.getAngleOfTrajectory(me, targetSheep);
//      if (Calculator.withinRunDistance(targetSheep, me)) {
//          targetSheep = Calculator.getMoveInDirection(targetSheep, angleDogToSheep, SHEEP_RUN_SPEED);
//      }
//      else if (Calculator.withinWalkDistance(targetSheep, me)) {
//          targetSheep = Calculator.getMoveInDirection(targetSheep, angleDogToSheep, SHEEP_WALK_SPEED);
//      }
      return targetSheep;
  }
}
