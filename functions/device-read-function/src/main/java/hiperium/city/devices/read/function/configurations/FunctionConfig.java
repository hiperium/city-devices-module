package hiperium.city.devices.read.function.configurations;

import hiperium.cities.commons.loggers.HiperiumLogger;
import hiperium.city.devices.read.function.dto.ReadDeviceResponse;
import hiperium.city.devices.read.function.functions.ReadFunction;
import hiperium.city.devices.read.function.mappers.DeviceMapper;
import hiperium.city.devices.read.function.services.DevicesService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import reactor.core.publisher.Mono;

import java.util.function.Function;

/**
 * This class represents the configuration for functions in the application.
 */
@Configuration(proxyBeanMethods=false)
public class FunctionConfig {

    public static final String FUNCTION_BEAN_NAME = "findById";

    private static final HiperiumLogger LOGGER = new HiperiumLogger(FunctionConfig.class);

    private final DeviceMapper deviceMapper;
    private final DevicesService devicesService;

    /**
     * This class represents the configuration for functions in the application.
     *
     * @param deviceMapper The DeviceMapper used for mapping device data between different representations.
     * @param devicesService The DevicesService used for working with devices.
     */
    public FunctionConfig(DeviceMapper deviceMapper, DevicesService devicesService) {
        this.deviceMapper = deviceMapper;
        this.devicesService = devicesService;
    }

    /**
     * Creates a bean that finds a device by its identifier.
     *
     * @return The function that finds a device by its identifier.
     */
    @Bean(FUNCTION_BEAN_NAME)
    public Function<Message<byte[]>, Mono<ReadDeviceResponse>> findByIdFunction() {
        LOGGER.debug("Creating Device Data Function Bean...");
        return new ReadFunction(this.deviceMapper, this.devicesService);
    }
}
