package lab1.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.util.function.BiConsumer;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import lab1.cmds_game.SelectCellCommand;

public class UtilsGUI {

	public static final Color PLAYER_COLOR = Color.BLUE;
	public static final Color DARK_BACKGROUND = new Color(45, 45, 45); // Dunkelgrauer Hintergrund
	public static final Color LIGHT_TEXT_COLOR = new Color(200, 200, 200); // Heller Text
	public static final Color BUTTON_COLOR = new Color(60, 60, 60); // Dunklere Buttonfarbe
	public static final Color FIELD_COLOR = new Color(80, 80, 80); // Dunkle Farbe für die Felder

	public static Container findContainerByName(Container parent, String name) {
		for (Component comp : parent.getComponents()) {

			if (comp instanceof Container container && comp instanceof JComponent jComp) {
				var prop = (String)jComp.getClientProperty("name");
				if (name.equals(prop)) {
					return container;
				}

				Container result = findContainerByName(container, name);
				if (result != null) return result;
			}
		}
		return null;
	}
	
	public static void showPanelIsBlockedForUser(JFrame frame, String name) {
		var panel = findContainerByName(frame, name);
		panel.setEnabled(false);
		panel.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	}
	
	// ----------------------------- Getters --------------------------------------
	
	public static JButton[][] getButtons(JFrame f){
		var panel = (JPanel)findContainerByName(f, "gridPanel");
		var buttons = (JButton[][])panel.getClientProperty("buttons");
		return buttons;
	}
	
	public static JPanel getScoreboardPlayersPanel(JFrame f) {
		var panel = (JPanel)findContainerByName(f, "playersPanel");
		return panel;
	}

	// ------------------------ Panel Fabric -------------------------------------------
	
	 public static JPanel createLeaveGamePanel(ActionListener action) {
	        JPanel panel = new JPanel();
	        panel.setBackground(DARK_BACKGROUND);
	        panel.putClientProperty("name", "leaveGamePanel");
	        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

	        JButton leaveButton = new JButton("Leave Game");
	        leaveButton.setBackground(BUTTON_COLOR);
	        leaveButton.setForeground(LIGHT_TEXT_COLOR);
	        leaveButton.setFocusPainted(false);
	        leaveButton.setBorderPainted(false);
	        leaveButton.setFont(new Font("Arial", Font.BOLD, 14));
	        leaveButton.addActionListener(action);

	        panel.add(leaveButton);

	        return panel;
	    }
	
