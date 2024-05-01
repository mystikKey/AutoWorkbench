package net.lockf.autoworkbenchmod.screen;

import net.lockf.autoworkbenchmod.block.ModBlocks;
import net.lockf.autoworkbenchmod.block.entity.AutoWorkbenchBlockEntity;
import net.lockf.autoworkbenchmod.inventory.PatternResultSlot;
import net.lockf.autoworkbenchmod.inventory.PatternSlot;
import net.lockf.autoworkbenchmod.util.ByteUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.SlotItemHandler;

public class AutoWorkbenchMenu extends AbstractContainerMenu {
    private final AutoWorkbenchBlockEntity blockEntity;
    private final Level level;
    private final ContainerData data;

    private final Container patternSlots;

    private final Container patternResultSlots;

    public AutoWorkbenchMenu(int id, Inventory inv, FriendlyByteBuf buffer) {
        this(id, inv, inv.player.level().getBlockEntity(buffer.readBlockPos()), new SimpleContainer(9), new SimpleContainer(1), new SimpleContainerData(11));
    }

    public AutoWorkbenchMenu(int id, Inventory inv, BlockEntity blockEntity, Container patternSlots, Container patternResultSlots, ContainerData data) {
        super(ModMenuTypes.AUTO_WORKBENCH_MENU.get(), id);

        this.patternSlots = patternSlots;
        this.patternResultSlots = patternResultSlots;

        checkContainerDataCount(data, 4);
        this.blockEntity = (AutoWorkbenchBlockEntity) blockEntity;
        this.level = inv.player.level();
        this.data = data;

        addPlayerInventory(inv);
        addPlayerHotbar(inv);

        this.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(itemHandler -> {
            for (int i = 0; i < 2; i++)
                for (int j = 0; j < 9; j++)
                    addSlot(new SlotItemHandler(itemHandler, 9 * i + j, 8 + 18 * j, 75 + 18 * i));
        });

        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
                addSlot(new PatternSlot(patternSlots, j + i * 3, 30 + j * 18, 17 + i * 18, () -> true));

        addSlot(new PatternResultSlot(patternResultSlots, 0, 124, 35, () -> true));

        addDataSlots(this.data);
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 124 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int i = 0; i < 9; i++) {
            addSlot(new Slot(playerInventory, i, 8 + i * 18, 182));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot sourceSlot = slots.get(index);
        if (sourceSlot == null || !sourceSlot.hasItem())
            return ItemStack.EMPTY;

        ItemStack sourceItem = sourceSlot.getItem();
        ItemStack sourceItemCopy = sourceItem.copy();

        if (index < 4 * 9) {
            //Player inventory slot -> Merge into tile inventory
            //"+ 18": Ignore 3x3 crafting grid and result slot
            if (!moveItemStackTo(sourceItem, 4 * 9 + 3, 4 * 9 + 18, false)) {
                //"+3" instead of nothing: Do not allow adding to first 3 output item only slot
                return ItemStack.EMPTY;
            }
        } else if (index < 4 * 9 + 18) {
            //Tile inventory slot -> Merge into player inventory
            if (!moveItemStackTo(sourceItem, 0, 4 * 9, false)) {
                return ItemStack.EMPTY;
            }
        } else if (index < 4 * 9 + 18 + 3 * 3 + 1) {
            return ItemStack.EMPTY;
        } else {
            throw new IllegalArgumentException("Invalid slot index");
        }

        if (sourceItem.getCount() == 0)
            sourceSlot.set(ItemStack.EMPTY);
        else
            sourceSlot.setChanged();

        sourceSlot.onTake(player, sourceItem);

        return sourceItemCopy;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()), player, ModBlocks.Auto_Workbench.get());
    }

    public Container getPatternSlots() {
        return patternSlots;
    }

    public int getScaledProgressArrowSize() {
        int progress = ByteUtils.from2ByteChunks((short)data.get(0), (short)data.get(1));
        int maxProgress = ByteUtils.from2ByteChunks((short)data.get(2), (short)data.get(3));
        int progressArrowSize = 24;

        return (maxProgress == 0 || progress == 0)?0:progress * progressArrowSize / maxProgress;
    }

    /**
     * @return Same as isCrafting but energy requirements are ignored
     */
    public boolean isCraftingActive() {
        return ByteUtils.from2ByteChunks((short)data.get(0), (short)data.get(1)) > 0;
    }

    public boolean isCrafting() {
        return ByteUtils.from2ByteChunks((short)data.get(0), (short)data.get(1)) > 0 && data.get(6) == 1;
    }

    public BlockEntity getBlockEntity() {
        return blockEntity;
    }
}
