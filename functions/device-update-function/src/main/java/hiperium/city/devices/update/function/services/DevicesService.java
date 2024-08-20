package hiperium.city.devices.update.function.services;

import hiperium.cities.commons.exceptions.ResourceNotFoundException;
import hiperium.cities.commons.loggers.HiperiumLogger;
import hiperium.city.devices.update.function.dto.EventBridgeRequest;
import hiperium.city.devices.update.function.entities.Device;
import hiperium.city.devices.update.function.mapper.DeviceMapper;
import hiperium.city.devices.update.function.repository.DevicesRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * The DevicesService class is responsible for performing operations related to devices.
 * It provides methods to update the status of a device and retrieve device information.
 */
@Service
public class DevicesService {

    private static final HiperiumLogger LOGGER = new HiperiumLogger(DevicesService.class);

    private final DeviceMapper deviceMapper;
    private final DevicesRepository devicesRepository;

    /**
     * The DevicesService class is responsible for performing operations related to devices.
     * It provides methods to update the status of a device and retrieve device information.
     *
     * @param deviceMapper        The DeviceMapper instance used for mapping device data.
     * @param devicesRepository   The DevicesRepository instance used for retrieving device information.
     */
    public DevicesService(DeviceMapper deviceMapper, DevicesRepository devicesRepository) {
        this.deviceMapper = deviceMapper;
        this.devicesRepository = devicesRepository;
    }

    /**
     * Updates the status of a device asynchronously.
     *
     * @param eventBridgeRequest The event object containing the device ID, city ID, and device operation.
     * @return A Mono<Void> representing the completion of the update operation.
     * @throws ResourceNotFoundException If no device is found with the provided ID.
     */
    public Mono<Void> updateDeviceStatus(final EventBridgeRequest eventBridgeRequest) {
        return Mono.fromCompletionStage(() -> this.devicesRepository.findByIdAsync(eventBridgeRequest.detail()))
            .flatMap(returnedItem -> {
                if (Objects.isNull(returnedItem) || returnedItem.isEmpty()) {
                    LOGGER.error("No device found with the provided ID.", eventBridgeRequest.detail());
                    return Mono.error(new ResourceNotFoundException("No device found with the provided ID."));
                }
                Device device = this.deviceMapper.mapToDevice(returnedItem);
                return Mono.just(device);
            })
            .flatMap(device -> Mono.fromCompletionStage(
                () -> this.devicesRepository.updateDeviceStatusAsync(device, eventBridgeRequest.detail().deviceOperation())
            ));
    }
}
