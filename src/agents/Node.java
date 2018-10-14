package agents;


import java.util.*;

import hanabAI.Action;
import hanabAI.State;

class Node{
	Node parent; 
	ArrayList<Node> children; 
	ArrayList<Action> expandable;
	Stack<Node> expandableStack;
	MyState state; 
	Action action;

	int player; // which player is this node 
	int visits; 
	int score; 
	
	public Node(Node parent, MyState s, Action action)
	{
		this.parent = parent;
		this.player = player;
		this.visits = 0;
		this.score = 0;
		this.action = action;
		children = new ArrayList<Node>();
		expandableStack = new Stack();
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
	
	public ArrayList<Action> getUnexp(){
		ArrayList<Action> unexp = new ArrayList<Action>(expandable);
		for(Node n:children)
		{
			if(expandable.contains(n.action))
				unexp.remove(n.action);
		}
		return unexp;
	}
	
}