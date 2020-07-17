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
package io.github.resilience4j.bulkhead;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.micronaut.aop.Around;
import io.micronaut.context.annotation.Type;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.AnnotationValueBuilder;
import io.micronaut.inject.annotation.TypedAnnotationMapper;
import io.micronaut.inject.visitor.VisitorContext;

import java.util.Arrays;
import java.util.List;

/**
 * An annotation mapper that maps {@link Bulkhead}.
 */
public class BulkheadAnnotationMapper implements TypedAnnotationMapper<Bulkhead> {

    @Override
    public Class<Bulkhead> annotationType() {
        return Bulkhead.class;
    }

    @Override
    public List<AnnotationValue<?>> map(AnnotationValue<Bulkhead> annotation, VisitorContext visitorContext) {
        final AnnotationValueBuilder<Bulkhead> builder = AnnotationValue.builder(Bulkhead.class).value(Bulkhead.Type.SEMAPHORE);
        annotation.enumValue("type", Bulkhead.Type.class).ifPresent(c ->
            builder.member("type", c)
        );
        annotation.stringValue("fallbackMethod").ifPresent(s -> builder.member("fallbackMethod", s));
        annotation.stringValue("name").ifPresent(c -> builder.member("name", c));

        final AnnotationValueBuilder<Type> typeBuilder = AnnotationValue.builder(Type.class)
            .member("value", BulkheadInterceptor.class, ThreadPoolBulkheadInterceptor.class);
        final AnnotationValueBuilder<Around> aroundBuilder = AnnotationValue.builder(Around.class);

        return Arrays.asList(builder.build(),
            typeBuilder.build(),
            aroundBuilder.build());
    }
}
