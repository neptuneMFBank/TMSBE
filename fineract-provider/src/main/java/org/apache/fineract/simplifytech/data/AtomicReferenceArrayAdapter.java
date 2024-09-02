/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.simplifytech.data;


import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonParseException;
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