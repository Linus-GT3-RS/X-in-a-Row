package lab1.gameevents;

import java.util.List;
import lab1.game.Player;

public record PlayersPointsChangedEvent(
		List<Player> players
		) 
implements IGameEvent {
}
