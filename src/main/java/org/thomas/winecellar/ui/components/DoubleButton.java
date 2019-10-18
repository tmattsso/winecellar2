package org.thomas.winecellar.ui.components;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.shared.Registration;

public class DoubleButton extends Button {

	private static final long serialVersionUID = -7269266927688889230L;

	private final Registration listener;

	private Icon secondIcon;

	public DoubleButton(String caption, Icon firstIcon, ComponentEventListener<ClickEvent<Button>> clickListener) {
		super(caption, firstIcon);

		listener = addClickListener(e -> confirm(clickListener));
	}

	public DoubleButton(Icon firstIcon, ComponentEventListener<ClickEvent<Button>> clickListener) {
		super(firstIcon);

		listener = addClickListener(e -> confirm(clickListener));
	}

	private void confirm(ComponentEventListener<ClickEvent<Button>> clickListener) {
		setIcon(secondIcon != null ? secondIcon : VaadinIcon.QUESTION.create());

		listener.remove();
		addClickListener(clickListener);
	}

	public void setSecondIcon(Icon icon) {
		secondIcon = icon;

	}
}
