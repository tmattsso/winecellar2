package org.thomas.winecellar.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thomas.winecellar.data.User;
import org.thomas.winecellar.data.WineList;
import org.thomas.winecellar.repo.UserRepository;

@Service
public class UserService {

	@Autowired
	private UserRepository repo;

	public User addUser(String name, String email) {

		User user = new User();

		if (user.getCellarList() == null) {
			final WineList cellarList = new WineList();
			cellarList.setName("Cellar List");
			user.setCellarList(cellarList);
		}

		final String uuid = UUID.randomUUID().toString();
		user.setUuid(uuid);

		user.setName(name);
		user.setEmail(email);

		user = repo.save(user);
		System.out.println("Created user " + user);
		return user;
	}

	public boolean userExists(String email) {
		return repo.getByEmail(email) != null;
	}

	public void update(User user) {
		repo.save(user);
	}

	public void removeList(User user, WineList list) {
		user.getWishlists().remove(list);
	}

	public WineList createList(User user, String name) {

		if (listNameInvalid(user, name)) {
			return null;
		}

		WineList list = new WineList();
		list.setName(name);

		user = repo.findById(user.getId()).get();
		user.addWishList(list);
		user = repo.save(user);

		final List<WineList> wishlists = user.getWishlists();
		list = wishlists.get(wishlists.size() - 1);

		return list;
	}

	public WineList renameList(User user, WineList list, String name) {

		if (listNameInvalid(user, name)) {
			return null;
		}

		user = repo.findById(user.getId()).get();
		list = user.getWishList(list.getId());
		list.setName(name);
		user.addWishList(list);
		repo.save(user);

		return list;
	}

	private boolean listNameInvalid(User user, String name) {
		if (name == null || name.isEmpty()) {
			return true;
		}

		final List<String> names = user.getWishlists().stream().map(l -> l.getName()).collect(Collectors.toList());
		if (names.contains(name)) {
			return true;
		}
		return false;
	}

	public User addWishList(User user, WineList cellar) {

		user = repo.findById(user.getId()).get();
		user.addWishList(cellar);
		repo.save(user);

		return user;
	}

}
