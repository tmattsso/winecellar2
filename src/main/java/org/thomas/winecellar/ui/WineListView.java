package org.thomas.winecellar.ui;

import org.springframework.beans.factory.annotation.Autowired;
import org.thomas.winecellar.data.User;
import org.thomas.winecellar.data.Wine;
import org.thomas.winecellar.data.WineList;
import org.thomas.winecellar.service.CurrentUserProvider;
import org.thomas.winecellar.service.UserService;
import org.thomas.winecellar.service.WineService;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.shared.Registration;

@Route(value = "list", layout = MainView.class)
@RouteAlias(value = "", layout = MainView.class)
@CssImport("./styles/winelist.css")
@CssImport(value = "./styles/winelist-buttons.css", themeFor = "vaadin-button")
public class WineListView extends VerticalLayout implements HasUrlParameter<Long>, HasViewTitle {

	private static final long serialVersionUID = -3913509714364345226L;

	@Autowired
	private WineService wservice;

	@Autowired
	private UserService uservice;

	@Autowired
	private CurrentUserProvider cup;

	private boolean isCellarList;
	private WineList selectedList;

	private Registration editClickListener;

	public WineListView() {

	}

	@Override
	public void setParameter(BeforeEvent event, @OptionalParameter Long parameter) {

		removeAll();

		final User user = cup.get();
		if (user == null) {
			add(new H3("No such list."));
			return;
		}

		isCellarList = false;

		if (parameter == null) {

			if (user.getCellarList() != null) {
				parameter = user.getCellarList().getId();
			} else {
				add(new H3("User has no cellar list"));
				return;
			}
		}

		if (user.getCellarList() != null && user.getCellarList().getId() == parameter) {
			isCellarList = true;
			renderList(user.getCellarList(), null);
		} else if (user.getWishList(parameter) != null) {
			renderList(user.getWishList(parameter), null);
		} else {
			add(new H3("No such list."));
		}
	}

	private void renderList(WineList cellarList, String filterText) {
		selectedList = cellarList;

		if (!isCellarList) {

			final HorizontalLayout hl = new HorizontalLayout();
			hl.setPadding(false);
			hl.setWidth("100%");
			add(hl);

			final TextField nameField = new TextField();
			nameField.setValue(selectedList.getName());
			nameField.setReadOnly(true);
			hl.add(nameField);
			hl.setFlexGrow(1, nameField);

			final Button edit = new Button(VaadinIcon.PENCIL.create());
			edit.addClassName("pureicon");
			edit.addThemeVariants(ButtonVariant.MATERIAL_CONTAINED);
			editClickListener = edit.addClickListener(e -> {

				// switch to edit
				nameField.setReadOnly(false);
				nameField.focus();
				editClickListener.remove();
				edit.setIcon(VaadinIcon.CHECK.create());
				edit.addClickListener(ev -> {
					// do edit
					final WineList list = uservice.renameList(cup.get(), selectedList, nameField.getValue());

					if (list != null) {
						setParameter(null, list.getId());
						Notification.show("Name saved", 1000, Position.MIDDLE);
					} else {
						// name error
						Notification.show("Invalid name", 3000, Position.MIDDLE);
						nameField.focus();
					}
				});
			});
			hl.add(edit);
		}

		if (cellarList.getWines().isEmpty()) {

			final H4 label = new H4("No wines in list.");
			final Button button = new Button("Search for wines...", e -> UI.getCurrent().navigate(SearchView.class));
			button.addThemeVariants(ButtonVariant.MATERIAL_CONTAINED);
			add(label, button);

		} else {

			final TextField filter = new TextField("Filter wines");
			filter.setClearButtonVisible(true);
			filter.addInputListener(e -> {
				if (e.isFromClient()) {
					removeAll();
					renderList(cellarList, filter.getValue());
				}
			});
			filter.setValue(filterText == null ? "" : filterText);
			add(filter);

			cellarList.getWines().forEach((wine, amount) -> {

				final HorizontalLayout hl = new HorizontalLayout();
				hl.setWidth("100%");
				hl.addClassName("winelist-item");
				add(hl);

				if (filterText != null) {
					if (wine.getName().toLowerCase().contains(filterText.toLowerCase())
							|| wine.getProducer().getName().toLowerCase().contains(filterText.toLowerCase())) {
						renderWineEntry(hl, wine, amount);
					}
				} else {
					renderWineEntry(hl, wine, amount);
				}
			});
		}

	}

	private void renderWineEntry(HorizontalLayout hl, Wine wine, Integer amount) {

		hl.removeAll();

		final Button name = new Button(wine.getName(),
				e -> UI.getCurrent().navigate(WineDetailsView.class, wine.getId()));
		name.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
		hl.add(name);
		hl.setFlexGrow(1, name);

		if (amount == null) {
			amount = 0;
		}
		final Label amountLabel = new Label(String.valueOf(amount));

		if (amount == 0) {
			final Icon icon = VaadinIcon.TRASH.create();
			icon.setColor("#F00");
			final Button remove = new Button(icon, e -> removeWine(wine, hl));
			remove.addThemeVariants(ButtonVariant.LUMO_ICON);
			remove.addClassName("amountButton");
			hl.add(remove);

		} else {
			final Button subtract = new Button(VaadinIcon.MINUS_CIRCLE.create(), e -> modifyWineAmount(wine, -1, hl));
			subtract.addThemeVariants(ButtonVariant.LUMO_ICON);
			subtract.addClassName("amountButton");

			hl.add(subtract);
		}
		final Button add = new Button(VaadinIcon.PLUS_CIRCLE.create(), e -> modifyWineAmount(wine, 1, hl));
		add.addThemeVariants(ButtonVariant.LUMO_ICON);
		add.addClassName("amountButton");

		hl.add(amountLabel, add);
	}

	private void removeWine(Wine wine, HorizontalLayout hl) {
		wservice.removeFromList(selectedList, wine);
		remove(hl);
	}

	private void modifyWineAmount(Wine wine, int delta, HorizontalLayout hl) {
		final int newAmount = wservice.modifyListAmount(selectedList, wine, delta);
		renderWineEntry(hl, wine, newAmount);
	}

	@Override
	public String getTitle() {
		if (isCellarList) {
			return "Cellar List";
		}
		if (selectedList != null) {
			return "Wish List";
		}
		return "No wine list found";
	}
}
