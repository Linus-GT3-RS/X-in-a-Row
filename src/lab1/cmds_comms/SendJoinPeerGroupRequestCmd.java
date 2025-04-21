package lab1.cmds_comms;

import lab1.comms.Peer;

public record SendJoinPeerGroupRequestCmd(Peer friend) implements ICommunicationCommand {

}
