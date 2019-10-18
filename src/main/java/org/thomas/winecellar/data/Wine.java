package org.thomas.winecellar.data;

import javax.validation.constraints.NotNull;

public class Wine extends NamedEntity {

	@NotNull
	private Producer producer;

	@NotNull
	private WineType type;

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
}
