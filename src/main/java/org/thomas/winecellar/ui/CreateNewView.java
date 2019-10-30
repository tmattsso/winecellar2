package org.thomas.winecellar.ui;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.thomas.winecellar.data.Producer;
import org.thomas.winecellar.data.Wine;
import org.thomas.winecellar.data.WineType;
import org.thomas.winecellar.service.WineService;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.Route;

@Route(value = "create", layout = MainView.class)
public class CreateNewView extends VerticalLayout {

	private static final long serialVersionUID = -3913509714364345226L;

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
		countryField.setItems(service.getCountries());
		countryField.setAllowCustomValue(true);
		countryField.addCustomValueSetListener(e -> addString(countryField, e.getDetail(), service.getCountries()));

		final ComboBox<String> regionField = new ComboBox<>("Region");
		regionField.setWidthFull();
		add(regionField);
		regionField.setItems(service.getRegions());
		regionField.setAllowCustomValue(true);
		regionField.addCustomValueSetListener(e -> addString(regionField, e.getDetail(), service.getCountries()));

		final ComboBox<String> subregionField = new ComboBox<>("Subregion");
		subregionField.setWidthFull();
		add(subregionField);
		subregionField.setItems(service.getSubregions());
		subregionField.setAllowCustomValue(true);
		subregionField.addCustomValueSetListener(e -> addString(subregionField, e.getDetail(), service.getCountries()));

		final TextField grapesField = new TextField("Grapes");
		grapesField.setWidthFull();
		add(grapesField);

		final Binder<Wine> binder = new Binder<>(Wine.class);
		binder.bind(typeSelect, Wine::getType, Wine::setType);
		binder.bind(nameField, Wine::getName, Wine::setName);
		binder.bind(producerField, Wine::getProducer, Wine::setProducer);
		binder.bind(countryField, Wine::getCountry, Wine::setCountry);
		binder.bind(regionField, Wine::getRegion, Wine::setRegion);
		binder.bind(subregionField, Wine::getSubregion, Wine::setSubregion);
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
				showSuccess();
			} catch (final ValidationException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		});
		add(save);
	}

	private void showSuccess() {
		// TODO Auto-generated method stub
		Notification.show("Wine added", 2, Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
		UI.getCurrent().navigate(WineListView.class);
	}

	private void addProducer(String detail) {
		final Producer prod = new Producer(detail);
		final List<Producer> producers = service.getProducers();
		producers.add(prod);
		producerField.setItems(producers);
		producerField.setValue(prod);
	}

	private void addString(ComboBox<String> cb, String detail, List<String> items) {
		items.add(detail);
		cb.setItems(items);
		cb.setValue(detail);
	}

}
