package org.thomas.winecellar.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;
import org.thomas.winecellar.data.User;
import org.thomas.winecellar.repo.UserRepository;

@SessionScope
@Component
public class CurrentUserProvider {

	@Autowired
	private UserRepository repo;

	private Long currentUserID;

	public User get() {
		if (currentUserID != null) {
			return repo.findById(currentUserID).orElse(null);
		}
		return null;
	}

	public User loginByEmail(String email) {
		final User user = repo.getByEmail(email);
		if (user != null) {
			currentUserID = user.getId();
		}

		return user;
	}

	public User loginByUuid(String uuid) {
		final User user = repo.getByUuid(uuid);
		if (user != null) {
			currentUserID = user.getId();
		}

		return user;
	}

}
