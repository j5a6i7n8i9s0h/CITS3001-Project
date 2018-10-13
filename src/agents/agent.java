package agents;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import hanabAI.Action;
import hanabAI.ActionType;
import hanabAI.Agent;
import hanabAI.Card;
import hanabAI.Colour;
import hanabAI.IllegalActionException;
import hanabAI.State;

public class agent implements Agent{
	static final int MAX_SCORE = 25;
	static double EXPLORE = Math.sqrt(2.0);
	
	
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
	    firstAction = false;
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

	        for(int i = 0; i<Math.min(numPlayers-1,s.getOrder());i++){
	          Action a = t.getPreviousAction();
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
	        	 Card replaced =  t.getHand(a.getPlayer())[a.getCard()];
	        	 if(replaced != null){
	        	 cardsLeftInDeck[mapColourToInt(replaced.getColour())][replaced.getValue()-1]--;
	        	 theyArrived[a.getPlayer()][a.getCard()] = t.getOrder();
	        	 //totalCards--;
	        	 }

	          }
	         
	          t = t.getPreviousState();
	        }
	        Action a = t.getPreviousAction();
			if(a == null){return;}
			
			
	        if((a.getType()==ActionType.HINT_COLOUR || a.getType() == ActionType.HINT_VALUE)){
	        	//already up to date
	        }else{
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
		
		 try {
			if(s.getHintTokens() > 7){
				return new Action(index, toString(), ActionType.PLAY,0);	
			}
			return new Action(index, toString(), ActionType.DISCARD,0);
		} catch (IllegalActionException e) {
			return null;
		}
	}
}
