package lab1.events_game;

import java.util.List;
import lab1.game.Player;

public record PlayersPointsChangedEvent(
		List<Player> players
		) 
implements IGameEvent {
}
