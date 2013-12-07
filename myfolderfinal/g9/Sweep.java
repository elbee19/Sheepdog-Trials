package sheepdog.g9;

import java.util.*;

public class Sweep extends Strategy {
    public enum SweepStage { MOVE_TO_GATE, LINEUP, PUSH, SQUEEZE, BACK_TO_GATE }
    public enum DogRole { UP, DOWN, RIGHT }
    public enum DogDir { LEFT, RIGHT }
    private final double DOG_PUSH_SPEED = 10;
    private final double DOG_SQUEEZE_SPPED = 10;
    private final double PUSH_GAP = 0.5;
    private final double SQUEEZE_GAP = 1;
    private final double BACK_TO_GATE_GAP = 7;
    private final double OVERLAP = 0.5;

    public String name = "Sweep";

    private DogRole role;
    private DogDir dir;
    SweepStage stage;
    private Point ret;

    public Sweep (int id, LinkedList<Strategy> strategyStack) {
        super(id, strategyStack);
        stage = SweepStage.MOVE_TO_GATE;
        dir = DogDir.LEFT;
    }

    public static double estimate(Point[] dogs, Point[] sheeps) {
        return 1 / (0.0000577936 * dogs.length + 0.0011124);
    }

    // Deterministic Finite Automata for current dog
    public Point move(Point[] dogs, Point[] allsheeps) {
        Point[] sheeps;

        if (Global.mode) {
            sheeps = new Point[Global.nblacks];
            for (int i=0; i<Global.nblacks; ++i) {
                sheeps[i] = new Point();
                sheeps[i].x = allsheeps[i].x;
                sheeps[i].y = allsheeps[i].y;
            }
        } else {
            sheeps = allsheeps;
        }

        Point me = dogs[id-1];

        switch (stage) {
            case MOVE_TO_GATE:

                if (me.x >= PlayerUtils.GATE.x) {
                    stage = SweepStage.LINEUP;
                    move(dogs, sheeps);
                }

                ret = PlayerUtils.moveDogTo(me, PlayerUtils.GATE);

                return ret;

            case LINEUP:
                boolean all_lineup = true;

                if (all_lineup(dogs)) {
                    stage = SweepStage.PUSH;
                    move(dogs, sheeps);
                }

                Point targetPos = dog_lineup_pos(id, dogs);
                if (targetPos.equals(me)) {
                    ret = me;
                    return ret;
                }

                ret = PlayerUtils.moveDogTo( me, targetPos );

                return ret;

            case PUSH:
                if (need_to_squeeze(dogs, sheeps) ) {
                    //System.out.println("start to squeeze\n");
                    stage = SweepStage.SQUEEZE;
                    move(dogs, sheeps);
                }

                double interval = PlayerUtils.HEIGHT / (double)(dogs.length - 2);
                Point rightmost = new Point(PlayerUtils.WIDTH, 0);
                Point sublm = new Point(PlayerUtils.WIDTH, (id-1) * interval);
                double upbound = Math.max(0, (id-1) * interval - OVERLAP * interval);
                double downbound = Math.min(PlayerUtils.HEIGHT, id * interval + OVERLAP * interval);

                for (int i=0; i<sheeps.length; ++i) {
                    Point t = PlayerUtils.PredictNextMove(i, dogs, sheeps);
                    if (t.x > rightmost.x) {
                        rightmost.x = t.x;
                        rightmost.y = t.y;
                    }
                    if ( upbound <= t.y && t.y <= downbound && t.x > sublm.x) {
                        sublm.x = t.x;
                        sublm.y = t.y;
                    }
                }

                targetPos = new Point();
                
                if (id == dogs.length-1) {
                    targetPos.x = Math.min(PlayerUtils.WIDTH*2, rightmost.x + PUSH_GAP);
                    targetPos.y = 0;
                } else if (id == dogs.length) {
                    targetPos.x = Math.min(PlayerUtils.WIDTH*2, rightmost.x + PUSH_GAP);
                    targetPos.y = PlayerUtils.HEIGHT;
                } else {
                    targetPos.x = Math.min(PlayerUtils.WIDTH*2, rightmost.x + PUSH_GAP);
                    targetPos.y = sublm.y;
                }
                ret = PlayerUtils.moveDogTo( me, targetPos );

                return ret;

            case SQUEEZE:
                double down_y = 0;
                double up_y = PlayerUtils.HEIGHT;
                rightmost = new Point(PlayerUtils.WIDTH, 0);

                interval = PlayerUtils.HEIGHT / (double)(dogs.length - 2);
                sublm = new Point(PlayerUtils.WIDTH, (id-1) * interval);
                upbound = Math.max(0, (id-1) * interval - OVERLAP * interval);
                downbound = Math.min(PlayerUtils.HEIGHT, id * interval + OVERLAP * interval);

                for (int i=0; i<sheeps.length; ++i) {
                    if (sheeps[i].x < PlayerUtils.GATE.x) continue;
                    Point t = PlayerUtils.PredictNextMove(i, dogs, sheeps);
                    up_y = Math.min(up_y, t.y);
                    down_y = Math.max(down_y, t.y);

                    if (t.x > rightmost.x) {
                        rightmost.x = t.x;
                        rightmost.y = t.y;
                    }
                    if (upbound <= t.y && t.y <= downbound && t.x > sublm.x) {
                        sublm.x = t.x;
                        sublm.y = t.y;
                    }
                }

                if (id==dogs.length || id==dogs.length-1) {
                    targetPos = defender_move(up_y, down_y, dogs, sheeps);
                    //System.out.println("up_y = " + up_y + ", down_y = " + down_y);
                    //System.out.println("targetPos = " + targetPos.toString());
                } else {
                    ret = me;
                    targetPos = new Point();
                    targetPos.x = rightmost.x + SQUEEZE_GAP * 2;

                    if (downbound < up_y) {
                        targetPos.y = Math.max(me.y, up_y - SQUEEZE_GAP);
                    } else if (upbound > down_y) {
                        targetPos.y = Math.min(me.y, down_y + SQUEEZE_GAP);
                    } else {
                        targetPos.y = sublm.y;
                        if (id == 1) 
                            targetPos.y = Math.max(0, targetPos.y - SQUEEZE_GAP);
                        if (id == dogs.length-2) 
                            targetPos.y = Math.min(PlayerUtils.HEIGHT, targetPos.y + SQUEEZE_GAP);
                    }
                    if (Math.abs(me.y - 50.0) <= 2)
                        targetPos.y -= 0.9 * SQUEEZE_GAP;
                }
                ret = PlayerUtils.moveDogTo( me, targetPos );

                if (done(sheeps))
                    strategyStack.pop();

                return ret;
            /*
            case BACK_TO_GATE:

                return ret;
                */
        }
        return new Point();
    }

