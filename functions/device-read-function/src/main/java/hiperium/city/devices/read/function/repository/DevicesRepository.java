package hiperium.city.devices.read.function.repository;

import hiperium.cities.commons.exceptions.ResourceNotFoundException;
import hiperium.cities.commons.loggers.HiperiumLogger;
import hiperium.city.devices.read.function.dto.DeviceDataRequest;
import hiperium.city.devices.read.function.entities.Device;
import hiperium.city.devices.read.function.mappers.DeviceMapper;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * The DevicesRepository class is responsible for retrieving Devices objects from the DynamoDB table.
 *
 * @apiNote The Enhanced Client has problems at runtime when used with Spring Native.
 * This is because the Enhanced Client uses reflection to create the DynamoDbClient.
 * The solution is to use the low-level client instead.
 */
@Repository
public class DevicesRepository {

    private static final HiperiumLogger LOGGER = new HiperiumLogger(DevicesRepository.class);

    private final DeviceMapper deviceMapper;
    private final DynamoDbClient dynamoDbClient;

    /**
     * The DevicesRepository class is responsible for retrieving City objects from the DynamoDB table.
     */
    public DevicesRepository(DeviceMapper deviceMapper, DynamoDbClient dynamoDbClient) {
        this.deviceMapper = deviceMapper;
        this.dynamoDbClient = dynamoDbClient;
    }

    /**
     * Finds a Device by its ID and city ID in the DynamoDB table.
     *
     * @param deviceDataRequest The request object containing the device ID and city ID.
     * @return The Device object found in the DynamoDB table.
     * @throws ResourceNotFoundException if the Device is not found with the specified ID and city ID.
     * @throws RuntimeException if there is an error finding the Device.
     */
    public Device findById(DeviceDataRequest deviceDataRequest) {
        LOGGER.debug("Find Device by ID", deviceDataRequest);

        HashMap<String, AttributeValue> keyMap = new HashMap<>();
        keyMap.put(Device.ID_COLUMN_NAME, AttributeValue.builder().s(deviceDataRequest.deviceId()).build());
        keyMap.put(Device.CITY_ID_COLUMN_NAME, AttributeValue.builder().s(deviceDataRequest.cityId()).build());
        GetItemRequest itemRequest = GetItemRequest.builder()
            .key(keyMap)
            .tableName(Device.TABLE_NAME)
            .build();

        Device device;
        try {
            Map<String, AttributeValue> returnedItem = this.dynamoDbClient.getItem(itemRequest).item();
            if (Objects.isNull(returnedItem) || returnedItem.isEmpty()) {
                throw new ResourceNotFoundException("Device not found with ID: " + deviceDataRequest.deviceId());
            }
            device = this.deviceMapper.mapToDevice(returnedItem);
        } catch (DynamoDbException exception) {
            LOGGER.error("When trying to find a Device with ID: " + deviceDataRequest.deviceId(), exception.getMessage());
            throw new RuntimeException("Error finding Device with ID: " + deviceDataRequest.deviceId(), exception);
        }
        return device;
    }
}
