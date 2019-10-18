package org.thomas.winecellar.ui.components;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.thomas.winecellar.data.User;
import org.thomas.winecellar.data.Wine;
import org.thomas.winecellar.data.WineList;
import org.thomas.winecellar.service.UserService;
import org.thomas.winecellar.service.WineService;
import org.thomas.winecellar.ui.WineListView;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;

@Component
@Scope("prototype")
public class WineInListsDialog extends Dialog {

	private static final long serialVersionUID = 7308981363610143852L;

	@Autowired
	private WineService service;

	@Autowired
	private UserService uservice;

	@Autowired
	private User currentUser;

	private Wine wine;
	private Runnable callback;

	public WineInListsDialog() {
		setWidth("100%");
		getElement().setAttribute("theme", "fullwidth");
	}

	public void openDialog(Wine wine, Runnable callback) {

		this.wine = wine;

		this.callback = callback;
		removeAll();
		open();

		final VerticalLayout vl = new VerticalLayout();
		vl.setPadding(false);
		add(vl);

		vl.add(new H4("Cellar situation:"));

		HorizontalLayout row = new HorizontalLayout();
		row.setWidth("100%");
		row.setHeight("36px");
		vl.add(row);

		if (currentUser.getCellarList().has(wine)) {
			final Integer amount = currentUser.getCellarList().get(wine);
			row.add(span(String.format("You have %d bottle%s!", amount, amount > 1 ? "s" : "")));
			final Button gotoList = new Button("Go to list", VaadinIcon.ARROW_FORWARD.create(),
					e -> UI.getCurrent().navigate(WineListView.class));
			gotoList.addThemeVariants(ButtonVariant.MATERIAL_CONTAINED);
			row.add(gotoList);
		} else {
			row.add(span("No bottles, add?"));
			final NumberField nf = new NumberField();
			nf.setValue(1d);
			nf.setStep(1);
			nf.setMin(1);
			nf.setWidth("2em");
			row.add(nf);
			final Button addToList = new Button("Add", VaadinIcon.PLUS.create(),
					e -> addToList(currentUser.getCellarList(), nf.getValue()));
			addToList.addThemeVariants(ButtonVariant.MATERIAL_CONTAINED);
			row.add(addToList);
		}
		row.setFlexGrow(1, row.getChildren().findFirst().get());

		vl.add(new H4("Wishlists:"));

		final VerticalLayout scrollWrapper = new VerticalLayout();
		scrollWrapper.setPadding(false);
		scrollWrapper.getStyle().set("overflow-y", "auto");
		scrollWrapper.getStyle().set("flex-grow", "1");
		scrollWrapper.getStyle().set("max-height", "325px");
		scrollWrapper.setWidth("100%");
		vl.add(scrollWrapper);
		vl.setFlexGrow(1, scrollWrapper);

		for (final WineList list : currentUser.getWishlists()) {
			row = new HorizontalLayout();
			row.setWidth("100%");
			row.setHeight("36px");
			scrollWrapper.add(row);

			final int amount = list.get(wine);
			row.add(span(String.format(list.getName() + ": %d bottle%s", amount, amount == 1 ? "" : "s")));
			if (list.has(wine)) {
				final Button gotoList = new Button("Go to list", VaadinIcon.ARROW_FORWARD.create(),
						e -> UI.getCurrent().navigate(WineListView.class, list.getId()));
				gotoList.addThemeVariants(ButtonVariant.MATERIAL_CONTAINED);
				row.add(gotoList);
			} else {
				final NumberField nf = new NumberField();
				nf.setValue(1d);
				nf.setStep(1);
				nf.setMin(1);
				nf.setWidth("2em");
				row.add(nf);
				final Button addToList = new Button("Add", VaadinIcon.PLUS.create(),
						e -> addToList(list, nf.getValue()));
				addToList.addThemeVariants(ButtonVariant.MATERIAL_CONTAINED);
				row.add(addToList);
			}
			row.setFlexGrow(1, row.getChildren().findFirst().get());

		}

		final HorizontalLayout hl = new HorizontalLayout();
		hl.setWidth("100%");
		vl.add(hl);

		final Button createList = new Button("Create new wishlist", VaadinIcon.PLUS.create(), e -> createList(hl));
		createList.addThemeVariants(ButtonVariant.MATERIAL_CONTAINED);
		hl.add(createList);
	}

	private Span span(String content) {
		final Span span = new Span(content);
		span.getStyle().set("align-self", "center");
		return span;
	}

	private void createList(HorizontalLayout hl) {
		hl.removeAll();

		final TextField nameField = new TextField();
		nameField.focus();
		hl.add(nameField);
		hl.setFlexGrow(1, nameField);

		final Button edit = new Button(VaadinIcon.CHECK.create());
		edit.addClassName("pureicon");
		edit.addThemeVariants(ButtonVariant.MATERIAL_CONTAINED);
		edit.addClickListener(e -> {

			final WineList list = uservice.createList(nameField.getValue());

			if (list != null) {
				service.modifyListAmount(list, wine, 1);
				close();
				Notification.show("List Created!", 2000, Position.MIDDLE);
			} else {
				// name error
				Notification.show("Invalid name", 3000, Position.MIDDLE);
			}
		});
		hl.add(edit);
	}

	private void addToList(WineList list, Double value) {
		service.modifyListAmount(list, wine, value.intValue());
		close();
		Notification.show("Wine added to list", 2000, Position.MIDDLE);
		callback.run();
	}
}
