package fuckenRollout;

import java.util.*;



public class MyState implements Cloneable {
	private int totalCards = 50;

	private Colour[][] knowColours;
	private int[][] knowValues;
	private int[][] theyArrived;

	private int[][] cardsLeftInDeck;

	private java.util.Stack<Card> deck;
	// int numLeft;

	private boolean firstAction = true;
	private int numPlayers;
	private int numCards;
	private int index;

	/** The name of each of the players in the game **/
	private String[] players;
	/** The stack of cards that have bee discarded, or incorrectly played **/
	private Stack<Card> discards;
	/** For each colour, the cards making up that firework so far **/
	private Map<Colour, Stack<Card>> fireworks;
	/** The hand of each player **/
	private Card[][] hands;
	/** The order of this state in the game **/
	private int order = 0;
	/** The number of hints remaining **/
	private int hints = 0;
	/** The number of fuse tokens left **/
	private int fuse = 0;
	/**
	 * The observer of this state. This allows hidden information to be redacted
	 **/
	private int observer = -1;
	/**
	 * The previous State of the game, so that all states are accessible back to the
	 * first state (with a null previous state)
	 **/
	//private MyState previousState;
	/**
	 * A list of all moves made so far in the game, in the order they were played
	 **/
	//private Action previousAction;
	/** The index of the next player to move **/
	private int nextPlayer = -1;
	/** The fnal play of the game (for when the deck runs out) **/
	private int finalAction = -1;
	private Action lastHint = null;
	
	public MyState(State s, int[][] cardsLeftInDeck, int[][] knownValues, Colour[][] knownColours, int[][] theyArrived) {
		this.players = s.getPlayers();
		this.discards = s.getDiscards();
		// hands and fireworks ??
		this.fireworks = new HashMap<Colour, Stack<Card>>();
		for (Colour c : Colour.values()) {
			this.fireworks.put(c, s.getFirework(c));
		}
		this.order = s.getOrder();
		this.hints = s.getHintTokens();
		this.fuse = s.getFuseTokens();
		this.observer = s.getObserver();
		this.finalAction = s.getFinalActionIndex();
		this.hands = new Card[players.length][players.length > 3 ? 4 : 5];
		for (int i = 0; i < this.players.length; i++) {
			if (i != this.nextPlayer)
				this.hands[i] = s.getHand(i);
		}
		this.knowColours = knownColours;
		this.knowValues = knownValues;
		this.cardsLeftInDeck = cardsLeftInDeck;
		this.nextPlayer = s.getNextPlayer();
		this.numPlayers = s.getPlayers().length;
		this.theyArrived = theyArrived;
		this.numCards = (this.players.length > 3 ? 4 : 5);
		this.deck = new Stack<Card>();
		//dealMyCards();
	}
	
