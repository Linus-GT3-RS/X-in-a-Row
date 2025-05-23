package lab1.comms;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import org.json.JSONObject;

import lab1.cmds_comms.ICommunicationCommand;
import lab1.cmds_comms.SendGamestateCmd;
import lab1.cmds_comms.SendJoinGameRequestCmd;
import lab1.cmds_comms.SendJoinPeerGroupRequestCmd;
import lab1.cmds_comms.SendLeaveGameCommand;
import lab1.cmds_comms.StartReceivingCommand;
import lab1.cmds_comms.StopReceivingCommand;
import lab1.events_comms.GamestateReceivedEvent;
import lab1.events_comms.JoinRequestReceivedEvent;
import lab1.events_comms.LeaveGameMsgReceivedEvent;
import lab1.events_comms.PeerGroupJoinedEvent;
import lab1.events_comms.PeerGroupLeftEvent;
import lab1.game.Utils;

public class PeerGroupCommunicator extends Communicator {

	// Peer2Peer
	private boolean isReceiving = false;
	private boolean stopReceiving = false;
	private Set<Peer> peergroup = new HashSet<Peer>();

	// UDP
	private DatagramSocket sendRecieveSocket;

	public PeerGroupCommunicator(int listenerPort) {
		peergroup = new HashSet<Peer>();
		try { this.sendRecieveSocket = new DatagramSocket(listenerPort); } 
		catch (SocketException e) { e.printStackTrace(); }
	}

	private Peer getMyself() {
		return new Peer(Utils.getIP(), sendRecieveSocket.getLocalPort());
	}
	

	// ------------- Send Messages ------------------------

