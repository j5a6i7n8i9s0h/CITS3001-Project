package agents;
import hanabAI.*;
import java.util.*;
// IS-MCTS

//MyState
//


public class MCTS {
	
	Node root;
	
	public MCTS(MyState s) throws IllegalActionException{
		root = new Node(null, s, null);
		
	}
	
	public Action MCTSsearch() throws CloneNotSupportedException, IllegalActionException{
		long timeLimit = System.currentTimeMillis() + 990;
		Node currentNode = root;
		while(System.currentTimeMillis() < timeLimit){
			currentNode = Select(currentNode);
			BackPropogation(currentNode, currentNode.state.Rollout());
		}
		return bestChild(root).action;
	}

	private void BackPropogation(Node currentNode, int rollout) {
		while(currentNode.parent!=null)
		{
			currentNode.score+=rollout;
			currentNode.visits++;
			currentNode = currentNode.parent;
		}
		currentNode.visits++;
	}


	private Node Expand(Node lastNode) throws IllegalActionException, CloneNotSupportedException {
		Action action = lastNode.expandable.pop();
		MyState newState = (MyState) lastNode.state.clone();
		Node child = new Node(lastNode,
				newState.nextState(action, newState.getDeck()),action);
		lastNode.children.add(child);
		return child;
	}

	private Node bestChild(Node currentNode) {
		Node bestSelection = null;
		double UctOfBest = 0;
		for(Node child: currentNode.children){
			if(child.getUCT() > UctOfBest){
				UctOfBest = child.getUCT();
				bestSelection = child;
			}
		}
		return bestSelection;
	}
	
	private Node Select(Node node) throws IllegalActionException, CloneNotSupportedException {
		Node currentNode = node;
		while (!currentNode.state.gameOver()){
			if(currentNode.expandable.isEmpty()){
				currentNode = bestChild(currentNode);
			}else{
				return Expand(currentNode);
			}
		}
		return currentNode;
	}
	
	
}