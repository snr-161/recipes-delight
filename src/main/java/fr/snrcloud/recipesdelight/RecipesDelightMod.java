package fr.snrcloud.recipesdelight;

import fr.snrcloud.recipesdelight.config.RecipesDelightConfigHolder;
import fr.snrcloud.recipesdelight.integration.ecologics.EcologicsCompat;
import fr.snrcloud.recipesdelight.network.RecipesDelightNetworking;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RecipesDelightMod implements ModInitializer {
	public static final String MOD_ID = "recipesdelight";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		RecipesDelightConfigHolder.load();
		RecipesDelightNetworking.register();

		if (FabricLoader.getInstance().isModLoaded("ecologics")) {
			EcologicsCompat.init();
		}
	}
}
