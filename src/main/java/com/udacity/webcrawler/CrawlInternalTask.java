package com.udacity.webcrawler;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.RecursiveAction;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class CrawlInternalTask extends RecursiveAction {

  private final String url;
  private final Instant deadline;
  private final int maxDepth;
  private final ConcurrentMap<String, Integer> counts;
  private final Set<String> visitedUrls;
  private final Clock clock;
  private final List<Pattern> ignoredUrls;
  private final List<Pattern> ignoredWords;
  private final PageParserFactory parserFactory;

  public CrawlInternalTask(
      String url,
      Instant deadline,
      int maxDepth,
      ConcurrentMap<String, Integer> counts,
      Set<String> visitedUrls,
      Clock clock,
      List<Pattern> ignoredUrls,
      List<Pattern> ignoredWords,
      PageParserFactory parserFactory) {
    this.url = url;
    this.deadline = deadline;
    this.maxDepth = maxDepth;
    this.counts = counts;
    this.visitedUrls = visitedUrls;
    this.clock = clock;
    this.ignoredUrls = ignoredUrls;
    this.ignoredWords = ignoredWords;
    this.parserFactory = parserFactory;
  }

  @Override
  protected void compute() {
    if (clock.instant().isAfter(deadline)) {
      return;
    }
    if (ignoredUrls.stream().anyMatch(p -> p.matcher(url).matches())) {
      return;
    }
    // ConcurrentHashMap.newKeySet() — add() is lock-free and atomic
    if (!visitedUrls.add(url)) {
      return;
    }

    PageParser.Result result = parserFactory.get(url).parse();

    for (Map.Entry<String, Integer> e : result.getWordCounts().entrySet()) {
      if (ignoredWords.stream().noneMatch(p -> p.matcher(e.getKey()).matches())) {
        counts.merge(e.getKey(), e.getValue(), Integer::sum);
      }
    }

    if (maxDepth > 0) {
      List<CrawlInternalTask> subtasks = result.getLinks()
          .stream()
          .map(link -> new CrawlInternalTask(
              link, deadline, maxDepth - 1, counts, visitedUrls,
              clock, ignoredUrls, ignoredWords, parserFactory))
          .collect(Collectors.toList());
      invokeAll(subtasks);
    }
  }
}
