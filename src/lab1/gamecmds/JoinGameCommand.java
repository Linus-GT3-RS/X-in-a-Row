package lab1.gamecmds;

import lab1.comms.Peer;

public record JoinGameCommand
		(
		Peer friend, 
		String playerName
		) 
		implements IGameCommand {
}
