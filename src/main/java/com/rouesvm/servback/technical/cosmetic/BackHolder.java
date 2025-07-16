package com.rouesvm.servback.technical.cosmetic;

import com.rouesvm.servback.content.item.impl.ContainerItem;
import com.rouesvm.servback.registry.item.BackpackItemJsonRegistry;
import com.rouesvm.servback.technical.config.Configuration;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.VirtualEntityUtils;
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import eu.pb4.polymer.virtualentity.api.elements.VirtualElement;
import eu.pb4.polymer.virtualentity.impl.EntityExt;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
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

import java.util.List;
import java.util.Objects;

public class BackHolder extends ElementHolder {
    private final LivingEntity entity;
    private final ItemDisplayElement element;

    private final Vector3f position = new Vector3f();

    private Vector3f cosmeticPosition = new Vector3f(0f, -0.65f, 0.28f);
    private Integer cosmeticRotation = 180;
    private Integer cosmeticPitchWhenSneaking = -25;

    private boolean hidden;
    private boolean hideFromPlayer;

    private BackHolder(ItemStack stack, LivingEntity entity) {
        super();

        this.entity = entity;
        this.element = new ItemDisplayElement();

        CustomModelDataComponent component = new CustomModelDataComponent(List.of(), List.of(), List.of("model"), List.of());
        stack.set(DataComponentTypes.CUSTOM_MODEL_DATA, component);

        this.element.setItem(stack);

        this.element.setTranslation(new Vector3f(0, 0.25f, 0));
        this.element.setScale(new Vector3f(0.875f));

        this.element.setTeleportDuration(1);
        this.element.ignorePositionUpdates();

        if (this.element.getItem().getItem() instanceof ContainerItem item) {
            int order = BackpackItemJsonRegistry.getBackpackUpgradeOrder(item.getSize());

            Configuration.Instance instance = Configuration.instance();
            cosmeticPosition = instance.back_positions.get(order);
            cosmeticRotation = instance.back_yaw.get(order);
            cosmeticPitchWhenSneaking = instance.back_pitch_when_sneaking.get(order);
        }

        this.addElement(this.element);
    }

    @Override
    protected void onTick() {
        if (entity.isDead() || entity.isRemoved()) {
            destroy();
            return;
        }

        boolean facingDown = entity.getFacing() == Direction.DOWN;
        boolean isSpectator = entity instanceof ServerPlayerEntity serverPlayer && serverPlayer.isSpectator();

        EntityPose pose = entity.getPose();
        boolean isHiddenPose = pose == EntityPose.SWIMMING || pose == EntityPose.SLEEPING || isSpectator;

        if (facingDown) {
            if (!hideFromPlayer && entity instanceof ServerPlayerEntity player) {
                stopWatching(player);
                hideFromPlayer = true;
            }
        } else if (hideFromPlayer && entity instanceof ServerPlayerEntity player) {
            startWatching(player);
            updatePosition();
            sendRidePacket();
            hideFromPlayer = false;
        }

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

            this.element.setYaw(entity.getBodyYaw() - cosmeticRotation);
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
