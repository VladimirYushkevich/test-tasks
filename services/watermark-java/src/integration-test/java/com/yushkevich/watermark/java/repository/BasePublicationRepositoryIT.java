package com.yushkevich.watermark.java.repository;

import com.yushkevich.watermark.java.domain.Content;
import com.yushkevich.watermark.java.domain.Publication;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Objects;

import static com.yushkevich.watermark.java.RepositoryDataFactory.buildPublication;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@DataJpaTest
public abstract class BasePublicationRepositoryIT {

    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private JournalRepository journalRepository;
    @Autowired
    private TestEntityManager entityManager;

    @SuppressWarnings("unchecked")
    protected void testPublicationCrudOperations(Content content) {
        final PublicationRepository publicationRepository = resolveRepository(content);

        //given
        final Publication publication = buildPublication(content);
        //when
        entityManager.persist(publication);
        //then
        assertThat(publicationRepository.findAll().size(), is(1));
        assertNotNull(publication);
        assertThat(Objects.requireNonNull(publicationRepository).findOne(publication.getId()), is(publication));

        //given
        publication.setAuthor("newAuthor");
        //when
        entityManager.persist(publication);
        //then
        assertThat(((Publication) publicationRepository.findOne(publication.getId())).getAuthor(), is("newAuthor"));

        //when
        publicationRepository.delete(publication.getId());
        //then
        assertThat(publicationRepository.findAll().size(), is(0));
    }

    @SuppressWarnings("unchecked")
    public void testPublicationPageable(Content content) throws Exception {
        final PublicationRepository publicationRepository = resolveRepository(content);

        //given
        entityManager.persist(buildPublication(content));
        entityManager.persist(buildPublication(content));
        entityManager.persist(buildPublication(content));

        //when
        final Page<Publication> publicationPage1 = publicationRepository.findAll(new PageRequest(0, 2));
        //then
        assertThat(publicationPage1.getTotalElements(), is(3L));
        assertThat(publicationPage1.getContent().size(), is(2));

        //when
        final Page<Publication> publicationPage2 = publicationRepository.findAll(new PageRequest(1, 2));
        //then
        assertThat(publicationPage2.getTotalElements(), is(3L));
        assertThat(publicationPage2.getContent().size(), is(1));
    }

    private PublicationRepository resolveRepository(Content content) {
        switch (content) {
            case BOOK:
                return bookRepository;
            case JOURNAL:
                return journalRepository;
            default:
                throw new RuntimeException();
        }
    }
}
