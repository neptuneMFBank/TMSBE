package org.apache.fineract.simplifytech.data;
import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class AtomicReferenceArrayAdapter implements JsonSerializer<AtomicReferenceArray<?>>, JsonDeserializer<AtomicReferenceArray<?>> {

    @Override
    public JsonElement serialize(AtomicReferenceArray<?> src, Type typeOfSrc, JsonSerializationContext context) {
        JsonArray jsonArray = new JsonArray();
        for (int i = 0; i < src.length(); i++) {
            jsonArray.add(context.serialize(src.get(i)));
        }
        return jsonArray;
    }

    @Override
    public AtomicReferenceArray<?> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonArray jsonArray = json.getAsJsonArray();
        Object[] array = new Object[jsonArray.size()];
        for (int i = 0; i < jsonArray.size(); i++) {
            array[i] = context.deserialize(jsonArray.get(i), Object.class);
        }
        return new AtomicReferenceArray<>(array);
    }
}