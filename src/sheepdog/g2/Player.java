package sheepdog.g2;

import sheepdog.sim.Point;

public class Player extends sheepdog.sim.Player {
    
	int nblacks;
	boolean mode;
	boolean reversed;
	
	@Override
	public void init(int nblacks, boolean mode) {
		// TODO Auto-generated method stub
		this.nblacks=nblacks;
		this.mode=mode;
	}
	
	boolean firstCall;
	sheepdog.sim.Player activePlayer;
	
	public Player()
	{
		firstCall=true;
		reversed=false;
	}
	
    public Point move(Point[] dogs, // positions of dogs
                      Point[] sheeps) { // positions of the sheeps
    	
        if(firstCall || (reversed && someBlackOnRight(sheeps)))
    	//if(firstCall || (reversed && allWhiteOnRight(sheeps)))
        {
        	reversed=false;
        	firstCall=false;
        	
        	activePlayer=new ImprovedPlayerOld();
        	
        	activePlayer.id=this.id;
        	activePlayer.init(nblacks,mode);
        }
        
        if(mode && !reversed && allBlackOnLeft(sheeps))
        {
        	System.out.println("Reversed");
        	reversed=true;
        	activePlayer=new OneOnOnePlayerReverse();
        	activePlayer.id=this.id;
        	activePlayer.init(nblacks,mode);
        }
        
        
    	return activePlayer.move(dogs, sheeps);
    }

    private boolean someBlackOnRight(Point[] sheeps) {
    	
		for(int i=0;i<nblacks;i++)
		{
			if(sheeps[i].x>50)
				return true;
		}
		
		return false;
	}

    private boolean someWhiteOnLeft(Point[] sheeps) {
	
		for(int i=nblacks;i<sheeps.length;i++)
		{
			if(sheeps[i].x<50)
				return true;
		}
		
		return false;
    }

	private boolean allBlackOnLeft(Point[] sheeps) {
		
		for(int i=0;i<nblacks;i++)
			if(sheeps[i].x>=50)
				return false;
		 
		return true;
	}
	
	private boolean allWhiteOnRight(Point[] sheeps) {
		
		for(int i=nblacks;i<sheeps.length;i++)
			if(sheeps[i].x<50)
				return false;
		 
		return true;
	}
}
