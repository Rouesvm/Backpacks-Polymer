package com.rouesvm.servback.content.upgrade;

public class Upgrade implements FunctionalUpgrade {
    private final UpgradeType<? extends Upgrade> type;

    public Upgrade(UpgradeType<? extends Upgrade> type) {
        this.type = type;
    }

    public UpgradeType<? extends Upgrade> getType() {
        return type;
    }
}
