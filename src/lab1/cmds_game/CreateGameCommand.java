package lab1.cmds_game;

import lab1.game.Player;

public record CreateGameCommand(
		int rows, 
		int cols, 
		int cellsNeededInRow, 
		Player player
		) 
		implements IGameCommand {
}
