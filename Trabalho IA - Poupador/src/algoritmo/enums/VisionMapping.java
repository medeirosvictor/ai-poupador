package algoritmo.enums;

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
