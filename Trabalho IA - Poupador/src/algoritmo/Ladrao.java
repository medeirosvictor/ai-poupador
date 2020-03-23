package algoritmo;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Ladrao extends ProgramaLadrao {
	
//	
	private final Integer WEIGHTMIN = -20000;
	private final Integer WEIGHTMAX = 20000;
	
	private final Integer WEIGHTSTAY = 5900;
	
	private final Integer WEIGHT_CLOSE = 9400;
	private final Integer WEIGHT_LONGER = 8390;
	
	private final Integer WEIGHT_SMELL_POP_ONE = 1270;
	private final Integer WEIGHT_SMELL_POP_TWO = 1100;
	private final Integer WEIGHT_SMELL_POP_THREE = 1000;
	private final Integer WEIGHT_SMELL_POP_FOUR = 900;
	private final Integer WEIGHT_SMELL_POP_FIVE = 800;
	
	private final Integer WEIGHT_HAVE_WALK = 10;
	
	//Codigo percepcoes visiveis
	private final Integer VISION_WITHOUT_LOCAL = -2;
	private final Integer VISION_WITHOUT = -1;
	private final Integer VISION_WALL = 1;
	private final Integer VISION_BANK = 3;
	private final Integer VISION_COIN = 4;
	private final Integer VISION_POWER = 5;
	
	//Codigo percepcoes alfativas
	
	//Acoes
	private final Integer ACTION_UP = 1;
	private final Integer ACTION_DOWN = 2;
	private final Integer ACTION_RIGHT = 3;
	private final Integer ACTION_LEFT = 4;
	
	//Posicao
	
	private final Integer POSITION_UP = 7;
	private final Integer POSITION_DOWN = 16;
	private final Integer POSITION_LEFT = 11;
	private final Integer POSITION_RIGHT = 12;
	
	//Pesos posicao
	private Integer WEIGHT_UP = 1;
	private Integer WEIGHT_DOWN = 1;
	private Integer WEIGHT_RIGHT = 1;
	private Integer WEIGHT_LEFT = 1;
	
	private Integer LAST_ACTION = 0;
	private Integer STOP_TIME = 0;
	private Point LAST_POINT;
	private List<Integer> VALID_ACTION = new ArrayList<Integer>();
	private HashMap<String, MinhaPosicao> HAST_POSITION = new HashMap<String, MinhaPosicao>();
	private List<MinhaPosicao> LIST_POSITION = new  ArrayList<Ladrao.MinhaPosicao>();
	boolean HAS_STOPED = false;
	private Integer LAST_STOPED = 0;
	boolean STOPED = false;
	int LAST_VISION;

	/* Medidas de desempenho
	 * 2 ladr�es por poupador
	 * Se mais de 2 ladr�es em um poupador o mais distante sai e vai atr�s do outro poupador.
	 * Se quando tive mais de 2 ladr�es em um mesmo poupador e a dist�ncia deles forem a mesma, nessa rodada todos os lad�es vao atras do poupador. A menos que a dist�ncia de todos for 1, entao os ladroes pegam o poupador.
	 * */
	
	private List<Integer> CAN_NOT_WALK = new ArrayList<Integer>();
	
	boolean debug = false;

	public int acao() {
		WEIGHT_UP = 1;
		WEIGHT_DOWN = 1;
		WEIGHT_RIGHT = 1;
		WEIGHT_LEFT = 1;
		VALID_ACTION = new ArrayList<Integer>();
		
		if(sensor.getPosicao().x == 1 && sensor.getPosicao().y == 0)
			debug = true;
		
		
		int [] visao = sensor.getVisaoIdentificacao();
		
		analisarSTOPED(visao);
		
		eliminarMovimentosNegativosObvios(visao);
		
		analisarProximidadeMaximaPoupador(visao);
		analisarProximidadeRetaPoupador(visao);
		analisarProximidadeMediaPoupador(visao);
		analisarProximidadeLongaPoupador(visao);

		analisarOlfatoPoupador();
		//analisarOlfatoLadrao();
		
		analisarSTOPED();
		
		analisarLocalMenosVisitado();
		
		analisarMemoria();
		return agir();
	}
	
	private void analisarSTOPED(int [] visao){
		Point myPoint = sensor.getPosicao();
		if(LAST_POINT != null && (LAST_POINT.x == myPoint.x && LAST_POINT.y == myPoint.y)){
			STOPED = true;
			STOP_TIME++;
		}else{
			STOPED = false;
			if(STOP_TIME > 0 && !HAS_STOPED){
				STOP_TIME--;
				LAST_STOPED = 0;
			}
		}
		HAS_STOPED = estouBeco(visao);
		LAST_POINT = myPoint;
	}
	
	private void analisarMemoria(){
		if (HAST_POSITION.size() >= 50) {
			HAST_POSITION.remove(LIST_POSITION.get(0).toString());
			LIST_POSITION.remove(0);
		}
	}
	
	private boolean estouBeco(int [] visao){
		int valorPosicao = visao[POSITION_UP];
		int valorPosicao2 = visao[POSITION_DOWN];
		
		if (!possoAndar(valorPosicao) && !possoAndar(valorPosicao2))
			return true;
		valorPosicao = visao[POSITION_RIGHT];
		valorPosicao2 = visao[POSITION_LEFT];
		if (!possoAndar(valorPosicao) && !possoAndar(valorPosicao2))
				return true;
		return false;
	}
	
	private boolean possoAndar(int valor){
		
		if(valor == VISION_WALL || valor == VISION_WITHOUT || valor == VISION_BANK || valor == VISION_COIN || valor == VISION_POWER)
			return false;
		return true;
	}
	
	private void analisarSTOPED(){
		int peso = ((STOP_TIME * WEIGHTSTAY)*-1);
		if(LAST_STOPED == ACTION_DOWN)
			atribuirPeso(POSITION_DOWN, peso);
		else if(LAST_STOPED == ACTION_UP)
			atribuirPeso(POSITION_UP, peso);
		else if(LAST_STOPED == ACTION_LEFT)
			atribuirPeso(POSITION_LEFT, peso);
		else if(LAST_STOPED == ACTION_RIGHT)
			atribuirPeso(POSITION_RIGHT, peso);
	}
	
	private void analisarOlfatoPoupador(){
		int [] olfato = sensor.getAmbienteOlfatoPoupador();
		
		for (int i = 0; i < olfato.length; i++) {
			if(i == 0) {
				atribuirPesoOlfatoPoupador(POSITION_UP, olfato[i]);
				atribuirPesoOlfatoPoupador(POSITION_LEFT, olfato[i]);
			} else if (i == 1) {
				atribuirPesoOlfatoPoupador(POSITION_UP, olfato[i]);
			}else if (i == 2) {
				atribuirPesoOlfatoPoupador(POSITION_UP, olfato[i]);
				atribuirPesoOlfatoPoupador(POSITION_RIGHT, olfato[i]);
			}else if (i == 3) {
				atribuirPesoOlfatoPoupador(POSITION_LEFT, olfato[i]);
			}else if (i == 4) {
				atribuirPesoOlfatoPoupador(POSITION_RIGHT, olfato[i]);
			}else if (i == 5) {
				atribuirPesoOlfatoPoupador(POSITION_DOWN, olfato[i]);
				atribuirPesoOlfatoPoupador(POSITION_LEFT, olfato[i]);
			}else if (i == 6) {
				atribuirPesoOlfatoPoupador(POSITION_DOWN, olfato[i]);
			}else if (i == 7) {
				atribuirPesoOlfatoPoupador(POSITION_DOWN, olfato[i]);
				atribuirPesoOlfatoPoupador(POSITION_RIGHT, olfato[i]);
			}
				
		}
		
	}
	
	private void analisarOlfatoLadrao(){
		int [] olfato = sensor.getAmbienteOlfatoLadrao();
		for (int i = 0; i < olfato.length; i++) {
			if (i == 1) {
				if(LAST_ACTION != ACTION_DOWN)
					atribuirPesoOlfatoLadrao(POSITION_UP, olfato[i]);
			}else if (i == 3 || i == 0 || i == 5) {
				if(LAST_ACTION != ACTION_RIGHT)
					atribuirPesoOlfatoLadrao(POSITION_LEFT, olfato[i]);
			}else if (i == 4 || i == 2 || i == 7) {
				if(LAST_ACTION != ACTION_LEFT)
					atribuirPesoOlfatoLadrao(POSITION_RIGHT, olfato[i]);
			}else if (i == 6) {
				if(LAST_ACTION != ACTION_UP)
					atribuirPesoOlfatoLadrao(POSITION_DOWN, olfato[i]);
			}
				
		}
		
	}
	
	private void atribuirPesoOlfatoPoupador(int posicao, int distancia) {
		int peso = 0;
		if(distancia == 1) {
			peso = WEIGHT_SMELL_POP_ONE;
		}
		if(distancia == 2) {
			peso = WEIGHT_SMELL_POP_TWO;
		}
		if(distancia == 3) {
			peso = WEIGHT_SMELL_POP_THREE;
		} 
		if(distancia == 4) {
			peso = WEIGHT_SMELL_POP_FOUR;
		} 
		if(distancia == 5) {
			peso = WEIGHT_SMELL_POP_FIVE;
		} 
		
		if(posicao == POSITION_DOWN && WEIGHT_DOWN < WEIGHTMAX)
			WEIGHT_DOWN = WEIGHT_DOWN + peso;
		
		if(posicao == POSITION_UP && WEIGHT_UP < WEIGHTMAX)
			WEIGHT_UP = WEIGHT_UP + peso;
		
		if(posicao == POSITION_LEFT && WEIGHT_LEFT < WEIGHTMAX)
			WEIGHT_LEFT = WEIGHT_LEFT + peso;
		
		if(posicao == POSITION_RIGHT && WEIGHT_RIGHT < WEIGHTMAX)
			WEIGHT_RIGHT = WEIGHT_RIGHT + peso;
	}
	
	private void atribuirPesoOlfatoLadrao(int posicao, int distancia) {
		int peso = 0;
		if(distancia == 1) {
			peso = WEIGHT_SMELL_POP_ONE;
		}
		if(distancia == 2) {
			peso = WEIGHT_SMELL_POP_TWO;
		}
		if(distancia == 3) {
			peso = WEIGHT_SMELL_POP_THREE;
		} 
		if(distancia == 4) {
			peso = WEIGHT_SMELL_POP_FOUR;
		} 
		if(distancia == 5) {
			peso = WEIGHT_SMELL_POP_FIVE;
		}
		
		if(posicao == POSITION_DOWN && WEIGHT_DOWN < WEIGHTMAX)
			WEIGHT_DOWN = WEIGHT_DOWN + peso;
		
		if(posicao == POSITION_UP && WEIGHT_UP < WEIGHTMAX)
			WEIGHT_UP = WEIGHT_UP + peso;
		
		if(posicao == POSITION_LEFT && WEIGHT_LEFT < WEIGHTMAX)
			WEIGHT_LEFT = WEIGHT_LEFT + peso;
		
		if(posicao == POSITION_RIGHT && WEIGHT_RIGHT < WEIGHTMAX)
			WEIGHT_RIGHT = WEIGHT_RIGHT + peso;
	}
	
	private void analisarProximidadeRetaPoupador(int [] visao) {
		int posicao = visao[2];
		if (posicao >= 100 && posicao <= 199) {
				atribuirPeso(POSITION_UP, WEIGHTMAX-20);
				int ladrao = visao[7];
				if(ladrao >= 200 && ladrao <=299){
					atribuirPeso(POSITION_RIGHT, WEIGHTMAX-10);
					atribuirPeso(POSITION_LEFT, WEIGHTMAX-10);
				}
		}
		
		posicao = visao[21];
		if (posicao >= 100 && posicao <= 199) {
				atribuirPeso(POSITION_DOWN, WEIGHTMAX-20);
				int ladrao = visao[16];
				if(ladrao >= 200 && ladrao <=299){
					atribuirPeso(POSITION_RIGHT, WEIGHTMAX-10);
					atribuirPeso(POSITION_LEFT, WEIGHTMAX-10);
				}
		}
		
		posicao = visao[13];
		if (posicao >= 100 && posicao <= 199) {
				atribuirPeso(POSITION_RIGHT, WEIGHTMAX-20);
				int ladrao = visao[12];
				if(ladrao >= 200 && ladrao <=299){
					atribuirPeso(POSITION_UP, WEIGHTMAX-10);
					atribuirPeso(POSITION_DOWN, WEIGHTMAX-10);
				}
		}
		
		
		posicao = visao[10];
		if (posicao >= 100 && posicao <= 199) {
				atribuirPeso(POSITION_LEFT, WEIGHTMAX-20);
				int ladrao = visao[11];
				if(ladrao >= 200 && ladrao <=299){
					atribuirPeso(POSITION_UP, WEIGHTMAX-10);
					atribuirPeso(POSITION_DOWN, WEIGHTMAX-10);
				}
		}
		
	}
	
	private void analisarProximidadeMaximaPoupador(int [] visao){
		int valorPosicao = visao[POSITION_UP];
		if (valorPosicao >= 100 && valorPosicao <= 199) {
			atribuirPeso(POSITION_UP, WEIGHTMAX);
		}
		
		valorPosicao = visao[POSITION_DOWN];
		if (valorPosicao >= 100 && valorPosicao <= 199) {
				atribuirPeso(POSITION_DOWN, WEIGHTMAX);
		}
		
		valorPosicao = visao[POSITION_RIGHT];
		if (valorPosicao >= 100 && valorPosicao <= 199) {
				atribuirPeso(POSITION_RIGHT, WEIGHTMAX);
		}
		
		valorPosicao = visao[POSITION_LEFT];
		if (valorPosicao >= 100 && valorPosicao <= 199) {
			
				atribuirPeso(POSITION_LEFT, WEIGHTMAX);
			
		}
	}
	
	private void analisarProximidadeLongaPoupador(int [] visao){
		int valorPosicao1 = visao[0];
		int valorPosicao2 = visao[1];
		int valorPosicao3 =   visao[5];
		if ((valorPosicao1 >= 100 && valorPosicao1 <= 199) || (valorPosicao2 >= 100 && valorPosicao2 <= 199) || valorPosicao3 >= 100 && valorPosicao3 <= 199) {
			atribuirPeso(POSITION_UP, WEIGHT_LONGER);
			atribuirPeso(POSITION_LEFT, WEIGHT_LONGER);
			if(valorPosicao1 >= 100 && valorPosicao1 <= 199)
				atribuirPeso(POSITION_UP, WEIGHT_LONGER+5);
			if(valorPosicao3 >= 100 && valorPosicao3 <= 199)
				atribuirPeso(POSITION_LEFT, WEIGHT_LONGER+5);
		}
		
		valorPosicao1 = visao[3];
		valorPosicao2 = visao[4];
		valorPosicao3 = visao[9];
		if ((valorPosicao1 >= 100 && valorPosicao1 <= 199) || (valorPosicao2 >= 100 && valorPosicao2 <= 199) || (valorPosicao3 >= 100 && valorPosicao3 <= 199)) { 
			atribuirPeso(POSITION_RIGHT, WEIGHT_LONGER);
			atribuirPeso(POSITION_UP, WEIGHT_LONGER);
			if(valorPosicao1 >= 100 && valorPosicao1 <= 199)
				atribuirPeso(POSITION_UP, WEIGHT_LONGER+5);
			if(valorPosicao3 >= 100 && valorPosicao3 <= 199)
				atribuirPeso(POSITION_RIGHT, WEIGHT_LONGER+5);
		}
		
		valorPosicao1 = visao[22];
		valorPosicao2 = visao[23];
		valorPosicao3 = visao[18];
		if ((valorPosicao1 >= 100 && valorPosicao1 <= 199) || (valorPosicao2 >= 100 && valorPosicao2 <= 199) || (valorPosicao3 >= 100 && valorPosicao3 <= 199)) {
			atribuirPeso(POSITION_DOWN, WEIGHT_LONGER);
			atribuirPeso(POSITION_RIGHT, WEIGHT_LONGER);
			if(valorPosicao1 >= 100 && valorPosicao1 <= 199)
				atribuirPeso(POSITION_DOWN, WEIGHT_LONGER+5);
			if(valorPosicao3 >= 100 && valorPosicao3 <= 199)
				atribuirPeso(POSITION_RIGHT, WEIGHT_LONGER+5);
		}
		
		valorPosicao1 = visao[19];
		valorPosicao2 = visao[20];
		valorPosicao3 = visao[14];
		if ((valorPosicao1 >= 100 && valorPosicao1 <= 199) || (valorPosicao2 >= 100 && valorPosicao2 <= 199) || (valorPosicao2 >= 100 && valorPosicao2 <= 199)) {
			atribuirPeso(POSITION_LEFT, WEIGHT_LONGER);
			atribuirPeso(POSITION_UP, WEIGHT_LONGER);
			if(valorPosicao2 >= 100 && valorPosicao2 <= 199)
				atribuirPeso(POSITION_DOWN, WEIGHT_LONGER+5);
			if(valorPosicao3 >= 100 && valorPosicao3 <= 199)
				atribuirPeso(POSITION_LEFT, WEIGHT_LONGER+5);
		}
		
	}
	
	private void analisarProximidadeMediaPoupador(int [] visao){
		int valorPosicao = visao[6];
		if (valorPosicao >= 100 && valorPosicao <= 199) {
				atribuirPeso(POSITION_UP, WEIGHT_CLOSE);
				atribuirPeso(POSITION_LEFT, WEIGHT_CLOSE);
		}
		valorPosicao = visao[8];
		if (valorPosicao >= 100 && valorPosicao <= 199) {
				atribuirPeso(POSITION_RIGHT, WEIGHT_CLOSE);
				atribuirPeso(POSITION_UP, WEIGHT_CLOSE);
		}
		valorPosicao = visao[17];
		if (valorPosicao >= 100 && valorPosicao <= 199) {

				atribuirPeso(POSITION_DOWN, WEIGHT_CLOSE);
				atribuirPeso(POSITION_RIGHT, WEIGHT_CLOSE);
		}
		
		valorPosicao = visao[15];
		if (valorPosicao >= 100 && valorPosicao <= 199) {
				atribuirPeso(POSITION_LEFT, WEIGHT_CLOSE);
				atribuirPeso(POSITION_DOWN, WEIGHT_CLOSE);
		}
		
	}
	
	private void eliminarMovimentosNegativosObvios(int [] visao) {
		int posicao = visao[POSITION_UP];
		
		if(posicao == VISION_WALL || posicao == VISION_COIN || posicao == VISION_POWER || posicao == VISION_WITHOUT 
				|| posicao == VISION_BANK || posicao == VISION_WITHOUT_LOCAL || (posicao >= 200 && posicao <= 299 ))
			atribuirPeso(POSITION_UP, WEIGHTMIN);
		else
			VALID_ACTION.add(ACTION_UP);
		
		posicao = visao[POSITION_DOWN];
		if(posicao == VISION_WALL || posicao == VISION_COIN || posicao == VISION_POWER || posicao == VISION_WITHOUT 
				|| posicao == VISION_BANK || posicao == VISION_WITHOUT_LOCAL || (posicao >= 200 && posicao <= 299 ))
			atribuirPeso(POSITION_DOWN, WEIGHTMIN);
		else
			VALID_ACTION.add(ACTION_DOWN);
		
		posicao = visao[POSITION_RIGHT];
		if(posicao == VISION_WALL || posicao == VISION_COIN || posicao == VISION_POWER || posicao == VISION_WITHOUT 
				|| posicao == VISION_BANK || posicao == VISION_WITHOUT_LOCAL || (posicao >= 200 && posicao <= 299 ))
			atribuirPeso(POSITION_RIGHT, WEIGHTMIN);
		else
			VALID_ACTION.add(ACTION_RIGHT);
		
		posicao = visao[POSITION_LEFT];
		if(posicao == VISION_WALL || posicao == VISION_COIN || posicao == VISION_POWER || posicao == VISION_WITHOUT 
				|| posicao == VISION_BANK || posicao == VISION_WITHOUT_LOCAL || (posicao >= 200 && posicao <= 299 ))
			atribuirPeso(POSITION_LEFT, WEIGHTMIN);
		else
			VALID_ACTION.add(ACTION_LEFT);
		
	}
	
	private void atribuirPeso(int posicao, int valor) {
		if(valor > 0) {
			
			if(posicao == POSITION_DOWN && WEIGHT_DOWN < WEIGHTMAX)
				WEIGHT_DOWN = WEIGHT_DOWN + valor;
			
			if(posicao == POSITION_UP && WEIGHT_UP < WEIGHTMAX)
				WEIGHT_UP = WEIGHT_UP + valor;
			
			if(posicao == POSITION_LEFT && WEIGHT_LEFT < WEIGHTMAX)
				WEIGHT_LEFT = WEIGHT_LEFT + valor;
			
			if(posicao == POSITION_RIGHT && WEIGHT_RIGHT < WEIGHTMAX)
				WEIGHT_RIGHT = WEIGHT_RIGHT + valor;
			
		} else if (valor < 0) {
			
			if(posicao == POSITION_DOWN && WEIGHT_DOWN > WEIGHTMIN)
				WEIGHT_DOWN = WEIGHT_DOWN + valor;
			
			if(posicao == POSITION_UP && WEIGHT_UP > WEIGHTMIN)
				WEIGHT_UP = WEIGHT_UP + valor;
			
			if(posicao == POSITION_LEFT && WEIGHT_LEFT > WEIGHTMIN)
				WEIGHT_LEFT = WEIGHT_LEFT + valor;
			
			if(posicao == POSITION_RIGHT && WEIGHT_RIGHT > WEIGHTMIN)
				WEIGHT_RIGHT = WEIGHT_RIGHT + valor;
			
		}
	}
	
	private int agir(){
		
		if(WEIGHT_DOWN == WEIGHTMIN && WEIGHT_UP == WEIGHTMIN && WEIGHT_RIGHT == WEIGHTMIN && WEIGHT_LEFT == WEIGHTMIN)
			return 0;
		
		int maior_peso = -999999999;
		if (WEIGHT_DOWN > maior_peso) {
			maior_peso = WEIGHT_DOWN;
		}
			
		if (WEIGHT_UP > maior_peso) {
			maior_peso = WEIGHT_UP;
		}
		
		if (WEIGHT_RIGHT > maior_peso) {
			maior_peso = WEIGHT_RIGHT;
		}
		
		if (WEIGHT_LEFT > maior_peso) {
			maior_peso = WEIGHT_LEFT;
		} 

		List<Integer> results = new ArrayList<Integer>();
		//random iguais
		if (WEIGHT_DOWN == maior_peso) {
			results.add(ACTION_DOWN);
		}
		if (WEIGHT_UP == maior_peso) {
			results.add(ACTION_UP);
		}
		if (WEIGHT_RIGHT == maior_peso) {
			results.add(ACTION_RIGHT);
		}
		if (WEIGHT_LEFT == maior_peso) {
			results.add(ACTION_LEFT);
		}
		
		
		int resultado = (int)(Math.random() * results.size());
		int acaoDef = results.get(resultado);
		
		atualizarHAST_POSITION(acaoDef);
		LAST_ACTION = acaoDef;
		
		if(STOPED && LAST_STOPED == 0)
			LAST_STOPED = acaoDef;
		return acaoDef;
	}
	
	private void atualizarHAST_POSITION(int acao) {
		int x = sensor.getPosicao().x;
		int y = sensor.getPosicao().y;
		if(acao == ACTION_DOWN) {
			y = y + 1;
		} else if(acao == ACTION_UP) {
			y = y - 1;
		} else if(acao == ACTION_RIGHT) {
			x = x + 1;
		} else if(acao == ACTION_LEFT) {
			x = x - 1;
		}
		
		String posicaoHash = x+","+y;
		MinhaPosicao posicao = HAST_POSITION.get(posicaoHash);
		if (posicao == null) {
			MinhaPosicao minhaPosicao = new MinhaPosicao(x,y,1);
			HAST_POSITION.put(minhaPosicao.toString(), minhaPosicao);
			LIST_POSITION.add(minhaPosicao);
		} else {
			posicao.incrementarPassagem();
			HAST_POSITION.put(posicao.toString(), posicao);
		}
		
	}
	
	private void analisarLocalMenosVisitado(){
		int meuX = sensor.getPosicao().x;
		int meuY = sensor.getPosicao().y;
		
		MinhaPosicao minhaPosicaoEsq = HAST_POSITION.get((meuX-1)+","+meuY);
		if (minhaPosicaoEsq == null) {
			minhaPosicaoEsq = new MinhaPosicao((meuX-1), meuY, 0);
			HAST_POSITION.put(minhaPosicaoEsq.toString(), minhaPosicaoEsq);
			LIST_POSITION.add(minhaPosicaoEsq);
		}
		meuX = sensor.getPosicao().x;
		meuY = sensor.getPosicao().y;
		MinhaPosicao minhaPosicaoDir = HAST_POSITION.get((meuX+1)+","+meuY);
		if (minhaPosicaoDir == null) {
			minhaPosicaoDir = new MinhaPosicao((meuX+1), meuY, 0);
			HAST_POSITION.put(minhaPosicaoDir.toString(), minhaPosicaoDir);
			LIST_POSITION.add(minhaPosicaoDir);
		}
		meuX = sensor.getPosicao().x;
		meuY = sensor.getPosicao().y;
		MinhaPosicao minhaPosicaoCima = HAST_POSITION.get(meuX+","+(meuY-1));
		if (minhaPosicaoCima == null) {
			minhaPosicaoCima = new MinhaPosicao(meuX, (meuY-1), 0);
			HAST_POSITION.put(minhaPosicaoCima.toString(), minhaPosicaoCima);
			LIST_POSITION.add(minhaPosicaoCima);
		}
		meuX = sensor.getPosicao().x;
		meuY = sensor.getPosicao().y;
		MinhaPosicao minhaPosicaoBaixo = HAST_POSITION.get(meuX+","+(meuY+1));
		if (minhaPosicaoBaixo == null) {
			minhaPosicaoBaixo = new MinhaPosicao(meuX, (meuY+1), 0);
			HAST_POSITION.put(minhaPosicaoBaixo.toString(), minhaPosicaoBaixo);
			LIST_POSITION.add(minhaPosicaoBaixo);
		}
		
		HashMap<String, Integer> hashOrganizar = new HashMap<String, Integer>();
		hashOrganizar.put(minhaPosicaoBaixo.toString(), ACTION_DOWN);
		hashOrganizar.put(minhaPosicaoCima.toString(), ACTION_UP);
		hashOrganizar.put(minhaPosicaoDir.toString(), ACTION_RIGHT);
		hashOrganizar.put(minhaPosicaoEsq.toString(), ACTION_LEFT);
		
		List<MinhaPosicao> naoOrdenado = new ArrayList<MinhaPosicao>();
		for (int i = 0; i < VALID_ACTION.size(); i++) {
			if(hashOrganizar.get(minhaPosicaoBaixo.toString()) == VALID_ACTION.get(i))
				naoOrdenado.add(minhaPosicaoBaixo);
		}
		for (int i = 0; i < VALID_ACTION.size(); i++) {
			if(hashOrganizar.get(minhaPosicaoCima.toString()) == VALID_ACTION.get(i))
				naoOrdenado.add(minhaPosicaoCima);
		}
		for (int i = 0; i < VALID_ACTION.size(); i++) {
			if(hashOrganizar.get(minhaPosicaoDir.toString()) == VALID_ACTION.get(i))
				naoOrdenado.add(minhaPosicaoDir);
		}
		for (int i = 0; i < VALID_ACTION.size(); i++) {
			if(hashOrganizar.get(minhaPosicaoEsq.toString()) == VALID_ACTION.get(i))
				naoOrdenado.add(minhaPosicaoEsq);
		}
		
		MinhaPosicao menor = pegarMenor(naoOrdenado);
		if(menor != null) {
			if(hashOrganizar.get(menor.toString()) == ACTION_DOWN)
				atribuirPeso(POSITION_DOWN, WEIGHT_HAVE_WALK);
			if(hashOrganizar.get(menor.toString()) == ACTION_UP)
				atribuirPeso(POSITION_UP, WEIGHT_HAVE_WALK);
			if(hashOrganizar.get(menor.toString()) == ACTION_RIGHT)
				atribuirPeso(POSITION_RIGHT, WEIGHT_HAVE_WALK);
			if(hashOrganizar.get(menor.toString()) == ACTION_LEFT)
				atribuirPeso(POSITION_LEFT, WEIGHT_HAVE_WALK);
		}
		
		for (int i = 0; i < naoOrdenado.size(); i++) {
			if(menor.getQntPassei() == naoOrdenado.get(i).getQntPassei() && !menor.toString().equals(naoOrdenado.get(i).toString())){
				if(hashOrganizar.get(naoOrdenado.get(i).toString()) == ACTION_DOWN)
					atribuirPeso(POSITION_DOWN, WEIGHT_HAVE_WALK);
				if(hashOrganizar.get(naoOrdenado.get(i).toString()) == ACTION_UP)
					atribuirPeso(POSITION_UP, WEIGHT_HAVE_WALK);
				if(hashOrganizar.get(naoOrdenado.get(i).toString()) == ACTION_RIGHT)
					atribuirPeso(POSITION_RIGHT, WEIGHT_HAVE_WALK);
				if(hashOrganizar.get(naoOrdenado.get(i).toString()) == ACTION_LEFT)
					atribuirPeso(POSITION_LEFT, WEIGHT_HAVE_WALK);
			}
		}
		
	}

	private MinhaPosicao pegarMenor(List<MinhaPosicao> naoOrdenado){
		if(naoOrdenado.size() > 0) {
			MinhaPosicao menor = naoOrdenado.get(0);
			for (MinhaPosicao minhaPosicao : naoOrdenado) {
				if(minhaPosicao.getQntPassei() < menor.getQntPassei())
					menor = minhaPosicao;
			}
			return menor;
		}
		
		return null;
	}
	
	class MinhaPosicao{
		
		private int x;
		private int y;
		private int qntPassei;
		
		public MinhaPosicao(int x, int y, int qntPassei){
			this.x = x;
			this.y = y;
			this.qntPassei = qntPassei;
		}
		
		public int getQntPassei() {
			return qntPassei;
		}
		public void incrementarPassagem() {
			qntPassei++;
		}
		@Override
		public String toString() {
			return x+","+y;
		}
	}

}