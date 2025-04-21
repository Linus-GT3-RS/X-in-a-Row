package lab1.events_comms;

import java.util.List;

import lab1.game.GameField;
import lab1.game.Player;

public record GamestateReceivedEvent(
		GameField field,
		List<Player> players
		) 
implements ICommunicationEvent{

}
