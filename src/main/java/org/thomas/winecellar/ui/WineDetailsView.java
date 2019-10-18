package org.thomas.winecellar.ui;

import org.springframework.beans.factory.annotation.Autowired;
import org.thomas.winecellar.data.User;
import org.thomas.winecellar.data.Wine;
import org.thomas.winecellar.service.WineService;
import org.thomas.winecellar.ui.components.WineInListsDialog;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.UIScope;

@Route(value = "wine", layout = MainView.class)
@UIScope
public class WineDetailsView extends VerticalLayout implements HasUrlParameter<Long>, HasViewTitle {

	private static final long serialVersionUID = -3913509714364345226L;

	@Autowired
	private WineService service;

	private Wine wine;

	@Autowired
	private User currentUser;

	@Autowired
	private WineInListsDialog listsDialog;

	public WineDetailsView() {

	}

	@Override
	public void setParameter(BeforeEvent event, Long parameter) {

		removeAll();

		if (parameter == null) {
			add(new H3("Wine not found!"));
			return;
		}

		add(listsDialog);

		wine = service.getById(parameter.longValue());

		if (wine == null) {
			add(new H3("Wine not found!"));
			return;
		}

		add(new H3(wine.getName()));

		final Button add = new Button();
		if (currentUser.getCellarList().has(wine)) {
			add.setText("In Cellar!");
			add.setIcon(VaadinIcon.CHECK.create());
		} else {
			add.setText("Add to list");
			add.setIcon(VaadinIcon.STAR.create());
		}
		add.addClickListener(e -> listsDialog.openDialog(wine, () -> setParameter(event, parameter)));
		add.addThemeVariants(ButtonVariant.MATERIAL_CONTAINED);
		add.addClassName("in-list-button");
		add(add);

		Span l = new Span(String.format("Producer: %s", wine.getProducer().getName()));
		add(l);

		l = new Span(String.format("Type: %s", wine.getType().toString()));
		add(l);

		// final Image img = new Image(
		// "https://images.alko.fi/images/cs_srgb,f_auto,t_medium/cdn/480617/querciabella-chianti-classico-2016.jpg",
		// "le wine");
		// img.setHeight("200px");
		// add(img);
	}

	@Override
	public String getTitle() {
		return "Wine Details";
	}

}
