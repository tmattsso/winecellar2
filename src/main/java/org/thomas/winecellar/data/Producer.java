package org.thomas.winecellar.data;

import javax.persistence.Entity;

@Entity
public class Producer extends NamedEntity {

	public Producer(String name) {
		super(name);
	}

	public Producer() {

	}

}
