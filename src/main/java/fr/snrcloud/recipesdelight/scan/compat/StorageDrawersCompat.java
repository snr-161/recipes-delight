package fr.snrcloud.recipesdelight.scan.compat;

import com.jaquadro.minecraft.storagedrawers.api.storage.IDrawer;
import com.jaquadro.minecraft.storagedrawers.api.storage.IDrawerGroup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Map;
import java.util.function.Predicate;

/**
 * Storage Drawers' drawer block entities never implement vanilla {@link net.minecraft.world.Container}
 * (a drawer isn't meant to be "opened" like a chest), so {@code NearbyContainerScanner} needs this
 * separate path using the mod's own {@link IDrawerGroup}/{@link IDrawer} API.
 *
 * This class is only ever touched (and therefore only ever class-loaded) once
 * {@code NearbyContainerScanner} has already confirmed Storage Drawers is loaded - see its
 * {@code STORAGE_DRAWERS_LOADED} guard. Referencing {@link IDrawerGroup}/{@link IDrawer} directly from
 * {@code NearbyContainerScanner} itself (which always loads, mod present or not) would otherwise risk
 * a {@code NoClassDefFoundError} for anyone without Storage Drawers installed.
 */
public final class StorageDrawersCompat {
	private StorageDrawersCompat() {
	}

	public static boolean isDrawerGroup(BlockEntity blockEntity) {
		return blockEntity instanceof IDrawerGroup;
	}

	public static void accountContents(BlockEntity blockEntity, Map<Item, Integer> counts) {
		IDrawerGroup group = (IDrawerGroup) blockEntity;
		for (int slot : group.getAccessibleDrawerSlots()) {
			IDrawer drawer = group.getDrawer(slot);
			if (!drawer.isEmpty()) {
				counts.merge(drawer.getStoredItemPrototype().getItem(), drawer.getStoredItemCount(), Integer::sum);
			}
		}
	}

	public static ItemStack extractOne(BlockEntity blockEntity, Predicate<ItemStack> predicate) {
		IDrawerGroup group = (IDrawerGroup) blockEntity;
		for (int slot : group.getAccessibleDrawerSlots()) {
			IDrawer drawer = group.getDrawer(slot);
			if (!drawer.isEmpty() && predicate.test(drawer.getStoredItemPrototype())) {
				ItemStack extracted = drawer.getStoredItemPrototype().copy();
				extracted.setCount(1);
				drawer.setStoredItemCount(drawer.getStoredItemCount() - 1);
				return extracted;
			}
		}
		return ItemStack.EMPTY;
	}
}
