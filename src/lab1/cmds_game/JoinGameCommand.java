package lab1.cmds_game;

import lab1.comms.Peer;

public record JoinGameCommand
		(
		Peer friend, 
		String playerName
		) 
		implements IGameCommand {
}
