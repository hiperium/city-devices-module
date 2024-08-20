package hiperium.city.devices.update.function.configurations;

import hiperium.cities.commons.loggers.HiperiumLogger;
import hiperium.city.devices.update.function.dto.DeviceUpdateResponse;
import hiperium.city.devices.update.function.functions.UpdateFunction;
import hiperium.city.devices.update.function.services.DevicesService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import reactor.core.publisher.Mono;

import java.util.function.Function;

/**
 * This class represents the configuration for functions in the application.
 */
@Configuration(proxyBeanMethods = false)
public class FunctionsConfig {

    public static final String UPDATE_STATUS_BEAN_NAME = "updateStatus";
    private static final HiperiumLogger LOGGER = new HiperiumLogger(FunctionsConfig.class);

    private final DevicesService devicesService;

    /**
     * Represents the configuration for functions in the application.
     */
    public FunctionsConfig(DevicesService devicesService) {
        this.devicesService = devicesService;
    }

    /**
     * Creates a bean that updates a device status.
     *
     * @return The function that updates a device status.
     */
    @Bean(UPDATE_STATUS_BEAN_NAME)
    public Function<Message<byte[]>, Mono<DeviceUpdateResponse>> updateStatusFunction() {
        LOGGER.debug("Creating Update Status Function bean...");
        return new UpdateFunction(this.devicesService);
    }
}
