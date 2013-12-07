package sheepdog.g4;

import java.util.HashSet;

import sheepdog.sim.Point;

public class Player extends sheepdog.sim.Player {
    private static final double DISTANCE_BUFFER = 1.4; //1.5
	private int nblacks;
    private boolean mode;
    private int move_num;
    private double distanceInTree[];
    boolean isLeaf[];
    public  Point [] sheeps,dogs;
    Tree myTree=new Tree(this);
    static double DOG_SPEED = 2.0;
    static Point gate= new Point(50,50);
    static double epsilon=1e-6;
    boolean idle;
    
    public void init(int nblacks, boolean mode) {
        this.nblacks = nblacks;
        this.mode = mode;
        move_num=0;
        idle=false;
    }
    
    // Return: the next position
    // my position: dogs[id-1]
    HashSet<Integer> targetID=new HashSet<Integer>();
    public Point move(Point[] dogs, // positions of dogs
                      Point[] sheeps) { // positions of the sheeps
    	
    	sheeps=updateToNewSheep(sheeps,dogs);
    	move_num++;
        this.sheeps=sheeps;
        this.dogs=dogs;
        //this.sheeps=sheepOnRight;
    	// If Advanced mode and sheep on left
        Point[] sheepOnRight= Util.removeSheepOnLeft(sheeps, mode, nblacks);
        if(mode ){ //&& whiteOnLeft.length > 0 && sheeps.length==0
        	if (id>sheepOnRight.length) {
        		Point[] whiteOnLeft=Util.findWhiteSheepOnOtherSide(sheeps, nblacks);
        		this.sheeps=sheepOnRight;
            	return advanceTask(whiteOnLeft);
        	}
        }
        
        if (move_num==1 || move_num%10==0) {
        	Point[] targets = Util.sortByAngle(sheepOnRight);
        	targets=Util.currentDogTargetsKevin(targets, id, dogs.length);
        	System.out.println(id+" targets "+targets.length);
        	int sheepid=-1;
        	targetID.clear();
        	for (int i = 0; i < targets.length; i++)
        		for (int j = 0; j < sheeps.length; j++)
					if(targets[i].x==sheeps[j].x && targets[i].y==sheeps[j].y){
						targetID.add(j);
						break;
					}
        	this.sheeps=targets;
		}else{
			Point[] targets=new Point[targetID.size()];
			int j=0;
			for(Integer i:targetID){
				targets[j++]=sheeps[i];
			}
			targets=Util.removeSheepOnLeft(targets, false, nblacks);
			this.sheeps=targets;
		}
		
        return basicTask();
    }

    private Point advanceTask(Point[] whiteOnLeft) {
    	if(whiteOnLeft.length>=id-sheeps.length) {
    		if(dogs[id-1].x>50.0+epsilon)            
                return initial_dog_move(dogs[id-1]);
    		
            Point sheep;
            // Spare dogs 
            int countLazyDogs = dogs.length - sheeps.length;
            // White sheep on left
            int allocate[] = allocateWhiteOnLeft(whiteOnLeft,countLazyDogs,dogs); 
            // Allocate one white sheep to each dog
            sheep = whiteOnLeft[allocate[id - 1]];
	        Point dogPoint = positionDogNearSheep(sheep, gate, 0.00001, false);
	        Point dogPos=normalizeByDogSpeed(dogPoint,dogs[id-1]);
	        return dogPos;
		}
		else
			return new Point(dogs[id-1].x, dogs[id-1].y<2?epsilon:dogs[id-1].y-1.99);
	}

	private int[] allocateWhiteOnLeft(Point[] sheeps, int k, Point[] dogs) {
    	int ret[]=new int[dogs.length];
		for (int id = dogs.length-1; id >= dogs.length-k; id--) {
			double[] d=new double[sheeps.length];
	    	for (int j = 0; j < sheeps.length; j++) {
				d[j]=distance(dogs[id], sheeps[j]);
			}
	    	
	    	for (int j = id+1; j < dogs.length; j++) {
				d[ret[j]]+=1000;
			}
	    	print_array(d);
	    	int[] index=new int[sheeps.length];
	    	for (int i = 0; i < index.length; i++) {
				index[i]=i;
			}
			int i=0;
			for (int j = i+1; j < sheeps.length; j++) {
				if (d[i]>d[j]) {
					double t=d[i];
					d[i]=d[j];
					d[j]=t;
					int tt=index[i];
					index[i]=index[j];
					index[j]=tt;
				}
			}
			ret[id]=index[0];
		}
		return ret;
	}

