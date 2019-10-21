package com.yushkevich.watermark.java.utils.mapper;

import com.yushkevich.watermark.java.domain.Book;
import com.yushkevich.watermark.java.domain.Content;
import com.yushkevich.watermark.java.domain.Journal;
import com.yushkevich.watermark.java.domain.Publication;
import com.yushkevich.watermark.java.dto.PageDTO;
import com.yushkevich.watermark.java.dto.PublicationDTO;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.data.domain.Page;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.yushkevich.watermark.java.domain.Content.BOOK;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;
import static org.springframework.beans.BeanUtils.copyProperties;

/**
 * Utility class for mapping publication to related DTOs and vice versa.
 */

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PublicationMapper {

    public static Publication buildPublication(PublicationDTO dto) {
        Publication publication = null;

        final Content content = dto.getContent();
        switch (content) {
            case BOOK:
                publication = new Book();
                final Book.Topic topic = dto.getTopic();
                copyProperties(dto, publication);
                if (nonNull(topic)) {
                    ((Book) publication).setTopic(topic.getName());
                }
                break;
            case JOURNAL:
                publication = new Journal();
                copyProperties(dto, publication);
                break;
        }

        publication.setContent(content);

        return publication;
    }

    public static PublicationDTO buildPublicationDTO(Publication publication) {
        final PublicationDTO dto = new PublicationDTO();

        copyProperties(publication, dto);
        dto.setContent(publication.getContent());
        if (BOOK.equals(publication.getContent())) {
            dto.setTopic(((Book) publication).getTopic());
        }

        return dto;
    }

    public static PageDTO<PublicationDTO> buildPagePublicationDTO(Page<Publication> publicationPage) {
        final List<PublicationDTO> entries = publicationPage.getContent().parallelStream()
                .map(PublicationMapper::buildPublicationDTO)
                .collect(toList());

        return PageDTO.<PublicationDTO>builder()
                .totalPages(publicationPage.getTotalPages())
                .totalEntries(publicationPage.getTotalElements())
                .entries(entries)
                .build();
    }

    public static String[] getNullPropertyNames (Object source) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        java.beans.PropertyDescriptor[] pds = src.getPropertyDescriptors();

        Set<String> emptyNames = new HashSet<>();
        for(java.beans.PropertyDescriptor pd : pds) {
            Object srcValue = src.getPropertyValue(pd.getName());
            if (srcValue == null) emptyNames.add(pd.getName());
        }
        String[] result = new String[emptyNames.size()];
        return emptyNames.toArray(result);
    }
}
