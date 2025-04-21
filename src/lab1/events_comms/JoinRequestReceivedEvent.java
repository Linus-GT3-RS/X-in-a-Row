package lab1.events_comms;

public record JoinRequestReceivedEvent(String username) implements ICommunicationEvent {

}
