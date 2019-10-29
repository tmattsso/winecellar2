package org.thomas.winecellar.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.ApplicationScope;
import org.thomas.winecellar.data.NamedEntity;
import org.thomas.winecellar.data.Producer;
import org.thomas.winecellar.data.Wine;
import org.thomas.winecellar.data.WineList;
import org.thomas.winecellar.repo.ProducerRepo;
import org.thomas.winecellar.repo.WineListRepo;
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

	public List<Wine> search(final String searchTerms, String producerId, String type) {

		if (searchTerms == null) {
			return new ArrayList<>();
		}

		Long pId;
		if (producerId != null && !producerId.isEmpty()) {
			pId = Long.valueOf(producerId);
		} else {
			pId = null;
		}

		final List<Wine> wines = getWines();
		return wines.stream().filter(w -> w.getName() != null).filter(w -> {

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

			if (type == null || type.isEmpty()) {
				return true;
			}
			if (w.getType().name().equals(type.toUpperCase())) {
				return true;
			} else {
				return false;
			}
		}).filter(w -> {

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

	public List<String> getCountries() {
		return getWines().stream().map(w -> w.getCountry()).filter(s -> s != null).distinct().sorted()
				.collect(Collectors.toList());
	}

	public List<String> getRegions() {
		return getWines().stream().map(w -> w.getRegion()).filter(s -> s != null).distinct().sorted()
				.collect(Collectors.toList());
	}

	public List<String> getSubregions() {
		return getWines().stream().map(w -> w.getSubregion()).filter(s -> s != null).distinct().sorted()
				.collect(Collectors.toList());
	}

}
