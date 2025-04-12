package lab1.core;

import lab1.gamecmds.IGameCommand;

public record UDPMsgReceivedCommand(String msg) implements IGameCommand {
	
}
