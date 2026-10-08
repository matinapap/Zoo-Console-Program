# Zoo Console Program

[![CI](https://github.com/matinapap/Zoo-Console-Program/actions/workflows/ci.yml/badge.svg)](https://github.com/matinapap/Zoo-Console-Program/actions/workflows/ci.yml)
![Java 17](https://img.shields.io/badge/Java-17-orange)
![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)

A command-line Java application for managing a zoo's animal records. You can list, add, search, edit and delete animals. Records are saved to a CSV file, so they are still there the next time you run it.

```text
===== Zoo Manager =====
 1. List all animals
 2. Add an animal
 3. Search by name
 4. Search by code
 5. Edit an animal
 6. Delete an animal
 0. Exit
Choose an option: 1
+------+--------------+---------+-----------+---------+-------------+------+
| CODE | NAME         | CLASS   | DIET      | MAX AGE | WEIGHT (kg) | WILD |
+------+--------------+---------+-----------+---------+-------------+------+
| 100  | flamingo     | bird    | omnivore  | 40      | 3.5         | yes  |
| 101  | tiger        | mammal  | carnivore | 20      | 220         | yes  |
| 102  | red panda    | mammal  | herbivore | 14      | 5           | yes  |
| 103  | green iguana | reptile | herbivore | 20      | 4           | yes  |
| 104  | goat         | mammal  | herbivore | 18      | 60          | no   |
| 105  | clownfish    | fish    | omnivore  | 8       | 0.25        | yes  |
+------+--------------+---------+-----------+---------+-------------+------+
6 animals in the zoo.
```

## Features

- **Full CRUD** on animal records, which are identified by a unique 3-digit code.
- **Search** by code, or by a partial name that ignores case (`pan` finds *red panda*).
- **Input validation**: each field is checked as you type it. If an answer is invalid, the program explains why and asks for that field again, so you don't have to start over.
- **Editing in place**: press Enter to keep a field's current value.
- **Delete confirmation** so you can't remove a record by accident.
- **Safe saves**: changes are written to a temporary file and then moved over the data file in one step. If the program is interrupted while saving, the data file is not left half-written.
- **Readable errors**: if the data file has a malformed row, the error message gives the line number.

## Getting started

**Requirements:** JDK 17+ and Maven 3.8+.

```bash
git clone https://github.com/matinapap/Zoo-Console-Program.git
cd Zoo-Console-Program
mvn package
java -jar target/zoo-console.jar
```

By default the app reads and writes `data/animals.csv`, which ships with some sample animals. To use a different file, pass its path as an argument. The file is created the first time you save:

```bash
java -jar target/zoo-console.jar my-zoo.csv
```

Run the test suite with:

```bash
mvn test
```

## Project structure

```text
src/main/java/com/mpapad/zoo/
├── Main.java                     # Entry point: wires the components together
├── model/
│   ├── Animal.java               # Immutable record that validates its own fields
│   ├── AnimalClass.java          # mammal, bird, reptile, ...
│   └── Diet.java                 # carnivore, herbivore, omnivore
├── repository/
│   ├── AnimalRepository.java     # Storage interface
│   ├── CsvAnimalRepository.java  # CSV-file implementation with atomic writes
│   ├── DuplicateCodeException.java
│   └── StorageException.java
└── ui/
    ├── ZooApp.java               # Menu loop and use cases
    ├── ConsoleInput.java         # Re-prompting, validated input
    └── AnimalTable.java          # Auto-sized text table rendering
```

### Design notes

- **Layered design.** The domain model, the persistence code and the console UI are kept separate. `ZooApp` depends only on the `AnimalRepository` interface, so the CSV storage could be replaced (for example with a database) without changing the UI.
- **Validation lives in the model.** `Animal` is a Java `record` whose constructor rejects invalid data, so an invalid `Animal` object can never exist. The UI calls the same validation methods to give feedback for each field as it is entered.
- **Testable I/O.** `ConsoleInput` and `ZooApp` take their input reader and output stream as constructor arguments. The tests use this to run the whole menu with scripted input and check the result.

### Data format

```csv
code,name,class,diet,max_age,weight_kg,wild
101,tiger,mammal,carnivore,20,220.0,true
```

## Tech stack

Java 17 (records, switch expressions, text blocks) · Maven · JUnit 5 · GitHub Actions

## Documentation

The original project report is available at [docs/zoo_documentation.pdf](docs/zoo_documentation.pdf).

## License

Distributed under the GNU GPL v3. See [LICENSE](LICENSE).
