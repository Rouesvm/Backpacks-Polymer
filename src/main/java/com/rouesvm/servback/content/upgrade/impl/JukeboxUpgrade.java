package com.rouesvm.servback.content.upgrade.impl;

import com.rouesvm.servback.content.upgrade.ClickableUpgrade;
import com.rouesvm.servback.content.upgrade.PersistentUpgrade;
import com.rouesvm.servback.content.upgrade.Upgrade;
import com.rouesvm.servback.registry.BackpackUpgradeRegistry;
import com.rouesvm.servback.technical.ui.inventory.BackpackInventory;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.VirtualEntityUtils;
import eu.pb4.polymer.virtualentity.api.attachment.ChunkAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.JukeboxPlayable;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;

public class JukeboxUpgrade extends Upgrade implements ClickableUpgrade, PersistentUpgrade {
    private SoundAttacher attacher;
    private JukeboxSong song;

    private int tick = 0;

    private boolean playing = false;
    private boolean looped = false;

    private boolean wasDropped = false;
    private boolean blockEntity = false;

    public ItemStack musicDisc;

    public JukeboxUpgrade() {
        super(BackpackUpgradeRegistry.JUKEBOX);
    }

    @Override
    public void tick(ServerPlayer player, ItemStack stack, ServerLevel world, Vec3 pos, BackpackInventory inventory) {
        if (song == null && musicDisc != null && !musicDisc.isEmpty() && world != null) {
            JukeboxPlayable jukebox = musicDisc.get(DataComponents.JUKEBOX_PLAYABLE);
            if (jukebox != null) song = jukebox.song().unwrap(world.registryAccess())
                    .orElse(null).value();
        }

        if (playing) {
            if (song != null && attacher != null && tick == 0
            ) this.attacher.play(song.soundEvent());

            tick++;

            if (song != null && song.hasFinished(tick)
            ) {
                tick = 0;
                playing = looped;
            }
        } else tick = 0;

        if (player == null && stack == null) {
            if (song != null && !blockEntity) {
                if (this.attacher != null) {
                    this.attacher.stop();
                    this.attacher.destroy();
                }

                this.attacher = new SoundAttacher();
                ChunkAttachment.ofTicking(attacher, world, pos);
                tick = 0;
            }

            blockEntity = true;
            return;
        }  else {
            blockEntity = false;
        }

        if (song != null && playing && !wasDropped && player == null) {
            tick = 0;
            playing = false;
            wasDropped = true;

            if (this.attacher != null) {
                this.attacher.stop();
                this.attacher.destroy();
                this.attacher = null;
            }
        }

        if (song != null && !playing && wasDropped && player != null) {
            wasDropped = false;
            playing = true;

            if (attacher == null) {
                this.attacher = new SoundAttacher();
                EntityAttachment.ofTicking(attacher, player);
                this.attacher.startWatching(player);
            }

            tick = 0;
        }
    }

    @Override
    public void addTooltip(List<Component> tooltip, ItemStack stack, PacketContext context) {
        if (!playing) tooltip.add(Component.translatable("info.serverbackpacks.stopped"));
        if (playing && looped)
            tooltip.add(Component.translatable("info.serverbackpacks.looped"));
        else if (playing) tooltip.add(Component.translatable("info.serverbackpacks.playing"));

        if (song != null) tooltip.add(song.description());
    }

    @Override
    public boolean onUsed(Level world, ServerPlayer player, ItemStack stack) {
        if (!playing) {
            playing = true;
            tick = 0;
            return true;
        } else {
            if (!looped) {
                looped = true;
                return true;
            }
            looped = false;
            playing = false;
            this.attacher.stop();
            return true;
        }
    }

    @Override
    public boolean onClicked(
            ServerPlayer serverPlayer,
            ItemStack stack,
            ItemStack otherStack,
            Slot slot,
            ClickAction clickType,
            boolean inContainer
    ) {
        if (this.attacher == null) {
            this.attacher = new SoundAttacher();
            EntityAttachment.ofTicking(attacher, serverPlayer);
            this.attacher.startWatching(serverPlayer);
        }

        if ((clickType == ClickAction.SECONDARY) == inContainer) return false;

        if (otherStack != null && !otherStack.isEmpty()) {
            musicDisc = otherStack.has(DataComponents.JUKEBOX_PLAYABLE) ? otherStack.copyAndClear() : null;

            if (song == null && musicDisc != null && !musicDisc.isEmpty()) {
                JukeboxPlayable jukebox = musicDisc.get(DataComponents.JUKEBOX_PLAYABLE);
                if (jukebox != null) song = jukebox.song().unwrap(serverPlayer.level().registryAccess())
                        .orElse(null).value();
            }

            return true;
        } else if (musicDisc != null &&
                !musicDisc.isEmpty() &&
                !ItemStack.isSameItem(serverPlayer.getInventory().getSelectedItem(), stack)
        ) {
            serverPlayer.getInventory().add(musicDisc.copyAndClear());
            song = null;
            playing = false;
            looped = false;
            tick = 0;
            return true;
        }

        if (!playing) {
            playing = true;
            tick = 0;
            return true;
        } else {
            if (!looped) {
                looped = true;
                return true;
            }
            looped = false;
            playing = false;
            this.attacher.stop();
            return true;
        }
    }

    @Override
    public void readView(ValueInput data) {
        playing = data.getBooleanOr("playing", false);
        looped = data.getBooleanOr("looped", false);

        data.read("song", JukeboxSong.CODEC).ifPresent(song -> this.song = song.value());
        musicDisc = data.read("itemStack", ItemStack.CODEC).orElse(null);
    }

    @Override
    public void writeView(ValueOutput data) {
        data.putBoolean("playing", playing);
        data.putBoolean("looped", looped);
        if (song != null) data.store("song", JukeboxSong.CODEC, Holder.direct(song));
        if (musicDisc != null) data.store("itemStack", ItemStack.CODEC, musicDisc);
    }

    private static class SoundAttacher extends ElementHolder {
        private final ItemDisplayElement element = new ItemDisplayElement();

        private SoundAttacher() {
            this.element.setInvisible(true);
            this.element.setTeleportDuration(3);
        }

        public void play(Holder<SoundEvent> event) {
            this.stop();
            this.addElement(element);
            this.sendPacket(VirtualEntityUtils.createPlaySoundFromEntityPacket(
                    this.element.getEntityId(),
                    event,
                    SoundSource.RECORDS,
                    1.0f,
                    1.0f,
                    0L
            ));
        }

        public void stop() {
            this.removeElement(element);
        }
    }
}
