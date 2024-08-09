package hiperium.city.devices.data.function.functions;

import hiperium.cities.commons.loggers.HiperiumLogger;
import hiperium.city.devices.data.function.dto.DeviceDataRequest;
import hiperium.city.devices.data.function.dto.DeviceDataResponse;
import hiperium.city.devices.data.function.entities.CityStatus;
import hiperium.city.devices.data.function.entities.Device;
import hiperium.city.devices.data.function.mappers.DeviceMapper;
import hiperium.city.devices.data.function.validations.BeanValidations;
import jakarta.validation.ValidationException;
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
public class DeviceDataFunction implements Function<Message<DeviceDataRequest>, DeviceDataResponse> {

    private static final HiperiumLogger LOGGER = new HiperiumLogger(DeviceDataFunction.class);

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
    public DeviceDataResponse apply(Message<DeviceDataRequest> deviceIdRequestMessage) {
        LOGGER.debug("Finding Device by ID", deviceIdRequestMessage.getPayload());
        DeviceDataRequest deviceDataRequest = deviceIdRequestMessage.getPayload();
        try {
            BeanValidations.validateBean(deviceDataRequest);
        } catch (ValidationException exception) {
            LOGGER.error("Invalid device ID request", exception.getMessage());
            return new DeviceDataResponse.Builder()
                .httpStatus(HttpStatus.BAD_REQUEST.value())
                .errorMessage(exception.getMessage())
                .build();
        }

        HashMap<String, AttributeValue> keyMap = new HashMap<>();
        keyMap.put(Device.ID_COLUMN_NAME, AttributeValue.builder().s(deviceDataRequest.deviceId()).build());
        keyMap.put(Device.CITY_ID_COLUMN_NAME, AttributeValue.builder().s(deviceDataRequest.cityId()).build());
        GetItemRequest request = GetItemRequest.builder()
            .key(keyMap)
            .tableName(Device.TABLE_NAME)
            .build();

        DeviceDataResponse response;
        try {
            Map<String, AttributeValue> returnedItem = this.dynamoDbClient.getItem(request).item();
            if (Objects.isNull(returnedItem) || returnedItem.isEmpty()) {
                response = new DeviceDataResponse.Builder()
                    .httpStatus(HttpStatus.NOT_FOUND.value())
                    .errorMessage("Device not found.")
                    .build();
            } else {
                Device device = this.deviceMapper.mapDevice(returnedItem);
                if (device.cityStatus().equals(CityStatus.DISABLED)) {
                    response = new DeviceDataResponse.Builder()
                        .httpStatus(HttpStatus.NOT_ACCEPTABLE.value())
                        .errorMessage("City is disabled.")
                        .build();
                } else {
                    response = this.deviceMapper.mapDeviceResponse(device, HttpStatus.OK.value(), null);
                }
            }
        } catch (DynamoDbException exception) {
            LOGGER.error("Couldn't find a Device with ID '" + deviceDataRequest.deviceId()
                + "' and City ID '" + deviceDataRequest.cityId() + "'", exception.getMessage());
            response = new DeviceDataResponse.Builder()
                .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .errorMessage("Internal server error.")
                .build();
        }
        return response;
    }
}
