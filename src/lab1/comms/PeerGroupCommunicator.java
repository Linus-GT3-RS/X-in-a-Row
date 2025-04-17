package lab1.comms;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashSet;
import java.util.Set;

import org.json.JSONObject;

import lab1.commcmds.ICommunicationCommand;
import lab1.commcmds.StartReceivingCmd;
import lab1.core.UDPMsgReceivedCommand;

public class PeerGroupCommunicator extends Communicator {

	// Peer2Peer
	private boolean isReceiving = false;
	private Set<Peer> peergroup;

	// UDP
	private DatagramSocket senderSocket;
	private DatagramSocket receiverSocket;
	
	public PeerGroupCommunicator() {
		peergroup = new HashSet<Peer>();
		try { this.senderSocket = new DatagramSocket(); } 
		catch (SocketException e) { e.printStackTrace(); }
	}
	
	// ------------- Send Messages ------------------------
	
	public void sendMsgP2P(String msg, Peer receiver) {
		System.out.println("Sending Point2Point msg using UDP");
		
		byte[] data = msg.getBytes();
		try {
			var packet = new DatagramPacket(data, data.length, receiver.getInetAddr(), receiver.port());
			senderSocket.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void sendMsgToGroup(String msg) {
		System.out.println("Sending group-msg using UDP");
		
		byte[] data = msg.getBytes();
		peergroup.forEach(p -> {
			try {
				var packet = new DatagramPacket(data, data.length, p.getInetAddr(), p.port());
				senderSocket.send(packet);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}

	// ---------------- Process-Functions --------------------------
	
	@Override
	public void processCommCmd(ICommunicationCommand cmd) {
		if(cmd instanceof StartReceivingCmd c) {
			System.out.println("StartReceivingCmd");
			onStartReceivingCmd(c);
		}
		else {
			System.out.println("Unknown ICommunication command");
		}
	}

	private void processReceivedMsg(String msg, Peer sender) {
		MsgType msgtype = JSONCommHandler.getMessageType(msg);
		JSONObject dataJSON = JSONCommHandler.getData(msg);

		switch (msgtype) {
		case GETPEERS -> onGetPeersMsg(dataJSON);
		case PEERLIST -> onPeerListMsg(dataJSON, sender);
		case JOINPEERGROUP -> onJoinPeerGroupMsg(dataJSON);
		case LEAVEPEERGROUP -> onLeavePeerGroupMsg(dataJSON);
		
		case JOINREQUEST -> onJoinRequestMsg(dataJSON);
		case GAMESTATE -> onGameStateMsg(dataJSON);
		
		default -> System.out.println("Unknown msgtype received, msg is:\n" + msg);
		}
	}

	// ------------------------ Callbacks Received Msg ------------------------------------------
	
	private void onGetPeersMsg(JSONObject receiveddata) {
		Peer receiver = JSONCommHandler.getPeer(receiveddata);
		String reply = JSONCommHandler.toMsg(MsgType.PEERLIST, JSONCommHandler.peers2JSON(peergroup));
		sendMsgP2P(reply, receiver);
    }

    private void onPeerListMsg(JSONObject receiveddata, Peer sender) {
    	this.peergroup = JSONCommHandler.getPeers(receiveddata);
    	this.peergroup.add(sender);
    }

    private void onJoinPeerGroupMsg(JSONObject receiveddata) {
    	Peer newMember = JSONCommHandler.getPeer(receiveddata);
    	peergroup.add(newMember);
    }

    private void onLeavePeerGroupMsg(JSONObject receiveddata) {
    	Peer leavingMember = JSONCommHandler.getPeer(receiveddata);
    	peergroup.remove(leavingMember);
    }

    private void onJoinRequestMsg(JSONObject receiveddata) {
    	// TODO
    }

    private void onGameStateMsg(JSONObject receiveddata) {
    	// TODO
    }

	// ------------------------ Callbacks Commands ------------------------------------------

	private void onStartReceivingCmd(StartReceivingCmd cmd) {
		if(isReceiving) return;
		isReceiving = true;

		new Thread(() -> {
			byte[] buffer = new byte[4096];

			try {
				receiverSocket = new DatagramSocket(cmd.port());

				// Infite Loop to receive msgs via UDP
				while (true) {
					var packet = new DatagramPacket(buffer, buffer.length);
					receiverSocket.receive(packet);

					String msg = new String(packet.getData(), 0, packet.getLength());	
					var sender = new Peer(packet.getAddress().getHostAddress(), packet.getPort());
					processReceivedMsg(msg, sender);
				}
			} 
			catch (Exception e) {
				e.printStackTrace();
				if(receiverSocket != null) receiverSocket.close();
			} 
		}).start();
	}

	

}
