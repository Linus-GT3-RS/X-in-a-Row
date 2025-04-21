package lab1.events_game;

import java.awt.Color;

public record CellSelectedEvent(int r, int c, Color col) implements IGameEvent {
}
