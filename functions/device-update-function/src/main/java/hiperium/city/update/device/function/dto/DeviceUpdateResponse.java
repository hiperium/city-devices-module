package hiperium.city.update.device.function.dto;

import java.util.Map;

/**
 * Represents a response object for the device update operation.
 */
public record DeviceUpdateResponse(

    int statusCode,
    Map<String, String> headers,
    String body) {

    /**
     * Represents a builder class for constructing instances of {@link DeviceUpdateResponse}.
     */
    public static class Builder {
        private int statusCode;
        private Map<String, String> headers;
        private String body;

        /**
         * Sets the status code for the response.
         *
         * @param statusCode the status code to set
         * @return the GenericResponseBuilder instance
         */
        public Builder statusCode(int statusCode) {
            this.statusCode = statusCode;
            return this;
        }

        /**
         * Sets the headers for the GenericResponseBuilder.
         *
         * @param headers a map representing the headers to be set
         * @return the updated GenericResponseBuilder instance
         */
        public Builder headers(Map<String, String> headers) {
            this.headers = headers;
            return this;
        }

        /**
         * Sets the body of the response.
         *
         * @param body the body content of the response
         * @return the updated GenericResponseBuilder object
         */
        public Builder body(String body) {
            this.body = body;
            return this;
        }

        /**
         * Constructs a new instance of {@link DeviceUpdateResponse} using the provided status code, headers, and body.
         *
         * @return a new instance of {@link DeviceUpdateResponse}
         */
        public DeviceUpdateResponse build() {
            return new DeviceUpdateResponse(statusCode, headers, body);
        }
    }
}
