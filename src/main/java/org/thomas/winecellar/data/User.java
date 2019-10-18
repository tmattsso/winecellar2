package org.thomas.winecellar.data;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

public class User extends NamedEntity {

	@NotNull
	private WineList cellarList;

	@NotNull
	private List<WineList> wishlists = new ArrayList<>();

	public WineList getCellarList() {
		return cellarList;
	}

	public void setCellarList(WineList cellarList) {
		this.cellarList = cellarList;
	}

	public List<WineList> getWishlists() {
		return wishlists;
	}

	public void setWishlists(List<WineList> wishlists) {
		this.wishlists = wishlists;
	}

	public boolean inCellarList(Wine w) {
		return cellarList.has(w);
	}

	public WineList inWishList(Wine w) {
		return wishlists.stream().filter(l -> l.has(w)).findFirst().orElse(null);
	}

	public void addWishList(WineList list) {
		if (!wishlists.contains(list)) {
			wishlists.add(list);
		}
	}

	public void removeWishList(WineList list) {
		wishlists.remove(list);
	}

	public WineList inWishList(Long wineId) {

		for (final WineList l : wishlists) {
			for (final Wine wine : l.getWines().keySet()) {
				if (wine.getId() == wineId) {
					return l;
				}
			}
		}
		return null;
	}

	public WineList getWishList(Long listId) {
		return wishlists.stream().filter(l -> l.getId() == listId).findAny().orElse(null);
	}
}
