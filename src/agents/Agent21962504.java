package agents;
import hanabAI.*;
import java.util.*;


public class Agent21962504 implements Agent {
	
	private Colour[][] knowncolours;
	private int[][] knownvalues;
	private int[][] cardsLeftInPlay;
	
	private int numPlayers,numCards;
	
	private static double explore = Math.sqrt(2.0);
	private boolean firstaction = true; 
	
	
	@Override
	public Action doAction(State s) {
		// TODO Auto-generated method stub
		if(firstaction)
			setup(s);
		
		return null;
	}
	
	private void setup(State s)
	{
		this.numPlayers = s.getPlayers().length;
		this.firstaction = false;
		this.numCards = (numPlayers>3)?4:5;
		this.knowncolours = new Colour[numPlayers][numCards];
		this.knownvalues = new int[numPlayers][numCards];
		
	}
	/*
	 * 
	 * https://int8.io/monte-carlo-tree-search-beginners-guide/
	 * Potential actions 
	 	* discard last 
	 	* hint colour 
	 	* hint value 
	 	* Hint usefulness (keep or play) 
	 	* hint uselessenss (discard) 
	 	* Play If certain
	 		* 	 Base off hints 
	 	* 
	 * 
	 * 
	 * */
	
	
	public String toString() {
		return "Jainish";
	}
	
	class Node{
		Node parent; 
		ArrayList<Node> children; 
		Action action; 
		int player; // which player is this node 
		int visits; 
		int score; 
		
		public Node(Node parent, Action action, int player)
		{
			this.parent = parent;
			this.action = action;
			this.player = player;
			this.visits = 0;
			this.score = 0;
			children = new ArrayList<Node>();
		}
		// for back tracking
		public void update(State s)
		{
			this.visits++;
			//if not root node/plyaer ? 
			this.score += s.getScore(); 
		}
		//Exploit vs explore 
		public double getUCT()
		{
			return (double)this.score/(double)this.visits + Agent21962504.explore*Math.sqrt(
					(double)Math.log(this.parent.visits)/(double)this.visits);
		}
		
		public Node select(ArrayList<Action> BestActions)
		{
			Node child_to_return = null;
			for(Node child: this.children)
				if(BestActions.contains(child.action))
					if(child_to_return == null || child_to_return.getUCT() < child.getUCT())
						child_to_return = child;
			return child_to_return;
		}
		
		
	}
}