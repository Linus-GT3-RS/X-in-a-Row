package lab1;

import java.util.Scanner;

import javax.swing.SwingUtilities;

import lab1.comms.PeerFactory;
import lab1.core.GameGUI_2DSwing;
import lab1.game.Player;

public class Main {

	public static void main(String[] args) {
		
		// ----------------- Get Player input via console -----------------
		
		System.out.println("Running game");
		var scanner = new Scanner(System.in);
		
		System.out.println("Please enter nickname: ");
		String playername = scanner.nextLine();
		
		int playerport;
		do {
			System.out.println("Please choose a port between "+PeerFactory.START_PORT+" - "+PeerFactory.LAST_PORT+": ");
			playerport = scanner.nextInt();
		}
		while(playerport < PeerFactory.START_PORT || playerport >= PeerFactory.LAST_PORT);
		
		var player = new Player(playerport, playername);
		
		// ----------------- Start GUI -----------------
		
		System.out.println("Starting ...");
		SwingUtilities.invokeLater(() -> new GameGUI_2DSwing(player));
		
		scanner.close();
	}

}
