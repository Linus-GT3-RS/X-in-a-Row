package lab1.gamecmds;

public record JoinGameCommand(int friendID, String playerName) implements IGameCommand {
}
