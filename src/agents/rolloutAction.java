package agents;

import hanabAI.Action;
import hanabAI.ActionType;
import hanabAI.Card;
import hanabAI.Colour;
import hanabAI.IllegalActionException;

public class rolloutAction{

	public static Action doAction(MyState s) {

		s.index = s.nextPlayer;
	    
		int maxValue = 0;
		int bestHint = -1;
		int hintPlayer = (s.index+1)%s.numPlayers;
		
		for(int i = 0; i < 10; i ++){
			int value = evaluateHint(s, hintPlayer, i);
			if(value > maxValue){
				maxValue = value;
				bestHint = i;
			}
		}

		if(s.numPlayers > 2){
			for(int i = 0; i < 10; i ++){
				int value = evaluateHint(s, hintPlayer, i);
				if(value - 6 > maxValue){
					maxValue = value - 6; // prioritise next player
					bestHint = i;
					hintPlayer = (s.index+2)%s.numPlayers;
				}
			}
		}
	    
	    try{
	      Action a = playKnown(s);
	      //if(a== null) a = playLastHint(s);
	      if(maxValue >= 14 || s.hints > 5){
	    	  if(a==null) a = hint(s, hintPlayer, bestHint);
	    	  if(a==null) a = discardKnown(s);
	    	  
	      }else{
		      if(a==null) a = discardKnown(s);
		      if(maxValue > 5){
		    	  if(a==null) a = hint(s, hintPlayer, bestHint);
		      }
	      }
	      //if(a==null) a = hint(s, hintPlayer, bestHint);
	      if(a==null) a = discardOldest(s);
	      if(a==null) a = guess(s);


	      return a;
	    }
	    catch(IllegalActionException e){
	      e.printStackTrace();
	      throw new RuntimeException("Something has gone very wrong");
	    }
	}
	


	private static Action playKnown(MyState s) throws IllegalActionException{
	    for(int i = 0; i<s.numCards; i++){
	    	if(s.knownColours[s.index][i] != null && s.knownValues[s.index][i] != 0){
	    		if(playable(s, s.knownColours[s.index][i], s.knownValues[s.index][i])){

	    	        return new Action(s.index, s.toString(), ActionType.PLAY,i);
	    		}
	    	}
	    	else if(s.knownValues[s.index][i] != 0){
	    		if(playable(s, s.knownValues[s.index][i])){

	    	        return new Action(s.index, s.toString(), ActionType.PLAY,i);
	    		}
	    	}
	    	else if(s.knownColours[s.index][i] != null){
	    		if(playable(s, s.knownColours[s.index][i])){

	    	        return new Action(s.index, s.toString(), ActionType.PLAY,i);
	    		}
	    	}
	    	else{
	    		//know nothing of the card
	    	}
	      }
	      return null;
	}
/*	private Action playLastHint(State s) throws IllegalActionException {
		if(s.getFuseTokens() < 3 || lastHint == null){return null;}
		
		int card = 0;
		int total = 0;
		
		for(int i = 0; i< s.numCards; i ++){
			if(lastHint.getHintedCards()[i]){
				card = i;
				total++;
			}
		}
		if(total > 1){return null;}
		
		s.knownColours[s.index][card] = null;
		s.knownValues[s.index][card] = 0;
		s.theyArrived[s.index][card] = s.getOrder();
		totalCards --;
        return new Action(s.index, s.toString(), ActionType.PLAY, card);
	}*/
	private static Action discardKnown(MyState s) throws IllegalActionException{
	    if (s.hints != 8) {
	        for(int i = 0; i<s.numCards; i++){
	        	if(s.knownColours[s.index][i]!=null && s.knownValues[s.index][i]>0){
	        		if(discardable(s, s.knownColours[s.index][i], s.knownValues[s.index][i])){

		        		return new Action(s.index, s.toString(), ActionType.DISCARD,i);
	        		}

	        	}
				else if(s.knownValues[s.index][i] != 0){
					if(discardable(s, s.knownValues[s.index][i])){

				        return new Action(s.index, s.toString(), ActionType.DISCARD,i);
					}
				}
				else if(s.knownColours[s.index][i] != null){
					if(discardable(s, s.knownColours[s.index][i])){

				        return new Action(s.index, s.toString(), ActionType.DISCARD,i);
					}
				}
				else{
					//know nothing of the card
				}
	        }
	      }
	      return null;
	}

