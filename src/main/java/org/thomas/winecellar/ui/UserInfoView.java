package org.thomas.winecellar.ui;

import org.springframework.beans.factory.annotation.Autowired;
import org.thomas.winecellar.data.User;
import org.thomas.winecellar.service.CurrentUserProvider;
import org.thomas.winecellar.service.UserService;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.Route;

@Route(value = "userinfo", layout = MainView.class)
public class UserInfoView extends VerticalLayout implements HasViewTitle, AfterNavigationObserver {

	private static final long serialVersionUID = -395995087522715766L;

	@Autowired
	private UserService service;

	@Autowired
	private CurrentUserProvider cup;

	@Override
	public String getTitle() {
		return "User info";
	}

	@Override
	public void afterNavigation(AfterNavigationEvent event) {

		removeAll();

		final TextField nameField = new TextField("Name");
		final EmailField emailField = new EmailField("Email");

		final Binder<User> binder = new Binder<User>();
		binder.bind(nameField, User::getName, User::setName);
		binder.bind(emailField, User::getEmail, User::setEmail);

		binder.readBean(cup.get());

		final Button save = new Button("Update info", VaadinIcon.REFRESH.create(), e -> {
			final User user = cup.get();
			try {
				binder.writeBean(user);
				service.update(user);
				final Notification n = Notification.show("Info updated!");
				n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
				n.setPosition(Position.MIDDLE);
			} catch (final ValidationException e1) {
				e1.printStackTrace();
				final Notification error = Notification.show("Failed to save info", 3000, Position.MIDDLE);
				error.addThemeVariants(NotificationVariant.LUMO_ERROR);
			}
		});
		save.addThemeVariants(ButtonVariant.MATERIAL_CONTAINED);

		add(nameField, emailField, save);
	}

}
