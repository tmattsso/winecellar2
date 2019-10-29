package org.thomas.winecellar.data;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class AbstractEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Override
	public boolean equals(Object obj) {

		if (obj == null || id == null) {
			return false;
		}

		if (obj instanceof AbstractEntity) {
			if (this.getClass().equals(obj.getClass()) && id.equals(((AbstractEntity) obj).id)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		if (id == null) {
			return super.hashCode();
		} else {
			return id.intValue();
		}
	}

	@Override
	public String toString() {
		return String.format("%s %d", this.getClass().getSimpleName(), id);
	}
}
