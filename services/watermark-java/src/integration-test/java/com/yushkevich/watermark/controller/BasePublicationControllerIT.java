package com.yushkevich.watermark.controller;

import com.yushkevich.watermark.domain.Content;
import com.yushkevich.watermark.dto.PublicationDTO;
import com.yushkevich.watermark.dto.PublicationRequestDTO;
import com.jayway.restassured.RestAssured;
import org.hamcrest.Matcher;
import org.springframework.http.HttpStatus;

import java.util.List;

import static com.jayway.restassured.http.ContentType.JSON;
import static org.hamcrest.Matchers.*;

/**
 * Base class for controller PublicationController Integration Tests.
 * Contains test cases for Publication abstraction.
 */

public abstract class BasePublicationControllerIT extends BaseControllerIT {

    protected void testCreatePublication_fail(Content content, List<String> errorFields) throws Exception {
        RestAssured.given()
            .contentType(JSON)
            .body(objectMapper.writeValueAsString(new PublicationDTO()))
        .when()
            .post(publicationBase + "/create").prettyPeek()
        .then()
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .body("errors.field", containsInAnyOrder("content", "title", "author"));

        RestAssured.given()
            .contentType(JSON)
            .body(objectMapper.writeValueAsString(PublicationDTO.builder().content(content).build()))
        .when()
            .post(publicationBase + "/create").prettyPeek()
        .then()
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .body("errors.field", containsInAnyOrder(errorFields.toArray()));
    }

    protected void testPublicationList_success(Content content, int numberOfItemsWithTopic) throws Exception {
        RestAssured.given()
            .parameter("content", content.toString())
            .and().parameter("page", 0)
            .and().parameter("size", 2)
        .when()
            .get(publicationBase + "/list").prettyPeek()
        .then()
            .statusCode(HttpStatus.OK.value())
            .body("entries.findAll { it.id > 0 }.size()", is(2))
            .body("entries.content", hasItems(content.toString()))
            .body("entries.findAll { it.title != null }.size()", is(2))
            .body("entries.findAll { it.author != null }.size()", is(2))
            .body("entries.findAll { it.topic != null }.size()", is(numberOfItemsWithTopic));
    }

    protected void testFindPublication_success(PublicationDTO publicationDTO, Matcher<Object> topicMatcher) throws Exception {
        final Long id = createAndVerifyPublication(publicationDTO, topicMatcher);

        RestAssured.given()
            .parameter("content", publicationDTO.getContent().toString())
        .when()
            .get(publicationBase + "/{publication_id}", id).prettyPeek()
        .then()
            .statusCode(HttpStatus.OK.value())
            .body("id", notNullValue())
            .body("content", is(publicationDTO.getContent().toString()));
    }

    protected void testFindPublication_fail(PublicationDTO publicationDTO, Matcher<Object> topicMatcher) throws Exception {
        final Long id = createAndVerifyPublication(publicationDTO, topicMatcher);

        RestAssured.given()
            .parameter("content", publicationDTO.getContent().toString())
        .when()
            .get(publicationBase + "/{publication_id}", id * 10000).prettyPeek()
        .then()
            .statusCode(HttpStatus.NOT_FOUND.value());

        RestAssured.given()
            .parameter("content", "")
        .when()
            .get(publicationBase + "/{publication_id}", id).prettyPeek()
        .then()
            .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    protected void testDeletePublication_success(PublicationDTO publicationDTO, Matcher<Object> topicMatcher) throws Exception {
        final Long id = createAndVerifyPublication(publicationDTO, topicMatcher);

        RestAssured.given()
            .contentType(JSON)
            .body(objectMapper.writeValueAsString(PublicationRequestDTO.builder()
                .publicationId(id)
                .content(publicationDTO.getContent())
                .build()))
        .when()
            .delete(publicationBase).prettyPeek()
        .then()
            .statusCode(HttpStatus.NO_CONTENT.value());
    }

    protected void testDeletePublication_fail(PublicationDTO publicationDTO, Matcher<Object> topicMatcher) throws Exception {
        final Long id = createAndVerifyPublication(publicationDTO, topicMatcher);

        RestAssured.given()
            .contentType(JSON)
            .body(objectMapper.writeValueAsString(PublicationRequestDTO.builder()
               .publicationId(id * 10000)
               .content(null)
               .build()))
        .when()
            .delete(publicationBase).prettyPeek()
        .then()
            .statusCode(HttpStatus.BAD_REQUEST.value());

        RestAssured.given()
            .contentType(JSON)
            .body(objectMapper.writeValueAsString(PublicationRequestDTO.builder()
                .publicationId(null)
                .content(publicationDTO.getContent())
                .build()))
        .when()
            .delete(publicationBase).prettyPeek()
        .then()
            .statusCode(HttpStatus.BAD_REQUEST.value());

        RestAssured.given()
            .contentType(JSON)
            .body(objectMapper.writeValueAsString(PublicationRequestDTO.builder()
                 .publicationId(id * 10000)
                 .content(publicationDTO.getContent())
                 .build()))
        .when()
            .delete(publicationBase).prettyPeek()
        .then()
            .statusCode(HttpStatus.NOT_FOUND.value());
    }
}
