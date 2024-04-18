package ru.yandex.practicum.filmorate.model.deserializers;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ru.yandex.practicum.filmorate.Exception.IncorrectParameterException;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class GenreDeserializer extends JsonDeserializer<List<Integer>> {
    @Override
    public List<Integer> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        List<Integer> genreIds = new ArrayList<>();
        while (!jsonParser.isClosed()) {
            JsonToken jsonToken = jsonParser.nextToken();
            if (JsonToken.FIELD_NAME.equals(jsonToken)) {
                if (!jsonParser.getCurrentName().equals("id")) {
                    throw new IncorrectParameterException("Неверный формат поля жанр");
                }
                jsonToken = jsonParser.nextToken();
                genreIds.add(jsonParser.getValueAsInt());
            }
        }
        return genreIds;
    }
}
