package com.company.watermark.utils.mapper;

import com.company.watermark.domain.Watermark;
import com.company.watermark.dto.TicketDTO;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import static com.company.watermark.domain.Watermark.Status.SUCCESS;
import static com.company.watermark.utils.mapper.PublicationMapper.buildPublicationDTO;
import static org.springframework.beans.BeanUtils.copyProperties;

/**
 * Utility class for mapping watermark to related DTOs and vice versa.
 */

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class WatermarkMapper {

    public static TicketDTO buildTicketDTO(Watermark watermark) {
        final TicketDTO dto = new TicketDTO();

        copyProperties(watermark, dto);
        if (SUCCESS.equals(watermark.getStatus())) {
            dto.setDocument(buildPublicationDTO(watermark.getPublication()));
        }

        return dto;
    }
}
