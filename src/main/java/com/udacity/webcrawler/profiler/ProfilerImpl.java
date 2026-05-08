package com.udacity.webcrawler.profiler;

import com.google.inject.Inject;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Objects;

@com.google.inject.Singleton
public final class ProfilerImpl implements Profiler {

  private final Clock clock;
  private final ProfilingState state = new ProfilingState();

  @Inject
  public ProfilerImpl(Clock clock) {
    this.clock = Objects.requireNonNull(clock);
  }

  @Override
  public <T> T wrap(Class<T> klass, T delegate) {
    Objects.requireNonNull(klass);
    Objects.requireNonNull(delegate);

    boolean hasProfiled = Arrays.stream(klass.getMethods())
        .anyMatch(m -> m.isAnnotationPresent(Profiled.class));
    if (!hasProfiled) {
      throw new IllegalArgumentException(
          klass.getName() + " has no @Profiled methods");
    }

    @SuppressWarnings("unchecked")
    T proxy = (T) Proxy.newProxyInstance(
        ProfilerImpl.class.getClassLoader(),
        new Class<?>[]{klass},
        new ProfilingMethodInterceptor(clock, delegate, state));
    return proxy;
  }

  @Override
  public void writeData(Path path) throws IOException {
    try (Writer writer = Files.newBufferedWriter(
        path, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
      writeData(writer);
    }
  }

  @Override
  public void writeData(Writer writer) throws IOException {
    writer.write("Run at " + LocalDateTime.ofInstant(clock.instant(), ZoneId.systemDefault()));
    writer.write(System.lineSeparator());
    state.write(writer);
    writer.write(System.lineSeparator());
  }
}
