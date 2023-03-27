FROM eclipse-temurin:17-jdk-alpine as build

WORKDIR /workspace/app
COPY . /workspace/app
RUN ./gradlew clean build -x test
RUN mkdir -p build/libs/dependency && (cd build/libs/dependency; jar -xf ../*.jar)

FROM eclipse-temurin:17-jre-alpine
RUN addgroup -S dockerfileUser && adduser -S dockerfileUser -G dockerfileUser
USER dockerfileUser


ARG TWITTER_CONSUMER_KEY
ENV TWITTER_CONSUMER_KEY=${TWITTER_CONSUMER_KEY}

ARG TWITTER_CONSUMER_SECRET
ENV TWITTER_CONSUMER_SECRET=${TWITTER_CONSUMER_SECRET}

ARG TWITTER_ACCESS_TOKEN
ENV TWITTER_ACCESS_TOKEN=${TWITTER_ACCESS_TOKEN}

ARG TWITTER_ACCESS_TOKEN_SECRET
ENV TWITTER_ACCESS_TOKEN_SECRET=${TWITTER_ACCESS_TOKEN_SECRET}

ARG DEPENDENCY=/workspace/app/build/libs/dependency
VOLUME /tmp

COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=build ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /app
ENTRYPOINT ["java", "-cp","app:app/lib/*","com.patrykglow.twitterconnector.ApplicationKt"]