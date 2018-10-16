package agents;


import java.util.*;

import hanabAI.Action;
import hanabAI.IllegalActionException;
import hanabAI.State;

class Node{
	Node parent; 
	ArrayList<Node> children; 
	Stack<Action> expandable;
	MyState state; 
	Action action;
//	int player; // which player is this node 
	int visits; 
	int score; 
	
	public Node(Node parent, MyState s, Action action) throws IllegalActionException
	{
		this.parent = parent;
//		this.player = player;
		this.state = s;
		this.visits = 0;
		this.score = 0;
		this.action = action;
		children = new ArrayList<Node>();
		this.expandable = this.state.getBestPossibleMoves();
	}

	//Exploit vs explore 
	public double getUCT()
	{
		return (double)this.score/(double)this.visits + agent.EXPLORE*Math.sqrt(
				(double)Math.log(this.parent.visits)/(double)this.visits);
	}
	
}