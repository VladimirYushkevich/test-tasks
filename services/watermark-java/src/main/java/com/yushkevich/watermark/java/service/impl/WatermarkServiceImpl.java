package com.yushkevich.watermark.java.service.impl;

import com.yushkevich.watermark.java.client.WatermarkClient;
import com.yushkevich.watermark.java.client.WatermarkHystrixCommandProperties;
import com.yushkevich.watermark.java.client.command.WatermarkCommand;
import com.yushkevich.watermark.java.domain.Content;
import com.yushkevich.watermark.java.domain.Publication;
import com.yushkevich.watermark.java.domain.Watermark;
import com.yushkevich.watermark.java.exception.NotFoundException;
import com.yushkevich.watermark.java.exception.WatermarkException;
import com.yushkevich.watermark.java.repository.WatermarkRepository;
import com.yushkevich.watermark.java.service.PublicationService;
import com.yushkevich.watermark.java.service.WatermarkService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import rx.Observable;

import java.util.Optional;
import java.util.UUID;

@Component
@AllArgsConstructor
@Transactional
@Slf4j
public class WatermarkServiceImpl implements WatermarkService {

    private final WatermarkRepository watermarkRepository;
    private final PublicationService publicationService;
    private final WatermarkClient watermarkClient;
    private final WatermarkHystrixCommandProperties watermarkHystrixCommandProperties;

    @Override
    public Observable<UUID> watermarkDocument(Long publicationId, Content content) {
        return Observable.create(subscriber -> {
                    subscriber.onNext(getWatermarkUuid(publicationId, content));
                    subscriber.onCompleted();
                    subscriber.onError(
                            new WatermarkException(String.format("can't watermark document [%s/%s]", publicationId, content)));
                }
        );
    }

    @Override
    public Observable<Watermark> pollWatermarkStatus(UUID ticketId) {
        return Observable.create(subscriber -> {
                    subscriber.onNext(find(ticketId));
                    subscriber.onCompleted();
                    subscriber.onError(
                            new WatermarkException(String.format("can't retrieve watermark with id=%s", ticketId)));
                }
        );
    }

    private Watermark find(UUID ticketId) {
        final Watermark watermark = Optional.ofNullable(watermarkRepository.findById(ticketId))
                .orElseThrow(NotFoundException::new);
        log.debug("found {}", watermark);
        return watermark;
    }

    /**
     * Retrieves UUID of Watermark and executes watermark generation via hystrix command({@link WatermarkCommand}).
     *
     * @param publicationId Publication id
     * @param content       Publication content
     * @return UUID of Watermark
     * @see Watermark
     * @see Publication
     */
    private UUID getWatermarkUuid(Long publicationId, Content content) {
        log.debug("::getWatermarkUuid [{}/{}]", publicationId, content);

        final Watermark createdWatermark = publicationService.setWatermark(publicationId, content);

        WatermarkCommand.builder()
                .groupKey(watermarkHystrixCommandProperties.getGroupKey())
                .debugMessage("watermarkDocument")
                .timeout(watermarkHystrixCommandProperties.getTimeoutInMilliseconds())
                .watermarkProperties(createdWatermark.getPublication().getWatermarkProperties())
                .watermarkClient(watermarkClient)
                .build()
                .observe()
                .subscribe(o -> publicationService.updateWatermarkStatus(createdWatermark.getPublication(), o));

        return createdWatermark.getId();
    }
}
