package hiperium.city.devices.read.function.configurations;

import hiperium.cities.commons.loggers.HiperiumLogger;
import hiperium.city.devices.read.function.dto.DeviceDataResponse;
import hiperium.city.devices.read.function.functions.DeviceDataFunction;
import hiperium.city.devices.read.function.mappers.DeviceMapper;
import hiperium.city.devices.read.function.repository.DevicesRepository;
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

    public static final String FIND_BY_ID_BEAN_NAME = "findById";

    private static final HiperiumLogger LOGGER = new HiperiumLogger(FunctionsConfig.class);

    private final DeviceMapper deviceMapper;
    private final DevicesRepository devicesRepository;

    /**
     * This class represents the configuration for functions in the application.
     */
    public FunctionsConfig(DeviceMapper deviceMapper, DevicesRepository devicesRepository) {
        this.deviceMapper = deviceMapper;
        this.devicesRepository = devicesRepository;
    }

    /**
     * Creates a bean that finds a device by its identifier.
     *
     * @return The function that finds a device by its identifier.
     */
    @Bean(FIND_BY_ID_BEAN_NAME)
    public Function<Message<byte[]>, Mono<DeviceDataResponse>> findByIdFunction() {
        LOGGER.debug("Creating Device Data Function Bean...");
        return new DeviceDataFunction(this.deviceMapper, this.devicesRepository);
    }
}
