package hiperium.city.devices.update.function.configurations;

import hiperium.city.devices.update.function.dto.DeviceUpdateRequest;
import hiperium.city.devices.update.function.dto.DeviceUpdateResponse;
import hiperium.city.devices.update.function.functions.UpdateStatusFunction;
import hiperium.city.devices.update.function.repository.DeviceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import java.util.function.Function;

/**
 * This class represents the configuration for functions in the application.
 */
@Configuration(proxyBeanMethods=false)
public class FunctionsConfig {

    public static final String UPDATE_STATUS_BEAN_NAME = "updateStatusFunction";
    private static final Logger LOGGER = LoggerFactory.getLogger(FunctionsConfig.class);

    private final DeviceRepository deviceRepository;

    public FunctionsConfig(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    /**
     * Creates a bean that finds a device by its identifier.
     *
     * @return The function that finds a device by its identifier.
     */
    @Bean(UPDATE_STATUS_BEAN_NAME)
    public Function<Message<DeviceUpdateRequest>, DeviceUpdateResponse> updateStatusFunction() {
        LOGGER.debug("Configuring the Update Status function...");
        return new UpdateStatusFunction(this.deviceRepository);
    }
}
