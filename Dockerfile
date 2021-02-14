FROM fabric8/java-alpine-openjdk11-jre:1.8.0
RUN apk add --no-cache nss

WORKDIR /opt

COPY ./target/axon-with-opentelemetry-demo-0.0.1-SNAPSHOT.jar axon-opentelemetry-demo.jar
COPY ./lib/opentelemetry-javaagent-all.jar opentelemetry-javaagent-all.jar
COPY ./lib/opentelemetry-exporters-newrelic-auto-0.13.1.jar opentelemetry-exporters-newrelic-auto-0.13.1.jar

ENV NEWRELIC_API_KEY ""
ENV PORT ""

CMD ["/bin/sh", "-c", "java -javaagent:opentelemetry-javaagent-all.jar -Dotel.exporter.jar=opentelemetry-exporters-newrelic-auto-0.13.1.jar -Dnewrelic.api.key=$NEWRELIC_API_KEY -Dnewrelic.service.name=axon-opentelemetry-demo -jar axon-opentelemetry-demo.jar --server.port=$PORT"]