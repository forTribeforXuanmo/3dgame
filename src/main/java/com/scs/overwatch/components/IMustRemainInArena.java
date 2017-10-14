package com.scs.overwatch.components;

import com.jme3.math.Vector3f;

public interface IMustRemainInArena {

	Vector3f getLocation();
	
	void remove();
	
	void respawn();
}
