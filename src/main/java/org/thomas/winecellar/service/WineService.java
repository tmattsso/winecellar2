package org.thomas.winecellar.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.ApplicationScope;
import org.thomas.winecellar.data.NamedEntity;
import org.thomas.winecellar.data.Producer;
import org.thomas.winecellar.data.User;
import org.thomas.winecellar.data.Wine;
import org.thomas.winecellar.data.WineList;
import org.thomas.winecellar.data.WineRating;
import org.thomas.winecellar.repo.ProducerRepo;
import org.thomas.winecellar.repo.WineListRepo;
import org.thomas.winecellar.repo.WineRatingRepo;
import org.thomas.winecellar.repo.WineRepository;

@Service
@ApplicationScope
public class WineService {

	@Autowired
	private WineRepository repo;

	@Autowired
	private WineListRepo listRepo;

	@Autowired
	private ProducerRepo producers;

	@Autowired
	private WineRatingRepo ratings;

	private List<Wine> CACHE;

	public List<Wine> getWines() {

		if (CACHE != null) {
			return CACHE;
		}

		CACHE = new ArrayList<>();
		CACHE.addAll((Collection<? extends Wine>) repo.findAll());
		return CACHE;
	}

	public Wine addWine(Wine toAdd) {

		CACHE = null;

		return repo.save(toAdd);
	}

	public List<Wine> getByProducer(Producer p) {

		if (p == null) {
			return new ArrayList<>();
		}

		return repo.findByProducer(p);
	}

	public List<Wine> search(final String searchTerms, String producerId, String type, String grapefilter) {

		if (searchTerms == null) {
			return new ArrayList<>();
		}

		Long pId;
		if (producerId != null && !producerId.isEmpty()) {
			pId = Long.valueOf(producerId);
		} else {
			pId = null;
		}

		final Collection<String> grapes = new HashSet<>();
		if (grapefilter != null) {
			final String[] grape = grapefilter.split(",");
			for (final String g : grape) {
				grapes.add(g.trim());
			}
		}

		final List<Wine> wines = getWines();
		return wines.stream().filter(w -> w.getName() != null).filter(w -> {

			// Producer ID

			if (pId == null) {
				return true;
			}
			if (w.getProducer() == null) {
				return true;
			} else if (w.getProducer() != null && w.getProducer().getId() == pId) {
				return true;
			} else {
				return false;
			}
		}).filter(w -> {

			// Wine Type

			if (type == null || type.isEmpty()) {
				return true;
			}
			if (w.getType().name().equals(type.toUpperCase())) {
				return true;
			} else {
				return false;
			}
		}).filter(w -> {

			// Grapes

			if (grapes.isEmpty()) {
				return true;
			}

			return w.getGrapes().stream().anyMatch(g -> grapes.contains(g));

		}).filter(w -> {

			// Text Search

			final boolean nameMatch = match(searchTerms, w.getName());
			final boolean producerMatch = match(searchTerms, w.getProducer());
			final boolean countryMatch = match(searchTerms, w.getCountry());
			final boolean regionMatch = match(searchTerms, w.getRegion());
			final boolean subregionMatch = match(searchTerms, w.getSubregion());
			final boolean grapeMatch = match(searchTerms, w.getGrapes());

			return nameMatch || producerMatch || countryMatch || regionMatch || subregionMatch || grapeMatch;

		}).sorted().collect(Collectors.toList());
	}

	private static boolean match(String searchTerm, String compare) {
		if (searchTerm == null || compare == null) {
			return false;
		}

		compare = compare.toLowerCase();
		searchTerm = searchTerm.toLowerCase();

		compare = StringUtils.stripAccents(compare);

		return compare.contains(searchTerm);
	}

	private static boolean match(String searchTerm, NamedEntity compare) {
		if (compare == null) {
			return false;
		}
		return match(searchTerm, compare.getName());
	}

	private static boolean match(String searchTerm, Collection<?> compare) {
		if (compare == null) {
			return false;
		}
		return match(searchTerm, compare.toString());
	}

