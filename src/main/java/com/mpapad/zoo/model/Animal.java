package com.mpapad.zoo.model;

import java.util.Objects;

/**
 * An animal record kept by the zoo. Instances are immutable and always valid:
 * every field is checked when the record is created.
 *
 * @param code      unique three-digit identifier (100-999)
 * @param name      species name in lowercase letters, e.g. {@code "red panda"}
 * @param animalClass biological class
 * @param diet      what the animal eats
 * @param maxAge    average life expectancy in years
 * @param weightKg  average weight in kilograms
 * @param wild      whether the animal is wild (as opposed to domesticated)
 */
public record Animal(
        int code,
        String name,
        AnimalClass animalClass,
        Diet diet,
        int maxAge,
        double weightKg,
        boolean wild) {

    public static final int MIN_CODE = 100;
    public static final int MAX_CODE = 999;

    private static final String NAME_PATTERN = "[a-z]+([ -][a-z]+)*";

    public Animal {
        requireValidCode(code);
        name = requireValidName(name);
        Objects.requireNonNull(animalClass, "animalClass");
        Objects.requireNonNull(diet, "diet");
        requireValidMaxAge(maxAge);
        requireValidWeight(weightKg);
    }

    public static int requireValidCode(int code) {
        if (code < MIN_CODE || code > MAX_CODE) {
            throw new IllegalArgumentException(
                    "Code must be between " + MIN_CODE + " and " + MAX_CODE + ".");
        }
        return code;
    }

    public static String requireValidName(String name) {
        String normalized = Objects.requireNonNull(name, "name").trim();
        if (!normalized.matches(NAME_PATTERN)) {
            throw new IllegalArgumentException(
                    "Name must use lowercase letters only (spaces and hyphens allowed between words).");
        }
        return normalized;
    }

    public static int requireValidMaxAge(int maxAge) {
        if (maxAge <= 0) {
            throw new IllegalArgumentException("Max age must be a positive number of years.");
        }
        return maxAge;
    }

    public static double requireValidWeight(double weightKg) {
        if (!(weightKg > 0) || Double.isInfinite(weightKg)) {
            throw new IllegalArgumentException("Weight must be a positive number of kilograms.");
        }
        return weightKg;
    }
}
