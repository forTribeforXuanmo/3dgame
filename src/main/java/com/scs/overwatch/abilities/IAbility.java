package com.scs.overwatch.abilities;

public interface IAbility {

	/**
	 * Called every interval.  Returns whether the HUD needs updating
	 */
	boolean process(float interpol);
	
	/**
	 * Called when activated
	 */
	boolean activate(float interpol);
	
	String getHudText();
}
