package com.rouesvm.servback.content.block.backpack;

import com.rouesvm.servback.content.block.BasicBackpackBlockEntity;
import com.rouesvm.servback.content.registry.BackpackDataComponentTypes;
import com.rouesvm.servback.content.registry.block.BackpackBlockEntityRegistry;
import com.rouesvm.servback.technical.data.BackpackInstance;
import com.rouesvm.servback.technical.data.BackpackManager;
import com.rouesvm.servback.technical.data.BackpackUtils;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

import static com.rouesvm.servback.ServerBackpacks.CAPACITY;

public class BackpackBlockEntity extends BasicBackpackBlockEntity {
    private UUID uuid;

    private int extraSize = 0;

    private BackpackInstance instance = null;
    private SlottedStorage<ItemVariant> storage;

    public BackpackBlockEntity(BlockPos pos, BlockState state) {
        super(BackpackBlockEntityRegistry.BACKPACK_BLOCK_ENTITY, pos, state);
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.writeNbt(nbt, registries);
        nbt.putInt("extraSize", extraSize);

        if (uuid != null) nbt.putString("uuid", uuid.toString());
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);
        extraSize = nbt.getInt("extraSize");
        uuid = UUID.fromString(nbt.getString("uuid"));

        setStorage();
    }

    public ItemStack getDefaultStack() {
        if (uuid == null) return super.getDefaultStack();

        DynamicRegistryManager registryManager = this.getWorld().getRegistryManager();
        RegistryEntry.Reference<Enchantment> capacity = registryManager.get(RegistryKeys.ENCHANTMENT).entryOf(CAPACITY);

        ItemStack stack = super.getDefaultStack().copy();
        stack.addEnchantment(capacity, extraSize / 9);
        stack.set(BackpackDataComponentTypes.BACKPACK_UUID_TYPE, uuid);

        BackpackUtils.addCustomData((ServerWorld) world, stack);

        return stack;
    }

    public void setStorage() {
        if (instance == null) instance = BackpackManager.getInstance(uuid, extraSize + getSize()).get();
        if (storage == null) {
            instance.inventory().setEntity(this);
            storage = InventoryStorage.of(instance.inventory(), null);
        }
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
        if (instance == null) setStorage();
    }

    public BackpackInstance getInstance() {
        if (instance == null) setStorage();
        return instance;
    }

    public int getExtraSize() {
        return extraSize;
    }

    public void setExtraSize(int extraSize) {
        this.extraSize = extraSize;
    }

    public @Nullable Storage<ItemVariant> getInventoryProvider(@Nullable Direction ignoredDirection) {
        return storage;
    }
}