	public List<Producer> getProducers() {
		return (List<Producer>) producers.findAll();
	}

	public Wine getById(Long id) {
		return repo.findById(id).orElse(null);
	}

	public Producer getProducerById(long id) {
		return producers.findById(id).orElse(null);

	}

	public int modifyListAmount(WineList selectedList, Wine wine, int delta) {
		if (selectedList == null || wine == null) {
			return 0;
		}
		final Map<Wine, Integer> map = selectedList.getWines();
		int existing = 0;

		if (map.containsKey(wine)) {
			existing = map.get(wine);
		}

		existing += delta;
		existing = Math.max(existing, 0);

		selectedList.getWines().put(wine, existing);

		listRepo.save(selectedList);

		return existing;
	}

	public void removeFromList(WineList selectedList, Wine wine) {
		if (selectedList == null || wine == null) {
			return;
		}
		selectedList.getWines().remove(wine);
		listRepo.save(selectedList);
	}

	/**
	 * @return Map Country -> Regions -> Subregions
	 */
	public Map<String, Map<String, Set<String>>> getCountries() {
		final Map<String, Map<String, Set<String>>> countryToRegion = new HashMap<>();

		for (final Wine w : getWines()) {

			if (w.getCountry() != null) {

				if (!countryToRegion.containsKey(w.getCountry())) {
					countryToRegion.put(w.getCountry(), new HashMap<>());
				}
				final Map<String, Set<String>> regionToSubRegion = countryToRegion.get(w.getCountry());

				if (w.getRegion() != null) {
					if (!regionToSubRegion.containsKey(w.getRegion())) {
						regionToSubRegion.put(w.getRegion(), new HashSet<>());
					}

					if (w.getSubregion() != null) {
						regionToSubRegion.get(w.getRegion()).add(w.getSubregion());
					}
				}

			}
		}

		return countryToRegion;
	}

	@Transactional
	public void addRating(User user, Wine wine, int rating, Integer vintage, String comment) {

		wine = repo.findById(wine.getId()).get();

		final WineRating wr = new WineRating();
		wr.setWine(wine);
		wr.setRating(rating);
		wr.setVintage(vintage);
		wr.setComment(comment);

		wr.setUsername(user.getName());
		wr.setReviewTime(new Date());

		final List<WineRating> oldRatings = getRatings(wine);
		final int sum = oldRatings.stream().mapToInt(r -> r.getRating()).sum();
		final double newTotal = (sum + rating) / (oldRatings.size() + 1d);
		wine.setRating(newTotal);
		wine.setNumRatings(oldRatings.size() + 1);

		repo.save(wine);
		ratings.save(wr);
	}

	@Transactional
	public List<WineRating> getRatings(Wine wine) {

		final List<WineRating> findByWine = ratings.findByWine(wine);
		Collections.sort(findByWine);
		return findByWine;
	}

	public List<WineRating> getRatings(Long parameter) {
		return getRatings(repo.findById(parameter).get());
	}

	public LinkedHashMap<String, Integer> getGrapePopularity() {
		final LinkedHashMap<String, Integer> map = new LinkedHashMap<>();

		getWines().forEach(w -> w.getGrapes().forEach(grape -> {

			if (!map.containsKey(grape)) {
				map.put(grape, 0);
			}
			map.put(grape, map.get(grape) + 1);
		}));

		return map;
	}

	public List<String> getGrapesByPopularity() {
		final LinkedHashMap<String, Integer> map = getGrapePopularity();
		final List<String> top10 = map.keySet().stream().sorted((s1, s2) -> map.get(s2).compareTo(map.get(s1))).limit(8)
				.collect(Collectors.toList());

		top10.forEach(s -> map.remove(s));
		final List<String> rest = map.keySet().stream().sorted().collect(Collectors.toList());

		top10.addAll(rest);
		return top10;

	}

	public List<String> getGrapes() {
		return getWines().stream().flatMap(w -> w.getGrapes().stream()).distinct().sorted()
				.collect(Collectors.toList());
	}
}
