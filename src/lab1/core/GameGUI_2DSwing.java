package lab1.core;

import javax.swing.*;
import lab1.game.Player;
import lab1.gamecmds.CreateGameCommand;
import lab1.gamecmds.CreatePlayerCommand;
import lab1.gamecmds.JoinGameCommand;
import lab1.gamecmds.SelectCellCommand;
import lab1.gameevents.CellSelectedEvent;
import lab1.gameevents.CellsClearedEvent;
import lab1.gameevents.GameCreatedEvent;
import lab1.gameevents.GameFieldChangedEvent;
import lab1.gameevents.GameJoinedEvent;
import lab1.gameevents.IGameEvent;
import lab1.gameevents.PlayerPointsChangedEvent;
import lab1.game.Cell;
import lab1.game.GameField;

import java.awt.*;
import java.awt.event.*;
import java.util.LinkedHashMap;
import java.util.Map;

interface IGameEventListener {
	public void onGameEvent(IGameEvent event);
}

public class GameGUI_2DSwing implements IGameEventListener {

	private static final Color PLAYER_COLOR = Color.BLUE;
	private static final Color DARK_BACKGROUND = new Color(45, 45, 45); // Dunkelgrauer Hintergrund
	private static final Color LIGHT_TEXT_COLOR = new Color(200, 200, 200); // Heller Text
	private static final Color BUTTON_COLOR = new Color(60, 60, 60); // Dunklere Buttonfarbe
	private static final Color FIELD_COLOR = new Color(80, 80, 80); // Dunkle Farbe für die Felder

	private JFrame frame;

	// Start screen
	JPanel buttonPanel;
	private JButton createGameButton;
	private JButton joinGameButton;

	// Scoreboard
	private JPanel scoreboardPanel;
	private JPanel playersPanel;
	private Map<Integer, Player> scoreboardUnsorted = new LinkedHashMap<>(); // playerID -> player

	// Player Console
//	private JPanel consolePanel;
	
	// Game field
	private JButton[][] buttons;

	// 1 Player is using the UI
	private Player player;

	private GameLogic gameLogic;

	public GameGUI_2DSwing(Player player) {		
		this.player = player;

		gameLogic = new GameLogic(player.getPort());
		gameLogic.addGameEventListener(this);

		gameLogic.processGameCommand(new CreatePlayerCommand(this.player));

		// -------------------------- Frame --------------------------------

		frame = new JFrame("X in a row");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout());
		frame.getContentPane().setBackground(DARK_BACKGROUND); // Hintergrundfarbe für das ganze Fenster

		// -------------------------- Buttons --------------------------------

		// Panel für die Buttons im Zentrum
		buttonPanel = new JPanel(new GridLayout(2, 1, 10, 10)); // 3 Buttons vertikal anordnen
		buttonPanel.setBackground(DARK_BACKGROUND); // Dunkler Hintergrund für das Panel

		// "Create Game" Button
		createGameButton = new JButton("Create Game");
		createGameButton.setFont(new Font("Arial", Font.BOLD, 16));
		createGameButton.setBackground(new Color(0, 153, 255)); // Blau für Create
		createGameButton.setForeground(Color.WHITE);
		createGameButton.setFocusPainted(false); // Kein Fokusrahmen
		createGameButton.addActionListener(e -> {
			onCreateGameButtonPressed();
		});

		// "Join Game" Button
		joinGameButton = new JButton("Join Game");
		joinGameButton.setFont(new Font("Arial", Font.BOLD, 16));
		joinGameButton.setBackground(new Color(0, 204, 0)); // Grün für Join
		joinGameButton.setForeground(Color.WHITE);
		joinGameButton.setFocusPainted(false); // Kein Fokusrahmen
		joinGameButton.addActionListener(e -> onJoinGameBtnPressed());

		// Füge Buttons zum Button Panel hinzu
		buttonPanel.add(createGameButton);
		buttonPanel.add(joinGameButton);

		// -------------------------- Frame --------------------------------

		frame.add(buttonPanel, BorderLayout.SOUTH);

		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
	
