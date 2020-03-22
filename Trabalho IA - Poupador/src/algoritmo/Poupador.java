package algoritmo;

import java.awt.Point;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import algoritmo.enums.MoveMapping;
import algoritmo.enums.VisionMapping;

public class Poupador extends ProgramaPoupador {
	private static final int NOVISION = -2;
	private static final int OUTSIDE = -1;
	private static final int WALL = 1;
	private static final int BANK = 3;
	private static final int COIN = 4;
	private static final int POWERCOIN = 5;
	private static final int FREE = 0;
	
	public Point BANK_LOCATION;
	public int FEAR_FACTOR = 0;
	public int AMBITION_FACTOR = 0;
	public String goal;
	public boolean knowsBankLocation = false;
	public int averageUp = 0;
	public int averageDown = 0;
	public int averageRight = 0;
	public int averageLeft = 0;
	public int previousAction = 0;
	
	public String name;
	
	public HashMap<String, int[]> generalDirections = new HashMap<String, int[]>();
	public HashMap<String, Integer> directionsWeight = new HashMap<String, Integer>();
	public int[] weight;
	
	public HashMap<Point, Integer> Mapa = new HashMap<Point, Integer>();
	
	public HashMap<Integer, int[]> VisionPointMapping = new HashMap<Integer, int[]>();
	public int[] generalUp = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
	public int[] generalDown = {14, 15, 16, 17, 18, 19, 20, 21, 22, 23};
	public int[] generalLeft = {0, 1, 5, 6, 10, 11, 14, 15, 19, 20};
	public int[] generalRight = {3, 4, 8, 9, 12, 13, 17, 18, 22, 23};


	public Poupador() {
		
		this.name = "Poupador" + Math.floor(Math.random() * 5);
		generalDirections.put("UP", generalUp);
		generalDirections.put("DOWN", generalDown);
		generalDirections.put("LEFT", generalLeft);
		generalDirections.put("RIGHT", generalRight);
		VisionPointMapping.put(0, new int[] {-2, -2});
		VisionPointMapping.put(1, new int[] {-1, -2});
		VisionPointMapping.put(2, new int[] {0, -2});
		VisionPointMapping.put(3, new int[] {1, -2});
		VisionPointMapping.put(4, new int[] {2, -2});
		VisionPointMapping.put(5, new int[] {-2, -1});
		VisionPointMapping.put(6, new int[] {-1, -1});
		VisionPointMapping.put(7, new int[] {1, 1});
		VisionPointMapping.put(8, new int[] {1, -1});
		VisionPointMapping.put(9, new int[] {2, -1});
		VisionPointMapping.put(10, new int[] {-2, 0});
		VisionPointMapping.put(11, new int[] {-1, 0});
		VisionPointMapping.put(12, new int[] {1, 0});
		VisionPointMapping.put(13, new int[] {2, 0});
		VisionPointMapping.put(14, new int[] {-2, 1});
		VisionPointMapping.put(15, new int[] {-1, 1});
		VisionPointMapping.put(16, new int[] {0, -1});
		VisionPointMapping.put(17, new int[] {1, 1});
		VisionPointMapping.put(18, new int[] {2, 1});
		VisionPointMapping.put(19, new int[] {-2, 2});
		VisionPointMapping.put(20, new int[] {-1, 2});
		VisionPointMapping.put(21, new int[] {0, 2});
		VisionPointMapping.put(22, new int[] {1, 2});
		VisionPointMapping.put(23, new int[] {2, 2});
		
		setGoal("explore");
	}


	public int acao() {
		weight = new int[24];
		evaluateMap();
		int move = evaluateVisibleSurroundings();
		return move;
	}

	public void evaluateMap() {
		if (previousAction != 0) {
			weight[previousAction] += -100;			
		}

		Point currentPos = sensor.getPosicao();
		if (Mapa.get(currentPos) == null) {
			Mapa.put(currentPos, 1);
		} else {
			Mapa.put(currentPos, Mapa.get(currentPos) + 1);
		}

		int[] currentVision = sensor.getVisaoIdentificacao();
		System.out.println(currentPos);
		for (int i = 0; i < currentVision.length; i++) {
			Point onMap = getPointLocation(i);
			System.out.println(onMap);
			weight[i] += Mapa.get(onMap) == null ? 0 : -5 * Mapa.get(onMap);
			if (onMap.equals(new Point(-1, -1))) {
				weight[i] = -100;
			}
		}
	}
	
