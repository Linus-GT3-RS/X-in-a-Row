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

import lab1.commcmds.ICommunicationCommand;
import lab1.commcmds.SendJoinGameRequestCmd;
import lab1.commcmds.SendLeaveGameCmd;
import lab1.core.UDPMsgReceivedCommand;

public class PeerGroupCommunicator extends Communicator {

	// Peer2Peer
	private boolean isReceiving = false;
	private Set<Peer> peergroup = new HashSet<Peer>();

	// UDP
	private DatagramSocket sendRecieveSocket;

	public PeerGroupCommunicator(int listenerPort) {
		peergroup = new HashSet<Peer>();
		try { this.sendRecieveSocket = new DatagramSocket(listenerPort); } 
		catch (SocketException e) { e.printStackTrace(); }
		
		startReceiverThread();
	}
	
	private Peer getMyself() {
		String myIP = getLocalNonLoopbackIPv4().getHostAddress();
		return new Peer(myIP, sendRecieveSocket.getLocalPort());
	}
	
	// Gibt die erste nicht-loopback IPv4-Adresse zurück
    private static InetAddress getLocalNonLoopbackIPv4() {
        Enumeration<NetworkInterface> interfaces;
		try {
			interfaces = NetworkInterface.getNetworkInterfaces();
			while (interfaces.hasMoreElements()) {
	            NetworkInterface ni = interfaces.nextElement();
	            if (!ni.isUp() || ni.isLoopback()) continue;

	            Enumeration<InetAddress> addresses = ni.getInetAddresses();
	            while (addresses.hasMoreElements()) {
	                InetAddress addr = addresses.nextElement();
	                if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
	                    return addr;
	                }
	            }
	        }
			
		} catch (SocketException e) {
			e.printStackTrace();
		}
		return null;        
    }

	// ------------- Send Messages ------------------------

	public void sendMsgP2P(String msg, Peer receiver) {
		System.out.println("Sending Point2Point msg using UDP: " + JSONCommHandler.getMessageType(msg));

		byte[] data = msg.getBytes();
		try {
			var packet = new DatagramPacket(data, data.length, receiver.getInetAddr(), receiver.port());
			sendRecieveSocket.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendMsgToGroup(String msg) {
		System.out.println("Sending group-msg using UDP: " + JSONCommHandler.getMessageType(msg));

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

	// ---------------- Process-Functions --------------------------

	@Override
	public void processCommCmd(ICommunicationCommand cmd) {
		if(cmd instanceof SendJoinGameRequestCmd c) {
			System.out.println("SendJoinGameRequestCmd");
			onSendJoinGameRequestCmd(c);
		}
		else if(cmd instanceof SendLeaveGameCmd c) {
			System.out.println("SendLeaveGameCmd");
			onSendLeaveGameCmd(c);
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
		System.out.println("onGetPeersMsg");
		
		Peer receiver = JSONCommHandler.getPeer(receiveddata);
		String reply = JSONCommHandler.toMsg(MsgType.PEERLIST, JSONCommHandler.peers2JSON(peergroup));
		sendMsgP2P(reply, receiver);
	}

	private void onPeerListMsg(JSONObject receiveddata, Peer sender) {
		System.out.println("onPeerListMsg");
		
		this.peergroup = JSONCommHandler.getPeers(receiveddata);
		this.peergroup.add(sender);
		
		String msg = JSONCommHandler.toMsg(MsgType.JOINPEERGROUP, JSONCommHandler.peer2JSON(getMyself()));
		sendMsgToGroup(msg);
	}

	private void onJoinPeerGroupMsg(JSONObject receiveddata) {
		System.out.println("onJoinPeerGroupMsg");
		
		Peer newMember = JSONCommHandler.getPeer(receiveddata);
		peergroup.add(newMember);
	}

	private void onLeavePeerGroupMsg(JSONObject receiveddata) {
		System.out.println("onLeavePeerGroupMsg");
		
		Peer leavingMember = JSONCommHandler.getPeer(receiveddata);
		peergroup.remove(leavingMember);
	}

	private void onJoinRequestMsg(JSONObject receiveddata) {
		System.out.println("onJoinRequestMsg");
		// TODO
	}

	private void onGameStateMsg(JSONObject receiveddata) {
		System.out.println("onGameStateMsg");
		// TODO
	}

	// ------------------------ Callbacks Commands ------------------------------------------

	private void startReceiverThread() {
		if(isReceiving) return;
		isReceiving = true;
		System.out.println("Starting ReceiverThread");

		new Thread(() -> {
			byte[] buffer = new byte[4096];

			try {
				// Infite Loop to receive msgs via UDP
				while (true) {
					var packet = new DatagramPacket(buffer, buffer.length);
					sendRecieveSocket.receive(packet);

					String msg = new String(packet.getData(), 0, packet.getLength());	
					var sender = new Peer(packet.getAddress().getHostAddress(), packet.getPort());
					processReceivedMsg(msg, sender);
				}
			} 
			catch (Exception e) {
				e.printStackTrace();
				if(sendRecieveSocket != null) sendRecieveSocket.close();
			} 
		}).start();

		// delay so that receiver thread can set up
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void onSendLeaveGameCmd(SendLeaveGameCmd c) {
		String msg = JSONCommHandler.toMsg(MsgType.LEAVEPEERGROUP, JSONCommHandler.peer2JSON(getMyself()));
		sendMsgToGroup(msg);
		this.peergroup = new HashSet<Peer>(); // create empty
	}

	private void onSendJoinGameRequestCmd(SendJoinGameRequestCmd c) {;
		String msg = JSONCommHandler.toMsg(MsgType.GETPEERS, JSONCommHandler.peer2JSON(getMyself()));
		sendMsgP2P(msg, c.friend());
	}



}
