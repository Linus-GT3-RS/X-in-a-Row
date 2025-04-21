package lab1.events_game;

import java.util.List;
import lab1.game.Cell;

public record CellsClearedEvent(List<Cell> cells) implements IGameEvent {
}
