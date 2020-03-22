package algoritmo.enums;

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