	private static Action hint(MyState s, int p, int hint) throws IllegalActionException{
		
		if(s.hints == 0 || hint == -1){
			return null;
		}
		
		boolean[] match = new boolean[s.numCards];
		
		if(hint > 4){
			
			for(int i = 0 ; i < s.numCards; i++){
				Card c = s.hands[p][i];
				if(c == null){continue;}
				if(c.getValue() == hint - 4){
					match[i] = true;
				}
			}
			
			return new Action(s.index, s.toString(), ActionType.HINT_VALUE, p, match, hint - 4);
		}else{
			
			for(int i = 0 ; i < s.numCards; i++){
				Card c = s.hands[p][i];
				if(c == null){continue;}
				if(c.getColour() == mapToColour(hint)){
					match[i] = true;
				}
			}
			
			return new Action(s.index, s.toString(), ActionType.HINT_COLOUR,p, match, mapToColour(hint));
		}
	}
	

	private static Action discardOldest(MyState s) throws IllegalActionException{
	
		
		if(s.hints == 8){return null;}
		int[] age = new int[s.numCards];
		for(int i = 0; i < s.numCards; i ++){
			age[i] = i;
		}
		for(int i = 1; i < s.numCards; i ++){
			
			int key = s.theyArrived[s.index][i];
			int j = i-1;
			
			while(j>=0 && s.theyArrived[s.index][age[j]]> key){
				age[j+1] = age[j];
				j = j -1;
			}
			age[j+1] = i;
		}
		
		int oldest = -1;
		//find oldest un-hinted card
		for(int i = 0; i < s.numCards; i ++){
			if(s.knownValues[s.index][age[i]] == 0 || s.knownColours[s.index][age[i]] == null){
				oldest = age[i];
			}
		}
		
		if(oldest == -1){
			return null;
		}else{

			return new Action(s.index, s.toString(), ActionType.DISCARD, oldest);
		}

	}

	private static Action guess(MyState s) throws IllegalActionException{
		double probCorrectPlay = 0;
		int play = 0;
		
		double probCorrectDiscard = 0;
		int discard = 0;
		
		int numPlayable = 0;
		int numDiscardable = 0;
		int numTotal = 0;
		
		
		for(int i = 0; i < s.numCards; i++){
			if(s.knownColours[s.index][i] != null && s.knownValues[s.index][i] > 0){
				numPlayable = 0;
				numDiscardable = 0;
				//since we wouldve played it already
			}
			else if(s.knownColours[s.index][i] != null){
				Colour c = s.knownColours[s.index][i];
				int top = topFw(s,c);
				if(top < 5){
					numPlayable = s.cardsLeftInDeck[mapColourToInt(c)][top];
				}
				for(int j = 0; j < top; j++){
					numDiscardable += s.cardsLeftInDeck[mapColourToInt(c)][j];
				}
				for(int j = 0; j < 5; j ++){
					numTotal += s.cardsLeftInDeck[mapColourToInt(c)][j];
				}
			}
			else if(s.knownValues[s.index][i] > 0){
				int card = s.knownValues[s.index][i];
				for(Colour c : Colour.values()){
					if(card == topFw(s,c)+1){
						numPlayable += s.cardsLeftInDeck[mapColourToInt(c)][card-1];
					}
					else if(card <= topFw(s,c)){
						numDiscardable += s.cardsLeftInDeck[mapColourToInt(c)][card -1];
					}
					numTotal += s.cardsLeftInDeck[mapColourToInt(c)][card-1];
				}
			}
			else{
				for(Colour c : Colour.values()){
					int top = topFw(s,c);
					if(top == 5){continue;}
					numPlayable += s.cardsLeftInDeck[mapColourToInt(c)][top];
					for(int j = 0; j < top; j++){
						numDiscardable += s.cardsLeftInDeck[mapColourToInt(c)][j];
					}
					
				}
				numTotal = s.totalCards;
				for(int p = 0; p < s.numPlayers; p ++){
					if(p == s.index){continue;}
					for(int j = 0; j < s.numCards; j ++){
						if(s.hands[p][j] == null){continue;}
						numTotal -= 1;
					}
				}
			}
			
			if(numPlayable != 0){
				double playProb = numPlayable/numTotal;
				if(playProb > probCorrectPlay){
					probCorrectPlay = playProb;
					play = i;
				}
			}
			
			if(numDiscardable != 0){
				double disProb = numDiscardable/numTotal;
				if(disProb > probCorrectDiscard){
					probCorrectDiscard = disProb;
					discard = i;
				}
			}
			
		}
		
		
		if(probCorrectDiscard >= probCorrectPlay || s.fuse < 2){

			
			return new Action(s.index, s.toString(),  ActionType.DISCARD, discard);
		}else{

			
			return new Action(s.index, s.toString(),  ActionType.PLAY, play);
		}
	
	}


	//helper classes and functions
	