	public void sendMsgP2P(String msg, Peer receiver) {
		System.out.println("Sending Point2Point msg using UDP: " + JSONHandler.getMessageType(msg));

		byte[] data = msg.getBytes();
		try {
			var packet = new DatagramPacket(data, data.length, receiver.getInetAddr(), receiver.port());
			sendRecieveSocket.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendMsgToGroup(String msg) {
		System.out.println("Sending group-msg using UDP: " + JSONHandler.getMessageType(msg));

		byte[] data = msg.getBytes();
		peergroup.forEach(p -> {
			try {
				var packet = new DatagramPacket(data, data.length, p.getInetAddr(), p.port());
				sendRecieveSocket.send(packet);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}

	// --------------------------------------------------------------------------------------
	// ------------------------------ COMMANDS --------------------------
	// --------------------------------------------------------------------------------------

	@Override
	public void processCommCmd(ICommunicationCommand cmd) {
		if(cmd instanceof SendJoinPeerGroupRequestCmd c) {
			Utils.logClass(c);
			onSendJoinPeerGroupRequestCmd(c);
		}
		else if(cmd instanceof SendLeaveGameCommand c) {
			Utils.logClass(c);
			onSendLeaveGameCommand(c);
		}
		else if(cmd instanceof SendJoinGameRequestCmd c) {
			Utils.logClass(c);
			onSendJoinGameRequestCmd(c);
		}
		else if(cmd instanceof SendGamestateCmd c) {
			Utils.logClass(c);
			onSendGamestateCmd(c);
		}
		else if(cmd instanceof StartReceivingCommand c) {
			Utils.logClass(c);
			onStartReceivingCommand(c);
		}
		else if(cmd instanceof StopReceivingCommand c) {
			Utils.logClass(c);
			onStopReceivingCommand(c);
		}
		else {
			System.out.println("Unknown ICommunication command");
		}
	}

	private void onStopReceivingCommand(StopReceivingCommand c) {
		stopReceiving = true;

		// send dummy udp-packet to myself so that blocked-receiver wakes up
		try {
			sendRecieveSocket.send(new DatagramPacket(new byte[] {0}, 1, getMyself().getInetAddr(), getMyself().port()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void onSendGamestateCmd(SendGamestateCmd c) {
		var data = JSONHandler.gamestate2JSON(c.field(), c.players());
		String msg = JSONHandler.toMsg(MsgType.GAMESTATE, data);
		sendMsgToGroup(msg);		
	}

	private void onSendJoinGameRequestCmd(SendJoinGameRequestCmd c) {
		var data = JSONHandler.name2JSON(c.username());
		String msg = JSONHandler.toMsg(MsgType.JOINREQUEST, data);
		sendMsgP2P(msg, peergroup.iterator().next());	// send msg to any 1 peer in group
	}

	public void onStartReceivingCommand(StartReceivingCommand c) {		
		if(isReceiving) return;
		isReceiving = true;

		new Thread(() -> {
			byte[] buffer = new byte[4096];

			try {
				// Infite Loop to receive msgs via UDP
				while (!stopReceiving) {
					var packet = new DatagramPacket(buffer, buffer.length);
					sendRecieveSocket.receive(packet);					

					String msg = new String(packet.getData(), 0, packet.getLength());	
					var sender = new Peer(packet.getAddress().getHostAddress(), packet.getPort());
					if(stopReceiving) break;
					processReceivedMsg(msg, sender);
				}

				isReceiving = false;
				stopReceiving = false;
				Utils.log("Stopped listening on IP " + getMyself().ip() + " on Port " + getMyself().port());
			}
			catch (Exception e) {
				e.printStackTrace();
				if(sendRecieveSocket != null) sendRecieveSocket.close();
			} 
		}).start();

		delayThread(10); // so that receiver thread can set up
		Utils.log("Listening on IP " + getMyself().ip() + " on Port " + getMyself().port());
	}

	private void onSendLeaveGameCommand(SendLeaveGameCommand c) {
		String msg1 = JSONHandler.toMsg(MsgType.LEAVEGAME, JSONHandler.name2JSON(c.username()));
		sendMsgToGroup(msg1);
		delayThread(10);

		String msg2 = JSONHandler.toMsg(MsgType.LEAVEPEERGROUP, JSONHandler.peer2JSON(getMyself()));
		sendMsgToGroup(msg2);
		delayThread(10);

		this.peergroup = new HashSet<Peer>(); // create empty
	}

	private void onSendJoinPeerGroupRequestCmd(SendJoinPeerGroupRequestCmd c) {;
	String msg = JSONHandler.toMsg(MsgType.GETPEERS, JSONHandler.peer2JSON(getMyself()));
	sendMsgP2P(msg, c.friend());
	}

	// --------------------------------------------------------------------------------------
	// ----------------------- RECEIVED MESSAGES --------------------------------------------
	//	--------------------------------------------------------------------------------------

	private void processReceivedMsg(String msg, Peer sender) {
		MsgType msgtype = JSONHandler.getMessageType(msg);
		JSONObject dataJSON = JSONHandler.getData(msg);

		switch (msgtype) {
		case GETPEERS -> onGetPeersMsg(dataJSON);
		case PEERLIST -> onPeerListMsg(dataJSON, sender);
		case JOINPEERGROUP -> onJoinPeerGroupMsg(dataJSON);
		case LEAVEPEERGROUP -> onLeavePeerGroupMsg(dataJSON);

		case JOINREQUEST -> onJoinRequestMsg(dataJSON);
		case GAMESTATE -> onGameStateMsg(dataJSON);
		case LEAVEGAME -> onLeaveGameMsg(dataJSON);

		default -> System.out.println("Unknown msgtype received, msg is:\n" + msg);
		}
	}

	private void onLeaveGameMsg(JSONObject receiveddata) {
		Utils.logFunc();

		this.sendCommEvent(new LeaveGameMsgReceivedEvent(JSONHandler.getUsername(receiveddata)));
	}

	private void onGetPeersMsg(JSONObject receiveddata) {
		Utils.logFunc();

		Peer receiver = JSONHandler.getPeer(receiveddata);
		String reply = JSONHandler.toMsg(MsgType.PEERLIST, JSONHandler.peers2JSON(peergroup));
		sendMsgP2P(reply, receiver);
	}

	private void onPeerListMsg(JSONObject receiveddata, Peer sender) {
		Utils.logFunc();

		this.peergroup = JSONHandler.getPeers(receiveddata);
		this.peergroup.add(sender);

		String msg = JSONHandler.toMsg(MsgType.JOINPEERGROUP, JSONHandler.peer2JSON(getMyself()));
		sendMsgToGroup(msg);

		delayThread(50);
		sendCommEvent(new PeerGroupJoinedEvent(1));
	}

	private void onJoinPeerGroupMsg(JSONObject receiveddata) {
		Utils.logFunc();

		Peer newMember = JSONHandler.getPeer(receiveddata);
		peergroup.add(newMember);
	}

	private void onLeavePeerGroupMsg(JSONObject receiveddata) {
		Utils.logFunc();

		Peer leavingMember = JSONHandler.getPeer(receiveddata);
		peergroup.remove(leavingMember);
	}

	private void onJoinRequestMsg(JSONObject receiveddata) {
		Utils.logFunc();

		var username = JSONHandler.getUsername(receiveddata);
		sendCommEvent(new JoinRequestReceivedEvent(username));
	}

	private void onGameStateMsg(JSONObject receiveddata) {
		Utils.logFunc();

		var field = JSONHandler.getGameField(receiveddata);
		var players = JSONHandler.getPlayers(receiveddata);
		sendCommEvent(new GamestateReceivedEvent(field, players));
	}

	// ------------------------ Utility Functions------------------------------------------

	private void delayThread(int ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
