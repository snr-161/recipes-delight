package fr.snrcloud.recipesdelight.scan;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public final class NearbyContainerScanner {
	private NearbyContainerScanner() {
	}

	public static Map<Item, Integer> scan(ServerLevel level, BlockPos origin, int radius, BlockEntity exclude) {
		Map<Item, Integer> counts = new HashMap<>();

		BlockPos min = origin.offset(-radius, -radius, -radius);
		BlockPos max = origin.offset(radius, radius, radius);

		for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
			BlockEntity blockEntity = level.getBlockEntity(pos);
			if (blockEntity == null || blockEntity == exclude || !(blockEntity instanceof Container container)) {
				continue;
			}

			for (int slot = 0; slot < container.getContainerSize(); slot++) {
				ItemStack stack = container.getItem(slot);
				if (!stack.isEmpty()) {
					counts.merge(stack.getItem(), stack.getCount(), Integer::sum);
				}
			}
		}

		return counts;
	}

	public static ItemStack extractMatching(ServerLevel level, BlockPos origin, int radius, BlockEntity exclude, Predicate<ItemStack> predicate) {
		BlockPos min = origin.offset(-radius, -radius, -radius);
		BlockPos max = origin.offset(radius, radius, radius);

		for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
			BlockEntity blockEntity = level.getBlockEntity(pos);
			if (blockEntity == null || blockEntity == exclude || !(blockEntity instanceof Container container)) {
				continue;
			}

			for (int slot = 0; slot < container.getContainerSize(); slot++) {
				ItemStack stack = container.getItem(slot);
				if (!stack.isEmpty() && predicate.test(stack)) {
					ItemStack extracted = stack.copyWithCount(1);
					stack.shrink(1);
					container.setChanged();
					return extracted;
				}
			}
		}

		return ItemStack.EMPTY;
	}
}
