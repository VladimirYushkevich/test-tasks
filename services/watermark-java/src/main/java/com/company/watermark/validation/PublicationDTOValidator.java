package com.company.watermark.validation;

import com.company.watermark.dto.PublicationDTO;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import java.util.Set;

import static com.company.watermark.domain.Content.BOOK;
import static java.util.Objects.isNull;

@Component
public class PublicationDTOValidator extends AbstractDTOValidator<PublicationDTO> {

    private static final String FIELD_CONTENT = "content";
    private static final String FIELD_TITLE = "title";
    private static final String FIELD_AUTHOR = "author";
    private static final String FIELD_TOPIC = "topic";

    @Override
    protected void validate(PublicationDTO dto, Errors errors, Set<Class<?>> hints) {

        if (isNull(errors.getFieldValue(FIELD_CONTENT))) {
            errors.rejectValue(FIELD_CONTENT, CODE_FIELD_REQUIRED);
        } else if (BOOK.equals(dto.getContent()) && isNull(dto.getTopic())) {
            errors.rejectValue(FIELD_TOPIC, CODE_FIELD_REQUIRED);
        }

        if (isNull(errors.getFieldValue(FIELD_TITLE))) {
            errors.rejectValue(FIELD_TITLE, CODE_FIELD_REQUIRED);
        }

        if (isNull(errors.getFieldValue(FIELD_AUTHOR))) {
            errors.rejectValue(FIELD_AUTHOR, CODE_FIELD_REQUIRED);
        }
    }
}
