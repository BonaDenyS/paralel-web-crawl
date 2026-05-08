package com.udacity.webcrawler.json;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public final class CrawlResultWriter {

  private final CrawlResult result;

  public CrawlResultWriter(CrawlResult result) {
    this.result = Objects.requireNonNull(result);
  }

  public void write(Path path) throws IOException {
    try (Writer writer = Files.newBufferedWriter(path)) {
      write(writer);
    }
  }

  public void write(Writer writer) throws IOException {
    Objects.requireNonNull(writer);
    ObjectMapper mapper = new ObjectMapper();
    mapper.writerWithDefaultPrettyPrinter().writeValue(writer, result);
  }
}
