package hiperium.city.devices.update.function.validations;

import hiperium.city.devices.update.function.dto.EventBridgeEvent;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.util.Set;

/**
 * Utility class for performing bean validations on objects.
 */
public final class BeanValidations {

    private BeanValidations() {
    }

    public static void validateBean(EventBridgeEvent eventBridgeEvent) {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();
            Set<ConstraintViolation<EventBridgeEvent>> violations = validator.validate(eventBridgeEvent);
            if (!violations.isEmpty()) {
                ConstraintViolation<EventBridgeEvent> firstViolation = violations.iterator().next();
                throw new ValidationException(firstViolation.getMessage());
            }
        }
    }
}
