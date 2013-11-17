package sheepdog.g2;

import sheepdog.sim.Point;

public class Player extends sheepdog.sim.Player {
    
	boolean firstCall;
	sheepdog.sim.Player activePlayer;
	
	public Player()
	{
		firstCall=true;
	}
	
    // Return: the next position
    // my position: dogs[id-1]
    public Point move(Point[] dogs, // positions of dogs
                      Point[] sheeps) { // positions of the sheeps
        if(firstCall)
        {
        	firstCall=false;
        	activePlayer=new OneDogPlayer();
        }

    	return activePlayer.move(dogs, sheeps);
    }

}
