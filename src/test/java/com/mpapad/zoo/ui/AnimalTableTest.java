package com.mpapad.zoo.ui;

import com.mpapad.zoo.model.Animal;
import com.mpapad.zoo.model.AnimalClass;
import com.mpapad.zoo.model.Diet;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AnimalTableTest {

    @Test
    void rendersAlignedTable() {
        String table = AnimalTable.render(List.of(
                new Animal(101, "tiger", AnimalClass.MAMMAL, Diet.CARNIVORE, 20, 220, true),
                new Animal(105, "clownfish", AnimalClass.FISH, Diet.OMNIVORE, 8, 0.25, true)));

        String expected = String.join("\n",
                "+------+-----------+--------+-----------+---------+-------------+------+",
                "| CODE | NAME      | CLASS  | DIET      | MAX AGE | WEIGHT (kg) | WILD |",
                "+------+-----------+--------+-----------+---------+-------------+------+",
                "| 101  | tiger     | mammal | carnivore | 20      | 220         | yes  |",
                "| 105  | clownfish | fish   | omnivore  | 8       | 0.25        | yes  |",
                "+------+-----------+--------+-----------+---------+-------------+------+");
        assertEquals(expected, table);
    }

    @Test
    void formatsWeightWithoutTrailingZeros() {
        assertEquals("80", AnimalTable.formatWeight(80.0));
        assertEquals("2.5", AnimalTable.formatWeight(2.5));
    }
}
