package org.thomas.winecellar.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.ApplicationScope;
import org.thomas.winecellar.data.Producer;
import org.thomas.winecellar.data.Wine;
import org.thomas.winecellar.data.WineList;

@Service
@ApplicationScope
public class WineService {

	private final List<Wine> wines = new ArrayList<>();

	public List<Wine> getWines() {
		return Collections.unmodifiableList(wines);
	}

	public void addWine(Wine toAdd) {
		synchronized (wines) {
			wines.add(toAdd);
		}
	}

	public List<Wine> getByProducer(Producer p) {

		if (p == null) {
			return new ArrayList<>();
		}

		return wines.stream().filter(w -> p.equals(w.getProducer())).sorted().collect(Collectors.toList());
	}

	public List<Wine> search(String searchTerms, String producerId, String type) {

		if (searchTerms == null) {
			return new ArrayList<>();
		}
		final String lowercasename = searchTerms.toLowerCase();

		Long pId;
		if (producerId != null && !producerId.isEmpty()) {
			pId = Long.valueOf(producerId);
		} else {
			pId = null;
		}

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

			final boolean nameMatch = w.getName().toLowerCase().contains(lowercasename);
			final boolean producerMatch = w.getProducer() != null
					&& w.getProducer().getName().toLowerCase().contains(lowercasename);

			return nameMatch || producerMatch;
		}).sorted().collect(Collectors.toList());
	}

	public List<Producer> getProducers() {

		return wines.stream().map(w -> w.getProducer()).filter(p -> p != null).distinct().sorted()
				.collect(Collectors.toList());
	}

	public Wine getById(Long id) {
		return wines.stream().filter(w -> w.getId() == id).findFirst().orElse(null);
	}

	public Producer getProducerById(long id) {
		return wines.stream().map(w -> w.getProducer()).filter(p -> p != null && p.getId() == id).findFirst()
				.orElse(null);

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
		return existing;
	}

	public void removeFromList(WineList selectedList, Wine wine) {
		if (selectedList == null || wine == null) {
			return;
		}
		selectedList.getWines().remove(wine);
	}

}
