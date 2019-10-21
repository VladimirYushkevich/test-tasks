package com.yushkevich.watermark.java.service.impl;

import com.yushkevich.watermark.java.client.WatermarkClient;
import com.yushkevich.watermark.java.domain.Content;
import com.yushkevich.watermark.java.domain.Publication;
import com.yushkevich.watermark.java.domain.Watermark;
import com.yushkevich.watermark.java.exception.NotFoundException;
import com.yushkevich.watermark.java.service.PublicationService;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit4.SpringRunner;

import static com.yushkevich.watermark.java.RepositoryDataFactory.buildPublication;
import static com.yushkevich.watermark.java.domain.Watermark.Status.*;
import static com.yushkevich.watermark.java.utils.WatermarkGenerator.generateWatermark;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@ComponentScan({"com.yushkevich.watermark.java.service.impl", "com.yushkevich.watermark.java.client"})
@DataJpaTest
public abstract class BasePublicationServiceIT {

    @Autowired
    protected PublicationService publicationService;
    @MockBean
    private WatermarkClient watermarkClient;

    protected void testPublicationCrudOperations(Content content, String watermarkProperty) {
        //create
        //when
        final Publication publication = publicationService.create(buildPublication(content));
        //then
        final Long id = publication.getId();
        assertNotNull(id);

        //when
        final Watermark watermark = publicationService.find(id, content).getWatermark();
        //then
        assertThat(watermark.getPublication(), is(publication));

        //update
        //given
        watermark.setProperty(generateWatermark(watermark.getPublication().getWatermarkProperties()));
        //when
        final String property = publicationService.find(id, content).getWatermark().getProperty();
        //then
        assertThat(property, is(watermarkProperty));

        //delete
        //when
        publicationService.delete(id, content);
        //then
        try {
            publicationService.find(id, content);
            fail("Should throw exception");
        } catch (NotFoundException ignored) {
        }

        //pageable
        //given
        publicationService.create(buildPublication(content));
        publicationService.create(buildPublication(content));
        publicationService.create(buildPublication(content));

        //when
        final Page<Publication> journalPage1 = publicationService.findAllByPage(new PageRequest(0, 2), content);
        //then
        assertThat(journalPage1.getTotalElements(), is(3L));
        assertThat(journalPage1.getContent().size(), is(2));

        //when
        final Page<Publication> journalPage2 = publicationService.findAllByPage(new PageRequest(1, 2), content);
        //then
        assertThat(journalPage2.getTotalElements(), is(3L));
        assertThat(journalPage2.getContent().size(), is(1));
    }

    protected void testWatermarkSetUp(Content content) {
        //given
        Publication publication = buildPublication(content);
        publication.setWatermark(null);
        final Publication publicationWithoutWatermark = publicationService.create(publication);
        assertNull(publicationWithoutWatermark.getWatermark());
        //when
        final Watermark watermark = publicationService.setWatermark(publicationWithoutWatermark.getId(), content);
        //then
        assertThat(publicationService.find(publication.getId(), content).getWatermark(), is(watermark));

        //given
        final Publication publicationWithNewWatermark = publicationService.create(buildPublication(content));
        assertThat(publicationWithNewWatermark.getWatermark().getStatus(), is(NEW));
        //when
        publicationService.setWatermark(publicationWithNewWatermark.getId(), content);
        //then
        assertThat(publicationService.find(publicationWithNewWatermark.getId(), content).getWatermark().getStatus(), is(PENDING));

        //given
        publication = buildPublication(content);
        publication.getWatermark().setStatus(FAILED.getName());
        Publication publicationWithFailedWatermark = publicationService.create(publication);
        assertThat(publicationService.find(publication.getId(), content).getWatermark().getStatus(), is(FAILED));
        //when
        publicationService.setWatermark(publicationWithFailedWatermark.getId(), content);
        //then
        assertThat(publicationService.find(publicationWithFailedWatermark.getId(), content).getWatermark().getStatus(), is(PENDING));

        //given
        publication = buildPublication(content);
        publication.getWatermark().setStatus(PENDING.getName());
        final Publication publicationWithPendingWatermark = publicationService.create(publication);
        //when
        publicationService.setWatermark(publicationWithPendingWatermark.getId(), content);
        //then
        fail("Should throw exception");
    }

    protected void testWatermarkSetUp_fail_notAllowedToUpdatePending(Content content) {
        //given
        Publication publication = buildPublication(content);
        publication.getWatermark().setStatus(PENDING.getName());
        final Publication publicationWithPendingWatermark = publicationService.create(publication);
        assertThat(publicationWithPendingWatermark.getWatermark().getStatus(), is(PENDING));
        //when
        publicationWithPendingWatermark.setAuthor("newAuthor");
        publicationService.update(publication);
        //then
        fail("Should throw exception");
    }

    protected void testWatermarkStatusUpdateForPublication(Content content) {
        //given
        Publication publication = publicationService.create(buildPublication(content));
        //when
        publicationService.updateWatermarkStatus(publication, "");
        //then
        assertThat(publicationService.find(publication.getId(), content).getWatermark().getStatus(), is(FAILED));

        //given
        publication = publicationService.create(buildPublication(content));
        //when
        publicationService.updateWatermarkStatus(publication, "someProperty");
        //then
        assertThat(publicationService.find(publication.getId(), content).getWatermark().getStatus(), is(SUCCESS));
    }
}
