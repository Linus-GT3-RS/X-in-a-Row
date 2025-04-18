package lab1.commcmds;

import lab1.comms.Peer;

public record SendJoinPeerGroupRequestCmd(Peer friend) implements ICommunicationCommand {

}
