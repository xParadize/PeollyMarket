package com.peolly.ordermicroservice.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class LocalDateTimeSerializer implements JsonSerializer<LocalDateTime> {

    /**
     * Serializes a LocalDateTime object into a JSON element.
     *
     * @param src        the LocalDateTime object to serialize.
     * @param typeOfSrc  the actual type of the source object.
     * @param context    the serialization context.
     * @return JsonElement representing the serialized LocalDateTime.
     */
    @Override
    public JsonElement serialize(LocalDateTime src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.format(DateTimeFormatter.ofPattern("dd-MM-yyyy'T'HH:mm:ss.SSSSSS")));
    }
}