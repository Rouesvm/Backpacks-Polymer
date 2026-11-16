package com.rouesvm.servback.technical.cosmetic;

import com.rouesvm.servback.content.item.impl.ContainerItem;
import com.rouesvm.servback.registry.BackpackPersistentDataAttachmentRegistry;
import com.rouesvm.servback.registry.item.BackpackItemJsonRegistry;
import com.rouesvm.servback.technical.config.BackpackItemConfiguration;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.VirtualEntityUtils;
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import eu.pb4.polymer.virtualentity.api.elements.VirtualElement;
import eu.pb4.polymer.virtualentity.impl.EntityExt;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.List;
import java.util.Objects;

public class BackHolder extends ElementHolder {
    private final LivingEntity entity;
    private final ItemDisplayElement element;

    private final Vector3f position = new Vector3f();

    private Vector3f cosmeticPosition = new Vector3f(0f, -0.65f, 0.28f);
    private Integer cosmeticRotation = 180;
    private Integer cosmeticPitchWhenSneaking = -25;

    private boolean hiddenForAll;
    private boolean hiddenForSelf;

    private BackHolder(ItemStack stack, LivingEntity entity) {
        super();

        this.entity = entity;
        this.element = new ItemDisplayElement();

        CustomModelData component = new CustomModelData(List.of(), List.of(), List.of("model"), List.of());
        stack.set(DataComponents.CUSTOM_MODEL_DATA, component);

        this.element.setItem(stack);

        this.element.setTranslation(entity.getEyePosition().toVector3f());
        this.element.setScale(new Vector3f(0.875f));

        this.element.setTeleportDuration(1);

        if (this.element.getItem().getItem() instanceof ContainerItem item) {
            BackpackItemConfiguration.BackCosmetic order = BackpackItemJsonRegistry.getBackpackCosmetic(item);
            cosmeticPosition = order.offset();
            cosmeticRotation = order.yaw();
            cosmeticPitchWhenSneaking = order.pitch_while_sneaking();
        }

        this.addElement(this.element);
    }

    @Override
    protected void onTick() {
        if (entity.isDeadOrDying() || entity.isRemoved()) {
            destroy();
            return;
        }

        boolean hideWhenLookingDown = entity.getAttachedOrElse(BackpackPersistentDataAttachmentRegistry.HIDE_BACKPACK_WHEN_LOOKING_DOWN, true);
        boolean selfVisible = entity.getAttachedOrElse(BackpackPersistentDataAttachmentRegistry.BACKPACK_SELF_VISIBILITY, true);

        boolean facingDown = entity.getNearestViewDirection() == Direction.DOWN;
        boolean isSpectator = entity instanceof ServerPlayer player && player.isSpectator();

        Pose pose = entity.getPose();

        boolean shouldHideForSelf = (!selfVisible || (hideWhenLookingDown && facingDown));
        boolean shouldHideForAll = pose == Pose.SWIMMING || pose == Pose.SLEEPING || isSpectator;

        if (shouldHideForSelf && !hiddenForSelf && entity instanceof ServerPlayer player) {
            hideForSelf(player, this);
            hiddenForSelf = true;
        } else if (!shouldHideForSelf && hiddenForSelf && entity instanceof ServerPlayer player) {
            showForSelf(player, this);
            hiddenForSelf = false;
            this.element.setTranslation(entity.getEyePosition().toVector3f());
            updatePosition();
            sendRidePacket();
        }

        if (shouldHideForAll && !hiddenForAll) {
            hideForAll(this);
            hiddenForAll = true;
        } else if (!shouldHideForAll && hiddenForAll) {
            showForAll(this);
            hiddenForAll = false;
            this.element.setTranslation(entity.getEyePosition().toVector3f());
            updatePosition();
            sendRidePacket();
        }

        if (!hiddenForAll) {
            boolean sneaking = entity.isShiftKeyDown();

            this.element.setYaw(((BackInterface) entity).backpacks$bodyYaw() - cosmeticRotation);
            this.element.setPitch(sneaking ? cosmeticPitchWhenSneaking : 0);

            float y = cosmeticPosition.y - (sneaking ? -0.05f : 0f);
            float z = cosmeticPosition.z - (sneaking ? 0.2f : 0f);

            if (!entity.getItemBySlot(EquipmentSlot.CHEST).isEmpty()) {
                z += 0.05f;
            }

            position.set(0, y, z);
            this.element.setTranslation(position);
        }
    }

    private void sendRidePacket() {
        var packet = VirtualEntityUtils.createClientboundSetPassengersPacket(
                entity.getId(),
                ((EntityExt) entity).polymerVE$getVirtualRidden()
        );
        this.sendPacket(packet);
    }

    @Override
    protected void notifyElementsOfPositionUpdate(Vec3 newPos, Vec3 delta) {
    }

    public static BackHolder createDisplay(ItemStack stack, ServerPlayer entity) {
        var model = new BackHolder(stack.copy(), entity);

        EntityAttachment.ofTicking(model, entity);
        model.startWatching(entity);

        VirtualEntityUtils.addVirtualPassenger(entity, model.getEntityIds().toIntArray());

        model.sendRidePacket();

        return model;
    }

    public static void hideForSelf(ServerPlayer player, ElementHolder elementHolder) {
        elementHolder.sendPacket(new ClientboundRemoveEntitiesPacket(elementHolder.getEntityIds()), (otherPlayer) -> player.getUUID() == otherPlayer.getUUID());
    }

    public static void showForSelf(ServerPlayer player, ElementHolder elementHolder) {
        var packets = new ObjectArrayList<Packet<? super ClientGamePacketListener>>();
        for (VirtualElement e : elementHolder.getElements()) {
            Objects.requireNonNull(packets);
            e.startWatching(player, packets::add);
        }
        elementHolder.sendPacket(new ClientboundBundlePacket(packets), (otherPlayer) -> player.getUUID() == otherPlayer.getUUID());
    }

    public static void hideForAll(ElementHolder elementHolder) {
        for (ServerGamePacketListenerImpl player : elementHolder.getWatchingPlayers()) {
            player.send(new ClientboundRemoveEntitiesPacket(elementHolder.getEntityIds()));
        }
    }

    public static void showForAll(ElementHolder elementHolder) {
        for (ServerGamePacketListenerImpl player : elementHolder.getWatchingPlayers()) {
            var packets = new ObjectArrayList<Packet<? super ClientGamePacketListener>>();
            for (VirtualElement e : elementHolder.getElements()) {
                Objects.requireNonNull(packets);
                e.startWatching(player.player, packets::add);
            }
            player.send(new ClientboundBundlePacket(packets));
        }
    }
}
