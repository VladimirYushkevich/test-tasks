FROM piegsaj/oracle-jre:1.8.0_172-b11
WORKDIR ./services/watermark-java
ADD /build/libs/watermark-0.0.1-SNAPSHOT.jar watermark.jar
ENTRYPOINT ["java","-jar","watermark.jar"]