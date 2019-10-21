package com.company.watermark;

import com.company.watermark.domain.Book;
import com.company.watermark.domain.Journal;
import com.company.watermark.domain.Watermark;
import com.company.watermark.repository.BookRepository;
import com.company.watermark.repository.JournalRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import static com.company.watermark.RepositoryDataFactory.createJournal;
import static com.company.watermark.domain.Book.Topic.*;
import static com.company.watermark.domain.Watermark.Status.NEW;

/**
 * Loads test data on start up. It avoid us writing SQL statements.
 */

@Component
@AllArgsConstructor
@Slf4j
@Profile("test")
public class TestDataLoader implements ApplicationListener<ContextRefreshedEvent> {

    private final BookRepository bookRepository;
    private final JournalRepository journalRepository;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {

        final Book book1 = Book.builder()
                .title("title1")
                .author("author1")
                .topic(SCIENCE)
                .build();
        book1.setWatermark(Watermark.builder()
                .publication(book1)
                .status(NEW.getName())
                .build());

        bookRepository.save(book1);

        log.debug("::[TEST] Loaded {}", book1);

        final Book book2 = bookRepository.save(Book.builder()
                .title("test2")
                .author("author2")
                .topic(BUSINESS)
                .build());

        log.debug("::[TEST] Loaded {}", book2);

        final Book book3 = bookRepository.save(Book.builder()
                .title("test3")
                .author("author3")
                .topic(MEDIA)
                .build());

        log.debug("::[TEST] Loaded {}", book3);

        bookRepository.flush();

        final Journal journal1 = journalRepository.save(Journal.builder()
                .title("title11")
                .author("author11")
                .build());
        journal1.setWatermark(Watermark.builder()
                .publication(book1)
                .status(NEW.getName())
                .build());

        journalRepository.save(journal1);

        journalRepository.save(createJournal());
        journalRepository.save(createJournal());

        journalRepository.flush();

        log.debug("::[TEST] Loaded {}", journal1);

        log.debug("::[TEST] Total loaded books/journals: [{}/{}]", bookRepository.findAll().size(), journalRepository.findAll().size());
    }
}
