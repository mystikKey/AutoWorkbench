package net.lockf.autoworkbenchmod.networking.packet;

import net.lockf.autoworkbenchmod.block.entity.AutoWorkbenchBlockEntity;
import net.lockf.autoworkbenchmod.screen.AutoWorkbenchMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class SetAutoCrafterPatternInputSlotsC2SPacket {
    private final BlockPos pos;
    private final List<ItemStack> itemStacks;
    private final ResourceLocation recipeId;

    public SetAutoCrafterPatternInputSlotsC2SPacket(BlockPos pos, List<ItemStack> itemStacks, ResourceLocation recipeId) {
        this.pos = pos;

        this.itemStacks = new ArrayList<>(itemStacks);

        while (this.itemStacks.size() < 9)
            this.itemStacks.add(ItemStack.EMPTY);

        this.recipeId = recipeId;
    }

    public SetAutoCrafterPatternInputSlotsC2SPacket(FriendlyByteBuf buffer) {
        pos = buffer.readBlockPos();

        itemStacks = new ArrayList<>(9);
        for (int i = 0; i < 9; i++)
            itemStacks.add(buffer.readItem());

        recipeId = buffer.readResourceLocation();
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);

        for (ItemStack itemStack : itemStacks)
            buffer.writeItemStack(itemStack, false);

        buffer.writeResourceLocation(recipeId);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            Level level = context.getSender().level();
            if (!level.hasChunk(SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ())))
                return;

            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (!(blockEntity instanceof AutoWorkbenchBlockEntity autoWorkbenchBlockEntity))
                return;

            AbstractContainerMenu menu = context.getSender().containerMenu;

            if (!(menu instanceof AutoWorkbenchMenu autoWorkbenchMenu))
                return;

            for (int i = 0; i < itemStacks.size(); i++)
                autoWorkbenchMenu.getPatternSlots().setItem(i, itemStacks.get(i));

            autoWorkbenchBlockEntity.setRecipeIdForSetRecipe(recipeId);
            autoWorkbenchBlockEntity.resetProgressAndMarkAsChanged();
        });
        return true;
    }
}