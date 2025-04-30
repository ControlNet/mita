FROM hseeberger/scala-sbt:8u312_1.6.2_2.13.8 AS builder

RUN ln -s /usr/local/openjdk-8/bin/java /usr/bin/java

COPY . /opt
WORKDIR /opt/ui

# Install Node.js 18 using NodeSource
RUN apt-get update && \
    apt-get install -y curl gnupg && \
    curl -fsSL https://deb.nodesource.com/setup_18.x | bash - && \
    apt-get install -y nodejs && \
    npm install && \
    npm run build

WORKDIR /opt
RUN sbt stage

FROM openjdk:8-jre-alpine

RUN apk add --no-cache bash

WORKDIR /app
COPY --from=builder /opt/target/universal/stage /app/
COPY --from=builder /opt/public /app/public
EXPOSE 9000
ENV MITA_SECRET_KEY=OFVRx8FKVEIyYBALRdMqtfOEhjAFOT5uDsOPuk2HEOCFvKKWVU6dYD
ENV MITA_TOKEN_SECRET=my-32-character-ultra-secure-and-ultra-long-secret
ENV MITA_PASSWORD=123456
ENV MITA_GUEST_PASSWORD=changeit
ENV REACT_APP_PASSWORD=123
ENTRYPOINT ["/app/bin/mita", "-Dplay.http.secret.key=${MITA_SECRET_KEY}"]
