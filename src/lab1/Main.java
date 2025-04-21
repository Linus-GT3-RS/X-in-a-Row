package lab1;

import java.util.Scanner;

import javax.swing.SwingUtilities;

import lab1.game.Player;
import lab1.gui.GameGUI_2DSwing;

public class Main {
	
	private static int START_PORT = 10_000;

	public static void main(String[] args) {
		
		// ----------------- Get Player input via console -----------------
		System.out.println("Running game");
				
		var scanner = new Scanner(System.in);
		System.out.print("Please enter nickname: ");
		String playername = scanner.nextLine();
		
		int playerport;
		do {
			System.out.print("Please choose a port >= "+START_PORT+": ");
			playerport = scanner.nextInt();
		}
		while(playerport < START_PORT);		
		
		final int listenerPort = playerport;
		
		// ----------------- Start GUI -----------------
		
		System.out.println("Starting ...");
		SwingUtilities.invokeLater(() -> new GameGUI_2DSwing(playername, listenerPort));
		
		scanner.close();
	}

}
