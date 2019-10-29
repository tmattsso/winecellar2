package org.thomas.winecellar.repo;

import org.springframework.data.repository.CrudRepository;
import org.thomas.winecellar.data.Producer;

public interface ProducerRepo extends CrudRepository<Producer, Long> {

}
