package com.yushkevich.watermark.java.utils.mapper;

import com.yushkevich.watermark.java.domain.Watermark;
import com.yushkevich.watermark.java.dto.TicketDTO;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import static com.yushkevich.watermark.java.domain.Watermark.Status.SUCCESS;
import static com.yushkevich.watermark.java.utils.mapper.PublicationMapper.buildPublicationDTO;
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
