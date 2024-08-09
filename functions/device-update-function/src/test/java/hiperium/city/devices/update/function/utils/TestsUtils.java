package hiperium.city.devices.update.function.utils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import hiperium.city.devices.update.function.dto.EventBridgeEvent;
import hiperium.city.devices.update.function.entities.Device;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.testcontainers.shaded.org.awaitility.Awaitility;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;
import software.amazon.awssdk.services.dynamodb.model.TableStatus;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;

public final class TestsUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper()
        .findAndRegisterModules()
        .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    private TestsUtils() {
    }

    public static void waitForDynamoDbToBeReady(final DynamoDbClient dynamoDbClient) {
        Awaitility.await()
            .atMost(Duration.ofSeconds(30))         // maximum wait time
            .pollInterval(Duration.ofSeconds(3))    // check every 3 seconds
            .until(() -> {
                DescribeTableRequest request = DescribeTableRequest.builder()
                    .tableName(Device.TABLE_NAME)
                    .build();
                try {
                    TableStatus tableStatus = dynamoDbClient.describeTable(request).table().tableStatus();
                    return TableStatus.ACTIVE.equals(tableStatus);
                } catch (ResourceNotFoundException e) {
                    return false;
                }
            });
    }

    public static <T> T unmarshal(byte[] jsonBytes, Class<T> type) {
        try {
            return MAPPER.readValue(jsonBytes, type);
        } catch (IOException e) {
            throw new IllegalArgumentException("Error unmarshalling the <" + type.getSimpleName() + "> object: " +
                e.getMessage());
        }
    }

    public static Message<EventBridgeEvent> createMessage(EventBridgeEvent eventBridgeEvent) {
        return new Message<>() {
            @NonNull
            @Override
            public EventBridgeEvent getPayload() {
                return eventBridgeEvent;
            }

            @NonNull
            @Override
            public MessageHeaders getHeaders() {
                return new MessageHeaders(Map.of());
            }
        };
    }
}
