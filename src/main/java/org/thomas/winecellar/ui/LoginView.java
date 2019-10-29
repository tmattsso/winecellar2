package org.thomas.winecellar.ui;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.thomas.winecellar.service.CurrentUserProvider;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
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
			cup.setCurrentUserID(1L);
			UI.getCurrent().getPage().reload();
		});

		add(loginButton);
	}
}
