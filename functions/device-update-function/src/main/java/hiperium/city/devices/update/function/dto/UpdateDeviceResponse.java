package hiperium.city.devices.update.function.dto;

import hiperium.cities.commons.dto.ErrorResponse;

/**
 * Represents a response object for the device update operation.
 */
public record UpdateDeviceResponse(

    Integer statusCode,
    String body,
    ErrorResponse error) {

    /**
     * The Builder class is a utility class that provides methods for constructing an UpdateDeviceResponse object
     * with various properties.
     */
    public static class Builder {
        private Integer statusCode;
        private String body;
        private ErrorResponse error;

        /**
         * Sets the status code of the UpdateDeviceResponse.
         *
         * @param statusCode the status code to be set
         * @return the Builder object with the updated status code
         */
        public Builder statusCode(Integer statusCode) {
            this.statusCode = statusCode;
            return this;
        }

        /**
         * Sets the body of the UpdateDeviceResponse.
         *
         * @param body the body to be set
         * @return the Builder object with the updated body
         */
        public Builder body(String body) {
            this.body = body;
            return this;
        }

        /**
         * Sets the error response for the device update response builder.
         *
         * @param error the error response to be set
         * @return the updated builder instance
         */
        public Builder error(ErrorResponse error) {
            this.error = error;
            return this;
        }

        /**
         * Builds an UpdateDeviceResponse object with the provided response code, body, and error.
         *
         * @return A new UpdateDeviceResponse object.
         */
        public UpdateDeviceResponse build() {
            return new UpdateDeviceResponse(statusCode, body, error);
        }
    }
}
