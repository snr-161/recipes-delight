package fr.snrcloud.recipesdelight.client.mixin;

import fr.snrcloud.recipesdelight.client.cache.NearbyContainerClientCache;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vectorwing.farmersdelight.common.block.entity.container.CookingPotMenu;

import java.util.Map;

@Mixin(RecipeBookComponent.class)
public abstract class RecipeBookComponentMixin {
	@Shadow
	@Final
	protected RecipeBookMenu menu;

	@Shadow
	@Final
	private StackedItemContents stackedContents;

	@Inject(
			method = {"initVisuals", "updateStackedContents"},
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/inventory/RecipeBookMenu;fillCraftSlotsStackedContents(Lnet/minecraft/world/entity/player/StackedItemContents;)V",
					shift = At.Shift.AFTER
			)
	)
	private void recipesdelight$accountNearbyContainers(CallbackInfo ci) {
		if (!(this.menu instanceof CookingPotMenu cookingPotMenu)) {
			return;
		}

		if (!cookingPotMenu.blockEntity.getBlockPos().equals(NearbyContainerClientCache.getPos())) {
			return;
		}

		for (Map.Entry<Item, Integer> entry : NearbyContainerClientCache.getCounts().entrySet()) {
			this.stackedContents.accountStack(new ItemStack(entry.getKey(), entry.getValue()));
		}
	}
}
