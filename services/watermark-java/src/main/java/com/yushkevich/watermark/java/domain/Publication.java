package com.yushkevich.watermark.java.domain;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

import static java.util.Objects.isNull;

/**
 * Base entity class to support inheritance (A single table per class hierarchy) for publications.
 *
 * @see Book
 * @see Journal
 */

@Entity
@Table(name = "publications")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "content", discriminatorType = DiscriminatorType.STRING)
@EntityListeners(AuditingEntityListener.class)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public abstract class Publication implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, insertable = false, updatable = false)
    private String content;

    private String title;

    private String author;

    @CreatedDate
    @Column(updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    @LastModifiedDate
    @Version
    @Temporal(TemporalType.TIMESTAMP)
    private Date updated;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "watermark_id")
    private Watermark watermark;

    public Content getContent() {
        return Content.findByName(this.content);
    }

    public void setContent(Content content) {
        this.content = content.getName();
    }

    public List<String> getWatermarkProperties() {
        return Lists.newArrayList(content, author, title);
    }

    @Override
    public String toString() {
        return "Publication[" +
                "id=" + id +
                ", content='" + content + '\'' +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", watermark=" + (isNull(watermark) ? null :
                String.format("[%s/%s/%s]", watermark.getId(), watermark.getProperty(), watermark.getStatus())) +
                ']';
    }
}
