package lab1.comms;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lab1.core.GameLogic;
import lab1.core.UDPMsgReceivedCommand;
import lab1.game.Player;
import lab1.gamecmds.IGameCommand;

public class UDPCommunicator {

	private List<Peer> friends;
	
	private boolean isReceiving = false;
	private DatagramSocket publisherSocket;
	
	public UDPCommunicator() {
		super();
		try { this.publisherSocket = new DatagramSocket(); } 
		catch (SocketException e) { e.printStackTrace(); }
	}
	
	public void sendMsg(String msg) {
		byte[] data = msg.getBytes();

		friends.forEach((Peer friend) -> {
			try {
				DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getByName(friend.ip()), friend.port());
				publisherSocket.send(packet);
			}
			catch (Exception e) { e.printStackTrace(); }
		});
		
		System.out.println("Sending via UDP Finished");
	}

}
