package hiperium.city.devices.update.function.utils;

import hiperium.city.devices.update.function.dto.EventBridgeRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * Utility class for performing bean validations on objects.
 */
public final class BeanValidationUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(BeanValidationUtils.class);

    private BeanValidationUtils() {
    }

    public static void validateBean(EventBridgeRequest eventBridgeRequest) {
        LOGGER.debug("Validating request data: {}", eventBridgeRequest);
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();
            Set<ConstraintViolation<EventBridgeRequest>> violations = validator.validate(eventBridgeRequest);
            if (!violations.isEmpty()) {
                ConstraintViolation<EventBridgeRequest> firstViolation = violations.iterator().next();
                throw new ValidationException(firstViolation.getMessage());
            }
        }
    }
}
