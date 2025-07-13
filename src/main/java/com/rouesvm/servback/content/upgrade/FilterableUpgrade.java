package com.rouesvm.servback.content.upgrade;

import com.rouesvm.servback.content.upgrade.extension.ItemFilter;

public interface FilterableUpgrade {
    ItemFilter getFilter();

    default boolean hasFilter() {
        return getFilter() != null;
    }
}