package com.jewey.rosia.recipe;

import com.jewey.rosia.Rosia;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, Rosia.MOD_ID);

    public static final RegistryObject<RecipeSerializer<AutoQuernRecipe>> AUTO_QUERN_SERIALIZER =
            SERIALIZERS.register("auto_quern", () ->AutoQuernRecipe.Serializer.INSTANCE);

    public static final RegistryObject<RecipeSerializer<ExtrudingMachineRecipe>> EXTRUDING_MACHINE_SERIALIZER =
            SERIALIZERS.register("extruding_machine", () ->ExtrudingMachineRecipe.Serializer.INSTANCE);

    public static final RegistryObject<RecipeSerializer<RollingMachineRecipe>> ROLLING_MACHINE_SERIALIZER =
            SERIALIZERS.register("rolling_machine", () ->RollingMachineRecipe.Serializer.INSTANCE);

    public static final RegistryObject<RecipeSerializer<ElectricLoomRecipe>> ELECTRIC_LOOM_SERIALIZER =
            SERIALIZERS.register("electric_loom", () ->ElectricLoomRecipe.Serializer.INSTANCE);


    public static void register(IEventBus eventBus) {
        SERIALIZERS.register(eventBus);
    }
}
