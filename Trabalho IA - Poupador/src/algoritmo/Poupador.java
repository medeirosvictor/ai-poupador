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


public class Poupador extends ProgramaPoupador {
	
	public enum MoveMapping {
		UP(1),
		DOWN (2),
		RIGHT(3),
	    LEFT(4);

	    public final int value ;
	    MoveMapping(int value) {
	        this.value = value;
	    }

	    public int getValue() {
	    	return value;
	    }
	}

	public enum VisionMapping {
		UP(7),
		DOWN (16),
		RIGHT(12),
		LEFT(11);

	    public final int value ;
		VisionMapping(int value) {
	        this.value = value;
	    }

	    public int getValue() {
	    	return value;
	    }
	}

    private static final int NOVISION = -2;
    private static final int OUTSIDE = -1;
    private static final int WALL = 1;
    private static final int BANK = 3;
    private static final int COIN = 4;
    private static final int POWERCOIN = 5;
    private static final int FREE = 0;
    private static final int THIEF = 200;
    
    private double metaLearningRate = 0.1;

    private int goToBankWill = 0;
    
    private int noCoinTimeStamp;
    private int previousCoinAmount = 0; 
    
    private int W_NOVISION;
    private int W_OUTSIDE;
    private int W_WALL;
    private int W_BANK;
    private int W_COIN;
    private int W_POWERCOIN;

    private double ambition;
    private double fear;

    private static Point BANK_LOCATION = new Point(-4, -4);
    
    private String goal;
    private boolean knowsBankLocation = false;
    private int previousAction = 0;
    private String name;

    private HashMap<String, int[]> visionDirections = new HashMap<String, int[]>();
    private int[] weight;

    //For visited locations and the amount of times it was visited
    private HashMap<Point, Integer> ExplorationMap = new HashMap<Point, Integer>();

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

        ambition = initWeight();
        fear = initWeight();

        setGoal("explore");
    }

    public int acao() {
        System.out.println("Action Init For: " + this.name);
        System.out.println("Ambition: " + ambition);
        weight = new int[24];
        int move = getActionGoalBasedBehavior();
        return move;
    }
    

    public int getActionGoalBasedBehavior() {
    	evaluateFeelings();
    	int action = evaluateAction();
    	return action;
    }
    
    public void evaluateNoCoinTimeStamp(int prevCoinAmount) {
    	int currentCoinAmount = sensor.getNumeroDeMoedas();
    	
    	if (prevCoinAmount == currentCoinAmount) {
    		noCoinTimeStamp++;
    	} else {
    		goToBankWill = (int) Math.pow(currentCoinAmount, 2);
    		noCoinTimeStamp = 0;
    	}
    }

    
    //Evaluates based on non-visited locations
    public void evaluateMap() {
        if (previousAction != 0) {
            weight[previousAction] += -50;
        }

        Point currentPos = sensor.getPosicao();
        if (ExplorationMap.get(currentPos) == null) {
            ExplorationMap.put(currentPos, 1);
        } else {
        	int currentThermal = ExplorationMap.get(currentPos);
            ExplorationMap.put(currentPos, currentThermal + 1);
        }

        int[] currentVision = sensor.getVisaoIdentificacao();
        System.out.println(currentPos);
        for (int i = 0; i < currentVision.length; i++) {
            Point onMap = getPointLocation(i);
            System.out.println("Value: " +  ExplorationMap.get(onMap));
            weight[i] += ExplorationMap.get(onMap) == null ? 0 : -5 * ExplorationMap.get(onMap);
        }
    }
    

    public void evaluateAmbition() {
//    	ambition
//    	noCoinTimeStamp
//    	metaLearningRate
//    	pesar moedas de acordo com ambicao / possibilidade de alterar o fator de ambicao do poupador
    	
    	
    }
    
    public void evaluateFear() {
//    	fear
//    	noCoinTimeStamp
//    	metaLearningRate
//    	dois tipos de alteracoes -> sutis e bruscas 
//
//    	int coinAmount = sensor.getNumeroDeMoedas();
//    	fear = fear + (metaLearnRate * coinAmount);
    }
    
    public int evaluateBankWeight() {
    	int val = 0;
    	int coinAmount = sensor.getNumeroDeMoedas() < 1 ? -1 : sensor.getNumeroDeMoedas();
    	int knowsBankLocation = getKnowsBankLocation() ? 1 : 0;
    	val = 1000 * knowsBankLocation * coinAmount;

		return val;
    }
    
    public int evaluateCoin() {
    	int val = 0;
    	int knowsBankLocation = getKnowsBankLocation() ? 1 : -1;
    	int surroundingThiefAmountVision = isSeeingThief();
    	
    	if (getGoal().equals("explore")) {
    		val = (int) (50 * knowsBankLocation * ambition) + noCoinTimeStamp;
    		val = surroundingThiefAmountVision > 0 ? val / surroundingThiefAmountVision : val;
    		
			return val;
    	} else if (getGoal().equals("get coins")) {
    		val = (int) (100 * knowsBankLocation * (1 - fear));
    		val = surroundingThiefAmountVision > 0 ? val / surroundingThiefAmountVision : val;
    		return  val;
    	}
    	
    	return val;
    }
    
    public void shouldIBankIt() {
    	boolean shouldI = false;
    	if (!BANK_LOCATION.equals(new Point(-4,-4)) && getKnowsBankLocation()) {
    		fear = (fear == 1) ? 0.9999999999999999 : fear;
    		System.out.println((goToBankWill / (1 - fear)) + calculateDistanceToPoint(BANK_LOCATION));     		
    	}
    
    }
    
