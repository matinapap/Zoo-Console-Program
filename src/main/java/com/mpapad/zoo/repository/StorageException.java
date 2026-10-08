package com.mpapad.zoo.repository;

/** Thrown when the data file cannot be read, parsed or written. */
public class StorageException extends RuntimeException {

    public StorageException(String message) {
        super(message);
    }

    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
