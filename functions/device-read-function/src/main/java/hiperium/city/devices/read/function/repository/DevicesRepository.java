package hiperium.city.devices.read.function.repository;

import hiperium.cities.commons.exceptions.CityException;
import hiperium.cities.commons.loggers.HiperiumLogger;
import hiperium.city.devices.read.function.dto.DeviceReadRequest;
import hiperium.city.devices.read.function.entities.Device;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

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
     * Retrieves a device from the DynamoDB table asynchronously based on the provided device data request.
     *
     * @param deviceReadRequest The device data request containing the device ID and city ID.
     * @return A CompletableFuture that completes with a Map of item attributes representing the found device.
     * @throws CityException if an error occurs while retrieving the device.
     */
    public CompletableFuture<Map<String, AttributeValue>> findByIdAsync(DeviceReadRequest deviceReadRequest) {
        HashMap<String, AttributeValue> keyMap = new HashMap<>();
        keyMap.put(Device.ID_COLUMN_NAME, AttributeValue.builder().s(deviceReadRequest.deviceId()).build());
        keyMap.put(Device.CITY_ID_COLUMN_NAME, AttributeValue.builder().s(deviceReadRequest.cityId()).build());
        GetItemRequest itemRequest = GetItemRequest.builder()
            .key(keyMap)
            .tableName(Device.TABLE_NAME)
            .build();

        return this.dynamoDbAsyncClient.getItem(itemRequest)
            .thenApply(GetItemResponse::item)
            .exceptionally(exception -> {
                LOGGER.error("Error when trying to find a Device by ID.", exception.getMessage(), deviceReadRequest);
                throw new CityException("Error when trying to find a Device by ID.");
            });
    }
}
