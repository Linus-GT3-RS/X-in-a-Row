package lab1.game;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lab1.cmds_comms.SendGamestateCmd;
import lab1.cmds_comms.SendJoinGameRequestCmd;
import lab1.cmds_comms.SendJoinPeerGroupRequestCmd;
import lab1.cmds_comms.SendLeaveGameCommand;
import lab1.cmds_comms.StartReceivingCommand;
import lab1.cmds_comms.StopReceivingCommand;
import lab1.cmds_game.CreateGameCommand;
import lab1.cmds_game.JoinGameCommand;
import lab1.cmds_game.LeaveGameCommand;
import lab1.cmds_game.SelectCellCommand;
import lab1.comms.Communicator;
import lab1.comms.ICommunicationEventListener;
import lab1.comms.PeerGroupCommunicator;
import lab1.events_comms.GamestateReceivedEvent;
import lab1.events_comms.ICommunicationEvent;
import lab1.events_comms.JoinRequestReceivedEvent;
import lab1.events_comms.LeaveGameMsgReceivedEvent;
import lab1.events_comms.PeerGroupJoinedEvent;
import lab1.events_comms.PeerGroupLeftEvent;
import lab1.events_game.CellSelectedEvent;
import lab1.events_game.CellsClearedEvent;
import lab1.events_game.GameInitializedEvent;
import lab1.events_game.GamestateUpdatedEvent;
import lab1.events_game.IGameEvent;
import lab1.events_game.PlayersPointsChangedEvent;
import lab1.gui.IGameEventListener;

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
	
	private Communicator communicator;
	
	private JoinGameCommand lastJoinGameCmd;

	private boolean isGameRunning;

	private int cellsNeededInRow;

	private List<Player> players;
	private int myPlayerID; // = index in list of allplayers

	private GameField gamefield;

	private List<IGameEventListener> gameEventListeners;

	public GameLogic(int listenerPort) {
		this.gameEventListeners = new ArrayList<>();
		this.isGameRunning = false;
		
		// create communicator
		// -> starts autom listening on given port
		this.communicator = new PeerGroupCommunicator(listenerPort);
		communicator.addCommEventListener(this);
	}
	
	private Player getMyself() {
		return players.get(myPlayerID);
	}

	// checks if player scored a point and returns List of cells which earned said point
	// 		- starter cell serves as info for which player to check for
	public List<Cell> isPointScored(int r, int c, boolean recursion, int dirx, int diry) {
		// TODO: algo noch net ganz vollständig: 
		// wenn es 4 in einer reihe sind, aber man placed nicht am rand, gibt er bisher noch keinen punkt
		
		Direction[] directions = Direction.values();		
		int targetCell = gamefield.getCell(r - dirx, c - diry);;	// found cells have to match this one

		for(Direction dir : directions) {
			int curR = r;
			int curC = c;
			var cellsInRow = new ArrayList<Cell>();

			while(curR >= 0 && curR < gamefield.getRowNumb() && 
					curC >= 0 && curC < gamefield.getColNumb()) 
			{
				if(targetCell != gamefield.getCell(curR, curC)) break;

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
		for(Player p : players) {
			if(p.getName().equals(ev.username())) break;
			indexOfPlayerLeaving++;
		}
		
		players.remove(indexOfPlayerLeaving);
		if(myPlayerID > indexOfPlayerLeaving) myPlayerID--;
		gamefield.removePlayer(indexOfPlayerLeaving);
		gamefield.adjustCellsBy1(indexOfPlayerLeaving);		
		
		sendGameEvent(new GamestateUpdatedEvent(gamefield, players));
	}

	private void onPeerGroupJoinedEvent(PeerGroupJoinedEvent ev) {
		communicator.processCommCmd(new SendJoinGameRequestCmd(lastJoinGameCmd.playerName()));
	}
	
	private void onGamestateReceivedEvent(GamestateReceivedEvent ev) {
		gamefield = ev.field();
		players = ev.players();
		
		 if(!isGameRunning) { // means that player received first data of joined game
			Utils.log("Succesfully joined Game");
			isGameRunning = true;
			
			cellsNeededInRow = X_NEEDED_IN_ROW;
			myPlayerID = players.size() - 1;
			
			sendGameEvent(new GameInitializedEvent(gamefield.getRowNumb(), gamefield.getColNumb()));
		}	
		 
		sendGameEvent(new GamestateUpdatedEvent(gamefield, players));
	}
	
	private void onJoinRequestReceivedEvent(JoinRequestReceivedEvent ev) {
		players.add(new Player(ev.username()));
		
		sendGameEvent(new PlayersPointsChangedEvent(players));
		
		communicator.processCommCmd(new SendGamestateCmd(gamefield, players));		
	}
	

	// ------------------------- Commands -------------------------

	public void processGameCommand(CreateGameCommand cmd) {
		Utils.logClass(cmd);

		if(isGameRunning) return;
		isGameRunning = true;

		gamefield = new GameField(cmd.rows(), cmd.cols());
		cellsNeededInRow = X_NEEDED_IN_ROW; // cmd.cellsNeededInRow();
		players = new ArrayList<Player>();
		players.add(cmd.player());
		myPlayerID = 0;
		
		sendGameEvent(new GameInitializedEvent(gamefield.getRowNumb(), gamefield.getColNumb()));
		sendGameEvent(new GamestateUpdatedEvent(gamefield, players));
		
		communicator.processCommCmd(new StartReceivingCommand());
	}

	public void processGameCommand(JoinGameCommand cmd) {
		Utils.logClass(cmd);

		lastJoinGameCmd = cmd;
		communicator.processCommCmd(new StartReceivingCommand());
		communicator.processCommCmd(new SendJoinPeerGroupRequestCmd(cmd.friend()));
	}
	
	public void processGameCommand(SelectCellCommand cmd) {
		Utils.logClass(cmd);

		boolean selectSucceeded = gamefield.setCell(cmd.r(), cmd.c(), myPlayerID);
		if(!selectSucceeded) return;

		sendGameEvent(new CellSelectedEvent(cmd.r(), cmd.c(), getMyself().getColor()));

		List<Cell> cellsInRow = isPointScored(cmd.r(), cmd.c(), true, 0, 0);
		if(cellsInRow != null) {			
			gamefield.resetCells(cellsInRow);
			getMyself().incrPoints();

			sendGameEvent(new CellsClearedEvent(cellsInRow));
			sendGameEvent(new PlayersPointsChangedEvent(players));
		}

		communicator.processCommCmd(new SendGamestateCmd(gamefield, players));
	}
	
	public void processGameCommand(LeaveGameCommand cmd) {
		Utils.logClass(cmd);
		
		isGameRunning = false;
		
		communicator.processCommCmd(new StopReceivingCommand());
		communicator.processCommCmd(new SendLeaveGameCommand(getMyself().getName()));
	}
}
