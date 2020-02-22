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
	public Map<String,int[]> movableMapLocations = new HashMap<String,int[]>();
	public Map<String,Integer> movesHashMap = new HashMap<String,Integer>();
	public List<Integer> currentViableMovesList = new ArrayList<Integer>();
	public final int LEFT = 11;
	public final int RIGHT = 12;
	public final int UP = 7;
	public final int DOWN = 16;
	public final int[] UPMapLocation = {-1, 0};
	public final int[] DOWNMapLocation = {+1, 0};
	public final int[] RIGHTMapLocation = {0, +1};
	public final int[] LEFTMapLocation = {0, -1};

	public boolean knowsBankLocation = false;


	public Poupador() {
		for (int[] row: this.Mapa) {
			Arrays.fill(row, 0);
		}

		int nameDiff = (int) Math.random() * 5;
		this.name = "Poupador 0" + nameDiff;

		movesHashMap.put( "UP", new Integer( 1 ));
		movesHashMap.put( "DOWN", new Integer( 2 ));
		movesHashMap.put( "RIGHT", new Integer( 3 ));
		movesHashMap.put( "LEFT", new Integer( 4 ));
		viableMoves.put( "UP", new Integer( 0 ));
		viableMoves.put( "DOWN", new Integer( 0 ));
		viableMoves.put( "RIGHT", new Integer( 0 ));
		viableMoves.put( "LEFT", new Integer( 0 ));

		movableMapLocations.put("UP", UPMapLocation);
		movableMapLocations.put("DOWN", DOWNMapLocation);
		movableMapLocations.put("RIGHT", RIGHTMapLocation);
		movableMapLocations.put("LEFT", LEFTMapLocation);
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
		for(String key: viableMoves.keySet()) {
			viableMoves.put(key, 0);
		}

		//Increment current position
		currentMapa[positionX][positionY]++;


		for (int row = visionMapX; row < visionMapX + 5; row ++) {
			for (int column = visionMapY; column < visionMapY + 5; column++) {
				if(row == positionX && column == positionY) {continue;}

				switch (currentVisao[countVisionArray]) {
					//Wall case
					case 1:
						currentMapa[row][column] = 88;
						break;

					//Bank case
					case 3:
						currentMapa[row][column] = 44;
						setKnowsBankLocation(true);
						break;

					//Coin case
					case 4:
						currentMapa[row][column] = 4;
						break;
				}
				countVisionArray++;
			}

		}

		evaluateMoveSpot("UP", currentVisao[UP]);
		evaluateMoveSpot("DOWN", currentVisao[DOWN]);
		evaluateMoveSpot("LEFT", currentVisao[LEFT]);
		evaluateMoveSpot("RIGHT", currentVisao[RIGHT]);

		prettyPrintMatrix();
	}


	//Utility Functions
	public void evaluateMoveSpot(String move, int currentSpotStatus) {
		int[][] currentMapa = this.Mapa;
		int currentMoveWeight = getCurrentMoveWeight(move);
		Point currentPosicao = sensor.getPosicao();
		int positionX = (int) currentPosicao.getY();
		int positionY = (int) currentPosicao.getX();
		int adjustedX = 0;
		int adjustedY = 0;

		//Get position in own map
		for (String key: movableMapLocations.keySet()) {
			if(key == move) {
				adjustedX = positionX + movableMapLocations.get(key)[0];
				adjustedY = positionY + movableMapLocations.get(key)[1];

				if(adjustedX > 29 || adjustedY > 29 || adjustedX < 0 || adjustedY < 0) {
					return;
				}
			}
		}

		//Check for unvisited spaces
		if (currentMapa[adjustedX][adjustedY] >= 1) {
			viableMoves.put(move, currentMoveWeight+=5);
		}

		if (currentSpotStatus == 0 && currentMapa[adjustedX][adjustedY] < 1) {
			viableMoves.put(move, currentMoveWeight+=50);
		}

		//Ignore coins or supercoins if no bank location
		if ((currentSpotStatus == 4 || currentSpotStatus == 5) && !getKnowsBankLocation()) {
			viableMoves.put(move, currentMoveWeight-=100);
		}

		if (getKnowsBankLocation() && currentSpotStatus == 4) {
			viableMoves.put(move, currentMoveWeight+=100);
		}
	}

	public int getCurrentMoveWeight(String move) {
		return viableMoves.get(move);
	}

	public boolean hasNearbyCoinButNoBankFound() {
		boolean test = false;
		int[] currentVisao = sensor.getVisaoIdentificacao();
		if (!this.knowsBankLocation && (currentVisao[RIGHT] == 4 ||currentVisao[LEFT] == 4 ||currentVisao[UP] == 4 ||currentVisao[DOWN] == 4)) {
			test = true;
		}

		return test;
	}
	public int weightProb() {
		Map<String, Integer> moves = this.viableMoves;
		double totalP = 0;
		for(String key:moves.keySet()) {
			if (moves.get(key) < 0) {
				moves.put(key, 0);
			}

			totalP += moves.get(key);
		}

		double rand  = Math.random() * totalP;
		String currentMove = "probably error";
		for(String key:moves.keySet()) {
			currentMove = key;
			int moveProb = moves.get(currentMove);
			rand -= moveProb;
			if (rand < 0) {
				break;
			}
		}

		return movesHashMap.get(currentMove);
	}

	public void prettyPrintMatrix() {
		for (int i = 0; i < 25; ++i)
			System.out.println();

		System.out.println(this.name);
		for(int i=0; i<30; i++){
			for(int j=0; j<30; j++){
				System.out.print(String.format("%7s", this.Mapa[i][j]));
			}
			System.out.println("");
		}
	}

	public void setKnowsBankLocation(boolean knowsBankLocation) {
		this.knowsBankLocation = knowsBankLocation;
	}

	public boolean getKnowsBankLocation() {
		return knowsBankLocation;
	}
}