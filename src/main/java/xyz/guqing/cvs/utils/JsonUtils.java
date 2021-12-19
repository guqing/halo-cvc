package xyz.guqing.cvs.utils;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.difflib.patch.Patch;
import java.io.IOException;

/**
 * @author guqing
 * @date 2021-12-19
 */
public class JsonUtils {

    private static final ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper();
    }

    public static String objectToString(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T jsonStringToObject(String json, Class<T> t) {
        try {
            return objectMapper.readValue(json, t);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T jsonStringToObject(String json, TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private static class PatchJsonDeserializer extends JsonDeserializer<Patch<String>> {

        @Override
        public Patch<String> deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JacksonException {
            ObjectCodec oc = jp.getCodec();
            JsonNode node = oc.readTree(jp);
            final Long id = node.get("id").asLong();
            final String name = node.get("name").asText();
            final String contents = node.get("contents").asText();
            final long ownerId = node.get("ownerId").asLong();
//            User user = new User();
//            user.setId(ownerId);
//            return new Program(id, name, contents, user);
            return null;
        }
    }
}
