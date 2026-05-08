package com.udacity.webcrawler;

import com.udacity.webcrawler.json.CrawlResult;
import org.junit.Test;

import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class ParallelWebCrawlerTest {

  private static final List<Pattern> NO_IGNORED_URLS = List.of();
  private static final List<Pattern> NO_IGNORED_WORDS = List.of();

  /** A fake PageParserFactory that returns predetermined results. */
  static PageParserFactory fakeFactory(Map<String, PageParser.Result> pages) {
    return url -> () -> pages.getOrDefault(url, new PageParser.Result.Builder().build());
  }

  @Test
  public void testDoesNotVisitSameUrlTwice() {
    Map<String, PageParser.Result> pages = new HashMap<>();
    // page A links to B, B links back to A — cycle
    pages.put("https://a.com", new PageParser.Result.Builder()
        .setWordCounts(Map.of("alpha", 1))
        .setLinks(List.of("https://b.com"))
        .build());
    pages.put("https://b.com", new PageParser.Result.Builder()
        .setWordCounts(Map.of("beta", 1))
        .setLinks(List.of("https://a.com"))
        .build());

    ParallelWebCrawler crawler = new ParallelWebCrawler(
        Clock.systemUTC(),
        Duration.ofSeconds(10),
        10,
        2,
        5,
        NO_IGNORED_URLS,
        NO_IGNORED_WORDS,
        fakeFactory(pages));

    CrawlResult result = crawler.crawl(List.of("https://a.com"));
    assertEquals(2, result.getUrlsVisited());
    assertTrue(result.getWordCounts().containsKey("alpha"));
    assertTrue(result.getWordCounts().containsKey("beta"));
  }

  @Test
  public void testRespectsTimeout() {
    // Negative timeout puts the deadline 1 second in the past, so every task sees expired deadline
    Map<String, PageParser.Result> pages = new HashMap<>();
    pages.put("https://example.com", new PageParser.Result.Builder()
        .setWordCounts(Map.of("word", 1))
        .setLinks(List.of("https://other.com"))
        .build());

    ParallelWebCrawler crawler = new ParallelWebCrawler(
        Clock.systemUTC(),
        Duration.ofSeconds(-1),
        10,
        2,
        5,
        NO_IGNORED_URLS,
        NO_IGNORED_WORDS,
        fakeFactory(pages));

    long start = System.currentTimeMillis();
    CrawlResult result = crawler.crawl(List.of("https://example.com"));
    long elapsed = System.currentTimeMillis() - start;

    assertEquals(0, result.getUrlsVisited());
    assertTrue("Should complete quickly", elapsed < 3000);
  }

  @Test
  public void testRespectsIgnoredUrls() {
    Map<String, PageParser.Result> pages = new HashMap<>();
    pages.put("https://example.com", new PageParser.Result.Builder()
        .setWordCounts(Map.of("hello", 1))
        .setLinks(List.of("https://ignore.com/page"))
        .build());
    pages.put("https://ignore.com/page", new PageParser.Result.Builder()
        .setWordCounts(Map.of("ignored", 99))
        .build());

    ParallelWebCrawler crawler = new ParallelWebCrawler(
        Clock.systemUTC(),
        Duration.ofSeconds(10),
        10,
        2,
        5,
        List.of(Pattern.compile("https://ignore\\.com/.*")),
        NO_IGNORED_WORDS,
        fakeFactory(pages));

    CrawlResult result = crawler.crawl(List.of("https://example.com"));
    assertEquals(1, result.getUrlsVisited());
    assertFalse(result.getWordCounts().containsKey("ignored"));
  }

  @Test
  public void testRespectsMaxDepth() {
    Map<String, PageParser.Result> pages = new HashMap<>();
    pages.put("https://depth0.com", new PageParser.Result.Builder()
        .setWordCounts(Map.of("zero", 1))
        .setLinks(List.of("https://depth1.com"))
        .build());
    pages.put("https://depth1.com", new PageParser.Result.Builder()
        .setWordCounts(Map.of("one", 1))
        .setLinks(List.of("https://depth2.com"))
        .build());
    pages.put("https://depth2.com", new PageParser.Result.Builder()
        .setWordCounts(Map.of("two", 1))
        .build());

    // maxDepth=1: visit depth0 and depth1 only
    ParallelWebCrawler crawler = new ParallelWebCrawler(
        Clock.systemUTC(),
        Duration.ofSeconds(10),
        10,
        2,
        1,
        NO_IGNORED_URLS,
        NO_IGNORED_WORDS,
        fakeFactory(pages));

    CrawlResult result = crawler.crawl(List.of("https://depth0.com"));
    assertTrue(result.getWordCounts().containsKey("zero"));
    assertTrue(result.getWordCounts().containsKey("one"));
    assertFalse(result.getWordCounts().containsKey("two"));
  }
}
