package hiperium.city.devices.update.function;

import hiperium.city.devices.update.function.common.TestContainersBase;
import hiperium.city.devices.update.function.configurations.FunctionsConfig;
import hiperium.city.devices.update.function.dto.EventBridgeRequest;
import hiperium.city.devices.update.function.dto.GenericResponse;
import hiperium.city.devices.update.function.utils.TestsUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.function.context.FunctionCatalog;
import org.springframework.cloud.function.context.test.FunctionalSpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@FunctionalSpringBootTest(classes = DeviceUpdateApplication.class)
class DeviceUpdateApplicationTest extends TestContainersBase {

    @Autowired
    private DynamoDbClient dynamoDbClient;

    @Autowired
    private FunctionCatalog functionCatalog;

    @BeforeEach
    void init() {
        TestsUtils.waitForDynamoDbToBeReady(this.dynamoDbClient);
    }

    @ParameterizedTest
    @DisplayName("Valid requests")
    @ValueSource(strings = {
        "requests/valid/lambda-valid-id-request.json"
    })
    void givenValidEvent_whenInvokeLambdaFunction_thenExecuteSuccessfully(String jsonFilePath) throws IOException {
        Function<Message<EventBridgeRequest>, Mono<GenericResponse>> createEventFunction = this.getFunctionUnderTest();
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(jsonFilePath)) {
            assert inputStream != null;
            EventBridgeRequest event = TestsUtils.unmarshal(inputStream.readAllBytes(), EventBridgeRequest.class);
            Message<EventBridgeRequest> requestMessage = TestsUtils.createMessage(event);

            StepVerifier.create(createEventFunction.apply(requestMessage))
                .assertNext(response -> {
                    assertThat(response).isNotNull();

                    // The status code should be a success code.
                    int statusCode = response.statusCode();
                    assertThat(statusCode >= HttpStatus.OK.value() && statusCode <= HttpStatus.IM_USED.value()).isTrue();
                })
                .verifyComplete();
        }
    }

    @ParameterizedTest
    @DisplayName("Non-valid requests")
    @ValueSource(strings = {
        "requests/non-valid/empty-device-id.json",
        "requests/non-valid/wrong-device-uuid.json",
        "requests/non-valid/non-existing-city.json",
        "requests/non-valid/non-existing-device.json",
        "requests/non-valid/existing-device-disabled-city.json"
    })
    void givenInvalidEvents_whenInvokeLambdaFunction_thenThrowsException(String jsonFilePath) throws IOException {
        Function<Message<EventBridgeRequest>, Mono<GenericResponse>> createEventFunction = this.getFunctionUnderTest();
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(jsonFilePath)) {
            assert inputStream != null;
            EventBridgeRequest event = TestsUtils.unmarshal(inputStream.readAllBytes(), EventBridgeRequest.class);
            Message<EventBridgeRequest> requestMessage = TestsUtils.createMessage(event);

            StepVerifier.create(createEventFunction.apply(requestMessage))
                .assertNext(response -> {
                    assertThat(response).isNotNull();

                    // The status code should be an error code.
                    int statusCode = response.statusCode();
                    assertThat(statusCode >= HttpStatus.OK.value() && statusCode <= HttpStatus.IM_USED.value()).isFalse();
                })
                .verifyComplete();
        }
    }

    private Function<Message<EventBridgeRequest>, Mono<GenericResponse>> getFunctionUnderTest() {
        Function<Message<EventBridgeRequest>, Mono<GenericResponse>> function = this.functionCatalog.lookup(Function.class,
            FunctionsConfig.UPDATE_STATUS_BEAN_NAME);
        assertThat(function).isNotNull();
        return function;
    }
}
