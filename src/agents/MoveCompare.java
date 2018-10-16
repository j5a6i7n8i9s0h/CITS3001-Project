package agents;

import java.util.Comparator;

public class MoveCompare implements Comparator<Move> {

	@Override
	public int compare(Move o1, Move o2) {
		if(o1.getScore() < o2.getScore())
			return 1;
		if(o1.getScore() > o2.getScore())
			return -1;
		return 0;
	}
	
}
