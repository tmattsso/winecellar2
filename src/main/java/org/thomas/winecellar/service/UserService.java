package org.thomas.winecellar.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.ApplicationScope;
import org.springframework.web.context.annotation.SessionScope;
import org.thomas.winecellar.data.User;
import org.thomas.winecellar.data.WineList;

@Service
@ApplicationScope
public class UserService {

	private final List<User> users = new ArrayList<>();

	public void addUser(User user) {
		users.add(user);
	}

	@Bean
	@SessionScope
	public User getCurrentUser() {
		return users.get(0);
	}

	public void removeList(WineList list) {
		getCurrentUser().getWishlists().remove(list);
	}

	public WineList createList(String name) {

		if (nameInvalid(name)) {
			return null;
		}

		final WineList list = new WineList();
		list.setName(name);
		list.setId(new Random().nextLong());

		getCurrentUser().addWishList(list);

		return list;
	}

	public WineList renameList(WineList list, String name) {

		if (nameInvalid(name)) {
			return null;
		}

		list.setName(name);
		return list;
	}

	private boolean nameInvalid(String name) {
		if (name == null || name.isEmpty()) {
			return true;
		}

		final List<String> names = getCurrentUser().getWishlists().stream().map(l -> l.getName())
				.collect(Collectors.toList());
		if (names.contains(name)) {
			return true;
		}
		return false;
	}

}
