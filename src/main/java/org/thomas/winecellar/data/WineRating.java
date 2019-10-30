package org.thomas.winecellar.data;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Entity
public class WineRating extends AbstractEntity implements Comparable<WineRating> {

	@ManyToOne(fetch = FetchType.LAZY)
	@NotNull
	private Wine wine;

	private Integer vintage;

	@NotNull
	@Min(0)
	@Max(5)
	private int rating;

	private String comment;

	private String username;

	@Temporal(TemporalType.TIMESTAMP)
	@NotNull
	private Date reviewTime;

	public Date getReviewTime() {
		return reviewTime;
	}

	public void setReviewTime(Date reviewTime) {
		this.reviewTime = reviewTime;
	}

	public Wine getWine() {
		return wine;
	}

	public void setWine(Wine wine) {
		this.wine = wine;
	}

	public Integer getVintage() {
		return vintage;
	}

	public void setVintage(Integer vintage) {
		this.vintage = vintage;
	}

	public int getRating() {
		return rating;
	}

	public void setRating(int rating) {
		this.rating = rating;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	@Override
	public int compareTo(WineRating o) {
		return o.reviewTime.compareTo(reviewTime);
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

}
