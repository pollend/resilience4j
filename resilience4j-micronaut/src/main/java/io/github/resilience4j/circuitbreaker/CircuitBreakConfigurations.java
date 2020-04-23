package io.github.resilience4j.circuitbreaker;


import io.github.resilience4j.common.circuitbreaker.configuration.CircuitBreakerConfigurationProperties;
import io.micronaut.context.annotation.EachProperty;
import io.micronaut.context.annotation.Parameter;

@EachProperty(value = "resilience4j.ratelimiter.configurations", primary = "default")
public class CircuitBreakConfigurations extends CircuitBreakerConfigurationProperties.InstanceProperties {

    private final String name;

    public CircuitBreakConfigurations(@Parameter  String name) {
        this.name = name;
    }


    public String getName() {
        return name;
    }
}
