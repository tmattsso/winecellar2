package org.thomas.winecellar.service;

import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;
import org.thomas.winecellar.data.User;
import org.thomas.winecellar.data.WineList;
import org.thomas.winecellar.repo.UserRepository;
import org.thomas.winecellar.repo.WineListRepo;

@Service
public class UserService {

	@Autowired
	private UserRepository repo;

	@Autowired
	private WineListRepo listRepo;

	public User addUser(User user) {

		if (user.getCellarList() == null) {
			final WineList cellarList = new WineList();
			cellarList.setName("Cellar List");
			user.setCellarList(cellarList);
		}

		user = repo.save(user);
		System.out.println("Created user " + user);
		return user;
	}

	@Bean
	@SessionScope
	public User getCurrentUser() {
		final Iterator<User> iterator = repo.findAll().iterator();
		if (!iterator.hasNext()) {
			return null;
		}
		return iterator.next();
	}

	public void removeList(WineList list) {
		getCurrentUser().getWishlists().remove(list);
	}

	public WineList createList(String name) {

		if (listNameInvalid(name)) {
			return null;
		}

		final WineList list = new WineList();
		list.setName(name);
		list.setId(new Random().nextLong());

		getCurrentUser().addWishList(list);

		return list;
	}

	public WineList renameList(WineList list, String name) {

		if (listNameInvalid(name)) {
			return null;
		}

		list.setName(name);
		return list;
	}

	private boolean listNameInvalid(String name) {
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

	public void addWishList(WineList cellar) {

		cellar = listRepo.save(cellar);

		final User user = getCurrentUser();
		user.addWishList(cellar);
		repo.save(user);
	}

}
