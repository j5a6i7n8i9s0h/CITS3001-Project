// to add
// analyse my last hint
// 2 step hints
// still discards
// wrong hint 
// play best known if more than one is known 
// add known cards in hand to be considered in the can see function 

//dumbass is newewst facepalm 



package agents;

import hanabAI.Action;
import hanabAI.ActionType;
import hanabAI.Agent;
import hanabAI.Card;
import hanabAI.Colour;
import hanabAI.IllegalActionException;
import hanabAI.State;

public class RuansGreedyAgent implements Agent{
	 
	private int totalCards = 50;
	
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
		
	public void init(State s) {		
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
	    
	    
		int maxValue = 0;
		int bestHint = -1;
		
		for(int i = 0; i < 10; i ++){
			int value = evaluateHint(s, (index+1)%numPlayers, i);
			if(value > maxValue){
				maxValue = value;
				bestHint = i;
			}
		}
	    
	    
	    try{
	      Action a = playKnown(s);
	      
	      if(maxValue >= 15){
	    	  if(a==null) a = hint(s, (index+1)%numPlayers, bestHint);
	    	  if(a==null) a = discardKnown(s);
	    	  
	      }else{
		      if(a==null) a = discardKnown(s);
		      if(maxValue > 5){
		    	  if(a==null) a = hint(s, (index+1)%numPlayers, bestHint);
		      }
	      }
	      
	      if(a==null) a = discardOldest(s);
	      if(a==null) a = hint(s, (index+1)%numPlayers, bestHint);
	      if(a==null) a = guess(s);
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
	    			totalCards --;
	    	        return new Action(index, toString(), ActionType.PLAY,i);
	    		}
	    	}
	    	else if(knowValues[index][i] != 0){
	    		if(playable(s, knowValues[index][i])){
	    			knowColours[index][i] = null;
	    			knowValues[index][i] = 0;
	    			theyArrived[index][i] = s.getOrder();
	    			totalCards --;
	    	        return new Action(index, toString(), ActionType.PLAY,i);
	    		}
	    	}
	    	else if(knowColours[index][i] != null){
	    		if(playable(s, knowColours[index][i])){
	    			knowColours[index][i] = null;
	    			knowValues[index][i] = 0;
	    			theyArrived[index][i] = s.getOrder();
	    			totalCards --;
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
	        	if(knowColours[index][i]!=null && knowValues[index][i]>0){
	        		if(discardable(s, knowColours[index][i], knowValues[index][i])){
	        			knowColours[index][i] = null;
		        		knowValues[index][i] = 0;
		        		theyArrived[index][i] = s.getOrder();
		        		totalCards --;
		        		return new Action(index, toString(), ActionType.DISCARD,i);
	        		}

	        	}
				else if(knowValues[index][i] != 0){
					if(discardable(s, knowValues[index][i])){
						knowColours[index][i] = null;
						knowValues[index][i] = 0;
						theyArrived[index][i] = s.getOrder();
						totalCards --;
				        return new Action(index, toString(), ActionType.DISCARD,i);
					}
				}
				else if(knowColours[index][i] != null){
					if(discardable(s, knowColours[index][i])){
						knowColours[index][i] = null;
						knowValues[index][i] = 0;
						theyArrived[index][i] = s.getOrder();
						totalCards --;
				        return new Action(index, toString(), ActionType.DISCARD,i);
					}
				}
				else{
					//know nothing of the card
				}
	        }
	      }
	      return null;
	}

	private Action hint(State s, int p, int hint) throws IllegalActionException{
		
		if(s.getHintTokens() == 0){
			return null;
		}
		
		boolean[] match = new boolean[numCards];
		
		if(hint > 4){
			
			for(int i = 0 ; i < numCards; i++){
				if(s.getHand(p)[i].getValue() == hint - 4){
					match[i] = true;
				}
			}
			
			return new Action(index, toString(), ActionType.HINT_VALUE, p, match, hint - 4);
		}else{
			
			for(int i = 0 ; i < numCards; i++){
				if(s.getHand(p)[i] == null){continue;}
				if(s.getHand(p)[i].getColour() == mapToColour(hint)){
					match[i] = true;
				}
			}
			
			return new Action(index, toString(), ActionType.HINT_COLOUR,p, match, mapToColour(hint));
		}
	}
	

	private Action discardOldest(State s) throws IllegalActionException{
	
		
		if(s.getHintTokens() == 8){return null;}
		int[] age = new int[numCards];
		for(int i = 0; i < numCards; i ++){
			age[i] = i;
		}
		for(int i = 1; i < numCards; i ++){
			
			int key = theyArrived[index][i];
			int j = i-1;
			
			while(j>=0 && theyArrived[index][age[j]]> key){
				age[j+1] = age[j];
				j = j -1;
			}
			age[j+1] = i;
		}
		
		int oldest = -1;
		//find oldest un-hinted card
		for(int i = 0; i < numCards; i ++){
			if(knowValues[index][age[i]] == 0 || knowColours[index][age[i]] == null){
				oldest = age[i];
			}
		}
		
		if(oldest == -1){
			return null;
		}else{
			return new Action(index, toString(), ActionType.DISCARD, oldest);
		}

	}

	private Action guess(State s) throws IllegalActionException{
		double probCorrectPlay = 0;
		int play = -1;
		
		double probCorrectDiscard = 0;
		int discard = -1;
		
		int numPlayable = 0;
		int numDiscardable = 0;
		int numTotal = 0;
		
		
		for(int i = 0; i < numCards; i++){
			if(knowColours[index][i] != null && knowValues[index][i] > 0){
				numPlayable = 0;
				numDiscardable = 0;
				//since we wouldve played it already
			}
			else if(knowColours[index][i] != null){
				Colour c = knowColours[index][i];
				int top = topFw(s,c);
				if(top < 5){
					numPlayable = cardsLeftInPlay[mapColourToInt(c)][top] - howManySee(s, new Card(c, top+1));
				}
				for(int j = 0; j < top; j++){
					numDiscardable += cardsLeftInPlay[mapColourToInt(c)][j] - howManySee(s, new Card(c, j + 1));
				}
				for(int j = 0; j < 5; j ++){
					numTotal += cardsLeftInPlay[mapColourToInt(c)][j] - howManySee(s, new Card(c, j+1));
				}
			}
			else if(knowValues[index][i] > 0){
				int card = knowValues[index][i];
				for(Colour c : Colour.values()){
					if(card == topFw(s,c)+1){
						numPlayable += cardsLeftInPlay[mapColourToInt(c)][card-1] - howManySee(s, new Card(c, card));
					}
					else if(card <= topFw(s,c)){
						numDiscardable += cardsLeftInPlay[mapColourToInt(c)][card -1] - howManySee(s, new Card(c, card));
					}
					numTotal += cardsLeftInPlay[mapColourToInt(c)][card-1] - howManySee(s, new Card(c, card));
				}
			}
			else{
				for(Colour c : Colour.values()){
					int top = topFw(s,c);
					if(top == 5){continue;}
					numPlayable += cardsLeftInPlay[mapColourToInt(c)][top] - howManySee(s, new Card(c, top+1));
					for(int j = 0; j < top; j++){
						numDiscardable += cardsLeftInPlay[mapColourToInt(c)][j] - howManySee(s, new Card(c, j + 1));
					}
					
				}
				numTotal = totalCards;
				for(int p = 0; p < numPlayers; p ++){
					if(p == index){continue;}
					for(int j = 0; j < numCards; j ++){
						if(s.getHand(p)[j] == null){continue;}
						numTotal -= 1;
					}
				}
			}
			
			double playProb = numPlayable/numTotal;
			if(playProb > probCorrectPlay){
				probCorrectPlay = playProb;
				play = i;
			}
			
			double disProb = numDiscardable/numTotal;
			if(disProb > probCorrectDiscard){
				probCorrectDiscard = disProb;
				discard = i;
			}
			
		}
		

		if(probCorrectDiscard >= probCorrectPlay || s.getFuseTokens() < 2){
			knowColours[index][discard] = null;
    		knowValues[index][discard] = 0;
    		theyArrived[index][discard] = s.getOrder();
    		totalCards --;
			
			return new Action(index, toString(),  ActionType.DISCARD, discard);
		}else{
			knowColours[index][play] = null;
    		knowValues[index][play] = 0;
    		theyArrived[index][play] = s.getOrder();
    		totalCards --;
			
			return new Action(index, toString(),  ActionType.PLAY, play);
		}
	
	}


	//helper classes and functions
	
	private int evaluateHint(State s, int p, int hint) {
		// 0:blue,1:red... (int to colour)
		//5:hint 1, 6:hint2, 7:hint3, 8:hint4, 9:hint5
		
		//first check if it will reveal a playable card  worth 15.
		
		//then check if it will save a final in play card worth 5.
		
		//if it will reveal a can discard worth 4
		
		//then check how much information it will reveal worth 1 each.
		
		int score = 0;
		
		score += 15*hintMakesPlayable(s,p,hint);
		score += 4*hintMakesDiscardable(s,p,hint);
		
		
		
		
		boolean[] finals = finalCards(s,p);
		
		for(int i = 0 ; i < numCards; i++){
			if(hint > 4){
				if(knowValues[p][i] == 0 && s.getHand(p)[i].getValue() == (hint-4)){
					score+= 1;
					if(finals[i]){
						score+=5;
					}
				}
			}else{
				if(knowColours[p][i] == null && s.getHand(p)[i].getColour() == mapToColour(hint)){
					score+= 1;
					if(finals[i]){
						score+=5;
					}
				}
			}
		}
		
		return score;
	}
	
	private int hintMakesDiscardable(State s, int p, int hint) {
		int discardBefore = 0;
		int discardAfter = 0;
		
		for(int i = 0; i<numCards; i++){
	    	if(knowColours[p][i] != null && knowValues[p][i] != 0){
	    		if(discardable(s, knowColours[p][i], knowValues[p][i])){
	    			discardBefore++;
	    		}
	    	}
	    	else if(knowValues[p][i] != 0){
	    		if(discardable(s, knowValues[p][i])){
	    			discardBefore++;
	    		}
	    	}
	    	else if(knowColours[p][i] != null){
	    		if(discardable(s, knowColours[p][i])){
	    			discardBefore++;
	    		}
	    	}
	    	else{
	    		//know nothing of the card
	    	}
	      }
		
		int[][] tempV = knowValues.clone();
		Colour[][] tempC = knowColours.clone();
		
		if(hint > 4){
			for(int i = 0; i < numCards; i ++){
				if(s.getHand(p)[i] == null){continue;}
				if(s.getHand(p)[i].getValue() == (hint - 4)){
					tempV[p][i] = hint - 4;
				}
			}
		}else{
			for(int i = 0; i < numCards; i ++){
				if(s.getHand(p)[i] == null){continue;}
				if(s.getHand(p)[i].getColour() == mapToColour(hint)){
					tempC[p][i] = mapToColour(hint);
				}
			}
		}
		
		
		for(int i = 0; i<numCards; i++){
	    	if(knowColours[p][i] != null && knowValues[p][i] != 0){
	    		if(discardable(s, knowColours[p][i], knowValues[p][i])){
	    			discardAfter++;
	    		}
	    	}
	    	else if(knowValues[p][i] != 0){
	    		if(discardable(s, knowValues[p][i])){
	    			discardAfter++;
	    		}
	    	}
	    	else if(knowColours[p][i] != null){
	    		if(discardable(s, knowColours[p][i])){
	    			discardAfter++;
	    		}
	    	}
	    	else{
	    		//know nothing of the card
	    	}
	      }
		
		return discardAfter - discardBefore;
	}


	private int hintMakesPlayable(State s, int p, int hint){
		
		int playableBefore = 0;
		int playableAfter = 0;
		
		for(int i = 0; i<numCards; i++){
	    	if(knowColours[p][i] != null && knowValues[p][i] != 0){
	    		if(playable(s, knowColours[p][i], knowValues[p][i])){
	    			playableBefore++;
	    		}
	    	}
	    	else if(knowValues[p][i] != 0){
	    		if(playable(s, knowValues[p][i])){
	    			playableBefore++;
	    		}
	    	}
	    	else if(knowColours[p][i] != null){
	    		if(playable(s, knowColours[p][i])){
	    			playableBefore++;
	    		}
	    	}
	    	else{
	    		//know nothing of the card
	    	}
	      }
		
		int[][] tempV = knowValues.clone();
		Colour[][] tempC = knowColours.clone();
		
		if(hint > 4){
			for(int i = 0; i < numCards; i ++){
				if(s.getHand(p)[i] == null){continue;}
				if(s.getHand(p)[i].getValue() == (hint - 4)){
					tempV[p][i] = hint - 4;
				}
			}
		}else{
			for(int i = 0; i < numCards; i ++){
				if(s.getHand(p)[i] == null){continue;}
				if(s.getHand(p)[i].getColour() == mapToColour(hint)){
					tempC[p][i] = mapToColour(hint);
				}
			}
		}
		
		
		for(int i = 0; i<numCards; i++){
	    	if(knowColours[p][i] != null && knowValues[p][i] != 0){
	    		if(playable(s, knowColours[p][i], knowValues[p][i])){
	    			playableAfter++;
	    		}
	    	}
	    	else if(knowValues[p][i] != 0){
	    		if(playable(s, knowValues[p][i])){
	    			playableAfter++;
	    		}
	    	}
	    	else if(knowColours[p][i] != null){
	    		if(playable(s, knowColours[p][i])){
	    			playableAfter++;
	    		}
	    	}
	    	else{
	    		//know nothing of the card
	    	}
	      }
		
		return playableAfter - playableBefore;
	}
	
	private boolean[] finalCards(State s, int p) {
		
		if(p == index){return null;}
		
		boolean[] finals = new boolean[numCards];
		for(int i = 0; i < numCards; i++){
			Card c = s.getHand(p)[i];
			if(c == null){continue;}
			int inPlay = cardsLeftInPlay[mapColourToInt(c.getColour())][c.getValue()-1];
			int iSee = howManySee(s, c);
			if(inPlay == 1|| iSee == 1){
				finals[i] = true;
			}else{
				finals[i] = false;
			}
		}
		return finals;
	}
	
	private int topFw (State s, Colour c){
		java.util.Stack<Card> fw = s.getFirework(c);
	    if (fw.size()==5) return 5;
	    else return (fw.size());
	}
	
	private boolean playable(State s, Colour c, int i) {
	    int top = topFw(s,c);
	    return top == i-1;
	}
	
	private boolean playable(State s, int i) {
		boolean[] canPlay = new boolean[5];
		
		for(Colour c: Colour.values()){
			canPlay[mapColourToInt(c)] = (topFw(s,c) == i-1);
		}
		
		boolean playable = true;
		
		for(int j = 0 ; j < 5; j++){
			if(!canPlay[j]){
				int inPlay = cardsLeftInPlay[j][i-1] - howManySee(s, new Card( mapToColour(j), i));
				if(inPlay != 0){playable = false;}
			}
		}
		
		return playable;
	}

	private boolean playable(State s, Colour c) {

		int toPlay;
		
		int top = topFw(s,c);
	    if (top==5) return false;
	    else toPlay = top + 1;
	    
	    
		boolean playable = true;
		
		for(int j = 0 ; j < 5; j++){
			if(j != toPlay){
				int inPlay = cardsLeftInPlay[mapColourToInt(c)][j] - howManySee(s, new Card(c, j+1));
				if(inPlay != 0){playable = false;}
			}
		}
		
		return playable;
	}


	private boolean discardable(State s, Colour c, int i) {
	    int top = topFw(s,c);
	    return top >= i;
	}
	
	private boolean discardable(State s, int i) {
		boolean[] canDiscard = new boolean[5];
		
		for(Colour c: Colour.values()){
			canDiscard[mapColourToInt(c)] = (topFw(s,c) >= i);
		}
		
		boolean discardable = true;
		
		for(int j = 0 ; j < 5; j++){
			if(!canDiscard[j]){
				int inPlay = cardsLeftInPlay[j][i-1] - howManySee(s, new Card( mapToColour(j), i));
				if(inPlay != 0){discardable = false;}
			}
		}
		
		return discardable;
	}

	private boolean discardable(State s, Colour c) {

		int toPlay;
		
		int top = topFw(s,c);
	    if (top==5) return true;
	    else toPlay = top + 1;
	    
	    
		boolean discardable = true;
		
		for(int j = 0 ; j < 5; j++){
			if(j >= toPlay){
				int inPlay = cardsLeftInPlay[mapColourToInt(c)][j] - howManySee(s, new Card(c, j+1));
				if(inPlay != 0){discardable = false;}
			}
		}
		
		return discardable;
	}
	
	
	
	private int howManySee(State s, Card c){
		
		int count = 0;
		for(int i = 0; i < numPlayers; i ++){
			if(i == index){continue;}
			for(int j = 0;  j < numCards; j ++){
				if(s.getHand(i)[j] == null){continue;}
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
	        	 Card c =  t.getPreviousState().getHand(a.getPlayer())[a.getCard()];
	        	 cardsLeftInPlay[mapColourToInt(c.getColour())][c.getValue()-1]--;
	        	 theyArrived[a.getPlayer()][a.getCard()] = t.getOrder();
	        	 totalCards--;
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
