package sheepdog.g4;

import java.util.Arrays;

import sheepdog.sim.Point;

public class Util {
	static Point gate= new Point(50,50);
	


	static Point[] sortByAngle(Point[] sheep){
		Point[] oldsheep=sheep;
		int counter=0;
		int k=0;

		for (int i=0;i<oldsheep.length ;i++ ) {
			if(oldsheep[i].x>=49.0)
				counter++;
			
		}

		sheep=new Point[counter];

		for (int i = 0; i < oldsheep.length; i++) {
			if(oldsheep[i].x>=49.0)
				sheep[k++]=oldsheep[i];
		}

		Point up=new Point(50,51);
		for (int i = 0; i < sheep.length; i++) {
			for (int j = i+1; j < sheep.length; j++) {
				if (projection_norm(gate,up,sheep[i])>projection_norm(gate,up,sheep[j])){
					Point t=sheep[i];
					sheep[i]=sheep[j];
					sheep[j]=t;
				}
			}
		}
		return sheep;
	}

	static Point[] currentDogTargets(Point[] sorted_sheep, int id, int dog_count){

		int start_index=0;
		int end_index=0;
		int step = 0;

		double ratio=(sorted_sheep.length/dog_count);

		if (ratio>1.0) {
			double mod = ratio - Math.floor(ratio);
			if (mod >= 0.5) {
				step = (int)Math.floor(ratio);
			}

			else {
				step = (int)Math.ceil(ratio);
			}
		}

		else if (ratio <= 1.0) {
			step = 1;

			if (id < sorted_sheep.length) {
				start_index = id-1;
				end_index = start_index;
			}

			else {
				start_index = (id - 1) % sorted_sheep.length;
				end_index = start_index;
			}

			int j=0;

			Point [] assign= new Point[end_index-start_index+1];	

			for (int i=start_index;i<=end_index;i++){

			assign[j++]=sorted_sheep[i];	

			}

			for (int i=0 ; i < assign.length ; i++)
			{
				System.out.println(assign[i]);
			}

			return assign;

		}

		if(id==1){
			start_index=0;
		}
		else{
			start_index=step*(id-1);
		}

		if(id==dog_count){
			end_index=sorted_sheep.length-1;
		}
		else{
			end_index = start_index + step - 1;
		}

		int j=0;

		Point [] assign= new Point[end_index-start_index+1];

		for (int i=start_index;i<=end_index;i++){

			assign[j++]=sorted_sheep[i];

		}

		return assign;
	} 

	private static double projection_norm(Point g, Point u, Point p) {
		return ((u.x-g.x)*(p.x-g.x)+(u.y-g.y)*(p.y-g.y))/distance(g, u)/distance(g, p);
	}
	
	static double distance(Point a, Point b) {
        return Math.sqrt((a.x-b.x) * (a.x-b.x) +
                         (a.y-b.y) * (a.y-b.y));
    }
	
	public static void main(String args[]) {
		Point[] sheep=new Point[3];
		sheep[0]=new Point(51, 52);
		sheep[1]=new Point(51, 53);
		sheep[2]=new Point(51, 51);
		sheep=sortByAngle(sheep);
		/*for (int i = 0; i < sheep.length; i++) {
			System.out.println(sheep[i].x+" "+sheep[i].y);
		}*/
	}

}
