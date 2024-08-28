package pl.myapp.webcrawler;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.myapp.webcrawler.domain.CrawledUrl;

@Repository
public interface CrawledUrlRepository extends JpaRepository<CrawledUrl, Long> {
    boolean existsByUrl(final String url);
}
