package io.github.resilience4j.bucket;

import io.micronaut.http.HttpRequest;

public interface BucketNameResolver {
    String resolver(HttpRequest<?> request);
}
