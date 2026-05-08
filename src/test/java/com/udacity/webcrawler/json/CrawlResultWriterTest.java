package com.udacity.webcrawler.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class CrawlResultWriterTest {

  @Test
  public void testWriteToWriter() throws Exception {
    Map<String, Integer> wordCounts = new LinkedHashMap<>();
    wordCounts.put("java", 54);
    wordCounts.put("web", 23);
    wordCounts.put("crawl", 14);

    CrawlResult result = new CrawlResult.Builder()
        .setWordCounts(wordCounts)
        .setUrlsVisited(12)
        .build();

    StringWriter writer = new StringWriter();
    new CrawlResultWriter(result).write(writer);

    String json = writer.toString();
    assertFalse(json.isEmpty());

    ObjectMapper mapper = new ObjectMapper();
    @SuppressWarnings("unchecked")
    Map<String, Object> parsed = mapper.readValue(json, Map.class);

    assertTrue(parsed.containsKey("wordCounts"));
    assertTrue(parsed.containsKey("urlsVisited"));
    assertEquals(12, parsed.get("urlsVisited"));

    @SuppressWarnings("unchecked")
    Map<String, Integer> parsedCounts = (Map<String, Integer>) parsed.get("wordCounts");
    assertEquals(54, (int) parsedCounts.get("java"));
    assertEquals(23, (int) parsedCounts.get("web"));
    assertEquals(14, (int) parsedCounts.get("crawl"));
  }

  @Test
  public void testWriteEmptyResult() throws Exception {
    CrawlResult result = new CrawlResult.Builder()
        .setWordCounts(Map.of())
        .setUrlsVisited(0)
        .build();

    StringWriter writer = new StringWriter();
    new CrawlResultWriter(result).write(writer);

    String json = writer.toString();
    ObjectMapper mapper = new ObjectMapper();
    @SuppressWarnings("unchecked")
    Map<String, Object> parsed = mapper.readValue(json, Map.class);

    assertEquals(0, parsed.get("urlsVisited"));
  }
}
