package org.thomas.winecellar.ui.components;

import java.text.DateFormat;

import org.thomas.winecellar.data.WineRating;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;

@CssImport("./styles/ratingspanel.css")
public class RatingsPanel extends Div {

	private static final long serialVersionUID = 1378961279336053470L;

	public RatingsPanel(WineRating rating) {

		setWidthFull();
		addClassName("ratingspanel");

		final Div stars = new Div();
		stars.addClassName("stars");
		add(stars);
		for (int i = 0; i < rating.getRating(); i++) {
			final Icon star = VaadinIcon.STAR.create();
			star.setSize("16px");
			stars.add(star);
		}

		final String vintageString = String.format("(%s)",
				rating.getVintage() == null ? "No vintage" : rating.getVintage().toString());
		final Span vintage = new Span(vintageString);
		vintage.addClassName("vintage");
		stars.add(vintage);

		final Div comment = new Div();
		comment.addClassName("comment");
		comment.setText(rating.getComment());
		add(comment);

		final Div signed = new Div();
		signed.addClassName("stars");
		add(signed);

		final Span author = new Span(rating.getUsername());
		author.addClassName("author");
		signed.add(author);

		final DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM);
		final Span timestamp = new Span(df.format(rating.getReviewTime()));
		timestamp.addClassName("timestamp");
		signed.add(timestamp);
	}
}
