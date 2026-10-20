package fr.snrcloud.recipesdelight.mixin;

import fr.snrcloud.recipesdelight.config.RecipesDelightConfigHolder;
import fr.snrcloud.recipesdelight.scan.NearbyContainerScanner;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vectorwing.farmersdelight.common.block.entity.CookingPotBlockEntity;
import vectorwing.farmersdelight.common.block.entity.container.CookingPotMenu;
import vectorwing.farmersdelight.common.crafting.CookingPotRecipe;
import vectorwing.farmersdelight.refabricated.inventory.ItemStackHandler;

import java.util.List;
import java.util.Map;

@Mixin(CookingPotMenu.class)
public abstract class CookingPotAutoPullMixin {
	private static final int MAX_STACK_SIZE = 64;

	@Shadow
	@Final
	public CookingPotBlockEntity blockEntity;
	@Shadow
	@Final
	public ItemStackHandler inventory;

	@Inject(method = "handlePlacement", at = @At("HEAD"), cancellable = true)
	private void recipesdelight$handlePlacement(
			boolean useMaxItems, boolean allowDroppingItemsToClear, RecipeHolder<?> recipe, ServerLevel level, Inventory playerInventory,
			CallbackInfoReturnable<RecipeBookMenu.PostPlaceAction> cir
	) {
		if (!(recipe.value() instanceof CookingPotRecipe cookingPotRecipe)) {
			return;
		}

		List<Ingredient> ingredients = cookingPotRecipe.input();
		int radius = RecipesDelightConfigHolder.get().getRadius();
		BlockPos pos = this.blockEntity.getBlockPos();
		Map<Item, Integer> nearby = NearbyContainerScanner.scan(level, pos, radius, this.blockEntity);

		boolean recipeMatchesPlaced = true;
		for (int i = 0; i < CookingPotRecipe.INPUT_SLOTS; i++) {
			ItemStack slotStack = this.inventory.getStackInSlot(i);
			boolean shouldMatch = i < ingredients.size() && ingredients.get(i).test(slotStack);
			boolean slotEmpty = slotStack.isEmpty();
			if (i < ingredients.size() ? !shouldMatch : !slotEmpty) {
				recipeMatchesPlaced = false;
				break;
			}
		}

		int[] currentCounts = new int[ingredients.size()];
		int[] availableCounts = new int[ingredients.size()];
		for (int i = 0; i < ingredients.size(); i++) {
			Ingredient ingredient = ingredients.get(i);
			ItemStack slotStack = this.inventory.getStackInSlot(i);
			currentCounts[i] = slotStack.isEmpty() ? 0 : slotStack.getCount();
			availableCounts[i] = currentCounts[i]
					+ this.recipesdelight$countInInventory(playerInventory, ingredient)
					+ this.recipesdelight$countNearby(nearby, ingredient);
		}

		int targetAmount;
		if (useMaxItems) {
			targetAmount = MAX_STACK_SIZE;
			for (int count : availableCounts) {
				targetAmount = Math.min(targetAmount, count);
			}
		} else if (recipeMatchesPlaced && ingredients.size() > 0) {
			int smallest = Integer.MAX_VALUE;
			for (int count : currentCounts) {
				smallest = Math.min(smallest, count);
			}
			targetAmount = smallest + 1;
		} else {
			targetAmount = 1;
		}

		for (int count : availableCounts) {
			if (count < targetAmount) {
				// Not actually craftable (not even counting nearby containers) - leave the pot's slots
				// untouched and let the client show the ghost recipe preview, exactly like vanilla does
				// for any other unsatisfiable recipe.
				cir.setReturnValue(RecipeBookMenu.PostPlaceAction.PLACE_GHOST_RECIPE);
				cir.cancel();
				return;
			}
		}

		for (int i = 0; i < ingredients.size(); i++) {
			int needed = targetAmount - currentCounts[i];
			Ingredient ingredient = ingredients.get(i);

			while (needed > 0) {
				ItemStack fromInventory = this.recipesdelight$extractFromInventory(playerInventory, ingredient);
				if (!fromInventory.isEmpty()) {
					this.recipesdelight$growSlot(i, fromInventory);
					needed--;
					continue;
				}

				ItemStack fromChest = NearbyContainerScanner.extractMatching(level, pos, radius, this.blockEntity, ingredient);
				if (!fromChest.isEmpty()) {
					this.recipesdelight$growSlot(i, fromChest);
					needed--;
					continue;
				}

				break;
			}
		}

		cir.setReturnValue(RecipeBookMenu.PostPlaceAction.NOTHING);
		cir.cancel();
	}

	private void recipesdelight$growSlot(int slot, ItemStack extracted) {
		ItemStack current = this.inventory.getStackInSlot(slot);
		if (current.isEmpty()) {
			this.inventory.setStackInSlot(slot, extracted);
		} else {
			current.grow(extracted.getCount());
		}
	}

	private int recipesdelight$countInInventory(Inventory playerInventory, Ingredient ingredient) {
		int total = 0;
		NonNullList<ItemStack> items = playerInventory.getNonEquipmentItems();
		for (ItemStack stack : items) {
			if (!stack.isEmpty() && ingredient.test(stack)) {
				total += stack.getCount();
			}
		}
		return total;
	}

	private ItemStack recipesdelight$extractFromInventory(Inventory playerInventory, Ingredient ingredient) {
		NonNullList<ItemStack> items = playerInventory.getNonEquipmentItems();
		for (ItemStack stack : items) {
			if (!stack.isEmpty() && ingredient.test(stack)) {
				ItemStack extracted = stack.copyWithCount(1);
				stack.shrink(1);
				playerInventory.setChanged();
				return extracted;
			}
		}
		return ItemStack.EMPTY;
	}

	private int recipesdelight$countNearby(Map<Item, Integer> nearby, Ingredient ingredient) {
		int total = 0;
		for (Map.Entry<Item, Integer> entry : nearby.entrySet()) {
			if (ingredient.test(new ItemStack(entry.getKey()))) {
				total += entry.getValue();
			}
		}
		return total;
	}
}
