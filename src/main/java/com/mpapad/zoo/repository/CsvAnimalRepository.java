package com.mpapad.zoo.repository;

import com.mpapad.zoo.model.Animal;
import com.mpapad.zoo.model.AnimalClass;
import com.mpapad.zoo.model.Diet;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * {@link AnimalRepository} backed by a CSV file.
 *
 * <p>The file is re-read on every call, so it always reflects what is on disk. Writes go to
 * a temporary file first and then replace the original, so an interrupted write never
 * leaves a half-written data file behind.
 */
public class CsvAnimalRepository implements AnimalRepository {

    static final String HEADER = "code,name,class,diet,max_age,weight_kg,wild";
    private static final int COLUMN_COUNT = 7;

    private final Path file;

    public CsvAnimalRepository(Path file) {
        this.file = file;
    }

    @Override
    public List<Animal> findAll() {
        return readAll();
    }

    @Override
    public Optional<Animal> findByCode(int code) {
        return readAll().stream().filter(a -> a.code() == code).findFirst();
    }

    @Override
    public List<Animal> searchByName(String query) {
        String needle = query.trim().toLowerCase(Locale.ROOT);
        return readAll().stream().filter(a -> a.name().contains(needle)).toList();
    }

    @Override
    public void add(Animal animal) {
        List<Animal> animals = readAll();
        if (animals.stream().anyMatch(a -> a.code() == animal.code())) {
            throw new DuplicateCodeException(animal.code());
        }
        animals.add(animal);
        writeAll(animals);
    }

    @Override
    public boolean update(Animal animal) {
        List<Animal> animals = readAll();
        for (int i = 0; i < animals.size(); i++) {
            if (animals.get(i).code() == animal.code()) {
                animals.set(i, animal);
                writeAll(animals);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean delete(int code) {
        List<Animal> animals = readAll();
        boolean removed = animals.removeIf(a -> a.code() == code);
        if (removed) {
            writeAll(animals);
        }
        return removed;
    }

    private List<Animal> readAll() {
        if (Files.notExists(file)) {
            return new ArrayList<>();
        }
        List<String> lines;
        try {
            lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new StorageException("Could not read " + file, e);
        }

        List<Animal> animals = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).strip();
            if (line.isEmpty() || line.equals(HEADER)) {
                continue;
            }
            try {
                animals.add(parse(line));
            } catch (IllegalArgumentException e) {
                throw new StorageException(
                        "Invalid record on line " + (i + 1) + " of " + file + ": " + e.getMessage(), e);
            }
        }
        animals.sort(Comparator.comparingInt(Animal::code));
        return animals;
    }

    private void writeAll(List<Animal> animals) {
        List<String> lines = new ArrayList<>(animals.size() + 1);
        lines.add(HEADER);
        animals.stream()
                .sorted(Comparator.comparingInt(Animal::code))
                .map(CsvAnimalRepository::format)
                .forEach(lines::add);

        try {
            Path dir = file.toAbsolutePath().getParent();
            Files.createDirectories(dir);
            Path tmp = Files.createTempFile(dir, file.getFileName().toString(), ".tmp");
            try {
                Files.write(tmp, lines, StandardCharsets.UTF_8);
                moveIntoPlace(tmp);
            } finally {
                Files.deleteIfExists(tmp);
            }
        } catch (IOException e) {
            throw new StorageException("Could not write " + file, e);
        }
    }

    private void moveIntoPlace(Path tmp) throws IOException {
        try {
            Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    static Animal parse(String line) {
        String[] f = line.split(",", -1);
        if (f.length != COLUMN_COUNT) {
            throw new IllegalArgumentException(
                    "expected " + COLUMN_COUNT + " columns but found " + f.length);
        }
        return new Animal(
                Integer.parseInt(f[0].trim()),
                f[1],
                AnimalClass.fromLabel(f[2]),
                Diet.fromLabel(f[3]),
                Integer.parseInt(f[4].trim()),
                Double.parseDouble(f[5].trim()),
                parseBoolean(f[6]));
    }

    static String format(Animal a) {
        return String.join(",",
                String.valueOf(a.code()),
                a.name(),
                a.animalClass().label(),
                a.diet().label(),
                String.valueOf(a.maxAge()),
                String.valueOf(a.weightKg()),
                String.valueOf(a.wild()));
    }

    private static boolean parseBoolean(String value) {
        return switch (value.trim().toLowerCase(Locale.ROOT)) {
            case "true", "yes" -> true;
            case "false", "no" -> false;
            default -> throw new IllegalArgumentException("wild must be true or false, got '" + value + "'");
        };
    }
}
