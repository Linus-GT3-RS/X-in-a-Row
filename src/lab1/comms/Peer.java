package lab1.comms;

import java.net.InetAddress;
import java.net.UnknownHostException;

public record Peer(String ip, int port) {
	
	public InetAddress getInetAddr() {
		try {
			return InetAddress.getByName(ip);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
