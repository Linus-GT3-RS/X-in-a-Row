package lab1.gameevents;

import lab1.game.GameField;

public record GameFieldChangedEvent(GameField field) implements IGameEvent {
}
