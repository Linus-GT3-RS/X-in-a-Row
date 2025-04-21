package lab1.cmds_comms;

import java.util.List;

import lab1.game.GameField;
import lab1.game.Player;

public record SendGamestateCmd(
		GameField field,
		List<Player> players
		)
implements ICommunicationCommand {

}
