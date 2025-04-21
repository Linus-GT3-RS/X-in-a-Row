package lab1.events_game;

import java.util.List;

import lab1.game.GameField;
import lab1.game.Player;

public record GamestateUpdatedEvent(
		GameField field,
		List<Player> players
		)
implements IGameEvent{

}
