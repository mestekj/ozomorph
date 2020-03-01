package org.example;

public class NoPlansFoundException extends IllegalArgumentException {
    public NoPlansFoundException() {
        super("No plan exists for given input.");
    }
}
