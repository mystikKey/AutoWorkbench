package net.lockf.autoworkbenchmod;

import com.mojang.logging.LogUtils;
import net.lockf.autoworkbenchmod.block.ModBlocks;
import net.lockf.autoworkbenchmod.block.entity.ModBlockEntities;
import net.lockf.autoworkbenchmod.item.ModCreativeModeTabs;
import net.lockf.autoworkbenchmod.item.ModItems;
import net.lockf.autoworkbenchmod.networking.ModMessages;
import net.lockf.autoworkbenchmod.recipe.ModRecipes;
import net.lockf.autoworkbenchmod.screen.AutoWorkbenchScreen;
import net.lockf.autoworkbenchmod.screen.ModMenuTypes;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(AutoWorkbenchMod.MOD_ID)
public class AutoWorkbenchMod {
    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "autoworkbenchmod";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    public AutoWorkbenchMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModCreativeModeTabs.register(modEventBus);

        ModItems.register(modEventBus);

        ModBlocks.register(modEventBus);

        ModBlockEntities.register(modEventBus);

        ModMenuTypes.register(modEventBus);

        ModRecipes.register(modEventBus);

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);

        // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        ModMessages.register();
    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if(event.getTab().equals(ModCreativeModeTabs.AUTOWORKBENCH_TAB.get())){
            event.accept(ModItems.WOODGEAR);
            event.accept(ModItems.STONEGEAR);
            event.accept(ModItems.IRONGEAR);
            event.accept(ModItems.GOLDGEAR);
            event.accept(ModItems.DIAMONDGEAR);

            event.accept(ModBlocks.Auto_Workbench);
        }
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            MenuScreens.register(ModMenuTypes.AUTO_WORKBENCH_MENU.get(), AutoWorkbenchScreen::new);
        }
    }
}
