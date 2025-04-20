package lab1.gamecmds;

import lab1.game.Player;

public record SelectCellCommand(
		int r, 
		int c
		) 
implements IGameCommand {
}
