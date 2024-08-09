package hiperium.city.devices.data.function.dto;

import hiperium.city.devices.data.function.common.DeviceStatus;

/**
 * Represents a response object that contains information about a device.
 */
public record DeviceDataResponse(

    String id,
    String name,
    String cityId,
    DeviceStatus status,
    Integer httpStatus,
    String errorMessage) {

    /**
     * Represents a builder class for creating instances of {@link DeviceDataResponse}.
     */
    public static class Builder {
        private String id;
        private String name;
        private String cityId;
        private DeviceStatus status;
        private Integer httpStatus;
        private String errorMessage;

        /**
         * Sets the ID of the device response.
         *
         * @param id the ID of the device response
         * @return the updated builder object
         */
        public Builder id(String id) {
            this.id = id;
            return this;
        }

        /**
         * Sets the name of the device response.
         *
         * @param name the name of the device
         * @return the builder instance
         */
        public Builder name(String name) {
            this.name = name;
            return this;
        }

        /**
         * Sets the city ID for the device response builder.
         *
         * @param cityId the city ID to set for the device response
         * @return the device response builder
         */
        public Builder cityId(String cityId) {
            this.cityId = cityId;
            return this;
        }

        /**
         * Sets the status of the device.
         *
         * @param status the status to set for the device
         * @return the builder instance
         */
        public Builder status(DeviceStatus status) {
            this.status = status;
            return this;
        }

        /**
         * Sets the HTTP status of the DeviceDataResponse.
         *
         * @param httpStatus The HTTP status to set.
         * @return The Builder object.
         */
        public Builder httpStatus(Integer httpStatus) {
            this.httpStatus = httpStatus;
            return this;
        }

        /**
         * Sets the error message for the DeviceDataResponse builder.
         *
         * @param errorMessage The error message to be set.
         * @return The updated DeviceDataResponse builder.
         */
        public Builder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        /**
         * Builds a {@link DeviceDataResponse} object with the provided values.
         *
         * @return The built {@link DeviceDataResponse} object.
         */
        public DeviceDataResponse build() {
            return new DeviceDataResponse(id, name, cityId, status, httpStatus, errorMessage);
        }
    }
}
