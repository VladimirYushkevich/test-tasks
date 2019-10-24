package com.yushkevich.watermark.service;

import com.yushkevich.watermark.domain.Content;
import com.yushkevich.watermark.domain.Publication;
import com.yushkevich.watermark.domain.Watermark;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Simple CRUD operations for Books/Journals.
 */

public interface PublicationService {

    Publication create(Publication publication);

    /**
     * Updates publication only with status different than PENDING.
     * @param publication Publication to updated
     * @return Updated publication
     */
    Publication update(Publication publication);

    void delete(Long publicationId, Content content);

    Publication find(Long publicationId, Content content);

    Page<Publication> findAllByPage(Pageable pageable, Content content);

    Watermark setWatermark(Long publicationId, Content content);

    void updateWatermarkStatus(Publication publication, String watermarkProperty);
}
