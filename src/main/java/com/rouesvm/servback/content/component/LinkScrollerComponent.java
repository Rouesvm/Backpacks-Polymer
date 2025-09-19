package com.rouesvm.servback.content.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.util.Uuids;

import java.util.List;
import java.util.UUID;

public record LinkScrollerComponent(List<UUID> uuids) {

    public static LinkScrollerComponent of(List<UUID> uuids) {
        return new LinkScrollerComponent(uuids);
    }

    public static final PacketCodec<ByteBuf, LinkScrollerComponent> PACKET_CODEC = null;

    public static final Codec<LinkScrollerComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Uuids.CODEC.listOf().fieldOf("uuids").forGetter(LinkScrollerComponent::uuids)).apply(instance, LinkScrollerComponent::of)
    );
}
