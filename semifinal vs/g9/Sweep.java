package sheepdog.g9;

import java.util.*;

public class Sweep extends Strategy {
    public enum SweepStage { MOVE_TO_GATE, LINEUP_ON_FENCE, RECIPROCATE, SHRINK }
    public enum DogRole { UP, DOWN, RIGHT }
    public enum DogDir { CLOCKWISE, COUNTER_CLOCKWISE }
    private final double DOG_RECIPROCATE_SPEED = 6;
    private final double CLEAR_GAP = 7;

    public String name = "Sweep";

    private DogDir dogdir;
    private DogRole role;
    SweepStage stage;
    private Point ret;

    private double width;
    private double height;

    public Sweep (int id, int nblacks, boolean mode) {
        super(id, nblacks, mode);
        stage = SweepStage.MOVE_TO_GATE;
        width = PlayerUtils.WIDTH;
        height = PlayerUtils.HEIGHT;
        dogdir = DogDir.CLOCKWISE;
    }

    // Deterministic Finite Automata for current dog
    public Point move(Point[] dogs, Point[] sheeps) {
        Point me = dogs[id-1];

        switch (stage) {
            case MOVE_TO_GATE:

                boolean all_in_gate = true;

                for (Point p : dogs) 
                    if (p.x < PlayerUtils.GATE.x) {
                        all_in_gate = false;
                        break;
                    }

                if (all_in_gate) {
                    stage = SweepStage.LINEUP_ON_FENCE;
                    move(dogs, sheeps);
                }

                ret = PlayerUtils.moveDogToWithSpeed(me, PlayerUtils.GATE, DOG_RECIPROCATE_SPEED );

                return ret;

            case LINEUP_ON_FENCE:
                boolean all_lineup = true;

                if (all_lineup(dogs)) {
                    stage = SweepStage.RECIPROCATE;
                    move(dogs, sheeps);
                }
                Point targetPos = dog_lineup_pos(id, dogs);
                if (targetPos.equals(me)) {
                    ret = me;
                    return ret;
                }

                ret = PlayerUtils.moveDogToWithSpeed( me, targetPos, DOG_RECIPROCATE_SPEED );

                return ret;

            case RECIPROCATE:
                if ( need_2_shrink(dogs, sheeps) ) {
                    System.out.println("SHRINK\n");

                    //TODO update width and height
                    width -= 4;
                    height -= 8;

                    stage = SweepStage.LINEUP_ON_FENCE;
                    move(dogs, sheeps);
                }

                ret = PlayerUtils.moveDogToWithSpeed( me, reciprocate_next_dog_pos(dogs, sheeps), DOG_RECIPROCATE_SPEED );

                return ret;
        }
        return new Point();
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

    // return dog's lineup position
    private Point dog_lineup_pos( int id, Point[] dogs ) {
        Point me = dogs[id-1];
        int up_dogs = dogs.length / 4;
        int down_dogs = up_dogs + dogs.length / 4;

        if (dogs.length == 3) {
            up_dogs = 1;
            down_dogs = 2;
        }

        Point ret = new Point();

        double small_dist = (PlayerUtils.HEIGHT - height) / 2;

        if (id <= up_dogs) { // up dogs
            ret.y = small_dist;
            ret.x = PlayerUtils.WIDTH + (id-1) * (width / up_dogs);
            role = DogRole.UP;

        } else if (id <= down_dogs) { // down dogs
            ret.y = PlayerUtils.HEIGHT - small_dist;
            ret.x = PlayerUtils.WIDTH + (id-1 - up_dogs + 1) * (width / (down_dogs - up_dogs));
            role = DogRole.DOWN;

        } else { // right dogs
            ret.x = PlayerUtils.WIDTH + width;
            ret.y = small_dist + (id-1 - down_dogs) * height / (dogs.length - down_dogs);
            role = DogRole.RIGHT;
        }

        return ret;
    }

    // shrink there's no sheep within the "gap"
    private boolean need_2_shrink( Point[] dogs, Point[] sheeps ) {
        double small_dist = (PlayerUtils.HEIGHT - height) / 2;

        for (int i=0; i<sheeps.length; ++i)
            if (sheeps[i].x > PlayerUtils.WIDTH + width - CLEAR_GAP || 
                    sheeps[i].y < small_dist + CLEAR_GAP ||
                    sheeps[i].y > PlayerUtils.HEIGHT - small_dist - CLEAR_GAP)
                return false;

        return true;
    }

    private Point reciprocate_next_dog_pos( Point[] dogs, Point[] sheeps ) {
        Point a = dog_lineup_pos(id, dogs);
        Point me = dogs[id-1];

        if (DogDir.COUNTER_CLOCKWISE == dogdir) {
            if (a.equals(me)) {
                dogdir = DogDir.CLOCKWISE;
                reciprocate_next_dog_pos(dogs, sheeps);
            } else 
                return a;
        }

        Point b = new Point();

        switch (role) {
            case UP:
                b.x = a.x + width / (dogs.length / 4);
                b.y = a.y;
                break;
            case DOWN:
                b.x = a.x - width / (dogs.length / 4);
                b.y = a.y;
                break;
            case RIGHT:
                b.x = a.x;
                b.y = a.y + height / (dogs.length - dogs.length / 4 - dogs.length / 4);
                break;
            default: break;
        }

        if (b.equals(me)) {
            dogdir = DogDir.COUNTER_CLOCKWISE;
            reciprocate_next_dog_pos(dogs, sheeps);
        }

        return b;
    }

    public String toString() {
        return String.format("%s\t%s\t dog  %d move to (%s)", name, stage.toString(), id, ret.toString());
    }
}
