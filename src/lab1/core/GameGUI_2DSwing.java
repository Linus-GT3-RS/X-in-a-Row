package lab1.core;

import javax.swing.*;
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
import lab1.Utils;
import lab1.UtilsGUI;
import lab1.comms.Peer;
import lab1.game.Cell;
import lab1.game.GameField;

import java.awt.*;
import java.awt.event.*;
import java.awt.font.TextAttribute;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

interface IGameEventListener {
	public void onGameEvent(IGameEvent event);
}

public class GameGUI_2DSwing implements IGameEventListener {

	private JFrame frame;

	// 1 Player is using the UI
	private String playername;

	private GameLogic gameLogic;

	public GameGUI_2DSwing(String playername, int listenerPort) {		
		this.playername = playername;

		gameLogic = new GameLogic(listenerPort);
		gameLogic.addGameEventListener(this);

		// GUI

		frame = new JFrame("X in a row");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout());
		frame.getContentPane().setBackground(UtilsGUI.DARK_BACKGROUND);

		JPanel waitingRoomPanel = UtilsGUI.createWaitingRoomPanel(
				e -> onCreateGameButtonPressed(), 
				e -> onJoinGameBtnPressed()
		);
		frame.add(waitingRoomPanel, BorderLayout.SOUTH);

		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
	
	private void updateScoreboard(List<Player> players) {
		JPanel playersPanel = UtilsGUI.getScoreboardPlayersPanel(frame);
		playersPanel.removeAll();

		players.stream()
		.sorted((e1, e2) -> Integer.compare(e2.getPoints(), e1.getPoints())) // sort descending via points
		.forEach(p -> {
			boolean isMe = p.getName().equals(playername);
			String displayedName = isMe ? (p.getName() + " (You)") : p.getName();
			String displayedPoints = (p.getPoints() == 1) ? (p.getPoints() + " Point") : (p.getPoints() + " Points");	
			int playerID = players.indexOf(p);
			
			JLabel playerLabel = new JLabel(String.format("%-15s ", displayedName.concat(" (ID_"+playerID+")")) + displayedPoints);			
			playerLabel.setFont(new Font("Courier New", Font.PLAIN, 16));
			if(isMe) {
				Font font = playerLabel.getFont();
				Map attributes = font.getAttributes();
				attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
				playerLabel.setFont(font.deriveFont(attributes));
			}
			playerLabel.setForeground(p.getColor());
			playerLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

			playersPanel.add(playerLabel);
			playersPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		});

		playersPanel.revalidate();
		playersPanel.repaint();		
	}

	private void refreshGUI() {
		frame.revalidate(); // wichtig: UI aktualisieren
		frame.repaint();
		frame.pack(); // <-- hier wird die Größe angepasst
		frame.setLocationRelativeTo(null);
	}
	
	//	------------------------- Events -------------------------

	@Override
	public void onGameEvent(IGameEvent event) {
		if(event instanceof CellSelectedEvent ev) onCellSelectedEvent(ev);
		else if(event instanceof CellsClearedEvent ev) onCellsClearedEvent(ev);
		else if(event instanceof PlayersPointsChangedEvent ev) onPlayersPointsChangedEvent(ev);
		else if(event instanceof GameInitializedEvent ev) onGameInitializedEvent(ev);
		else if(event instanceof GamestateUpdatedEvent ev) onGamestateUpdatedEvent(ev);
		else Utils.log("Unhandled event type");
	}

	private void onGameInitializedEvent(GameInitializedEvent ev) {
		Utils.logClass(ev);

		frame.remove(UtilsGUI.findContainerByName(frame, "waitingRoomPanel"));

		// add panels
		var scoreboardPanel = UtilsGUI.createScoreboardPanel();
		JPanel gamefieldPanel = UtilsGUI.createGameFieldPanel(
			ev.rowcount(), 
			ev.colcount(), 
			(Integer row, Integer col) -> gameLogic.processGameCommand(new SelectCellCommand(row, col))
		);
		var consolePanel = UtilsGUI.createConsolePanel
				(
						(row, col) -> gameLogic.processGameCommand(new SelectCellCommand(row, col)), 
						(JButton[][])gamefieldPanel.getClientProperty("buttons")
		);	
		var leaveGamePanel = UtilsGUI.createLeaveGamePanel((e) -> onLeaveGameBtnPressed());
		frame.add(scoreboardPanel, BorderLayout.WEST);
		frame.add(gamefieldPanel, BorderLayout.CENTER);
		frame.add(consolePanel, BorderLayout.EAST);
		frame.add(leaveGamePanel, BorderLayout.SOUTH);

		refreshGUI();		
	}

