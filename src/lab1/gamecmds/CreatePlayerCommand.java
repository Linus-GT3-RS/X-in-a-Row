package lab1.gamecmds;

import lab1.game.Player;

public record CreatePlayerCommand(Player p) implements IGameCommand {
}