	private Point basicTask() {
		
		if (sheeps.length ==0)
            return new Point(dogs[id-1].x, dogs[id-1].y>98?100-epsilon:dogs[id-1].y+1.99);
		
		//sheeps = Util.sortByAngle(sheeps);
		//sheeps=Util.currentDogTargets(sheeps, id, dogs.length);
		
        System.out.println(id+" targets sheeps "+sheeps.length);
        for (int i = 0; i < sheeps.length; i++) {
			System.out.println(sheeps[i].x+" "+sheeps[i].y);
		}
    	if(dogs[id-1].x<50.0-epsilon)
    		return initial_dog_move(dogs[id-1]);   
    	
    	distanceInTree=new double[sheeps.length];
        int [] father = myTree.buildTree(sheeps);
        isLeaf=new boolean[sheeps.length];
		for (int i = 0; i < isLeaf.length; i++) {
			isLeaf[i]=true;
		}
		findDistanceInTree(father, -1 , 0.0);
		//int[] farthest=commonSenseAllocate(1);	
		//int sheepid=farthest[0];
		int sheepid=farthestInTree();
		if(sheepid!=-1 && father[sheepid]!=-1 && distance(sheeps[sheepid], sheeps[father[sheepid]])<1){
			sheepid=father[sheepid];
		}
		
		Point fromPoint= sheeps[sheepid];
		Point toPoint=null;
		if(father[sheepid]==-1){
			toPoint=gate;
		}
        else 
            toPoint= sheeps[father[sheepid]];
		
		Point dogPoint = positionDogNearSheep(fromPoint, toPoint, 1.00001, true);
		Point dogPos=normalizeByDogSpeed(dogPoint,dogs[id-1]);
		return dogPos;
	}

	private int farthestInTree() {
		int ret=0;
		double[] d=new double[distanceInTree.length];
    	for (int j = 0; j < d.length; j++) {
			d[j]=distanceInTree[j]-distance(dogs[id-1], sheeps[j])*DISTANCE_BUFFER;//Math.sqrt(distance(dogs[id-1], sheeps[j]))*4;//
		}
    	for (int j = 0; j < d.length; j++) {
			if (isLeaf[j]=false)
				d[j]-=1000;
		}
    	
    	int[] index=new int[distanceInTree.length];
    	for (int i = 0; i < index.length; i++) {
			index[i]=i;
		}
		int i=0;
		for (int j = i+1; j < distanceInTree.length; j++) {
			if (d[i]<d[j]) {
				double t=d[i];
				d[i]=d[j];
				d[j]=t;
				int tt=index[i];
				index[i]=index[j];
				index[j]=tt;
			}
		}		
		ret=index[0];
		return ret;
	}

	private Point normalizeByDogSpeed(Point dogPoint, Point point) {
    	Point motion=new Point(dogPoint.x-point.x, dogPoint.y-point.y);
		double motionDist=vectorLengthPoint(motion);
		if (motionDist>DOG_SPEED) {
			motion.x=motion.x/motionDist*(DOG_SPEED-epsilon);
		    motion.y=motion.y/motionDist*(DOG_SPEED-epsilon);
		}
		//System.out.println("dist of motion is "+vectorLengthPoint(motion));
		Point dogPos=new Point(point.x+motion.x, point.y+motion.y);// TODO Auto-generated method stub
		return dogPos;
	}

	private Point[] updateToNewSheep(Point[] sheeps,Point[] dogs) {
		Sheepdog sheepdog=new Sheepdog(dogs.length, sheeps.length, nblacks, false);
		sheepdog.sheeps=sheeps;
		sheepdog.dogs=dogs;
		
		Point[] newSheeps = new Point[sheeps.length];
        for (int i = 0; i < sheeps.length; ++i) {
            // compute its velocity vector
            newSheeps[i] = sheepdog.moveSheep(i);
        }
		return newSheeps;
	}

