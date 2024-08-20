package hiperium.city.devices.read.function.services;

import hiperium.cities.commons.exceptions.ResourceNotFoundException;
import hiperium.cities.commons.loggers.HiperiumLogger;
import hiperium.city.devices.read.function.dto.DeviceReadRequest;
import hiperium.city.devices.read.function.entities.Device;
import hiperium.city.devices.read.function.mappers.DeviceMapper;
import hiperium.city.devices.read.function.repository.DevicesRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * The DevicesService class is a service class that provides methods for working with devices.
 */
@Service
public class DevicesService {

    private static final HiperiumLogger LOGGER = new HiperiumLogger(DevicesService.class);

    private final DeviceMapper deviceMapper;
    private final DevicesRepository devicesRepository;

    /**
     * Represents a service class that provides methods for working with devices.
     *
     * @param deviceMapper         The mapper used for mapping device data between different representations.
     * @param devicesRepository    The repository used for retrieving devices from the DynamoDB table.
     */
    public DevicesService(DeviceMapper deviceMapper, DevicesRepository devicesRepository) {
        this.deviceMapper = deviceMapper;
        this.devicesRepository = devicesRepository;
    }

    /**
     * Finds a device by its ID.
     *
     * @param deviceReadRequest The request object containing the device ID and city ID.
     * @return A Mono that emits the found Device object, or throws a ResourceNotFoundException if no device is found.
     */
    public Mono<Device> findById(final DeviceReadRequest deviceReadRequest) {
        return Mono.fromCompletionStage(() -> this.devicesRepository.findByIdAsync(deviceReadRequest))
            .handle((returnedItem, sink) -> {
                if (Objects.isNull(returnedItem) || returnedItem.isEmpty()) {
                    LOGGER.error("No device found with the provided ID.", deviceReadRequest);
                    sink.error(new ResourceNotFoundException("No device found with the provided ID."));
                    return;
                }
                sink.next(this.deviceMapper.mapToDevice(returnedItem));
            });
    }
}
