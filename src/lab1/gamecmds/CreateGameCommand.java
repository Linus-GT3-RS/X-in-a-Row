package lab1.gamecmds;

public record CreateGameCommand(int rows, int cols, int cellsNeededInRow) implements IGameCommand {
}
