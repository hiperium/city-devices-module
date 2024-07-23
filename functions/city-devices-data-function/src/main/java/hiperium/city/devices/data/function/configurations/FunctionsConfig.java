package hiperium.city.devices.data.function.configurations;

import hiperium.city.devices.data.function.dto.DeviceIdRequest;
import hiperium.city.devices.data.function.dto.DeviceResponse;
import hiperium.city.devices.data.function.functions.DeviceDataFunction;
import hiperium.city.devices.data.function.mappers.DeviceMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.util.function.Function;

/**
 * This class represents the configuration for functions in the application.
 */
@Configuration(proxyBeanMethods=false)
public class FunctionsConfig {

    public static final String FIND_BY_ID_BEAN_NAME = "findById";

    private static final Logger LOGGER = LoggerFactory.getLogger(FunctionsConfig.class);

    private final DeviceMapper deviceMapper;
    private final DynamoDbClient dynamoDbClient;

    public FunctionsConfig(DeviceMapper deviceMapper, DynamoDbClient dynamoDbClient) {
        this.deviceMapper = deviceMapper;
        this.dynamoDbClient = dynamoDbClient;
    }

    /**
     * Creates a bean that finds a device by its identifier.
     *
     * @return The function that finds a device by its identifier.
     */
    @Bean(FIND_BY_ID_BEAN_NAME)
    public Function<Message<DeviceIdRequest>, DeviceResponse> findById() {
        LOGGER.debug("Configuring Device Data Function...");
        return new DeviceDataFunction(this.deviceMapper, this.dynamoDbClient);
    }
}
