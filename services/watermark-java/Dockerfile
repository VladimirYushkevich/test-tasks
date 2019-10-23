FROM piegsaj/oracle-jre:1.8.0_172-b11
#FROM gradle:5.6.3-jdk8-alpine
WORKDIR /opt/watermark-java/
#ADD --chown=gradle:gradle . /opt/watermark-java
COPY . /opt/watermark-java
RUN ./gradlew build integrationTest jacocoTestReport jacocoTestCoverageVerification