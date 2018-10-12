package MCST;

import java.util.ArrayList;

import hanabAI.Action;
import hanabAI.State;

class Node{
	Node parent; 
	ArrayList<Node> children; 
	ArrayList<Action> expandable;
	MyState state; 

	int player; // which player is this node 
	int visits; 
	int score; 
	
	public Node(Node parent, MyState s)
	{
		this.parent = parent;
		this.player = player;
		this.visits = 0;
		this.score = 0;
		children = new ArrayList<Node>();
	}
	// for back tracking
	public void update(State s)
	{
		this.visits++;
		//if not root node/player ? 
		this.score += s.getScore(); 
	}
	//Exploit vs explore 
	public double getUCT()
	{
		return ((double)this.score/(double)agent.MAX_SCORE)/(double)this.visits + agent.EXPLORE*Math.sqrt(
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