package hiperium.city.devices.read.function.dto;

import hiperium.cities.commons.dto.ErrorResponse;
import hiperium.city.devices.read.function.common.DeviceStatus;

/**
 * Represents a response object that contains information about a device.
 */
public record ReadDeviceResponse(

    String id,
    String name,
    String cityId,
    DeviceStatus status,
    ErrorResponse error) {
}
