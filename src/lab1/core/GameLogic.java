package lab1.core;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lab1.Utils;
import lab1.commcmds.SendGamestateCmd;
import lab1.commcmds.SendJoinGameRequestCmd;
import lab1.commcmds.SendJoinPeerGroupRequestCmd;
import lab1.commcmds.SendLeaveGameCommand;
import lab1.commcmds.StartReceivingCommand;
import lab1.commcmds.StopReceivingCommand;
import lab1.commevents.GamestateReceivedEvent;
import lab1.commevents.ICommunicationEvent;
import lab1.commevents.JoinRequestReceivedEvent;
import lab1.commevents.LeaveGameMsgReceivedEvent;
import lab1.commevents.PeerGroupJoinedEvent;
import lab1.commevents.PeerGroupLeftEvent;
import lab1.comms.Communicator;
import lab1.comms.ICommunicationEventListener;
import lab1.comms.PeerGroupCommunicator;
import lab1.game.Cell;
import lab1.game.GameField;
import lab1.game.Player;
import lab1.gamecmds.CreateGameCommand;
import lab1.gamecmds.JoinGameCommand;
import lab1.gamecmds.LeaveGameCommand;
import lab1.gamecmds.SelectCellCommand;
import lab1.gameevents.CellSelectedEvent;
import lab1.gameevents.CellsClearedEvent;
import lab1.gameevents.GameInitializedEvent;
import lab1.gameevents.GamestateUpdatedEvent;
import lab1.gameevents.IGameEvent;
import lab1.gameevents.PlayersPointsChangedEvent;

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

//	private int playerUIPort;
	
//	private UDPCommunicator communicator;
	private Communicator comm;
	
	private JoinGameCommand lastJoinGameCmd;

	private boolean isGameRunning;

	private int cellsNeededInRow;

