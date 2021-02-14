package com.example.axonsample.infrastructure.objectmapper;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class ObjectMapperUtils {
    private static ObjectMapper objectMapper;

    public static ObjectMapper instance() {
        if (objectMapper == null) {
            synchronized (ObjectMapperUtils.class) {
                objectMapper = new ObjectMapper()
                        .registerModule(new Jdk8Module())
                        .registerModule(new JavaTimeModule())
                        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            }
        }

        return objectMapper;
    }
}
