package fr.snrcloud.recipesdelight.client.cache;

import fr.snrcloud.recipesdelight.network.NearbyContainerContentsPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public final class NearbyContainerClientCache {
	private static @Nullable BlockPos pos;
	private static Map<Item, Integer> counts = Map.of();

	private NearbyContainerClientCache() {
	}

	public static void update(NearbyContainerContentsPayload payload) {
		pos = payload.pos();

		if (payload.entries().isEmpty()) {
			counts = Map.of();
			return;
		}

		Map<Item, Integer> newCounts = new HashMap<>();
		for (NearbyContainerContentsPayload.Entry entry : payload.entries()) {
			Item item = BuiltInRegistries.ITEM.getValue(entry.item());
			if (item != null) {
				newCounts.put(item, entry.count());
			}
		}
		counts = newCounts;
	}

	public static Map<Item, Integer> getCounts() {
		return counts;
	}

	public static @Nullable BlockPos getPos() {
		return pos;
	}
}
