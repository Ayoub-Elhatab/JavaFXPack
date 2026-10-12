package com.ayoub.javafxpack.models;


import lombok.Getter;
import java.util.ArrayList;
import java.util.List;

/**
 * Collects the error messages found during validation.
 *
 * @author Ayoub Elhatab
 * LinkedIn: <a href="https://www.linkedin.com/in/ayoub-elhatab/">Ayoub Elhatab</a>
 */
@Getter
public class ValidationResult {

    private final List<String> errors = new ArrayList<>();

    /**
     * Adds an error message to the result.
     *
     * @param message the error description
     */
    public void addError(String message) {
        errors.add(message);
    }

    /**
     * Tells whether validation passed.
     *
     * @return true if no errors were recorded
     */
    public boolean isValid() {
        return errors.isEmpty();
    }
}
