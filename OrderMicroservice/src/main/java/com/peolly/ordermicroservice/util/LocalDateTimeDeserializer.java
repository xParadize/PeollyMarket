package com.peolly.ordermicroservice.util;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Component
public class LocalDateTimeDeserializer implements JsonDeserializer<LocalDateTime> {

    /**
     * Deserializes a JSON element into a LocalDateTime object.
     *
     * @param json      the JSON element representing the date-time.
     * @param typeOfT   the type of the object to deserialize to.
     * @param context   the deserialization context.
     * @return LocalDateTime object parsed from the JSON element.
     * @throws JsonParseException if parsing fails or the format is invalid.
     */
    @Override
    public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        return LocalDateTime.parse(json.getAsString(),
                DateTimeFormatter.ofPattern("dd-MM-yyyy'T'HH:mm:ss.SSSSSS").withLocale(Locale.ENGLISH));
    }
}