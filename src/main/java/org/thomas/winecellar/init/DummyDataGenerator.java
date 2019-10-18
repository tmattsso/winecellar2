package org.thomas.winecellar.init;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.thomas.winecellar.data.Producer;
import org.thomas.winecellar.data.User;
import org.thomas.winecellar.data.Wine;
import org.thomas.winecellar.data.WineList;
import org.thomas.winecellar.data.WineType;
import org.thomas.winecellar.service.UserService;
import org.thomas.winecellar.service.WineService;

@Component
public class DummyDataGenerator {

	private final Logger LOG = LoggerFactory.getLogger(DummyDataGenerator.class);

	@Autowired
	WineService service;

	@Autowired
	UserService users;

	long id = 0;

	@EventListener
	public void generate(ContextStartedEvent ctxStartEvt) {

		LOG.info("Creating dummy data...");

		final WineList cellar = new WineList();
		cellar.setName("Cellar List");
		cellar.setId(++id);
		final WineList wish = new WineList();
		wish.setName("Wish List");
		wish.setId(++id);

		final User u = new User();
		u.setName("Thomas");
		u.setCellarList(cellar);
		u.addWishList(wish);
		users.addUser(u);

		createLotsaLists(u, true);

		final Producer cs = createProducer("Charles Smith Wines");
		createWine("Substance CS", cs, WineType.RED);
		cellar.put(createWine("Stoneridge CS", cs, WineType.RED), 3);
		createWine("Kung Fu Girl", cs, WineType.WHITE);

		final Producer ch = createProducer("Charles Heidsick");
		createWine("Vintage 2001", ch, WineType.BUBBLY);
		wish.put(createWine("Rosé Vintage 1999", ch, WineType.BUBBLY));
		createWine("Cuvee NV", ch, WineType.BUBBLY);

		final Producer sl = createProducer("Stags Leap");
		cellar.put(createWine("The Investor", sl, WineType.RED));
		wish.put(createWine("Artemis", sl, WineType.RED));

		LOG.info("Dummy data done.");
	}

	private void createLotsaLists(User u, boolean b) {
		if (!b) {
			return;
		}

		for (int i = 0; i < 30; i++) {
			final WineList cellar = new WineList();
			cellar.setName("Wish list " + i);
			cellar.setId(++id);

			u.addWishList(cellar);
		}
	}

	private Wine createWine(String name, final Producer cs, WineType type) {
		final Wine w = new Wine(name, cs, type);
		w.setId(++id);
		service.addWine(w);
		return w;
	}

	private Producer createProducer(String name) {
		final Producer prod = new Producer(name);
		prod.setId(++id);
		return prod;
	}

}
