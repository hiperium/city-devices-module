package hiperium.city.devices.update.function.repository;

import hiperium.cities.commons.exceptions.CityException;
import hiperium.cities.commons.loggers.HiperiumLogger;
import hiperium.city.devices.update.function.commons.DeviceOperation;
import hiperium.city.devices.update.function.commons.DeviceStatus;
import hiperium.city.devices.update.function.dto.EventBridgeDetail;
import hiperium.city.devices.update.function.entities.Device;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

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

    private final DynamoDbAsyncClient dynamoDbAsyncClient;

    /**
     * The DevicesRepository class represents a repository for accessing and manipulating device data
     * in a DynamoDB database.
     *
     * @param dynamoDbAsyncClient The DynamoDB asynchronous client used to interact with the database.
     * @see DynamoDbAsyncClient
     */
    public DevicesRepository(DynamoDbAsyncClient dynamoDbAsyncClient) {
        this.dynamoDbAsyncClient = dynamoDbAsyncClient;
    }

    /**
     * Retrieves a device from the database by its ID asynchronously.
     *
     * @param eventDetail The EventBridge detail object containing the device ID and city ID.
     * @return A CompletableFuture that completes with a Map of attribute names and attribute values if the device is found, or completes exceptionally if there is an error.
     * @throws RuntimeException If there is an error when trying to find the device by ID.
     */
    public CompletableFuture<Map<String, AttributeValue>> findByIdAsync(final EventBridgeDetail eventDetail) {
        HashMap<String, AttributeValue> keyMap = new HashMap<>();
        keyMap.put(Device.ID_COLUMN_NAME, AttributeValue.builder().s(eventDetail.deviceId()).build());
        keyMap.put(Device.CITY_ID_COLUMN_NAME, AttributeValue.builder().s(eventDetail.cityId()).build());
        GetItemRequest itemRequest = GetItemRequest.builder()
            .key(keyMap)
            .tableName(Device.TABLE_NAME)
            .build();

        return this.dynamoDbAsyncClient.getItem(itemRequest)
            .thenApply(GetItemResponse::item)
            .exceptionally(exception -> {
                LOGGER.error("Error when trying to find a Device by ID.", exception.getMessage(), eventDetail);
                throw new CityException("Error when trying to find a Device by ID.");
            });
    }

    /**
     * Updates the status of a device asynchronously.
     *
     * @param device The device object to update.
     * @param deviceOperation The operation to perform on the device (ACTIVATE or INACTIVATE).
     * @return A CompletableFuture representing the completion of the update operation.
     *         The CompletableFuture will complete normally if the update is successful, and exceptionally if there is an error.
     * @throws RuntimeException if the device status couldn't be updated.
     */
    public CompletableFuture<Void> updateDeviceStatusAsync(final Device device, final DeviceOperation deviceOperation) {
        HashMap<String, AttributeValue> keyMap = new HashMap<>();
        keyMap.put(Device.ID_COLUMN_NAME, AttributeValue.builder().s(device.id()).build());
        keyMap.put(Device.CITY_ID_COLUMN_NAME, AttributeValue.builder().s(device.cityId()).build());

        DeviceStatus newDeviceStatus = deviceOperation.equals(DeviceOperation.ACTIVATE) ?
            DeviceStatus.ON : DeviceStatus.OFF;

        UpdateItemRequest itemRequest = UpdateItemRequest.builder()
            .tableName(Device.TABLE_NAME)
            .key(keyMap)
            .updateExpression("SET #deviceStatus = :new_status") // Use another field name to avoid reserved keyword.
            .expressionAttributeNames(Map.of("#deviceStatus", Device.STATUS_COLUMN_NAME))
            .expressionAttributeValues(Map.of(
                ":new_status", AttributeValue.builder().s(newDeviceStatus.name()).build()))
            .build();

        return this.dynamoDbAsyncClient.updateItem(itemRequest)
            .thenRun(() -> LOGGER.info("Successfully updated device status for Device ID: " + device.id()))
            .exceptionally(exception -> {
                LOGGER.error("Couldn't update device status.", exception.getMessage(), device);
                throw new CityException("Couldn't update device status.");
            });
    }
}
