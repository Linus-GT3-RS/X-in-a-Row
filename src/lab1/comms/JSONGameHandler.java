package lab1.comms;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Map;

import lab1.game.GameField;
import lab1.game.Player;

public class JSONGameHandler {
	
//	public static UDPMsgType getUDPMsgType(String msg) {
//		var json = new JSONObject(msg);
//		String msgtype = json.getString("msgtype");
//		if(msgtype.equals("gamestate")) {
//			return UDPMsgType.GAMESTATE;
//		} else if(msgtype.equals("joinrequest")) {
//			return UDPMsgType.JOINREQUEST;
//		}
//		return null;
//	}
	
	public static String getUsername(String msg) {
		return new JSONObject(msg).getJSONObject("data").getString("username");
	}
	
	public static int[][] getGameField(String msg) {
		var data = new JSONObject(msg).getJSONObject("data");
		
		int rows = data.getJSONArray("gamefield").length();
		int cols = data.getJSONArray("gamefield").getJSONArray(0).length();
		var gamefield = new int[rows][cols];
		
		for(int i = 0; i < rows; i++) {
			var currow = data.getJSONArray("gamefield").getJSONArray(i);
			for(int j = 0; j < cols; j++) gamefield[i][j] = currow.getInt(j);
		}
		
		return gamefield;		
	}
	
	public static Map<Integer, Player> getPlayers(String msg) {
		var data = new JSONObject(msg).getJSONObject("data");		
		var players = new LinkedHashMap<Integer, Player>();
		
		int playercount = data.getJSONArray("players").length();
		for(int i = 0; i < playercount; i++) {
			var curjson = data.getJSONArray("players").getJSONObject(i);
			Player p = new Player(PeerFactory.START_PORT + i, curjson.getString("username"));
			p.setPoints(curjson.getInt("points"));
			players.put(i, p);
		}
		
		return players;
	}
	
	public static String toJoinrequestMsg(String username) {
		var json = new JSONObject();
		json.put("msgtype", "joinrequest");
		json.put("data", new JSONObject().put("username", username));
		return json.toString(4);
	}
	
	public static String toGameStateMsg(GameField field, Map<Integer, Player> players) {
		var json = new JSONObject();	
		json.put("msgtype", "gamestate");
		
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
		players.forEach((k, v) -> {
			var pdata = new JSONObject();
			pdata.put("username", v.getName());
			pdata.put("points", v.getPoints());
			playersData.put(pdata);
		});
		data.put("players", playersData);
		
		json.put("data", data);
		
		return json.toString(4);
	}

}
