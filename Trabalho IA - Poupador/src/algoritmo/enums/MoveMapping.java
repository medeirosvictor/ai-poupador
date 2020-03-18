package algoritmo.enums;

public enum MoveMapping {
    LEFT(4),
    RIGHT(3),
    UP(1),
    DOWN (2);

    public final int value ;
    MoveMapping(int value) {
        this.value = value;
    }

    public int getValue() {
    	return value;
    }
}
