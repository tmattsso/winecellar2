package org.thomas.winecellar.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.thomas.winecellar.data.Producer;
import org.thomas.winecellar.data.Wine;
import org.thomas.winecellar.data.WineType;
import org.thomas.winecellar.service.WineService;
import org.thomas.winecellar.ui.components.GrapeSearchFilter;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;

@Route(value = "search", layout = MainView.class)
@CssImport(value = "./styles/search-accordion.css", themeFor = "vaadin-accordion")
@CssImport(value = "./styles/search-grid.css", themeFor = "vaadin-grid")
public class SearchView extends VerticalLayout implements HasViewTitle, AfterNavigationObserver {

	private static final long serialVersionUID = -3913509714364345226L;
	private final WineService service;

	private TextField searchBox;
	private ComboBox<Producer> filterProducer;
	private ComboBox<WineType> filterType;

	private final Grid<Wine> results;
	private GrapeSearchFilter filterGrapes;

	@Autowired
	public SearchView(WineService service) {

		this.service = service;
		setHeight("100%");

		searchBox = new TextField("Search for anything:");
		searchBox.focus();
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
		// TODO https://github.com/vaadin/vaadin-accordion-flow/issues/43
		filters.getElement().executeJs("setTimeout(() => {$0.opened = false;},200);", filters.getElement());

		filterType = new ComboBox<>("By Type", WineType.values());
		filterType.setWidth("100%");
		filterType.setClearButtonVisible(true);
		filterContent.add(filterType);

		filterProducer = new ComboBox<>("By producer:", service.getProducers());
		filterProducer.setItemLabelGenerator(prod -> prod.getName());
		filterProducer.setWidth("100%");
		filterProducer.setClearButtonVisible(true);
		filterContent.add(filterProducer);

		filterGrapes = new GrapeSearchFilter(service);
		filterGrapes.setWidthFull();
		filterContent.add(filterGrapes);

		results = new Grid<>();
		results.setWidth("100%");
		results.addClassName("searchgrid");
		results.getStyle().set("flex-grow", "1");
		results.addThemeVariants(GridVariant.LUMO_COMPACT);

		results.addColumn(Wine::getName).setHeader("Wine").setWidth("50%");
		results.addColumn(w -> w.getProducer() == null ? "N/A" : w.getProducer().getName()).setHeader("Producer");
		results.addColumn(w -> w.getType().toString()).setHeader("Type").setAutoWidth(true);

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
		filterGrapes.addValueChangeListener(e -> {
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

		final Set<String> grapes = filterGrapes.getValue();

		final Map<String, String> params = new HashMap<>();
		params.put("q", terms);
		if (producer != null) {
			params.put("p", producer);
		}
		if (type != null) {
			params.put("t", type.name());
		}
		if (grapes != null && !grapes.isEmpty()) {
			params.put("g", grapes.stream().collect(Collectors.joining(",")));
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
		list = parameters.get("g");
		String g = "";
		if (list != null && !list.isEmpty()) {
			g = list.get(0);
		}

		results.setItems(service.search(q, p, t, g));

		// Set values
		searchBox.setValue(q);

		if (p.isEmpty()) {
			filterProducer.clear();
		} else {
			final long id = Long.valueOf(p);
			final Producer producer = service.getProducerById(id);
			filterProducer.setValue(producer);
		}
		if (t.isEmpty()) {
			filterType.clear();
		} else {
			final WineType type = WineType.valueOf(t.toUpperCase());
			filterType.setValue(type);
		}
		if (g.isEmpty()) {
			filterGrapes.clear();
		} else {

			list = new ArrayList<>();
			for (final String param : parameters.get("g")) {
				final String[] split = param.split(",");
				for (final String s : split) {
					if (!s.isEmpty()) {
						list.add(s.trim());
					}
				}
			}
			filterGrapes.setValue(new HashSet<>(list));
		}

	}

}
