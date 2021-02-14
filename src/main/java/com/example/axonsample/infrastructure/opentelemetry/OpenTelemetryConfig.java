package com.example.axonsample.infrastructure.opentelemetry;

import com.newrelic.telemetry.opentelemetry.export.NewRelicSpanExporter;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.config.EventProcessingConfigurer;
import org.axonframework.messaging.correlation.CorrelationDataProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenTelemetryConfig {
    @Bean
    public Tracer tracer() {
        return GlobalOpenTelemetry.getTracer("axon-opentelemetry-demo");
    }

    @Bean
    public ContextPropagators contextPropagators() {
        return GlobalOpenTelemetry.getPropagators();
    }

    @Bean
    public BatchSpanProcessor spanProcessor(@Value("${newrelic.api-key}") String apiKey) {
        System.out.println("API_KEY = " + apiKey);
        return BatchSpanProcessor.builder(NewRelicSpanExporter.newBuilder()
                .apiKey(apiKey)
                .enableAuditLogging()
                .build())
                .setScheduleDelayMillis(1_000)
                .build();
    }

    @Autowired
    public void configureOpenTelemetrySdk(BatchSpanProcessor spanProcessor) {
        OpenTelemetrySdk.getGlobalTracerManagement().addSpanProcessor(spanProcessor);
    }

    @Bean
    public OpenTelemetryDispatchInterceptor openTelemetryDispatchInterceptor(ContextPropagators contextPropagators) {
        return new OpenTelemetryDispatchInterceptor(contextPropagators);
    }

    @Bean
    public OpenTelemetryHandlerInterceptor openTelemetryHandlerInterceptor(Tracer tracer, ContextPropagators contextPropagators) {
        return new OpenTelemetryHandlerInterceptor(tracer, contextPropagators);
    }

    @Bean
    public CommandGateway openTelemetryCommandGateway(Tracer tracer, CommandBus commandBus) {
        return new OpenTelemetryCommandGateway(tracer, commandBus);
    }

    @Bean
    public CorrelationDataProvider openTelemetryCorrelationDataProvider(ContextPropagators contextPropagators) {
        return new OpenTelemetryCorrelationDataProvider(contextPropagators);
    }

    @Autowired
    public void configureEventHandler(EventProcessingConfigurer eventProcessingConfigurer,
                                      OpenTelemetryHandlerInterceptor openTelemetryHandlerInterceptor) {
        eventProcessingConfigurer.registerDefaultHandlerInterceptor(
                (configuration, name) -> openTelemetryHandlerInterceptor);
    }
}
