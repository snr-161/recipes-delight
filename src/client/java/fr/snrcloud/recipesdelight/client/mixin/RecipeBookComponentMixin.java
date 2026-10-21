package fr.snrcloud.recipesdelight.client.mixin;

import fr.snrcloud.recipesdelight.client.cache.NearbyContainerClientCache;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
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

	@Unique
	private int recipesdelight$lastSeenRevision = -1;

	@Invoker("updateStackedContents")
	abstract void recipesdelight$invokeUpdateStackedContents();

	@Inject(method = "tick", at = @At("TAIL"))
	private void recipesdelight$onTick(CallbackInfo ci) {
		if (!(this.menu instanceof CookingPotMenu cookingPotMenu)) {
			return;
		}

		if (!cookingPotMenu.blockEntity.getBlockPos().equals(NearbyContainerClientCache.getPos())) {
			if (this.recipesdelight$lastSeenRevision == -1) {
				this.recipesdelight$lastSeenRevision = -2;
			}
			return;
		}

		int revision = NearbyContainerClientCache.getRevision();
		if (revision != this.recipesdelight$lastSeenRevision) {
			this.recipesdelight$lastSeenRevision = revision;
			this.recipesdelight$invokeUpdateStackedContents();
		}
	}

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

		Map<Item, Integer> counts = NearbyContainerClientCache.getCounts();
		for (Map.Entry<Item, Integer> entry : counts.entrySet()) {
			this.stackedContents.accountStack(new ItemStack(entry.getKey(), entry.getValue()));
		}
	}
}
