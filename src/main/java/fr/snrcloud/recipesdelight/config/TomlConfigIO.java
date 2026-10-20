package fr.snrcloud.recipesdelight.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class TomlConfigIO {
	private static final Logger LOGGER = LoggerFactory.getLogger("recipesdelight");
	private static final String RADIUS_KEY = "radius";

	private TomlConfigIO() {
	}

	public static RecipesDelightConfig load(Path path) {
		RecipesDelightConfig config = new RecipesDelightConfig();

		if (!Files.exists(path)) {
			save(path, config);
			return config;
		}

		try {
			List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
			for (String line : lines) {
				String trimmed = line.trim();
				if (trimmed.isEmpty() || trimmed.startsWith("#")) {
					continue;
				}

				int equalsIndex = trimmed.indexOf('=');
				if (equalsIndex < 0) {
					continue;
				}

				String key = trimmed.substring(0, equalsIndex).trim();
				String value = trimmed.substring(equalsIndex + 1).trim();

				if (key.equals(RADIUS_KEY)) {
					try {
						config.setRadius(Integer.parseInt(value));
					} catch (NumberFormatException e) {
						LOGGER.warn("Invalid value for '{}' in {}: '{}', keeping default", RADIUS_KEY, path, value);
					}
				}
			}
		} catch (IOException e) {
			LOGGER.warn("Could not read {}, using defaults", path, e);
		}

		return config;
	}

	public static void save(Path path, RecipesDelightConfig config) {
		String content = """
				# Recipes Delight configuration
				# Radius (in blocks)
				radius = %d
				""".formatted(config.getRadius());

		try {
			Path parent = path.getParent();
			if (parent != null) {
				Files.createDirectories(parent);
			}
			Files.writeString(path, content, StandardCharsets.UTF_8);
		} catch (IOException e) {
			LOGGER.warn("Could not write {}", path, e);
		}
	}
}
