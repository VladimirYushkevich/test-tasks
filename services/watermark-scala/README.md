Watermark [![Build Status](https://github.com/VladimirYushkevich/test-tasks/workflows/watermark-java/badge.svg)](https://github.com/VladimirYushkevich/test-tasks/actions?workflow=watermark-java)
=
### Description:

This is a scala version of watermark application.


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

### Environment

macOS Sierra (version 10.12.6)  
Scala 2.12.2
SBT 0.13.15