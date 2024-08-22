package hiperium.city.devices.update.function.repository;

import hiperium.cities.commons.exceptions.CityException;
import hiperium.cities.commons.loggers.HiperiumLogger;
import hiperium.city.devices.update.function.commons.DeviceOperation;
import hiperium.city.devices.update.function.commons.DeviceStatus;
import hiperium.city.devices.update.function.dto.EventBridgeDetail;
import hiperium.city.devices.update.function.entities.Device;
import lombok.NonNull;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

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
     * @param device The device object to update the status for. Must not be null.
     * @param deviceOperation The device operation that represents the new status. Must not be null.
     * @return A Mono<Void> representing the completion of the update operation.
     * @throws IllegalStateException If the DynamoDbAsyncClient is not initialized.
     */
    public Mono<Void> updateDeviceStatusAsync(@NonNull final Device device,
                                              @NonNull final DeviceOperation deviceOperation) {
        final String deviceId = device.id();
        final String cityId = device.cityId();
        final DeviceStatus newDeviceStatus = this.mapDeviceOperationToStatus(deviceOperation);

        HashMap<String, AttributeValue> keyMap = new HashMap<>();
        keyMap.put(Device.ID_COLUMN_NAME, AttributeValue.builder().s(deviceId).build());
        keyMap.put(Device.CITY_ID_COLUMN_NAME, AttributeValue.builder().s(cityId).build());

        UpdateItemRequest updateItemRequest = this.createUpdateItemRequest(keyMap, newDeviceStatus);

        return Mono.justOrEmpty(this.dynamoDbAsyncClient)
            .flatMap(client -> Mono.fromCompletionStage(client.updateItem(updateItemRequest))
                .doOnSuccess(response -> LOGGER.info("Successfully updated device status for Device ID: " + deviceId))
                .doOnError(exception -> LOGGER.error("Couldn't update device status.", exception.getMessage(), device))
                .onErrorMap(DynamoDbException.class, exception -> new CompletionException("Couldn't update device status.", exception))
            )
            .switchIfEmpty(Mono.error(new CityException("DynamoDbAsyncClient is not initialized.")))
            .then();
    }

    private DeviceStatus mapDeviceOperationToStatus(DeviceOperation deviceOperation){
        return deviceOperation.equals(DeviceOperation.ACTIVATE) ? DeviceStatus.ON : DeviceStatus.OFF;
    }

    private UpdateItemRequest createUpdateItemRequest(final Map<String, AttributeValue> keyMap,
                                                      final DeviceStatus newDeviceStatus) {
        return UpdateItemRequest.builder()
            .tableName(Device.TABLE_NAME)
            .key(keyMap)
            .updateExpression("SET #deviceStatus = :new_status")
            .expressionAttributeNames(Map.of("#deviceStatus", Device.STATUS_COLUMN_NAME))
            .expressionAttributeValues(Map.of(":new_status", AttributeValue.builder().s(newDeviceStatus.name()).build()))
            .build();
    }
}
