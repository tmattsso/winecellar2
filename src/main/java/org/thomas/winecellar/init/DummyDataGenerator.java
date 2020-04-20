package org.thomas.winecellar.init;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
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
import org.thomas.winecellar.repo.ProducerRepo;
import org.thomas.winecellar.repo.UserRepository;
import org.thomas.winecellar.service.UserService;
import org.thomas.winecellar.service.WineService;

@Component
public class DummyDataGenerator {

	private final Logger LOG = LoggerFactory.getLogger(DummyDataGenerator.class);

	@Autowired
	WineService service;

	@Autowired
	UserService users;

	@Autowired
	UserRepository userRepo;

	@Autowired
	ProducerRepo producerRepo;

	Map<String, Producer> producers = new HashMap<String, Producer>();

	final int COL_ID = 0;
	final int COL_NAME = 1;
	final int COL_PROD = 2;
	final int COL_SIZE = 3;
	final int COL_PRICE = 4;
	final int COL_TYPE = 8;
	final int COL_COUNTRY = 11;
	final int COL_REGION = 12;
	final int COL_YEAR = 13;
	final int COL_REGION2 = 14;
	final int COL_GRAPES = 16;
	final int COL_NOTES = 17;

	private User demoUser;

	@EventListener
	public void generate(ContextStartedEvent ctxStartEvt) {

		final Iterator<User> currentUser = userRepo.findAll().iterator();
		if (currentUser.hasNext()) {
			return;
		}

		LOG.info("Creating dummy data...");

		Wine special = null;
		try {
			special = initAlkoData();
		} catch (final IOException e) {
			e.printStackTrace();
		}

		demoUser = users.addUser("Thomas", "thomas.mattsson99@gmail.com");

		final User u2 = users.addUser("User 2", "user2@gmail.com");

		createLotsaLists(true);

		if (special != null) {

			service.addRating(demoUser, special, 5, 2010, "It's fine");
			service.addRating(u2, special, 2, 2008,
					"It's crap. How crap? Very crap. Indeed, a crappier wine I've yet to taste. It's crappiness could be described by the vastness of Sahara, or the population count on the Earth. But yeah, I'd buy again.");
			service.addRating(u2, special, 2, 2008,
					"It's crap. How crap? Very crap. Indeed, a crappier wine I've yet to taste. It's crappiness could be described by the vastness of Sahara, or the population count on the Earth. But yeah, I'd buy again.");
			service.addRating(u2, special, 2, 2008,
					"It's crap. How crap? Very crap. Indeed, a crappier wine I've yet to taste. It's crappiness could be described by the vastness of Sahara, or the population count on the Earth. But yeah, I'd buy again.");
			service.addRating(u2, special, 2, 2008,
					"It's crap. How crap? Very crap. Indeed, a crappier wine I've yet to taste. It's crappiness could be described by the vastness of Sahara, or the population count on the Earth. But yeah, I'd buy again.");
			service.addRating(u2, special, 2, 2008,
					"It's crap. How crap? Very crap. Indeed, a crappier wine I've yet to taste. It's crappiness could be described by the vastness of Sahara, or the population count on the Earth. But yeah, I'd buy again.");
			service.addRating(u2, special, 2, 2008,
					"It's crap. How crap? Very crap. Indeed, a crappier wine I've yet to taste. It's crappiness could be described by the vastness of Sahara, or the population count on the Earth. But yeah, I'd buy again.");
			service.addRating(u2, special, 2, 2008,
					"It's crap. How crap? Very crap. Indeed, a crappier wine I've yet to taste. It's crappiness could be described by the vastness of Sahara, or the population count on the Earth. But yeah, I'd buy again.");

			service.modifyListAmount(demoUser.getCellarList(), special, 3);
		}

		LOG.info("Dummy data done.");
	}

	private void createLotsaLists(boolean b) {
		if (!b) {
			return;
		}

		for (int i = 1; i < 15; i++) {
			final WineList cellar = new WineList();
			cellar.setName("Wish list " + i);

			demoUser = users.addWishList(demoUser, cellar);
		}
	}

	private Wine initAlkoData() throws IOException {

		File file = new File("/Users/thomas/Work/Personal/WineCellar2/alkon-hinnasto-tekstitiedostona.xlsx");
		if (!file.exists()) {

			LOG.info("No local file cache found, fetching...");
			final String URL = "https://www.alko.fi/INTERSHOP/static/WFS/Alko-OnlineShop-Site/-/Alko-OnlineShop/fi_FI/Alkon%20Hinnasto%20Tekstitiedostona/alkon-hinnasto-tekstitiedostona.xlsx";

			file = File.createTempFile("alkofile", "xlsx");
			FileUtils.copyURLToFile(new URL(URL), file, 1000, 1000);
		}

		final Workbook w = WorkbookFactory.create(file);

		final Sheet sheet = w.getSheetAt(0);

		final Map<String, WineType> types = new HashMap<>();
		types.put("punaviinit", WineType.RED);
		types.put("valkoviinit", WineType.WHITE);
		types.put("roseviinit", WineType.ROSE);
		types.put("kuohuviinit & samppanjat", WineType.BUBBLY);

		int numAdded = 0;
		Wine special = null;
		for (int i = 0; i < sheet.getPhysicalNumberOfRows(); i++) {

			final Row row = sheet.getRow(i);

			final Cell cell = row.getCell(COL_TYPE);
			if (cell == null) {
				continue;
			}

			final String typeString = cell.getStringCellValue();
			final String sizeString = row.getCell(COL_SIZE).getStringCellValue();
			if (types.keySet().contains(typeString) && "0,75 l".equals(sizeString)) {

				final String producerName = row.getCell(COL_PROD).getStringCellValue();
				Producer prod = producers.get(producerName);
				if (prod == null) {
					prod = new Producer(producerName);
					prod = producerRepo.save(prod);
					producers.put(producerName, prod);
				}

				final String wineName = row.getCell(COL_NAME).getStringCellValue();

				final Wine wine = new Wine(wineName, prod, types.get(typeString));

				wine.setAlko_id(getCell(row, COL_ID));
				wine.setCountry(getCell(row, COL_COUNTRY));
				wine.setRegion(getCell(row, COL_REGION));
				wine.setSubregion(getCell(row, COL_REGION2));

				final String grapes = getCell(row, COL_GRAPES);

				if (grapes != null) {
					final List<String> grapeset = new ArrayList<>();
					wine.setGrapes(grapeset);
					for (String sub : grapes.split(",")) {
						sub = sub.trim();
						if (sub.isEmpty()) {
							continue;
						}
						grapeset.add(sub);
					}
				}

				service.addWine(wine);
				special = wine;

				numAdded++;
			}

		}

		w.close();
		LOG.info(String.format("imported %d wines from alko list", numAdded));

		return special;
	}

	private static String getCell(Row row, int col) {
		final Cell cell = row.getCell(col);

		return cell == null ? null : cell.getStringCellValue();
	}

}
