package sheepdog.g9;

public class PointNode extends Point {
    // assume that the tree is represented as an array.
    // parent is represented as the index of that array. -1 means root
    public int parent;
    // distance from this node to the root
    public double treeDistance;

    public PointNode(Point p) {
        x = p.x;
        y = p.y;
        sid = p.sid;
        parent = -1;
    }

    public static PointNode[] build(Point[] sheeps) {
        PointNode[] nodes = new PointNode[sheeps.length+1];
        // root
        PointNode pn = new PointNode(PlayerUtils.GATE);
        pn.treeDistance = pn.distance(PlayerUtils.GATE);
        nodes[0] = pn;
        for (int i = 0; i < sheeps.length; i++) {
            pn = new PointNode(sheeps[i]);
            double minDistance = pn.distance(PlayerUtils.GATE);
            for (int j = 0; j <= i; j++) {
                if (minDistance >= pn.distance(nodes[j])) {
                    pn.parent = j;
                    pn.treeDistance = pn.distance(nodes[j]) + nodes[j].treeDistance;
                    minDistance = pn.distance(nodes[j]);
                }
            }
            nodes[i+1] = pn;
        }
        return nodes;
    }

    /*
    public static PointNode[] build(Point[] sheeps) {
        PointNode[] nodes = new PointNode[sheeps.length+1];
    }

    */

    public static int get_farthest_sheep(PointNode[] tree) {
        if (null == tree || 0 == tree.length) return -1;
        double max_distance = tree[0].treeDistance;
        int max_index = 0;
        for (int i=1; i<tree.length; ++i) {
            if ((max_distance < tree[i].treeDistance) &&
                (tree[i].treeDistance > (tree[tree[i].parent].treeDistance + 0.5))) {
                max_index = i;
                max_distance = tree[i].treeDistance;
            }
        }
        return max_index;
    }
}
