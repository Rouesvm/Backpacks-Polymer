package com.rouesvm.servback.technical.cosmetic;

import com.rouesvm.servback.content.item.impl.ContainerItem;
import com.rouesvm.servback.registry.BackpackDataComponentTypes;
import com.rouesvm.servback.registry.item.BackpackItemJsonRegistry;
import com.rouesvm.servback.technical.config.BackpackItemConfiguration;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.VirtualEntityUtils;
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import eu.pb4.polymer.virtualentity.api.elements.VirtualElement;
import eu.pb4.polymer.virtualentity.impl.EntityExt;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BundleS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitiesDestroyS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

import java.util.Objects;

public class BackHolder extends ElementHolder {
    private final LivingEntity entity;
    private final ItemDisplayElement element;

    private final Vector3f position = new Vector3f();

    private Vector3f cosmeticPosition = new Vector3f(0f, -0.65f, 0.28f);
    private Integer cosmeticRotation = 180;
    private Integer cosmeticPitchWhenSneaking = -25;

    private boolean hidden;

    private BackHolder(ItemStack stack, LivingEntity entity) {
        super();

        this.entity = entity;
        this.element = new ItemDisplayElement();

        ItemStack copy = stack.copy();
        copy.set(BackpackDataComponentTypes.IS_3D, true);

        this.element.setItem(copy);

        this.element.setTranslation(new Vector3f(0, 0.25f, 0));
        this.element.setScale(new Vector3f(0.875f));

        this.element.setTeleportDuration(1);
        this.element.ignorePositionUpdates();

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
        if (entity.isDead() || entity.isRemoved()) {
            destroy();
        }

        boolean facingDown = entity.getFacing() == Direction.DOWN;
        boolean isSpectator = entity instanceof ServerPlayerEntity serverPlayer && serverPlayer.isSpectator();

        EntityPose pose = entity.getPose();
        boolean isHiddenPose = facingDown || pose == EntityPose.SWIMMING || pose == EntityPose.SLEEPING || isSpectator;

        if (isHiddenPose) {
            if (!hidden) {
                hideForAll(this);
                hidden = true;
            }
        } else {
            if (hidden) {
                showForAll(this);
                updatePosition();
                sendRidePacket();
                hidden = false;
            }

            boolean sneaking = entity.isSneaking();

            this.element.setYaw(((BackInterface) entity).backpacks$bodyYaw() - cosmeticRotation);
            this.element.setPitch(sneaking ? cosmeticPitchWhenSneaking : 0);

            float y = sneaking ? cosmeticPosition.y - 0.02f : cosmeticPosition.y;
            float z = sneaking ? cosmeticPosition.z - 0.10f : cosmeticPosition.z;

            if (!entity.getEquippedStack(EquipmentSlot.CHEST).isEmpty()) {
                z += 0.05f;
            }

            position.set(0, y, z);
            this.element.setTranslation(position);
        }
    }

    private void sendRidePacket() {
        var packet = VirtualEntityUtils.createRidePacket(
                entity.getId(),
                ((EntityExt) entity).polymerVE$getVirtualRidden()
        );
        this.sendPacket(packet);
    }

    @Override
    protected void notifyElementsOfPositionUpdate(Vec3d newPos, Vec3d delta) {
    }

    public static BackHolder createDisplay(ItemStack stack, ServerPlayerEntity entity) {
        var model = new BackHolder(stack.copy(), entity);

        EntityAttachment.ofTicking(model, entity);
        model.startWatching(entity);

        VirtualEntityUtils.addVirtualPassenger(entity, model.getEntityIds().toIntArray());

        var packet = VirtualEntityUtils.createRidePacket(entity.getId(), ((EntityExt)entity).polymerVE$getVirtualRidden());
        model.sendPacket(packet);

        return model;
    }

    public static void hideForAll(ElementHolder elementHolder) {
        for (ServerPlayNetworkHandler player : elementHolder.getWatchingPlayers()) {
            player.sendPacket(new EntitiesDestroyS2CPacket(elementHolder.getEntityIds()));
        }
    }

    public static void showForAll(ElementHolder elementHolder) {
        for (ServerPlayNetworkHandler player : elementHolder.getWatchingPlayers()) {
            var packets = new ObjectArrayList<Packet<? super ClientPlayPacketListener>>();
            for (VirtualElement e : elementHolder.getElements()) {
                Objects.requireNonNull(packets);
                e.startWatching(player.player, packets::add);
            }
            player.sendPacket(new BundleS2CPacket(packets));
        }
    }
}
