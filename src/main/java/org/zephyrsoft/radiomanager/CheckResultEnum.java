package org.zephyrsoft.radiomanager;

/**
 * Signals the result of a check for an available broadcast.
 * 
 * @author Mathis Dirksen-Thedens
 */
public enum CheckResultEnum {
	BROADCAST_FOUND(0), NO_BROADCAST_FOUND(1), ERROR(-1);
	
	private final int intValue;
	
	private CheckResultEnum(int i) {
		intValue = i;
	}
	
	public int getIntValue() {
		return intValue;
	}
}
