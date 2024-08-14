package hiperium.city.update.device.function.functions;

import com.fasterxml.jackson.databind.ObjectMapper;
import hiperium.cities.commons.exceptions.DisabledCityException;
import hiperium.cities.commons.exceptions.ResourceNotFoundException;
import hiperium.cities.commons.loggers.HiperiumLogger;
import hiperium.city.update.device.function.dto.DeviceUpdateResponse;
import hiperium.city.update.device.function.dto.EventBridgeEvent;
import hiperium.city.update.device.function.entities.CityStatus;
import hiperium.city.update.device.function.entities.Device;
import hiperium.city.update.device.function.repository.DevicesRepository;
import hiperium.city.update.device.function.validations.BeanValidations;
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
public class UpdateStatusFunction implements Function<Message<byte[]>, Mono<DeviceUpdateResponse>> {

    private static final HiperiumLogger LOGGER = new HiperiumLogger(UpdateStatusFunction.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final DevicesRepository devicesRepository;

    /**
     * Represents a function that updates the status of a device.
     * This function applies the device update request and updates the device status in the DynamoDB table.
     *
     * @param devicesRepository The repository for managing devices in the DynamoDB table.
     */
    public UpdateStatusFunction(DevicesRepository devicesRepository) {
        this.devicesRepository = devicesRepository;
    }

    /**
     * Applies the device update request and updates the device status.
     *
     * @param message The device update request.
     * @return A Mono that emits the generic response indicating the success or failure of the update operation.
     */
    @Override
    public Mono<DeviceUpdateResponse> apply(Message<byte[]> message) {
        EventBridgeEvent event;
        try {
            event = OBJECT_MAPPER.readValue(message.getPayload(), EventBridgeEvent.class);
        } catch (IOException exception) {
            LOGGER.error("Couldn't deserialize payload request", exception.getMessage());
            return Mono.just(new DeviceUpdateResponse.Builder()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("Invalid request payload")
                .build());
        }
        return Mono.just(event)
            .doOnNext(BeanValidations::validateBean)
            .map(this.devicesRepository::findByIdAndChangeStatus)
            .doOnNext(this::validateCityStatus)
            .doOnNext(this.devicesRepository::updateDeviceStatus)
            .then(Mono.just(new DeviceUpdateResponse.Builder().statusCode(HttpStatus.NO_CONTENT.value()).build()))
            .onErrorResume(UpdateStatusFunction::handleException);
    }

    private void validateCityStatus(Device device) {
        LOGGER.debug("Validating City Status", device);
        if (device.cityStatus().equals(CityStatus.DISABLED)) {
            throw new DisabledCityException("City is disabled: " + device.cityId());
        }
    }

    private static Mono<DeviceUpdateResponse> handleException(Throwable throwable) {
        LOGGER.error("Couldn't update device status", throwable.getMessage());
        DeviceUpdateResponse deviceUpdateResponse = createDeviceUpdateResponse(throwable);
        return Mono.just(deviceUpdateResponse);
    }

    private static DeviceUpdateResponse createDeviceUpdateResponse(Throwable throwable) {
        int statusCode;
        String message = throwable.getMessage();

        switch (throwable) {
            case ValidationException ignored -> statusCode = HttpStatus.BAD_REQUEST.value();
            case ResourceNotFoundException ignored -> statusCode = HttpStatus.NOT_FOUND.value();
            case DisabledCityException ignored -> statusCode = HttpStatus.NOT_ACCEPTABLE.value();
            default -> statusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
        }
        return new DeviceUpdateResponse.Builder()
            .statusCode(statusCode)
            .body(message)
            .build();
    }
}
