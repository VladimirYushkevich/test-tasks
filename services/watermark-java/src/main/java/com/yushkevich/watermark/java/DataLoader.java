package com.yushkevich.watermark.java;

import com.yushkevich.watermark.java.domain.Book;
import com.yushkevich.watermark.java.domain.Journal;
import com.yushkevich.watermark.java.domain.Watermark;
import com.yushkevich.watermark.java.repository.BookRepository;
import com.yushkevich.watermark.java.repository.JournalRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import static com.yushkevich.watermark.java.domain.Book.Topic.*;
import static com.yushkevich.watermark.java.domain.Watermark.Status.*;
import static com.yushkevich.watermark.java.utils.WatermarkGenerator.generateWatermark;

/**
 * Loads data on start up. It avoid us writing SQL statements.
 */

@Component
@AllArgsConstructor
@Slf4j
@Profile("!test")
public class DataLoader implements ApplicationListener<ContextRefreshedEvent> {

    private final BookRepository bookRepository;
    private final JournalRepository journalRepository;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {

        final Book book1 = Book.builder()
                .title("title1")
                .author("author1")
                .topic(MEDIA)
                .build();
        book1.setWatermark(Watermark.builder()
                .publication(book1)
                .status(NEW.getName())
                .build());

        bookRepository.save(book1);

        log.info("Loaded {}", book1);

        final Book book2 = Book.builder()
                .title("The Dark Code")
                .author("Bruce Wayne")
                .topic(SCIENCE)
                .build();
        book2.setWatermark(Watermark.builder()
                .publication(book2)
                .property(generateWatermark(book2.getWatermarkProperties()))
                .status(SUCCESS.getName())
                .build());

        bookRepository.save(book2);

        log.info("Loaded {}", book2);

        final Book book3 = Book.builder()
                .title("How to make money")
                .author("Dr. Evil")
                .topic(BUSINESS)
                .build();
        book3.setWatermark(Watermark.builder()
                .publication(book3)
                .status(FAILED.getName())
                .build());

        bookRepository.save(book3);

        log.info("Loaded {}", book3);

        final Journal journal1 = Journal.builder()
                .title("Journal of human flight routes")
                .author("Clark Kent")
                .build();
        journal1.setWatermark(Watermark.builder()
                .publication(journal1)
                .property(generateWatermark(journal1.getWatermarkProperties()))
                .status(SUCCESS.getName())
                .build());

        journalRepository.save(journal1);

        log.info("Loaded {}", journal1);

        log.info("Total loaded books/journals: [{}/{}]", bookRepository.findAll().size(), journalRepository.findAll().size());
    }
}
