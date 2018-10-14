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
			currentNode = currentNode.parent;
		}
	}


	private Node Expand(Node lastNode) throws IllegalActionException, CloneNotSupportedException {
		ArrayList<Action> expand= lastNode.state.getBestPossibleMoves();
		for(Action a:expand)
		{
			lastNode.children.add(new Node(lastNode,lastNode.state.nextState(a, lastNode.state.getDeck()),a));
		}
		return lastNode.children.get(new Random().nextInt(expand.size()));
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