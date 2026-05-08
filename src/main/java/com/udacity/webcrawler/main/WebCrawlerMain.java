package com.udacity.webcrawler.main;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.udacity.webcrawler.WebCrawler;
import com.udacity.webcrawler.WebCrawlerModule;
import com.udacity.webcrawler.json.ConfigurationLoader;
import com.udacity.webcrawler.json.CrawlResult;
import com.udacity.webcrawler.json.CrawlResultWriter;
import com.udacity.webcrawler.json.CrawlerConfiguration;
import com.udacity.webcrawler.profiler.Profiler;
import com.udacity.webcrawler.profiler.ProfilerImpl;

import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Path;

public final class WebCrawlerMain {

  private final CrawlerConfiguration config;

  private WebCrawlerMain(CrawlerConfiguration config) {
    this.config = config;
  }

  private void run() throws Exception {
    Injector injector = Guice.createInjector(new WebCrawlerModule(config));

    WebCrawler crawler = injector.getInstance(WebCrawler.class);
    Profiler profiler = injector.getInstance(ProfilerImpl.class);

    WebCrawler profiledCrawler = profiler.wrap(WebCrawler.class, crawler);

    CrawlResult result = profiledCrawler.crawl(config.getStartPages());
    CrawlResultWriter resultWriter = new CrawlResultWriter(result);

    String resultPath = config.getResultPath();
    if (!resultPath.isEmpty()) {
      resultWriter.write(Path.of(resultPath));
    } else {
      // Do not close System.out — just flush after writing
      Writer stdoutWriter = new OutputStreamWriter(System.out);
      resultWriter.write(stdoutWriter);
      stdoutWriter.flush();
    }

    String profileOutputPath = config.getProfileOutputPath();
    if (!profileOutputPath.isEmpty()) {
      profiler.writeData(Path.of(profileOutputPath));
    } else {
      Writer stdoutWriter = new OutputStreamWriter(System.out);
      profiler.writeData(stdoutWriter);
      stdoutWriter.flush();
    }
  }

  public static void main(String[] args) throws Exception {
    if (args.length != 1) {
      System.out.println("Usage: WebCrawlerMain <config-file-path>");
      return;
    }
    CrawlerConfiguration config = new ConfigurationLoader(Path.of(args[0])).load();
    new WebCrawlerMain(config).run();
  }
}
