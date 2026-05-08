package com.udacity.webcrawler.profiler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

final class ProfilingMethodInterceptor implements InvocationHandler {

  private final Clock clock;
  private final Object delegate;
  private final ProfilingState state;

  ProfilingMethodInterceptor(Clock clock, Object delegate, ProfilingState state) {
    this.clock = Objects.requireNonNull(clock);
    this.delegate = Objects.requireNonNull(delegate);
    this.state = Objects.requireNonNull(state);
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    // Bypass profiling for Object#equals to avoid infinite proxy loops
    if (method.getDeclaringClass().equals(Object.class) && method.getName().equals("equals")) {
      return delegate.equals(args[0]);
    }

    boolean profiled = method.isAnnotationPresent(Profiled.class);
    Instant start = profiled ? clock.instant() : null;

    try {
      return method.invoke(delegate, args);
    } catch (InvocationTargetException e) {
      throw e.getCause();
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    } finally {
      if (profiled) {
        state.record(delegate.getClass(), method, Duration.between(start, clock.instant()));
      }
    }
  }
}
