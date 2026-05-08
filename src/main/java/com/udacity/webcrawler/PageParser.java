package com.udacity.webcrawler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface PageParser {

  Result parse();

  final class Result {
    private final Map<String, Integer> wordCounts;
    private final List<String> links;

    private Result(Builder builder) {
      this.wordCounts = Collections.unmodifiableMap(builder.wordCounts);
      this.links = Collections.unmodifiableList(builder.links);
    }

    public Map<String, Integer> getWordCounts() { return wordCounts; }
    public List<String> getLinks() { return links; }

    public static final class Builder {
      private Map<String, Integer> wordCounts = new HashMap<>();
      private List<String> links = new ArrayList<>();

      public Builder setWordCounts(Map<String, Integer> wordCounts) {
        this.wordCounts = wordCounts;
        return this;
      }

      public Builder setLinks(List<String> links) {
        this.links = links;
        return this;
      }

      public Result build() {
        return new Result(this);
      }
    }
  }
}
