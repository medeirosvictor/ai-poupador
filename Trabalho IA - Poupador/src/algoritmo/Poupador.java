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
    
    private int W_NOVISION;
    private int W_OUTSIDE;
    private int W_WALL;
    private int W_BANK;
    private int W_COIN;
    private int W_POWERCOIN;

    private double courage;
    private double fear;
    private double intelligence;

    private Point BANK_LOCATION;
    private String goal;
    private boolean knowsBankLocation = false;
    private int previousAction = 0;
    private String name;

    private HashMap<String, int[]> visionDirections = new HashMap<String, int[]>();
    private int[] weight;

    private HashMap<Point, Integer> Mapa = new HashMap<Point, Integer>();

    private HashMap<Integer, int[]> VisionPointMapping = new HashMap<Integer, int[]>();
    private int[] visionUp = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
    private int[] visionDown = {14, 15, 16, 17, 18, 19, 20, 21, 22, 23};
    private int[] visionLeft = {0, 1, 5, 6, 10, 11, 14, 15, 19, 20};
    private int[] visionRight = {3, 4, 8, 9, 12, 13, 17, 18, 22, 23};

    public Poupador() {

        this.name = "Poupador " + Math.floor(Math.random() * 5);

        //Positions on the Vision Sensor for each direction
        visionDirections.put("UP", visionUp);
        visionDirections.put("DOWN", visionDown);
        visionDirections.put("LEFT", visionLeft);
        visionDirections.put("RIGHT", visionRight);

        //Mapping Vision to Point Location on the Main Environment Matrix
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

        courage = initWeight();
        intelligence = initWeight();
        fear = initWeight();
    }

    public int acao() {
        System.out.println("Action Init For: " + this.name);
        weight = new int[24];
        int move = getActionGoalBasedBehavior();
        return move;
    }
    

    public int getActionGoalBasedBehavior() {
    	String currentGoal = getGoal();
    	int action = 0;
    	if (currentGoal.equals("explore")) {
			action = exploreBehavior();
    	} else if (currentGoal.equals("get coins")) {
//    		action = getCoinsBehavior();
    	} else if (currentGoal.equals("go to bank")) {
//    		action = goToBankBehavior();
    	}

    	return action;
    }

    public void evaluateMap() {
        if (previousAction != 0) {
            weight[previousAction] += -100;
        }

        Point currentPos = sensor.getPosicao();
        if (Mapa.get(currentPos) == null) {
            Mapa.put(currentPos, 1);
        } else {
        	int currentThermal = Mapa.get(currentPos);
            Mapa.put(currentPos, currentThermal + 1);
        }

        int[] currentVision = sensor.getVisaoIdentificacao();
        System.out.println(currentPos);
        for (int i = 0; i < currentVision.length; i++) {
            Point onMap = getPointLocation(i);
            System.out.println("Value: " +  Mapa.get(onMap));
            weight[i] += Mapa.get(onMap) == null ? 50 : -5 * Mapa.get(onMap);
            if (onMap.equals(new Point(-1, -1))) {
                weight[i] = -100;
            }
        }
    }
    
    public int exploreBehavior() {
        evaluateMap();
        int action = 0;
        int[] currentVision = sensor.getVisaoIdentificacao();

        //Evaluating all vision sensor spots
        for (int i = 0; i < currentVision.length; i++) {
            Point onMap = getPointLocation(i);
            switch(currentVision[i]) {
                case BANK:
                	if (!getKnowsBankLocation()) {
                		BANK_LOCATION = getPointLocation(i);
                        setKnowsBankLocation(true);
                        setGoal("get coins");
                        weight[i] += 1000;
                	}
                	else {
                		if (sensor.getNumeroDeMoedas() < 1) {
                			weight[i] -= 200;
                		} else {
                			weight[i] += 200;
                		}
                		
                	}
                break;
                case COIN:
                    weight[i] += 1000;
                break;
                case POWERCOIN:
                    weight[i] -= 200;
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
        for (String key: visionDirections.keySet()) {
            int pos = VisionMapping.valueOf(key).getValue();
            int movePos = currentVision[pos];
            System.out.println(key+": " + movePos);
            if (movePos == WALL || movePos == OUTSIDE || movePos >= 100) {
                weight[pos] += -6000;
            }
        }

        Integer[] diretionWeights = {getSumOfWeights("UP"), getSumOfWeights("DOWN"), getSumOfWeights("RIGHT"), getSumOfWeights("LEFT")};
        ArrayList<Integer> equalWeights = new ArrayList<Integer>();
        
        int maxx = Collections.max(Arrays.asList(diretionWeights));

        //Find equal max values
        for (int i = 0; i < diretionWeights.length; i++) {
            if (diretionWeights[i] == maxx) {
                equalWeights.add(i + 1);
            }
        }

        if (equalWeights.size() > 1) {
            int indice = (int) (Math.random() * (equalWeights.size()));
            action = equalWeights.get(indice);
        } else {
            action = getMaxWeightMove();
        }

        System.out.println(getSumOfWeights("UP") + " UP /" + getSumOfWeights("DOWN")+ " DOWN /"+getSumOfWeights("RIGHT")+" RIGHT /"+getSumOfWeights("LEFT")+" LEFT ");
        
        previousAction = getOppositeAction(action);
        System.out.println(previousAction);
        return action;
    }
    
    public int getSumOfWeights(String move) {
        int[] moveSpotsInVisionArray = visionDirections.get(move);
        int totalSum = 0;
        for(int val: moveSpotsInVisionArray) {
            totalSum += weight[val];
        }
        
        return totalSum;
    }

    public int getOppositeAction(int move) {
        if (move == 1) {
            return 16;
        } else if (move == 2) {
            return 7;
        } else if (move == 4) {
            return 12;
        } else if (move == 3) {
            return 11;
        }

        return 0;
    }

    public int getMaxWeightMove() {
        int largest = -99999999;
        String move = "";
        for (String key: visionDirections.keySet()) {
            if (getSumOfWeights(key) > largest) {
                largest = getSumOfWeights(key);
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

    private double initWeight() {
        return new Random().nextDouble();
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