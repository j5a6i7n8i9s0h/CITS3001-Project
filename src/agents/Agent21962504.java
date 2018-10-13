package agents;
import hanabAI.*;
import java.util.*;
// IS-MCTS

public class Agent21962504 implements Agent {
	
	private Colour[][] knowncolours;
	private int[][] knownvalues;
	private int[][] cardsLeftInPlay;
	private boolean[][] canDiscard;
	private int numPlayers,numCards;
	
	private static double explore = Math.sqrt(2.0);
	private boolean firstaction = true; 
	
	
	@Override
	public Action doAction(State s) {
		// TODO Auto-generated method stub
		if(firstaction)
			setup(s);
		
		Node curr_node = new Node(null,null,-1);
		gameState curr_state = new gameState();
		long time = System.currentTimeMillis();
		while(System.currentTimeMillis()-time<990)
		{
			//select 
			
			
			//expand 
			
			//backpropagate
		}
		
		return null;
	}
	
	public
	
	
	private void setup(State s)
	{
		this.numPlayers = s.getPlayers().length;
		this.firstaction = false;
		this.numCards = (numPlayers>3)?4:5;
		this.knowncolours = new Colour[numPlayers][numCards];
		this.knownvalues = new int[numPlayers][numCards];
		for(int i=0;i<5;i++) 
			for(int j=0; j<5 ;j++)
				this.cardsLeftInPlay[i][j] = (j==0)?3:(j==5)?1:2;
		firstaction = false;
		
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
	class gameState{
		/**The name of each of the players in the game**/
		private String[] players;
		/**The stack of cards that have bee discarded, or incorrectly played**/
	    private Stack<Card> discards;
	    /**For each colour, the cards making up that firework so far**/
	    private Map<Colour,Stack<Card>> fireworks;
	    /**The hand of each player**/
	    private Card[][] hands;
	    /**The order of this state in the game**/
	    private int order=0;
	    /**The number of hints remaining**/
	    private int hints=0;
	    /**The number of fuse tokens left**/
	    private int fuse=0;
	    /**The observer of this state. This allows hidden information to be redacted**/
	    private int observer=-1;
	    /**The previous State of the game, so that all states are accessible back to the first state (with a null previous state)**/
	    private State previousState;
	    /**A list of all moves made so far in the game, in the order they were played**/
	    private Action previousAction;
	    /**The index of the next player to move**/
	    private int nextPlayer=-1;
	    /**The fnal play of the game (for when the deck runs out)**/
	    private int finalAction=-1;
		public gameState(String [] players, Stack<Card> discards, 
				Map<Colour,Stack<Card>> fireworks, 
				Card[][] hands, int order, int hints, int fuse, int observer) {
			
		}
	}
}