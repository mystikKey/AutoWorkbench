package net.lockf.autoworkbenchmod.networking.packet;

import net.lockf.autoworkbenchmod.inventory.ItemStackPacketUpdate;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ItemStackSyncS2CPacket {
    private final int slot;
    private final ItemStack itemStack;
    private final BlockPos pos;

    public ItemStackSyncS2CPacket(int slot, ItemStack itemStack, BlockPos pos) {
        this.slot = slot;
        this.itemStack = itemStack;
        this.pos = pos;
    }

    public ItemStackSyncS2CPacket(FriendlyByteBuf buffer) {
        slot = buffer.readInt();
        itemStack = buffer.readItem();
        pos = buffer.readBlockPos();
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeInt(slot);
        buffer.writeItemStack(itemStack, false);
        buffer.writeBlockPos(pos);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            BlockEntity blockEntity = Minecraft.getInstance().level.getBlockEntity(pos);

            //BlockEntity
            if(blockEntity instanceof ItemStackPacketUpdate itemStackStorage) {
                itemStackStorage.setItemStack(slot, itemStack);
            }
        });

        return true;
    }
}