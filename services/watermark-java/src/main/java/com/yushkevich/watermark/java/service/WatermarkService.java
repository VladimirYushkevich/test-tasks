package com.yushkevich.watermark.java.service;

import com.yushkevich.watermark.java.domain.Content;
import com.yushkevich.watermark.java.domain.Publication;
import com.yushkevich.watermark.java.domain.Watermark;
import com.yushkevich.watermark.java.exception.WatermarkException;
import rx.Observable;

import java.util.UUID;

/**
 * Watermark service for creation/monitoring of watermarks.
 */

public interface WatermarkService {

    /**
     * For a given content document (publication) the service should
     * return a ticket, which can be used to poll the status of processing.
     * Assuming that document watermarking is a time consuming process.
     * Not allowed to watermark document if it already in pending status.
     *
     * @param publicationId Id of Publication (Document Id)
     * @param content       Content of document
     * @return Observable UUID of Ticket/Watermark
     * @see Publication
     */
    Observable<UUID> watermarkDocument(Long publicationId, Content content) throws WatermarkException;

    /**
     * Polls status of watermark. If the watermarking is finished the
     * document can be retrieved with the ticket.
     *
     * @param ticketId Id of ticket/watermark
     * @return Ticket with optional document
     */
    Observable<Watermark> pollWatermarkStatus(UUID ticketId);
}