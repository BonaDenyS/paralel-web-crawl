package com.udacity.webcrawler;

import com.google.inject.Inject;
import com.udacity.webcrawler.json.CrawlResult;
import com.udacity.webcrawler.profiler.Profiled;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@com.google.inject.Singleton
public final class ParallelWebCrawler implements WebCrawler {

  private final Clock clock;
  private final Duration timeout;
  private final int popularWordCount;
  private final ForkJoinPool pool;
  private final int maxDepth;
  private final List<Pattern> ignoredUrls;
  private final List<Pattern> ignoredWords;
  private final PageParserFactory parserFactory;

  @Inject
  public ParallelWebCrawler(
      Clock clock,
      @Timeout Duration timeout,
      @PopularWordCount int popularWordCount,
      @TargetParallelism int threadCount,
      @MaxDepth int maxDepth,
      @IgnoredUrls List<Pattern> ignoredUrls,
      @IgnoredWords List<Pattern> ignoredWords,
      PageParserFactory parserFactory) {
    this.clock = clock;
    this.timeout = timeout;
    this.popularWordCount = popularWordCount;
    this.pool = new ForkJoinPool(Math.min(threadCount, getMaxParallelism()));
    this.maxDepth = maxDepth;
    this.ignoredUrls = ignoredUrls;
    this.ignoredWords = ignoredWords;
    this.parserFactory = parserFactory;
  }

  @Override
  @Profiled
  public CrawlResult crawl(List<String> urls) {
    Instant deadline = clock.instant().plus(timeout);
    ConcurrentMap<String, Integer> counts = new ConcurrentHashMap<>();
    Set<String> visitedUrls = ConcurrentHashMap.newKeySet();

    // Fork all start URLs as parallel subtasks inside the pool
    pool.invoke(new RecursiveAction() {
      @Override
      protected void compute() {
        List<CrawlInternalTask> tasks = urls.stream()
            .map(url -> new CrawlInternalTask(
                url, deadline, maxDepth, counts, visitedUrls,
                clock, ignoredUrls, ignoredWords, parserFactory))
            .collect(Collectors.toList());
        invokeAll(tasks);
      }
    });

    if (counts.isEmpty()) {
      return new CrawlResult.Builder()
          .setWordCounts(counts)
          .setUrlsVisited(visitedUrls.size())
          .build();
    }
    return new CrawlResult.Builder()
        .setWordCounts(WordCounts.sort(counts, popularWordCount))
        .setUrlsVisited(visitedUrls.size())
        .build();
  }

  @Override
  public int getMaxParallelism() {
    return Runtime.getRuntime().availableProcessors();
  }
}
