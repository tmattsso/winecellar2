package org.thomas.winecellar.ui;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.thomas.winecellar.data.User;
import org.thomas.winecellar.service.CurrentUserProvider;
import org.thomas.winecellar.service.UserService;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.PendingJavaScriptResult;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.Route;

@Route("login")
public class LoginView extends VerticalLayout implements AfterNavigationObserver {

	private static final long serialVersionUID = -1724824498184786980L;

	private static final String LOCAL_STORAGE_ID = "WINECELLAR_UUID";
	private static final String LOCAL_STORAGE_GET = "return localStorage.getItem('" + LOCAL_STORAGE_ID + "');";
	private static final String LOCAL_STORAGE_INSERT = "localStorage.setItem('" + LOCAL_STORAGE_ID + "',$0);";
	public static final String LOCAL_STORAGE_REMOVE = "localStorage.removeItem('" + LOCAL_STORAGE_ID + "');";

	private final Logger LOG = LoggerFactory.getLogger(LoginView.class);

	@Autowired
	private CurrentUserProvider cup;

	@Autowired
	private UserService service;

	@PostConstruct
	public void init() {

		final EmailField email = new EmailField("Whooo are you");
		email.setValue("thomas@gmail.com");

		final Button loginButton = new Button("login");
		loginButton.addClickListener(e -> login(email.getValue()));
		loginButton.addClickShortcut(Key.ENTER);
		loginButton.addThemeVariants(ButtonVariant.MATERIAL_CONTAINED);

		add(email, loginButton);
	}

	private void login(String email) {
		final User u = cup.loginByEmail(email);

		if (u != null) {

			LOG.info("Email login success, storing UUID: " + u.toString());

			// success, store UUID for next time
			final PendingJavaScriptResult result = UI.getCurrent().getPage().executeJs(LOCAL_STORAGE_INSERT,
					u.getUuid());
			result.then(json -> UI.getCurrent().getPage().reload());
		} else {
			noUserMaybeCreate(email);
		}
	}

	private void noUserMaybeCreate(String email) {

		final VerticalLayout vl = new VerticalLayout();
		final Dialog d = new Dialog(vl);
		d.setWidth("300px");

		final Label l = new Label("No user with email '" + email + "' found, create new user?");

		final Button ok = new Button("Yes please!", VaadinIcon.PLUS.create(), e -> {

			vl.removeAll();

			final Label label = new Label("Please enter your name");
			final TextField nameField = new TextField();
			nameField.setWidthFull();

			final Button create = new Button("Finish signup", VaadinIcon.CHECK.create(), ev -> {
				if (!nameField.getValue().isEmpty()) {
					createUser(nameField.getValue(), email);
				} else {
					Notification.show("Please enter a name");
				}
			});
			create.setWidthFull();
			create.addThemeVariants(ButtonVariant.MATERIAL_CONTAINED);
			vl.add(label, nameField, create);
			nameField.focus();

		});
		ok.setWidthFull();
		ok.addThemeVariants(ButtonVariant.MATERIAL_CONTAINED);

		final Button cancel = new Button("No thanks.", e -> {
			d.close();
			remove(d);
		});
		cancel.setWidthFull();
		cancel.addThemeVariants(ButtonVariant.MATERIAL_OUTLINED);

		vl.add(l, ok, cancel);
		add(d);
		d.open();

	}

	private void createUser(String name, String email) {
		service.addUser(name, email);
		login(email);
	}

	@Override
	public void afterNavigation(AfterNavigationEvent event) {

		// Auto-login check
		final PendingJavaScriptResult result = UI.getCurrent().getPage().executeJs(LOCAL_STORAGE_GET);
		result.then(json -> {
			final String jsonString = json.asString();
			if ("null".equals(jsonString)) {
				// no local storage, fine
				LOG.info("no login info stored");
			} else {

				// try login based on UUID
				final User u = cup.loginByUuid(jsonString);

				if (u != null) {
					LOG.info("Autologin success for " + u.toString());

					final String path = event.getLocation().getPath();
					if (path.contains("login")) {
						// redirect
						UI.getCurrent().navigate(WineListView.class);
					} else {
						UI.getCurrent().getPage().reload();
					}
				} else {

					// there was something in storage, but it wasn't valid; clear it
					LOG.info("Received invalid logging string " + jsonString + ", removing");
					UI.getCurrent().getPage().executeJs(LOCAL_STORAGE_REMOVE);
				}
			}

		});
	}

}
