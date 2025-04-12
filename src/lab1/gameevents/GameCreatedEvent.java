package lab1.gameevents;

public record GameCreatedEvent(int rows, int cols) implements IGameEvent {
}
