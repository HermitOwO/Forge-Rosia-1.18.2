package com.jewey.rosia.common.container;

import com.jewey.rosia.common.blocks.entity.ModBlockEntities;
import com.jewey.rosia.common.blocks.entity.block_entity.ElectricForgeBlockEntity;
import com.jewey.rosia.common.blocks.entity.block_entity.FireBoxBlockEntity;
import com.jewey.rosia.common.blocks.entity.block_entity.WaterPumpBlockEntity;
import com.jewey.rosia.common.blocks.entity.block_entity.SteamGeneratorBlockEntity;
import net.dries007.tfc.common.blockentities.InventoryBlockEntity;
import net.dries007.tfc.common.container.BlockEntityContainer;
import net.dries007.tfc.common.container.ItemStackContainer;
import net.dries007.tfc.util.registry.RegistrationHelpers;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

import static com.jewey.rosia.Rosia.MOD_ID;

public class ModContainerTypes {
    public static final DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.CONTAINERS, MOD_ID);


    public static final RegistryObject<MenuType<FireBoxContainer>> FIRE_BOX = ModContainerTypes.<FireBoxBlockEntity, FireBoxContainer>registerBlock("fire_box", ModBlockEntities.FIRE_BOX_BLOCK_ENTITY, FireBoxContainer::create);

    public static final RegistryObject<MenuType<SteamGeneratorContainer>> STEAM_GENERATOR = ModContainerTypes.<SteamGeneratorBlockEntity, SteamGeneratorContainer>registerBlock("steam_generator", ModBlockEntities.STEAM_GENERATOR_BLOCK_ENTITY, SteamGeneratorContainer::create);

    public static final RegistryObject<MenuType<WaterPumpContainer>> WATER_PUMP = ModContainerTypes.<WaterPumpBlockEntity, WaterPumpContainer>registerBlock("pump", ModBlockEntities.WATER_PUMP_BLOCK_ENTITY, WaterPumpContainer::create);

    public static final RegistryObject<MenuType<LeatherSatchelContainer>> LEATHER_SATCHEL = registerItem("leather_satchel", LeatherSatchelContainer::create);

    public static final RegistryObject<MenuType<ElectricForgeContainer>> ELECTRIC_FORGE = ModContainerTypes.<ElectricForgeBlockEntity, ElectricForgeContainer>registerBlock("electric_forge", ModBlockEntities.ELECTRIC_FORGE_BLOCK_ENTITY, ElectricForgeContainer::create);

    private static <T extends InventoryBlockEntity<?>, C extends BlockEntityContainer<T>> RegistryObject<MenuType<C>> registerBlock(String name, Supplier<BlockEntityType<T>> type, BlockEntityContainer.Factory<T, C> factory)
    {
        return RegistrationHelpers.registerBlockEntityContainer(CONTAINERS, name, type, factory);
    }

    private static <C extends ItemStackContainer> RegistryObject<MenuType<C>> registerItem(String name, ItemStackContainer.Factory<C> factory)
    {
        return RegistrationHelpers.registerItemStackContainer(CONTAINERS, name, factory);
    }

    private static <C extends AbstractContainerMenu> RegistryObject<MenuType<C>> register(String name, IContainerFactory<C> factory)
    {
        return RegistrationHelpers.registerContainer(CONTAINERS, name, factory);
    }


}


