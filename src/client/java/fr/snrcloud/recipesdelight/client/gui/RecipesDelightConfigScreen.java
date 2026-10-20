package fr.snrcloud.recipesdelight.client.gui;

import fr.snrcloud.recipesdelight.config.RecipesDelightConfig;
import fr.snrcloud.recipesdelight.config.RecipesDelightConfigHolder;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class RecipesDelightConfigScreen {
	private RecipesDelightConfigScreen() {
	}

	public static Screen create(Screen parent) {
		RecipesDelightConfig config = RecipesDelightConfigHolder.get();

		ConfigBuilder builder = ConfigBuilder.create()
				.setParentScreen(parent)
				.setTitle(Component.translatable("config.recipesdelight.title"))
				.setSavingRunnable(RecipesDelightConfigHolder::save);

		ConfigCategory category = builder.getOrCreateCategory(Component.translatable("config.recipesdelight.category.general"));
		ConfigEntryBuilder entryBuilder = builder.entryBuilder();

		category.addEntry(entryBuilder.startIntSlider(
						Component.translatable("config.recipesdelight.radius"),
						config.getRadius(),
						RecipesDelightConfig.MIN_RADIUS,
						RecipesDelightConfig.MAX_RADIUS)
				.setDefaultValue(RecipesDelightConfig.DEFAULT_RADIUS)
				.setTooltip(Component.translatable("config.recipesdelight.radius.tooltip"))
				.setSaveConsumer(config::setRadius)
				.build());

		return builder.build();
	}
}
