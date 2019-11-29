package org.thomas.winecellar.ui.components;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.thomas.winecellar.service.WineService;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.checkbox.CheckboxGroupVariant;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

/**
 * TODO: consider changing to Grid for filtering (and lazy laoding)
 */
@CssImport(value = "./styles/search-checkboxgroup.css", themeFor = "vaadin-checkbox-group")
public class GrapeSearchFilter extends CustomField<Set<String>> {

	private static final long serialVersionUID = 4909513026822001150L;

	private final Div container;
	private final List<String> allGrapes;
	private Set<String> selected = new HashSet<>();

	public GrapeSearchFilter(WineService service) {

		allGrapes = service.getGrapesByPopularity();

		getElement().setAttribute("class", "grapesearchfilter");

		setLabel("Filter grapes:");
		container = new Div();
		container.setWidthFull();
		container.addClassName("wrapper");

		add(container);
		setWidthFull();

		render();
	}

	private void render() {
		container.removeAll();

		final String text = selected.isEmpty() ? "No filter" : parseSelectionString();
		final Button button = new Button(text);
		container.add(button);

		button.addClickListener(e -> openPopup());
	}

	private void openPopup() {

		final VerticalLayout vl = new VerticalLayout();
		vl.setPadding(false);
		vl.setHeight("80%");

		final Dialog d = new Dialog(vl);
		d.setWidth("100%");
		d.getElement().setAttribute("theme", "fullwidth");
		d.open();

		final Span label = new Span("Select grapes to search for:");
		vl.add(label);

		final Button clear = new Button(String.format("Clear selection (%d selected)", selected.size()));
		clear.addThemeVariants(ButtonVariant.MATERIAL_OUTLINED);
		clear.setIcon(VaadinIcon.CLOSE.create());
		clear.getElement().getStyle().set("margin-left", "auto");
		clear.setEnabled(selected.size() > 0);

		final CheckboxGroup<String> group = new CheckboxGroup<>();
		group.addClassName("grapesSelector");
		group.setItems(allGrapes);
		group.setValue(selected);
		group.addThemeVariants(CheckboxGroupVariant.MATERIAL_VERTICAL);

		group.addValueChangeListener(e -> {
			final int size = group.getSelectedItems().size();
			clear.setEnabled(size > 0);
			clear.setText(String.format("Clear selection (%d selected)", size));
		});
		clear.addClickListener(e -> {
			group.setValue(new HashSet<>());
		});
		vl.add(clear);

		final VerticalLayout scrollWrapper = new VerticalLayout(group);
		scrollWrapper.setPadding(false);
		scrollWrapper.getStyle().set("overflow-y", "auto");
		scrollWrapper.getStyle().set("flex-grow", "1");
		scrollWrapper.getStyle().set("max-height", "325px");
		scrollWrapper.setWidth("100%");
		vl.add(scrollWrapper);
		vl.setFlexGrow(1, scrollWrapper);

		final Button ok = new Button("Apply", VaadinIcon.CHECK.create());
		ok.addThemeVariants(ButtonVariant.MATERIAL_CONTAINED);

		final HorizontalLayout hl = new HorizontalLayout(ok, clear);
		hl.setWidthFull();
		vl.add(hl);

		ok.addClickListener(e -> {
			selected = group.getValue();
			updateValue();
			d.close();
			render();
		});
	}

	private String parseSelectionString() {
		String text = selected.stream().limit(4).collect(Collectors.joining(", "));

		if (text.length() > 30) {
			text = text.substring(0, 30) + "...";
		}

		return text;
	}

	@Override
	protected Set<String> generateModelValue() {
		return selected;
	}

	@Override
	protected void setPresentationValue(Set<String> newPresentationValue) {
		selected = newPresentationValue;
		render();
	}

	@Override
	public void clear() {
		setPresentationValue(new HashSet<>());
	}

}
