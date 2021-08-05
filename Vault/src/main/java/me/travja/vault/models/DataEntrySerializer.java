package me.travja.vault.models;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class DataEntrySerializer extends StdSerializer<DataEntry> {

    public DataEntrySerializer() {
        this(null);
    }

    public DataEntrySerializer(Class<DataEntry> t) {
        super(t);
    }

    @Override
    public void serialize(DataEntry value, JsonGenerator jgen, SerializerProvider serializerProvider) throws IOException {
        jgen.writeStartObject();
        jgen.writeStringField("id", value.getId());
        jgen.writeStringField("username", value.getUsername());
        jgen.writeStringField("password", value.getPassword());
        jgen.writeStringField("url", value.getUrl());
        jgen.writeEndObject();
    }
}
