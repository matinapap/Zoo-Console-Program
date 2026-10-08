package com.mpapad.zoo.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AnimalTest {

    @Test
    void createsValidAnimalAndTrimsName() {
        Animal a = new Animal(100, "  red panda ", AnimalClass.MAMMAL, Diet.HERBIVORE, 14, 5.0, true);
        assertEquals("red panda", a.name());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 99, 1000, -5})
    void rejectsCodeOutsideRange(int code) {
        assertThrows(IllegalArgumentException.class,
                () -> new Animal(code, "lion", AnimalClass.MAMMAL, Diet.CARNIVORE, 15, 190, true));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "Lion", "lion1", "li,on", " - ", "lion  king"})
    void rejectsInvalidNames(String name) {
        assertThrows(IllegalArgumentException.class, () -> Animal.requireValidName(name));
    }

    @ParameterizedTest
    @ValueSource(doubles = {0, -1, Double.NaN, Double.POSITIVE_INFINITY})
    void rejectsInvalidWeight(double weight) {
        assertThrows(IllegalArgumentException.class, () -> Animal.requireValidWeight(weight));
    }

    @Test
    void rejectsNonPositiveMaxAge() {
        assertThrows(IllegalArgumentException.class, () -> Animal.requireValidMaxAge(0));
    }

    @Test
    void enumLabelsRoundTrip() {
        for (AnimalClass c : AnimalClass.values()) {
            assertEquals(c, AnimalClass.fromLabel(c.label()));
        }
        for (Diet d : Diet.values()) {
            assertEquals(d, Diet.fromLabel(d.label()));
        }
    }
}
