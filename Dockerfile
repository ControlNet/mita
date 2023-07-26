FROM hseeberger/scala-sbt:8u312_1.6.2_2.13.8
EXPOSE 9000

RUN ln -s /usr/local/openjdk-8/bin/java /usr/bin/java
SHELL ["/bin/bash", "--login", "-c"]

COPY . /opt

WORKDIR /opt
RUN curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.1/install.sh | bash

WORKDIR /opt/ui
RUN nvm install
RUN nvm use
RUN npm install

WORKDIR /opt
RUN sbt stage

ENV MITA_SECRET_KEY=OFVRx8FKVEIyYBALRdMqtfOEhjAFOT5uDsOPuk2HEOCFvKKWVU6dYD
ENV MITA_TOKEN_SECRET=my-32-character-ultra-secure-and-ultra-long-secret
ENV MITA_PASSWORD=123456
ENV MITA_GUEST_PASSWORD=changeit
ENV REACT_APP_PASSWORD=123


ENTRYPOINT ["/bin/bash", "--login", "-c", "./target/universal/stage/bin/mita -Dplay.http.secret.key=${MITA_SECRET_KEY}"]