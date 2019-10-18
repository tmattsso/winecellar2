package org.thomas.winecellar.ui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.thomas.winecellar.data.Producer;
import org.thomas.winecellar.data.Wine;
import org.thomas.winecellar.data.WineType;
import org.thomas.winecellar.service.WineService;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;

@Route(value = "search", layout = MainView.class)
@CssImport(value = "./styles/search-accordion.css", themeFor = "vaadin-accordion")
public class SearchView extends VerticalLayout implements HasViewTitle, AfterNavigationObserver {

	private static final long serialVersionUID = -3913509714364345226L;
	private final WineService service;

	private TextField searchBox;
	private ComboBox<Producer> filterProducer;
	private ComboBox<WineType> filterType;

	private final Grid<Wine> results;

	@Autowired
	public SearchView(WineService service) {

		this.service = service;
		setHeight("100%");

		searchBox = new TextField("Search for anything:");
		add(searchBox);

		final Accordion controls = new Accordion();
		controls.getElement().setAttribute("class", "search-accordion");
		final Div filterContent = new Div();
		final AccordionPanel filters = new AccordionPanel("Filter", filterContent);
		controls.add(filters);
		final AccordionPanel sorts = new AccordionPanel("Sort", new VerticalLayout());
		controls.add(sorts);
		add(controls);

		filters.setOpened(false);

		filterType = new ComboBox<>("By Type", WineType.values());
		filterType.setWidth("100%");
		filterType.setClearButtonVisible(true);
		filterContent.add(filterType);

		filterProducer = new ComboBox<>("By producer:", service.getProducers());
		filterProducer.setItemLabelGenerator(prod -> prod.getName());
		filterProducer.setWidth("100%");
		filterProducer.setClearButtonVisible(true);
		filterContent.add(filterProducer);

		results = new Grid<>();
		results.setWidth("100%");
		results.getStyle().set("flex-grow", "1");

		results.addColumn(Wine::getName).setHeader("Wine");
		results.addColumn(w -> w.getProducer() == null ? "N/A" : w.getProducer().getName()).setHeader("Producer");
		results.addColumn(w -> w.getType().toString()).setHeader("Type");

		add(results);

		results.setItems(service.getWines());

		searchBox.addValueChangeListener(e -> {
			if (!e.isFromClient()) {
				return;
			}
			filters.setOpened(false);
			runSearch(filters);
		});

		filterProducer.addValueChangeListener(e -> {
			if (!e.isFromClient()) {
				return;
			}
			runSearch(filters);
		});
		filterType.addValueChangeListener(e -> {
			if (!e.isFromClient()) {
				return;
			}
			runSearch(filters);
		});

		results.addSelectionListener(e -> {
			if (e.isFromClient() && !e.getAllSelectedItems().isEmpty()) {
				final Wine wine = e.getFirstSelectedItem().get();
				UI.getCurrent().navigate(WineDetailsView.class, wine.getId());
			}
		});
	}

	private void runSearch(final AccordionPanel filters) {

		final String terms = searchBox.getValue();

		String producer = null;
		final Producer value = filterProducer.getValue();
		if (value != null) {
			producer = String.valueOf(value.getId());
		}

		final WineType type = filterType.getValue();

		final Map<String, String> params = new HashMap<>();
		params.put("q", terms);
		if (producer != null) {
			params.put("p", producer);
		}
		if (type != null) {
			params.put("t", type.name());
		}
		final QueryParameters qp = QueryParameters.simple(params);
		UI.getCurrent().navigate("search", qp);
	}

	@Override
	public String getTitle() {
		return "Search For Wine";
	}

	@Override
	public void afterNavigation(AfterNavigationEvent event) {
		final Map<String, List<String>> parameters = event.getLocation().getQueryParameters().getParameters();

		List<String> list = parameters.get("q");
		String q = "";
		if (list != null && !list.isEmpty()) {
			q = list.get(0);
		}
		list = parameters.get("p");
		String p = "";
		if (list != null && !list.isEmpty()) {
			p = list.get(0);
		}
		list = parameters.get("t");
		String t = "";
		if (list != null && !list.isEmpty()) {
			t = list.get(0);
		}

		results.setItems(service.search(q, p, t));

		// Set values
		searchBox.setValue(q);

		if (p.isEmpty()) {
			filterProducer.clear();
		} else {
			final long id = Long.valueOf(t);
			final Producer producer = service.getProducerById(id);
			filterProducer.setValue(producer);
		}
		if (t.isEmpty()) {
			filterType.clear();
		} else {
			final WineType type = WineType.valueOf(t.toUpperCase());
			filterType.setValue(type);
		}

	}

}
