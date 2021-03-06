package org.thomas.winecellar.ui;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.thomas.winecellar.data.User;
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
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.spring.annotation.UIScope;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.material.Material;

@CssImport("./styles/main.css")
@CssImport(value = "./styles/buttons.css", themeFor = "vaadin-button")
@CssImport(value = "./styles/dialog.css", themeFor = "vaadin-dialog-overlay")
@CssImport(value = "./styles/notifications.css", themeFor = "vaadin-notification-card")
@Theme(Material.class)
@UIScope
public class MainView extends AppLayout {

	private static final long serialVersionUID = -2453134437569568704L;

	private final Logger LOG = LoggerFactory.getLogger(MainView.class);

	private static final String TITLE_DEFAULT = "WineCellar v2.0";

	private final H4 welcomeLabel = new H4();
	private final Label title;
	private final Label userLabel = new Label();

	@Autowired
	private CurrentUserProvider currentUser;

	public MainView() {

		UI.getCurrent().getPage().setTitle(TITLE_DEFAULT);

		setDrawerOpened(false);

		title = new Label(TITLE_DEFAULT);
		title.getStyle().set("flex-grow", "1");
		final Button search = new Button(VaadinIcon.SEARCH.create(), e -> UI.getCurrent().navigate(SearchView.class));
		search.addClassName("searchbutton");
		search.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY);

		userLabel.addClassName("userLabel");

		addToNavbar(true, new DrawerToggle(), title, userLabel, search);

		final Div welcome = new Div(welcomeLabel);
		welcome.addClassName("welcome");
		addToDrawer(welcome);

		addToDrawer(new DrawerComponent("My Cellar", VaadinIcon.OPEN_BOOK.create(),
				() -> UI.getCurrent().navigate(WineListView.class)));

		addToDrawer(new DrawerComponent("My Wishlists", VaadinIcon.STAR_O.create(),
				() -> UI.getCurrent().navigate(WishListsView.class)));

		addToDrawer(new DrawerComponent("Search for wine", VaadinIcon.SEARCH.create(),
				() -> UI.getCurrent().navigate(SearchView.class)));

		addToDrawer(new DrawerComponent("Add wine", VaadinIcon.PLUS_CIRCLE_O.create(),
				() -> UI.getCurrent().navigate(CreateNewView.class)));

		addToDrawer(new DrawerComponent("User info", VaadinIcon.USER.create(),
				() -> UI.getCurrent().navigate(UserInfoView.class)));

		addToDrawer(new DrawerComponent("About", VaadinIcon.INFO_CIRCLE_O.create(),
				() -> UI.getCurrent().navigate(AboutView.class)));

		addToDrawer(new DrawerComponent("Logout", VaadinIcon.SIGN_OUT.create(), this::logout));

	}

	@PostConstruct
	private void init() {
		final User user = currentUser.get();
		final String name = user.getName().split(" ")[0];
		userLabel.setText(String.format("Welcome back, %s!", name));
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

	public void logout() {
		LOG.info("Logging out user " + currentUser.get());
		UI.getCurrent().navigate("/logout");
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
