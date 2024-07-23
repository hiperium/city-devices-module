package hiperium.city.devices.data.function;

import hiperium.city.devices.data.function.common.TestContainersBase;
import hiperium.city.devices.data.function.utils.TestsUtils;
import hiperium.city.devices.data.function.configurations.FunctionsConfig;
import hiperium.city.devices.data.function.dto.DeviceIdRequest;
import hiperium.city.devices.data.function.dto.DeviceResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.function.context.FunctionCatalog;
import org.springframework.cloud.function.context.test.FunctionalSpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.shaded.org.apache.commons.lang3.StringUtils;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@FunctionalSpringBootTest(classes = FunctionalApplication.class)
class FunctionalApplicationTest extends TestContainersBase {

    private static final String ENABLED_CITY_ID = "a0ecb466-7ef5-47bf-a1ca-12f9f9328528";
    private static final String DISABLED_CITY_ID = "a0ecb466-7ef5-47bf-a1ca-12f9f9328529";
    private static final String NON_EXISTING_CITY_ID = "a0ecb466-7ef5-47bf-a1ca-12f9f9328530";

    private static final String EXISTING_DEVICE_1 = "37f44ed4-b672-4f81-a579-47679c0d6f31";
    private static final String EXISTING_DEVICE_2 = "37f44ed4-b672-4f81-a579-47679c0d6f32";
    private static final String NON_EXISTING_DEVICE_ID = "39f44ed4-b672-4f81-a579-47679c0d6f31";

    @Autowired
    private DynamoDbClient dynamoDbClient;

    @Autowired
    private FunctionCatalog functionCatalog;

    @BeforeEach
    void init() {
        TestsUtils.waitForDynamoDbToBeReady(this.dynamoDbClient);
    }

    @Test
    @DisplayName("Existing device - Enabled city")
    void givenExistingDeviceAndEnabledCity_whenInvokeLambdaFunction_thenReturnCityData() {
        Function<Message<DeviceIdRequest>, DeviceResponse> function = this.getFunctionUnderTest();
        Message<DeviceIdRequest> requestMessage = TestsUtils.createMessage(
            new DeviceIdRequest.Builder()
                .deviceId(EXISTING_DEVICE_1)
                .cityId(ENABLED_CITY_ID)
                .build());
        DeviceResponse response = function.apply(requestMessage);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(EXISTING_DEVICE_1);
        assertThat(response.cityId()).isEqualTo(ENABLED_CITY_ID);
        assertThat(response.httpStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("Existing device - Disabled city")
    void givenExistingDeviceAndDisabledCity_whenInvokeLambdaFunction_thenError() {
        Function<Message<DeviceIdRequest>, DeviceResponse> function = this.getFunctionUnderTest();
        Message<DeviceIdRequest> requestMessage = TestsUtils.createMessage(
            new DeviceIdRequest.Builder()
                .deviceId(EXISTING_DEVICE_2)
                .cityId(DISABLED_CITY_ID)
                .build());
        DeviceResponse response = function.apply(requestMessage);

        assertThat(response).isNotNull();
        assertThat(response.id()).isNull();
        assertThat(response.cityId()).isNull();
        assertThat(response.httpStatus()).isEqualTo(HttpStatus.NOT_ACCEPTABLE.value());
        assertThat(response.errorMessage()).isNotBlank();
    }

    @Test
    @DisplayName("Non-existing Device")
    void givenNonExistingDevice_whenInvokeLambdaFunction_thenReturnError() {
        Function<Message<DeviceIdRequest>, DeviceResponse> function = this.getFunctionUnderTest();
        Message<DeviceIdRequest> requestMessage = TestsUtils.createMessage(
            new DeviceIdRequest.Builder()
                .deviceId(NON_EXISTING_DEVICE_ID)
                .cityId(ENABLED_CITY_ID)
                .build());
        DeviceResponse response = function.apply(requestMessage);

        assertThat(response).isNotNull();
        assertThat(response.id()).isNull();
        assertThat(response.cityId()).isNull();
        assertThat(response.httpStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(response.errorMessage()).isNotBlank();
    }

    @Test
    @DisplayName("Non-existing City")
    void givenNonExistingCity_whenInvokeLambdaFunction_thenReturnError() {
        Function<Message<DeviceIdRequest>, DeviceResponse> function = this.getFunctionUnderTest();
        Message<DeviceIdRequest> requestMessage = TestsUtils.createMessage(
            new DeviceIdRequest.Builder()
                .deviceId(EXISTING_DEVICE_1)
                .cityId(NON_EXISTING_CITY_ID)
                .build());
        DeviceResponse response = function.apply(requestMessage);

        assertThat(response).isNotNull();
        assertThat(response.id()).isNull();
        assertThat(response.cityId()).isNull();
        assertThat(response.httpStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(response.errorMessage()).isNotBlank();
    }

    @Test
    @DisplayName("Blank City ID param")
    void givenBlankCityParam_whenInvokeLambdaFunction_thenReturnError() {
        Function<Message<DeviceIdRequest>, DeviceResponse> function = this.getFunctionUnderTest();
        Message<DeviceIdRequest> requestMessage = TestsUtils.createMessage(
            new DeviceIdRequest.Builder()
                .deviceId(EXISTING_DEVICE_1)
                .cityId(StringUtils.SPACE)
                .build());
        DeviceResponse response = function.apply(requestMessage);

        assertThat(response).isNotNull();
        assertThat(response.id()).isNull();
        assertThat(response.cityId()).isNull();
        assertThat(response.httpStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.errorMessage()).isNotBlank();
    }

    @Test
    @DisplayName("Invalid Device ID param")
    void givenInvalidDeviceParam_whenInvokeLambdaFunction_thenReturnError() {
        Function<Message<DeviceIdRequest>, DeviceResponse> function = this.getFunctionUnderTest();
        Message<DeviceIdRequest> requestMessage = TestsUtils.createMessage(
            new DeviceIdRequest.Builder()
                .deviceId("a0ecb466-7ef5-47bf")
                .cityId(ENABLED_CITY_ID)
                .build());
        DeviceResponse response = function.apply(requestMessage);

        assertThat(response).isNotNull();
        assertThat(response.id()).isNull();
        assertThat(response.cityId()).isNull();
        assertThat(response.httpStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.errorMessage()).isNotBlank();
    }

    private Function<Message<DeviceIdRequest>, DeviceResponse> getFunctionUnderTest() {
        Function<Message<DeviceIdRequest>, DeviceResponse> function = this.functionCatalog.lookup(Function.class,
            FunctionsConfig.FIND_BY_ID_BEAN_NAME);
        assertThat(function).isNotNull();
        return function;
    }
}
