package com.yushkevich.watermark.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.restassured.RestAssured;
import com.yushkevich.watermark.domain.Content;
import com.yushkevich.watermark.dto.PublicationDTO;
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
@ActiveProfiles("it")
public abstract class BaseControllerIT {

    @Autowired
    protected ObjectMapper objectMapper;
    @Value("http://localhost:${local.server.port}/api/v1/publication")
    protected String publicationBase;
    @Value("http://localhost:${local.server.port}/api/v1/watermark")
    protected String watermarkBase;

    protected PublicationDTO book;
    protected PublicationDTO journal;

    @Before
    public void setUp() throws Exception {
        book = objectMapper.readValue(getClass().getResourceAsStream("/json/publication_dto_book.json"), PublicationDTO.class);
        journal = objectMapper.readValue(getClass().getResourceAsStream("/json/publication_dto_journal.json"), PublicationDTO.class);
    }

    Long createAndVerifyPublication(PublicationDTO publicationDTO, Matcher<Object> topicMatcher) throws Exception {
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

    void testUpdatePublication_success(PublicationDTO originalPublicationDTO, PublicationDTO publicationDTOToUpdate,
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

    void testUpdatePublication_fail(PublicationDTO publicationDTOToUpdate) throws Exception {
        RestAssured.given()
                .contentType(JSON)
                .body(objectMapper.writeValueAsString(publicationDTOToUpdate))
                .when()
                .put(publicationBase + "/update").prettyPeek()
                .then()
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    PublicationDTO resolvePublicationDTO(Content content) {
        switch (content) {
            case BOOK:
                return book;
            case JOURNAL:
                return journal;
        }
        return null;
    }
}
