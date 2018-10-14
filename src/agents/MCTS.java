package agents;
import hanabAI.*;
import java.util.*;
// IS-MCTS

//MyState
//


public class MCTS {
	
	Node root;
	
	public MCTS(MyState s){
		root = new Node(null, s, null);
		
	}
	
	public void MCTSsearch() throws CloneNotSupportedException, IllegalActionException{
		long timeLimit = System.currentTimeMillis() + 990;
		
		while(System.currentTimeMillis() < timeLimit){
			Node currentNode = root;
			currentNode = Select(root);
			BackPropogation(currentNode, currentNode.state.Rollout());
		}
	}

	private void BackPropogation(Node currentNode, int rollout) {
		while(currentNode.parent!=null)
		{
			currentNode.score+=rollout;
			currentNode = currentNode.parent;
		}
	}


	private Node Expand(Node lastNode) throws IllegalActionException, CloneNotSupportedException {
		ArrayList<Action> expand= lastNode.state.getBestPossibleMoves();
		Action action = expand.get(new Random().nextInt(expand.size()));
		Node child = new Node(lastNode,lastNode.state.nextState(action, lastNode.state.getDeck()),action);
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