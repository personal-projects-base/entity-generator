package com.gonthera.cli.service.common;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.gonthera.cli.model.Endpoints;
import com.gonthera.cli.model.Entities;
import com.gonthera.cli.model.Enums;
import com.gonthera.cli.model.Authorization;
import com.gonthera.cli.model.Messaging;
import com.gonthera.cli.model.Properties;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ConfigurationFileValidator {

    private ConfigurationFileValidator() {
    }

    public static void validate(Path projectRoot) {
        List<String> errors = new ArrayList<>();
        Path gontheraDirectory = projectRoot.resolve(".gonthera");
        if (!Files.isDirectory(gontheraDirectory)) {
            errors.add(".gonthera directory is required for validation");
            throw new ProjectValidationException(errors);
        }

        validateGontheraDirectory(gontheraDirectory, errors);

        if (!errors.isEmpty()) throw new ProjectValidationException(errors);
    }

    public static void validateForGeneration(Path projectRoot) {
        List<String> errors = new ArrayList<>();
        Path gontheraDirectory = projectRoot.resolve(".gonthera");
        if (Files.isDirectory(gontheraDirectory)) {
            validateGontheraDirectory(gontheraDirectory, errors);
        } else if (Files.isRegularFile(projectRoot.resolve("project.json"))) {
            validateFile(projectRoot.resolve("project.json"), Properties.class, true, errors);
        } else {
            validateFile(projectRoot.resolve("properties.json"), Properties.class, true, errors);
        }

        if (!errors.isEmpty()) throw new ProjectValidationException(errors);
    }

    private static void validateGontheraDirectory(Path gontheraDirectory, List<String> errors) {
        validateFile(gontheraDirectory.resolve("project.json"), Properties.class, true, errors);
        validateFile(gontheraDirectory.resolve("entities.json"), new TypeToken<List<Entities>>() { }.getType(), false, errors);
        validateFile(gontheraDirectory.resolve("endpoints.json"), new TypeToken<List<Endpoints>>() { }.getType(), false, errors);
        validateFile(gontheraDirectory.resolve("enums.json"), new TypeToken<List<Enums>>() { }.getType(), false, errors);
        validateFile(gontheraDirectory.resolve("messaging.json"), Messaging.class, false, errors);
        validateFile(gontheraDirectory.resolve("authorization.json"), Authorization.class, false, errors);
    }

    private static void validateFile(Path file, Type type, boolean required, List<String> errors) {
        if (!Files.isRegularFile(file)) {
            if (required) errors.add(".gonthera/" + file.getFileName() + " is required");
            return;
        }
        try (Reader reader = Files.newBufferedReader(file)) {
            JsonElement json = JsonParser.parseReader(reader);
            validateElement(json, type, file.getFileName().toString(), errors);
        } catch (JsonSyntaxException ex) {
            errors.add(file.getFileName() + " contains invalid JSON: " + ex.getMessage());
        } catch (IOException ex) {
            errors.add("unable to read " + file.getFileName() + ": " + ex.getMessage());
        }
    }

    private static void validateElement(JsonElement json, Type type, String path, List<String> errors) {
        if (json == null || json.isJsonNull()) {
            errors.add(path + " must not be null");
            return;
        }
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            if (parameterizedType.getRawType() == List.class) {
                if (!json.isJsonArray()) {
                    errors.add(path + " must be an array");
                    return;
                }
                Type itemType = parameterizedType.getActualTypeArguments()[0];
                for (int index = 0; index < json.getAsJsonArray().size(); index++) {
                    validateElement(json.getAsJsonArray().get(index), itemType, path + "[" + index + "]", errors);
                }
                return;
            }
        }
        if (!(type instanceof Class)) return;
        Class<?> typeClass = (Class<?>) type;
        if (isScalar(typeClass)) {
            if (!json.isJsonPrimitive()) {
                errors.add(path + " must be a scalar value");
            } else if ((typeClass == boolean.class || typeClass == Boolean.class) && !json.getAsJsonPrimitive().isBoolean()) {
                errors.add(path + " must be a boolean");
            } else if (Number.class.isAssignableFrom(typeClass) && !json.getAsJsonPrimitive().isNumber()) {
                errors.add(path + " must be a number");
            } else if ((typeClass == String.class || typeClass.isEnum()) && !json.getAsJsonPrimitive().isString()) {
                errors.add(path + " must be a string");
            } else if (typeClass.isEnum() && Arrays.stream(typeClass.getEnumConstants())
                    .map(Object::toString)
                    .noneMatch(json.getAsString()::equals)) {
                errors.add(path + " must be one of " + Arrays.toString(typeClass.getEnumConstants()));
            }
            return;
        }
        if (!json.isJsonObject()) {
            errors.add(path + " must be an object");
            return;
        }

        Map<String, Type> fields = jsonFields(typeClass);
        JsonObject object = json.getAsJsonObject();
        for (Map.Entry<String, JsonElement> property : object.entrySet()) {
            Type propertyType = fields.get(property.getKey());
            if (propertyType == null) {
                errors.add(path + "." + property.getKey() + " is not a recognized property");
            } else {
                validateElement(property.getValue(), propertyType, path + "." + property.getKey(), errors);
            }
        }
    }

    private static Map<String, Type> jsonFields(Class<?> typeClass) {
        Map<String, Type> fields = new HashMap<>();
        for (Field field : typeClass.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) || Modifier.isTransient(field.getModifiers())) continue;
            SerializedName serializedName = field.getAnnotation(SerializedName.class);
            if (serializedName == null) {
                fields.put(field.getName(), field.getGenericType());
            } else {
                fields.put(serializedName.value(), field.getGenericType());
                for (String alternate : serializedName.alternate()) fields.put(alternate, field.getGenericType());
            }
        }
        return fields;
    }

    private static boolean isScalar(Class<?> typeClass) {
        return typeClass.isPrimitive() || typeClass.isEnum() || typeClass == String.class || Number.class.isAssignableFrom(typeClass) || typeClass == Boolean.class;
    }
}
