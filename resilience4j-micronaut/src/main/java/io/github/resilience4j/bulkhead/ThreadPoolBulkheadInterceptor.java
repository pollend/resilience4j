package io.github.resilience4j.bulkhead;

import io.github.resilience4j.BaseInterceptor;
import io.github.resilience4j.ResilienceInterceptPhase;
import io.github.resilience4j.fallback.UnhandledFallbackException;
import io.micronaut.aop.MethodInterceptor;
import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.context.BeanContext;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.core.type.ReturnType;
import io.micronaut.inject.ExecutableMethod;
import io.micronaut.inject.MethodExecutionHandle;
import io.micronaut.retry.intercept.RecoveryInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

/**
 * A {@link MethodInterceptor} that intercepts all method calls which are annotated with a {@link io.github.resilience4j.bulkhead.annotation.Bulkhead}
 * annotation.
 **/
@Singleton
@Requires(beans = ThreadPoolBulkheadRegistry.class)
public class ThreadPoolBulkheadInterceptor extends BaseInterceptor implements MethodInterceptor<Object,Object> {
    Logger LOG = LoggerFactory.getLogger(ThreadPoolBulkheadInterceptor.class);

    private final ThreadPoolBulkheadRegistry bulkheadRegistry;
    private final BeanContext beanContext;

    /**
     * @param beanContext      The bean context to allow for DI of class annotated with {@link javax.inject.Inject}.
     * @param bulkheadRegistry bulkhead registry used to retrieve {@link Bulkhead} by name
     */
    public ThreadPoolBulkheadInterceptor(BeanContext beanContext,
                                         ThreadPoolBulkheadRegistry bulkheadRegistry) {
        this.bulkheadRegistry = bulkheadRegistry;
        this.beanContext = beanContext;
    }

    @Override
    public int getOrder() {
        return ResilienceInterceptPhase.BULKHEAD.getPosition();
    }

    /**
     * Finds a fallback method for the given context.
     *
     * @param context The context
     * @return The fallback method if it is present
     */
    @Override
    public Optional<? extends MethodExecutionHandle<?, Object>> findFallbackMethod(MethodInvocationContext<Object, Object> context) {
        ExecutableMethod executableMethod = context.getExecutableMethod();
        final String fallbackMethod = executableMethod.stringValue(io.github.resilience4j.bulkhead.annotation.Bulkhead.class, "fallbackMethod").orElse("");
        Class<?> declaringType = context.getDeclaringType();
        return beanContext.findExecutionHandle(declaringType, fallbackMethod, context.getArgumentTypes());
    }

    @Override
    public Object intercept(MethodInvocationContext<Object, Object> context) {

        Optional<AnnotationValue<io.github.resilience4j.bulkhead.annotation.Bulkhead>> opt = context.findAnnotation(io.github.resilience4j.bulkhead.annotation.Bulkhead.class);
        if (!opt.isPresent()) {
            return context.proceed();
        }
        final io.github.resilience4j.bulkhead.annotation.Bulkhead.Type type = opt.get().enumValue("type", io.github.resilience4j.bulkhead.annotation.Bulkhead.Type.class).orElse(io.github.resilience4j.bulkhead.annotation.Bulkhead.Type.SEMAPHORE);
        if (type != io.github.resilience4j.bulkhead.annotation.Bulkhead.Type.THREADPOOL) {
            return context.proceed();
        }
        final String name = opt.get().stringValue().orElse("default");
        ThreadPoolBulkhead bulkhead = this.bulkheadRegistry.bulkhead(name);
        ReturnType<Object> rt = context.getReturnType();
        Class<Object> returnType = rt.getType();
        if (CompletionStage.class.isAssignableFrom(returnType)) {
            Object result = context.proceed();
            if (result == null) {
                return result;
            }
            return this.fallbackCompletable(bulkhead.executeSupplier(() -> {
                try {
                    return ((CompletableFuture<?>) result).get();
                } catch (InterruptedException e) {
                    throw new CompletionException(e.getCause());
                } catch (ExecutionException e) {
                    throw new CompletionException(e);
                }
            }),context);
        } else if (Publishers.isConvertibleToPublisher(returnType)) {
            throw new IllegalStateException(
                "ThreadPool bulkhead is only applicable for completable futures ");
        }

        CompletableFuture<Object> newFuture = new CompletableFuture<>();
        bulkhead.executeSupplier(context::proceed).whenComplete((o, throwable) -> {
            if (throwable == null) {
                newFuture.complete(o);
            } else {
                Optional<? extends MethodExecutionHandle<?, Object>> fallbackMethod = findFallbackMethod(context);
                if (fallbackMethod.isPresent()) {
                    MethodExecutionHandle<?, Object> fallbackHandle = fallbackMethod.get();
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Type [{}] resolved fallback: {}", context.getTarget().getClass(), fallbackHandle);
                    }
                    try {
                        Object result = fallbackHandle.invoke(context.getParameterValues());
                        newFuture.complete(result);
                    } catch (Exception e) {
                        if (LOG.isErrorEnabled()) {
                            LOG.error("Error invoking Fallback [" + fallbackHandle + "]: " + e.getMessage(), e);
                        }
                        newFuture.completeExceptionally(throwable);
                    }
                } else {
                    newFuture.completeExceptionally(throwable);
                }
            }
        });
        try {
            return newFuture.get();
        } catch (RuntimeException e) {
            throw e;
        } catch (Throwable throwable) {
            throw new UnhandledFallbackException("Error invoking fallback for type [" + context.getTarget().getClass().getName() + "]: " + throwable.getMessage(), throwable);
        }

    }
}
