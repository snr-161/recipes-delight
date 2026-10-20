package fr.snrcloud.recipesdelight.config;

public class RecipesDelightConfig {
	public static final int MIN_RADIUS = 1;
	public static final int MAX_RADIUS = 8;
	public static final int DEFAULT_RADIUS = 4;

	private int radius = DEFAULT_RADIUS;

	public int getRadius() {
		return radius;
	}

	public void setRadius(int radius) {
		this.radius = clamp(radius);
	}

	public static int clamp(int radius) {
		return Math.clamp(radius, MIN_RADIUS, MAX_RADIUS);
	}
}
