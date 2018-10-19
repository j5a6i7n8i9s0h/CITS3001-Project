package fuckenRollout;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;


public class RolloutAgent implements Agent{
	static final int MAX_SCORE = 25;
	static double EXPLORE = Math.sqrt(2);
	static int BRANCH_FACTOR = 100;
	
	
	private int totalCards = 50;
	
	private Colour[][] knowColours;
	private int[][] knowValues;
	private int[][] theyArrived;
	  
	private int[][] cardsLeftInDeck;
	  
	  
	private boolean firstAction = true;
	private int numPlayers;
	private int numCards;
	private int index;

	@Override
	public String toString(){return "MCTS";}
		
	public void init(State s) {		
	    index = s.getNextPlayer();
		numPlayers = s.getPlayers().length;
	    numCards = (numPlayers>3?4:5);
	    knowColours = new Colour[numPlayers][numCards];
    	knowValues = new int[numPlayers][numCards];
    	theyArrived = new int[numPlayers][numCards];
	    
	    cardsLeftInDeck = new int[5][5];
	    for(int i = 0; i < 5; i ++){
	    	for(int j = 0; j < 5; j ++){
	    		if(j == 0){
	    			cardsLeftInDeck[i][j] = 3;
	    		}else if(j == 4){
	    			cardsLeftInDeck[i][j] = 1;
	    		}else{
	    			cardsLeftInDeck[i][j] = 2;
	    		}
	    	}
	    }

	    
	    Stack<Card> tempDisc = s.getDiscards();
	    Stack<Card> tempFw;
	    
	    while(!tempDisc.isEmpty()){
			Card c = tempDisc.pop();
			cardsLeftInDeck[mapColourToInt(c.getColour())][c.getValue() - 1]--;
	    }
	    
	    for(int i = 0; i < 5; i ++){
	    	tempFw = s.getFirework(mapToColour(i));
		    while(!tempFw.isEmpty()){
				Card c = tempFw.pop();
				cardsLeftInDeck[mapColourToInt(c.getColour())][c.getValue() - 1]--;
		    }
	    }
	    
	    for(int i = 0; i < numPlayers; i ++){
	    	for(int j = 0; j < numCards; j ++){
	    		theyArrived[i][j] = 1;
	    		if(i != index){
	    			Card c = s.getHand(i)[j];
	    			cardsLeftInDeck[mapColourToInt(c.getColour())][c.getValue() - 1]--;
	    		}
	    	}
	    }
	    
	}
	
