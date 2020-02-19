package algoritmo;

import java.awt.Point;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Poupador extends ProgramaPoupador {
	public int[][] Mapa = new int[30][30];
	public String name;
	public Map<String,Integer> viableMoves = new HashMap<String,Integer>();
	public Map<String,Integer> movesHashMap = new HashMap<String,Integer>();
	public List<Integer> currentViableMovesList = new ArrayList<Integer>();
	public final int LEFT = 11;
	public final int RIGHT = 12;
	public final int UP = 7;
	public final int DOWN = 16;


	public Poupador() {
		for (int[] row: this.Mapa) {
			Arrays.fill(row, 0);
		}

		this.name = "Poupador 0" + (int) Math.random() * 5;

		movesHashMap.put( "up", new Integer( 1 ));
		movesHashMap.put( "down", new Integer( 2 ));
		movesHashMap.put( "right", new Integer( 3 ));
		movesHashMap.put( "left", new Integer( 4 ));
		viableMoves.put( "up", new Integer( 0 ));
		viableMoves.put( "down", new Integer( 0 ));
		viableMoves.put( "right", new Integer( 0 ));
		viableMoves.put( "left", new Integer( 0 ));
	}


	public int acao() {
		viableMoveOptions();
		int val = weightProb();
		return val;
	}


	// only visao based so far
	public void viableMoveOptions() {
		int[][] currentMapa = this.Mapa;
		int[] currentVisao = sensor.getVisaoIdentificacao();
		Point currentPosicao = sensor.getPosicao();
		int positionX = (int) currentPosicao.getY();
		int positionY = (int) currentPosicao.getX();
		int visionMapX = positionX - 2;
		int visionMapY = positionY - 2;
		int countVisionArray = 0;
		for(String key: this.viableMoves.keySet()) {
			this.viableMoves.put(key, 0);
		}

		//Increment current position
		currentMapa[positionX][positionY]++;


		for (int row = visionMapX; row < visionMapX + 5; row ++) {
			//check if out of bounds
			for (int column = visionMapY; column < visionMapY + 5; column++) {

				//check if same place as currently is
				if(row == positionX && column == positionY) {continue;}

				switch (currentVisao[countVisionArray]) {
					case 1:
						currentMapa[row][column] = 88;
						break;
					case 3:
						currentMapa[row][column] = 44;
						break;
					case 4:
						currentMapa[row][column] = 4;
						break;
				}
				countVisionArray++;
			}

		}

		//Check for viable options to move (VERIFYING: walls)
	 	int currentLeftWeight = this.viableMoves.get("left");
		int currentRightWeight = this.viableMoves.get("right");
		int currentUpWeight = this.viableMoves.get("up");
		int currentDownWeight = this.viableMoves.get("down");

		if (currentVisao[LEFT] >= 0 && currentVisao[LEFT] != 1) {
			if(currentMapa[positionX][positionY-1] < 1) {
				this.viableMoves.put("left", currentLeftWeight+=10);
			} else {
				this.viableMoves.put("left", currentLeftWeight+=5);
			}
		}
		if(currentVisao[DOWN] >= 0  && currentVisao[DOWN] != 1) {
			if(currentMapa[positionX+1][positionY] < 1) {
				this.viableMoves.put("down", currentDownWeight+=10);
			} else {
				this.viableMoves.put("down", currentDownWeight+=5);
			}
		}
		if(currentVisao[UP] >= 0  && currentVisao[UP] != 1) {
			if(currentMapa[positionX-1][positionY] < 1) {
				this.viableMoves.put("up", currentUpWeight+=10);
			} else {
				this.viableMoves.put("up", currentUpWeight +=5);
			}
		}
		if(currentVisao[RIGHT] >= 0  && currentVisao[RIGHT] != 1) {
			if(currentMapa[positionX][positionY+1] < 1) {
				this.viableMoves.put("right", currentRightWeight+=10);
			} else {
				this.viableMoves.put("right", currentRightWeight+=5);
			}
		}

		prettyPrintMatrix();
	}


	//Utility Functions
	public int weightProb() {
		Map<String, Integer> moves = this.viableMoves;
		double totalP = 0;
		for(String key:moves.keySet()) {
			totalP += moves.get(key);
		}

		double rand  = Math.random() * totalP;
		String move = "probably error";
		for(String key:moves.keySet()) {
			move = key;
			int moveProb = moves.get(key);
			rand -= moveProb;
			if (rand < 0) {
				break;
			}
		}

		return movesHashMap.get(move);
	}

	public void prettyPrintMatrix() {
		for (int i = 0; i < 25; ++i)
			System.out.println();
		for(int i=0; i<30; i++){
			for(int j=0; j<30; j++){
				System.out.print(String.format("%7s", this.Mapa[i][j]));
			}
			System.out.println("");
		}
	}

	public int getArrayIndex(int[] arr,int value) {
		int k=0;
		for(int i=0;i<arr.length;i++){
			if(arr[i]==value){
				k=i;
				break;
			}
		}
		return k;
	}
}