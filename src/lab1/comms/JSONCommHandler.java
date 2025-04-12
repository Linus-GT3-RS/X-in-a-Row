package lab1.comms;

import org.json.JSONObject;

enum MsgType {
	GETPEERS, PEERLIST, JOINPEERGROUP, LEAVEPEERGROUP, JOINREQUEST, GAMESTATE, UNKNOWN
}

public class JSONCommHandler {
	
	public static MsgType getMessageType(String msg) {
		var json = new JSONObject(msg);
		
		return switch(json.getString("msgtype")) {
		case "getpeers" -> MsgType.GETPEERS;
		case "peerlist" -> MsgType.PEERLIST;
		case "joinpeergroup" -> MsgType.JOINPEERGROUP;
		case "leavepeergroup" -> MsgType.LEAVEPEERGROUP;
		
		case "joinrequest" -> MsgType.JOINREQUEST;
		case "gamestate" -> MsgType.GAMESTATE;
		
		default -> MsgType.UNKNOWN;
		};
	}
	
	public static JSONObject getDataJSON(String msg) {
		return new JSONObject(msg).getJSONObject("data");
	}
	
}
