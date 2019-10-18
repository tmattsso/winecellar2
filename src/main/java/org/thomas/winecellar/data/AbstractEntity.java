package org.thomas.winecellar.data;

public abstract class AbstractEntity {

	private long id = -1;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	@Override
	public boolean equals(Object obj) {

		if (obj == null || id == -1) {
			return false;
		}

		if (obj instanceof AbstractEntity) {
			if (this.getClass().equals(obj.getClass()) && id == ((AbstractEntity) obj).id) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		if (id == -1) {
			return super.hashCode();
		} else {
			return (int) id;
		}
	}

	@Override
	public String toString() {
		return String.format("%s %d", this.getClass().getSimpleName(), id);
	}
}
