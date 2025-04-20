package lab1.commevents;

public record LeaveGameMsgReceivedEvent(
		String username
		) 
implements ICommunicationEvent {

}
