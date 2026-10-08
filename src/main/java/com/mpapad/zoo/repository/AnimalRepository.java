package com.mpapad.zoo.repository;

import com.mpapad.zoo.model.Animal;

import java.util.List;
import java.util.Optional;

/** Storage for the zoo's animal records, keyed by {@link Animal#code()}. */
public interface AnimalRepository {

    /** Returns every animal, ordered by code. */
    List<Animal> findAll();

    Optional<Animal> findByCode(int code);

    /** Returns all animals whose name contains {@code query}, ignoring case. */
    List<Animal> searchByName(String query);

    default boolean existsByCode(int code) {
        return findByCode(code).isPresent();
    }

    /**
     * Stores a new animal.
     *
     * @throws DuplicateCodeException if an animal with the same code already exists
     */
    void add(Animal animal);

    /**
     * Replaces the animal that has the same code.
     *
     * @return {@code false} if no animal with that code exists
     */
    boolean update(Animal animal);

    /** @return {@code false} if no animal with that code exists */
    boolean delete(int code);
}
