package hiperium.city.devices.data.function.mappers;

import hiperium.city.devices.data.function.dto.DeviceDataResponse;
import hiperium.city.devices.data.function.entities.CityStatus;
import hiperium.city.devices.data.function.common.DeviceStatus;
import hiperium.city.devices.data.function.entities.Device;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.Map;

/**
 * The DeviceMapper interface is responsible for mapping device data between different representations.
 * It provides methods to convert a map of item attributes to a Device object, and to convert a Device object to a response object.
 * The interface also provides helper methods to retrieve values from attribute maps and convert them to enum values.
 */
@Mapper(componentModel = "spring")
public interface DeviceMapper {

    /**
     * Converts a map of item attributes to a Device object.
     *
     * @param itemAttributesMap The map of item attributes.
     * @return The converted Device object.
     */
    @Mapping(target = "id",          expression = "java(getStringValueFromAttributesMap(itemAttributesMap, Device.ID_COLUMN_NAME))")
    @Mapping(target = "name",        expression = "java(getStringValueFromAttributesMap(itemAttributesMap, Device.NAME_COLUMN_NAME))")
    @Mapping(target = "status",      expression = "java(getDeviceStatusEnumFromAttributesMap(itemAttributesMap))")
    @Mapping(target = "description", expression = "java(getStringValueFromAttributesMap(itemAttributesMap, Device.DESCRIPTION_COLUMN_NAME))")
    @Mapping(target = "cityId",      expression = "java(getStringValueFromAttributesMap(itemAttributesMap, Device.CITY_ID_COLUMN_NAME))")
    @Mapping(target = "cityStatus",  expression = "java(getCityStatusEnumFromAttributesMap(itemAttributesMap))")
    Device mapDevice(Map<String, AttributeValue> itemAttributesMap);

    /**
     * Converts a {@link Device} object to a {@link DeviceDataResponse} object with the specified HTTP status and error message.
     *
     * @param device        The {@link Device} object to convert.
     * @param httpStatus    The HTTP status code.
     * @param errorMessage  The error message.
     * @return The converted {@link DeviceDataResponse} object.
     */
    DeviceDataResponse mapDeviceResponse(Device device, int httpStatus, String errorMessage);

    /**
     * Retrieves the string value associated with the specified key from the given attributes map.
     *
     * @param attributesMap the map containing the attribute values
     * @param key           the key of the desired value
     * @return the string value associated with the key if it exists, or null if the key is not present in the map
     */
    default String getStringValueFromAttributesMap(Map<String, AttributeValue> attributesMap, String key) {
        return attributesMap.containsKey(key) ? attributesMap.get(key).s() : null;
    }

    /**
     * Retrieves the DeviceStatus enum value from the given attributes map.
     *
     * @param itemAttributesMap The map containing the device attributes.
     * @return The DeviceStatus enum value retrieved from the attributes map.
     */
    default DeviceStatus getDeviceStatusEnumFromAttributesMap(Map<String, AttributeValue> itemAttributesMap) {
        return DeviceStatus.valueOf(this.getStringValueFromAttributesMap(itemAttributesMap, Device.STATUS_COLUMN_NAME));
    }

    /**
     * Retrieves the CityStatus enum value from the given attributes map.
     *
     * @param itemAttributesMap The map containing the item attributes.
     *                          The key is the attribute name and the value is the AttributeValue.
     * @return The CityStatus enum value obtained from the attributes map.
     * @throws IllegalArgumentException If the CityStatus enum value cannot be obtained
     *                                  from the attribute map.
     */
    default CityStatus getCityStatusEnumFromAttributesMap(Map<String, AttributeValue> itemAttributesMap) {
        return CityStatus.valueOf(this.getStringValueFromAttributesMap(itemAttributesMap, Device.CITY_STATUS_COLUMN_NAME));
    }
}
