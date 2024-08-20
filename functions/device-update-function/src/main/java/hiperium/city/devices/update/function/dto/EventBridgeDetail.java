package hiperium.city.devices.update.function.dto;

import hiperium.cities.commons.annotations.ValidUUID;
import hiperium.city.devices.update.function.commons.DeviceOperation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * The {@code EventBridgeDetail} class represents the details of an event related to a device.
 * It contains information about the device ID, city ID, and the operation performed on the device.
 */
public record EventBridgeDetail(

    @NotEmpty(message = "Device ID cannot be empty.")
    @NotBlank(message = "Device ID cannot be blank.")
    @ValidUUID(message = "Device ID must have a valid format.")
    String deviceId,

    @NotEmpty(message = "City ID cannot be empty.")
    @NotBlank(message = "City ID cannot be blank.")
    @ValidUUID(message = "City ID must have a valid format.")
    String cityId,

    @NotNull(message = "Device operation cannot be null.")
    DeviceOperation deviceOperation) {
}
