package hiperium.city.devices.update.function.configurations;

import hiperium.cities.commons.loggers.HiperiumLogger;
import hiperium.city.devices.update.function.filters.MdcFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for Filters.
 * <p>
 * This class provides configuration for the MdcFilter, which is responsible for setting and removing
 * contextual information in the Mapped Diagnostic Context (MDC) during the processing of web requests.
 * The MDC is used to store diagnostic context information, such as request-specific data, for the duration
 * of the request processing.
 */
@Configuration
public class FiltersConfig {

    private static final HiperiumLogger LOGGER = new HiperiumLogger(FiltersConfig.class);

    /**
     * MDC Web Filter configuration method.
     * <p>
     * This method creates and configures an instance of the MdcFilter class, which implements the WebFilter interface.
     * The MdcFilter is responsible for performing operations related to the Mapped Diagnostic Context (MDC) during
     * the processing of web requests.
     * <p>
     * The MDC is a thread-local map that provides a way to store diagnostic context information, such as request-specific
     * data, for the duration of the request processing. It is commonly used in multithreaded applications to associate
     * contextual information with log messages or other monitoring activities.
     * <p>
     * The MdcFilter sets the "info" MDC key to a value obtained from the "X-Info-Header" request header, and removes it
     * after the request is processed.
     *
     * @return The configured MdcFilter instance.
     */
    @Bean
    public MdcFilter mdcFilter() {
        LOGGER.info("Creating MDC Filter Bean...");
        return new MdcFilter();
    }
}
