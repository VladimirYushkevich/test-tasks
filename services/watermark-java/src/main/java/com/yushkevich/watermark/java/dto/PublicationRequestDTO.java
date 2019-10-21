package com.yushkevich.watermark.java.dto;

import com.yushkevich.watermark.java.domain.Content;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * Request for publication operations.
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicationRequestDTO {
    @NotNull
    private Long publicationId;
    @NotNull
    private Content content;
}
