package lab1.gameevents;

import java.util.List;

import lab1.game.GameField;
import lab1.game.Player;

public record GameInitializedEvent(
//		GameField field,
//		List<Player> players
		int rowcount,
		int colcount
		) 
		implements IGameEvent {

}