	private void dealMyCards(){
 	    for(int i = 0; i < numCards; i ++){
 	      if(knowValues[nextPlayer][i]> 0 && knowColours[nextPlayer][i] != null){
 	        cardsLeftInDeck[mapColourToInt(knowColours[nextPlayer][i])][knowValues[nextPlayer][i]-1]--;
 	        if(cardsLeftInDeck[mapColourToInt(knowColours[nextPlayer][i])][knowValues[nextPlayer][i]-1] < 0){
 	        	System.out.println("help");
 	        }
 	        hands[nextPlayer][i] = new Card(knowColours[nextPlayer][i], knowValues[nextPlayer][i]);
 	      }
 	    }
 	   for(int i = 0; i < numCards; i ++){
 	      
 		   if(hands[nextPlayer][i] != null){continue;}
 		   if(knowValues[nextPlayer][i]> 0 ){
 	      	int total = 0;
 	        for(Colour c: Colour.values()){
 	          total += cardsLeftInDeck[mapColourToInt(c)][knowValues[nextPlayer][i]-1];
 	        }
 	        if(total == 0){continue;}//can improve later with hungarian
 	        int counter = -1;
 	        java.util.Random r = new java.util.Random();

 	 	      int pick = r.nextInt(total) + 1;
 	        while(pick > 0){
 	          pick -= cardsLeftInDeck[++counter][knowValues[nextPlayer][i]-1];
 	        }
 	        cardsLeftInDeck[counter][knowValues[nextPlayer][i]-1]--;
 	        hands[nextPlayer][i] = new Card(mapToColour(counter), knowValues[nextPlayer][i]);
 	      }
 	   }
 	   for(int i = 0; i < numCards; i ++){
 		  if(hands[nextPlayer][i] != null){continue;}
 	       if(knowColours[nextPlayer][i] != null){
 	    	   int total = 0;
 	    	   for(int j = 0; j < 5; j ++){
 	    		   total += cardsLeftInDeck[mapColourToInt(knowColours[nextPlayer][i])][j];
 	    	   }
 	    	   if(total == 0){continue;}
 	    	   int counter = -1;
 	    	   java.util.Random r = new java.util.Random();

 	 	       int pick = r.nextInt(total) + 1;
 	 	       while(pick > 0){
 	 	    	   pick -= cardsLeftInDeck[mapColourToInt(knowColours[nextPlayer][i])][++counter];
 	 	       }
 	 	       cardsLeftInDeck[mapColourToInt(knowColours[nextPlayer][i])][counter]--;
 	 	       hands[nextPlayer][i] = new Card(knowColours[nextPlayer][i], counter+1);
 	      }
 	    }
 	    
 	    makeDeck();
 	    
 	    for(int i = 0; i < numCards; i ++){
 	      if(hands[nextPlayer][i] == null){
 	    	  if(!deck.isEmpty()){
 	    		  hands[nextPlayer][i] = deck.pop();
 	    	  }
 	      }
 	    }
 	  }

	private void makeDeck() {
		int num = 0;
		java.util.LinkedList<Card> tempDeck = new java.util.LinkedList<Card>();
		for (int i = 0; i < 5; i++) {
			for (int j = 0; j < 5; j++) {
				for (int k = 0; k < cardsLeftInDeck[i][j]; k++) {
					tempDeck.add(new Card(mapToColour(i), j + 1));
					num++;
				}
			}
		}
		java.util.Random r = new java.util.Random();
		while (!tempDeck.isEmpty()) {
			deck.push(tempDeck.remove(r.nextInt(num--)));
		}
	}

	
	public int getScore() {
		int score = 0;
		if (fuse == 0)
			return 0;
		for (Colour c : Colour.values())
			if (!fireworks.get(c).isEmpty())
				score += fireworks.get(c).peek().getValue();
		return score;
	}
	
	public int getNextPlayer() {return (gameOver()?-1:nextPlayer);}
	
	public boolean gameOver() {
		return (order == finalAction || fuse == 0 || getScore() == 25);
	}



	public Action doAction(MyState s) {

		index = nextPlayer;
	    firstAction = false;
	    
		int maxValue = 0;
		int bestHint = -1;
		int hintPlayer = (index+1)%numPlayers;
		
		for(int i = 0; i < 10; i ++){
			int value = evaluateHint(s, hintPlayer, i);
			if(value > maxValue){
				maxValue = value;
				bestHint = i;
			}
		}

		if(numPlayers > 2){
			for(int i = 0; i < 10; i ++){
				int value = evaluateHint(s, hintPlayer, i);
				if(value - 6 > maxValue){
					maxValue = value - 6; // prioritise next player
					bestHint = i;
					hintPlayer = (index+2)%numPlayers;
				}
			}
		}
	    
	    try{
	      Action a = playKnown(s);
	      if(a== null) a = playLastHint(s);
	      if(maxValue >= 14 || s.hints > 5){
	    	  if(a==null) a = hint(s, hintPlayer, bestHint);
	    	  if(a==null) a = discardKnown(s);
	    	  
	      }else{
		      if(a==null) a = discardKnown(s);
		      if(maxValue > 5){
		    	  if(a==null) a = hint(s, hintPlayer, bestHint);
		      }
	      }


	      if(s.hints > 4){
	    	  if(a==null) a = hint(s, hintPlayer, bestHint);
	      }
	      if(a==null) a = discardDuplicate(s);
	      if(a==null) a = discardOldest(s);
	      if(a==null) a = guess(s);

	      if(s.finalAction == s.order){
	    	  if(s.getScore() < 5){
	    		  System.out.println("idiot ai");
	    	  }
	      }
	      return a;
	    }
	    catch(IllegalActionException e){
	      e.printStackTrace();
	      throw new RuntimeException("Something has gone very wrong");
	    }
	}
	
