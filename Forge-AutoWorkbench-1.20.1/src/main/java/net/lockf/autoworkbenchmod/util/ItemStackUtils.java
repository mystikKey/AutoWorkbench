package net.lockf.autoworkbenchmod.util;

import net.minecraft.world.item.ItemStack;

import java.util.LinkedList;
import java.util.List;

public class ItemStackUtils {
    private ItemStackUtils() {}

    // Combine item stacks
    public static List<ItemStack> combineItemStacks(List<ItemStack> itemStacks) {
        List<ItemStack> combinedItemStacks = new LinkedList<>();
        for(ItemStack itemStack:itemStacks) {
            boolean inserted = false;
            int amountLeft = itemStack.getCount();
            for(ItemStack combinedItemStack:combinedItemStacks) {
                if(ItemStack.isSameItem(itemStack, combinedItemStack) && ItemStack.isSameItemSameTags(itemStack, combinedItemStack) &&
                        combinedItemStack.getMaxStackSize() > combinedItemStack.getCount()) {
                    int amount = Math.min(amountLeft, combinedItemStack.getMaxStackSize() - combinedItemStack.getCount());
                    amountLeft -= amount;

                    combinedItemStack.grow(amount);

                    if(amountLeft == 0) {
                        inserted = true;
                        break;
                    }
                }
            }
            if(!inserted)
                combinedItemStacks.add(itemStack.copyWithCount(amountLeft));
        }
        return combinedItemStacks;
    }
}
