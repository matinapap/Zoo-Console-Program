package com.mpapad.zoo.repository;

import com.mpapad.zoo.model.Animal;
import com.mpapad.zoo.model.AnimalClass;
import com.mpapad.zoo.model.Diet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CsvAnimalRepositoryTest {

    private static final Animal TIGER =
            new Animal(101, "tiger", AnimalClass.MAMMAL, Diet.CARNIVORE, 20, 220, true);
    private static final Animal GOAT =
            new Animal(104, "goat", AnimalClass.MAMMAL, Diet.HERBIVORE, 18, 60, false);
    private static final Animal FLAMINGO =
            new Animal(100, "flamingo", AnimalClass.BIRD, Diet.OMNIVORE, 40, 3.5, true);

    @TempDir
    Path dir;

    private Path file;
    private CsvAnimalRepository repo;

    @BeforeEach
    void setUp() {
        file = dir.resolve("nested/animals.csv");
        repo = new CsvAnimalRepository(file);
    }

    @Test
    void missingFileMeansEmptyZoo() {
        assertTrue(repo.findAll().isEmpty());
    }

    @Test
    void addPersistsAndReturnsAnimalsSortedByCode() {
        repo.add(TIGER);
        repo.add(GOAT);
        repo.add(FLAMINGO);

        List<Animal> fromDisk = new CsvAnimalRepository(file).findAll();
        assertEquals(List.of(FLAMINGO, TIGER, GOAT), fromDisk);
    }

    @Test
    void addRejectsDuplicateCode() {
        repo.add(TIGER);
        Animal sameCode = new Animal(101, "lion", AnimalClass.MAMMAL, Diet.CARNIVORE, 15, 190, true);
        assertThrows(DuplicateCodeException.class, () -> repo.add(sameCode));
        assertEquals(List.of(TIGER), repo.findAll());
    }

    @Test
    void searchByNameMatchesPartialAndCaseInsensitive() {
        repo.add(TIGER);
        repo.add(GOAT);
        assertEquals(List.of(TIGER), repo.searchByName("TIG"));
        assertEquals(List.of(TIGER, GOAT), repo.searchByName("g"));
        assertTrue(repo.searchByName("zebra").isEmpty());
    }

    @Test
    void updateReplacesExistingRecord() {
        repo.add(TIGER);
        Animal heavier = new Animal(101, "tiger", AnimalClass.MAMMAL, Diet.CARNIVORE, 20, 250, true);

        assertTrue(repo.update(heavier));
        assertEquals(heavier, repo.findByCode(101).orElseThrow());
        assertFalse(repo.update(GOAT));
    }

    @Test
    void deleteRemovesOnlyMatchingRecord() {
        repo.add(TIGER);
        repo.add(GOAT);

        assertTrue(repo.delete(101));
        assertFalse(repo.delete(101));
        assertEquals(List.of(GOAT), repo.findAll());
    }

    @Test
    void writesHeaderAndLeavesNoTempFiles() throws IOException {
        repo.add(TIGER);
        repo.delete(101);

        assertEquals(List.of(CsvAnimalRepository.HEADER), Files.readAllLines(file));
        try (var files = Files.list(file.getParent())) {
            assertEquals(1, files.count());
        }
    }

    @Test
    void reportsLineNumberOfMalformedRecord() throws IOException {
        Files.createDirectories(file.getParent());
        Files.writeString(file, CsvAnimalRepository.HEADER + "\n101,tiger,mammal,carnivore,20,heavy,true\n");

        StorageException e = assertThrows(StorageException.class, repo::findAll);
        assertTrue(e.getMessage().contains("line 2"), e.getMessage());
    }

    @Test
    void formatAndParseRoundTrip() {
        for (Animal a : List.of(TIGER, GOAT, FLAMINGO)) {
            assertEquals(a, CsvAnimalRepository.parse(CsvAnimalRepository.format(a)));
        }
    }
}
