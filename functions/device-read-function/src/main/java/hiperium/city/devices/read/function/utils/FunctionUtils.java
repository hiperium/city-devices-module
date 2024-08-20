package hiperium.city.devices.read.function.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import hiperium.cities.commons.exceptions.ParsingException;
import hiperium.cities.commons.loggers.HiperiumLogger;
import hiperium.cities.commons.utils.ExceptionHandlerUtil;
import hiperium.city.devices.read.function.dto.DeviceReadRequest;
import hiperium.city.devices.read.function.dto.DeviceReadResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.messaging.Message;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;

/**
 * Utility class for common function operations.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FunctionUtils {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final HiperiumLogger LOGGER = new HiperiumLogger(FunctionUtils.class);

    /**
     * Deserialize a request message into a {@code DeviceReadRequest} object.
     *
     * @param requestMessage the request message to deserialize
     * @return the deserialized {@code DeviceReadRequest} object
     * @throws ParsingException if an error occurs during deserialization
     */
    public static DeviceReadRequest deserializeRequest(final Message<byte[]> requestMessage) {
        try {
            return OBJECT_MAPPER.readValue(requestMessage.getPayload(), DeviceReadRequest.class);
        } catch (IOException exception) {
            String messageContent = new String(requestMessage.getPayload(), StandardCharsets.UTF_8);
            LOGGER.error("Couldn't deserialize request message.", exception.getMessage(), messageContent);
            throw new ParsingException("Couldn't deserialize request message.");
        }
    }

    /**
     * Validates a DeviceReadRequest object using bean validation.
     *
     * @param dataRequest The DeviceReadRequest object to be validated.
     * @throws ValidationException if the DeviceReadRequest object is invalid.
     */
    public static void validateRequest(final DeviceReadRequest dataRequest) {
        LOGGER.debug("Validating request message", dataRequest);
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();
            Set<ConstraintViolation<DeviceReadRequest>> violations = validator.validate(dataRequest);
            if (!violations.isEmpty()) {
                ConstraintViolation<DeviceReadRequest> firstViolation = violations.iterator().next();
                throw new ValidationException(firstViolation.getMessage());
            }
        }
    }

    /**
     * Handles a runtime exception by generating an error response and mapping it to a {@code DeviceReadResponse} object.
     *
     * @param throwable the runtime exception to handle
     * @return a {@code Mono} that emits a {@code DeviceReadResponse} object with the error response
     */
    public static Mono<DeviceReadResponse> handleRuntimeException(final Throwable throwable) {
        return Mono.just(throwable)
            .map(ExceptionHandlerUtil::generateErrorResponse)
            .map(errorResponse -> new DeviceReadResponse(null, null, null, null, errorResponse))
            .doOnNext(deviceUpdateResponse -> LOGGER.debug("Mapped response", deviceUpdateResponse));
    }
}
