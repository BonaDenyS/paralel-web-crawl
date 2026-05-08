package com.udacity.webcrawler.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@JsonDeserialize(builder = CrawlerConfiguration.Builder.class)
public final class CrawlerConfiguration {

  private final List<String> startPages;
  private final List<Pattern> ignoredUrls;
  private final List<Pattern> ignoredWords;
  private final int parallelism;
  private final String implementationOverride;
  private final int maxDepth;
  private final Duration timeout;
  private final int popularWordCount;
  private final String profileOutputPath;
  private final String resultPath;

  private CrawlerConfiguration(Builder builder) {
    this.startPages = Collections.unmodifiableList(new ArrayList<>(builder.startPages));
    this.ignoredUrls = builder.ignoredUrls.stream()
        .map(Pattern::compile)
        .collect(Collectors.toUnmodifiableList());
    this.ignoredWords = builder.ignoredWords.stream()
        .map(Pattern::compile)
        .collect(Collectors.toUnmodifiableList());
    this.parallelism = builder.parallelism;
    this.implementationOverride = builder.implementationOverride;
    this.maxDepth = builder.maxDepth;
    this.timeout = Duration.ofSeconds(builder.timeoutSeconds);
    this.popularWordCount = builder.popularWordCount;
    this.profileOutputPath = builder.profileOutputPath;
    this.resultPath = builder.resultPath;
  }

  public List<String> getStartPages() { return startPages; }
  public List<Pattern> getIgnoredUrls() { return ignoredUrls; }
  public List<Pattern> getIgnoredWords() { return ignoredWords; }
  public int getParallelism() { return parallelism; }
  public String getImplementationOverride() { return implementationOverride; }
  public int getMaxDepth() { return maxDepth; }
  public Duration getTimeout() { return timeout; }
  public int getPopularWordCount() { return popularWordCount; }
  public String getProfileOutputPath() { return profileOutputPath; }
  public String getResultPath() { return resultPath; }

  @JsonPOJOBuilder(withPrefix = "set")
  public static final class Builder {

    private List<String> startPages = new ArrayList<>();
    private List<String> ignoredUrls = new ArrayList<>();
    private List<String> ignoredWords = new ArrayList<>();
    private int parallelism = -1;
    private String implementationOverride = "";
    private int maxDepth = 10;
    private int timeoutSeconds = 2;
    private int popularWordCount = 10;
    private String profileOutputPath = "";
    private String resultPath = "";

    @JsonProperty("startPages")
    public Builder setStartPages(List<String> startPages) {
      this.startPages = startPages != null ? startPages : new ArrayList<>();
      return this;
    }

    @JsonProperty("ignoredUrls")
    public Builder setIgnoredUrls(List<String> ignoredUrls) {
      this.ignoredUrls = ignoredUrls != null ? ignoredUrls : new ArrayList<>();
      return this;
    }

    @JsonProperty("ignoredWords")
    public Builder setIgnoredWords(List<String> ignoredWords) {
      this.ignoredWords = ignoredWords != null ? ignoredWords : new ArrayList<>();
      return this;
    }

    @JsonProperty("parallelism")
    public Builder setParallelism(int parallelism) {
      this.parallelism = parallelism;
      return this;
    }

    @JsonProperty("implementationOverride")
    public Builder setImplementationOverride(String implementationOverride) {
      this.implementationOverride = implementationOverride != null ? implementationOverride : "";
      return this;
    }

    @JsonProperty("maxDepth")
    public Builder setMaxDepth(int maxDepth) {
      this.maxDepth = maxDepth;
      return this;
    }

    @JsonProperty("timeoutSeconds")
    public Builder setTimeoutSeconds(int timeoutSeconds) {
      this.timeoutSeconds = timeoutSeconds;
      return this;
    }

    @JsonProperty("popularWordCount")
    public Builder setPopularWordCount(int popularWordCount) {
      this.popularWordCount = popularWordCount;
      return this;
    }

    @JsonProperty("profileOutputPath")
    public Builder setProfileOutputPath(String profileOutputPath) {
      this.profileOutputPath = profileOutputPath != null ? profileOutputPath : "";
      return this;
    }

    @JsonProperty("resultPath")
    public Builder setResultPath(String resultPath) {
      this.resultPath = resultPath != null ? resultPath : "";
      return this;
    }

    public CrawlerConfiguration build() {
      return new CrawlerConfiguration(this);
    }
  }
}
