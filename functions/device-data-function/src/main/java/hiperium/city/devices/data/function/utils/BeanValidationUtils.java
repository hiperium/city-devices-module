package hiperium.city.devices.data.function.utils;

import hiperium.city.devices.data.function.dto.DeviceIdRequest;
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

    /**
     * Validates a DeviceIdRequest object using bean validation.
     *
     * @param deviceIdRequest The DeviceIdRequest object to be validated.
     * @throws ValidationException if the DeviceIdRequest object is invalid.
     */
    public static void validateBean(DeviceIdRequest deviceIdRequest) {
        LOGGER.debug("Validating request data: {}", deviceIdRequest);
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();
            Set<ConstraintViolation<DeviceIdRequest>> violations = validator.validate(deviceIdRequest);
            if (!violations.isEmpty()) {
                ConstraintViolation<DeviceIdRequest> firstViolation = violations.iterator().next();
                throw new ValidationException(firstViolation.getMessage());
            }
        }
    }
}
