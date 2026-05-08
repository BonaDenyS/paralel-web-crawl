package com.udacity.webcrawler;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public final class WordCounts {

  private WordCounts() {}

  public static Map<String, Integer> sort(Map<String, Integer> wordCounts, int popularWordCount) {
    return wordCounts
        .entrySet()
        .stream()
        .sorted(new WordCountComparator())
        .limit(popularWordCount)
        .collect(Collectors.toMap(
            Map.Entry::getKey,
            Map.Entry::getValue,
            (e1, e2) -> e1,
            LinkedHashMap::new));
  }
}
