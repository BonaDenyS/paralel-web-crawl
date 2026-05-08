package com.udacity.webcrawler.profiler;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class ProfilingState {

  private final List<String> entries = Collections.synchronizedList(new ArrayList<>());

  void record(Class<?> callingClass, Method method, Duration elapsed) {
    entries.add(callingClass.getName() + "#" + method.getName() + ": " + elapsed);
  }

  void write(Writer writer) throws IOException {
    for (String entry : new ArrayList<>(entries)) {
      writer.write(entry);
      writer.write(System.lineSeparator());
    }
  }
}
