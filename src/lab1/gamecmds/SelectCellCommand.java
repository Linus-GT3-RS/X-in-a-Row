package lab1.gamecmds;

import lab1.game.Player;

public record SelectCellCommand(int r, int c, Player playerGUI) implements IGameCommand {
}
