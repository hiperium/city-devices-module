package hiperium.city.update.device.function.validations;

import hiperium.city.update.device.function.dto.EventBridgeEvent;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * Utility class for performing bean validations on objects.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BeanValidations {

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
