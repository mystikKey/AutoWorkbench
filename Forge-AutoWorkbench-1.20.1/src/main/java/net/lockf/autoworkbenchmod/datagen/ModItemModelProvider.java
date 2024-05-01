package net.lockf.autoworkbenchmod.datagen;

import net.lockf.autoworkbenchmod.AutoWorkbenchMod;
import net.lockf.autoworkbenchmod.block.ModBlocks;
import net.lockf.autoworkbenchmod.item.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, AutoWorkbenchMod.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        simpleItem(ModItems.WOODGEAR);
        simpleItem(ModItems.STONEGEAR);
        simpleItem(ModItems.IRONGEAR);
        simpleItem(ModItems.GOLDGEAR);
        simpleItem(ModItems.DIAMONDGEAR);
        withExistingParent(ModBlocks.Auto_Workbench.getId().getPath(), modLoc("block/auto_workbench"));
    }

    private ItemModelBuilder simpleItem(RegistryObject<Item> item){
        return withExistingParent(item.getId().getPath(),
                new ResourceLocation("item/generated")).texture("layer0",
                new ResourceLocation(AutoWorkbenchMod.MOD_ID, "item/" + item.getId().getPath()));
    }
}
