package org.thomas.winecellar.data;

public enum WineType {

	WHITE, RED, BUBBLY, DESSERT;

	@Override
	public String toString() {
		return name().charAt(0) + "" + name().toLowerCase().substring(1);
	}
}
