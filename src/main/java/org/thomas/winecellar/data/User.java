package org.thomas.winecellar.data;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "winedrinkers")
public class User extends NamedEntity {

	@NotNull
	@OneToOne(cascade = CascadeType.ALL)
	private WineList cellarList;

	@NotNull
	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@OrderColumn(name = "listorder")
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
		return getWishlists().stream().filter(l -> l.has(w)).findFirst().orElse(null);
	}

	public void addWishList(WineList list) {
		if (!getWishlists().contains(list)) {
			getWishlists().add(list);
		}
	}

	public void removeWishList(WineList list) {
		getWishlists().remove(list);
	}

	public WineList inWishList(Long wineId) {

		for (final WineList l : getWishlists()) {
			for (final Wine wine : l.getWines().keySet()) {
				if (wine.getId() == wineId) {
					return l;
				}
			}
		}
		return null;
	}

	public WineList getWishList(Long listId) {
		return getWishlists().stream().filter(l -> l.getId() == listId).findAny().orElse(null);
	}
}
