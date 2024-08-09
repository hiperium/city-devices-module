package hiperium.city.devices.update.function.functions;

import com.fasterxml.jackson.databind.ObjectMapper;
import hiperium.cities.commons.loggers.HiperiumLogger;
import hiperium.city.devices.update.function.dto.EventBridgeEvent;
import hiperium.city.devices.update.function.dto.LambdaResponse;
import hiperium.city.devices.update.function.entities.CityStatus;
import hiperium.city.devices.update.function.entities.Device;
import hiperium.city.devices.update.function.exceptions.CityException;
import hiperium.city.devices.update.function.exceptions.DisabledCityException;
import hiperium.city.devices.update.function.exceptions.ResourceNotFoundException;
import hiperium.city.devices.update.function.repository.DeviceRepository;
import hiperium.city.devices.update.function.validations.BeanValidations;
import jakarta.validation.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.function.Function;

/**
 * The UpdateStatusFunction class represents a function that applies the device update request
 * and updates the device status.
 */
public class UpdateStatusFunction implements Function<Message<byte[]>, Mono<LambdaResponse>> {

    private static final HiperiumLogger LOGGER = new HiperiumLogger(UpdateStatusFunction.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final DeviceRepository deviceRepository;

    public UpdateStatusFunction(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    /**
     * Applies the device update request and updates the device status.
     *
     * @param message The device update request.
     * @return A Mono that emits the generic response indicating the success or failure of the update operation.
     */
    @Override
    public Mono<LambdaResponse> apply(Message<byte[]> message) {
        try {
            EventBridgeEvent event = OBJECT_MAPPER.readValue(message.getPayload(), EventBridgeEvent.class);
            return Mono.just(event)
                .doOnNext(BeanValidations::validateBean)
                .doOnNext(this::validateCityStatus)
                .doOnNext(this.deviceRepository::updateDeviceStatus)
                .then(Mono.just(new LambdaResponse.Builder().statusCode(HttpStatus.NO_CONTENT.value()).build()))
                .onErrorResume(UpdateStatusFunction::handleException);
        } catch (IOException exception) {
            return Mono.error(new RuntimeException("Error deserializing payload", exception));
        }
    }

    private void validateCityStatus(EventBridgeEvent eventBridgeEvent) {
        LOGGER.debug("Validating City Status", eventBridgeEvent.detail());
        Device device = this.deviceRepository.findById(eventBridgeEvent);
        if (device == null) {
            throw new ResourceNotFoundException("Device not found: " + eventBridgeEvent.detail().deviceId());
        }
        if (device.cityStatus().equals(CityStatus.DISABLED)) {
            throw new DisabledCityException("City is disabled: " + eventBridgeEvent.detail().cityId());
        }
    }

    private static Mono<LambdaResponse> handleException(Throwable throwable) {
        LOGGER.error("Couldn't update device status", throwable.getMessage());

        LambdaResponse lambdaResponse;
        if (throwable instanceof ValidationException validationException) {
            lambdaResponse = new LambdaResponse.Builder()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body(validationException.getMessage())
                .build();
        } else if (throwable instanceof CityException cityException) {
            lambdaResponse = new LambdaResponse.Builder()
                .statusCode(HttpStatus.NOT_ACCEPTABLE.value())
                .body(cityException.getMessage())
                .build();
        } else {
            lambdaResponse = new LambdaResponse.Builder()
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .body(throwable.getMessage())
                .build();
        }
        return Mono.just(lambdaResponse);
    }
}
