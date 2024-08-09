package hiperium.city.devices.update.function.filters;

import com.fasterxml.jackson.databind.ObjectMapper;
import hiperium.cities.commons.loggers.HiperiumLogger;
import hiperium.city.devices.update.function.dto.EventBridgeEvent;
import org.slf4j.MDC;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.lang.NonNull;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * MdcFilter is responsible for setting and removing contextual information in the Mapped Diagnostic Context (MDC)
 * during the processing of web requests.
 * <p>
 * The MDC is a thread-local map that provides a way to store diagnostic context information, such as request-specific
 * data, for the duration of the request processing. It is commonly used in multithreaded applications to associate
 * contextual information with log messages or other monitoring activities.
 */
public class MdcFilter implements WebFilter {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final HiperiumLogger LOGGER = new HiperiumLogger(MdcFilter.class);

    @NonNull
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, @NonNull WebFilterChain webFilterChain) {
        LOGGER.debug("MDC Lambda Headers Filter", exchange.getRequest().getHeaders());
        ServerHttpRequest request = exchange.getRequest();
        String eventData = request.getHeaders().getFirst("Lambda-Payload");

        if (eventData != null) {
            try {
                EventBridgeEvent event = OBJECT_MAPPER.readValue(eventData, EventBridgeEvent.class);
                String eventSource = event.source();
                String eventId = event.id();
                MDC.put("eventSource", eventSource);
                MDC.put("eventId", eventId);
            } catch (Exception e) {
                LOGGER.error("Failed to parse EventBridge event data", e.getMessage());
            }
        }
        return webFilterChain
            .filter(exchange)
            .doOnEach(signal -> MDC.clear());
    }
}
