FROM openjdk:8
ENV SBT_VERSION 1.2.8

RUN \
  curl -L -o sbt-$SBT_VERSION.deb http://dl.bintray.com/sbt/debian/sbt-$SBT_VERSION.deb && \
  dpkg -i sbt-$SBT_VERSION.deb && \
  rm sbt-$SBT_VERSION.deb && \
  apt-get update && \
  apt-get install sbt && \
  sbt sbtVersion

WORKDIR ./services/watermark-scala
ADD target/scala-2.13/watermark-scala-assembly-0.1.0-SNAPSHOT.jar watermark.jar
ENTRYPOINT ["java","-jar","watermark.jar"]