	private static int evaluateHint(MyState s, int p, int hint) {
		// 0:blue,1:red... (int to colour)
		//5:hint 1, 6:hint2, 7:hint3, 8:hint4, 9:hint5
		
		//first check if it will reveal a playable card  worth 15.
		
		//then check if it will save a final in play card worth 5.
		
		//if it will reveal a can discard worth 4
		
		//then check how much information it will reveal worth 1 each.
		
		int score = 0;
		
		score += 15*hintMakesPlayable(s,p,hint);
		score += 6*hintMakesDiscardable(s,p,hint);
		
		//if the card is playable and hasnt been hinted
		
		
		
		boolean[] finals = finalCards(s,p);
		
		for(int i = 0 ; i < s.numCards; i++){
			Card c = s.hands[p][i];
			if(c == null){continue;}
			if(hint > 4){
				if(s.knownValues[p][i] == 0 && c.getValue() == (hint-4)){
					score+= 1;
					if(playable(s, c.getColour(), c.getValue())){
						score += 8;
					}

					if(finals[i]){
						score+=3;
					}
				}
			}else{
				if(s.knownColours[p][i] == null && c.getColour() == mapToColour(hint)){
					score+= 1;
					if(playable(s, c.getColour(), c.getValue())){
						score += 8; //could add some convention priority later
					}
					if(finals[i]){
						score+=3;
					}
				}
			}
		}
		
		return score;
	}
	
	private static int hintMakesDiscardable(MyState s, int p, int hint) {
		int discardBefore = 0;
		int discardAfter = 0;
		
		for(int i = 0; i<s.numCards; i++){
	    	if(s.knownColours[p][i] != null && s.knownValues[p][i] != 0){
	    		if(discardable(s, s.knownColours[p][i], s.knownValues[p][i])){
	    			discardBefore++;
	    		}
	    	}
	    	else if(s.knownValues[p][i] != 0){
	    		if(discardable(s, s.knownValues[p][i])){
	    			discardBefore++;
	    		}
	    	}
	    	else if(s.knownColours[p][i] != null){
	    		if(discardable(s, s.knownColours[p][i])){
	    			discardBefore++;
	    		}
	    	}
	    	else{
	    		//know nothing of the card
	    	}
	      }
		
		int[] tempV = s.knownValues[p].clone();
		Colour[] tempC = s.knownColours[p].clone();
		
		if(hint > 4){
			for(int i = 0; i < s.numCards; i ++){
				if(s.hands[p][i] == null){continue;}
				if(s.hands[p][i].getValue() == (hint - 4)){
					tempV[i] = hint - 4;
				}
			}
		}else{
			for(int i = 0; i < s.numCards; i ++){
				if(s.hands[p][i] == null){continue;}
				if(s.hands[p][i].getColour() == mapToColour(hint)){
					tempC[i] = mapToColour(hint);
				}
			}
		}
		
		
		for(int i = 0; i<s.numCards; i++){
	    	if(tempC[i] != null && tempV[i] != 0){
	    		if(discardable(s, tempC[i], tempV[i])){
	    			discardAfter++;
	    		}
	    	}
	    	else if(tempV[i] != 0){
	    		if(discardable(s, tempV[i])){
	    			discardAfter++;
	    		}
	    	}
	    	else if(tempC[i] != null){
	    		if(discardable(s, tempC[i])){
	    			discardAfter++;
	    		}
	    	}
	    	else{
	    		//know nothing of the card
	    	}
	      }
		
		return discardAfter - discardBefore;
	}


	private static int hintMakesPlayable(MyState s, int p, int hint){
		
		
		int playableBefore = 0;
		int playableAfter = 0;
		
		for(int i = 0; i<s.numCards; i++){
	    	if(s.knownColours[p][i] != null && s.knownValues[p][i] != 0){
	    		if(playable(s, s.knownColours[p][i], s.knownValues[p][i])){
	    			playableBefore++;
	    		}
	    	}
	    	else if(s.knownValues[p][i] != 0){
	    		if(playable(s, s.knownValues[p][i])){
	    			playableBefore++;
	    		}
	    	}
	    	else if(s.knownColours[p][i] != null){
	    		if(playable(s, s.knownColours[p][i])){
	    			playableBefore++;
	    		}
	    	}
	    	else{
	    		//know nothing of the card
	    	}
	      }
		
		int[] tempV = s.knownValues[p].clone();

		Colour[] tempC = s.knownColours[p].clone();
		
		if(hint > 4){
			for(int i = 0; i < s.numCards; i ++){
				if(s.hands[p][i] == null){continue;}
				if(s.hands[p][i].getValue() == (hint - 4)){
					tempV[i] = hint - 4;
				}
			}
		}else{
			for(int i = 0; i < s.numCards; i ++){
				if(s.hands[p][i] == null){continue;}
				if(s.hands[p][i].getColour() == mapToColour(hint)){
					tempC[i] = mapToColour(hint);
				}
			}
		}
		
		
		for(int i = 0; i<s.numCards; i++){
	    	if(tempC[i] != null && tempV[i] != 0){
	    		if(playable(s, tempC[i], tempV[i])){
	    			playableAfter++;
	    		}
	    	}
	    	else if(tempV[i] != 0){
	    		if(playable(s, tempV[i])){
	    			playableAfter++;
	    		}
	    	}
	    	else if(tempC[i] != null){
	    		if(playable(s, tempC[i])){
	    			playableAfter++;
	    		}
	    	}
	    	else{
	    		//know nothing of the card
	    	}
	      }
		
		return playableAfter - playableBefore;
	}
	
