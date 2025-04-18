package lab1.core;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lab1.Utils;
import lab1.commcmds.SendJoinGameRequestCmd;
import lab1.commevents.ICommunicationEvent;
import lab1.commevents.JoinRequestReceivedEvent;
import lab1.commevents.PeerGroupJoinedEvent;
import lab1.commevents.PeerGroupLeftEvent;
import lab1.comms.Communicator;
import lab1.comms.ICommunicationEventListener;
import lab1.comms.JSONGameHandler;
import lab1.comms.PeerGroupCommunicator;
import lab1.game.Cell;
import lab1.game.GameField;
import lab1.game.Player;
import lab1.gamecmds.CreateGameCommand;
import lab1.gamecmds.CreatePlayerCommand;
import lab1.gamecmds.JoinGameCommand;
import lab1.gamecmds.ResetGridCommand;
import lab1.gamecmds.SelectCellCommand;
import lab1.gameevents.CellSelectedEvent;
import lab1.gameevents.CellsClearedEvent;
import lab1.gameevents.GameCreatedEvent;
import lab1.gameevents.GameFieldChangedEvent;
import lab1.gameevents.GameJoinedEvent;
import lab1.gameevents.IGameEvent;
import lab1.gameevents.PlayerPointsChangedEvent;

enum Direction {
	LEFT(0, -1), UP_LEFT(-1, -1), UP(-1, 0), UP_RIGHT(-1, 1),
	RIGHT(0, 1), DOWN_RIGHT(1, 1), DOWN(1, 0), DOWN_LEFT(1, -1); 

	private final int dr;
	private final int dc;

	Direction(int dr, int dc) {
		this.dr = dr;
		this.dc = dc;
	}

	public int getDr() {
		return dr;
	}

	public int getDc() {
		return dc;
	}
}

public class GameLogic implements ICommunicationEventListener {
	
	public static int X_NEEDED_IN_ROW = 4;

	private int playerUIPort;
	
//	private UDPCommunicator communicator;
	private Communicator comm;

	private boolean isGameRunning;

	private int cellsNeededInRow;

	private Map<Integer, Player> players = new LinkedHashMap<>(); // playerID -> player

	private GameField field;

	private List<IGameEventListener> gameEventListeners;

	public GameLogic(int playerUIPort) {
		this.gameEventListeners = new ArrayList<>();
		this.isGameRunning = false;
		this.playerUIPort = playerUIPort;
		
		this.comm = new PeerGroupCommunicator(1); // TODO port anpassen TODO direkt im konstruktor erstellen? oder später erst
	}

	// TODO: algo noch net ganz vollständig: wenn es 4 in einer reihe sind, aber man placed nicht am rand, gibt er bisher noch keinen punkt
	// checks if player scored a point and returns List of cells which earned said point
	// 		- starter cell serves as info for which player to check for
	public List<Cell> isPointScored(int r, int c, boolean recursion, int dirx, int diry) {
		Direction[] directions = Direction.values();		
		int targetCell = field.getCell(r - dirx, c - diry);;	// found cells have to match this one

		for(Direction dir : directions) {
			int curR = r;
			int curC = c;
			var cellsInRow = new ArrayList<Cell>();

			while(curR >= 0 && curR < field.getRowNumb() && 
					curC >= 0 && curC < field.getColNumb()) 
			{
				if(targetCell != field.getCell(curR, curC)) break;

				cellsInRow.add(new Cell(curR, curC));
				if(cellsInRow.size() == cellsNeededInRow) return cellsInRow;					

				curR += dir.getDr();
				curC += dir.getDc();
			}
			
//			if(recursion) return isPointScored(curR + dir.getDr(), curC + dir.getDc(), false, dir.getDr(), dir.getDc());
		}
		
		return null;
	}
	
	private void startPlayerConsoleThread() { // TODO wid
		// replaced by PlayerConsole in UI -> separates LogInfos from PlayerInput
	}

	//	------------------------- Events -------------------------

	public void addGameEventListener(IGameEventListener listener) {
		gameEventListeners.add(listener);
	}

	private void sendEvent(IGameEvent event) {
		gameEventListeners.forEach((listener) -> listener.onGameEvent(event));
	}
	
	// ------------------------- Event Callbacks -------------------------
	
	@Override
	public void onCommEvent(ICommunicationEvent event) {
		if(event instanceof PeerGroupJoinedEvent ev) {
			Utils.logClass(ev);
			onPeerGroupJoinedEvent(ev);
		}
		else if(event instanceof PeerGroupLeftEvent ev) {
			Utils.logClass(ev);
			onPeerGroupLeftEvent(ev);
		}
		else if(event instanceof JoinRequestReceivedEvent ev) {
			Utils.logClass(ev);
			onJoinRequestReceivedEvent(ev);
		}
		else {
			Utils.log("Unknow ICommunicationEvent");
		}
	}

