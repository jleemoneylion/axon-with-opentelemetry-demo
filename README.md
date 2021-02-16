## What is this about?

1. A demonstration of OpenTelemetry instrumentation of an Java application with Axon and SQS components.

## Prerequisite

1. Create a NewRelic Insights API Key
2. Copy the file `.env.example` and name it as `.env.dev` and replace the API key with your own. `.env.dev` has been excluded from the VCS.

## How to Run the Application

1. Build it

```shell
mvn clean install -DskipTests
```

2. Run it

```shell
docker-compose --env-file .env.dev up --build
```

3. Clean up (Optional)

```shell
docker-compose down
```