	public int evaluateVisibleSurroundings() {
		int move = 0;
		int[] currentVision = sensor.getVisaoIdentificacao();
		
		System.out.println(this.name);
		
		//Evaluating all vision sensor spots
		for (int i = 0; i < currentVision.length; i++) {
			switch(currentVision[i]) {
				case BANK:
					BANK_LOCATION = getPointLocation(i);
					setKnowsBankLocation(true);
					setGoal("get coins");
					weight[i] += 1000;
					break;
				case COIN:
					weight[i] += 1000;
					break;
				default:
					if (currentVision[i] >= 100 && currentVision[i] < 200) {
						System.out.println("Poupador Found");
						weight[i] += -500;
					} else if(currentVision[i] >= 200) {
						System.out.println("Ladrao Found");
						weight[i] += -1000;
					}
					break;
			}
		}
		
		//evaluate directly possible moves (up, down, right, left)
		for (String key: generalDirections.keySet()) {
			int pos = VisionMapping.valueOf(key).getValue();
			int movePos = currentVision[pos];
			System.out.println(key+": " + movePos);
			if (movePos == WALL || movePos == POWERCOIN || movePos == OUTSIDE || movePos >= 100) {
				weight[pos] += -6000;
			}
		}
		

		Integer[] averageWeights = {getAverageFromMove("UP"), getAverageFromMove("DOWN"), getAverageFromMove("RIGHT"), getAverageFromMove("LEFT")};
		ArrayList<Integer> equalWeights = new ArrayList<Integer>();

		int maxx = Collections.max(Arrays.asList(averageWeights));
		
		
		//Find equal max values
		for (int i = 0; i < averageWeights.length; i++) {
			if (averageWeights[i] == maxx) {
				equalWeights.add(i + 1);
			}
		}

		if (equalWeights.size() > 1) {
			int indice = (int) (Math.random() * (equalWeights.size()));
			move = equalWeights.get(indice);
		} else {
			move = getMaxWeightMove();
		}

		System.out.println(getAverageFromMove("UP") + " - UP " + getAverageFromMove("DOWN")+ " - DOWN "+getAverageFromMove("RIGHT")+" - RIGHT "+getAverageFromMove("LEFT")+" - LEFT ");
		
		previousAction = getOppositeAction(move);
		return move;
	}

	
	public int getAverageFromMove(String move) {
		int[] moveSpotsInVisionArray = generalDirections.get(move);
		int totalSum = 0;
		for(int val: moveSpotsInVisionArray) {
			totalSum += weight[val];
		}
		
		return totalSum;
	}
	
	public int getOppositeAction(int move) {
		if (move == 7) {
			return 16;
		} else if (move == 16) {
			return 7;
		} else if (move == 11) {
			return 12;
		} else if (move == 12) {
			return 11;
		}
		
		return 0;
	}
	
	public int getMaxWeightMove() {
		int largest = -99999999;
		String move = "";
		for (String key: generalDirections.keySet()) {
			if (getAverageFromMove(key) > largest) {
				largest = getAverageFromMove(key);
				move = key;
			}
		}

		return MoveMapping.valueOf(move).getValue();
	}
	
	public Point getNextPos(String move) {
	    Point currentPos = sensor.getPosicao();
	    Point nextPos = currentPos;
	    switch (move) {
	    	case "UP":
	    		nextPos.y--;
	    		break;
	    	case "DOWN":
	    		nextPos.y++;
	    		break;
	    	case "RIGHT":
	    		nextPos.x++;
	    		break;
	    	case "LEFT":
	    		nextPos.x--;
	    		break;
	    }
	    
	    return nextPos;
	}
	
	public Point getPointLocation(int visionPos) {
		Point p = new Point(-1, -1);
		Point currentP = sensor.getPosicao();
		int adjustX = VisionPointMapping.get(visionPos)[0];
		int adjustY = VisionPointMapping.get(visionPos)[1];
		if (currentP.x + adjustX < 30 &&  currentP.x + adjustX >= 0 && currentP.y + adjustY < 30 && currentP.y + adjustY >= 0) {
			p.x = currentP.x + adjustX;
			p.y = currentP.y + adjustY;	
		} 
		return p;
	}

	public void setKnowsBankLocation(boolean knowsBankLocation) {
		this.knowsBankLocation = knowsBankLocation;
	}

	public boolean getKnowsBankLocation() {
		return knowsBankLocation;
	}

	public String getGoal() {
		return goal;
	}

	public void setGoal(String goal) {
		this.goal = goal;
	}

}