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
		long timeLimit = System.currentTimeMillis() + 380;
		Node currentNode = root;
		while(System.currentTimeMillis() < timeLimit){
			currentNode = Select(root);
			BackPropogation(currentNode, currentNode.state.Rollout());
		}

		//System.out.println("done");
		Action a= bestChildtoPlay(root).action;
		return a;

	}

	private Node bestChildtoPlay(Node rootNode) {
		Node bestSelection = null;
		double score = 0;
		for(Node child: rootNode.children){
			if(child.averageScore() > score){
				score = child.averageScore();
				bestSelection = child;
			}
		}
		return bestSelection;
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
		Action action = lastNode.expandable.poll().getAction();
		MyState newState = (MyState) lastNode.state.nextState(lastNode.state, action);
		Node child = new Node(lastNode,newState,action);
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
			if(currentNode.expandable.isEmpty()||currentNode.children.size()==agent.BRANCH_FACTOR){
				Node bestChild = bestChild(currentNode);
				if(bestChild==null) break;
				//bestChild.reshuffle(currentNode.state);
				currentNode = bestChild;
			}else{
				return Expand(currentNode);
			}
		}
		return currentNode;
	}
	
	
}