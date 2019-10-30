package org.thomas.winecellar.ui;

import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.thomas.winecellar.data.Wine;
import org.thomas.winecellar.service.CurrentUserProvider;
import org.thomas.winecellar.service.WineService;
import org.thomas.winecellar.ui.components.WineInListsDialog;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.UIScope;

@Route(value = "wine", layout = MainView.class)
@CssImport("./styles/winedetails.css")
@UIScope
public class WineDetailsView extends VerticalLayout implements HasUrlParameter<Long>, HasViewTitle {

	private static final long serialVersionUID = -3913509714364345226L;

	@Autowired
	private WineService service;

	private Wine wine;

	@Autowired
	private CurrentUserProvider currentUser;

	@Autowired
	private WineInListsDialog listsDialog;

	public WineDetailsView() {

		addClassName("winedetails");
	}

	@Override
	public void setParameter(BeforeEvent event, Long parameter) {

		if (currentUser.get() == null) {
			// Workaround for https://github.com/vaadin/flow/issues/4595
			// MainView will redirect to login, nothing to do here.
			return;
		}

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
		if (currentUser.get().getCellarList().has(wine)) {
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

		final HorizontalLayout ratings = WineRatingView.createSummary(wine);
		ratings.addClickListener(e -> openRatings());
		add(ratings);

		addSection("Producer:", wine.getProducer().getName());
		addSection("Type:", wine.getType().toString());
		addSection("Grapes:", wine.getGrapes().stream().collect(Collectors.joining(", ")));
		addSection("Country:", wine.getCountry());
		addSection("Region:", wine.getRegion());
		addSection("Subregion:", wine.getSubregion());

		if (wine.getAlko_id() != null) {
			final Button gotoAlko = new Button();
			gotoAlko.setText("View on Alko.fi");
			gotoAlko.setIcon(VaadinIcon.EXTERNAL_LINK.create());
			gotoAlko.addClickListener(e -> {
				UI.getCurrent().getPage().open("https://www.alko.fi/en/tuotteet/" + wine.getAlko_id());
			});
			gotoAlko.addThemeVariants(ButtonVariant.MATERIAL_CONTAINED);
			add(gotoAlko);
		}

		// final Image img = new Image(
		// "https://images.alko.fi/images/cs_srgb,f_auto,t_medium/cdn/480617/querciabella-chianti-classico-2016.jpg",
		// "le wine");
		// img.setHeight("200px");
		// add(img);
	}

	private void openRatings() {
		UI.getCurrent().navigate(WineRatingView.class, wine.getId());
	}

	private void addSection(String titleText, String contentText) {

		final Div title = new Div(new Span(titleText));
		final Div content = new Div(new Span(contentText));

		final Div div = new Div(title, content);
		div.addClassName("inforow");
		add(div);
	}

	@Override
	public String getTitle() {
		return "Wine Details";
	}

}
