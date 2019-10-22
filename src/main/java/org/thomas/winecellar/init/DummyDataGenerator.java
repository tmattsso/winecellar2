package org.thomas.winecellar.init;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

		final Producer sl = createProducer("Stags Leap");
		cellar.put(createWine("The Investor", sl, WineType.RED));
		wish.put(createWine("Artemis", sl, WineType.RED));

		try {
			initAlkoData();
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

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

	private void initAlkoData() throws IOException {
		final Workbook w = WorkbookFactory
				.create(new File("/Users/thomas/Work/Personal/WineCellar2/alkon-hinnasto-tekstitiedostona.xlsx"));

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
					prod.setId(++id);
					producers.put(producerName, prod);
				}

				final String wineName = row.getCell(COL_NAME).getStringCellValue();

				final Wine wine = createWine(wineName, prod, types.get(typeString));

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

				numAdded++;
			}

		}
		LOG.info(String.format("imported %d wines from alko list", numAdded));
	}

	private static String getCell(Row row, int col) {
		final Cell cell = row.getCell(col);

		return cell == null ? null : cell.getStringCellValue();
	}

}
