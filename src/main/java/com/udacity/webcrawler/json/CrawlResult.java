package com.udacity.webcrawler.json;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public final class CrawlResult {

  private final Map<String, Integer> wordCounts;
  private final int urlsVisited;

  private CrawlResult(Builder builder) {
    this.wordCounts = Collections.unmodifiableMap(Objects.requireNonNull(builder.wordCounts));
    this.urlsVisited = builder.urlsVisited;
  }

  @JsonProperty("wordCounts")
  public Map<String, Integer> getWordCounts() {
    return wordCounts;
  }

  @JsonProperty("urlsVisited")
  public int getUrlsVisited() {
    return urlsVisited;
  }

  public static final class Builder {
    private Map<String, Integer> wordCounts = Collections.emptyMap();
    private int urlsVisited = 0;

    public Builder setWordCounts(Map<String, Integer> wordCounts) {
      this.wordCounts = Objects.requireNonNull(wordCounts);
      return this;
    }

    public Builder setUrlsVisited(int urlsVisited) {
      this.urlsVisited = urlsVisited;
      return this;
    }

    public CrawlResult build() {
      return new CrawlResult(this);
    }
  }
}
