package hiperium.city.devices.update.function.services;

import hiperium.cities.commons.exceptions.ResourceNotFoundException;
import hiperium.cities.commons.loggers.HiperiumLogger;
import hiperium.city.devices.update.function.dto.EventBridgeRequest;
import hiperium.city.devices.update.function.entities.Device;
import hiperium.city.devices.update.function.mapper.DeviceMapper;
import hiperium.city.devices.update.function.repository.DevicesRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.Map;

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
     * Updates the status of a device.
     *
     * @param eventBridgeRequest The EventBridge request containing the ID and operation details of the device.
     * @return A Mono representing the completion of the update operation.
     */
    public Mono<Void> updateDeviceStatus(final EventBridgeRequest eventBridgeRequest) {
        return Mono.fromCompletionStage(() ->
                this.devicesRepository.findByIdAsync(eventBridgeRequest.detail()))
            .flatMap(deviceAttributes ->
                this.validateAndMapDeviceAttributes(deviceAttributes, eventBridgeRequest))
            .flatMap(device ->
                this.devicesRepository.updateDeviceStatusAsync(device, eventBridgeRequest.detail().deviceOperation())
            );
    }

    private Mono<Device> validateAndMapDeviceAttributes(final Map<String, AttributeValue> deviceAttributes,
                                                        final EventBridgeRequest eventBridgeRequest) {
        if (deviceAttributes == null || deviceAttributes.isEmpty()) {
            LOGGER.error("No device found with the provided ID.", eventBridgeRequest.detail());
            return Mono.error(new ResourceNotFoundException("No device found with the provided ID."));
        }
        return Mono.just(this.deviceMapper.mapToDevice(deviceAttributes));
    }
}
