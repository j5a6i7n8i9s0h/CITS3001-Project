package agents;

import java.util.*;
import hanabAI.*;

public class MyState implements Cloneable {
	private int totalCards = 50;

	private Colour[][] knownColours;
	private int[][] knownValues;
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
		this.deck = new Stack<Card>();
		dealMyCards();
	}
	
	private void dealMyCards(){
 	    for(int i = 0; i < numCards; i ++){
 	      if(knownValues[nextPlayer][i]> 0 && knownColours[nextPlayer][i] != null){
 	        cardsLeftInDeck[mapColourToInt(knownColours[nextPlayer][i])][knownValues[nextPlayer][i]-1]--;
 	        if(cardsLeftInDeck[mapColourToInt(knownColours[nextPlayer][i])][knownValues[nextPlayer][i]-1] < 0){
 	        	System.out.println("help");
 	        }
 	        hands[nextPlayer][i] = new Card(knownColours[nextPlayer][i], knownValues[nextPlayer][i]);
 	      }
 	    }
 	   for(int i = 0; i < numCards; i ++){
 	      
 		   if(hands[nextPlayer][i] != null){continue;}
 		   if(knownValues[nextPlayer][i]> 0 ){
 	      	int total = 0;
 	        for(Colour c: Colour.values()){
 	          total += cardsLeftInDeck[mapColourToInt(c)][knownValues[nextPlayer][i]-1];
 	        }
 	        if(total == 0){continue;}//can improve later with hungarian
 	        int counter = -1;
 	        java.util.Random r = new java.util.Random();

 	 	      int pick = r.nextInt(total) + 1;
 	        while(pick > 0){
 	          pick -= cardsLeftInDeck[++counter][knownValues[nextPlayer][i]-1];
 	        }
 	        cardsLeftInDeck[counter][knownValues[nextPlayer][i]-1]--;
 	        hands[nextPlayer][i] = new Card(mapToColour(counter), knownValues[nextPlayer][i]);
 	      }
 	   }
 	   for(int i = 0; i < numCards; i ++){
 		  if(hands[nextPlayer][i] != null){continue;}
 	       if(knownColours[nextPlayer][i] != null){
 	    	   int total = 0;
 	    	   for(int j = 0; j < 5; j ++){
 	    		   total += cardsLeftInDeck[mapColourToInt(knownColours[nextPlayer][i])][j];
 	    	   }
 	    	   if(total == 0){continue;}
 	    	   int counter = -1;
 	    	   java.util.Random r = new java.util.Random();

 	 	       int pick = r.nextInt(total) + 1;
 	 	       while(pick > 0){
 	 	    	   pick -= cardsLeftInDeck[mapColourToInt(knownColours[nextPlayer][i])][++counter];
 	 	       }
 	 	       cardsLeftInDeck[mapColourToInt(knownColours[nextPlayer][i])][counter]--;
 	 	       hands[nextPlayer][i] = new Card(knownColours[nextPlayer][i], counter+1);
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

	public boolean legalAction(Action a) throws IllegalActionException {
		if(a.getPlayer()!=nextPlayer) return false;
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

	public MyState nextState(Action action, Stack<Card> deck) throws IllegalActionException, CloneNotSupportedException {
		if (!legalAction(action)) 
			throw new IllegalActionException("Invalid action!: " + action);
		if (gameOver())
			throw new IllegalActionException("Game Over!");
		MyState s = (MyState) this.clone();
		switch (action.getType()) {
		case PLAY:
			Card c = hands[action.getPlayer()][action.getCard()];
			Stack<Card> fw = fireworks.get(c.getColour());
			if ((fw.isEmpty() && c.getValue() == 1) || (!fw.isEmpty() && fw.peek().getValue() == c.getValue() - 1)) {
				s.fireworks.get(c.getColour()).push(c);
				if (s.fireworks.get(c.getColour()).size() == 5 && s.hints < 8)
					s.hints++;
			} else {
				s.discards.push(c);
				s.fuse--;
			}
			if (!deck.isEmpty())
				s.hands[action.getPlayer()][action.getCard()] = s.deck.pop();
			else {
				if (finalAction == -1)
					s.finalAction = order + players.length;
				s.hands[action.getPlayer()][action.getCard()] = null;
			}
			Card newC = s.hands[action.getPlayer()][action.getCard()];
			if(newC != null){
				s.cardsLeftInDeck[mapColourToInt(newC.getColour())][newC.getValue()-1]--;
				s.theyArrived[action.getPlayer()][action.getCard()] = s.order;
				s.totalCards--;
			}
			break;
		case DISCARD:
			c = hands[action.getPlayer()][action.getCard()];
			s.discards.push(c);
			if (!deck.isEmpty())
				s.hands[action.getPlayer()][action.getCard()] = s.deck.pop();
			else{
				if (finalAction == -1)
					s.finalAction = order + players.length;
				s.hands[action.getPlayer()][action.getCard()] = null;
			}
			if (hints < 8)
				s.hints++;
			Card newD = s.hands[action.getPlayer()][action.getCard()];
			if(newD != null){
				s.cardsLeftInDeck[mapColourToInt(newD.getColour())][newD.getValue()-1]--;
				s.theyArrived[action.getPlayer()][action.getCard()] = s.order;
				s.totalCards--;
			}
			break;
		case HINT_COLOUR:
            for(int i = 0; i < numCards; i ++){
                if(action.getHintedCards()[i]){
                    s.knownColours[action.getHintReceiver()][i] = action.getColour() ;
                }
            }
            s.hints--;
            break;
        case HINT_VALUE:
            for(int i = 0; i < numCards; i ++){
                if(action.getHintedCards()[i]){
                    s.knownValues[action.getHintReceiver()][i] = action.getValue() ;
                }
            }
            s.hints--;
            break;
		default:
			break;
		}
		s.order++;
		//s.previousAction = action;
		s.nextPlayer = (nextPlayer + 1) % players.length;
		//s.previousState = this;
		return s;
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

	public int Rollout() throws CloneNotSupportedException, IllegalActionException {

    MyState s = (MyState) this.clone();
	    while(!s.gameOver())
	      s = s.nextState(doAction(s), s.deck);
	return s.getScore();
	}

public Action doAction(MyState s) {
		index = s.getNextPlayer();
		//updateLastActions(s);

		int maxValue = 0;
		int bestHint = -1;
		int hintPlayer = (index+1)%numPlayers;

		
		for(int i = 0; i < 10; i ++){
			int value = evaluateHint(s, hintPlayer , i);
			if(value > maxValue){
				maxValue = value;
				bestHint = i;
			}
		}

		if(numPlayers > 2){
			for(int i = 0; i < 10; i ++){
				int value = evaluateHint(s, hintPlayer, i);
				if(value - (8-hints) > maxValue){
					maxValue = value - (8-hints); // prioritise next player
					bestHint = i;
					hintPlayer = (index+2)%numPlayers;
				}
			}
		}
	    

		try {
			Action a = playKnown(s);

			if (maxValue >= 15 || hints > 5){
				if (a == null)
					a = hint(s, hintPlayer, bestHint);
				if (a == null)
					a = discardKnown(s);

			} else {
				if (a == null)
					a = discardKnown(s);
				if (maxValue > 5 || hints > 4) {
					if (a == null)
						a = hint(s, hintPlayer, bestHint);
				}
			}

			if (a == null)
				a = discardOldest(s);
			if (a == null)
				a = hint(s, hintPlayer, bestHint);
			if (a == null)
				a = guess(s);
			return a;
		} catch (IllegalActionException e) {
			e.printStackTrace();
			throw new RuntimeException("Something has gone very wrong");
		}
	}

	private Action playKnown(MyState s) throws IllegalActionException {
		for (int i = 0; i < numCards; i++) {
			if (knownColours[index][i] != null && knownValues[index][i] != 0) {
				if (playable(s, knownColours[index][i], knownValues[index][i])) {
					knownColours[index][i] = null;
					knownValues[index][i] = 0;
					theyArrived[index][i] = s.order;
					totalCards--;
					return new Action(index, toString(), ActionType.PLAY, i);
				}
			} else if (knownValues[index][i] != 0) {
				if (playable(s, knownValues[index][i])) {
					knownColours[index][i] = null;
					knownValues[index][i] = 0;
					theyArrived[index][i] = s.order;
					totalCards--;
					return new Action(index, toString(), ActionType.PLAY, i);
				}
			} else if (knownColours[index][i] != null) {
				if (playable(s, knownColours[index][i])) {
					knownColours[index][i] = null;
					knownValues[index][i] = 0;
					theyArrived[index][i] = s.order;
					totalCards--;
					return new Action(index, toString(), ActionType.PLAY, i);
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
				if (knownColours[index][i] != null && knownValues[index][i] > 0) {
					if (discardable(s, knownColours[index][i], knownValues[index][i])) {
						knownColours[index][i] = null;
						knownValues[index][i] = 0;
						theyArrived[index][i] = s.order;
						totalCards--;
						return new Action(index, toString(), ActionType.DISCARD, i);
					}

				} else if (knownValues[index][i] != 0) {
					if (discardable(s, knownValues[index][i])) {
						knownColours[index][i] = null;
						knownValues[index][i] = 0;
						theyArrived[index][i] = s.order;
						totalCards--;
						return new Action(index, toString(), ActionType.DISCARD, i);
					}
				} else if (knownColours[index][i] != null) {
					if (discardable(s, knownColours[index][i])) {
						knownColours[index][i] = null;
						knownValues[index][i] = 0;
						theyArrived[index][i] = s.order;
						totalCards--;
						return new Action(index, toString(), ActionType.DISCARD, i);
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
					knownValues[p][i] = hint -4;
				}
			}

			return new Action(index, toString(), ActionType.HINT_VALUE, p, match, hint - 4);
		} else {

			for (int i = 0; i < numCards; i++) {
				if (s.hands[p][i] == null) {
					continue;
				}
				if (s.hands[p][i].getColour() == mapToColour(hint)) {
					match[i] = true;
					knownColours[p][i] = mapToColour(hint);
				}
			}

			return new Action(index, toString(), ActionType.HINT_COLOUR, p, match, mapToColour(hint));
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

			int key = theyArrived[index][i];
			int j = i - 1;

			while (j >= 0 && theyArrived[index][age[j]] > key) {
				age[j + 1] = age[j];
				j = j - 1;
			}
			age[j + 1] = i;
		}

		int oldest = -1;
		// find oldest un-hinted card
		for (int i = 0; i < numCards; i++) {
			if (knownValues[index][age[i]] == 0 || knownColours[index][age[i]] == null) {
				oldest = age[i];
			}
		}

		if (oldest == -1) {
			return null;
		} else {
			knownColours[index][oldest] = null;
			knownValues[index][oldest] = 0;
			theyArrived[index][oldest] = order;
			totalCards --;
			return new Action(index, toString(), ActionType.DISCARD, oldest);
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
			if (knownColours[index][i] != null && knownValues[index][i] > 0) {
				numPlayable = 0;
				numDiscardable = 0;
				// since we wouldve played it already
			} else if (knownColours[index][i] != null) {
				Colour c = knownColours[index][i];
				int top = topFw(s, c);
				if (top < 5) {
					numPlayable = cardsLeftInDeck[mapColourToInt(c)][top];
				}
				for (int j = 0; j < top; j++) {
					numDiscardable += cardsLeftInDeck[mapColourToInt(c)][j];
				}
				for (int j = 0; j < 5; j++) {
					numTotal += cardsLeftInDeck[mapColourToInt(c)][j];
				}
			} else if (knownValues[index][i] > 0) {
				int card = knownValues[index][i];
				for (Colour c : Colour.values()) {
					if (card == topFw(s, c) + 1) {
						numPlayable += cardsLeftInDeck[mapColourToInt(c)][card - 1];
					} else if (card <= topFw(s, c)) {
						numDiscardable += cardsLeftInDeck[mapColourToInt(c)][card - 1];
					}
					numTotal += cardsLeftInDeck[mapColourToInt(c)][card - 1];
				}
			} else {
				for (Colour c : Colour.values()) {
					int top = topFw(s, c);
					if (top == 5) {
						continue;
					}
					numPlayable += cardsLeftInDeck[mapColourToInt(c)][top];
					for (int j = 0; j < top; j++) {
						numDiscardable += cardsLeftInDeck[mapColourToInt(c)][j];
					}

				}
				numTotal = totalCards;
				for (int p = 0; p < numPlayers; p++) {
					if (p == index) {
						continue;
					}
					for (int j = 0; j < numCards; j++) {
						if (s.hands[p][j] == null) {
							continue;
						}
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

		if (probCorrectDiscard >= probCorrectPlay || s.fuse < 2) {
			knownColours[index][discard] = null;
			knownValues[index][discard] = 0;
			theyArrived[index][discard] = s.order;
			totalCards--;

			return new Action(index, toString(), ActionType.DISCARD, discard);
		} else {
			knownColours[index][play] = null;
			knownValues[index][play] = 0;
			theyArrived[index][play] = s.order;
			totalCards--;

			return new Action(index, toString(), ActionType.PLAY, play);
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
			Card c = hands[p][i];
			if(c == null){continue;}
			if(hint > 4){
				if(knownValues[p][i] == 0 && c.getValue() == (hint-4)){
					score+= 1;
					if(playable(s, c.getColour(), c.getValue())){
						score += 7;
					}

					if(finals[i]){
						//score+=4;
					}
				}
			}else{
				if(knownColours[p][i] == null && c.getColour() == mapToColour(hint)){
					score+= 1;
					if(playable(s, c.getColour(), c.getValue())){
						score += 7; //could add some convention priority later
					}
					if(finals[i]){
						//score+=5;
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
			if (knownColours[p][i] != null && knownValues[p][i] != 0) {
				if (discardable(s, knownColours[p][i], knownValues[p][i])) {
					discardBefore++;
				}
			} else if (knownValues[p][i] != 0) {
				if (discardable(s, knownValues[p][i])) {
					discardBefore++;
				}
			} else if (knownColours[p][i] != null) {
				if (discardable(s, knownColours[p][i])) {
					discardBefore++;
				}
			} else {
				// know nothing of the card
			}
		}

		int[] tempV = knownValues[p].clone();
		Colour[] tempC = knownColours[p].clone();

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
			if (knownColours[p][i] != null && knownValues[p][i] != 0) {
				if (playable(s, knownColours[p][i], knownValues[p][i])) {
					playableBefore++;
				}
			} else if (knownValues[p][i] != 0) {
				if (playable(s, knownValues[p][i])) {
					playableBefore++;
				}
			} else if (knownColours[p][i] != null) {
				if (playable(s, knownColours[p][i])) {
					playableBefore++;
				}
			} else {
				// know nothing of the card
			}
		}

		int[] tempV = knownValues[p].clone();
		Colour[] tempC = knownColours[p].clone();

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

	private boolean[] finalCards(MyState s, int p) {//fix

//		if (p == index) {
//			return null;
//		}

		boolean[] finals = new boolean[numCards];
		for (int i = 0; i < numCards; i++) {
			Card c = s.hands[p][i];
			if (c == null) {
				continue;
			}
			int inPlay = cardsLeftInDeck[mapColourToInt(c.getColour())][c.getValue() - 1];
			if (inPlay == 0) {
				finals[i] = true;
			} else {
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

		for (int j = 0; j < 5; j++) {
			if (!canPlay[j]) {
				int inPlay = cardsLeftInDeck[j][value - 1];
				if (inPlay != 0) {
					playable = false;
				}
			}
		}

		return playable;
	}

	private boolean playable(MyState s, Colour c) {

		int toPlay;

		int top = topFw(s, c);
		if (top == 5)
			return false;
		else
			toPlay = top + 1;

		boolean playable = true;

		for (int j = 0; j < 5; j++) {
			if (j != toPlay) {
				int inPlay = cardsLeftInDeck[mapColourToInt(c)][j];
				if (inPlay != 0) {
					playable = false;
				}
			}
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
				int inPlay = cardsLeftInDeck[j][value - 1];
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
				int inPlay = cardsLeftInDeck[mapColourToInt(c)][j];
				if (inPlay != 0) {
					discardable = false;
				}
			}
		}

		return discardable;
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
	
	public int discardcard(int i) {
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
	}
	
	
	public Stack<Card> getDeck()
	{
		return (Stack<Card>) deck.clone();
	}
	
	public int playcard(int i) {
		i=i%numCards;
		Colour c = knownColours[nextPlayer][i];
		int value = knownValues[nextPlayer][i];
		if(this.hands[nextPlayer][i]==null)
			return Integer.MIN_VALUE;
		if(c!=null && value!=0)
		{
			return (playable(this,c,value) ?Integer.MAX_VALUE: Integer.MIN_VALUE);
		}
		else if(c!=null) // only colour known
		{
			return (playable(this,c) ?Integer.MAX_VALUE: Integer.MIN_VALUE);
		}
		else if(value!=0) // only val known
		{
			return (playable(this,value) ?Integer.MAX_VALUE: Integer.MIN_VALUE);
		}
		else
		{
			return Integer.MIN_VALUE;
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
				return evaluateHint(this,this.nextPlayer,this.mapColourToInt(c.getColour()));		
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
				score=discardcard(i);
				Action a=new Action(nextPlayer,
						players[nextPlayer],
						ActionType.DISCARD,i%this.numCards);
				if(score!=Integer.MIN_VALUE)
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
		      s.cardsLeftInDeck =cardsLeftInDeck.clone();
		      for(int i=0;i<5;i++)
		      {
		    	  s.cardsLeftInDeck[i] = s.cardsLeftInDeck[i].clone();
		      }
		      s.deck = (Stack<Card>) this.deck.clone();
		      
		      s.knownColours = new Colour[s.numPlayers][s.numCards];
		      s.theyArrived = new int[s.numPlayers][s.numCards];
		      s.knownValues = new int[s.numPlayers][s.numCards];
		      for(int i=0;i<s.numPlayers;i++)
		      {
		    	  s.knownColours[i] = s.knownColours[i].clone();
		    	  s.theyArrived[i] =s.theyArrived[i].clone();
		    	  s.knownValues[i] = s.knownValues[i].clone();
		      }
 		      return s;
		    }
		    catch(CloneNotSupportedException e){return null;}	
	}
}
