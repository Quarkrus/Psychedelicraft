/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.screen;

import ivorius.psychedelicraft.blocks.BlockWithFluid;
import ivorius.psychedelicraft.client.screen.TickableContainer;
import ivorius.psychedelicraft.fluids.Resovoir;
import ivorius.psychedelicraft.items.FluidContainerItem;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.*;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.*;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.math.Direction;

/**
 * Created by lukas on 26.10.14.
 * Updated by Sollace on 3 Jan 2023
 */
public class FluidContraptionScreenHandler<T extends BlockEntity & BlockWithFluid.DirectionalFluidResovoir> extends ScreenHandler implements TickableContainer {
    public int drainSpeedPerTick = 100;
    public boolean currentlyDrainingItem;

    private final Inventory inputInventory = new SimpleInventory(1);
    private final Resovoir tank;
    private final T blockEntity;

    @SuppressWarnings("unchecked")
    public FluidContraptionScreenHandler(ScreenHandlerType<? extends FluidContraptionScreenHandler<T>> type, int syncId, PlayerInventory inventory, PacketByteBuf buffer) {
        this(type, syncId, inventory, (T)inventory.player.world.getBlockEntity(buffer.readBlockPos()), buffer.readEnumConstant(Direction.class));
    }

    public FluidContraptionScreenHandler(ScreenHandlerType<? extends FluidContraptionScreenHandler<T>> type, int syncId, PlayerInventory inventory, T blockEntity, Direction direction) {
        super(type, syncId);
        this.tank = blockEntity.getTank(direction);
        this.blockEntity = blockEntity;
        addSlot(new Slot(inputInventory, 0, 25, 40) {
            @Override
            public void markDirty() {
                super.markDirty();
                FluidContraptionScreenHandler.this.onContentChanged(inputInventory);
            }
        });
        for (int y = 0; y < 3; ++y) {
            for (int x = 0; x < 9; ++x) {
                addSlot(new Slot(inventory, x + y * 9 + 9, 8 + x * 18, 84 + y * 18));
            }
        }

        for (int x = 0; x < 9; ++x) {
            addSlot(new Slot(inventory, x, 8 + x * 18, 142));
        }
    }

    public Resovoir getTank() {
        return tank;
    }

    public T getBlockEntity() {
        return blockEntity;
    }

    @Override
    public void onClientTick() {
        transferLiquid(currentlyDrainingItem, drainSpeedPerTick);
    }

    public void transferLiquid(boolean drainItem, int drainSpeed) {
        ItemStack inputStack = inputInventory.getStack(0);
        if (inputStack.getItem() instanceof FluidContainerItem container) {
            if (drainItem) {
                inputInventory.setStack(0, tank.deposit(drainSpeed, inputStack));
            }
        }
    }

    @Override
    public boolean onButtonClick(PlayerEntity player, int action) {
        if (action == 1 || action == 0) {
            currentlyDrainingItem = action == 1;
            return true;
        }

        return super.onButtonClick(player, action);
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return tank.canPlayerUse(player);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int index) {
        ItemStack originalStack = null;
        Slot slot = slots.get(index);

        if (slot != null && slot.hasStack()) {
            ItemStack stack = slot.getStack();
            originalStack = stack.copy();

            if (index == 0) {
                if (!insertItem(stack, 1, 37, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (slots.get(0).hasStack() || !slots.get(0).canInsert(stack)) {
                    return ItemStack.EMPTY;
                }

                if (stack.hasNbt() && stack.getCount() == 1) {
                    slots.get(0).setStack(stack.copy());
                    stack.decrement(1);
                } else if (stack.getCount() >= 1) {
                    slots.get(0).setStack(stack.split(1));
                }
            }

            if (stack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }

            if (stack.getCount() == originalStack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTakeItem(player, stack);
        }

        return originalStack;
    }
}
