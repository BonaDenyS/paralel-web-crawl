package com.udacity.webcrawler;

import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class WordCountsTest {

  @Test
  public void testSortByCountDescending() {
    Map<String, Integer> input = new HashMap<>();
    input.put("apple", 3);
    input.put("banana", 10);
    input.put("cherry", 7);

    Map<String, Integer> sorted = WordCounts.sort(input, 3);
    List<String> keys = List.copyOf(sorted.keySet());

    assertEquals("banana", keys.get(0));
    assertEquals("cherry", keys.get(1));
    assertEquals("apple", keys.get(2));
  }

  @Test
  public void testTieBrokenByLengthDescending() {
    Map<String, Integer> input = new HashMap<>();
    input.put("cat", 5);
    input.put("elephant", 5);
    input.put("dog", 5);

    Map<String, Integer> sorted = WordCounts.sort(input, 3);
    List<String> keys = List.copyOf(sorted.keySet());

    // "elephant" (8 chars) comes first, then "cat"/"dog" (3 chars each)
    assertEquals("elephant", keys.get(0));
  }

  @Test
  public void testTieBrokenAlphabeticallyAscending() {
    Map<String, Integer> input = new HashMap<>();
    input.put("cat", 5);
    input.put("dog", 5);
    input.put("ant", 5);

    Map<String, Integer> sorted = WordCounts.sort(input, 3);
    List<String> keys = List.copyOf(sorted.keySet());

    // same count (5), same length (3), so alphabetical: ant, cat, dog
    assertEquals("ant", keys.get(0));
    assertEquals("cat", keys.get(1));
    assertEquals("dog", keys.get(2));
  }

  @Test
  public void testLimitToPopularWordCount() {
    Map<String, Integer> input = new HashMap<>();
    input.put("a", 10);
    input.put("b", 9);
    input.put("c", 8);
    input.put("d", 7);
    input.put("e", 6);

    Map<String, Integer> sorted = WordCounts.sort(input, 3);
    assertEquals(3, sorted.size());
    assertTrue(sorted.containsKey("a"));
    assertTrue(sorted.containsKey("b"));
    assertTrue(sorted.containsKey("c"));
  }

  @Test
  public void testPopularWordCountLargerThanInput() {
    Map<String, Integer> input = new HashMap<>();
    input.put("hello", 2);
    input.put("world", 1);

    Map<String, Integer> sorted = WordCounts.sort(input, 10);
    assertEquals(2, sorted.size());
  }
}
