package com.rouesvm.servback.block;

import com.rouesvm.servback.item.ContainerItem;
import com.rouesvm.servback.registry.BackpackBlockEntityRegistry;
import com.rouesvm.servback.registry.BackpackDataComponentTypes;
import com.rouesvm.servback.utils.BackpackInstance;
import com.rouesvm.servback.utils.BackpackManager;
import com.rouesvm.servback.utils.BackpackUtils;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

import static com.rouesvm.servback.Main.CAPACITY;

public class BackpackBlockEntity extends BlockEntity {
    private UUID uuid;
    private int size = 9;
    private int extraSize = 0;

    private DyeColor color = DyeColor.BROWN;
    private Text customName;

    private BackpackInstance instance;
    private InventoryStorage storage;

    public BackpackBlockEntity(BlockPos pos, BlockState state) {
        super(BackpackBlockEntityRegistry.BACKPACK_BLOCK_ENTITY, pos, state);
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.writeNbt(nbt, registries);
        nbt.putInt("dye", color.getIndex());
        nbt.putInt("size", size);
        nbt.putInt("extraSize", extraSize);

        if (uuid != null) nbt.putString("uuid", uuid.toString());
        if (instance != null) BackpackManager.getManager().saveBackpack(instance);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.writeNbt(nbt, registries);
        color = DyeColor.byIndex(nbt.getInt("dye", DyeColor.BROWN.getIndex()));
        size = nbt.getInt("size", 9);
        extraSize = nbt.getInt("extraSize", 0);
        uuid = UUID.fromString(nbt.getString("uuid", UUID.randomUUID().toString()));
        setStorage();
    }

    public ItemStack getDefaultStack() {
        if (uuid == null) return ContainerItem.getDefaultBackpack(1).getDefaultStack();

        DynamicRegistryManager registryManager = this.getWorld().getRegistryManager();
        RegistryEntry.Reference<Enchantment> capacity = registryManager.getOptional(RegistryKeys.ENCHANTMENT).get().getOrThrow(CAPACITY);

        ItemStack stack = ContainerItem.getColoredBackpack(color, size / 9).getDefaultStack();
        stack.addEnchantment(capacity, extraSize / 9);
        stack.set(BackpackDataComponentTypes.UUID_TYPE, uuid.toString());

        BackpackUtils.addCustomData(stack, (ServerWorld) world);

        if (customName != null) stack.set(DataComponentTypes.CUSTOM_NAME, customName);

        return stack;
    }

    public void setStorage() {
        if (instance == null) instance = BackpackManager.getInstance(uuid, extraSize + size);
        if (instance != null && storage == null) storage = InventoryStorage.of(instance.getInventory(), null);
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
        setStorage();
    }

    public BackpackInstance getInstance() {
        return instance;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getExtraSize() {
        return extraSize;
    }

    public void setExtraSize(int extraSize) {
        this.extraSize = extraSize;
    }

    public void setColor(DyeColor color) {
        this.color = color;
    }

    public void setCustomName(Text customName) {
        this.customName = customName;
    }

    public @Nullable InventoryStorage getInventoryProvider(@Nullable Direction direction) {
        return storage;
    }
}
