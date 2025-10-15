package com.rouesvm.servback.technical.config;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class BackpackConfigurationFixer {
    public static List<BackpackItemConfiguration.BackpackDefinedType> convertOldConfigToNew() {
        List<Configuration.BackpackType> backpackTypes = Configuration.manager.backpackTypes;
        List<BackpackItemConfiguration.BackpackDefinedType> backpackDefinedTypes = new ArrayList<>();

        backpackTypes.sort(Comparator.comparingInt(Configuration.BackpackType::tier));

        for (int i = 0; i < backpackTypes.size(); i++) {
            Configuration.BackpackType current = backpackTypes.get(i);
            Configuration.BackpackType next = (i + 1 < backpackTypes.size()) ? backpackTypes.get(i + 1) : null;

            for (String backpack : current.backpacks()) {
                String upgrade = (next != null && !next.backpacks().isEmpty())
                        ? next.backpacks().getFirst()
                        : backpack;

                backpackDefinedTypes.add(new BackpackItemConfiguration.BackpackDefinedType(
                        backpack, upgrade,
                        current.slots(), current.dyeable(),
                        current.dyeBlacklist(),
                        BackpackItemConfiguration.DEFAULT_COSMETIC
                ));
            }
        }

        return backpackDefinedTypes;
    }
}
