package sheepdog.g2;

import sheepdog.sim.Point;

public class Player extends sheepdog.sim.Player {
    
	@Override
	public void init(int nblacks, boolean mode) {
		// TODO Auto-generated method stub
		
	}
	
	boolean firstCall;
	sheepdog.sim.Player activePlayer;
	
	public Player()
	{
		firstCall=true;
	}
	
    public Point move(Point[] dogs, // positions of dogs
                      Point[] sheeps) { // positions of the sheeps
        if(firstCall)
        {
        	firstCall=false;
        	
        	/*if(dogs.length==1)
        		activePlayer=new OneDogPlayer();
        	else
        		activePlayer=new OneOnOnePlayer();*/
        	
        	activePlayer=new ImprovedPlayerOld();
        	
        	activePlayer.id=this.id;
        }

    	return activePlayer.move(dogs, sheeps);
    }

	

}
