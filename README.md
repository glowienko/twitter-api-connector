# Twitter API consumer


## Tools used:

+ **Spring Boot with Reactive Web** - reactive webclient for stream API handling, dependency injection, properties, etc.
+ **Kotlin Coroutines** - API fetching non blocking way
+ **Resilience4j** - Twitter complex retry policy handling, also other features if needed
+ **Wiremock** - API integration testing - separate integrationTest profile and package
+ **Micrometer** - tweets fetching related metrics gathering
+ **Jib plugin** - generating secure, layered and bullet-proof docker image (I've also provided a Dockerfile as a second option)
+ **Gradle** - building whole application
+ **Docker** - application can be run as a docker container

## How to run the application ##

Startup script is in build-and-run.sh file.
It basically creates a docker image and runs it.
You need to pass correct environment variables, especially:

+ -e TWITTER_ACCESS_TOKEN=access-token \
+ -e TWITTER_ACCESS_TOKEN_SECRET=token-secret \