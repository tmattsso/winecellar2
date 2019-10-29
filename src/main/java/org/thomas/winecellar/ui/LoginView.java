package org.thomas.winecellar.ui;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.thomas.winecellar.data.User;
import org.thomas.winecellar.service.CurrentUserProvider;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.Route;

@Route("login")
public class LoginView extends Div {

	private static final long serialVersionUID = -1724824498184786980L;

	@Autowired
	private CurrentUserProvider cup;

	@PostConstruct
	public void init() {
		final Button loginButton = new Button("login");

		loginButton.addClickListener(e -> {
			final User u = cup.loginByEmail("thomas@gmail.com");

			if (u != null) {
				UI.getCurrent().getPage().reload();
			} else {
				noUserMaybeCreate();
			}
		});

		add(loginButton);
	}

	private void noUserMaybeCreate() {
		Notification.show("No user");
	}

	@Override
	protected void onAttach(AttachEvent attachEvent) {
		super.onAttach(attachEvent);

		// Auto-login check
	}
}