	private void onGamestateUpdatedEvent(GamestateUpdatedEvent ev) {
		Utils.logClass(ev);

		// GUI-Update Field
		int[][] field = ev.field().getCopy();
		var buttons = UtilsGUI.getButtons(frame);

		for(int row = 0; row < ev.field().getRowNumb(); row++) {
			for(int col = 0; col < ev.field().getColNumb(); col++) {

				int curcell = field[row][col];
				JButton curb = buttons[row][col];

				if(curcell == GameField.EMPTY_FIELD) {
					curb.setBackground(Color.WHITE);
					curb.setEnabled(true);
				}
				else {
					curb.setBackground(ev.players().get(curcell).getColor()); 
					curb.setEnabled(false);
				}
			}
		}

		// GUI-Update Scoreboard
		updateScoreboard(ev.players());
	}

	private void onCellSelectedEvent(CellSelectedEvent ev) {
		Utils.logClass(ev);

		JButton cur = UtilsGUI.getButtons(frame)[ev.r()][ev.c()];
		cur.setBackground(ev.col());
		cur.setEnabled(false);
	}

	private void onCellsClearedEvent(CellsClearedEvent ev) {
		Utils.logClass(ev);

		ev.cells().forEach(cell -> {
			JButton cur = UtilsGUI.getButtons(frame)[cell.r()][cell.c()];
			cur.setBackground(Color.WHITE);
			cur.setEnabled(true);
		});
	}

	private void onPlayersPointsChangedEvent(PlayersPointsChangedEvent ev) { 
		Utils.logClass(ev);

		updateScoreboard(ev.players());
	}

	//	------------------------- Callbacks -------------------------

	private void onCreateGameButtonPressed() {
		JSpinner spinnerRows = new JSpinner(new SpinnerNumberModel(10, 2, 12, 1));
		JSpinner spinnerCols = new JSpinner(new SpinnerNumberModel(10, 2, 12, 1));
		JSpinner spinnerX = new JSpinner(new SpinnerNumberModel(4, 2, 100, 1)); // currently not used, instead FIX number = 4

		Object[] message = {
				"Number of rows:", spinnerRows,
				"Number of cols:", spinnerCols,
				"Cells needed in a row:", spinnerX
		};

		int option = JOptionPane.showConfirmDialog(
				null, message, "Please enter game settings",
				JOptionPane.OK_CANCEL_OPTION
				);

		if (option == JOptionPane.OK_OPTION) {
			int rows = (Integer) spinnerRows.getValue();
			int cols = (Integer) spinnerCols.getValue();
			
			UtilsGUI.showPanelIsBlockedForUser(frame, "waitingRoomPanel");
			gameLogic.processGameCommand(new CreateGameCommand(rows, cols, GameLogic.X_NEEDED_IN_ROW, new Player(playername)));
		}
	}

	private void onJoinGameBtnPressed() {
		var txtfieldFriendIP = new JTextField();
		txtfieldFriendIP.setText("127.0.0.1");
		var spinnerFriendPort = new JSpinner(new SpinnerNumberModel(15_000, 8_000, 200_000, 1));

		Object[] message = {
				"Enter Friend IP", txtfieldFriendIP,
				"Enter Friend Port", spinnerFriendPort
		};

		while (true) {
			int option = JOptionPane.showConfirmDialog(null, message, "Please Enter Friend Attributes", JOptionPane.OK_CANCEL_OPTION);

			if (option == JOptionPane.OK_OPTION) {
				String friendIP = txtfieldFriendIP.getText().trim(); 

				if (Utils.isValidIP(friendIP)) {
					var friend = new Peer(friendIP, (Integer)spinnerFriendPort.getValue());

					UtilsGUI.showPanelIsBlockedForUser(frame, "waitingRoomPanel");
					gameLogic.processGameCommand(new JoinGameCommand(friend, playername));
					return;
				}                
				JOptionPane.showMessageDialog(null, "Invalid IP address. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
			} 
			else {
				break; // User canceled
			}
		}		
	}
	
	private void onLeaveGameBtnPressed() {
		frame.remove(UtilsGUI.findContainerByName(frame, "scoreboardPanel"));
		frame.remove(UtilsGUI.findContainerByName(frame, "gridPanel"));
		frame.remove(UtilsGUI.findContainerByName(frame, "consolePanel"));
		frame.remove(UtilsGUI.findContainerByName(frame, "leaveGamePanel"));
		
		JPanel waitingRoomPanel = UtilsGUI.createWaitingRoomPanel(
				e -> onCreateGameButtonPressed(), 
				e -> onJoinGameBtnPressed()
		);
		frame.add(waitingRoomPanel);
		refreshGUI();
		
		gameLogic.processGameCommand(new LeaveGameCommand());
	}

	//	------------------------- Test UI -------------------------

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> new GameGUI_2DSwing("TestName", 15000));
	}




}
