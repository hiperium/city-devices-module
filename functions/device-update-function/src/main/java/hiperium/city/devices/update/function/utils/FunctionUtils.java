package hiperium.city.devices.update.function.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import hiperium.cities.commons.exceptions.ParsingException;
import hiperium.cities.commons.loggers.HiperiumLogger;
import hiperium.cities.commons.utils.ExceptionHandlerUtil;
import hiperium.city.devices.update.function.dto.DeviceUpdateResponse;
import hiperium.city.devices.update.function.dto.EventBridgeRequest;
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
     * Deserializes a request from EventBridge into an {@code EventBridgeRequest} object.
     *
     * @param requestMessage the message containing the request payload as a byte array
     * @return the deserialized {@code EventBridgeRequest} object
     * @throws ParsingException if the request message cannot be deserialized
     */
    public static EventBridgeRequest deserializeRequest(Message<byte[]> requestMessage) {
        try {
            return OBJECT_MAPPER.readValue(requestMessage.getPayload(), EventBridgeRequest.class);
        } catch (IOException exception) {
            String messageContent = new String(requestMessage.getPayload(), StandardCharsets.UTF_8);
            LOGGER.error("Couldn't deserialize request message.", exception.getMessage(), messageContent);
            throw new ParsingException("Couldn't deserialize request message.");
        }
    }

    /**
     * Validates the given EventBridge request by performing bean validations on the object.
     *
     * @param eventBridgeRequest the EventBridgeRequest object to be validated
     *
     * @throws ValidationException if the validation fails and there are constraint violations
     */
    public static void validateRequest(final EventBridgeRequest eventBridgeRequest) {
        LOGGER.debug("Validating request message", eventBridgeRequest);
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();
            Set<ConstraintViolation<EventBridgeRequest>> violations = validator.validate(eventBridgeRequest);
            if (!violations.isEmpty()) {
                ConstraintViolation<EventBridgeRequest> firstViolation = violations.iterator().next();
                throw new ValidationException(firstViolation.getMessage());
            }
        }
    }

    /**
     * Handles a runtime exception and generates an error response wrapped in a {@code DeviceUpdateResponse} object.
     *
     * @param throwable the runtime exception to handle
     * @return a Mono emitting the {@code DeviceUpdateResponse} object with the generated error response
     */
    public static Mono<DeviceUpdateResponse> handleRuntimeException(Throwable throwable) {
        return Mono.just(throwable)
            .map(ExceptionHandlerUtil::generateErrorResponse)
            .map(errorResponse -> new DeviceUpdateResponse(null, null, errorResponse))
            .doOnNext(deviceUpdateResponse -> LOGGER.debug("Mapped response", deviceUpdateResponse));
    }
}