//	private Map<Integer, Player> players = new LinkedHashMap<>(); // playerID -> player
	private List<Player> allplayers;
	private int myPlayerID; // = index in list of allplayers

	private GameField field;

	private List<IGameEventListener> gameEventListeners;

	public GameLogic(int listenerPort) {
		this.gameEventListeners = new ArrayList<>();
		this.isGameRunning = false;
		
		// create communicator
		// -> starts autom listening on given port
		this.comm = new PeerGroupCommunicator(listenerPort);
		comm.addCommEventListener(this);
	}
	
	private Player getMyself() {
		return allplayers.get(myPlayerID);
	}

	// checks if player scored a point and returns List of cells which earned said point
	// 		- starter cell serves as info for which player to check for
	public List<Cell> isPointScored(int r, int c, boolean recursion, int dirx, int diry) {
		// TODO: algo noch net ganz vollständig: 
		// wenn es 4 in einer reihe sind, aber man placed nicht am rand, gibt er bisher noch keinen punkt
		
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

	//	------------------------- Events -------------------------

	public void addGameEventListener(IGameEventListener listener) {
		gameEventListeners.add(listener);
	}

	private void sendGameEvent(IGameEvent event) {
		gameEventListeners.forEach((listener) -> listener.onGameEvent(event));
	}
	
	// ------------------------- Event Callbacks -------------------------
	
	@Override
	public void onCommEvent(ICommunicationEvent event) {
		if(event instanceof PeerGroupJoinedEvent ev) {
			Utils.logClass(ev);
			onPeerGroupJoinedEvent(ev);
		}
		else if(event instanceof GamestateReceivedEvent ev) {
			Utils.logClass(ev);
			onGamestateReceivedEvent(ev);
		}
		else if(event instanceof JoinRequestReceivedEvent ev) {
			Utils.logClass(ev);
			onJoinRequestReceivedEvent(ev);
		}
		else if(event instanceof LeaveGameMsgReceivedEvent ev) {
			Utils.logClass(ev);
			onLeaveGameMsgReceivedEvent(ev);
		}
		else {
			Utils.log("Unknow ICommunicationEvent");
		}
	}

	// BAD BAD BAD but has to exist because of compatibility with eduards code :((
	// gets id of removed player and adjusts id in field of all players > id
	private void onLeaveGameMsgReceivedEvent(LeaveGameMsgReceivedEvent ev) {
		int indexOfPlayerLeaving = 0;
		for(Player p : allplayers) {
			if(p.getName().equals(ev.username())) break;
			indexOfPlayerLeaving++;
		}
		
		allplayers.remove(indexOfPlayerLeaving);
		if(myPlayerID > indexOfPlayerLeaving) myPlayerID--;
		field.removePlayer(indexOfPlayerLeaving);
		field.adjustCellsBy1(indexOfPlayerLeaving);		
		
		sendGameEvent(new GamestateUpdatedEvent(field, allplayers));
	}

	private void onPeerGroupJoinedEvent(PeerGroupJoinedEvent ev) {
		comm.processCommCmd(new SendJoinGameRequestCmd(lastJoinGameCmd.playerName()));
	}
	
	private void onGamestateReceivedEvent(GamestateReceivedEvent ev) {
		field = ev.field();
		allplayers = ev.players();
		
		 if(!isGameRunning) { // means that player received first data of joined game
			Utils.log("Succesfully joined Game");
			isGameRunning = true;
			
			cellsNeededInRow = X_NEEDED_IN_ROW;
			myPlayerID = allplayers.size() - 1;
			
			sendGameEvent(new GameInitializedEvent(field.getRowNumb(), field.getColNumb()));
		}	
		 
		sendGameEvent(new GamestateUpdatedEvent(field, allplayers));
	}
	
	private void onJoinRequestReceivedEvent(JoinRequestReceivedEvent ev) {
		allplayers.add(new Player(ev.username()));
		
		sendGameEvent(new PlayersPointsChangedEvent(allplayers));
		
		comm.processCommCmd(new SendGamestateCmd(field, allplayers));		
	}
	

	// ------------------------- Commands -------------------------

	public void processGameCommand(CreateGameCommand cmd) {
		Utils.logClass(cmd);

		if(isGameRunning) return;
		isGameRunning = true;

		field = new GameField(cmd.rows(), cmd.cols());
		cellsNeededInRow = X_NEEDED_IN_ROW; // cmd.cellsNeededInRow();
		allplayers = new ArrayList<Player>();
		allplayers.add(cmd.player());
		myPlayerID = 0;
		
		sendGameEvent(new GameInitializedEvent(field.getRowNumb(), field.getColNumb()));
		sendGameEvent(new GamestateUpdatedEvent(field, allplayers));
		
		comm.processCommCmd(new StartReceivingCommand());
	}

	public void processGameCommand(JoinGameCommand cmd) {
		Utils.logClass(cmd);

		lastJoinGameCmd = cmd;
		comm.processCommCmd(new StartReceivingCommand());
		comm.processCommCmd(new SendJoinPeerGroupRequestCmd(cmd.friend()));
	}
	
	public void processGameCommand(SelectCellCommand cmd) {
		Utils.logClass(cmd);

		boolean selectSucceeded = field.setCell(cmd.r(), cmd.c(), myPlayerID);
		if(!selectSucceeded) return;

		sendGameEvent(new CellSelectedEvent(cmd.r(), cmd.c(), getMyself().getColor()));

		List<Cell> cellsInRow = isPointScored(cmd.r(), cmd.c(), true, 0, 0);
		if(cellsInRow != null) {			
			field.resetCells(cellsInRow);
			getMyself().incrPoints();

			sendGameEvent(new CellsClearedEvent(cellsInRow));
			sendGameEvent(new PlayersPointsChangedEvent(allplayers));
		}

		comm.processCommCmd(new SendGamestateCmd(field, allplayers));
	}
	
	public void processGameCommand(LeaveGameCommand cmd) {
		Utils.logClass(cmd);
		
		isGameRunning = false;
		
		comm.processCommCmd(new StopReceivingCommand());
		comm.processCommCmd(new SendLeaveGameCommand(getMyself().getName()));
	}
}
