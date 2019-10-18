package org.thomas.winecellar.ui.components;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;

@CssImport(value = "./styles/drawer-buttons.css", themeFor = "vaadin-button")
public class DrawerComponent extends Div {

	private static final long serialVersionUID = -3456419228724532871L;

	public DrawerComponent(String title, Icon icon, Runnable clickHandler) {

		setWidth("100%");

		final Button button = new Button(title, icon, e -> clickHandler.run());
		button.addClassName("drawerbutton");
		button.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
		button.setWidth("100%");
		add(button);
	}
}
