package me.fbiflow.stormevents.util.validator.implementation.yaml;

import me.fbiflow.stormevents.util.validator.ValidState;
import me.fbiflow.stormevents.util.validator.ValidationResult;
import me.fbiflow.stormevents.util.validator.ValidFilter;

public class QueueTimeValidFilter implements ValidFilter {

    @Override
    public ValidationResult apply(Object string) {
        if (string == null) {
            return new ValidationResult(ValidState.NULL);
        }

        String s;

        try {
            s = (String) string;
        } catch (ClassCastException exception) {
            return new ValidationResult(ValidState.EXCEPTION, "Invalid method parameter, required String, " +
                    "but accessed " + string.getClass().getName());
        }

        int queueTimeOffset;

        try {
            queueTimeOffset = Integer.parseInt(s.substring(s.charAt(0) == '+' ? 1 : 0));
        } catch (NumberFormatException exception) {
            return new ValidationResult(ValidState.EXCEPTION, exception.getMessage());
        }
        if (queueTimeOffset <= -1439 || queueTimeOffset >= 1439) {
            //на самом деле достаточно даже 720, чтобы покрыть весь возможный диапазон, но текущий вариант поудобнее
            return new ValidationResult(ValidState.INVALID,
                    "The value may be: GREATER THEN -1439 and LESS THEN 1439, but value is: " + queueTimeOffset);
        }
        return new ValidationResult(ValidState.VALID);
    }
}