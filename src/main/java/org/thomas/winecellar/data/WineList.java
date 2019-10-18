package org.thomas.winecellar.data;

import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.NotNull;

public class WineList extends NamedEntity {

	@NotNull
	private Map<Wine, Integer> wines = new HashMap<>();

	public Map<Wine, Integer> getWines() {
		return wines;
	}

	public void setWines(Map<Wine, Integer> wines) {
		this.wines = wines;
	}

	public void put(Wine wine, int amount) {
		wines.put(wine, amount);
	}

	public void put(Wine wine) {
		put(wine, 1);
	}

	public boolean has(Wine w) {

		final Integer integer = wines.get(w);
		return integer != null && integer != 0;
	}

	public int get(Wine w) {

		final Integer integer = wines.get(w);
		return integer == null ? 0 : integer;
	}
}
