package lab1.commcmds;

import lab1.comms.Peer;

public record SendJoinGameRequestCmd(Peer friend) implements ICommunicationCommand {

}