	public static JPanel createGameFieldPanel(int rows, int cols, BiConsumer<Integer, Integer> OnBtnPressed) {
		int CELL_SIZE = 50;

		JPanel gridPanel = new JPanel(new GridLayout(rows + 2, cols + 2));
		gridPanel.putClientProperty("name", "gridPanel");
		gridPanel.setBackground(UtilsGUI.DARK_BACKGROUND);		
		
		var buttons = new JButton[rows][cols];
		gridPanel.putClientProperty("buttons", buttons);

		for (int row = 0; row <= rows + 1; row++) {
			for (int col = 0; col <= cols + 1; col++) {
				if ((row == 0 || row == rows + 1) && (col == 0 || col == cols + 1)) {
					gridPanel.add(new JLabel(""));
				} else if (row == 0 || row == rows + 1) {
					if (col > 0 && col <= cols) {
						JLabel label = new JLabel(String.valueOf(col), SwingConstants.CENTER);
						label.setPreferredSize(new Dimension(CELL_SIZE, CELL_SIZE));
						label.setForeground(UtilsGUI.LIGHT_TEXT_COLOR);
						gridPanel.add(label);
					} else {
						gridPanel.add(new JLabel(""));
					}
				} else if (col == 0 || col == cols + 1) {
					if (row > 0 && row <= rows) {
						char letter = (char) ('A' + row - 1);
						JLabel label = new JLabel(String.valueOf(letter), SwingConstants.CENTER);
						label.setPreferredSize(new Dimension(CELL_SIZE, CELL_SIZE));
						label.setForeground(UtilsGUI.LIGHT_TEXT_COLOR);
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

					button.addActionListener(e -> OnBtnPressed.accept(finalRow, finalCol));

					buttons[finalRow][finalCol] = button;
					gridPanel.add(button);
				}
			}
		}

		return gridPanel;
	}
	
	public static JPanel createConsolePanel(BiConsumer<Integer, Integer> consumer, JButton[][] buttons) {
		JPanel panel = new JPanel(new BorderLayout());
		panel.putClientProperty("name", "consolePanel");
        panel.setBackground(UtilsGUI.DARK_BACKGROUND);

        // Textarea für Ausgaben
        JTextArea ausgabeFeld = new JTextArea();
        ausgabeFeld.setEditable(false);
        ausgabeFeld.setBackground(UtilsGUI.FIELD_COLOR);
        ausgabeFeld.setForeground(UtilsGUI.LIGHT_TEXT_COLOR);
        ausgabeFeld.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(ausgabeFeld);
        ausgabeFeld.append("> Select a cell by giving\n  me the row and column\n");
        ausgabeFeld.append("  Example: \"A1\" selects the cell A1\n");
        
        // Eingabefeld
        JTextField eingabeFeld = new JTextField();
        eingabeFeld.setBackground(UtilsGUI.BUTTON_COLOR);
        eingabeFeld.setForeground(UtilsGUI.LIGHT_TEXT_COLOR);
        eingabeFeld.setCaretColor(UtilsGUI.LIGHT_TEXT_COLOR);
        eingabeFeld.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Eingabe-Handling
        eingabeFeld.addActionListener(e -> {
        	String input = eingabeFeld.getText();
            eingabeFeld.setText("");
            ausgabeFeld.append("> " + input + "\n");

            if(input.matches("^[A-Z][0-9]+$")) {
            	int row = (int)input.charAt(0) - 65; 
                int col = Integer.parseInt(input.substring(1)) - 1; 
                
                if(row >= 0 && row < buttons.length &&
            	   col >= 0 && col < buttons[0].length) {
                	consumer.accept(row, col);
                }
            }
        });

        // Komponenten hinzufügen
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(eingabeFeld, BorderLayout.SOUTH);
        
        panel.setPreferredSize(new Dimension(300, 200));
        return panel;
	}

	public static JPanel createWaitingRoomPanel(ActionListener listenerCreateGameBtn, ActionListener listenerJoinGameBtn) {
		var waitingRoomPanel = new JPanel(new GridLayout(2, 1, 10, 10));
		waitingRoomPanel.putClientProperty("name", "waitingRoomPanel");
		waitingRoomPanel.setBackground(UtilsGUI.DARK_BACKGROUND);
		waitingRoomPanel.setPreferredSize(new Dimension(500, 300));

		// "Create Game" Button
		var createGameButton = new JButton("Create Game");
		createGameButton.setFont(new Font("Arial", Font.BOLD, 16));
		createGameButton.setBackground(new Color(0, 153, 255)); 
		createGameButton.setForeground(Color.WHITE);
		createGameButton.setFocusPainted(false);
		createGameButton.addActionListener(listenerCreateGameBtn);
		

		// "Join Game" Button
		var joinGameButton = new JButton("Join Game");
		joinGameButton.setFont(new Font("Arial", Font.BOLD, 16));
		joinGameButton.setBackground(new Color(0, 204, 0));
		joinGameButton.setForeground(Color.WHITE);
		joinGameButton.setFocusPainted(false);
		joinGameButton.addActionListener(listenerJoinGameBtn);

		waitingRoomPanel.add(createGameButton);
		waitingRoomPanel.add(joinGameButton);
		
		return waitingRoomPanel;
	}

	public static JPanel createScoreboardPanel() {
		var scoreboardPanel = new JPanel(new BorderLayout());
		scoreboardPanel.putClientProperty("name", "scoreboardPanel");
		scoreboardPanel.setBackground(DARK_BACKGROUND);
		scoreboardPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

		JLabel titleLabel = new JLabel("Scoreboard");
		titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
		titleLabel.setForeground(LIGHT_TEXT_COLOR);
		titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
		scoreboardPanel.add(titleLabel, BorderLayout.NORTH);

		var playersPanel = new JPanel();
		playersPanel.putClientProperty("name", "playersPanel");
		playersPanel.setLayout(new BoxLayout(playersPanel, BoxLayout.Y_AXIS));
		playersPanel.setBackground(DARK_BACKGROUND);
		scoreboardPanel.add(new JScrollPane(playersPanel), BorderLayout.CENTER);

		scoreboardPanel.setPreferredSize(new Dimension(350, 0));
		return scoreboardPanel;
	}




}
