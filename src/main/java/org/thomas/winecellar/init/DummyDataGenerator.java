package org.thomas.winecellar.init;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
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

			final Cell cell = row.getCell(8);
			if (cell == null) {
				continue;
			}

			final String typeString = cell.getStringCellValue();
			if (types.keySet().contains(typeString)) {

				final String producerName = row.getCell(2).getStringCellValue();
				Producer prod = producers.get(producerName);
				if (prod == null) {
					prod = new Producer(producerName);
					prod.setId(++id);
					producers.put(producerName, prod);
				}

				final String wineName = row.getCell(1).getStringCellValue();

				createWine(wineName, prod, types.get(typeString));
				numAdded++;
			}

		}
		LOG.info(String.format("imported %d wines from alko list", numAdded));
	}

}
