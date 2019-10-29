package org.thomas.winecellar.repo;

import org.springframework.data.repository.CrudRepository;
import org.thomas.winecellar.data.WineList;

public interface WineListRepo extends CrudRepository<WineList, Long> {

}
