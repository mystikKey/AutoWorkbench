package net.lockf.autoworkbenchmod.screen;

import net.lockf.autoworkbenchmod.AutoWorkbenchMod;
import net.lockf.autoworkbenchmod.networking.ModMessages;
import net.lockf.autoworkbenchmod.networking.packet.CycleAutoCrafterRecipeOutputC2SPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@OnlyIn(Dist.CLIENT)
public class AutoWorkbenchScreen extends AbstractGenericEnergyStorageContainerScreen<AutoWorkbenchMenu> {
    public AutoWorkbenchScreen(AutoWorkbenchMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle, new ResourceLocation(AutoWorkbenchMod.MOD_ID, "textures/gui/auto_workbench.png"));
        imageHeight = 206;
        inventoryLabelY = imageHeight - 94;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if(mouseButton == 0) {
            if(isHovering(126, 16, 12, 12, mouseX, mouseY)) {
                //Cycle through recipes

                ModMessages.sendToServer(new CycleAutoCrafterRecipeOutputC2SPacket(menu.getBlockEntity().getBlockPos()));
            }
        }

        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        super.renderBg(guiGraphics, partialTick, mouseX, mouseY);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        renderProgressArrow(guiGraphics, x, y);
    }

    private void renderProgressArrow(GuiGraphics guiGraphics, int x, int y) {
        if(menu.isCraftingActive())
            guiGraphics.blit(TEXTURE, x + 89, y + 34, 176, 53, menu.getScaledProgressArrowSize(), 17);
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderTooltip(guiGraphics, mouseX, mouseY);

        if(isHovering(126, 16, 12, 12, mouseX, mouseY)) {
            //Cycle through recipes

            List<Component> components = new ArrayList<>(2);
            components.add(Component.translatable("tooltip.autoworkbenchmod.auto_workbench.cycle_through_recipes"));

            guiGraphics.renderTooltip(font, components, Optional.empty(), mouseX, mouseY);
        }
    }
}
