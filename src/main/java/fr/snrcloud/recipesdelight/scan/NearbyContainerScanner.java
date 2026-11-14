package fr.snrcloud.recipesdelight.scan;

import fr.snrcloud.recipesdelight.scan.compat.StorageDrawersCompat;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Scans a cube of the given radius around a position for block entities implementing {@link Container}
 * (chests, barrels, shulker boxes, hoppers, droppers/dispensers, furnaces, ...) and aggregates their
 * contents by item, ignoring stack-distinguishing NBT - consistent with how the vanilla recipe book
 * accounts for "craftable" ingredients.
 *
 * Also recognizes Storage Drawers' drawers via {@link StorageDrawersCompat}, since those never
 * implement vanilla {@link Container} at all.
 */
public final class NearbyContainerScanner {
	private static final boolean STORAGE_DRAWERS_LOADED = FabricLoader.getInstance().isModLoaded("storagedrawers");

	private NearbyContainerScanner() {
	}

	public static Map<Item, Integer> scan(ServerLevel level, BlockPos origin, int radius, BlockEntity exclude) {
		Map<Item, Integer> counts = new HashMap<>();

		BlockPos min = origin.offset(-radius, -radius, -radius);
		BlockPos max = origin.offset(radius, radius, radius);

		for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
			BlockEntity blockEntity = level.getBlockEntity(pos);
			if (blockEntity == null || blockEntity == exclude) {
				continue;
			}

			if (blockEntity instanceof Container container) {
				for (int slot = 0; slot < container.getContainerSize(); slot++) {
					ItemStack stack = container.getItem(slot);
					if (!stack.isEmpty()) {
						counts.merge(stack.getItem(), stack.getCount(), Integer::sum);
					}
				}
			} else if (STORAGE_DRAWERS_LOADED && StorageDrawersCompat.isDrawerGroup(blockEntity)) {
				StorageDrawersCompat.accountContents(blockEntity, counts);
			}
		}

		return counts;
	}

	/**
	 * Finds the first item matching {@code predicate} in a container near {@code origin} and physically
	 * removes one unit of it from that container (the chest really loses the item - this is a real
	 * transfer, not a virtual count). Returns {@link ItemStack#EMPTY} if nothing matches nearby.
	 */
	public static ItemStack extractMatching(ServerLevel level, BlockPos origin, int radius, BlockEntity exclude, Predicate<ItemStack> predicate) {
		BlockPos min = origin.offset(-radius, -radius, -radius);
		BlockPos max = origin.offset(radius, radius, radius);

		for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
			BlockEntity blockEntity = level.getBlockEntity(pos);
			if (blockEntity == null || blockEntity == exclude) {
				continue;
			}

			if (blockEntity instanceof Container container) {
				for (int slot = 0; slot < container.getContainerSize(); slot++) {
					ItemStack stack = container.getItem(slot);
					if (!stack.isEmpty() && predicate.test(stack)) {
						ItemStack extracted = stack.copyWithCount(1);
						stack.shrink(1);
						container.setChanged();
						return extracted;
					}
				}
			} else if (STORAGE_DRAWERS_LOADED && StorageDrawersCompat.isDrawerGroup(blockEntity)) {
				ItemStack extracted = StorageDrawersCompat.extractOne(blockEntity, predicate);
				if (!extracted.isEmpty()) {
					return extracted;
				}
			}
		}

		return ItemStack.EMPTY;
	}
}
