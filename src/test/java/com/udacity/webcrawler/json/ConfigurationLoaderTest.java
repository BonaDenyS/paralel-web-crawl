package com.udacity.webcrawler.json;

import org.junit.Test;

import java.io.StringReader;

import static org.junit.Assert.*;

public class ConfigurationLoaderTest {

  @Test
  public void testReadDefaults() throws Exception {
    CrawlerConfiguration config = ConfigurationLoader.read(new StringReader("{}"));
    assertNotNull(config);
    assertTrue(config.getStartPages().isEmpty());
    assertTrue(config.getIgnoredUrls().isEmpty());
    assertTrue(config.getIgnoredWords().isEmpty());
    assertEquals(-1, config.getParallelism());
    assertEquals("", config.getImplementationOverride());
    assertEquals(10, config.getMaxDepth());
    assertEquals(2, (int) config.getTimeout().getSeconds());
    assertEquals(10, config.getPopularWordCount());
    assertEquals("", config.getProfileOutputPath());
    assertEquals("", config.getResultPath());
  }

  @Test
  public void testReadStartPages() throws Exception {
    String json = "{\"startPages\": [\"https://example.com\", \"https://test.org\"]}";
    CrawlerConfiguration config = ConfigurationLoader.read(new StringReader(json));
    assertEquals(2, config.getStartPages().size());
    assertEquals("https://example.com", config.getStartPages().get(0));
    assertEquals("https://test.org", config.getStartPages().get(1));
  }

  @Test
  public void testReadFullConfig() throws Exception {
    String json = "{"
        + "\"startPages\": [\"https://example.com\"],"
        + "\"ignoredUrls\": [\".*#.*\"],"
        + "\"ignoredWords\": [\"^.$\"],"
        + "\"parallelism\": 4,"
        + "\"implementationOverride\": \"\","
        + "\"maxDepth\": 3,"
        + "\"timeoutSeconds\": 5,"
        + "\"popularWordCount\": 8,"
        + "\"profileOutputPath\": \"profile.txt\","
        + "\"resultPath\": \"result.json\""
        + "}";
    CrawlerConfiguration config = ConfigurationLoader.read(new StringReader(json));
    assertEquals(1, config.getStartPages().size());
    assertEquals("https://example.com", config.getStartPages().get(0));
    assertEquals(1, config.getIgnoredUrls().size());
    assertEquals(1, config.getIgnoredWords().size());
    assertEquals(4, config.getParallelism());
    assertEquals(3, config.getMaxDepth());
    assertEquals(5, (int) config.getTimeout().getSeconds());
    assertEquals(8, config.getPopularWordCount());
    assertEquals("profile.txt", config.getProfileOutputPath());
    assertEquals("result.json", config.getResultPath());
  }

  @Test
  public void testIgnoredUrlsArePatterns() throws Exception {
    String json = "{\"ignoredUrls\": [\".*#.*\", \"https://.*\\.pdf\"]}";
    CrawlerConfiguration config = ConfigurationLoader.read(new StringReader(json));
    assertEquals(2, config.getIgnoredUrls().size());
    assertTrue(config.getIgnoredUrls().get(0).matcher("https://example.com#section").matches());
    assertFalse(config.getIgnoredUrls().get(0).matcher("https://example.com").matches());
  }
}