	private void onJoinRequestReceivedEvent(JoinRequestReceivedEvent ev) {
		// TODO Auto-generated method stub
		
	}

	private void onPeerGroupLeftEvent(PeerGroupLeftEvent ev) {
		// TODO Auto-generated method stub
		
	}

	// ------------------------- Commands -------------------------

	private void onPeerGroupJoinedEvent(PeerGroupJoinedEvent ev) {
		comm.processCommCmd(new SendJoinGameRequestCmd("")); // TODO playername
	}

	public void processGameCommand(CreateGameCommand cmd) {
		System.out.println("CreateGameCmd");

		if(isGameRunning) return;
		isGameRunning = true;

		field = new GameField(cmd.rows(), cmd.cols());
		cellsNeededInRow = cmd.cellsNeededInRow();

		sendEvent(new GameCreatedEvent(cmd.rows(), cmd.cols()));

//		communicator.startReceiving(playerUIPort, this);
		
		startPlayerConsoleThread();
	}

	public void processGameCommand(JoinGameCommand cmd) {
		System.out.println("JoinGameCmd");

		String msg = JSONGameHandler.toJoinrequestMsg(cmd.playerName());
//		communicator.setReceivers(PeerFactory.joinLocal(cmd.friendID()));
//		communicator.sendMsg(msg);

//		communicator.startReceiving(playerUIPort, this);
	}

	public void processGameCommand(UDPMsgReceivedCommand cmd) {
		System.out.print("UDPMsgReceivedCommand: ");
		
		System.out.println(cmd.msg());
//
//		UDPMsgType msgtype = JSONGameHandler.getUDPMsgType(cmd.msg());
//		switch(msgtype) {
//		case GAMESTATE -> {
//			System.out.println("GameState");
//			
//			this.field = new GameField(JSONGameHandler.getGameField(cmd.msg()));
//			this.players = JSONGameHandler.getPlayers(cmd.msg());
//
//			if(!isGameRunning) { // means that player just now joined the game
//				isGameRunning = true;
//				cellsNeededInRow = X_NEEDED_IN_ROW;
//				sendEvent(new GameJoinedEvent(field.getRowNumb(), field.getColNumb()));
//				startPlayerConsoleThread();
//			}
//			
//			sendEvent(new PlayerPointsChangedEvent(new ArrayList<Player>(players.values()))); // update players in gui first
//			sendEvent(new GameFieldChangedEvent(field));
//		}
//		case JOINREQUEST -> {
//			System.out.println("JoinRequest");
//			
//			// delay so that player that requests to join has time to start listening to msgs and doesnt miss first msg
//			try { Thread.sleep(10); } catch (InterruptedException e) { e.printStackTrace();}
//			
//			int newPlayerID = Collections.max(players.keySet()) + 1;
//			var newPlayer = new Player(newPlayerID + PeerFactory.START_PORT, 
//					JSONGameHandler.getUsername(cmd.msg()));
//			players.put(newPlayerID, newPlayer);
//
//			var list = new ArrayList<Player>();
//			list.add(newPlayer);
//			sendEvent(new PlayerPointsChangedEvent(list));
//			
//			String msg = JSONGameHandler.toGameStateMsg(field, players);
//			communicator.setReceivers(PeerFactory.friendsLocal(players, playerUIPort));	// TODO unschön -> constructor von UDP this übergeben und dann holt der es sich?
//			communicator.sendMsg(msg);
//		}
//		}
	}

	public void processGameCommand(SelectCellCommand cmd) {
		System.out.println("SelectCellCmd");

		Player p = cmd.playerGUI();
		boolean selectSucceeded = field.setCell(cmd.r(), cmd.c(), p.getID());
		if(!selectSucceeded) return;

		sendEvent(new CellSelectedEvent(cmd.r(), cmd.c(), p.getColor()));

		List<Cell> cellsInRow = isPointScored(cmd.r(), cmd.c(), true, 0, 0);
		if(cellsInRow != null) {			
			field.resetCells(cellsInRow);
			sendEvent(new CellsClearedEvent(cellsInRow));

			players.put(p.getID(), p.incrPoints());
			var list = new ArrayList<Player>();
			list.add(p);
			sendEvent(new PlayerPointsChangedEvent(list));
		}

		// Send gamestate per UDP
		String msg = JSONGameHandler.toGameStateMsg(field, players);		
//		communicator.setReceivers(PeerFactory.friendsLocal(players, playerUIPort));	// TODO unschön -> constructor von UDP this übergeben und dann holt der es sich?
//		communicator.sendMsg(msg);
	}

	public void processGameCommand(CreatePlayerCommand cmd) {
		System.out.println("CreatePlayerCommand");

		Player p = cmd.p();
		players.put(p.getID(), p);
	}

	public void processGameCommand(ResetGridCommand cmd) {
		System.out.println("Inactive ResetGridCommand");
	}
	
}
