package org.thomas.winecellar.ui;

import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route(value = "about", layout = MainView.class)
public class AboutView extends VerticalLayout {

	private static final long serialVersionUID = -3913509714364345226L;

	public AboutView() {

		add(new Label("WineCellar 2.0, provided by Vaadin Platform 14"));
	}

}
