package agents;

import java.util.*;
import hanabAI.Action;
import hanabAI.ActionType;
import hanabAI.Card;
import hanabAI.Colour;
import hanabAI.IllegalActionException;
import hanabAI.State;

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
	private MyState previousState;
	/**
	 * A list of all moves made so far in the game, in the order they were played
	 **/
	private Action previousAction;
	/** The index of the next player to move **/
	private int nextPlayer = -1;
	/** The fnal play of the game (for when the deck runs out) **/
	private int finalAction = -1;

	public MyState(State s, int[][] cardsLeftInDeck, int[][] knownValues, Colour[][] knownColours) {
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

		this.numCards = (this.players.length > 3 ? 4 : 5);

		dealMyCards();
	}
	
 	private void dealMyCards(){
 	    for(int i = 0; i < numCards; i ++){
 	      if(knownValues[nextPlayer][i]> 0 && knownColours[nextPlayer][i] != null){
 	        cardsLeftInDeck[mapColourToInt(knownColours[nextPlayer][i])][knownValues[nextPlayer][i]]--;
 	        hands[nextPlayer][i] = new Card(knownColours[nextPlayer][i], knownValues[nextPlayer][i]);
 	      }
 	      else if(knownValues[nextPlayer][i]> 0 ){
 	      	int total = 0;
 	        for(Colour c: Colour.values()){
 	          total += cardsLeftInDeck[mapColourToInt(c)][knownValues[nextPlayer][i]];
 	        }
 	        int counter = 0;
 	        java.util.Random r = new java.util.Random();
 	 	      int pick = r.nextInt(total) + 1;
 	        while(pick > 0){
 	          total -= cardsLeftInDeck[counter++][knownValues[nextPlayer][i]];
 	        }
 	        cardsLeftInDeck[counter][knownValues[nextPlayer][i]]--;
 	        hands[nextPlayer][i] = new Card(mapToColour(counter), knownValues[nextPlayer][i]);
 	      }
 	      else if(knownColours[nextPlayer][i] != null){
 	        int total = 0;
 	        for(int j = 0; j < 5; j ++){
 	          total += cardsLeftInDeck[mapColourToInt(knownColours[nextPlayer][i])][j];
 	        }
 	        int counter = 0;
 	        java.util.Random r = new java.util.Random();
 	 	      int pick = r.nextInt(total) + 1;
 	        while(pick > 0){
 	          total -= cardsLeftInDeck[mapColourToInt(knownColours[nextPlayer][i])][counter++];
 	        }
 	        cardsLeftInDeck[mapColourToInt(knownColours[nextPlayer][i])][counter]--;
 	        hands[nextPlayer][i] = new Card(knownColours[nextPlayer][i], counter+1);
 	      }
 	      else{
 	        //we will deal totally random after deck has been constructed;
 	      }
 	    }
 	    
 	    makeDeck();
 	    
 	    for(int i = 0; i < numCards; i ++){
 	      if(hands[nextPlayer][i] == null){
 	        hands[nextPlayer][i] = deck.pop();
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

	public MyState hideHand(int observer) throws IllegalActionException, CloneNotSupportedException {
		if (this.observer == -1 && observer >= 0 && observer < hands.length) {
			MyState local = (MyState) this.clone();
			local.observer = observer;
			return local;
		} else
			throw new IllegalActionException("Hand already hidden, or observer out of bounds");
	}

	public boolean legalAction(Action a) throws IllegalActionException {
		if (observer != -1 && a.getPlayer() != observer)
			throw new IllegalActionException("Local states may only test the legality of observers moves");
		if (a.getPlayer() != nextPlayer)
			return false;
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
		if (observer != -1)
			throw new IllegalActionException("Next state unavailable!");
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
				s.hands[action.getPlayer()][action.getCard()] = deck.pop();
			if (deck.isEmpty()) {
				if (finalAction == -1)
					s.finalAction = order + players.length;
				s.hands[action.getPlayer()][action.getCard()] = null;
			}
			break;
		case DISCARD:
			c = hands[action.getPlayer()][action.getCard()];
			s.discards.push(c);
			if (!deck.isEmpty())
				s.hands[action.getPlayer()][action.getCard()] = deck.pop();
			if (deck.isEmpty()) {
				if (finalAction == -1)
					s.finalAction = order + players.length;
				s.hands[action.getPlayer()][action.getCard()] = null;
			}
			if (hints < 8)
				s.hints++;
			break;
		case HINT_COLOUR:
			s.hints--;
			break;
		case HINT_VALUE:
			s.hints--;
			break;
		default:
			break;
		}
		
		/*
		 * try {
			State t = (State) s.clone();
			for (int i = 0; i < Math.min(numPlayers - 1, s.getOrder()); i++) {
				Action a = t.getPreviousAction();
				if ((a.getType() == ActionType.HINT_COLOUR || a.getType() == ActionType.HINT_VALUE)) {
					boolean[] hints = t.getPreviousAction().getHintedCards();
					for (int j = 0; j < hints.length; j++) {
						if (hints[j]) {
							if (a.getType() == ActionType.HINT_COLOUR)
								knownColours[a.getHintReceiver()][j] = a.getColour();
							else
								knownValues[a.getHintReceiver()][j] = a.getValue();
						}
					}
				} else {
					Card c = t.getPreviousState().getHand(a.getPlayer())[a.getCard()];
					cardsLeftInPlay[mapColourToInt(c.getColour())][c.getValue() - 1]--;
					theyArrived[a.getPlayer()][a.getCard()] = t.getOrder();
					totalCards--;
				}

				t = t.getPreviousState();
			}
		} catch (IllegalActionException e) {
			e.printStackTrace();
		}
		 * 
		 * 
		 * */
		s.order++;
		s.previousAction = action;
		s.nextPlayer = (nextPlayer + 1) % players.length;
		s.previousState = this;
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
		// TODO Auto-generated method stub
		return (order == finalAction || fuse == 0 || getScore() == 25);
	}

	public int Rollout() throws CloneNotSupportedException, IllegalActionException {
    int score = 0;
    MyState s = (MyState) this.clone();
	    while(!s.gameOver())
	      s = s.nextState(doAction(s), deck);
	return s.getScore();
	}

public Action doAction(MyState s) {
		index = s.getNextPlayer();
		updateLastActions(s);

		int maxValue = 0;
		int bestHint = -1;

		for (int i = 0; i < 10; i++) {
			int value = evaluateHint(s, (index + 1) % numPlayers, i);
			if (value > maxValue) {
				maxValue = value;
				bestHint = i;
			}
		}

		try {
			Action a = playKnown(s);

			if (maxValue >= 15) {
				if (a == null)
					a = hint(s, (index + 1) % numPlayers, bestHint);
				if (a == null)
					a = discardKnown(s);

			} else {
				if (a == null)
					a = discardKnown(s);
				if (maxValue > 5) {
					if (a == null)
						a = hint(s, (index + 1) % numPlayers, bestHint);
				}
			}

			if (a == null)
				a = discardOldest(s);
			if (a == null)
				a = hint(s, (index + 1) % numPlayers, bestHint);
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

		if (s.hints == 0) {
			return null;
		}

		boolean[] match = new boolean[numCards];

		if (hint > 4) {

			for (int i = 0; i < numCards; i++) {
				if (s.hands[p][i].getValue() == hint - 4) {
					match[i] = true;
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
				}
			}

			return new Action(index, toString(), ActionType.HINT_COLOUR, p, match, mapToColour(hint));
		}
	}

	private Action discardOldest(State s) throws IllegalActionException {

		if (s.getHintTokens() == 8) {
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
			return new Action(index, toString(), ActionType.DISCARD, oldest);
		}

	}

	private Action guess(State s) throws IllegalActionException {
		double probCorrectPlay = 0;
		int play = -1;

		double probCorrectDiscard = 0;
		int discard = -1;

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
					numPlayable = cardsLeftInPlay[mapColourToInt(c)][top] - howManySee(s, new Card(c, top + 1));
				}
				for (int j = 0; j < top; j++) {
					numDiscardable += cardsLeftInPlay[mapColourToInt(c)][j] - howManySee(s, new Card(c, j + 1));
				}
				for (int j = 0; j < 5; j++) {
					numTotal += cardsLeftInPlay[mapColourToInt(c)][j] - howManySee(s, new Card(c, j + 1));
				}
			} else if (knownValues[index][i] > 0) {
				int card = knownValues[index][i];
				for (Colour c : Colour.values()) {
					if (card == topFw(s, c) + 1) {
						numPlayable += cardsLeftInPlay[mapColourToInt(c)][card - 1] - howManySee(s, new Card(c, card));
					} else if (card <= topFw(s, c)) {
						numDiscardable += cardsLeftInPlay[mapColourToInt(c)][card - 1]
								- howManySee(s, new Card(c, card));
					}
					numTotal += cardsLeftInPlay[mapColourToInt(c)][card - 1] - howManySee(s, new Card(c, card));
				}
			} else {
				for (Colour c : Colour.values()) {
					int top = topFw(s, c);
					if (top == 5) {
						continue;
					}
					numPlayable += cardsLeftInPlay[mapColourToInt(c)][top] - howManySee(s, new Card(c, top + 1));
					for (int j = 0; j < top; j++) {
						numDiscardable += cardsLeftInPlay[mapColourToInt(c)][j] - howManySee(s, new Card(c, j + 1));
					}

				}
				numTotal = totalCards;
				for (int p = 0; p < numPlayers; p++) {
					if (p == index) {
						continue;
					}
					for (int j = 0; j < numCards; j++) {
						if (s.getHand(p)[j] == null) {
							continue;
						}
						numTotal -= 1;
					}
				}
			}

			double playProb = numPlayable / numTotal;
			if (playProb > probCorrectPlay) {
				probCorrectPlay = playProb;
				play = i;
			}

			double disProb = numDiscardable / numTotal;
			if (disProb > probCorrectDiscard) {
				probCorrectDiscard = disProb;
				discard = i;
			}

		}

		if (probCorrectDiscard >= probCorrectPlay || s.getFuseTokens() < 2) {
			knownColours[index][discard] = null;
			knownValues[index][discard] = 0;
			theyArrived[index][discard] = s.getOrder();
			totalCards--;

			return new Action(index, toString(), ActionType.DISCARD, discard);
		} else {
			knownColours[index][play] = null;
			knownValues[index][play] = 0;
			theyArrived[index][play] = s.getOrder();
			totalCards--;

			return new Action(index, toString(), ActionType.PLAY, play);
		}

	}

	// helper classes and functions

	private int evaluateHint(MyState s, int p, int hint) {
		// 0:blue,1:red... (int to colour)
		// 5:hint 1, 6:hint2, 7:hint3, 8:hint4, 9:hint5

		// first check if it will reveal a playable card worth 15.

		// then check if it will save a final in play card worth 5.

		// if it will reveal a can discard worth 4

		// then check how much information it will reveal worth 1 each.

		int score = 0;

		score += 15 * hintMakesPlayable(s, p, hint);
		score += 4 * hintMakesDiscardable(s, p, hint);

		boolean[] finals = finalCards(s, p);

		for (int i = 0; i < numCards; i++) {
			if (hint > 4) {
				if (knownValues[p][i] == 0 && s.getHand(p)[i].getValue() == (hint - 4)) {
					score += 1;
					if (finals[i]) {
						score += 5;
					}
				}
			} else {
				if (knownColours[p][i] == null && s.getHand(p)[i].getColour() == mapToColour(hint)) {
					score += 1;
					if (finals[i]) {
						score += 5;
					}
				}
			}
		}

		return score;
	}

	private int hintMakesDiscardable(State s, int p, int hint) {
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

		int[][] tempV = knownValues.clone();
		Colour[][] tempC = knownColours.clone();

		if (hint > 4) {
			for (int i = 0; i < numCards; i++) {
				if (s.getHand(p)[i] == null) {
					continue;
				}
				if (s.getHand(p)[i].getValue() == (hint - 4)) {
					tempV[p][i] = hint - 4;
				}
			}
		} else {
			for (int i = 0; i < numCards; i++) {
				if (s.getHand(p)[i] == null) {
					continue;
				}
				if (s.getHand(p)[i].getColour() == mapToColour(hint)) {
					tempC[p][i] = mapToColour(hint);
				}
			}
		}

		for (int i = 0; i < numCards; i++) {
			if (knownColours[p][i] != null && knownValues[p][i] != 0) {
				if (discardable(s, knownColours[p][i], knownValues[p][i])) {
					discardAfter++;
				}
			} else if (knownValues[p][i] != 0) {
				if (discardable(s, knownValues[p][i])) {
					discardAfter++;
				}
			} else if (knownColours[p][i] != null) {
				if (discardable(s, knownColours[p][i])) {
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

		int[][] tempV = knownValues.clone();
		Colour[][] tempC = knownColours.clone();

		if (hint > 4) {
			for (int i = 0; i < numCards; i++) {
				if (s.hands[p][i] == null) {
					continue;
				}
				if (ss.hands[p][i].getValue() == (hint - 4)) {
					tempV[p][i] = hint - 4;
				}
			}
		} else {
			for (int i = 0; i < numCards; i++) {
				if (s.hands[p][i] == null) {
					continue;
				}
				if (s.hands[p][i].getColour() == mapToColour(hint)) {
					tempC[p][i] = mapToColour(hint);
				}
			}
		}

		for (int i = 0; i < numCards; i++) {
			if (knownColours[p][i] != null && knownValues[p][i] != 0) {
				if (playable(s, knownColours[p][i], knownValues[p][i])) {
					playableAfter++;
				}
			} else if (knownValues[p][i] != 0) {
				if (playable(s, knownValues[p][i])) {
					playableAfter++;
				}
			} else if (knownColours[p][i] != null) {
				if (playable(s, knownColours[p][i])) {
					playableAfter++;
				}
			} else {
				// know nothing of the card
			}
		}

		return playableAfter - playableBefore;
	}

	private boolean[] finalCards(State s, int p) {

		if (p == index) {
			return null;
		}

		boolean[] finals = new boolean[numCards];
		for (int i = 0; i < numCards; i++) {
			Card c = s.getHand(p)[i];
			if (c == null) {
				continue;
			}
			int inPlay = cardsLeftInPlay[mapColourToInt(c.getColour())][c.getValue() - 1];
			int iSee = howManySee(s, c);
			if (inPlay == 1 || iSee == 1) {
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

	private boolean playable(MyState s, Colour c, int i) {
		int top = topFw(s, c);
		return top == i - 1;
	}

	private boolean playable(MyState s, int i) {
		boolean[] canPlay = new boolean[5];

		for (Colour c : Colour.values()) {
			canPlay[mapColourToInt(c)] = (topFw(s, c) == i - 1);
		}

		boolean playable = true;

		for (int j = 0; j < 5; j++) {
			if (!canPlay[j]) {
				int inPlay = cardsLeftInPlay[j][i - 1] - howManySee(s, new Card(mapToColour(j), i));
				if (inPlay != 0) {
					playable = false;
				}
			}
		}

		return playable;
	}

	private boolean playable(State s, Colour c) {

		int toPlay;

		int top = topFw(s, c);
		if (top == 5)
			return false;
		else
			toPlay = top + 1;

		boolean playable = true;

		for (int j = 0; j < 5; j++) {
			if (j != toPlay) {
				int inPlay = cardsLeftInPlay[mapColourToInt(c)][j] - howManySee(s, new Card(c, j + 1));
				if (inPlay != 0) {
					playable = false;
				}
			}
		}

		return playable;
	}

	private boolean discardable(MyState s, Colour c, int i) {
		int top = topFw(s, c);
		return top >= i;
	}

	private boolean discardable(MyState s, int i) {
		boolean[] canDiscard = new boolean[5];

		for (Colour c : Colour.values()) {
			canDiscard[mapColourToInt(c)] = (topFw(s, c) >= i);
		}

		boolean discardable = true;

		for (int j = 0; j < 5; j++) {
			if (!canDiscard[j]) {
				int inPlay = cardsLeftInPlay[j][i - 1] - howManySee(s, new Card(mapToColour(j), i));
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
			toPlay = top + 1;

		boolean discardable = true;

		for (int j = 0; j < 5; j++) {
			if (j >= toPlay) {
				int inPlay = cardsLeftInPlay[mapColourToInt(c)][j] - howManySee(s, new Card(c, j + 1));
				if (inPlay != 0) {
					discardable = false;
				}
			}
		}

		return discardable;
	}

	private int howManySee(MyState s, Card c) {

		int count = 0;
		for (int i = 0; i < numPlayers; i++) {
			if (i == index) {
				continue;
			}
			for (int j = 0; j < numCards; j++) {
				if (s.hands[i][j] == null) {
					continue;
				}
				if (s.hands[i][j].equals(c)) {
					count++;
				}
			}
		}

		return count;
	}

	private void updateLastActions(MyState s) {
		try {
			State t = (State) s.clone();
			for (int i = 0; i < Math.min(numPlayers - 1, s.order); i++) {
				Action a = t.getPreviousAction();
				if ((a.getType() == ActionType.HINT_COLOUR || a.getType() == ActionType.HINT_VALUE)) {
					boolean[] hints = t.getPreviousAction().getHintedCards();
					for (int j = 0; j < hints.length; j++) {
						if (hints[j]) {
							if (a.getType() == ActionType.HINT_COLOUR)
								knownColours[a.getHintReceiver()][j] = a.getColour();
							else
								knownValues[a.getHintReceiver()][j] = a.getValue();
						}
					}
				} else {
					Card c = t.getPreviousState().getHand(a.getPlayer())[a.getCard()];
					cardsLeftInPlay[mapColourToInt(c.getColour())][c.getValue() - 1]--;
					theyArrived[a.getPlayer()][a.getCard()] = t.getOrder();
					totalCards--;
				}

				t = t.getPreviousState();
			}
		} catch (IllegalActionException e) {
			e.printStackTrace();
		}
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
}
