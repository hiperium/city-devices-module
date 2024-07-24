package hiperium.city.devices.update.function.dto;

import hiperium.city.devices.update.function.common.DeviceOperation;
import hiperium.city.devices.update.function.annotations.ValidUUID;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Represents a request to update a device status using its identifier.
 */
public record DeviceUpdateRequest(

    @NotBlank
    @ValidUUID
    String deviceId,

    @NotBlank
    @ValidUUID
    String cityId,

    @NotNull
    DeviceOperation deviceOperation) {

    /**
     * This class represents a builder for creating {@code DeviceUpdateRequest} objects.
     *
     * <p>
     * Example usage:
     * </p>
     *
     * <pre>{@code
     * DeviceUpdateRequest request = new DeviceUpdateRequest.Builder()
     *     .deviceId("device123")
     *     .cityId("city456")
     *     .deviceOperation(DeviceOperation.ACTIVATE)
     *     .build();
     * }</pre>
     */
    public static class Builder {
        private String deviceId;
        private String cityId;
        private DeviceOperation deviceOperation;

        /**
         * Sets the device ID for the request.
         *
         * @param deviceId the device ID to be set
         * @return the updated Builder object
         */
        public Builder deviceId(String deviceId) {
            this.deviceId = deviceId;
            return this;
        }

        /**
         * Sets the city ID for the device ID request.
         *
         * @param cityId the city ID to be set
         * @return the builder instance with the city ID set
         */
        public Builder cityId(String cityId) {
            this.cityId = cityId;
            return this;
        }

        /**
         * Sets the device operation for the request.
         *
         * @param deviceOperation the device operation to be set
         * @return the modified Builder instance
         */
        public Builder deviceOperation(DeviceOperation deviceOperation) {
            this.deviceOperation = deviceOperation;
            return this;
        }

        /**
         * Builds a {@code DeviceUpdateRequest} object with the provided values.
         *
         * @return a new {@code DeviceUpdateRequest} object
         */
        public DeviceUpdateRequest build() {
            return new DeviceUpdateRequest(deviceId, cityId, deviceOperation);
        }
    }
}