    private boolean done(Point[] sheeps) {
        for (int i = 0; i < sheeps.length; i++) {
            if (sheeps[i].x > PlayerUtils.GATE.x)
                return false;
        }
        return true;
    }

   private boolean all_lineup( Point[] dogs ) {
        boolean[] is_lineup = new boolean[dogs.length];
        Arrays.fill(is_lineup, false);

        for (int i=0; i<dogs.length; ++i) {
            Point targetPos = dog_lineup_pos(i+1, dogs);
            int j=0;
            for (j=0; j<dogs.length; ++j) {
                if (!is_lineup[j] && dogs[j].equals(targetPos)) {
                    is_lineup[j] = true;
                    break;
                }
            }
            if (j==dogs.length) return false;
        }

        return true;
    }

    private Point defender_move(double up_y, double down_y, Point[] dogs, Point[] sheeps) {
        Point me = dogs[id-1];
        Point targetPos = new Point();
        double rightbound = dogs[1].x;
        double leftbound = 50.0;

        if (Math.abs(me.y - 50.0) <= 4) {
            targetPos.x = (rightbound-50.0) * 0.8 + 50.0;
        } else {

            if (Math.abs(leftbound - me.x) <= 0.1) {
                dir = DogDir.RIGHT;
            } else if (Math.abs(rightbound - me.x) <= 0.1) {
                dir = DogDir.LEFT;
            }

            if ( dir == DogDir.LEFT ) {
                targetPos.x = leftbound;
            } else if (dir == DogDir.RIGHT ) {
                targetPos.x = rightbound;
            }
        }

        if (id == dogs.length-1) { // up right
            //targetPos.y = Math.max(me.y, up_y - SQUEEZE_GAP);
            targetPos.y = Math.max(0, up_y - SQUEEZE_GAP);
            targetPos.y = Math.min(targetPos.y, PlayerUtils.HEIGHT / 2.0 - 1 );
        } else if (id == dogs.length) { // down right
            //targetPos.y = Math.min(me.y, down_y + SQUEEZE_GAP);
            targetPos.y = Math.min(PlayerUtils.HEIGHT, down_y + SQUEEZE_GAP);
            targetPos.y = Math.max(targetPos.y, PlayerUtils.HEIGHT / 2.0 + 1 );

        } /* else if (id == 1) { // up left
            targetPos.x = (Math.abs(97.0 - me.x) <= 0.1) ? :96.0;

            } else { // down left
            targetPos.x = (Math.abs(97.0 - me.x) <= 0.1) ? :96.0;

            }
          */

        return targetPos;
    }
    
    private Point dog_lineup_pos( int id, Point[] dogs ) {
        if (id == dogs.length || id == dogs.length-1) {
            Point q = new Point(PlayerUtils.WIDTH*2, PlayerUtils.GATE.y);
            return q;
        }
        
        Point p = new Point(PlayerUtils.WIDTH*2, (id-1) * PlayerUtils.HEIGHT / (double)(dogs.length-2));
        return p;
    }

    private boolean need_to_squeeze(Point[] dogs, Point[] sheeps) {
        for (int i=0; i<dogs.length; ++i)
            if (dogs[i].x > 56)
                return false;

        return true;
    }

    public String toString() {
        return String.format("%s\t%s\t dog  %d move to (%s)", name, stage.toString(), id, ret.toString());
    }
}

