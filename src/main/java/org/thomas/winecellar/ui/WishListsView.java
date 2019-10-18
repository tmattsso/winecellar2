package org.thomas.winecellar.ui;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.thomas.winecellar.data.User;
import org.thomas.winecellar.data.WineList;
import org.thomas.winecellar.service.UserService;
import org.thomas.winecellar.ui.components.DoubleButton;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

@Route(value = "wishlists", layout = MainView.class)
@CssImport("./styles/winelist.css")
@CssImport(value = "./styles/winelist-buttons.css", themeFor = "vaadin-button")
public class WishListsView extends VerticalLayout {

	private static final long serialVersionUID = 2382311022514671116L;

	private VerticalLayout listsWrapper;

	@Autowired
	private User currentUser;

	@Autowired
	private UserService service;

	private TextField filter;

	@PostConstruct
	public void init() {

		removeAll();

		listsWrapper = new VerticalLayout();

		filter = new TextField("Filter lists");
		filter.setClearButtonVisible(true);
		filter.addInputListener(e -> {
			if (e.isFromClient()) {
				renderList(filter.getValue());
			}
		});
		add(filter);

		listsWrapper.setPadding(false);
		add(listsWrapper);
		setFlexGrow(1, listsWrapper);
		renderList(null);

		final Button name = new Button("Create new List", VaadinIcon.PLUS.create(), e -> createList());
		name.addThemeVariants(ButtonVariant.MATERIAL_CONTAINED);
		add(name);
	}

	private void renderList(String filterText) {

		listsWrapper.removeAll();

		currentUser.getWishlists().forEach((list) -> {

			if (filterText != null && !list.getName().toLowerCase().contains(filterText)) {
				// NOOP
			} else {
				final HorizontalLayout hl = new HorizontalLayout();
				hl.setWidth("100%");
				hl.addClassName("winelist-item");
				listsWrapper.add(hl);

				final int amount = list.getWines().size();
				final Button name = new Button(
						String.format("%S (%d wine%s)", list.getName(), amount, amount == 1 ? "" : "s"),
						e -> UI.getCurrent().navigate(WineListView.class, list.getId()));
				name.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
				hl.add(name);
				hl.setFlexGrow(1, name);

				final Button remove = new DoubleButton(VaadinIcon.TRASH.create(), e -> deleteList(list));
				remove.addThemeVariants(ButtonVariant.MATERIAL_CONTAINED);
				remove.addClassName("amountButton");
				hl.add(remove);
			}
		});
	}

	private void deleteList(WineList list) {
		service.removeList(list);
		Notification.show("List deleted", 2000, Position.BOTTOM_CENTER);
		renderList(filter.getValue());
	}

	private void createList() {
		final Dialog popup = new Dialog();
		popup.setWidth("300px");

		final VerticalLayout vl = new VerticalLayout();
		vl.setPadding(false);
		popup.add(vl);

		final TextField nameField = new TextField("List name");
		nameField.setWidth("100%");
		vl.add(nameField);

		final Span error = new Span("");
		error.setMinHeight("22px");
		vl.add(error);

		nameField.addInputListener(e -> error.setText(""));

		final Button create = new Button("Create list", VaadinIcon.STAR.create(), e -> {

			final String name = nameField.getValue();
			if (name == null || name.isEmpty()) {
				error.setText("Invalid name!");
			} else {
				final WineList list = service.createList(name);
				if (list != null) {
					UI.getCurrent().navigate(WineListView.class, list.getId());
				} else {
					error.setText("Name already taken");
				}
			}

		});
		create.addThemeVariants(ButtonVariant.MATERIAL_CONTAINED);
		create.setWidth("100%");
		vl.add(create);

		add(popup);
		popup.open();

		nameField.focus();
	}

}
