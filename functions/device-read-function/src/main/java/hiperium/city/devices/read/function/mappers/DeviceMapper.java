package hiperium.city.devices.read.function.mappers;

import hiperium.cities.commons.loggers.HiperiumLogger;
import hiperium.city.devices.read.function.common.DeviceStatus;
import hiperium.city.devices.read.function.dto.DeviceReadResponse;
import hiperium.city.devices.read.function.entities.Device;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.Map;

/**
 * The DeviceMapper interface is responsible for mapping device data between different representations.
 * It provides methods to convert a map of item attributes to a Device object, and to convert a Device object to a response object.
 * The interface also provides helper methods to retrieve values from attribute maps and convert them to enum values.
 */
@Mapper(componentModel = "spring")
public interface DeviceMapper {

    HiperiumLogger LOGGER = new HiperiumLogger(DeviceMapper.class);

    /**
     * Converts a map of item attributes to a Device object.
     *
     * @param itemAttributesMap The map of item attributes.
     * @return The converted Device object.
     */
    @Mapping(target = "id",           expression = "java(getStringValueFromAttributesMap(itemAttributesMap, Device.ID_COLUMN_NAME))")
    @Mapping(target = "name",         expression = "java(getStringValueFromAttributesMap(itemAttributesMap, Device.NAME_COLUMN_NAME))")
    @Mapping(target = "cityId",       expression = "java(getStringValueFromAttributesMap(itemAttributesMap, Device.CITY_ID_COLUMN_NAME))")
    @Mapping(target = "status",       expression = "java(getDeviceStatusEnumFromAttributesMap(itemAttributesMap))")
    @Mapping(target = "description",  expression = "java(getStringValueFromAttributesMap(itemAttributesMap, Device.DESCRIPTION_COLUMN_NAME))")
    Device mapToDevice(Map<String, AttributeValue> itemAttributesMap);

    /**
     * Converts a {@link Device} object to a {@link DeviceReadResponse} object with the specified HTTP status and error message.
     *
     * @param device The {@link Device} object to convert.
     * @return       The converted {@link DeviceReadResponse} object.
     */
    @Mapping(target = "error", ignore = true)
    DeviceReadResponse mapToDeviceResponse(Device device);

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
     * Performs operations after mapping from source to a target object in the {@link DeviceMapper} class.
     *
     * @param device               The mapped {@link Device} object.
     * @param itemAttributesMap    The map of item attributes used for mapping.
     */
    @AfterMapping
    default void afterMapToDevice(@MappingTarget Device device, Map<String, AttributeValue> itemAttributesMap) {
        LOGGER.debug("Mapped device", device);
    }

    /**
     * Performs the necessary operations after mapping a Device object to a DeviceReadResponse object.
     *
     * @param response The mapped DeviceReadResponse object.
     * @param device   The original Device object.
     */
    @AfterMapping
    default void afterMapToResponse(@MappingTarget DeviceReadResponse response, Device device) {
        LOGGER.debug("Mapped response", response);
    }
}
