package com.yushkevich.watermark.java.repository;

import com.yushkevich.watermark.java.domain.Book;
import com.yushkevich.watermark.java.domain.Journal;
import com.yushkevich.watermark.java.domain.Watermark;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.UUID;

import static com.yushkevich.watermark.java.RepositoryDataFactory.createBook;
import static com.yushkevich.watermark.java.RepositoryDataFactory.createJournal;
import static com.yushkevich.watermark.java.utils.WatermarkGenerator.generateWatermark;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@DataJpaTest
public class WatermarkRepositoryIT {

    @Autowired
    private WatermarkRepository watermarkRepository;
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private JournalRepository journalRepository;
    @Autowired
    private TestEntityManager entityManager;

    @Test
    public void testWatermarkForBookCrudOperations() throws Exception {
        //create watermark
        //given
        final Book book = createBook();
        //when
        entityManager.persist(book);
        //then
        assertThat(bookRepository.findOne(book.getId()), is(book));
        assertNull(bookRepository.findOne(book.getId()).getWatermark().getProperty());

        final Watermark watermark = book.getWatermark();
        final UUID watermarkId = watermark.getId();
        assertThat(watermarkRepository.findById(watermarkId), is(watermark));
        assertThat(watermarkRepository.findById(watermarkId).getPublication(), is(book));

        //update watermark
        //given
        watermark.setProperty(generateWatermark(watermark.getPublication().getWatermarkProperties()));
        //when
        entityManager.persist(book);
        //then
        assertThat(bookRepository.findOne(book.getId()).getWatermark().getProperty(),
                is("---*book*authorBook*titleBook*Science*---"));

        //delete watermark
        //when
        book.getWatermark().setPublication(null);
        book.setWatermark(null);
        //then
        assertNull(bookRepository.findOne(book.getId()).getWatermark());
        assertTrue(watermarkRepository.findAll().isEmpty());
    }

    @Test
    public void testWatermarkForJournalCrudOperations() throws Exception {
        //create watermark
        //given
        final Journal journal = createJournal();
        //when
        entityManager.persist(journal);
        //then
        assertThat(journalRepository.findOne(journal.getId()), is(journal));
        assertNull(journalRepository.findOne(journal.getId()).getWatermark().getProperty());

        final Watermark watermark = journal.getWatermark();
        final UUID watermarkId = journal.getWatermark().getId();
        assertThat(watermarkRepository.findById(watermarkId), is(journal.getWatermark()));
        assertThat(watermarkRepository.findById(watermarkId).getPublication(), is(journal));

        //update watermark
        //given
        journal.getWatermark().setProperty(generateWatermark(watermark.getPublication().getWatermarkProperties()));
        //when
        entityManager.persist(journal);
        //then
        assertThat(journalRepository.findOne(journal.getId()).getWatermark().getProperty(),
                is("---*journal*authorJournal*titleJournal*---"));

        //delete watermark
        //when
        watermark.setPublication(null);
        journal.setWatermark(null);
        //then
        assertNull(journalRepository.findOne(journal.getId()).getWatermark());
        assertTrue(watermarkRepository.findAll().isEmpty());
    }
}
