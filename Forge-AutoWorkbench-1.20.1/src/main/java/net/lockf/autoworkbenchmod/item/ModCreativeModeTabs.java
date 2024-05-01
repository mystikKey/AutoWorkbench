package net.lockf.autoworkbenchmod.item;

import net.lockf.autoworkbenchmod.AutoWorkbenchMod;
import net.lockf.autoworkbenchmod.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, AutoWorkbenchMod.MOD_ID);

    public static final RegistryObject<CreativeModeTab> AUTOWORKBENCH_TAB = CREATIVE_MODE_TABS.register("autoworkbench_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.WOODGEAR.get()))
                    .title(Component.translatable("creativetab.autoworkbench_tab"))
                    .displayItems((itemDisplayParameters, output) -> {
                        output.accept(ModItems.WOODGEAR.get());
                        output.accept(ModItems.STONEGEAR.get());
                        output.accept(ModItems.IRONGEAR.get());
                        output.accept(ModItems.GOLDGEAR.get());
                        output.accept(ModItems.DIAMONDGEAR.get());
                        output.accept(ModBlocks.Auto_Workbench.get());
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
