package hiperium.city.devices.update.function.configurations;

import hiperium.cities.commons.loggers.HiperiumLogger;
import hiperium.city.devices.update.function.dto.LambdaResponse;
import hiperium.city.devices.update.function.functions.UpdateStatusFunction;
import hiperium.city.devices.update.function.repository.DeviceRepository;
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
    private static final HiperiumLogger LOGGER = new HiperiumLogger(FunctionsConfig.class);

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
    public Function<Message<byte[]>, Mono<LambdaResponse>> updateStatusFunction() {
        LOGGER.debug("Creating the Update Status Function Bean...");
        return new UpdateStatusFunction(this.deviceRepository);
    }
}
