package com.example.borrowbuddy.util;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;

public class GsonJavaTimeAdapters {
    public static class InstantTypeAdapter extends TypeAdapter<Instant> {
        @Override
        public void write(JsonWriter out, Instant value) throws IOException {
            out.value(value == null ? null : value.toString());
        }
        @Override
        public Instant read(JsonReader in) throws IOException {
            String s = in.nextString();
            return s == null ? null : Instant.parse(s);
        }
    }
    public static class LocalDateTypeAdapter extends TypeAdapter<LocalDate> {
        @Override
        public void write(JsonWriter out, LocalDate value) throws IOException {
            out.value(value == null ? null : value.toString());
        }
        @Override
        public LocalDate read(JsonReader in) throws IOException {
            String s = in.nextString();
            return s == null ? null : LocalDate.parse(s);
        }
    }
}