	private JPanel createConsolePanel() {
		JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(DARK_BACKGROUND);

        // Textarea für Ausgaben
        JTextArea ausgabeFeld = new JTextArea();
        ausgabeFeld.setEditable(false);
        ausgabeFeld.setBackground(FIELD_COLOR);
        ausgabeFeld.setForeground(LIGHT_TEXT_COLOR);
        ausgabeFeld.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(ausgabeFeld);
        ausgabeFeld.append("> Select a cell by giving\n  me the row and column\n");
        ausgabeFeld.append("  Example: \"A1\" selects the cell A1\n");
        
        // Eingabefeld
        JTextField eingabeFeld = new JTextField();
        eingabeFeld.setBackground(BUTTON_COLOR);
        eingabeFeld.setForeground(LIGHT_TEXT_COLOR);
        eingabeFeld.setCaretColor(LIGHT_TEXT_COLOR);
        eingabeFeld.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Eingabe-Handling
        eingabeFeld.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String input = eingabeFeld.getText();
                eingabeFeld.setText("");
                ausgabeFeld.append("> " + input + "\n");

                if(input.matches("^[A-Z][0-9]+$")) {
                	int row = (int)input.charAt(0) - 65; 
                    int col = Integer.parseInt(input.substring(1)) - 1; 
                    
                    if(row >= 0 && row < buttons.length &&
                	   col >= 0 && col < buttons[0].length) {
                    	gameLogic.processGameCommand(new SelectCellCommand(row, col, player));
                    }
                }
            }
        });

        // Komponenten hinzufügen
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(eingabeFeld, BorderLayout.SOUTH);
        
        panel.setPreferredSize(new Dimension(300, 200));
        return panel;
	}

	public JPanel createGameFieldPanel(int rows, int cols) {
		int CELL_SIZE = 50;

		JPanel gridPanel = new JPanel(new GridLayout(rows + 2, cols + 2));
		gridPanel.setBackground(DARK_BACKGROUND);
		buttons = new JButton[rows][cols];

		for (int row = 0; row <= rows + 1; row++) {
			for (int col = 0; col <= cols + 1; col++) {
				if ((row == 0 || row == rows + 1) && (col == 0 || col == cols + 1)) {
					gridPanel.add(new JLabel(""));
				} else if (row == 0 || row == rows + 1) {
					if (col > 0 && col <= cols) {
						JLabel label = new JLabel(String.valueOf(col), SwingConstants.CENTER);
						label.setPreferredSize(new Dimension(CELL_SIZE, CELL_SIZE));
						label.setForeground(LIGHT_TEXT_COLOR);
						gridPanel.add(label);
					} else {
						gridPanel.add(new JLabel(""));
					}
				} else if (col == 0 || col == cols + 1) {
					if (row > 0 && row <= rows) {
						char letter = (char) ('A' + row - 1);
						JLabel label = new JLabel(String.valueOf(letter), SwingConstants.CENTER);
						label.setPreferredSize(new Dimension(CELL_SIZE, CELL_SIZE));
						label.setForeground(LIGHT_TEXT_COLOR);
						gridPanel.add(label);
					} else {
						gridPanel.add(new JLabel(""));
					}
				} else {
					JButton button = new JButton();
					button.setPreferredSize(new Dimension(CELL_SIZE, CELL_SIZE));
					button.setBackground(Color.WHITE);
					button.setBorder(BorderFactory.createLineBorder(Color.BLACK));
					button.setEnabled(true);

					final int finalRow = row - 1;
					final int finalCol = col - 1;

					button.addActionListener(e -> {
						gameLogic.processGameCommand(new SelectCellCommand(finalRow, finalCol, player));
					});

					buttons[finalRow][finalCol] = button;
					gridPanel.add(button);
				}
			}
		}

		return gridPanel;
	}

	private void createScoreboard() {
		scoreboardPanel = new JPanel(new BorderLayout());
		scoreboardPanel.setBackground(DARK_BACKGROUND);
		scoreboardPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

		JLabel titleLabel = new JLabel("Scoreboard");
		titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
		titleLabel.setForeground(LIGHT_TEXT_COLOR);
		titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
		scoreboardPanel.add(titleLabel, BorderLayout.NORTH);

		playersPanel = new JPanel();
		playersPanel.setLayout(new BoxLayout(playersPanel, BoxLayout.Y_AXIS));
		playersPanel.setBackground(DARK_BACKGROUND);

		scoreboardPanel.add(new JScrollPane(playersPanel), BorderLayout.CENTER);
		scoreboardPanel.setPreferredSize(new Dimension(350, 0));

		frame.add(scoreboardPanel, BorderLayout.WEST);
	}

	private void paintScoreboardPoints() {
		playersPanel.removeAll();

		scoreboardUnsorted.entrySet().stream()
		.sorted((e1, e2) -> Integer.compare(e2.getValue().getPoints(), e1.getValue().getPoints())) // sort descending via points
		.forEach(entry -> {
			Player p = entry.getValue();
			String name = p.getName();
			int points = p.getPoints();

			String displayedName = (name.equals(player.getName())) ? (name + " (You)") : name;
			String displayedPoints = (points == 1) ? (points + " Point") : (points + " Points");			
			JLabel playerLabel = new JLabel(String.format("%-15s ", displayedName.concat(" (id:"+p.getID()+")")) + displayedPoints);
			playerLabel.setFont(new Font("Courier New", Font.PLAIN, 16));
			playerLabel.setForeground(LIGHT_TEXT_COLOR);
			playerLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

			playersPanel.add(playerLabel);
			playersPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		});

		playersPanel.revalidate();
		playersPanel.repaint();
	}

	//	------------------------- Events -------------------------

	@Override
	public void onGameEvent(IGameEvent event) {

		if(event instanceof GameCreatedEvent ev) {
			System.out.println("GameCreatedEvent");

			frame.remove(buttonPanel);

			createScoreboard();
			scoreboardUnsorted.put(player.getID(), player);
			paintScoreboardPoints();

			frame.add(createGameFieldPanel(ev.rows(), ev.cols()), BorderLayout.CENTER);
			
			frame.add(createConsolePanel(), BorderLayout.EAST);

			frame.revalidate(); // wichtig: UI aktualisieren
			frame.repaint();
			frame.pack(); // <-- hier wird die Größe angepasst
			frame.setLocationRelativeTo(null);
		}
		else if(event instanceof GameJoinedEvent ev) {
			System.out.println("GameJoinedEvent");

			frame.remove(buttonPanel);

			createScoreboard();
			paintScoreboardPoints();

			frame.add(createGameFieldPanel(ev.rows(), ev.cols()), BorderLayout.CENTER);
			
			frame.add(createConsolePanel(), BorderLayout.EAST);

			frame.revalidate(); // wichtig: UI aktualisieren
			frame.repaint();
			frame.pack(); // <-- hier wird die Größe angepasst
			frame.setLocationRelativeTo(null);
		}
		else if(event instanceof CellSelectedEvent ev) {
			System.out.println("CellSelectedEvent");

			JButton cur = buttons[ev.r()][ev.c()];
			cur.setBackground(ev.col());
			cur.setEnabled(false);
		}
		else if(event instanceof CellsClearedEvent ev) {
			System.out.println("CellsClearedEvent");

			ev.cells().forEach(cell -> {
				JButton cur = buttons[cell.r()][cell.c()];
				cur.setBackground(Color.WHITE);
				cur.setEnabled(true);
			});
		}
		else if(event instanceof PlayerPointsChangedEvent ev) { 
			System.out.println("PlayerPointsChangedEvent");

			ev.p().forEach(p -> scoreboardUnsorted.put(p.getID(), p));			
			paintScoreboardPoints();
		}
		else if(event instanceof GameFieldChangedEvent ev) { 
			System.out.println("GameFieldChangedEvent");

			int[][] field = ev.field().getCopy(); 
			
			for(int row = 0; row < ev.field().getRowNumb(); row++) {
				for(int col = 0; col < ev.field().getColNumb(); col++) {
					
					int curcell = field[row][col];
					JButton curb = buttons[row][col];

					if(curcell == GameField.EMPTY_FIELD) {
						curb.setBackground(Color.WHITE);
						curb.setEnabled(true);
					}
					else {
						curb.setBackground(scoreboardUnsorted.get(curcell).getColor());
						curb.setEnabled(false);
					}
				}
			}
		}
		else {
			System.out.println("Unhandled event type");
		}
	}

	//	------------------------- Callbacks -------------------------

	private void onCreateGameButtonPressed() {
		JSpinner spinnerRows = new JSpinner(new SpinnerNumberModel(10, 2, 12, 1));
		JSpinner spinnerCols = new JSpinner(new SpinnerNumberModel(10, 2, 12, 1));
		JSpinner spinnerX = new JSpinner(new SpinnerNumberModel(4, 2, 100, 1)); // currently not used, instead FIX 4

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
			gameLogic.processGameCommand(new CreateGameCommand(rows, cols, GameLogic.X_NEEDED_IN_ROW));
		}
	}

	private void onJoinGameBtnPressed() {
		JSpinner spinnerFriendID = new JSpinner(new SpinnerNumberModel(0, 0, 9, 1));
		Object[] message = {
				"Enter Friend ID", spinnerFriendID
		};
		int option = JOptionPane.showConfirmDialog(
				null, message, "Please enter game settings",
				JOptionPane.OK_CANCEL_OPTION
				);

		if (option == JOptionPane.OK_OPTION) {
			int friendID = (Integer) spinnerFriendID.getValue();			
			gameLogic.processGameCommand(new JoinGameCommand(friendID, player.getName()));
		}		
	}

	//	------------------------- Test UI -------------------------

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> new GameGUI_2DSwing(new Player(2, "TestName")));
	}




}
