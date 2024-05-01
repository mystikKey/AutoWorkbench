package net.lockf.autoworkbenchmod.item;

import net.lockf.autoworkbenchmod.AutoWorkbenchMod;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, AutoWorkbenchMod.MOD_ID);

    public static final RegistryObject<Item> WOODGEAR =  ITEMS.register("wood_gear", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> STONEGEAR =  ITEMS.register("stone_gear", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> IRONGEAR =  ITEMS.register("iron_gear", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> GOLDGEAR =  ITEMS.register("gold_gear", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> DIAMONDGEAR =  ITEMS.register("diamond_gear", () -> new Item(new Item.Properties()));
    //public static final RegistryObject<Item> AUTOWORKBENCHITEM =  ITEMS.register("auto_workbench_item", () -> new Item(new Item.Properties()));

    public static void register(IEventBus eventBus){
        ITEMS.register(eventBus);
    }
}
