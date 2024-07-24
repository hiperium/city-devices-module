package hiperium.city.devices.update.function.functions;

import hiperium.city.devices.update.function.dto.EventBridgeRequest;
import hiperium.city.devices.update.function.dto.GenericResponse;
import hiperium.city.devices.update.function.entities.CityStatus;
import hiperium.city.devices.update.function.entities.Device;
import hiperium.city.devices.update.function.exceptions.CityException;
import hiperium.city.devices.update.function.exceptions.DisabledCityException;
import hiperium.city.devices.update.function.exceptions.ResourceNotFoundException;
import hiperium.city.devices.update.function.repository.DeviceRepository;
import hiperium.city.devices.update.function.utils.BeanValidationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import reactor.core.publisher.Mono;

import java.util.function.Function;

/**
 * The UpdateStatusFunction class represents a function that applies the device update request
 * and updates the device status.
 */
public class UpdateStatusFunction implements Function<Message<EventBridgeRequest>, Mono<GenericResponse>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateStatusFunction.class);

    private final DeviceRepository deviceRepository;

    public UpdateStatusFunction(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    /**
     * Applies the device update request and updates the device status.
     *
     * @param deviceUpdateRequestMono The device update request.
     * @return A Mono that emits the generic response indicating the success or failure of the update operation.
     */
    @Override
    public Mono<GenericResponse> apply(Message<EventBridgeRequest> deviceUpdateRequestMono) {
        return Mono.just(deviceUpdateRequestMono.getPayload())
            .doOnNext(BeanValidationUtils::validateBean)
            .doOnNext(this::validateCityStatus)
            .doOnNext(this.deviceRepository::updateDeviceStatus)
            .then(Mono.just(new GenericResponse.Builder().statusCode(HttpStatus.NO_CONTENT.value()).build()))
            .onErrorResume(UpdateStatusFunction::handleException);
    }

    private void validateCityStatus(EventBridgeRequest eventBridgeRequest) {
        LOGGER.debug("Validating city status for request: {}", eventBridgeRequest);
        Device device = this.deviceRepository.findById(eventBridgeRequest);
        if (device == null) {
            throw new ResourceNotFoundException("Device not found: " + eventBridgeRequest.detail().deviceId());
        }
        if (device.cityStatus().equals(CityStatus.DISABLED)) {
            throw new DisabledCityException("City is disabled: " + eventBridgeRequest.detail().cityId());
        }
    }

    private static Mono<GenericResponse> handleException(Throwable throwable) {
        LOGGER.error("ERROR: Couldn't update device status: {}", throwable.getMessage());

        GenericResponse genericResponse;
        if (throwable instanceof CityException cityException) {
            genericResponse = new GenericResponse.Builder()
                .statusCode(HttpStatus.NOT_ACCEPTABLE.value())
                .body(cityException.getMessage())
                .build();
        } else {
            genericResponse = new GenericResponse.Builder()
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .body(throwable.getMessage())
                .build();
        }
        return Mono.just(genericResponse);
    }
}
