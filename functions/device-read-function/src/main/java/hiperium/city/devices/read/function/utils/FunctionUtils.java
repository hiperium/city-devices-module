package hiperium.city.devices.read.function.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import hiperium.cities.commons.exceptions.ParsingException;
import hiperium.cities.commons.loggers.HiperiumLogger;
import hiperium.cities.commons.utils.ExceptionHandlerUtil;
import hiperium.city.devices.read.function.dto.ReadDeviceRequest;
import hiperium.city.devices.read.function.dto.ReadDeviceResponse;
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
     * Deserialize a request message into a {@code ReadDeviceRequest} object.
     *
     * @param requestMessage the request message to deserialize
     * @return the deserialized {@code ReadDeviceRequest} object
     * @throws ParsingException if an error occurs during deserialization
     */
    public static ReadDeviceRequest deserializeRequest(final Message<byte[]> requestMessage) {
        try {
            return OBJECT_MAPPER.readValue(requestMessage.getPayload(), ReadDeviceRequest.class);
        } catch (IOException exception) {
            String messageContent = new String(requestMessage.getPayload(), StandardCharsets.UTF_8);
            LOGGER.error("Couldn't deserialize request message.", exception.getMessage(), messageContent);
            throw new ParsingException("Couldn't deserialize request message.");
        }
    }

    /**
     * Validates a ReadDeviceRequest object using bean validation.
     *
     * @param dataRequest The ReadDeviceRequest object to be validated.
     * @throws ValidationException if the ReadDeviceRequest object is invalid.
     */
    public static void validateRequest(final ReadDeviceRequest dataRequest) {
        LOGGER.debug("Validating request message", dataRequest);
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();
            Set<ConstraintViolation<ReadDeviceRequest>> violations = validator.validate(dataRequest);
            if (!violations.isEmpty()) {
                ConstraintViolation<ReadDeviceRequest> firstViolation = violations.iterator().next();
                throw new ValidationException(firstViolation.getMessage());
            }
        }
    }

    /**
     * Handles a runtime exception by generating an error response and mapping it to a {@code ReadDeviceResponse} object.
     *
     * @param throwable the runtime exception to handle
     * @return a {@code Mono} that emits a {@code ReadDeviceResponse} object with the error response
     */
    public static Mono<ReadDeviceResponse> handleRuntimeException(final Throwable throwable) {
        return Mono.just(throwable)
            .map(ExceptionHandlerUtil::generateErrorResponse)
            .map(errorResponse -> new ReadDeviceResponse(null, null, null, null, errorResponse))
            .doOnNext(deviceUpdateResponse -> LOGGER.debug("Mapped response", deviceUpdateResponse));
    }
}
