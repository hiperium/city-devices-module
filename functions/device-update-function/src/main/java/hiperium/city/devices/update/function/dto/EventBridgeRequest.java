package hiperium.city.devices.update.function.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * The EventBridgeRequest class represents a request received from EventBridge.
 * It contains various attributes such as version, id, detailType, source, account, time, region, resources, and detail.
 *
 * <p>
 * The EventBridgeRequest class is a record, which provides a concise way to declare a class with final fields,
 * get methods, equals, hashCode, and toString methods automatically generated.
 * </p>
 *
 * <p>
 * Example usage:
 * </p>
 *
 * <pre>{@code
 * EventBridgeRequest eventRequest = new EventBridgeRequest("1.0", "123", "detail", "source", "account", "time",
 *     "region", List.of("resource1", "resource2"),
 *     new EventBridgeDetail("device-id", "city-id", DeviceOperation.ACTIVATE));
 * }</pre>
 *
 * <p>
 * The EventBridgeRequest class is typically used as a parameter in various methods that process EventBridge requests.
 * </p>
 *
 * @see EventBridgeDetail
 */
public record EventBridgeRequest(
    String id,
    String version,
    String source,
    String account,
    String time,
    String region,
    List<String> resources,

    @JsonProperty("detail-type")
    String detailType,

    @Valid
    @NotNull(message = "Event detail cannot be null.")
    EventBridgeDetail detail) {
}
