package sheepdog.g3;

import java.util.ArrayList;

import sheepdog.sim.Point;

public class Calculator {
  public static final double RUN_DISTANCE = 2.0;
  public static final double WALK_DISTANCE = 10.0;
  public static final double FIELD_SIZE = 100.0;
  public static final double DOG_MAX_SPEED = 1.95;
  public static final double OPEN_LEFT = 49.0;
  public static final double OPEN_RIGHT = 51.0;
  public static enum SIDE { WHITE_GOAL_SIDE, BLACK_GOAL_SIDE, MIDDLE };
  
  public static double getAngleOfTrajectory(Point cur, Point dest) {
      return Math.atan2(dest.y - cur.y,dest.x - cur.x);
  }
  
  public static Point getMoveTowardPoint(Point pos, Point dest) {
      double angle = getAngleOfTrajectory(pos, dest);
      double dist;
      if (dist(pos, dest) < DOG_MAX_SPEED) {
          dist = dist(pos, dest);
      }
      else {
          dist = DOG_MAX_SPEED;
      }
      return getMoveInDirection(pos, angle, dist);
  }
  
  public static Point getMoveInDirection(Point pos, double angle, double distance) {
      double x = pos.x + Math.cos(angle) * distance;
      double y = pos.y + Math.sin(angle) * distance;
      return new Point(x, y);
  }
  
  public static double dist(Point p1, Point p2) {
    return Math.sqrt((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y));
  }
  
  public static boolean withinRunDistance(Point sheep, Point position) {
    return dist(sheep, position) < RUN_DISTANCE;
  }
  
  public static boolean withinWalkDistance(Point sheep, Point position) {
    return dist(sheep, position) < WALK_DISTANCE;
  }
  
  public static boolean isMoveValid(Point src, Point dst) {
      if (dst.x < 0 || dst.x > FIELD_SIZE) {
          return false;
      }
      if (dst.y < 0 || dst.y > FIELD_SIZE) {
          return false;
      }
      if (dist(src, dst) > DOG_MAX_SPEED) {
          return false;
      }
      if (hitTheFence(src, dst)) {
          return false;
      }
      return true;
  }
  
  public static SIDE getSide(double x) {
    if (x < FIELD_SIZE * 0.5)
      return SIDE.WHITE_GOAL_SIDE;
    else if (x > FIELD_SIZE * 0.5)
      return SIDE.BLACK_GOAL_SIDE;
    else
      return SIDE.MIDDLE;
}

  public static boolean hitTheFence(Point src, Point dst) {
    // on the same side
    if (getSide(src.x) == getSide(dst.x)) { return false; }

    // one point is on the fence
    if (getSide(src.x) == SIDE.MIDDLE || getSide(dst.x) == SIDE.MIDDLE) { return false; }
    
    // Find the y coordinate of the the point (50, y) on the line connecting
    // the two points.
    double y3 = (dst.y-src.y)/(dst.x-src.x)*(50-src.x)+src.y;

    // between the opening?
    if (y3 >= OPEN_LEFT && y3 <= OPEN_RIGHT) {
        return false;
    }
    else {
        return true;
    }
  }
  
  public static ArrayList<Integer> undeliveredWhiteSheep(Point[] sheepToDeliver) {
    ArrayList<Integer> undelivered = new ArrayList<Integer>(sheepToDeliver.length);
    for (int i = 0; i < sheepToDeliver.length; i++) {
      if (sheepToDeliver[i].x >= FIELD_SIZE * 0.5) {
        undelivered.add(i);
      }
    }
    return undelivered;
  }
  
  public static ArrayList<Integer> undeliveredBlackSheep(Point[] sheepToDeliver) {
    ArrayList<Integer> undelivered = new ArrayList<Integer>(sheepToDeliver.length);
    for (int i = 0; i < sheepToDeliver.length; i++) {
      if (sheepToDeliver[i].x >= FIELD_SIZE * 0.5) {
        undelivered.add(i);
      }
    }
    return undelivered;
  }
}
