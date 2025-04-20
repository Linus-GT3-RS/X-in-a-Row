package lab1.commcmds;

public record SendLeaveGameCommand(
		String username
		) implements ICommunicationCommand {

}
