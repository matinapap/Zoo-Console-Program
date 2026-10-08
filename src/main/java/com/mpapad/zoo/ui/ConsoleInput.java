package com.mpapad.zoo.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.util.Locale;
import java.util.function.Function;

/**
 * Reads validated values from the user, re-prompting until the input is acceptable.
 *
 * <p>All prompts share one reader, so there is a single owner of standard input.
 */
public class ConsoleInput {

    /** Thrown when input ends (e.g. Ctrl+D) while a value is still expected. */
    public static class EndOfInputException extends RuntimeException {
        public EndOfInputException() {
            super("Input ended.");
        }
    }

    private final BufferedReader in;
    private final PrintStream out;

    public ConsoleInput(BufferedReader in, PrintStream out) {
        this.in = in;
        this.out = out;
    }

    /**
     * Prompts until {@code parser} accepts the input. The parser signals invalid input by
     * throwing {@link IllegalArgumentException}; its message is shown to the user.
     */
    public <T> T prompt(String label, Function<String, T> parser) {
        return prompt(label, parser, null, null);
    }

    /**
     * Like {@link #prompt(String, Function)}, but an empty answer keeps {@code current}.
     * {@code currentDisplay} is how the current value is shown in the prompt.
     */
    public <T> T prompt(String label, Function<String, T> parser, T current, String currentDisplay) {
        while (true) {
            String suffix = current == null ? "" : " [" + currentDisplay + "]";
            out.print(label + suffix + ": ");
            out.flush();
            String line = readLine().trim();
            if (line.isEmpty() && current != null) {
                return current;
            }
            try {
                return parser.apply(line);
            } catch (NumberFormatException e) {
                out.println("  Please enter a valid number.");
            } catch (IllegalArgumentException e) {
                out.println("  " + e.getMessage());
            }
        }
    }

    public int promptInt(String label, Function<Integer, Integer> validator) {
        return prompt(label, s -> validator.apply(Integer.parseInt(s)));
    }

    public boolean confirm(String question) {
        return prompt(question + " (y/n)", ConsoleInput::parseYesNo);
    }

    public static boolean parseYesNo(String s) {
        return switch (s.toLowerCase(Locale.ROOT)) {
            case "y", "yes" -> true;
            case "n", "no" -> false;
            default -> throw new IllegalArgumentException("Please answer y or n.");
        };
    }

    private String readLine() {
        try {
            String line = in.readLine();
            if (line == null) {
                throw new EndOfInputException();
            }
            return line;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
