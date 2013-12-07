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
  private static final double SHEEP_RUN_SPEED = 1.000;
  private static final double SHEEP_WALK_SPEED = 0.100;
  private static final double SHEEP_CRAWL_SPEED = 0.05; // 0.5m/s
  static double CLUSTER_DIST = 1.0;
  public static enum SIDE { BLACK_GOAL_SIDE, WHITE_GOAL_SIDE, MIDDLE };
  
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
      return SIDE.BLACK_GOAL_SIDE;
    else if (x > FIELD_SIZE * 0.5)
      return SIDE.WHITE_GOAL_SIDE;
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
      if (getSide(sheepToDeliver[i].x) != SIDE.WHITE_GOAL_SIDE) {
        undelivered.add(i);
      }
    }
    return undelivered;
  }
  
  public static ArrayList<Integer> undeliveredBlackSheep(Point[] sheepToDeliver) {
    ArrayList<Integer> undelivered = new ArrayList<Integer>(sheepToDeliver.length);
    for (int i = 0; i < sheepToDeliver.length; i++) {
      if (getSide(sheepToDeliver[i].x) != SIDE.BLACK_GOAL_SIDE) {
        undelivered.add(i);
      }
    }
    return undelivered;
  }
  
  static Point moveSheep(Point thisSheep, Point[] dogs, Point[] sheeps) {
      int nsheeps = sheeps.length;
      double dspeed = 0;
      Point closestDog = getClosestDog(thisSheep, dogs, sheeps);
      double dist = dist(thisSheep, closestDog);
      assert dist > 0;

      if (dist < RUN_DISTANCE)
          dspeed = SHEEP_RUN_SPEED;
      else if (dist < WALK_DISTANCE)
          dspeed = SHEEP_WALK_SPEED;
      
      // offset from dogs
      double ox_dog = (thisSheep.x - closestDog.x) / dist * dspeed;
      double oy_dog = (thisSheep.y - closestDog.y) / dist * dspeed;

      // offset from clustering
      double ox_cluster = 0, oy_cluster = 0;

      // aggregate offsets then normalize
      for (int i = 0; i < nsheeps; ++i) {
          // skip this sheep itself
          if (sheeps[i] == thisSheep) continue;

          double d = dist(thisSheep, sheeps[i]);

          // ignore overlapping sheep
          if (d < CLUSTER_DIST && d > 0) {
              // add an unit vector to x-axis, y-axis
              ox_cluster += ((thisSheep.x - sheeps[i].x) / d);
              oy_cluster += ((thisSheep.y - sheeps[i].y) / d);
          }
      }
      // normalize by length
      double l = vectorLength(ox_cluster, oy_cluster);
      if (l != 0) {
          ox_cluster = ox_cluster / l * SHEEP_CRAWL_SPEED;
          oy_cluster = oy_cluster / l * SHEEP_CRAWL_SPEED;
      }

      double ox = ox_dog + ox_cluster, oy = oy_dog + oy_cluster;
      
      Point npos = updatePosition(thisSheep, ox, oy);

      return npos;

  }
  
  private static double vectorLength(double ox, double oy) {
      return Math.sqrt(ox * ox + oy * oy);
  }
  
  public static Point getClosestDog(Point thisSheep, Point[] dogs, Point[] sheeps) {
      int mindog = -1;
      int ndogs = dogs.length;
      double mindist = Double.MAX_VALUE;
      for (int i = 0; i < ndogs; ++i) {
          double d = dist(thisSheep, dogs[i]);
          if (d < mindist && d != 0) { // ignore overlapping dog
              mindist = d;
              mindog = i;
          }
      }
      return dogs[mindog];
  }

  // update the current point according to the offsets
  public static Point updatePosition(Point now, double ox, double oy) {
      double nx = now.x + ox, ny = now.y + oy;

      // hit the left fence        
      if (nx < 0) {
          //            System.err.println("SHEEP HITS THE LEFT FENCE!!!");

          // move the point to the left fence
          Point temp = new Point(0, now.y);
          // how much we have already moved in x-axis?
          double moved = 0 - now.x;
          // how much we still need to move
          // BUT in opposite direction
          double ox2 = -(ox - moved); 
          return updatePosition(temp, ox2, oy);
      }
      // hit the right fence
      if (nx > FIELD_SIZE) {
          //            System.err.println("SHEEP HITS THE RIGHT FENCE!!!");

          // move the point to the right fence
          Point temp = new Point(FIELD_SIZE, now.y);
          double moved = (FIELD_SIZE - now.x);
          double ox2 = -(ox - moved);
          return updatePosition(temp, ox2, oy);
      }
      // hit the up fence
      if (ny < 0) {
          //            System.err.println("SHEEP HITS THE UP FENCE!!!");

          // move the point to the up fence
          Point temp = new Point(now.x, 0);
          double moved = 0 - now.y;
          double oy2 = -(oy - moved);
          return updatePosition(temp, ox, oy2);
      }
      // hit the bottom fence
      if (ny > FIELD_SIZE) {
          //            System.err.println("SHEEP HITS THE BOTTOM FENCE!!!");

          Point temp = new Point(now.x, FIELD_SIZE);
          double moved = (FIELD_SIZE - now.y);
          double oy2 = -(oy - moved);
          return updatePosition(temp, ox, oy2);
      }

      assert nx >= 0 && nx <= FIELD_SIZE;
      assert ny >= 0 && ny <= FIELD_SIZE;
      
      // hit the middle fence
      if (hitTheFence(now, new Point(nx, ny))) {
          //            System.err.println("SHEEP HITS THE CENTER FENCE!!!");
          //            System.err.println(nx + " " + ny);
          //            System.err.println(ox + " " + oy);

          // move the point to the fence
          Point temp = new Point(50, now.y);
          double moved = (50 - now.x);
          double ox2 = -(ox-moved);
          return updatePosition(temp, ox2, oy);
      }

      // otherwise, we are good
      return new Point(nx, ny);
  }
}
