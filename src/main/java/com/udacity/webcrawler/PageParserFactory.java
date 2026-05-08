package com.udacity.webcrawler;

public interface PageParserFactory {
  PageParser get(String url);
}
