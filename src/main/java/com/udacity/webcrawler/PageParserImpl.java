package com.udacity.webcrawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

final class PageParserImpl implements PageParser {

  private final String url;
  private final Duration timeout;

  PageParserImpl(String url, Duration timeout) {
    this.url = url;
    this.timeout = timeout;
  }

  @Override
  public Result parse() {
    try {
      Document doc = Jsoup.connect(url)
          .timeout((int) Math.min(timeout.toMillis(), Integer.MAX_VALUE))
          .get();

      Map<String, Integer> wordCounts = new HashMap<>();
      for (String word : doc.text().split("[^a-zA-Z]+")) {
        if (!word.isEmpty()) {
          wordCounts.merge(word.toLowerCase(Locale.ROOT), 1, Integer::sum);
        }
      }

      List<String> links = doc.select("a[href]")
          .stream()
          .map(a -> a.absUrl("href"))
          .filter(link -> link.startsWith("http://") || link.startsWith("https://"))
          .collect(Collectors.toList());

      return new Result.Builder()
          .setWordCounts(wordCounts)
          .setLinks(links)
          .build();

    } catch (IOException e) {
      return new Result.Builder().build();
    }
  }
}
