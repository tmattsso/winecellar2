package org.thomas.winecellar.ui;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.thomas.winecellar.data.User;
import org.thomas.winecellar.service.CurrentUserProvider;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.PendingJavaScriptResult;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.router.Route;

@Route("login")
public class LoginView extends VerticalLayout {

	private static final long serialVersionUID = -1724824498184786980L;

	private static final String LOCAL_STORAGE_ID = "WINECELLAR_UUID";
	private static final String LOCAL_STORAGE_GET = "return localStorage.getItem('" + LOCAL_STORAGE_ID + "');";
	private static final String LOCAL_STORAGE_INSERT = "localStorage.setItem('" + LOCAL_STORAGE_ID + "',$0);";
	private static final String LOCAL_STORAGE_REMOVE = "localStorage.removeItem('" + LOCAL_STORAGE_ID + "');";

	private final Logger LOG = LoggerFactory.getLogger(LoginView.class);

	@Autowired
	private CurrentUserProvider cup;

	@PostConstruct
	public void init() {

		final EmailField email = new EmailField("Whooo are you");
		email.setValue("thomas@gmail.com");

		final Button loginButton = new Button("login");
		loginButton.addClickListener(e -> login(email));
		loginButton.addClickShortcut(Key.ENTER);
		loginButton.addThemeVariants(ButtonVariant.MATERIAL_CONTAINED);

		add(email, loginButton);
	}

	private void login(EmailField email) {
		final User u = cup.loginByEmail(email.getValue());

		if (u != null) {

			LOG.info("Email login success, storing UUID: " + u.toString());

			// success, store UUID for next time
			System.out.println(String.format(LOCAL_STORAGE_INSERT, u.getUuid()));
			final PendingJavaScriptResult result = UI.getCurrent().getPage().executeJs(LOCAL_STORAGE_INSERT,
					u.getUuid());
			result.then(json -> UI.getCurrent().getPage().reload());
		} else {
			noUserMaybeCreate();
		}
	}

	private void noUserMaybeCreate() {
		Notification.show("No user");
	}

	@Override
	protected void onAttach(AttachEvent attachEvent) {
		super.onAttach(attachEvent);

		// Auto-login check
		final PendingJavaScriptResult result = attachEvent.getUI().getPage().executeJs(LOCAL_STORAGE_GET);
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
					UI.getCurrent().getPage().reload();
				} else {

					// there was something in storage, but it wasn't valid; clear it
					LOG.info("Received invalid logging string " + jsonString + ", removing");
					attachEvent.getUI().getPage().executeJs(LOCAL_STORAGE_REMOVE);
				}
			}

		});
	}
}