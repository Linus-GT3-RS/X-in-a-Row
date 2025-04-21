package lab1.game;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

enum CommunicationKind {
	VPN, SINGLE_PC
}

public class Utils {

	// Prints name of caller func
	public static void logFunc() {
		String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
		System.out.println(methodName);
	}

	public static void logFunc(String msg) {
		String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
		System.out.println(methodName + " " + msg);
	}

	public static void logClass(Object c) {
		System.out.println(c.getClass().getSimpleName());
	}

	public static void log(String msg) {
		System.out.println(msg);
	}

	// ------------ IP -------------------------------------

	public static boolean isValidIP(String ip) {
		String regex =
				"^((25[0-5]|2[0-4]\\d|[0-1]?\\d\\d?)\\.){3}" +
						"(25[0-5]|2[0-4]\\d|[0-1]?\\d\\d?)$";

		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(ip);
		return matcher.matches();
	}
	
	// Flag for testing
	public static CommunicationKind connectionKind = CommunicationKind.SINGLE_PC;
	
	public static String getIP() {
		return switch(connectionKind) {
		case SINGLE_PC -> getLocalNonLoopbackIPv4();
		case VPN -> getPublicIPv4();		
		};
	}

	// does not work in VPN
	// does work without VPN
	private static String getLocalNonLoopbackIPv4() {
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
						return addr.getHostAddress();
					}
				}
			}

		} catch (SocketException e) {
			e.printStackTrace();
		}
		return null;        
	}

	// works in VPN
	// does not work without VPN
	private static String getPublicIPv4() {
		Enumeration<NetworkInterface> interfaces;
		try {
			interfaces = NetworkInterface.getNetworkInterfaces();
			while (interfaces.hasMoreElements()) {
				NetworkInterface iface = interfaces.nextElement();
				if (iface.isUp() && !iface.isLoopback()) {
					var addrs = iface.getInetAddresses();
					while (addrs.hasMoreElements()) {
						InetAddress addr = addrs.nextElement();
						if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
							String ip = addr.getHostAddress();

							// Filter raus, was privat ist:
							if (!ip.startsWith("10.") &&
									!ip.startsWith("192.168.") &&
									!ip.startsWith("172.16.") &&
									!ip.startsWith("172.17.") &&
									!ip.startsWith("172.18.") &&
									!ip.startsWith("172.19.") &&
									!ip.startsWith("172.2") && // covers 172.20–172.31
									!ip.startsWith("127.") &&
									!ip.startsWith("169.254")) {

								return ip; // Treffer!
							}
						}
					}
				}
			}
		} catch (SocketException e) {
			e.printStackTrace();
		}

		return null;
	}

}
