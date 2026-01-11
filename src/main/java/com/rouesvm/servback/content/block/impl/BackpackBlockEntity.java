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
import com.rouesvm.servback.technical.manager.BackpackUUID;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
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
    public void tick(Level world, BlockPos pos, BlockState state, BlockEntity blockEntity) {
        if (uuid == null) return;
        if (upgradeList != null && !upgradeList.isEmpty()) upgradeList.forEach((upgrade) ->
                upgrade.tick(world, pos.getCenter(), instance.inventory())
        );
    }

    @Override
    protected void saveAdditional(ValueOutput view) {
        super.saveAdditional(view);
        view.putInt("extraSize", extraSize);
        if (uuid != null) view.putString("uuid", uuid.toString());
        if (upgradeList != null) view.list("upgrade", UpgradeContainerComponent.CODEC).add(UpgradeContainerComponent.of(upgradeList));
    }

    @Override
    protected void loadAdditional(ValueInput view) {
        super.loadAdditional(view);
        extraSize = view.getIntOr("extraSize", 0);
        uuid = UUID.fromString(view.getStringOr("uuid", BackpackUUID.generateUniqueUUID().toString()));

        var upgradeContainer = view.listOrEmpty("upgrade", UpgradeContainerComponent.CODEC);
        Optional<UpgradeContainerComponent> upgradeContainerComponent = upgradeContainer.stream().findFirst();
        if (!upgradeContainer.isEmpty() && upgradeContainerComponent.isPresent()) {
            upgradeList = upgradeContainerComponent.get().baseUpgrades();
        }

        setStorage();
    }

    public ItemStack getDefaultStack() {
        ItemStack stack = super.getDefaultStack();
        if (uuid == null) return stack;

        RegistryAccess registryManager = this.getLevel().registryAccess();
        Holder.Reference<Enchantment> capacity = registryManager.lookup(Registries.ENCHANTMENT).get().getOrThrow(CAPACITY);

        stack.enchant(capacity, extraSize / 9);
        stack.set(BackpackDataComponentTypes.BACKPACK_UUID, uuid);

        if (upgradeList != null) {
            stack.set(BackpackDataComponentTypes.UPGRADE_CONTAINER, UpgradeContainerComponent.of(upgradeList));
        }

        BackpackUtils.addCustomData((ServerLevel) level, stack);
        return stack;
    }

    public void setUpgradeList(List<Upgrade> upgradeList) {
        this.upgradeList = upgradeList;
    }

    public void setStorage() {
        if (instance == null) instance = BackpackManager.getInstanceAndResize(uuid, extraSize + getSize()).get();
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
