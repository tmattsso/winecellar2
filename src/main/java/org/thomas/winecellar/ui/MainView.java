package org.thomas.winecellar.ui;

import org.springframework.beans.factory.annotation.Autowired;
import org.thomas.winecellar.service.CurrentUserProvider;
import org.thomas.winecellar.ui.components.DrawerComponent;

import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.material.Material;

@CssImport("./styles/main.css")
@CssImport(value = "./styles/createnewbutton.css", themeFor = "vaadin-button")
@CssImport(value = "./styles/dialog.css", themeFor = "vaadin-dialog-overlay")
@Theme(Material.class)
public class MainView extends AppLayout implements BeforeEnterObserver {

	private static final long serialVersionUID = -2453134437569568704L;

	private static final String TITLE_DEFAULT = "WineCellar v2.0";

	private final Label title;

	@Autowired
	private CurrentUserProvider currentUser;

	public MainView() {

		setDrawerOpened(false);

		title = new Label(TITLE_DEFAULT);
		title.getStyle().set("flex-grow", "1");
		final Button search = new Button(VaadinIcon.SEARCH.create(), e -> UI.getCurrent().navigate(SearchView.class));
		search.addClassName("searchbutton");
		search.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY);

		addToNavbar(true, new DrawerToggle(), title, search);

		addToDrawer(new DrawerComponent("My Cellar", VaadinIcon.OPEN_BOOK.create(),
				() -> UI.getCurrent().navigate(WineListView.class)));

		addToDrawer(new DrawerComponent("My Wishlists", VaadinIcon.STAR.create(),
				() -> UI.getCurrent().navigate(WishListsView.class)));

		addToDrawer(new DrawerComponent("Search for wine", VaadinIcon.SEARCH.create(),
				() -> UI.getCurrent().navigate(SearchView.class)));

		addToDrawer(new DrawerComponent("Add wine", VaadinIcon.PLUS.create(),
				() -> UI.getCurrent().navigate(CreateNewView.class)));

		addToDrawer(new DrawerComponent("About", VaadinIcon.INFO_CIRCLE.create(),
				() -> UI.getCurrent().navigate(AboutView.class)));

	}

	@Override
	public void showRouterLayoutContent(HasElement content) {
		super.showRouterLayoutContent(content);

		if (content instanceof HasStyle) {
			((HasStyle) content).addClassName("main-panel-content-wrapper");
		}

		if (content instanceof HasViewTitle) {
			title.setText(((HasViewTitle) content).getTitle());
		} else {
			title.setText(TITLE_DEFAULT);
		}
	}

	@Override
	public void beforeEnter(BeforeEnterEvent event) {
		if (currentUser.get() == null) {
			event.rerouteTo(LoginView.class);
		}
	}

	// @Override
	// protected void onAttach(AttachEvent attachEvent) {
	// super.onAttach(attachEvent);
	//
	// addCreateNewButton(attachEvent);
	// }
	//
	// private void addCreateNewButton(AttachEvent attachEvent) {
	// final Icon icon = VaadinIcon.PLUS.create();
	// icon.setSize("40px");
	// icon.setColor("#FFF");
	//
	// final Button createNew = new Button(icon, e ->
	// UI.getCurrent().navigate(CreateNewView.class));
	// createNew.addThemeVariants(ButtonVariant.LUMO_ICON);
	// createNew.addClassName("createnew");
	//
	// attachEvent.getUI().add(createNew);
	// }

}
