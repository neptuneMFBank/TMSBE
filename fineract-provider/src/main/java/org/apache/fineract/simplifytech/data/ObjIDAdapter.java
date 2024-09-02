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

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.rmi.server.ObjID;

public class ObjIDAdapter extends TypeAdapter<ObjID> {

    @Override
    public void write(JsonWriter out, ObjID value) throws IOException {
        // Serialize ObjID as a JSON object with a single field "objNum"
        out.beginObject();
        out.name("objNum").value(value.toString()); // Serializing ObjID as its string representation
        out.endObject();
    }

    @Override
    public ObjID read(JsonReader in) throws IOException {
        // Deserialize ObjID from its string representation
        in.beginObject();
        String objNum = null;
        while (in.hasNext()) {
            String name = in.nextName();
            if (name.equals("objNum")) {
                objNum = in.nextString();
            } else {
                in.skipValue();
            }
        }
        in.endObject();

        // Construct ObjID from its string representation if available, else create a default ObjID
        return objNum != null ? new ObjID(Integer.parseInt(objNum)) : new ObjID();
    }
}

