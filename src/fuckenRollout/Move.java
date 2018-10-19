package fuckenRollout;
import java.util.Comparator;

import hanabAI.*;

public class Move{
	private Action action; 
	private int score; 
	
	public Move(Action action , int score ) 
	{
		this.action = action ;
		this.score = score;
	}

	
	public Action getAction(){return this.action;}
	public int getScore(){return this.score;}
	
}
