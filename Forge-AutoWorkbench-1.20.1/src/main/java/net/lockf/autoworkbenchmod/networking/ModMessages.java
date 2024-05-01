package net.lockf.autoworkbenchmod.networking;

import net.lockf.autoworkbenchmod.AutoWorkbenchMod;
import net.lockf.autoworkbenchmod.networking.packet.CycleAutoCrafterRecipeOutputC2SPacket;
import net.lockf.autoworkbenchmod.networking.packet.ItemStackSyncS2CPacket;
import net.lockf.autoworkbenchmod.networking.packet.SetAutoCrafterPatternInputSlotsC2SPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class ModMessages {
    private ModMessages() {}

    private static SimpleChannel INSTANCE;

    private static int packetId = 0;

    private static int id() {
        return packetId++;
    }

    public static void register() {
        SimpleChannel net = NetworkRegistry.ChannelBuilder.named(new ResourceLocation(AutoWorkbenchMod.MOD_ID, "messages")).
                networkProtocolVersion(() -> "1.0").
                clientAcceptedVersions(v -> true).
                serverAcceptedVersions(v -> true).
                simpleChannel();

        INSTANCE = net;

        //Server -> Client

        net.messageBuilder(ItemStackSyncS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT).
                decoder(ItemStackSyncS2CPacket::new).
                encoder(ItemStackSyncS2CPacket::toBytes).
                consumerMainThread(ItemStackSyncS2CPacket::handle).
                add();

        net.messageBuilder(SetAutoCrafterPatternInputSlotsC2SPacket.class, id(), NetworkDirection.PLAY_TO_SERVER).
                decoder(SetAutoCrafterPatternInputSlotsC2SPacket::new).
                encoder(SetAutoCrafterPatternInputSlotsC2SPacket::toBytes).
                consumerMainThread(SetAutoCrafterPatternInputSlotsC2SPacket::handle).
                add();

        net.messageBuilder(CycleAutoCrafterRecipeOutputC2SPacket.class, id(), NetworkDirection.PLAY_TO_SERVER).
                decoder(CycleAutoCrafterRecipeOutputC2SPacket::new).
                encoder(CycleAutoCrafterRecipeOutputC2SPacket::toBytes).
                consumerMainThread(CycleAutoCrafterRecipeOutputC2SPacket::handle).
                add();
    }

    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.sendToServer(message);
    }

    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    public static <MSG> void sendToPlayerNear(MSG message, PacketDistributor.TargetPoint targetPoint) {
        INSTANCE.send(PacketDistributor.NEAR.with(() -> targetPoint), message);
    }

    public static <MSG> void sendToPlayersWithinXBlocks(MSG message, BlockPos pos, ResourceKey<Level> dimension, int distance) {
        INSTANCE.send(PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(pos.getX(), pos.getY(), pos.getZ(), distance, dimension)), message);
    }

    public static <MSG> void sendToAllPlayers(MSG message) {
        INSTANCE.send(PacketDistributor.ALL.noArg(), message);
    }
}