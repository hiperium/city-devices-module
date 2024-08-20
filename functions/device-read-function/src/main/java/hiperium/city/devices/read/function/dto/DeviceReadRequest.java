package hiperium.city.devices.read.function.dto;

import hiperium.cities.commons.annotations.ValidUUID;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

/**
 * Represents a request to retrieve information about a device using its unique identifier.
 */
public record DeviceReadRequest(

    @NotEmpty(message = "Device ID cannot be empty.")
    @NotBlank(message = "Device ID cannot be blank.")
    @ValidUUID(message = "Device ID must have a valid format.")
    String deviceId,

    @NotEmpty(message = "City ID cannot be empty.")
    @NotBlank(message = "City ID cannot be blank.")
    @ValidUUID(message = "City ID must have a valid format.")
    String cityId) {
}
