package hiperium.city.devices.data.function.dto;

import hiperium.city.devices.data.function.annotations.ValidUUID;
import jakarta.validation.constraints.NotBlank;

/**
 * Represents a request to retrieve information about a device using its unique identifier.
 */
public record DeviceIdRequest(@NotBlank @ValidUUID String deviceId,
                              @NotBlank @ValidUUID String cityId) {

    /**
     * Represents a builder class for creating a {@code DeviceIdRequest} object with the specified device ID and city ID.
     */
    public static class Builder {
        private String deviceId;
        private String cityId;

        public Builder deviceId(String deviceId) {
            this.deviceId = deviceId;
            return this;
        }

        /**
         * Sets the city ID for the Builder.
         *
         * @param cityId the city ID to set
         * @return the DeviceIdRequestBuilder with the updated city ID
         */
        public Builder cityId(String cityId) {
            this.cityId = cityId;
            return this;
        }

        /**
         * Builds a {@code DeviceIdRequest} object with the given device ID and city ID.
         *
         * @return a new {@code DeviceIdRequest} object with the specified device ID and city ID.
         */
        public DeviceIdRequest build() {
            return new DeviceIdRequest(deviceId, cityId);
        }
    }
}
