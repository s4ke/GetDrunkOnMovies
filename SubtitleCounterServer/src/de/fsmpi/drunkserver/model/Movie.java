package de.fsmpi.drunkserver.model;

import java.util.Map;

public class Movie {

	private Map<String, Integer> drink;
	private String name;

	public Map<String, Integer> getDrink() {
		return drink;
	}

	public void setDrink(Map<String, Integer> drink) {
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
		builder.append("Movie [drink=").append(drink).append(", name=")
				.append(name).append("]");
		return builder.toString();
	}

}
