package pl.myapp.webcrawler;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URISyntaxException;

@RestController
@RequiredArgsConstructor
public class WebCrawlerController {

    final private WebCrawlerService service;

    @GetMapping("/crawl")
    public String crawl(@RequestParam final String url) {
        try {
            service.crawlWebpage(url);
            return "Crawling started for: " + url;
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            return "Error occurred while crawling the URL.";
        }
    }
}
