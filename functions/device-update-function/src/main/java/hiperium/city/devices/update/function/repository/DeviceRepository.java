package hiperium.city.devices.update.function.repository;

import hiperium.cities.commons.loggers.HiperiumLogger;
import hiperium.city.devices.update.function.common.DeviceOperation;
import hiperium.city.devices.update.function.common.DeviceStatus;
import hiperium.city.devices.update.function.dto.EventBridgeEvent;
import hiperium.city.devices.update.function.entities.Device;
import hiperium.city.devices.update.function.mapper.DeviceMapper;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * The DeviceRepository class is responsible for retrieving Device objects from the DynamoDB table.
 *
 * @apiNote The Enhanced Client has problems at runtime when used with Spring Native.
 * This is because the Enhanced Client uses reflection to create the DynamoDbClient.
 * The solution is to use the low-level client instead.
 */
@Repository
public class DeviceRepository {

    private static final HiperiumLogger LOGGER = new HiperiumLogger(DeviceRepository.class);

    private final DeviceMapper deviceMapper;
    private final DynamoDbClient dynamoDbClient;

    public DeviceRepository(DeviceMapper deviceMapper, DynamoDbClient dynamoDbClient) {
        this.deviceMapper = deviceMapper;
        this.dynamoDbClient = dynamoDbClient;
    }

    /**
     * Retrieves a Device object by its device ID and city ID.
     *
     * @param eventBridgeEvent The device request containing the device ID and city ID.
     * @return The Device object if found, otherwise null.
     */
    public Device findById(final EventBridgeEvent eventBridgeEvent) {
        LOGGER.debug("Find device by ID", eventBridgeEvent.detail());

        HashMap<String, AttributeValue> keyMap = new HashMap<>();
        keyMap.put(Device.ID_COLUMN_NAME, AttributeValue.builder().s(eventBridgeEvent.detail().deviceId()).build());
        keyMap.put(Device.CITY_ID_COLUMN_NAME, AttributeValue.builder().s(eventBridgeEvent.detail().cityId()).build());
        GetItemRequest request = GetItemRequest.builder()
            .key(keyMap)
            .tableName(Device.TABLE_NAME)
            .build();

        GetItemResponse response = this.dynamoDbClient.getItem(request);
        return deviceMapper.mapDevice(response.item());
    }

    /**
     * Updates the status of a device in the DynamoDB table.
     *
     * @param eventBridgeEvent The device update request containing the device ID, city ID, and device operation.
     */
    public void updateDeviceStatus(final EventBridgeEvent eventBridgeEvent) {
        LOGGER.debug("Update device status", eventBridgeEvent.detail());

        // Change the device status
        DeviceStatus deviceStatus = eventBridgeEvent.detail().deviceOperation().equals(DeviceOperation.ACTIVATE) ?
            DeviceStatus.ON : DeviceStatus.OFF;

        // Prepare the update request
        HashMap<String, AttributeValue> keyMap = new HashMap<>();
        keyMap.put(Device.ID_COLUMN_NAME, AttributeValue.builder().s(eventBridgeEvent.detail().deviceId()).build());
        keyMap.put(Device.CITY_ID_COLUMN_NAME, AttributeValue.builder().s(eventBridgeEvent.detail().cityId()).build());

        UpdateItemRequest itemRequest = UpdateItemRequest.builder()
            .tableName(Device.TABLE_NAME)
            .key(keyMap)
            .updateExpression("SET #deviceStatus = :new_status")
            .expressionAttributeNames(Map.of("#deviceStatus", Device.STATUS_COLUMN_NAME)) // For reserved keywords
            .expressionAttributeValues(Map.of(
                ":new_status", AttributeValue.builder().s(deviceStatus.name()).build()))
            .build();

        // Update the device status
        this.dynamoDbClient.updateItem(itemRequest);
    }
}