	private int[] commonSenseAllocate(int k) {
		int ret[]=new int[k];
		
		for (int id = 1; id <= k; id++) {
			double[] d=new double[distanceInTree.length];
	    	for (int j = 0; j < d.length; j++) {
				d[j]=distanceInTree[j]-distance(dogs[id-1], sheeps[j])*DISTANCE_BUFFER;//Math.sqrt(distance(dogs[id-1], sheeps[j]))*4;//
			}
	    	for (int j = 0; j < d.length; j++) {
				if (isLeaf[j]=false)
					d[j]-=1000;
			}
	    	for (int j = 1; j < id; j++) {
				d[ret[j]]-=1000;
			}
	    	int[] index=new int[distanceInTree.length];
	    	for (int i = 0; i < index.length; i++) {
				index[i]=i;
			}
			for (int i = 0; i < distanceInTree.length; i++) {
				for (int j = i+1; j < distanceInTree.length; j++) {
					if (d[i]<d[j]) {
						double t=d[i];
						d[i]=d[j];
						d[j]=t;
						int tt=index[i];
						index[i]=index[j];
						index[j]=tt;
					}
				}
			}
			ret[id-1]=index[0];
		}
		return ret;
	}

	public void print_array(double[] array){
        for (int i=0; i<array.length;i++ ) {
            System.out.print(array[i] + ",  ");
        }
        System.out.println();
    }

    public void print_array(int[] array){
        for (int i=0; i<array.length;i++ ) {
            System.out.print(array[i] + ",  ");   
        }
        System.out.println();
    }

    static Point initial_dog_move(Point dog){
            double dist_from_gate=distance(gate, dog);
            double move_distance=dist_from_gate;
            double speed=DOG_SPEED-epsilon;
            if(dist_from_gate<=speed)
                return gate;
            else
                move_distance=speed;
            double x_new = dog.x + (move_distance)*(gate.x-dog.x)/dist_from_gate;
            double y_new = dog.y + (move_distance)*(gate.y-dog.y)/dist_from_gate;
            
            dog.x=x_new;
            dog.y=y_new;
            return dog;            
    }
    
    // compute Euclidean distance between two points
    static double distance(Point a, Point b) {
        return Math.sqrt((a.x-b.x) * (a.x-b.x) +
                         (a.y-b.y) * (a.y-b.y));
    }
    // return the length of a vector
    static double vectorLength(double ox, double oy) {

        return Math.sqrt(ox * ox + oy * oy);
    }

    static double vectorLengthPoint(Point p) {

        return Math.sqrt(p.x*p.x + p.y*p.y);
    }
    
    //vipul
    static Point positionDogNearSheep(Point from, Point to, double BUFFER, boolean truncate){
        // Calculate Slope, Intercept and Initial Distance
        double slope = (from.y - to.y)/(from.x - to.x);
        //System.out.println("Slope: " + slope);
        double initialDistance = distance(from, to);
        //System.out.println("Initial Dist: " + initialDistance);
        // Final Coordinates
        Point finalDestination = new Point();
        finalDestination.x = ((from.x - to.x)*(initialDistance + BUFFER)/(initialDistance)) + to.x;
        finalDestination.y = ((from.y - to.y)*(initialDistance + BUFFER)/(initialDistance)) + to.y;
        
        if(!truncate)
        	return finalDestination;
        // Error Checking
        if (finalDestination.x > 100)
        {
		finalDestination.x = 100;
		finalDestination.y = slope*(100 - to.x) + to.y;
        }

        if (finalDestination.x < 50)
        {
	       finalDestination.x = 50;
	       finalDestination.y = slope*(50 - to.x) + to.y;
        }

        if (finalDestination.y > 100){
            finalDestination.y = 100;
            finalDestination.x = (100 - to.y)/slope + to.x;
        }

        if (finalDestination.y < 0){
            finalDestination.y = 0;
            finalDestination.x = (0 - to.y)/slope + to.x;
        }
        //System.out.println("X: " + finalDestination.x);
        //System.out.println("Y: " + finalDestination.y);
        return finalDestination;
    }

    private void findDistanceInTree(int[] father, int index, double distance){
        double temp_distance = 0.0;
        for (int i=0;i<sheeps.length;i++) {
            if (father[i] == index) {
                if(index == -1)
                    temp_distance = distance(gate, sheeps[i]);
                else{
                    temp_distance = distance(sheeps[index], sheeps[i]);
                    isLeaf[index]=false;
                }
                distanceInTree[i] = distance+temp_distance;
                findDistanceInTree(father, i, distance+temp_distance);
            }         
        }
         return;
    }
}
