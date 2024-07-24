package hiperium.city.devices.update.function.utils;

import hiperium.city.devices.update.function.dto.DeviceUpdateRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.Set;

/**
 * Utility class for performing bean validations on objects.
 */
public final class BeanValidationUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(BeanValidationUtils.class);

    private BeanValidationUtils() {
    }

    public static void validateBean(DeviceUpdateRequest deviceUpdateRequest) {
        LOGGER.debug("Validating request data: {}", deviceUpdateRequest);
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();
            Set<ConstraintViolation<DeviceUpdateRequest>> violations = validator.validate(deviceUpdateRequest);
            if (!violations.isEmpty()) {
                ConstraintViolation<DeviceUpdateRequest> firstViolation = violations.iterator().next();
                throw new ValidationException(firstViolation.getMessage());
            }
        }
    }
}
