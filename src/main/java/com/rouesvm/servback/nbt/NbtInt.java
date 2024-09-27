package com.rouesvm.servback.nbt;

import net.minecraft.nbt.NbtCompound;

public class NbtInt {
    private final NbtCompound tag;
    private final String key;
    private Integer integer;

    public NbtInt(NbtCompound tag, String key, Integer provide) {
        this.tag = tag;
        this.key = key;

        if (tag.contains(key))
            integer = tag.getInt(key);
        else integer = provide;
    }

    public NbtCompound getTag() {
        return tag;
    }

    public void set(Integer value) {
        integer = value;
        tag.putInt(key, value);
    }

    public Integer get() {
        return integer;
    }
}
