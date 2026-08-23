
#pre-build stage
FROM gradle:8.10.2-jdk-21-and-22 AS pre-build
WORKDIR /opt/naeayedea/keith/
COPY settings.gradle .
COPY build.gradle .
COPY common ./common
COPY core/build.gradle ./core/
COPY discord/build.gradle ./discord/
RUN gradle dependencies --refresh-dependencies

#build stage
FROM pre-build AS build
WORKDIR /opt/naeayedea/keith/core/
COPY core/src /opt/naeayedea/keith/core/src
RUN gradle bootJar
RUN find /opt/naeayedea/keith/core/build/* -not -regex '.*libs.*' -delete
RUN rm -rf /opt/naeayedea/keith/core/src
RUN rm /opt/naeayedea/keith/core/build.gradle

# Package stage
FROM amazoncorretto:21 AS package
ENV JAR_NAME=keith-core.jar
ENV APP_HOME=/opt/naeayedea/keith/core/
WORKDIR $APP_HOME
COPY --from=build $APP_HOME .
ENTRYPOINT ["java", "-jar", "/opt/naeayedea/keith/core/build/libs/keith-core.jar"]
