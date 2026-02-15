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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.*;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public class JukeboxUpgrade extends Upgrade implements ClickableUpgrade, PersistentUpgrade {
    private SoundAttacher attacher;
    private JukeboxSong song;

    private int tick = 0;
    private boolean playing = false;

    private boolean wasDropped = false;
    private boolean blockEntity = false;

    public JukeboxUpgrade() {
        super(BackpackUpgradeRegistry.JUKEBOX);
    }

    @Override
    public boolean onClicked(ServerPlayer serverPlayer, ItemStack stack, Slot slot, ClickAction clickType, boolean inContainer) {
        return openGui(serverPlayer, clickType == ClickAction.SECONDARY, inContainer);
    }

    @Override
    public void tick(ServerPlayer player, ItemStack stack, ServerLevel world, Vec3 pos, BackpackInventory inventory) {
        if (playing) {
            if (song != null && song.hasFinished(tick)
            ) tick = 0;

            if (song != null && attacher != null && tick == 0
            ) this.attacher.play(song.soundEvent());

            tick++;
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

    public boolean openGui(ServerPlayer serverPlayer, boolean isRight, boolean inContainer) {
        if (song == null) {
            song = new JukeboxPlayable(new EitherHolder(JukeboxSongs.CREATOR_MUSIC_BOX)).song().unwrap(serverPlayer.level().registryAccess())
                    .orElse(null).value();
        }

        if (this.attacher == null) {
            this.attacher = new SoundAttacher();
            EntityAttachment.ofTicking(attacher, serverPlayer);
            this.attacher.startWatching(serverPlayer);
        }

        if (!playing) {
            playing = true;
            this.attacher.play(song.soundEvent());
        } else {
            playing = false;
            this.attacher.stop();
        }

        return false;
    }

    @Override
    public void readView(ValueInput data) {

    }

    @Override
    public void writeView(ValueOutput data) {

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
