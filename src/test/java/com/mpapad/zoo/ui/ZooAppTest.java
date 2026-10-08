package com.mpapad.zoo.ui;

import com.mpapad.zoo.model.Animal;
import com.mpapad.zoo.model.AnimalClass;
import com.mpapad.zoo.model.Diet;
import com.mpapad.zoo.repository.CsvAnimalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Drives the menu end to end with scripted input. */
class ZooAppTest {

    @TempDir
    Path dir;

    private CsvAnimalRepository repo;

    @BeforeEach
    void setUp() {
        repo = new CsvAnimalRepository(dir.resolve("animals.csv"));
    }

    private String run(String... lines) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(buffer, true, StandardCharsets.UTF_8);
        BufferedReader in = new BufferedReader(new StringReader(String.join("\n", lines) + "\n"));
        new ZooApp(repo, new ConsoleInput(in, out), out).run();
        return buffer.toString(StandardCharsets.UTF_8);
    }

    @Test
    void addsAnimalAfterRejectingInvalidAnswers() {
        String output = run(
                "2",
                "abc", "50", "101",       // code: not a number, out of range, ok
                "Tiger", "tiger",         // name: uppercase rejected
                "9", "1",                 // class: out of range, mammal
                "carnivore",              // diet by label
                "-3", "20",               // max age
                "0", "220",               // weight
                "maybe", "y",             // wild
                "0");

        assertTrue(output.contains("Please enter a valid number."), output);
        assertTrue(output.contains("Added tiger with code 101."), output);
        assertEquals(List.of(new Animal(101, "tiger", AnimalClass.MAMMAL, Diet.CARNIVORE, 20, 220, true)),
                repo.findAll());
    }

    @Test
    void rejectsCodeThatIsAlreadyTaken() {
        repo.add(new Animal(101, "tiger", AnimalClass.MAMMAL, Diet.CARNIVORE, 20, 220, true));

        String output = run("2", "101", "102", "goat", "1", "2", "18", "60", "n", "0");

        assertTrue(output.contains("Code 101 is already taken"), output);
        assertEquals(2, repo.findAll().size());
    }

    @Test
    void editKeepsValuesWhenEnterIsPressed() {
        repo.add(new Animal(101, "tiger", AnimalClass.MAMMAL, Diet.CARNIVORE, 20, 220, true));

        run("5", "101", "", "", "", "22", "", "", "0");

        assertEquals(new Animal(101, "tiger", AnimalClass.MAMMAL, Diet.CARNIVORE, 22, 220, true),
                repo.findByCode(101).orElseThrow());
    }

    @Test
    void deleteAsksForConfirmation() {
        repo.add(new Animal(101, "tiger", AnimalClass.MAMMAL, Diet.CARNIVORE, 20, 220, true));

        run("6", "101", "n", "0");
        assertTrue(repo.existsByCode(101));

        run("6", "101", "y", "0");
        assertTrue(repo.findAll().isEmpty());
    }

    @Test
    void exitsCleanlyWhenInputEnds() {
        String output = run("1");
        assertTrue(output.contains("The zoo has no animals yet."), output);
        assertTrue(output.endsWith("Goodbye!" + System.lineSeparator()), output);
    }
}
