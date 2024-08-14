package hiperium.city.devices.read.function.functions;

import com.fasterxml.jackson.databind.ObjectMapper;
import hiperium.cities.commons.exceptions.DisabledCityException;
import hiperium.cities.commons.exceptions.ResourceNotFoundException;
import hiperium.cities.commons.loggers.HiperiumLogger;
import hiperium.city.devices.read.function.dto.DeviceDataRequest;
import hiperium.city.devices.read.function.dto.DeviceDataResponse;
import hiperium.city.devices.read.function.entities.CityStatus;
import hiperium.city.devices.read.function.entities.Device;
import hiperium.city.devices.read.function.mappers.DeviceMapper;
import hiperium.city.devices.read.function.repository.DevicesRepository;
import hiperium.city.devices.read.function.validations.BeanValidations;
import jakarta.validation.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.function.Function;

/**
 * Represents a function that finds a device by its identifier.
 */
public class DeviceDataFunction implements Function<Message<byte[]>, Mono<DeviceDataResponse>> {

    private static final HiperiumLogger LOGGER = new HiperiumLogger(DeviceDataFunction.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final DeviceMapper deviceMapper;
    private final DevicesRepository devicesRepository;

    /**
     * The DeviceDataFunction class is responsible for processing a request message
     * and retrieving device data from the DevicesRepository using the DeviceMapper.
     */
    public DeviceDataFunction(DeviceMapper deviceMapper, DevicesRepository devicesRepository) {
        this.deviceMapper = deviceMapper;
        this.devicesRepository = devicesRepository;
    }

    /**
     * Applies the {@code DeviceDataFunction} to process a request message and retrieve device data.
     *
     * @param deviceIdRequestMessage the request message containing the device ID and city ID
     * @return a Mono of {@code DeviceDataResponse} representing the response with device data
     * @throws IllegalArgumentException if the payload of the request message cannot be deserialized to a {@code DeviceDataRequest}
     */
    @Override
    public Mono<DeviceDataResponse> apply(Message<byte[]> deviceIdRequestMessage) {
        DeviceDataRequest deviceDataRequest;
        try {
            deviceDataRequest = OBJECT_MAPPER.readValue(deviceIdRequestMessage.getPayload(), DeviceDataRequest.class);
        } catch (IOException exception) {
            LOGGER.error("Couldn't deserialize payload request", exception.getMessage());
            return Mono.just(new DeviceDataResponse.Builder()
                .httpStatus(HttpStatus.BAD_REQUEST.value())
                .errorMessage("Invalid request payload")
                .build());
        }
        return Mono.just(deviceDataRequest)
            .doOnNext(BeanValidations::validateBean)
            .map(this.devicesRepository::findById)
            .doOnNext(this::validateCityStatus)
            .map(device -> this.deviceMapper.mapToDeviceResponse(device, HttpStatus.OK.value(), null))
            .onErrorResume(DeviceDataFunction::handleException);
    }

    private void validateCityStatus(Device device) {
        LOGGER.debug("Validating City Status", device);
        if (device.cityStatus().equals(CityStatus.DISABLED)) {
            throw new DisabledCityException("City is disabled: " + device.id());
        }
    }

    private static Mono<DeviceDataResponse> handleException(Throwable throwable) {
        LOGGER.error("Couldn't find Device data", throwable.getMessage());
        DeviceDataResponse deviceUpdateResponse = createDeviceUpdateResponse(throwable);
        return Mono.just(deviceUpdateResponse);
    }

    private static DeviceDataResponse createDeviceUpdateResponse(Throwable throwable) {
        int statusCode;
        String message = throwable.getMessage();

        switch (throwable) {
            case ValidationException ignored -> statusCode = HttpStatus.BAD_REQUEST.value();
            case ResourceNotFoundException ignored -> statusCode = HttpStatus.NOT_FOUND.value();
            case DisabledCityException ignored -> statusCode = HttpStatus.NOT_ACCEPTABLE.value();
            default -> statusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
        }
        return new DeviceDataResponse.Builder()
            .httpStatus(statusCode)
            .errorMessage(message)
            .build();
    }
}
