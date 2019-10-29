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

		try {
			initAlkoData();
		} catch (final IOException e) {
			e.printStackTrace();
		}

		final WineList wish = new WineList();
		wish.setName("Wish List");

		demoUser = new User();
		demoUser.setName("Thomas");
		demoUser.addWishList(wish);
		demoUser = users.addUser(demoUser);

		createLotsaLists(true);

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

	private void initAlkoData() throws IOException {
		final String URL = "https://www.alko.fi/INTERSHOP/static/WFS/Alko-OnlineShop-Site/-/Alko-OnlineShop/fi_FI/Alkon%20Hinnasto%20Tekstitiedostona/alkon-hinnasto-tekstitiedostona.xlsx";

		final File tempFile = File.createTempFile("alkofile", "xlsx");
		FileUtils.copyURLToFile(new URL(URL), tempFile, 1000, 1000);

		final Workbook w = WorkbookFactory.create(tempFile);

		final Sheet sheet = w.getSheetAt(0);

		final Map<String, WineType> types = new HashMap<>();
		types.put("punaviinit", WineType.RED);
		types.put("valkoviinit", WineType.WHITE);
		types.put("roseviinit", WineType.ROSE);
		types.put("kuohuviinit & samppanjat", WineType.BUBBLY);

		int numAdded = 0;
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

				numAdded++;
			}

		}

		w.close();
		LOG.info(String.format("imported %d wines from alko list", numAdded));
	}

	private static String getCell(Row row, int col) {
		final Cell cell = row.getCell(col);

		return cell == null ? null : cell.getStringCellValue();
	}

}
