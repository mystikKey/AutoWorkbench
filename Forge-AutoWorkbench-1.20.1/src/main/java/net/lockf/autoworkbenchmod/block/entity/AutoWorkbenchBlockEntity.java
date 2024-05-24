package net.lockf.autoworkbenchmod.block.entity;

import com.mojang.datafixers.util.Pair;
import net.lockf.autoworkbenchmod.Config;
import net.lockf.autoworkbenchmod.block.entity.handler.InputOutputItemHandler;
import net.lockf.autoworkbenchmod.networking.ModMessages;
import net.lockf.autoworkbenchmod.networking.packet.ItemStackSyncS2CPacket;
import net.lockf.autoworkbenchmod.screen.AutoWorkbenchMenu;
import net.lockf.autoworkbenchmod.util.ByteUtils;
import net.lockf.autoworkbenchmod.util.ItemStackUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class AutoWorkbenchBlockEntity extends BlockEntity implements MenuProvider {

    private final ItemStackHandler itemHandler = new ItemStackHandler(18) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if (slot < 0 || slot >= 18)
                return super.isItemValid(slot, stack);

            //Slot 0, 1, and 2 are for output items only
            return slot >= 3;
        }
    };

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    protected final ContainerData data;

    private final ContainerListener updatePatternListener = container -> updateRecipe();

    private CraftingRecipe craftingRecipe;

    private int progress = 0;
    private int maxProgress = Config.maxProgress /*10 - default*/;

    private boolean hasRecipeLoaded = false;

    private CraftingContainer oldCopyOfRecipe;

    private ResourceLocation recipeIdForSetRecipe;

    private final SimpleContainer patternResultSlots = new SimpleContainer(1);

    // Crafting slots
    private final SimpleContainer patternSlots = new SimpleContainer(3 * 3) {
        @Override
        public int getMaxStackSize() {
            return 1;
        }
    };
    // Crafting slots
    private final AbstractContainerMenu dummyContainerMenu = new AbstractContainerMenu(null, -1) {
        @Override
        public ItemStack quickMoveStack(Player player, int index) {
            return null;
        }

        @Override
        public boolean stillValid(Player player) {
            return false;
        }

        @Override
        public void slotsChanged(Container container) {
        }
    };

    private final LazyOptional<IItemHandler> lazyItemHandlerSided = LazyOptional.of(
            () -> new InputOutputItemHandler(this.itemHandler, (i, stack) -> i >= 3,
                    i -> isOutputOrCraftingRemainderOfInput(this.itemHandler.getStackInSlot(i))));

    public AutoWorkbenchBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.Auto_Workbench.get(), pPos, pBlockState);

        patternSlots.addListener(updatePatternListener);

        this.data = new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0, 1 -> ByteUtils.get2Bytes(AutoWorkbenchBlockEntity.this.progress, index);
                    case 2, 3 -> ByteUtils.get2Bytes(AutoWorkbenchBlockEntity.this.maxProgress, index - 2);
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                switch (index) {
                    case 0, 1 -> AutoWorkbenchBlockEntity.this.progress = ByteUtils.with2Bytes(
                            AutoWorkbenchBlockEntity.this.progress, (short) value, index
                    );
                    case 2, 3 -> AutoWorkbenchBlockEntity.this.maxProgress = ByteUtils.with2Bytes(
                            AutoWorkbenchBlockEntity.this.maxProgress, (short) value, index - 2);
                }
            }

            @Override
            public int getCount() {
                return 11;
            }
        };
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            if (side == null)
                return this.lazyItemHandler.cast();

            return this.lazyItemHandlerSided.cast();
        }

        return super.getCapability(cap, side);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        this.lazyItemHandler = LazyOptional.of(() -> this.itemHandler);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        this.lazyItemHandler.invalidate();
    }

    public void drops() {
        //gets the number of slots in the inventory
        SimpleContainer inventory = new SimpleContainer(this.itemHandler.getSlots());
        for (int i = 0; i < this.itemHandler.getSlots(); i++) {
            inventory.setItem(i, this.itemHandler.getStackInSlot(i));
        }
        assert this.level != null;
        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.autoworkbenchmod.auto_workbench");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        ModMessages.sendToPlayer(new ItemStackSyncS2CPacket(0, this.itemHandler.getStackInSlot(0),
                getBlockPos()), (ServerPlayer) player);
        return new AutoWorkbenchMenu(id, inventory, this, patternSlots, patternResultSlots, data);
    }

    @Override
    protected void saveAdditional(CompoundTag pTag) {
        pTag.put("inventory", this.itemHandler.serializeNBT());
        pTag.put("pattern", savePatternContainer());

        if (craftingRecipe != null)
            pTag.put("recipe.id", StringTag.valueOf(craftingRecipe.getId().toString()));

        pTag.put("recipe.progress", IntTag.valueOf(progress));

        super.saveAdditional(pTag);
    }

    private Tag savePatternContainer() {
        ListTag nbtTagList = new ListTag();
        for (int i = 0; i < patternSlots.getContainerSize(); i++) {
            if (!patternSlots.getItem(i).isEmpty()) {
                CompoundTag itemTag = new CompoundTag();
                itemTag.putInt("Slot", i);
                patternSlots.getItem(i).save(itemTag);
                nbtTagList.add(itemTag);
            }
        }

        return nbtTagList;
    }

    @Override
    public void load(@NotNull CompoundTag pTag) {
        super.load(pTag);
        this.itemHandler.deserializeNBT(pTag.getCompound("inventory"));
        loadPatternContainer(pTag.get("pattern"));
        if(pTag.contains("recipe.id")) {
            Tag tag = pTag.get("recipe.id");

            if(!(tag instanceof StringTag stringTag))
                throw new IllegalArgumentException("Tag must be of type StringTag!");

            recipeIdForSetRecipe = ResourceLocation.tryParse(stringTag.getAsString());
        }

        progress = pTag.getInt("recipe.progress");
    }

    private void loadPatternContainer(Tag tag) {
        if(!(tag instanceof ListTag))
            throw new IllegalArgumentException("Tag must be of type ListTag!");

        patternSlots.removeListener(updatePatternListener);
        ListTag tagList = (ListTag)tag;
        for(int i = 0;i < tagList.size();i++) {
            CompoundTag itemTags = tagList.getCompound(i);
            int slot = itemTags.getInt("Slot");

            if(slot >= 0 && slot < patternSlots.getContainerSize()) {
                patternSlots.setItem(slot, ItemStack.of(itemTags));
            }
        }
        patternSlots.addListener(updatePatternListener);
    }

    public static void tick(Level level, BlockPos blockPos, BlockState state, AutoWorkbenchBlockEntity blockEntity) {
        if (level.isClientSide)
            return;

        if (!blockEntity.hasRecipeLoaded) {
            blockEntity.updateRecipe();

            if (blockEntity.craftingRecipe == null)
                blockEntity.resetProgress();
        }

        int itemCount = 0;
        for (int i = 0; i < blockEntity.patternSlots.getContainerSize(); i++)
            if (!blockEntity.patternSlots.getItem(i).isEmpty())
                itemCount++;

        //Ignore empty recipes
        if (itemCount == 0)
            return;

        if (blockEntity.craftingRecipe != null && (blockEntity.progress > 0 || (blockEntity.canInsertItemsIntoOutputSlots() && blockEntity.canExtractItemsFromInput()))) {
            if (!blockEntity.canInsertItemsIntoOutputSlots() || !blockEntity.canExtractItemsFromInput())
                return;

            if (blockEntity.progress == 0) {
                if (!blockEntity.canExtractItemsFromInput())
                    return;
            }

            if (blockEntity.progress < 0 || blockEntity.maxProgress < 0) {
                //Reset progress for invalid values

                blockEntity.resetProgress();
                setChanged(level, blockPos, state);

                return;
            }

            blockEntity.progress++;

            if (blockEntity.progress >= blockEntity.maxProgress) {
                SimpleContainer patternSlotsForRecipe = blockEntity.patternSlots;
                CraftingContainer copyOfPatternSlots = new TransientCraftingContainer(blockEntity.dummyContainerMenu, 3, 3);

                for (int i = 0; i < patternSlotsForRecipe.getContainerSize(); i++)
                    copyOfPatternSlots.setItem(i, patternSlotsForRecipe.getItem(i));

                blockEntity.extractItems();
                blockEntity.craftItem(copyOfPatternSlots);
            }

            setChanged(level, blockPos, state);
        } else {
            blockEntity.resetProgress();
            setChanged(level, blockPos, state);
        }
    }

    private boolean isOutputOrCraftingRemainderOfInput(ItemStack itemStack) {
        if (this.craftingRecipe == null)
            return false;

        SimpleContainer patternSlotsForRecipe = this.patternSlots;
        CraftingContainer copyOfPatternSlots = new TransientCraftingContainer(this.dummyContainerMenu, 3, 3);
        for (int i = 0; i < patternSlotsForRecipe.getContainerSize(); i++)
            copyOfPatternSlots.setItem(i, patternSlotsForRecipe.getItem(i));

        ItemStack resultItemStack = this.craftingRecipe instanceof CustomRecipe ? this.craftingRecipe.assemble(copyOfPatternSlots, this.level.registryAccess()) :
                this.craftingRecipe.getResultItem(this.level.registryAccess());

        if (ItemStack.isSameItemSameTags(itemStack, resultItemStack))
            return true;

        for (ItemStack remainingItem : this.craftingRecipe.getRemainingItems(copyOfPatternSlots))
            if (ItemStack.isSameItemSameTags(itemStack, remainingItem))
                return true;

        return false;
    }


    private boolean canExtractItemsFromInput() {
        if (this.craftingRecipe == null)
            return false;

        SimpleContainer patternSlotsForRecipe = this.patternSlots;

        List<ItemStack> patternItemStacks = new ArrayList<>(9);
        for (int i = 0; i < patternSlotsForRecipe.getContainerSize(); i++)
            if (!patternSlotsForRecipe.getItem(i).isEmpty())
                patternItemStacks.add(patternSlotsForRecipe.getItem(i));

        List<ItemStack> itemStacks = ItemStackUtils.combineItemStacks(patternItemStacks);

        List<Integer> checkedIndices = new ArrayList<>(9);
        outer:
        for (int i = itemStacks.size() - 1; i >= 0; i--) {
            ItemStack itemStack = itemStacks.get(i);

            for (int j = 0; j < this.itemHandler.getSlots(); j++) {
                if (checkedIndices.contains(j))
                    continue;

                ItemStack testItemStack = this.itemHandler.getStackInSlot(j);
                if (testItemStack.isEmpty()) {
                    checkedIndices.add(j);
                    continue;
                }

                if (ItemStack.isSameItemSameTags(itemStack, testItemStack)) {
                    int amount = Math.min(itemStack.getCount(), testItemStack.getCount());
                    checkedIndices.add(j);

                    if (amount == itemStack.getCount()) {
                        itemStacks.remove(i);
                        continue outer;
                    } else {
                        itemStack.shrink(amount);
                    }
                }
            }
            return false;
        }
        return itemStacks.isEmpty();
    }

    private boolean canInsertItemsIntoOutputSlots() {
        if (this.craftingRecipe == null)
            return false;

        SimpleContainer patternSlotsForRecipe = this.patternSlots;
        CraftingContainer copyOfPatternSlots = new TransientCraftingContainer(this.dummyContainerMenu, 3, 3);
        for (int i = 0; i < patternSlotsForRecipe.getContainerSize(); i++)
            copyOfPatternSlots.setItem(i, patternSlotsForRecipe.getItem(i));

        List<ItemStack> outputItemStacks = new ArrayList<>(10);
        ItemStack resultItemStack = this.craftingRecipe instanceof CustomRecipe ? this.craftingRecipe.assemble(copyOfPatternSlots, this.level.registryAccess()) :
                this.craftingRecipe.getResultItem(this.level.registryAccess());

        if (!resultItemStack.isEmpty())
            outputItemStacks.add(resultItemStack);

        for (ItemStack remainingItem : this.craftingRecipe.getRemainingItems(copyOfPatternSlots))
            if (!remainingItem.isEmpty())
                outputItemStacks.add(remainingItem);

        List<ItemStack> itemStacks = ItemStackUtils.combineItemStacks(outputItemStacks);

        List<Integer> checkedIndices = new ArrayList<>(18);
        List<Integer> emptyIndices = new ArrayList<>(18);
        outer:
        for (int i = itemStacks.size() - 1; i >= 0; i--) {
            ItemStack itemStack = itemStacks.get(i);
            for (int j = 0; j < this.itemHandler.getSlots(); j++) {
                if (checkedIndices.contains(j) || emptyIndices.contains(j))
                    continue;

                ItemStack testItemStack = this.itemHandler.getStackInSlot(j);
                if (testItemStack.isEmpty()) {
                    emptyIndices.add(j);
                    continue;
                }

                if (ItemStack.isSameItemSameTags(itemStack, testItemStack)) {
                    int amount = Math.min(itemStack.getCount(), testItemStack.getMaxStackSize() - testItemStack.getCount());

                    if (amount + testItemStack.getCount() == testItemStack.getMaxStackSize())
                        checkedIndices.add(j);

                    if (amount == itemStack.getCount()) {
                        itemStacks.remove(i);

                        continue outer;
                    } else {
                        itemStack.shrink(amount);
                    }
                }
            }

            //Leftover -> put in empty slot
            if (emptyIndices.isEmpty())
                return false;

            int index = emptyIndices.remove(0);
            if (itemStack.getCount() == itemStack.getMaxStackSize())
                checkedIndices.add(index);

            itemStacks.remove(i);
        }
        return itemStacks.isEmpty();
    }

    private void extractItems() {
        SimpleContainer patternSlotsForRecipe = this.patternSlots;

        List<ItemStack> patternItemStacks = new ArrayList<>(9);
        for (int i = 0; i < patternSlotsForRecipe.getContainerSize(); i++)
            if (!patternSlotsForRecipe.getItem(i).isEmpty())
                patternItemStacks.add(patternSlotsForRecipe.getItem(i));

        List<ItemStack> itemStacksExtract = ItemStackUtils.combineItemStacks(patternItemStacks);

        for (ItemStack itemStack : itemStacksExtract) {
            for (int i = 0; i < this.itemHandler.getSlots(); i++) {
                ItemStack testItemStack = this.itemHandler.getStackInSlot(i);
                if (ItemStack.isSameItemSameTags(itemStack, testItemStack)) {
                    ItemStack ret = this.itemHandler.extractItem(i, itemStack.getCount(), false);
                    if (!ret.isEmpty()) {
                        int amount = ret.getCount();
                        if (amount == itemStack.getCount())
                            break;

                        itemStack.shrink(amount);
                    }
                }
            }
        }
    }

    private void updateRecipe() {
        if (this.level == null)
            return;

        CraftingRecipe oldRecipe = null;
        ItemStack oldResult = null;

        // Check if the possible recipe was already loaded
        if (this.hasRecipeLoaded && this.craftingRecipe != null && this.oldCopyOfRecipe != null) {
            oldRecipe = this.craftingRecipe;

            oldResult = this.craftingRecipe instanceof CustomRecipe ? this.craftingRecipe.assemble(this.oldCopyOfRecipe, this.level.registryAccess()) :
                    this.craftingRecipe.getResultItem(this.level.registryAccess());
        }

        this.hasRecipeLoaded = true;

        // Copy pattern to local variable, for shorting
        SimpleContainer patternSlotsForRecipe = this.patternSlots;
        // Copy of the pattern slots
        CraftingContainer copyOfPatternSlots = new TransientCraftingContainer(this.dummyContainerMenu, 3, 3);

        for (int i = 0; i < patternSlotsForRecipe.getContainerSize(); i++)
            copyOfPatternSlots.setItem(i, patternSlotsForRecipe.getItem(i));

        Optional<Pair<ResourceLocation, CraftingRecipe>> recipe = getRecipeFor(copyOfPatternSlots, this.level, this.recipeIdForSetRecipe);

        if (recipe.isPresent()) {
            this.craftingRecipe = recipe.get().getSecond();

            //Recipe with saved recipe id does not exist or pattern items are not compatible with recipe
            if (this.recipeIdForSetRecipe != null && !Objects.equals(this.craftingRecipe.getId(), this.recipeIdForSetRecipe)) {
                this.recipeIdForSetRecipe = this.craftingRecipe.getId();
                resetProgress();
            }

            ItemStack resultItemStack = this.craftingRecipe instanceof CustomRecipe ? this.craftingRecipe.assemble(copyOfPatternSlots, level.registryAccess()) :
                    this.craftingRecipe.getResultItem(this.level.registryAccess());

            this.patternResultSlots.setItem(0, resultItemStack);

            if (oldRecipe != null && this.oldCopyOfRecipe != null && (this.craftingRecipe != oldRecipe || !ItemStack.isSameItemSameTags(resultItemStack, oldResult)))
                resetProgress();

            this.oldCopyOfRecipe = new TransientCraftingContainer(this.dummyContainerMenu, 3, 3);

            for (int i = 0; i < patternSlotsForRecipe.getContainerSize(); i++)
                this.oldCopyOfRecipe.setItem(i, copyOfPatternSlots.getItem(i).copy());

        } else {
            this.recipeIdForSetRecipe = null;

            this.craftingRecipe = null;

            this.patternResultSlots.setItem(0, ItemStack.EMPTY);

            this.oldCopyOfRecipe = null;

            resetProgress();
        }
    }

    private Optional<Pair<ResourceLocation, CraftingRecipe>> getRecipeFor(CraftingContainer patternSlots, Level level, ResourceLocation recipeId) {
        // List of possible recipes for the pattern
        List<CraftingRecipe> recipes = getRecipesFor(patternSlots, level);
        Optional<CraftingRecipe> recipe = recipes.stream().filter(r -> r.getId().equals(recipeId)).findFirst();

        return recipe.or(() -> recipes.stream().findFirst()).map(r -> Pair.of(r.getId(), r));
    }

    private void resetProgress() {
        this.progress = 0;
    }

    private void craftItem(CraftingContainer copyOfPatternSlots) {
        if (this.craftingRecipe == null) {
            resetProgress();

            return;
        }

        List<ItemStack> outputItemStacks = new ArrayList<>(10);

        // If it is an instance of custom recipe, craft item, else, get result item
        ItemStack resultItemStack = this.craftingRecipe instanceof CustomRecipe ? this.craftingRecipe.assemble(copyOfPatternSlots, this.level.registryAccess()) :
                this.craftingRecipe.getResultItem(this.level.registryAccess());

        outputItemStacks.add(resultItemStack);

        // If there are still enough items craft item based on the pattern and add to stack
        for (ItemStack remainingItem : this.craftingRecipe.getRemainingItems(copyOfPatternSlots))
            if (!remainingItem.isEmpty())
                outputItemStacks.add(remainingItem);

        // Combines item stacks
        List<ItemStack> itemStacksInsert = ItemStackUtils.combineItemStacks(outputItemStacks);

        List<Integer> emptyIndices = new ArrayList<>(18);

        // "outer:" is a label for loop, see java documentation
        outer:
        for (ItemStack itemStack : itemStacksInsert) {
            for (int i = 0; i < this.itemHandler.getSlots(); i++) {
                ItemStack testItemStack = this.itemHandler.getStackInSlot(i);
                if (emptyIndices.contains(i))
                    continue;

                if (testItemStack.isEmpty()) {
                    emptyIndices.add(i);
                    continue;
                }

                if (ItemStack.isSameItemSameTags(itemStack, testItemStack)) {
                    int amount = Math.min(itemStack.getCount(), testItemStack.getMaxStackSize() - testItemStack.getCount());
                    if (amount > 0) {
                        this.itemHandler.setStackInSlot(i, this.itemHandler.getStackInSlot(i).copyWithCount(testItemStack.getCount() + amount));

                        itemStack.setCount(itemStack.getCount() - amount);

                        if (itemStack.isEmpty())
                            continue outer;
                    }
                }
            }

            //Leftover -> put in empty slot
            if (emptyIndices.isEmpty())
                continue; //Should not happen

            this.itemHandler.setStackInSlot(emptyIndices.remove(0), itemStack);
        }

        resetProgress();
    }

    private List<CraftingRecipe> getRecipesFor(CraftingContainer patternSlots, Level level) {
        return level.getRecipeManager().getAllRecipesFor(RecipeType.CRAFTING).
                stream().filter(recipe -> recipe.matches(patternSlots, level)).
                sorted(Comparator.comparing(recipe -> recipe.getResultItem(level.registryAccess()).getDescriptionId())).
                toList();
    }

    public void setRecipeIdForSetRecipe(ResourceLocation recipeIdForSetRecipe) {
        this.recipeIdForSetRecipe = recipeIdForSetRecipe;
        updateRecipe();
    }

    public void resetProgressAndMarkAsChanged() {
        resetProgress();
        setChanged(level, getBlockPos(), getBlockState());
    }

    public void cycleRecipe() {
        SimpleContainer patternSlotsForRecipe = this.patternSlots;
        CraftingContainer copyOfPatternSlots = new TransientCraftingContainer(this.dummyContainerMenu, 3, 3);
        for (int i = 0; i < patternSlotsForRecipe.getContainerSize(); i++)
            copyOfPatternSlots.setItem(i, patternSlotsForRecipe.getItem(i));

        List<CraftingRecipe> recipes = getRecipesFor(copyOfPatternSlots, this.level);

        //No recipe found
        if (recipes.isEmpty()) {
            updateRecipe();
            return;
        }

        if (this.recipeIdForSetRecipe == null)
            this.recipeIdForSetRecipe = (this.craftingRecipe == null || this.craftingRecipe.getId() == null) ? recipes.get(0).getId() :
                    this.craftingRecipe.getId();

        for (int i = 0; i < recipes.size(); i++) {
            if (Objects.equals(recipes.get(i).getId(), this.recipeIdForSetRecipe)) {
                this.recipeIdForSetRecipe = recipes.get((i + 1) % recipes.size()).getId();
                break;
            }
        }
        updateRecipe();
    }

    public void drops(Level level, BlockPos worldPosition) {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++)
            inventory.setItem(i, itemHandler.getStackInSlot(i));

        Containers.dropContents(level, worldPosition, inventory);
    }
}
