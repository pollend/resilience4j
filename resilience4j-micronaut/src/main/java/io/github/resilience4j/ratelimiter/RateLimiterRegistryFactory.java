package io.github.resilience4j.ratelimiter;

import io.github.resilience4j.common.CompositeCustomizer;
import io.github.resilience4j.common.ratelimiter.configuration.RateLimiterConfigCustomizer;
import io.github.resilience4j.common.ratelimiter.configuration.RateLimiterConfigurationProperties;
import io.micronaut.context.annotation.Factory;

import javax.inject.Singleton;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Factory
public class RateLimiterRegistryFactory {
    /**
     * The registry factory method.
     *
     * @param registryConfiguration The registry configuration
     * @param rateLimiterConfigurations All rate limiter configurations
     * @return The rate limiter registry
     */
    @Singleton
    RateLimiterRegistry registry(
        RateLimiterProperties registryConfiguration,
        List<RateLimiterConfigurations> rateLimiterConfigurations) {
        Map<String, RateLimiterConfig> configMap = new HashMap<>(rateLimiterConfigurations.size());

        RateLimiterConfigurationProperties configurationProperties = new RateLimiterConfigurationProperties();

        for (RateLimiterConfigurations config: rateLimiterConfigurations) {
            configurationProperties.getConfigs().put(config.getName(),config);
        }
        Map<String, RateLimiterConfig> configs = configurationProperties.getConfigs().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
            entry -> configurationProperties.createRateLimiterConfig(entry.getValue(),
                new CompositeCustomizer<>(Collections.emptyList()), entry.getKey())));

        RateLimiterRegistry rateLimiterRegistry = RateLimiterRegistry.of(configs);

//        io.vavr.collection.Map<String, String> tags = registryConfiguration.getTags().map(io.vavr.collection.HashMap::ofAll).orElse(io.vavr.collection.HashMap.empty());
//        return RateLimiterRegistry.of(configMap, tags);
        return null;
    }


}
