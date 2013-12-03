package sheepdog.g3;

import java.util.Arrays;

import sheepdog.sim.Point;

public abstract class DogBrain {
  protected int mId;
  protected boolean mAdvanced;
  protected int mNblacks;
  
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
  
}
