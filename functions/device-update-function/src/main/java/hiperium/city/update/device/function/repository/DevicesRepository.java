package hiperium.city.update.device.function.repository;

import hiperium.cities.commons.exceptions.ResourceNotFoundException;
import hiperium.cities.commons.loggers.HiperiumLogger;
import hiperium.city.update.device.function.common.DeviceOperation;
import hiperium.city.update.device.function.common.DeviceStatus;
import hiperium.city.update.device.function.dto.EventBridgeEvent;
import hiperium.city.update.device.function.dto.EventBridgeEventDetail;
import hiperium.city.update.device.function.entities.Device;
import hiperium.city.update.device.function.mapper.DeviceMapper;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * The DevicesRepository class is responsible for retrieving Device objects from the DynamoDB table.
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
     * Represents a repository for managing devices.
     * <p>
     * The DevicesRepository class provides methods to retrieve and update device information using a DynamoDB table.
     *
     * @param deviceMapper   The mapper responsible for converting device data between different representations.
     * @param dynamoDbClient The AWS DynamoDB client used for interacting with the DynamoDB table.
     */
    public DevicesRepository(DeviceMapper deviceMapper, DynamoDbClient dynamoDbClient) {
        this.deviceMapper = deviceMapper;
        this.dynamoDbClient = dynamoDbClient;
    }

    /**
     * Retrieves a Device object by its device ID and city ID.
     *
     * @param eventBridgeEvent The device request containing the device ID and city ID.
     * @return The Device object if found, otherwise null.
     */
    public Device findByIdAndChangeStatus(final EventBridgeEvent eventBridgeEvent) {
        LOGGER.debug("Find device by ID", eventBridgeEvent.detail());
        EventBridgeEventDetail eventDetail = eventBridgeEvent.detail();

        HashMap<String, AttributeValue> keyMap = new HashMap<>();
        keyMap.put(Device.ID_COLUMN_NAME, AttributeValue.builder().s(eventDetail.deviceId()).build());
        keyMap.put(Device.CITY_ID_COLUMN_NAME, AttributeValue.builder().s(eventDetail.cityId()).build());
        GetItemRequest itemRequest = GetItemRequest.builder()
            .key(keyMap)
            .tableName(Device.TABLE_NAME)
            .build();

        Device device;
        try {
            Map<String, AttributeValue> returnedItem = this.dynamoDbClient.getItem(itemRequest).item();
            if (Objects.isNull(returnedItem) || returnedItem.isEmpty()) {
                throw new ResourceNotFoundException("Device not found with ID: " + eventDetail.deviceId());
            }
            DeviceStatus newDeviceStatus = eventDetail.deviceOperation().equals(DeviceOperation.ACTIVATE) ?
                DeviceStatus.ON : DeviceStatus.OFF;
            device = this.deviceMapper.mapToDeviceWithNewStatus(returnedItem, newDeviceStatus);

        } catch (DynamoDbException exception) {
            LOGGER.error("When trying to find a Device with ID: " + eventDetail.deviceId(), exception.getMessage());
            throw new RuntimeException("Error finding Device with ID: " + eventDetail.deviceId(), exception);
        }
        return device;
    }

    public void updateDeviceStatus(final Device device) {
        LOGGER.debug("Update device status", device);

        // Prepare the update request
        HashMap<String, AttributeValue> keyMap = new HashMap<>();
        keyMap.put(Device.ID_COLUMN_NAME, AttributeValue.builder().s(device.id()).build());
        keyMap.put(Device.CITY_ID_COLUMN_NAME, AttributeValue.builder().s(device.cityId()).build());

        UpdateItemRequest itemRequest = UpdateItemRequest.builder()
            .tableName(Device.TABLE_NAME)
            .key(keyMap)
            .updateExpression("SET #deviceStatus = :new_status")
            .expressionAttributeNames(Map.of("#deviceStatus", Device.STATUS_COLUMN_NAME)) // For reserved keywords
            .expressionAttributeValues(Map.of(
                ":new_status", AttributeValue.builder().s(device.deviceStatus().name()).build()))
            .build();

        // Update the device status
        this.dynamoDbClient.updateItem(itemRequest);
    }
}
