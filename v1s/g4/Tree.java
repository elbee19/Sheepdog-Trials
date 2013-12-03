package sheepdog.g4;

import sheepdog.sim.Point;

public class Tree {
	Player player;
	public Tree(Player player) {
		this.player=player;
	}
	int[] buildTree(Point[] sheeps){
		int n=sheeps.length;
		Point root=new Point(50,50);
		double[] disToRoot=new double[n];
		for (int i = 0; i < disToRoot.length; i++) {
			disToRoot[i]=player.distance(root, sheeps[i]);
		}
		// ^^^^
		//by now just calculate distance of each sheep from root 
		int[] father=new int[n];
		for (int i = 0; i < sheeps.length; i++) {
			double nearest=disToRoot[i];
			father[i]=-1;
			for (int j = 0; j < n; j++) {
				//For each sheep i, check each sheep j st i!=j
				//and j is not farther from root than i
				if (i==j || ( disToRoot[j]>=disToRoot[i] ))
					continue;
				
				//from the sheeps that qualify these conditions, set the nearest sheep as the father
				if (nearest>player.distance(sheeps[i], sheeps[j])) {
					father[i]=j;
					nearest=player.distance(sheeps[i], sheeps[j]);
				}
			}
		}
		return father;
	}
}
