package com.rouesvm.servback.content.upgrade;

import java.util.Objects;

public class Upgrade implements FunctionalUpgrade {
    private final UpgradeType<? extends Upgrade> type;

    public Upgrade(UpgradeType<? extends Upgrade> type) {
        this.type = type;
    }

    public UpgradeType<? extends Upgrade> getType() {
        return type;
    }

    @Override
    public String toString() {
        return type.getId().toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        Upgrade upgrade = (Upgrade) o;
        return Objects.equals(type, upgrade.type);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(type);
    }
}
