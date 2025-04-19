package lab1.game;

import java.awt.Color;

public class Player {	
	
	private String name;
	private Color color;	
	private int points;
	
	public Player(String name) {
		this.name = name;
		this.color = calcColor(name);
		this.points = 0;
	}
	
	public static Color calcColor(String name) {
        int hash = name.hashCode();        
        int red = (hash >> 16) & 0xFF;   
        int green = (hash >> 8) & 0xFF;  
        int blue = hash & 0xFF;         
        return new Color(red, green, blue);
	}
		
	public Color getColor() {
		return this.color;
	}
		
	public int getPoints() {
		return points;
	}
	
	public String getName() {
		return name;
	}
	
	public Player incrPoints() {
		points++;
		return this;
	}
	
	public void setPoints(int p) {
		points = p;
	}
	
	
}
