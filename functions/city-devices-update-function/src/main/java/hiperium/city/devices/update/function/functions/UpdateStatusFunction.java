package hiperium.city.devices.update.function.functions;

import hiperium.city.devices.update.function.dto.DeviceUpdateRequest;
import hiperium.city.devices.update.function.dto.DeviceUpdateResponse;
import hiperium.city.devices.update.function.entities.CityStatus;
import hiperium.city.devices.update.function.entities.Device;
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
 * Function that updates a device status.
 *
 * @apiNote The Enhanced Client has problems at runtime when used with Spring Native.
 * This is because the Enhanced Client uses reflection to create the DynamoDbAsyncClient.
 * The solution is to use the low-level client instead.
 */
public class UpdateStatusFunction implements Function<Message<DeviceUpdateRequest>, DeviceUpdateResponse> {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateStatusFunction.class);

    private final DeviceRepository deviceRepository;

    public UpdateStatusFunction(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    /**
     * Applies the given device update request and updates the device status.
     *
     * @param deviceUpdateRequestMono the device update request.
     * @return the device update response with the HTTP status code.
     */
    @Override
    public DeviceUpdateResponse apply(Message<DeviceUpdateRequest> deviceUpdateRequestMono) {
        Mono.just(deviceUpdateRequestMono.getPayload())
            .doOnNext(BeanValidationUtils::validateBean)
            .doOnNext(this::validateCityStatus)
            .doOnNext(this.deviceRepository::updateDeviceStatus)
            .subscribe(
                request -> LOGGER.debug("Update device status successfully: {}", request),
                throwable -> LOGGER.error("ERROR: Couldn't update device status: {}", throwable.getMessage()));

        return new DeviceUpdateResponse(HttpStatus.OK.value());
    }

    private void validateCityStatus(DeviceUpdateRequest deviceUpdateRequest) {
        LOGGER.debug("Validating city status for request: {}", deviceUpdateRequest);
        Device device = this.deviceRepository.findById(deviceUpdateRequest);
        if (device == null) {
            throw new ResourceNotFoundException("Device not found.");
        }
        if (device.cityStatus().equals(CityStatus.DISABLED)) {
            throw new DisabledCityException("City is disabled.");
        }
    }
}
