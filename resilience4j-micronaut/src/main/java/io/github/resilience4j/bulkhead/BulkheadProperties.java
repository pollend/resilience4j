package io.github.resilience4j.bulkhead;


import io.github.resilience4j.common.CommonProperties;
import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.core.util.Toggleable;

import java.util.Collections;
import java.util.List;

@ConfigurationProperties("resilience4j.bulkhead")
public class BulkheadProperties extends CommonProperties implements Toggleable {

    public static final String PATH_KEY = "resilience4j.bulkhead.paths";
    private List<String> paths = Collections.emptyList();

    private boolean enabled;

    public void setPaths(List<String> paths) {
        this.paths = paths;
    }

    public List<String> getPaths() {
        return paths;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
