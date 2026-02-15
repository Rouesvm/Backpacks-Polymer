package com.rouesvm.servback.mixin;

import com.rouesvm.servback.content.component.UpgradeContainerComponent;
import com.rouesvm.servback.content.item.impl.ContainerItem;
import com.rouesvm.servback.registry.BackpackDataComponentTypes;
import com.rouesvm.servback.technical.BackpackGlobalLinker;
import com.rouesvm.servback.technical.manager.BackpackManager;
import com.rouesvm.servback.technical.manager.BackpackUUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public class ItemEntityMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    public void tick(CallbackInfo ci) {
        ItemEntity source = (ItemEntity) (Object) this;
        Level world = source.level();
        BackpackGlobalLinker.testLink(source, world);

        if (source.getItem().getItem() instanceof ContainerItem) {
            UpgradeContainerComponent component = source.getItem().get(BackpackDataComponentTypes.UPGRADE_CONTAINER);
            if (component != null) {
                component.baseUpgrades().forEach(upgrade ->
                        upgrade.tick(null, source.getItem(), (ServerLevel) world, source.position(), BackpackManager.getInventory(BackpackUUID.getStackUUID(source.getItem())))
                );
            }
        }
    }
}
