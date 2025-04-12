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

record Peer(String ip, int port) {}

public class UDPCommunicator {

	private List<Peer> friends;
	
	private boolean isReceiving = false;
	private DatagramSocket publisherSocket;
	
	public UDPCommunicator() {
		super();
		try { this.publisherSocket = new DatagramSocket(); } 
		catch (SocketException e) { e.printStackTrace(); }
	}

	public void startReceiving(int listenerPort, GameLogic gamelogic) {
		if(isReceiving) return;
		isReceiving = true;
		
		new Thread(() -> {	
			DatagramSocket socket = null;
			byte[] buffer = new byte[4096];
			try {
				socket = new DatagramSocket(listenerPort);

				while (true) {
					var packet = new DatagramPacket(buffer, buffer.length);
					socket.receive(packet);
					String msg = new String(packet.getData(), 0, packet.getLength());					
					gamelogic.processGameCommand(new UDPMsgReceivedCommand(msg));
				}
			} 
			catch (Exception e) {
				e.printStackTrace();
				if(socket != null) socket.close();
			} 
		}).start();
	}

	// Has to be called before each sendMsg()
	public void setReceivers(List<Peer> friends) {
		this.friends = friends;
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
