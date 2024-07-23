package hiperium.city.devices.data.function.dto;

import hiperium.city.devices.data.function.common.DeviceStatus;

/**
 * Represents a response object that contains information about a city.
 */
public record DeviceResponse(

    String id,
    String name,
    String cityId,
    DeviceStatus status,
    Integer httpStatus,
    String errorMessage) {

    // Create a builder class for the DeviceResponse record.
    public static class Builder {
        private String id;
        private String name;
        private String cityId;
        private DeviceStatus status;
        private Integer httpStatus;
        private String errorMessage;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder cityId(String cityId) {
            this.cityId = cityId;
            return this;
        }

        public Builder status(DeviceStatus status) {
            this.status = status;
            return this;
        }

        public Builder httpStatus(Integer httpStatus) {
            this.httpStatus = httpStatus;
            return this;
        }

        public Builder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public DeviceResponse build() {
            return new DeviceResponse(id, name, cityId, status, httpStatus, errorMessage);
        }
    }
}
