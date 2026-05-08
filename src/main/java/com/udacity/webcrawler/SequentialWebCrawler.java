package com.udacity.webcrawler;

import com.google.inject.Inject;
import com.udacity.webcrawler.json.CrawlResult;
import com.udacity.webcrawler.profiler.Profiled;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@com.google.inject.Singleton
final class SequentialWebCrawler implements WebCrawler {

  private final Clock clock;
  private final Duration timeout;
  private final int popularWordCount;
  private final int maxDepth;
  private final List<Pattern> ignoredUrls;
  private final List<Pattern> ignoredWords;
  private final PageParserFactory parserFactory;

  @Inject
  SequentialWebCrawler(
      Clock clock,
      @Timeout Duration timeout,
      @PopularWordCount int popularWordCount,
      @MaxDepth int maxDepth,
      @IgnoredUrls List<Pattern> ignoredUrls,
      @IgnoredWords List<Pattern> ignoredWords,
      PageParserFactory parserFactory) {
    this.clock = clock;
    this.timeout = timeout;
    this.popularWordCount = popularWordCount;
    this.maxDepth = maxDepth;
    this.ignoredUrls = ignoredUrls;
    this.ignoredWords = ignoredWords;
    this.parserFactory = parserFactory;
  }

  @Override
  @Profiled
  public CrawlResult crawl(List<String> urls) {
    Instant deadline = clock.instant().plus(timeout);
    Map<String, Integer> counts = new HashMap<>();
    Set<String> visitedUrls = new HashSet<>();
    for (String url : urls) {
      crawlInternal(url, deadline, maxDepth, counts, visitedUrls);
    }
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

  private void crawlInternal(
      String url,
      Instant deadline,
      int depth,
      Map<String, Integer> counts,
      Set<String> visitedUrls) {

    if (clock.instant().isAfter(deadline)) {
      return;
    }
    if (ignoredUrls.stream().anyMatch(p -> p.matcher(url).matches())) {
      return;
    }
    if (visitedUrls.contains(url)) {
      return;
    }
    visitedUrls.add(url);

    PageParser.Result result = parserFactory.get(url).parse();
    for (Map.Entry<String, Integer> e : result.getWordCounts().entrySet()) {
      if (ignoredWords.stream().noneMatch(p -> p.matcher(e.getKey()).matches())) {
        counts.merge(e.getKey(), e.getValue(), Integer::sum);
      }
    }

    if (depth > 0) {
      for (String link : result.getLinks()) {
        crawlInternal(link, deadline, depth - 1, counts, visitedUrls);
      }
    }
  }

  @Override
  public int getMaxParallelism() {
    return 1;
  }
}
