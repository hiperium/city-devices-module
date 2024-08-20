package hiperium.city.devices.update.function.functions;

import hiperium.city.devices.update.function.dto.DeviceUpdateResponse;
import hiperium.city.devices.update.function.services.DevicesService;
import hiperium.city.devices.update.function.utils.FunctionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import reactor.core.publisher.Mono;

import java.util.function.Function;

/**
 * The UpdateFunction class represents a function that applies the device update request
 * and updates the device status.
 */
public class UpdateFunction implements Function<Message<byte[]>, Mono<DeviceUpdateResponse>> {

    private final DevicesService devicesService;

    /**
     * Constructor for the UpdateFunction class.
     *
     * @param devicesService the DevicesService instance used for updating device status
     */
    public UpdateFunction(DevicesService devicesService) {
        this.devicesService = devicesService;
    }

    /**
     * Applies the device update request and updates the device status.
     *
     * @param requestMessage the message containing the request payload as a byte array
     * @return a Mono emitting the DeviceUpdateResponse object representing the result of the operation
     */
    @Override
    public Mono<DeviceUpdateResponse> apply(Message<byte[]> requestMessage) {
        return Mono.fromCallable(() -> FunctionUtils.deserializeRequest(requestMessage))
            .doOnNext(FunctionUtils::validateRequest)
            .flatMap(this.devicesService::updateDeviceStatus)
            .then(this.createResponse())
            .onErrorResume(FunctionUtils::handleRuntimeException);
    }

    private Mono<DeviceUpdateResponse> createResponse() {
        return Mono.fromSupplier(() -> new DeviceUpdateResponse.Builder()
            .statusCode(HttpStatus.OK.value())
            .body("Device status updated successfully.")
            .build());
    }
}
