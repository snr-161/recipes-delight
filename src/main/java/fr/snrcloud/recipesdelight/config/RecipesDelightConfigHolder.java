package fr.snrcloud.recipesdelight.config;

import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public final class RecipesDelightConfigHolder {
	private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("recipes-delight.toml");

	private static RecipesDelightConfig config;

	private RecipesDelightConfigHolder() {
	}

	public static void load() {
		config = TomlConfigIO.load(CONFIG_PATH);
	}

	public static RecipesDelightConfig get() {
		if (config == null) {
			load();
		}
		return config;
	}

	public static void save() {
		TomlConfigIO.save(CONFIG_PATH, get());
	}
}
