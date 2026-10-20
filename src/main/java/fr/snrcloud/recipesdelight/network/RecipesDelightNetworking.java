package fr.snrcloud.recipesdelight.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public final class RecipesDelightNetworking {
	private RecipesDelightNetworking() {
	}

	public static void register() {
		PayloadTypeRegistry.clientboundPlay().register(NearbyContainerContentsPayload.TYPE, NearbyContainerContentsPayload.STREAM_CODEC);
	}
}
