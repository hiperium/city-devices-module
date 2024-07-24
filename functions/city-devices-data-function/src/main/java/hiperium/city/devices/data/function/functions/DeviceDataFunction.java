package hiperium.city.devices.data.function.functions;

import hiperium.city.devices.data.function.dto.DeviceIdRequest;
import hiperium.city.devices.data.function.dto.DeviceResponse;
import hiperium.city.devices.data.function.entities.CityStatus;
import hiperium.city.devices.data.function.entities.Device;
import hiperium.city.devices.data.function.mappers.DeviceMapper;
import hiperium.city.devices.data.function.utils.BeanValidationUtils;
import jakarta.validation.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * Represents a function that finds a device by its identifier.
 *
 * @apiNote The Enhanced Client has problems at runtime when used with Spring Native.
 * This is because the Enhanced Client uses reflection to create the DynamoDbClient.
 * The solution is to use the low-level client instead.
 */
public class DeviceDataFunction implements Function<Message<DeviceIdRequest>, DeviceResponse> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceDataFunction.class);

    private final DeviceMapper deviceMapper;
    private final DynamoDbClient dynamoDbClient;

    public DeviceDataFunction(DeviceMapper deviceMapper, DynamoDbClient dynamoDbClient) {
        this.deviceMapper = deviceMapper;
        this.dynamoDbClient = dynamoDbClient;
    }

    /**
     * Applies the given device ID request to find a device in the DynamoDB table.
     *
     * @param deviceIdRequestMessage The message containing the device ID request.
     * @return The response containing the result of the operation.
     */
    @Override
    public DeviceResponse apply(Message<DeviceIdRequest> deviceIdRequestMessage) {
        LOGGER.debug("Finding Device by ID: {}", deviceIdRequestMessage);
        DeviceIdRequest deviceIdRequest = deviceIdRequestMessage.getPayload();
        try {
            BeanValidationUtils.validateBean(deviceIdRequest);
        } catch (ValidationException exception) {
            LOGGER.error("ERROR: Invalid device ID request: {}", exception.getMessage());
            return new DeviceResponse.Builder()
                .httpStatus(HttpStatus.BAD_REQUEST.value())
                .errorMessage(exception.getMessage())
                .build();
        }

        HashMap<String, AttributeValue> keyMap = new HashMap<>();
        keyMap.put(Device.ID_COLUMN_NAME, AttributeValue.builder().s(deviceIdRequest.deviceId()).build());
        keyMap.put(Device.CITY_ID_COLUMN_NAME, AttributeValue.builder().s(deviceIdRequest.cityId()).build());
        GetItemRequest request = GetItemRequest.builder()
            .key(keyMap)
            .tableName(Device.TABLE_NAME)
            .build();

        DeviceResponse response;
        try {
            Map<String, AttributeValue> returnedItem = this.dynamoDbClient.getItem(request).item();
            if (Objects.isNull(returnedItem) || returnedItem.isEmpty()) {
                response = new DeviceResponse.Builder()
                    .httpStatus(HttpStatus.NOT_FOUND.value())
                    .errorMessage("Device not found.")
                    .build();
            } else {
                Device device = this.deviceMapper.mapDevice(returnedItem);
                if (device.cityStatus().equals(CityStatus.DISABLED)) {
                    response = new DeviceResponse.Builder()
                        .httpStatus(HttpStatus.NOT_ACCEPTABLE.value())
                        .errorMessage("City is disabled.")
                        .build();
                } else {
                    response = this.deviceMapper.mapDeviceResponse(device, HttpStatus.OK.value(), null);
                }
            }
        } catch (DynamoDbException exception) {
            LOGGER.error("ERROR: When trying to find a Device with ID '{}' for city '{}' >>> {}",
                deviceIdRequest.deviceId(), deviceIdRequest.cityId(), exception.getMessage());
            response = new DeviceResponse.Builder()
                .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .errorMessage("Internal server error.")
                .build();
        }
        return response;
    }
}
