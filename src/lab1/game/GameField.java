package lab1.game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GameField {

	public static final int EMPTY_FIELD = -1;

	private int[][] field;

	public GameField(int rows, int cols) {
		field = new int[rows][cols];

		// Init each cell as empty
		for(int[] row : field) {
			Arrays.fill(row, EMPTY_FIELD);
		}
	}
	
	public GameField(int[][] field) {
		this.field = field;
	}
	
	public int[][] getCopy() {
		return field.clone();
	}

	public int getCell(int r, int c) {
		return field[r][c];
	}

	public boolean setCell(int r, int c, int player) {
		if(field[r][c] != -1) return false;

		field[r][c] = player;
		return true;
	}
	
	public void resetCells(List<Cell> cells) {
		cells.forEach((Cell c) -> field[c.r()][c.c()] = EMPTY_FIELD);
	}
	
	// BAD BAD BAD but has to exist because of compatibility with eduards code :((
	// gets id of removed player and adjusts id in field of all players > id
	public void adjustCellsBy1(int idRemovedPlayer) {
		for(int i = 0; i < getRowNumb(); i++) {
			for(int j = 0; j < getColNumb(); j++) {
				if(field[i][j] > idRemovedPlayer) {
					field[i][j] = field[i][j] - 1;
				}
			}
		}
	}
	
	public void removePlayer(int id) {
		for(int i = 0; i < getRowNumb(); i++) {
			for(int j = 0; j < getColNumb(); j++) {
				if(field[i][j] == id) {
					field[i][j] = EMPTY_FIELD;
				}
			}
		}
	}
	
	public int getRowNumb() {
		return field.length;
	}
	
	public int getColNumb() {
		return field[0].length;
	}

}
