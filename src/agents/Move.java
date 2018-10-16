package agents;
import hanabAI.*;

public class Move implements Comparable<Move> {
	private Action action; 
	private int score; 
	
	public Move(Action action , int score ) 
	{
		this.action = action ;
		this.score = score;
	}

	@Override
	public int compareTo(Move o) {
		return o.score-this.score;
	}
	
	public Action getAction(){return this.action;}

}