	private Action discardDuplicate(MyState s) throws IllegalActionException {
		if(s.hints > 3){return null;}
		int[] discards = new int[numCards];
		for(int i = 0; i < numCards; i ++){
			int value = knowValues[index][i];
			Colour colour = knowColours[index][i];
			if(value > 0 && colour != null){
				if(cardsLeftInDeck[mapColourToInt(colour)][value-1] > 1){
					return new Action(index, toString(), ActionType.DISCARD,i);
				}
			}
			else if(value > 0){
				boolean canDiscard = true;
				for(Colour c: Colour.values()){
					if(topFw(s,c) <= value && cardsLeftInDeck[mapColourToInt(c)][value-1] == 1){
						canDiscard = false;
					}
				}
				if(canDiscard){
					return new Action(index, toString(), ActionType.DISCARD,i);
				}
			}else if(colour != null){
				boolean canDiscard = true;
				for(int v = 0; v < 5; v++){
					if(topFw(s,colour) < v && cardsLeftInDeck[mapColourToInt(colour)][v] == 1){
						canDiscard = false;
					}
				}
				if(canDiscard){
					return new Action(index, toString(), ActionType.DISCARD,i);
				}
			}
		}
		
		return null;
	}


	private Action playKnown(MyState s) throws IllegalActionException{
	    for(int i = 0; i<numCards; i++){
	    	if(knowColours[index][i] != null && knowValues[index][i] != 0){
	    		if(playable(s, knowColours[index][i], knowValues[index][i])){

	    	        return new Action(index, toString(), ActionType.PLAY,i);
	    		}
	    	}
	    	else if(knowValues[index][i] != 0){
	    		if(playable(s, knowValues[index][i])){

	    	        return new Action(index, toString(), ActionType.PLAY,i);
	    		}
	    	}
	    	else if(knowColours[index][i] != null){
	    		if(playable(s, knowColours[index][i])){

	    	        return new Action(index, toString(), ActionType.PLAY,i);
	    		}
	    	}
	    	else{
	    		//know nothing of the card
	    	}
	      }
	      return null;
	}
	private Action playLastHint(MyState s) throws IllegalActionException {
		
		//dont risk it if we have no fuses.
		if(s.fuse < 2 || s.lastHint == null){return null;}
		
		
		//make sure that the hint is alteast playable somewhere
		
		if(lastHint.getType() == ActionType.HINT_COLOUR){
			//if colour is already stacked or atleast 1 playable card exists
			Colour hintedColour = lastHint.getColour();
			int top = topFw(s, hintedColour);
			if(top == 5||cardsLeftInDeck[mapColourToInt(hintedColour)][top] == 0){return null;}
		}else{
			boolean matchTop = false;
			for(Colour c: Colour.values()){
				if(topFw(s, c) == lastHint.getValue()-1){matchTop = true;}
			}
			//if no value has no playable moves
			if(!matchTop){return null;}
		}
		
		//chec that the hint highlights 1 card only, then assume we can play it
		int card = 0;
		int total = 0;
		
		for(int i = 0; i< numCards; i ++){
			if(lastHint.getHintedCards()[i]){
				card = i;
				total++;
			}
		}
		if(total > 1){return null;}
		
        return new Action(index, toString(), ActionType.PLAY, card);
	}
	private Action discardKnown(MyState s) throws IllegalActionException{
	    if (s.hints != 8) {
	        for(int i = 0; i<numCards; i++){
	        	if(knowColours[index][i]!=null && knowValues[index][i]>0){
	        		if(discardable(s, knowColours[index][i], knowValues[index][i])){

		        		return new Action(index, toString(), ActionType.DISCARD,i);
	        		}

	        	}
				else if(knowValues[index][i] != 0){
					if(discardable(s, knowValues[index][i])){

				        return new Action(index, toString(), ActionType.DISCARD,i);
					}
				}
				else if(knowColours[index][i] != null){
					if(discardable(s, knowColours[index][i])){

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

	private Action hint(MyState s, int p, int hint) throws IllegalActionException{
		
		if(s.hints == 0 || hint == -1){
			return null;
		}
		
		boolean[] match = new boolean[numCards];
		
		if(hint > 4){
			
			for(int i = 0 ; i < numCards; i++){
				Card c = s.hands[p][i];
				if(c == null){continue;}
				if(c.getValue() == hint - 4){
					match[i] = true;
					//knowValues[p][i] = hint -4;
				}
			}
			
			return new Action(index, toString(), ActionType.HINT_VALUE, p, match, hint - 4);
		}else{
			
			for(int i = 0 ; i < numCards; i++){
				Card c = s.hands[p][i];
				if(c == null){continue;}
				if(c.getColour() == mapToColour(hint)){
					match[i] = true;
					//knowColours[p][i] = mapToColour(hint);
				}
			}
			
			return new Action(index, toString(), ActionType.HINT_COLOUR,p, match, mapToColour(hint));
		}
	}
	

	private Action discardOldest(MyState s) throws IllegalActionException{
	
		
		if(s.hints == 8){return null;}
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
			if(knowValues[index][age[i]] == 0 && knowColours[index][age[i]] == null){
				oldest = age[i];
			}
		}
		
		if(oldest == -1){
			return null;
		}else{

			return new Action(index, toString(), ActionType.DISCARD, oldest);
		}

	}

	private Action guess(MyState s) throws IllegalActionException{
		double probCorrectPlay = 0;
		int play = 0;
		
		double probCorrectDiscard = 0;
		int discard = 0;
		
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
					numPlayable = cardsLeftInDeck[mapColourToInt(c)][top];
				}
				for(int j = 0; j < top; j++){
					numDiscardable += cardsLeftInDeck[mapColourToInt(c)][j];
				}
				for(int j = 0; j < 5; j ++){
					numTotal += cardsLeftInDeck[mapColourToInt(c)][j];
				}
			}
			else if(knowValues[index][i] > 0){
				int card = knowValues[index][i];
				for(Colour c : Colour.values()){
					if(card == topFw(s,c)+1){
						numPlayable += cardsLeftInDeck[mapColourToInt(c)][card-1];
					}
					else if(card <= topFw(s,c)){
						numDiscardable += cardsLeftInDeck[mapColourToInt(c)][card -1];
					}
					numTotal += cardsLeftInDeck[mapColourToInt(c)][card-1];
				}
			}
			else{
				for(Colour c : Colour.values()){
					int top = topFw(s,c);
					if(top == 5){continue;}
					numPlayable += cardsLeftInDeck[mapColourToInt(c)][top];
					for(int j = 0; j < top; j++){
						numDiscardable += cardsLeftInDeck[mapColourToInt(c)][j];
					}
					
				}
				numTotal = totalCards;
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

			
			return new Action(index, toString(),  ActionType.DISCARD, discard);
		}else{

			
			return new Action(index, toString(),  ActionType.PLAY, play);
		}
	
	}


	//helper classes and functions
	
	private int evaluateHint(MyState s, int p, int hint) {
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
		
		for(int i = 0 ; i < numCards; i++){
			Card c = s.hands[p][i];
			if(c == null){continue;}
			if(hint > 4){
				if(knowValues[p][i] == 0 && c.getValue() == (hint-4)){
					score+= 1;
					if(playable(s, c.getColour(), c.getValue())){
						score += 8;
					}

					if(finals[i]){
						score+=3;
					}
				}
			}else{
				if(knowColours[p][i] == null && c.getColour() == mapToColour(hint)){
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
	
	private int hintMakesDiscardable(MyState s, int p, int hint) {
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
		
		int[] tempV = knowValues[p].clone();
		Colour[] tempC = knowColours[p].clone();
		
		if(hint > 4){
			for(int i = 0; i < numCards; i ++){
				if(s.hands[p][i] == null){continue;}
				if(s.hands[p][i].getValue() == (hint - 4)){
					tempV[i] = hint - 4;
				}
			}
		}else{
			for(int i = 0; i < numCards; i ++){
				if(s.hands[p][i] == null){continue;}
				if(s.hands[p][i].getColour() == mapToColour(hint)){
					tempC[i] = mapToColour(hint);
				}
			}
		}
		
		
		for(int i = 0; i<numCards; i++){
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


	private int hintMakesPlayable(MyState s, int p, int hint){
		
		
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
		
		int[] tempV = knowValues[p].clone();

		Colour[] tempC = knowColours[p].clone();
		
		if(hint > 4){
			for(int i = 0; i < numCards; i ++){
				if(s.hands[p][i] == null){continue;}
				if(s.hands[p][i].getValue() == (hint - 4)){
					tempV[i] = hint - 4;
				}
			}
		}else{
			for(int i = 0; i < numCards; i ++){
				if(s.hands[p][i] == null){continue;}
				if(s.hands[p][i].getColour() == mapToColour(hint)){
					tempC[i] = mapToColour(hint);
				}
			}
		}
		
		
		for(int i = 0; i<numCards; i++){
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
	
	private boolean[] finalCards(MyState s, int p) {
		
		if(p == index){return null;}
		
		int[][] cardsInPlay = new int[5][5];
		
	    for(int i = 0; i < numPlayers; i ++){
	    	for(int j = 0; j < numCards; j ++){
	    		//theyArrived[i][j] = 1;
	    		if(i != index){
	    			Card c = s.hands[i][j];
	    			if(c == null){continue;}
	    			cardsInPlay[mapColourToInt(c.getColour())][c.getValue() - 1]++;
	    		}
	    	}
	    }
				
		boolean[] finals = new boolean[numCards];
		for(int i = 0; i < numCards; i++){
			Card c = s.hands[p][i];
			if(c == null){continue;}
			int inDeck = cardsLeftInDeck[mapColourToInt(c.getColour())][c.getValue()-1];
			int inPlay = cardsInPlay[mapColourToInt(c.getColour())][c.getValue()-1];
			if(inDeck == 0 && inPlay == 1){
				finals[i] = true;
			}else{
				finals[i] = false;
			}
		}
		return finals;
	}
	
	private int topFw (MyState s, Colour c){
		java.util.Stack<Card> fw = s.fireworks.get(c);
	    if (fw.size()==5) return 5;
	    else return (fw.size());
	}
	
	private boolean playable(MyState s, Colour c, int value) {
	    int top = topFw(s,c);
	    return top == value-1;
	}
	
	private boolean playable(MyState s, int value) {
		boolean[] canPlay = new boolean[5];
		
		for(Colour c: Colour.values()){
			canPlay[mapColourToInt(c)] = (topFw(s,c) == value-1);
		}
		
		boolean playable = true;
		
		for(int j = 0 ; j < 5; j++){
			if(!canPlay[j]){
				int inPlay = cardsLeftInDeck[j][value-1];
				if(inPlay != 0){playable = false;}
			}
		}
		
		return playable;
	}

	private boolean playable(MyState s, Colour c) {

		int toPlay;
		
		int top = topFw(s,c);
	    if (top==5) return false;
	    else toPlay = top;
	    
	    
		boolean playable = true;
		
		for(int j = 0 ; j < 5; j++){
			if(j != toPlay){
				int inPlay = cardsLeftInDeck[mapColourToInt(c)][j];
				if(inPlay != 0){playable = false;}
			}
		}
		
		return playable;
	}


	private boolean discardable(MyState s, Colour c, int value) {
	    int top = topFw(s,c);
	    return top >= value;
	}
	
	private boolean discardable(MyState s, int value) {
		boolean[] canDiscard = new boolean[5];
		
		for(Colour c: Colour.values()){
			canDiscard[mapColourToInt(c)] = (topFw(s,c) >= value);
		}
		
		boolean discardable = true;
		
		for(int j = 0 ; j < 5; j++){
			if(!canDiscard[j]){
				int inPlay = cardsLeftInDeck[j][value-1];
				if(inPlay != 0){discardable = false;}
			}
		}
		
		return discardable;
	}

	private boolean discardable(MyState s, Colour c) {

		int toPlay;
		
		int top = topFw(s,c);
	    if (top==5) return true;
	    else toPlay = top;
	    
	    
		boolean discardable = true;
		
		for(int j = 0 ; j < 5; j++){
			if(j >= toPlay){
				int inPlay = cardsLeftInDeck[mapColourToInt(c)][j];
				if(inPlay != 0){discardable = false;}
			}
		}
		
		return discardable;
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
