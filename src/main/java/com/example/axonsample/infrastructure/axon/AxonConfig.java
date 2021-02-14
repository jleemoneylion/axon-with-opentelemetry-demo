package com.example.axonsample.infrastructure.axon;

import org.axonframework.common.jdbc.DataSourceConnectionProvider;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.tokenstore.jdbc.JdbcTokenStore;
import org.axonframework.eventsourcing.eventstore.EmbeddedEventStore;
import org.axonframework.eventsourcing.eventstore.EventStorageEngine;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.eventsourcing.eventstore.jdbc.JdbcEventStorageEngine;
import org.axonframework.modelling.saga.repository.SagaStore;
import org.axonframework.modelling.saga.repository.jdbc.JdbcSagaStore;
import org.axonframework.serialization.Serializer;
import org.axonframework.spring.config.AxonConfiguration;
import org.axonframework.spring.messaging.unitofwork.SpringTransactionManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.support.JdbcTransactionManager;

import javax.sql.DataSource;

@Configuration
public class AxonConfig {
    @Bean
    public JdbcEventStorageEngine eventStorageEngine(DataSource dataSource,
                                                     @Qualifier("eventSerializer") Serializer serializer) {
        return JdbcEventStorageEngine.builder()
                .connectionProvider(new DataSourceConnectionProvider(dataSource))
                .eventSerializer(serializer)
                .snapshotSerializer(serializer)
                .transactionManager(new SpringTransactionManager(new JdbcTransactionManager(dataSource)))
                .build();
    }

    @Bean
    public EventStore eventStore(EventStorageEngine eventStorageEngine,
                                 AxonConfiguration configuration) {
        return EmbeddedEventStore.builder()
                .storageEngine(eventStorageEngine)
                .messageMonitor(configuration.messageMonitor(EventStore.class, "eventStore"))
                .build();
    }

    @Bean
    public EventBus eventBus(EventStore eventStore) {
        return eventStore;
    }

    @Bean
    public JdbcTokenStore tokenStore(DataSource dataSource,
                                     Serializer serializer) {
        return JdbcTokenStore.builder()
                .connectionProvider(new DataSourceConnectionProvider(dataSource))
                .serializer(serializer)
                .build();
    }

    @Bean
    public SagaStore sagaStore(DataSource dataSource,
                               Serializer serializer) {
        return JdbcSagaStore.builder()
                .connectionProvider(new DataSourceConnectionProvider(dataSource))
                .serializer(serializer)
                .build();
    }
}
