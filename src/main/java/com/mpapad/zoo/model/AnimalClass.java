package com.mpapad.zoo.model;

import java.util.Locale;

/** Biological class an animal belongs to. */
public enum AnimalClass {
    MAMMAL,
    BIRD,
    REPTILE,
    AMPHIBIAN,
    FISH,
    INVERTEBRATE;

    /** Lowercase label used for display and persistence. */
    public String label() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static AnimalClass fromLabel(String label) {
        return valueOf(label.trim().toUpperCase(Locale.ROOT));
    }
}
