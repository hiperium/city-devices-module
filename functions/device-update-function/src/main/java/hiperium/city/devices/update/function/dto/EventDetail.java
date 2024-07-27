package hiperium.city.devices.update.function.dto;

import hiperium.city.devices.update.function.annotations.ValidUUID;
import hiperium.city.devices.update.function.common.DeviceOperation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * The {@code EventDetail} class represents the details of an event related to a device.
 * It contains information about the device ID, city ID, and the operation performed on the device.
 * The device ID and city ID must be non-blank and valid UUIDs, and the device operation must be non-null.
 *
 * <p>
 * The {@code EventDetail} class is a record, which provides a concise way to declare a class with final fields,
 * get methods, equals, hashCode, and toString methods automatically generated.
 * </p>
 *
 * <p>
 * Example usage:
 * </p>
 *
 * <pre>{@code
 * EventDetail eventDetail = new EventDetail("device-id", "city-id", DeviceOperation.ACTIVATE);
 * }</pre>
 *
 * <p>
 * The valid UUID format used for device ID and city ID is "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"
 * </p>
 *
 * <p>
 * The {@code EventDetail} class is typically used as a member of the {@code EventBridgeRequest} class.
 * </p>
 *
 * @see EventBridgeRequest
 * @since 1.0.0
 */
public record EventDetail(

    @NotBlank
    @ValidUUID
    String deviceId,

    @NotBlank
    @ValidUUID
    String cityId,

    @NotNull
    DeviceOperation deviceOperation) {
}
