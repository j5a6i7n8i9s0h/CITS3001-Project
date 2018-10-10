package agents;

import hanabAI.Action;
import hanabAI.ActionType;
import hanabAI.Agent;
import hanabAI.Card;
import hanabAI.Colour;
import hanabAI.IllegalActionException;
import hanabAI.State;

public class RuansGreedyAgent implements Agent{
	  
	private Colour[][] knowColours;
	private int[][] knowValues;
	private int[][] theyArrived;
	  
	private int[][] cardsLeftInPlay;
	  
	  
	private boolean firstAction = true;
	private int numPlayers;
	private int numCards;
	private int index;

	
	public RuansGreedyAgent(){}
	  
	@Override
	public String toString(){return "Ruan";}
		
	private void init(State s) {		
	    numPlayers = s.getPlayers().length;
	    if(numPlayers>3){
	    	numCards = 4;
	    	knowColours = new Colour[numPlayers][4];
	    	knowValues = new int[numPlayers][4];
	    	theyArrived = new int[numPlayers][4];
	      
	    }
	    else{
	    	numCards = 5;
	    	knowColours = new Colour[numPlayers][5];
	    	knowValues = new int[numPlayers][5];
	    	theyArrived = new int[numPlayers][5];
	      
	    }
	    
	    for(int i = 0; i < numPlayers; i ++){
	    	for(int j = 0; j < numCards; j ++){
	    		theyArrived[i][j] = 1;
	    	}
	    }
	    
	    cardsLeftInPlay = new int[5][5];
	    for(int i = 0; i < 5; i ++){
	    	for(int j = 0; j < 5; j ++){
	    		if(j == 0){
	    			cardsLeftInPlay[i][j] = 3;
	    		}else if(j == 4){
	    			cardsLeftInPlay[i][j] = 1;
	    		}else{
	    			cardsLeftInPlay[i][j] = 2;
	    		}
	    	}
	    }
	    
	    index = s.getNextPlayer();
	    firstAction = false;
	}
	
	@Override
	public Action doAction(State s) {
		if(firstAction){
			init(s);
	    } 
		
		index = s.getNextPlayer();
		updateLastActions(s);
	    
	    index = s.getNextPlayer();
	    //get any hints
	    try{
	      Action a = playKnown(s);
	      if(a==null) a = hint(s);
	      if(a==null) a = discardKnown(s);
	      if(a==null) a = hintRandom(s);
	      if(a==null) a = discardGuess(s);
	      if(a==null) a = playGuess(s);
	      return a;
	    }
	    catch(IllegalActionException e){
	      e.printStackTrace();
	      throw new RuntimeException("Something has gone very wrong");
	    }
	}
	
	private Action playKnown(State s) throws IllegalActionException{
	    for(int i = 0; i<numCards; i++){
	    	if(knowColours[index][i] != null && knowValues[index][i] != 0){
	    		if(playable(s, knowColours[index][i], knowValues[index][i])){
	    			knowColours[index][i] = null;
	    			knowValues[index][i] = 0;
	    			theyArrived[index][i] = s.getOrder();
	    	        return new Action(index, toString(), ActionType.PLAY,i);
	    		}
	    	}
	    	else if(knowValues[index][i] != 0){
	    		if(playable(s, knowValues[index][i])){
	    			knowColours[index][i] = null;
	    			knowValues[index][i] = 0;
	    			theyArrived[index][i] = s.getOrder();
	    	        return new Action(index, toString(), ActionType.PLAY,i);
	    		}
	    	}
	    	else if(knowColours[index][i] != null){
	    		if(playable(s, knowColours[index][i])){
	    			knowColours[index][i] = null;
	    			knowValues[index][i] = 0;
	    			theyArrived[index][i] = s.getOrder();
	    	        return new Action(index, toString(), ActionType.PLAY,i);
	    		}
	    	}
	    	else{
	    		//know nothing of the card
	    	}
	      }
	      return null;
	}
	
	private Action discardKnown(State s) throws IllegalActionException{
	    if (s.getHintTokens() != 8) {
	        for(int i = 0; i<numCards; i++){
	          if(knowColours[index][i]!=null && knowValues[index][i]>0 && knowValues[index][i]<topFw(s, knowColours[index][i])){
	    		knowColours[index][i] = null;
	    		knowValues[index][i] = 0;
	    		theyArrived[index][i] = s.getOrder();
	            return new Action(index, toString(), ActionType.DISCARD,i);
	          }
	        }
	      }
	      return null;
	}

	private Action hint(State s) throws IllegalActionException{
		
		
		
		return null;
	}
	
	private Action hintRandom(State s) throws IllegalActionException {
		// TODO Auto-generated method stub
		return null;
	}

	private Action discardGuess(State s) throws IllegalActionException{
		// TODO Auto-generated method stub
		return null;
	}

	private Action playGuess(State s) throws IllegalActionException{
		// TODO Auto-generated method stub
		return null;
	}





	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	//helper classes and functions
	
	private int topFw (State s, Colour c){
		java.util.Stack<Card> fw = s.getFirework(c);
	    if (fw.size()==5) return -1;
	    else return (fw.size()+1);
	}
	
	private boolean playable(State s, Colour c, int i) {
	    int top = topFw(s,c);
	    return top == i;
	}
	
	private boolean playable(State s, int i) {
		boolean[] canPlay = new boolean[5];
		
		for(Colour c: Colour.values()){
			java.util.Stack<Card> fw = s.getFirework(c);
			canPlay[mapColourToInt(c)] = (fw.size()+1 == i);
		}
		
		boolean playable = true;
		
		for(int j = 0 ; j < 5; j++){
			if(!canPlay[j]){
				int inPlay = cardsLeftInPlay[j][i] - howManySee(s, new Card( mapToColour(j), i));
				if(inPlay != 0){playable = false;}
			}
		}
		
		return playable;
	}

	private boolean playable(State s, Colour c) {

		int toPlay;
		
		java.util.Stack<Card> fw = s.getFirework(c);
	    if (fw.size()==5) return false;
	    else toPlay = fw.size() + 1;
	    
	    
		boolean playable = true;
		
		for(int j = 0 ; j < 5; j++){
			if(j != toPlay){
				int inPlay = cardsLeftInPlay[mapColourToInt(c)][j] - howManySee(s, new Card(c, j));
				if(!(inPlay == 0)){playable = false;}
			}
		}
		
		return playable;
	}


	private int howManySee(State s, Card c){
		
		int count = 0;
		for(int i = 0; i < numPlayers; i ++){
			if(i == index){continue;}
			for(int j = 0;  j < numCards; j ++){
				if(s.getHand(i)[j].equals(c)){
					count++;
				}
			}
		}
		
		return count;
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
	        	 Card c =  t.getHand(a.getPlayer())[a.getCard()];
	        	 cardsLeftInPlay[mapColourToInt(c.getColour())][c.getValue()]--;
	        	 theyArrived[a.getPlayer()][a.getCard()] = t.getOrder();
	          }
	         
	          t = t.getPreviousState();
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

}
