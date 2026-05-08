package com.udacity.webcrawler;

import com.google.inject.Inject;

import java.time.Duration;

final class PageParserFactoryImpl implements PageParserFactory {

  private final Duration timeout;

  @Inject
  PageParserFactoryImpl(@Timeout Duration timeout) {
    this.timeout = timeout;
  }

  @Override
  public PageParser get(String url) {
    return new PageParserImpl(url, timeout);
  }
}
