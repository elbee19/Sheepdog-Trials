package sheepdog.g3;

import java.util.Arrays;
import sheepdog.sim.Point;

public class Player extends sheepdog.sim.Player  {
  private int mNblacks;
  private boolean mMode;
  private DogBrain mBrain;
  
  @Override
  public void init(int nblacks, boolean mode) {
    mNblacks = nblacks;
    mMode = mode;
    mBrain = new StraightLineBrain(id, mode, nblacks);
  }

  @Override
  public Point move(Point[] dogs, Point[] sheeps) {
    return mBrain.getMove(dogs, sheeps);
  }

}
