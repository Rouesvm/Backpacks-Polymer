package com.rouesvm.servback.utils.cosmetic;

import com.rouesvm.servback.Main;
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
    private final boolean model3D = Main.configuration.getInstance().display_3d_model_on_back;

    private final LivingEntity entity;
    private final ItemDisplayElement element;

    private boolean hidden;
    private boolean hideFromPlayer;

    private BackHolder(ItemStack stack, LivingEntity entity) {
        super();

        this.entity = entity;
        this.element = new ItemDisplayElement();

        if (model3D) {
            CustomModelDataComponent component = new CustomModelDataComponent(List.of(), List.of(), List.of("model"), List.of());
            stack.set(DataComponentTypes.CUSTOM_MODEL_DATA, component);
        }

        this.element.setItem(stack);

        this.element.setTranslation(new Vector3f(0, 0.25f, 0));
        this.element.setScale(new Vector3f(0.625f));

        this.element.setTeleportDuration(1);
        this.element.ignorePositionUpdates();

        this.addElement(this.element);
    }

    // copy go brrr pls don't sue me
    @Override
    protected void onTick() {
        if (this.entity.isDead() || entity.isRemoved()) {
            destroy();
        }

        if (this.entity.getFacing() == Direction.DOWN) {
            hideFromPlayer = true;
            if (entity instanceof ServerPlayerEntity player)
                this.stopWatching(player);
        } else {
            if (hideFromPlayer && entity instanceof ServerPlayerEntity player) {
                this.startWatching(player);
                this.updatePosition();

                var packet = VirtualEntityUtils.createRidePacket(entity.getId(), ((EntityExt)entity).polymerVE$getVirtualRidden());
                this.sendPacket(packet);

                hideFromPlayer = false;
            }
        }

        if (this.entity.getPose() == EntityPose.SWIMMING
                || this.entity.getPose() == EntityPose.SLEEPING
                || (this.entity instanceof ServerPlayerEntity serverPlayer && serverPlayer.isSpectator()))
        {
            if (!hidden) {
                hideForAll(this);
                hidden = true;
            }
        } else {
            if (hidden) {
                showForAll(this);
                this.updatePosition();

                var packet = VirtualEntityUtils.createRidePacket(entity.getId(), ((EntityExt)entity).polymerVE$getVirtualRidden());
                this.sendPacket(packet);

                hidden = false;
            }

            boolean sneaking = this.entity.isSneaking();

            this.element.setYaw(model3D ? entity.getBodyYaw() - 180 : entity.getBodyYaw());
            this.element.setPitch(model3D ? sneaking ? -25 : 0 : sneaking ? 25 : 0);

            float yTranslation = sneaking ? -0.6f : -0.65f;
            float zTranslation;

            if (entity.getEquippedStack(EquipmentSlot.CHEST) != ItemStack.EMPTY) {
                if (model3D)
                    zTranslation = sneaking ? -0.225f : -0.468f;
                else zTranslation = sneaking ? 0.120f : 0.234f;
            } else {
                if (model3D)
                    zTranslation = sneaking ? 0.120f : 0.250f;
                else zTranslation = sneaking ? 0f : -0.125f;
            }

            this.element.setTranslation(new Vector3f(0, yTranslation, zTranslation));
        }
    }

    @Override
    protected void notifyElementsOfPositionUpdate(Vec3d newPos, Vec3d delta) {
    }

    public static BackHolder createDisplay(ItemStack stack, ServerPlayerEntity entity) {
        var model = new BackHolder(stack, entity);

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
