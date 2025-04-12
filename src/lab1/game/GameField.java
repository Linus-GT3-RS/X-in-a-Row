package lab1.game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GameField {

	private int[][] field;

	public static final int EMPTY_FIELD = -1;

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
	
	public int getRowNumb() {
		return field.length;
	}
	
	public int getColNumb() {
		return field[0].length;
	}

}
