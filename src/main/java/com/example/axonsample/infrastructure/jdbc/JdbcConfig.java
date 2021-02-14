package com.example.axonsample.infrastructure.jdbc;

import com.zaxxer.hikari.HikariDataSource;
import org.postgresql.Driver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.sql.SQLException;

@Configuration
public class JdbcConfig {
    @Bean
    public DataSource dataSource(@Value("${db.url}") String dbUrl) throws SQLException {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(Driver.class.getCanonicalName());
        dataSource.setUrl(dbUrl);
        dataSource.setUsername("postgres");
        dataSource.setPassword("postgres");
        HikariDataSource hikariDataSource = new HikariDataSource();
        hikariDataSource.setDataSource(dataSource);
        hikariDataSource.setMaximumPoolSize(10);
        hikariDataSource.setLoginTimeout(10);
        return hikariDataSource;
    }
}
