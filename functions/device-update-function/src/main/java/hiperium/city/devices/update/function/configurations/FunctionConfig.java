package hiperium.city.devices.update.function.configurations;

import hiperium.cities.commons.loggers.HiperiumLogger;
import hiperium.city.devices.update.function.dto.UpdateDeviceResponse;
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
public class FunctionConfig {

    public static final String FUNCTION_BEAN_NAME = "updateStatus";
    private static final HiperiumLogger LOGGER = new HiperiumLogger(FunctionConfig.class);

    private final DevicesService devicesService;

    /**
     * Represents the configuration for functions in the application.
     */
    public FunctionConfig(DevicesService devicesService) {
        this.devicesService = devicesService;
    }

    /**
     * Creates a bean that updates a device status.
     *
     * @return The function that updates a device status.
     */
    @Bean(FUNCTION_BEAN_NAME)
    public Function<Message<byte[]>, Mono<UpdateDeviceResponse>> updateStatusFunction() {
        LOGGER.debug("Creating Update Status Function bean...");
        return new UpdateFunction(this.devicesService);
    }
}
