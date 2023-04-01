package com.jewey.rosia.common.blocks.entity.custom;

import com.jewey.rosia.common.blocks.entity.ModBlockEntities;
import com.jewey.rosia.common.items.ModItems;
import com.jewey.rosia.networking.ModMessages;
import com.jewey.rosia.networking.packet.EnergySyncS2CPacket;
import com.jewey.rosia.recipe.AutoQuernRecipe;
import com.jewey.rosia.screen.AutoQuernMenu;
import com.jewey.rosia.util.ModEnergyStorage;
import net.dries007.tfc.client.TFCSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.Random;


public class AutoQuernBlockEntity extends BlockEntity implements MenuProvider {
    private final ItemStackHandler itemHandler = new ItemStackHandler(3) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            switch (slot) {
                case 0: return stack.getItem() == ModItems.STEEL_GRINDSTONE.get();
            }
            return super.isItemValid(slot, stack);
        }
    };

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    protected final ContainerData data;
    private int progress = 0;
    private int maxProgress= 120;
    private float sTick;


    public AutoQuernBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.AUTO_QUERN_BLOCK_ENTITY.get(), pPos, pBlockState);
        this.data = new ContainerData() {
            public int get(int index) {
                return switch (index) {
                    case 0 -> AutoQuernBlockEntity.this.progress;
                    case 1 -> AutoQuernBlockEntity.this.maxProgress;
                    default -> 0;
                };
            }

            public void set(int index, int value) {
                switch (index) {
                    case 0 -> AutoQuernBlockEntity.this.progress = value;
                    case 1 -> AutoQuernBlockEntity.this.maxProgress = value;
                }
            }

            public int getCount() {
                return 2;
            }
        };
    }

    @Override
    public @NotNull Component getDisplayName() {
        return new TextComponent("Auto-Quern");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        ModMessages.sendToClients(new EnergySyncS2CPacket(this.ENERGY_STORAGE.getEnergyStored(), getBlockPos()));
        return new AutoQuernMenu(pContainerId, pPlayerInventory, this, this.data);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @javax.annotation.Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return lazyItemHandler.cast();
        }
        if(cap == CapabilityEnergy.ENERGY) {
            return lazyEnergyHandler.cast();
        }

        return super.getCapability(cap, side);
    }

    private final ModEnergyStorage ENERGY_STORAGE = new ModEnergyStorage(1000, 100) {
        @Override
        public void onEnergyChanged() {
            setChanged();
            ModMessages.sendToClients(new EnergySyncS2CPacket(this.energy, getBlockPos()));
        }
    };
    private static final int ENERGY_REQ = 100; // Energy cost to craft item

    private LazyOptional<IEnergyStorage> lazyEnergyHandler = LazyOptional.empty();


    public IEnergyStorage getEnergyStorage() {
        return ENERGY_STORAGE;
    }

    public void setEnergyLevel(int energy) {
        this.ENERGY_STORAGE.setEnergy(energy);
    }



    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
        lazyEnergyHandler = LazyOptional.of(() -> ENERGY_STORAGE);
    }

    @Override
    public void invalidateCaps()  {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
        lazyEnergyHandler.invalidate();
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        tag.put("inventory", itemHandler.serializeNBT());
        tag.putInt("auto_quern.progress", progress);
        tag.putInt("energy", ENERGY_STORAGE.getEnergyStored());
        super.saveAdditional(tag);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        itemHandler.deserializeNBT(nbt.getCompound("inventory"));
        progress = nbt.getInt("auto_quern.progress");
        ENERGY_STORAGE.setEnergy(nbt.getInt("energy"));
    }

    public void drops() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }

        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    public static void tick(Level pLevel, BlockPos pPos, BlockState pState, AutoQuernBlockEntity pBlockEntity) {
        if(hasRecipe(pBlockEntity) && hasEnoughEnergy(pBlockEntity)) {
            pBlockEntity.progress++;
            setChanged(pLevel, pPos, pState);
            if(pBlockEntity.progress > pBlockEntity.maxProgress) {
                craftItem(pBlockEntity);
                extractEnergy(pBlockEntity);
                pBlockEntity.resetSTick();
            }

            if(pBlockEntity.sTick > 0) { pBlockEntity.sTick -= 1; }
            if(pBlockEntity.progress == 15 && pBlockEntity.sTick == 0) {
                pLevel.playSound(null, pPos, TFCSounds.QUERN_DRAG.get(),
                        SoundSource.BLOCKS, 1, 1 + ((pLevel.random.nextFloat() - pLevel.random.nextFloat()) / 16));
                pBlockEntity.sTick += pBlockEntity.maxProgress - 15;
            }
        } else {
            pBlockEntity.resetProgress();
            setChanged(pLevel, pPos, pState);
        }
    }

    private static void extractEnergy(AutoQuernBlockEntity pBlockEntity) {
        pBlockEntity.ENERGY_STORAGE.extractEnergy(ENERGY_REQ, false);
    }

    private static boolean hasEnoughEnergy(AutoQuernBlockEntity pBlockEntity) {
        return pBlockEntity.ENERGY_STORAGE.getEnergyStored() >= ENERGY_REQ;
    }

    private static boolean hasRecipe(AutoQuernBlockEntity entity) {
        Level level = entity.level;
        SimpleContainer inventory = new SimpleContainer(entity.itemHandler.getSlots());
        for (int i = 0; i < entity.itemHandler.getSlots(); i++) {
            inventory.setItem(i, entity.itemHandler.getStackInSlot(i));
        }

        Optional<AutoQuernRecipe> match = level.getRecipeManager()
                .getRecipeFor(AutoQuernRecipe.Type.INSTANCE, inventory, level);

        return match.isPresent() && canInsertAmountIntoOutputSlot(inventory)
                && canInsertItemIntoOutputSlot(inventory, match.get().getResultItem())
                && hasToolsInToolSlot(entity);
    }


    private static boolean hasToolsInToolSlot(AutoQuernBlockEntity entity) {
        return entity.itemHandler.getStackInSlot(0).getItem() == ModItems.STEEL_GRINDSTONE.get();
    }

    private static void craftItem(AutoQuernBlockEntity entity) {
        Level level = entity.level;
        SimpleContainer inventory = new SimpleContainer(entity.itemHandler.getSlots());
        for (int i = 0; i < entity.itemHandler.getSlots(); i++) {
            inventory.setItem(i, entity.itemHandler.getStackInSlot(i));
        }

        Optional<AutoQuernRecipe> match = level.getRecipeManager()
                .getRecipeFor(AutoQuernRecipe.Type.INSTANCE, inventory, level);

        if(match.isPresent()) {
            if(entity.itemHandler.getStackInSlot(0).hurt(1, new Random(), null)) {
                entity.itemHandler.extractItem(0,1, false);
            }
            entity.itemHandler.extractItem(1,1, false);

            entity.itemHandler.setStackInSlot(2, new ItemStack(match.get().getResultItem().getItem(),
                    entity.itemHandler.getStackInSlot(2).getCount() + match.get().getResultItem().getCount()));

            entity.resetProgress();
        }
    }

    private void resetProgress() {
        this.progress = 0;
    }
    private void resetSTick() {
        this.sTick = 0;
    }

    private static boolean canInsertItemIntoOutputSlot(SimpleContainer inventory, ItemStack output) {
        return inventory.getItem(2).getItem() == output.getItem() || inventory.getItem(2).isEmpty();
    }

    private static boolean canInsertAmountIntoOutputSlot(SimpleContainer inventory) {
        return inventory.getItem(2).getMaxStackSize() > inventory.getItem(2).getCount();
    }
}
