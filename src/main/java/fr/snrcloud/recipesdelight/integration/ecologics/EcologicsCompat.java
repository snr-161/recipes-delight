package fr.snrcloud.recipesdelight.integration.ecologics;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class EcologicsCompat {
	public static final Item NUTS_COOKIE = register("nuts_cookie",
			new Item.Properties().food(new FoodProperties.Builder().nutrition(2).saturationModifier(0.1F).build()));

	public static final Item CHOCOLATE_NUTS_COOKIE = register("chocolate_nuts_cookie",
			new Item.Properties().food(new FoodProperties.Builder().nutrition(3).saturationModifier(0.2F).build()));

	public static final Item CHOCOLATE_COCONUT_COOKIE = register("chocolate_coconut_cookie",
			new Item.Properties().food(new FoodProperties.Builder().nutrition(3).saturationModifier(0.2F).build()));


	private EcologicsCompat() {
	}

	public static void init() {
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FOOD_AND_DRINKS).register(output -> {
			output.insertAfter(Items.COOKIE, new ItemStack(NUTS_COOKIE));
			output.insertAfter(Items.COOKIE, new ItemStack(CHOCOLATE_NUTS_COOKIE));
			output.insertAfter(Items.COOKIE, new ItemStack(CHOCOLATE_COCONUT_COOKIE));
		});
	}

	private static Item register(String path, Item.Properties properties) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("recipesdelight", path));
		Item item = new Item(properties.setId(key));
		return Registry.register(BuiltInRegistries.ITEM, key, item);
	}
}
