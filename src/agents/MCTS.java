package agents;
import hanabAI.*;
import java.util.*;
// IS-MCTS

//MyState
//


public class MCTS {
	
	Node root;
	
	public MCTS(MyState s){
		root = new Node(null, s);
		
	}
	
	public void MCTSsearch(){
		long timeLimit = System.currentTimeMillis() + 990;
		
		while(System.currentTimeMillis() < timeLimit){
			Node currentNode = root;
			currentNode = Select(root);
			BackPropogation(currentNode, currentNode.state.Rollout());
		}
		

	}

	private void BackPropogation(Node currentNode, Object rollout) {
		// TODO Auto-generated method stub
		
	}


	private Node Expand(Node lastNode) {
		
		return null;
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
	
	private Node Select(Node node) {
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