package org.thomas.winecellar.repo;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.thomas.winecellar.data.Wine;
import org.thomas.winecellar.data.WineRating;

public interface WineRatingRepo extends CrudRepository<WineRating, Long> {

	public List<WineRating> findByWine(Wine wine);
}
