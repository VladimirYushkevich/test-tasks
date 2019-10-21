package com.yushkevich.watermark.java.controller;

import com.yushkevich.watermark.java.domain.Content;
import com.yushkevich.watermark.java.dto.PublicationDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.restassured.RestAssured;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static com.jayway.restassured.http.ContentType.JSON;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Base class for Integration Tests.
 */

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class BaseControllerIT {

    @Autowired
    protected ObjectMapper objectMapper;
    @Value("http://localhost:${local.server.port}/publication")
    protected String publicationBase;
    @Value("http://localhost:${local.server.port}/watermark")
    protected String watermarkBase;

    protected PublicationDTO book;
    protected PublicationDTO journal;

    @Before
    public void setUp() throws Exception {
        book = objectMapper.readValue(getClass().getResourceAsStream("/json/publication_dto_book.json"), PublicationDTO.class);
        journal = objectMapper.readValue(getClass().getResourceAsStream("/json/publication_dto_journal.json"), PublicationDTO.class);
    }

    protected Long createAndVerifyPublication(PublicationDTO publicationDTO, Matcher<Object> topicMatcher) throws Exception {
        return RestAssured.given()
            .contentType(JSON)
            .body(objectMapper.writeValueAsString(publicationDTO))
        .when()
            .post(publicationBase + "/create").prettyPeek()
        .then()
            .statusCode(HttpStatus.CREATED.value())
            .body("id", notNullValue())
            .body("content", is(publicationDTO.getContent().toString()))
            .body("title", is(publicationDTO.getTitle()))
            .body("author", is(publicationDTO.getAuthor()))
            .body("topic", topicMatcher)
        .extract()
            .jsonPath()
            .getLong("id");
    }

    protected void testUpdatePublication_success(PublicationDTO originalPublicationDTO, PublicationDTO publicationDTOToUpdate,
                                                  Matcher<Object> topicMatcher) throws Exception {
        RestAssured.given()
            .contentType(JSON)
            .body(objectMapper.writeValueAsString(publicationDTOToUpdate))
        .when()
            .put(publicationBase + "/update").prettyPeek()
        .then()
            .statusCode(HttpStatus.CREATED.value())
            .body("id", notNullValue())
            .body("content", is(originalPublicationDTO.getContent().toString()))
            .body("title", is(originalPublicationDTO.getTitle()))
            .body("author", is(publicationDTOToUpdate.getAuthor()))
            .body("topic", topicMatcher);
    }

    protected void testUpdatePublication_fail(PublicationDTO publicationDTOToUpdate) throws Exception {
        RestAssured.given()
            .contentType(JSON)
            .body(objectMapper.writeValueAsString(publicationDTOToUpdate))
        .when()
            .put(publicationBase + "/update").prettyPeek()
        .then()
            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    protected PublicationDTO resolvePublicationDTO(Content content) {
        switch (content) {
            case BOOK:
                return book;
            case JOURNAL:
                return journal;
        }
        return null;
    }
}
