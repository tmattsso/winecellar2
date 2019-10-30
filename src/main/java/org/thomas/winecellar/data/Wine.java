package org.thomas.winecellar.data;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;
import javax.persistence.OrderColumn;
import javax.validation.constraints.NotNull;

@Entity
public class Wine extends NamedEntity {

	@NotNull
	@OneToOne
	private Producer producer;

	@NotNull
	@Enumerated(EnumType.STRING)
	private WineType type;

	private String alko_id;

	private String country;
	private String region;
	private String subregion;

	@ElementCollection(fetch = FetchType.EAGER)
	@OrderColumn(name = "listorder")
	private List<String> grapes = new ArrayList<String>();

	public Wine(String name, Producer prod, WineType type) {
		super(name);
		producer = prod;
		this.type = type;
	}

	public Wine() {
	}

	@Override
	public int compareTo(NamedEntity o) {

		final int nameComp = super.compareTo(o);

		if (nameComp == 0 && producer != null) {
			if (o instanceof Wine && ((Wine) o).producer != null) {
				return producer.compareTo(((Wine) o).producer);
			}
		}

		return nameComp;
	}

	public Producer getProducer() {
		return producer;
	}

	public void setProducer(Producer producer) {
		this.producer = producer;
	}

	public WineType getType() {
		return type;
	}

	public void setType(WineType type) {
		this.type = type;
	}

	public String getAlko_id() {
		return alko_id;
	}

	public void setAlko_id(String alko_id) {
		this.alko_id = alko_id;
	}

	public String getSubregion() {
		return subregion;
	}

	public void setSubregion(String subregion) {
		this.subregion = subregion;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public List<String> getGrapes() {
		return grapes;
	}

	public void setGrapes(List<String> grapes) {
		this.grapes = grapes;
	}

	public boolean hasGrape(String searchTerm) {
		for (final String g : grapes) {
			if (g.toLowerCase().equals(searchTerm.toLowerCase())) {
				return true;
			}
		}
		return false;
	}
}
