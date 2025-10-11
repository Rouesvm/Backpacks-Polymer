package com.rouesvm.servback.content.block.impl;

import com.rouesvm.servback.content.block.BasicBackpackBlockEntity;
import com.rouesvm.servback.content.block.TickableBlockEntity;
import com.rouesvm.servback.content.component.UpgradeContainerComponent;
import com.rouesvm.servback.content.upgrade.Upgrade;
import com.rouesvm.servback.registry.BackpackDataComponentTypes;
import com.rouesvm.servback.registry.block.BackpackBlockEntityRegistry;
import com.rouesvm.servback.technical.BackpackUtils;
import com.rouesvm.servback.technical.data.BackpackInstance;
import com.rouesvm.servback.technical.manager.BackpackManager;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
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
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.rouesvm.servback.ServerBackpacks.CAPACITY;

public class BackpackBlockEntity extends BasicBackpackBlockEntity implements TickableBlockEntity {
    private UUID uuid;

    private int extraSize = 0;

    private BackpackInstance instance = null;
    private SlottedStorage<ItemVariant> storage;

    private List<Upgrade> upgradeList;

    public BackpackBlockEntity(BlockPos pos, BlockState state) {
        super(BackpackBlockEntityRegistry.BACKPACK_BLOCK_ENTITY, pos, state);
    }

    @Override
    public void tick(World world, BlockPos pos, BlockState state, BlockEntity blockEntity) {
        if (uuid == null) return;
        if (upgradeList != null && !upgradeList.isEmpty()) upgradeList.forEach((upgrade) ->
                upgrade.tick(world, pos.toCenterPos(), instance.inventory())
        );
    }

    @Override
    protected void writeNbt(NbtCompound view, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(view, registryLookup);
        view.putInt("extraSize", extraSize);
        if (uuid != null) view.putString("uuid", uuid.toString());
        if (upgradeList != null) view.getListAppender("upgrade", UpgradeContainerComponent.CODEC).add(UpgradeContainerComponent.of(upgradeList));
    }

    @Override
    protected void readNbt(NbtCompound view, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(view, registryLookup);
        extraSize = view.getInt("extraSize", 0);
        uuid = UUID.fromString(view.getString("uuid"));

        var upgradeContainer = view.getTypedListView("upgrade", UpgradeContainerComponent.CODEC);
        Optional<UpgradeContainerComponent> upgradeContainerComponent = upgradeContainer.stream().findFirst();
        if (!upgradeContainer.isEmpty() && upgradeContainerComponent.isPresent()) {
            upgradeList = upgradeContainerComponent.get().getBaseUpgrades();
        }

        setStorage();
    }

    public ItemStack getDefaultStack() {
        ItemStack stack = super.getDefaultStack();
        if (uuid == null) return stack;

        DynamicRegistryManager registryManager = this.getWorld().getRegistryManager();
        Optional<RegistryEntry.Reference<Enchantment>> capacity = registryManager.getOptional(RegistryKeys.ENCHANTMENT).get().getEntry(CAPACITY);

        stack.addEnchantment(capacity.get(), extraSize / 9);
        stack.set(BackpackDataComponentTypes.BACKPACK_UUID, uuid);

        if (upgradeList != null) {
            stack.set(BackpackDataComponentTypes.UPGRADE_CONTAINER, UpgradeContainerComponent.of(upgradeList));
        }

        BackpackUtils.addCustomData((ServerWorld) world, stack);
        return stack;
    }

    public void setUpgradeList(List<Upgrade> upgradeList) {
        this.upgradeList = upgradeList;
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
