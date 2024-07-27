package hiperium.city.devices.update.function.configurations;

import hiperium.city.devices.update.function.dto.EventBridgeRequest;
import hiperium.city.devices.update.function.dto.GenericResponse;
import hiperium.city.devices.update.function.functions.UpdateStatusFunction;
import hiperium.city.devices.update.function.repository.DeviceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import reactor.core.publisher.Mono;

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
     * Creates a bean that updates a device status.
     *
     * @return The function that updates a device status.
     */
    @Bean(UPDATE_STATUS_BEAN_NAME)
    public Function<Message<EventBridgeRequest>, Mono<GenericResponse>> updateStatusFunction() {
        LOGGER.debug("Configuring the Update Status function...");
        return new UpdateStatusFunction(this.deviceRepository);
    }
}
