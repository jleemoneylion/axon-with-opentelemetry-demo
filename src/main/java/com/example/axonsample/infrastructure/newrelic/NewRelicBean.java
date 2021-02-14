//package com.example.axonsample.infrastructure.newrelic;
//
//import com.newrelic.telemetry.opentelemetry.export.NewRelicExporters;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//
//import javax.annotation.PreDestroy;
//
//@Component
//public class NewRelicBean {
//    public NewRelicBean(@Value("${newrelic.api-key}") String apiKey,
//                        @Value("${newrelic.service-name}") String serviceName) {
//        NewRelicExporters.start(new NewRelicExporters.Configuration(apiKey, serviceName)
//                .enableAuditLogging()
//                .collectionIntervalSeconds(10));
//    }
//
//    @PreDestroy
//    public void destroy() {
//        NewRelicExporters.shutdown();
//    }
//}
