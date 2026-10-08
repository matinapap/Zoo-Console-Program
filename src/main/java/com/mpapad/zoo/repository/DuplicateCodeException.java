package com.mpapad.zoo.repository;

/** Thrown when adding an animal whose code is already taken. */
public class DuplicateCodeException extends RuntimeException {

    public DuplicateCodeException(int code) {
        super("An animal with code " + code + " already exists.");
    }
}
