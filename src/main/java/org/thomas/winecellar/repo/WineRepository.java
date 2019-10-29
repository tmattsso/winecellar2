package org.thomas.winecellar.repo;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.thomas.winecellar.data.Producer;
import org.thomas.winecellar.data.Wine;

public interface WineRepository extends CrudRepository<Wine, Long> {

	public List<Wine> findByProducer(Producer p);
}
