package com.company.watermark.domain;

import lombok.*;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import static com.company.watermark.domain.Content.JOURNAL;
import static com.company.watermark.domain.Content.Values.JOURNAL_VALUE;

/**
 * Entity class for Journal.
 *
 * @see Publication
 */

@Entity
@DiscriminatorValue(JOURNAL_VALUE)
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Journal extends Publication {

    @Builder
    public Journal(final String title, final String author) {
        super(null, JOURNAL.getName(), title, author, null, null, null);
    }
}