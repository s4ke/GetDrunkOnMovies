package de.fsmpi.drunkserver.model;

import com.google.common.collect.ImmutableSortedMap;

public class Movie {

	private ImmutableSortedMap<String, Integer> drink;
	private String name;

	public ImmutableSortedMap<String, Integer> getDrink() {
		return drink;
	}

	public void setDrink(ImmutableSortedMap<String, Integer> drink) {
		this.drink = drink;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Movie [drink=").append(this.drink).append(", name=")
				.append(this.name).append("]");
		return builder.toString();
	}

}
