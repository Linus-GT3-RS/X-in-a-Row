package lab1.game;

import java.awt.Color;

public class Player {
	
	private int port;
	
	private String name;
	private Color color;
	
	private int points;
	
	public Player(int port, String name) {
		super();
		this.port = port;
		this.name = name;
		this.color = calcColor(name);
		this.points = 0;
	}
	
	public static Color calcColor(String name) {
        int hash = name.hashCode();
        
        int red = (hash >> 16) & 0xFF;   // Die oberen 8 Bits für Rot
        int green = (hash >> 8) & 0xFF;  // Die mittleren 8 Bits für Grün
        int blue = hash & 0xFF;          // Die unteren 8 Bits für Blau

        return new Color(red, green, blue);
	}
	
	
	public int getPort() {
		return this.port;
	}
	
	public Color getColor() {
		return this.color;
	}
	
	// can be from 0-9
	public int getID() {
		return port - 90000;
	}
	
	public int getPoints() {
		return points;
	}
	
	public Player incrPoints() {
		points++;
		return this;
	}
	
	public void setPoints(int p) {
		points = p;
	}
	
	public String getName() {
		return name;
	}
}
