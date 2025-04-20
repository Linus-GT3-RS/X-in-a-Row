package lab1.comms;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import lab1.game.GameField;
import lab1.game.Player;

enum MsgType {
	GETPEERS, PEERLIST, JOINPEERGROUP, LEAVEPEERGROUP, 
	JOINREQUEST, GAMESTATE, LEAVEGAME
}

public class JSONHandler {

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
	
	public static GameField getGameField(JSONObject data) {		
		int rows = data.getJSONArray("gamefield").length();
		int cols = data.getJSONArray("gamefield").getJSONArray(0).length();
		var gamefield = new int[rows][cols];
		
		for(int i = 0; i < rows; i++) {
			var currow = data.getJSONArray("gamefield").getJSONArray(i);
			for(int j = 0; j < cols; j++) gamefield[i][j] = currow.getInt(j);
		}
		
		return new GameField(gamefield);		
	}
	
	public static List<Player> getPlayers(JSONObject data) {	
		var players = new ArrayList<Player>();
		
		var jsonarr = data.getJSONArray("players");
		for(int i = 0; i < jsonarr.length(); i++) {
			var curjson = jsonarr.getJSONObject(i);
			Player p = new Player(curjson.getString("username"));
			p.setPoints(curjson.getInt("points"));
			players.add(p);
		}
		
		return players;
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
	
	public static JSONObject gamestate2JSON(GameField field, List<Player> players) {
		var data = new JSONObject();
		
		// gamefield-data
		var gameFieldData = new JSONArray();
		for(int[] row : field.getCopy()) {
			var fdata = new JSONArray();
			for(int i : row) fdata.put(i);
			gameFieldData.put(fdata);
		}
		data.put("gamefield", gameFieldData);
		
		// player-data
		var playersData = new JSONArray();
		players.forEach(p -> {
			var pdata = new JSONObject();
			pdata.put("username", p.getName());
			pdata.put("points", p.getPoints());
			playersData.put(pdata);
		});
		data.put("players", playersData);
		
		return data;
	}

}
