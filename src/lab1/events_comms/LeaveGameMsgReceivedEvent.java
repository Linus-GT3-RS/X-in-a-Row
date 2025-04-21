package lab1.events_comms;

public record LeaveGameMsgReceivedEvent(
		String username
		) 
implements ICommunicationEvent {

}
