package me.fbiflow.stormevents.util.validator;

public class ValidationResult {

    public final String message;
    public final ValidState state;

    public ValidationResult(ValidState state, String message) {
        this.state = state;
        this.message = message;
    }

    public ValidationResult(ValidState state) {
        this.state = state;
        this.message = state.name();
    }
}