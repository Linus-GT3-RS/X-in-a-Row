package lab1.cmds_comms;

public record SendLeaveGameCommand(
		String username
		) implements ICommunicationCommand {

}
