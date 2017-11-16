
FROM openjdk:8-alpine

MAINTAINER Sergii Maksymenko "sergey.maksymenko@gmail.com"


#Install utils
RUN apk add --update \
      bash \
      curl \
    && rm -rf /var/cache/apk/*

# solve issue with libnative-platform.so
RUN apk update && apk add libstdc++ && rm -rf /var/cache/apk/*

WORKDIR /usr/app
COPY ./build/libs/docker_api.jar /usr/app

EXPOSE 8080

CMD ["java", "-jar", "./docker_api.jar"]