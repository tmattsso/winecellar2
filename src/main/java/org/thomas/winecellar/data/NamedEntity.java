package org.thomas.winecellar.data;

import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotNull;

@MappedSuperclass
public abstract class NamedEntity extends AbstractEntity implements Comparable<NamedEntity> {

	@NotNull
	private String name;

	public NamedEntity() {

	}

	public NamedEntity(String name) {
		setName(name);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public int compareTo(NamedEntity o) {

		if (name == null && o.name != null) {
			return 1;
		}
		if (name != null && o.name == null) {
			return -1;
		}
		return name.compareTo(o.name);

	}

	@Override
	public String toString() {
		return String.format("%s %d (%s)", this.getClass().getSimpleName(), getId(), getName());
	}
}
