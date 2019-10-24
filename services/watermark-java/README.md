Watermark [![Build Status](https://github.com/VladimirYushkevich/test-tasks/workflows/watermark-java/badge.svg)](https://github.com/VladimirYushkevich/test-tasks/actions?workflow=watermark-java)
=
### Description:

Watermark-service is a Spring boot application.
It uses in memory h2database in the persistence layer with appropriate domain model. Database populated during start up via
corresponding **DataLoader** (and **TestDataLoader** for test profile)
All Watermark requests handled asynchronously by **WatermarkController** (Spring **DeferredResult** + rx **Observable**). 
For CRUD operations with publications(book and journals) sync **WatermarkController** has been created.

Watermark processing done by mocked WatermarkClient with configured time delay via Hystrix Command.
Currently it takes 30 sec. See *watermark.client.delayInMilliseconds* in **application.properties** file.

It is not allowed to update Publication during Watermark creation.

#### Watermark-Test

A global publishing company that publishes books and journals wants to develop a service to
watermark their documents. Book publications include topics in business, science and media. Journals
don’t include any specific topics. A document (books, journals) has a title, author and a watermark
property. An empty watermark property indicates that the document has not been watermarked yet.

The watermark service has to be asynchronous. For a given content document the service should
return a ticket, which can be used to poll the status of processing. If the watermarking is finished the
document can be retrieved with the ticket. The watermark of a book or a journal is identified by
setting the watermark property of the object. For a book the watermark includes the properties
content, title, author and topic. The journal watermark includes the content, title and author.

#### Examples for watermarks:
```
{content:”book”, title:”The Dark Code”, author:”Bruce Wayne”, topic:”Science”}
{content:”book”, title:”How to make money”, author:”Dr. Evil”, topic:”Business”}
{content:”journal”, title:”Journal of human flight routes”, author:”Clark Kent”}
```

#### TODO
a) Create an appropriate object-oriented model for the problem.<br />
b) Implement the Watermark-Service, meeting the above conditions.<br />
c) Provide Unit-Tests to ensure the functionality of the service.

### Test coverage
[JaCoCo plugin](https://docs.gradle.org/current/userguide/jacoco_plugin.html#sec:jacoco_report_configuration) is used 
to verify minimum test coverage and see reports. Since we are using unit and integration test there are two independent
coverage limits are used in *build.gradle* configuration.
The production build uses following steps:
`run unit tests`->`verify coverage limit`->`run it tests`->`verify coverage limit`
Of course is possible to run unit tests only with coverage:
`./gradlew clean test jacocoTestReport jacocoTestCoverageVerification`

### Run service:
```
./gradlew build integrationTest jacocoTestReport jacocoTestCoverageVerification && java -jar build/libs/watermark-0.0.1-SNAPSHOT.jar
```
Some tests are a bit time consuming (total time ~ 1min), to speed up launch:
```
./gradlew clean build -x test && java -jar build/libs/watermark-0.0.1-SNAPSHOT.jar
```
You can also run it from docker image (than port should be 8081 in examples):
```
docker-compose up
docker-compose down
```

### Usage:

[In memory DB console](http://localhost:8080/h2-console)  
[SWAGGER](http://localhost:8080/swagger-ui.html)

Or if you prefer CLI:
```
curl localhost:8080/publication/1?content=BOOK | jq
```
```
curl POST localhost:8080/publication/create -d '{"content": "BOOK", "title": "bookTitle", "author": "bookAuthor", "topic": "BUSINESS"}' -H 'Content-Type: application/json' | jq
```
```
curl POST localhost:8080/watermark -d '{"publicationId": 5, "content": "BOOK"}' -H 'Content-Type: application/json' | jq
```
```
curl localhost:8080/watermark/eb849f71-cadf-4084-b85d-a588a6143479 | jq
```
```
curl -X GET --header 'Accept: application/json' 'http://localhost:8080/publication/list?content=BOOK&page=0&size=2&sort=author' | jq
```

### Environment

macOS Sierra (version 10.12.1)  
Java(TM) SE Runtime Environment (build 1.8.0_172-b11 )