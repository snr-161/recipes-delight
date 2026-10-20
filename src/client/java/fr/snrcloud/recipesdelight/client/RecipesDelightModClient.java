package fr.snrcloud.recipesdelight.client;

import fr.snrcloud.recipesdelight.client.cache.NearbyContainerClientCache;
import fr.snrcloud.recipesdelight.network.NearbyContainerContentsPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;

public class RecipesDelightModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientPlayNetworking.registerGlobalReceiver(NearbyContainerContentsPayload.TYPE,
				(payload, context) -> Minecraft.getInstance().execute(() -> NearbyContainerClientCache.update(payload)));
	}
}
