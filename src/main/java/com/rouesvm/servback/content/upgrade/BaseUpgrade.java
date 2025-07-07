package com.rouesvm.servback.content.upgrade;

import com.rouesvm.servback.ServerBackpacks;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class BaseUpgrade implements TickingUpgrade {
    public Identifier id;

    public BaseUpgrade(String name) {
        this.id = Identifier.of(ServerBackpacks.MOD_ID, name);
    }

    public MutableText toTranslationKey() {
        return Text.translatable(id.toTranslationKey("upgrade"));
    }

    @Override
    public String toString() {
        return id.toString();
    }

    public void readView(ReadView data) {

    }

    public void writeView(WriteView data) {

    }
}
