package com.udacity.webcrawler;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.udacity.webcrawler.json.CrawlerConfiguration;
import com.udacity.webcrawler.profiler.ProfilerImpl;
import com.udacity.webcrawler.profiler.Profiler;

import java.time.Clock;
import java.time.Duration;
import java.util.List;
import java.util.regex.Pattern;

public final class WebCrawlerModule extends AbstractModule {

  private final CrawlerConfiguration config;

  public WebCrawlerModule(CrawlerConfiguration config) {
    this.config = config;
  }

  @Override
  protected void configure() {
    bind(Clock.class).toInstance(Clock.systemUTC());
    bind(Profiler.class).to(ProfilerImpl.class);

    int parallelism = config.getParallelism() == -1
        ? Runtime.getRuntime().availableProcessors()
        : config.getParallelism();

    bind(Duration.class).annotatedWith(Timeout.class).toInstance(config.getTimeout());
    bindConstant().annotatedWith(PopularWordCount.class).to(config.getPopularWordCount());
    bindConstant().annotatedWith(MaxDepth.class).to(config.getMaxDepth());
    bindConstant().annotatedWith(TargetParallelism.class).to(parallelism);
    bind(new TypeLiteral<List<Pattern>>() {})
        .annotatedWith(IgnoredUrls.class)
        .toInstance(config.getIgnoredUrls());
    bind(new TypeLiteral<List<Pattern>>() {})
        .annotatedWith(IgnoredWords.class)
        .toInstance(config.getIgnoredWords());

    bind(PageParserFactory.class).to(PageParserFactoryImpl.class);

    String override = config.getImplementationOverride();
    if (!override.isEmpty()) {
      try {
        @SuppressWarnings("unchecked")
        Class<? extends WebCrawler> implClass =
            (Class<? extends WebCrawler>) Class.forName(override);
        bind(WebCrawler.class).to(implClass);
      } catch (ClassNotFoundException e) {
        throw new RuntimeException("Could not find crawler class: " + override, e);
      }
    } else if (parallelism > 1) {
      bind(WebCrawler.class).to(ParallelWebCrawler.class);
    } else {
      bind(WebCrawler.class).to(SequentialWebCrawler.class);
    }
  }
}
