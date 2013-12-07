package sheepdog.g9;

import java.util.*;

public class Tree extends Strategy {
    public String name = "Tree";

    public Tree ( int id, LinkedList<Strategy> strategyStack ) {
        super(id, strategyStack);
    }

    public static double estimate( Point[] dogs, Point[] sheeps ) {
        return 0;
    }

    public Point move(Point[] dogs, Point[] allsheeps) {

        Point me = dogs[id-1];

        if (me.x < PlayerUtils.GATE.x) {
            return PlayerUtils.moveDogTo(me, PlayerUtils.GATE);
        }


        int num_dogs = dogs.length;

        ArrayList<Point> buf = new ArrayList<Point>();
        if (Global.mode) {
            for (int i=0; i<Global.nblacks; ++i) {
                if (allsheeps[i].x >= PlayerUtils.GATE.x) {
                    Point p = new Point( allsheeps[i] );
                    p.sid = i;
                    buf.add(p);
                }
            }
        } else {
            for (int i=0; i<allsheeps.length; ++i) {
                if (allsheeps[i].x >= PlayerUtils.GATE.x) {
                    Point p = new Point( allsheeps[i] );
                    p.sid = i;
                    buf.add(p);
                }
            }
        }

        Point[] sheeps = buf.toArray(new Point[buf.size()]);
        Arrays.sort(sheeps);


        //print_array(sheeps);

        /*
        System.out.println("--------------------------------");
        System.out.println("for dog id : " + id);
        for (int i=0; i<sheeps.length; ++i) {
            System.out.println(sheeps[i].sid + ", " + sheeps[i].x + ", " + sheeps[i].y
                    + ", " );
        }
        System.out.println("--------------------------------");
        */

        sheeps = get_sheeps_inside_sector(num_dogs, sheeps);
        Arrays.sort(sheeps, Point.PointDistanceComparator);

        //print_array(sheeps);

        PointNode[] tree = PointNode.build(sheeps);

        /*
        System.out.println("tree : ");
        for (int i=0; i<tree.length; ++i) {
            System.out.println("index: "+i+", sid: "+tree[i].sid+", parent:"+tree[i].parent
                    + ", treeDistance: "+ tree[i].treeDistance);
        }

        System.out.println("tree finish");
        */

        int index_targetSheep = PointNode.get_farthest_sheep(tree);
        int _parent = tree[index_targetSheep].parent;

        if (-1 == _parent || (sheeps.length <= 1)) { // root
            double x = PlayerUtils.GATE.x;
            double y = PlayerUtils.GATEOPENLEFT +
                (PlayerUtils.GATEOPENRIGHT - PlayerUtils.GATEOPENLEFT) *
                ((double)id / (dogs.length + 1));
            Fetch fetch = new Fetch(id, strategyStack, -1, new Point(x, y));
            strategyStack.push(fetch);
            return fetch.move(dogs, allsheeps);
        }

        int sid_targetSheep = tree[index_targetSheep].sid;

        int fetchTurns = (int)java.lang.Math.round(me.distance(PlayerUtils.GATE)/10);

        Fetch fetch = new Fetch(id, strategyStack, sid_targetSheep, tree[_parent], fetchTurns);
        strategyStack.push(fetch);
        return fetch.move(dogs, allsheeps);
    }

    private void print_array( Point[] arr ) {
        for (int i=0; i<arr.length; ++i) {
            System.out.println("(" + arr[i].sid + ", " +arr[i].x+", "+arr[i].y+")  ");
        }
        System.out.println();
    }

    private Point[] get_sheeps_inside_sector(int num_dogs, Point[] sheeps) {
        double partition = sheeps.length / num_dogs;
        int lowerBound = (int)java.lang.Math.round(partition * (id-1));
        int upperBound = (int)java.lang.Math.round(partition * id);
        if (num_dogs == id)
            upperBound = sheeps.length;
        return Arrays.copyOfRange(sheeps, lowerBound, upperBound);
    }

    private boolean done(Point[] sheeps) {
        int maxIter = Global.mode ? Global.nblacks : sheeps.length;
        for (int i = 0; i < maxIter; i++) {
            if (sheeps[i].x > PlayerUtils.GATE.x)
                return false;
        }
        return true;
    }

    public String toString() {
        return "";
        //return String.format("%s\t%s\t dog  %d move to (%s)", name, stage.toString(), id, ret.toString());
    }
}

