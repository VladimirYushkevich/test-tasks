package com.yushkevich.watermark.java.controller;

import com.jayway.restassured.RestAssured;
import com.yushkevich.watermark.java.client.WatermarkClient;
import com.yushkevich.watermark.java.domain.Content;
import com.yushkevich.watermark.java.domain.Watermark;
import com.yushkevich.watermark.java.dto.PublicationDTO;
import com.yushkevich.watermark.java.dto.PublicationRequestDTO;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;

import java.util.UUID;

import static com.jayway.restassured.http.ContentType.JSON;
import static com.yushkevich.watermark.java.domain.Watermark.Status.*;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.reset;

public abstract class BaseWatermarkControllerIT extends BaseControllerIT {

    @MockBean
    private WatermarkClient watermarkClient;

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    void testWatermarkPublicationAsync_success(PublicationDTO publicationDTO, Content content,
                                               Matcher<Object> topicMatcher) throws Exception {
        //given
        delayWatermarkClient(500L, false);
        //when
        Long publicationId1 = createAndVerifyPublication(publicationDTO, topicMatcher);
        Long publicationId2 = createAndVerifyPublication(publicationDTO, topicMatcher);
        //then
        triggerWatermarkCreationAndVerifyTicketId(publicationId1, content);
        triggerWatermarkCreationAndVerifyTicketId(publicationId2, content);
    }

    void testWatermarkTicketStatusFlow_success_updateAllowedAfterSuccess(PublicationDTO publicationDTO, Content content,
                                                                         Matcher<Object> topicMatcher) throws Exception {
        //given
        delayWatermarkClient(500L, false);

        Long publicationId = createAndVerifyPublication(publicationDTO, topicMatcher);
        final UUID ticketId = triggerWatermarkCreationAndVerifyTicketId(publicationId, content);
        pollAndVerifyTicketStatus(ticketId, PENDING, nullValue());
        Thread.sleep(500L);
        pollAndVerifyTicketStatus(ticketId, SUCCESS, notNullValue());
        //when
        final PublicationDTO publicationToUpdate = PublicationDTO.builder().content(content).author("newAuthor").build();
        publicationToUpdate.setId(publicationId);
        testUpdatePublication_success(resolvePublicationDTO(publicationToUpdate.getContent()), publicationToUpdate, topicMatcher);
        //then
        pollAndVerifyTicketStatus(ticketId, NEW, nullValue());
    }

    void testWatermarkTicketStatusFlow_success_updateAllowedAfterFail(PublicationDTO publicationDTO, Content content,
                                                                      Matcher<Object> topicMatcher) throws Exception {
        //given
        delayWatermarkClient(500L, true);

        Long publicationId = createAndVerifyPublication(publicationDTO, topicMatcher);
        final UUID ticketId = triggerWatermarkCreationAndVerifyTicketId(publicationId, content);
        pollAndVerifyTicketStatus(ticketId, PENDING, nullValue());
        Thread.sleep(500L);
        pollAndVerifyTicketStatus(ticketId, FAILED, nullValue());
        //when
        final PublicationDTO publicationToUpdate = PublicationDTO.builder().content(content).author("newAuthor").build();
        publicationToUpdate.setId(publicationId);
        testUpdatePublication_success(resolvePublicationDTO(publicationToUpdate.getContent()), publicationToUpdate, topicMatcher);
        //then
        pollAndVerifyTicketStatus(ticketId, NEW, nullValue());
    }

    void testWatermarkTicketStatusFlow_fail_updateNotAllowed(PublicationDTO publicationDTO, Content content,
                                                             Matcher<Object> topicMatcher) throws Exception {
        //given
        delayWatermarkClient(500L, true);

        Long publicationId = createAndVerifyPublication(publicationDTO, topicMatcher);
        final UUID ticketId = triggerWatermarkCreationAndVerifyTicketId(publicationId, content);
        pollAndVerifyTicketStatus(ticketId, PENDING, nullValue());
        //when
        final PublicationDTO publicationToUpdate = PublicationDTO.builder().content(content).author("newAuthor").build();
        publicationToUpdate.setId(publicationId);
        testUpdatePublication_fail(publicationToUpdate);
    }

    private void pollAndVerifyTicketStatus(UUID ticketId, Watermark.Status status, Matcher<Object> documentMatcher) throws Exception {
        RestAssured.when()
                .get(watermarkBase + "/{ticket_id}", ticketId).prettyPeek()
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("id", is(ticketId.toString()))
                .body("status", is(status.toString()))
                .body("document", documentMatcher);
    }

    private UUID triggerWatermarkCreationAndVerifyTicketId(Long publicationId, Content content) throws Exception {
        return RestAssured.given()
                .contentType(JSON)
                .body(objectMapper.writeValueAsString(PublicationRequestDTO.builder()
                        .publicationId(publicationId)
                        .content(content)
                        .build()))
                .when()
                .post(watermarkBase).prettyPeek()
                .then()
                .statusCode(HttpStatus.OK.value())
                .body(not(""))
                .extract()
                .as(UUID.class);
    }

    private void delayWatermarkClient(long timeout, boolean isFailed) {
        doAnswer(invocation -> {
            Thread.sleep(timeout);
            if (isFailed) {
                throw new RuntimeException("Watermark client failed");
            }
            return "watermarkIT";
        }).when(watermarkClient).createWatermark(any());
    }

    @After
    public void tearDown() throws Exception {
        reset(watermarkClient);
        //allow main thread to write everything in log
        Thread.sleep(1000L);
    }
}
