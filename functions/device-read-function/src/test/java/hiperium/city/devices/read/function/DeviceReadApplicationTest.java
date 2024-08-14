package hiperium.city.devices.read.function;

import hiperium.city.devices.read.function.common.TestContainersBase;
import hiperium.city.devices.read.function.configurations.FunctionsConfig;
import hiperium.city.devices.read.function.dto.DeviceDataResponse;
import hiperium.city.devices.read.function.utils.TestsUtils;
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
@FunctionalSpringBootTest(classes = DeviceReadApplication.class)
class DeviceReadApplicationTest extends TestContainersBase {

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
    void givenExistingDeviceAndEnabledCity_whenInvokeLambdaFunction_thenReturnCityData(String jsonFilePath) throws IOException {
        Function<Message<byte[]>, Mono<DeviceDataResponse>> function = this.getFunctionUnderTest();
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(jsonFilePath)) {
            assert inputStream != null;
            Message<byte[]> requestMessage = TestsUtils.createMessage(inputStream.readAllBytes());

            StepVerifier.create(function.apply(requestMessage))
                .assertNext(response -> {
                    assertThat(response).isNotNull();
                    // The status code should be a success code.
                    int statusCode = response.httpStatus();
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
        "requests/non-valid/existing-device-disabled-city.json",
    })
    void givenInvalidEvents_whenInvokeLambdaFunction_thenThrowsException(String jsonFilePath) throws IOException {
        Function<Message<byte[]>, Mono<DeviceDataResponse>> function = this.getFunctionUnderTest();
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(jsonFilePath)) {
            assert inputStream != null;
            Message<byte[]> requestMessage = TestsUtils.createMessage(inputStream.readAllBytes());

            StepVerifier.create(function.apply(requestMessage))
                .assertNext(response -> {
                    assertThat(response).isNotNull();
                    // The status code should be an error code.
                    int statusCode = response.httpStatus();
                    assertThat(statusCode >= HttpStatus.OK.value() && statusCode <= HttpStatus.IM_USED.value()).isFalse();
                })
                .verifyComplete();
        }
    }

    private Function<Message<byte[]>, Mono<DeviceDataResponse>> getFunctionUnderTest() {
        Function<Message<byte[]>, Mono<DeviceDataResponse>> function = this.functionCatalog.lookup(Function.class,
            FunctionsConfig.FIND_BY_ID_BEAN_NAME);
        assertThat(function).isNotNull();
        return function;
    }
}