	private static boolean[] finalCards(MyState s, int p) {
		
		if(p == s.index){return null;}
		
		int[][] cardsInPlay = new int[5][5];
		
	    for(int i = 0; i < s.numPlayers; i ++){
	    	for(int j = 0; j < s.numCards; j ++){
	    		s.theyArrived[i][j] = 1;
	    		if(i != s.index){
	    			Card c = s.hands[i][j];
	    			if(c == null){continue;}
	    			cardsInPlay[mapColourToInt(c.getColour())][c.getValue() - 1]++;
	    		}
	    	}
	    }
				
		boolean[] finals = new boolean[s.numCards];
		for(int i = 0; i < s.numCards; i++){
			Card c = s.hands[p][i];
			if(c == null){continue;}
			int inDeck = s.cardsLeftInDeck[mapColourToInt(c.getColour())][c.getValue()-1];
			int inPlay = cardsInPlay[mapColourToInt(c.getColour())][c.getValue()-1];
			if(inDeck == 0 && inPlay == 1){
				finals[i] = true;
			}else{
				finals[i] = false;
			}
		}
		return finals;
	}
	
	private static int topFw (MyState s, Colour c){
		java.util.Stack<Card> fw = s.fireworks.get(c);
	    if (fw.size()==5) return 5;
	    else return (fw.size());
	}
	
	private static boolean playable(MyState s, Colour c, int value) {
	    int top = topFw(s,c);
	    return top == value-1;
	}
	
	private static boolean playable(MyState s, int value) {
		boolean[] canPlay = new boolean[5];
		
		for(Colour c: Colour.values()){
			canPlay[mapColourToInt(c)] = (topFw(s,c) == value-1);
		}
		
		boolean playable = true;
		
		for(int j = 0 ; j < 5; j++){
			if(!canPlay[j]){
				int inPlay = s.cardsLeftInDeck[j][value-1];
				if(inPlay != 0){playable = false;}
			}
		}
		
		return playable;
	}

	private static boolean playable(MyState s, Colour c) {

		int toPlay;
		
		int top = topFw(s,c);
	    if (top==5) return false;
	    else toPlay = top;
	    
	    
		boolean playable = true;
		
		for(int j = 0 ; j < 5; j++){
			if(j != toPlay){
				int inPlay = s.cardsLeftInDeck[mapColourToInt(c)][j];
				if(inPlay != 0){playable = false;}
			}
		}
		
		return playable;
	}


	private static boolean discardable(MyState s, Colour c, int value) {
	    int top = topFw(s,c);
	    return top >= value;
	}
	
	private static boolean discardable(MyState s, int value) {
		boolean[] canDiscard = new boolean[5];
		
		for(Colour c: Colour.values()){
			canDiscard[mapColourToInt(c)] = (topFw(s,c) >= value);
		}
		
		boolean discardable = true;
		
		for(int j = 0 ; j < 5; j++){
			if(!canDiscard[j]){
				int inPlay = s.cardsLeftInDeck[j][value-1];
				if(inPlay != 0){discardable = false;}
			}
		}
		
		return discardable;
	}

	private static boolean discardable(MyState s, Colour c) {

		int toPlay;
		
		int top = topFw(s,c);
	    if (top==5) return true;
	    else toPlay = top;
	    
	    
		boolean discardable = true;
		
		for(int j = 0 ; j < 5; j++){
			if(j >= toPlay){
				int inPlay = s.cardsLeftInDeck[mapColourToInt(c)][j];
				if(inPlay != 0){discardable = false;}
			}
		}
		
		return discardable;
	}
	
	


	static int mapColourToInt(Colour c){
	    switch(c){
	      case BLUE: return 0;
	      case RED: return 1;
	      case GREEN: return 2;
	      case WHITE: return 3;
	      case YELLOW: return 4;
	      default: return -1;
	    }
	}
	
	private static Colour mapToColour(int c) {
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
