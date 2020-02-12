package org.thomas.winecellar.ui;

import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.thomas.winecellar.data.Wine;
import org.thomas.winecellar.data.WineRating;
import org.thomas.winecellar.service.CurrentUserProvider;
import org.thomas.winecellar.service.WineService;
import org.thomas.winecellar.ui.components.RatingStars;
import org.thomas.winecellar.ui.components.RatingsPanel;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;

@Route(value = "rating", layout = MainView.class)
public class WineRatingView extends VerticalLayout implements HasUrlParameter<Long> {

	private static final long serialVersionUID = 5142173920542647473L;

	@Autowired
	private WineService service;

	@Autowired
	private CurrentUserProvider currentUser;

	private Wine wine;

	@Override
	public void setParameter(BeforeEvent event, Long parameter) {
		removeAll();

		wine = service.getById(parameter);

		if (wine == null) {
			return;
		}

		add(new H3(wine.getName()));

		final Button backButton = new Button("(back to wine details)",
				e -> getUI().get().navigate(WineDetailsView.class, wine.getId()));
		backButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
		add(backButton);

		final Button addReview = new Button();
		addReview.setText("Add a review");
		addReview.setIcon(VaadinIcon.USER_STAR.create());
		addReview.addClickListener(e -> openReviewDialog());
		addReview.addThemeVariants(ButtonVariant.MATERIAL_CONTAINED);
		addReview.addClassName("in-list-button");
		add(addReview);

		add(createSummary(wine));

		final List<WineRating> ratings = service.getRatings(parameter);

		final Div scroller = new Div();
		scroller.getStyle().set("overflow-y", "scroll");
		scroller.setWidthFull();
		add(scroller);

		setHeightFull();
		expand(scroller);

		final VerticalLayout reviews = new VerticalLayout();
		reviews.setPadding(false);
		reviews.setWidthFull();
		scroller.add(reviews);

		ratings.forEach(r -> {

			reviews.add(new RatingsPanel(r));
		});

	}

	private void openReviewDialog() {
		final Dialog d = new Dialog();
		d.getElement().setAttribute("theme", "fullwidth");
		add(d);
		d.open();

		final VerticalLayout vl = new VerticalLayout();
		vl.setPadding(false);
		d.add(vl);

		final RatingStars rating = new RatingStars();
		rating.setRequiredIndicatorVisible(true);
		vl.add(rating);

		final int min = 1900;
		final int max = LocalDate.now().get(ChronoField.YEAR);

		final NumberField vintage = new NumberField("Vintage:");
		vintage.setStep(1);
		vintage.setMin(min);
		vintage.setMax(max);
		vintage.setPlaceholder("2012");
		vintage.setWidthFull();
		vl.add(vintage);

		final TextArea comment = new TextArea("Optional comment:");
		comment.setWidthFull();
		vl.add(comment);

		final Button create = new Button("Add review", VaadinIcon.PLUS.create(), e -> {

			boolean fails = false;
			final Integer ratingVal = rating.getValue();
			if (ratingVal == null || ratingVal == 0) {
				rating.setErrorMessage("Please select a rating!");
				rating.setInvalid(true);
				fails = true;
			}

			final Integer vintageVal = vintage.getValue() == null ? null : vintage.getValue().intValue();
			if (vintageVal != null) {

				if (vintageVal <= min || vintageVal >= max) {
					vintage.setErrorMessage("Value must be between " + min + " and " + max + " (or empty)");
					fails = true;
				}
			}

			if (fails) {
				return;
			}

			String value = comment.getValue();
			if (value.isEmpty()) {
				value = null;
			}
			service.addRating(currentUser.get(), wine, ratingVal, vintageVal, value);

			d.close();
			remove(d);

			setParameter(null, wine.getId());
		});
		create.setWidthFull();
		create.addThemeVariants(ButtonVariant.MATERIAL_CONTAINED);
		vl.add(create);
	}

	public static HorizontalLayout createSummary(Wine wine) {

		final HorizontalLayout ratingsLayout = new HorizontalLayout();
		ratingsLayout.setWidthFull();
		ratingsLayout.setPadding(false);

		if (wine.getRating() != null) {

			final int fullStars = wine.getRating().intValue();
			for (int i = 0; i < fullStars; i++) {
				ratingsLayout.add(VaadinIcon.STAR.create());
			}
			final double rest = wine.getRating() - fullStars;
			if (rest > 0.75) {
				ratingsLayout.add(VaadinIcon.STAR.create());
			} else if (rest > 0.25) {
				ratingsLayout.add(VaadinIcon.STAR_HALF_LEFT.create());
			}
		}

		if (wine.getNumRatings() == 0) {
			final Span beFirstToRateLabel = new Span("Be the first to rate this wine!");
			beFirstToRateLabel.getStyle().set("align-self", "flex-end");
			ratingsLayout.add(beFirstToRateLabel);
		} else {

			final Span bolded = new Span(String.format("%.2f", wine.getRating()));
			bolded.getStyle().set("align-self", "flex-end");
			bolded.getStyle().set("font-weigth", "bolder");
			ratingsLayout.add(bolded);

			final Span ratingLabel = new Span(String.format(" (%d reviews)", wine.getNumRatings()));
			ratingLabel.getStyle().set("align-self", "flex-end");
			ratingsLayout.add(ratingLabel);
		}

		return ratingsLayout;
	}

}
