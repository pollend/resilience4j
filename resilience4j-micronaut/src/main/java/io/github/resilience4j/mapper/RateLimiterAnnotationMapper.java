/*
 * Copyright 2019 Michael Pollind
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.resilience4j.mapper;

import io.github.resilience4j.ratelimiter.RateLimiterInterceptor;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.micronaut.aop.Around;
import io.micronaut.context.annotation.Type;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.AnnotationValueBuilder;
import io.micronaut.core.annotation.Internal;
import io.micronaut.inject.annotation.TypedAnnotationMapper;
import io.micronaut.inject.visitor.VisitorContext;

import java.util.Arrays;
import java.util.List;

/**
 * An annotation mapper that maps {@link RateLimiter}.
 */
public final class RateLimiterAnnotationMapper implements TypedAnnotationMapper<RateLimiter> {

    @Override
    public Class<RateLimiter> annotationType() {
        return RateLimiter.class;
    }

    @Override
    public List<AnnotationValue<?>> map(AnnotationValue<RateLimiter> annotation, VisitorContext visitorContext) {
        final AnnotationValueBuilder<RateLimiter> builder = AnnotationValue.builder(RateLimiter.class);
        annotation.stringValue("name").ifPresent(c -> builder.member("name", c));
        annotation.stringValue("fallbackMethod").ifPresent(c -> builder.member("fallbackMethod", c));

        final AnnotationValueBuilder<Type> typeBuilder = AnnotationValue.builder(Type.class).member("value", RateLimiterInterceptor.class);
        final AnnotationValueBuilder<Around> aroundBuilder = AnnotationValue.builder(Around.class);
        return Arrays.asList(builder.build(), typeBuilder.build(), aroundBuilder.build());
    }
}
