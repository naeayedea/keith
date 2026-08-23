
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
WORKDIR /opt/naeayedea/keith/discord/
COPY discord/src /opt/naeayedea/keith/discord/src
RUN gradle bootJar
RUN find /opt/naeayedea/keith/discord/build/* -not -regex '.*libs.*' -delete
RUN rm -rf /opt/naeayedea/keith/discord/src
RUN rm /opt/naeayedea/keith/discord/build.gradle

# Package stage
FROM amazoncorretto:21 AS package
ENV JAR_NAME=keith-discord.jar
ENV APP_HOME=/opt/naeayedea/keith/discord/
WORKDIR $APP_HOME
COPY --from=build $APP_HOME .
ENTRYPOINT ["java", "-jar", "/opt/naeayedea/keith/discord/build/libs/keith-discord.jar"]
