package com.yushkevich.watermark.service.impl;

import com.yushkevich.watermark.domain.Content;
import com.yushkevich.watermark.domain.Publication;
import com.yushkevich.watermark.domain.Watermark;
import com.yushkevich.watermark.exception.NotFoundException;
import com.yushkevich.watermark.exception.WatermarkException;
import com.yushkevich.watermark.repository.BookRepository;
import com.yushkevich.watermark.repository.JournalRepository;
import com.yushkevich.watermark.repository.PublicationRepository;
import com.yushkevich.watermark.service.PublicationService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static com.yushkevich.watermark.domain.Watermark.Status.*;
import static com.yushkevich.watermark.utils.mapper.PublicationMapper.getNullPropertyNames;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.springframework.beans.BeanUtils.copyProperties;

@Component
@AllArgsConstructor
@Transactional
@Slf4j
public class PublicationServiceImpl implements PublicationService {

    private final BookRepository bookRepository;
    private final JournalRepository journalRepository;

    @Override
    @SuppressWarnings("unchecked")
    public Publication create(Publication publication) {
        final Publication createdPublication = (Publication) resolveRepository(publication.getContent()).save(publication);
        log.debug("created {}", createdPublication);
        return createdPublication;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Publication update(Publication publication) {
        final Long id = publication.getId();
        final PublicationRepository publicationRepository = resolveRepository(publication.getContent());

        final Publication publicationToUpdate = Optional.ofNullable(publicationRepository.findById(id))
                .orElseThrow(NotFoundException::new);
        log.debug("found publicationToUpdate {}", publicationToUpdate);

        validateWatermarkStatus(id, publication.getContent());

        copyProperties(publication, publicationToUpdate, getNullPropertyNames(publication));

        final Watermark watermark = publicationToUpdate.getWatermark();
        if (nonNull(watermark) && (FAILED.equals(watermark.getStatus()) || SUCCESS.equals(watermark.getStatus()))) {
            watermark.setStatus(NEW.getName());
            watermark.setProperty(null);
        }
        final Publication updatedPublication = (Publication) publicationRepository.save(publicationToUpdate);

        log.debug("updated {}", updatedPublication);

        return updatedPublication;
    }

    @Override
    public Publication find(Long publicationId, Content content) {
        final Publication publication = Optional.ofNullable(resolveRepository(content).findById(publicationId))
                .orElseThrow(NotFoundException::new);
        log.debug("found {}", publication);
        return publication;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void delete(Long publicationId, Content content) {
        final PublicationRepository publicationRepository = resolveRepository(content);

        Optional.ofNullable(publicationRepository.findById(publicationId))
                .orElseThrow(NotFoundException::new);

        //noinspection unchecked
        publicationRepository.delete(publicationId);

        log.debug("deleted publication with id = {}", publicationId);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Page<Publication> findAllByPage(Pageable pageable, Content content) {
        final Page page = resolveRepository(content).findAll(pageable);
        log.debug("page {} from {} has {} publications", page.getNumber(), page.getTotalElements(), page.getContent().size());
        return page;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Watermark setWatermark(Long publicationId, Content content) {
        log.debug("::setWatermark [{}/{}]", publicationId, content);

        validateWatermarkStatus(publicationId, content);

        final Publication publication = find(publicationId, content);
        final Watermark watermark = publication.getWatermark();
        if (isNull(watermark)) {
            publication.setWatermark(Watermark.builder()
                    .publication(publication)
                    .status(PENDING.getName())
                    .build());
        } else {
            watermark.setStatus(PENDING.getName());
            watermark.setProperty(null);
        }

        final Watermark updatedWatermark = ((Publication) resolveRepository(content).save(publication)).getWatermark();
        log.debug("publication with id={} related to watermark {}", publication.getId(), updatedWatermark);
        return updatedWatermark;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void updateWatermarkStatus(Publication publication, String watermarkProperty) {
        final Watermark watermark = publication.getWatermark();
        if (watermarkProperty.isEmpty()) {
            watermark.setStatus(FAILED.getName());
        } else {
            watermark.setProperty(watermarkProperty);
            watermark.setStatus(SUCCESS.getName());
        }
        log.debug("::updateWatermarkStatus [{}/{}/{}]", watermark.getStatus(), publication, watermarkProperty);

        resolveRepository(publication.getContent()).save(publication);
    }

    private void validateWatermarkStatus(Long publicationId, Content content) {
        final PublicationRepository publicationRepository = resolveRepository(content);

        final Publication publication = publicationRepository.findById(publicationId);
        final Watermark watermark = publication.getWatermark();
        log.debug("::validateWatermarkStatus {}", watermark);
        if (nonNull(watermark) && PENDING.equals(watermark.getStatus())) {
            throw new WatermarkException(String.format("Document with id=%s is already pending for watermark property",
                    publication.getId()));
        } else {
            log.debug("valid {}", watermark);
        }
    }

    private PublicationRepository resolveRepository(Content content) {
        switch (content) {
            case BOOK:
                return bookRepository;
            case JOURNAL:
                return journalRepository;
            default:
                log.error("Content not resolved for {}", content);
                throw new IllegalArgumentException();
        }
    }
}
