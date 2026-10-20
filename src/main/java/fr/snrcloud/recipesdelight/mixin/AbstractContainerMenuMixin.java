package fr.snrcloud.recipesdelight.mixin;

import fr.snrcloud.recipesdelight.config.RecipesDelightConfigHolder;
import fr.snrcloud.recipesdelight.network.NearbyContainerContentsPayload;
import fr.snrcloud.recipesdelight.scan.NearbyContainerScanner;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vectorwing.farmersdelight.common.block.entity.container.CookingPotMenu;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mixin(AbstractContainerMenu.class)
public abstract class AbstractContainerMenuMixin {
	private static final int RESCAN_INTERVAL_TICKS = 20;

	@Unique
	private int recipesdelight$tickCounter;
	@Unique
	private Map<Item, Integer> recipesdelight$lastSent = Map.of();

	@Inject(method = "broadcastChanges", at = @At("TAIL"))
	private void recipesdelight$onBroadcastChanges(CallbackInfo ci) {
		if (!((Object) this instanceof CookingPotMenu cookingPotMenu)) {
			return;
		}

		this.recipesdelight$tickCounter++;
		if (this.recipesdelight$tickCounter != 1 && this.recipesdelight$tickCounter % RESCAN_INTERVAL_TICKS != 0) {
			return;
		}

		if (!(cookingPotMenu.blockEntity.getLevel() instanceof ServerLevel serverLevel)) {
			return;
		}

		ServerPlayer viewer = recipesdelight$findViewer(serverLevel, (AbstractContainerMenu) (Object) this);
		if (viewer == null) {
			return;
		}

		int radius = RecipesDelightConfigHolder.get().getRadius();
		Map<Item, Integer> counts = NearbyContainerScanner.scan(serverLevel, cookingPotMenu.blockEntity.getBlockPos(), radius, cookingPotMenu.blockEntity);

		if (counts.equals(this.recipesdelight$lastSent)) {
			return;
		}
		this.recipesdelight$lastSent = counts;

		List<NearbyContainerContentsPayload.Entry> entries = new ArrayList<>(counts.size());
		for (Map.Entry<Item, Integer> entry : counts.entrySet()) {
			Identifier id = BuiltInRegistries.ITEM.getKey(entry.getKey());
			if (id != null) {
				entries.add(new NearbyContainerContentsPayload.Entry(id, entry.getValue()));
			}
		}

		ServerPlayNetworking.send(viewer, new NearbyContainerContentsPayload(cookingPotMenu.blockEntity.getBlockPos(), entries));
	}

	@Inject(method = "removed", at = @At("TAIL"))
	private void recipesdelight$onRemoved(Player player, CallbackInfo ci) {
		if ((Object) this instanceof CookingPotMenu cookingPotMenu && player instanceof ServerPlayer serverPlayer) {
			ServerPlayNetworking.send(serverPlayer, new NearbyContainerContentsPayload(cookingPotMenu.blockEntity.getBlockPos(), List.of()));
		}
	}

	@Unique
	private static @Nullable ServerPlayer recipesdelight$findViewer(ServerLevel level, AbstractContainerMenu menu) {
		for (ServerPlayer player : level.players()) {
			if (player.containerMenu == menu) {
				return player;
			}
		}
		return null;
	}
}
