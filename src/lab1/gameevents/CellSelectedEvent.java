package lab1.gameevents;

import java.awt.Color;

public record CellSelectedEvent(int r, int c, Color col) implements IGameEvent {
}
