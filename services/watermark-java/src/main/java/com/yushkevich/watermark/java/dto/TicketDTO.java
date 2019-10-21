package com.yushkevich.watermark.java.dto;

import com.yushkevich.watermark.java.domain.Watermark;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Ticket class used for watermark polling. This entity is in one-to-one relation with
 * {@link com.yushkevich.watermark.java.domain.Watermark}
 * (Ticket {@link #id} is a primary key in <b>Watermark</b>).
 * If the watermarking is finished the
 * document can be retrieved with the ticket.
 *
 * @see PublicationDTO
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TicketDTO {
    private UUID id;
    private Watermark.Status status;
    private PublicationDTO document;
}
