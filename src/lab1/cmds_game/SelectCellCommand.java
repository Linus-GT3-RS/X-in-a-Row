package lab1.cmds_game;

import lab1.game.Player;

public record SelectCellCommand(
		int r, 
		int c
		) 
implements IGameCommand {
}