//    evaluateGoToBankWill() {
//    	
//    }
//    
    public void evaluateFeelings() {
    	//changes to fear, ambition and willingness to go to bank(?)
    	shouldIBankIt();
    	System.out.println("Hora de aprender!");
    }
    
    public int evaluateAction() {
    	int action = 0;
        int[] currentVision = sensor.getVisaoIdentificacao();
    	
        evaluateMap();
        
        //Evaluating all vision sensor spots
        for (int i = 0; i < currentVision.length; i++) {
            Point onMap = getPointLocation(i);
            switch(currentVision[i]) {
                case BANK:
                	if (!getKnowsBankLocation()) {
                		BANK_LOCATION = getPointLocation(i);
                        setKnowsBankLocation(true);
                        setGoal("get coins");
                	}

                	weight[i] += evaluateBankWeight();
                break;
                case COIN:
                	weight[i] += evaluateCoin();
                break;
                
                default:
                    if (currentVision[i] >= 100 && currentVision[i] < 200) {
                    	System.out.println("Poupador Found");
                    	
                    	// Comuncation between a Poupador that knows the bank's location and one in its sight
                    	// that does not know the location of said bank. We set the flag true and now he is able
                    	// to access the static variable BANK_LOCATION to his advantage.
                    	if (!getKnowsBankLocation() && !BANK_LOCATION.equals(new Point(-4,-4))) {
                    		System.out.println("Bank Comunication!");
                    		setKnowsBankLocation(true);
                    		setGoal("get coins");
                    	}
                        weight[i] += -200;
                    } else if(currentVision[i] >= 200) {
                        System.out.println("Ladrao Found");
                        weight[i] += -500;
                    }
                break;
            }
        }

      //evaluate directly possible moves (up, down, right, left)
        for (String key: visionDirections.keySet()) {
            int pos = VisionMapping.valueOf(key).getValue();
            int movePos = currentVision[pos];
            System.out.print(key+": " + movePos + " ");
            if (movePos == WALL || movePos == OUTSIDE || movePos >= 100) {
                weight[pos] += -1500;
            } else if (movePos == POWERCOIN && sensor.getNumeroDeMoedas() < 5) {
            	weight[pos] += -500;
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

        previousAction = getOppositeAction(action);
        evaluateNoCoinTimeStamp(previousCoinAmount);
        previousCoinAmount = sensor.getNumeroDeMoedas();
        
        System.out.println("No Coin Time Stamp: " + noCoinTimeStamp);
        printWeightedOptions();
        printWeightedVisionMatrix();

        return action;
    }
    
    private int isSeeingThief() {
    	int[] vision = sensor.getVisaoIdentificacao();
        int thiefNum = 0;
        for(int i = 0; i< vision.length; i++) {
            if(vision[i] >= THIEF) {
                thiefNum++;
            }
        }

        return thiefNum;
    }
    
    public int calculateDistanceToPoint(Point finish) {
    	Point start = sensor.getPosicao();
        return Math.abs(start.x - finish.x) + Math.abs(start.y - finish.y);
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
    
    public void printWeightedVisionMatrix() {
    	System.out.println("Weighted Vision Matrix");
    	for (int i = 0; i < weight.length; i++) {
    		if (i % 5 == 0) {
    			System.out.println("");
    		}
    		System.out.print(weight[i] + "     ");
    	}
    	System.out.println("");
    }
    
    public void printWeightedOptions() {
    	System.out.println(getSumOfWeights("UP") + " UP /" + getSumOfWeights("DOWN")+ " DOWN /"+getSumOfWeights("RIGHT")+" RIGHT /"+getSumOfWeights("LEFT")+" LEFT ");
    	System.out.println("");
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