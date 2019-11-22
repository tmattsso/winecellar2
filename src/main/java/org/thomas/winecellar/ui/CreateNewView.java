package org.thomas.winecellar.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.thomas.winecellar.data.Producer;
import org.thomas.winecellar.data.Wine;
import org.thomas.winecellar.data.WineType;
import org.thomas.winecellar.service.WineService;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.Route;

@Route(value = "create", layout = MainView.class)
public class CreateNewView extends VerticalLayout {

	private static final long serialVersionUID = -3913509714364345226L;

	private static final String EMPTY_SELECTION = "(none)";

	private ComboBox<Producer> producerField;

	@Autowired
	private WineService service;

	@PostConstruct
	public void init() {

		add(new H3("Add Wine to system"));
		add(new Label("Although you can add a wine here, please do a search first!"));

		final ComboBox<WineType> typeSelect = new ComboBox<>("Wine Type", WineType.values());
		typeSelect.setWidthFull();
		add(typeSelect);

		final TextField nameField = new TextField("Wine name");
		nameField.setWidthFull();
		add(nameField);

		producerField = new ComboBox<>("Producer");
		producerField.setWidthFull();
		add(producerField);

		producerField.setItems(service.getProducers());
		producerField.setItemLabelGenerator(p -> p.getName());
		producerField.setAllowCustomValue(true);
		producerField.addCustomValueSetListener(e -> addProducer(e.getDetail()));

		final ComboBox<String> countryField = new ComboBox<>("Country");
		countryField.setWidthFull();
		add(countryField);
		countryField.setAllowCustomValue(true);
		countryField.addCustomValueSetListener(e -> addString(countryField, e.getDetail()));

		final ComboBox<String> regionField = new ComboBox<>("Region");
		regionField.setWidthFull();
		add(regionField);
		regionField.setAllowCustomValue(true);
		regionField.addCustomValueSetListener(e -> addString(regionField, e.getDetail()));

		final ComboBox<String> subregionField = new ComboBox<>("Subregion");
		subregionField.setWidthFull();
		add(subregionField);
		subregionField.setAllowCustomValue(true);
		subregionField.addCustomValueSetListener(e -> addString(subregionField, e.getDetail()));

		setupCountrySelectors(countryField, regionField, subregionField);

		final TextField grapesField = new TextField("Grapes (comma separated)");
		grapesField.setWidthFull();
		add(grapesField);

		final BeanValidationBinder<Wine> binder = new BeanValidationBinder<>(Wine.class);
		binder.forField(typeSelect).asRequired().bind("type");
		binder.forField(nameField).asRequired().bind("name");
		binder.forField(producerField).asRequired().bind("producer");
		binder.forField(countryField).asRequired().bind("country");
		binder.bind(regionField, "region");
		binder.bind(subregionField, "subregion");
		binder.bind(grapesField, w -> w.getGrapes().toString(), (w, value) -> {
			final String[] split = value.split(",");
			final List<String> list = new ArrayList<>();
			for (final String s : split) {
				if (!s.isEmpty()) {
					list.add(s.trim());
				}
			}
			w.setGrapes(list);
		});

		final Button save = new Button("Add wine", VaadinIcon.CHECK.create(), e -> {
			try {
				final Wine wine = new Wine();
				binder.writeBean(wine);
				service.addWine(wine);

				Notification.show("Wine added", 1500, Position.MIDDLE)
						.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
				UI.getCurrent().navigate(WineListView.class);

			} catch (final ValidationException e1) {

				e1.printStackTrace();
				Notification.show("Couldn't store wine", 2000, Position.MIDDLE)
						.addThemeVariants(NotificationVariant.LUMO_ERROR);
			}
		});
		save.addThemeVariants(ButtonVariant.MATERIAL_CONTAINED);
		add(save);
	}

	private void setupCountrySelectors(ComboBox<String> countryField, ComboBox<String> regionField,
			ComboBox<String> subregionField) {

		final Map<String, Map<String, Set<String>>> countrymap = service.getCountries();

		final List<String> countries = countrymap.keySet().stream().distinct().sorted().collect(Collectors.toList());
		countryField.setItems(countries);

		countryField.addValueChangeListener(e -> {
			final String country = e.getValue();

			if (country == null) {
				regionField.setItems(new HashSet<>());
				return;
			}

			final Map<String, Set<String>> map = countrymap.get(country);
			final List<String> regions = map.keySet().stream().distinct().sorted().collect(Collectors.toList());

			// Some wines have empty regions, but subregions (typically appellations)
			regions.replaceAll(s -> s.isEmpty() ? EMPTY_SELECTION : s);

			regionField.setItems(regions);
		});

		regionField.addValueChangeListener(e -> {
			final String region = e.getValue();
			final String country = countryField.getValue();

			if (region == null || country == null) {
				regionField.setItems(new HashSet<>());
				return;
			}

			final Map<String, Set<String>> map = countrymap.get(country);
			final Set<String> set = map.get(EMPTY_SELECTION.equals(region) ? "" : region);
			final List<String> regions = set.stream().distinct().sorted().collect(Collectors.toList());

			subregionField.setItems(regions);
		});
	}

	private void addProducer(String detail) {
		final Producer prod = new Producer(detail);
		final List<Producer> producers = service.getProducers();
		producers.add(prod);
		producerField.setItems(producers);
		producerField.setValue(prod);
	}

	private void addString(ComboBox<String> cb, String detail) {

		if (detail == null || detail.isEmpty()) {
			return;
		}

		@SuppressWarnings("unchecked")
		final Collection<String> items = ((ListDataProvider<String>) cb.getDataProvider()).getItems();
		items.add(detail);
		cb.setItems(items);
		cb.setValue(detail);
	}

}
