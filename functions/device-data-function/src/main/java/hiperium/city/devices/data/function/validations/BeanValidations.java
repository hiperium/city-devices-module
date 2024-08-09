package hiperium.city.devices.data.function.validations;

import hiperium.city.devices.data.function.dto.DeviceDataRequest;
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

    /**
     * Validates a DeviceDataRequest object using bean validation.
     *
     * @param deviceDataRequest The DeviceDataRequest object to be validated.
     * @throws ValidationException if the DeviceDataRequest object is invalid.
     */
    public static void validateBean(DeviceDataRequest deviceDataRequest) {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();
            Set<ConstraintViolation<DeviceDataRequest>> violations = validator.validate(deviceDataRequest);
            if (!violations.isEmpty()) {
                ConstraintViolation<DeviceDataRequest> firstViolation = violations.iterator().next();
                throw new ValidationException(firstViolation.getMessage());
            }
        }
    }
}
