package com.company.scratchgame.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;

public class JSONUtil {
    private final static ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .configure(SerializationFeature.INDENT_OUTPUT, true);

    public static <T> String convertToJSON(T obj) throws IOException {
        return mapper.writeValueAsString(obj);
    }

    public static <T> T readFromFile(String fileName, Class<T> clazz) throws IOException {
        return mapper.readValue(new File(fileName), clazz);
    }
}
