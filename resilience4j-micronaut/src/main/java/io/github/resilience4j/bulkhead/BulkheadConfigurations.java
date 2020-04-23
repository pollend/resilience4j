package io.github.resilience4j.bulkhead;

import io.github.resilience4j.common.bulkhead.configuration.BulkheadConfigurationProperties;
import io.micronaut.context.annotation.Parameter;

public class BulkheadConfigurations extends BulkheadConfigurationProperties.InstanceProperties {
    private final String name;
    /**
     * @param name The name of the configuration
     */
    public BulkheadConfigurations(@Parameter String name) {
        this.name = name;
    }


    public String getName() {
        return name;
    }
}
