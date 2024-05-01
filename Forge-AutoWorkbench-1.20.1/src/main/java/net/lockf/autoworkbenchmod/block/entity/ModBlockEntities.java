package net.lockf.autoworkbenchmod.block.entity;

import net.lockf.autoworkbenchmod.AutoWorkbenchMod;
import net.lockf.autoworkbenchmod.block.ModBlocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, AutoWorkbenchMod.MOD_ID);

    public static final RegistryObject<BlockEntityType<AutoWorkbenchBlockEntity>> Auto_Workbench =
            BLOCK_ENTITIES.register("auto_workbench",
                    () -> BlockEntityType.Builder.of(AutoWorkbenchBlockEntity::new,
                            ModBlocks.Auto_Workbench.get()).build(null));

    public static void register(IEventBus eventBus){
        BLOCK_ENTITIES.register(eventBus);
    }
}
