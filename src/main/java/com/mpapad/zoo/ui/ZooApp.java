package com.mpapad.zoo.ui;

import com.mpapad.zoo.model.Animal;
import com.mpapad.zoo.model.AnimalClass;
import com.mpapad.zoo.model.Diet;
import com.mpapad.zoo.repository.AnimalRepository;
import com.mpapad.zoo.repository.StorageException;

import java.io.PrintStream;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/** Interactive menu that lets the user browse and manage the zoo's animals. */
public class ZooApp {

    private static final String MENU = """

            ===== Zoo Manager =====
             1. List all animals
             2. Add an animal
             3. Search by name
             4. Search by code
             5. Edit an animal
             6. Delete an animal
             0. Exit""";

    private final AnimalRepository repository;
    private final ConsoleInput input;
    private final PrintStream out;

    public ZooApp(AnimalRepository repository, ConsoleInput input, PrintStream out) {
        this.repository = repository;
        this.input = input;
        this.out = out;
    }

    public void run() {
        try {
            boolean running = true;
            while (running) {
                out.println(MENU);
                int choice = input.promptInt("Choose an option", ZooApp::validateMenuChoice);
                try {
                    running = handle(choice);
                } catch (StorageException e) {
                    out.println("Error: " + e.getMessage());
                }
            }
        } catch (ConsoleInput.EndOfInputException e) {
            out.println();
        }
        out.println("Goodbye!");
    }

    private boolean handle(int choice) {
        switch (choice) {
            case 1 -> listAnimals();
            case 2 -> addAnimal();
            case 3 -> searchByName();
            case 4 -> searchByCode();
            case 5 -> editAnimal();
            case 6 -> deleteAnimal();
            case 0 -> {
                return false;
            }
            default -> throw new IllegalStateException("Unexpected menu choice " + choice);
        }
        return true;
    }

    private void listAnimals() {
        List<Animal> animals = repository.findAll();
        if (animals.isEmpty()) {
            out.println("The zoo has no animals yet.");
            return;
        }
        printTable(animals);
        out.println(animals.size() + (animals.size() == 1 ? " animal" : " animals") + " in the zoo.");
    }

    private void addAnimal() {
        int code = input.promptInt("Code (" + Animal.MIN_CODE + "-" + Animal.MAX_CODE + ")", c -> {
            Animal.requireValidCode(c);
            if (repository.existsByCode(c)) {
                throw new IllegalArgumentException("Code " + c + " is already taken. Try another one.");
            }
            return c;
        });
        Animal animal = new Animal(
                code,
                input.prompt("Name", Animal::requireValidName),
                promptEnum("Class", AnimalClass.values(), AnimalClass::label, null),
                promptEnum("Diet", Diet.values(), Diet::label, null),
                input.promptInt("Max age (years)", Animal::requireValidMaxAge),
                input.prompt("Weight (kg)", s -> Animal.requireValidWeight(Double.parseDouble(s))),
                input.confirm("Is it a wild animal?"));
        repository.add(animal);
        out.println("Added " + animal.name() + " with code " + animal.code() + ".");
    }

    private void searchByName() {
        String query = input.prompt("Name (or part of it)", s -> {
            if (s.isEmpty()) {
                throw new IllegalArgumentException("Please type at least one letter.");
            }
            return s;
        });
        List<Animal> matches = repository.searchByName(query);
        if (matches.isEmpty()) {
            out.println("No animals match \"" + query + "\".");
        } else {
            printTable(matches);
        }
    }

    private void searchByCode() {
        findByPromptedCode("Code to search").ifPresent(a -> printTable(List.of(a)));
    }

    private void editAnimal() {
        Optional<Animal> found = findByPromptedCode("Code of the animal to edit");
        if (found.isEmpty()) {
            return;
        }
        Animal current = found.get();
        printTable(List.of(current));
        out.println("Enter new values, or press Enter to keep the value in brackets.");

        Animal updated = new Animal(
                current.code(),
                input.prompt("Name", Animal::requireValidName, current.name(), current.name()),
                promptEnum("Class", AnimalClass.values(), AnimalClass::label, current.animalClass()),
                promptEnum("Diet", Diet.values(), Diet::label, current.diet()),
                input.prompt("Max age (years)", s -> Animal.requireValidMaxAge(Integer.parseInt(s)),
                        current.maxAge(), String.valueOf(current.maxAge())),
                input.prompt("Weight (kg)", s -> Animal.requireValidWeight(Double.parseDouble(s)),
                        current.weightKg(), AnimalTable.formatWeight(current.weightKg())),
                input.prompt("Is it a wild animal? (y/n)", ConsoleInput::parseYesNo,
                        current.wild(), current.wild() ? "y" : "n"));
        repository.update(updated);
        out.println("Updated animal " + updated.code() + ".");
    }

    private void deleteAnimal() {
        Optional<Animal> found = findByPromptedCode("Code of the animal to delete");
        if (found.isEmpty()) {
            return;
        }
        Animal animal = found.get();
        printTable(List.of(animal));
        if (input.confirm("Delete " + animal.name() + "?")) {
            repository.delete(animal.code());
            out.println("Deleted animal " + animal.code() + ".");
        } else {
            out.println("Nothing was deleted.");
        }
    }

    private Optional<Animal> findByPromptedCode(String label) {
        int code = input.promptInt(label, Animal::requireValidCode);
        Optional<Animal> animal = repository.findByCode(code);
        if (animal.isEmpty()) {
            out.println("There is no animal with code " + code + ".");
        }
        return animal;
    }

    /** Shows the options as a numbered list and accepts either the number or the label. */
    private <E extends Enum<E>> E promptEnum(String label, E[] values, Function<E, String> labeler, E current) {
        StringBuilder options = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            options.append(i == 0 ? "" : ", ").append(i + 1).append('=').append(labeler.apply(values[i]));
        }
        out.println(label + " options: " + options);
        Function<String, E> parser = s -> {
            for (int i = 0; i < values.length; i++) {
                if (s.equals(String.valueOf(i + 1)) || s.equalsIgnoreCase(labeler.apply(values[i]))) {
                    return values[i];
                }
            }
            throw new IllegalArgumentException("Please choose a number from 1 to " + values.length + ".");
        };
        return current == null
                ? input.prompt(label, parser)
                : input.prompt(label, parser, current, labeler.apply(current));
    }

    private void printTable(List<Animal> animals) {
        out.println(AnimalTable.render(animals));
    }

    private static int validateMenuChoice(int choice) {
        if (choice < 0 || choice > 6) {
            throw new IllegalArgumentException("Please choose a number from 0 to 6.");
        }
        return choice;
    }
}
