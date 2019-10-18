package org.thomas.winecellar.ui;

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
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
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

		add(new H3("Add stuffs"));

		final Select<WineType> typeSelect = new Select<>(WineType.values());
		add(typeSelect);

		final TextField nameField = new TextField("Wine name");
		add(nameField);

		producerField = new ComboBox<>("Producer");
		add(producerField);

		producerField.setItems(service.getProducers());

		producerField.setAllowCustomValue(true);
		producerField.addCustomValueSetListener(e -> addProducer(e.getDetail()));

		final Binder<Wine> binder = new Binder<>(Wine.class);
		binder.bind(typeSelect, Wine::getType, Wine::setType);
		binder.bind(nameField, Wine::getName, Wine::setName);
		binder.bind(producerField, Wine::getProducer, Wine::setProducer);

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
		Notification.show("Wine added", 2, Position.BOTTOM_CENTER);
		UI.getCurrent().navigate(WineListView.class);
	}

	private void addProducer(String detail) {
		final Producer prod = new Producer(detail);
		final List<Producer> producers = service.getProducers();
		producers.add(prod);
		producerField.setItems(producers);
		producerField.setValue(prod);
	}

}
