package com.mpapad.zoo;

import com.mpapad.zoo.repository.CsvAnimalRepository;
import com.mpapad.zoo.ui.ConsoleInput;
import com.mpapad.zoo.ui.ZooApp;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Path;

/**
 * Entry point. Usage: {@code java -jar zoo-console.jar [data-file]}.
 * The data file defaults to {@code data/animals.csv} and is created on first save.
 */
public final class Main {

    private static final Path DEFAULT_DATA_FILE = Path.of("data", "animals.csv");

    private Main() {
    }

    public static void main(String[] args) {
        Path dataFile = args.length > 0 ? Path.of(args[0]) : DEFAULT_DATA_FILE;

        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in, Charset.defaultCharset()));
        ZooApp app = new ZooApp(
                new CsvAnimalRepository(dataFile),
                new ConsoleInput(stdin, System.out),
                System.out);

        System.out.println("Using data file: " + dataFile.toAbsolutePath());
        app.run();
    }
}
