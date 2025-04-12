package lab1.gameevents;

public record GameJoinedEvent(int rows, int cols) implements IGameEvent {
}
