package com.PsichiX.JustIDS;

import java.io.Serializable;

public class PlayerInfo implements Serializable {
	public String id;
	public double attackStrength;
	public double lifePoints;
	
	@Override
	public String toString() {
		return "ID:" + id + ", Attack: " + attackStrength + ", Life Points: " + lifePoints;
	}
}
