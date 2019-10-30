package org.thomas.winecellar.ui.components;

import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;

@CssImport(value = "./styles/ratingstars.css", themeFor = "vaadin-custom-field")
public class RatingStars extends CustomField<Integer> {

	private static final long serialVersionUID = -722491256546744730L;

	private int value = 0;

	private final Div container;

	public RatingStars() {

		getElement().setAttribute("class", "ratingstars");

		setLabel("Rating:");
		container = new Div();
		container.setWidthFull();
		container.addClassName("wrapper");

		add(container);
		setWidthFull();

		render();
	}

	private void render() {
		container.removeAll();

		for (int i = 0; i < 5; i++) {

			Icon icon = null;
			if (i < value) {
				icon = VaadinIcon.STAR.create();
			} else {
				icon = VaadinIcon.STAR_O.create();
			}
			icon.setSize("48px");
			container.add(icon);

			final int value = i + 1;

			icon.addClickListener(e -> setValue(value));
		}
	}

	@Override
	protected Integer generateModelValue() {
		return value;
	}

	@Override
	protected void setPresentationValue(Integer newValue) {
		value = newValue;
		setInvalid(false);
		render();
	}

}
