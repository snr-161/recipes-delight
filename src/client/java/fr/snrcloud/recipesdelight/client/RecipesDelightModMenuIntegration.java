package fr.snrcloud.recipesdelight.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import fr.snrcloud.recipesdelight.client.gui.RecipesDelightConfigScreen;

public class RecipesDelightModMenuIntegration implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return RecipesDelightConfigScreen::create;
	}
}
