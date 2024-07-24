package hiperium.city.devices.data.function;

import hiperium.city.devices.data.function.common.TestContainersBase;
import hiperium.city.devices.data.function.configurations.FunctionsConfig;
import hiperium.city.devices.data.function.dto.DeviceIdRequest;
import hiperium.city.devices.data.function.dto.DeviceResponse;
import hiperium.city.devices.data.function.utils.TestsUtils;
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
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@FunctionalSpringBootTest(classes = FunctionalApplication.class)
class FunctionalApplicationTest extends TestContainersBase {

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
        Function<Message<DeviceIdRequest>, DeviceResponse> function = this.getFunctionUnderTest();
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(jsonFilePath)) {
            assert inputStream != null;
            DeviceIdRequest deviceIdRequest = TestsUtils.unmarshal(inputStream.readAllBytes(), DeviceIdRequest.class);

            Message<DeviceIdRequest> requestMessage = TestsUtils.createMessage(deviceIdRequest);
            DeviceResponse response = function.apply(requestMessage);

            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(deviceIdRequest.deviceId());
            assertThat(response.cityId()).isEqualTo(deviceIdRequest.cityId());
            assertThat(response.httpStatus()).isEqualTo(HttpStatus.OK.value());
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
        Function<Message<DeviceIdRequest>, DeviceResponse> function = this.getFunctionUnderTest();
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(jsonFilePath)) {
            assert inputStream != null;
            DeviceIdRequest event = TestsUtils.unmarshal(inputStream.readAllBytes(), DeviceIdRequest.class);

            Message<DeviceIdRequest> requestMessage = TestsUtils.createMessage(event);
            DeviceResponse response = function.apply(requestMessage);

            assertThat(response).isNotNull();
            assertThat(response.id()).isNull();
            assertThat(response.cityId()).isNull();
            assertThat(response.httpStatus()).isNotEqualTo(HttpStatus.OK.value());
            assertThat(response.errorMessage()).isNotBlank();
        }
    }

    private Function<Message<DeviceIdRequest>, DeviceResponse> getFunctionUnderTest() {
        Function<Message<DeviceIdRequest>, DeviceResponse> function = this.functionCatalog.lookup(Function.class,
            FunctionsConfig.FIND_BY_ID_BEAN_NAME);
        assertThat(function).isNotNull();
        return function;
    }
}
