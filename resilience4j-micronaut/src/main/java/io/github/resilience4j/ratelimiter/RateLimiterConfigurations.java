package io.github.resilience4j.ratelimiter;

import io.github.resilience4j.common.ratelimiter.configuration.RateLimiterConfigurationProperties;
import io.micronaut.context.annotation.EachProperty;
import io.micronaut.context.annotation.Parameter;

//@EachProperty(value = "resilience4j.ratelimiter.configurations", primary = "default")
public class RateLimiterConfigurations extends RateLimiterConfigurationProperties.InstanceProperties {

    private final String name;
    /**
     * @param name The name of the configuration
     */
    public RateLimiterConfigurations(@Parameter String name) {
        this.name = name;
    }


    public String getName() {
        return name;
    }
}
