# GeoIP

Test app with documentation first approach - Spring controllers & Co are generated from Openapi documentation

Version format is ${lastTag}.${commitDistanceFromLastTag}-${gitShortCommitId}, example: 1.2.3-626c097d03

- lastTag: the last tag like "v@1.0"
- commitDistanceFromLastTag: number of commits from "lastTag" like "9"
- gitShortCommitId: commitId abbreviation like "v"

*hint:* if container labeled with "0.0.0-ABCDEF1234" please make sure you checkout git tags as well

# Run

run ```./gradlew run``` to run locally. App will automatically use `application-local-dev.properties` file, so it is a
good way to provide `app.ipstack.accessKey` for testing. Then open sandbox in browser `http://localhost:8080/`

# Build docker image

To build docker image just run ```./gradlew jibDockerBuild```

# Run Docker

`docker run -p 8888:8080 myregistry/com/github/bademux/geoip:latest`

*note:* by default api sandbox is disabled.

# Packaging helm chart

To package helm chart please run ```./gradlew helmPackage```

# Adding authorization

It should be straightforward, please check https://github.com/bademux/spring-oauth2-resourceserver

# Adding HttpClient status code handling, caches, etc. 

Please see https://github.com/bademux/ghfetcher_apache_client

# Known issues for some cases there can be problem with building the project on windows platform 

The problem happens dues to [the issue](https://github.com/palantir/gradle-git-version/issues/263).
Please replace `version "${lastTag}.${git.commitDistance}-${git.gitHash}" as String` with `version "0.0.0"` and remove invocation of `versionDetails` in `build.gradle`
