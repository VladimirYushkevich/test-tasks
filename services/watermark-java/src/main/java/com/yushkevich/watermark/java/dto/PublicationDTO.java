package com.yushkevich.watermark.java.dto;

import com.yushkevich.watermark.java.domain.Book;
import com.yushkevich.watermark.java.domain.Content;
import com.yushkevich.watermark.java.domain.Journal;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * Class for publication presentation. Base class for (book, journal) and watermark properties.
 *
 * @see Book
 * @see Journal
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PublicationDTO {
    private Long id;
    @NotNull
    @ApiModelProperty(notes = "Content of publication", required = true)
    private Content content;
    @NotNull
    @ApiModelProperty(notes = "Title of publication", required = true)
    private String title;
    @NotNull
    @ApiModelProperty(notes = "Author of publication", required = true)
    private String author;
    @ApiModelProperty(notes = "Topic(for book only)")
    private Book.Topic topic;

}