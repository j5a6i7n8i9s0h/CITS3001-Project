package agents;

import java.util.*;
import hanabAI.*;

public class MyState implements Cloneable {
	int totalCards = 50;

	Colour[][] knownColours;
	int[][] knownValues;
	int[][] theyArrived;

	int[][] cardsLeftInDeck;

	java.util.Stack<Card> deck;
	// int numLeft;

	boolean firstAction = true;
	int numPlayers;
	int numCards;
	int index;
	int rootIndex;
	int penalty;

	/** The name of each of the players in the game **/
	String[] players;
	/** The stack of cards that have bee discarded, or incorrectly played **/
	Stack<Card> discards;
	/** For each colour, the cards making up that firework so far **/
	Map<Colour, Stack<Card>> fireworks;
	/** The hand of each player **/
	Card[][] hands;
	/** The order of this state in the game **/
	int order = 0;
	/** The number of hints remaining **/
	int hints = 0;
	/** The number of fuse tokens left **/
	int fuse = 0;
	/**
	 * The observer of this state. This allows hidden information to be redacted
	 **/
	int observer = -1;
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
	int nextPlayer = -1;
	/** The fnal play of the game (for when the deck runs out) **/
	int finalAction = -1;

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
		this.knownColours = knownColours;
		this.knownValues = knownValues;
		this.cardsLeftInDeck = cardsLeftInDeck;
		this.nextPlayer = s.getNextPlayer();
		this.numPlayers = s.getPlayers().length;
		this.theyArrived = theyArrived;
		this.numCards = (this.players.length > 3 ? 4 : 5);
		this.index = s.getNextPlayer();
		this.rootIndex = s.getNextPlayer();
		dealMyCards();
	}
	
	private void dealMyCards(){
		deck = new Stack<Card>();
		hands[rootIndex] = new Card[numCards];
 	    for(int i = 0; i < numCards; i ++){
 	      if(knownValues[rootIndex][i]> 0 && knownColours[rootIndex][i] != null){
 	        cardsLeftInDeck[mapColourToInt(knownColours[rootIndex][i])][knownValues[rootIndex][i]-1]--;
 	        hands[rootIndex][i] = new Card(knownColours[rootIndex][i], knownValues[rootIndex][i]);
 	      }
 	    }
 	   for(int i = 0; i < numCards; i ++){
 	      
 		   if(hands[rootIndex][i] != null){continue;}
 		   if(knownValues[rootIndex][i]> 0 ){
 	      	int total = 0;
 	        for(Colour c: Colour.values()){
 	          total += cardsLeftInDeck[mapColourToInt(c)][knownValues[rootIndex][i]-1];
 	        }
 	        if(total == 0){continue;}//can improve later with hungarian
 	        int counter = -1;
 	        java.util.Random r = new java.util.Random();

 	 	      int pick = r.nextInt(total) + 1;
 	        while(pick > 0){
 	          pick -= cardsLeftInDeck[++counter][knownValues[rootIndex][i]-1];
 	        }
 	        cardsLeftInDeck[counter][knownValues[rootIndex][i]-1]--;
 	        hands[rootIndex][i] = new Card(mapToColour(counter), knownValues[rootIndex][i]);
 	      }
 	   }
 	   for(int i = 0; i < numCards; i ++){
 		  if(hands[rootIndex][i] != null){continue;}
 	       if(knownColours[rootIndex][i] != null){
 	    	   int total = 0;
 	    	   for(int j = 0; j < 5; j ++){
 	    		   total += cardsLeftInDeck[mapColourToInt(knownColours[rootIndex][i])][j];
 	    	   }
 	    	   if(total == 0){continue;}
 	    	   int counter = -1;
 	    	   java.util.Random r = new java.util.Random();

 	 	       int pick = r.nextInt(total) + 1;
 	 	       while(pick > 0){
 	 	    	   pick -= cardsLeftInDeck[mapColourToInt(knownColours[rootIndex][i])][++counter];
 	 	       }
 	 	       cardsLeftInDeck[mapColourToInt(knownColours[rootIndex][i])][counter]--;
 	 	       hands[rootIndex][i] = new Card(knownColours[rootIndex][i], counter+1);
 	      }
 	    }
 	    
 	    makeDeck();
 	    
 	    for(int i = 0; i < numCards; i ++){
 	      if(hands[rootIndex][i] == null){
 	    	  if(!deck.isEmpty()){
 	    		 hands[rootIndex][i] = deck.pop();
 	    		 cardsLeftInDeck[mapColourToInt(hands[rootIndex][i].getColour())][hands[rootIndex][i].getValue()-1]--;
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

	public boolean legalAction(Action a) throws IllegalActionException {
		if(a.getPlayer()!=nextPlayer){
			return false;
		}
		switch (a.getType()) {
		case PLAY:
			return (a.getCard() >= 0 && a.getCard() < hands[nextPlayer].length);
		case DISCARD:
			if (hints == 8)
				throw new IllegalActionException("Discards cannot be made when there are 8 hint tokens");
			return (a.getCard() >= 0 && a.getCard() < hands[nextPlayer].length);
		case HINT_COLOUR:
			if (hints == 0 || a.getHintReceiver() < 0 || a.getHintReceiver() > players.length
					|| a.getHintReceiver() == a.getPlayer())
				return false;
			boolean[] hint = new boolean[hands[a.getHintReceiver()].length];
			for (int i = 0; i < hint.length; i++) {
				Card c = hands[a.getHintReceiver()][i];
				hint[i] = (c == null ? null : c.getColour()) == a.getColour();
			}
			return Arrays.equals(hint, a.getHintedCards());
		case HINT_VALUE:
			if (hints == 0 || a.getHintReceiver() < 0 || a.getHintReceiver() > players.length
					|| a.getHintReceiver() == a.getPlayer())
				return false;
			hint = new boolean[hands[a.getHintReceiver()].length];
			for (int i = 0; i < hint.length; i++) {
				Card c = hands[a.getHintReceiver()][i];
				hint[i] = (c == null ? -1 : c.getValue()) == a.getValue();
			}
			return Arrays.equals(hint, a.getHintedCards());
		default:
			return false;
		}
	}

	public MyState nextState(MyState previous, Action action) throws IllegalActionException, CloneNotSupportedException {
		if (!previous.legalAction(action)) 
			throw new IllegalActionException("Invalid action!: " + action);
		if (gameOver())
			throw new IllegalActionException("Game Over!");
		MyState next = (MyState) previous.clone();
		
		switch (action.getType()) {
		case PLAY:
			
			next.penalty = previous.givePenalty(action);
			
			Card c = previous.hands[action.getPlayer()][action.getCard()];
			Stack<Card> fw = previous.fireworks.get(c.getColour());
			if ((fw.isEmpty() && c.getValue() == 1) || (!fw.isEmpty() && fw.peek().getValue() == c.getValue() - 1)) {
				next.fireworks.get(c.getColour()).push(c);
				if (next.fireworks.get(c.getColour()).size() == 5 && next.hints < 8)
					next.hints++;
			} else {
				next.discards.push(c);
				next.fuse--;
			}
		    if(!next.deck.isEmpty()) next.hands[action.getPlayer()][action.getCard()] = next.deck.pop();
		    else next.hands[action.getPlayer()][action.getCard()] = null;
		    if(next.deck.isEmpty() && previous.finalAction==-1) next.finalAction = previous.order+numPlayers;

			Card newC = next.hands[action.getPlayer()][action.getCard()];
			if(newC != null){
				next.cardsLeftInDeck[mapColourToInt(newC.getColour())][newC.getValue()-1]--;
				next.theyArrived[action.getPlayer()][action.getCard()] = next.order;
				next.totalCards--;
			}
			next.knownColours[action.getPlayer()][action.getCard()] = null;
			next.knownValues[action.getPlayer()][action.getCard()] = 0;
			next.theyArrived[action.getPlayer()][action.getCard()] = previous.order;
			
			break;
		case DISCARD:
			c = previous.hands[action.getPlayer()][action.getCard()];
			next.discards.push(c);
		    if(!next.deck.isEmpty()) next.hands[action.getPlayer()][action.getCard()] = next.deck.pop();
		    else next.hands[action.getPlayer()][action.getCard()] = null;
		    if(next.deck.isEmpty() && previous.finalAction==-1) next.finalAction = next.order+numPlayers;
			if (previous.hints < 8)
				next.hints++;
			Card newD = next.hands[action.getPlayer()][action.getCard()];
			if(newD != null){
				next.cardsLeftInDeck[mapColourToInt(newD.getColour())][newD.getValue()-1]--;
				next.theyArrived[action.getPlayer()][action.getCard()] = next.order;
				next.totalCards--;
			}
			next.knownColours[action.getPlayer()][action.getCard()] = null;
			next.knownValues[action.getPlayer()][action.getCard()] = 0;
			next.theyArrived[action.getPlayer()][action.getCard()] = previous.order;
			break;
		case HINT_COLOUR:
            for(int i = 0; i < numCards; i ++){
                if(action.getHintedCards()[i]){
                    next.knownColours[action.getHintReceiver()][i] = action.getColour() ;
                }
            }
            next.hints--;
            break;
        case HINT_VALUE:
            for(int i = 0; i < numCards; i ++){
                if(action.getHintedCards()[i]){
                    next.knownValues[action.getHintReceiver()][i] = action.getValue() ;
                }
            }
            next.hints--;
            break;
		default:
			break;
		}
		next.order++;
		//s.previousAction = action;
		next.nextPlayer = (previous.nextPlayer + 1) % numPlayers;
		//s.previousState = this;
		return next;
	}
	private int givePenalty(Action action) throws IllegalActionException {
/*		int value = knownValues[action.getPlayer()][action.getCard()];
		Colour colour = knownColours[action.getPlayer()][action.getCard()];
		
		if(value == 0 && colour == null && totalCards > 10){
			return 5;
		}
		if((value == 0 || colour == null) && totalCards > 35){
			return 1;
		}
*/
		return 0;
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
		return ((finalAction!=-1 &&order==finalAction+1) || fuse == 0 || getScore()==25);
	}

	public int Rollout() throws CloneNotSupportedException, IllegalActionException {

		MyState s = (MyState) this.clone();
		for(int i = 0; i < s.numCards; i ++){
			if( s.hands[rootIndex][i] == null){continue;}
			int colour = mapColourToInt(s.hands[rootIndex][i].getColour());
			int value = s.hands[rootIndex][i].getValue();
			s.cardsLeftInDeck[colour][value-1] ++;
		}
		s.dealMyCards();
		
		while(!s.gameOver()){
			for(int i = 0; i < s.numCards; i ++){
				if( s.hands[s.nextPlayer][i] == null){continue;}
				int colour = mapColourToInt(s.hands[s.nextPlayer][i].getColour());
				int value = s.hands[s.nextPlayer][i].getValue();
				s.cardsLeftInDeck[colour][value-1] ++;
			}
			
			
			Action a = rolloutAction.doAction(s);
			
			int putBack = s.index;;
			for(int i = 0; i < s.numCards; i ++){
				if( s.hands[putBack][i] == null){continue;}
				int colour = mapColourToInt(s.hands[putBack][i].getColour());
				int value = s.hands[putBack][i].getValue();
				s.cardsLeftInDeck[colour][value-1] --;
			}
			
		    s = (MyState) nextState(s, a);   
		}
		return s.getScore() - penalty;
	}

	private Action playKnown(MyState s) throws IllegalActionException {
		for (int i = 0; i < numCards; i++) {
			if (s.knownColours[s.index][i] != null && s.knownValues[s.index][i] != 0) {
				if (playable(s, s.knownColours[s.index][i], s.knownValues[s.index][i])) {
					return new Action(s.index, toString(), ActionType.PLAY, i);
				}
			} else if (s.knownValues[s.index][i] != 0) {
				if (playable(s, s.knownValues[s.index][i])) {
					return new Action(s.index, toString(), ActionType.PLAY, i);
				}
			} else if (s.knownColours[s.index][i] != null) {
				if (playable(s, s.knownColours[s.index][i])) {
					return new Action(s.index, toString(), ActionType.PLAY, i);
				}
			} else {
				// know nothing of the card
			}
		}
		return null;
	}

	private Action discardKnown(MyState s) throws IllegalActionException {
		if (s.hints != 8) {
			for (int i = 0; i < numCards; i++) {
				if (s.knownColours[s.index][i] != null && s.knownValues[s.index][i] > 0) {
					if (discardable(s, s.knownColours[s.index][i], s.knownValues[s.index][i])) {
						return new Action(s.index, toString(), ActionType.DISCARD, i);
					}

				} else if (s.knownValues[s.index][i] != 0) {
					if (discardable(s, s.knownValues[s.index][i])) {
						return new Action(s.index, toString(), ActionType.DISCARD, i);
					}
				} else if (s.knownColours[s.index][i] != null) {
					if (discardable(s, s.knownColours[s.index][i])) {
						return new Action(s.index, toString(), ActionType.DISCARD, i);
					}
				} else {
					// know nothing of the card
				}
			}
		}
		return null;
	}

	private Action hint(MyState s, int p, int hint) throws IllegalActionException {

		if (s.hints == 0 || hint == -1) {
			return null;
		}
		
		boolean[] match = new boolean[numCards];

		if (hint > 4) {

			for (int i = 0; i < numCards; i++) {
				if(s.hands[p][i]==null)
					continue;
				if (s.hands[p][i].getValue() == hint - 4) {
					match[i] = true;
					s.knownValues[p][i] = hint -4;
				}
			}

			return new Action(s.index, toString(), ActionType.HINT_VALUE, p, match, hint - 4);
		} else {

			for (int i = 0; i < numCards; i++) {
				if (s.hands[p][i] == null) {
					continue;
				}
				if (s.hands[p][i].getColour() == mapToColour(hint)) {
					match[i] = true;
					s.knownColours[p][i] = mapToColour(hint);
				}
			}

			return new Action(s.index, toString(), ActionType.HINT_COLOUR, p, match, mapToColour(hint));
		}
	}

	private Action discardOldest(MyState s) throws IllegalActionException {

		if (s.hints == 8) {
			return null;
		}
		int[] age = new int[numCards];
		for (int i = 0; i < numCards; i++) {
			age[i] = i;
		}
		for (int i = 1; i < numCards; i++) {

			int key = s.theyArrived[s.index][i];
			int j = i - 1;

			while (j >= 0 && s.theyArrived[s.index][age[j]] > key) {
				age[j + 1] = age[j];
				j = j - 1;
			}
			age[j + 1] = i;
		}

		int oldest = -1;
		// find oldest un-hinted card
		for (int i = 0; i < numCards; i++) {
			if (s.knownValues[s.index][age[i]] == 0 && s.knownColours[s.index][age[i]] == null) {
				oldest = age[i];
			}
		}

		if (oldest == -1) {
			return null;
		} else {
			return new Action(s.index, toString(), ActionType.DISCARD, oldest);
		}

	}

	private Action guess(MyState s) throws IllegalActionException {
		double probCorrectPlay = 0;
		int play = 0;

		double probCorrectDiscard = 0;
		int discard = 0;

		int numPlayable = 0;
		int numDiscardable = 0;
		int numTotal = 0;

		for (int i = 0; i < numCards; i++) {
			if (s.knownColours[s.index][i] != null && s.knownValues[s.index][i] > 0) {
				numPlayable = 0;
				numDiscardable = 0;
				// since we wouldve played it already
			} else if (s.knownColours[s.index][i] != null) {
				Colour c = s.knownColours[s.index][i];
				int top = topFw(s, c);
				if (top < 5) {
					numPlayable = s.cardsLeftInDeck[mapColourToInt(c)][top];
				}
				for (int j = 0; j < top; j++) {
					numDiscardable += s.cardsLeftInDeck[mapColourToInt(c)][j];
				}
				for (int j = 0; j < 5; j++) {
					numTotal += s.cardsLeftInDeck[mapColourToInt(c)][j];
				}
			} else if (s.knownValues[s.index][i] > 0) {
				int card = s.knownValues[s.index][i];
				for (Colour c : Colour.values()) {
					if (card == topFw(s, c) + 1) {
						numPlayable += s.cardsLeftInDeck[mapColourToInt(c)][card - 1];
					} else if (card <= topFw(s, c)) {
						numDiscardable += s.cardsLeftInDeck[mapColourToInt(c)][card - 1];
					}
					numTotal += s.cardsLeftInDeck[mapColourToInt(c)][card - 1];
				}
			} else {
				for (Colour c : Colour.values()) {
					int top = topFw(s, c);
					if (top == 5) {
						continue;
					}
					numPlayable += s.cardsLeftInDeck[mapColourToInt(c)][top];
					for (int j = 0; j < top; j++) {
						numDiscardable += s.cardsLeftInDeck[mapColourToInt(c)][j];
					}

				}
				numTotal = s.totalCards;
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

		if (probCorrectDiscard >= probCorrectPlay || s.fuse < 2) {
			return new Action(s.index, toString(), ActionType.DISCARD, discard);
		} else {
			return new Action(s.index, toString(), ActionType.PLAY, play);
		}

	}

	// helper classes and functions

	private int evaluateHint(MyState s, int p, int hint) {
		// 0:blue,1:red... (int to colour)
		//5:hint 1, 6:hint2, 7:hint3, 8:hint4, 9:hint5
		
		//first check if it will reveal a playable card  worth 15.
		
		//then check if it will save a final in play card worth 5.
		
		//if it will reveal a can discard worth 4
		
		//then check how much information it will reveal worth 1 each.
		
		int score = 0;
		
		score += 15*hintMakesPlayable(s,p,hint);
		score += 4*hintMakesDiscardable(s,p,hint);
		
		//if the card is playable and hasnt been hinted
		
		
		
		boolean[] finals = finalCards(s,p);
		
		for(int i = 0 ; i < numCards; i++){
			Card c = s.hands[p][i];
			if(c == null){continue;}
			if(hint > 4){
				if(s.knownValues[p][i] == 0 && c.getValue() == (hint-4)){
					score+= 1;
					if(playable(s, c.getColour(), c.getValue())){
						score += 7;
					}

					if(finals[i]){
						score+=3;
					}
				}
			}else{
				if(s.knownColours[p][i] == null && c.getColour() == mapToColour(hint)){
					score+= 1;
					if(playable(s, c.getColour(), c.getValue())){
						score += 7; //could add some convention priority later
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

		for (int i = 0; i < numCards; i++) {
			if (s.knownColours[p][i] != null && s.knownValues[p][i] != 0) {
				if (discardable(s, s.knownColours[p][i], s.knownValues[p][i])) {
					discardBefore++;
				}
			} else if (s.knownValues[p][i] != 0) {
				if (discardable(s, s.knownValues[p][i])) {
					discardBefore++;
				}
			} else if (s.knownColours[p][i] != null) {
				if (discardable(s, s.knownColours[p][i])) {
					discardBefore++;
				}
			} else {
				// know nothing of the card
			}
		}

		int[] tempV = s.knownValues[p].clone();
		Colour[] tempC = s.knownColours[p].clone();

		if (hint > 4) {
			for (int i = 0; i < numCards; i++) {
				if (s.hands[p][i] == null) {
					continue;
				}
				if (s.hands[p][i].getValue() == (hint - 4)) {
					tempV[i] = hint - 4;
				}
			}
		} else {
			for (int i = 0; i < numCards; i++) {
				if (s.hands[p][i] == null) {
					continue;
				}
				if (s.hands[p][i].getColour() == mapToColour(hint)) {
					tempC[i] = mapToColour(hint);
				}
			}
		}

		for (int i = 0; i < numCards; i++) {
			if (tempC[i] != null && tempV[i] != 0) {
				if (discardable(s, tempC[i], tempV[i])) {
					discardAfter++;
				}
			} else if (tempV[i] != 0) {
				if (discardable(s, tempV[i])) {
					discardAfter++;
				}
			} else if (tempC[i] != null) {
				if (discardable(s, tempC[i])) {
					discardAfter++;
				}
			} else {
				// know nothing of the card
			}
		}

		return discardAfter - discardBefore;
	}

	private int hintMakesPlayable(MyState s, int p, int hint) {

		int playableBefore = 0;
		int playableAfter = 0;

		for (int i = 0; i < numCards; i++) {
			if (s.knownColours[p][i] != null && s.knownValues[p][i] != 0) {
				if (playable(s, s.knownColours[p][i], s.knownValues[p][i])) {
					playableBefore++;
				}
			} else if (s.knownValues[p][i] != 0) {
				if (playable(s, s.knownValues[p][i])) {
					playableBefore++;
				}
			} else if (s.knownColours[p][i] != null) {
				if (playable(s, s.knownColours[p][i])) {
					playableBefore++;
				}
			} else {
				// know nothing of the card
			}
		}

		int[] tempV = s.knownValues[p].clone();
		Colour[] tempC = s.knownColours[p].clone();

		if (hint > 4) {
			for (int i = 0; i < numCards; i++) {
				if (s.hands[p][i] == null) {
					continue;
				}
				if (s.hands[p][i].getValue() == (hint - 4)) {
					tempV[i] = hint - 4;
				}
			}
		} else {
			for (int i = 0; i < numCards; i++) {
				if (s.hands[p][i] == null) {
					continue;
				}
				if (s.hands[p][i].getColour() == mapToColour(hint)) {
					tempC[i] = mapToColour(hint);
				}
			}
		}

		for (int i = 0; i < numCards; i++) {
			if (tempC[i] != null && tempV[i] != 0) {
				if (playable(s, tempC[i], tempV[i])) {
					playableAfter++;
				}
			} else if (tempV[i] != 0) {
				if (playable(s, tempV[i])) {
					playableAfter++;
				}
			} else if (tempC[i] != null) {
				if (playable(s, tempC[i])) {
					playableAfter++;
				}
			} else {
				// know nothing of the card
			}
		}

		return playableAfter - playableBefore;
	}

	private boolean[] finalCards(MyState s, int p) {
		
		int[][] cardsInPlay = new int[5][5];
		
	    for(int i = 0; i < numPlayers; i ++){
	    	for(int j = 0; j < numCards; j ++){
	    		s.theyArrived[i][j] = 1;
	    		if(i != s.index){
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
	

	private int topFw(MyState s, Colour c) {
		java.util.Stack<Card> fw = s.fireworks.get(c);
		if (fw.size() == 5)
			return 5;
		else
			return (fw.size());
	}

	private boolean playable(MyState s, Colour c, int value) {
		int top = topFw(s, c);
		return top == value - 1;
	}

	private boolean playable(MyState s, int value) {
		boolean[] canPlay = new boolean[5];

		for (Colour c : Colour.values()) {
			canPlay[mapColourToInt(c)] = (topFw(s, c) == value - 1);
		}

		boolean playable = true;
		
		for(int i = 0; i < numCards; i ++){
			if( s.hands[s.index][i] == null){continue;}
			int colour = mapColourToInt(s.hands[s.index][i].getColour());
			int v = s.hands[s.index][i].getValue();
			s.cardsLeftInDeck[colour][v-1] ++;
		}
		
		for (int j = 0; j < 5; j++) {
			if (!canPlay[j]) {
				int inPlay = s.cardsLeftInDeck[j][value - 1];
				if (inPlay != 0) {
					playable = false;
				}
			}
		}
		
		for(int i = 0; i < numCards; i ++){
			if( s.hands[s.index][i] == null){continue;}
			int colour = mapColourToInt(s.hands[s.index][i].getColour());
			int v = s.hands[s.index][i].getValue();
			s.cardsLeftInDeck[colour][v-1] --;
		}

		return playable;
	}

	private boolean playable(MyState s, Colour c) {

		int toPlay;

		int top = topFw(s, c);
		if (top == 5)
			return false;
		else
			toPlay = top;

		boolean playable = true;
		
		for(int i = 0; i < numCards; i ++){
			if( s.hands[s.index][i] == null){continue;}
			int colour = mapColourToInt(s.hands[s.index][i].getColour());
			int value = s.hands[s.index][i].getValue();
			s.cardsLeftInDeck[colour][value-1] ++;
		}
		
		for (int j = 0; j < 5; j++) {
			if (j != toPlay) {
				int inPlay = s.cardsLeftInDeck[mapColourToInt(c)][j];
				if (inPlay != 0) {
					playable = false;
				}
			}
		}
		
		for(int i = 0; i < numCards; i ++){
			if( s.hands[s.index][i] == null){continue;}
			int colour = mapColourToInt(s.hands[s.index][i].getColour());
			int value = s.hands[s.index][i].getValue();
			s.cardsLeftInDeck[colour][value-1] --;
		}

		return playable;
	}

	private boolean discardable(MyState s, Colour c, int value) {
		int top = topFw(s, c);
		return top >= value;
	}

	private boolean discardable(MyState s, int value) {
		boolean[] canDiscard = new boolean[5];

		for (Colour c : Colour.values()) {
			canDiscard[mapColourToInt(c)] = (topFw(s, c) >= value);
		}

		boolean discardable = true;

		for (int j = 0; j < 5; j++) {
			if (!canDiscard[j]) {
				int inPlay = s.cardsLeftInDeck[j][value - 1];
				if (inPlay != 0) {
					discardable = false;
				}
			}
		}

		return discardable;
	}

	private boolean discardable(MyState s, Colour c) {

		int toPlay;

		int top = topFw(s, c);
		if (top == 5)
			return true;
		else
			toPlay = top;

		boolean discardable = true;

		for (int j = 0; j < 5; j++) {
			if (j >= toPlay) {
				int inPlay = s.cardsLeftInDeck[mapColourToInt(c)][j];
				if (inPlay != 0) {
					discardable = false;
				}
			}
		}

		return discardable;
	}
	
	public Stack<Card> getDeck()
	{
		return (Stack<Card>) deck.clone();
	}

	int mapColourToInt(Colour c) {
		switch (c) {
		case BLUE:
			return 0;
		case RED:
			return 1;
		case GREEN:
			return 2;
		case WHITE:
			return 3;
		case YELLOW:
			return 4;
		default:
			return -1;
		}
	}

	private Colour mapToColour(int c) {
		switch (c) {
		case 0:
			return Colour.BLUE;
		case 1:
			return Colour.RED;
		case 2:
			return Colour.GREEN;
		case 3:
			return Colour.WHITE;
		case 4:
			return Colour.YELLOW;
		default:
			return null;
		}
	}
	
/*	public int discardcard(int i) {
		i=i%this.numCards;
		if(this.hints>7) return Integer.MIN_VALUE;
		Colour c = this.knownColours[this.nextPlayer][i];
		int value = this.knownValues[this.nextPlayer][i];
		int order = this.theyArrived[this.nextPlayer][i];
		if(value==5)
			return Integer.MIN_VALUE;
		if(c!=null && value!=0) //both is known
		{
			if(this.fireworks.get(c).size()!=0 && this.fireworks.get(c).peek().getValue()>=value) 
				return Integer.MAX_VALUE;
			return (value==1?3:2)*this.cardsLeftInDeck[this.mapColourToInt(c)][value-1];
			// Future <=  consider other peoples hands 
		}
		else if(c!=null) //only colour is known 
		{
			int topcard = this.topFw(this, c);
			if(topcard!=0) {
			if(topcard==5)
				return Integer.MAX_VALUE;
			int CardsOfThisColour[] = this.cardsLeftInDeck[this.mapColourToInt(c)];
			int couldkeep =0;
			for(int j=topcard-1;j<5;j++)
				couldkeep+=CardsOfThisColour[j];
			return (this.order-order)*(5-topcard)*couldkeep;
			}
			return Integer.MIN_VALUE;
			
		}
		else if(value!=0) //only value is known
		{
			return (this.order-order)*(value==1?3:2);
		}
		else // neither is known
		{
			return this.order-order;
		}
	}*/
	
	

	public int playcard(int i) {
		i=i%numCards;
		Colour c = knownColours[nextPlayer][i];
		int value = knownValues[nextPlayer][i];
		if(this.hands[nextPlayer][i]==null)
			return Integer.MIN_VALUE;
		if(c!=null && value!=0)
		{
			return ((playable(this,c,value)||fuse>1) ?Integer.MAX_VALUE: Integer.MIN_VALUE);
		}
		else if(c!=null) // only colour known
		{
			return ((playable(this,c)||fuse>1) ?Integer.MAX_VALUE: Integer.MIN_VALUE);
		}
		else if(value!=0) // only val known
		{
			return ((playable(this,value)||fuse>1) ?Integer.MAX_VALUE: Integer.MIN_VALUE);
		}
		else
		{
			return ((fuse>1) ?Integer.MAX_VALUE: Integer.MIN_VALUE);
		}
			
	}
	
	public int hintCardByValue(int i) {
		int playerToHint = (this.nextPlayer + (i/(this.numCards*2)))%this.numPlayers;
		int card = i%this.numCards;
		Card c = this.hands[playerToHint][card];
		int k=1;
		if(this.knownValues[playerToHint][card]!=0)
			return Integer.MIN_VALUE;
		for(Card s:this.hands[playerToHint]) {
			if(s==null) continue;//System.out.println(playerToHint);
			if(c==null)continue;
			if(s.getValue()==c.getValue()) k++ ;
		}
		if(hints>0) {
			if(c==null)
				return Integer.MIN_VALUE;
			if(c.getValue()==5||playable(this,c.getColour(),c.getValue()))
				return Integer.MAX_VALUE;
			return evaluateHint(this,playerToHint,c.getValue()+4);		
			
		}
		return Integer.MIN_VALUE;
	}
	
	public int hintCardByColour(int i) {
		int playerToHint = (this.nextPlayer + (i/(this.numCards*2)))%this.numPlayers;
		int card = i%this.numCards;
		Card c = this.hands[playerToHint][card];
		if(c!=null) {
			if(hints>0) {
				return evaluateHint(this,playerToHint,this.mapColourToInt(c.getColour()));		
			}
		}
		return Integer.MIN_VALUE;
	}
	
	public PriorityQueue<Move> getBestPossibleMoves() throws IllegalActionException {
		PriorityQueue<Move> bestmoves = new PriorityQueue<Move>(new MoveCompare());
		ArrayList<String> actionStringList = new ArrayList<String>();
		for(int i = 0; i<numCards*2*numPlayers;i++)
		{
			int score;
			if(i>=0 && i<numCards) {
				score=playcard(i);
				Action a = new Action(nextPlayer,
						players[nextPlayer],
						ActionType.PLAY,i%this.numCards);
				if(score!=Integer.MIN_VALUE) 
					bestmoves.add(new Move(a,score));
			}else if(i>=numCards && i<numCards*2) {
				score=1;
				Action a=new Action(nextPlayer,
						players[nextPlayer],
						ActionType.DISCARD,i%this.numCards);
				if(score!=Integer.MIN_VALUE && hints!=8)
					bestmoves.add(new Move(a, score));
			}else{
				int j=(int)i/this.numCards;
				score= (j%2!=0?hintCardByValue(i):hintCardByColour(i));
				int playerToHint = (this.nextPlayer + (i/(this.numCards*2)))%this.numPlayers;
				int card = i%this.numCards;
				Card c = this.hands[playerToHint][card];
				if(c==null) continue;
					if(j%2!=0)
					{
						boolean[] cards = new boolean[this.numCards];
						for(int k=0;k<this.numCards;k++)
						{
							cards[k]=this.hands[playerToHint][k]!=null?this.hands[playerToHint][k].getValue()==c.getValue():false;
								
						}
						Action newAct= new Action(this.nextPlayer,
								this.players[this.nextPlayer],
								ActionType.HINT_VALUE,playerToHint,cards,c.getValue());
						if(!actionStringList.contains(newAct.toString()) && score!=Integer.MIN_VALUE) {
							bestmoves.add(new Move(newAct,score));
							actionStringList.add(newAct.toString());
						}
							
					}
					else
					{
						boolean[] cards = new boolean[this.numCards];
						for(int k=0;k<this.numCards;k++)
						{
							cards[k]=this.hands[playerToHint][k]!=null?this.hands[playerToHint][k].getColour()==c.getColour():false;
								
						}
						Action newAct=new Action(this.nextPlayer,
								this.players[this.nextPlayer],
								ActionType.
								HINT_COLOUR,playerToHint,cards,c.getColour());
						if(!actionStringList.contains(newAct.toString()) && score!=Integer.MIN_VALUE ) {
							bestmoves.add(new Move(newAct,score));
							actionStringList.add(newAct.toString());
						}
					}
			}
		}
		return bestmoves;
	}
	
	public Object clone()
	{
		try{
		      MyState s = (MyState) super.clone();
		      s.players = players.clone();
		      s.discards = (Stack<Card>)discards.clone();
		      s.hands = (Card[][]) hands.clone();
		      for(int i = 0; i<hands.length; i++) s.hands[i] = (Card[])s.hands[i].clone();
		      s.fireworks = (Map<Colour,Stack<Card>>)((HashMap)fireworks).clone();
		      for(Colour c: Colour.values()) s.fireworks.put(c,(Stack<Card>)fireworks.get(c).clone());
		      s.cardsLeftInDeck = new int[5][5];
		      for(int i=0;i<5;i++)
		      {
		    	  s.cardsLeftInDeck[i] = cardsLeftInDeck[i].clone();
		      }
		      s.deck = (Stack<Card>) this.deck.clone();

		      s.hints = hints;
		      s.knownColours = new Colour[s.numPlayers][s.numCards];
		      s.theyArrived = new int[s.numPlayers][s.numCards];
		      s.knownValues = new int[s.numPlayers][s.numCards];
		      for(int i=0;i<s.numPlayers;i++)
		      {
		    	  s.knownColours[i] = knownColours[i].clone();
		    	  s.theyArrived[i] = theyArrived[i].clone();
		    	  s.knownValues[i] = knownValues[i].clone();
		      }
 		      return s;
		    }
		    catch(CloneNotSupportedException e){return null;}	
	}

	public void reDealCards() {
		for(int i = 0; i < numCards; i ++){
			if( hands[rootIndex][i] == null){continue;}
			int colour = mapColourToInt(hands[rootIndex][i].getColour());
			int value = hands[rootIndex][i].getValue();
			cardsLeftInDeck[colour][value-1] ++;
		}
		dealMyCards();
		
	}
}
