package hiperium.city.devices.read.function.functions;

import hiperium.city.devices.read.function.dto.DeviceReadResponse;
import hiperium.city.devices.read.function.entities.Device;
import hiperium.city.devices.read.function.mappers.DeviceMapper;
import hiperium.city.devices.read.function.services.DevicesService;
import hiperium.city.devices.read.function.utils.FunctionUtils;
import org.springframework.messaging.Message;
import reactor.core.publisher.Mono;

import java.util.function.Function;

/**
 * Represents a function that finds a device by its identifier.
 */
public class ReadFunction implements Function<Message<byte[]>, Mono<DeviceReadResponse>> {

    private final DeviceMapper deviceMapper;
    private final DevicesService devicesService;

    /**
     * Represents a function that retrieves device data by its identifier.
     *
     * @param deviceMapper The DeviceMapper used for mapping device data between different representations.
     * @param devicesService The DevicesService used for working with devices.
     */
    public ReadFunction(DeviceMapper deviceMapper, DevicesService devicesService) {
        this.deviceMapper = deviceMapper;
        this.devicesService = devicesService;
    }

    /**
     * Applies the ReadFunction to the given request Message and performs a series of operations on it.
     *
     * @param requestMessage the request Message to apply the function to
     * @return a Mono that emits the resulting DeviceReadResponse
     */
    @Override
    public Mono<DeviceReadResponse> apply(Message<byte[]> requestMessage) {
        return Mono.fromCallable(() -> FunctionUtils.deserializeRequest(requestMessage))
            .doOnNext(FunctionUtils::validateRequest)
            .flatMap(this.devicesService::findById)
            .flatMap(this::mapResponse)
            .onErrorResume(FunctionUtils::handleRuntimeException);
    }

    private Mono<DeviceReadResponse> mapResponse(Device device) {
        return Mono.fromSupplier(() -> this.deviceMapper.mapToDeviceResponse(device));
    }
}
