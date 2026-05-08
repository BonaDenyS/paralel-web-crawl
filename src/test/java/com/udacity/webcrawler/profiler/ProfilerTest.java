package com.udacity.webcrawler.profiler;

import org.junit.Before;
import org.junit.Test;

import java.io.StringWriter;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.*;

public class ProfilerTest {

  interface Greeter {
    @Profiled
    String greet(String name);

    String noProfile(String name);
  }

  interface ThrowingInterface {
    @Profiled
    void doWork() throws IllegalArgumentException;
  }

  interface NoProfiledMethods {
    String onlyUnprofiled();
  }

  private Profiler profiler;

  @Before
  public void setUp() {
    profiler = new ProfilerImpl(Clock.fixed(Instant.now(), ZoneId.systemDefault()));
  }

  @Test
  public void testProfiledMethodIsRecordedAndReturnsCorrectly() throws Exception {
    Greeter delegate = new Greeter() {
      @Override public String greet(String name) { return "Hello, " + name; }
      @Override public String noProfile(String name) { return "Untracked " + name; }
    };

    Greeter wrapped = profiler.wrap(Greeter.class, delegate);
    assertEquals("Hello, World", wrapped.greet("World"));

    StringWriter writer = new StringWriter();
    profiler.writeData(writer);
    String output = writer.toString();
    assertTrue("Profile data should mention greet", output.contains("greet"));
  }

  @Test
  public void testUnprofiledMethodNotRecorded() throws Exception {
    Greeter delegate = new Greeter() {
      @Override public String greet(String name) { return "Hi"; }
      @Override public String noProfile(String name) { return "Untracked"; }
    };

    Greeter wrapped = profiler.wrap(Greeter.class, delegate);
    wrapped.noProfile("test");

    StringWriter writer = new StringWriter();
    profiler.writeData(writer);
    assertFalse("noProfile should not be recorded", writer.toString().contains("noProfile"));
  }

  @Test
  public void testExceptionIsPropagated() {
    ThrowingInterface delegate = () -> {
      throw new IllegalArgumentException("boom");
    };

    ThrowingInterface wrapped = profiler.wrap(ThrowingInterface.class, delegate);
    try {
      wrapped.doWork();
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      assertEquals("boom", e.getMessage());
    }
  }

  @Test
  public void testExceptionIsStillRecorded() throws Exception {
    ThrowingInterface delegate = () -> {
      throw new IllegalArgumentException("boom");
    };

    ThrowingInterface wrapped = profiler.wrap(ThrowingInterface.class, delegate);
    try {
      wrapped.doWork();
    } catch (IllegalArgumentException ignored) {}

    StringWriter writer = new StringWriter();
    profiler.writeData(writer);
    assertTrue("Timing should be recorded even after exception",
        writer.toString().contains("doWork"));
  }

  @Test
  public void testEqualsNotIntercepted() {
    Greeter delegate = new Greeter() {
      @Override public String greet(String name) { return "Hi"; }
      @Override public String noProfile(String name) { return "no"; }
    };

    Greeter wrapped = profiler.wrap(Greeter.class, delegate);
    // equals() should bypass profiling and not throw; delegate != proxy so both return false
    assertFalse(wrapped.equals("something else"));
    assertFalse(wrapped.equals(null));
    // delegate.equals(delegate) should be true
    assertTrue(delegate.equals(delegate));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrapRequiresProfiledMethod() {
    NoProfiledMethods delegate = () -> "value";
    profiler.wrap(NoProfiledMethods.class, delegate);
  }
}
