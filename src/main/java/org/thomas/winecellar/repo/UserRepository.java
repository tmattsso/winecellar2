package org.thomas.winecellar.repo;

import org.springframework.data.repository.CrudRepository;
import org.thomas.winecellar.data.User;

public interface UserRepository extends CrudRepository<User, Long> {

	public User getByUuid(String uuid);

	public User getByEmail(String email);
}
