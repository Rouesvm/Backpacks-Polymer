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
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class JukeboxUpgrade extends Upgrade implements ClickableUpgrade, PersistentUpgrade {
    private SoundAttacher attacher;
    private JukeboxSong song;

    private static final int DOUBLE_CLICK_COUNT = 3;

    private int tick = 0;
    private int clickTimer = 0;
    private int clickCounter = 0;

    private boolean playing = false;
    private boolean looped = false;

    private boolean wasDropped = false;
    private boolean blockEntity = false;

    public ItemStack musicDisc;

    public JukeboxUpgrade() {
        super(BackpackUpgradeRegistry.JUKEBOX);
    }

    @Override
    public void tick(@Nullable ServerPlayer player, @Nullable ItemStack stack, ServerLevel world, Vec3 pos, BackpackInventory inventory) {
        if (clickCounter > 0) {
            clickTimer++;

            if (clickTimer > DOUBLE_CLICK_COUNT) {
                clickCounter = 0;
                clickTimer = 0;
            }
        }

        if (song == null && musicDisc != null && !musicDisc.isEmpty() && world != null) {
            Optional<Holder<JukeboxSong>> jukebox = JukeboxSong.fromStack(musicDisc);
            jukebox.ifPresent(jukeboxSongHolder -> song = jukeboxSongHolder.value());
        }

        if (playing) {
            if (song != null && attacher != null && tick == 0
            ) this.attacher.play(song.soundEvent());

            if (song != null && attacher != null) {
                tick++;
            } else tick = 0;

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
                    this.attacher = null;
                }

                this.attacher = new SoundAttacher();
                ChunkAttachment.ofTicking(attacher, world, pos);
                tick = 0;
            }

            blockEntity = true;
            return;
        }  else {
            if (this.attacher == null && player != null) {
                this.attacher = new SoundAttacher();
                EntityAttachment.ofTicking(attacher, player);
                this.attacher.startWatching(player);
            }

            if (blockEntity) {
                if (this.attacher != null) {
                    this.attacher.stop();
                    this.attacher.destroy();
                    this.attacher = null;
                }

                blockEntity = false;
            }
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
            tick = 0;
        }
    }

    @Override
    public void addTooltip(List<Component> tooltip, ItemStack stack, PacketContext context) {
        MutableComponent component = Component.empty();

        if (!playing) component = (Component.translatable("info.serverbackpacks.stopped").withStyle(ChatFormatting.RED));
        if (playing && looped)
            component = (Component.translatable("info.serverbackpacks.looped").withStyle(ChatFormatting.GREEN));
        else if (playing) component = (Component.translatable("info.serverbackpacks.playing").withStyle(ChatFormatting.GREEN));

        tooltip.add(component.copy());
        if (song != null) tooltip.add(song.description().copy().withStyle(ChatFormatting.GRAY));
    }

    @Override
    public boolean onUsed(Level world, ServerPlayer player, ItemStack stack) {
        if (!playing) {
            playing = true;
            tick = 0;
        } else {
            if (!looped) {
                looped = true;
                return true;
            }
            looped = false;
            playing = false;
            if (attacher != null) this.attacher.stop();
        }

        return true;
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
        boolean doubleClick = false;

        if ((clickType == ClickAction.SECONDARY) == inContainer) return false;

        if (clickCounter == 1 && clickTimer <= DOUBLE_CLICK_COUNT) {
            doubleClick = true;
        }

        if (clickCounter == 0) {
            clickCounter = 1;
            clickTimer = 0;
        } else {
            clickCounter = 0;
        }

        if (otherStack != null && !otherStack.isEmpty()) {
            if (musicDisc != null && !musicDisc.isEmpty()) return true;
            musicDisc = otherStack.has(DataComponents.JUKEBOX_PLAYABLE) ? otherStack.copyAndClear() : null;
            if (song == null && musicDisc != null && !musicDisc.isEmpty()) {
                Optional<Holder<JukeboxSong>> jukebox = JukeboxSong.fromStack(musicDisc);
                jukebox.ifPresent(jukeboxSongHolder -> song = jukeboxSongHolder.value());

                clickCounter = 0;
                clickTimer = 0;
            }

            return true;
        } else if (
                doubleClick &&
                musicDisc != null &&
                !musicDisc.isEmpty() &&
                !ItemStack.isSameItem(serverPlayer.getInventory().getSelectedItem(), stack)
        ) {
            serverPlayer.getInventory().add(musicDisc.copyAndClear());
            song = null;
            playing = false;
            looped = false;
            tick = 0;
            if (attacher != null) this.attacher.stop();
            return true;
        }

        return onUsed(serverPlayer.level(), serverPlayer, stack);
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
            this.element.setInvisible(false);
            this.element.setTeleportDuration(3);
        }

        public void play(Holder<SoundEvent> event) {
            this.stop();
            this.addElement(element);
            this.sendPacket(VirtualEntityUtils.createClientboundSoundEntityPacket(
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
