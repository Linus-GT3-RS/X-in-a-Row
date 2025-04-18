package lab1.comms;

import java.util.HashSet;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

enum MsgType {
	GETPEERS, PEERLIST, JOINPEERGROUP, LEAVEPEERGROUP, 
	JOINREQUEST, GAMESTATE
}

public class JSONCommHandler {

	public static MsgType getMessageType(String msg) {
		var msgtype = new JSONObject(msg).getString("msgtype");
		return MsgType.valueOf(msgtype.toUpperCase());
	}

	// -------------- From JSON ------------------------------------

	public static JSONObject getData(String msg) {
		return new JSONObject(msg).getJSONObject("data");
	}

	public static Peer getPeer(JSONObject data) {
		return new Peer(data.getString("ip"), data.getInt("port"));
	}

	public static Set<Peer> getPeers(JSONObject data) {
		var peers = new HashSet<Peer>();
		
		data.getJSONArray("peers").forEach(jsonobj -> {
			var j = (JSONObject)jsonobj;
			peers.add(new Peer(j.getString("ip"), j.getInt("port")));
		});
		
		return peers;		
	}
	
	public static String getUsername(JSONObject data) {
		return data.getString("username");
	}

	// -------------- Into JSON ------------------------------------

	public static String toMsg(MsgType msgtype, JSONObject data) {
		var json = new JSONObject();
		json.put("msgtype", msgtype.toString().toLowerCase());
		json.put("data", data);
		return json.toString(4); 
	}
	
	public static JSONObject peer2JSON(Peer p) {
		var json = new JSONObject();
		json.put("ip", p.ip());
		json.put("port", p.port());
		return json;		
	}

	public static JSONObject peers2JSON(Set<Peer> peers) {
		var json = new JSONObject();
		var jsonarray = new JSONArray();		
		peers.forEach(p -> jsonarray.put(peer2JSON(p)));		
		json.put("peers", jsonarray);
		return json;
	}
	
	public static JSONObject name2JSON(String username) {
		var json = new JSONObject();
		json.put("username", username);
		return json;
	}

}
