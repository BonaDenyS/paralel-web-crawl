package com.udacity.webcrawler;

import java.util.Comparator;
import java.util.Map;

final class WordCountComparator implements Comparator<Map.Entry<String, Integer>> {

  @Override
  public int compare(Map.Entry<String, Integer> a, Map.Entry<String, Integer> b) {
    if (!a.getValue().equals(b.getValue())) {
      return b.getValue() - a.getValue();
    }
    if (a.getKey().length() != b.getKey().length()) {
      return b.getKey().length() - a.getKey().length();
    }
    return a.getKey().compareTo(b.getKey());
  }
}