	private void updateLastActions(State s) {
	    
		try{
	        State t = (State) s.clone();
	        Stack<Action> updateActions = new Stack<Action>();
	        Stack<State> updateStates = new Stack<State>();
	        for(int i = 0; i<Math.min(numPlayers-1,s.getOrder());i++){
	        	Action a = t.getPreviousAction();
	        	updateActions.add(a);
	        	updateStates.add(t);
	        	t = t.getPreviousState();
	        }
	        
	        Action a = t.getPreviousAction();
			if(a != null){
		        if((a.getType()==ActionType.HINT_COLOUR || a.getType() == ActionType.HINT_VALUE)){
		            boolean[] hints = t.getPreviousAction().getHintedCards();
		            for(int j = 0; j<hints.length; j++){
		              if(hints[j]){
		                if(a.getType()==ActionType.HINT_COLOUR) 
		                  knowColours[a.getHintReceiver()][j] = a.getColour();
		                else
		                  knowValues[a.getHintReceiver()][j] = a.getValue();  
		              }
		            }
		        }else{
			        knowColours[a.getPlayer()][a.getCard()] = null;
			        knowValues[a.getPlayer()][a.getCard()] = 0;
		        	
		        	Stack<Card> temp = t.getDiscards();
		        	
		        	if(t.getPreviousState().getDiscards().size() < temp.size()){
		        		cardsLeftInDeck[mapColourToInt(temp.peek().getColour())][temp.peek().getValue()-1]--;
		        	}else{
		        		for(Colour c: Colour.values()){
		        			Stack<Card> tempFw = t.getFirework(c);
		    	        	if(t.getPreviousState().getFirework(c).size() < tempFw.size()){
		    	        		cardsLeftInDeck[mapColourToInt(tempFw.peek().getColour())][tempFw.peek().getValue()-1]--;
		    	        	}
		    	        }
		        	}
		        }
			}
	        while(!updateActions.isEmpty()){
	          Action updateAction = updateActions.pop();
	          State updateState =  updateStates.pop();
	          if((updateAction.getType()==ActionType.HINT_COLOUR || updateAction.getType() == ActionType.HINT_VALUE)){
	            boolean[] hints = updateAction.getHintedCards();
	            for(int j = 0; j<hints.length; j++){
	              if(hints[j]){
	                if(updateAction.getType()==ActionType.HINT_COLOUR) 
	                  knowColours[updateAction.getHintReceiver()][j] = updateAction.getColour();
	                else
	                  knowValues[updateAction.getHintReceiver()][j] = updateAction.getValue();  
	              }
	            }
	          }else{
		         knowColours[updateAction.getPlayer()][updateAction.getCard()] = null;
		         knowValues[updateAction.getPlayer()][updateAction.getCard()] = 0;
	        	 Card replaced =  updateState.getHand(updateAction.getPlayer())[updateAction.getCard()];
	        	 if(replaced != null && !firstAction){
	        		 cardsLeftInDeck[mapColourToInt(replaced.getColour())][replaced.getValue()-1]--;
	        		 theyArrived[updateAction.getPlayer()][updateAction.getCard()] = updateState.getOrder();
	        		 totalCards--;
	        	 }

	          }
	        }

	      System.out.print("");
	      }
	      catch(IllegalActionException e){e.printStackTrace();}
	}
	
	int mapColourToInt(Colour c){
	    switch(c){
	      case BLUE: return 0;
	      case RED: return 1;
	      case GREEN: return 2;
	      case WHITE: return 3;
	      case YELLOW: return 4;
	      default: return -1;
	    }
	}
	
	private Colour mapToColour(int c) {
	    switch(c){
	      case 0: return Colour.BLUE;
	      case 1: return Colour.RED;
	      case 2: return Colour.GREEN;
	      case 3: return Colour.WHITE;
	      case 4: return Colour.YELLOW;
	      default: return null;
	    }
	}

	@Override
	public Action doAction(State s) {
		if(firstAction){
			init(s);
	    } 
		
		index = s.getNextPlayer();
		updateLastActions(s);
		firstAction = false;
		
		State current_state = (State) s.clone();
		int[][] cloneCardsLeftInDeck = new int[5][5];
		for(int i = 0; i < 5; i ++){
			cloneCardsLeftInDeck[i] = cardsLeftInDeck[i].clone();
		}
		int[][] cloneKnowValues = new int[numPlayers][numCards];
		for(int i = 0; i < numPlayers; i ++){
			cloneKnowValues[i] = knowValues[i].clone();
		}
		Colour[][] cloneknowColours = new Colour[numPlayers][numCards];
		for(int i = 0; i < numPlayers; i ++){
			cloneknowColours[i] = knowColours[i].clone();
		}
		int[][] cloneArrived = new int[numPlayers][numCards];
		for(int i = 0; i < numPlayers; i ++){
			cloneArrived[i] = theyArrived[i].clone();
		}
		
		MyState rollout = new MyState(current_state, cloneCardsLeftInDeck, cloneKnowValues, cloneknowColours, cloneArrived);
		Action a = rollout.doAction(rollout);
		return a;
//		 try {
//			if(s.getHintTokens() > 7){
//				return new Action(index, toString(), ActionType.PLAY,0);	
//			}
//			return new Action(index, toString(), ActionType.DISCARD,0);
//		} catch (IllegalActionException e) {
//			return null;
//		}
	}
}