package com.mpapad.zoo.model;

import java.util.Locale;

/** What an animal eats. */
public enum Diet {
    CARNIVORE,
    HERBIVORE,
    OMNIVORE;

    /** Lowercase label used for display and persistence. */
    public String label() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static Diet fromLabel(String label) {
        return valueOf(label.trim().toUpperCase(Locale.ROOT));
    }
}
